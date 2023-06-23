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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.DEBUG;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

import org.openqa.selenium.*;

import com.ibm.bear.qa.spot.core.config.Timeouts;

/**
 * Class to manage browser frame identified with a web element.
 * <p>
 * TODO Only use this kind of frame, the two other ones should become
 * obsolete
 */
public class WebElementFrame extends WebBrowserFrame {

	/**
	 * The web element of the current frame.
	 */
	private final WebBrowserElement element;

public WebElementFrame(final WebBrowser browser, final By locator) {
	this(browser, browser.waitForMandatoryDisplayedElement(locator, Timeouts.DEFAULT_TIMEOUT));
}

public WebElementFrame(final WebBrowser browser, final WebBrowserElement element) {
	super(browser);
	this.element = element;
}

@Override
public boolean equals(final Object obj) {
	if (obj instanceof WebElementFrame) {
		WebElementFrame frame = (WebElementFrame) obj;
		return this.element.equals(frame.element);
	}
	if (obj instanceof WebBrowserElement) {
		return this.element.equals(obj);
	}
	return super.equals(obj);
}

@Override
WebBrowserElement getElement() {
	return this.element;
}

@Override
public int hashCode() {
	return this.element.hashCode();
}

/**
 * {@inheritDoc}
 * <p>
 * This class stores the frame element hence it can know whether it's displayed
 * or not using the corresponding {@link WebBrowserElement#isDisplayed()} API
 * method.
 * </p>
 * @return <code>true</code> if the frame element is still displayed,
 * <code>false</code> otherwise.
 */
@Override
public boolean isDisplayed() {
	try {
		return this.element.isDisplayed(false);
	}
	catch (StaleElementReferenceException | NoSuchElementException ex) {
		return false;
	}
}

/**
 * Select current frame.
 */
@Override
void switchTo() {
	if (DEBUG) debugPrintln("		+ Switch to "+this);
	this.driver.switchTo().defaultContent();
	this.driver.switchTo().frame(this.element.getWebElement());
}

@Override
public String toString() {
	return "Frame element "+this.element;
}

}
