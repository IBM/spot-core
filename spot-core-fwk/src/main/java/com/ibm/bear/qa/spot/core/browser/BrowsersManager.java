/*********************************************************************
* Copyright (c) 2012, 2024 IBM Corporation and others.
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

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import com.ibm.bear.qa.spot.core.browsers.*;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioMissingImplementationError;
import com.ibm.bear.qa.spot.core.utils.FileUtil;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * Class to manage several framework browsers.
 * <p>
 * Note that browser is used for <i>instance of {@link WebBrowser} </i>. Which means that each
 * instance of browser might have one or several opened windows...
 * </p><p>
 * This class is responsible of storing the browser instances and retrieve them when requested. The
 * current model is to have one different browser per user.
 * </p><p>
 * In order to keep framework backward behavior compatible, it also provides a way to get the
 * currently used browser, allowing snapshots to be taken on the active browser when Selenium
 * execution problem occurs.
 * </p><p>
 * Following internal methods are also defined or specialized by this page:
 * <ul>
 * </ul>
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #close(User)}: Close the browser associated with the given user.</li>
 * <li>{@link #closeAll()}: Close the browser associated with the given user.</li>
 * <li>{@link #getBrowser(User)}: Get the browser used for the given user.</li>
 * <li>{@link #getBrowser(User,boolean)}: Get the browser used for the given user.</li>
 * <li>{@link #getBrowserOpened(User)}: Get the browser used and opened for the given user.</li>
 * <li>{@link #getCurrentBrowser()}: Get the currently used browser.</li>
 * <li>{@link #getDownloadDir()}: Return the directory where to download files from browsers.</li>
 * <li>{@link #getInstance()}: Return the singleton instance created when loading the class.</li>
 * <li>{@link #getLocale()}: Return the browser locale.</li>
 * <li>{@link #getName()}: Return the browser name.</li>
 * <li>{@link #getPath()}: Return the browser path.</li>
 * <li>{@link #getProfile(User)}: Return the browser profile name for the given user.</li>
 * <li>{@link #getType()}: Return the browser type.</li>
 * <li>{@link #isHeadless()}: Tells whether browser is headless or not.</li>
 * <li>{@link #isInPrivateMode(User)}: Tells whether browser is headless or not.</li>
 * <li>{@link #openNewBrowser(User)}: Open a new browser for the given page.</li>
 * <li>{@link #printBrowserInformation()}: Print information for browsers used during the scenario execution.</li>
 * <li>{@link #remove(User)}: Remove corresponding user from the managed browsers.</li>
 * <li>{@link #shutdown()}: Close all browsers.</li>
 * </ul>
 * </p>
 */
public class BrowsersManager {

	private class BrowserInformation {
		final String name, version, driverInfo;
		final Set<User> users = new HashSet<>();
		final Dimension windowSize;
		final Point windowPosition;
		public BrowserInformation(final WebBrowser browser) {
			this.name = browser.getName();
			this.version = browser.getVersion();
			this.driverInfo = browser.getDriverInfo();
			this.windowSize = browser.getWindowSize();
			this.windowPosition = browser.getWindowPosition();
		}
		void printInformation() {
			println("Browser used during the scenario execution:");
			println("	- " + this.name + " version: " + this.version);
			println("	- " + this.driverInfo);
			println("	- Browser is headless: " + isHeadless());
			print("	- Browser in private mode: ");
			String sep = EMPTY_STRING;
			for (User user: this.users) {
				print(sep+user.getId()+"="+isInPrivateMode(user));
				sep = ", ";
			}
			println();
			println("	- Browser window size: " + this.windowSize);
			println("	- Browser window position: " + this.windowPosition);
		}
	}

	/* Constants*/
	private final static BrowsersManager MANAGER = new BrowsersManager();

	 /**
	 * Return the singleton instance created when loading the class.
	 *
	 * @return <b>The</b> browser manager
	 */
	public static BrowsersManager getInstance() {
		return MANAGER;
	}
	/* Fields */
	// Browsers map
	final private Map<User, WebBrowser> browsers = new HashMap<User, WebBrowser>();
	// Current browser
	private WebBrowser currentBrowser = null;
	// Download directory
	private File downloadDir;

// Store browser information
private BrowserInformation browserInformation = null;

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
 * Close the browser associated with the given user.
 */
public void closeAll() {
	debugPrintEnteringMethod();
	Set<WebBrowser> uniqueBrowsers = new HashSet<>(this.browsers.values());
	for (WebBrowser browser: uniqueBrowsers) {
		List<User> toBeRemoved = new ArrayList<>();
		for (User user: this.browsers.keySet()) {
			if (browser == this.browsers.get(user)) { // == is intentionnal
				toBeRemoved.add(user);
			}
		}
		for (User user: toBeRemoved) {
			this.browsers.remove(user);
		}
		browser.close();
	}
	this.currentBrowser = null;
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
			this.currentBrowser = getNewBrowser(user);
		}
		else if (this.currentBrowser == null) {
			debugPrintln("		  -> There's no current browser, hence we need to open a new one...");
			this.currentBrowser = getNewBrowser(user);
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

private WebBrowser getNewBrowser(final User user) {
	final int type = getType();
	WebBrowser newBrowser;
	switch (type) {
		case BROWSER_KIND_FIREFOX:
			newBrowser = new FirefoxBrowser(this, user);
			break;
		case BROWSER_KIND_IEXPLORER:
			throw new RuntimeException("Internet Explorer is no longer supported use MS Edge instead.");
		case BROWSER_KIND_GCHROME:
			newBrowser = new ChromeBrowser(this, user);
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
//	printBrowserInfo(newBrowser, user);
	// Store new browser information
	if (this.browserInformation == null) {
		this.browserInformation = new BrowserInformation(newBrowser);
	}
	if (user != null) {
		this.browserInformation.users.add(user);
	}
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
 * Return the browser profile name for the given user.
 *
 * @param user The optional user the profile is associated with, might be <code>null</code>
 * @return The profile name to be used when creating a browser instance
 * or <code>null</code> if no specific profile has to be used
 */
public String getProfile(final User user) {
	String profile;
	if (user != null && getParameterBooleanValue(BROWSER_SPECIFIC_USER_PROFILE)) {
		profile = user.getId();
	} else {
		profile = getParameterValue(BROWSER_PROFILE_ID);
	}
	return profile;
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
 * @param user The optional user the profile is associated with, might be <code>null</code>
 * @return <code>true</code> if browser is headless, <code>false</code> otherwise
 */
public boolean isInPrivateMode(final User user) {
	if (getParameterBooleanValue(BROWSER_PRIVATE_ID)) {
		return true;
	}
	if (user == null) {
		return false;
	}
	String browserPrivateUsers = getParameterValue(BROWSER_PRIVATE_USERS);
	if (browserPrivateUsers != null) {
		String[] privateUsers = browserPrivateUsers.split(" ,");
		for (String privateUser: privateUsers) {
			if (user.getId().equals(privateUser)) {
				return true;
			}
		}
	}
	return false;
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
 * Print information for browsers used during the scenario execution.
 */
public void printBrowserInformation() {
	println();
	if (this.browserInformation == null) {
		println("Unfortunately there was no browser information! :-(");
	} else {
		this.browserInformation.printInformation();
	}
}

/**
 * Remove corresponding user from the managed browsers.
 *
 * @param user
 */
public void remove(final User user) {
	WebBrowser userBrowser = this.browsers.remove(user);
	if (this.currentBrowser == userBrowser) { // == is intentional!
		for (WebBrowser browser: this.browsers.values()) {
			if (browser == userBrowser) { // == is intentional!
				// We've found another user using current browser hence do not reset it
				return;
			}
		}
		this.currentBrowser = null;
	}
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
