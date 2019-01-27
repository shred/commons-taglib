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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * A bean that stores the parameters of a tag attribute. This bean is immutable once it is
 * created.
 *
 * @author Richard "Shred" Körber
 */
@ParametersAreNonnullByDefault
@Immutable
public class AttributeBean implements Comparable<AttributeBean> {

    private final String name;
    private final String type;
    private final boolean required;
    private final boolean rtexprvalue;

    /**
     * Creates and initializes a new {@link AttributeBean}.
     *
     * @param name
     *            Attribute name
     * @param type
     *            Attribute type
     * @param required
     *            {@code true}: The attribute is required
     * @param rtexprvalue
     *            {@code true} The attribute is a rtexpression value
     */
    public AttributeBean(String name, String type, boolean required, boolean rtexprvalue) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.rtexprvalue = rtexprvalue;
    }

    public @Nonnull String getName()        { return name; }

    public @Nonnull String getType()        { return type; }

    public boolean isRequired()             { return required; }

    public boolean isRtexprvalue()          { return rtexprvalue; }

    /**
     * {@inheritDoc}
     * <p>
     * Two {@link AttributeBean} are considered equal if they have an equal name.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AttributeBean)) {
            return false;
        }
        return ((AttributeBean) obj).getName().equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(AttributeBean o) {
        return name.compareTo(o.name);
    }

}
