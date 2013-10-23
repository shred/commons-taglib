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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field of the tag implementation as tag parameter. Those fields will be
 * filled with the tag parameters before the tag implementation is invoked.
 *
 * @author Richard "Shred" Körber
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TagParameter {

    /**
     * Parameter name. Will be derived from the field name if none is given.
     */
    String name() default "";

    /**
     * Is the parameter required? Defaults to {@code false}.
     */
    boolean required() default false;

    /**
     * Is the parameter a rtexpression value? Defaults to {@code true}.
     */
    boolean rtexprvalue() default true;

}
