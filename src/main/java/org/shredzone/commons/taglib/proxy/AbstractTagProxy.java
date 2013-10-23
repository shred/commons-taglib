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
package org.shredzone.commons.taglib.proxy;

import java.lang.reflect.Field;
import java.util.Enumeration;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspTag;

import org.shredzone.commons.taglib.annotation.TagParameter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.FrameworkServlet;

/**
 * An abstract TagProxy implementation that offers all basic methods.
 *
 * @author Richard "Shred" Körber
 */
public abstract class AbstractTagProxy<T extends JspTag> implements JspTag, ProxiedTag<T> {

    public static final String TAGPROXY_BEANFACTORY_CACHE = AbstractTagProxy.class + ".beanFactory";

    private T tagImpl;

    protected abstract String getBeanName();

    /**
     * Creates a new instance of the implementing target bean.
     *
     * @param jspContext
     *            {@link JspContext}
     */
    @SuppressWarnings("unchecked")
    protected void initTargetBean(JspContext jspContext) {
        BeanFactory bf = getBeanFactory(jspContext);

        String beanName = getBeanName();
        if (!bf.isPrototype(beanName)) {
            throw new IllegalStateException("Bean " + beanName + " must be prototype scoped!");
        }

        tagImpl = (T) bf.getBean(beanName);
    }

    /**
     * Gets the {@link BeanFactory} from the given {@link JspContext}. The default
     * implementation automagically finds a {@link BeanFactory} that was previously set by
     * a {@link FrameworkServlet}. The result is cached.
     *
     * @param jspContext
     *            {@link JspContext} to be used
     * @return {@link BeanFactory} found
     */
    @SuppressWarnings("unchecked")
    protected BeanFactory getBeanFactory(JspContext jspContext) {
        Object bfCache = jspContext.getAttribute(TAGPROXY_BEANFACTORY_CACHE, PageContext.APPLICATION_SCOPE);
        if (bfCache != null && bfCache instanceof BeanFactory) {
            return (BeanFactory) bfCache;
        }

        Enumeration<String> en = jspContext.getAttributeNamesInScope(PageContext.APPLICATION_SCOPE);
        while (en.hasMoreElements()) {
            String attribute = en.nextElement();
            if (attribute.startsWith(FrameworkServlet.SERVLET_CONTEXT_PREFIX)) {
                Object bf = jspContext.getAttribute(attribute, PageContext.APPLICATION_SCOPE);
                if (bf != null && bf instanceof BeanFactory) {
                    BeanFactory bfBean = (BeanFactory) bf;
                    jspContext.setAttribute(TAGPROXY_BEANFACTORY_CACHE, bfBean, PageContext.APPLICATION_SCOPE);
                    return bfBean;
                }
            }
        }

        throw new IllegalStateException("Could not find a BeanFactory. Use a FrameworkServlet or @BeanFactoryReference.");
    }

    @Override
    public T getTargetBean() {
        return tagImpl;
    }

    /**
     * Sets a TagParameter annotated parameter in the target implementation.
     *
     * @param name
     *            Parameter name
     * @param value
     *            Parameter value
     */
    protected void setParameter(String name, Object value) {
        boolean fieldSet = false;
        // TODO: getDeclaredFields is not recursive!
        // TODO: create a name/field map on construction time!
        // TODO: also support multiple fields and setter methods!
        for (Field field : tagImpl.getClass().getDeclaredFields()) {
            TagParameter param = field.getAnnotation(TagParameter.class);
            if (param != null && checkName(field, param, name)) {
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, tagImpl, value);
                fieldSet = true;
            }
        }

        if (!fieldSet) {
            throw new IllegalArgumentException("Missing property '" + name + "'");
        }
    }

    /**
     * Checks if the target implementation has a parameter with the given name.
     *
     * @param field
     *            Field to be set
     * @param param
     *            TagParameter describing the parameter
     * @param name
     *            Parameter name
     * @return {@code true} if there is such a parameter.
     */
    private boolean checkName(Field field, TagParameter param, String name) {
        if (StringUtils.hasText(param.name()) && param.name().equals(name))
            return true;
        if (field.getName().equals(name))
            return true;
        return false;
    }

}
