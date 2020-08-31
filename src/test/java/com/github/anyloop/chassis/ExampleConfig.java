/*
 * ExampleConfig.java
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
 * alon with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 *
 */

package com.github.anyloop.chassis;

import com.github.anyloop.chassis.annotations.ConfigProperty;
import com.github.anyloop.chassis.annotations.DefaultValue;

/**
 * Example configuration objects for our unit tests.
 *
 * For the sake of simplicity the dependent configuration objects
 * are implemented as inner interfaces.
 *
 * @since 0.1.0
 * @author https://github.com/tom65536/
 */
public interface ExampleConfig {

    /**
     * Configuration object for dependencies
     */
    public interface Dependency {
        /**
         * The group ID
         *
         * @return the group ID
         */
        @ConfigProperty(".groupId")
        String getGroupId() throws ConfigurationException;

        /**
         * The artifact ID
         *
         * @return the artifact ID
         */
        @ConfigProperty(".artifactId")
        String getArtifactId() throws ConfigurationException;

        /**
         * The version as a string
         *
         * @return the version
         */
        @ConfigProperty(".version")
        String getVersion() throws ConfigurationException;
    }

    /**
     * The version of the configuration.
     *
     * @return the version as a string.
     */
    @ConfigProperty("version")
    @DefaultValue("unknown")
	String getVersion() throws ConfigurationException;

    /**
     * The versions of the configuration as an arry but only a single
     * value is provided.
     *
     * @return the versions as a list of strings.
     */
    @ConfigProperty("version")
	String[] getVersions() throws ConfigurationException;
        
    /**
     * Tries to convert the version to an integer and is doomed to fail.
     * 
     * @return never
     * @throws ConfigurationException because the value cannot converted
     */
    @ConfigProperty("version")
    int getVersionAsInt() throws ConfigurationException;

    /**
     * List of dependencies.
     *
     * @return a list of dependencies
     */
    @ConfigProperty("dependencies.dependency")
    Dependency[] getDependencies() throws ConfigurationException;

    /**
     * Some Interface valued property which is not in an array.
     */
    @ConfigProperty("artifact_info")
    Dependency getArtifactInfo() throws ConfigurationException;

    /**
     * Some Interface valued property which is not in an array
     * but should be returned as an array.
     */
    @ConfigProperty("artifact_info")
    Dependency[] getArtifactInfos() throws ConfigurationException;
    
    /**
     * Some Interface valued property which is not in an array,
     * with a default value (which is not allowed).
     * 
     * @return never returns
     * @throws ConfigurationException because a default value is not
     *     allowed for interface valued properties.
     */
    @ConfigProperty("artifact_info")
    @DefaultValue("foo:bar:1.0")
    Dependency getDefaultArtifactInfo() throws ConfigurationException;
    
    /**
     * Some integer value.
     *
     * @return an integer value
     */
    @ConfigProperty("hash")
    @DefaultValue("1")
    int getHash() throws ConfigurationException;

    /**
     * The same integer value again.
     *
     * @return the same integer value as before
     */
    @ConfigProperty("hash")
    @DefaultValue("2")
    Integer getHashObject() throws ConfigurationException;

    /**
     * Some missing value with a default value.
     *
     * @return the default value as the configuration value is missing.
     */
    @ConfigProperty("nonexistent_hash")
    @DefaultValue("0x22")
    long getTheDefault() throws ConfigurationException;

    /**
     * Some missing value without a default value.
     *
     * @return the default value as the configuration value is missing.
     * @throws ConfigurationException because the property is not set
     */
    @ConfigProperty("nonexistent_hash")
    long getNoDefault() throws ConfigurationException;
    
    /**
     * Method without {@link ConfigProperty} annotation.
     * 
     * @return never returns because it always fails
     */
    String getNoAnnotation() throws ConfigurationException;
    
    /**
     * Configuration property with array of Strings.
     *
     * @returns a list of Strings
     */
    @ConfigProperty("authors")
    String[] getAuthors();
    
    /**
     * Configuration property with an array of Strings and a
     * default value.
     * 
     * @return never returns
     * @throws ConfigurationException because default values are not
     *    allowed with array valued properties
     */
    @ConfigProperty("authors")
    @DefaultValue("Goofey")
    String[] getDefaultAuthors() throws ConfigurationException;
}

