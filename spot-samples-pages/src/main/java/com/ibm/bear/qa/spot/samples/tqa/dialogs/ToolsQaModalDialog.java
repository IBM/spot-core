/*********************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.samples.tqa.dialogs;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintEnteringMethod;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;
import com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaLargeModalDialog;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;

/**
 * Class to manage modal dialog used in the <b>Modal Dialogs</b> section in the <b>Alerts, Frame &
 * Windows</b> of the ToolsQA test page.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getContent()}: Return the text displayed in the dialog.</li>
 * <li>{@link #getTitle()}: Return the dialog title.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getCloseButtonLocator(boolean)}: Return the locator for the button to close the window.</li>
 * </ul>
 * </p>
 */
public class ToolsQaModalDialog extends SpotAbstractDialog implements ToolsQaLargeModalDialog {

	/* Constants */
	// Locators
	private final static By DIALOG_LOCATOR = By.className("modal-content");

	/* Fields */
	private final String kind;

/**
 * Create a typed dialog instance belonging to the given page.
 *
 * @param page The page from where the dialog is opened
 * @param kind The dialog kind
 */
public ToolsQaModalDialog(final ToolsQaTestPage page, final String kind) {
	super(page, DIALOG_LOCATOR);
	this.kind = kind;
}

@Override
protected By getCloseButtonLocator(final boolean validate) {
	return validate ? By.id("close"+this.kind+"Modal") : By.className("close");
}

/**
 * Return the text displayed in the dialog.
 *
 * @return The displayed text
 */
public String getContent() {
	debugPrintEnteringMethod();
	WebBrowserElement textElement = this.element.waitShortlyForMandatoryDisplayedChildElement(By.className("modal-body"));
	return textElement.getText();
}

/**
 * Return the dialog title.
 *
 * @return The title
 */
public String getTitle() {
	debugPrintEnteringMethod();
	WebBrowserElement textElement = this.element.waitShortlyForMandatoryDisplayedChildElement(By.className("modal-title"));
	return textElement.getText();
}
}
