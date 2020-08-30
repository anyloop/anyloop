/*
 * ConfiguratorHandler.java
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

import com.github.anyloop.chassis.annotations.ConfigProperty;
import com.github.anyloop.chassis.annotations.DefaultValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import org.apache.commons.configuration2.ImmutableHierarchicalConfiguration;
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
     * This configuration is used for resolving absolute names.
     * 
     * @since 0.1.0
     */
    private final BaseHierarchicalConfiguration rootConfig;
    
    /**
     * Configuration that supplies the values for the invoked methods.
     * This configuration is used for resolving relative names.
     * 
     * @since 0.1.0
     */
    private final ImmutableHierarchicalConfiguration currentConfig;
    
    /**
     * Creates an invocation handler.
     * 
     * @param theRootConfig the configuration to be used
     * 
     * @since 0.1.0
     */
    ConfiguratorHandler(
            final BaseHierarchicalConfiguration theRootConfig) {
        this(theRootConfig, theRootConfig);
    }

    /**
     * Creates an invocation handler.
     * 
     * @param theRootConfig the configuration to be used for absolute
     *        node names
     * @param theCurrentConfig the configuration relative to which
     *        node names are interpreted if they are not absolute.
     * 
     * @since 0.1.0
     */    
    protected ConfiguratorHandler(
            final BaseHierarchicalConfiguration theRootConfig,
            final ImmutableHierarchicalConfiguration theCurrentConfig) {
        this.rootConfig = theRootConfig;
        this.currentConfig = theCurrentConfig;
    }
    
    @Override
    public Object invoke(
        final Object proxy, 
        final Method method,
        final Object[] args)
            throws Throwable {
        // Extract annotations of type:
        // - ConfigProperty
        String subPath = null;
        String defaultValue = null;
        boolean useDefaultValue = false;
        
        final Annotation[] annotations = method.getAnnotations();
        for (final Annotation annotation : annotations) {
            if (annotation instanceof ConfigProperty) {
                // no need to check for multiple occurrences,
                // this is done at compile time.
                
                subPath = ((ConfigProperty) annotation).value();
                continue;
            }
            if (annotation instanceof DefaultValue) {
                // no need to check for multiple occurrences,
                // this is done at compile time.
                useDefaultValue = true;
                defaultValue = ((DefaultValue) annotation).value();
                continue;
            }
        }
        
        if (subPath == null) {
            throw new ConfigurationException(
                "Annotation @"
                + ConfigProperty.class.getName()
                + " missing at method "
                + method.getDeclaringClass().getName()
                + "." + method.getName());
        }

        Class rettype = method.getReturnType();
        
        return this.getProperty(subPath, rettype,
            useDefaultValue, defaultValue);
    }
    
    /**
     * Obtain the value of property where the given path points to
     * with a given type.
     * 
     * @param thePath (relative or absolute) path pointing to a property
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
        
        ImmutableHierarchicalConfiguration configuration;
        String path;
        
        if (thePath.startsWith(PATH_SEPARATOR)) {
            configuration = this.currentConfig;
            path = thePath.substring(1);
        } else {
            configuration = this.rootConfig;
            path = thePath;
        }
        
        if (theType.isArray()) {
            if (useDefault) {
                throw new ConfigurationException(
                    "@DefaultValue annotation is not allowed with arrays");
            }
            
            final Class componentType = theType.getComponentType();
            return this.getArrayProperty(path, configuration, componentType);
        }
            
        if (theType.isInterface()) {
            if (useDefault) {
                throw new ConfigurationException(
                    "@DefaultValue annotation is not allowed with interfaces");
            }

            var sub = configuration.immutableConfigurationAt(path);
            return this.getInterfaceProperty(sub, theType);
        }
                        
        final Object value = configuration.getProperty(path);
        
        if (value == null) {
            if (useDefault) {
                return this.convertProperty(theDefault, theType, configuration);
            } else {
                throw new ConfigurationException(
                    "The configuration key " + path + " is not set.");
            }
        }

        return this.convertProperty(value, theType, configuration);
    }
    
    private Object getArrayProperty(
            final String theLocalPath,
            final ImmutableHierarchicalConfiguration theLocalConfig,
            final Class theComponentType) throws ConfigurationException {
                
        if (theComponentType.isInterface()) {
            var confs = theLocalConfig.immutableConfigurationsAt(theLocalPath);
            final int n = confs.size();
            
            final Object result = Array.newInstance(theComponentType, n);

            int i = 0;
            for (var it = confs.iterator(); it.hasNext();) {
                var sub = it.next();
                
                final InvocationHandler handler =
                    new ConfiguratorHandler(this.rootConfig, sub);
                Array.set(result, i++, Proxy.newProxyInstance(
                    theComponentType.getClassLoader(),
                    new Class[] {theComponentType},
                    handler));
            }
            return result;
        }
            
        final Object value = theLocalConfig
            .getProperty(theLocalPath);
                
        if (value == null) {
            final Object[] result = new Object[0];
            return result;
        }

        if (value instanceof Collection) {
            final Collection c = (Collection) value;
            final Object result = Array.newInstance(theComponentType, c.size());
            
            int i = 0;
            for (final Object element : c) {
                Array.set(result, i++, this.convertProperty(
                    element,
                    theComponentType,
                    theLocalConfig));
            }
            return result;
        }
            
        final Object result = Array.newInstance(theComponentType, 1);
        Array.set(result, 0, this.convertProperty(
            value,
            theComponentType,
            theLocalConfig));
        return result;
    }

    private Object getInterfaceProperty(
            final ImmutableHierarchicalConfiguration theLocalConfiguration,
            final Class theInterfaceType) throws ConfigurationException {
        final InvocationHandler handler =
            new ConfiguratorHandler(
                this.rootConfig,
                theLocalConfiguration);
        return Proxy.newProxyInstance(
            theInterfaceType.getClassLoader(),
            new Class[] {theInterfaceType},
            handler);
    }
    
    /**
     * Converts a property value to a specific return type.
     * 
     * @param theValue the value of the property to be converted
     * @param theType the type to which the property should be converted
     * @param theLocalConfiguration the configuration used to retrieve the
     *        propertie values
     * 
     * @return the converted value
     * @throws ConfigurationException if the value cannot be properly converted
     * 
     * @since 0.1.0
     */
    private Object convertProperty(
            final Object theValue,
            final Class theType,
            final ImmutableHierarchicalConfiguration theLocalConfiguration
            ) throws ConfigurationException {
        if (theValue == null) {
            return null;
        }
        
        /* Never invoked this way:
        if (theType.isInterface()) {
            final InvocationHandler handler = new ConfiguratorHandler(
                this.rootConfig,
                theLocalConfiguration);
            return Proxy.newProxyInstance(
                theType.getClassLoader(),
                new Class[] {theType},
                handler);
        } */
        
        try {
            return this.rootConfig.getConversionHandler().to(
                theValue,
                theType,
                this.rootConfig.getInterpolator());
        } catch (ConversionException ex) {
            throw new ConfigurationException(
                "Value \"" + theValue + "\" cannot be converted to "
                    + theType.getName(),
                ex);
        }
    }
}

