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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.TryCatchFinally;

/**
 * This bean stores all information about a tag. Parts of this bean are immutable.
 *
 * @author Richard "Shred" Körber
 */
public class TagBean implements Comparable<TagBean> {

    private Map<String, AttributeBean> attributes = new HashMap<String, AttributeBean>();

    private final String name;
    private final String className;
    private final String bodycontent;
    private final String typeClass;
    private String info;
    private String proxyClassName;
    private String beanFactoryReference;
    private String beanName;
    private boolean tryCatchFinally;

    /**
     * Creates and initializes a new {@link TagBean}.
     *
     * @param name
     *            Tag name
     * @param className
     *            Name of the implementing class
     * @param bodycontent
     *            Body Content flag
     * @param typeClass
     *            Tag type class
     */
    public TagBean(String name, String className, String bodycontent, String typeClass) {
        this.name = name;
        this.className = className;
        this.bodycontent = bodycontent;
        this.typeClass = typeClass;
    }

    public String getName()                     { return name; }

    public String getClassName()                { return className; }

    public String getBodycontent()              { return bodycontent; }

    public String getType()                     { return typeClass; }

    /**
     * Information about the tag.
     */
    public String getInfo()                     { return info; }
    public void setInfo(String info)            { this.info = info; }

    /**
     * Name of the proxy class.
     */
    public String getProxyClassName()           { return proxyClassName; }
    public void setProxyClassName(String proxyClassName) { this.proxyClassName = proxyClassName; }

    /**
     * A reference to the bean factory.
     */
    public String getBeanFactoryReference()     { return beanFactoryReference; }
    public void setBeanFactoryReference(String beanFactoryReference) { this.beanFactoryReference = beanFactoryReference; }

    /**
     * Name of the bean.
     */
    public String getBeanName()                 { return beanName; }
    public void setBeanName(String beanName)    { this.beanName = beanName; }

    /**
     * Does the tag class implement the {@link TryCatchFinally} interface?
     */
    public boolean isTryCatchFinally()          { return tryCatchFinally; }
    public void setTryCatchFinally(boolean tryCatchFinally) { this.tryCatchFinally = tryCatchFinally; }

    /**
     * Adds a tag attribute to the tag bean.
     *
     * @param attribute
     *            {@link AttributeBean} of the tag attribute
     */
    public void addAttribute(AttributeBean attribute) {
        if (attributes.containsKey(attribute.getName())) {
            throw new ProcessorException("Tag " + name + ": parameter " + attribute.getName() + " already defined");
        }
        attributes.put(attribute.getName(), attribute);
    }

    /**
     * Gets all tag attributes.
     *
     * @return Collection of {@link AttributeBean} of all tag attributes
     */
    public Collection<AttributeBean> getAttributes() {
        return attributes.values();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Two {@link TagBean} are considered equal when their names are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TagBean)) return false;
        return ((TagBean) obj).getName().equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(TagBean o) {
        return name.compareTo(o.name);
    }

}
