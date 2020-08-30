/*
 * ClassHelperTest.java
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test checks the {@link ClassHelper} class.
 * 
 * @author ttps://github.com/tom65536
 * @since 0.1.0
 */
public class ClassHelperTest {

    /**
     * Regular expression for a version number according to the
     * Semantic Versioning 2.0.0 convention.
     * 
     * @see https://semver.org/spec/v2.0.0.html
     */
    public static final String SEMVER_REGEX =
        "^([0-9]+)\\.([0-9]+)\\.([0-9]+)"
        + "(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?"
        + "(?:\\+[0-9A-Za-z-]+)?$";

    /**
     * Checks whether {@see ClassHelper#getVersion()} works as
     * expected.
     */
    @Test
    public void testGetVersion() {
        final String version = ClassHelper.getVersion(Test.class);
        
        assertNotNull(version);
        assertTrue(version.matches(SEMVER_REGEX));
    }
    
    /**
     * Checks whether {@see ClassHelper#getName()} works as
     * expected.
     */
    @Test
    public void testGetName() {
        final String name = ClassHelper.getName(Test.class);
        
        assertNotNull(name);
        assertEquals("junit-jupiter-api", name);
    }

}
