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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.pause;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.ibm.bear.qa.spot.core.api.SpotUser;
import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.nls.NlsMessages;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.WaitElementTimeoutError;
import com.ibm.bear.qa.spot.core.topology.Application;
import com.ibm.bear.qa.spot.core.topology.Topology;

/**
 * This class manage a web element belonging to a web page and add some actions and functionalities
 * that anyone can use. It also add some specific operations only accessible to the class hierarchy.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getPage()}: Return the web page in which the current element belongs to.</li>
 * <li>{@link #shortTimeout()}: Return the short timeout used on the page.</li>
 * <li>{@link #timeout()}: Return the general timeout used on the page.</li>
 * <li>{@link #waitForMandatoryDisplayedPageElementWithTimeout(By,int)}: Wait until have found a mandatory web element using the given locator and a specific timeout.</li>
 * <li>{@link #waitForPotentialDisplayedPageElementWithTimeout(By,int)}: Wait until have found a potential web element using the given locator with a specific timeout.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #check(By,boolean)}: Set or unset the check-box found inside the current page using the given locator.</li>
 * <li>{@link #check(WebBrowserElement,boolean)}: Set/Unset the given check-box web element.</li>
 * <li>{@link #check(WebBrowserElement,By,boolean)}: Set or unset the check-box found inside the given parent web element using the given locator.</li>
 * <li>{@link #click(By)}: Click on the web element found in the current page using the given locator.</li>
 * <li>{@link #click(WebBrowserElement,By)}: Click on the web element found using the given locator relatively to the given parent web element.</li>
 * <li>{@link #clickButton(By)}: Click on the button found in the current page using the given locator.</li>
 * <li>{@link #enterText(WebBrowserElement,String)}: Enter the given text in the given text element.</li>
 * <li>{@link #getApplication()}: Return the application associated with the current page.</li>
 * <li>{@link #getConfig()}: Return the configuration associated with the current page.</li>
 * <li>{@link #getFrame()}: Return the frame used inside the wrapped element.</li>
 * <li>{@link #getNlsMessages()}: Return the NLS messages associated with the web page.</li>
 * <li>{@link #getTopology()}: Return the current test topology that the current page belongs to.</li>
 * <li>{@link #getUser()}: Return the user used when the page was loaded.</li>
 * <li>{@link #hasFrame()}: Return whether a frame is used for the page element or not.</li>
 * <li>{@link #openPageUsingBrowser(Class,String...)}: Retrieve the existing page for the browser current URL. Create it if it's the first</li>
 * <li>{@link #openPageUsingLink(WebBrowserElement,Class,String...)}: Click on the given link assuming that it will open the given page.</li>
 * <li>{@link #openTimeout()}: Return the timeout while opening the page.</li>
 * <li>{@link #resetFrame()}: Reset the current frame for the current window.</li>
 * <li>{@link #resetTimeout()}: Reset current timeout.</li>
 * <li>{@link #scrollToMakeElementVisible(WebBrowserElement)}: Scroll the current page window to make the given web element visible.</li>
 * <li>{@link #select(By,String)}: Select the given item in the given list element found.</li>
 * <li>{@link #select(WebBrowserElement,String)}: Select the given item in the given list element found.</li>
 * <li>{@link #selectFrame()}: Select the frame in which the current window is expected to be found.</li>
 * <li>{@link #startTimeout(int,String)}: Start a timeout of the given seconds;</li>
 * <li>{@link #storeBrowserFrame()}: Store the browser the frame.</li>
 * <li>{@link #switchToBrowserFrame()}: Switch to initial browser frame.</li>
 * <li>{@link #switchToStoredFrame()}: Switch to element frame.</li>
 * <li>{@link #testTimeout()}: Test whether the current timeout has been reached or not.</li>
 * <li>{@link #typePassword(WebBrowserElement,SpotUser)}: Type a password into the given input web element.</li>
 * <li>{@link #typeText(By,String)}: Type a text into an input web element found in the current page using the given locator.</li>
 * <li>{@link #typeText(WebBrowserElement,By,String)}: Type a text into an input web element found inside the given parent web element using the given locator.</li>
 * <li>{@link #typeText(WebBrowserElement,String)}: Type a text into the given input web element found.</li>
 * <li>{@link #waitForFirstMandatoryDisplayedPageElementInList(By...)}: Wait until have found and identify one of the elements using the given locators list.</li>
 * <li>{@link #waitForMandatoryDisplayedPageElement(By)}: Wait until have found the web element using the given locator.</li>
 * <li>{@link #waitForMandatoryDisplayedPageElements(By)}: Wait until have found some elements (ie. at least one) web elements using the given locator.</li>
 * <li>{@link #waitForMandatoryDisplayedPageElementsWithTimeout(By,int)}: Wait until have found some (ie. at least one) web elements using the given locator and a specific timeout.</li>
 * <li>{@link #waitForMandatoryPageElement(By)}: Wait until have found a possibly hidden web element using the given locator.</li>
 * <li>{@link #waitForPotentialDisplayedPageElementsWithTimeout(By,int)}: Wait until have potentially found some (ie. at least one) web elements using the given locator and a specific timeout.</li>
 * <li>{@link #workaround(String)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * </ul>
 * </p>
 */
abstract public class WebPageElement {

	//	/**
    //	 * Number of tries to workaround any problem while performing action on
    //	 * current element.
    //	 */
    //	protected int workarounds;

    class Timeout {
    	final int sec;
    	final long start, end;
    	String msg;
    	Timeout(final int seconds, final String message) {
    		this.sec = seconds;
    		this.start = System.currentTimeMillis();
    		this.end = this.start + seconds * 1000;
    		this.msg = message;
    	}
    	void test() {
    		if (System.currentTimeMillis() > this.end) {
    			throw new WaitElementTimeoutError(this.msg);
    		}
    	}
    }

	/**
	 * The CLM web page from which the element is found.
	 */
	protected WebPage page;

	/**
	 * The browser associated with the page.
	 * <p>
	 * This is a shortcut to access the browser page.
	 * </p>
	 */
	protected WebBrowser browser;

	/**
	 * The frames that the windows has to deal with:
	 * <ul>
	 * <li>slot 0: The browser frame when the dialog was opened.
	 * <p>
	 * It's important to store this piece of information to be able to restore it
	 * when closing the dialog.
	 * </p>
	 * </li>
	 * <li>slot 1: The frame used by the dialog
	 * <p>
	 * Can be <code>null</code> if no frame is used by the window
	 * </p><p>
	 * Note that not all the window elements are supposed to be in this frame,
	 * typically window title is not in this frame
	 * </p>
	 * </li>
	 * <li>slot 2: The current used frame.
	 * <p>
	 * If slot 1 is null, then this slot is always <code>null</code>, otherwise
	 * it can be either equals to slot 1 if frame elements want to be found or
	 * <code>null</code>  if other elements are searched.
	 * </p>
	 * </li>
	 * </ul>
	 */
	protected WebBrowserFrame[] frames;

	/**
	 * Current timeout.
	 */
	private Timeout currentTimeout;

public WebPageElement(final WebPage page) {
	this(page, null);
}

public WebPageElement(final WebPage page, final WebBrowserFrame frame) {
	this.page = page;
	this.browser = page.getBrowser();
	this.frames = new WebBrowserFrame[3];
	this.frames[0] = this.browser.getCurrentFrame();
	this.frames[1] = frame;
}

/**
 * @see WebPage#check(By, boolean)
 */
protected WebBrowserElement check(final By locator, final boolean on) {
	return this.page.check(locator, on);
}

/**
 * @see WebPage#check(WebBrowserElement, boolean)
 */
protected boolean check(final WebBrowserElement element, final boolean on) {
	return this.page.check(element, on);
}

/**
 * @see WebPage#check(WebBrowserElement, By, int, boolean)
 */
protected WebBrowserElement check(final WebBrowserElement parentElement, final By locator, final boolean on) {
	return this.page.check(parentElement, locator, on ? 1 : -1, true/*validate*/);
}

/**
 * @see WebPage#click(By)
 */
protected WebBrowserElement click(final By locator) {
	return this.page.click(locator);
}

/**
 * @see WebPage#click(WebBrowserElement, By)
 */
protected WebBrowserElement click(final WebBrowserElement parentElement, final By locator) {
	return this.page.click(parentElement, locator);
}

/**
 * @see WebPage#clickButton(By)
 */
protected WebBrowserElement clickButton(final By locator) {
	return this.page.clickButton(locator);
}

/**
 * Enter the given text in the given text element.
 * <p>
 * Note that the text element content will be replaced by the given text (ie. cleared
 * before typing the text in it. Note also that the <b>Enter</b> key will be hit after
 * having typing the text in the text element.
 * </p>
 * @see WebBrowser#typeText(WebBrowserElement, String, org.openqa.selenium.Keys, boolean, int)
 */
protected void enterText(final WebBrowserElement inputElement, final String text) {
	this.browser.typeText(inputElement, text, Keys.ENTER, /*clear:*/true, timeout());
}

/**
 * @see WebPage#getApplication()
 */
protected Application getApplication() {
	return this.page.getApplication();
}

/**
 * @see WebPage#getConfig()
 */
protected Config getConfig() {
	return this.page.getConfig();
}

/**
 * Return the frame used inside the wrapped element.
 *
 * @return The frame as a {@link WebBrowserFrame} or <code>null</code> if
 * no frame is used.
 */
protected WebBrowserFrame getFrame() {
	return this.frames[1];
}

/**
 * Return the NLS messages associated with the web page.
 *
 * @return The messages as {@link NlsMessages}.
 */
protected NlsMessages getNlsMessages() {
	return this.page.getNlsMessages();
}

/**
 * Return the web page in which the current element belongs to.
 *
 * @return The page as a subclass of {@link WebPage}
 */
public WebPage getPage() {
	return this.page;
}

/**
 * @see WebPage#getTopology()
 */
protected Topology getTopology() {
	return this.page.getTopology();
}

/**
 * @see WebPage#getUser()
 */
protected User getUser() {
	return this.page.getUser();
}

/**
 * Return whether a frame is used for the page element or not.
 *
 * @return <code>true</code> if a frame is used, <code>false</code> otherwise.
 */
protected boolean hasFrame() {
	return this.frames[1] != null;
}

/**
 * Retrieve the existing page for the browser current URL. Create it if it's the first
 * time the page is requested.
 *
 * @param pageClass The class associated with the page to open
 * @param data Additional CLM information to be stored in the page
 * @return The instance of the class associate with the page.
 */
protected <P extends WebPage> P openPageUsingBrowser(final Class<P> pageClass, final String... data) {
	return getPage().openPageUsingBrowser(pageClass, data);
}

/**
 * Click on the given link assuming that it will open the given page.
 *
 * @param linkElement The link on which to click
 * @param openedPageClass The class associated with the opened page
 * @param pageData Provide additional information to store in the page when opening it
 * @return The web page (as a subclass of {@link WebPage}) opened after
 * @see WebPage#openPageUsingLink(WebBrowserElement, Class, String...)
 */
protected <P extends WebPage> P openPageUsingLink(final WebBrowserElement linkElement, final Class<P> openedPageClass, final String... pageData) {
	return getPage().openPageUsingLink(linkElement, openedPageClass, pageData);
}

/**
 * @see WebPage#openTimeout()
 */
protected int openTimeout() {
	return this.page.openTimeout();
}

/**
 * Reset the current frame for the current window.
 *
 * @see WebBrowser#resetFrame()
 */
protected void resetFrame() {
	this.frames[2] = null;
	this.browser.resetFrame();
	pause(100);
}

/**
 * Reset current timeout.
 */
protected void resetTimeout() {
	this.currentTimeout = null;
}

/**
 * Scroll the current page window to make the given web element visible.
 *
 * @see WebPage#scrollToMakeElementVisible(WebBrowserElement)
 */
protected void scrollToMakeElementVisible(final WebBrowserElement element) {
	this.page.scrollToMakeElementVisible(element);
}

/**
 * Select the given item in the given list element found.
 * <p>
 * The items of the selection list are supposed to be found using
 * <code>by.xpath("./option")</code> search mechanism.
 * </p>
 * @param locator The locator of the list element in which perform the selection.
 * @param selection The item to select in the list, assuming that text matches
 * @return The selected element as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no item matches the expected selection.
 */
protected WebBrowserElement select(final By locator, final String selection) {
	return this.page.select(locator, selection);
}

/**
 * Select the given item in the given list element found.
 * <p>
 * The items of the selection list are supposed to be found using
 * <code>by.xpath("./option")</code> search mechanism.
 * </p>
 * @param listElement The list element in which perform the selection.
 * @param selection The item to select in the list, assuming that text matches
 * @return The selected element as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no item matches the expected selection.
 */
protected WebBrowserElement select(final WebBrowserElement listElement, final String selection) {
	return this.page.select(listElement, selection);
}

/**
 * Select the frame in which the current window is expected to be found.
 *
 * @see WebBrowser#selectFrame(int)
 */
protected void selectFrame() {
	if (this.frames[2] != this.frames[1]) { // == is intentional
		this.frames[2] = this.frames[1];
		this.browser.selectFrame(this.frames[2]);
		pause(250);
	}
}

/**
 * @see WebPage#shortTimeout()
 */
public int shortTimeout() {
	return this.page.shortTimeout();
}

/**
 * Start a timeout of the given seconds;
 *
 * @param timeout The timeout to wait in seconds
 * @param message Error message to display if the timeout is reached
 * @throws ScenarioFailedError If there was already a started timeout and it has
 * expired.
 */
protected void startTimeout(final int timeout, final String message) {
	if (this.currentTimeout == null) {
		this.currentTimeout = new Timeout(timeout, message);
	}
	this.currentTimeout.test();
}

/**
 * Store the browser the frame.
 * <p>
 * As frame might not be set when building the wrapper, this method allows
 * subclasses to store the current browser frame when they know that it matches
 * the one displayed inside by the wrapped element.
 * </p>
 */
protected void storeBrowserFrame() {
	this.frames[1] = this.browser.getCurrentFrame();
}

/**
 * Switch to initial browser frame.
 */
protected void switchToBrowserFrame() {
	switchToFrame(0);
}

private void switchToFrame(final int idx) {
	this.browser.selectFrame(this.frames[idx]);
	this.frames[2] = this.frames[idx];
}

/**
 * Switch to element frame.
 */
protected void switchToStoredFrame() {
	switchToFrame(1);
}

/**
 * Test whether the current timeout has been reached or not.
 *
 * @throws ScenarioFailedError If no timeout was set.
 * @throws WaitElementTimeoutError If the timeout has been reached
 * was set.
 */
protected void testTimeout() {
	if (this.currentTimeout == null) {
		throw new ScenarioFailedError("Programmation error, no timeout has been set, hence it cannot be tested!");
	}
	this.currentTimeout.test();
}

/**
 * @see WebPage#timeout()
 */
public int timeout() {
	return this.page.timeout();
}

/**
 * @see WebPage#typePassword(WebBrowserElement, SpotUser)
 * @since 6.0
 */
protected void typePassword(final WebBrowserElement element, final SpotUser user) {
	this.page.typePassword(element, user);
}

/**
 * @see WebPage#typeText(By, String)
 */
protected WebBrowserElement typeText(final By locator, final String text) {
	return this.page.typeText(locator, text);
}

/**
 * @see WebPage#typeTextWithParent(WebBrowserElement, By, String)
 */
protected WebBrowserElement typeText(final WebBrowserElement parentElement, final By locator, final String text) {
	return this.page.typeTextWithParent(parentElement, locator, text);
}

/**
 * @see WebPage#typeText(WebBrowserElement, String)
 */
protected void typeText(final WebBrowserElement inputElement, final String text) {
	this.page.typeText(inputElement, text);
}

/**
 * @see WebPage#waitForFirstMandatoryDisplayedElementInList(By...)
 */
protected WebBrowserElement[] waitForFirstMandatoryDisplayedPageElementInList(final By... locators) {
	return this.page.waitForFirstMandatoryDisplayedElementInList(locators);
}

/**
 * @see WebPage#waitForMandatoryDisplayedElement(By)
 */
protected WebBrowserElement waitForMandatoryDisplayedPageElement(final By locator) {
	return this.page.waitForMandatoryDisplayedElement(locator);
}

/**
 * @see WebPage#waitForMandatoryDisplayedElements(By)
 */
protected List<WebBrowserElement> waitForMandatoryDisplayedPageElements(final By locator) {
	return this.page.waitForMandatoryDisplayedElements(locator);
}

/**
 * @see WebPage#waitForMandatoryDisplayedElementsWithTimeout(By, int)
 */
protected List<WebBrowserElement> waitForMandatoryDisplayedPageElementsWithTimeout(final By locator, final int timeout) {
	return this.page.waitForMandatoryDisplayedElementsWithTimeout(locator, timeout);
}

/**
 * @see WebPage#waitForMandatoryDisplayedElementWithTimeout(By, int)
 */
public WebBrowserElement waitForMandatoryDisplayedPageElementWithTimeout(final By locator, final int timeout) {
	return this.page.waitForMandatoryDisplayedElementWithTimeout(locator, timeout);
}

/**
 * @see WebPage#waitForMandatoryElement(By)
 */
protected WebBrowserElement waitForMandatoryPageElement(final By locator) {
	return this.page.waitForMandatoryElement(locator);
}

/**
 * @see WebPage#waitForPotentialDisplayedElementsWithTimeout(By, int)
 */
protected List<WebBrowserElement> waitForPotentialDisplayedPageElementsWithTimeout(final By locator, final int timeout) {
	return this.page.waitForPotentialDisplayedElementsWithTimeout(locator, timeout);
}

/**
 * @see WebPage#waitForPotentialDisplayedElementWithTimeout(By, int)
 */
public WebBrowserElement waitForPotentialDisplayedPageElementWithTimeout(final By locator, final int timeout) {
	return this.page.waitForPotentialDisplayedElementWithTimeout(locator, timeout);
}

/**
 * @see WebPage#workaround(String)
 */
protected void workaround(final String message) {
	this.page.workaround(message);
}
}
