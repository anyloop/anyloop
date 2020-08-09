/*
 * Main.java
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

package com.github.anyloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.anyloop.chassis.Configurator;
import com.github.anyloop.chassis.ConfigurableRunnable;
import com.github.anyloop.chassis.DefaultConfigurator;

/**
 * The main class of the AnyLoop program.
 *
 * @author https://github.com/tom65536
 * @since 0.1.0
 *
 */
public final class Main {
    /**
     * The logger for this class.
     */
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Disable constructor. This class is purely static.
     */
    private Main() { }

    /**
     * The entry point of the application.
     *
     * @param args the command line arguments
     *
     * @since 0.1.0
     */
    public static void main(final String[] args) {

        logger.debug("Program started");

        DefaultConfigurator configurator = new DefaultConfigurator(args);
        configurator.run(new ConfigurableRunnable() {

                @Override
                public void init(final Configurator c) { }

                @Override
                public void run() {
                    System.out.println(getName() + " " + getVersion());
                    System.out.println("Licensed under the EUPL");
                }

                @Override
                public void terminate() { }

                @Override
                public synchronized String getVersion() {
                    String version = null;
                    Package aPackage = Main.class.getPackage();
                    if (aPackage != null) {
                        version = aPackage.getImplementationVersion();

                        if (version == null) {
                            version = aPackage.getSpecificationVersion();
                        }
                    }
                    return version;
                }

                @Override
                public synchronized String getName() {
                    String title = null;
                    Package aPackage = Main.class.getPackage();
                    if (aPackage != null) {
                        title = aPackage.getImplementationTitle();

                        if (title == null) {
                            title = aPackage.getSpecificationTitle();
                        }
                    }
                    return title;
                }
            });
    }
}
