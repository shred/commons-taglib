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

import javax.annotation.Nonnull;
import javax.servlet.jsp.tagext.JspTag;

/**
 * This interface marks the Tag class as proxied tag, and gives access to the target tag
 * implementation.
 *
 * @param <T>
 *            Type of the {@link JspTag} this proxy delegates to
 * @author Richard "Shred" Körber
 */
public interface ProxiedTag<T extends JspTag> {

    /**
     * Returns a reference to the target bean this proxy delegates to.
     *
     * @return Target bean
     */
    @Nonnull T getTargetBean();

}
