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
package org.shredzone.commons.taglib;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.shredzone.commons.taglib.proxy.ProxiedTag;

/**
 * Utility class for taglib beans.
 *
 * @author Richard "Shred" Körber
 */
public final class TaglibUtils {

    private TaglibUtils() {}

    /**
     * Finds an ancestor tag of the given type. Starting from the given Tag, it traverses
     * up the parent tags until a tag of the given type is found.
     * <p>
     * Use this method instead of {@link TagSupport#findAncestorWithClass(Tag, Class)}, as
     * it is aware of proxied tag classes, while findAncestorWithClass only sees the
     * proxy instances instead of the tag classes behind it. Furthermore, this method is
     * also able to locate interfaces.
     *
     * @param <T>
     *            Type to find and return
     * @param from
     *            Tag to start from
     * @param type
     *            Type to find
     * @return Ancestor of that type, or {@code null} if none was found.
     */
    @SuppressWarnings("unchecked")
    public static <T> T findAncestorWithType(Tag from, Class<T> type) {
        Tag parent = from.getParent();
        while (parent != null) {
            if (parent instanceof ProxiedTag) {
                parent = ((ProxiedTag<Tag>) parent).getTargetBean();
            }
            if (type.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

}
