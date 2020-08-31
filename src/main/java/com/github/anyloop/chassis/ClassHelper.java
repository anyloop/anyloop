/*
 * ClassHelper.java
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

import java.util.Properties;

/**
 * This class provides methods for obtaining information stored in the
 * classpath.
 *
 * @since 0.1.0
 *
 */
public final class ClassHelper {

    /**
     * Hidden constructor.
     *
     * @since 0.1.0
     */
    private ClassHelper() { /* intentionally left blank */ }

    /**
     * Obtains a properties resource from the classpath.
     *
     * @param clazz a class of which the classloader is used for
     *        obtaining the resource
     * @param resourceName the path referring to the resource
     *
     * @return a properties object with the properties loaded from the
     *         resource.
     *
     * @since 0.1.0
     */
    public static Properties getClassPathProperties(
            final Class clazz,
            final String resourceName) {

        Properties result = new Properties();
        try {
            try (java.io.InputStream rstream =
                    clazz.getResourceAsStream(resourceName)) {
                result.load(rstream);
            }
        } catch (java.io.IOException ex) {
            throw new RuntimeException(
                "Could not access resource file '" + resourceName + "'", ex);
        }

        return result;
    }

    /**
     * Obtains a properties resource from the classpath.
     *
     * The resource name is built from the package and class name:
     * <code>org.acme.Foo</code> &rarr; <code>/org/acme/Foo.properties</code>
     *
     * @param clazz a class of which the classloader is used for
     *        obtaining the resource; also used for deriving the resource
     *        name.
     * @return a properties object with the properties loaded from the
     *         resource.
     *
     * @since 0.1.0
     */
    public static Properties getClassPathProperties(final Class clazz) {
        return getClassPathProperties(
            clazz,
            "/" + clazz.getName().replace('.', '/') + ".properties");
    }

    /**
     * Obtains the version of the package containing the given class.
     *
     * @param clazz the class for which the package version is obtained.
     *
     * @return a string representation of the version of the package
     *
     * @since 0.1.0
     */
    public static String getVersion(final Class clazz) {
        String version = null;
        final Package aPackage = clazz.getPackage();
        if (aPackage != null) {
            version = aPackage.getImplementationVersion();

            if (version == null) {
                version = aPackage.getSpecificationVersion();
            }
        }
        return version;
    }

    /**
     * Obtains the name of the package containing the given class.
     *
     * @param clazz the class for which the package name is obtained.
     *
     * @return the name of the package
     *
     * @since 0.1.0
     */
    public static String getName(final Class clazz) {
        String title = null;
        final Package aPackage = clazz.getPackage();
        if (aPackage != null) {
            title = aPackage.getImplementationTitle();

            if (title == null) {
                title = aPackage.getSpecificationTitle();
            }
        }
        return title;
    }
}
