/*
 * ConfigurationTest.java
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * This test checks the CLI interface and configuration files.
 *
 * @author https://github.com/tom65536
 * @since 0.1.0
 */
public class ConfigurationTest {

    private final ByteArrayOutputStream outContent =
        new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent =
        new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    /**
     * The logger for this class.
     */
    private static Logger logger =
        LoggerFactory.getLogger(ConfigurationTest.class);

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Checks whether the '--help' and '--version' arguments
     * trigger any of the methods
     * {@link ConfigurableRunnable#init},
     * {@link ConfigurableRunnable#run} or
     * {@link ConfigurableRunnable#terminate}.
     *
     * @since 0.1.0
     */
    @ParameterizedTest
	@ValueSource(strings = {
        "--help",
        "-h",
        "--version",
        "-v",
    })
    public void testHelpAndUsage(String arg) {
        final String[] args = new String[1];
        args[0] = arg;

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable();

        configurator.run(runnable);

        assertFalse(runnable.is_initialized);
        assertFalse(runnable.is_run);
        assertFalse(runnable.is_terminated);
    }

    @Test
    public void testUnknownOption() {
        final String[] args = { "-z" };

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable();

        configurator.run(runnable);

        assertFalse(runnable.is_initialized);
        assertFalse(runnable.is_run);
        assertFalse(runnable.is_terminated);
    }

    @Test
    public void testUnsupportedFormat() {
        final String[] args = { "-c", "res://config.xlsx" };

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable();

        configurator.run(runnable);

        assertFalse(runnable.is_initialized);
        assertFalse(runnable.is_run);
        assertFalse(runnable.is_terminated);
    }

   @Test
    public void testFileNotLoaded() {
        final String[] args = { "-c", "res://rmpftlprmf.yaml" };

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable();

        configurator.run(runnable);

        assertFalse(runnable.is_initialized);
        assertFalse(runnable.is_run);
        assertFalse(runnable.is_terminated);
    }

    /**
     * This test loads a YAML configuration file from the resources
     * and accesses it through the {@link ExampleConfig} interface.
     *
     * @since 0.1.0
     */
    @Test
    public void testLoadYamlConfig() {
        final String[] args = {
            "-c", "res://example-config.yaml"
        };

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable() {
                private ExampleConfig conf;

                @Override
                public void init(final Configurator c) {
                    super.init(c);
                    this.conf = c.create(ExampleConfig.class);
                }

                @Override
                public void run() {
                    super.run();

                    try {
                        assertEquals(this.conf.getVersion(), "2.1");

                        final var versions = this.conf.getVersions();
                        assertEquals(1, versions.length);
                        assertEquals("2.1", versions[0]);

                        assertThrows(ConfigurationException.class,
                            () -> this.conf.getVersionAsInt());

                        final var deps = this.conf.getDependencies();
                        assertEquals(deps.length, 3);

                        assertEquals(deps[0].getGroupId(),
                            "commons-configuration");
                        assertEquals(deps[0].getArtifactId(),
                            "commons-configuration");
                        assertEquals(deps[0].getVersion(), "1.8");

                        assertEquals(deps[1].getGroupId(),
                            "commons-beanutils");
                        assertEquals(deps[1].getArtifactId(),
                            "commons-beanutils");
                        assertEquals(deps[1].getVersion(), "1.8.0");

                        assertEquals(deps[2].getGroupId(),
                            "commons-jxpath");
                        assertEquals(deps[2].getArtifactId(),
                            "commons-jxpath");
                        assertEquals("1.3", deps[2].getVersion());

                        assertEquals(0xA1, conf.getHash());
                        assertEquals(0xA1, conf.getHashObject());
                        assertEquals(0x22, conf.getTheDefault());

                        final String[] authors = conf.getAuthors();

                        assertEquals(3, authors.length);
                        assertEquals("Mickey Mouse", authors[0]);
                        assertEquals("Donald Duck", authors[1]);
                        assertEquals("Daisy Duck", authors[2]);

                        final var info = conf.getArtifactInfo();
                        assertEquals("com.github.anyloop", info.getGroupId());
                        assertEquals("anyloop", info.getArtifactId());
                        assertEquals("0.1.0", info.getVersion());

                        final var infos = conf.getArtifactInfos();
                        assertEquals(1, infos.length);
                        assertEquals("com.github.anyloop",
                            infos[0].getGroupId());
                        assertEquals("anyloop", infos[0].getArtifactId());
                        assertEquals("0.1.0", infos[0].getVersion());

                    } catch (ConfigurationException ex) {
                        fail(ex);
                    }
                }

            };

        configurator.run(runnable);
    }

    @Test
    public void testNoAnnotation() {
        final String[] args = {
            "-c", "res://example-config.yaml"
        };

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable() {
                private ExampleConfig conf;

                @Override
                public void init(final Configurator c) {
                    super.init(c);
                    this.conf = c.create(ExampleConfig.class);
                }

                @Override
                public void run() {
                    super.run();
                    assertThrows(ConfigurationException.class,
                        () -> this.conf.getNoAnnotation());
                }
            };

        configurator.run(runnable);
    }

    @Test
    public void testWrongAnnotation() {
        final String[] args = {
            "-c", "res://example-config.yaml"
        };

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable() {
                private ExampleConfig conf;

                @Override
                public void init(final Configurator c) {
                    super.init(c);
                    this.conf = c.create(ExampleConfig.class);
                }

                @Override
                public void run() {
                    super.run();
                    assertThrows(ConfigurationException.class,
                        () -> this.conf.getDefaultAuthors());

                    assertThrows(ConfigurationException.class,
                        () -> this.conf.getDefaultArtifactInfo());

                    assertThrows(ConfigurationException.class,
                        () -> this.conf.getNoDefault());
                }
            };

        configurator.run(runnable);
    }

    @Test
    public void testMultipleConfigs() {
        final String[] args = {
            "-c", "res://example-config.yaml",
            "-c", "res://overlay-config.xml",
            "-D", "version=0.5.0",
        };

        final DefaultConfigurator configurator = new DefaultConfigurator(args);
        final BaseConfigurableRunnable runnable =
            new BaseConfigurableRunnable() {
                private ExampleConfig conf;

                @Override
                public void init(final Configurator c) {
                    super.init(c);
                    this.conf = c.create(ExampleConfig.class);
                }

                @Override
                public void run() {
                    super.run();

                   try {
                        // overlaid with -D option
                        assertEquals(this.conf.getVersion(), "0.5.0");

                        // overlaid with xml file
                        assertEquals(15, conf.getHash());

                        final String[] authors = conf.getAuthors();

                        assertEquals(3, authors.length);
                        assertEquals("Tick", authors[0]);
                        assertEquals("Trick", authors[1]);
                        assertEquals("Track", authors[2]);

                        final var info = conf.getArtifactInfo();
                        // should not have changed
                        assertEquals("com.github.anyloop", info.getGroupId());
                        assertEquals("anyloop", info.getArtifactId());
                        // overlaid in xml file
                        assertEquals("0.1.1", info.getVersion());
                    } catch (ConfigurationException ex) {
                        fail(ex);
                    }
                }
            };

        configurator.run(runnable);
    }
}

/**
 * Base class implementing those methods of the
 * {@link ConfigurableRunnable} interface, which are not strictly
 * relevant for our tests
 */
class BaseConfigurableRunnable implements ConfigurableRunnable {

    /**
     * Set to true if the {@link #terminate} method has been called.
     */
    public boolean is_terminated = false;

    /**
     * Set to true if the {@link #run} method has been called.
     */
    public boolean is_run = false;

    /**
     * Set to true if the {@link #init} method has been called.
     */
    public boolean is_initialized = false;

    @Override
    public void init(final Configurator c) {
        this.is_initialized = true;
    }

    @Override
    public void terminate() {
        this.is_terminated = true;
    }

    @Override
    public void run() {
        this.is_run = true;
    }

    @Override
    public synchronized String getVersion() {
        return "TEST VERSION";
    }

    @Override
    public synchronized String getName() {
        return "TEST NAME";
    }
}

