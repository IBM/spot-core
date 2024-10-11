/*********************************************************************
* Copyright (c) 2012, 2024 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.web;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to handle a checkable web element (eg. typically a check-box) in a web page.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #select()}: Select the associated web element.</li>
 * </ul>
 * </p>
 */
public class SpotCheckableElement extends SpotAttributeSelectableElement {

	/* Constants */
	private static final String ARIA_CHECKED = "aria-checked";

/**
 * Create a checkable element in the given parent using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the created instance.
 * </p>
 * @param parent The element wrapper in which the checkable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotCheckableElement(final WebElementWrapper parent, final By locator) {
	super(parent, locator, ARIA_CHECKED);
}

/**
 * Create a checkable element in the given parent using the wrapped and selection
 * web elements found by the given locators.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the selection web element.
 * </p>
 * @param parent The element wrapper in which the checkable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotCheckableElement(final WebElementWrapper parent, final By locator, final By selectionLocator) {
	super(parent, locator, selectionLocator, ARIA_CHECKED);
}

/**
 * Create a checkable element in the given parent using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the created instance.
 * </p>
 * @param parent The element wrapper in which the checkable element is located
 * @param wwElement The wrapped web element
 */
public SpotCheckableElement(final WebElementWrapper parent, final WebBrowserElement wwElement) {
	super(parent, wwElement, ARIA_CHECKED);
}

/**
 * Create a checkable element in the given parent using the given wrapped web
 * element and the selection web element found using the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the selection web element.
 * </p>
 * @param parent The element wrapper in which the checkable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotCheckableElement(final WebElementWrapper parent, final WebBrowserElement wwElement, final By selectionLocator) {
	super(parent, wwElement, selectionLocator, ARIA_CHECKED);
}

/**
 * Create a checkable element in the given page using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the created instance.
 * </p>
 * @param page The page in which the checkable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotCheckableElement(final WebPage page, final By locator) {
	super(page, locator, ARIA_CHECKED);
}

/**
 * Create a checkable element in the given page using the wrapped and selection
 * web elements found by the given locators.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the selection web element.
 * </p>
 * @param page The page in which the checkable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotCheckableElement(final WebPage page, final By locator, final By selectionLocator) {
	super(page, locator, selectionLocator, ARIA_CHECKED);
}

/**
 * Create a checkable element in the given page using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the created instance.
 * </p>
 * @param page The page in which the checkable element is located
 * @param wwElement The wrapped web element
 */
public SpotCheckableElement(final WebPage page, final WebBrowserElement wwElement) {
	super(page, wwElement, ARIA_CHECKED);
}

/**
 * Create a checkable element in the given page using the given wrapped web
 * element and the selection web element found using the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-checked</code> attribute
 * of the selection web element.
 * </p>
 * @param page The page in which the checkable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotCheckableElement(final WebPage page, final WebBrowserElement wwElement, final By selectionLocator) {
	super(page, wwElement, selectionLocator, ARIA_CHECKED);
}

@Override
public void select() throws ScenarioFailedError {
	/* TODO Remove the entire method after having been 100% sure that this change has no impact
	if (!isSelected()) {
		debugPrintEnteringMethod();
		pause(250);
		getSelectionElement().click();
		pause(250);
		waitUntilSelection(true, true);
	}
	*/
	super.select();
}
}
