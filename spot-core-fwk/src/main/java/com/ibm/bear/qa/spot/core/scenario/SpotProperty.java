/*********************************************************************
* Copyright (c) 2012, 2023 IBM Corporation and others.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
**********************************************************************/
package com.ibm.bear.qa.spot.core.scenario;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import com.ibm.bear.qa.spot.core.params.ScenarioParametersManager;

/**
 * Class to manage a property defined and/or used during scenario execution.
 * <p>
 * A framework property handles a name and a value as usual property but it also stores the origin
 * of property definition (see {@link Origin} and the default value.
 * </p><p>
 * When initializing the property, it looks where the property has been defined and stores the
 * origin if a value is found either in the {@link System} properties or through
 * {@link ScenarioParametersManager} or environment variables.
 * </p><p>
 * This class defines following public API methods of {@link Comparable} interface:
 * <ul>
 * <li>{@link #compareTo(SpotProperty)}: Compares this object with the specified object for order.</li>
 * </ul>
 * </p><p>
 * This class also defines following internal API methods:
 * <ul>
 * <li>{@link #equals(Object)}: Compares the argument to the receiver, and answers true</li>
 * <li>{@link #getDefaultValue()}: Get the property default value.</li>
 * <li>{@link #getName()}: Return the property name.</li>
 * <li>{@link #getOrigin()}: Return the origin of the property</li>
 * <li>{@link #getValue()}: Return the property value.</li>
 * <li>{@link #hashCode()}: Answers an integer hash code for the receiver. Any two</li>
 * <li>{@link #toString()}: Answers a string containing a concise, human-readable</li>
 * </ul>
 * </p>
 */
public class SpotProperty implements Comparable<SpotProperty> {

	/**
	 * Enumeration to list all possible ways to define a property
	 */
	public enum Origin {
		/** The associated property is not defined */
		Undefined,
		/** The associated property is defined in properties file and read using {@link ScenarioParametersManager} */
		Parameter,
		/** The associated property is defined using an OS environment variable */
		Environment,
		/** The associated property is defined using {@link System} properties */
		System
	}

	final private String name;
	private String value;
	final private String defaultValue;
	private Origin origin = Origin.Undefined;
	private boolean undefined;

/**
 * Create a property using given name and default value.
 * 
 * @param name The property name
 * @param defaultValue The default value if not defined
 */
public SpotProperty(final String name, final String defaultValue) {
	this.name = name;
	this.defaultValue = defaultValue;
	initialize();
}


@Override
public int compareTo(final SpotProperty o) {
	return this.name.compareTo(o.name);
}

@Override
public boolean equals(final Object o) {
	if (o instanceof SpotProperty) {
		SpotProperty property = (SpotProperty) o;
		return this.name.equals(property.name) && this.value.equals(property.value) && this.origin == property.origin;
	}
	if (o instanceof String) {
		return this.name.equals(o);
	}
	return false;
}

/**
 * Get the property default value.
 *
 * @return The default value, might be <code>null</code>
 */
public String getDefaultValue() {
	return this.defaultValue;
}

/**
 * Return the property name.
 *
 * @return The property name
 */
public String getName() {
	return this.name;
}

/**
 * Return the origin of the property
 * <p>
 * See {@link Origin} enumeration for all possible values and their meaning.
 * </p>
 * @return The property origin
 */
public Origin getOrigin() {
	return this.origin;
}

/**
 * Return the property value.
 * <p>
 * Note that returned value might has been set to default value if the property
 * was not defined.
 * </p>
 * @return The property value
 */
public String getValue() {
	return this.value;
}

@Override
public int hashCode() {
	return this.name.hashCode();
}

/**
 * Initialize the property.
 * <p>
 * Try first to get it from {@link System} properties. If not defined, then try
 * to get it from {@link ScenarioUtils#PARAMETERS_MANAGER}. If not defined
 * and use of environment variables is allowed, then try to look for an environment
 * variable with similar name.
 * </p><p>
 * If a value was got from one of the possible origin then stores it and the
 * assignment origin. If not, then assign it with the default value and let
 * the assignment origin to {@link Origin#Undefined}.
 * </p>
 */
private void initialize() {

	// Try to get the value from System properties
	this.value = System.getProperty(this.name);
	if (this.value != null) {
		this.origin = Origin.System;
	} else {
		// Then try to get it from properties manager
		if (PARAMETERS_MANAGER != null) {
			this.value = PARAMETERS_MANAGER.getProperty(this.name);
		}
		if (this.value != null) {
			this.origin = Origin.Parameter;
		} else {
			// Then try to get it from environment variable
			if (USE_ENV_VARIABLES) {
				this.value = getEnvVariableValue(this.name);
			}
			if (this.value != null) {
				this.origin = Origin.Environment;
			}
		}
	}

	// If property is not specified, then use default value
	this.undefined = this.value == null;
	if (this.undefined) {
		this.value =  this.defaultValue;
	}
}

/**
 * Tells whether the property value is not <code>null</code>.
 *
 * @return <code>true</code> if the property value is not <code>null</code>,
 * <code>false</code> otherwise
 */
public boolean isNotNull() {
	return this.value != null;
}

@Override
public String toString() {
	StringBuilder builder = new StringBuilder(this.name).append('=');
	String lowercaseName = this.name.toLowerCase();
	if (this.value != null && (lowercaseName.contains("password") || lowercaseName.contains("pwd"))) {
		builder.append(this.value.charAt(0)).append("*******");
	} else if (this.value != null && lowercaseName.matches(".*api[\\._]?(key|token).*")) {
		builder.append("*******");
	} else {
		builder.append(this.value);
	}
	return builder.toString();
}
}
