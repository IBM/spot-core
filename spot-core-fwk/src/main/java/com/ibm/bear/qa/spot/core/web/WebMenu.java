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

import static com.ibm.bear.qa.spot.core.performance.PerfManager.PERFORMANCE_ENABLED;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.List;

import org.openqa.selenium.*;

import com.ibm.bear.qa.spot.core.api.elements.SpotWindow;
import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;
import com.ibm.bear.qa.spot.core.factories.SpotWindowFactory;
import com.ibm.bear.qa.spot.core.performance.PerfManager;
import com.ibm.bear.qa.spot.core.performance.PerfManager.RegressionType;
import com.ibm.bear.qa.spot.core.scenario.errors.*;

/**
 * Class to manage menus.
 * <p>
 * Menus can be considered like window as they are also opened by clicking on a web element, usually
 * a menu item or drop-down button.
 * </p><p>
 * The open operation looks like a window one but have the peculiarity to check that items are
 * loaded before returning.
 * </p><p>
 * This class implements following API methods of {@link SpotWindow} interface:
 * <ul>
 * <li>{@link #cancelAll()}: Close all possible opened windows.</li>
 * <li>{@link #isOpenedBeforeTimeout(int)}: Check whether a window is opened or not during the given amount of seconds.</li>
 * </ul>
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #cancelAll()}: Close all possible opened windows.</li>
 * <li>{@link #clickItem(String)}: Click on item element found using the given label.</li>
 * <li>{@link #clickItem(String,By,Class,String...)}: Open the given page by clicking on the link element found by using the given</li>
 * <li>{@link #clickItem(String,Class,String...)}: Open the given page by clicking on the given menu item label.</li>
 * <li>{@link #clickItem(WebBrowserElement,Class)}: Open the given dialog by clicking on the given menu item element.</li>
 * <li>{@link #clickItemAndOpenWindow(String,By,Class,String...)}: Open the given menu by clicking on the given menu item.</li>
 * <li>{@link #clickItemAndOpenWindow(String,Class,String...)}: Open the given window by clicking on the given menu item.</li>
 * <li>{@link #getItemElement(String)}: Return the web element for the item matching the given label.</li>
 * <li>{@link #getItemElement(String,boolean)}: Return the web element for the item matching the given label.</li>
 * <li>{@link #getItemElements()}: Returns the list of item elements of the current menu.</li>
 * <li>{@link #getLabels(boolean)}: Returns the labels list of current menu items.</li>
 * <li>{@link #hasItem(String)}: Check whether an item with the given name exists in the menu or not.</li>
 * <li>{@link #open(WebBrowserElement)}: Open the window by clicking on the given web element.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #addPerfResult(RegressionType,String)}: Add a performance result if manager is activated.</li>
 * <li>{@link #clickOnOpenElement(WebBrowserElement)}: Click on open element in order to open the menu.</li>
 * <li>{@link #closeAction(boolean)}: Close the menu by sending Escape key to opening element.</li>
 * <li>{@link #compareItemToLabel(String,WebBrowserElement)}: Return true if the item element text equals to the specified item label</li>
 * <li>{@link #displayItemElement(String,WebBrowserElement)}: Display a non-visible element from the menu.</li>
 * <li>{@link #findElements(By,boolean)}: Find a list of web elements matching the given locator.</li>
 * <li>{@link #getCloseButtonLocator(boolean)}: There's no action to close a menu, it closes alone.</li>
 * <li>{@link #getItemElement(String,int,boolean,boolean)}: Return the web element for the item matching the given label.</li>
 * <li>{@link #getItemElementsLocator()}: Returns the locator of the menu item elements.</li>
 * <li>{@link #getItemLocator(String)}: Returns the locator for the given item.</li>
 * <li>{@link #getMenuElement(int)}: Return the menu element.</li>
 * <li>{@link #isMenuItemDisplayed(String)}: Returns whether the given menu item is displayed or not.</li>
 * <li>{@link #setPerfManagerRegressionType(RegressionType,boolean)}: Set regression type on performances manager.</li>
 * <li>{@link #waitForItemElement(String,boolean,int)}: Wait for the given item the given timeout.</li>
 * <li>{@link #waitForLoadingEnd()}: Wait until the menu is loaded.</li>
 * <li>{@link #waitForMandatoryElements(By,int,boolean)}: Wait until have found some elements (ie. at least one) web elements using the given locator.</li>
 * </ul>
 * </p>
 */
public class WebMenu extends SpotAbstractWindow {

	final private boolean useRightClick;
	final private boolean useFrame;

/**
 * Create a menu element in the given parent using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that menu does not use right click
 * and no frame either.
 * </p>
 * @param parent The element wrapper in which the menu element is located
 * @param locator The locator to find the wrapped web element
 */
public WebMenu(final WebElementWrapper parent, final By locator) {
	super(parent, locator);
	this.useRightClick = false;
	this.useFrame = false;
}

/**
 * Create a menu element in the given page using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that menu does not use right click
 * and no frame either.
 * </p>
 * @param page The page in which the menu element is located
 * @param locator The locator to find the wrapped web element
 */
public WebMenu(final WebPage page, final By locator) {
	this(page, locator, false);
}

/**
 * Create a menu element in the given page using the wrapped web element
 * found by the given locator and using right click if specified.
 * <p>
 * When using this constructor, it's assumed that menu does not use any frame.
 * </p>
 * @param page The page in which the menu element is located
 * @param locator The locator to find the wrapped web element
 * @param useRightClick Tells whether the menu should use right click to select items or not
 */
public WebMenu(final WebPage page, final By locator, final boolean useRightClick) {
	this(page, locator, useRightClick, false);
}

/**
 * Create a menu element in the given page using the wrapped web element
 * found by the given locator and using right click and frame if specified.
 *
 * @param page The page in which the menu element is located
 * @param locator The locator to find the wrapped web element
 * @param useRightClick Tells whether the menu should use right click to select items or not
 * @param useFrame Tells whether the menu should use a frame or not
 */
public WebMenu(final WebPage page, final By locator, final boolean useRightClick, final boolean useFrame) {
	super(page, locator);
	this.useRightClick = useRightClick;
	this.useFrame = useFrame;
}

/**
 * Create a menu element in the given page using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p>
 * @param page The page in which the menu element is located
 * @param wbElement The wrapped web element
 */
public WebMenu(final WebPage page, final WebBrowserElement wbElement) {
	super(page, wbElement);
	this.useRightClick = false;
	this.useFrame = false;
}

/**
 * Add a performance result if manager is activated.
 * <p>
 * Note that the result is added as a regression type to server.
 * This is a no-op if the performances are not managed during the scenario
 * execution.
 * </p>
 * @param regressionType Regression to apply
 * @param pageTitle The page title
 * @throws ScenarioFailedError If performances are <b>not enabled</b> during
 * scenario execution. Hence, callers have to check whether the performances are
 * enabled before calling this method using {@link PerfManager#PERFORMANCE_ENABLED}.
 */
protected void addPerfResult(final RegressionType regressionType, final String pageTitle) throws ScenarioFailedError {

	// Check that performances are enabled
	if (!PERFORMANCE_ENABLED) {
		throw new ScenarioFailedError("Performances are not enabled for the scenario execution. Use -DperformanceEnabled=true to avoid this failure.");
	}

	// Experimental Client loading
	PerfManager perfManager = this.browser.getPerfManager();
	perfManager.loadClient();

	// Set regression type
	if (regressionType != null) {
		setPerfManagerRegressionType(regressionType,false);
	}

	// Post final performance result
	perfManager.addPerfResult(pageTitle, "CLM Dropdown URL");
}

@Override
public void cancelAll() {
	throw new ScenarioMissingImplementationError(whoAmI());
}

/**
 * Click on item element found using the given label.
 *
 * @param item The item text to click on
 */
public void clickItem(final String item) {
	debugPrintEnteringMethod("item", item);

	// Get item element
	WebBrowserElement itemElement = getItemElement(item);

	// Check that the
	itemElement.click();
}

/**
 * Open the given page by clicking on the link element found by using the given
 * relative xpath from the given menu item element found by using the item label.
 *
 * @param itemLabel The item label to click on
 * @param linkBy The relative path from the found item web element to the
 * web element on which it's necessary to click to have the real action.
 * @param pageClass The class of the page to be opened by the item click
 * @param data Additional data to store in the opened page
 * @return The opened page
 */
public <P extends WebPage> P clickItem(final String itemLabel, final By linkBy, final Class<P> pageClass, final String... data) {
	WebBrowserElement itemElement = getItemElement(itemLabel);
	if (linkBy != null) {
		itemElement = itemElement.findElement(linkBy);
	}
	return getPage().openPageUsingLink(itemElement, pageClass, data);
}

/**
 * Open the given page by clicking on the given menu item label.
 *
 * @param itemLabel The item label to click on
 * @param pageClass The class of the page to be opened by the item click
 * @param data Additional data to store in the opened page
 * @return The opened page
 */
public <P extends WebPage> P clickItem(final String itemLabel, final Class<P> pageClass, final String... data) {
	return clickItem(itemLabel, null/*linkPath*/, pageClass, data);
}

/**
 * Open the given dialog by clicking on the given menu item element.
 *
 * @param itemElement The item element to click on
 * @param dialogClass The class of the dialog to be opened by the item click
 * @return The opened dialog
 */
public <D extends SpotAbstractDialog> D clickItem(final WebBrowserElement itemElement, final Class<D> dialogClass) {
	try {
		D dialog = SpotWindowFactory.createInstance(getPage(), dialogClass);
		dialog.open(itemElement);
		return dialog;
	}
	catch (Exception ex) {
		throw new ScenarioFailedError(ex);
	}
}

/**
 * Open the given menu by clicking on the given menu item.
 *
 * @param item The item label to click on
 * @param windowBy The mechanism to find the window when opened
 * @param windowClass The class of the window which will be opened when
 * clicking on the item
 * @param data Data used by the opened window
 * @return The opened window as an instance of the given class
 */
public <W extends SpotAbstractWindow> W clickItemAndOpenWindow(final String item, final By windowBy, final Class<W> windowClass, final String... data) {
	debugPrintEnteringMethod("item", item, "windowBy", windowBy, "windowClass", getClassSimpleName(windowClass), "data", getTextFromList(data));
	WebBrowserElement itemElement = getItemElement(item);
	try {
		W window = SpotWindowFactory.createInstance(getPage(), windowBy, windowClass, data);
		window.open(itemElement);
		return window;
	}
	catch (Exception ex) {
		throw new ScenarioFailedError(ex);
	}
}

/**
 * Open the given window by clicking on the given menu item.
 *
 * @param item The item label to click on
 * @param windowClass The class of the window which will be opened when
 * clicking on the item
 * @param data Data used by the opened window
 * @return The opened window as an instance of the given class
 */
public <W extends SpotAbstractWindow> W clickItemAndOpenWindow(final String item, final Class<W> windowClass, final String... data) {
	return clickItemAndOpenWindow(item, null, windowClass, data);
}

/**
 * Click on open element in order to open the menu.
 * <p>
 * Default is to click or right-click on the given element, depending on the menu
 * click type. This action is protected against fixed navbar which can hide the
 * given open element.
 * </p>
 * @param openElement The element on which to click to open the menu.
 * If <code>null</code>, then do nothing as the dialog is supposed to be already
 * opened.
 */
protected void clickOnOpenElement(final WebBrowserElement openElement) {
	scrollIfNecessary(openElement);
	if (this.useRightClick) {
		openElement.rightClick();
	} else {
		openElement.click();
	}
}

/**
 * Close the menu by sending Escape key to opening element.
 */
@Override
protected void closeAction(final boolean cancel) {
	this.openingElement.sendKeys(Keys.ESCAPE);
}

/**
 * Return true if the item element text equals to the specified item label
 *
 * @param itemLabel The item label
 * @param itemElement The item element
 * @return true if the item element text equals to the specified item label
 */
protected boolean compareItemToLabel(final String itemLabel, final WebBrowserElement itemElement) {
	return itemElement.getText().equals(itemLabel);
}

/**
 * Display a non-visible element from the menu.
 * <p>
 * Subclass needs to override this method with typical action in order to avoid
 * to get the default {@link ScenarioFailedError}.
 * </p>
 * @param itemLabel The item label
 * @param itemElement The web element of the menu item
 * @throws ScenarioFailedError If there's no way to make the item visible.
 */
protected void displayItemElement(final String itemLabel, final WebBrowserElement itemElement) throws ScenarioFailedError {
	debugPrintEnteringMethod("itemLabel", itemLabel, "itemElement", itemElement);

	// Check that menu is still opened
	WebBrowserElement menuElement = getMenuElement(0);

	// Loop until the web element is really found and displayed
	if (menuElement == null || !menuElement.isDisplayed()) {
		if (this.openingElement == null) {
			throw new ScenarioFailedError("Cannot access to item '"+itemLabel+"' as the menu was never opened!");
		}
		debugPrintln("		->  Menu was initially opened but vanished, hence try to reopen it...");
		int count = 0;
		while (menuElement == null || !menuElement.isDisplayed()) {
			if (count++ > 2) {
				throw new WaitElementTimeoutError("Menu was initially opened but vanished and it was not possible to reopened it!");
			}
			clickOnOpenElement(this.openingElement);
			menuElement = getMenuElement(1);
		}
	}

	// Check that now the item is visible again
	if (!itemElement.isDisplayed()) {
		throw new ScenarioFailedError("Menu item '"+itemLabel+"' exists but is not accessible.");
	}
}

/**
 * Find a list of web elements matching the given locator.
 * <p>
 * This way to find elements take into account the fact whether the current menu
 * is displayed in a frame or not.
 * </p>
 * @param elemLocator The locator to find elements in the page
 * @param recovery Tells whether the recovery has to be activated or not
 * @return The list of elements as a {@link List} of {@link WebElement}.
 */
protected List<WebElement> findElements(final By elemLocator, final boolean recovery) {
	return this.useFrame
		? this.browser.findElements(elemLocator, recovery)
		: this.element.findElements(elemLocator, recovery);
}

/**
 * There's no action to close a menu, it closes alone.
 */
@Override
protected By getCloseButtonLocator(final boolean validate) {
	return null;
}

/**
 * Return the web element for the item matching the given label.
 * <p>
 * Note that the returned element has to be visible otherwise this method will
 * raise a {@link WaitElementTimeoutError}.
 * </p><p>
 * If the element is not found after the timeout has expired, then a page refresh
 * is done and the menu opened again to make another try. If the element is still
 * not found after this workaround, then a {@link WaitElementTimeoutError} is
 * raised.
 * </p>
 * @param itemLabel The label of the item to click on
 * @return The corresponding item element as {@link WebBrowserElement}
 * @throws WaitElementTimeoutError If the item element is still not found after
 * having try to workaround
 */
public WebBrowserElement getItemElement(final String itemLabel) throws WaitElementTimeoutError {
	debugPrintEnteringMethod("itemLabel", itemLabel);
	return getItemElement(itemLabel, openTimeout(), true/*displayed*/, true/*workaround*/);
}

/**
 * Return the web element for the item matching the given label.
 * <p>
 * If the element is not found after the timeout has expired, then a page refresh
 * is done and the menu opened again to make another try. If the element is still
 * not found after this workaround, then a {@link WaitElementTimeoutError} is
 * raised.
 * </p>
 * @param itemLabel The label of the item to click on
 * @param displayed When <code>true</code> then only displayed item element can be returned.
 * When <code>false</code> then the returned item element can be either displayed or hidden.
 * @return The corresponding item element as {@link WebBrowserElement}
 * @throws WaitElementTimeoutError If the item element is still not found after
 * having try to workaround
 */
public WebBrowserElement getItemElement(final String itemLabel, final boolean displayed) throws WaitElementTimeoutError {
	return getItemElement(itemLabel, openTimeout(), displayed, true/*canWorkaround*/);
}

/**
 * Return the web element for the item matching the given label.
 *
 * @param itemLabel The label of the item to click on
 * @param timeout Time in seconds to wait for the item element to be found
 * @param displayed When <code>true</code> then only displayed item element can be returned.
 * When <code>false</code> then the returned item element can be either displayed or hidden.
 * @param canWorkaround Tells whether a workaround is accepted if the item
 * element is not found after the timeout
 * @return The corresponding item element in the menu as a {@link WebBrowserElement}
 * or <code>null</code> if not found after the given timeout and no workaround
 * is allowed
 * @throws ScenarioFailedError If the item element is found but not visible.
 * @throws WaitElementTimeoutError If the workaround is allowed and the item
 * element is still not found after it.
 */
protected WebBrowserElement getItemElement(final String itemLabel, final int timeout, final boolean displayed, final boolean canWorkaround) throws WaitElementTimeoutError {
	debugPrintEnteringMethod("itemLabel", itemLabel, "timeout", timeout, "displayed", displayed, "canWorkaround", canWorkaround);

	// Wait for item element
	WebBrowserElement itemElement = waitForItemElement(itemLabel, displayed, timeout);

	// If element is not found
	if (itemElement == null) {

		// If workaround is allowed then try to refresh the page
		if (canWorkaround) {

			// Print workaround info
			debugPrintln("WARNING: Cannot find the '"+itemLabel+"' item in '"+this+"' menu!");
			debugPrintStackTrace(2);

			// Check which kind of workaround can be applied
			if (isOpened()) {
				debugPrintln("Cause: Menu is opened but item is missing, so that might be due to menu displayed at window bottom...");
				debugPrintln("Workaround: Scroll the window until the item appears...");
				int menuSize = getItemElements().size();
				while (itemElement == null) {
					getPage().scrollUp();
					if (menuSize == getItemElements().size()) {
						throw new WaitElementTimeoutError("Cannot find the '"+itemLabel+"' item in '"+this+"' menu.");
					}
					itemElement = waitForItemElement(itemLabel, displayed, timeout);
				}
				debugPrintln("	=> item has been finally found after having scroll-up the page :-)");
			} else {
				this.browser.takeScreenshotWarning("WebMenu_getItemElement");
				debugPrintln("Cause: Menu element was not found...");
				debugPrintln("Workaround: Try to reopen it...");

				// Re-open the menu
				open(this.openingElement);

				// Wait for the item again
				itemElement = waitForItemElement(itemLabel, displayed, timeout);
			}
		}

		// Raise an error if the element was still not found
		if (itemElement == null) {
			throw new WaitElementTimeoutError("Cannot find the '"+itemLabel+"' item in '"+this+"' menu.");
		}
	}

	// If the element was found but not visible, try to make it visible
	if (!itemElement.isDisplayed()) {
		displayItemElement(itemLabel, itemElement);
	}

	// Return the found item
	return itemElement;
}

/**
 * Returns the list of item elements of the current menu.
 *
 * @noreference Internal public method. It should not be used outside the framework.
 * @return A list of {@link WebElement}s representing the menu items
 */
public List<WebBrowserElement> getItemElements() {
	debugPrintEnteringMethod();
//	return waitForMandatoryElements(getItemElementsLocator(), shortTimeout(), /*displayed:*/ true);
	WebBrowserElement parentElement = this.useFrame ? null : this.element;
	return this.browser.waitForElements(parentElement, 	getItemElementsLocator(), /*fail:*/false, shortTimeout(), /*displayed:*/ true);
}

/**
 * Returns the locator of the menu item elements.
 * <p>
 * By default the item elements are identified as <code>tr</code> tag name web
 * having <code>class</code> attribute containing with <code>'dijitMenuItem'</code>
 * and not being read only.
 * </p><p>
 * Subclasses might want to override this method in order to provide a more specific
 * locator.
 * </p>
 * @return The locator as a {@link By}
 */
protected By getItemElementsLocator() {
	return By.xpath(".//tr[contains(@class,'dijitMenuItem') and not(contains(@class,'dijitReadOnly'))]");
}

/**
 * Returns the locator for the given item.
 *
 * @param itemLabel The item label
 * @return The item locator as a {@link By}.
 */
protected By getItemLocator(final String itemLabel) {
	debugPrintEnteringMethod("itemLabel", itemLabel);
	char firstChar = itemLabel.charAt(0);
	if (Character.isLetter(firstChar) || Character.isDigit(firstChar)) {
		char stringQuote = itemLabel.indexOf('\'') < 0 ? '\'' : '"';
		StringBuilder xpathBuilder = new StringBuilder(".//*[(contains(@dojoattachpoint,'containerNode') or contains(@data-dojo-attach-point,'containerNode')) and normalize-space(text())=")
			.append(stringQuote)
			.append(itemLabel)
			.append(stringQuote)
			.append(']');
		return By.xpath(xpathBuilder.toString());
	}
	return null;
}

/**
 * Returns the labels list of current menu items.
 *
 * @param close Tells whether to close the menu after having provided the list
 * @return A item labels list
 */
public List<String> getLabels(final boolean close) {
	List<WebBrowserElement> elements  = getItemElements();
	List<String> strings = toStrings(elements);
	if (close) {
		close();
	}
	return strings;
}

/**
 * Return the menu element.
 *
 * @param seconds The seconds to wait the element for
 * @return The menu element found using stored locator
 */
protected WebBrowserElement getMenuElement(final int seconds) {
	return this.browser.waitForElement(getParentElement(), this.locator, false/*do not fail*/, seconds, true/*displayed*/, true/*single*/);
}

/**
 * Check whether an item with the given name exists in the menu or not.
 *
 * @param menuItem the name of the item to check the existence of.
 * @return <code>true</false> if the item exists, <code>false</code> otherwise.
 */
public boolean hasItem(final String menuItem) {
	return waitForItemElement(menuItem, true /*displayed*/, 1/*second*/) != null;
}

/**
 * Returns whether the given menu item is displayed or not.
 *
 * @param menuItem name of the menu item.
 * @return <code>true</code> if the item is displayed, <code>false</code> otherwise.
 */
protected boolean isMenuItemDisplayed(final String menuItem) {
	// Get all menu options as string
	List<String> items = getLabels(false);

	// Iterate over the items
	for (String item : items) {
		// If target item found return true
		if (item.equals(menuItem)) {
			return true;
		}
	}

	// Return false
	return false;
}

@Override
public boolean isOpenedBeforeTimeout(final int seconds) {
	if (DEBUG) debugPrintln("		+ Check whether the menu is opened or not");
	return getMenuElement(seconds) != null;
}

/**
 * {@inheritDoc}
 * <p>
 * Open the menu found with the given search mechanism and return the
 * corresponding web element.
 * </p><p>
 * Note that the menu is opened by clicking on a link element found using
 * the given search mechanism.
 * </p><p>
 * When possible, it also waits for all items to be loaded before returning.
 * </p>
 */
@Override
public WebBrowserElement open(final WebBrowserElement webElement) {
	if (DEBUG) debugPrintln("		+ Open menu "+this.locator+" by clicking on "+webElement);

	// Store the open element
	this.openingElement = webElement;

	// Get popup menu web element
	WebBrowserElement menuElement = getMenuElement(0);
	if (menuElement != null) {
		if (DEBUG) debugPrintln("		  -> found an already opened menu..."+menuElement);
	}

	// Loop until the web element is really found and displayed
	int count = 0;
	while (menuElement == null || !menuElement.isDisplayed()) {
		if (count++ > 10) {
			throw new WaitElementTimeoutError("Menu was never displayed.");
		}
		clickOnOpenElement(webElement);
		menuElement = getMenuElement(1);
	}

	// Store the menu web element
	this.element = menuElement;

	// Set and select frame if necessary
	if (this.useFrame) {
		setFrame(new WebElementFrame(this.browser, menuElement));
		selectFrame();
	}

	// Wait for the end of the load of the menu items
	waitForLoadingEnd();

	// Add performance result
	if (PERFORMANCE_ENABLED) {
		addPerfResult(RegressionType.Server, "CLM Dropdown");
	}

	// Return the menu element
	return this.element;
}

private void setFrame(final WebElementFrame frame) {
	if (this.frames[2] != null) {
		throw new ScenarioImplementationError("Cannot set frame although one is already selected.");
	}
	this.frames = new WebBrowserFrame[3];
	this.frames[0] = this.browser.getCurrentFrame();
	this.frames[1] = frame;
}

/**
 * Set regression type on performances manager.
 * <p>
 * This method sets the default regression type to provided regressionType.
 * </p>
 * @param regressionType : Regression type to apply.
 * @param override : True will override and lock the regression type,
 * while false will only change the regression type if it is not locked.
 * @throws ScenarioFailedError If performances are <b>not enabled</b> during
 * scenario execution. Hence, callers have to check whether the performances are
 * enabled before calling this method using {@link PerfManager#PERFORMANCE_ENABLED}.
 */
protected void setPerfManagerRegressionType(final RegressionType regressionType, final boolean override) throws ScenarioFailedError {
	if (PERFORMANCE_ENABLED) {
		this.browser.perfManager.setRegressionType(regressionType, override);
	} else {
		throw new ScenarioFailedError("Performances are not enabled for the scenario execution. Use -DperformanceEnabled=true to avoid this failure.");
	}
}

/**
 * Wait until have found some elements (ie. at least one) web elements using the given locator.
 *
 * @param timeout The time to wait before giving up the research
 */
protected List<WebBrowserElement> waitForMandatoryElements(final By elemLocator, final int timeout, final boolean displayed) {
	debugPrintEnteringMethod("elemLocator", elemLocator, "timeout", timeout, "displayed", displayed);
	WebBrowserElement parentElement = this.useFrame ? null : this.element;
	return this.browser.waitForElements(parentElement, elemLocator, /*fail:*/true, timeout, displayed);
}

/**
 * Wait for the given item the given timeout.
 *
 * @param itemLabel The item label
 * @param seconds The amount of seconds to wait before given up.
 * @return The item element as a {@link WebBrowserElement} or <code>null</code>
 * if not found before the given timeout.
 */
protected WebBrowserElement waitForItemElement(final String itemLabel, final boolean displayed, final int seconds) {
	debugPrintEnteringMethod("itemLabel", itemLabel, "displayed", displayed, "seconds", seconds);

	// Get item locator
	By itemLocator = getItemLocator(itemLabel);
	if (itemLocator == null) {
		// If menu does not use xpath to find them, then use the items list instead
		List<WebBrowserElement> itemElements = getItemElements();
		for (WebBrowserElement itemElement: itemElements) {
			if (compareItemToLabel(itemLabel, itemElement)) {
				return itemElement;
			}
		}
		return null;
	}

	// Get item element
	WebBrowserElement itemElement = findElement(itemLocator, displayed, false/*no recovery*/);

	// Try to workaround when item has not be found
	if (itemElement == null) {

		// First loop until openTimeout occurs
		long timeout = seconds * 1000 + System.currentTimeMillis();
		while (itemElement == null) {
			if (System.currentTimeMillis() > timeout) {
				break;
			}
			sleep(1);
			itemElement = findElement(itemLocator, displayed, false/*no recovery*/);
		}
	}

	// Return found element
	return itemElement;
}

/**
 * Wait until the menu is loaded.
 * <p>
 * So far, it only waits for the first item not to be 'Loading...'.
 * </p><p>
 * Initially we were also testing that the items number was stable, but that was
 * too time consuming. Then, we tried to test that last item become stable, but
 * that didn't work for menus which have several columns...
 * </p>
 */
protected void waitForLoadingEnd() {
	debugPrintEnteringMethod();

	// Get first item
	startTimeout(openTimeout(), "Menu "+this.element+" never finish to load.");
	List<WebBrowserElement> itemElements = getItemElements();
	int size = itemElements.size();
	if (size == 0) {
		// We should have at least one item, hence something wrong happened
		// Try to reopen the menu if the timeout has not been reached
		testTimeout();
		sleep(2);
		debugPrintln("Menu "+this.element+" had no item, try to reopen it...");
		if (this.useFrame) {
			switchToBrowserFrame();
		}
		open(this.openingElement);
		return;
	}

	// Wait until first item is no longer saying "Loading..."
	for (WebBrowserElement itemElement: itemElements) {
		String itemText = itemElement.getText(false/*recovery*/);
		if (itemText.startsWith("Loading")) {
			testTimeout();
			sleep(1);
			waitForLoadingEnd();
			return;
		}
	}

	// Check that menu items are all displayed
	// TODO Improve following commented algorithm which tests the last item
	// and wait to become stable. Unfortunately that was not possible to activate
	// it as it didn't work for multi-column menus...
//	WebBrowserElement lastItemElement = getLastItemElement();
//	String initialLastItemText = previousLastItemElement.getText();
//	while (!lastItemElement.getText().equals(initialLastItemText)) {
//		testTimeout();
//		sleep(1);
//	}
	/*
	do {
		debugPrintln("		  -> waiting for menu items size to stabilize");
		testTimeout();
		sleep(1);
		itemElements = this.element.waitForPotentialDisplayedChildrenElements(getItemElementsLocator(), shortTimeout());
	}
	while (size != itemElements.size());
	*/

	// Reset timeout
	resetTimeout();
}
}
