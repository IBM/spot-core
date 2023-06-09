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
package com.ibm.bear.qa.spot.core.web;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to handle a selectable web element in a web page using a specific attribute
 * to tell whether the element is selected or not.
 * <p>
 * By default the attribute is the <code>aria-selected</code> one but specific
 * constructor might be used to specify another one.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #isSelected()}: Returns whether the associated web element is selected or not.</li>
 * </ul>
 * </p>
 */
public class SpotAttributeSelectableElement extends SpotSelectableElement {

	/* Constants */
	private static final String ARIA_SELECTED = "aria-selected";

	/* Fields */
	private final String selectedAttribute;

/**
 * Create a selectable element in the given parent using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the created instance.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final By locator) {
	this(parent, locator, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given parent using the wrapped and selection
 * web elements found by the given locators.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the selection web element.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final By locator, final By selectionLocator) {
	this(parent, locator, selectionLocator, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given parent using the wrapped and selection
 * web elements found by the given locators using the given attribute for the selection
 * status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the given attribute of the selection web element.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final By locator, final By selectionLocator, final String attribute) {
	super(parent, locator, selectionLocator);
	this.selectedAttribute = attribute;
}

/**
 * Create a selectable element in the given parent using the wrapped web element
 * found by the given locator using the given attribute for the selection status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the given attribute of the created instance.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final By locator, final String attribute) {
	super(parent, locator);
	this.selectedAttribute = attribute;
}

/**
 * Create a selectable element in the given parent using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the created instance.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param wwElement The wrapped web element
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final WebBrowserElement wwElement) {
	this(parent, wwElement, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given parent using the given wrapped web
 * element and the selection web element found using the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the selection web element.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final WebBrowserElement wwElement, final By selectionLocator) {
	this(parent, wwElement, selectionLocator, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given parent using the given wrapped web
 * element and the selection web element found using the given locator using
 * the given attribute for the selection status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the given attribute of the selection web element.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final WebBrowserElement wwElement, final By selectionLocator, final String attribute) {
	super(parent, wwElement, selectionLocator);
	this.selectedAttribute = attribute;
}

/**
 * Create a selectable element in the given parent using the given wrapped web
 * element using the given attribute for the selection status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the given attribute of the created instance.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebElementWrapper parent, final WebBrowserElement wwElement, final String attribute) {
	super(parent, wwElement);
	this.selectedAttribute = attribute;
}

/**
 * Create a selectable element in the given page using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the created instance.
 * </p>
 * @param page The page in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotAttributeSelectableElement(final WebPage page, final By locator) {
	this(page, locator, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given page using the wrapped and selection
 * web elements found by the given locators.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the selection web element.
 * </p>
 * @param page The page in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotAttributeSelectableElement(final WebPage page, final By locator, final By selectionLocator) {
	this(page, locator, selectionLocator, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given page using the wrapped and selection
 * web elements found by the given locators using the given attribute for the selection
 * status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the given attribute of the selection web element.
 * </p>
 * @param page The page in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebPage page, final By locator, final By selectionLocator, final String attribute) {
	super(page, locator, selectionLocator);
	this.selectedAttribute = attribute;
}

/**
 * Create a selectable element in the given page using the wrapped web element
 * found by the given locator using the given attribute for the selection status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the given attribute of the created instance.
 * </p>
 * @param page The page in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebPage page, final By locator, final String attribute) {
	super(page, locator);
	this.selectedAttribute = attribute;
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the created instance.
 * </p>
 * @param page The page in which the selectable element is located
 * @param wwElement The wrapped web element
 */
public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement wwElement) {
	this(page, wwElement, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element and the selection web element found using the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the <code>aria-selected</code> attribute
 * of the selection web element.
 * </p>
 * @param page The page in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement wwElement, final By selectionLocator) {
	this(page, wwElement, selectionLocator, ARIA_SELECTED);
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element and the selection web element found using the given locator using
 * the given attribute for the selection status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p><p>
 * The selection status is provided by the given attribute of the selection web element.
 * </p>
 * @param page The page in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement wwElement, final By selectionLocator, final String attribute) {
	super(page, wwElement, selectionLocator);
	this.selectedAttribute = attribute;
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element using the given attribute for the selection status.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p><p>
 * The selection status is provided by the given attribute of the created instance.
 * </p>
 * @param page The page in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param attribute The attribute of the selection showing the selection status
 */
public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement wwElement, final String attribute) {
	super(page, wwElement);
	this.selectedAttribute = attribute;
}

@Override
public boolean isSelected() throws ScenarioFailedError {
	String selectionAttribute = getSelectionElement().getAttributeValue(this.selectedAttribute);
	switch (selectionAttribute) {
		case "false":
			return false;
		case "true":
			return true;
		default:
			throw new ScenarioFailedError("Unexpected value '"+selectionAttribute+"' for '"+this.selectedAttribute+" attribute in selectable element "+this);
	}
}
}
