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
import org.openqa.selenium.Keys;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to handle a CKEditor simple element.
 * <p>
 * Following features are accessible in this page:
 * <ul>
 * <li>{@link #setContent(String)}: Set the editor content with the given text.</li>
 * </ul>
 * </p><p>
 * Following internal features are also defined or specialized by this page:
 * <ul>
 * </ul>
 * </p>
 */
public class WebCKEditorSimpleElement extends WebElementWrapper {

public static final By CKEDITOR_LOCATOR = By.xpath(".//div[starts-with(@class,'RichTextEditorWidget')]");

public WebCKEditorSimpleElement(final WebPage page, final WebBrowserElement element) {
	super(page, element);
}

public WebCKEditorSimpleElement(final WebPage page, final WebBrowserElement element, final boolean parent) {
	super(page, parent ? element.findElement(CKEDITOR_LOCATOR) : element);
}

/**
 * Set the editor content with the given text.
 *
 * @param text The text to set the editor content with
 */
public void setContent(final String text) {

	// Type text in the editor
	typeText(this.element, text);

	// Check that the text was well entered
	if (!this.element.getText().equals(text)) {

		// Hit ENTER key to enter in edit mode
		this.element.sendKeys(Keys.ENTER);

		// Enter text again
		typeText(this.element, text);

		// Give up if the text was still not set
		if (!this.element.getText().equals(text)) {
			throw new ScenarioFailedError("Cannot set text for CKEditor element '"+this.element+"' in page '"+getPage().getTitle()+"'");
		}
	}
}
}
