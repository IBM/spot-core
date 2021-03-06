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

import com.ibm.bear.qa.spot.core.api.elements.SpotWindow;

/**
 * Class to manage web element with <code>table</code> tag.
 * <p>
 *
 * </p><p>
 * Public API for this class is defined in {@link SpotWindow} interface.
 * </p><p>
 * Internal API methods accessible in the framework are:
 * <ul>
 * </ul>
 * </p><p>
 * Internal API methods accessible from subclasses are:
 * <ul>
 * </ul>
 * </p>
 */
public class SpotSimpleTableElement extends SpotAbstractTableElement {

	protected static final By HEADERS_LOCATOR = By.xpath("./thead/tr/th");
	protected static final String ROWS_XPATH = "./tbody/tr";
	protected static final By ROWS_LOCATOR = By.xpath(ROWS_XPATH);
	protected static final By CELLS_LOCATOR = By.xpath("./td");
	protected static final String MATCHING_ROW_XPATH = ROWS_XPATH + "/td[.='%s']/ancestor::tr[1]";

/**
 * Create a table element child of the given parent which has the <b>table</b> tag.
 *
 * @param parent The wrapped web element parent
 */
public SpotSimpleTableElement(final WebElementWrapper parent) {
	super(parent, By.cssSelector("table"));
}

/**
 * Create a table element child of the given parent and the given relative locator.
 *
 * @param parent The wrapped web element parent
 */
public SpotSimpleTableElement(final WebElementWrapper parent, final By locator) {
	super(parent, locator);
}

/**
 * Create a table element with the given parent and web element.
 *
 * @param parent The wrapped web element parent
 * @param element The table web element
 */
public SpotSimpleTableElement(final WebElementWrapper parent, final WebBrowserElement element) {
	super(parent, element);
}

/**
 * Create a table element found in the page with the given absolute locator.
 *
 * @param page The page in which the table element belongs to
 */
public SpotSimpleTableElement(final WebPage page, final By locator) {
	super(page, locator);
}

@Override
protected By getHeaderElementsLocator() {
	return HEADERS_LOCATOR;
}

@Override
protected By getRowCellsElementsLocator() {
	return CELLS_LOCATOR;
}

@Override
public WebBrowserElement getRowElementContainingText(final String text) {
	return this.element.waitForElement(By.xpath(String.format(MATCHING_ROW_XPATH, text)), shortTimeout());
}

@Override
protected By getRowElementsLocator() {
	return ROWS_LOCATOR;
}
}
