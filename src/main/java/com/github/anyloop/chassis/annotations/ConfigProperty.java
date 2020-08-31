/*
 * ConfigProperty.java
 *
 * Copyright 2020 Thomas Reiter
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 *
 */
package com.github.anyloop.chassis.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * This annotation marks a method of an interface as a getter
 * of a configuration value.
 *
 * A dynamic implementation of the marked method is filled by
 * the properties denoted by the given path.
 *
 * @author https://github.com/tom65536
 * @since 0.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigProperty {

    /**
     * An expression which denotes the property in
     * the configuration.
     *
     * The value must start with a period if you want to denote
     * a property relative to a parent. Public paths must not start
     * with a period.
     *
     * @return the pat.h to the property.
     */
    String value();
}

