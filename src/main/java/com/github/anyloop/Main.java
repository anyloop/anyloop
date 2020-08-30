/*
 * Main.java
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

package com.github.anyloop;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.anyloop.chassis.ClassHelper;
import com.github.anyloop.chassis.Configurator;
import com.github.anyloop.chassis.ConfigurableRunnable;
import com.github.anyloop.chassis.DefaultConfigurator;
import com.github.anyloop.chassis.annotations.ConfigProperty;
import com.github.anyloop.chassis.annotations.DefaultValue;

/**
 * The main class of the AnyLoop program.
 *
 * @author https://github.com/tom65536
 * @since 0.1.0
 *
 */
public final class Main {


    private interface MainConfig {

        @ConfigProperty("debug")
        boolean getDebug();

        @ConfigProperty("jobs")
        @DefaultValue("1")
        int getNumberOfJobs();
    }

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
        Properties properties = ClassHelper.getClassPathProperties(
            Main.class,
            "/com/github/anyloop/COPYING.properties");
        logger.debug("Program started");

        DefaultConfigurator configurator = new DefaultConfigurator(args);
        configurator.run(new ConfigurableRunnable() {

            private MainConfig config;

            @Override
            public void init(final Configurator c) {
                this.config = c.create(MainConfig.class);
            }

            @Override
            public void run() {
                final String notice = String.format(
                    properties.getProperty("copyright"),
                    getName() + " " + getVersion(),
                    properties.getProperty("year"),
                    properties.getProperty("author"));
                System.out.println(notice);

                if (this.config.getDebug()) {
                    logger.debug("This is a debug message");
                }
                logger.info("JOBS = " + this.config.getNumberOfJobs());
            }

            @Override
            public void terminate() { }

            @Override
            public synchronized String getVersion() {
                return ClassHelper.getVersion(Main.class);
            }

            @Override
            public synchronized String getName() {
                return ClassHelper.getName(Main.class);
            }
        });
    }
}
