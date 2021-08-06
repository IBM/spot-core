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
package com.ibm.bear.qa.spot.core.dialog;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.NLS_MESSAGES;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Simple confirmation dialog that contains two buttons.
 */
public class SpotConfirmationDialog extends SpotAbstractDialog {

	protected String closeButtonText;
	protected String cancelButtonText;

/**
 * Create a standard dialog instance.
 * <p>
 * A standard confirmation dialog is a dialog where validation button is defined
 * by NLS message
 * </p>
 * @param page The page in which the dialog will be opened
 * @param selector The selector to find the dialog web element in the page
 */
public SpotConfirmationDialog(final WebPage page, final By selector) {
	this(page, selector, NLS_MESSAGES.getDialogOkButtonLabel(), NLS_MESSAGES.getDialogCancelButtonLabel());
}

/**
 * Create a standard dialog instance.
 * <p>
 * A standard confirmation dialog is a dialog where validation button is defined
 * by NLS message
 * </p>
 * @param page The page in which the dialog will be opened
 * @param closeButtonText The text for dialog validation button
 * @param selector The selector to find the dialog web element in the page
 */
public SpotConfirmationDialog(final WebPage page, final By selector, final String closeButtonText) {
	this(page, selector, closeButtonText, NLS_MESSAGES.getDialogCancelButtonLabel());
}

/**
 * Create a generic dialog instance.
 *
 * @param page The page in which the dialog will be opened
 * @param selector The selector to find the dialog web element in the page
 * @param closeButtonText The text for dialog validation button
 * @param cancelButtonText The text for dialog cancellation button
 */
public SpotConfirmationDialog(final WebPage page, final By selector, final String closeButtonText, final String cancelButtonText) {
	super(page, selector);

	this.closeButtonText = closeButtonText;
	this.cancelButtonText = cancelButtonText;
}

@Override
protected By getCloseButtonLocator(final boolean validate) {
	// Strip the leading and trailing spaces first before comparing, ie. trim.
	String xpath = ".//button[normalize-space(text())='"+(validate?this.closeButtonText:this.cancelButtonText)+"']";
	return By.xpath(xpath);
}

/**
 * Get the text of the close button; good for debug messages.
 *
 * @return text of the close button.
 */
public String getCloseButtonText() {
	return this.closeButtonText;
}
}
