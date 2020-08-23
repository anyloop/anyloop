/*
 * ConfiguratorHandler.java
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

import com.github.anyloop.chassis.annotations.ConfigProperty;
import com.github.anyloop.chassis.annotations.DefaultValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;

/**
 * The invocation handler that actually maps getters to
 * configuration properties.
 *
 * @since 0.1.0
 */
class ConfiguratorHandler implements InvocationHandler {
        
    /**
     * Separator for indicating the boundary between
     * a parent path and its children.
     */
    public static final String PATH_SEPARATOR =
        DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER;

    /**
     * Configuration that supplies the values for the invoked methods.
     * 
     * @since 0.1.0
     */
    private final BaseHierarchicalConfiguration config;
    
    /**
     * The path relative to which relative paths are interpreted.
     */
    private final String path;
    
    /**
     * Creates an invocation handler.
     * 
     * @param theConfig the configuration to be used
     * @param thePath the path relative to which node names are interpreted
     *        if they are not absolute.
     * 
     * @since 0.1.0
     */
    public ConfiguratorHandler(
            final BaseHierarchicalConfiguration theConfig,
            final String thePath) {
        this.config = theConfig;
        this.path = thePath;
    }
    
    @Override
    public Object invoke(
        final Object proxy, 
        final Method method,
        final Object[] args)
            throws Throwable
    {
        // Extract annotations of type:
        // - ConfigProperty
        String subPath = null;
        String defaultValue = null;
        boolean useDefaultValue = false;
        
        final Annotation[] annotations = method.getAnnotations();
        for (final Annotation annotation : annotations)
        {
            if (annotation instanceof ConfigProperty) {
                if (subPath != null) {
                    throw new RuntimeException(
                        "Multiple occurence of @" +
                        ConfigProperty.class.getName() +
                        " at method " +
                        method.getDeclaringClass().getName() +
                        "." + method.getName());
                }
                
                subPath = ((ConfigProperty)annotation).value();
                continue;
            }
            if (annotation instanceof DefaultValue) {
                if (useDefaultValue) {
                    throw new RuntimeException(
                        "Multiple occurence of @" +
                        DefaultValue.class.getName() +
                        " at method " +
                        method.getDeclaringClass().getName() +
                        "." + method.getName());
                }
                useDefaultValue = true;
                defaultValue = ((DefaultValue)annotation).value();
                continue;
            }
        }
        
        if (subPath == null) {
            throw new RuntimeException(
                "Annotation @" +
                ConfigProperty.class.getName() +
                " missing at method " +
                method.getDeclaringClass().getName() +
                "." + method.getName());
        }
        
        final String absPath = (subPath.startsWith(PATH_SEPARATOR)) ?
            this.path + subPath : subPath;
        
        
        Class rettype = method.getReturnType();
        
        return this.getProperty(absPath, rettype,
            useDefaultValue, defaultValue);
    }
    
    /**
     * Obtain the value of property where the given path points to
     * with a given type.
     * 
     * @param thePath path pointing to a property
     * @param theType type that is expected as return type.
     * @param useDefault tells whether the default value should be used
     * @param theDefault the default value
     * @return the object at the given position
     * 
     * @throws ConfigurationException if the property is absent or if
     *         the value cannot be converted.
     * 
     * @since 0.1.0
     */
    private Object getProperty(
            final String thePath,
            final Class theType,
            final boolean useDefault,
            final Object theDefault) throws ConfigurationException {
        
        final Object value = this.getProperty(thePath, useDefault, theDefault);
        assert value != null;
        
        if (theType.isArray()) {
            if (value instanceof Collection) {
                final Collection c = (Collection) value;
                final Object[] result = new Object[c.size()];
                
                int i = 0;
                for (final Object element : c) {
                    result[i] = this.convertProperty(
                        element,
                        theType.getComponentType(),
                        thePath);
                }
                return result;
            } else {
                final Object[] result = new Object[1];
                result[0] = this.convertProperty(
                    value,
                    theType.getComponentType(),
                    thePath);
                return result;
            }
        }
        
        return this.convertProperty(value, theType, thePath);
    }

    /**
     * Obtain the value of property where the given path points to
     * with a given type.
     * 
     * @param thePath path pointing to a property
     * @param useDefault tells whether the default value should be used
     * @param theDefault the default value
     * @return the object at the given position
     * 
     * @throws ConfigurationException if the property is absent or if
     *         the value cannot be converted.
     * 
     * @since 0.1.0
     */
    private Object getProperty(
            final String thePath,
            final boolean useDefault,
            final Object theDefault) throws ConfigurationException {
                
        final Object result = this.config.getProperty(thePath);
        
        if (result == null) {
            if (useDefault)
                return theDefault;
            else
                throw new ConfigurationException(
                    "The configuration key " + thePath + " is not set.");
        }
        
        return result;
    }
    
    /**
     * Converts a property value to a specific return type.
     * 
     * @param theValue the value of the property to be converted
     * @param theType the type to which the property should be converted
     * @param thePath the path to the current property
     * 
     * @return the converted value
     * @throws ConfigurationException if the value cannot be properly converted
     * 
     * @since 0.1.0
     */
    private Object convertProperty(
            final Object theValue,
            final Class theType,
            final String thePath) throws ConfigurationException {
        if (theType.isInterface()) {
            final InvocationHandler handler = new ConfiguratorHandler(
                this.config,
                thePath);
            return Proxy.newProxyInstance(
                theType.getClassLoader(),
                new Class[] {theType},
                handler);
        }
        
        try {
            return this.config.getConversionHandler().to(
                theValue,
                theType,
                this.config.getInterpolator());
        } catch (ConversionException ex) {
            throw new ConfigurationException(
                "Value at " + thePath + " cannot be converted",
                ex);
        }
    }
}

