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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.sleep;

import java.util.List;

import org.openqa.selenium.*;

import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.WaitElementTimeoutError;

/**
 * This class wraps a web element and add some actions and functionalities
 * that anyone can use. It also add some specific operations only accessible to
 * the class hierarchy.
 * <p>
 * There's still no public action or functionalities at this level, only common
 * operations for subclasses usage:
 * <ul>
 * <li>{@link #clickButton(By)}: Click on the button found using the given locator.</li>
 * <li>{@link #clickButton(By, int)}: Click on the button found using the given locator.</li>
 * <li>{@link #findElement(By, boolean)}: Find an element using the given search
 * locator relatively to the wrapped element.</li>
 * <li>{@link #findElement(String, boolean)}: Find an element using the given
 * xpath relatively to the wrapped element.</li>
 * <li>{@link #waitWhileDisplayed(int)}: Wait until the current window is closed.</li>
 * <li>{@link #waitForElement(By)}: Wait until having found an element
 * searched using the given locator.</li>
 * </ul>
 * </p>
 */
abstract public class WebElementWrapper extends WebPageElement {

	/**
	 * Locator for title element.
	 * <p>
	 * Design Backward compatibility with 4.0.0.1 version
	 * </p>
	 */
	protected static final By[] TITLE_POSSIBLE_BYS = new By[] {
		By.xpath(".//*[@dojoattachpoint='_headerPrimary']"),
		By.xpath(".//*[@dojoattachpoint='_primaryHeaderText']"),
	};

	/**
	 * The wrapped web element.
	 */
	protected WebBrowserElement element;

	/**
	 * The parent of current wrapper.
	 * <p>
	 * Children web elements should be looked from the web element of this
	 * wrapper if not <code>null</code>. If <code>null</code>, then they have
	 * to be looked for in the entire page.
	 * </p>
	 */
	protected WebElementWrapper parent;

public WebElementWrapper(final WebElementWrapper parent, final By locator, final WebBrowserFrame frame) {
	this(parent.getPage(), frame==null ? parent.element.waitForMandatoryElement(locator) : parent.getPage().waitForElement(locator), frame);
	this.parent = parent;
}

public WebElementWrapper(final WebElementWrapper parent, final By locator) {
	this(parent, locator, null);
}

public WebElementWrapper(final WebElementWrapper parent, final WebBrowserElement element) {
	this(parent.getPage(), element);
	this.parent = parent;
}

public WebElementWrapper(final WebElementWrapper parent, final WebBrowserElement element, final WebBrowserFrame frame) {
	this(parent.getPage(), element, frame);
	this.parent = parent;
}

public WebElementWrapper(final WebPage page) {
	super(page);
}

public WebElementWrapper(final WebPage page, final By locator) {
	super(page);
	//waitForElement() in this class can't filter out hidden elements, hence use super class implementation instead
	this.element = super.waitForElement(locator, true, openTimeout());
}

public WebElementWrapper(final WebPage page, final WebBrowserElement element) {
	super(page);
	this.element = element;
}

public WebElementWrapper(final WebPage page, final WebBrowserFrame frame) {
	super(page, frame);
}

public WebElementWrapper(final WebPage page, final WebBrowserElement element, final WebBrowserFrame frame) {
	super(page, frame);
	this.element = element;
}

public WebElementWrapper(final WebPage page, final By locator, final WebBrowserFrame frame) {
	super(page, frame);
	this.element = super.waitForElement(locator, true, openTimeout());
}

/**
 * Click on the button found using the given xpath.
 *
 * @param locator The locator to find the button
 */
@Override
protected WebBrowserElement clickButton(final By locator) {
	return clickButton(locator, shortTimeout());
}

/**
 * Click on the button found using the given locator.
 *
 * @param locator The locator to find the button
 * @param timeout Timeout while waiting for the button to become enabled
 */
protected WebBrowserElement clickButton(final By locator, final int timeout) {

	// Get button element
	WebBrowserElement buttonElement = findElement(locator, false/*no recovery*/);
	if (buttonElement == null) {
		throw new WaitElementTimeoutError("Cannot find element "+locator);
	}

	// Click on button
	return this.browser.clickButton(buttonElement, timeout, false/*do not validate*/);
}

/**
 * Perform a deep search to find the element from the given xpath.
 * <p>
 * Initially searching for the web element by using {@link #findElement(By, boolean)}.
 * If it's not found using this method, then try to look for the element in possible
 * frames of the wrapped element.
 * </p>
 * @param xpath The Xpath of the web element to find
 * @return The web element as a {@link WebBrowserElement} or <code>null</code>
 * if it has not been found.
 * @deprecated Do not use. It's only kept for tracability...
 */
@Deprecated
protected WebBrowserElement deepFindElement(final String xpath) {

	// Look for the element using regular method
	WebBrowserElement buttonElement = findElement(xpath, false/*no recovery*/);

	// If not found , then try to find it in another frame
	if (buttonElement == null) {
		buttonElement = findElementInFrames(By.xpath(xpath));
	}

	// Return the found element
	return buttonElement;
}

/**
 * Find an element using the given locator relatively to the wrapped element.
 * <p>
 * Note that if a frame is selected, then the element is searched relatively to
 * this frame instead.
 * </p>
 * @param elemLocator The locator to find the element
 * @param recovery Tells whether recovery is allowed when searching the element.
 * @return The found element as a {@link WebBrowserElement} or <code>null</code>
 * if the element was not found and recovery was not allowed.
 * @throws NoSuchElementException If the element is not found at the given
 * locator and recovery was allowed.
 * @see WebBrowser#findElement(By)
 * @noreference Framework internal usage only
 */
public WebBrowserElement findElement(final By elemLocator, final boolean recovery) {
	return findElement(elemLocator, true/*displayed*/, recovery);
}

/**
 * Find an element using the given locator relatively to the wrapped element.
 * <p>
 * Note that if a frame is selected, then the element is searched relatively to
 * this frame instead.
 * </p>
 * @param elemLocator The locator to find the element
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param recovery Tells whether recovery is allowed when searching the element.
 * @return The found element as a {@link WebBrowserElement} or <code>null</code>
 * if the element was not found and recovery was not allowed.
 * @throws NoSuchElementException If the element is not found at the given
 * locator and recovery was allowed.
 * @see WebBrowser#findElement(By)
 * @noreference Framework internal usage only
 */
public WebBrowserElement findElement(final By elemLocator, final boolean displayed, final boolean recovery) {

	// Find elements
	List<WebElement> elements;
	if (this.frames[2] != null) {
		elements = this.browser.findElements(elemLocator, displayed, recovery);
	} else if (this.element == null) {
		elements = this.browser.findElements(elemLocator, displayed, recovery);
	} else {
		elements = this.element.findElements(elemLocator, displayed, recovery);
	}

	// Nothing was found, check frame
	if (elements == null || elements.size() == 0) {
		if (this.frames[2] == null && this.frames[1] != null) {
			selectFrame();
			elements = this.browser.findElements(elemLocator, displayed, recovery);
		}
		if (elements == null || elements.size() == 0) {
			return null;
		}
	}

	// Check element uniqueness
	if (elements.size() > 1) {
		if (this.element.isDisplayed(false/*recovery*/)) {
			throw new ScenarioFailedError("Unexpected multiple elements found.");
		}
		return null;
	}

	// Return the found element
	return (WebBrowserElement) elements.get(0);
}

/**
 * Find an element using the given xpath relatively to the wrapped element.
 * <p>
 * Note that if a frame is selected, then the element is searched relatively to
 * this frame instead.
 * </p>
 * @param xpath The path to find the window's element
 * @param recovery Tells whether recovery is allowed when searching the element.
 * @return The found element as a {@link WebBrowserElement} or <code>null</code>
 * if the element was not found and recovery was not allowed.
 * @throws NoSuchElementException If the element is not found at the given
 * locator and recovery was allowed.
 * @see #findElement(By, boolean)
 * TODO Deprecate as this forces usage of xpath locator although we should prefer
 * css selector locator instead...
 */
protected WebBrowserElement findElement(final String xpath, final boolean recovery) {

	// Use given path if there's an active frame
	if (this.frames[2] != null) {
		return findElement(By.xpath(xpath), recovery);
	}

	// Make xpath relative
	String relativeXpath = xpath;
	if (relativeXpath.startsWith("/")) {
		relativeXpath = "." + xpath;
	}

	// Find element using xpath locator
	return findElement(By.xpath(relativeXpath), recovery);
}

/**
 * Return whether the currentl wrapped element is displayed or not.
 *
 * @param recovery Tells whether to use recovery or not to get the info
 * @return <code>true</code> if the wrapped element is still valid and displayed,
 * <code>false</code> otherwise.
 */
public boolean isDisplayed(final boolean recovery) {
	return this.element.isDisplayed(recovery);
}

/**
 * Returns the parent element.
 *
 * @return The parent element of <code>null</code> if there's no parent.
 */
protected WebElementWrapper getParent() {
	return this.parent;
}

/**
 * Returns the parent element.
 *
 * @return The parent element of <code>null</code> if there's no parent.
 */
protected WebBrowserElement getParentElement() {
	if (this.parent == null) {
		return null;
	}
	return this.parent.element;
}

/**
 * Return the text of the wrapped element.
 *
 * @return The text as a {@link String}.
 */
public String getText() {
	return this.element.getText();
}

/**
 * Wait until having found an element searched using the given locator.
 * <p>
 * The element is searched in the entire document and with no frame.
 * </p>
 * @param locator The locator to use for the search
 * @return The found web element as a {@link WebBrowserElement}.
 * @throws ScenarioFailedError If the element is not found before default
 * timeout is reached.
 * TODO Try to get rid off this method by selecting the frame explicitly before
 * waiting for an element. Hence, {@link WebPageElement} waitForElement*
 * methods could be used instead.
 */
protected WebBrowserElement waitForMandatoryElement(final By locator) {
	return waitForMandatoryElement(null, locator, timeout(), false/*no frame*/);
}

/**
 * Wait until having found an element searched using the given locator.
 * <p>
 * The element is searched in the entire document and with no frame.
 * </p>
 * @param locator The locator to use for the search
 * @param timeout Time to wait until giving up if the element is not found
 * @return The found web element as a {@link WebBrowserElement}.
 * @throws ScenarioFailedError If the element is not found before the given
 * timeout is reached.
 * TODO Try to get rid off this method by selecting the frame explicitly before
 * waiting for an element. Hence, {@link WebPageElement} waitForElement*
 * methods could be used instead.
 */
protected WebBrowserElement waitForMandatoryElement(final By locator, final int timeout) {
	return waitForMandatoryElement(null, locator, timeout, false/*no frame*/);
}

/**
 * Wait until have found the web element using the given locator relatively
 * to the given parent element.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if:
 * <ol>
 * <li>the element is not found before {@link #timeout()} seconds</li>
 * <li>there's more than one element found</li>
 * </ol></li>
 * <li>hidden element will be ignored</li>
 * </ul>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locator The locator to find the element in the current page.
 * @return The web element as {@link WebBrowserElement}
 * @throws ScenarioFailedError if no element was found before the timeout.
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 * TODO Try to get rid off this method by selecting the frame explicitly before
 * waiting for an element. Hence, {@link WebPageElement} waitForElement*
 * methods could be used instead.
 */
protected WebBrowserElement waitForMandatoryElement(final WebBrowserElement parentElement, final By locator) {
	return waitForMandatoryElement(parentElement, locator, timeout(), false/*no frame*/);
}

/**
 * Wait until having found an element searched using the given locator.
 * <p>
 * The element is searched with no frame.
 * </p>
 * @param parentElement The element from which the search has to be started.
 * If <code>null</code>, then search in the entire page.
 * @param locator The locator to use for the search
 * @param timeout Time to wait until giving up if the element is not found
 * @return The found web element as a {@link WebBrowserElement}.
 * @throws ScenarioFailedError If the element is not found before the given
 * timeout is reached.
 * TODO Try to get rid off this method by selecting the frame explicitly before
 * waiting for an element. Hence, {@link WebPageElement} waitForElement*
 * methods could be used instead.
 */
protected WebBrowserElement waitForMandatoryElement(final WebBrowserElement parentElement, final By locator, final int timeout) {
	return waitForMandatoryElement(parentElement, locator, timeout, false);
}

/**
 * Wait until having found an element searched using the given locator.
 * <p>
 * If the element has to be searched in a frame and it's found, then the matching
 * frame is selected after the method execution. That may impact further element
 * researches...
 * </p>
 * @param parentElement The element from which the search has to be started.
 * If <code>null</code>, then search in the entire page.
 * @param locator The locator to use for the search
 * @param timeout Time to wait until giving up if the element is not found
 * @param frame Tells whether the element should be searched in a frame or
 * not.
 * @return The found web element as a {@link WebBrowserElement}.
 * @throws ScenarioFailedError If the element is not found before the given
 * timeout is reached.
 * TODO Try to get rid off this method by selecting the frame explicitly before
 * waiting for an element. Hence, {@link WebPageElement} waitForElement*
 * methods could be used instead.
 */
protected WebBrowserElement waitForMandatoryElement(final WebBrowserElement parentElement, final By locator, final int timeout, final boolean frame) {

	// Get the search context
	SearchContext searchContext = parentElement == null ? (this.element == null ? this.browser : this.element) : parentElement;

	// Try to find the element
	final boolean findInFrame = parentElement == null && frame;
	WebBrowserElement foundElement = findInFrame
		? this.browser.findElementInFrames(locator)
		: (WebBrowserElement) searchContext.findElement(locator);

	// Wait until the element is found or timeout reached
	long maxTime = System.currentTimeMillis() + timeout * 1000;
	while (foundElement == null) {
		if (System.currentTimeMillis() > maxTime) { // Timeout
			throw new WaitElementTimeoutError("Cannot get the expected element "+locator);
		}
		sleep(1);
		foundElement = findInFrame
			? this.browser.findElementInFrames(locator)
			: (WebBrowserElement) searchContext.findElement(locator);
	}

	// Return the found element
	return foundElement;
}

/**
 * Wait until the wrapped element is no longer displayed.
 *
 * @param seconds The timeout before giving up if the element is still displayed
 * @throws WaitElementTimeoutError If the wrapped element is still displayed after
 * the {@link Config#closeDialogTimeout()}.
 */
protected void waitWhileDisplayed(final int seconds) throws WaitElementTimeoutError {
	if (this.element != null) {
		this.element.waitWhileDisplayed(seconds);
	}
}
}
