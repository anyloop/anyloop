/*
 * ConfigurableRunnable.java
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
 * Component that uses a {@link Configurator} to configure itself.
 *
 * @author https://github.com/tom65536
 * @since 0.1.0

 */
public interface ConfigurableRunnable extends Runnable {
    /**
     * Returns the name of the component.
     *
     * @return the name of the component
     *
     * @since 0.1.0
     */
    String getName();

    /**
     * Returns a string representation of the version of the component.
     *
     * @return the version of the component
     *
     * @since 0.1.0
     */
    String getVersion();

    /**
     * Initializes the component.
     *
     * @param configurator the configurator to be used for configuring
     *        this component.
     *
     * @since 0.1.0
     */
    void init(Configurator configurator);

    /**
     * Cleans up after running the component.
     *
     * @since 0.1.0
     */
    void terminate();
}
