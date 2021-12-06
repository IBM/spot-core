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

import java.util.List;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.topology.Application;

/**
 * Abstract class to manage a web page login operation.
 * <p>
 * A login operation belongs to a page and use a specific user. Most of methods implemented in this
 * class are simple redirection to the page (typically for action or search for web elements).
 * </p><p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #beforeLogin()}: Perform some check or actions before performing the actual login action.</li>
 * <li>{@link #checkInvalidLoginMessage()}: Check the whether the login operation failed or not.</li>
 * <li>{@link #click(By)}: Click on the web element found in the current page using the given locator.</li>
 * <li>{@link #click(WebBrowserElement,By)}: Click on the web element found using the given locator relatively to</li>
 * <li>{@link #getApplication()}: Return the application associated with the page.</li>
 * <li>{@link #getLoginButtonLocator()}: Return the locator for the login button to click on to perform the login.</li>
 * <li>{@link #getPage()}: Return the web page in which the current element belongs to.</li>
 * <li>{@link #getUserIdElement()}: Return the web element where user ID has to be entered.</li>
 * <li>{@link #getUserIdLocator()}: Return the locator for the web element to enter user ID.</li>
 * <li>{@link #getUserPasswordElement()}: Return the web element where user password has to be entered.</li>
 * <li>{@link #getUserPasswordLocator()}: Return the locator for the web element to enter user password.</li>
 * <li>{@link #isExpectingLogin()}: Return whether the current page is currently expecting a log operation or not.</li>
 * <li>{@link #performLogin()}: Perform the login operation on the associated page.</li>
 * <li>{@link #resetFrame()}: Reset the current browser frame.</li>
 * <li>{@link #selectFrame(WebBrowserElement)}: Set the current browser frame to the given web element.</li>
 * <li>{@link #shortTimeout()}: Return the short timeout used on the page.</li>
 * <li>{@link #storeUserAsLoggedInApplication()}: Store the user as logged int for the corresponding page application.</li>
 * <li>{@link #timeout()}: Return the general timeout used on the page.</li>
 * <li>{@link #typeText(By,String)}: Type a text into an input web element found in the current page using the</li>
 * <li>{@link #waitForMandatoryPageElement(By)}: Wait until have found in current page the web element using the given locator.</li>
 * <li>{@link #waitForPotentialPageElementWithTimeout(By,int)}: Wait until have found in current page the web element using the given locator.</li>
 * <li>{@link #waitForPotentialPageElementsWithTimeout(By,int)}: Wait until have found in current page some elements (ie. at least one) web elements</li>
 * </ul>
 * </p>
 */
public abstract class SpotAbstractLoginOperation {

	/**
	 * The page on which the login operation occurs.
	 */
	protected final WebPage page;

	/**
	 * The user which will be used for login
	 */
	protected final User user;

/**
 * Create a login operation instance for the given page
 * @param webPage
 */
protected SpotAbstractLoginOperation(final WebPage webPage, final User pageUser) {
	this.page = webPage;
	this.user = pageUser;
}

/**
 * Perform some check or actions before performing the actual login action.
 * <p>
 * By default no check nor action before the login operation. Subclasses might
 * want to override this method add specific checks and/or actions before
 * the login operation was performed.
 * </p>
 * @throws ScenarioFailedError If check or action fails
 */
protected void beforeLogin() throws ScenarioFailedError {
	// No check or action before doing the login operation
}

/**
 * Check the whether the login operation failed or not.
 */
abstract protected void checkInvalidLoginMessage();

/**
 * Click on the web element found in the current page using the given locator.
 *
 * @see WebPage#click(By)
 */
protected void click(final By locator) {
	this.page.click(locator);

}

/**
 * Click on the web element found using the given locator relatively to
 * the given parent web element.
 *
 * @see WebPage#click(WebBrowserElement, By)
 */
protected void click(final WebBrowserElement element, final By locator) {
	this.page.click(element, locator);
}

/**
 * Return the application associated with the page.
 *
 * @return The application
 */
protected Application getApplication() {
	return this.page.getApplication();
}

/**
 * Return the locator for the login button to click on to perform the login.
 *
 * @return The locator
 */
abstract protected By getLoginButtonLocator();

/**
 * Return the web page in which the current element belongs to.
 *
 * @return The page as a subclass of {@link WebPage}
 */
protected WebPage getPage() {
	return this.page;
}

/**
 * Return the web element where user ID has to be entered.
 *
 * @return The web element
 */
protected WebBrowserElement getUserIdElement() {
	return this.page.waitForMandatoryDisplayedElement(getUserIdLocator());
}

/**
 * Return the locator for the web element to enter user ID.
 *
 * @return The locator
 */
abstract protected By getUserIdLocator();

/**
 * Return the web element where user password has to be entered.
 *
 * @return The web element
 */
protected WebBrowserElement getUserPasswordElement() {
	return this.page.waitForMandatoryDisplayedElement(getUserPasswordLocator());
}

/**
 * Return the locator for the web element to enter user password.
 *
 * @return The locator
 */
abstract protected By getUserPasswordLocator();

/**
 * Return whether the current page is currently expecting a log operation or not.
 * <p>
 * Typically it means that all web elements for the login operation are
 * displayed in the browser. So far, only the login button is checked.
 * </p>
 * @return <code>true</code> if the login button is present in the page, <code>false</false> otherwise.
 */
protected boolean isExpectingLogin() {
	By locator = getLoginButtonLocator();
	if (locator == null) {
		return false;
	}
	return this.page.waitForPotentialDisplayedElementWithTimeout(locator, 1) != null;
}

/**
 * Perform the login operation on the associated page.
 */
protected void performLogin() {

	// Do some check or actions before the actual login
	beforeLogin();

	// Enter user credentials
	this.page.typeText(getUserIdElement(), this.user.getId());
	this.page.typePassword(getUserPasswordElement(), this.user);

	// Click on 'Login' button
	this.page.clickButton(getLoginButtonLocator());

	// Store user in application
	storeUserAsLoggedInApplication();

	// Check for an Invalid user ID or password message
	checkInvalidLoginMessage();
}

/**
 * Reset the current browser frame.
 *
 * @see WebBrowser#resetFrame()
 */
protected void resetFrame() {
	this.page.browser.resetFrame();

}

/**
 * Set the current browser frame to the given web element.
 *
 * @see WebBrowser#selectFrame(WebBrowserElement)
 */
protected void selectFrame(final WebBrowserElement frameElement) {
	this.page.browser.selectFrame(frameElement);
}

/**
 * Return the short timeout used on the page.
 *
 * @see WebPage#shortTimeout()
 */
protected int shortTimeout() {
	return this.page.shortTimeout();
}

/**
 * Store the user as logged int for the corresponding page application.
 */
protected void storeUserAsLoggedInApplication() {
	this.page.topology.login(this.page.getLocation(), this.user);
}

/**
 * Return the general timeout used on the page.
 *
 * @see WebPage#timeout()
 */
protected int timeout() {
	return this.page.timeout();
}

/**
 * Type a text into an input web element found in the current page using the
 * given locator.
 *
 * @see WebPage#typeText(By, String)
 */
protected void typeText(final By locator, final String text) {
	this.page.typeText(locator, text);

}

/**
 * Wait until have found in current page the web element using the given locator.
 *
 * @see WebPage#waitForMandatoryDisplayedElement(By)
 */
protected WebBrowserElement waitForMandatoryPageElement(final By locator) {
	return this.page.waitForMandatoryDisplayedElement(locator);
}

/**
 * Wait until have found in current page the web element using the given locator.
 *
 * @see WebPage#waitForPotentialDisplayedElementWithTimeout(By, int)
 */
protected WebBrowserElement waitForPotentialPageElementWithTimeout(final By locator, final int timeout) {
	return this.page.waitForPotentialDisplayedElementWithTimeout(locator, timeout);
}

/**
 * Wait until have found in current page some elements (ie. at least one) web elements
 * using the given locator and timeout.
 *
 * @see WebPage#waitForPotentialDisplayedElementsWithTimeout(By, int)
 */
protected List<WebBrowserElement> waitForPotentialPageElementsWithTimeout(final By locator, final int timeout) {
	return this.page.waitForPotentialDisplayedElementsWithTimeout(locator, timeout);
}
}