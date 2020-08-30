/*
 * Configurator.java
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

package com.github.anyloop.chassis;

/**
 * A configurator uses a given configuration to derive implementations
 * from decorated interfaces.
 * 
 * @since 0.1.0
 * @author https://github.com/tom65536
 */
public interface Configurator {
    
    /**
     * Interprets the configuration and decides how to invoke
     * the given runnable.
     * 
     * Depending on the configuration and/or the command line arguments
     * the methods of the runnable may not be invoked at all.
     * For example, if an option is called that terminates the program
     * (<tt>--help</tt>, <tt>--version</tt>), the runnable
     * is <b>not</b> started. Otherwise the runnable is
     * initialized, run and eventually terminated in this order.
     * 
     * @param runnable the runnable that is run with this configurator.
     * @since 0.1.0
     */
    void run(ConfigurableRunnable runnable);
    
    /**
     * Creates a dynamic implementation of the given interface
     * with getter methods returning values from the configuration.
     * 
     * @param T implicit generic type of the interface to be implemented
     * @param clazz the interface to be implemented
     * 
     * @return a dynamic proxy implementing the given interface
     * 
     * @since 0.1.0
     */
    <T> T create(Class<T> clazz);
}
