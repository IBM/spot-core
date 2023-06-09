/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.utils;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.lang.reflect.Constructor;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.ScenarioOperation;
import com.ibm.bear.qa.spot.core.scenario.ScenarioStep;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.topology.Application;
import com.ibm.bear.qa.spot.core.topology.Topology;
import com.ibm.bear.qa.spot.core.web.SpotAbstractWindow;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Factory to create classes instances.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #createApplicationInstance(String,String)}: Create an instance of the given application class using the given URL.</li>
 * <li>{@link #createOperationInstance(Class,ScenarioStep)}: Create an instance of the given operation associated with the given step.</li>
 * <li>{@link #createTopologyInstance(String)}: Create an instance of the given topology class.</li>
 * <li>{@link #createWindowInstance(WebPage,Class)}: Create an instance of the given web window class located in the given page.</li>
 * <li>{@link #createWindowInstance(WebPage,Class,String...)}: Create an instance of the given web window class located in the given page.</li>
 * <li>{@link #createWindowInstance(WebPage,By,Class)}: Create an instance of the given web window class located in the given page.</li>
 * <li>{@link #createWindowInstance(WebPage,By,Class,String...)}: Create an instance of the given web window class located in the given page.</li>
 * </ul>
 * </p>
 */
public class SpotFactory {

// No instance allowed for this class
private SpotFactory() {}

/**
 * Create an instance of the given application class using the given URL.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with a string parameter.
 * </p>
 * @param className The class of the topology to be created
 * @param url The URL used to identify the application
 * @return The instance of the given application
 * @throws ScenarioFailedError If anything wrong happens during the class instance
 * creation, typically the expected class constructor does not exist.
 */
@SuppressWarnings("unchecked")
public static <A extends Application> A createApplicationInstance(final String className, final String url) throws ScenarioFailedError {
	debugPrintEnteringMethod("className", className);

	try {
		// Get application class
		Class<A> applicationClass = (Class<A>) Class.forName(className);

		// Create application instance using the constructor with String parameter
		Constructor<A> constructor = applicationClass.getConstructor(String.class);
		return constructor.newInstance(url);
	}
	catch (Exception ex) {
		throw new ScenarioFailedError(ex);
	}
}

/**
 * Create an instance of the given topology class.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with no parameters.
 * </p>
 * @param className The class of the topology to be created
 * @return The instance of the given topology
 * @throws ScenarioFailedError If anything wrong happens during the class instance
 * creation, typically the expected class constructor does not exist.
 */
@SuppressWarnings("unchecked")
public static <T extends Topology> T createTopologyInstance(final String className) throws ScenarioFailedError {
	debugPrintEnteringMethod("className", className);

	try {
		// Get topology class
		Class<T> topologyClass = (Class<T>) Class.forName(className);

		// Create topology instance using default constructor
		Constructor<T> constructor = topologyClass.getConstructor();
		return constructor.newInstance();
	}
	catch (Exception ex) {
		throw new ScenarioFailedError(ex);
	}
}

/**
 * Create an instance of the given operation associated with the given step.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with following parameters:
 * <ul>
 * <li>{@link ScenarioStep} or one of its direct subclass</li>
 * </ul>
 * </p>
 * @param operationClass The class of the operation to be created
 * @param step The step creating the operation
 * @return The instance of the given operation
 * @throws ScenarioFailedError If anything wrong happens during the class instance
 * creation, typically the expected class constructor does not exist.
 */
@SuppressWarnings("unchecked")
public static <O extends ScenarioOperation> O createOperationInstance(final Class<O> operationClass, final ScenarioStep step) throws ScenarioFailedError {
	debugPrintEnteringMethod("operationClass", getClassSimpleName(operationClass));

	// Get step class
	Class<? extends ScenarioStep> stepClass = step.getClass();

	// Loop until found the constructor on the right subclass of ScenarioStep
	Exception exception = null;
	while (stepClass != null) {
		try {
			Constructor<O> constructor = operationClass.getConstructor(stepClass);
			return constructor.newInstance(step);
		}
		catch (@SuppressWarnings("unused") NoSuchMethodException nsme) {
			// Skip as we loop to find the right constructor...
		}
		catch (Exception ex) {
			if (exception == null) {
				exception = ex;
			}
		}
		stepClass = (Class< ? extends ScenarioStep>) stepClass.getSuperclass();
	}

	// No constructor were found, give up
	if (exception != null) {
		throw new ScenarioFailedError(exception);
	}
	throw new ScenarioFailedError("Cannot create instance of "+getClassSimpleName(operationClass));
}

/**
 * Create an instance of the given web window class located in the given page.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with following parameters:
 * <ul>
 * <li>{@link WebPage} or one of its direct subclass</li>
 * </ul>
 * </p>
 * @param page The page from which the window will belong to
 * @param windowClass The framework class of the window
 * @return The instance of the given window class
 * @throws Exception Thrown if typically the expected class constructor does
 * not exist.
 */
public static <W extends SpotAbstractWindow> W createWindowInstance(final WebPage page, final Class<W> windowClass) throws Exception {
	return createWindowInstance(page, null, windowClass, (String[]) null);
}

/**
 * Create an instance of the given web window class located in the given page.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with following parameters:
 * <ul>
 * <li>{@link WebPage} or one of its direct subclass</li>
 * </ul>
 * </p>
 * @param page The page from which the window will belong to
 * @param windowClass The framework class of the window
 * @param data Additional data provided when creating the instance as a list
 * of strings
 * @return The instance of the given window class
 * @throws Exception Thrown if typically the expected class constructor does
 * not exist.
 */
public static <W extends SpotAbstractWindow> W createWindowInstance(final WebPage page, final Class<W> windowClass, final String... data) throws Exception {
	return createWindowInstance(page, null, windowClass, data);
}

/**
 * Create an instance of the given web window class located in the given page.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with following parameters:
 * <ul>
 * <li>{@link WebPage} or one of its direct subclass</li>
 * <li>{@link By}</li>
 * </ul>
 * </p>
 * @param page The page from which the window will belong to
 * @param locator The mechanism to find the web window element when opened
 * @param windowClass The framework class of the window
 * @return The instance of the given window class
 * @throws Exception Thrown if typically the expected class constructor does
 * not exist.
 */
public static <W extends SpotAbstractWindow> W createWindowInstance(final WebPage page, final By locator, final Class<W> windowClass) throws Exception {
	return createWindowInstance(page, locator, windowClass, (String[]) null);
}

/**
 * Create an instance of the given web window class located in the given page.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with following parameters:
 * <ul>
 * <li>{@link WebPage} or one of its direct subclass</li>
 * <li>{@link By}</li>
 * <li>{@link String}...</li>
 * </ul>
 * </p>
 * @param page The page from which the window will belong to
 * @param locator The mechanism to find the web window element when opened
 * @param windowClass The framework class of the window
 * @param data Additional data provided when creating the instance as a list
 * of strings
 * @return The instance of the given window class
 * @throws Exception Thrown if typically the expected class constructor does
 * not exist.
 */
@SuppressWarnings({"unchecked", "unused" })
public static <W extends SpotAbstractWindow> W createWindowInstance(final WebPage page, final By locator, final Class<W> windowClass, final String... data) throws Exception {
	debugPrintEnteringMethod("page", page.getLocation(), "locator", locator, "windowClass", getClassSimpleName(windowClass), "data", getTextFromList(data));

	// Get page class
	Class<? extends WebPage> pageClass = page.getClass();

	// Loop until found the constructor on the right subclass of WebPage
	Exception exception = null;
	while (pageClass != null) {
		try {
			// Use default locator constructors
			if (locator == null) {

				// Use no data constructor
				if (data == null || data.length == 0) {
					Constructor<W> constructor = windowClass.getConstructor(pageClass);
					return constructor.newInstance(page);
				}

				// Try constructor using simple String in case of data length equals to 1
				if (data.length == 1) {
					try {
						Constructor<W> constructor = windowClass.getConstructor(pageClass, String.class);
						return constructor.newInstance(page, data[0]);
					}
					catch (NoSuchMethodException nsme) {
						// Skip as we still want to try constructor with String array...
					}
				}

				// Use data constructor
				Constructor<W> constructor = windowClass.getConstructor(pageClass, String[].class);
				return constructor.newInstance(page, data);
			}

			// Use locator constructor with no data
			if (data == null || data.length == 0) {
				Constructor<W> constructor = windowClass.getConstructor(pageClass, By.class);
				return constructor.newInstance(page, locator);
			}

			// Use locator constructor with data
			Constructor<W> constructor = windowClass.getConstructor(pageClass, By.class, String[].class);
			return constructor.newInstance(page, locator, data);
		}
		catch (NoSuchMethodException nsme) {
			// Skip as we loop to find the right constructor...
		}
		catch (Exception ex) {
			if (exception == null) {
				exception = ex;
			}
		}
		pageClass = (Class< ? extends WebPage>) pageClass.getSuperclass();
	}

	// No constructor were found, give up
	if (exception != null) {
		throw exception;
	}
	throw new Exception("Cannot create instance of "+windowClass.getName()+" web menu.");
}
}
