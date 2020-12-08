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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import org.openqa.selenium.*;

import com.ibm.bear.qa.spot.core.scenario.errors.WaitElementTimeoutError;

/**
 * Abstract class for any window opened as a rich hover in a browser page.
 * <p>
 * Following functionalities are specialized by the rich hover:
 * <ul>
 * <li>{@link #getText()}: Return the text content of the hover.</li>
 * <li>{@link #open(WebBrowserElement)}: open the window by clicking on the
 * given web element.</li>
 * </ul>
* </p><p>
 * Following operations are also defined or specialized for rich hovers:
 * <ul>
 * <li>{@link #closeAction(boolean)}: The action to perform to close the window.</li>
 * <li>{@link #getCloseButtonLocator(boolean)}: The button to close the hover.</li>
 * </ul>
  * </p>
 */
abstract public class WebTextHover extends SpotAbstractWindow {

	private static By LAST_TEXT_HOVER_LOCATOR;

public WebTextHover(final WebPage page, final By locator) {
	super(page, locator);
	LAST_TEXT_HOVER_LOCATOR = locator;
}

/**
 * Wait that a text hover gets opened and cancel it.
 * <p>
 * This is a convenient method to close any kind of hover while moving to an
 * element to ensure it's visible in the page.
 * </p><p>
 * Of course, this is a no-op if there's no hover currently opened.
 * </p>
 * @param page The page on which the hover occurs.
 */
public static void waitAndCancel(final WebPage page) {
	if (DEBUG) debugPrintln("		+ (page="+page+")");
	WebBrowserElement dialogElement = page.waitForElement(LAST_TEXT_HOVER_LOCATOR, false/*fail*/, 1/*sec*/);
	if (dialogElement != null) {
		pause(250);
		dialogElement.sendKeys(Keys.ESCAPE);
		pause(250);
	}
}

/**
 * The action to perform to close the window.
 * <p>
 * There's no close button for this dialog, the only way to close it is to click
 * somewhere else in the page.
 * </p><p>
 * First attempt to do this is to scroll to the top.
 * Second attempt to do hit the Escape key on the link element
 * </p>
 */
@Override
protected void closeAction(final boolean cancel) {
	this.openingElement.sendKeys(Keys.ESCAPE);
}

/**
 * {@inheritDoc}
 * <p>
 * There's no button close this kind of rich hover.
 * </p>
 */
@Override
protected By getCloseButtonLocator(final boolean validate) {
	return null;
}

/**
 * Return the link element on which the hover is opened.
 * <p>
 * <b>Warning</b>: This method should not be used from scenarios.
 * </p>
 * @return The link element as a {@link WebBrowserElement}.
 */
public WebBrowserElement getLinkElement() {
	return this.openingElement;
}

/**
 * Return the text content of the hover.
 *
 * @return The content as a {@link String}.
 */
@Override
public String getText() {
	if (DEBUG) debugPrintln("		+ Get text for hover "+this.element);

	// Get content element
	WebBrowserElement contentElement = waitForMandatoryElement(By.xpath(".//div[@dojoattachpoint='content']"), openTimeout());

	// Return text if content is found
	if (contentElement != null) {
		return contentElement.getText();
	}

	// Invalid content, return empty text
	debugPrintln("		  -> no web element matching expected xpath, return empty string");
	return EMPTY_STRING;
}

/**
 * {@inheritDoc}
 * <p>
 * The rich hover is opened by hovering the mouse over the given element.
 * </p>
 */
@Override
public WebBrowserElement open(final WebBrowserElement webElement) {

	// Store the link element
	this.openingElement = webElement;

	// Wait for the element to be displayed (allow recovering if element has become stale)
	long timeout = openTimeout() * 1000 + System.currentTimeMillis();	 // Timeout currentTimeMilliseconds
	sleep(1);
	while (!webElement.isDisplayed()) {
		if (System.currentTimeMillis() > timeout) {
			throw new WaitElementTimeoutError("Cannot get the link element '"+webElement+"' on which rich hover should be opened.");
		}
		sleep(1);
	}

	// Move the mouse to the link element in order to trigger the hover
	while (true) {
		if (System.currentTimeMillis() > timeout) {
			throw new WaitElementTimeoutError("Cannot open the rich hover over "+webElement);
		}
		try {
			this.browser.moveToElement(this.openingElement, false);
//			this.openingElement.mouseOver();
			break;
		}
		catch (StaleElementReferenceException sere) {
			debugPrintException(sere);
			webElement.isDisplayed(); // allow recovery
		}
	}

	// Store the hover element
	this.element = this.browser.waitForElement(null, this.locator, true/*fail*/, (int) ((timeout - System.currentTimeMillis())/1000), true/*displayed*/, true/*single expected*/);

	waitForLoadingEnd();

	// Return the opened hover
	return this.element;
}

protected void waitForLoadingEnd() {
	// Do nothing by default

}
}
