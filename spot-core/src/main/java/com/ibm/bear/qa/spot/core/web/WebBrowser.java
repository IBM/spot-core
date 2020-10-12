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

import static com.ibm.bear.qa.spot.core.performance.PerfManager.PERFORMANCE_ENABLED;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.ByUtils.fixLocator;
import static com.ibm.bear.qa.spot.core.utils.ByUtils.getLocatorString;
import static com.ibm.bear.qa.spot.core.utils.FileUtil.createDir;
import static com.ibm.bear.qa.spot.core.web.WebBrowserElement.MAX_RECOVERY_ATTEMPTS;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.ibm.bear.qa.spot.core.api.SpotUser;
import com.ibm.bear.qa.spot.core.browser.BrowserConstants;
import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.Timeouts;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.javascript.DrapAndDropSimulator;
import com.ibm.bear.qa.spot.core.javascript.DrapAndDropSimulator.Position;
import com.ibm.bear.qa.spot.core.performance.PerfManager;
import com.ibm.bear.qa.spot.core.scenario.ScenarioUtils;
import com.ibm.bear.qa.spot.core.scenario.errors.*;
import com.ibm.bear.qa.spot.core.utils.*;
import com.ibm.bear.qa.spot.core.utils.ByUtils.ComparisonPattern;

/**
 * Abstract class to handle information of browser used to run FVT Selenium tests.
 * <p>
 * Currently supported browsers are:
 * <ul>
 * <li>Firefox, versions 3.6 and 10</li>
 * <li>InternetExplorer, versions 7, 8 and 9</li>
 * <li>Google Chrome</li>
 * </ul>
 * </p><p>
 * By default, the browser used when running a scenario is Firefox 10. To change
 * it, use the following parameters:
 * <ul>
 * <li><code>"browserKind"</code>
 * <p>
 * Kind of browser to use during the scenario run, one of the following values:
 * <ul>
 * <li><b>1</b>: Firefox</li>
 * <li><b>2</b>: Internet Explorer</li>
 * <li><b>3</b>: Google Chrome</li>
 * </ul>
 * </p><p>
 * <b>Default value</b>: <b>1</b>
 * </p><p>
 * Usage:
 * <ul>
 * <li><code>browserKind=2</code> in the properties file</li>
 * <li><code>-DbrowserKind=2</code> in the VM Arguments field of the launch
 * configuration.</li>
 * </ul></p>
 * </li><li>
 * <p><code>"browserPath"</code>
 * <p>
 * Path for the browser executable on the machine where the scenario is run.
 * If not set, then it's assumed to be on the system path to be accessible by default.
 * </p><p>
 * Note that for Google chrome, this is not the path of Chrome executable, but
 * the path of the Chrome Driver instead.
 * </p><p>
 * Usage:
 * <ul>
 * <li><code>browserPath=C:/Firefox/v10/firefox.exe</code> in the properties
 * file</li>
 * <li><code>-DbrowserPath=C:/Firefox/v10/firefox.exe</code> in the VM
 * Arguments field of the launch configuration.</li>
 * </ul>
 * </p><p>
 * Note that even for Windows, the path has to use slash.
 * </p>
 * </li><li>
 * <p><code>"browserProfile"</code>
 * <p>
 * The path to the folder of the browser profile. It only works for Firefox and
 * Google Chrome browsers.
 * </p><p>
 * Usage:
 * <ul>
 * <li><code>browserProfile=C:/tmp/profiles/ff.v10/dvpt</code> in the properties
 * file</li>
 * <li><code>-DbrowserProfile=C:/tmp/profiles/ff.v10/dvpt</code> in the VM
 * Arguments field of the launch configuration.</li>
 * </ul>
 * </p><p>
 * Note that even for Windows, the path has to use slash.
 * </p>
 * </li><li>
 * <p><code>"newBrowserSessionPerUser"</code>
 * <p>
 * This is to specify whether or not to open a new browser session per each user.
 * </p><p>
 * Usage:
 * <ul>
 * <li><code>newBrowserSessionPerUser=true</code> in the properties file</li>
 * <li><code>-DnewBrowserSessionPerUser=true</code> in the VM
 * Arguments field of the launch configuration.</li>
 * </ul>
 * </p>
 * </li>
 * </ul>
 * </p><p>
 * This class is also responsible to access the Selenium WebDriver. This new
 * Selenium API deals with {@link WebElement} but due to web page script
 * execution a web element found at some point might become stale a few
 * seconds after. Hence, current class implements the {@link SearchContext} and
 * instantiates internal {@link WebBrowserElement} which has the ability
 * to recover itself when such failure occurs.
 * </p>
 */
public abstract class WebBrowser implements SearchContext, BrowserConstants {

	/*Enumerations */
	/**
	 * Possible state for clickable workaround
	 */
	enum ClickableWorkaroundState { None, Init, Up, Esc, Down, DoubleUp }

	/**
	 * Inner class to help to find elements in current browser page frames even if
	 * some of them are embedded.
	 */
	class FramesScanner {
		int[] currentIndexes = new int[100];
		int[] maxIndexes = new int[100];
		int depth, maxDepth;
		final TargetLocator targetLocator;

		FramesScanner() {
		    this.targetLocator = WebBrowser.this.driver.switchTo();
	    }

		/**
		 * Find the elements matching the given locator in the current browser page
		 * or in any of its visible frames, even if they are embedded.
		 *
		 * @param locator The elements locator
		 * @return The list of found element as a {@link List} of {@link WebBrowserElement}
		 * or <code>null</code> if none was found.
		 */
		List<WebBrowserElement> findElements(final By locator) {
			if (DEBUG) debugPrintln("		+ Find elements in frames");

			// Try to find the element in current frame
			int frameIndex = getCurrentFrame() == null ? -1 : getCurrentFrame().getIndex();
			if (DEBUG) debugPrintln("		  -> find element in current frame: "+frameIndex);
			selectFrame();
			List<WebElement> elements = WebBrowser.this.findElements(locator, false/*no recovery*/);
			if (elements.size() > 0) return WebBrowserElement.getList(elements);

			// Get frames info
			getFrames();

			// Scan discovered frames
			if (DEBUG) debugPrintln("		  -> scan frames to find elements");
			for (int level=0; level<this.maxDepth; level++) {
				if (DEBUG) debugPrintln("			+ level "+level);
				for (int index=0; index<this.maxIndexes[level]; index++) {
					if (DEBUG) debugPrintln("				* index "+index);
					selectParentFrame(level);
					this.targetLocator.frame(index);
					final WebIndexedFrame webFrame = new WebIndexedFrame(WebBrowser.this, index);
					elements = WebBrowser.this.findElements(locator, true/*displayed*/, webFrame, false/*no recovery*/);
					if (elements.size() > 0) {
						if (DEBUG) debugPrintln("				-> found "+elements.size()+" elements");
						setCurrentFrame(webFrame);
						return WebBrowserElement.getList(elements);
					}
				}
			}

			// No element was found in any frame, give up
			return null;
		}

		/**
		 * Get all frames displayed in the current browser page.
		 */
		void getFrames() {
			this.depth = 0;
			getFrames(0);
			this.maxDepth = this.depth +1;
			System.arraycopy(this.maxIndexes, 0, this.maxIndexes = new int[this.maxDepth], 0, this.maxDepth);
			this.currentIndexes = new int[this.maxDepth];
		}

		/**
		 * Get the frames displayed at a certain level of frames depth.
		 *
		 * @param level The parent frame depth level
		 */
		void getFrames(final int level) {
			selectParentFrame(level);
			List<WebElement> frames = WebBrowser.this.findElements(By.tagName("iframe"));
			if (this.depth < level) this.depth = level;
			this.maxIndexes[level] = frames.size();
			for (int index=0; index<this.maxIndexes[level]; index++) {
				this.currentIndexes[level] = index;
				getFrames(level+1);
			}
		}

		/**
		 * Select the frame at the given depth level.
		 *
		 * @param level The depth level
		 */
		void selectParentFrame(final int level) {
			this.targetLocator.defaultContent();
			for (int f=0; f<level; f++) {
				this.targetLocator.frame(this.currentIndexes[f]);
			}
		}
	}

	// Selenium specific
	public static final String JAVASCRIPT_ERROR_ALERT_PATTERN = "JavaScript Error: \"(e|h) is null\"";

	/*
	 * Fields
	 */
	// Pages cache
	final List<WebPage> pagesCache = new ArrayList<>();

    // Browser manager
    protected BrowsersManager manager;

	// Selenium driver
	protected WebDriver driver;
	final Actions actions;

	// Snapshots
	final private File[] screenshotsDir =  new File[3];

	// Specify whether or not to open a new browser session per each user
	private final boolean newSessionPerUser;

	// Page info
	String location;
	private String url; // TODO Check the url is necessary for browser
	String mainWindowHandle;

	// Frames
	WebBrowserFrame frame;
	// TODO Check that's safe to remove frame popup
//	private WebBrowserFrame framePopup;

	// Performances
	final PerfManager perfManager = PerfManager.createInstance(this); // Warning: Can be null!

	// Window info
	Dimension windowSize;

protected WebBrowser(final BrowsersManager manager) {

	// Init manager
	this.manager = manager;

	// Initialize the profile
	initProfile();

	// Init driver
	initDriver();
	this.actions = new Actions(this.driver);

	// Init screenshots directories
	initScreenshotsDir();

	// Init if a new browser session to be used per each user
	this.newSessionPerUser = getParameterBooleanValue(NEW_BROWSER_SESSION_PER_USER, true);

	// Initialize the browser window
	initWindow();
}

/**
 * Accept the alert with the given message.
 * <p>
 * A warning is displayed in the console when the accepted alert has not the
 * expected text.
 * </p>
 * @param message
 * @return <code>true</code> if the accepted has the correct message,
 * <code>false</code>  otherwise.
 */
public boolean acceptAlert(final String message) {
	if (DEBUG) debugPrintln("		+ Accept alert '"+message+"'.");

	// Get a handle to the open alert, prompt or confirmation
	Alert alert = null;
	long timeout = Timeouts.SHORT_TIMEOUT*500 + System.currentTimeMillis();
	int count = 0;
	while (alert == null) {
		if (System.currentTimeMillis() > timeout) {
			println("WARNING: No alert '"+message+"' was detected!");
			printStackTrace(1);
			println("	=> Workaround is to give up and continue the execution...");
			return false;
		}
		try {
			alert = this.driver.switchTo().alert();
		}
		catch (@SuppressWarnings("unused") NoAlertPresentException nape) {
			count++;
			if (DEBUG) debugPrintln("		  -> alert is not present, wait 500ms and retry ("+count+")...");
			pause(500);
		}
	}
	String alertText = alert.getText();
	if (DEBUG) debugPrintln("		  -> alert '"+alertText+"' has been detected...");

	// Acknowledge the alert (equivalent to clicking "OK")
	alert.accept();
	if (DEBUG) debugPrintln("		  -> alert has been accepted...");

	// Return OK if the alert was expected
	if (alertText.contains(message)) {
		return true;
	}

	// Warn if this was an unexpected alert
	println("WARNING: Unexpected alert '"+alertText+"' was accepted although '"+message+"' was expected. Scenario execution has been continued though...");
	return false;
}

/**
 * Accepts security certificates on Internet Explorer. Switches to a popup window if one exists and clicks
 * link to proceed to website. Does not switch back to main browser window if popup window is found.
 */
@Deprecated
public void acceptInternetExplorerCertificate() {

	// Switch to the popup window
	if (DEBUG) debugPrintln("		  -> main window handle "+this.mainWindowHandle);
	Iterator<String> iterator = getWindowHandles().iterator();
	while (iterator.hasNext()) {
		String handle = iterator.next();
		if (!handle.equals(this.mainWindowHandle)) {
			if (DEBUG) debugPrintln("		  -> switch to window handle "+handle);
			this.driver.switchTo().window(handle);
			break;
		}
	}
	try {
		if (this.driver.getCurrentUrl().contains("invalidcert.htm")) {
			if (DEBUG) debugPrintln("		+ Accept Internet Explorer certificate");
			this.driver.navigate().to("javascript:document.getElementById('overridelink').click()");
		}
	}
	catch (@SuppressWarnings("unused") Exception ex) {
		// skip
	}
}

/**
 * Accept private connection if any.
 * <p>
 * Default is to do nothing as we assume that browser web driver will be configured
 * to automatically accept those private connections.
 * </p>
 * @return <code>true</code> if a private connection was accepted,
 * <code>false</code> otherwise
 */
public boolean acceptPrivateConnection() {
	return false;
}

/**
 * Move back a single "item" in the browser's history.
 * <p>
 * <b>Warning</b>: This method does not modify the web pages cache, hence
 * it should be used with caution. It would be better to use {@link WebPage#goBack()}
 * method instead to keep browser content and pages cache synchronized.
 * </p><p>
 * Note that this will possible desynchronization will be fixed when pages cache
 * is managed by the browser itself instead of WebPage class...
 * </p>
 * @see org.openqa.selenium.WebDriver.Navigation#back()
 */
public WebPage back() {
	if (DEBUG) {
		debugPrintln("		+ Move back one step in browser history: ");
		debugPrintln("		  -> current state: "+this);
	}
	if (isGoogleChrome()) sleep(2);
	this.driver.navigate().back();
	sleep(2);
	purgeAlerts("While going back to previous page...");
	this.location = getCurrentUrl();
	debugPrintln("		  -> location after back: "+this.location);

	// Remove current page from cache
	int size = this.pagesCache.size();
	if (size < 2) {
		throw new ScenarioImplementationError("We should have found a page to go back.");
	}
	this.pagesCache.remove(size-1);

	// Return previous page
	return this.pagesCache.get(size-2);
}

/**
 * Return the first cached page matching the given url and connected with given user.
 *
 * @param pageUrl The page URL
 * @param user The user associated with the page
 * @return The found page or <code>null</code> if none was found.
 */
WebPage cachedPage(final String pageUrl, final User user) {
	int size = this.pagesCache.size();
	for (int i=size-1; i>=0; i--) {
		WebPage page = this.pagesCache.get(i);
		String pageLocation = page.getLocation();
		if (pageLocation.equals(pageUrl) && ((user == null && page.getUser() == null) || (user != null && user.equals(page.getUser())))) {
			return page;
		}
	}
	return null;
}

private int cacheIndex(final String pageUrl, final User user) {
	int size = this.pagesCache.size();
	for (int i=size-1; i>=0; i--) {
		WebPage page = this.pagesCache.get(i);
		String pageLocation = page.getLocation();
		if (pageLocation.equals(pageUrl) && ((user == null && page.getUser() == null) || (user != null && user.equals(page.getUser())))) {
			return i;
		}
	}
	return -1;
}

/**
 * Cache the given page.
 *
 * @param page The page to be cached
 */
void cachePage(final WebPage page) {
	int index = cacheIndex(page.getLocation(), page.getUser());
	if (index >= 0) {
		this.pagesCache.remove(index);
	}
	this.pagesCache.add(page);
	page.handle = this.mainWindowHandle;
}

/*
 * Catch WebDriverException until max allowed recovery tries is reached.
 *
 * TODO Change the exception parameter as StaleElementReferenceException
 * as this is the only caught exception now...
 */
void catchWebDriverException(final WebDriverException wde, final String title, final int count) {

	// Special treatment for alert exception
	if (purgeAlerts(title) > 0) {
		return;
	}

	// Give up right now if the exception is too serious
	if (!isSafari() && !(wde instanceof StaleElementReferenceException)) {
		debugPrintln("Fatal exception occured when "+title+"'... give up");
    	debugPrintException(wde);
		throw wde;
	}

	// If max retry has been reached, then really throw the WDE exception
	if (count > MAX_RECOVERY_ATTEMPTS) {
		debugPrintln("More than 10 exceptions occured when "+title+"'... give up");
    	debugPrintException(wde);
		throw wde;
	}

	// ScenarioWorkaround the WDE exception
	// Workaround
	debugPrint("Workaround exception when "+title+"': ");
	debugPrintException(wde);

	// First wait 1 second
	sleep(1);
}

/**
 * Set, unset or toggle the given element assuming this is a check-box.
 *
 * @param element The check-box in the current page
 * @param toggle Tells whether it should be toggled (0), set "on" (1) or set "off" (-1).
 * For all other values than 0, 1 or -1, 0 will be used.
 * @param validate Validate whether the check-box value is well set.
 * @return <code>true</code>If the check-box value has been changed,
 * <code>false</code> otherwise.
 */
public boolean check(final WebBrowserElement element, final int toggle, final boolean validate) {
	if (DEBUG) debugPrintln("		+ check box '"+element+"'");

	// Check the box
	boolean expected;
	boolean selected = element.isSelected();
	String selectedString = selected ? "selected" : "unselected";
	switch (toggle) {
		case 1: // set "on"
			if (selected) {
				if (DEBUG) debugPrintln("		  -> check-box was already 'on'");
				return false;
			}
			if (DEBUG) debugPrintln("		  -> set check-box to 'on'");
			element.click();
			expected = true;
			pause(250);
			break;
		case -1: // set "off"
			if (selected) {
				if (DEBUG) debugPrintln("		  -> set check-box to 'off'");
				element.click();
			} else {
				if (DEBUG) debugPrintln("		  -> check-box was already 'off'");
				return false;
			}
			expected = false;
			pause(250);
			break;
		default: // toggle
			if (DEBUG) debugPrintln("		  -> check-box was "+selectedString);
			element.click();
			pause(250);
			if (DEBUG) debugPrintln("		  -> check-box is now '"+(element.isSelected() ? "selected" : "unselected")+"'");
			expected = !selected;
			break;
	}

	// Validate the check-box state
	if (validate) {
		String expectedString = expected ? "selected" : "unselected";
		if (DEBUG) debugPrintln("		  -> validate that check-box is now "+expectedString+"...");
		int count = 0;
		while (element.isSelected() != expected) {
			if (count++ > 40) { // wait 10 seconds max
				if (DEBUG) debugPrintln("		  -> never turned "+expectedString+"!");
				throw new WaitElementTimeoutError("Check-box never turned "+expectedString+"!");
			}
			pause(250);
			if (count%4==0) 	element.click();
		}
	}

	// The check-box value has changed
	return true;
}

/**
 * Check whether there's a connection error or not.
 *
 * @throws BrowserConnectionError If there's a connection error with the current page.
 * TODO Check that the web element is the same for all browsers
 */
public void checkConnection() {
	WebBrowserElement errorContainerElement = findElement(By.xpath("//*[@id='errorPageContainer']"));
	if (errorContainerElement != null) {
		throw new BrowserConnectionError("Cannot access page "+getCurrentUrl());
	}
}

/**
 * Clear the browser cache.
 */
public void clearCache() {
	this.pagesCache.clear();
}

/**
 * Click on the given element and might validate whether it turns disabled after
 * the operation (e.g. for save buttons...)
 *
 * @param button The button to click on
 * @param timeout The time in seconds to wait before failing if the button never
 * turns enable.
 * @param validate Validate whether the button turns disabled after having been
 * clicked.
 * @throws SpotNotEnabledError When the button is not enabled
 */
public WebBrowserElement clickButton(final WebBrowserElement button, final int timeout, final boolean validate) throws SpotNotEnabledError {
	if (DEBUG) debugPrintln("		+ click on button '"+button+"'");

	// Timeout Loop until button is enabled
	int seconds = 0;
	while (!button.isEnabled()) {
		if (seconds++ > timeout) {
			if (DEBUG) debugPrintln("		  -> never turned enabled!");
			throw new SpotNotEnabledError("Button never turned enabled!");
		}
		sleep(1);
	}

	// Click on enabled button
	button.click();

	// Check, if requested, that button turn disabled after the click
	if (validate) {
		if (DEBUG) debugPrintln("		+ check button to turn disabled");
		int count = 0;
		boolean redoAction = false, click = true;
		while (button.isEnabled(false)) {

			// Check whether the button is still displayed. If not, then stop the validation and dump a warning message.
			if (!button.isDisplayed(false)) {
				println("WARNING: Button has disappeared while trying to validate that it was turning disabled!");
				printStackTrace(1);
				println("Workaround is to cancel the validation but that means the scenario should not have tried to validate when clicking on that button...");
				println();
				break;
			}

			// Try to redo the action every second
			if (redoAction) {
				if (DEBUG) debugPrintln("		  -> redo ("+(count/4)+") action by "+(click?"clicking again on the button":"hitting the Enter key"));
				if (click) {
					button.click();
				} else {
					button.sendKeys(Keys.ENTER);
				}
				click = !click;
			}

			// Give up after 10 seconds
			if (count++ > 40) {
				if (DEBUG) debugPrintln("		  -> never turned disabled!");
				throw new WaitElementTimeoutError("Button never turned disabled!");
			}

			// Pause before next loop
			redoAction = count%4==0;
			pause(250);
		}
	}

	// Return button
	return button;
}

/**
 * Close the browser which closes every associated window.
 */
public void close() {
	debugPrintEnteringMethod();

	// Save performance results and close the writers/logs
	if (this.perfManager != null) {
		this.perfManager.close();
	}

	// Shutdown current Selenium session
//	this.driver.quit();
	// Workaround for Firefox issue https://bugzilla.mozilla.org/show_bug.cgi?id=1027222
	// See also https://code.google.com/p/selenium/issues/detail?id=7506
	try {
		if (DEBUG) debugPrintln("		  -> closing current window (handle: "+this.driver.getWindowHandle()+")");
		this.driver.close();
		sleep(1);
	}
	catch (UnreachableBrowserException ube) {
		if (DEBUG) debugPrintln("		  -> Browser looks to have been already closed ("+ube.getMessage()+", hence do nothing...");
		return;
	}
	catch (WebDriverException wde) {
		if (wde.getCause().getClass().equals(ConnectException.class)) {
			if (DEBUG) debugPrintln("		  -> Browser looks to have been already closed ("+wde.getMessage()+", hence do nothing...");
		} else {
			if (DEBUG) debugPrintln("		  -> Catching driver exception when closing browser: "+wde.getMessage()+"...");
		}
		return;
	}

	// Shutdown all other existing sessions if any
	try {
		Set<String> windowHandles = getWindowHandles(false);
		int size = windowHandles.size();
		if (size > 0) {
			if (DEBUG) debugPrintln("		  -> "+size+" other window"+(size>1?"s were opened, close them all.":" was opened, close it."));
			for (String handle: windowHandles) {
				if (DEBUG) debugPrintln("			+ handle "+handle);
				this.driver.switchTo().window(handle);
				this.driver.close();
				sleep(1);
			}
		}
	}
	catch (WebDriverException wde) {
		if (!wde.getCause().getClass().equals(ConnectException.class)) {
			if (DEBUG) debugPrintln("		  -> Catching driver exception when closing browser: "+wde.getMessage()+"...");
		}
		return;
	}

	// TODO Clean page caches
//	this.page = null;
//	this.history.clear();
}

/**
 * Close other windows than the main one.
 */
public void closeOtherWindows() {
	debugPrintEnteringMethod();

	// Close all handles except main one
	for (String handle: this.driver.getWindowHandles()) {
		if (!handle.equals(this.mainWindowHandle)) {
			if (DEBUG) debugPrintln("		  -> switch to handle "+handle+" to close it...");
			this.driver.switchTo().window(handle);
			this.driver.close();
		}
	}

	// Switch back to main handle...
	if (this.getWindowHandles().size() != 1) {
		throw new ScenarioFailedError("Unexpected number of remaining handles: "+this.getWindowHandles().size()+" found although expecting only 1.");
	}
	this.driver.switchTo().window(this.mainWindowHandle);

	// Store the first page of the cache which has the same handle than the browser
	WebPage mainPage = null;
	for (WebPage page: this.pagesCache) {
		if (page.handle.equals(this.mainWindowHandle)) {
			mainPage = page;
			break;
		}
	}
	if (mainPage == null) {
		String messagePrefix = "Cannot find page in cache for main handle "+this.mainWindowHandle;
		if (this.pagesCache.size() == 0) {
			debugPrintln("WARNING: "+messagePrefix+" because pages cache is empty.");
			return;
		}
		throw new ScenarioFailedError(messagePrefix+" although pages cache was not empty.");
	}

	// Clean browsers manager and pages cache
	List<WebPage> removedPages = new ArrayList<>();
	for (WebPage page: this.pagesCache) {
		if (!page.handle.equals(this.mainWindowHandle)) {
			if (mainPage.user != null && page.user != null && !mainPage.user.equals(page.user)) {
				this.manager.remove(page.user);
			}
			removedPages.add(page);
		}
	}
	this.pagesCache.removeAll(removedPages);

	// Check cache pages
	removedPages = new ArrayList<>();
	if (this.pagesCache.size() > 1) {
		debugPrintln("Cache has extra pages. Clean them by comparing their URL to browser URL:");
		debugPrintln(" - browser url: "+getUrl());
		for (WebPage page: this.pagesCache) {
			if (!page.matchBrowserUrl()) {
				debugPrintln(" - page which does not match browser content:");
				debugPrintln("   + location: "+page.getLocation());
				debugPrintln("   + URL: "+page.getUrl());
				removedPages.add(page);
			}
		}
		this.pagesCache.removeAll(removedPages);
		debugPrintln(" => "+removedPages.size()+" pages have been removed from cache.");
		int size = this.pagesCache.size();
		debugPrintln(" => there's now "+size+" pages in the cache.");
		if (size > 1) {
			debugPrintln("	WARNING: the cache should only have one entry after the cleanup!");
			debugPrintln("		Here's pages cache content:");
			for (WebPage page: this.pagesCache) {
				debugPrintln("		 - page location: "+page.getLocation());
				debugPrintln("		   (URL: "+page.getUrl()+")");
			}
			debugPrintStackTrace(1);
		}
	} else {
		// Check current page
		if (!mainPage.matchBrowserUrl()) {
			String message = "Last page after having closed all others does not match browser URL!";
			String mainPageUrl = mainPage.getUrl();
			if (mainPageUrl.startsWith(mainPage.location)) {
				debugPrintln("WARNING: "+message);
				debugPrintln(" => Assuming that browser URL is the actual page location:");
				debugPrintln("      - page old location: "+mainPage.getLocation());
				debugPrintln("      - page new location: "+mainPageUrl);
				debugPrintln("      - browser url: "+getUrl());
				debugPrintln("        => replace page old location with new one...");
				mainPage.location = mainPageUrl;
			} else {
				println("WARNING: "+message);
				println(" - page location: "+mainPage.getLocation());
				println(" - page URL: "+mainPage.getUrl());
				println(" - browser url: "+getUrl());
				println("        => do nothing as there's no evidence that page or browser URL was the correct page location...");
			}
		}
	}
}

/**
 * Close all other windows than the one displaying the given page.
 *
 * @param page The page to be kept opened in current browser
 */
public void closeOtherWindowsIfNeeded(final WebPage page) {
	debugPrintEnteringMethod("page", page.getLocation());
	this.mainWindowHandle = page.handle;
	closeOtherWindows();
}

/**
 * Delete all current browser cookies.
 *
 * @see Options#deleteAllCookies()
 */
public void deleteAllCookies() {
	debugPrintEnteringMethod();
	this.driver.manage().deleteAllCookies();
}

/**
 * Delete the given cookie stored in the browser.
 *
 * @param cookieName The name of the cookie to be deleted.
 * @see Options#deleteCookieNamed(String)
 */
public void deleteCookieNamed(final String cookieName) {
	debugPrintEnteringMethod("cookieName", cookieName);
	this.driver.manage().deleteCookieNamed(cookieName);
}

/**
 * Double-click on the given element;
 *
 * @param element The web element to doubl-click on
 */
public void doubleClick(final WebBrowserElement element) {
	this.actions.doubleClick(element).build().perform();
}

/**
 * Drag given sourceElement and drop it to targetElement.
 *
 * @param sourceElement The web element to be dragged
 * @param targetElement The web element over which sourceElement has to be dropped
 * @see Actions#dragAndDrop(WebElement, WebElement)
 */
public void dragAndDrop(final WebBrowserElement sourceElement, final WebBrowserElement targetElement) {
	debugPrintln("		+ Drag " + sourceElement + " and drop over to " + targetElement);
	if (sourceElement.getFrame() != targetElement.getFrame()) { // == is intentional
		throw new ScenarioFailedError("Cannot move elements which belong to different frames.");
	}
	WebBrowserFrame browserFrame = this.frame;
	try {
		if (sourceElement.getFrame() != this.frame) {
			selectFrame(sourceElement.getFrame());
			sleep(1);
		}
//		println("Source coordinates: in view="+sourceElement.getCoordinates().inViewPort()+", on page="+sourceElement.getCoordinates().onPage());
//		println("Target coordinates: in view="+targetElement.getCoordinates().inViewPort()+", on page="+targetElement.getCoordinates().onPage());
//		Action move = new Actions(this.driver).clickAndHold(sourceElement).moveToElement(targetElement).build();
//		move.perform();
//		sleep(1);
//		Action release = new Actions(this.driver).release(targetElement).build();
//		release.perform();
//		sleep(1);
//		new Actions(this.driver).dragAndDrop(sourceElement, targetElement).perform();
		// Work with DnD sample
//		this.actions.dragAndDrop(sourceElement, targetElement).perform();
		// Workaround for Firefox: does not work
//		Actions builder = new Actions(this.driver);
//        builder.clickAndHold(sourceElement);
//        builder.moveToElement(targetElement, 5, 5);
//        builder.perform();
//        pause(250);
//        builder.release(targetElement);
//        builder.perform();
		// Another Workaround for Firefox: does not work
//		Actions builder1 = new Actions(this.driver);
//        builder1.moveToElement(sourceElement);
//        builder1.clickAndHold();
//        builder1.perform();
//		Actions builder2 = new Actions(this.driver);
//        builder2.moveToElement(targetElement);
//        builder2.perform();
//        pause(250);
//		Actions builder3 = new Actions(this.driver);
//        builder3.release();
//        builder3.perform();
        // Use javascript workaround
		dragAndDrop(sourceElement, targetElement, Position.Top_Left, Position.Top_Left);
	}
	finally {
		if (this.frame != browserFrame) {
			selectFrame(browserFrame);
		}
	}
}

@SuppressWarnings("boxing")
private void dragAndDrop(final WebBrowserElement dragFrom, final WebBrowserElement dragTo, final Position dragFromPosition, final Position dragToPosition) {
	Point fromLocation = dragFrom.getLocation();
	Point toLocation = dragTo.getLocation();
	Dimension fromSize = dragFrom.getSize();
	Dimension toSize = dragTo.getSize();

	// Get Client X and Client Y locations
	int dragFromX= fromLocation.getX() + dragFromPosition.getOffset(fromSize.getWidth());
	int dragFromY= fromLocation.getY() + dragFromPosition.getOffset(fromSize.getHeight());
	int dragToX= toLocation.getX() + dragToPosition.getOffset(toSize.getWidth());
	int dragToY= toLocation.getY() + dragToPosition.getOffset(toSize.getHeight());

	getJavascriptExecutor().executeScript(DrapAndDropSimulator.JAVASCRIPT_SIMULATE_EVENHTML5_DRAGANDDROP, dragFrom.webElement, dragTo.webElement, dragFromX, dragFromY, dragToX, dragToY);
}

/**
 * Perform a drag and drop from the given horizontal and vertical offsets.
 *
 * @param element The element to be dragged and dropped
 * @param xOffset The horizontal offset for the drag
 * @param yOffset The vertical offset for the drag
 */
public void dragAndDropBy(final WebBrowserElement element, final int xOffset, final int yOffset) {
	debugPrintln("		+ Drag " + element + " to (" + xOffset+", "+yOffset+")");
	this.actions.dragAndDropBy(element, xOffset, yOffset).build().perform();
}

/**
 * Execute the given script on the WebBrowser Element
 *
 * @param script The script to execute
 * @return One of Boolean, Long, String, List or WebElement. Or null.
 */
public Object executeScript(final String script) {
	return getJavascriptExecutor().executeScript(script);
}

/**
 * Find an element in the current browser page for the given locator.
 * <p>
 * Note that this method allow recovery while trying to find the element
 * (see {@link #findElement(By, boolean)} for details on recovery). So, if an
 * exception occurs during the operation it will retry it {@link ScenarioUtils#MAX_RECOVERY_TRIES}
 * times before giving up and actually raise the exception...
 * </p><p>
 * The element is searched in the current browser frame.
 * </p>
 * @param locator The way to find the element in the page (see {@link By}).
 * @return The found element or <code>null</code> if the element was not found.
 */
@Override
public WebBrowserElement findElement(final By locator) {
	return findElement(locator, getCurrentFrame(), true/*recovery*/);
}

/**
 * Find an element in the current browser page for the given locator.
 * <p>
 * If recovery is allowed, {@link WebDriverException} exceptions are caught
 * and the operation is retried again until maximum of allowed retries is reached.
 * </p><p>
 * If recovery is not allowed, <code>null</code> is returned when a {@link WebDriverException}
 * occurs...
 * </p><p>
 * The element is searched in the current browser frame.
 * </p>
 * @param locator The locator to find the element in the page.
 * @param recovery Tells whether recovery is allowed when searching the element.
 * @return The found element or <code>null</code> if the element was not found.
 */
public WebBrowserElement findElement(final By locator, final boolean recovery) {
	return findElement(locator, getCurrentFrame(), recovery);
}

/**
 * Find an element in the current browser page for the given locator.
 * <p>
 * If recovery is allowed, {@link WebDriverException} exceptions are caught
 * and the operation is retried again until maximum of allowed retries is reached.
 * </p><p>
 * If recovery is not allowed, <code>null</code> is returned when a {@link WebDriverException}
 * occurs...
 * </p>
 * @param locator The locator to find the element in the page.
 * @param webFrame The expected frame where the element should be searched
 * @param recovery Tells whether recovery is allowed when searching the element.
 * @return The found element or <code>null</code> if the element was not found.
 * TODO Add the ability not to throw {@link ScenarioFailedError} when not found
 * (ie. add <code>fail</code> argument..)
 */
public WebBrowserElement findElement(final By locator, final WebBrowserFrame webFrame, final boolean recovery) {
	if (DEBUG) {
		debugPrintEnteringMethod("locator", getLocatorString(locator), "webFrame", webFrame, "recovery", recovery);
	}
//	if (DEBUG) debugPrintln("			(finding element "+locator+" for browser "+this+" in frame '"+webFrame+"')");

	// Fix locator if necessary
	By fixedLocator = fixLocator(locator);

	// Loop until exception
	int count = 0;
	while (true) {
		try {
			// Create a specific web element to be able to manage recovery
			WebBrowserElement webBrowserElement = new WebBrowserElement(this, webFrame, this.driver, fixedLocator);

			// If element is created, then no exception occurs, return it
			if (DEBUG) debugPrintln("			(  -> found "+webBrowserElement+")");
			return webBrowserElement;

		}
		catch (@SuppressWarnings("unused") NoSuchElementException nsee) {
			return null;
		}
		catch (@SuppressWarnings("unused") UnhandledAlertException uae) {
			purgeAlerts("Finding element '"+fixedLocator+"'");
			if (!recovery) {
				return null;
			}
		}
		catch (WebDriverException wde) {
			// If recovery is allowed, catch exception to retry
			if (recovery) {
				catchWebDriverException(wde, "finding element '"+fixedLocator+"'", count++);
			} else {
				// If not, dump exception and leave with no result
				if (DEBUG) debugPrintException(wde);
				return null;
			}
		}
	}
}

/**
 * Find an element with the given locator in the current browser page or one of
 * its visible frame, even if there are some embedded ones.
 * <p>
 * If an element is found in one of this frame, it becomes the current browser
 * frame. In case no element is found, the browser has no frame selected when
 * returning from this method.
 * </p><p>
 * Note that this method allow recovery while trying to find the element
 * (see {@link #findElements(By, boolean)} for details on recovery). So, if an
 * exception occurs during the operation it will retry it {@link ScenarioUtils#MAX_RECOVERY_TRIES}
 * times before giving up and actually raise the exception...
 * </p>
 * @param locator The locator to find the element in the page (see {@link By}).
 * @return The found element as {@link WebBrowserElement}.
 * TODO Try to get rid off this method as its has a high performance cost.
 * Frames should be explicitly handled...
 */
public WebBrowserElement findElementInFrames(final By locator) {
	if (DEBUG) {
		debugPrintEnteringMethod("locator", getLocatorString(locator));
	}
//	if (DEBUG) debugPrintln("		+ Find frame element");

	// Find elements
	List<WebBrowserElement> foundElements = findElementsInFrames(locator);
	if (foundElements == null) return null;
	int size = foundElements.size();
	if (size == 0) return null;

	// Warn if several are found
	if (size > 1) {
//		if (single) {
//			throw new MultipleVisibleElementsError("Unexpected multiple elements found.");
//		}
		debugPrintln("WARNING: found more than one elements ("+size+"), return the first one!");
	}

	// Return the found element
	return foundElements.get(0);
}

/**
 * Find elements in the current browser page for the given locator.
 * <p>
 * Note that this method allow recovery while trying to find the element
 * (see {@link #findElements(By, boolean)} for details on recovery). So, if an
 * exception occurs during the operation it will retry it {@link ScenarioUtils#MAX_RECOVERY_TRIES}
 * times before giving up and actually raise the exception...
 * </p>
 * @param locator The locator to find the element in the page.
 * @return The list of found elements as {@link List}. Note that each element of
 * the list is a  {@link WebBrowserElement}.
 */
@Override
public List<WebElement> findElements(final By locator) {
	return findElements(locator, true/*displayed*/, getCurrentFrame(), true/*recovery*/);
}

/**
 * Find elements in the current browser page for the given locator.
 *
 * @param locator The locator to find the element in the page
 * @param recovery Tells whether the research should try to workaround safely or
 * return <code>null</code> right away if any {@link WebDriverException}
 * exception occurs.
 * @return The list of found elements as {@link List}. Each element of the list
 * is a  {@link WebBrowserElement}.
 */
public List<WebElement> findElements(final By locator, final boolean recovery) {
	return findElements(locator, true/*displayed*/, getCurrentFrame(), recovery);
}

/**
 * Find elements in the current browser page for the given locator.
 *
 * @param locator The locator to find the element in the page
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param recovery Tells whether the research should try to workaround safely or
 * return <code>null</code> right away if any {@link WebDriverException}
 * exception occurs.
 * @return The list of found elements as {@link List}. Each element of the list
 * is a  {@link WebBrowserElement}.
 */
public List<WebElement> findElements(final By locator, final boolean displayed, final boolean recovery) {
	return findElements(locator, displayed, getCurrentFrame(), recovery);
}

/**
 * Find elements in the current browser page for the given locator.
 *
 * @param locator The locator to find the element in the page
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param webFrame The expected frame where the element should be searched
 * @param recovery Tells whether the research should try to workaround safely or
 * return <code>null</code> right away if any {@link WebDriverException}
 * exception occurs.
 * @return The list of found elements as {@link List}. Each element of the list
 * is a  {@link WebBrowserElement}.
 */
public List<WebElement> findElements(final By locator, final boolean displayed, final WebBrowserFrame webFrame, final boolean recovery) {
	if (DEBUG) {
		debugPrintEnteringMethod("locator", getLocatorString(locator), "displayed", displayed, "webFrame", webFrame, "recovery", recovery);
	}
//	if (DEBUG) debugPrintln("			(finding elements "+locator+" for "+this+")");

	// Fix locator if necessary
	By fixedLocator = fixLocator(locator);

	// Loop until exception
	int count = 0;
	while (true) {
		try {
			// Find web driver elements with the given locator
			List<WebElement> foundElements = this.driver.findElements(fixedLocator);

			// Workaround: it seems that although WebDriver.findElements contract
			// assumes to return a list, it might happen that a <code>null</code>
			// value was returned (might be a Selenium bug?).
			// To prevent the NPE, returns an empty list when this unexpected case happens
			if (foundElements == null) {
				if (DEBUG) debugPrintln("Workaround: return empty list when this.driver.findElements(By) returns null!");
				return NO_ELEMENT_FOUND;
			}

			// Seek the list got to build framework web elements list
			final int size = foundElements.size();
			int idx = 0;
			List<WebElement> pageElements = new ArrayList<WebElement>(size);
			for (WebElement foundElement: foundElements) {
				if (foundElement == null) {
					// Workaround: it happened sometimes that we get null slots
					// in the returned list (might be a Selenium bug?)!
					if (DEBUG) debugPrintln("Workaround: skip null element in this.driver.findElements(By) list!");
				} else {
					boolean addingElement = false;
					try {
						// Check whether the element is displayed
						addingElement = !displayed || foundElement.isDisplayed();
					}
					catch (@SuppressWarnings("unused") WebDriverException wde) {
						// Skip any web driver exception by considering the faulty element as not displayed
					}
					if (addingElement) {
						// Create a specific web element to be able to manage recovery
						final WebBrowserElement webBrowserElement = new WebBrowserElement(this, webFrame, this.driver, fixedLocator, foundElement, size, idx);
						pageElements.add(webBrowserElement);
						if (DEBUG) {
							debugPrint("			  (-> found '"+webBrowserElement);
							debugPrintln(")");
						}
					} else {
						if (DEBUG) debugPrintln("			  (-> element not displayed)");
					}

					// Increment the web elements index in its parent list
					idx++;
				}
			}

			// Return the web elements list
			return pageElements;
		}
		catch (UnreachableBrowserException ure) {
			throw ure;
		}
		catch (WebDriverException wde) {
			// If recovery is allowed, catch exception to retry
			if (recovery) {
				catchWebDriverException(wde, "finding elements '"+fixedLocator+"'", count++);
			} else {
				// If not, dump exception and leave with no result
				if (DEBUG) debugPrintException(wde);
				return NO_ELEMENT_FOUND;
			}
		}
	}
}

/**
 * Find elements with the given locator in the current browser page or one of
 * its visible frame, even if there are some embedded ones.
 * <p>
 * If elements are found in one of this frame, it becomes the current browser
 * frame. In case no element is found, the browser has no frame selected when
 * returning from this method.
 * </p><p>
 * Note that this method allow recovery while trying to find the element
 * (see {@link #findElements(By, boolean)} for details on recovery). So, if an
 * exception occurs during the operation it will retry it {@link ScenarioUtils#MAX_RECOVERY_TRIES}
 * times before giving up and actually raise the exception...
 * </p>
 * @param locator The locator to find the element in the page (see {@link By}).
 * @return The found element as {@link WebBrowserElement}.
 * TODO Try to get rid off this method as its has a high performance cost.
 * Frames should be explicitly handled...
 */
public List<WebBrowserElement> findElementsInFrames(final By locator) {
	if (DEBUG) {
		debugPrintEnteringMethod("locator", getLocatorString(locator));
	}
//	if (DEBUG) debugPrintln("		+ Find frame elements");

// TODO Check that's safe to remove popup window management
//	// Check that no popup is opened
//	if (hasPopupWindow()) {
//		throw new ScenarioFailedError("An element cannot be find by looking in page frames when there's an opened popup window!");
//	}

	// Return found elements
	FramesScanner scanner = new FramesScanner();
	return scanner.findElements(locator);
}

///**
// * Get the web page content for the given web page.
// * <p>
// * This is a no-op if the browser has already loaded the page.
// * </p>
// *
// * @param newPage The page to open
// * @see #get(String)
// */
//public final void get(final WebPage newPage) {
//	if (this.page != null) {
//		this.history.push(this.page);
//	}
//	this.page = newPage;
//	get(newPage.location);
//}

/**
 * Focus the browser on the given page.
 * <p>
 * Focus means that only the given page will be kept opened in the current browser.
 * All other opened pages will be closed and removed from cache.
 * </p><p>
 * <b>Warning</b>: This is a no-op if the given page is not opened in the current
 * browser (ie. it's not found in the cache).
 * </p>
 * @return The current page as a {@link WebPage} or <code>null</code>
 * if the page is not found in the cache.
 */
@SuppressWarnings("unchecked")
public <P extends WebPage> P focusOnPage(final Class<P> pageClass) {
	debugPrintEnteringMethod("pageClass", pageClass);
	int size = this.pagesCache.size();
	for (int i=size-1; i>=0; i--) {
		WebPage page = this.pagesCache.get(i);
		Class<? extends WebPage> cachedPageClass = page.getClass();
		while (cachedPageClass != null) {
			if (cachedPageClass.equals(pageClass)) {
				closeOtherWindowsIfNeeded(page);
				return (P) page;
			}
			cachedPageClass = (Class<? extends WebPage>) cachedPageClass.getSuperclass();
		}
	}
	return null;
}

/**
 * Open the given page in the browser.
 * <p>
 * This is a no-op if either the browser url or the driver url are already at the
 * given page url.
 * </p><p>
 * This method already handles the InternetExplorer certificate, no needs to add
 * specific after this call to manage it.
 * </p><p>
 * Alerts opened at the page opening are also handled by this method and purged
 * as workarounds.
 * </p>
 *
 * @param page The page to be opened in the browser
 * @see WebDriver#get(String)
 */
public final void get(final WebPage page) {
	debugPrintEnteringMethod("page", page.location);

	// URL has changed, load the new one
	String currentUrl = this.driver.getCurrentUrl();
	String pageLocation = page.location;
	if (pageLocation.equals(currentUrl)) {
		if (DEBUG) debugPrintln("INFO: browser was already at '"+pageLocation+"'.");
	} else {
		if (DEBUG) {
			debugPrintln("		+ browser get: "+pageLocation);
			debugPrintln("		  -> driver url: "+currentUrl);
			debugPrintln("		  -> stored location: "+this.location);
		}

		// Get current location
		this.driver.get(pageLocation);

		// Accept private connections if any
		acceptPrivateConnection();

		// Purge alerts if necessary
		purgeAlerts("Alerts observed when getting page "+pageLocation);

		// Wait 2 seconds that browser URL changes (only if not in login operation)
		if (!currentUrl.endsWith("login")) { // TODO Check whether this test can be removed as it was before CLM SaaS implementation
			long timeout = System.currentTimeMillis() + 2000; // 2 seconds
			while (currentUrl.equals(this.driver.getCurrentUrl())) {
				if (System.currentTimeMillis() > timeout) {
					// Cannot fail here as product might accept two different URLs for the same page and replace page location automatically by current URL...
					// As an example: 'https://.../jts/dashboards' will be automatically replaced by 'https://.../jts/dashboards/1'
					// Hence just dump a warning in the debug file to keep a trace of this kind fof behavior
					debugPrintln("WARNING: Browser URL didn't change after having being set to '"+pageLocation+"', it stayed at '"+currentUrl+"'");
					debugPrintStackTrace(1);
					break;
				}
				pause(100);
			}
		}
	}

	// Store new location and handle
	this.location = pageLocation;
	page.handle = this.driver.getWindowHandle();

	// Dump the info that there was a page redirection
	// (or at least the browser URL has changed after the page load...)
	if (!pageLocation.equals(this.driver.getCurrentUrl())) {
		// Info
		if (DEBUG) debugPrintln("INFO: URL of page '"+pageLocation+"' has changed to '"+this.driver.getCurrentUrl()+"' just after having been loaded.");
	}
}

abstract protected Capabilities getCapabilities();

/**
 * Get the current cookies
 *
 * @return The current cookies in web browser
 * @see Options#getCookies()
 */
public Set<Cookie> getCookies() {
	return this.driver.manage().getCookies();
}

/**
 * Return the current frame used by the browser.
 * <p>
 * Note that in case of an opened popup window, this the frame of this window
 * which is returned.
 * </p>
 * @return The frame as a {@link WebBrowserFrame}
 */
public WebBrowserFrame getCurrentFrame() {
// TODO Check that's safe to remove framePopup
//	if (hasPopupWindow()) {
//		return this.framePopup;
//	}
	return this.frame;
}

/**
 * Return the current page displayed on the browser.
 * <p>
 * This is the last page of the internal cache.
 * </p>
 * @return The current page as a {@link WebPage}.
 */
public WebPage getCurrentPage() {
	int size = this.pagesCache.size();
	if (size == 0) return null;
	return this.pagesCache.get(size-1);
}

/**
 * Get the current page URL.
 *
 * @return The page URL as a {@link String}.
 * @see WebDriver#getCurrentUrl()
 */
public final String getCurrentUrl() {
	return this.url = this.driver.getCurrentUrl();
}

/**
 * Get the path of the default download directory.
 *
 * @return The path of the default download directory as a {@link File}
 */
public String getDownloadDir(){
	final File downloadDir = this.manager.getDownloadDir();
	if (downloadDir == null) {
		return null;
	}
	return downloadDir.getAbsolutePath();
}

/**
 * Return the Selenium driver. This method is strictly used for performance testing only.
 *
 * @return The Selenium driver as a {@link WebDriver}.
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
public WebDriver getDriverForPerformance() {
	return this.driver;
}

/**
 * Get the version information from current browser.
 *
 * @return Browser version
 */
public abstract String getDriverInfo();

private JavascriptExecutor getJavascriptExecutor() {
	return (JavascriptExecutor) this.driver;
}

/**
 * Return the browser name.
 *
 * @return The browser name
 */
public String getName() {
	return this.manager.getName();
}

/**
 * Return the current page displayed on the browser.
 * <p>
 * This is the last page of the internal cache.
 * </p>
 * @return The current page as a {@link WebPage}.
 */
@SuppressWarnings("unchecked")
public <P extends WebPage> P getPage(final Class<P> pageClass) {
	int size = this.pagesCache.size();
	for (int i=size-1; i>=0; i--) {
		WebPage page = this.pagesCache.get(i);
		Class<? extends WebPage> cachedPageClass = page.getClass();
		while (cachedPageClass != null) {
			if (cachedPageClass.equals(pageClass)) {
				switchToHandle(page.handle);
				cachePage(page);
				return (P) page;
			}
			cachedPageClass = (Class<? extends WebPage>) cachedPageClass.getSuperclass();
		}
	}
	return null;
}

/**
 * Return the performance manager.
 *
 * @return The performance manager as a {@link PerfManager}.
 */
public PerfManager getPerfManager() {
	return this.perfManager;
}

/**
 * Get the current page URL.
 *
 * @return The page URL as a {@link URL}.
 * @see WebDriver#getCurrentUrl()
 */
public final URL getUrl() {
	try {
		return new URL(getCurrentUrl());
	} catch (MalformedURLException mue) {
		throw new ScenarioFailedError(mue, /*print:*/true);
	}
}

/**
 * Get the version information from current browser.
 *
 * @return Browser version
 */
public String getVersion() {
	Capabilities capabilities = getCapabilities();
	String version = (String) capabilities.getCapability("version");
	if (version == null) {
		version = (String) capabilities.getCapability("browserVersion");
		if (version == null) {
			return "Unknown";
		}
	}
	return version;
}

/**
 * Find the visible frame expecting this is the last one. Display warnings if they
 * are several visible frame or if it's not the last frame in the browser page.
 */
private WebBrowserElement getVisibleFrame(final int retries) throws ScenarioFailedError {
	debugPrintln("		+ Get visible frame");

	// Reset frame
	resetFrame();

	// Get all page frames
	List<WebBrowserElement> frames = waitForElements(null, By.tagName("iframe"), true, 10/*sec*/, false);

	// Wait for a visible frame
	WebBrowserElement visibleFrameElement = null;
	WebBrowserElement lastFrameElement = null;
	for (WebBrowserElement frameElement: frames) {
		if (frameElement.isDisplayed()) {
			if (visibleFrameElement != null) {
				throw new ScenarioFailedError("There are several visible frame!!!");
			}
			visibleFrameElement = frameElement;
			debugPrintln("		  -> visible frame: "+frameElement);
		}
		lastFrameElement = frameElement;
	}

	// Retry if none was found
	if (visibleFrameElement == null) {
		debugPrintln("		  -> no visible frame was found");
		if (retries > 10) {
			throw new ScenarioFailedError("No visible frame found.");
		}
		debugPrintln("		  -> retry after having waited 1 seconds ("+retries+")");
		sleep(1);
		return getVisibleFrame(retries+1);
	}

	// Retry if the visible frame is not the last one and that was expected
	if (visibleFrameElement != lastFrameElement) {
		debugPrintln("		  -> last frame was not visible.");
		if (retries < 2) {
			debugPrintln("		  -> retry after having waited 1 seconds ("+retries+")");
			sleep(1);
			return getVisibleFrame(retries+1);
		}
		debugPrintln("WORKAROUND: select visible frame which was not the last one!");
	}

	// Store the visible frame
	return visibleFrameElement;
}

/**
 * Get window handles.
 *
 * @return The list of current handles as a {@link Set} of {@link String}.
 * @throws ScenarioFailedError If there are several handles
 */
Set<String> getWindowHandles() throws ScenarioFailedError {
	return getWindowHandles(true);
}

/**
 * Get window handles.
 *
 * @param check Tells to check whether several handles are found or not.
 * @return The list of current handles as a {@link Set} of {@link String}.
 * @throws ScenarioFailedError If there are several handles and check was requested
 */
Set<String> getWindowHandles(final boolean check) throws ScenarioFailedError {
	Set<String> handles;
	try {
		handles = this.driver.getWindowHandles();
	}
	catch (@SuppressWarnings("unused") NoSuchSessionException snfe) {
		return Sets.newHashSet();
	}
	if (check && handles.size() > 2) {
		println("Unexpected number of window handles: "+handles.size());
		println(" - main window handle: "+this.mainWindowHandle);
		println(" - handles: "+getTextFromList(handles));
		throw new ScenarioFailedError("Unexpected number of handles: "+handles.size());
	}
	return handles;
}

/**
 * Return the window position use for the browser.
 *
 * @return The browser window position
 */
public Point getWindowPosition() {
	return this.driver.manage().window().getPosition();
}

/**
 * Return the window size use for the browser.
 *
 * @return The browser window size
 */
public Dimension getWindowSize() {
	return this.driver.manage().window().getSize();
}

/**
 * Tell whether a specific download directory has been specified.
 *
 * @return <code>true</code> if property {@link BrowserConstants#BROWSER_DOWNLOAD_DIR_ID}
 * has been set, <code>false</code> otherwise
 */
protected boolean hasDownloadDir() {
	return getParameterValue(BROWSER_DOWNLOAD_DIR_ID) != null;
}

// TODO Check that's safe to remove popup window management
///**
// * Tells whether a popup windows is currently opened.
// *
// * @return <code>true</code> if a popup window is opened,
// * <code>false</code> otherwise.
// */
//public boolean hasPopupWindow() {
//	if (getWindowHandles().size() == 2) {
//		return true;
//	}
//	// TODO Check that's safe to remove frame popup
////	this.framePopup = null;
//	return false;
//}

/**
 * Tells whether the browser has a frame or not.
 *
 * @return <code>true</code> if a frame is selected, <code>false</code> otherwise.
 */
public boolean hasFrame() {
	return getCurrentFrame() != null;
}

/**
 * Init the driver corresponding to the current browser.
 */
protected abstract void initDriver();

/**
 * Init the browser profile.
 */
protected abstract void initProfile();

private void initScreenshotsDir() {
	String screenshotsRootDir = getParameterValue(SPOT_SCREENSHOT_DIR_ID, SPOT_SCREENSHOT_DIR_DEFAULT);
	if (screenshotsRootDir.indexOf(File.separatorChar) < 0) {
		screenshotsRootDir = System.getProperty(USER_DIR_ID) + File.separator + screenshotsRootDir;
	}
	this.screenshotsDir[FAILURE_SCREENSHOT] = createDir(screenshotsRootDir, "failures");
	this.screenshotsDir[WARNING_SCREENSHOT] = createDir(screenshotsRootDir, "warnings");
	this.screenshotsDir[INFO_SCREENSHOT] = createDir(screenshotsRootDir, "infos");
}

private void initWindow() {

	// Store the main window handle
	this.mainWindowHandle = this.driver.getWindowHandle();

	// Maximize window if specified
	if (getParameterBooleanValue("windowMax", true)) {
		if (getParameterIntValue("windowWidth") == 0 && getParameterIntValue("windowHeight") == 0) {
			maximize();
			return;
		}
	}

	// Compute and set window size
	try {
		Dimension dim = new Dimension(
			getParameterIntValue("windowWidth", DEFAULT_WIDTH),
			getParameterIntValue("windowHeight", DEFAULT_HEIGHT));

		// Get window size
		int x = getParameterIntValue("windowX", -1);
		int y = getParameterIntValue("windowY", -1);
		Point loc = null;
		if (x >= 0 || y >= 0) {
			if (x < 0) x = 0;
			if (y < 0) y = 0;
			loc = new Point(x, y);
		}

		// Set window size
		setWindow(dim, loc);
	}
	catch (NumberFormatException nfe) {
		throw new ScenarioFailedError(nfe, /*print:*/true);
	}
}

/**
 * Tells whether the current browser is Microsoft Edge or not.
 *
 * @return <code>true</code> if the current browser is Edge, <code>false</code>
 * otherwise.
 */
public boolean isEdge() {
	return this.manager.getType() == BROWSER_KIND_MSEDGE;
}

/**
 * Tells whether the current browser is Firefox or not.
 *
 * @return <code>true</code> if the current browser is FF, <code>false</code>
 * otherwise.
 */
public boolean isFirefox() {
	return this.manager.getType() == BROWSER_KIND_FIREFOX;
}

/**
 * Tells whether the current browser is Google Chrome or not.
 *
 * @return <code>true</code> if the current browser is GC, <code>false</code>
 * otherwise.
 */
public boolean isGoogleChrome() {
	return this.manager.getType() == BROWSER_KIND_GCHROME;
}

/**
 * Tells whether the current browser is Internet Explorer or not.
 *
 * @return <code>true</code> if the current browser is IE, <code>false</code>
 * otherwise.
 */
public boolean isInternetExplorer() {
	return this.manager.getType() == BROWSER_KIND_IEXPLORER;
}

/**
 * Tells whether the current browser is Safari or not.
 *
 * @return <code>true</code> if the current browser is Safari, <code>false</code>
 * otherwise.
 */
public boolean isSafari() {
	return this.manager.getType() == BROWSER_KIND_SAFARI;
}

/**
 * Maximize the browser window.
 */
public void maximize() {
	debugPrintEnteringMethod();
	Window window = this.driver.manage().window();
	window.maximize();
	Dimension wSize = window.getSize();
	debugPrintln("		  -> max window size: "+wSize);
}

/**
 * Move the mouse to the middle of the given element.
 * <p>
 * Note that for link this action trigger the rich hover.
 * </p><p>
 * <b>Warning</b>: It's strongly advised to use {@link WebBrowserElement#moveToElement(boolean)}
 * instead which is protected against {@link StaleElementReferenceException}
 * although this method is not...
 * </p>
 * @param element The web element to move to
 * @param entirelyVisible Ensure that the entire web element will be visible in
 * the browser window
 * @see Actions#moveToElement(WebElement)
 */
public void moveToElement(final WebBrowserElement element, final boolean entirelyVisible) {
	if (DEBUG) debugPrintln("		+ Move mouse to web element "+element);

	// Add extra move if we want the entire element to be visible
	WebElement webElement = element.getWebElement();
	if (entirelyVisible) {
		// Get element size
		Dimension size = element.getSize();

		// Put the mouse to the element's top-left corner
		try {
			this.actions	.moveToElement(webElement, 0, 0);
		}
		catch (WebDriverException wde) {
			if (DEBUG) {
				debugPrintException(wde);
				debugPrintln("		  -> catching WebDriverException while trying to move to an element"+(element.isInFrame()?" (which was in a frame)":"")+", hence do nothing...");
			}
			return;
		}
		// Put the mouse to the element's bottom-right corner
		try {
			this.actions.moveToElement(webElement, size.width, size.height);
		}
		catch (WebDriverException wde) {
			if (DEBUG) {
				debugPrintException(wde);
				debugPrintln("		  -> catching WebDriverException while trying to move to an element"+(element.isInFrame()?" (which was in a frame)":"")+", hence do nothing...");
			}
			return;
		}
	}

	// Put the mouse into the middle of the element
    try {
	    this.actions.moveToElement(webElement).build().perform();
    } catch (WebDriverException wde) {
		if (DEBUG) {
			debugPrintException(wde);
			debugPrintln("		  -> catching WebDriverException while trying to move to an element"+(element.isInFrame()?" (which was in a frame)":"")+", hence do nothing...");
		}
		return;
    }
}

/**
 * Moves the mouse to an offset from the top-left corner of the element.
 *
 * @param element The web element to move to
 * @param xOffset Offset from the top-left corner. A negative value means coordinates right of the element.
 * @param yOffset Offset from the top-left corner. A negative value means coordinates above the element.
 */
public void moveToElement(final WebBrowserElement element, final int xOffset, final int yOffset) {
	this.actions.moveToElement(element, xOffset, yOffset).perform();
}

/**
 * Return whether or not to open a new browser session per each user.
 *
 * @return Whether or not to open a new browser session per each user as a <code>boolean</code>.
 */
public boolean newSessionPerUser() {
	return this.newSessionPerUser;
}

/**
 * Purge the given alert by accepting them before executing the given action.
 *
 * @return <code>true</code> if an alert was actually purged, <code>false</code>
 * if no alert was present.
 */
public boolean purgeAlert(final String action, final int count) {
	debugPrintEnteringMethod("action", action, "count", Integer.toString(count));

	// We don't want to loop infinitely in this method, hence give up after 10 tries
	if (count > 10) {
		throw new ScenarioFailedError("Too many unexpected alerts, give up!");
	}

	// Get a handle to the open alert, prompt or confirmation
	long startTime = System.currentTimeMillis();
	Alert alert;
	try {
		alert = this.driver.switchTo().alert();
	}
	catch (@SuppressWarnings("unused") NoAlertPresentException | NoSuchWindowException e) {
		long time = System.currentTimeMillis()-startTime;
		debugPrintln("		  -> That took "+timeString(time)+" to detect that there was no pending alert...");
		return false;
	}
	catch (Exception ex) {
		println("WARNING: Catch following exception while trying to purge alerts...");
		printException(ex);
		println(" => workaround is to consider that no alert was present and return safely...");
		return false;
	}
	long time = System.currentTimeMillis()-startTime;
	debugPrintln("		  -> That took "+timeString(time)+" to detect that there was a pending alert...");
	startTime = System.currentTimeMillis();

	// Get alert text
	String alertText;
	try {
		// Sometimes getText() on found alert can throw an ClassCassException.
		// Handle this exception if occurs and then accept alert even if getText() failed.
		alertText = alert.getText();
	}
	catch(Exception e) {
		alertText = "??? (unable to get alert text due to: "+e.getMessage()+")";
	}
	time = System.currentTimeMillis()-startTime;
	debugPrintln("		  -> That took "+timeString(time)+" to get the pending alert text...");

	// Display the alert text
	println("Alert "+count+": "+alertText);
	println("	- action: "+action);
	println("	- pause 1 second...");
	sleep(1);

	// Acknowledge the alert (equivalent to clicking "OK")
	startTime = System.currentTimeMillis();
	try {
		println("	- accept the alert...");
		alert.accept();
		println("done.");
	}
	catch(Exception e){
		println("	 get following exception while accepting the alert:");
		printException(e);
	}
	time = System.currentTimeMillis()-startTime;
	debugPrintln("		  -> That took "+timeString(time)+" to accept the alert...");

	// Return that an alert was purged
	return true;
}

/**
 * Purge alerts by accepting them before executing the given action.
 *
 * @param action The action message
 */
public int purgeAlerts(final String action) {
	debugPrintEnteringMethod("action", action);
	int n=1;
	while (purgeAlert(action, n)) {
		if (++n > 10) {
			throw new ScenarioFailedError("Too many unexpected alerts, give up!");
		}
		sleep(1);
	}
	return n-1;
}

/**
 * Refresh the current page content.
 */
public void refresh() {
	debugPrintEnteringMethod();
	debugPrintln("		  -> current browser url: "+getCurrentUrl());
	this.driver.navigate().refresh();
	purgeAlerts("While refreshing page...");
}

/**
 * Refresh the current page content with possible logout management.
 */
public void refreshManagingLogin(final WebPage currentPage) {
	debugPrintEnteringMethod("currentPage", currentPage==null ? "null" : getClassSimpleName(currentPage.getClass()));
	refresh();

	if (currentPage != null && currentPage.getUser() != null) {
		debugPrintln("		  -> page location: "+currentPage.getLocation());
		debugPrintln("		  -> page url: "+currentPage.getUrl());
		SpotAbstractLoginOperation logOperation = currentPage.getLoginOperation(currentPage.getUser());
		if (logOperation != null && logOperation.isExpectingLogin()) {
			debugPrintln("		  -> page is expecting login operation, perform it...");
			logOperation.performLogin();
		}
	}
}

/**
 * Reset the current browser embedded frame.
 * <p>
 * If the current browser is not an embedded frame (ie; {@link WebEmbeddedFrame})
 * then nothing is done on the current browser frame.
 * </p>
 */
public void resetEmbeddedFrame() {
    WebBrowserFrame currentFrame = getCurrentFrame();
	if (DEBUG) debugPrintln("		+ Reset embedded frame "+currentFrame);
	if (currentFrame instanceof WebEmbeddedFrame) {
		WebEmbeddedFrame embeddedFrame = (WebEmbeddedFrame) currentFrame;
		WebElementFrame parentFrame = embeddedFrame.switchToParent();
	// TODO Check that's safe to remove frame popup
//		if (hasPopupWindow()) {
//			this.framePopup = parentFrame;
//		} else {
			this.frame = parentFrame;
//		}
	}
}

/**
 * Reset the current browser frame.
 * <p>
 * After this operation no frame will be selected in the browser.
 * </p>
 */
public void resetFrame() {
	resetFrame(true/*store*/);
}

/**
 * Reset the current browser frame.
 * <p>
 * <b>WARNING</b>: When caller used a <code>false</code>value to not store
 * the reset, it's strongly recommended to call {@link #selectFrame()} after
 * in order to resynchronize the browser instance with its window.
 * </p>
 *
 * @param store Tells whether the reset frame should be stored in the browser or not.
 */
public void resetFrame(final boolean store) {
	if (DEBUG) debugPrintln("		+ Reset frame "+getCurrentFrame());
	try {
		this.driver.switchTo().defaultContent();
	}
	finally {
		if (store) {
			this.frame = null;
//			this.framePopup = null;
		}
	}
}

/**
 * Scroll one page down.
 */
public void scrollOnePageDown() {
	debugPrintEnteringMethod();
    executeScript("window.scrollBy(0, window.innerHeight);");
}

/**
 * Scroll one page up.
 */
public void scrollOnePageUp() {
	debugPrintEnteringMethod();
    executeScript("window.scrollBy(0, -window.innerHeight);");
}

/**
 * Scroll the page to the given element.
 * <p>
 * This is a no-op if the web element is already visible in the browser view.
 * </p>
 * @param element The web element to scroll the page to
 */
public void scrollPageTo(final WebBrowserElement element) {
	element.scrollIntoView();
}

/**
 * Scroll the page to top.
 */
public void scrollPageTop() {
	debugPrintEnteringMethod();
    executeScript("window.scrollTo(0,0);");
}

/**
 * Scroll the window displayed in the browser by the given horizontal and vertical values.
 *
 * @param x The horizontal value to scroll the window. If the value is positive, then
 * the scroll will be to the right and to the left if it's negative.
 * @param y The vertical value to scroll the window. If the value is positive, then
 * the scroll will be down and up if it's negative.
 */
public void scrollWindowBy(final int x, final int y) {
	if (DEBUG) debugPrintln("		+ Scroll browser window by x="+x+" and y="+y+" for current web element height .");
	StringBuilder scriptBuilder = new StringBuilder(" window.scrollBy(")
		.append(x).append(", ")
		.append(y).append(");");
	getJavascriptExecutor().executeScript(scriptBuilder.toString());
}

/**
 * Select items in elements list got from the given list element and the given
 * locator to find its children.
 * <p>
 * All {@link StringComparisonCriterion} are used to determine how to match an item in the elements
 * list to the expected/given option
 * </p>
 * <p>
 * If  useControl is set to true, it holds the {@link Keys#CONTROL} key
 * to perform a multi-selection. Of course, that works only if the list web element
 * allow multiple selection.
 * </p><p>
 * If the expected entries are not found, {@link WebBrowserElement#MAX_RECOVERY_ATTEMPTS}
 * attempts are done before raising a {@link ScenarioFailedError}. Note that a sleep
 * of 2 seconds is done between each attempt.
 * </p>
 * @param listElement The element which children are the elements list to
 * consider for selection.
 * @param entriesBy The way to find the children
 * @param useControl should hold control while selecting multiple elements
 * @param expected The items to select in the list, assuming that text matches
 * @return The array of the selected elements as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if not all elements to select were found after
 * having retried {@link WebBrowserElement#MAX_RECOVERY_ATTEMPTS} times.
 */
public WebBrowserElement[] select(final WebBrowserElement listElement, final By entriesBy, final boolean useControl, final String... expected) {
	return select(listElement, entriesBy, useControl, StringComparisonCriterion.values(), expected);
}

/**
 * Select items in elements list got from the given list element and the given
 * locator to find its children.
 * <p>
 * If  useControl is set to true, it holds the {@link Keys#CONTROL} key
 * to perform a multi-selection. Of course, that works only if the list web element
 * allow multiple selection.
 * </p><p>
 * If the expected entries are not found, {@link WebBrowserElement#MAX_RECOVERY_ATTEMPTS}
 * attempts are done before raising a {@link ScenarioFailedError}. Note that a sleep
 * of 2 seconds is done between each attempt.
 * </p>
 * @param listElement The element which children are the elements list to
 * consider for selection.
 * @param entriesBy The way to find the children
 * @param useControl should hold control while selecting multiple elements
 * @param comparisonCriteria A list of criteria to determine how to match an item in the
 * elements list to the expected/given option
 * @param expected The items to select in the list, assuming that text matches
 * @return The array of the selected elements as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if not all elements to select were found after
 * having retried {@link WebBrowserElement#MAX_RECOVERY_ATTEMPTS} times.
 */
public WebBrowserElement[] select(final WebBrowserElement listElement, final By entriesBy, final boolean useControl, final StringComparisonCriterion[] comparisonCriteria, final String... expected) {

	// Init array to return
	final int length = expected.length;
	WebBrowserElement[] selectedElements = new WebBrowserElement[length];
	int selected = 0;

	// Select elements with possible recovery
	String selectedOptions = "no option";
	recoveryLoop: for (int recovery=1; recovery<=MAX_RECOVERY_ATTEMPTS; recovery++) {

		// Get the elements list
		List<WebElement> listElements = listElement.findElements(entriesBy, true/*displayed*/, false/*no recovery*/);
		final int size = listElements.size();
		if (size < length) {
			debugPrintln("Workaround ("+recovery+"): only "+size+" items were found ("+length+" expected at least...), retry to get the elements...");
			sleep(recovery);
			continue;
		}

		// Look for requirements in the list
		selected = 0;
		Iterator<WebElement> optionsListIterator = listElements.iterator();
		optionsLoop: while (optionsListIterator.hasNext()) {

			// Get the option element text
			WebBrowserElement optionElement = (WebBrowserElement) optionsListIterator.next();
			String optionText = optionElement.getText();
			if (optionText.isEmpty()) {
				optionText = optionElement.getAttribute("aria-label");
				if (optionText.isEmpty()) {
					optionText = optionElement.getAttribute("value");
					if (optionText.isEmpty()) {
						throw new ScenarioFailedError("Cannot find text for option '"+optionElement+"' of selection '"+listElement+"'");
					}
				}
			}

			// First wait while the list content is loaded
			int loadTimeout = 0;
			while (optionText.equals("Loading...")) {
				if (loadTimeout++ > 10) { // 10 seconds
					debugPrintln("Workaround ("+recovery+"): list items are still loading after 10 seconds, retry to get the elements...");
					continue recoveryLoop;
				}
				sleep(1);
			}

			// Check for the requested selections
			ClickableWorkaroundState state = ClickableWorkaroundState.Init;
			for (int i=0; i<length; i++) {
				// Compare a candidate to select by relying on the given list of
				// comparison criteria
				for (StringComparisonCriterion comparisonCriterion : comparisonCriteria) {
					if (comparisonCriterion.compare(optionText, expected[i])) {
						selectedElements[i] = optionElement;
						if (++selected == length) {
							// All expected options have been found, prepare to click on them and leave
							while (true) {
								try {
									if (useControl) {
										try {
											// Hold control while selecting
											Actions selectAction = new Actions(this.driver);
											selectAction = selectAction.keyDown(Keys.CONTROL);
											// Select elements
											for (WebBrowserElement element: selectedElements) {
												selectAction = selectAction.click(element);
											}
											selectAction = selectAction.keyUp(Keys.CONTROL);
											selectAction.build().perform();
											if (state != ClickableWorkaroundState.Init) {
												println("	- Workaround worked :-)");
											}
										}
										catch (@SuppressWarnings("unused") StaleElementReferenceException sere) {
											continue recoveryLoop;
										}
									} else {
										for (WebBrowserElement element: selectedElements) {
											element.scrollIntoView();
											element.click();
										}
									}

									// Return the selected elements
									return selectedElements;
								}
								catch (WebDriverException wde) {
									if (wde.getMessage().contains("Element is not clickable")) {
										state = workaroundForNotClickableException(state, optionElement, wde);
									} else {
										throw wde;
									}
								}
							}
						}
						// A matching element has been found. Therefore, return to the
						// optionsLoop to continue with searching for the remaining elements.
						continue optionsLoop;
					}
                }
			}
		}

		// We missed at least one item to select, try again
		if (selected == 0) {
			selectedOptions = "no option";
		} else {
			selectedOptions = "only "+selected+" option";
			if (selected > 1) {
				selectedOptions += "s";
			}
		}
		final StringBuffer message = new StringBuffer("Workaround (")
			.append(recovery)
			.append("): ")
			.append(selectedOptions)
			.append(" selected although we expected ")
			.append(length)
			.append(", try again...");
		debugPrintln(message.toString());
		sleep(2);
	}

	// Fail as not all elements were selected
	final StringBuffer message = new StringBuffer("After ")
		.append(MAX_RECOVERY_ATTEMPTS)
		.append(" attempts, there are still ")
		.append(selectedOptions)
		.append(" selected although we expected ")
		.append(length)
		.append(", give up");
	throw new WaitElementTimeoutError(message.toString());
}

/**
 * Select items in elements list got from the given list element and the given
 * locator to find its children.
 * <p>
 * If the expected entries are not found, {@link WebBrowserElement#MAX_RECOVERY_ATTEMPTS}
 * attempts are done before raising a {@link ScenarioFailedError}. Note that a sleep
 * of 2 seconds is done between each attempt.
 * </p>
 * @param listElement The element which children are the elements list to
 * consider for selection.
 * @param entriesBy The way to find the children
 * @param expected The items to select in the list, assuming that text matches
 * @return The array of the selected elements as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if not all elements to select were found after
 * having retried {@link WebBrowserElement#MAX_RECOVERY_ATTEMPTS} times.
 */
public WebBrowserElement[] select(final WebBrowserElement listElement, final By entriesBy, final String... expected) {
	return select(listElement, entriesBy, expected.length > 1, expected);
}

/**
 * Set the current browser frame to the given web element.
 *
 * @param frameElement The frame element to select.
 */
public void selectEmbeddedFrame(final WebBrowserElement frameElement, final WebBrowserFrame browserFrame, final boolean store) {
	if (DEBUG) {
		debugPrintln("		+ Select embedded frame "+frameElement);
		debugPrintln("		  -> current frame: "+getCurrentFrame());
	}
	if (browserFrame == null) {
		selectFrame(frameElement, true/*force*/, store);
	} else {
		final WebEmbeddedFrame newFrame = new WebEmbeddedFrame(this, browserFrame, frameElement);
		newFrame.switchTo();
		if (store) {
			setCurrentFrame(newFrame);
		}
	}
}

/**
 * Select the current stored frame.
 * <p>
 * That can be necessary in case of desynchronization between the browser
 * instance and the real browser...
 * </p>
 */
public void selectFrame() {
	selectFrame(/*ignoreError*/false);
}

/**
 * Select the current stored frame.
 * <p>
 * That can be necessary in case of desynchronization between the browser
 * instance and the real browser...
 * </p>
 */
void selectFrame(final boolean ignoreError) {
	try {
		if (getCurrentFrame() == null) {
			if (DEBUG) debugPrintln("		+ Select no frame ");
			this.driver.switchTo().defaultContent();
		} else {
			if (DEBUG) debugPrintln("		+ Select current frame "+getCurrentFrame());
			getCurrentFrame().switchTo();
		}
	}
	catch (WebDriverException wde) {
		if (ignoreError) {
			debugPrintln("INFO: Exception "+wde.getMessage()+" was silently ignored...");
		} else {
			throw wde;
		}
	}
}

/**
 * Set the current browser frame to the given index.
 *
 * @param index The index of the frame to select.
 */
public void selectFrame(final int index) {
	if (DEBUG) {
		debugPrintln("		+ select frame index to "+index);
		debugPrintln("		  -> current frame: "+getCurrentFrame());
	}
	try {
		if (index < 0) {
			resetFrame();
		} else {
			final WebIndexedFrame newFrame = new WebIndexedFrame(this, index);
			newFrame.switchTo();
			setCurrentFrame(newFrame);
		}
	}
	catch (@SuppressWarnings("unused") NoSuchFrameException nsfe) {
		// Workaround
		println("Workaround: cannot select given frame indexed #"+index+", hence get back to the previous one: "+getCurrentFrame());
		selectFrame();
	}
	catch (@SuppressWarnings("unused") StaleElementReferenceException sere) {
		// Workaround
		takeScreenshotWarning("InvalidFrameSelection");
		println("WORKAROUND: the given frame name indexed #"+index+" is no longer displayed in the page");
		println("The current workaround is to reset the browser frame to current one: "+getCurrentFrame());
		println("Check framework or scenario implementation!");
		printStackTrace(1);
		selectFrame();
	}
}

/**
 * Set the current browser frame to the given name.
 * <p>
 * Note that nothing happen if the given frame is null. To reset the frame,
 * caller has to use the explicit method {@link #resetFrame()}.
 * </p>
 * @param frameName The name of the frame to select. That can be either a real
 * name for the frame or an index value.
 * @param force Force the frame selection. If not set and the browser frame
 * is the same than the given one, then nothing is done.
 * @throws NoSuchFrameException If the given frame name or index does not
 * exist in the web page content.
 */
@SuppressWarnings({"boxing", "unused", "unlikely-arg-type" })
public void selectFrame(final String frameName, final boolean force) {
	if (DEBUG) {
		debugPrintln("		+ Select frame to '"+frameName+"' (force="+force+")");
		debugPrintln("		  -> current frame:"+getCurrentFrame());
	}
	if (frameName != null || force) {
		try {
			try {
				int frameIndex = Integer.parseInt(frameName);
				if (getCurrentFrame().equals(frameIndex) && !force) {
					if (DEBUG) debugPrintln("		  -> nothing to do.");
				} else {
					final WebIndexedFrame newFrame = new WebIndexedFrame(this, frameIndex);
					newFrame.switchTo();
					setCurrentFrame(newFrame);
				}
			}
			catch (NumberFormatException nfe) {
				if (getCurrentFrame() != null && getCurrentFrame().equals(frameName) && !force) {
					if (DEBUG) debugPrintln("		  -> nothing to do");
				} else {
					final WebNamedFrame newFrame = new WebNamedFrame(this, frameName);
					newFrame.switchTo();
					setCurrentFrame(newFrame);
				}
			}
		}
		catch (NoSuchFrameException nsfe) {
			// Workaround
			println("Workaround: cannot select given frame named '"+frameName+"', hence get back to the previous one: "+getCurrentFrame());
			selectFrame();
		}
		catch (StaleElementReferenceException sere) {
			// Workaround
			takeScreenshotWarning("InvalidFrameSelection");
			println("Workaround: the given frame named '"+frameName+"' is no longer displayed in the page.");
			println("The current workaround is to reset the browser frame to current one: "+getCurrentFrame());
			println("Check framework or scenario implementation!");
			printStackTrace(1);
			selectFrame();
		}
	}
}

/**
 * Set the current browser frame to the given web element.
 *
 * @param frameElement The frame element to select.
 * @see org.openqa.selenium.WebDriver.TargetLocator#frame(WebElement)
 */
public void selectFrame(final WebBrowserElement frameElement) {
	selectFrame(frameElement, true);
}

/**
 * Set the current browser frame to the given web element.
 *
 * @param frameElement The frame element to select.
 * @param force Tells whether the frame should be set even if it's already the
 * current browser one
 * @see org.openqa.selenium.WebDriver.TargetLocator#frame(WebElement)
 */
public void selectFrame(final WebBrowserElement frameElement, final boolean force) {
	selectFrame(frameElement, force, true/*store*/);
}

/**
 * Set the current browser frame to the given web element.
 *
 * @param frameElement The frame element to select.
 * @param force Tells whether the frame should be set even if it's already the
 * current browser one
 * @param store Tells whether the new frame should be store in the browser or not.
 * Setting this argument to <code>false</code>  should be done cautiously as that
 * will imply a desynchronization between the browser instance and the window.
 * @see org.openqa.selenium.WebDriver.TargetLocator#frame(WebElement)
 */
@SuppressWarnings("unlikely-arg-type")
public void selectFrame(final WebBrowserElement frameElement, final boolean force, final boolean store) {
	if (DEBUG) {
		debugPrintln("		+ select frame "+frameElement);
		debugPrintln("		  -> current frame: "+getCurrentFrame());
	}
	try {
		if (frameElement == null) {
			if (getCurrentFrame() != null || force) {
				resetFrame();
			}
		} else if (force || getCurrentFrame() == null || !getCurrentFrame().equals(frameElement)) {
			final WebElementFrame newFrame = new WebElementFrame(this, frameElement);
			newFrame.switchTo();
			if (store) {
				setCurrentFrame(newFrame);
			}
		}
	}
	catch (@SuppressWarnings("unused") NoSuchFrameException nsfe) {
		// Workaround
		println("Workaround: cannot select given frame element "+frameElement+", hence get back to the previous one: "+getCurrentFrame());
		selectFrame();
	}
	catch (@SuppressWarnings("unused") StaleElementReferenceException sere) {
		// Workaround
		takeScreenshotWarning("InvalidFrameSelection");
		println("Workaround: the given frame element '"+frameElement+"' is no longer displayed in the page.");
		println("The current workaround is to reset the browser frame to current one: "+getCurrentFrame());
		println("Check framework or scenario implementation!");
		printStackTrace(1);
		selectFrame();
	}
}

public void selectFrame(final WebBrowserFrame webFrame) {
	selectFrame(webFrame, true/*store*/);
}

public void selectFrame(final WebBrowserFrame webFrame, final boolean store) {
	if (DEBUG) debugPrintln("		+ Select frame "+webFrame);
	debugPrintln("		  -> current frame:"+getCurrentFrame());
	try {
		if (webFrame == null) {
			this.driver.switchTo().defaultContent();
		} else {
			webFrame.switchTo();
		}
		if (store) {
			setCurrentFrame(webFrame);
		}
	}
	catch (@SuppressWarnings("unused") NoSuchFrameException nsfe) {
		// Workaround
		println("Workaround: cannot select given web frame '"+webFrame+"', hence get back to the previous one: "+getCurrentFrame());
		selectFrame();
	}
	catch (@SuppressWarnings("unused") StaleElementReferenceException sere) {
		// Workaround
		takeScreenshotWarning("InvalidFrameSelection");
		println("Workaround: the given web frame '"+webFrame+"' is no longer displayed in the page.");
		println("The current workaround is to reset the browser frame to current one: "+getCurrentFrame());
		println("Check framework or scenario implementation!");
		printStackTrace(1);
		selectFrame();
	}
}

/**
 * Select the current visible browser frame.
 * <p>
 * Do nothing if the browser has no visible frame.
 * </p>
 * @param timeout Timeout to find the visible frame
 * @return The visible frame in the browser which has becomethe current one
 * or <code>null</code> if browser has no visible frame.
 * @throws ScenarioFailedError If no visible frame has been found.
 */
public WebBrowserFrame selectVisibleFrame(final int timeout) {
	if (DEBUG) {
		debugPrintln("		+ Select visible frame");
		debugPrintln("		  -> current frame: "+getCurrentFrame());
	}

	try {
		// Check whether there's already a visible frame selected
		WebBrowserFrame currentFrame = getCurrentFrame();
		if (currentFrame instanceof WebElementFrame) {
			if (((WebElementFrame) currentFrame).isDisplayed()) {
				currentFrame.switchTo();
				return currentFrame;
			}
		}

		// Get visible frame
		WebBrowserElement frameElement = getVisibleFrame(0);
		WebElementFrame visibleFrame = new WebElementFrame(this, frameElement);

		// Switch to the visible frame and return it
		visibleFrame.switchTo();
		setCurrentFrame(visibleFrame);
		return visibleFrame;
	}
	catch (@SuppressWarnings("unused") ScenarioFailedError sfe) {
		// If error occurs when getting visible, that means there no visible frame
		if (DEBUG) debugPrintln("		  -> no frame visible in browser");
		return null;
	}
}

/**
 * Type the given text to the active element.
 * <p>
 * As the element isn't refocused after, you may use this
 * method to TAB between elements
 * </p>
 * @param sequence the key sequence to execute
 * @see Actions#sendKeys(CharSequence...)
 */
public void sendKeys(final CharSequence... sequence) {
	if (DEBUG) debugPrintln("		+ Send keys sequence '"+sequence+"' to current browser element.");
	this.actions.sendKeys(sequence).perform();
}

/**
 * Set current browser frame with the given one.
 * <p>
 * Note that current frame might be in the popup window if any is opened.
 * </p>
 * @param newFrame The new frame to store
 */
void setCurrentFrame(final WebBrowserFrame newFrame) {
	// TODO Check that's safe to remove frame popup
//	if (hasPopupWindow()) {
//		this.framePopup = newFrame;
//	} else {
		this.frame = newFrame;
//	}
}

/**
 * Set the browser window dimension and location.
 *
 * @param dimension The new browser window dimension.
 * @param location The new browser window location.
 */
private void setWindow(final Dimension dimension, final Point location) {
	debugPrintEnteringMethod("dimension", dimension, "location", location);
	if (DEBUG) debugPrintln("		+ (dimension="+dimension+", location="+location+")");

	// Check width argument
	int newWidth = dimension.width;
	if (newWidth < MIN_WIDTH || newWidth > MAX_WIDTH) {
		println("Specified window width: "+newWidth+" is not valid to run a CLM scenario, expected it between "+MIN_WIDTH+" and "+MAX_WIDTH+".");
		println("	=> this argument will be ignored and "+DEFAULT_WIDTH+" width used instead.");
		newWidth = DEFAULT_WIDTH;
	}

	// Check height argument
	int newHeight = dimension.height;
	if (newHeight < MIN_HEIGHT || newHeight > MAX_HEIGHT) {
		println("Specified window height: "+newHeight+" is not valid to run a CLM scenario, expected it between "+MIN_HEIGHT+" and "+MAX_HEIGHT+".");
		println("	=> this argument will be ignored and "+DEFAULT_HEIGHT+" height used instead.");
		newHeight = DEFAULT_HEIGHT;
	}

	// Resize window if necessary
	Dimension newDimension = new Dimension(newWidth, newHeight);
	Window window = this.driver.manage().window();
	Dimension wSize = window.getSize();
	debugPrintln("		  -> current window size: "+wSize);
	if (wSize.width < newDimension.width || wSize.height < newDimension.height) {
		int windowWidth = wSize.getWidth() < newDimension.width ? newDimension.width : wSize.getWidth();
		int windowHeight= wSize.getHeight() < newDimension.height ? newDimension.height : wSize.getHeight();
		this.windowSize = new Dimension(windowWidth, windowHeight);
		debugPrintln("		  -> new window size: "+this.windowSize);
		window.setSize(this.windowSize);
	} else {
		this.windowSize = wSize;
	}

	// Set window location
	if (location != null) {
		window.setPosition(location);
	}
}

/**
 * Perform shift click to do a range selection.
 *
 * @param destination The last WebBrowseElment of a range selection selected by performing a shift click
 */
public void shiftClick(final WebBrowserElement destination) {
	this.actions.keyDown(Keys.SHIFT)
	    .moveToElement(destination)
	    .click()
	    .keyUp(Keys.SHIFT)
	    .build()
	    .perform();
}

/**
 * Switch to given window handle.
 *
 * @param handle The handle to switch to
 * @throws NoSuchWindowException If the window cannot be found
 */
public void switchToHandle(final String handle) throws NoSuchWindowException {
	debugPrintEnteringMethod("handle", handle);
	this.driver.switchTo().window(handle);
	if (!this.driver.getWindowHandle().equals(handle)) {
		throw new ScenarioFailedError("Cannot switch to new handle "+handle);
	}
	this.mainWindowHandle = handle;
}

/*
 * DISCARDED As now browser can have several handles...
 */
///**
// * Switch to popup window.
// *
// * @throws NoSuchWindowException When popup is transient and closed before
// * being able to switch to it.
// */
//public void switchToPopupWindow() throws NoSuchWindowException {
//	if (DEBUG) debugPrintln("		+ Switch to popup window");
//
//	// Check that a popup exist
//	long timeout = 10000 + System.currentTimeMillis(); // Timeout 10 seconds
//	while (!hasPopupWindow()) {
//		if (System.currentTimeMillis() > timeout) {
//			throw new NoSuchWindowException("Popup window never comes up.");
//		}
//		pause(250);
//	}
//
//	// Switch to the popup window
//	if (DEBUG) debugPrintln("		  -> main window handle "+this.mainWindowHandle);
//	Iterator<String> iterator = getWindowHandles().iterator();
//	while (iterator.hasNext()) {
//		String handle = iterator.next();
//		if (!handle.equals(this.mainWindowHandle) && !handle.equals(this.driver.getWindowHandle())) {
//			if (DEBUG) debugPrintln("		  -> switch to window handle "+handle);
//			this.driver.switchTo().window(handle);
//			break;
//		}
//	}
//
//	// Accept certificate
//	if (isInternetExplorer()) {
//		acceptInternetExplorerCertificate();
//	}
//}

/*
 * DISCARDED As there's no real way to know at this point what the new window is...
 */
///**
// * Switch to a new opened window.
// *
// * @param close Tells whether previous window should be closed
// * @throws NoSuchWindowException When popup is transient and closed before
// * being able to switch to it.
// */
//public void switchToNewWindow(final boolean close) throws NoSuchWindowException {
//	if (DEBUG) debugPrintln("		+ Switch to popup window");
//
//	// Check that a popup exist
//	long timeout = 10000 + System.currentTimeMillis(); // Timeout 10 seconds
//	while (!hasPopupWindow()) {
//		if (System.currentTimeMillis() > timeout) {
//			throw new NoSuchWindowException("Popup window never comes up.");
//		}
//		pause(250);
//	}
//
//	// Get new window handle
//	if (DEBUG) debugPrintln("		  -> main window handle "+this.mainWindowHandle);
//	Iterator<String> handles = getWindowHandles().iterator();
//	String newWindowHandle = null;
//	while (handles.hasNext()) {
//		String handle = handles.next();
//		if (handle.equals(this.mainWindowHandle)) {
//			if (!handle.equals(this.driver.getWindowHandle())) {
//				throw new ScenarioFailedError("Unexpected driver handle: "+this.driver.getWindowHandle()+", it should have been "+handle);
//			}
//		} else {
//			newWindowHandle = handle;
//		}
//	}
//
//	// Close previous window if requested
//	if (close) {
//		this.driver.close();
//	}
//
//	// Switch to the new window
//	if (DEBUG) debugPrintln("		  -> switch to window handle "+newWindowHandle);
//	this.driver.switchTo().window(newWindowHandle);
//	this.mainWindowHandle = newWindowHandle;
//
//	// Accept certificate
//	if (isInternetExplorer()) {
//		acceptInternetExplorerCertificate();
//	}
//}

/**
 * Switch back to current window.
 */
public void switchToMainWindow() {
	debugPrintEnteringMethod();
	this.driver.switchTo().window(this.mainWindowHandle);
}

/**
 * Switch to a browser opened window.
 *
 * @param urlInWindow The url of the window/tab to switch to
 * @throws NoSuchWindowException When popup is transient and closed before
 * being able to switch to it.
 */
public void switchToOpenedWindow(final String urlInWindow) throws NoSuchWindowException {
	if (DEBUG) debugPrintln("		+ switchToNewWindow("+urlInWindow+")");

	// Check whether the browser isn't already on the expected page
	if (getCurrentUrl().startsWith(urlInWindow)) {
		return;
	}

	// Get new window handle
	long timeout = System.currentTimeMillis() + 30000; // Wait 30 seconds by default
	if (DEBUG) {
		debugPrintln("		  -> main window handle: "+this.mainWindowHandle);
		debugPrintln("		  -> looking for handle of URL : "+urlInWindow);
	}
	while (System.currentTimeMillis() < timeout) {
		Iterator<String> handles = getWindowHandles().iterator();
		while (handles.hasNext()) {
			String handle = handles.next();
			if (!handle.equals(this.mainWindowHandle)) {
				this.driver.switchTo().window(handle);
				if (DEBUG) debugPrintln("		  -> found handle with URL: "+this.url);
				if (getCurrentUrl().startsWith(urlInWindow)) {
					// We've found the corresponding handle, hence store it as main and return
					this.mainWindowHandle = handle;
					if (DEBUG) debugPrintln("		  -> new main window handle is now: "+this.mainWindowHandle);
					return;
				}
			}
		}
		if (DEBUG) debugPrintln("		  -> no handle was found, wait 5 seconds more an retry...");
		sleep(5);
	}

	// Window with expected url was not found, fail the execution
	throw new ScenarioFailedError("Cannot find an opened browser window or tab with url: "+urlInWindow);
}

/**
 * Takes a snapshot of the given kind.
 *
 * @param fileName The name of the snapshot.
 * @param kind Snapshot kind, can be:
 * <ul>
 * <li>0: Snapshot for information (default)</li>
 * <li>1: Snapshot for warnings</li>
 * <li>2: Snapshot for failures</li>
 * <ul>
 */
private void takeScreenshot(final String fileName, final int kind) {
	debugPrintEnteringMethod("fileName", fileName, "kind", kind);

	// Get snapshot dir
	File currentSnapshotsDir = this.screenshotsDir[kind];

	// Get destination file name
	String destFileName = COMPACT_DATE_STRING + "_" + fileName + ".png";
	File file = new File(currentSnapshotsDir, destFileName);
	if (file.exists()) {
		int idx = 1;
		while (file.exists()) {
			destFileName = COMPACT_DATE_STRING + "_" + fileName + (idx < 10 ? "_0" : "_") + idx + ".png";
			file = new File(currentSnapshotsDir, destFileName);
			idx++;
		}
	}

	try {
		// Take snapshot
		sleep(2);
   	    File snapshotFile = ((TakesScreenshot)this.driver).getScreenshotAs(OutputType.FILE);
	    try {
	        File destFile = FileUtil.copyFile(snapshotFile, currentSnapshotsDir, destFileName);
		    println("		  -> screenshot available at " + destFile.getAbsolutePath());
        } catch (IOException e) {
	        printException(e);
		    println("		  -> cannot copy "+snapshotFile.getAbsolutePath()+" to "+currentSnapshotsDir+File.separator+destFileName+"!!!");
        }
	} catch (Throwable th) {
		// Catch if any exception occurs but which should not prevent
		// test to succeed at this stage...
		th.printStackTrace();
    }
}

/**
 * Takes a failure snapshot.
 *
 * @param fileName The name of the snapshot file.
 */
public void takeScreenshotFailure(final String fileName) {
	takeScreenshot(fileName, FAILURE_SCREENSHOT);
}

/**
 * Takes an information snapshot.
 *
 * @param fileName The name of the snapshot file.
 */
public void takeScreenshotInfo(final String fileName) {
	takeScreenshot(fileName, INFO_SCREENSHOT);
}

/**
 * Takes a snapshot and return it as a bytes array.
 *
 * @return The snapshot bytes array.
 */
public byte[] takeScreenshotsBytes() {
	 return ((TakesScreenshot)this.driver).getScreenshotAs(OutputType.BYTES);
}

/**
 * Takes a warning snapshot.
 *
 * @param fileName The name of the snapshot file.
 */
public void takeScreenshotWarning(final String fileName) {
	takeScreenshot(fileName, WARNING_SCREENSHOT);
}

@Override
public String toString() {
	StringBuilder builder = new StringBuilder(getName()+" browser");
	if (this.manager.getPath() != null) builder.append(", path=").append(this.manager.getPath());
	if (this.location != null) {
		builder.append(", url=").append(this.url);
		if (this.url == null  || !this.url.equals(this.location)) {
			builder.append(", location=").append(this.location);
		}
		builder.append(" (").append(getCurrentFrame()).append(')');
	}
	return builder.toString();
}

/**
 * Type a text in an input element.
 * <p>
 * Note that to raise the corresponding javascript even, an additional {@link Keys#TAB}
 * is hit after having entered the text.<br>
 * </p>
 * @param element The input field.
 * @param timeout The timeout before giving up if the text is not enabled
 * @param user User whom password has to be typed
 * @throws ScenarioFailedError if the input is not enabled before the timeout
 * @since 6.0
 */
public void typePassword(final WebBrowserElement element, final int timeout, final SpotUser user) {
	typeText(element, null, Keys.TAB, 100/*delay works for most of the cases*/, true/*clear*/, timeout, user);
}

/**
 * Type a text in an input element.
 * <p>
 * Note that to raise the corresponding javascript even, an additional {@link Keys#TAB}
 * is hit after having entered the text.<br>
 * </p>
 * @param element The input field.
 * @param text The text to type
 * @param key The key to hit after having entered the text in the input field
 * @param clear Tells whether the input field needs to be cleared before putting
 * the text in.
 * @param timeout The timeout before giving up if the text is not enabled
 * @throws ScenarioFailedError if the input is not enabled before the timeout
 */
public void typeText(final WebBrowserElement element, final String text, final Keys key, final boolean clear, final int timeout) {
	typeText(element, text, key, 100/*delay works for most of the cases*/, clear, timeout);
}

/**
 * Type a text in an input element.
 * <p>
 * Note that to raise the corresponding javascript even, an additional {@link Keys#TAB}
 * is hit after having entered the text.<br>
 * </p>
 * @param element The input field.
 * @param text The text to type.
 * @param key The key to hit after having entered the text in the input field.
 * @param keyDelay Defines the waiting time before the key parameter send to the input element.
 * @param clear Tells whether the input field needs to be cleared before putting.
 * the text in.
 * @param timeout The timeout before giving up if the text is not enabled.
 * @throws ScenarioFailedError if the input is not enabled before the timeout.
 */
public void typeText(final WebBrowserElement element, final String text, final Keys key, final int keyDelay, final boolean clear, final int timeout) {
	typeText(element, text, key, keyDelay, clear, timeout, null/*user*/);
}

private void typeText(final WebBrowserElement element, final String text, final Keys key, final int keyDelay, final boolean clear, final int timeout, final SpotUser user) {
	String printedText;
	if (user == null) {
		printedText = "text '"+text+"'";
	} else {
		printedText = "password '******'";
	}
	if (DEBUG) debugPrintln("		+ Type "+printedText+(clear?" (cleared)":EMPTY_STRING));

	// Check argument
	if (text == null && user == null) {
		throw new ScenarioFailedError("Invalid null arguments while type text in input web element.");
	}

	// Leave if text is not enabled
	if (!element.isEnabled()) {
		// Workaround
		debugPrintln("Workaround: Waiting for element to be enabled before typing text in it...");
		int count = 0;
		while (!element.isEnabled()) {
			if (count++ > timeout) { // Timeout
				throw new WaitElementTimeoutError("Cannot type "+printedText+" because the web element is disabled!");
			}
			sleep(1);
		}
	}

	// Move to element that will help to trigger javascript events
	element.moveToElement();

	// Type text
	if (clear) element.clear();
	if (user == null) {
		element.sendKeys(text);
	} else {
		element.enterPassword(user);
	}

	// Hit the key
	if (key != null) {
		pause(keyDelay); // short pause for elements such as 'FilterSelect'
		element.sendKeys(key);
	}
}

/**
 * Wait until have found the element using given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>the search occurs in the entire page or in the current frame if there's
 * one selected in the browser (see {@link #hasFrame()})</li>
 * <li>hidden element will be ignored</li>
 * <li>it will fail if:
 * <ol>
 * <li>the element is not found before timeout seconds</li>
 * <li>there's more than one element found</li>
 * </ol></li>
 * </ul>
 * </p>
 * </p>
 * @param locator Locator to find the element in the current page.
 * @param timeout The time to wait before giving up the research
 * @return The web element as {@link WebBrowserElement}
 * @throws WaitElementTimeoutError if no element was found before the timeout and asked to fail
 * @throws MultipleVisibleElementsError if several elements are found and only single one was expected
 */
public WebBrowserElement waitForElement(final By locator, final int timeout) {
	return waitForElement(null, locator, true/*fail*/, timeout, true/*displayed*/, true/*single*/);
}

/**
 * Wait until have found the element using given locator.
 * <p>
 * Only fail if specified and after having waited the given timeout.
 * </p>
 * @param parentElement The element from where the search must start.
 * If <code>null</code> then element is expected in the current page.
 * @param locator Locator to find the element in the current page.
 * @param fail Tells whether to fail if none of the locators is find before timeout
 * @param timeout The time to wait before giving up the research
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param single Tells whether a single element is expected
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws WaitElementTimeoutError if no element was found before the timeout and asked to fail
 * @throws MultipleVisibleElementsError if several elements are found and only single one was expected
 */
public WebBrowserElement waitForElement(final WebBrowserElement parentElement, final By locator, final boolean fail, final int timeout, final boolean displayed, final boolean single) {
	if (DEBUG) {
		debugPrintEnteringMethod("parentElement", parentElement, "locator", getLocatorString(locator), "fail", fail, "timeout", timeout, "displayed", displayed, "single", single);
	}
	/*
	if (DEBUG) {
		debugPrint("		+ waiting for element: [");
		if (parentElement != null) debugPrint(parentElement.getFullPath()+"]//[");
		debugPrint(locator+"]");
		debugPrintln(" (fail="+fail+", timeout="+timeout+", displayed="+displayed+", single="+single+")");
	}
	*/

	// Wait for all elements
	List<WebBrowserElement> foundElements = waitForElements(parentElement, locator, fail, timeout, displayed);
	if (foundElements == null) return null;
	int size = foundElements.size();
	if (size == 0) return null;
	if (!PERFORMANCE_ENABLED&&size > 1) {
		if (single) {
			throw new MultipleVisibleElementsError(foundElements);
		}
		debugPrintln("WARNING: found more than one elements ("+size+"), return the first one!");
	}

	// Return the found element
	return foundElements.get(0);
}

/**
 * Wait until have found one of element using the given search locators.
 * <p>
 * Fail only if specified and after having waited the given timeout.
 * </p>
 * @param parentElement The element from where the search must start.
 * If <code>null</code> then element is expected in the current page.
 * @param locators Search locators of the expected elements.
 * @param fail Tells whether to fail if none of the locators is find before timeout
 * @param timeout The time to wait before giving up the research
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws WaitElementTimeoutError if no element was found before the timeout and asked to fail
 */
public WebBrowserElement waitForElement(final WebBrowserElement parentElement, final By[] locators, final boolean fail, final int timeout) {
	if (DEBUG) {
		debugPrintEnteringMethod("parentElement", parentElement, "locators", getTextFromList(locators), "fail", fail, "timeout", timeout);
	}
	/*
	if (DEBUG) {
		debugPrintln("		+ waiting until finding one of following elements: ");
		if (parentElement != null) debugPrintln("		  - parent: "+parentElement.getFullPath());
		debugPrintln("		  - elements: ");
		for (By locator: locators) {
			debugPrintln("			* "+locator);
		}
	}
	*/

	// Wait for first found element
	WebBrowserElement[] multipleElements = waitForMultipleElements(parentElement, locators, fail, timeout);
	if (multipleElements != null) {
		for (WebBrowserElement foundElement: multipleElements) {
			if (foundElement != null) return foundElement;
		}
	}

	// No found element
	if (fail) {
		throw new WaitElementTimeoutError("Cannot find any of the researched elements.");
	}
	return null;
}

/**
 * Wait until have found one or several elements using given locator.
 * <p>
 * Only fail if specified and after having waited the given timeout.
 * </p>
 * @param parentElement The element from where the search must start.
 * If <code>null</code> then element is expected in the current page.
 * @param locator Locator to find the element in the current page.
 * @param fail Tells whether to fail if none of the locators is find before timeout
 * @param timeout The time to wait before giving up the research
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @return A {@link List} of web element as {@link WebBrowserElement}. Might
 * be empty if no element was found before the timeout and asked not to fail
 * @throws WaitElementTimeoutError if no element was found before the timeout and
 * asked to fail
 */
public List<WebBrowserElement> waitForElements(final WebBrowserElement parentElement, final By locator, final boolean fail, final int timeout, final boolean displayed) {
	if (DEBUG) {
		debugPrintEnteringMethod("parentElement", parentElement, "locator", getLocatorString(locator), "fail", fail, "timeout", timeout, "displayed", displayed);
	}
	/*
	if (DEBUG) {
		debugPrint("		+ waiting for elements: [");
		if (parentElement != null) debugPrint(parentElement.getFullPath()+"]//[");
		debugPrintln(locator+"]");
	}
	*/

	// Init counters
	final int max = timeout << 2;
	int count = 0;

	// Timeout Loop until timeout is reached
	while (count <= max) {

		// Find web driver elements or parent web element children elements
		List<WebElement> foundElements = parentElement == null
			? findElements(locator, displayed, true/*recovery*/)
			: parentElement.findElements(locator, displayed, true/*recovery*/);

		// Seek found elements to split visible and hidden ones
		int size = foundElements.size();
		List<WebBrowserElement> hiddenElements = new ArrayList<WebBrowserElement>(size);
		List<WebBrowserElement> visibleElements = new ArrayList<WebBrowserElement>(size);
		for (WebElement foundElement: foundElements) {

			// List element is a framework web element
			WebBrowserElement foundBrowserElement = (WebBrowserElement) foundElement;

			// Split visible and hidden elements
			if (!displayed) {
				visibleElements.add(foundBrowserElement);
				if (DEBUG) debugPrintln("		  -> found element: "+foundBrowserElement);
			}
			else if (foundBrowserElement.isDisplayed(false)) {
				visibleElements.add(foundBrowserElement);
				if (DEBUG) debugPrintln("		  -> found element: "+foundBrowserElement+" (visible)");
			} else {
				hiddenElements.add(foundBrowserElement);
				if (DEBUG) debugPrintln("		  -> found element: "+foundBrowserElement+" (hidden)");
			}
		}

		// Return visible elements if any
		int visibleSize = visibleElements.size();
		if (visibleSize > 0) {
			if (DEBUG) debugPrintln("		  -> return "+visibleSize+(displayed ? " visible" : " ")+" elements");
			return visibleElements;
		}

		// Return hidden elements if any and allowed
		int hiddenSize = hiddenElements.size();
		if (hiddenSize > 0 && !displayed) {
			if (DEBUG) debugPrintln("		  -> return "+hiddenSize+" hidden elements");
			return hiddenElements;
		}

		// Nothing to return, pause
		if (count < max) {
			pause(250);
		}
		count++;
	}

	// Fail as we can only reach this point if expected elements were not found in time
	if (DEBUG) debugPrintln("		  -> no element was found");
	if (fail) {
		StringBuilder builder = new StringBuilder("Timeout while waiting for '");
		builder.append(locator);
		builder.append("'. Took longer than '");
		builder.append(timeout);
		builder.append("' seconds.");

		throw new WaitElementTimeoutError(builder.toString());
	}
	return NO_BROWSER_ELEMENT_FOUND;
}

/**
 * Wait until at least one element is found using each of the given locator.
 * <p>
 * That method stores each found element using the given locators in the
 * the returned array, hence it may have more than one non-null slot.
 * </p><p>
 * Note that the method stop to search as soon as at least one element is found.
 * Hence, when several elements are found and returned in the array, that means
 * they have been found in the same loop. The timeout is only reached when
 * <b>no</b> element is found...
 * </p><p>
 * Note also that only displayed elements are returned.
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locators List of locators to use to find the elements in the current page.
 * @param fail Tells whether to fail if none of the locators is find before timeout
 * @param timeout The time to wait before giving up the research
 * @return An array with one non-null slot per element found before timeout
 * occurs or <code>null</code> if none was found and it has been asked not to fail.
 * @throws WaitElementTimeoutError if no element is found before the timeout occurs
 * and it has been asked to fail.
 */
public WebBrowserElement[] waitForMultipleElements(final WebBrowserElement parentElement, final By[] locators, final boolean fail, final int timeout) {
	return waitForMultipleElements(parentElement, locators, null/*displayed*/, fail, timeout);
}

/**
 * Wait until at least one element is found using each of the given locator.
 * <p>
 * That method stores each found element using the given locators in the
 * the returned array, hence it may have more than one non-null slot.
 * </p><p>
 * Note that the method stop to search as soon as at least one element is found.
 * Hence, when several elements are found and returned in the array, that means
 * they have been found in the same loop. The timeout is only reached when
 * <b>no</b> element is found...
 * </p><p>
 * Note also that only displayed elements are returned.
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locators List of locators to use to find the elements in the current page.
 * @param displayFlags List of flag telling whether the corresponding element should
 * be displayed or not. If <code>null</code>, then it's assumed that all elements
 * have to be displayed.
 * @param fail Tells whether to fail if none of the locators is find before timeout
 * @param timeout The time to wait before giving up the research
 * @return An array with one non-null slot per element found before timeout
 * occurs or <code>null</code> if none was found and it has been asked not to fail.
 * @throws WaitElementTimeoutError if no element is found before the timeout occurs
 * and it has been asked to fail.
 */
public WebBrowserElement[] waitForMultipleElements(final WebBrowserElement parentElement, final By[] locators, final boolean[] displayFlags, final boolean fail, final int timeout) {
	if (DEBUG) {
		debugPrintEnteringMethod("parentElement", parentElement, "locators", getTextFromList(locators), "displayFlags", getTextFromBooleans(displayFlags,"displayed","hidden"), "fail", fail, "timeout", timeout);
	}
	// Check arrays length
	if (displayFlags != null && displayFlags.length != locators.length) {
		throw new RuntimeException("Invalid lengthes of arrays: "+locators.length+" xpaths and "+displayFlags.length+" displayed flags.");
	}
	/*
	StringBuilder locatorBuilder = new StringBuilder();
	if (DEBUG) {
		debugPrint("		+ waiting for multiple elements (fail="+fail+", timeout="+timeout+"): ");
		String sep = "";
		int i=0;
		for (By locator: locators) {
			locatorBuilder.append(sep+"'"+locator+"'");
			if (displayFlags != null) {
				boolean displayed = displayFlags[i++];
				locatorBuilder.append(" ("+(displayed?"displayed":"hidden")+")");
			}
			sep = ", ";
		}
		debugPrintln(locatorBuilder.toString());
	}
	*/

	// Init
	final int max = timeout << 2;
	int count = 0;
	int length = locators.length;
	WebBrowserElement[] foundElements = new WebBrowserElement[length];

	// Timeout Loop until timeout is reached
	while (count <= max) {
		boolean found = false;

		// For each specified find locator
		for (int i=0; i<length; i++) {

			// Get displayed flag
			boolean displayed = displayFlags == null ? true : displayFlags[i];

			// Find the framework web elements
			List<WebElement> findElements = parentElement == null
				? findElements(locators[i], displayed, fail/*recovery*/)
				: parentElement.findElements(locators[i], displayed, fail/*recovery*/);

			// Put the found element in the return array
			for (WebElement findElement: findElements) {
				if (DEBUG)  debugPrintln("		  -> found '"+locators[i]+"'");
				foundElements[i] = (WebBrowserElement) findElement;
				found = true;
				break;
			}
		}

		// Leave as soon as one of the element is found
		if (found) {
			return foundElements;
		}

		// Nothing found, pause
		if (count < max) {
			pause(250);
		}
		count++;
	}

	// No elements were not found in allowed time, fail or return null
	if (DEBUG) debugPrintln("		  -> no elements were found");
	if (fail) {
		StringBuilder errorBuilder = new StringBuilder("Timeout while waiting for multiple elements: ");
		errorBuilder.append(getTextFromList(locators));
		errorBuilder.append(". Took longer than '");
		errorBuilder.append(timeout);
		errorBuilder.append("' seconds.");
		throw new WaitElementTimeoutError(errorBuilder.toString());
	}
	return null;
}

/**
 * Returns the text for the given element if it matches one of the given ones or
 * <code>null</code> if none matches before the given timeout.
 *
 * @param element The web element to get the text from
 * @param fail Tells whether to fail if element text does not match any of the
 * given ones before timeout occurs
 * @param timeout The time to wait before giving up
 * @param texts Several possible texts for the given element text.
 * @return The matching text as a <code>String</code> if one matches
 * before after having waited the given timeout or <code>null</code> when
 * it's asked not to fail.
 * @throws ScenarioFailedError If the text never matches before timeout occurs
 * and if it's asked to fail.
 */
public String waitForText(final WebBrowserElement element, final boolean fail, final int timeout, final String... texts) {
	if (DEBUG) {
		debugPrintEnteringMethod("element", element, "fail", fail, "timeout", timeout, "texts", getTextFromList(texts));
	}

	/* Get the text web element
	if (DEBUG) {
		debugPrint("		+ wait for texts: ");
		String separator = "";
		for (String msg : texts) {
			debugPrint(separator + "\"" + msg + "\"");
			separator = ", ";
		}
		debugPrintln();
	}
	*/

	// Timeout Loop until timeout is reached
	int count = 0;
	final int max = timeout << 2;
	String previousText = null;
	while (count < max) {

		// Get element text
		final String elementText = element.getText();

		// Check if text matches one of the given ones
		for (String text : texts) {
			if ((text.length() == 0 && elementText.length() == 0 ||
				(text.length() > 0 && elementText.startsWith(text)))) {
				if (DEBUG) debugPrintln("		  -> text was found: \""+elementText+"\"");
				return text;
			}
		}

		// Display element text if it has changed
		if (!elementText.equals(previousText)) {
			if (DEBUG) debugPrintln("		  -> current text is: \""+elementText+"\"");
			previousText = elementText;
		}

		// Pause if not at max and increment
		if (count < max) {
			pause(250);
		}
		count++;
	}

	// No elements were not found in allowed time, fail or return null
	if (DEBUG) debugPrintln("		  -> no text was found!");
	if (fail) {
		StringBuilder builder = new StringBuilder("timeout while waiting for '");
		builder.append(element.getLocator());
		builder.append('\'');
		throw new WaitElementTimeoutError(builder.toString());
	}
	return null;
}

/*
 * DISCARDED As now browser can have several handles...
 */
///**
// * Wait for a popup window to be opened and/or closed.
// * <p>
// * The possible state to wait for are:
// * <ul>
// * <li>0: Wait for the popup window to be opened.</li>
// * <li>1: Wait for the popup window to be closed.</li>
// * <li>2: Wait for the popup window to be opened, then closed.</li>
// * </ul>
// * </p>
// * @param state The expected state for the popup window, see above for the
// * valid values.
// * @param seconds Timeout in seconds to wait for the expected popup window
// * status
// * @param fail Tells whether to fail (ie. throw a {@link ScenarioFailedError}) if
// * the popup window state does not match the expected one
// * @return <code>true</code> if the popup window behaved as expected,
// * <code>false</code> otherwise when no failure is expected
// * @throws ScenarioFailedError if the popup window does not behave as expected
// * and failure was requested
// */
//public boolean waitForPopupWindowState(final PopupWindowState state, final int seconds, final boolean fail) throws ScenarioFailedError {
//	if (DEBUG) {
//		debugPrintln("		+ Wait for a popup window to "+state);
//	}
//
//	// Wait for popup to be opened if necessary
//	long timeout = seconds * 1000 + System.currentTimeMillis();
//	if (state != PopupWindowState.CLOSED) {
//		while (!hasPopupWindow()) {
//			if (System.currentTimeMillis() > timeout) {
//				if (fail) {
//					throw new ScenarioFailedError("Popup window never comes up.");
//				}
//				println("WARNING: Popup window never comes up.");
//				return false;
//			}
//			pause(100);
//		}
//	}
//
//	// Accept certificate
//	if (isInternetExplorer()) {
//
//		// Switch to the popup window
//		if (DEBUG) debugPrintln("		  -> main window handle "+this.mainWindowHandle);
//		Iterator<String> iterator = getWindowHandles().iterator();
//		while (iterator.hasNext()) {
//			String handle = iterator.next();
//			if (!handle.equals(this.mainWindowHandle)) {
//				if (DEBUG) debugPrintln("		  -> switch to window handle "+handle);
//				this.driver.switchTo().window(handle);
//				break;
//			}
//		}
//
//		// Accept certificate
//		acceptInternetExplorerCertificate();
//
//		// Back to main window
//		switchToMainWindow();
//	}
//
//	// Wait for popup to be closed if necessary
//	timeout = seconds * 1000 + System.currentTimeMillis();
//	if (state != PopupWindowState.OPENED) {
//		while (hasPopupWindow()) {
//			if (System.currentTimeMillis() > timeout) {
//				if (fail) {
//					throw new ScenarioFailedError("Popup window never close down.");
//				}
//				println("WARNING: Popup window never close down.");
//				return false;
//			}
//			pause(100);
//		}
//	}
//
//	// Popup window state is correct
//	return true;
//}

/**
 * Returns whether one of the given text is present in the current displayed page
 * content.
 *
 * @param parentElement The element from where the search must start.
 * If <code>null</code> then element is expected in the current page.
 * @param fail Tells whether to fail if none of the locators is find before timeout
 * @param timeout The time to wait before giving up the research
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param single Tells whether a single element is expected
 * @param pattern The pattern used for matching text (see {@link ComparisonPattern}).
 * @param texts List of the text to find in the page
 * @return The found web element where text matches one of the given one or
 *  <code>null</code> if not asked to fail
 * @throw ScenarioFailedError if none of the text is found before the timeout
 * was reached.
 */
public WebBrowserElement waitForTextPresent(final WebBrowserElement parentElement, final boolean fail, final int timeout, final boolean displayed, final boolean single, final ComparisonPattern pattern, final String... texts) {
	if (DEBUG) {
		debugPrintEnteringMethod("parentElement", parentElement, "fail", fail, "timeout", timeout, "displayed", displayed, "single", single, "pattern", pattern, "texts", getTextFromList(texts));
	}

	// Create the search locator
	By	textBy = ByUtils.xpathMatchingTexts(pattern, false/*all*/, texts);

	// Return the element found
	return waitForElement(parentElement, textBy, fail, timeout, displayed, single);

}

public <V> V waitUntil(final Function<WebDriver, V> function) {
	return new WebDriverWait(this.driver, Timeouts.DEFAULT_TIMEOUT).until(function);
}

ClickableWorkaroundState workaroundForNotClickableException(final ClickableWorkaroundState state, final WebBrowserElement element, final WebDriverException wde) {
	println("WARNING: Element element is not clickable!");
	println("	- Message: "+wde.getMessage());
	println("	- Locator="+element.locator);
	println("	- Full locator="+element.getFullLocator());
	printStackTrace(wde.getStackTrace(), 1);
	takeScreenshotInfo("ElementIsNotClickable");
	switch (state) {
		case Init:
			println("	- Try to workaround this scrolling up by 100 (in case a toolbar is masking it)...");
			scrollWindowBy(/*x:*/0, /*y:*/-100);
			return ClickableWorkaroundState.Up;
		case Up:
			println("	- Try to workaround this sending ESC to element (in case a tooltip is masking it)...");
			WebBrowserElement currentElement = element;
			boolean applied = false;
			while (!applied && currentElement != null) {
				try {
					currentElement.sendKeys(Keys.ESCAPE);
					applied = true;
				}
				catch (@SuppressWarnings("unused") ElementNotInteractableException enie) {
					currentElement = currentElement.getParent();
				}
			}
			return ClickableWorkaroundState.Esc;
		case Esc:
			println("	- Try to workaround this scrolling up by 200 (in case several toolbars are masking it)...");
			scrollWindowBy(/*x:*/0, /*y:*/-200);
			return ClickableWorkaroundState.DoubleUp;
		case DoubleUp:
			println("	- Try to workaround this scrolling down by 200 (just in case)...");
			scrollWindowBy(/*x:*/0, /*y:*/200);
			return ClickableWorkaroundState.Down;
		default:
			throw wde;
	}
}
}
