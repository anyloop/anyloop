/*
 * ConfigurationTest.java
 * 
 * Copyright 2020 Thomas Reiter
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This test checks the CLI interface and configuration files.
 * 
 * @author Thomas Reiter <https://github.com/tom65536>
 * @since 0.1.0
 */
public class ConfigurationTest {
    
    private final ByteArrayOutputStream outContent =
        new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent =
        new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

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

