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
 * Annotates a package to be a tag library.
 *
 * @author Richard "Shred" Körber
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface TagLib {

    /**
     * Tag library version. Required.
     */
    String tlibversion();

    /**
     * JSP version. Defaults to "1.1".
     */
    String jspversion() default "1.1";

    /**
     * Short name of the tag library. Required.
     */
    String shortname();

    /**
     * Tag library URI. Optional.
     */
    String uri() default "";

    /**
     * Name of the TLD file. Defaults to "META-INF/taglib.tld".
     */
    String tld() default "META-INF/taglib.tld";

}
