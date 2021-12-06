/*********************************************************************
* Copyright (c) 2020, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.samples.tqa.elements;

import java.lang.reflect.Constructor;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.SpotImplementationError;
import com.ibm.bear.qa.spot.core.web.WebElementWrapper;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;

/**
 * Abstract class to manage common behavior for any ToolsQA test page container.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getPage()}: Return the web page in which the current element belongs to.</li>
 * </ul>
 * </p>
 */
public abstract class ToolsQaTestContainer extends WebElementWrapper {

/**
 * Create a container instance belonging to the given page matching the given item.
 *
 * @param page The page which owns the container created instance
 * @param containerClass The class of container to be created
 * @param itemLabel The label of item used for container instance creation
 * @return The created specific container instance
 */
static ToolsQaTestContainer createInstance(final ToolsQaTestPage page, final Class<? extends ToolsQaTestContainer> containerClass, final String itemLabel) {
	Constructor< ? extends ToolsQaTestContainer> constructor;
	try {
		if (containerClass == null) {
			throw new SpotImplementationError("Missing implementation for Elements item '"+itemLabel+"'.");
		}
		constructor = containerClass.getConstructor(ToolsQaTestPage.class);
		return constructor.newInstance(page);
	} catch (Exception ex) {
		throw new ScenarioFailedError(ex);
	}
}

/**
 * Create an instance of container belonging to the given page using given locator.
 *
 * @param page The page which owns the container created instance
 * @param locator The locator used to find the container element in the page
 */
ToolsQaTestContainer(final ToolsQaTestPage page, final By locator) {
	super(page, locator);
}

@Override
public ToolsQaTestPage getPage() {
	return (ToolsQaTestPage) super.getPage();
}
}
