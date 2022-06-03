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
package com.ibm.bear.qa.spot.core.browser;

import static com.ibm.bear.qa.spot.core.browser.BrowserConstants.*;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.io.File;
import java.util.*;

import com.ibm.bear.qa.spot.core.browsers.*;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioMissingImplementationError;
import com.ibm.bear.qa.spot.core.utils.FileUtil;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * Class to manage several framework browsers.
 * <p>
 * Note that browser is used for <i>instance of {@link WebBrowser}</i>.
 * Which means that each instance of browser might have one or several
 * opened windows...
 * </p><p>
 * This class is responsible of storing the browser instances and retrieve them
 * when requested. The current model is to have one different browser per user.
 * </p><p>
 * Sub-class has to implement {@link #getNewBrowser()} method in order
 * to return a specific kind of browser specified by {@link BrowserConstants#BROWSER_KIND_ID}
 * property.
 * </p><p>
 * In order to keep framework backward behavior compatible, it also provides
 * a way to get the currently used browser, allowing snapshots to be taken
 * on the active browser when Selenium execution problem occurs.
 * </p><p>
 * Following internal API methods are available on this class:
 * <ul>
 * <li>{@link #close(User)}: Close the browser associated with the given user.</li>
 * <li>{@link #getBrowser(User, boolean)}: Get the browser used for the given user.</li>
 * <li>{@link #getBrowserOpened(User)}: Get the browser used and opened for the given user.</li>
 * <li>{@link #getCurrentBrowser()}: Get the currently used browser.</li>
 * <li>{@link #getDownloadDir()}: Close all browsers.</li>
 * <li>{@link #getLocale()}: Close all browsers.</li>
 * <li>{@link #getPath()}: Close all browsers.</li>
 * <li>{@link #getProfile()}: Close all browsers.</li>
 * <li>{@link #getType()}: Close all browsers.</li>
 * <li>{@link #openNewBrowser(User)}: Close all browsers.</li>
 * <li>{@link #printBrowserInfo(WebBrowser)}: Close all browsers.</li>
 * <li>{@link #shutdown()}: Close all browsers.</li>
 * </ul>
 * </p><p>
 * Following internal methods are also defined or specialized by this page:
 * <ul>
 * </ul>
 * </p>
 */
public class BrowsersManager {

	/* Constants*/
	private final static BrowsersManager MANAGER = new BrowsersManager();

    /* Fields */
	// Browsers map
	final private Map<User, WebBrowser> browsers = new HashMap<User, WebBrowser>();
	// Current browser
	private WebBrowser currentBrowser = null;
	// Download directory
	private File downloadDir;

/**
 * Return the singleton instance created when loading the class.
 *
 * @return <b>The</b> browser manager
 */
public static BrowsersManager getInstance() {
	return MANAGER;
}

/**
 * Private constructor in order to use singleton pattern.
 */
private BrowsersManager() {}

/**
 * Close the browser associated with the given user.
 *
 * @param user The user to close the associated browser
 */
public void close(final User user) {
	debugPrintEnteringMethod("user", user);
	WebBrowser browser = getBrowser(user, false);
	if (browser == null) {
		println("Warning: there was no browser associated with user "+user.getId());
		println("	Hence, the requested close action was a no-op...");
		printStackTrace(1);
		return;
	}
	browser.close();
	// TODO Remove user is not enough, we should remove browser in the map instead...
	remove(user);
}

/**
 * Get the browser used for the given user.
 * <p>
 * Note that using this method, current browser will be associated with
 * the given user if none was initially found. If there's no browser currently
 * opened, then a new one will be opened, set as current browser and returned.
 * </p>
 * @param user The user who will use the browser
 * @return The browser instance
 */
public WebBrowser getBrowser(final User user) {
	return getBrowser(user, false);
}

/**
 * Get the browser used for the given user.
 *
 * @param user The user who will use the browser or <code>null</code> if there's
 * no user associated with current page
 * @param open Tells whether a new browser has to be opened if none is found
 * @return The browser instance or <code>null</code> if no browser
 * is found and <code>open</code> flag is <code>false</code>
 */
public WebBrowser getBrowser(final User user, final boolean open) {
	debugPrintEnteringMethod("user", user, "open", open);
	WebBrowser browser = user == null ? this.currentBrowser : this.browsers.get(user);
	if (browser == null) {
		debugPrintln("		  -> No browser found for given user...");
		if (open) {
			debugPrintln("		  -> Open a new browser...");
			this.currentBrowser = getNewBrowser();
		}
		else if (this.currentBrowser == null) {
			debugPrintln("		  -> There's no current browser, hence we need to open a new one...");
			this.currentBrowser = getNewBrowser();
		}
		if (user != null) {
			this.browsers.put(user, this.currentBrowser);
		}
	} else {
		debugPrintln("		  -> Found already opened browser...");
		this.currentBrowser = browser;
	}
	return this.currentBrowser;
}

/**
 * Get the browser used and opened for the given user.
 * <p>
 * Note that it opens a new browser if none was found associated
 * with the given user. If one is already opened for the user
 * then it's returned and set as current browser.
 * </p>
 * @param user The user who will use the browser
 * @return The browser instance
 */
public WebBrowser getBrowserOpened(final User user) {
	return getBrowser(user, true);
}

/**
 * Get the currently used browser.
 * <p>
 * This is a convenient method to know quickly which is
 * the browser under usage and, for example, allow to take
 * snapshots of its content in case of Selenium troubles...
 * </p>
 * @return The currenly used browser
 */
public WebBrowser getCurrentBrowser() {
	debugPrintEnteringMethod();
	if (this.currentBrowser == null) debugPrintln("		  -> No browser is currently opened...");
	return this.currentBrowser;
}

/**
 * Return the directory where to download files from browsers.
 * <p>
 * Can be modified by using {@link #BROWSER_DOWNLOAD_DIR_ID} property
 * </p><p>
 * Note that if specified directory does not exist, it's automatically created.
 * </p><p>
 * Warning: Currently MS Edge browser does not support to set a specific download directory
 * </p>
 * @return The directory where to download files from browsers
 * @throws ScenarioFailedError If the specified is not a directory or cannot be created
 */
public File getDownloadDir() {
	if (this.downloadDir == null) {
		String parameterValue = getParameterValue(BROWSER_DOWNLOAD_DIR_ID);
		if (parameterValue != null && getType() != BROWSER_KIND_MSEDGE) {
			this.downloadDir = FileUtil.createDir(parameterValue);
			if (this.downloadDir == null) {
				throw new ScenarioFailedError("Cannot create download directory "+parameterValue);
			}
		}
	}
	return this.downloadDir;
}

/**
 * Return the browser locale.
 *
 * @return The locale to be used when creating a browser instance
 * or <code>null</code> if default locale has to be used
 */
public String getLocale() {
	return getParameterValue(BROWSER_LOCALE_ID);
}

private WebBrowser getNewBrowser() {
	final int type = getType();
	WebBrowser newBrowser;
	switch (type) {
		case BROWSER_KIND_FIREFOX:
			newBrowser = new FirefoxBrowser(this);
			break;
		case BROWSER_KIND_IEXPLORER:
			throw new RuntimeException("Internet Explorer is no longer supported use MS Edge instead.");
		case BROWSER_KIND_GCHROME:
			newBrowser = new GoogleChromeBrowser(this);
			break;
		case BROWSER_KIND_MSEDGE:
			newBrowser = new EdgeBrowser(this);
			break;
		case BROWSER_KIND_SAFARI:
			newBrowser = new SafariBrowser(this);
			break;
		default:
			throw new RuntimeException("'"+type+"' is not a know browser kind, only 1 (Firefox), 3 (Chrome), 4 (Edge) and 5 (Safari) are currently supported");
	}
	printBrowserInfo(newBrowser);
	newBrowser.deleteAllCookies();
	return newBrowser;
}

/**
 * Return the browser path.
 * <p>
 * This should be the absolute path to the browser corresponding driver executable.
 * </p>
 * @return The path which might be <code>null</code> if the browser is on the system PATH.
 */
public String getPath() {
	String value = getParameterValue(BROWSER_PATH_ID);
	return value == null || value.length() == 0 ? null : value;
}

/**
 * Return the browser profile name.
 *
 * @return The profile name to be used when creating a browser instance
 * or <code>null</code> if no specific profile has to be used
 */
public String getProfile() {
	return getParameterValue(BROWSER_PROFILE_ID);
}

/**
 * Return the browser type.
 * <p>
 * Currently supported types are:
 * <ul>
 * <li>{@link BrowserConstants#BROWSER_KIND_FIREFOX}</li>
 * <li>{@link BrowserConstants#BROWSER_KIND_GCHROME}</li>
 * <li>{@link BrowserConstants#BROWSER_KIND_IEXPLORER}</li>
 * </ul>
 * </p>
 * @return The browser type
 */
public int getType() {
	return getParameterIntValue(BROWSER_KIND_ID, BROWSER_KIND_FIREFOX);
}

/**
 * Return the browser name.
 * <p>
 * Currently supported types are:
 * <ul>
 * <li>{@link BrowserConstants#BROWSER_KIND_FIREFOX}</li>
 * <li>{@link BrowserConstants#BROWSER_KIND_GCHROME}</li>
 * <li>{@link BrowserConstants#BROWSER_KIND_IEXPLORER}</li>
 * </ul>
 * </p>
 * @return The browser type
 */
public String getName() {
	switch (getType()) {
		case BROWSER_KIND_FIREFOX:
			return "Firefox";
		case BROWSER_KIND_GCHROME:
			return "Chrome";
		case BROWSER_KIND_IEXPLORER:
			return "Internet Explorer";
		case BROWSER_KIND_MSEDGE:
			return "Edge";
		case BROWSER_KIND_SAFARI:
			return "Safari";
		default:
			throw new ScenarioMissingImplementationError(whoAmI());
	}
}

/**
 * Tells whether browser is headless or not.
 * <p>
 * Default is not headless.
 * </p>
 * @return <code>true</code> if browser is headless, <code>false</code> otherwise
 */
public boolean isHeadless() {
	return getParameterBooleanValue(BROWSER_HEADLESS_ID);
}

/**
 * Tells whether browser is headless or not.
 * <p>
 * Default is not headless.
 * </p>
 * @return <code>true</code> if browser is headless, <code>false</code> otherwise
 */
public boolean isInPrivateMode() {
	return getParameterBooleanValue(BROWSER_PRIVATE_ID);
}

/**
 * Open a new browser for the given page.
 * <p>
 * Note that it closes the existing user browser if one is already opened
 * for the given user.
 * </p><p>
 * Note also that this method does not set the URL, that's the responbility
 * of callers. Hence, the returned new browser is opened but empty...
 * </p>
 * @param user The user who will use the browser
 * @return The opened browser
 */
public WebBrowser openNewBrowser(final User user) {
	WebBrowser browser = this.browsers.remove(user);
	if (browser != null) {
		browser.close();
	}
	return getBrowser(user, true);
}

/**
 * Remove corresponding user from the managed browsers.
 *
 * @param user
 */
public void remove(final User user) {
	this.browsers.remove(user);
}

/**
 * Print some general Browser information in the console.
 * <p>
 * The displayed information are:
 * <ul>
 * <li>Browser version</li>
 * <li>Browser window size</li>
 * <li>Browser window position</li>
 * </ul>
 * </p>
 */
void printBrowserInfo(final WebBrowser browser) {
	println("Browser information:");
	println("	- " + browser.getName()+" version: "+browser.getVersion());
	println("	- " + browser.getDriverInfo());
	println("	- Browser is headless = " + isHeadless());
	println("	- Browser in private mode = " + isInPrivateMode());
	println("	- Browser window size = " + browser.getWindowSize());
	println("	- Browser window position = " + browser.getWindowPosition());
}

/**
 * Close all browsers.
 */
public void shutdown() {
	Set<WebBrowser> allBrowsers = new HashSet<>(this.browsers.values());
	for (WebBrowser browser: allBrowsers) {
		browser.close();
		if (this.currentBrowser == browser) {
			this.currentBrowser = null;
		}
	}
	if (this.currentBrowser != null) {
		this.currentBrowser.close();
	}
}
}
