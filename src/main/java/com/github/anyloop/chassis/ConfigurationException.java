/*
 * ConfigurationException.java
 *
 * Copyright 2020 Thomas Reiter <tom65536@web.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
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

package com.github.anyloop.chassis;

/**
 * This exception is raised when the program cannot be configured
 * successfully.
 *
 * @since 0.1.0
 *
 */
public class ConfigurationException extends Exception {

    /**
     * Constructs a new {@link ConfigurationException} with specified
     * detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause that triggered this exception
     *
     * @since 0.1.0
     */
	public ConfigurationException (
            final String message,
            final Throwable cause) {
        super(message, cause);
	}

    /**
     * Constructs a new {@link ConfigurationException} with specified
     * detail message.
     *
     * @param message the detail message
     *
     * @since 0.1.0
     */
    public ConfigurationException (final String message) {
        super(message);
    }
}

