/*
 * Shredzone Commons
 *
 * Copyright (C) 2012 Richard "Shred" Körber
 *   http://commons.shredzone.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.shredzone.commons.taglib.processor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.shredzone.commons.taglib.annotation.BeanFactoryReference;
import org.shredzone.commons.taglib.annotation.Tag;
import org.shredzone.commons.taglib.annotation.TagInfo;
import org.shredzone.commons.taglib.annotation.TagLib;
import org.shredzone.commons.taglib.annotation.TagParameter;
import org.shredzone.commons.taglib.proxy.BodyTagProxy;
import org.shredzone.commons.taglib.proxy.IterationTagProxy;
import org.shredzone.commons.taglib.proxy.SimpleTagProxy;
import org.shredzone.commons.taglib.proxy.TagProxy;
import org.springframework.util.StringUtils;

/**
 * A javac processor that scans for tag annotations and creates proxy classes that allows
 * to use Spring in tag library implementations.
 *
 * @author Richard "Shred" Körber
 */
@SupportedAnnotationTypes("org.shredzone.commons.taglib.annotation.*")
@ParametersAreNonnullByDefault
public class TaglibProcessor extends AbstractProcessor {

    private static final Map<String, String> PROXY_MAP = new HashMap<>();
    private static final Pattern METHOD_PATTERN = Pattern.compile("^set([^(]+)\\((.+?)\\)$");

    static {
        PROXY_MAP.put(javax.servlet.jsp.tagext.Tag.class.getName(), TagProxy.class.getName());
        PROXY_MAP.put(IterationTag.class.getName(), IterationTagProxy.class.getName());
        PROXY_MAP.put(BodyTag.class.getName(), BodyTagProxy.class.getName());
        PROXY_MAP.put(SimpleTag.class.getName(), SimpleTagProxy.class.getName());
    }

    private TaglibBean taglib;
    private boolean taglibSet = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        taglib = new TaglibBean();

        try {
            for (Element e : roundEnv.getElementsAnnotatedWith(Tag.class)) {
                processTag(e);
            }

            for (Element e : roundEnv.getElementsAnnotatedWith(TagInfo.class)) {
                processTagInfo(e);
            }

            for (Element e : roundEnv.getElementsAnnotatedWith(BeanFactoryReference.class)) {
                processBeanFactoryReference(e);
            }

            for (Element e : roundEnv.getElementsAnnotatedWith(TagParameter.class)) {
                processTagParameter(e);
            }

            for (Element e : roundEnv.getElementsAnnotatedWith(TagLib.class)) {
                processTagLib(e);
            }

            if (!taglib.getTags().isEmpty()) {
                for (TagBean tag : taglib.getTags()) {
                    generateProxyClass(tag);
                }
                generateTaglibTld(taglib.getTldName());
            }

        } catch (ProcessorException | IOException ex) {
            Logger.getLogger(TaglibProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            Messager messager = processingEnv.getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
            return false;

        } finally {
            taglib = null;
            taglibSet = false;
        }

        return true;
    }

    /**
     * Processes a {@link Tag} annotation.
     *
     * @param element
     *            Program element with that tag
     */
    private void processTag(Element element) {
        Tag tagAnno = element.getAnnotation(Tag.class);
        String className = element.toString();
        String tagName = computeTagName(tagAnno.name(), className);

        // Try to evaluate the class name of the tag type
        String tagTypeClass = null;
        try {
            Class<? extends JspTag> tagType = tagAnno.type();
            tagTypeClass = tagType.getName();
        } catch(MirroredTypeException ex) {
            // This is a hack, see http://forums.sun.com/thread.jspa?threadID=791053
            Logger.getLogger(TaglibProcessor.class.getName()).log(Level.FINE, "use type mirror", ex);
            tagTypeClass = ex.getTypeMirror().toString();
        }

        if (!PROXY_MAP.containsKey(tagTypeClass)) {
            throw new ProcessorException("No proxy for tag type " + tagTypeClass);
        }

        TagBean tag = new TagBean(tagName, className, tagAnno.bodycontent(), tagTypeClass);
        tag.setProxyClassName(className + "Proxy");

        if (StringUtils.hasText(tagAnno.bean())) {
            tag.setBeanName(tagAnno.bean());
        } else {
            tag.setBeanName(StringUtils.uncapitalize(StringUtils.unqualify(className)));
        }

        tag.setTryCatchFinally(tagAnno.tryCatchFinally());

        taglib.addTag(tag);
    }

    /**
     * Processes a {@link TagInfo} annotation.
     *
     * @param element
     *            Program element with that tag
     */
    private void processTagInfo(Element element) {
        TagInfo tagAnno = element.getAnnotation(TagInfo.class);

        if (element.getKind().equals(ElementKind.PACKAGE)) {
            taglib.setInfo(tagAnno.value());
            return;
        }

        String className = element.toString();

        TagBean tag = taglib.getTagForClass(className);
        if (tag == null) {
            throw new ProcessorException("Missing @Tag on class: " + className);
        }

        tag.setInfo(tagAnno.value());
    }

    /**
     * Processes a {@link BeanFactoryReference} annotation.
     *
     * @param element
     *            Program element with that tag
     */
    private void processBeanFactoryReference(Element element) {
        BeanFactoryReference tagAnno = element.getAnnotation(BeanFactoryReference.class);

        if (element.getKind().equals(ElementKind.PACKAGE)) {
            if (taglib.getBeanFactoryReference() != null) {
                throw new ProcessorException("Package @BeanFactoryReference already defined");
            }

            taglib.setBeanFactoryReference(tagAnno.value());
            return;
        }

        String className = element.toString();
        TagBean tag = taglib.getTagForClass(className);
        if (tag == null) {
            throw new ProcessorException("Missing @Tag on class: " + className);
        }

        tag.setBeanFactoryReference(tagAnno.value());
    }

    /**
     * Processes a {@link TagLib} annotation.
     *
     * @param element
     *            Program element with that tag
     */
    private void processTagLib(Element element) {
        if (taglibSet) {
            throw new ProcessorException("@TagLib already defined");
        }

        TagLib tagAnno = element.getAnnotation(TagLib.class);

        taglib.setShortname(tagAnno.shortname());
        taglib.setTlibversion(tagAnno.tlibversion());
        taglib.setTldName(tagAnno.tld());

        if (StringUtils.hasText(tagAnno.jspversion())) {
            taglib.setJspversion(tagAnno.jspversion());
        }

        if (StringUtils.hasText(tagAnno.uri())) {
            taglib.setUri(tagAnno.uri());
        }

        taglibSet = true;
    }

    /**
     * Processes a {@link TagParameter} annotation.
     *
     * @param element
     *            Program element with that tag
     */
    private void processTagParameter(Element element) {
        TagParameter tagAnno = element.getAnnotation(TagParameter.class);
        String methodName = element.toString();
        String className = element.getEnclosingElement().toString();

        TagBean tag = taglib.getTagForClass(className);
        if (tag == null) {
            throw new ProcessorException("Missing @Tag on class: " + className);
        }

        Matcher m = METHOD_PATTERN.matcher(methodName);
        if (!m.matches()) {
            throw new ProcessorException("@TagParameter must be used on a setter method: " + methodName);
        }

        String attrName = StringUtils.uncapitalize(m.group(1));
        String attrType = m.group(2);

        if (attrType.indexOf(',') >= 0) {
            throw new ProcessorException("@TagParameter setter only allows one parameter: " + methodName);
        }

        AttributeBean attr = new AttributeBean(attrName, attrType, tagAnno.required(), tagAnno.rtexprvalue());
        tag.addAttribute(attr);
    }

    /**
     * Computes the name of a tag. If there was a name given in the annotation, it will be
     * used. Otherwise, a name is derived from the class name of the tag class, with a
     * "Tag" suffix removed.
     *
     * @param annotation
     *            Tag name, as given in the annotation
     * @param className
     *            Name of the tag class
     * @return Name of the tag
     */
    private @Nonnull String computeTagName(String annotation, String className) {
        String result = annotation;
        if (!StringUtils.hasText(result)) {
            result = StringUtils.unqualify(className);
            if (result.endsWith("Tag")) {
                result = result.substring(0, result.length() - 3);
            }
            result = StringUtils.uncapitalize(result);
        }
        return result;
    }

    /**
     * Generates a proxy class that connects to Spring and allows all Spring features like
     * dependency injection in the implementing tag class.
     *
     * @param tag
     *            {@link TagBean} that describes the tag.
     * @throws IOException
     *             when the generated Java code could not be saved.
     */
    private void generateProxyClass(TagBean tag) throws IOException {
        String beanFactoryReference = tag.getBeanFactoryReference();
        if (beanFactoryReference == null) {
            beanFactoryReference = taglib.getBeanFactoryReference();
        }

        JavaFileObject src = processingEnv.getFiler().createSourceFile(tag.getProxyClassName());

        String packageName = null;
        int packPos = tag.getClassName().lastIndexOf('.');
        if (packPos >= 0) {
            packageName = tag.getClassName().substring(0, packPos);
        }

        String proxyClass = PROXY_MAP.get(tag.getType());

        try (PrintWriter out = new PrintWriter(src.openWriter())) {
            if (packageName != null) {
                out.printf("package %s;", packageName).println();
                out.println();
            }

            out.print("@javax.annotation.Generated(\"");
            out.print(TaglibProcessor.class.getName());
            out.println("\")");

            out.printf("public class %s extends %s<%s> %s {",
                    StringUtils.unqualify(tag.getProxyClassName()),
                    proxyClass,
                    tag.getClassName(),
                    tag.isTryCatchFinally() ? "implements javax.servlet.jsp.tagext.TryCatchFinally" : ""
            ).println();

            if (beanFactoryReference != null) {
                out.println("  protected org.springframework.beans.factory.BeanFactory getBeanFactory(javax.servlet.jsp.JspContext jspContext) {");
                out.printf(
                        "    java.lang.Object beanFactory = jspContext.findAttribute(\"%s\");",
                        beanFactoryReference
                ).println();
                out.println("    if (beanFactory == null) {");
                out.printf("      throw new java.lang.NullPointerException(\"attribute '%s' not set\");", beanFactoryReference).println();
                out.println("    }");
                out.println("    return (org.springframework.beans.factory.BeanFactory) beanFactory;");
                out.println("  }");
            }

            out.println("  protected java.lang.String getBeanName() {");
            out.printf("    return \"%s\";", tag.getBeanName()).println();
            out.println("  }");

            for (AttributeBean attr : new TreeSet<>(tag.getAttributes())) {
                out.printf("  public void set%s(%s _%s) {",
                        StringUtils.capitalize(attr.getName()),
                        attr.getType(),
                        attr.getName()
                ).println();

                out.printf("    getTargetBean().set%s(_%s);",
                        StringUtils.capitalize(attr.getName()),
                        attr.getName()
                ).println();

                out.println("  }");
            }

            out.println("}");
        }
    }

    /**
     * Generates a TLD file for the tag library.
     *
     * @param tldfile
     *            name of the TLD file to be generated
     * @throws IOException
     *             when the generated TLD file could not be saved.
     */
    private void generateTaglibTld(String tldfile) throws IOException {
        FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", tldfile);
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(file.openOutputStream(), "UTF-8"))) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<!DOCTYPE taglib PUBLIC \"-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN\" \"http://java.sun.com/j2ee/dtds/web-jsptaglibrary_1_1.dtd\">");
            out.println("<!-- Generated file, do not edit! -->");
            out.println("<taglib>");
            out.printf("  <tlibversion>%s</tlibversion>", taglib.getTlibversion()).println();
            out.printf("  <jspversion>%s</jspversion>", taglib.getJspversion()).println();
            out.printf("  <shortname>%s</shortname>", taglib.getShortname()).println();
            out.printf("  <uri>%s</uri>", escapeXml(taglib.getUri())).println();
            out.printf("  <info>%s</info>", escapeXml(taglib.getInfo())).println();

            for (TagBean tag : new TreeSet<>(taglib.getTags())) {
                out.println("  <tag>");
                out.printf("    <name>%s</name>", tag.getName()).println();
                out.printf("    <tagclass>%s</tagclass>", tag.getProxyClassName()).println();
                out.printf("    <bodycontent>%s</bodycontent>", tag.getBodycontent()).println();
                if (tag.getInfo() != null) {
                    out.printf("    <info>%s</info>", escapeXml(tag.getInfo())).println();
                }

                for (AttributeBean attr : new TreeSet<>(tag.getAttributes())) {
                    out.println("    <attribute>");
                    out.printf("      <name>%s</name>", attr.getName()).println();
                    out.printf("      <required>%s</required>", String.valueOf(attr.isRequired())).println();
                    out.printf("      <rtexprvalue>%s</rtexprvalue>", String.valueOf(attr.isRtexprvalue())).println();
                    out.println("    </attribute>");
                }

                out.println("  </tag>");
            }

            out.println("</taglib>");
        }
    }

    /**
     * Escapes a string so it can be used in XML.
     *
     * @param text
     *            String to be escaped
     * @return Escaped text
     */
    private static @Nonnull String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
    }

}
