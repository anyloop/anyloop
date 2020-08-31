/*
 * DefaultConfigurator.java
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.XMLPropertiesConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.VFSFileSystem;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration2.tree.OverrideCombiner;

import org.apache.commons.io.FilenameUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up a configuration from the configuration files and
 * the command line.
 *
 * The command line accepts the following arguments:
 * <dl>
 *   <dd><code>-h, --help</code></dd><dt>prints a help message and exits</dt>
 *   <dd><code>-v, --version</code></dd><dt>prints the version and exits</dt>
 *   <dd><code>-c, --config</code></dd><dt>adds a given file or other source
 *      to the set of configuration files.</dt>
 *   <dd><code>-D, --define</code></dd><dt>defines a property directly at the
 *      command line</dt>
 * </dl>
 *
 * The parser accepts multiple <code>-c</code> options. The last option
 * has the highest priority, i.e. if <code>-c file1 -c file2</code> is
 * given at the command line, where both files define a property of the
 * same name, the value of <code>file2</code> is taken as configuration value.
 *
 * @since 0.1.0
 * @author https://github.com/tom65536
 */
public final class DefaultConfigurator implements Configurator {
    /**
     * The command line arguments passed to the program.
     */
    private final String[] args;

    /**
     * The configuration derived from the command line.
     */
    private BaseHierarchicalConfiguration config = null;

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DefaultConfigurator.class);

    /**
     * Mapping from file extensions to file based configuration classes.
     */
    private static final Map<String, Supplier<FileBasedConfiguration>>
        EXTENSIONS = getExtensionMapping();

    /**
     * A lock object.
     */
    private static final Object LOCK = new Object();

    /**
     * The static data for this class.
     */
     private static final Properties PROPERTIES =
        ClassHelper.getClassPathProperties(DefaultConfigurator.class);

    /**
     * Creates a new instance of the {@link DefaultConfigurator} class.
     *
     * @param arguments the command line arguments passed to the program.
     */
    public DefaultConfigurator(final String[] arguments) {
        this.args = java.util.Arrays.copyOf(arguments, arguments.length);
    }

    @Override
    public void run(final ConfigurableRunnable runnable) {
        try {
            synchronized (LOCK) {
                if (this.config == null) {
                    this.config = createConfigurationFromCommandLine(
                        this.args,
                        runnable);
                }
            }
        } catch (ConfigurationException exp) {
            LOGGER.error("Configuration failed", exp);
            return;
        }

        if (this.config == null) {
            LOGGER.debug("No configuration set");
            return;
        }

        this.config.getKeys().forEachRemaining((String key) ->
            LOGGER.debug("KEY: " + key));

        runnable.init(this);
        runnable.run();
        runnable.terminate();
    }

    @Override
    public <T> T create(final Class<T> clazz) {
        final InvocationHandler handler = new ConfiguratorHandler(
            this.config);
        return (T) Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class[] {clazz},
            handler);
    }

    /**
     * Builds a configuration from a list of command line arguments.
     *
     * @param arguments the list of command line arguments
     * @param runnable the configurable runnable, used here for name and
     *        version information only.
     * @return a configuration derived from the command line arguments
     *
     * @since 0.1.0
     */
    private static BaseHierarchicalConfiguration
        createConfigurationFromCommandLine(
            final String[] arguments,
            final ConfigurableRunnable runnable) throws ConfigurationException {

        final Options options = setupOptions();
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(options, arguments);

            if (cmd.hasOption(
                    PROPERTIES.getProperty("Option.help.short"))) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(runnable.getName(), options);
                return null;
            }

            if (cmd.hasOption(
                    PROPERTIES.getProperty("Option.version.short"))) {
                System.out.println(getVersionInformation(runnable));
                return null;
            }

            final String[] configs = cmd.getOptionValues(
                PROPERTIES.getProperty("Option.config.short"));

            final Properties props = cmd.getOptionProperties(
                PROPERTIES.getProperty("Option.define.short"));

            return createConfiguration(configs, props);
        } catch (ParseException exp) {
            throw new ConfigurationException(
                PROPERTIES.getProperty("Message.cli_not_understood"),
                exp);
        }

    }

    /**
     * Builds a configuration from a list of configuration file names
     * and a set of properties.
     *
     * The configuration file names must be given in the order such
     * that the latter file names override the properties of the earlier
     * names. The properties of the second argument have the highest
     * precedence, i.e. override all properties given in the files.
     *
     * @param configFileNames the names of a list configuration files
     * @param cliProperties properties defined at the command line
     *
     * @return a configuration combining all configuration files and
     *         the properties.
     */
    private static BaseHierarchicalConfiguration
        createConfiguration(
            final String[] configFileNames,
            final Properties cliProperties) throws ConfigurationException {
        final CombinedConfiguration result = new CombinedConfiguration(
            new OverrideCombiner());

        if (configFileNames != null) {
            for (int i = configFileNames.length - 1; i >= 0; --i) {
                final String fileName = configFileNames[i];
                final String ext = FilenameUtils.getExtension(fileName)
                    .toLowerCase();

                if (EXTENSIONS.containsKey(ext)) {
                    FileBasedConfiguration fb = EXTENSIONS.get(ext).get();
                    FileHandler fh = new FileHandler(fb);
                    fh.setFileSystem(new VFSFileSystem());
                    try {
                        fh.load(fileName);
                    } catch (org.apache.commons.configuration2.ex.
                            ConfigurationException ex) {
                        throw new ConfigurationException(
                            String.format(
                                    PROPERTIES.getProperty(
                                    "Message.config_not_loaded"),
                                fileName),
                            ex);
                    }
                    result.addConfiguration(fb, fileName);
                } else {
                    throw new ConfigurationException(
                        String.format(
                            PROPERTIES.getProperty(
                                "Message.format_not_supported"),
                            ext
                    ));
                }
            }
        }

        result.addConfiguration(new MapConfiguration(cliProperties),
            "cli");

        return result;
    }

    /**
     * Generates a version message for output.
     *
     * This method combines the runnable's name and version into
     * a single line output.
     *
     * @param runnable the runnable of which the version information
     *  should be displayed.
     *
     * @return a String of the form <code>name - version </code>
     */
    private static String getVersionInformation(
            final ConfigurableRunnable runnable) {
        return runnable.getName() + " - " + runnable.getVersion();
    }

    /**
     * Creates the command line options for this program.
     *
     * @return the newly created options.
     */
    public static Options setupOptions() {
        final Options options = new Options();

        final Option property = Option.builder(
                PROPERTIES.getProperty("Option.define.short"))
            .longOpt(PROPERTIES.getProperty("Option.define.long"))
            .argName(PROPERTIES.getProperty("Option.define.argname"))
            .desc(PROPERTIES.getProperty("Option.define.description"))
            .hasArgs()
            .numberOfArgs(2)
            .valueSeparator()
            .build();

        final Option help = Option.builder(
                PROPERTIES.getProperty("Option.help.short"))
            .longOpt(PROPERTIES.getProperty("Option.help.long"))
            .desc(PROPERTIES.getProperty("Option.help.description"))
            .build();

        final Option version = Option.builder(
                PROPERTIES.getProperty("Option.version.short"))
            .longOpt(PROPERTIES.getProperty("Option.version.long"))
            .desc(PROPERTIES.getProperty("Option.version.description"))
            .build();

        final Option configs = Option.builder(
                PROPERTIES.getProperty("Option.config.short"))
            .longOpt(PROPERTIES.getProperty("Option.config.long"))
            .argName(PROPERTIES.getProperty("Option.config.argname"))
            .desc(PROPERTIES.getProperty("Option.config.description"))
            .hasArgs()
            .numberOfArgs(1)
            .build();

        options.addOption(property);
        options.addOption(help);
        options.addOption(version);
        options.addOption(configs);

        return options;
    }

    private static Map<String, Supplier<FileBasedConfiguration>>
            getExtensionMapping() {
        Map<String, Supplier<FileBasedConfiguration>> result = new HashMap<>();

        result.put("ini", () -> new INIConfiguration());
        result.put("properties", () -> new PropertiesConfiguration());
        result.put("json", () -> new JSONConfiguration());
        result.put("plist", () -> new PropertyListConfiguration());
        result.put("xml", () -> new XMLConfiguration());
        result.put("xproperties", () -> new XMLPropertiesConfiguration());
        result.put("xplist", () -> new XMLPropertyListConfiguration());
        result.put("yaml", () -> new YAMLConfiguration());
        result.put("yml", () -> new YAMLConfiguration());

        return Collections.unmodifiableMap(result);
    }
}
