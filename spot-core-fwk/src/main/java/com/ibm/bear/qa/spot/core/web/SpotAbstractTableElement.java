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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;

import com.ibm.bear.qa.spot.core.api.elements.SpotTable;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to handle common code for web elements with <code>table</code> tag name.
 * <p>
 * This class defines following public API methods of {@link SpotTable} interface:
 * <ul>
 * <li>{@link #contains(String,String)}: Tells whether the given column contains a cell with the given text.</li>
 * <li>{@link #getColumnContent(String)}: Return the content of the table given column.</li>
 * <li>{@link #getColumnHeaders()}: Return the list of displayed columns.</li>
 * <li>{@link #getColumnsSize()}: Return the table columns size.</li>
 * <li>{@link #getContent()}: Return the content of the table as a list of strings list.</li>
 * <li>{@link #getHeaderIndex(String)}: Return the header index of the given column name.</li>
 * <li>{@link #getNumberOfRows()}: Get the number of rows of the table.</li>
 * <li>{@link #isColumnDisplayed(String)}: Check if the given column is displayed.</li>
 * </ul>
 * </p><p>
 * This class also defines following internal API methods:
 * <ul>
 * <li>{@link #getCellElement(int,int)}: Return the cell web element at the given row for the given column index.</li>
 * <li>{@link #getCellElement(int,String)}: Return the cell web element containing the given text for the given column index.</li>
 * <li>{@link #getCellElements(int)}: Return the list of cell web elements for the given column index.</li>
 * <li>{@link #getCellElements(String)}: Return the list of cell web elements for the given column name.</li>
 * <li>{@link #getColumnCellElementAtIndex(String,int)}: Get the cell web element for the given column at the given row index.</li>
 * <li>{@link #getHeaderElement(String)}: Return the header web element which text is matching the given column name.</li>
 * <li>{@link #getHeaderElements()}: Return the list of header web elements.</li>
 * <li>{@link #getRowElementContainingText(String)}: Return the row web element containing a cell with the given text.</li>
 * <li>{@link #getRowElements()}: Return the list of row web elements.</li>
 * <li>{@link #waitForTableToBeLoaded()}: Wait for the table to be loaded.</li>
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
public abstract class SpotAbstractTableElement extends WebElementWrapper implements SpotTable {

	/* Fields */
	// Be cautious with this field which has to be used only inside a method to avoid getting this web element multiple times
	// When using it the first time in a method, always assume it's either null or stale. Hence, reinitialize it first using getHeaderElement(String) method...
	WebBrowserElement headerElement;

public SpotAbstractTableElement(final WebElementWrapper parent, final By locator) {
	super(parent, locator);
}

public SpotAbstractTableElement(final WebElementWrapper parent, final WebBrowserElement element) {
	super(parent, element);
}

public SpotAbstractTableElement(final WebPage page) {
	super(page);
}

public SpotAbstractTableElement(final WebPage page, final By locator) {
	super(page, locator);
}

public SpotAbstractTableElement(final WebPage page, final By locator, final WebBrowserFrame frame) {
	super(page, locator, frame);
}

public SpotAbstractTableElement(final WebPage page, final WebBrowserElement element) {
	super(page, element);
}

public SpotAbstractTableElement(final WebPage page, final WebBrowserElement element, final WebBrowserFrame frame) {
	super(page, element, frame);
}

public SpotAbstractTableElement(final WebPage page, final WebBrowserFrame frame) {
	super(page, frame);
}

@Override
public boolean contains(final String column, final String text) {
	int index = getHeaderIndex(column);
	if (index < 0) {
		throw new ScenarioFailedError("There's no column named '"+column+"'.");
	}
	return getCellElement(index, text) != null;
}

/**
 * Return the cell web element at the given row for the given column index.
 *
 * @param column The 0-based index of the table columns
 * @param row The 0-based index of table rows
 * @return The web element at column and row indexes
 * @throws ScenarioFailedError If the both indexes value exceeds the number of
 * columns or rows in the table or if it's negative.
 */
public WebBrowserElement getCellElement(final int column, final int row) {
	List<WebBrowserElement> cellElements = getCellElements(column);
	if (row >= cellElements.size()) {
		throw new ScenarioFailedError("Row index "+row+" exceeds the total number of table rows ("+cellElements.size()+").");
	}
	return cellElements.get(row);
}

/**
 * Return the cell web element containing the given text for the given column index.
 *
 * @param column The 0-based index of the table columns
 * @return The web element or <code>null</code> if the element is not found.
 * @throws ScenarioFailedError If the index value exceeds the number of columns
 * in the table or if it's negative.
 */
public WebBrowserElement getCellElement(final int column, final String text) {
	List<WebBrowserElement> cellElements = getCellElements(column);
	for (WebBrowserElement cellElement: cellElements) {
		if (cellElement.getText().equals(text)) {
			return cellElement;
		}
	}
	return null;
}

/**
 * Return the list of cell web elements for the given column index.
 * <p>
 * See {@link #getRowElements()} to know how rows are retrieved in the table grid element.
 * </p>
 * @param column The 0-based index of the table columns
 * @return The web elements list
 * @throws ScenarioFailedError If the index value exceeds the number of columns
 * in the table or if it's negative.
 */
public List<WebBrowserElement> getCellElements(final int column) throws ScenarioFailedError {

	// Check column argument
	if (column < 0) {
		throw new ScenarioFailedError("Column index must be positive.");
	}
	int size = getColumnsSize();
	if (column >= size) {
		throw new ScenarioFailedError("Invalid 0-based index "+column+" although table has only "+size+" columns.");
	}

	// Build cells list
	List<WebBrowserElement> rows = getRowElements();
	List<WebBrowserElement> cells = new ArrayList<>(rows.size());
	for (WebBrowserElement rowElement: rows) {
		try {
			List<WebBrowserElement> rowCells = rowElement.waitShortlyForMandatoryDisplayedChildrenElements(getRowCellsElementsLocator());
			cells.add(rowCells.get(column));
		} catch (@SuppressWarnings("unused") StaleElementReferenceException | NoSuchElementException ex) {
			// Skip row which has disappeared
			debugPrintln("A row has been removed from the list, ignore it ("+rowElement.getLocator()+")");
		}
	}

	// Return list
	return cells;
}

/**
 * Return the list of cell web elements for the given column name.
 * <p>
 * See {@link #getCellElements(int)} to know how the cells elements
 * are found in the table.
 * </p>
 * @param column The table column name
 * @return The web elements list.
 * @throws ScenarioFailedError If the index value exceeds the number of columns
 * in the table or if it's negative.
 */
public List<WebBrowserElement> getCellElements(final String column) {
	int index = getHeaderIndex(column);
	return getCellElements(index);
}

/**
 * Get the cell web element for the given column at the given row index.
 *
 * @param column The table column name
 * @param rowIndex The row index
 * @return The cell web element for the given column at the given row index.
 *
 * @throws ScenarioFailedError If the rowIndex value exceeds the number of rows
 * in the table or if it's negative.
 */
public WebBrowserElement getColumnCellElementAtIndex(final String column, final int rowIndex) throws ScenarioFailedError {
	List<WebBrowserElement> cellElements = getCellElements(column);
	int size = cellElements.size();
	if (size == 0) {
		return null;
	}

	// Check index argument
	if (rowIndex < 0) {
		throw new ScenarioFailedError("Row index must be positive.");
	}
	if (rowIndex >= size) {
		throw new ScenarioFailedError("Invalid 0-based index "+rowIndex+" although table has only "+size+" rows.");
	}
	return cellElements.get(rowIndex);
}

@Override
public List<String> getColumnContent(final String column) {
	List<WebBrowserElement> cellElements = getCellElements(column);
	List<String> cells = new ArrayList<>(cellElements.size());
	for (WebBrowserElement cellElement: cellElements) {
		cells.add(cellElement.getText());
	}
	return cells;
}

@Override
public List<String> getColumnHeaders() {
	List<WebBrowserElement> headersElement = getHeaderElements();
	List<String> headers = new ArrayList<String>(headersElement.size());
	for (WebBrowserElement hElement: headersElement) {
		WebBrowserElement labelElement = hElement.findElement(By.className("gridxSortNode"));
		headers.add(labelElement==null ? hElement.getText() : labelElement.getText());
	}
	return headers;
}

@Override
public int getColumnsSize() {
	return getHeaderElements().size();
}

@Override
public List<List<String>> getContent() {
	List<WebBrowserElement> rowElements = getRowElements();
	List<List<String>> rows = new ArrayList<>(rowElements.size());
	for (WebBrowserElement rowElement: rowElements) {
		List<WebBrowserElement> cellElements = rowElement.waitShortlyForMandatoryDisplayedChildrenElements(getRowCellsElementsLocator());
		List<String> cells = new ArrayList<>(cellElements.size());
		for (WebBrowserElement cellElement: cellElements) {
			cells.add(cellElement.getText());
		}
		rows.add(cells);
	}
	return rows;
}

/**
 * Return the header web element which text is matching the given column name.
 * <p>
 * Note that this method is setting {@link #headerElement} field.
 * </p>
 * @param column The column to be found
 * @return The header element as a {@link WebBrowserElement} or <code>null</code>
 * if there's no column with the given name in the current table
 */
public WebBrowserElement getHeaderElement(final String column) {
	List<WebBrowserElement> headersElement = getHeaderElements();
	for (WebBrowserElement hElement: headersElement) {
		WebBrowserElement labelElement = hElement.findElement(By.className("gridxSortNode"));
		String headerText = labelElement==null ? hElement.getText() : labelElement.getText();
		if (headerText.equals(column)) {
			return this.headerElement = hElement;
		}
	}
	return null;
}

/**
 * Return the list of header web elements.
 * <p>
 * By default these are web elements with <code>td</code> tag name and having
 * the <code>@role='columnheader'</code> attribute. Subclass might want
 * to override {@link #getHeaderElementsLocator()} method if the way
 * to get these elements is different.
 * </p>
 * @return The web elements as a {@link List} of {@link WebBrowserElement}.
 */
public List<WebBrowserElement> getHeaderElements() {
	return this.element.waitShortlyForMandatoryDisplayedChildrenElements(getHeaderElementsLocator());
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
abstract protected By getHeaderElementsLocator();

@Override
public int getHeaderIndex(final String column) {
	List<String> headers = getColumnHeaders();
	return headers.indexOf(column);
}

@Override
public int getNumberOfRows(){
	return getRowElements().size();
}

/**
 * Return the locator to find cells elements of a row displayed in the grid table element.
 * <p>
 * Subclasses must override this method to specify their specific way to get these
 * elements.
 * </p>
 * @return The rows locator
 */
abstract protected By getRowCellsElementsLocator();

/**
 * Return the row web element containing a cell with the given text.
 *
 * @return The row web element or <code>null</code> if no row was found
 */
abstract public WebBrowserElement getRowElementContainingText(final String text);

/**
 * Return the list of row web elements.
 *
 * @return The web elements as a {@link List} of {@link WebBrowserElement}.
 */
public List<WebBrowserElement> getRowElements() {
	waitForTableToBeLoaded();
	if (isEmpty()) {
		return Collections.emptyList();
	}
	return this.element.waitForPotentialDisplayedChildrenElements(getRowElementsLocator(), shortTimeout());
}

/**
 * Return the locator to find row web elements in the displayed table element.
 * <p>
 * Subclasses must override this method to specify their specific way to get these
 * elements.
 * </p>
 * @return The rows web elements locator
 */
abstract protected By getRowElementsLocator();


@Override
public boolean isColumnDisplayed(final String columnTitle) {
	return getColumnHeaders().contains(columnTitle);
}

/**
 * {@inheritDoc}
 * <p>
 * By default, it's using row elements locator to check whether table as rows or not.
 * </p>
 * <p>
 * Subclasses might want to override this behavior which can be strongly improved
 * if the table as a specific element displayed when the table is empty.
 * </p>
 */
@Override
public boolean isEmpty() {
	List<WebBrowserElement> rowElements = this.element.waitForPotentialDisplayedChildrenElements(getRowElementsLocator(), 1);
	return rowElements == null || rowElements.size() == 0;
}

/**
 * Wait for the table to be loaded.
 * <p>
 * Default is to do nothing.
 * </p>
 * @return An element displaying text when the table is empty
 * or <code>null</code> if there's no such element in the table
 */
public WebBrowserElement waitForTableToBeLoaded() {
	return null;
}
}
