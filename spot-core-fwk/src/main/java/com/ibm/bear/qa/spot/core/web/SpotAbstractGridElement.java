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

import static com.ibm.bear.qa.spot.core.browser.BrowserConstants.NO_BROWSER_ELEMENT_FOUND;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.List;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.api.elements.SpotGrid;
import com.ibm.bear.qa.spot.core.api.elements.SpotTable;
import com.ibm.bear.qa.spot.core.scenario.errors.*;
import com.ibm.bear.qa.spot.core.timeout.SpotAbstractTimeout;
import com.ibm.bear.qa.spot.core.timeout.SpotTextTimeout;
import com.ibm.bear.qa.spot.core.utils.StringUtils.Comparison;

/**
 * Class to handle common code for web element with <code>table</code> tag name using grid
 * specifications.
 * <p>
 * This class implements following public API methods of {@link SpotGrid} interface:
 * <ul>
 * <li>{@link #applySortMode(String,SortMode)}: Apply the given sort mode to the given column.</li>
 * <li>{@link #getColumnSortMode(String)}: Return the sort mode of the column matching the given name.</li>
 * <li>{@link #getSortedColumn()}: Return the name of the column which has a sorting activated.</li>
 * </ul>
 * </p><p>
 * This class implements following public API methods of {@link SpotTable} interface:
 * <ul>
 * <li>{@link #getRowElementContainingText(String)}: Return the row web element containing a cell with the given text.</li>
 * <li>{@link #isEmpty()}: Return whether the table is empty or not.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following internal API methods:
 * <ul>
 * <li>{@link #getRowElements()}: Return the list of row web elements.</li>
 * <li>{@link #waitForTableToBeLoaded()}: Wait for the table to be loaded.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getHeaderElementsLocator()}: Return the locator to find header web elements in the displayed grid container element.</li>
 * <li>{@link #getRowCellsElementsLocator()}: Return the locator to find cells elements of a row displayed in the grid table element.</li>
 * <li>{@link #getRowElements(boolean)}: Return the list of row web elements including hidden elements or not.</li>
 * <li>{@link #getRowElementsLocator()}: Return the locator to find row web elements in the displayed table element.</li>
 * <li>{@link #getSortMode(WebBrowserElement)}: Return the sorting state of the given column header element.</li>
 * <li>{@link #getStatusMessageElement()}: The status message element.</li>
 * </ul>
 * </p>
 */
public abstract class SpotAbstractGridElement extends SpotAbstractTableElement implements SpotGrid {

	/* Constants*/
	private static final By DEFAULT_GRID_TABLE_LOCATOR = By.className("bx--grid");
	private static final String THERE_ARE_NO_ITEMS_TO_DISPLAY = "There are no items to display";
	private static final String NO_ITEMS_TO_DISPLAY = "No items to display";

	/* Fields */
	// Temporary header element storage
	// Be cautious with this field which has to be used only inside a method to avoid getting this web element multiple times
	// When using it the first time in a method, always assume it's either null or stale. Hence, reinitialize it first using getHeaderElement(String) method...
	private WebBrowserElement headerElement;
	// Flag to filter rows which are not checked
	protected boolean onlyChecked = false;

public SpotAbstractGridElement(final WebElementWrapper parent) {
	super(parent, DEFAULT_GRID_TABLE_LOCATOR);
}

public SpotAbstractGridElement(final WebElementWrapper parent, final By locator) {
	super(parent, locator);
}

public SpotAbstractGridElement(final WebElementWrapper parent, final WebBrowserElement element) {
	super(parent, element);
}

public SpotAbstractGridElement(final WebPage page) {
	super(page);
}

public SpotAbstractGridElement(final WebPage page, final By locator) {
	super(page, locator);
}

public SpotAbstractGridElement(final WebPage page, final By locator, final WebBrowserFrame frame) {
	super(page, locator, frame);
}

public SpotAbstractGridElement(final WebPage page, final WebBrowserElement element) {
	super(page, element);
}

public SpotAbstractGridElement(final WebPage page, final WebBrowserElement element, final WebBrowserFrame frame) {
	super(page, element, frame);
}

public SpotAbstractGridElement(final WebPage page, final WebBrowserFrame frame) {
	super(page, frame);
}

@Override
public void applySortMode(final String column, final SortMode mode) throws ScenarioFailedError {
	if (DEBUG) debugPrintln("		+ Apply sort mode '"+mode+"' to column '"+column+"'.");

	// Check if it's necessary to set mode
	SortMode currentMode = getColumnSortMode(column);
	if (currentMode == mode) {
		if (DEBUG) debugPrintln("		  -> sort mode was already active on the column, do nothing...");
		return;
	}
	if (currentMode == null) {
		throw new ScenarioFailedError(column + " column is not sortable");
	}

	// Click on header element until it's mode matches the expected one
	this.headerElement.makeVisible();
	while (true) {
		this.headerElement.click();
		waitForTableToBeLoaded();
		SortMode newMode = getSortMode(this.headerElement);
		if (newMode == mode) {
			// We found the expected mode, leave now
			break;
		}
		if (newMode == currentMode) {
			// It seems that we're looping... raise error to avoid infinite loop
			throw new ScenarioFailedError("For an unknown reason it was not possible to set sort mode '"+mode+"'.");
		}
	}
}

private WebBrowserElement getBodyEmptyElement() throws WaitElementTimeoutError, MultipleElementsFoundError {
	return this.element.waitShortlyForMandatoryChildElement(By.className("gridxBodyEmpty"));
}

@Override
public SortMode getColumnSortMode(final String column) throws ScenarioFailedError {
	if (getHeaderElement(column) == null) {
		throw new ScenarioFailedError("There's no column '"+column+"' in the current table.");
	}
	return getSortMode(this.headerElement);
}

/**
 * {@inheritDoc}
 * <p>
 * Note that this method is setting {@link #headerElement} field.
 * </p>
 */
@Override
public WebBrowserElement getHeaderElement(final String column) {
	return this.headerElement = super.getHeaderElement(column);
}

/**
 * Return the locator to find header web elements in the displayed grid container element.
 * <p>
 * By default these are web elements with <code>td</code> tag name and having
 * the <code>@role='columnheader'</code> attribute. Subclass might want to override
 * this method if the way to get these elements is different.
 * </p>
 * @return The header web elements locator
 */
@Override
protected By getHeaderElementsLocator() {
	return By.cssSelector("td[role='columnheader']");
}

/**
 * {@inheritDoc}
 * <p>
 * For table grid, default row cell elements are web elements with <code>class</code>
 * attribute containing <code>gridxCell</code>. However, subclasses might want
 * to override this method if the way to get these elements is different.
 * </p>
 */
@Override
protected By getRowCellsElementsLocator() {
	return By.className("gridxCell");
}

/**
 * Return the row web element containing a cell with the given text, whether it is displayed or hidden.
 *
 * @return The row web element or <code>null</code> if no row was found
 */
@Override
public WebBrowserElement getRowElementContainingText(final String text) {
	for (WebBrowserElement rowElement: getRowElements(false)) {
		if (!rowElement.isDisplayed()) {
			rowElement.moveToElement();
		}
		if (rowElement.findElement(By.xpath(".//*[text()='"+text+"']")) != null) {
			return rowElement;
		}
	}
	return null;
}

/**
 * {@inheritDoc}
 * <p>
 * Additionally look at empty element text (initialized in {@link #waitForTableToBeLoaded()}
 * to figure out whether there should be items displayed in the table or not.
 * </p>
 */
@Override
public List<WebBrowserElement> getRowElements() {
	return getRowElements(true);
}

/**
 * Return the list of row web elements including hidden elements or not.
 * <p>
 * First check whether the table might be empty or not.
 * </p>
 * @param displayed Tells whether to only considered displayed elements or hidden ones as well
 */
protected List<WebBrowserElement> getRowElements(final boolean displayed) {

	// Wait for the loading to be finished first
	WebBrowserElement bodyEmptyElement = waitForTableToBeLoaded();

	// Then check the empty element text to know whether items are displayed or not
	String text = bodyEmptyElement == null ? EMPTY_STRING : bodyEmptyElement.getText();
	switch (text) {
		case EMPTY_STRING:
			return displayed
				? this.element.waitForPotentialDisplayedChildrenElements(getRowElementsLocator(), shortTimeout())
				: this.element.waitForPotentialChildrenElements(getRowElementsLocator(), shortTimeout());
		case THERE_ARE_NO_ITEMS_TO_DISPLAY:
		case NO_ITEMS_TO_DISPLAY:
			return NO_BROWSER_ELEMENT_FOUND;
	}
	throw new ScenarioImplementationError("Unexpected empty element text: " + text);
}

/**
 * {@inheritDoc}
 * <p>
 * For table grid, default row elements are web elements with <code>class</code>
 * attribute containing <code>gridxRow</code>. However, subclasses might want
 * to override this method if the way to get these elements is different.
 * </p>
 * @return The rows web elements locator
 */
@Override
protected By getRowElementsLocator() {
	return By.cssSelector(".gridxRow" + (this.onlyChecked?".gridxRowSelected":EMPTY_STRING));
}

@Override
public String getSortedColumn() {
	List<WebBrowserElement> headersElement = getHeaderElements();
	for (WebBrowserElement hElement: headersElement) {
		if (getSortMode(hElement) != SortMode.NoSort) {
			return hElement.getText();
		}
	}
	return null;
}

/**
 * Return the sorting state of the given column header element.
 * <p>
 * By default a column is sorted if its header element has the <code>aria-sort</code>
 * attribute equals to one of the attribute value of {@link SortMode} enumeration.
 * </p><p>
 * Subclass might want to override this behavior if the sorting flag is specifically
 * managed in the corresponding table element.
 * </p>
 * @param hElement The header element to get sorting mode
 * @return The sorting mode as a {@link SortMode} value or <code>null</code>
 * if the column is not sortable
 */
@SuppressWarnings("javadoc")
protected SortMode getSortMode(final WebBrowserElement hElement) {
	String ariaSortAttribute = hElement.getAttribute("aria-sort");
	if (ariaSortAttribute == null) {
		return null;
	}
	return SortMode.fromAttribute(ariaSortAttribute);
}

/**
 * The status message element.
 * <p>
 * Note that this element is not relative to the current wrapped web element,
 * explaining why it uses {@link WebBrowser#waitForElements(WebBrowserElement, By, boolean, int, boolean)}
 * method instead of {@link #waitShortlyForMandatoryChildElement(By)}...
 * </p><p>
 * TODO See if there wouldn't be a better place to implement this method (eg {@link WebPage}...
 * </p>
 * @return The corresponding web element
 * @throws WaitElementTimeoutError If the element is not found or not visible in the page
 */
protected WebBrowserElement getStatusMessageElement() {
	return this.browser.waitForElement(null, By.id("statusMessage"), /*fail:*/true, shortTimeout(), /*displayed:*/false, /*single:*/true);
}

@Override
public boolean isEmpty() {
	return getBodyEmptyElement().isDisplayed();
}

/**
 * {@inheritDoc}
 * <p>
 * First check whether there would not be any work in progress with the server.
 * Then wait both for the 'Loading...' to disappear from the table content and
 * for the loading element to be no longer displayed.
 * </p>
 */
@Override
public WebBrowserElement waitForTableToBeLoaded() {
	debugPrintEnteringMethod();

	// Wait for the working operation to finish before doing anything else
	SpotTextTimeout statusTimeout = new SpotTextTimeout("Working", Comparison.StartsWith, getStatusMessageElement());
	statusTimeout.waitWhile(openTimeout());

	// Get empty and loading elements
	final WebBrowserElement bodyEmptyElement = getBodyEmptyElement();
	final WebBrowserElement loadElement = this.element.waitShortlyForMandatoryChildElement(By.className("gridxLoad"));

	// Wait for loading operation end
	SpotAbstractTimeout timeout = new SpotAbstractTimeout() {
		@Override
		protected boolean getCondition() {
			return bodyEmptyElement.isDisplayed() && !bodyEmptyElement.getText().startsWith("Loading") && !loadElement.isDisplayed();
		}
		@Override
		protected String getConditionLabel() {
			return "Table loading operation to be finished";
		}
	};
	timeout.waitUntil(openTimeout());
	return bodyEmptyElement;
}
}
