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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;

import com.ibm.bear.qa.spot.core.config.Timeouts;
import com.ibm.bear.qa.spot.core.scenario.ScenarioUtils;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to handle a CKEditor element based on a iframe.
 * <p>
 * Note that the corresponding iframe is found by default using {@link #CKEDITOR_IFRAME_LOCATOR} locator.
 * </p>
 * <p>
 * Following features are accessible in this page:
 * <ul>
 * <li>{@link #addHtml(String)}: Adds the provided html string to the editors content</li>
 * <li>{@link #addNewLine()}: Adds a new line to the end of the editors content</li>
 * <li>{@link #addText(String)}: Adds the provided text string to the editors content</li>
 * <li>{@link #addText(String, boolean)}: Adds the provided text string to the editors content. If true is passed a new line will be added after the content.</li>
 * <li>{@link #clearContent()}: clears the editor content</li>
 * <li>{@link #getAllInsertedTablesContent()}: Get a list of the content for all inserted tables in the editor.</li>
 * <li>{@link #getHtmlContent()}: Return the content of the current requirement as a html string.</li>
 * <li>{@link #getInsertedTableContent()}: Return the content of the last table.</li>
 * <li>{@link #getInsertedTableContent(int)}: Get the content for the table inserted at the given index in the editor.</li>
 * <li>{@link #getInsertedTablesCount()}: Return the number of tables inserted in the editor.</li>
 * <li>{@link #getTextContent()}: Return the content of the current requirement as a text string.</li>
 * <li>{@link #moveCursorToBeginning()}: Moves the cursor to the beginning of the editor.</li>
 * <li>{@link #moveCursorToEnd()}: Moves the cursor to the end of the editor.</li>
 * <li>{@link #setContent(String)}: Sets the content of the editor with the given text as plain text.</li>
 * <li>{@link #setContent(String, boolean)}: Sets the content of the editor with the given text using the given format.</li>
 * </ul>
 * </p>
 */
public class WebCKEditorFramedElement extends WebElementWrapper {

	/* Locators */
	private static final By CKEDITOR_IFRAME_LOCATOR = By.xpath("//iframe[contains(@title,'Rich Text Editor')]");

	/* Constants */
	private static final String EDITOR_BEGINNING = "Start";
	private static final String EDITOR_END = "End";

	/* Fields */
	private String editorInstance;

public WebCKEditorFramedElement(final WebPage page) {
	this(page, CKEDITOR_IFRAME_LOCATOR);
}

public WebCKEditorFramedElement(final WebPage page, final By editorIframeLocator) {
	super(page, page.getBrowser().waitForMandatoryDisplayedElement(editorIframeLocator, Timeouts.DEFAULT_TIMEOUT), new WebElementFrame(page.getBrowser(), editorIframeLocator));
	setEditorInstance();
}

public WebCKEditorFramedElement(final WebPage page, final WebBrowserElement editorIframeElement) {
	super(page, editorIframeElement, new WebElementFrame(page.getBrowser(), editorIframeElement));
	setEditorInstance();
}

/**
 * Adds a html string to the editor's content
 *
 * @param html A HTML string to add to the editor's content
 */
public void addHtml(final String html) {
	executeEditorScript(".insertHtml('" + html + "')");
}

/**
 * Adds a new line to the end of the editors content
 */
public void addNewLine() {
	// Add line return at end of content
	moveCursorToEnd();
	this.element.sendKeys(Keys.ENTER);
}

/**
 * Adds a text string to the editors content
 *
 * @param text A text string to add to the editor's content
 */
public void addText(final String text) {
	addText(text, false);
}

/**
 * Adds a text string to the editors content
 *
 * @param text A text string to add to the editor's content
 * @param addNewLine If true, a new line will be added after the new text
 */
public void addText(final String text, final boolean addNewLine) {
	addText(text, addNewLine, false);
}

/**
 * Adds a text string to the editors content
 *
 * @param text A text string to add to the editor's content
 * @param addNewLine If true, a new line will be added after the new text
 * @param save If true, perform a save 'Ctrl + s' event
 */
public void addText(final String text, final boolean addNewLine, final boolean save){
	executeEditorScript(".insertText('" + text + "')");
	if (addNewLine) {
		this.element.sendKeys(Keys.ENTER);
	}
	if (save) {
		saveContent();
	}
}

/**
 * Clears the editor's content
 */
public void clearContent() {
	// Select all of the editors content
	executeEditorScript(".execCommand('selectAll')");
	// Set the content to an empty string
	addText("");
}

private Object executeEditorScript(final String script) {
	return this.browser.executeScript("return " + getEditorScriptRoot() + script + ";");
}

/**
 * Fills last inserted table with the given content.
 * <p>
 * The provided content data can have less rows and/or columns than the specified
 * rows and columns number but must not have more. Missing rows and columns
 * will just lead to empty cells while filling the table. <code>null</code> slot in the
 * arrays will also be skipped and let the corresponding cell empty in the table.
 * </p><p>
 * @param content The table content to be set
 * @throws ScenarioFailedError If there's no table in the editor or if the
 * given content is <code>null</code> or if there are more rows or columns in
 * the provided content than in the editor table.
 */
public void fillInsertedTable(final String[][] content) throws ScenarioFailedError {
	if (DEBUG) debugPrintln("		+ Get the content of last inserted table.");
	int index = getInsertedTablesCount() - 1;
	fillInsertedTable(content, index);
}

/**
 * Fills inserted table at the given index with the given content.
 * <p>
 * The provided content data can have less rows and/or columns than the specified
 * rows and columns number but must not have more. Missing rows and columns
 * just will lead to empty cells while filling the table. <code>null</code> slot in the
 * arrays will also be skipped and let the corresponding cell empty in the table.
 * </p><p>
 * @param content The table content to be set
 * @param index The table index
 * @throws ScenarioFailedError If there's no table at the given index or if the
 * given content is <code>null</code> or if there are more rows or columns in
 * the provided content than in the editor table.
 */
public void fillInsertedTable(final String[][] content, final int index) {
	if (DEBUG) debugPrintln("		+ Set inserted table #"+index+" content in CKEditor.");

	// Content should not be null
	if (content == null) {
		throw new ScenarioFailedError("Cannot set table content with null content");
	}
	if (content.length == 0) {
		throw new ScenarioFailedError("Cannot set table content with empty content");
	}

	// Get table element
	WebBrowserElement tableElement = getInsertedTableElement(index);

	// Move to the beginning of the table element
	String tableID = tableElement.getAttributeValue("id");
	if (tableID.length() > 0) {
		moveCursorToElementEditStart(tableID);
	}

	// Check rows size
	int rowsSize = tableElement.waitForPotentialDisplayedChildrenElements(By.xpath(".//tr"), shortTimeout()).size();
	final int rows = content.length;
	if (rows != rowsSize) {
		throw new ScenarioFailedError("Unexpected number of rows: "+rowsSize+" found in table element and "+rows+" data rows provided.");
	}

	// Store columns size
	int columnsSize = tableElement.waitShortlyForMandatoryDisplayedChildrenElements(By.xpath(".//tr[1]//td")).size();

	// Select frame as the table elements are in the CKEditor frame
	selectFrame();

	// Parse each row of the table
	try {
		fillTable(content, rowsSize, columnsSize);
	}
	finally {
		resetFrame();
	}
}

private void fillTable(final String[][] content, final int rowsSize, final int columnsSize) throws ScenarioFailedError {

	// Initialize row counter
	int row = 0;

	// For each row data
	for (String[] line: content) {

		// Skip the row if the corresponding content is null
		if (line == null) {
			continue;
		}

		// Check columns size
		final int columns = line.length;
		if (columns > columnsSize) {
			throw new ScenarioFailedError("Unexpected number of columns: table has "+columnsSize+" columns and a line of data has "+columns+" columns: "+getTextFromList(line, "|"));
		}

		// Parse each cell of the row
		int col = 0;
		while (col < columnsSize) {

			// Skip the cell if the corresponding content is null
			if (line[col] == null) {
				continue;
			}

			// Type the content into the cell
			this.browser.sendKeys(line[col]);

			// Jump to next column
			if (++col < columnsSize) {

				// Keys.ARROW_RIGHT not only works for rm, but also works for qm
				this.browser.sendKeys(Keys.ARROW_RIGHT);
			}
		}

		// Jump to next row
		if (++row < rowsSize) {

			// Keys.ARROW_RIGHT not only works for rm, but also works for qm
			this.browser.sendKeys(Keys.ARROW_RIGHT);
		}
	}
}

/**
 * Get a list of the content for all inserted tables in the editor.
 *
 * @return The matrixes list representing the content of all tables in the editor.
 */
public List<String[][]> getAllInsertedTablesContent() {

	// Select frame
	selectFrame();

	// Get all table elements
	List<WebBrowserElement> tableElements = getInsertedTableElements();

	// Build list of contents
	List<String[][]> tablesContent = new ArrayList<String[][]>(tableElements.size());
	for(WebBrowserElement tableElement : tableElements) {
		tablesContent.add(getInsertedTableContent(tableElement));
	}

	// Return list of contents
	return tablesContent;
}

private String getContent(final boolean isHtml) {
	String content = null;
	try {
		String script = (isHtml) ? ".getData()" : ".document.getBody().getText()";
		content = (String) executeEditorScript(script);
	} catch (Throwable t) {
		ScenarioUtils.printException(t);
	}
	return content;
}

private String getEditorScriptRoot() {
	resetFrame(); // CKEDITOR singleton is only available in main document...
	return "CKEDITOR.instances['" + this.editorInstance + "']";
}

/**
 * Returns the editor's content as a html string
 */
public String getHtmlContent() {
	return getContent(true);
}

private String getInsertedTableCellContent(final WebBrowserElement cell) {

	// Get text
	String text = cell.getText();

	// TODO AFAIK, getText() will *never* return null
	if (text == null) {
		// text is sometimes inside a span tag
		WebElement cellContentElement = cell.findElement(By.xpath(".//span"));
		text = cellContentElement.getText();
	}

	// Replace all NBSP with normal space to make the comparison easier
	return text.replaceAll("[\\u00A0]+$", SPACE_STRING);
}

/**
 * Return the content of the last table inserted in the editor.
 *
 * @return The cells of each table rows as a matrix of {@link String} or
 * <code>null</code> if the editor has no table inserted.
 */
public String[][] getInsertedTableContent() {
	if (DEBUG) debugPrintln("		+ Get the content of last inserted table.");
	int index = getInsertedTablesCount() - 1;
	return getInsertedTableContent(index);
}

/**
 * Get the content for the table inserted at the given index in the editor.
 *
 * @param index The index of the inserted table.
 * @return The table cells as a matrix (ie. an array of arrays) of {@link String} or
 * <code>null</code> if the index is over the number of inserted tables in the editor.
  */
public String[][] getInsertedTableContent(final int index) {
	if (DEBUG) debugPrintln("		+ Get the content of inserted table #"+index+".");

	// Get corresponding table
	WebBrowserElement tableElement = getInsertedTableElement(index);
	if (tableElement == null) {
		return null;
	}

	// Return table content
	return getInsertedTableContent(tableElement);
}

private String[][] getInsertedTableContent(final WebBrowserElement tableElement) {

	// Select frame
	selectFrame();

	// Get table row elements
	List<WebBrowserElement> rowElements = tableElement.waitForPotentialDisplayedChildrenElements(By.xpath(".//tr"), shortTimeout());

	// Build table content
	String[][] tableContent = new String[rowElements.size()][];
	int i = 0;
	for(WebBrowserElement rowElement : rowElements) {
		tableContent[i++] = getInsertedTableRowContent(rowElement);
	}

	// Reset frame
	resetFrame();

	// Return table content
	return tableContent;
}

private WebBrowserElement getInsertedTableElement(final int index) {
	List<WebBrowserElement> tableElements = getInsertedTableElements();
	if (index < tableElements.size()) {
		return tableElements.get(index);
	}
	return null;
}

private List<WebBrowserElement> getInsertedTableElements() {
	try {
		selectFrame();
		return waitForMandatoryDisplayedPageElements(By.xpath(".//table"));
	}
	finally {
		resetFrame();
	}
}

private String[] getInsertedTableRowContent(final WebBrowserElement row) {

	// Get row cell elements
	List<WebBrowserElement> cellElements = row.getChildren("td");

	// Build string array with cells content
	String[] cells = new String[cellElements.size()];
	int i = 0;
	for(WebBrowserElement cell : cellElements) {
		cells[i++] = getInsertedTableCellContent(cell);
	}

	// Return resutl
	return cells;
}

/**
 * Return the number of tables inserted in the editor.
 *
 * @return The number of inserted tables as an <code>int</code>.
 */
public int getInsertedTablesCount() {
	if (DEBUG) debugPrintln("		+ Get the number of inserted tables");
	int count = getInsertedTableElements().size();
	if (DEBUG) debugPrintln("		  -> "+count+" tables were found");
	return count;
}

/**
 * Returns the editor's text content
 */
public String getTextContent() {
	if (DEBUG) debugPrintln("		+ Get the content of last inserted table.");
	String content = getContent(false);
	if (DEBUG) {
		debugPrintln("		  -> found following text:");
		debugPrintln(content);
	}
	return content;
}

private void moveCursor(final String position) {
	StringBuilder script = new StringBuilder();
	// Get the selection range from the editor
	script.append("var range = " + getEditorScriptRoot() + ".createRange();");
	// Move the range to the provided position
	script.append("range.moveToElementEdit" + position + "(range.root);");
	// Update editors range
	script.append(getEditorScriptRoot() + ".getSelection().selectRanges([range]);");
	this.browser.executeScript(script.toString());
}

/**
 * Moves the cursor to the beginning of the editor
 */
public void moveCursorToBeginning() {
	if (DEBUG) debugPrintln("		+ Move cursor to CKEditor beginning.");
	moveCursor(EDITOR_BEGINNING);
}

private void moveCursorToElementEditStart(final String elementId) {
	StringBuilder script = new StringBuilder();
	script.append("var ed = "+getEditorScriptRoot()+";");
	script.append("var elm = ed.document.getById( '"+elementId+"' );");
	script.append("var rng = ed.createRange();");
	script.append("rng.moveToElementEditStart(elm);");
	script.append("ed.getSelection().selectRanges([rng]);");
	this.browser.executeScript(script.toString());
}

/**
 * Moves the cursor to the end of the editor
 */
public void moveCursorToEnd() {
	if (DEBUG) debugPrintln("		+ Move cursor to CKEditor end.");
	moveCursor(EDITOR_END);
}

/**
 * Saves the content in the editor with 'Ctrl + s'
 */
public void saveContent(){
	this.element.sendKeys(Keys.chord(Keys.CONTROL, "s"));
}

/**
 * Sets the content of the editor with the given text as plain text.
 * <p>
 * Any existing content will be replaced.
 * </p>
 * @param content The new text content for the editor
 */
public void setContent(final String content) {
	if (DEBUG) debugPrintln("		+ Set CKEditor content...");
	setContent(content, false);
}

/**
 * Sets the content of the editor with the given text using the given format.
 * <p>
 * Any existing content will be replaced.
 * </p>
 * @param content The new content for the editor
 * @param isHtml Is the content a html string
 */
public void setContent(final String content, final boolean isHtml){
	setContent(content, isHtml, false);
}

/**
 * Sets the content of the editor with the given text using the given format.
 * <p>
 * Any existing content will be replaced.
 * </p>
 * @param content The new content for the editor
 * @param isHtml Is the content a html string
 * @param save Saves the content, for use for an in place edit.
 */
public void setContent(final String content, final boolean isHtml, final boolean save) {
	if (DEBUG) {
		debugPrintln("		+ Set CKEditor content with following "+(isHtml?"HTML":"Plain")+" text:");
		debugPrintln(content);
	}
	// Clear out the content
	clearContent();
	// Set the content
	if (isHtml) {
		addHtml(content);
	} else {
		addText(content, /*new line*/false, save);
	}
}

private void setEditorInstance() {
	// The editor instance is embedded in the iframes 'title' attribute.
	// It is in the format of 'Rich Text Editor, editor<id>'
	String[] editorTitle = this.element.getAttribute("title").split(", ");
	if (editorTitle != null && editorTitle.length > 1) {
		this.editorInstance = editorTitle[1];
	}
}
}