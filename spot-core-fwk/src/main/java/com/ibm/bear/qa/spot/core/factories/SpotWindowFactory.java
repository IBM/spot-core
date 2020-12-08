/*********************************************************************
* Copyright (c) 2012, 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.factories;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.web.SpotAbstractWindow;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Factory to create instances of {@link SpotAbstractWindow}.
 */
public class SpotWindowFactory {

private SpotWindowFactory() {
}

/**
 * Create an instance of the given web window class located in the given page.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with a single {@link WebPage} or one of its direct
 * subclass parameter.
 * </p>
 * @param page The page from which the window will belong to
 * @param windowClass The framework class of the window
 * @return The instance of the given window class
 * @throws Exception Thrown if typically the expected class constructor does
 * not exist.
 */
public static <W extends SpotAbstractWindow> W createInstance(final WebPage page, final Class<W> windowClass) throws Exception {
	return createInstance(page, null, windowClass, (String[]) null);
}

/**
 * Create an instance of the given web window class located in the given page.
 * <p>
 * When using this factory method, the framework assumes that the given
 * class has a constructor with a single {@link WebPage} or one of its direct
 * subclass parameter.
 * </p>
 * @param page The page from which the window will belong to
 * @param windowClass The framework class of the window
 * @param data Additional data provided when creating the instance as a list
 * of strings
 * @return The instance of the given window class
 * @throws Exception Thrown if typically the expected class constructor does
 * not exist.
 */
public static <W extends SpotAbstractWindow> W createInstance(final WebPage page, final Class<W> windowClass, final String... data) throws Exception {
	return createInstance(page, null, windowClass, data);
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
public static <W extends SpotAbstractWindow> W createInstance(final WebPage page, final By locator, final Class<W> windowClass) throws Exception {
	return createInstance(page, locator, windowClass, (String[]) null);
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
public static <W extends SpotAbstractWindow> W createInstance(final WebPage page, final By locator, final Class<W> windowClass, final String... data) throws Exception {
	debugPrintEnteringMethod("page", page.getLocation(), "locator", locator, "windowClass", getClassSimpleName(windowClass), "data", getTextFromList(data));

	// Start from the first abstract class
	Class<? extends WebPage> pageClass = page.getClass();
	while ((pageClass.getModifiers() & Modifier.ABSTRACT) == 0) {
		pageClass = (Class<? extends WebPage>) pageClass.getSuperclass();
	}

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
