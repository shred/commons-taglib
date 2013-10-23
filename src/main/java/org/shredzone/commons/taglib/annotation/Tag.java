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
package org.shredzone.commons.taglib.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.TryCatchFinally;

/**
 * Annotates a Tag implementation class.
 *
 * @author Richard "Shred" Körber
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Tag {

    /**
     * Class type of the Tag.
     * <p>
     * Currently these types are supported:
     * <ul>
     *   <li>javax.servlet.jsp.tagext.Tag</li>
     *   <li>javax.servlet.jsp.tagext.IterationTag</li>
     *   <li>javax.servlet.jsp.tagext.BodyTag</li>
     *   <li>javax.servlet.jsp.tagext.SimpleTag</li>
     * </ul>
     */
    Class<? extends JspTag> type();

    /**
     * Name of the tag. Will be derived from the class name if none is given.
     */
    String name() default "";

    /**
     * Body content of the tag. Defaults to "JSP".
     */
    String bodycontent() default "JSP";

    /**
     * A custom spring bean name. Optional.
     */
    String bean() default "";

    /**
     * Does the tag class implements the {@link TryCatchFinally} implementation?
     */
    boolean tryCatchFinally() default false;

}
