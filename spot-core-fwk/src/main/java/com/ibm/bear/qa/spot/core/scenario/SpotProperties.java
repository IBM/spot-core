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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.println;

import java.util.*;

import com.ibm.bear.qa.spot.core.params.ScenarioParametersManager;
import com.ibm.bear.qa.spot.core.scenario.SpotProperty.Origin;

/**
 * Class to manage all properties defined and/or used during scenario execution.
 * <p>
 * This class is a subclass of {@link Hashtable} using property names as keys and
 * {@link SpotProperty} as values.
 * </p><p>
 * When referring to a property during scenario execution, it can be either undefined or defined
 * using three different ways: System properties, Parameters file (see
 * {@link ScenarioParametersManager} ) or environment variables. The purpose of this specific class
 * (instead of using standard {@link Properties} ) is to allow testers to have a trace in the log of
 * where a property has been defined and what was the value it as been assigned (defined, default or <code>null</code>).
 * </p><p>
 * So, while getting a property, it's possible to log its storage and/or usage depending on the
 * <code>spot.log.properties</code> property value (default is<code>AtEnd</code>, see
 * {@link LogProperties} for all other possible values for this log).
 * </p><p>
 * Note that properties with <code>null</code> values are not logged by default even when log
 * for properties is activated but that can be changed this by setting the <code>spot.log.null.properties</code>
 * property value to <code>true</code>.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #get(String,String)}: Return the value of the given property using default value if not defined.</li>
 * <li>{@link #log()}: Dump stored properties at the end of the log file if specified.</li>
 * </ul>
 * </p>
 */
public class SpotProperties extends Hashtable<String, SpotProperty> {

	/**
	 * Flavors while logging properties value
	 */
	private enum LogProperties {
		/**
		 * Using this flavor means that properties value should never be logged.
		 */
		Never,
		/**
		 * Using this flavor means that properties value should be logged only
		 * at the end of scenario execution (precisely when closing debug log file).
		 */
		AtEnd,
		/**
		 * Using this flavor means that properties value should be logged
		 * when stored in current list and at the end of scenario execution.
		 */
		StorageAndAtEnd,
		/**
		 * Using this flavor means that properties value should be logged
		 * only when stored in current list.
		 */
		Storage,
		/**
		 * Using this flavor means that properties value should be always
		 * logged (which includes each time the property is used).
		 */
		Always
	}

	/**
	 *  Tells when to log properties value storage and usage. Default is {@link LogProperties#AtEnd}.
	 */
	private final static LogProperties LOG_PROPERTIES = LogProperties.valueOf(System.getProperty("spot.log.properties", LogProperties.AtEnd.name()));

	/**
	 * Tells whether properties with null value should be logged or not.
	 */
	private final static boolean LOG_NULL_PROPERTIES = System.getProperty("spot.log.null.properties", "false").equals("true");
	static {
		System.out.println("LOG_PROPERTIES="+LOG_PROPERTIES);
		System.out.println("LOG_NULL_PROPERTIES="+LOG_NULL_PROPERTIES);
	}

public SpotProperties() {
}

/**
 * Return the value of the given property using default value if not defined.
 * <p>
 * Get the stored property value and create it (see {@link SpotProperty})
 * if it's the first time the property value is requested.
 * </p><p>
 * Log the property storage and/or usage depending on the <code>spot.log.properties</code>
 * and <code>spot.log.null.properties</code> properties value.
 * </p>
 * @param name The parameter name
 * @param defaultValue The default value if the property is undefined
 * @return The string corresponding to the parameter value or given default value
 * if the parameter is undefined.
 */
public SpotProperty get(final String name, final String defaultValue) {

	// Get the property
	SpotProperty property = get(name);
	boolean newProperty = false;
	if (property == null) {
		property = new SpotProperty(name, defaultValue);
		put(name, property);
		newProperty = true;
	}

	// Log property value if specified
	if (LOG_PROPERTIES != null && (property.isNotNull() || LOG_NULL_PROPERTIES)) {
		switch (LOG_PROPERTIES) {
			case Always:
				if (!newProperty) {
					debugPrintln("Used property "+property);
				}
				//$FALL-THROUGH$
			case Storage:
			case StorageAndAtEnd:
				if (newProperty) {
					debugPrintln("Stored property "+property+" ("+ property.getOrigin()+")");
				}
				break;
			default:
				break;
		}
	}

	// Return the found or stored property
	return property;
}

/**
 * Dump stored properties at the end of the log file if specified.
 *
 * @see #LOG_PROPERTIES
 * @see #LOG_NULL_PROPERTIES
 */
public void log() {
	switch (LOG_PROPERTIES) {
		case Always:
		case AtEnd:
		case StorageAndAtEnd:
			println();
			println("Following properties were used during scenario execution:");
			@SuppressWarnings("unchecked")
			List<SpotProperty>[] lists = new ArrayList[Origin.values().length];
			for (Origin origin : Origin.values()) {
				lists[origin.ordinal()] = new ArrayList<>();
			}
			for (String propertyName : keySet()) {
				SpotProperty property = get(propertyName);
				lists[property.getOrigin().ordinal()].add(property);
			}
			for (Origin origin : Origin.values()) {
				List<SpotProperty> originProperties = lists[origin.ordinal()];
				if (originProperties.size() > 0) {
					Collections.sort(originProperties);
					println("	+ " + origin + " properties:");
					for (SpotProperty property : originProperties) {
						if (property.isNotNull() || LOG_NULL_PROPERTIES) {
							println("		- " + property);
						}
					}
				}
			}
			break;
		default:
			break;
	}
}
}
