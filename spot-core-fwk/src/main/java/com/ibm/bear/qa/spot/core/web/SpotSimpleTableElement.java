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
package com.ibm.bear.qa.spot.core.web;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.api.elements.SpotTable;

/**
 * Class to manage web element with <code>table</code> tag.
 * <p>
 * This class implements following public API methods of {@link SpotTable} interface:
 * <ul>
 * <li>{@link #isEmpty()}: Return whether the table is empty or not.</li>
 * </ul>
 * </p><p>
 * This class defines or overrides following internal API methods:
 * <ul>
 * <li>{@link #getRowElementContainingText(String)}: Return the row web element containing a cell with the given text.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getHeaderElementsLocator()}: Return the locator to find header web elements in the displayed grid container element.</li>
 * <li>{@link #getRowCellsElementsLocator()}: Return the locator to find cells elements of a row displayed in the grid table element.</li>
 * <li>{@link #getRowElementsLocator()}: Return the locator to find row web elements in the displayed table element.</li>
 * </ul>
 * </p>
 */
public class SpotSimpleTableElement extends SpotAbstractTableElement {

	/* Constants */
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
	super(parent, By.tagName("table"));
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

/**
 * Create a table element with the given page and web element.
 *
 * @param page The page in which the table element belongs to
 * @param element The table web element
 */
public SpotSimpleTableElement(final WebPage page, final WebBrowserElement element) {
	super(page, element);
}

@Override
protected By getHeaderElementsLocator() {
	return HEADERS_LOCATOR;
}

/**
 * {@inheritDoc}
 * <p>
 * For simple table, default row cell elements are web elements with <code>td</code>
 * tag. However, subclasses might want to override this method if the way to get
 * these elements is different.
 * </p>
 * @return The rows locator
 */
@Override
protected By getRowCellsElementsLocator() {
	return CELLS_LOCATOR;
}

@Override
public WebBrowserElement getRowElementContainingText(final String text) {
	return this.element.waitForPotentialDisplayedChildElement(By.xpath(String.format(MATCHING_ROW_XPATH, text)), shortTimeout());
}

/**
 * {@inheritDoc}
 * <p>
 * For simple table, default row cell elements are web elements with <code>tr</code>
 * tag. However, subclasses might want to override this method if the way to get
 * these elements is different.
 * </p>
 * @return The rows web elements locator
 */
@Override
protected By getRowElementsLocator() {
	return ROWS_LOCATOR;
}

@Override
public boolean isEmpty() {
	WebBrowserElement bodyElement = this.element.findElement(By.tagName("tbody"));
	return bodyElement == null || !bodyElement.isDisplayed() || bodyElement.getChildren().size() == 0;
}
}
