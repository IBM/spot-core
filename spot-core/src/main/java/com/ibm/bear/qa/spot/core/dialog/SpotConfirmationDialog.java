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

package com.ibm.bear.qa.spot.core.dialog;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Simple confirmation dialog that contains two buttons.
 */
public class SpotConfirmationDialog extends SpotAbstractDialog {

	protected String closeButtonText;
	protected String cancelButtonText;

public SpotConfirmationDialog(final WebPage page, final By findby, final String closeButtonText, final String cancelButtonText) {
	super(page, findby);

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
