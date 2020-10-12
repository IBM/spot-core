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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintEnteringMethod;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.pause;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to handle a checkable web element (eg. typically a check-box) in a web page.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #select()}: Select the associated web element.</li>
 * </ul>
 * </p>
 */
public class SpotCheckableElement extends SpotAttributeSelectableElement {

	/* Constants */
	private static final String ARIA_CHECKED = "aria-checked";

public SpotCheckableElement(final WebPage page, final By locator, final By expansionLocator) {
	super(page, locator, expansionLocator, ARIA_CHECKED);
}

public SpotCheckableElement(final WebPage page, final By locator) {
	super(page, locator, ARIA_CHECKED);
}

public SpotCheckableElement(final WebPage page, final WebBrowserElement webElement, final By expansionLocator) {
	super(page, webElement, expansionLocator, ARIA_CHECKED);
}

public SpotCheckableElement(final WebPage page, final WebBrowserElement webElement) {
	super(page, webElement, ARIA_CHECKED);
}

@Override
public void select() throws ScenarioFailedError {
	if (!isSelected()) {
		debugPrintEnteringMethod();
		pause(250);
		getSelectionElement().click();
		pause(250);
		waitUntilSelection(true, true);
	}
}
}
