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

import static com.ibm.bear.qa.spot.core.browser.BrowserConstants.NO_ELEMENT_FOUND;
import static com.ibm.bear.qa.spot.core.config.Timeouts.SHORT_TIMEOUT;
import static com.ibm.bear.qa.spot.core.performance.PerfManager.PERFORMANCE_ENABLED;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.ByUtils.fixLocator;
import static com.ibm.bear.qa.spot.core.utils.ByUtils.getLocatorString;
import static com.ibm.bear.qa.spot.core.utils.StringUtils.hidePasswordInLocation;

import java.util.*;

import org.openqa.selenium.*;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.ibm.bear.qa.spot.core.api.SpotUser;
import com.ibm.bear.qa.spot.core.config.Timeouts;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.*;
import com.ibm.bear.qa.spot.core.timeout.*;
import com.ibm.bear.qa.spot.core.web.WebBrowser.ClickableWorkaroundState;

/**
 * A web browser element found in a {@link WebBrowser} page content.
 * <p>
 * This class implements the {@link WebElement} interface to be as most compatible as possible with
 * Selenium behavior.
 * </p><p>
 * This object is instantiated while finding element through {@link SearchContext} interface. As
 * {@link WebBrowser} and {@link WebBrowserElement} implement this interface, they only produce this
 * kind of object when finding element in the current web page content.
 * </p><p>
 * The main functionality of this specific web element is to be able to self recover when a
 * {@link StaleElementReferenceException} occurs while trying to execute any of the
 * {@link WebElement} interface operations.
 * </p><p>
 * This recovery is retried several times before given up if maximum of retries (
 * {@link #MAX_RECOVERY_ATTEMPTS} ) is reached.
 * </p><p>
 * When searching the web element for the first time, the browser, the frame and the index of the
 * elements in the parent's list are also stored to have the precise context used for the initial
 * research and then be sure to find the same element when recovering.
 * </p><p>
 * Additionally to the WebElement methods, this class also provide some useful functionalities as:
 * <ul>
 * <li>{@link #alter(boolean)}: Alter the selection status of the element.</li>
 * <li>{@link #click(boolean)}: Perform the {@link WebElement#click()} operation w/o recovery.</li>
 * <li>{@link #clickToMove()}: Perform a righ-click to select an element.</li>
 * <li>{@link #enterPassword(SpotUser)}: Enter the given password in current element.</li>
 * <li>{@link #executeScript(String)}: Execute the given script on the current web element.</li>
 * <li>{@link #findElement(By,boolean)}: Perform the {@link WebElement#findElement(By)} operation w/o recovery.</li>
 * <li>{@link #findElement(By,WebBrowserFrame,boolean)}: Perform the {@link WebElement#findElement(By)} operation in a frame w/o recovery.</li>
 * <li>{@link #findElements(By,boolean)}: Perform the {@link WebElement#findElements(By)} operation w/o recovery.</li>
 * <li>{@link #findElement(By,WebBrowserFrame,boolean)}: Perform the {@link WebElement#findElements(By)} operation in a frame w/o recovery.</li>
 * <li>{@link #getAncestor(int)}: Return the ancestor of the current element.</li>
 * <li>{@link #getAttributeClass()}: Return the value of the the &quot;class&quot; attribute.</li>
 * <li>{@link #getAttributeId()}: Return the value of the the &quot;id&quot; attribute.</li>
 * <li>{@link #getAttributeValue(String)}: Safely return the value of the given attribute.</li>
 * <li>{@link #getChild()}: Return the single child of the current element.</li>
 * <li>{@link #getChild(int)}: Return the child at the given index of the current element.</li>
 * <li>{@link #getChild(String)}: Return the child with the given tag of the current element.</li>
 * <li>{@link #getChildren()}: Return all children of the current element.</li>
 * <li>{@link #getChildren(String)}: Return specific children of the current element.</li>
 * <li>{@link #getFollowingSibling()}: Return the next sibling element from current one.</li>
 * <li>{@link #getFollowingSibling(String)}: Return the next sibling element from current one with the given tag.</li>
 * <li>{@link #getFrame()}: Return the element frame.</li>
 * <li>{@link #getFullLocator()}: Return the full locator for the current element.</li>
 * <li>{@link #getLocator()}: Return the locator to find the current element.</li>
 * <li>{@link #getParent()}: Return the parent of the current element.</li>
 * <li>{@link #getText(boolean)}: Perform the {@link WebElement#getText()} operation w/o recovery.</li>
 * <li>{@link #getTextEvenIfHidden()}: Returns the text of the web element even if it's hidden.</li>
 * <li>{@link #getWebElement()}: Return the wrapped {@link WebElement} .</li>
 * <li>{@link #isDisplayed(boolean)}: Perform the {@link WebElement#isDisplayed()} operation w/o recovery.</li>
 * <li></li>
 * </ul>
 * <p>
 * This class defines following public API methods of {@link WebElement} interface:
 * <ul>
 * <li>{@link #clear()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #click()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #findElement(By)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #findElements(By)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getAttribute(String)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getCssValue(String)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getLocation()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getRect()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getSize()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getTagName()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getText()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #isDisplayed()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #isEnabled()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #isSelected()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #sendKeys(CharSequence...)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #submit()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * </ul>
 * </p><p>
 * This class also defines following internal API methods:
 * <ul>
 * <li>{@link #alter(boolean)}: Alter the selection status of the element.</li>
 * <li>{@link #click(boolean)}: Perform the</li>
 * <li>{@link #click(boolean,boolean)}: Perform the</li>
 * <li>{@link #clickToMove()}: Perform a righ-click to select an element.</li>
 * <li>{@link #enterPassword(SpotUser)}: Enter the given password in current element.</li>
 * <li>{@link #executeScript(String)}: Execute the given script on the current web element.</li>
 * <li>{@link #findElement(By,boolean)}: Perform the</li>
 * <li>{@link #findElement(By,WebBrowserFrame,boolean)}: Perform the</li>
 * <li>{@link #findElements(By,boolean)}: Perform the</li>
 * <li>{@link #findElements(By,boolean,boolean)}: Perform the</li>
 * <li>{@link #getAllChildren()}: Return all children of the current element.</li>
 * <li>{@link #getAncestor(int)}: Return the ancestor of the current element.</li>
 * <li>{@link #getAttributeClass()}: Return the value of the the &quot;class&quot; attribute.</li>
 * <li>{@link #getAttributeId()}: Return the value of the the &quot;id&quot; attribute.</li>
 * <li>{@link #getAttributeValue(String)}: Safely return the value of the given attribute.</li>
 * <li>{@link #getChild()}: Return the single child of the current element.</li>
 * <li>{@link #getChild(int)}: Return the child at the given index of the current element.</li>
 * <li>{@link #getChild(String)}: Return the child with the given tag of the current element.</li>
 * <li>{@link #getChildren()}: Return children of the current element.</li>
 * <li>{@link #getChildren(String)}: Return specific children of the current element.</li>
 * <li>{@link #getFollowingSibling()}: Return the next sibling element from current one.</li>
 * <li>{@link #getFollowingSibling(String)}: Return the next sibling element from current one with the given tag.</li>
 * <li>{@link #getFrame()}: Return the element frame.</li>
 * <li>{@link #getFullLocator()}: Return the full locator for the current element.</li>
 * <li>{@link #getList(List)}: Return a list of</li>
 * <li>{@link #getLocator()}: Return the locator to find the current element.</li>
 * <li>{@link #getParent()}: Return the parent of the current element.</li>
 * <li>{@link #getRelative(By)}: Find the relative element with given locator from current web element context.</li>
 * <li>{@link #getScreenshotAs(OutputType)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getText(boolean)}: Perform the</li>
 * <li>{@link #getTextEvenIfHidden()}: Returns the text of the web element even if it's hidden.</li>
 * <li>{@link #getWebElement()}: Return the wrapped</li>
 * <li>{@link #isDisplayed(boolean)}: Perform the</li>
 * <li>{@link #isEnabled(boolean)}: Perform the</li>
 * <li>{@link #isInFrame()}: Tells whether the current element is in a frame or not.</li>
 * <li>{@link #makeVisible()}: Make the web element visible.</li>
 * <li>{@link #makeVisible(boolean)}: Make the web element visible.</li>
 * <li>{@link #mouseOver()}: Simulate a mouse over by forcing a trigger of the javascript <code>mouseover</code></li>
 * <li>{@link #moveToElement()}: Move to the current web element.</li>
 * <li>{@link #moveToElement(boolean)}: Move to the current web element.</li>
 * <li>{@link #rightClick()}: Performs a right click action on the element.</li>
 * <li>{@link #scrollIntoView()}: Scroll the page to the given element.</li>
 * <li>{@link #scrollSmoothIntoView()}: Scroll the page to the given element.</li>
 * <li>{@link #select()}: Select or check the given element. This operation only applies to input</li>
 * <li>{@link #sendKeys(boolean,CharSequence...)}: Perform the</li>
 * <li>{@link #setVisibility(boolean)}: Set current element visibility to given value.</li>
 * <li>{@link #setVisible(int)}: Set the current element visible with the given width.</li>
 * <li>{@link #synchronize()}: Resynchronize the current with web element displayed in the page.</li>
 * <li>{@link #toString()}: Answers a string containing a concise, human-readable</li>
 * <li>{@link #waitForMandatoryDisplayedChildElement(By,int)}: Wait until have found the mandatory child element using the given relative locator and timeout.</li>
 * <li>{@link #waitForPotentialChildElement(By,int)}: Wait until have found a potential child element using the given relative locator and timeout.</li>
 * <li>{@link #waitForPotentialChildrenElements(By,int)}: Wait until have found potential children elements using the given relative locator and timeout.</li>
 * <li>{@link #waitForPotentialDisplayedChildElement(By,int)}: Wait until have found a potential displayed element using the given relative locator and timeout.</li>
 * <li>{@link #waitForPotentialDisplayedChildrenElements(By,int)}: Wait until have found potential displayed elements using the given relative locator and timeout.</li>
 * <li>{@link #waitShortlyForFirstMandatoryDisplayedChildElementInList(By...)}: Wait shortly until have found one mandatory displayed child element of the given relative locators list.</li>
 * <li>{@link #waitShortlyForMandatoryChildElement(By)}: Wait shortly until have found the mandatory child element using the given relative locator.</li>
 * <li>{@link #waitShortlyForMandatoryChildrenElements(By)}: Wait shortly until have found mandatory children elements using the given relative locator.</li>
 * <li>{@link #waitShortlyForMandatoryDisplayedChildElement(By)}: Wait shortly until have found mandatory displayed child element using the given relative locator.</li>
 * <li>{@link #waitShortlyForMandatoryDisplayedChildrenElements(By)}: Wait shortly until have found mandatory displayed children elements using the given relative locator.</li>
 * <li>{@link #waitWhileDisplayed(int)}: Wait while the current web element is displayed in the page.</li>
 * <li>{@link #waitWhileDisplayed(int,boolean)}: Wait while the current web element is displayed in the page.</li>
 * <li>{@link #waitWhileEnabled(int)}: Wait while the current web element is enabled in the page.</li>
 * <li>{@link #waitWhileEnabled(int,boolean)}: Wait while the current web element is enabled in the page.</li>
 * <li>{@link #waitWhileHasText(int)}: Wait until the current web element has text visible.</li>
 * <li>{@link #waitWhileHasText(int,boolean)}: Wait until the current web element has text visible.</li>
 * <li>{@link #waitWhileNotDisplayed(int)}: Wait until the current web element gets displayed in the page.</li>
 * <li>{@link #waitWhileNotDisplayed(int,boolean)}: Wait until the current web element gets displayed in the page.</li>
 * <li>{@link #waitWhileNotEnabled(int)}: Wait until the current web element gets enabled in the page.</li>
 * <li>{@link #waitWhileNotEnabled(int,boolean)}: Wait until the current web element gets enabled in the page.</li>
 * </ul>
 * </p>
 */
public class WebBrowserElement implements WebElement {

	/* Locators */
	private static final By CHILDREN_LOCATOR = By.xpath("./child::*");

	/* Javascripts */
	private final static String MOUSE_OVER_JAVASCRIPT =
		"var forceHoverEvent = document.createEvent('MouseEvents');" +
		"forceHoverEvent.initEvent( 'mouseover', true, false );" +
		"arguments[0].dispatchEvent(forceHoverEvent);";
//	private final static String MOVE_TO_ELEMENT = "arguments[0].scrollIntoView(true);";

	/* Constants */
	private static final CharSequence[] NIL_KEYS_SEQUENCE = new CharSequence[] { Keys.SPACE, Keys.BACK_SPACE };

	/**
	 * The maximum of attempts when recovering the current web element.
	 */
	public static final int MAX_RECOVERY_ATTEMPTS = 5;

/**
 * Return a list of {@link WebBrowserElement} assuming the given list *is* a
 * list of this kind of {@link WebElement}.
 *
 * @param elements The list of {@link WebElement}.
 * @return The list of {@link WebBrowserElement}.
 * @throws IllegalArgumentException If one of the element of the given list is
 * not a {@link WebBrowserElement}.
 */
public static List<WebBrowserElement> getList(final List<WebElement> elements) {
	List<WebBrowserElement> webElements = new ArrayList<WebBrowserElement>(elements.size());
	for (WebElement element: elements) {
		try {
			webElements.add((WebBrowserElement)element);
		}
		catch (ClassCastException cce) {
			throw new IllegalArgumentException("The given list was not a list of WebBrowserElement: "+cce.getMessage());
		}
	}
	return webElements;
}

	/**
	 * The browser to use to search the web element.
	 */
	final WebBrowser browser;

	/**
	 * The locator to use to search the web element.
	 */
	final By locator;

	/**
	 * The context to use to search the web element.
	 * <p>
	 * If the search is expected to be done in the entire web document, then
	 * it will be a {@link WebDriver} object, otherwise, ie. if the search is expected
	 * to be done relatively to another web element, then it will be a
	 * {@link WebBrowserElement}.
	 * </p>
	 */
	final private SearchContext context;

	/**
	 * The wrapped selenium web element.
	 */
	WebElement webElement;

	/**
	 * The frame used when searching the current web element.
	 */
	private WebBrowserFrame frame;

	/**
	 * Information of parent when the current web element has been found
	 * among several other elements.
	 * <p>
	 * These information allow recovering to be more precise, hence be sure not
	 * to recover another element.
	 * </p>
	 */
	final private int parentListSize, parentListIndex;

/**
 * Create a web browser element using the given locator.
 * <p>
 * The search of the corresponding {@link WebElement} is done through the entire
 * browser page and in the current frame. The browser is stored to allow recovery.
 * </p><p>
 * Note that this constructor is typically used when search for a single element.
 * </p>
 * @param browser The browser where web page containing the web element is displayed.
 * @param locator The locator to be used to search for the element in the context
 */
protected WebBrowserElement(final WebBrowser browser, final By locator) {
	this(browser, browser.getCurrentFrame(), browser.driver, locator, null, 0, -1);
}

/**
 * Create a web browser element using the given locator in the given
 * search context and in the current frame.
 * <p>
 * The browser is stored to allow recovery.
 * </p><p>
 * Note that this constructor is typically used when search for a single element.
 * </p>
 * @param browser The browser where web page containing the web element is displayed.
 * @param context The context to search for the element. It might be the browser driver
 * or a parent element.
 * @param locator The locator to be used to search for the element in the context
 */
public WebBrowserElement(final WebBrowser browser, final SearchContext context, final By locator) {
	this(browser, browser.getCurrentFrame(), context, locator, null, 0, -1);
}

/**
 * Create a web browser element instance using the given locator
 * in the given search context and frame.
 * <p>
 * The browser is stored to allow recovery.
 * </p><p>
 * Note that this constructor is typically used when search for a single element.
 * </p>
 * @param browser The browser where web page containing the web element is displayed.
 * @param webFrame The frame in which the element is supposed to be (<code>null</code>
 * if the web element does not belong to any frame)
 * @param context The context to search for the element. It might be the browser driver
 * or a parent element.
 * @param locator The locator to be used to search for the element in the context
 */
public WebBrowserElement(final WebBrowser browser, final WebBrowserFrame webFrame, final SearchContext context, final By locator) {
	this(browser, webFrame, context, locator, null, 0, -1);
}

/**
 * Create a web browser element using the given locator in the given
 * search context.
 * <p>
 * The browser is stored to allow recovery.
 * </p><p>
 * Note that this constructor is typically used when search for a single element.
 * </p>
 * @param browser The browser where web page containing the web element is displayed.
 * @param webFrame The frame in which the element is supposed to be (<code>null</code>
 * if the web element does not belong to any frame)
 * @param context The context to search for the element. It might be the browser driver
 * or a parent element.
 * @param locator The locator to be used to search for the element in the context
 * @param element The element wrapped by the created instance. If this
 * argument is used, then the locator will be ignored.
 * @param size The size of the parent element children list. This argument is
 * used when searching for several element (see {@link #findElements(By, boolean, boolean)})
 * @param index The index in the parent element children list. This argument is
 * used when searching for several element (see {@link #findElements(By, boolean, boolean)})
 */
public WebBrowserElement(final WebBrowser browser, final WebBrowserFrame webFrame, final SearchContext context, final By locator, final WebElement element, final int size, final int index) {
	super();
	this.browser = browser;
	this.context = context;
	this.locator = locator;
	this.frame = webFrame;
	this.parentListSize = size;
	this.parentListIndex = index;
	if (element == null) {
		if (context instanceof WebBrowserElement) {
			WebBrowserElement parentElement = (WebBrowserElement) context;
			this.webElement = parentElement.webElement.findElement(locator);
			if (this.frame != null && !this.frame.equals(parentElement.frame) || (this.frame == null && parentElement.frame != null)) {
				throw new ScenarioFailedError("Current frame ("+this.frame+") is different than its parent ("+parentElement.frame+ ")! Web element hierarchy should be in the same frame!");
			}
		} else {
			this.webElement = context.findElement(locator);
		}
	} else {
		if (element instanceof WebBrowserElement) {
			WebBrowserElement parentElement = (WebBrowserElement) element;
			this.webElement = parentElement.webElement;
			if (this.frame != null && !this.frame.equals(parentElement.frame) || (this.frame == null && parentElement.frame != null)) {
				throw new ScenarioFailedError("Current frame ("+this.frame+") is different than its parent ("+parentElement.frame+ ")! Web element hierarchy should be in the same frame!");
			}
		} else {
			this.webElement = element;
		}
	}
	if (this.webElement == null) {
		throw new ScenarioFailedError("Web element should not be null!");
	}
	if (this.webElement instanceof WebBrowserElement) {
		throw new ScenarioFailedError("Web element should not be a WebBrowserElement!");
	}
}

/**
 * Alter the selection status of the element.
 * <p>
 * This operation only applies to input elements such as checkboxes, options in a
 * select and radio buttons. The element status will only be altered if the current
 * status is different from the provided.
 * </p>
 * @param select Specifies whether to select or clear the element. The value <b>true</b>
 * or <b>false</b> implies that the element should be selected or cleared respectively.
 * @return The element that has been altered.
 */
public WebBrowserElement alter(final boolean select) {
	if ((select && !isSelected()) || (!select && isSelected())) {
		click();
	}
	return this;
}

/*
 * Recovery when an exception has occurred on a web element operation.
 *
 * TODO Change the exception parameter as StaleElementReferenceException
 * as this is the only caught exception now...
 */
private void catchWebDriverException(final WebDriverException wde, final String title, final int count, final boolean recovery) {

	// First use browser exception catching
	this.browser.catchWebDriverException(wde, title, count);

	// Recover the element
	int n = 0;
	while (true) {
		try {
			if (	recover(n) || !recovery) return;
			if (n++ >= MAX_RECOVERY_ATTEMPTS) {
				debugPrintln("Cannot recover even after "+MAX_RECOVERY_ATTEMPTS+" retries... give up");
				return;
			}
		}
		catch (WebDriverException ex) {

			// Give up right now if the exception is too serious
			if (!(ex instanceof StaleElementReferenceException)) {
				debugPrintln("Fatal exception occured when "+title+"'... give up");
		    	debugPrintException(ex);
				throw ex;
			}

			// Give up now if no recovery
			if (!recovery) return;

			// Give up if too many failures occurred
			if (n++ >= MAX_RECOVERY_ATTEMPTS) {
				debugPrintln("More than "+MAX_RECOVERY_ATTEMPTS+" exceptions occured when trying to find again the "+this.locator+"'... give up");
		    	debugPrintException(ex);
				throw wde;
			}

			// Workaround
			debugPrint("ScenarioWorkaround exception when trying to find again the "+this.locator+"': ");
			debugPrintException(ex);
			sleep(1);
		}
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Note that after the selenium clear operation is done, 2 keyboard keys (see
 * {@link #NIL_KEYS_SEQUENCE}) are pressed to simulate a true manual clear.
 * These keyboards hits do not add any text but by using the keyboard that
 * triggers corresponding events to activate potential javascript listeners.
 * </p>
 */
@Override
public void clear() {
	debugPrintEnteringMethod();

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				this.webElement.clear();
				this.webElement.sendKeys(NIL_KEYS_SEQUENCE);
				if (DEBUG) debugPrintln("			 ( -> done.)");
				return;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "clearing", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public void click() {
	// Start performance timer if necessary
	if (PERFORMANCE_ENABLED) {
		this.browser.perfManager.startServerTimer();
	}

	try {
		click(true/*recovery*/);
	}
	catch (ElementNotInteractableException enie) {
		println("WARNING: Catching ElementNotVisibleException exception: "+enie.getMessage());
		printStackTrace(enie.getStackTrace(), 1);
		this.browser.takeScreenshotWarning("ClickOnNonInteractableElement");
		println("	-> Workaround is to force the element to be visible using javascript...");
		setVisibility(true);
		println("	-> Click again on the element...");
		click(true/*recovery*/);
	}
}

/**
 * Perform the {@link WebElement#click()} operation w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 *  </p>
 * @param recovery Tells whether try to recover is a {@link WebDriverException}
 * occurs
 * @see WebElement#click()
 */
public void click(final boolean recovery) {
	debugPrintEnteringMethod("recovery", recovery);
	click(recovery, true);
}

/**
 * Perform the {@link WebElement#click()} operation w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 *  </p>
 * @param recovery Tells whether try to recover is a {@link WebDriverException} occurs
 * @param workaround Tells whether workaround should be used or not
 * @see WebElement#click()
 */
public void click(final boolean recovery, final boolean workaround) {
	debugPrintEnteringMethod("recovery", recovery, "workaround", workaround);

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	this.browser.errorMessage = null;
	ClickableWorkaroundState state = workaround ? ClickableWorkaroundState.Init : ClickableWorkaroundState.None;
	WebDriverException initialNotClickableException = null;
	try {
		int count = 0;
		while (true) {
			try {
				switch (state) {
					case Javascript:
						executeScript("click()");
						break;
					case Failed:
						if (initialNotClickableException == null) {
							throw new ScenarioImplementationError("We should have stored an exception at this point!!!");
						}
						throw initialNotClickableException;
					default:
						this.webElement.click();
						break;
				}
				if (DEBUG) debugPrintln("			 ( -> done.)");
				return;
			}
			catch (ElementNotInteractableException enie) {
				if (!workaround) {
					throw enie;
				}
				if (initialNotClickableException == null) {
					initialNotClickableException = enie;
				}
				state = this.browser.workaroundForNotClickableException(state, this, enie);
			}
			catch (WebDriverException wde) {
				String message = wde.getMessage();
				if (message.contains(" is not clickable") && message.contains("Other element would receive the click")) {
					if (initialNotClickableException == null) {
						initialNotClickableException = wde;
					}
					state = this.browser.workaroundForNotClickableException(state, this, wde);
				} else {
					if (recovery) {
						catchWebDriverException(wde, "clicking", count++, true);
					} else {
						throw wde;
					}
				}
			}
		}
	}
	finally {
		if (workaround && state != ClickableWorkaroundState.Init) {
			if (initialNotClickableException != null) {
				println("WARNING: Following exception was encountered when clicking on web element "+getFullLocator()+":");
				printStackTrace(initialNotClickableException.getStackTrace(), 1);
			}
			if (state == ClickableWorkaroundState.Failed) {
				println("Workarounds were tentatively applied with no success to circumvent it:");
			} else {
				println("Workaround "+state+" was found and applied and successfully circumvent it:");
			}
			this.browser.printErrorMessage();
		}
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Perform a righ-click to select an element.
 * <p>
 * Note that the ESC Key is also send to the current element where the mouse
 * cursor has been moved to. Then, if a popup-menu appears while doing this
 * right-click operation, it's instantaneously closed.
 * </p>
 */
public void clickToMove() {
	debugPrintEnteringMethod();
//	if (DEBUG) debugPrintln("			(clicking to move on "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		this.browser.actions.contextClick(this).sendKeys(Keys.ESCAPE).build().perform();
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Enter the given password in current element.
 * <p>
 * This operation is equivalent to {@link #sendKeys(CharSequence...)} where text
 * is the given user password, decrypted if necessary.
 * </p><p>
 * Recovery is allowed for this operation which means that any {@link WebDriverException}
 * (except {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  is caught and the operation retried until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 *  </p>
 * @param user User whom password has to be typed in the current element
 * @see #sendKeys(boolean, CharSequence...)
 * @since 6.0
 */
public void enterPassword(final SpotUser user) {
	sendKeys(true/*recovery*/, true/*password*/, ((User)user).getDecryptedPassword());
}

/**
 * Execute the given script on the current web element.
 *
 * @param script The script to execute
 * @return One of Boolean, Long, String, List or WebElement. Or null.
 */
public Object executeScript(final String script) {
	return getJavascriptExecutor().executeScript("arguments[0]."+script+";", this.webElement);
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p><p>
 * The search is performed in the current frame.
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @return The found web element as a {@link WebBrowserElement} or <code>null</code>
 * if the element is not found
 */
@Override
public WebBrowserElement findElement(final By relativeLocator) {
	return findElement(relativeLocator, this.frame, true/*recovery*/);
}

/**
 * Perform the {@link WebElement#findElement(By)} operation w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 * </p><p>
 * The search is performed in the current frame.
 *  </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param recovery Tells whether try to recover if a {@link WebDriverException} occurs
 * @return The found web element as a {@link WebBrowserElement} or <code>null</code>
 * if the element is not found
 */
public WebBrowserElement findElement(final By relativeLocator, final boolean recovery) {
	return findElement(relativeLocator, this.frame, recovery);
}

/**
 * Perform the {@link WebElement#findElement(By)} operation in a frame w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 * </p><p>
 * If recovery is not allowed and an exception occurred, then it's still caught
 * but <code>null</code> is returned instead of retrying.
 * </p><p>
 * The search is performed in the current frame.
 *  </p>
 * @param relativeLocator The locator to use for the search
 * @param webFrame The frame to use to find for the web element
 * @param recovery Tells whether try to recover if a {@link WebDriverException} occurs
 * @return The found web element as a {@link WebBrowserElement} or <code>null</code>
 * if the element is not found
 * @see WebElement#findElement(By)
 */
public WebBrowserElement findElement(final By relativeLocator, final WebBrowserFrame webFrame, final boolean recovery) {
	if (DEBUG) {
		debugPrintEnteringMethod("relativeLocator", getLocatorString(relativeLocator), "webFrame", webFrame, "recovery", recovery);
	}

	// Fix locator if necessary
	By fixedLocator = fixLocator(relativeLocator);

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		// Find element
		int count = 0;
		while (true) {
			try {
				WebBrowserElement webPageElement = new WebBrowserElement(this.browser, webFrame, this, fixedLocator);
				if (DEBUG) debugPrintln("			(  -> found "+webPageElement+")");
				return webPageElement;
			}
			catch (@SuppressWarnings("unused") NoSuchElementException nsee) {
				return null;
			}
			catch (@SuppressWarnings("unused") UnhandledAlertException uae) {
				this.browser.purgeAlerts("Finding element '"+fixedLocator+"'");
				if (!recovery) {
					return null;
				}
			}
			catch (WebDriverException wde) {
				if (recovery) {
					catchWebDriverException(wde, "finding element '"+fixedLocator+")", count, recovery);
				} else {
					if (DEBUG) debugPrintException(wde);
					return null;
				}
				count++;
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 * @param relativeLocator The locator to find the elements in the current page.
 * @return The web elements list as a {@link List} of {@link WebBrowserElement}
 */
@Override
public List<WebElement> findElements(final By relativeLocator) {
	return findElements(relativeLocator, true/*displayed*/, true/*recovery*/);
}

/**
 * Perform the {@link WebElement#findElements(By)} operation w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 * </p><p>
 * If recovery is not allowed and an exception occurs, then it's still caught
 * but an empty list is returned instead of retrying.
 * </p><p>
 * Note that only displayed elements are added to the returned list.
 *  </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param recovery Tells whether try to recover is a {@link WebDriverException} occurs
 * @return The web elements list as a {@link List} of {@link WebBrowserElement}
 */
public List<WebElement> findElements(final By relativeLocator, final boolean recovery) {
	return findElements(relativeLocator, true/*displayed*/, recovery);
}

/**
 * Perform the {@link WebElement#findElements(By)} operation in a frame w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 * </p><p>
 * If recovery is not allowed and an exception occurs, then it's still caught
 * but an empty list is returned instead of retrying.
 *  </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param recovery Tells whether try to recover is a {@link WebDriverException} occurs
 * @return The web elements list as a {@link List} of {@link WebBrowserElement}
 * or an empty list if nothing matches
 * @see WebElement#findElements(By)
 */
public List<WebElement> findElements(final By relativeLocator, final boolean displayed, final boolean recovery) {
	if (DEBUG) {
		debugPrintEnteringMethod("relativeLocator", getLocatorString(relativeLocator), "displayed", displayed, "recovery", recovery);
	}

	// Fix locator if necessary
	By fixedLocator = fixLocator(relativeLocator);

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	// Find elements
	try{
		int count = 0;
		while (true) {
			try {
				List<WebElement> foundElements = this.webElement.findElements(fixedLocator);
				final int listSize = foundElements.size();
				List<WebElement> pageElements = new ArrayList<WebElement>(listSize);
				for (int idx=0; idx<listSize; idx++) {
					WebElement foundElement = foundElements.get(idx);
					boolean addingElement = false;
					try {
						// Check whether the element is displayed
						addingElement = !displayed || foundElement.isDisplayed();
					}
					catch (@SuppressWarnings("unused") WebDriverException wde) {
						// Skip any web driver exception by considering the faulty element as not displayed
					}
					catch (NullPointerException npe) {
						if (DEBUG) {
							debugPrintln("			WORKAROUND: exception "+npe.getMessage()+" has been caught...");
							debugPrintln("			 -> See Selenium bug https://github.com/SeleniumHQ/selenium/issues/9266 which won't be fixed in version 3...");
							debugPrintln("			 -> Ignore it!");
						}
					}
					if (addingElement) {
						WebBrowserElement webPageElement = new WebBrowserElement(this.browser, this.frame, this, fixedLocator, foundElement, listSize, idx);
						pageElements.add(webPageElement);
						if (DEBUG) {
							debugPrint("			  (-> found '"+webPageElement);
							debugPrintln(")");
						}
					} else {
						if (DEBUG) debugPrintln("			  (-> element not displayed)");
					}
				}
				return pageElements;
			}
			catch (@SuppressWarnings("unused") UnhandledAlertException uae) {
				this.browser.purgeAlerts("Finding element '"+fixedLocator+"'");
				if (!recovery) {
					return NO_ELEMENT_FOUND;
				}
			}
			catch (WebDriverException wde) {
				if (recovery) {
					catchWebDriverException(wde, "finding elements '"+fixedLocator+")", count++, recovery);
				} else {
					if (DEBUG) debugPrintException(wde);
					return NO_ELEMENT_FOUND;
				}
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Return all children of the current element.
 * <p>
 * Note that both displayed and hidden elements are returned.
 * </p>
 * @return The list of web element children as a {@link List} of {@link WebBrowserElement}.
 */
public List<WebBrowserElement> getAllChildren() {
	return getList(findElements(CHILDREN_LOCATOR, /* displayed: */false, /* recovery: */true));
}

/**
 * Return the ancestor of the current element.
 *
 * @param depth The depth in the ancestor hierarchy. Must be positive, if <code>0</code>
 * then return the current instance.
 * @return The web element ancestor as a {@link WebBrowserElement}.
 */
public WebBrowserElement getAncestor(final int depth) {
	if (depth < 0) {
		throw new IllegalArgumentException("Cannot get ancestor with negative or zero relative depth.");
	}
	if (depth == 0) return this;
	StringBuilder xpathBuilder = new StringBuilder("..");
	for (int i=1; i<depth; i++) {
		xpathBuilder.append("/..");
	}
	return findElement(By.xpath(xpathBuilder.toString()));
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public String getAttribute(final String name) {
	return getAttribute(name, false/*fail*/);
}

private String getAttribute(final String name, final boolean fail) throws ScenarioFailedError {
	debugPrintEnteringMethod("name", name, "fail", fail);
//	if (DEBUG) debugPrintln("			(getting attribute '"+name+"' for "+this+", fail="+fail+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				String attribute = this.webElement.getAttribute(name);
				if (DEBUG) {
					debugPrint("			 ( -> \"");
					if (attribute == null) {
						debugPrintln("null\")");
					}
					else {
						debugPrint(name.equals("href") ? hidePasswordInLocation(attribute):attribute);
						debugPrintln("\")");
					}
				}
				if (attribute == null && fail) {
					throw new ScenarioFailedError("Cannot find attribute '"+name+"' in web element "+this);
				}
				return attribute;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "getting attribute '"+name+"'", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Return the value of the the &quot;class&quot; attribute.
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 * @return The non-null attribute value as a {@link String}
 * @throws ScenarioFailedError If the &quot;class&quot; attribute is not found
 */
public String getAttributeClass() throws ScenarioFailedError {
	return getAttribute("class", true/*fail*/);
}

/**
 * Return the value of the the &quot;id&quot; attribute.
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 * @return The non-null attribute value as a {@link String}
 * @throws ScenarioFailedError If the &quot;id&quot; attribute is not found
 */
public String getAttributeId() throws ScenarioFailedError {
	return getAttribute("id", true/*fail*/);
}

/**
 * Safely return the value of the given attribute.
 * <p>
 * Controversially to {@link #getAttribute(String)} method, this one will fail if
 * the attribute is not found (ie. when interface implementation {@link #getAttribute(String)}
 * would have returned <code>null</code>).
 * </p><p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 * @param name The attribute name
 * @return The non-null attribute value as a {@link String}
 * @throws ScenarioFailedError If the attribute is not found in the current element
 */
public String getAttributeValue(final String name) throws ScenarioFailedError {
	return getAttribute(name, true/*fail*/);
}

/**
 * Return the single child of the current element.
 *
 * @return The web element child as a {@link WebBrowserElement}.
 * @throws ScenarioFailedError If there are either no child or several children
 * for the current element.
 */
public WebBrowserElement getChild() throws ScenarioFailedError{
	List<WebBrowserElement> children = getChildren();
	switch(children.size()) {
		case 1:
			return children.get(0);
		case 0:
			throw new WaitElementTimeoutError("Web element "+this+" has no child.");
		default:
			throw new ScenarioFailedError("Web element "+this+" has more than one child.");
	}
}

/**
 * Return the child at the given index of the current element.
 *
 * @return The web element child as a {@link WebBrowserElement}.
 * @throws ScenarioFailedError If there are either no child or the index is
 * out of children bound.
 */
public WebBrowserElement getChild(final int index) throws ScenarioFailedError{
	List<WebBrowserElement> children = getChildren();
	int size = children.size();
	if (index >= 0 && index < size) {
		return children.get(index);
	}
	if (size == 0) {
		throw new WaitElementTimeoutError("Web element "+this+" has no child.");
	}
	throw new ScenarioFailedError("Web element "+this+" has only "+size+" "+(size==1?"child":"children")+", hence cannot return child at index "+index+".");
}

/**
 * Return the child with the given tag of the current element.
 *
 * @param tag The tag of the expected child element.
 * @return The web element child as a {@link WebBrowserElement}.
 * @throws ScenarioFailedError If there are either no child or several children
 * for the current element.
 */
public WebBrowserElement getChild(final String tag) throws ScenarioFailedError{
	List<WebBrowserElement> children = getChildren(tag);
	switch(children.size()) {
		case 1:
			return children.get(0);
		case 0:
			throw new WaitElementTimeoutError("Web element "+this+" has no child with tag name '"+tag+"'.");
		default:
			throw new ScenarioFailedError("Web element "+this+" has more than one child with tag name '"+tag+"'.");
	}
}

/**
 * Return children of the current element.
 * <p>
 * Note that only displayed elements are returned.
 * </p>
 * @return The list of web element children as a {@link List} of {@link WebBrowserElement}.
 */
public List<WebBrowserElement> getChildren() {
	return getList(findElements(CHILDREN_LOCATOR));
}

/**
 * Return specific children of the current element.
 *
 * @param tag The tag name of looked for children
 * @return The list of web element children as a {@link List} of {@link WebBrowserElement}.
 */
public List<WebBrowserElement> getChildren(final String tag) {
	return getList(findElements(By.xpath("./child::"+tag)));
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public String getCssValue(final String propertyName) {
	debugPrintEnteringMethod("propertyName", propertyName);
//	if (DEBUG) debugPrintln("			(getting CSS value of '"+propertyName+"' for "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				String value = this.webElement.getCssValue(propertyName);
				if (DEBUG) debugPrintln("			 ( -> \""+value+"\")");
				return value;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "getting CSS value of '"+propertyName+")", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Return the next sibling element from current one.
 *
 * @return The current element following sibling element
 * @throws WaitElementTimeoutError If there is no current element sibling.
 */
public WebBrowserElement getFollowingSibling() throws WaitElementTimeoutError {
	return getFollowingSibling("*");
}

/**
 * Return the next sibling element from current one with the given tag.
 *
 * @param tag The tag of the expected sibling element.
 * @return The current element following sibling element
 * @throws WaitElementTimeoutError If there is no current element sibling
 * with the given tag.
 */
public WebBrowserElement getFollowingSibling(final String tag) throws WaitElementTimeoutError {
	By siblingLocator = By.xpath("./following-sibling::"+tag);
	return this.browser.waitForElement(this, siblingLocator, /*fail: */true, SHORT_TIMEOUT, /*displayed:*/true, /*single:*/false);

}

/**
 * Return the element frame.
 *
 * @return The frame as a {@link WebBrowserFrame}.
 */
public WebBrowserFrame getFrame() {
	return this.frame;
}

/**
 * Return the full locator for the current element.
 *
 * @return The full xpath as a {@link String} or <code>null</code> if the search
 * locator was not found {@link ByXPath} one.
 */
public String getFullLocator() {
	StringBuilder locatorBuilder = new StringBuilder();
	if (this.context instanceof WebBrowserElement) {
		locatorBuilder.append(((WebBrowserElement) this.context).getFullLocator());
	}
	final String locatorString = getLocatorString(this.locator);
	if (locatorBuilder.length() > 0) {
		locatorBuilder.append("->");
		if (this.locator instanceof By.ByXPath) {
			if (!locatorString.startsWith("By.xpath(\".")) {
			println("Non relative xpath for child element:");
				println("	- xpath: "+locatorString);
				println("	- parent: "+locatorBuilder.substring(0, locatorBuilder.length()-2));
			println("	- stack trace:");
			StackTraceElement[] elements = new Exception().getStackTrace();
			printStackTrace(elements, 2);
		}
	}
}
	return locatorBuilder.append(locatorString).toString();
}

private JavascriptExecutor getJavascriptExecutor() {
	return this.browser.getJavascriptExecutor();
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public Point getLocation() {
	debugPrintEnteringMethod();
//	if (DEBUG) debugPrintln("			(getting location of "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				Point location = this.webElement.getLocation();
				if (DEBUG) debugPrintln("			 ( -> "+location+")");
				return location;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "getting location", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Return the locator to find the current element.
 *
 * @return The locator as a {@link By}.
 */
public By getLocator() {
	return this.locator;
}

private String getNewVisibilityStyle(final String styleAttribute, final int width) throws ScenarioFailedError {
	if (styleAttribute.isEmpty()) {
		return "visibility: visible; width: "+width+"px;";
	}
	StringTokenizer tokenizer = new StringTokenizer(styleAttribute, ";");
	StringBuffer newStyleAttribute = new StringBuffer();
	String separator = EMPTY_STRING;
	while (tokenizer.hasMoreTokens()) {
		newStyleAttribute.append(separator);
		String token = tokenizer.nextToken().trim();
		if (token.startsWith("visibility")) {
			newStyleAttribute.append("visibility: visible");
		} else if (token.startsWith("width")) {
			newStyleAttribute.append("width: ").append(width).append("px");
		} else {
			newStyleAttribute.append(token);
		}
		separator = "; ";
	}
	return newStyleAttribute.append(';').toString();
}

/**
 * Return the parent of the current element.
 *
 * @return The web element parent as a {@link WebBrowserElement}.
 */
public WebBrowserElement getParent() {
	return findElement(By.xpath(".."));
}

@Override
public Rectangle getRect() {
	return new Rectangle(getLocation(), getSize());
}

/**
 * Find the relative element with given locator from current web element context.
 *
 * @param relativeLocator The locator of the searched element relatively to current context
 * @return Found element or <code>null</code> if no element is found
 */
public WebBrowserElement getRelative(final By relativeLocator) {
	return new WebBrowserElement(this.browser, this.context, relativeLocator);
}

@Override
public <X> X getScreenshotAs(final OutputType<X> target) throws WebDriverException {
	return ((TakesScreenshot)this.browser.driver).getScreenshotAs(target);
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public Dimension getSize() {
	debugPrintEnteringMethod();
//	if (DEBUG) debugPrintln("			(getting size of "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				Dimension size = this.webElement.getSize();
				if (DEBUG) debugPrintln("			 ( -> "+size+")");
				return size;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "getting size", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public String getTagName() {
	debugPrintEnteringMethod();
//	if (DEBUG) debugPrintln("			(getting tag name of "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				String tagName= this.webElement.getTagName();
				if (DEBUG) debugPrintln("			 ( -> \""+tagName+"\")");
				return tagName;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "getting tag name", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public String getText() {
	return getText(true/*recovery*/);
}

/**
 * Perform the {@link WebElement#getText()} operation w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made. In the latter case, no exception is raised, but an
 *  empty string is returned.
 * </p><p>
 * If recovery is not allowed and an exception occurs, then it's still caught
 * but an empty string is returned instead of retrying.
 *  </p><p>
 *  Make sure that the element is in the currently selected frame before
 *  calling this function. Otherwise the driver will switch active frames
 *  without the new frame being reflected in {@link WebBrowser}
 *  </p>
 *
 * @param recovery Tells whether try to recover is a {@link WebDriverException}
 * occurs
 * @see WebElement#getText()
 */
public String getText(final boolean recovery) {
	debugPrintEnteringMethod("recovery", recovery);
//	if (DEBUG) debugPrintln("			(getting text for "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				String text= this.webElement.getText().trim();
				if (DEBUG) debugPrintln("			 ( -> \""+text+"\")");
				return text;
			}
			catch (WebDriverException wde) {
				if (recovery) {
					try{
						catchWebDriverException(wde, "getting text", count++, true);
					}
					catch (WebDriverException wde2) {
						if (DEBUG) {
							debugPrintln("			(WORKAROUND: exception "+wde2.getMessage()+" has been caught...");
							debugPrintln("			 -> return empty string \"\" instead)");
						}
						return EMPTY_STRING;
					}
				} else {
	//				if (DEBUG) debugPrintException(wde);
					if (DEBUG) {
						debugPrintln("			(WORKAROUND: exception "+wde.getMessage()+" has been caught...");
						debugPrintln("			 -> return empty string \"\" instead)");
					}
					return EMPTY_STRING;
				}
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Returns the text of the web element even if it's hidden.
 *
 * @return The web element text
 */
public String getTextEvenIfHidden() {
	debugPrintEnteringMethod();
	if (isDisplayed()) {
		return getText();
	}
	return getAttribute("textContent");
}

/**
 * Return the wrapped {@link WebElement}.
 *
 * @return The wrapped web element as a {@link WebElement}.
 */
public WebElement getWebElement() {
	return this.webElement;
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public boolean isDisplayed() {
	return isDisplayed(true/*recovery*/);
}

/**
 * Perform the {@link WebElement#isDisplayed()} operation w/o recovery.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made. In the latter case, no exception is raised, but an
 *  <code>false</code> is returned.
 * </p><p>
 * If recovery is not allowed and an exception occurs, then it's still caught
 * but <code>false</code> is returned instead of retrying.
 *  </p>
 * @param recovery Tells whether try to recover is a {@link WebDriverException}
 * occurs
 * @see WebElement#isDisplayed()
 */
public boolean isDisplayed(final boolean recovery) {
	debugPrintEnteringMethod("recovery", recovery);
//	if (DEBUG) debugPrintln("			(getting displayed state for "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				boolean state = this.webElement.isDisplayed();
				if (DEBUG) debugPrintln("			 ( -> "+state+")");
				return state;
			}
			catch (NullPointerException npe) {
				if (DEBUG) {
					debugPrintln("			(WORKAROUND: exception "+npe.getMessage()+" has been caught...");
					debugPrintln("			 -> See Selenium bug https://github.com/SeleniumHQ/selenium/issues/9266 which won't be fixed in version 3...");
					debugPrintln("			 -> return false instead)");
				}
				return false;
			}
			catch (WebDriverException wde) {
				if (recovery) {
					try{
						catchWebDriverException(wde, "getting displayed state", count++, true);
					}
					catch (WebDriverException wde2) {
						if (DEBUG) {
							debugPrintln("			(WORKAROUND: exception "+wde2.getMessage()+" has been caught...");
							debugPrintln("			 -> return false instead)");
						}
						return false;
					}
				} else {
	//				if (DEBUG) debugPrintException(wde);
					if (DEBUG) {
						debugPrintln("			(WORKAROUND: exception "+wde.getMessage()+" has been caught...");
						debugPrintln("			 -> return false instead)");
					}
					return false;
				}
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame(/*ignoreError:*/!recovery);
		}
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public boolean isEnabled() {
	return isEnabled(true/*recovery*/);
}

/**
 * Perform the {@link WebElement#isEnabled()} operation.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made. In the latter case, no exception is raised, but a
 *  <code>false</code> is returned.
 * </p><p>
 * If recovery is not allowed and an exception occurs, then it's still caught
 * but <code>false</code> is returned instead of retrying.
 *  </p>
 * @param recovery Tells whether try to recover if a {@link WebDriverException}
 * occurs
 * @see WebElement#isEnabled()
 */
public boolean isEnabled(final boolean recovery) {
	debugPrintEnteringMethod("recovery", recovery);
//	if (DEBUG) debugPrintln("			(getting enabled state for "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				boolean state = this.webElement.isEnabled();
				if (DEBUG) debugPrintln("			 ( -> "+state+")");
				return state;
			}
			catch (WebDriverException wde) {
				if (recovery) {
					try{
						catchWebDriverException(wde, "getting enabled state", count++, true);
					}
					catch (@SuppressWarnings("unused") WebDriverException wde2) {
						if (DEBUG) debugPrintln("Workaround exceptions by simulating a false return to isEnabled() call!");
						return false;
					}
				} else {
					if (DEBUG) debugPrintException(wde);
					return false;
				}
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Tells whether the current element is in a frame or not.
 *
 * @return <code>true</code> if the element is in a frame, <code>false</code> otherwise.
 */
public boolean isInFrame() {
	return this.frame != null;
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public boolean isSelected() {
	debugPrintEnteringMethod();
//	if (DEBUG) debugPrintln("			(getting selected state for "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				boolean state = this.webElement.isSelected();
				if (DEBUG) debugPrintln("			 ( -> "+state+")");
				return state;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "getting selected state", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Make the web element visible.
 * <p>
 * This is a no-op if the current web element is already visible.
 * </p>
 * @return This element to allow callers to insert this method .
 */
public WebBrowserElement makeVisible() {
	return makeVisible(false);
}

/**
 * Make the web element visible.
 *
 * @param force Force the visibility, even if it's already visible. That makes the
 * mouse cursor move to the current web element.
 * @return This element to allow callers to insert this method .
 */
public WebBrowserElement makeVisible(final boolean force) {
	debugPrintEnteringMethod("force", force);
	if (force || !isDisplayed(false)) {
		moveToElement(true/*entirelyVisible*/);
	}
	return this;
}

/**
 * Simulate a mouse over by forcing a trigger of the javascript <code>mouseover</code>
 * event on the associated web element.
 * <p>
 * Note this method is a workaround of numerous issues we get with the mouse
 * over using Google Chrome and Internet Explorer and also with Firefox since
 * Selenium version 2.35.0...
 * </p>
 */
public void mouseOver() {
	debugPrintEnteringMethod();
	getJavascriptExecutor().executeScript(MOUSE_OVER_JAVASCRIPT, this.webElement);
}

/**
 * Move to the current web element.
 * <p>
 * This is a simple move, hence web element might not be entirely visible in
 * the browser window after this action.
 * </p><p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 * @see #moveToElement(boolean)
 */
public void moveToElement() {
	moveToElement(false);
}

/**
 * Move to the current web element.
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 * @param entirelyVisible Ensure that the entire web element will be visible in
 * the browser window
 */
public void moveToElement(final boolean entirelyVisible) {
	debugPrintEnteringMethod("entirelyVisible", entirelyVisible);

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				this.browser.moveToElement(this, entirelyVisible);
				pause(500); // It looks like it needs a small pause after the move to be actually taken into account
				if (DEBUG) debugPrintln("			 ( -> done.)");
				return;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "move to element", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/*
 * Recover the web element. When the current browser element has a
 * WebBrowserElement as parent, then recover it first.
 */
private boolean recover(final int n) {
	debugPrintln("		+ Recover "+this);

	// If there's a parent, then recover it first
	if (this.context instanceof WebBrowserElement) {
		final WebBrowserElement parentElement = (WebBrowserElement) this.context;
		if (!parentElement.isDisplayed(false) && !parentElement.recover(n)) {
			return false;
		}
	}

	// Select the frame again if necessary
	WebBrowserFrame browserFrame = this.browser.getCurrentFrame();
	if (this.frame != browserFrame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	// 	Try to find again the web element
	try {
		debugPrint("		  -> find element {"+this.locator+"}");
		WebElement recoveredElement = null;
		if (this.parentListSize <= 1) {

			// Single element expected
			if (this.context instanceof WebBrowserElement) {
				debugPrintln(" as single element in parent element...");
				try {
					recoveredElement = ((WebBrowserElement) this.context).webElement.findElement(this.locator);
				}
				catch (NoSuchElementException nsee) {
					if (n >= MAX_RECOVERY_ATTEMPTS) {
						if (this.browser.hasFrame()) {
							debugPrintln("Workaround: recovery cannot find the element again, try with any possible frame");
							WebBrowserElement elementInFrame = this.browser.findElementInFrames(this.locator);
							if (elementInFrame == null) {
								debugPrintln("	-> recovery cannot find the element even in other frame, give up...");
								throw nsee;
							}
							recoveredElement = elementInFrame.webElement;
							this.frame = this.browser.getCurrentFrame();
						} else {
							debugPrintln("	-> recovery cannot retrieve the element, give up...");
							throw nsee;
						}
					}
					debugPrintln("	-> #"+n+" recovery attempt cannot retrieve the element, try another time...");
				}
			} else {
				debugPrintln(" as single element in web driver...");
				try {
					recoveredElement = this.context.findElement(this.locator);
				}
				catch (NoSuchElementException nsee) {
					if (n >= MAX_RECOVERY_ATTEMPTS) {
						if (this.browser.hasFrame()) {
							WebBrowserElement elementInFrame = this.browser.findElementInFrames(this.locator);
							if (elementInFrame == null) {
								debugPrintln("	-> recovery cannot find the element even in other frame, give up...");
								throw nsee;
							}
							recoveredElement = elementInFrame.webElement;
							this.frame = this.browser.getCurrentFrame();
						} else {
							debugPrintln("	-> recovery cannot retrieve the element, give up...");
							throw nsee;
						}
					}
					debugPrintln("	-> #"+n+" recovery attempt cannot retrieve the element, try another time...");
				}
			}
		} else {
			// Multiple element expected
			List<WebElement> foundElements;
			if (this.context instanceof WebBrowserElement) {
				debugPrintln(" as multiple elements in parent element...");
				foundElements = ((WebBrowserElement) this.context).webElement.findElements(this.locator);
			} else {
				debugPrintln(" as multiple elements in web driver...");
				foundElements = this.context.findElements(this.locator);
			}

			// If no element was found, give up now
			final int listSize = foundElements.size();
			debugPrintln("		  -> found "+listSize+" elements:");
			if (listSize == 0) {
				debugPrintln("		  -> no element found => cannot recover, hence give up");
				return false;
			}

			// Check the element position
			int idx = 0;
			WebElement tempElement = null;
			boolean canRecover = true;
			for (WebElement foundElement: foundElements) {
				boolean elementIsDisplayed = false;
				try {
					// Check whether the element is displayed
					elementIsDisplayed = foundElement.isDisplayed();
				}
				catch (@SuppressWarnings("unused") WebDriverException wde) {
					// Skip any web driver exception by considering the faulty element as not displayed
				}
				catch (NullPointerException npe) {
					if (DEBUG) {
						debugPrintln("			WORKAROUND: exception "+npe.getMessage()+" has been caught...");
						debugPrintln("			 -> See Selenium bug https://github.com/SeleniumHQ/selenium/issues/9266 which won't be fixed in version 3...");
						debugPrintln("			 -> Ignore it!");
					}
				}
				if (elementIsDisplayed) {
					if (listSize == this.parentListSize && idx == this.parentListIndex) {
						debugPrintln("		  -> an element is visible at the same place int the list ("+idx+") => use it to recover.");
						recoveredElement = foundElement;
						break;
					}
					if (tempElement == null) {
						debugPrintln("		  -> an element is visible at the a different place in the list ("+idx+") => store it in case it will be the only one...");
						tempElement = foundElement;
					} else {
						debugPrintln("		  -> more than one element is visible at the a different place in the list ("+idx+") => if none is found at the same index, we'll try to recover with the first one...");
						canRecover = false;
					}
				} else {
					if (listSize == this.parentListSize && idx == this.parentListIndex) {
						debugPrintln("		  -> an element is hidden at the same place int the list ("+idx+") => store it in case it will be the only one...");
						tempElement = foundElement;
					} else {
						debugPrintln("		  -> an element is hidden at the a different place in the list ("+idx+") => it won't be stored...");
					}
				}
				idx++;
			}

			// If element position does not match exactly the expected one, try to use
			// the better found one, if any.
			if (recoveredElement == null) {
				if (canRecover) {
					if (tempElement == null) {
						debugPrintln("		  -> no visible element was found to recover!");
					} else if (n == MAX_RECOVERY_ATTEMPTS) {
						debugPrintln("		  -> last try, hence use possible element found.");
						recoveredElement = tempElement;
					}
				} else if (n == MAX_RECOVERY_ATTEMPTS) {
					debugPrintln("		  -> last try, hence use possible element found.");
					recoveredElement = tempElement;
				} else {
					debugPrintln("		  -> several visible elements were found to recover but not at the same index!");
				}
			}
		}

		// Give up if no element was found
		if (recoveredElement == null) {
			debugPrintln("WARNING: Cannot recover web element for "+this.locator+"!");
			return false;
		}

		// Store the recovered element
		this.webElement = recoveredElement;

		// Check element type
		if (this.webElement instanceof WebBrowserElement) {
			throw new ScenarioFailedError("Web element should not be a WebBrowserElement!");
		}
		return true;
	}
	finally {
		if (browserFrame != this.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Performs a right click action on the element.
 *
 */
public void rightClick() {

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		this.browser.actions.contextClick(this).perform();
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Scroll the page to the given element.
 * <p>
 * This is a no-op if the web element is already visible in the browser view.
 * </p><p>
 * Note that when element is actually scrolled to the view (ie. if it was not
 * visible in the view prior the call), then it's put at the top of the view
 * after the scroll occurred. Which means that if the page has a static
 * toolbar, it won't be accessible for {@link #click()} or {@link #sendKeys(CharSequence...)}
 * operations. When this use case occurs, you typically needs to use the
 * {@link WebPage#scrollToMakeElementVisible(WebBrowserElement)}
 * in order to make the element truely visible and usable for {@link #click()} or
 * {@link #sendKeys(CharSequence...)} operations.
 * </p>
 */
public void scrollIntoView() {
	debugPrintEnteringMethod();
	executeScript("scrollIntoView( true );");
}

/**
 * Scroll the page to the given element.
 * <p>
 * This is a no-op if the web element is already visible in the browser view.
 * </p><p>
 * Note that when element is actually scrolled to the view (ie. if it was not
 * visible in the view prior the call), we will use the behavior property set to
 * smooth scrolling option, and the block property set to nearest value.
 * </p>
 */
public void scrollSmoothIntoView() {
	debugPrintEnteringMethod();
	executeScript("scrollIntoView( {behavior: \"smooth\", block: \"nearest\"} )");
}

/**
 * Select or check the given element. This operation only applies to input
 * elements such as checkboxes, options in a select and radio buttons.
 * The element will only be selected/checked if it has not been
 * selected/checked already.
 *
 * @return The element that has been selected or checked.
 */
public WebBrowserElement select() {
	return alter(true /* select */);
}

private void sendKeys(final boolean recovery, final boolean password, final CharSequence... keysToSend) {
	if (DEBUG) {
		StringBuilder builder = new StringBuilder();
		String separator = "";
		for (CharSequence sequence: keysToSend) {
			builder.append(sequence.toString()).append(separator);
			separator = "', '";
		}
		String printedText;
		if (password) {
			printedText = "password '*****'";
		} else {
			printedText = "keys '"+builder.toString()+"'";
		}
		debugPrintln("			(sending "+printedText+" to "+this+")");
	}

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				this.webElement.sendKeys(keysToSend);
				if (DEBUG) debugPrintln("			 ( -> done.)");
				return;
			}
			catch (WebDriverException wde) {
				if (recovery) {
					catchWebDriverException(wde, "sending keys '"+keysToSend+")", count++, true);
				} else {
	//				if (DEBUG) debugPrintException(wde);
					if (DEBUG) {
						debugPrintln("			(WORKAROUND: exception "+wde.getMessage()+" has been caught...");
						debugPrintln("			 -> DO Nothing instead!)");
					}
					return;
				}
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Perform the {@link WebElement#sendKeys(CharSequence...)} operation.
 * <p>
 * If recovery is allowed, then catch any {@link WebDriverException} (except
 *  {@link InvalidSelectorException} and {@link UnreachableBrowserException})
 *  and retry the operation until success or {@link #MAX_RECOVERY_ATTEMPTS}
 *  attempts has been made.
 * </p><p>
 * If recovery is not allowed and an exception occurs, then it silently ignors
 * {@link StaleElementReferenceException} exception.
 *  </p>
 * @param recovery Tells whether try to recover is a {@link WebDriverException}
 * occurs
 * @see WebElement#sendKeys(CharSequence...)
 */
public void sendKeys(final boolean recovery, final CharSequence... keysToSend) {
	sendKeys(recovery, false/*password*/, keysToSend);
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public void sendKeys(final CharSequence... keysToSend) {
	sendKeys(true/*recovery*/, keysToSend);
}

/**
 * Set current element visibility to given value.
 *
 * @param visible Tells whether the current element should become visible or hidden
 */
public void setVisibility(final boolean visible) {
	String status = visible ? "visible" : "hidden";
	println("Set current element "+status);
	println("	-> current style="+getAttribute("style"));
	getJavascriptExecutor().executeScript("arguments[0].style.visibility=\""+(visible?"visible":"hidden")+"\";", this.webElement);
	println("	-> new style="+getAttribute("style"));
}

/**
 * Set the current element visible with the given width.
 *
 * @param width The element width when it becomes visible
 */
public void setVisible(final int width) {
	debugPrintEnteringMethod("width", width);
	String styleAttribute = getAttribute("style");
	debugPrintln("	-> current style="+styleAttribute);
	String newStyleAttribute = getNewVisibilityStyle(styleAttribute, width);
	debugPrintln("	-> new style="+newStyleAttribute);
	getJavascriptExecutor().executeScript("arguments[0].style=\""+newStyleAttribute+"\";", this.webElement);
	if (!isDisplayed(false)) {
		throw new ScenarioFailedError("Workaround to make current element "+this+" visible did NOT work.");
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Catch {@link WebDriverException} and retry the operation until success or
 * {@link #MAX_RECOVERY_ATTEMPTS} attempts has been made.
 * </p>
 */
@Override
public void submit() {
	debugPrintEnteringMethod();
//	if (DEBUG) debugPrintln("			(submitting on "+this+")");

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, false/*store*/);
	}

	try {
		int count = 0;
		while (true) {
			try {
				this.webElement.submit();
				if (DEBUG) debugPrintln("			 ( -> done.)");
				return;
			}
			catch (WebDriverException wde) {
				catchWebDriverException(wde, "submitting", count++, true);
			}
		}
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Resynchronize the current with web element displayed in the page.
 * <p>
 * This synchronization is usually handled automatically by the framework, but
 * in certain circumstances, object user might know that a refresh will be necessary
 * before getting some information from the current instance.
 * </p>
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
public WebElement synchronize() {
	recover(MAX_RECOVERY_ATTEMPTS);
	return this.webElement;
}

@Override
public String toString() {
	StringBuilder builder = new StringBuilder("Web element {");
	builder.append(getFullLocator());
	builder.append("} ");
	if (this.frame != null) {
		builder.append("<in frame: ").append(this.frame).append(">");
	}
	return builder.toString();
}

/**
 * Wait until have found the mandatory child element using the given relative locator and timeout.
 * <p>
 * Note that:
 * <ol>
 * <li>hidden element are not returned by this method.</li>
 * </ol>
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param timeout The time in seconds to wait before raising an error if the mandatory
 * element is not found in the current element HTML hierarchy.
 * @return The web element as {@link WebBrowserElement}. It cannot be <code>null</code>.
 * @throws WaitElementTimeoutError If no element is found before the given timeout.
 * @throws MultipleElementsFoundError If there are several found elements as only one is expected.
 */
public WebBrowserElement waitForMandatoryDisplayedChildElement(final By relativeLocator, final int timeout) throws WaitElementTimeoutError, MultipleElementsFoundError {
	return this.browser.waitForElement(this, relativeLocator, true/*fail*/, timeout, true/*displayed*/, true/*single*/);
}

/**
 * Wait until have found a potential child element using the given relative locator and timeout.
 * <p>
 * Note that returned element might be hidden.
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param timeout The time in seconds before giving up if the element is not found
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout
 * @throws MultipleElementsFoundError If there are several found elements although
 * only one was expected.
 */
public WebBrowserElement waitForPotentialChildElement(final By relativeLocator, final int timeout) throws MultipleElementsFoundError {
	return waitForPotentialChildElement(relativeLocator, timeout, /*displayed:*/false);
}

private WebBrowserElement waitForPotentialChildElement(final By relativeLocator, final int timeout, final boolean displayed) throws MultipleElementsFoundError {
	if (DEBUG) {
		debugPrintEnteringMethod("relativeLocator", getLocatorString(relativeLocator), "timeout", timeout, "displayed", displayed);
	}

	// Wait for all elements
	List<WebBrowserElement> foundElements = waitForPotentialChildrenElements(relativeLocator, timeout, displayed);
	if (foundElements == null) {
		return null;
	}
	int listSize = foundElements.size();
	if (listSize == 0) return null;
	if (!getParameterBooleanValue("performanceEnabled",false) && listSize > 1) {
		throw new MultipleElementsFoundError(foundElements);
	}

	// Return the found element
	return foundElements.get(0);
}

/**
 * Wait until have found potential children elements using the given relative locator and timeout.
 * <p>
 * Note that returned elements might be hidden.
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param timeout The time in seconds before giving up if the element is not found
 * @return A {@link List} of web element as {@link WebBrowserElement}. Might
 * be empty if no element was found before the timeout
 */
public List<WebBrowserElement> waitForPotentialChildrenElements(final By relativeLocator, final int timeout) {
	return waitForPotentialChildrenElements(relativeLocator, timeout, /*displayed:*/ false);
}

private List<WebBrowserElement> waitForPotentialChildrenElements(final By relativeLocator, final int timeout, final boolean displayed) {
	if (DEBUG) {
		debugPrintEnteringMethod("relativeLocator", getLocatorString(relativeLocator), "timeout", timeout, "displayed", displayed);
	}

	// Select the frame again if necessary
	if (this.frame != this.browser.frame) {
		this.browser.selectFrame(this.frame, /*store: */false);
	}

	try {
		return this.browser.waitForElements(this, relativeLocator, /*fail:*/false, timeout, displayed);
	}
	finally {
		if (this.frame != this.browser.frame) {
			this.browser.selectFrame();
		}
	}
}

/**
 * Wait until have found a potential displayed element using the given relative locator and timeout.
 * <p>
 * Note that hidden elements are ignored by this method.
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param timeout The time in seconds before giving up if the element is not found
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout
 * @throws MultipleElementsFoundError If there are several found elements although
 * only one was expected.
 */
public WebBrowserElement waitForPotentialDisplayedChildElement(final By relativeLocator, final int timeout) throws MultipleElementsFoundError {
	return waitForPotentialChildElement(relativeLocator, timeout, /*displayed:*/true);
}

/**
 * Wait until have found potential displayed elements using the given relative locator and timeout.
 * <p>
 * Note that hidden elements are ignored by this method.
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @param timeout The time in seconds before giving up if the element is not
 * found
 * @return A {@link List} of web element as {@link WebBrowserElement}. Might
 * be empty if no element was found before the timeout
 */
public List<WebBrowserElement> waitForPotentialDisplayedChildrenElements(final By relativeLocator, final int timeout) {
	return waitForPotentialChildrenElements(relativeLocator, timeout, /*displayed:*/ true);
}

/**
 * Wait shortly until have found one mandatory displayed child element of the given relative locators list.
 * <p>
 * Note that:
 * <li>it will fail if the element is not found before {@link #SHORT_TIMEOUT} seconds</li>
 * <li>hidden element will be ignored</li>
 * <ul>
 * </p>
 * @param locators The locators to find the element relatively to the current element.
 * @return The array of web elements as {@link WebBrowserElement}
 * @throws WaitElementTimeoutError if no element was found before the timeout
 *
 * @see WebBrowser#waitForFirstDisplayedElementInList(WebBrowserElement, By[], boolean, int)
 * to have more details on how the returned array is filled with found elements
 */
public WebBrowserElement[] waitShortlyForFirstMandatoryDisplayedChildElementInList(final By... locators) throws WaitElementTimeoutError {
	return this.browser.waitForFirstDisplayedElementInList(this, locators, /*fail:*/ true, SHORT_TIMEOUT);
}

/**
 * Wait shortly until have found the mandatory child element using the given relative locator.
 * <p>
 * Note that returned element might be hidden.
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @return The web element as {@link WebBrowserElement}. It cannot be <code>null</code>.
 * @throws WaitElementTimeoutError If no element is found before the given timeout.
 * @throws MultipleElementsFoundError If there are several found elements as only one is expected.
 */
public WebBrowserElement waitShortlyForMandatoryChildElement(final By relativeLocator) throws WaitElementTimeoutError, MultipleElementsFoundError {
	return this.browser.waitForElement(this, relativeLocator, /*fail:*/ true, SHORT_TIMEOUT, /*displayed:*/ false, /*single:*/ true);
}

/**
 * Wait shortly until have found mandatory children elements using the given relative locator.
 * <p>
 * Note that:
 * <ul>
 * <li>returned elements might be hidden</li>
 * <li>it will fail if no element is found before {@link Timeouts#SHORT_TIMEOUT} seconds</li>
 * </ul>
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @return A {@link List} of web element as {@link WebBrowserElement}. It cannot be empty.
 * @throws WaitElementTimeoutError If no element was found before the timeout
 */
public List<WebBrowserElement> waitShortlyForMandatoryChildrenElements(final By relativeLocator) throws WaitElementTimeoutError {
	return this.browser.waitForElements(this, relativeLocator, /*fail:*/true, SHORT_TIMEOUT, /*displayed:*/false);
}

/**
 * Wait shortly until have found mandatory displayed child element using the given relative locator.
 * <p>
 * Note that:
 * <ol>
 * <li>hidden element will be ignored</li>
 * <li>{@link Timeouts#SHORT_TIMEOUT} is used as default timeout before given
 * up the search operation if no element is found.</li>
 * </ol>
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @return The web element as {@link WebBrowserElement}. It cannot be <code>null</code>.
 * @throws WaitElementTimeoutError If no element is found before the default timeout.
 * @throws MultipleElementsFoundError If there are several found elements as only one is expected.
 */
public WebBrowserElement waitShortlyForMandatoryDisplayedChildElement(final By relativeLocator) throws WaitElementTimeoutError, MultipleElementsFoundError {
	return waitForMandatoryDisplayedChildElement(relativeLocator, SHORT_TIMEOUT);
}

/**
 * Wait shortly until have found mandatory displayed children elements using the given relative locator.
 * <p>
 * Note that:
 * <ul>
 * <li>hidden element will be ignored</li>
 * <li>it will fail if no element is found before {@link Timeouts#SHORT_TIMEOUT} seconds</li>
 * </ul>
 * </p>
 * @param relativeLocator Locator to find the element in the current element hierarchy.
 * @return A {@link List} of web element as {@link WebBrowserElement}. Cannot be empty
 * @throws WaitElementTimeoutError If no element was found before the timeout
 */
public List<WebBrowserElement> waitShortlyForMandatoryDisplayedChildrenElements(final By relativeLocator) throws WaitElementTimeoutError {
	return this.browser.waitForElements(this, relativeLocator, true/*fail*/, SHORT_TIMEOUT, /*displayed:*/true);
}

/**
 *  Wait while the current web element is displayed in the page.
 *
 *  @param seconds The timeout before giving up if the element is still displayed.
 *  @throws WaitElementTimeoutError If the element is still displayed after the
 *  given timeout has been reached.
 */
public boolean waitWhileDisplayed(final int seconds) {
	return waitWhileDisplayed(seconds, true/*fail*/);
}

/**
 *  Wait while the current web element is displayed in the page.
 *
 *  @param seconds The timeout before giving up if the element is still displayed.
 *  @param fail Tells whether to return <code>false</code> instead throwing
 *  a {@link WaitElementTimeoutError} when the timeout is reached.
 *  @return <code>true</code> if the element has disappeared before the timeout
 *  is reached. Otherwise return <code>false</code> only if it has been asked not
 *  to fail.
 *  @throws WaitElementTimeoutError If the element is still displayed after the
 *  given timeout has been reached and it has been asked to fail.
 */
public boolean waitWhileDisplayed(final int seconds, final boolean fail) {
	debugPrintEnteringMethod("seconds", seconds, "fail", fail);
	SpotDisplayedTimeout timeout = new SpotDisplayedTimeout(this, fail);
	return timeout.waitWhile(seconds);
}

/**
 *  Wait while the current web element is enabled in the page.
 *
 *  @param seconds The timeout before giving up if the element is still enabled.
 *  @throws WaitElementTimeoutError If the element is still enabled after the
 *  given timeout has been reached.
 */
public boolean waitWhileEnabled(final int seconds) {
	return waitWhileEnabled(seconds, true/*fail*/);
}

/**
 *  Wait while the current web element is enabled in the page.
 *
 *  @param seconds The timeout before giving up if the element is still enabled.
 *  @param fail Tells whether to return <code>false</code> instead throwing
 *  a {@link WaitElementTimeoutError} when the timeout is reached.
 *  @return <code>true</code> if the element has disappeared before the timeout
 *  is reached. Otherwise return <code>false</code> only if it has been asked not
 *  to fail.
 *  @throws WaitElementTimeoutError If the element is still enabled after the
 *  given timeout has been reached and it has been asked to fail.
 */
public boolean waitWhileEnabled(final int seconds, final boolean fail) {
	debugPrintEnteringMethod("seconds", seconds, "fail", fail);
	SpotEnabledTimeout timeout = new SpotEnabledTimeout(this, fail);
	return timeout.waitWhile(seconds);
}

/**
 *  Wait until the current web element has text visible.
 *
 *  @param seconds The timeout before giving up if the element still has text.
 *  @throws WaitElementTimeoutError If the element still has text displayed
 *  after the given timeout has been reached.
 */
public void waitWhileHasText(final int seconds) {
	waitWhileHasText(seconds, /*fail:*/true);
}

/**
 *  Wait until the current web element has text visible.
 *
 *  @param seconds The timeout before giving up if the element still has text.
 *  @param fail Tells whether to return <code>false</code> instead throwing
 *  a {@link WaitElementTimeoutError} when the timeout is reached.
 *  @return <code>true</code> if the element has no longer text displayed in the page
 *  before the timeout is reached. Otherwise return <code>false</code> only if it has
 *  been asked not to fail.
 *  @throws WaitElementTimeoutError If the element still has text displayed
 *  after the given timeout has been reached and it has been asked to fail.
 */
public boolean waitWhileHasText(final int seconds, final boolean fail) {
	debugPrintEnteringMethod("seconds", seconds, "fail", fail);
	SpotTextTimeout timeout = new SpotTextTimeout(this, fail);
	return timeout.waitWhile(seconds);
}

/**
 *  Wait until the current web element gets displayed in the page.
 *
 *  @param seconds The timeout before giving up if the element is still not displayed.
 *  @throws WaitElementTimeoutError If the element is still not displayed after the
 *  given timeout has been reached.
 */
public boolean waitWhileNotDisplayed(final int seconds) {
	return waitWhileNotDisplayed(seconds, /*fail:*/true);
}

/**
 *  Wait until the current web element gets displayed in the page.
 *
 *  @param seconds The timeout before giving up if the element is still not displayed.
 *  @param fail Tells whether to return <code>false</code> instead throwing
 *  a {@link WaitElementTimeoutError} when the timeout is reached.
 *  @return <code>true</code> if the element has disappeared before the timeout
 *  is reached. Otherwise return <code>false</code> only if it has been asked not
 *  to fail.
 *  @throws WaitElementTimeoutError If the element is still not displayed after the
 *  given timeout has been reached and it has been asked to fail.
 */
public boolean waitWhileNotDisplayed(final int seconds, final boolean fail) {
	debugPrintEnteringMethod("seconds", seconds, "fail", fail);
	SpotDisplayedTimeout timeout = new SpotDisplayedTimeout(this, fail);
	return timeout.waitUntil(seconds);
}

/**
 *  Wait until the current web element gets enabled in the page.
 *
 *  @param seconds The timeout before giving up if the element is still not enabled.
 *  @throws WaitElementTimeoutError If the element is still not enabled after the
 *  given timeout has been reached.
 */
public boolean waitWhileNotEnabled(final int seconds) {
	return waitWhileNotEnabled(seconds, /*fail:*/true);
}

/**
 *  Wait until the current web element gets enabled in the page.
 *
 *  @param seconds The timeout before giving up if the element is still not enabled.
 *  @param fail Tells whether to return <code>false</code> instead throwing
 *  a {@link WaitElementTimeoutError} when the timeout is reached.
 *  @return <code>true</code> if the element has disappeared before the timeout
 *  is reached. Otherwise return <code>false</code> only if it has been asked not
 *  to fail.
 *  @throws WaitElementTimeoutError If the element is still not enabled after the
 *  given timeout has been reached and it has been asked to fail.
 */
public boolean waitWhileNotEnabled(final int seconds, final boolean fail) {
	debugPrintEnteringMethod("seconds", seconds, "fail", fail);
	SpotEnabledTimeout timeout = new SpotEnabledTimeout(this, fail);
	return timeout.waitUntil(seconds);
}
}
