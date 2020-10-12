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
package com.ibm.bear.qa.spot.core.web;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to handle a selectable web element in a web page using a specific attribute to tell whether
 * the element is selected or not.
 * <p>
 * By default the attribute is the <code>aria-selected</code> one but specific constructor might be
 * used to specify another one.
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

public SpotAttributeSelectableElement(final WebPage page, final By locator) {
	this(page, locator, ARIA_SELECTED);
}

public SpotAttributeSelectableElement(final WebPage page, final By locator, final By expansionLocator) {
	this(page, locator, expansionLocator, ARIA_SELECTED);
}

public SpotAttributeSelectableElement(final WebPage page, final By locator, final By expansionLocator, final String attribute) {
	super(page, locator, expansionLocator);
	this.selectedAttribute = attribute;
}

public SpotAttributeSelectableElement(final WebPage page, final By locator, final String attribute) {
	super(page, locator);
	this.selectedAttribute = attribute;
}

public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement webElement) {
	this(page, webElement, ARIA_SELECTED);
}

public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement webElement, final By expansionLocator) {
	this(page, webElement, expansionLocator, ARIA_SELECTED);
}

public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement webElement, final By expansionLocator, final String attribute) {
	super(page, webElement, expansionLocator);
	this.selectedAttribute = attribute;
}

public SpotAttributeSelectableElement(final WebPage page, final WebBrowserElement webElement, final String attribute) {
	super(page, webElement);
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
