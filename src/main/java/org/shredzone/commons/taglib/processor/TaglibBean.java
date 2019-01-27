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

import javax.annotation.Nonnull;

/**
 * This bean stores all information about a tag library.
 *
 * @author Richard "Shred" Körber
 */
public class TaglibBean {

    private Map<String, TagBean> classTagMap = new HashMap<>();
    private Map<String, TagBean> tags = new HashMap<>();

    private String tlibversion;
    private String jspversion;
    private String shortname;
    private String uri;
    private String info;
    private String tldName;

    private String beanFactoryReference;

    /**
     * The tag lib version.
     */
    public String getTlibversion()                  { return tlibversion; }
    public void setTlibversion(String tlibversion)  { this.tlibversion = tlibversion; }

    /**
     * The JSP version.
     */
    public String getJspversion()                   { return jspversion; }
    public void setJspversion(String jspversion)    { this.jspversion = jspversion; }

    /**
     * The short name of the tag library.
     */
    public String getShortname()                    { return shortname; }
    public void setShortname(String shortname)      { this.shortname = shortname; }

    /**
     * The tag library URI.
     */
    public String getUri()                          { return uri; }
    public void setUri(String uri)                  { this.uri = uri; }

    /**
     * Information about the tag library.
     */
    public String getInfo()                         { return info; }
    public void setInfo(String info)                { this.info = info; }

    /**
     * Name of the TLD file.
     */
    public String getTldName()                      { return tldName; }
    public void setTldName(String tldName)          { this.tldName = tldName; }

    /**
     * A reference to the bean factory.
     */
    public String getBeanFactoryReference()         { return beanFactoryReference; }
    public void setBeanFactoryReference(String beanFactoryReference) { this.beanFactoryReference = beanFactoryReference; }

    /**
     * Adds a {@link TagBean} to this tag library.
     *
     * @param tag
     *            {@link TagBean} to be added
     */
    public void addTag(@Nonnull TagBean tag) {
        if (tags.containsKey(tag.getName())) {
            throw new ProcessorException("Tag '" + tag.getName() + "' already defined");
        }
        tags.put(tag.getName(), tag);
        classTagMap.put(tag.getClassName(), tag);
    }

    /**
     * Gets all tags of this tag library.
     *
     * @return Collection of {@link TagBean}
     */
    public @Nonnull Collection<TagBean> getTags() {
        return tags.values();
    }

    /**
     * Gets the {@link TagBean} that is related to the given implementation class.
     *
     * @param className
     *            Implementation class name
     * @return {@link TagBean} that belongs to that class, or {@code null} if there is
     *         none.
     */
    public TagBean getTagForClass(@Nonnull String className) {
        return classTagMap.get(className);
    }

}
