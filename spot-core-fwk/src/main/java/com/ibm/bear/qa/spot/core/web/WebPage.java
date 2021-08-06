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

import static com.ibm.bear.qa.spot.core.config.Timeouts.DELAY_AFTER_CLICK_LINK_TIMEOUT;
import static com.ibm.bear.qa.spot.core.config.Timeouts.DELAY_BEFORE_CLICK_LINK_TIMEOUT;
import static com.ibm.bear.qa.spot.core.performance.PerfManager.PERFORMANCE_ENABLED;
import static com.ibm.bear.qa.spot.core.performance.PerfManager.USER_ACTION_NOT_PROVIDED;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.StringUtils.hidePasswordInLocation;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.ibm.bear.qa.spot.core.api.SpotConfig;
import com.ibm.bear.qa.spot.core.api.SpotUser;
import com.ibm.bear.qa.spot.core.api.pages.SpotPage;
import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.factories.SpotWindowFactory;
import com.ibm.bear.qa.spot.core.nls.NlsMessages;
import com.ibm.bear.qa.spot.core.performance.PerfManager;
import com.ibm.bear.qa.spot.core.performance.PerfManager.RegressionType;
import com.ibm.bear.qa.spot.core.scenario.errors.*;
import com.ibm.bear.qa.spot.core.timeout.SpotAbstractTimeout;
import com.ibm.bear.qa.spot.core.topology.Application;
import com.ibm.bear.qa.spot.core.topology.Topology;
import com.ibm.bear.qa.spot.core.utils.ByUtils.ComparisonPattern;
import com.ibm.bear.qa.spot.core.utils.StringComparisonCriterion;

/**
 * The abstract class for any web page connected to a {@link WebBrowser}.
 * <p>
 * A web page is created using a {@link #location} which is the initial url sent to
 * the browser to load the page. It may slightly differ from the browser current
 * url after it has been loaded (see {@link #getUrl()}.
 * </p><p>
 * It also stores the configuration used while running the test in order
 * to access any necessary information from it (e.g. timeouts).
 * </p><p>
 * Finally, the web page is also associated with a {@link #getUser()} as its content
 * might slightly or completely differ depending of it. This user let also the page
 * know whether a login operation when a new one is connected to it.
 * </p><p>
 * This class provides the following basic functionalities:
 * <ul>
 * <li>{@link #get()}: get page content.</li>
 * </ul>
 * It also provides several convenient methods to access basic browser functionalities
 * directly from the page:
 * <ul>
 * </ul>
 * </p><p>
 * </p>
 * TODO Rename this class as <b>WebAbstractPage</b>
 */
public abstract class WebPage implements SpotPage {

	// The browser in which the current page is displayed
	protected WebBrowser browser;
	String handle;

	// Test config
	protected Config config;

	// Page info
	protected String location;
	private SpotAbstractLoginOperation loginOperation;
	final protected Topology topology;
	private Application application;
	final protected User user;

	// Info telling whether a refresh has been applied or force a reload
	protected boolean refreshed = false;
	boolean forceReload;

	// Timeouts
	private int timeout;
	private int openTimeout;
	private int shortTimeout;
	protected int delayBeforeLinkClick;
	protected int delayAfterLinkClick;

	// Count how many times we've tried to verify that the displayed username is correct
	private int verifyTries = 1;

	// NLS messages
	final private NlsMessages nlsMessages;

	// Additional information
	protected String[] data;

/**
 * Create an instance of the page using the given URL, configuration and user.
 * <p>
 * Using this constructor means that the page instance won't have any data.
 * </p><p>
 * On a concrete subclass, it's strongly recommended not to implement at the same time
 * this constructor and the constructor with data ({@link #WebPage(String, Config, User, String...)}).
 * Then framework can know whether it can automatically check some data in the page or not.
 * </p>
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 */
protected WebPage(final String url, final Config config, final User user) {

	// Initialize configuration, topology and configuration
	this.config = config;
	this.topology = config.getTopology();
	this.application = this.topology.getApplication(url);

	// Get page location and check whether login is necessary or not
	this.user = user;
	this.location = this.application.getPageUrlForUser(url, user);
	if (user == null || ((!this.topology.needLogin(this.location, user) && getApplication().isUserConnected(user)))) {
		this.loginOperation = null;
	} else {
		this.loginOperation = getLoginOperation(user);
	}

	// Init browser
	this.browser = BrowsersManager.getInstance().getBrowser(user);

	// Init timeouts to make their access faster and also allow each page to change them easily
	this.timeout = config.getDefaultTimeout();
	this.openTimeout = config.getOpenPageTimeout();
	this.shortTimeout = config.getShortTimeout();
	this.delayBeforeLinkClick = DELAY_BEFORE_CLICK_LINK_TIMEOUT;
	this.delayAfterLinkClick = DELAY_AFTER_CLICK_LINK_TIMEOUT;

	// Init NLS messages
	this.nlsMessages = initNlsMessages();
}

/**
 * Create an instance of the page using the given URL, configuration, user and data.
 * <p>
 * On a concrete subclass, it's strongly recommended not to implement both constructors
 * w/o data (see {@link #WebPage(String, Config, User)}).
 * With a unique page constructor, framework can know whether it should check some data
 * in the page or not.
 * </p>
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 * @param infos The page information to be stored
 */
protected WebPage(final String url, final Config config, final User user, final String... infos) {
	this(url, config, user);
	this.data = infos;
}

// TODO Move page creation to WebBrowser
protected static <P extends WebPage> P createPage(final String location, final Config config, final User user, final Class<P> pageClass) {
	return createPage(location, config, user, pageClass, (String[]) null);
}

// TODO Move page creation to WebBrowser
@SuppressWarnings("unchecked")
protected static <P extends WebPage> P createPage(final String location, final Config config, final User user, final Class<P> pageClass, final String... data) {
	String newlocation = hidePasswordInLocation(location);
	if (DEBUG) {
		debugPrintln("		+ Create page "+newlocation+ " for user "+user);
		debugPrintln("		  -> location: "+newlocation);
		debugPrintln("		  -> user: "+user);
		debugPrintln("		  -> class:    "+pageClass.getName());
	}

	// Get the page from cache
	String locationKey = location;
	BrowsersManager manager= config.getBrowserManager();
	WebBrowser browser = manager.getBrowser(user);
	P page = (P) browser.cachedPage(locationKey, user);

	// If page does not exist create it
	if (page == null) {
		page = createPageInstance(location, config, user, pageClass, data);
	} else {
		if (DEBUG) debugPrintln("		  - > found page at "+newlocation+": "+hidePasswordInLocation(page.location));
		if (page.getClass() != pageClass && page.getClass().getSuperclass() != pageClass) {
			throw new ScenarioFailedError("Unexpected page class '"+getClassSimpleName(page.getClass())+"', expecting '"+getClassSimpleName(pageClass)+"' instead.");
		}
		if (user != null && !user.equals(page.getUser())) {
			if (DEBUG) {
				String pageUserId = page.getUser() == null ? "no user" : page.getUser().getId();
				debugPrintln("		  - > change page user from "+pageUserId+" to "+user.getId());
			}
			page.login(user);
		}

		// Refresh page data
		page.data = data;
	}

	// Add page to history
	browser.cachePage(page);

	// Return the page
	return page;
}

// TODO Move page creation and cache to WebBrowser
@SuppressWarnings("unchecked")
protected static <P> P createPageInstance(final String location, final Config config, final User user, final Class<P> pageClass, final String... data) {
	Exception exception = null;
	Class<? extends User> userClass = user == null ? User.class : user.getClass();
	while (userClass.getSuperclass() != null) {
		Class<? extends Config> configClass = config.getClass();
		while (configClass.getSuperclass() != null) {
			try {
				if (data == null || data.length == 0) {
					Constructor<P> constructor = pageClass.getConstructor(String.class, configClass, userClass);
					return constructor.newInstance(location, config, user);
				}
				Constructor<P> constructor = pageClass.getConstructor(String.class, configClass, userClass, String[].class);
				return constructor.newInstance(location, config, user, data);
			}
			catch (NoSuchMethodException ex) {
				if (exception == null) {
					exception = ex;
				}
			}
			catch (InvocationTargetException ite) {
				throw new ScenarioFailedError(ite.getTargetException());
			}
			catch (Exception ex) {
				throw new ScenarioFailedError(ex);
			}
			configClass = (Class< ? extends Config>) configClass.getSuperclass();
		}
		userClass = (Class< ? extends User>) userClass.getSuperclass();
	}
	throw new ScenarioImplementationError(exception);
}

/* Cache has been moved to WebBrowser... */
///**
// * Clear web pages history.
// */
//public void clearHistory() {
//	this.browser.pagesCache.clear();
//}

//private List<WebPage> getPagesHistory() {
//	return PAGES_HISTORY.get();
//}

///**
// * Return the first page found in the history.
// *
// * @param location The page location
// * @param user The user associated with the page
// * @return The found page or <code>null</code> if none was found.
// */
//public WebPage searchPageInHistory(final String location, final User user) {
//	final List<WebPage> pagesHistory = this.browser.pagesCache;
//	int size = pagesHistory.size();
//	for (int i=size-1; i>=0; i--) {
//		WebPage page = pagesHistory.get(i);
//		String pageLocation = page.getLocation();
//		if (pageLocation.equals(location) && user.equals(page.getUser())) {
//			return page;
//		}
//	}
//	return null;
//}

///**
// * Return the current page displayed on the browser.
// * <p>
// * This is the last page of the internal cache.
// * </p>
// * @return The current page as a {@link WebPage}.
// */
//public static WebPage getCurrentPage() {
//	final List<WebPage> pagesHistory = getPagesHistory();
//	int size = pagesHistory.size();
//	if (size == 0) return null;
//	return pagesHistory.get(size-1);
//}

/*
 * Discarded as there's no way to know if user associated with found page
 * would match the expected one. If such need would appear, then it seems
 * that we'd need an additional user agrument to be able to peek the right page.
 */
///**
// * Seek the cache to find the instance of the given page class.
// * <p>
// * Note that this is a very expensive method which should not be used too
// * frequently.
// * </p><p>
// * TODO Replace the pages cache keys to store class instead location.
// *
// * @param pageClass The page class
// * @return The instance of the class associate with the give page page class or
// * <code>null</code> if this page hasn't been created yet.
// */
// TODO Move page creation and cache to WebBrowser
//@SuppressWarnings("unchecked")
//public static <P extends WebPage> P getPage(final Class<P> pageClass) {
//	if (DEBUG) debugPrintln("		+ Get web page for "+pageClass);
//
//	// Get the page from cache
//	Iterator<WebPage> pageInstances = getPagesHistory().iterator();
//	while (pageInstances.hasNext()) {
//		WebPage pageInstance = pageInstances.next();
//		if (pageInstance.getClass().equals(pageClass)) {
//			return (P) pageInstance;
//		}
//	}
//
//	// Return the found page
//	return null;
//}

/*
 * Discarded as there's no way to know if user associated with found page
 * would match the expected one. If such need would appear, then it seems
 * that we'd need an additional user agrument to be able to peek the right page.
 */
///**
// * Get from the cache the page instance for the given location.
// *
// * @param location The page location
// * @return The instance of the class associate with the page or <code>null</code>
// * if this page hasn't been created yet.
// */
//// TODO Move page creation and cache to WebBrowser
//public static WebPage getPage(final String location) {
//	if (DEBUG) debugPrintln("		+ get page "+location);
//
//	// Get the page from cache
//	WebPage page = searchPageInHistory(location);
//
//	// The page should have been found
//	if (page == null) {
//		if (DEBUG) debugPrintln("		  -> not found.");
//		int index = location.indexOf('&');
//		if (index > 0) {
//			String locationKey = location.substring(0, index);
//			if (DEBUG) debugPrintln("		  -> try with key="+locationKey);
//			page = searchPageInHistory(locationKey);
//		}
//	}
//
//	// Return the found page
//	return page;
//}

/*
 * Discarded as there's no way to find which browser would be concerned by following method.
 * If such need would appear, then it seems that we'd need an additional user agrument to be
 * able to use the right browser to open the page
 */
///**
// * Retrieve the existing page for the browser current URL. Create it if it's the first
// * time the page is requested.
// *
// * @return The instance of the class associate with the page or <code>null</code>
// * if no page location matching the browser url is found in the cache...
// */
//public static WebPage getPageUsingBrowser(final Config config) {
//	if (DEBUG) debugPrintln("		+ Get page using browser...");
//
//	// Get page url from browser
//	String currentUrl = config.getBrowser().getCurrentUrl();
//	if (DEBUG) debugPrintln("		  -> current URL:"+currentUrl);
//
//	// Adapt browser url to application
//	Application application = config.getTopology().getApplication(currentUrl);
//	String pageUrl = application == null ? currentUrl : application.getPageUrl(currentUrl);
//	if (DEBUG) debugPrintln("		  -> page URL:"+pageUrl);
//
//	// Look for a page location matching the page url in the cache
//	WebPage page = searchPageInHistory(pageUrl);
//	if (DEBUG) {
//		if (page == null) {
//			debugPrintln("		  -> no page was found!");
//		} else {
//			debugPrintln("		  -> page "+page+" was found.");
//		}
//	}
//	return page;
//}

/*
 * Discarded as there's no way to know if user associated with found page
 * would match the expected one. If such need would appear, then it seems
 * that we'd need an additional user agrument to be able to peek the right page.
 */
// TODO Move page creation and cache to WebBrowser
//public static WebPage openPage(final String location) {
//
//	// Get the page
//	WebPage page = getPage(location);
//	if (page == null) {
//		throw new ScenarioFailedError("The page with url '"+location+"' was not already created!");
//	}
//
//	// Get the page content
//	page.get();
//
//	// Return page
//	return page;
//}

/**
 * Reopen the given page.
 * <p>
 * This specific method is used when restarting the browser on the given page.
 * It can also be used to force the page reloading.
 * </p>
 * @param page The page to reopen
 * @param user The user associated with the page. It's necessary because the
 * login information of the provided page might have been reset prior the call...
 * @return The instance of the class associate with the page.
 */
public static WebPage reopenPage(final WebPage page, final SpotUser user) {
	debugPrintEnteringMethod("page", page.location, "user", user.getId());
	page.forceReload = true;
	WebPage reopenedPage = null;
	try {
		reopenedPage = openPage(page.location, USER_ACTION_NOT_PROVIDED, page.config, (User) user, page.getClass(), page.data);
	} finally {
		if (reopenedPage != null) {
			reopenedPage.forceReload = false;
		}
	}
	return reopenedPage;
}

/**
 * Retrieve the existing page for the given location. Create it if it is the first time
 * the page is requested.
 *
 * @param location The url of the page
 * @param config The config to use for the requested page
 * @param user The user to use on the requested page
 * @param pageClass The class associated with the page to open
 * @param data Additional CLM information to be stored in the page
 * @return The instance of the class associate with the page.
 */
// TODO Move page creation to WebBrowser
public static <P extends WebPage> P openPage(final String location, final Config config, final User user, final Class<P> pageClass, final String... data) {
	return openPage(location, USER_ACTION_NOT_PROVIDED, config, user, pageClass, data);
}

/**
 * Retrieve the existing page for the given location. Create it if it is the first time
 * the page is requested.
 *
 * @param location The url of the page
 * @param userAction The user action executed while opening the page
 * @param config The config to use for the requested page
 * @param user The user to use on the requested page
 * @param pageClass The class associated with the page to open
 * @param data Additional CLM information to be stored in the page
 * @return The instance of the class associate with the page.
 */
protected static <P extends WebPage> P openPage(final String location, final String userAction, final Config config, final User user, final Class<P> pageClass, final String... data) {

	// Create page
	P page = createPage(location, config, user, pageClass, data);

	// Set performance user action if any
	if (PERFORMANCE_ENABLED && !userAction.equals(USER_ACTION_NOT_PROVIDED)) {
		page.setPerfManagerUserActionName(userAction);
	}

	// Get the page content
	page.get();

	// Return page
	return page;
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
	perfManager.addPerfResult(pageTitle, getUrl());
}

/**
 * Set on the check-box found in the page using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the check-box is not found before {@link #timeout()} seconds</li>
 * <li>if the check-box is already checked, then nothing happen</li>
 * <li>validate that the check-box is well checked after having clicked on it</li>
 * </p>
 * @param locator The locator to find the check-box element in the current
 * page
 * @return The check-box web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #waitForElement(By)
 * @see WebBrowser#check(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement check(final By locator) {
	WebBrowserElement element = waitForElement(locator);
	this.browser.check(element, 1/*on*/, true/*validate*/);
	return element;
}

/**
 * Set or unset the check-box found inside the current page using the given locator.
 * <p>
 * The result of the check operation will be verified.
 * </p><p>
 * Note that this method can also be used to 'select' a radio button, i.e.,
 * when the argument 'on' is <code>true</code>. Technically, there is no way to
 * 'unselect' a radio button (without selecting some other radio button), so this
 * method should not be used with a radio button and <code>false</code>.
 * </p><p>
 * Note that it will fail:
 * <ul>
 * <li>if the check-box is not found before {@link #timeout()} seconds</li>
 * <li>if the check-box is not in the expected state after the operation</li>
 * </p>
 * @param locator The locator to find the check-box element in the current page
 * @param on Tells whether the check-box should be set or unset.
 * @return The check-box web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(WebBrowserElement, By)
 * @see WebBrowser#check(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement check(final By locator, final boolean on) {
	return check(locator, on, true/*validate*/);
}

/**
 * Set or unset the check-box found inside the current page using the given locator
 * and checking the result if specified.
 * <p>
 * Note that this method can also be used to 'select' a radio button, i.e.,
 * when the argument 'on' is <code>true</code>. Technically, there is no way to
 * 'unselect' a radio button (without selecting some other radio button), so this
 * method should not be used with a radio button and <code>false</code>.
 * </p><p>
 * Note that it will fail:
 * <ul>
 * <li>if the check-box is not found before {@link #timeout()} seconds</li>
 * <li>if the check-box is not in the expected state after the operation</li>
 * </p>
 * @param locator The locator to find the check-box element in the current page
 * @param on Tells whether the check-box should be set or unset.
 * @param validate Tells whether the validate check-box after the operation or not
 * @return The check-box web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(WebBrowserElement, By)
 * @see WebBrowser#check(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement check(final By locator, final boolean on, final boolean validate) {
	WebBrowserElement element = waitForElement(locator);
	this.browser.check(element, on ? 1 : -1, validate);
	return element;
}

/**
 * Set on the given check-box web element.
 * <p>
 * Note that:
 * <ul>
 * <li>if the check-box is already checked, then nothing happen</li>
 * <li>validate that the check-box is well checked after having clicked on it</li>
 * </p>
 * @param element The check-box to check
 * @return <code>true</code>If the check-box value has been changed,
 * <code>false</code> otherwise.
 *
 * @see WebBrowser#check(WebBrowserElement, int, boolean)
 */
protected boolean check(final WebBrowserElement element) {
	return this.browser.check(element, 1/*on*/, true/*validate*/);
}

/**
 * Set/Unset the given check-box web element .
 * <p>
 * Note that:
 * <ul>
 * <li>if the check-box is already in the given state, then nothing happen</li>
 * <li>validate that the check-box is well checked/unchecked after having clicked
 * on it</li>
 * </p>
 * @param element The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param on Tells whether set or unset the check-box
 * @return <code>true</code>If the check-box value has been changed,
 * <code>false</code> otherwise.
 *
 * @see #check(WebBrowserElement, By, int, boolean)
 */
protected boolean check(final WebBrowserElement element, final boolean on) {
	return this.browser.check(element, on ? 1 : -1, true/*validate*/);
}

/**
 * Set on the check-box found inside the given parent web element using
 * the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the check-box is not found before {@link #timeout()} seconds</li>
 * <li>if the check-box is already checked, then nothing happen</li>
 * <li>validate that the check-box is well checked after having clicked on it</li>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locator The locator to find the check-box element in the current
 * page
 * @return The check-box web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #check(WebBrowserElement, By, int, boolean)
 */
protected WebBrowserElement check(final WebBrowserElement parentElement, final By locator) {
	return check(parentElement, locator, 1/*on*/, true/*validate*/);
}

/**
 * Toggle the check-box found inside the given parent web element using
 * the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the check-box is not found before {@link #timeout()} seconds</li>
 * <li>the check-box is checked if it was unchecked and vice-versa</li>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locator The locator to find the check-box element in the current
 * page
 * @param toggle Tells whether the check-box should be toggled (0), set "on" (1)
 * or set "off" (-1). If any other values is specified then toggle (0) is assumed.
 * @param validate Tells whether to validate hat the check-box has the expected
 * state after having clicked on it</li>
 * @return The check-box web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #waitForElement(By, By)
 * @see WebBrowser#check(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement check(final WebBrowserElement parentElement, final By locator, final int toggle, final boolean validate) {
	WebBrowserElement element = waitForElement(parentElement, locator);
	this.browser.check(element, toggle, validate);
	return element;
}

/**
 * Check the hover title of the given link element.
 * <p>
 * This check opens the hover by positioning the mouse pointer over
 * the given link element and checks whether its title matches the given text.
 * </p>
 * @param <P> The expected class for the hover
 * @param linkElement The link on which to hover
 * @param hoverClass The expected class for the hover
 * @return The opened hover web element as {@link WebBrowserElement}
 * @throws ScenarioFailedError in following cases:
 * <ul>
 * <li>The hover is not found (typically when it fails to open)</li>
 * <li>The hover title is not found after {@link #shortTimeout()} (typically
 * when the hover is still empty when the timeout is reached)</li>
 * <li>The title does not match the expected one</li>
 * </ul>
 * TODO Should infer the hover class as done in ClmProjectAreaPageHelper#checkRichHover(...)
 */
public <P extends WebLinkHover<? extends WebPage>> P checkHoverTitle(final WebBrowserElement linkElement, final Class<P> hoverClass) {
	if (DEBUG) debugPrintln("		+ Check hover on "+linkElement+" using "+hoverClass);
	if (linkElement == null) {
		throw new ScenarioFailedError("Cannot hover on a null element.");
	}

	// Get hover
	P hover = hoverOverLink(linkElement, hoverClass);

	// Check the title
	if (!hover.getTitleElement().getText().equals(linkElement.getText())) {
		throw new ScenarioFailedError("Unexpected hover title.");
	}

	// Return the rich hover for further usage
	return hover;
}

/**
 * Check the rich hover of the given link element.
 * <p>
 * This check opens the rich hover by positioning the mouse pointer over
 * the given link element and perform checks on its content (typically the title).
 * </p>
 * @param <RH> The expected class for the hover
 * @param linkElement The link on which to hover
 * @param richHoverClass The expected class for the hover
 * @return The opened rich hover web element as {@link WebBrowserElement}
 * @throws ScenarioFailedError in following cases:
 * <ul>
 * <li>The rich hover is not found (typically when it fails to open)</li>
 * <li>The rich hover title is not found after {@link #shortTimeout()} (typically
 * when the hover is still empty when the timeout is reached)</li>
 * <li>The title does not match the expected one</li>
 * </ul>
 * TODO Should infer the hover class as done in ClmProjectAreaPageHelper#checkRichHover(...)
 */
public <RH extends WebRichHover<? extends WebPage>> RH checkRichHover(final WebBrowserElement linkElement, final Class<RH> richHoverClass, final String... pageData) {
	if (DEBUG) debugPrintln("		+ Check rich hover on "+linkElement+" using "+richHoverClass);
	if (linkElement == null) {
		throw new ScenarioFailedError("Cannot hover on a null element.");
	}

	// Get hover
	RH richHover = richHoverOverLink(linkElement, richHoverClass, pageData);

	// Check the hover
	richHover.check();

	// Return the rich hover for further usage
	return richHover;
}

/**
 * Click on the web element found in the current page using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * </p>
 * @param locator The locator to find the element in the current page
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(By)
 * @see WebBrowserElement#click()
 */
protected WebBrowserElement click(final By locator) {
	return click((By)null, locator);
}

/**
 * Click on the web element found relatively to the parent web element found
 * using the respective given locators.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if either the parent or the element are not found before
 * {@link #timeout()} seconds</li>
 * </p>
 * @param parentLocator The locator to find the parent element in the current
 * page, if <code>null</code>, the element will be searched in the entire page
 * content
 * @param locator The locator to find the element in the current page or
 * from the given parent element if not <code>null</code>
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(By, By)
 * @see WebBrowserElement#click()
 */
protected WebBrowserElement click(final By parentLocator, final By locator) {
	if (DEBUG) debugPrintln("		+ Click on "+(parentLocator==null ? "" : parentLocator+"//")+locator);
	WebBrowserElement parentElement = parentLocator == null ? null : waitForElement(parentLocator);
	return click(parentElement, locator);
}

/**
 * Click on the web element found using the given locator relatively to
 * the given parent web element.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if either the parent or the element are not found before
 * {@link #timeout()} seconds</li>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locator The locator to find the element in the current page
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(WebBrowserElement, By)
 * @see WebBrowserElement#click()
 */
public WebBrowserElement click(final WebBrowserElement parentElement, final By locator) {
	if (DEBUG) debugPrintln("		+ Click on "+parentElement+"//"+locator);

	// Store page title in case of perf result
	String pageTitle = PERFORMANCE_ENABLED ? getTitle() : null;

	// Wait for element
	WebBrowserElement element = waitForElement(parentElement, locator);

	// Click on given element
	element.click();

	// Add Performance result
	if (pageTitle != null) {
		addPerfResult(RegressionType.Client, pageTitle+ ": Action: " + locator);
	}

	// Return the found element
	return element;
}

/**
 * Click on the button found in the current page using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * <li>there's no verification that the button turns to enable after having clicked
 * on it</li>
 * </p>
 * @param buttonBy The locator to find the button in the current page
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(By)
 * @see WebBrowser#clickButton(WebBrowserElement, int, boolean)
 */
public WebBrowserElement clickButton(final By buttonBy) {
	WebBrowserElement button = waitForElement(buttonBy);
	return this.browser.clickButton(button, timeout(), false);
}

/**
 * Click on the button found in the current page using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * </p>
 * @param buttonBy The locator to find the button in the current page
 * @param check Tells whether to check the button turns disabled after having
 * been clicked or not.
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(By)
 * @see WebBrowser#clickButton(WebBrowserElement, int, boolean)
 */
public WebBrowserElement clickButton(final By buttonBy, final boolean check) {
	WebBrowserElement button = waitForElement(buttonBy);
	return this.browser.clickButton(button, timeout(), check);
}

/**
 * Click on the button found in the current page using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before the given timeout (in seconds)</li>
 * <li>there's no verification that the button turns to enable after having clicked
 * on it</li>
 * </p>
 * @param buttonBy The locator to find the button in the current page
 * @param time_out The time (in seconds) to wait before giving up the research
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(By)
 * @see WebBrowser#clickButton(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement clickButton(final By buttonBy, final int time_out) {
	WebBrowserElement button = waitForElement(buttonBy, true/*fail*/, time_out);
	return this.browser.clickButton(button, time_out, false);
}

/**
 * Click on the given button.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * </p>
 * @param button The button in the current page
 * @param check Tells whether to check the button turns disabled after having
 * been clicked or not.
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see WebBrowser#clickButton(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement clickButton(final WebBrowserElement button, final boolean check) {
	return this.browser.clickButton(button, timeout(), check);
}

/**
 * Click on the button found relatively to the given parent web element.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * <li>there's no verification that the button turns to enable after having clicked
 * on it</li>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param buttonBy The locator to find the button in the current page
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(WebBrowserElement, By)
 * @see WebBrowser#clickButton(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement clickButton(final WebBrowserElement parentElement, final By buttonBy) {
	WebBrowserElement button = waitForElement(parentElement, buttonBy);
	return this.browser.clickButton(button, timeout(), false);
}

/**
 * Click on the button found relatively to the given parent web element.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param buttonBy The locator to find the button in the current page
 * @param check Tells whether to check the button turns disabled after having
 * been clicked or not.
 * @return The web element (as a {@link WebBrowserElement}) found in the page
 *
 * @see #waitForElement(WebBrowserElement, By)
 * @see WebBrowser#clickButton(WebBrowserElement, int, boolean)
 */
protected WebBrowserElement clickButton(final WebBrowserElement parentElement, final By buttonBy, final boolean check) {
	WebBrowserElement button = waitForElement(parentElement, buttonBy);
	return this.browser.clickButton(button, timeout(), check);
}

/**
 * Click on a link element found in the current page using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * <li>if the link opens a new handle, then it automatically switches to it</li>
 * </p>
 * @param locator The locator to find the element in the current page
 */
protected void clickOnLink(final By locator) {

	// Get current handles
	Set<String> handles = this.browser.getWindowHandles(/*check:*/false);

	// Click on link
	click((By)null, locator);
	pause(250);

	// Switch to the new handle if one has been created
	Set<String> newHandles = this.browser.getWindowHandles(/*check:*/false);
	if (newHandles.size() > handles.size()) {
		for (String browserHandle: newHandles) {
			if (!handles.contains(browserHandle)) {
				this.browser.switchToHandle(browserHandle);
				break;
			}
		}
	}
}

@Override
public boolean equals(final Object o) {
	if (o instanceof WebPage) {
		WebPage p = (WebPage) o;
		return this.location.equals(p.location);
	}
	return false;
}

/**
 * Find an element in the current browser page for the given locator.
 *
 * @param locator The way to find the element in the page (see {@link By}).
 * @return The found element or <code>null</code> if the element was not found.
 * @see WebBrowser#findElement(By)
 */
protected WebBrowserElement findElement(final By locator) {
	return this.browser.findElement(locator);
}

/**
 * Get the page content.
 *
 * @return The page instance
 */
public final WebPage get() {
	if (DEBUG) {
		debugPrintln("		+ get page content ("+hidePasswordInLocation(this.location)+")");
		debugPrintln("		  -> browser URL: "+hidePasswordInLocation(this.browser.getCurrentUrl()));
		debugPrintln("		  -> current user: "+getUser());
	}

	// Do nothing if the page is already loaded
	if (!this.forceReload && this.loginOperation == null && isLoaded()) {
		if (DEBUG) {
			debugPrintln("		  -> page was already loaded");
			debugPrintln("		  -> page URL: "+hidePasswordInLocation(getUrl()));
		}

		// Set page handle
		this.handle = this.browser.mainWindowHandle;

		// Add performances result
		if (PERFORMANCE_ENABLED) {
			addPerfResult(RegressionType.Server, getTitle());
		}

		// Verify user
		verifyPageUser();

		// Returned opened page
		return this;
	}

	// Get page content
	long start = System.currentTimeMillis();
	if (DEBUG) {
		debugPrintln("		  -> loading page...");
		debugPrintln("		  -> page URL: "+hidePasswordInLocation(getUrl()));
	}

	// Start server timer
	if (PERFORMANCE_ENABLED) {
		startPerfManagerServerTimer();
	}

	// Load the web page
	load();

	// Verify user
	verifyPageUser();

	// Add performance result
	if (PERFORMANCE_ENABLED) {
		addPerfResult(RegressionType.Server, getTitle());
	}

	// Returned opened page
	if (DEBUG) {
		debugPrintln("		  -> page loaded in "+elapsedTimeString(start));
		debugPrintln("		  -> browser URL: "+hidePasswordInLocation(this.browser.getCurrentUrl()));
		debugPrintln("		  -> page URL: "+hidePasswordInLocation(getUrl()));
	}
	return this;
}

/**
 * Return the application associated with the current page.
 *
 * @return The application as subclass of {@link Application}
 * @see Topology#getApplication(String)
 */
public Application getApplication() {
	if (this.application == null) {
		this.application = this.topology.getApplication(this.location);
	}
	return this.application;
}

WebBrowserElement getBodyElement() {
	return waitForElement(By.tagName("body"));
}

/**
 * Return the browser associated with the current page.
 *
 * @return The browser as {@link WebBrowser}
 */
public WebBrowser getBrowser() {
	return this.browser;
}

/**
 * Return the configuration associated with the current page.
 *
 * @return The configuration as {@link Config}
 */
public Config getConfig() {
	return this.config;
}

/**
 * Return the value of the <code>document.title</code>.
 *
 * @return The page document title
 */
public String getDocumentTitle() {
	return this.browser.getTitle();
}

/**
 * Return the page location used when creating it.
 *
 * @return The page location
 */
@Override
public String getLocation() {
	return this.location;
}

/**
 * Return the page location used when creating it
 * as a {@link URL}.
 *
 * @return The page location url
 */
public URL getLocationUrl() {
	try {
		return new URL(this.location);
	} catch (MalformedURLException mue) {
		throw new ScenarioFailedError(mue, /*print:*/true);

	}
}

private WebBrowserElement getLoggedUserElement(final boolean fail, final int waitTimeout) throws WaitElementTimeoutError {
	debugPrintEnteringMethod("fail", fail, "waitTimeout", waitTimeout);
	By userElementLocator = getLoggedUserElementLocator();
	if (userElementLocator == null) {
		return null;
	}
	return waitForElement(userElementLocator, fail, waitTimeout);
}

/**
 * Return the locator of the web element displaying the logged user name.
 *
 * @return The user element locator or <code>null</code> if no user is logged
 * on the page or no user information is accessible on the page
 */
abstract protected By getLoggedUserElementLocator();

/**
 * Return a login operation instance dedicated to the current page and given user.
 *
 * @param pageUser The user to be used for the login
 * @return The login operation
 */
final protected SpotAbstractLoginOperation getLoginOperation(final User pageUser) {
	return getApplication().getLoginOperation(this, pageUser);
}

/**
 * Return the NLS messages manager.
 *
 * @return The NLS message or <code>null</code> if page does not manage NLS messages
 */
protected NlsMessages getNlsMessages() {
	return this.nlsMessages;
}

/**
 * Get the root web element of the current web page.
 *
 * @return The root element as a {@link WebBrowserElement}.
 */
protected WebBrowserElement getRootElement() {
	this.browser.resetFrame();
	return waitForElement(getRootElementLocator());
}

/**
 * Return the locator for the root web element of the current web page.
 *
 * @return The locator as a {@link By}.
 */
protected abstract By getRootElementLocator();

/**
 * {@inheritDoc}
 * <p>
 * By default page title is the value of the <code>document.title</code>.
 * </p>
 */
@Override
public String getTitle() {
	return getDocumentTitle();
}

/**
 * Return the current test topology that the current page belongs to.
 *
 * @return The topology as a {@link Topology}
 */
protected Topology getTopology() {
	return this.topology;
}

/**
 * Return the URL of the page loaded in the browser.
 *
 * @return The browser URL as a {@link String}
 * @see WebBrowser#getCurrentUrl()
 */
public final String getUrl() {
	final String currentUrl = this.browser.getCurrentUrl();
	return getTopology().getPageUrl(currentUrl);
}

/**
 * Return the user used when the page was loaded.
 *
 * @return The {@link User user}.
 */
public User getUser() {
	return this.user;
}

/**
 * Move back a single "item" in the browser's history.
 *
 * @see WebBrowser#back()
 */
public WebPage goBack() {
	debugPrintEnteringMethod();
	return this.browser.back();
}

@Override
public int hashCode() {
	return this.location.hashCode();
}

/**
 * Perform a mouse hovering over the given link element.
 *
 * @param <H> The hover specialized class
 * @param linkElement The web element on which to hover
 * @param hoverClass The hover specialized class
 * @return The opened hover as the given specialized class
 */
@SuppressWarnings("unchecked")
protected <H extends WebLinkHover<? extends WebPage>> H hoverOverLink(final WebBrowserElement linkElement, final Class<H> hoverClass) {

	// Create the hover window
	H hover;
	try {
		Constructor<? extends WebLinkHover<? extends WebPage>> constructor = hoverClass.getConstructor(WebPage.class);
		hover = (H) constructor.newInstance(this);
	}
	catch (Exception ex) {
		throw new ScenarioFailedError(ex);
	}

	// Hover over the link element
	hover.open(linkElement);

	// Return the created hover
	return hover;
}

/**
 * Initialize and return the NLS messages instance.
 * <p>
 * By default no NLS support is provided in web pages. Each subclass needs
 * to override this method to return their own NLS messages class in order to
 * have convienent getters of the supported NLS strings.
 * </p>
 * @return A NLS messages instance as a subclass of {@link NlsMessages}.
 */
protected NlsMessages initNlsMessages() {
	return null;
}

/**
 * Return whether the page is currently loaded or not.
 * <p>
 * By default, check whether the current URL (see {@link #getUrl()}) starts with
 * the page location or not.
 * </p><p>
 * Subclasses might want (or need) to override this default behavior. However,
 * it's strongly recommended to always call this super implementation to be sure
 * that browser URL is still checked.
 * </p>
 * @return <code>true</code> if the page is already loaded, <code>false</false> otherwise.
 */
protected boolean isLoaded() {
	return matchBrowserUrl();
}

/**
 * Load the current page into the browser.
 * <p>
 * By default, it loads the current page location into the browser (see
 * {@link WebBrowser#get(WebPage)}.
 * </p><p>
 * It also performs the login operation if there's a user stored in
 * the {@link #loginOperation} is set.
 * </p><p>
 * Subclasses might want (or need) to override this default behavior to add
 * specific load operations. However, it's <b>strongly recommended</> to call
 * this root level implementation to be sure that browser will actually perform
 * the load operation.
 * </p>
 */
protected void load() {
	debugPrintEnteringMethod();

	/* TODO Improve this test as it would have side effect when re-running tests if it was activated...
	// Do not set the browser location already matches current page one
	if (!this.location.equals(this.browser.location) || this.loginUser != null) {
		this.browser.get(this.location); // TODO replace with: this.browser.get(this);
	} else {
		if (DEBUG) debugPrintln("INFO: Page '"+this.location+"' is already the browser location, hence do call WebBrowser.get(String) method to avoid an unnecessary page reload...");
	}
	 */

	// Load the page in the browser
	this.browser.get(this);

	// Login if necessary
	if (this.loginOperation == null) {
		// No direct login is necessary but we need to store user in application if it uses basic auth
		if (this.application.getUserInfo() != null && this.user != null) {
			debugPrintln("		  -> directly login user "+this.user.getId()+" to application");
			this.application.login(this.user);
		}
	} else {
		debugPrintln("		  -> login necessary while loading the page");
		this.loginOperation.performLogin();
		this.loginOperation = null;
	}

	// Wait initial loading
	waitInitialPageLoading();
}

/**
 * Login the page from current user to the given user.
 * <p>
 * Nothing happen if the current user is already logged in.
 * In the contrary, the current user is logged out prior
 * the login operation with the given user.
 * </p>
 * @param pageUser The user to log in.
 * @return <code>true</code> if the login operation occurred,
 * <code>false</code> if it was a no-op
 */
public boolean login(final User pageUser) {
	return login(pageUser, false);
}

/**
 * Login the page from current user to the given user.
 * <p>
 * Nothing happen if the current user is already logged in
 * except if a <i>forced</i> login is requested.
 * In case the login operation with the given user will occur,
 * then the current user is logged out before.
 * </p>
 * @param pageUser The user to log in.
 * @param force Force the user login, even if it's already logged
 * @return <code>true</code> if the login operation occurred,
 * <code>false</code> if it was a no-op
 */
public boolean login(final User pageUser, final boolean force) {

	// Current user
	User currentUser = getUser();

	// Login if necessary
	if (force || this.topology.needLogin(this.location, pageUser)) {

		// Check whether the current page is on the expected user or not
		WebBrowserElement userProfileElement;
		if (!force) {
			userProfileElement = getLoggedUserElement(false/*fail*/, 2/*sec*/);
			if (userProfileElement != null && matchDisplayedUser(pageUser, userProfileElement)) {
				return true;
			}
		}

		// Logout if necessary
		if (currentUser != null) {
			performLogout();
		}

		// Set user login
		this.loginOperation = getLoginOperation(pageUser);

		// Get page content (the login operation will happen there...)
		get();

		// Check that new user is well displayed in the page
		userProfileElement = getLoggedUserElement(true/*fail*/, timeout());
		if (!matchDisplayedUser(pageUser, userProfileElement)) {
			println("WARNING: User name '"+userProfileElement.getText()+"' does not match expected one: '"+pageUser.getName()+"'");
		}

		// Return that user has changed
		return true;
	}

	// No real login
	return false;

}

/**
 * Perform a logout operation.
 */
public void logout() {
	performLogout();
	this.application.logout(this.user);
}

/**
 * Return whether the current page location matches the browser URL or not.
 * <p>
 * That basically looks whether the browser URL starts with the location after
 * having replaced "%20" characters by space. But that might be refined by
 * subclass to more subtle match.
 * </p><p>
 * At this general level, it also try to see if there's an id (ie. "&id=") in both
 * addresses and , if it's the case, compare them.
 * </p>
 * @return <code>true</code> if the location and the url match, <code>false</code>
 * otherwise.
 */
protected boolean matchBrowserUrl() {
	String pageUrl = getUrl();
	if (DEBUG) {
		debugPrintln("		+ Test whether page location matches current browser URL or not.");
		debugPrintln("		  -> page url: "+hidePasswordInLocation(pageUrl));
		debugPrintln("		  -> page location: "+hidePasswordInLocation(this.location));
	}

	// Special case when restarting the browser after it has died
	if (pageUrl.equals("about:blank") || pageUrl.startsWith("data:")) {
		debugPrintln("		  -> browser has a blank page => NO MATCH");
		return false;
	}

	// Compare URL starts
	URL pageUrlURL = null;
	URL pageLocationURL = null;
	String pageUrlPath = null;
	String pageLocationPath = null;
	try {
		pageUrlURL = new URL(URLDecoder.decode(pageUrl, "UTF-8"));
		pageLocationURL = new URL(URLDecoder.decode(this.location, "UTF-8"));
		if (DEBUG) {
			debugPrintln("		  -> browser URL: "+hidePasswordInLocation(pageUrlURL.toString()));
			debugPrintln("		  -> page URL: "+hidePasswordInLocation(pageLocationURL.toString()));
		}
		if (!pageUrlURL.getProtocol().equals(pageLocationURL.getProtocol()) ||
				!pageUrlURL.getHost().equals(pageLocationURL.getHost()) ||
				pageUrlURL.getPort() != pageLocationURL.getPort()) {
			if (DEBUG) debugPrintln("		  -> NO MATCH: protocol, host or port does not match");
			return false;
		}
		pageUrlPath = pageUrlURL.getPath();
		pageLocationPath = pageLocationURL.getPath();
		if (!pageUrlPath.startsWith(pageLocationPath)) {
			if (DEBUG) debugPrintln("		  -> NO MATCH: browser URL path does not start with page URL path");
			return false;
		}
	}
	catch (MalformedURLException mue) {
		throw new ScenarioFailedError(mue);
	}
	catch (UnsupportedEncodingException uee) {
		throw new ScenarioFailedError(uee);
	}

	// If fragments are the same then return now
	String pageUrlRef = pageUrlURL.getRef();
	if (pageUrlRef != null && pageUrlRef.equals(pageLocationURL.getRef())) {
		if (DEBUG) debugPrintln("		  -> MATCH: browser URL reference matches page URL reference");
		return true;
	}

	// If page location has no fragment, then we expect the path to be equals
	String pageLocationRef = pageLocationURL.getRef();
	if (pageLocationRef == null && !pageLocationPath.equals(pageUrlPath)) {
		if (DEBUG) debugPrintln("		  -> NO MATCH: page has no fragment and pathes are not the same");
		return false;
	}

	// Check IDs if possible
	String pageUrlString = pageUrlURL.toString();
	int idx1 = -1, idx2 = -1;
	if ((idx1 = pageUrlString.indexOf("&id=")) > 0 && (idx2 = this.location.indexOf("&id=")) > 0) {
		String browserUrlID = pageUrlString.substring(idx1 + 4);
		if ((idx1 = browserUrlID.indexOf('&')) > 0) {
			browserUrlID = browserUrlID.substring(0, idx1);
		}
		String locationID = this.location.substring(idx2 + 4);
		if ((idx2 = locationID.indexOf('&')) > 0) {
			locationID = locationID.substring(0, idx2);
		}
		if (browserUrlID.equals(locationID)) {
			if (DEBUG) debugPrintln("		  -> MATCH: ID in browser URL matches ID in page URL");
			return true;
		}
	}

	// Remove vvc configuration and component from browser URLs
	// remove vvc first
	pageUrlString = stripBrowserUrl(pageUrlString);

	// Check that browser URL at least starts with page location
	String pageUrlFromLocation = getTopology().getPageUrl(this.location).replaceAll("%20", SPACE_STRING);
	try {
		pageUrlFromLocation = URLDecoder.decode(pageUrlFromLocation, "UTF-8");
	} catch (@SuppressWarnings("unused") final UnsupportedEncodingException e) {
		// skip
	}
	pageUrlFromLocation = stripBrowserUrl(pageUrlFromLocation);
	pageUrlString = pageUrlString.replaceAll("%20", SPACE_STRING);
	boolean match = pageUrlString.startsWith(pageUrlFromLocation);
	if (DEBUG) {
		if (match) {
			debugPrintln("		  -> MATCH: page URL ("+hidePasswordInLocation(pageUrlFromLocation)+") matches page location ("+hidePasswordInLocation(pageUrlFromLocation)+")");
		} else {
			debugPrintln("		  -> NO MATCH: page URL ("+hidePasswordInLocation(pageUrlString)+") does not match page location ("+hidePasswordInLocation(pageUrlFromLocation)+")");
		}
	}
	return match;
}

/**
 * Strip some section from the browser url.
 * <p>
 * Do nothing by default, subclasses might override to remove some
 * part of the URL which should not be used while comparing page URLs
 * </p>
 * @param browserUrl The url string.
 * @return String Stripped down version of url.
 */
protected String stripBrowserUrl(final String browserUrl) {
	return browserUrl;
}

/**
 * Return whether the displayed user matches the user name or not.
 * <p>
 * Note that this method is only called when page has a logged in a user.
 * </p>
 * @param pageUser The user to check name display
 * @param loggedUserElement The web element displaying user name in the page.
 * @return <code>true</code> if the displayed user matches the page user name,
 * <code>false</code> otherwise.
 */
abstract protected boolean matchDisplayedUser(User pageUser, WebBrowserElement loggedUserElement);

/**
 * Park the mouse at a location where links are not found in order to prevent unexpected rich hovers
 * from loading by accident. The parking zone used for this purpose is the (0,0) location of
 * the <body> element, which is the top-left corner of the display area of the browser.
 */
public void parkMouse() {
	this.browser.moveToElement(getBodyElement(), 0, 0);
}

/**
 * Logout the page from current user to new user.
 */
protected abstract void performLogout();

/**
 * Helper method to open a page and wait until it's finished loading.
 *
 * <p>
 * Note that the page location is directly inferred from the browser URL
 * modulo application possible modification (see {@link Application#getPageUrl(String)}).
 * </p>
 * @param pageClass The class associated with the page to open
 * @param waiting Tells whether wait for the initial loading or not
 * @param pageData Additional CLM information to be stored in the page
 * @return The instance of the class associate with the page.
 */
private <P extends WebPage> P openAndWaitForPage(final Class<P> pageClass, final boolean waiting, final String... pageData) {

	// Wait that at least 'Loading...' appears in the page
	if (waiting) {
		waitInitialPageLoading(false /* throwError */);
	}

	// Open the page and for it being loaded
	P page = openPage(getUrl(), USER_ACTION_NOT_PROVIDED, this.config, getUser(), pageClass, pageData);
	page.waitForLoadingPageEnd();

	// Return the opened page
	return page;
}

/**
 * Helper method to open a page and wait until it's finished loading.
 *
 * @param pageLocation The page location
 * @param newConfig The config to open the page with
 * @param newUser The user to open the page with
 * @param pageClass TThe class associated with the page to open
 * @param pageData Additional CLM information to be stored in the page
 * @return The instance of the class associate with the page.
 */
public <P extends WebPage> P openAndWaitForPage(final String pageLocation, final SpotConfig newConfig, final SpotUser newUser, final Class<P> pageClass, final String... pageData) {

	// Open the page and for it being loaded
	P page = openPage(pageLocation, USER_ACTION_NOT_PROVIDED, (Config) newConfig, (User) newUser, pageClass, pageData);
	page.waitForLoadingPageEnd();

	// Return the opened page
	return page;
}

/**
 * Open a menu by clicking on the link element found using the given locator.
 * <p>
 * Note that the opened menu is found with the given menu locator.
 * </p>
 * @param menuLocator The locator to find the menu when it will be opened
 * @param linkLocator The locator to find the web element to open the menu
 * @return The opened menu
 */
public WebMenu openMenu(final By linkLocator, final By menuLocator) {
	return openMenu(linkLocator, menuLocator, WebMenu.class);
}

/**
 * Open a menu by clicking on the link element found using the given locator.
 * <p>
 * Note that the opened menu is found with the given menu locator.
 * </p>
 * @param linkLocator The locator to find the web element to open the menu
 * @param menuLocator The locator to find the menu when it will be opened
 * @param menuClass The class of the opened menu
 * @return The opened menu
 */
public <M extends WebMenu> M openMenu(final By linkLocator, final By menuLocator, final Class<M> menuClass) {
	debugPrintEnteringMethod("menuLocator", menuLocator.toString(), " linkLocator", linkLocator.toString(), "menuClass", getClassSimpleName(menuClass));

	// Get link element to open the menu
	WebBrowserElement linkElement = waitForElement(linkLocator, false/* do not fail */, openTimeout(), true/* displayed */, false/* first occurrence */);
	if (linkElement == null) {
		throw new WaitElementTimeoutError("Cannot find web element '" + linkLocator + "' to open menu.");
	}

	// Return a new menu instance
	return openMenu(linkElement, menuLocator, menuClass);
}

/**
 * Open a menu by clicking on the given link element.
 * <p>
 * Note that the opened menu is found with the given menu locator.
 * <p>
 * @param linkElement The web element to open the menu
 * @param menuLocator The locator to find the menu when it will be opened
 * @param menuClass The class of the opened menu
 * @return The opened menu
 */
public <M extends WebMenu> M openMenu(final WebBrowserElement linkElement, final By menuLocator, final Class<M> menuClass) {
	debugPrintEnteringMethod("locator", menuLocator.toString(), " linkElement", linkElement.toString(), "menuClass", getClassSimpleName(menuClass));
	// Open the menu and return the created object
	try {
		M menu = SpotWindowFactory.createInstance(this, menuLocator, menuClass);
		menu.open(linkElement);
		return menu;
	}
	catch (Exception ex) {
		throw new RuntimeException(ex);
	}
}

/**
 * Retrieve the existing page for the browser current URL. Create it if it's the first
 * time the page is requested.
 *
 * @param pageClass The class associated with the page to open
 * @param pageData Additional CLM information to be stored in the page
 * @return The instance of the class associate with the page.
 */
public <P extends WebPage> P openPageUsingBrowser(final Class<P> pageClass, final String... pageData) {

	// Notify the perfManager that a page is loading
	if (PERFORMANCE_ENABLED) this.browser.getPerfManager().setPageLoading(true);

	// Open, wait for, and return the opened page
	return openAndWaitForPage(pageClass, true, pageData);
}

/**
 * Retrieve the existing page for the browser current URL. Create it if it's the first
 * time the page is requested.
 *
 * @param pageClass The class associated with the page to open
 * @param pageData Additional CLM information to be stored in the page
 * @return The instance of the class associate with the page.
 */
public <P extends WebPage> P openPageUsingBrowserWithoutWaiting(final Class<P> pageClass, final String... pageData) {

	// Notify the perfManager that a page is loading
	if (PERFORMANCE_ENABLED) this.browser.getPerfManager().setPageLoading(true);

	// Open, wait for, and return the opened page
	return openAndWaitForPage(pageClass, false, pageData);
}

/**
 * Click on the given hover title to open a new page.
 * <p>
 * Note that the browser url after having clicked on the hover title will be used
 * for the page location.
 * </p>
 * @param hover The hover on which to click on title
 * @param openedPageClass The class associated with the opened page
 * @param pageData Provide additional information to store in the page when opening it
 * @return The web page (as a subclass of {@link WebPage}) opened after
 * having clicked on the link
 *
 * @see #openPage(String, Config, User, Class, String...)
 * @see WebBrowserElement#click()
 */
public <P extends WebPage> P openPageUsingHoverTitle(final WebRichHover<? extends WebPage> hover, final Class<P> openedPageClass, final String... pageData) {
	if (DEBUG) debugPrintln("		+ Click to hover title '"+hover.getTitleElement()+"' to open "+openedPageClass.getName());

	// Click on hover title and close it
	hover.getTitleElement().click();
	hover.close();

	// Open, wait for, and return the opened page
	return openAndWaitForPage(openedPageClass, true, pageData);
}

/**
 * Click on the link found using the given locator assuming that will open
 * a new {@link WebPage page}.
 * <p>
 * The opened page URL is got from the application which usually takes it from
 * the <code>href</code> attribute of the link web element (see
 * {@link Topology#getPageUrl(String)}.
 * </p><p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * <li>no additional info is provided to the opened page</li>
 * </p>
 * @param linkLocator The locator to find the link element in the current page
 * @param pageUser The page user
 * @param openedPageClass The class associated with the opened page
 * @return The web page (as a subclass of {@link WebPage}) opened after
 * having clicked on the link
 *
 * @see #openPageUsingLink(WebBrowserElement, By, User, Class, boolean, int, String...)
 */
public <P extends WebPage> P openPageUsingLink(final By linkLocator, final User pageUser, final Class<P> openedPageClass, final String... info) {
	return openPageUsingLink(null, linkLocator, pageUser, openedPageClass, true/*fail*/, timeout(), info);
}

/* Discarded as it's not used. That also reduces the number of methods with same name but different signature... */
///**
// * Click on the link found using the given locator assuming that will open
// * a new {@link WebPage page}.
// * <p>
// * The opened page URL is got from the application which usually takes it from
// * the <code>href</code> attribute of the link web element (see
// * {@link Topology#getPageUrl(String)}.
// * </p><p>
// * Note that:
// * <ul>
// * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
// * <li>no additional info is provided to the opened page</li>
// * </p>
// * @param linkBy The locator to find the link element in the current page
// * @param openedPageClass The class associated with the opened page
// * @param timeOut Seconds to wait before giving up if the web element is not
// * found.
// * @return The web page (as a subclass of {@link WebPage}) opened after
// * having clicked on the link
// *
// * @see #openPageUsingLink(WebBrowserElement, By, Class, boolean, int, String...)
// */
//public <P extends WebPage> P openPageUsingLink(final By linkBy, final Class<P> openedPageClass, final int timeOut) {
//	return openPageUsingLink(null, linkBy, openedPageClass, false/*do not fail*/, timeOut);
//}

/* Discarded as it's not used. That also reduces the number of methods with same name but different signature... */
///**
// * Click on the link found using the given locator assuming that will open
// * a new {@link WebPage page}.
// * <p>
// * The opened page URL is got from the application which usually takes it from
// * the <code>href</code> attribute of the link web element (see
// * {@link Topology#getPageUrl(String)}.
// * </p><p>
// * Note that:
// * <ul>
// * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
// * </p>
// * @param linkLocator The locator to find the link element in the current page
// * @param openedPageClass The class associated with the opened page
// * @param info Provide additional information to store in the page when opening it
// * @return The web page (as a subclass of {@link WebPage}) opened after
// * having clicked on the link
// *
// * @see #openPageUsingLink(WebBrowserElement, By, Class, boolean, int, String...)
// */
//public <P extends WebPage> P openPageUsingLink(final By linkLocator, final Class<P> openedPageClass, final String... info) {
//	return openPageUsingLink(null, linkLocator, openedPageClass, true/*fail*/, timeout(), info);
//}

/* Discarded as it's not used. That also reduces the number of methods with same name but different signature... */
///**
// * Click on the link found using the given locator assuming that will open
// * a new {@link WebPage page}.
// * <p>
// * The opened page URL is got from the application which usually takes it from
// * the <code>href</code> attribute of the link web element (see
// * {@link Topology#getPageUrl(String)}.
// * </p><p>
// * Note that:
// * <ul>
// * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
// * <li>no additional info is provided to the opened page</li>
// * </p>
// * @param parentElement The parent element where to start to search from,
// * if <code>null</code>, then search in the entire page content
// * @param linkBy The locator to find the link element in the current page
// * @param openedPageClass The class associated with the opened page
// * @return The web page (as a subclass of {@link WebPage}) opened after
// * having clicked on the link
// *
// * @see #openPageUsingLink(WebBrowserElement, By, Class, boolean, int, String...)
// */
//public <P extends WebPage> P openPageUsingLink(final WebBrowserElement parentElement, final By linkBy, final Class<P> openedPageClass) {
//	return openPageUsingLink(parentElement, linkBy, openedPageClass, true/*fail*/, timeout());
//}

/**
 * Click on the link found using the given locator assuming that will open
 * a new {@link WebPage page}.
 * <p>
 * The opened page URL is got from the application which usually takes it from
 * the <code>href</code> attribute of the link web element (see
 * {@link Topology#getPageUrl(String)}.
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param linkLocator The locator to find the link element in the current page
 * @param pageUser The page user
 * @param openedPageClass The class associated with the opened page
 * @param fail Tells whether to fail if none of the elements is find before timeout
 * @param time_out The time to wait before giving up the research
 * @param info Provide additional information to store in the page when opening it
 * @return The web page (as a subclass of {@link WebPage}) opened after
 * having clicked on the link
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 * @see #openPageUsingLink(WebBrowserElement, User, Class, String...)
 * @see WebBrowserElement#click()
 */
public <P extends WebPage> P openPageUsingLink(final WebBrowserElement parentElement, final By linkLocator, final User pageUser, final Class<P> openedPageClass, final boolean fail, final int time_out, final String... info) {
	WebBrowserElement linkElement = this.browser.waitForElement(parentElement, linkLocator, fail, time_out, true/*visible*/, true/*single element expected*/);
	if (linkElement == null) return null;
	return openPageUsingLink(linkElement, pageUser, openedPageClass, info);
}

/**
 * Click on the given link assuming that will open a new page.
 * <p>
 * The opened page URL is got from the application (see
 * {@link Topology#getPageUrl(String)}) which usually takes
 * it from the <code>href</code> attribute of the link web element.
 * </p>
 * @param linkElement The link on which to click
 * @param openedPageClass The class associated with the opened page
 * @param pageData Provide additional information to store in the page when opening it
 * @return The web page (as a subclass of {@link WebPage}) opened after
 * having clicked on the link
 */
public <P extends WebPage> P openPageUsingLink(final WebBrowserElement linkElement, final Class<P> openedPageClass, final String... pageData) {
	return openPageUsingLink(linkElement, getUser(), openedPageClass, pageData);
}

/**
 * Click on the given link assuming that will open a new page.
 * <p>
 * The opened page URL is got from the application (see
 * {@link Topology#getPageUrl(String)}) which usually takes
 * it from the <code>href</code> attribute of the link web element.
 * </p>
 * @param linkElement The link on which to click
 * @param pageUser The page user
 * @param openedPageClass The class associated with the opened page
 * @param pageData Provide additional information to store in the page when opening it
 * @return The web page (as a subclass of {@link WebPage}) opened after
 * having clicked on the link
 */
public <P extends WebPage> P openPageUsingLink(final WebBrowserElement linkElement, final User pageUser, final Class<P> openedPageClass, final String... pageData) {
	return openPageUsingLink(linkElement, this.config, pageUser, openedPageClass, pageData);
}

/**
 * Click on the given link assuming that will open a new page.
 * <p>
 * The opened page URL is got from the application (see
 * {@link Topology#getPageUrl(String)}) which usually takes
 * it from the <code>href</code> attribute of the link web element.
 * </p>
 * @param linkElement The link on which to click
 * @param pageConfig The page config
 * @param pageUser The page user
 * @param openedPageClass The class associated with the opened page
 * @param pageData Provide additional information to store in the page when opening it
 * @return The web page (as a subclass of {@link WebPage}) opened after
 * having clicked on the link
 */
@SuppressWarnings("unchecked")
public <P extends WebPage> P openPageUsingLink(final WebBrowserElement linkElement, final Config pageConfig, final User pageUser, final Class<P> openedPageClass, final String... pageData) {
	if (DEBUG) debugPrintln("		+ Click to link "+linkElement+ " to open web page of "+openedPageClass.getName()+" class");

	// Store current page information to check whether the link click did really occurred
	String linkUrl = linkElement.getAttribute("href");
	String linkString = linkElement.toString();
	if (DEBUG) {
		debugPrintln("		  -> link URL: "+hidePasswordInLocation(linkUrl));
		debugPrintln("		  -> link string: "+linkString);
	}
	Set<String> handles = this.browser.getWindowHandles(/*check:*/false);

	// Check to see if we're already on the right url, with the right kind of page.
	// This can happen if you're trying to follow a link that leads right back to the same page.
	// If the current page class is null, we'll go through loading the page
	WebPage currentPage = this.browser.getCurrentPage();
	if (currentPage != this) {
		println("WARNING: Try to open a new page from a page which is not the current page:");
		println(" - framework is on page: "+getClass());
		println(" - but browser page is: "+(currentPage==null ? "null" : currentPage.getClass()));
		println(" - new page is: "+openedPageClass);
		printStackTrace(1);
	}
	String currentPageClass = currentPage == null ? null : getClassSimpleName(currentPage.getClass());
	String pageUrl = getUrl();
	if (DEBUG) debugPrintln("		  -> page URL: "+hidePasswordInLocation(pageUrl));
	if (pageUrl.equals(linkUrl) && currentPage != null && currentPage.getClass().equals(openedPageClass)) {
		if (DEBUG) debugPrintln("		  -> Browser is already on the expected page, hence do nothing...");
		return (P) currentPage;
	}

	// Since Selenium 2.43.1 and/or Firefox 31 upgrade it happens sometimes that there's a freeze
	// when clicking on a link while trying to load the corresponding page...
	// Hence adding a security pause before the click in order to circumvent this issue
	if (this.delayBeforeLinkClick > 0) {
		if (DEBUG) debugPrintln("		  -> delay before click used: "+this.delayBeforeLinkClick+"ms");
		pause(this.delayBeforeLinkClick);
	}

	// Click on the link
	linkElement.click();
	pause(250);

	// Since Selenium 2.43.1 and/or Firefox 31 upgrade it happens frequently that there's a delay
	// between the click action and the browser content change...
	// Hence adding a security pause after the click in order to circumvent this issue
	if (this.delayAfterLinkClick > 0) {
		if (DEBUG) debugPrintln("		  -> delay after click used: "+this.delayAfterLinkClick+"ms");
		pause(this.delayAfterLinkClick);
	}

	// Notify the perfManager that a page is loading
	if (PERFORMANCE_ENABLED) this.browser.getPerfManager().setPageLoading(true);

	// Check whether a new handle has been created
	Set<String> newHandles = this.browser.getWindowHandles(/*check:*/false);
	if (newHandles.size() > handles.size()) {
		for (String browserHandle: newHandles) {
			if (!handles.contains(browserHandle)) {
				pause(500);
				this.browser.switchToHandle(browserHandle);
				this.browser.maximize();
				break;
			}
		}
	}

	// Accept private connection if any
	this.browser.acceptPrivateConnection();

	// Wait that at least for the body element with some content appeared in the page
	SpotAbstractTimeout childrenTimeout = new SpotAbstractTimeout() {
		@Override
		protected String getConditionLabel() {
			return "Has children";
		}
		@Override
		protected boolean getCondition() {
			return getBodyElement().getChildren().size() > 0;
		}
	};
	childrenTimeout.waitUntil(timeout());

	// If we're just opening the same page as we're on, skip the workaround.
	// Note: if the url is the same but the class of the page is different, we will get here.
	// The workaround will be skipped but the page will be opened properly.
	String newPageUrl = getUrl();
	String hiddenPasswordLocation = hidePasswordInLocation(newPageUrl);
	if (DEBUG) debugPrintln("	- page URL: "+hiddenPasswordLocation);
	if (!newPageUrl.equals(linkUrl)) {
		// Check whether a new handle has been opened
		// Check that the browser URL has changed after the click
		if (DEBUG) debugPrintln("		  -> page URL: "+hiddenPasswordLocation);
		long stimeout = timeout()*1000 + System.currentTimeMillis();
		int count = 0;
		while (pageUrl.equals(newPageUrl = getUrl())) {
			if (DEBUG && count == 0) {
				debugPrintln("================================================================================");
				debugPrintln("WARNING: Browser URL hasn't changed after having clicked on "+linkString);
				debugPrintln("	- browser URL: "+hidePasswordInLocation(this.browser.getCurrentUrl()));
				debugPrintln("	- page URL: "+hiddenPasswordLocation);
				debugPrintln("	- expected URL: "+hidePasswordInLocation(linkUrl));
				debugPrintln("	- stack trace:");
				debugPrintStackTrace(1);
			}
			if (System.currentTimeMillis() > stimeout) {
				if (DEBUG) {
					debugPrintln("	Browser URL still stays the same after "+shortTimeout()+" seconds...");
					debugPrintln("	=> Workaround is to open page directly setting browser URL with: "+linkUrl);
					debugPrintln("================================================================================");
				}
				if (linkUrl == null) {
					throw new ScenarioFailedError("Cannot open a page from link which has no URL.");
				}
				return openPage(linkUrl, getConfig(), getUser(), openedPageClass, pageData);
			}
			count++;
			sleep(1);
		}
		if (DEBUG && count > 0) {
			debugPrintln();
			debugPrintln("================================================================================");
		}
	}

	// Open the page and wait for it being loaded
	P page = openPage(newPageUrl, USER_ACTION_NOT_PROVIDED, pageConfig, pageUser, openedPageClass, pageData);

	// Fail to click on links occasionally, which causes ClassCastExceptions
	// If link failed, directly open page from href property of the link
	Class<? extends WebPage> pageClass = page.getClass();
	boolean validClass = pageClass.equals(openedPageClass);
	while(!validClass) {
		pageClass = (Class< ? extends WebPage>) pageClass.getSuperclass();
		if (pageClass == null) break;
		validClass = pageClass.equals(openedPageClass);
	}
	if (!validClass) {
		if (DEBUG) {
			debugPrintln("================================================================================");
			debugPrintln("WARNING: Unexpected new page class after having clicked on "+linkString);
			debugPrintln("	- previous class before click: "+currentPageClass);
			debugPrintln("	- current class after click: "+getClassSimpleName(page.getClass()));
			debugPrintln("	- expected class after click: "+getClassSimpleName(openedPageClass));
			debugPrintln("	=> Workaround is to open page directly setting browser URL with: "+linkUrl);
			debugPrintln("================================================================================");
		}
		if (linkUrl == null) {
			throw new ScenarioFailedError("Cannot open a page from link which has no URL.");
		}
		return openPage(linkUrl, getConfig(), pageUser, openedPageClass, pageData);
	}

	// Return opened page
	return page;
}

/**
 * Return the timeout while opening the page.
 *
 * @return The timeout as an <code>int</code>
 */
final public int openTimeout() {
	return this.openTimeout;
}

/**
 * {@inheritDoc}
 * <p>
 * If subclass overrides this method, it's strongly recommended to call the super
 * implementation in order to implicitly wait for the end of the page load, but also
 * to set the {@link #refreshed} flag...
 * </p>
 * @see #waitForLoadingPageEnd()
 */
@Override
public void refresh() {
	debugPrintEnteringMethod();
	if (DEBUG) debugPrintln("		  -> the current page location is: '"+this.location+"'");

	// Perform the refresh action
	this.browser.refresh();
	sleep(1);

	// Replace the page location if necessary
	String browserUrl = this.browser.getCurrentUrl();
	if (!this.location.equals(browserUrl)) {
		this.browser.cachePage(this);
		this.location = browserUrl;
		if (DEBUG) debugPrintln("		  -> the page location has been replaced with browser URL: '"+hidePasswordInLocation(browserUrl)+"'");
	}

	// Wait for the end of the page loading
	waitForLoadingPageEnd();

	// Store that a refresh occurred
	this.refreshed = true;
}

/**
 * Perform a mouse hovering over the given link element.
 *
 * @param <RH> The rich hover specialized class
 * @param linkElement The web element on which to hover
 * @param richHoverClass The rich hover specialized class
 * @param additionalData Additional data to check in the rich hover
 * @return The opened rich hover as the given specialized class
 */
@SuppressWarnings("unchecked")
public <RH extends WebTextHover> RH richHoverOverLink(final WebBrowserElement linkElement, final Class<RH> richHoverClass, final String... additionalData) {

	// Check link element
	if (linkElement == null) {
		throw new WaitElementTimeoutError("Cannot hover over a null link.");
	}

	// Create the hover window
	RH richHover;
	try {
		if (additionalData == null || additionalData.length == 0) {
			Constructor<? extends WebTextHover> constructor = richHoverClass.getConstructor(WebPage.class);
			richHover = (RH) constructor.newInstance(this);
		} else {
			Constructor<? extends WebTextHover> constructor = richHoverClass.getConstructor(WebPage.class, String[].class);
			richHover = (RH) constructor.newInstance(this, additionalData);
		}
	}
	catch (Exception ex) {
		throw new ScenarioFailedError(ex);
	}

	// Tempo for Google Chrome browser
	if (this.browser.isGoogleChrome()) {
		sleep(1);
	}

	// Hover over the link element
	richHover.open(linkElement);

	// Return the created hover
	return richHover;
}

/**
 * Scroll the current page window to make the given web element visible.
 * <p>
 * Default is to do nothing. Subclass might want to override this method in order
 * to make the element visible when possibly hidden by a fixed toolbar.
 * </p>
 * @param webElement The web element to make visible
 */
protected void scrollToMakeElementVisible(final WebBrowserElement webElement) {
	// Do nothing
}

/**
 * Scroll the page down.
 */
public void scrollDown() {
	debugPrintEnteringMethod();
	this.browser.scrollOnePageDown();
}

/**
 * Scroll the page up.
 */
public void scrollUp() {
	debugPrintEnteringMethod();
    this.browser.scrollOnePageDown();
}

/**
 * Scroll the page to top.
 */
public void scrollToTop() {
	debugPrintEnteringMethod();
	this.browser.scrollPageTop();
}

/**
 * Select the given item in the list element found using the given locator.
 * <p>
 * The items of the found list are supposed to be found using <code>by.xpath("./option")</code>
 * locator.
 * </p>
 * @param locator The locator to find the list element in the current page.
 * @param selection The item to select in the list, assuming that text matches
 * @return The selected element as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no item matches the expected selection.
 */
protected WebBrowserElement select(final By locator, final String selection) {
	WebBrowserElement listElement = waitForElement(locator);
	return select(listElement, selection);
}

/**
 * Select the given item in the list element found using the given locator.
 * <p>
 * The items of the found list are supposed to be found using <code>by.xpath("./option")</code>
 * locator.
 * </p>
 * @param locator The locator to find the list element in the current page.
 * @param comparisonCriteria A list of criteria to determine how to match an item in the
 * elements list to the selection
 * @param selection The item to select in the list, assuming that text matches
 * @return The selected element as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no item matches the expected selection.
 */
protected WebBrowserElement select(final By locator, final String selection, final StringComparisonCriterion... comparisonCriteria) {
	WebBrowserElement listElement = waitForElement(locator);
	WebBrowserElement[] selectedElements = this.browser.select(listElement, By.xpath("./option"), false /* Use Control */, comparisonCriteria, selection);
	return selectedElements[0];
}

/**
 * Select the given item in the given list element found.
 * <p>
 * The items of the selection list are supposed to be found using
 * <code>by.xpath("./option")</code> locator.
 * </p>
 * @param listElement The list element in which perform the selection.
 * @param selection The item to select in the list, assuming that text matches
 * @return The selected element as {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no item matches the expected selection.
 */
protected WebBrowserElement select(final WebBrowserElement listElement, final String selection) {
	WebBrowserElement[] selectedElements = this.browser.select(listElement, By.xpath("./option"), selection);
	return selectedElements[0];
}

final protected void setOpenTimeout(final int timeout) {
	this.openTimeout = timeout;
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
 * Set user action name on performances manager.
 *
 * @param action The action name
 * @throws ScenarioFailedError If performances are <b>not enabled</b> during
 * scenario execution. Hence, callers have to check whether the performances are
 * enabled before calling this method using {@link PerfManager#PERFORMANCE_ENABLED}.
 */
protected void setPerfManagerUserActionName(final String action) throws ScenarioFailedError {
	if (PERFORMANCE_ENABLED) {
		this.browser.perfManager.setUserActionName(action);
	} else {
		throw new ScenarioFailedError("Performances are not enabled for the scenario execution. Use -DperformanceEnabled=true to avoid this failure.");
	}
}

final protected void setShortTimeout(final int timeout) {
	this.shortTimeout = timeout;
}

final protected void setTimeout(final int timeout) {
	this.timeout = timeout;
}

/**
 * Return the short timeout used on the page.
 *
 * @return The timeout as an <code>int</code>
 */
final public int shortTimeout() {
	return this.shortTimeout;
}

/* Discarded as it's no longer matching the multi-users model */
///**
// * Close the current browser session and open a new one at the same page.
// */
//protected void startNewBrowserSession() {
//	this.browser = getConfig().getBrowserManager().openNewBrowser(this);
//
//	// Clear page cache except for current page object
//	getPagesHistory().clear();
//	getPagesHistory().add(this);
//
//	//Clear login data
//	getTopology().logoutApplications();
//
//	// Reopen current page in browser
//	this.browser.get(getUrl());
//}

/**
 * Starts the perfManager server timer
 *
 * @throws ScenarioFailedError If performances are <b>not enabled</b> during
 * scenario execution. Hence, callers have to check whether the performances are
 * enabled before calling this method using {@link PerfManager#PERFORMANCE_ENABLED}.
 */
protected void startPerfManagerServerTimer() throws ScenarioFailedError {
	if (PERFORMANCE_ENABLED) {
		this.browser.perfManager.startServerTimer();
	} else {
		throw new ScenarioFailedError("Performances are not enabled for the scenario execution. Use -DperformanceEnabled=true to avoid this failure.");
	}
}

/**
 * Takes a failure snapshot.
 *
 * @param fileName The name of the snapshot file.
 */
public void takeSnapshotFailure(final String fileName) {
	this.browser.takeScreenshotFailure(fileName);
}

/**
 * Takes an information snapshot.
 *
 * @param fileName The name of the snapshot file.
 */
public void takeSnapshotInfo(final String fileName) {
	this.browser.takeScreenshotInfo(fileName);
}

/**
 * Takes a warning snapshot.
 *
 * @param fileName The name of the snapshot file.
 */
public void takeSnapshotWarning(final String fileName) {
	this.browser.takeScreenshotWarning(fileName);
}

/**
 * Return the general timeout used on the page.
 *
 * @return The timeout as an <code>int</code>
 */
final public int timeout() {
	return this.timeout;
}

@Override
public String toString() {
	return "Web page at location '"+hidePasswordInLocation(this.location)+"' ("+getApplication()+", url: "+hidePasswordInLocation(getUrl())+")";
}

/**
 * Type a password into the given input web element.
 * <p>
 * Note that:
 * <ul>
 * <li>if will fail if the input field does not turn enabled before {@link #shortTimeout()}
 * seconds</li>
 * <li>the input element will be cleared prior entering the given text</li>
 * </ul>
 * </p><p>
 * Note also that a {@link Keys#TAB} is hit after having entered the text in the
 * input field in order to trigger the 'keyEvent' and makes the javascript associated
 * with the filed working properly.
 * </p>
 * @param element The input web element in the current page
 * @param usr User whom password has to be typed
 *
 * @see WebBrowser#typePassword(WebBrowserElement, int, SpotUser)
 */
protected void typePassword(final WebBrowserElement element, final SpotUser usr) {
	this.browser.typePassword(element, shortTimeout(), usr);
}

/**
 * Type a text into an input web element found relatively to a parent web element
 * using respective given locators.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the input field is not found before {@link #timeout()} seconds</li>
 * <li>if will fail if the input field does not turn enabled before {@link #shortTimeout()}
 * seconds</li>
 * <li>the input element will be cleared prior entering the given text</li>
 * </ul>
 * </p><p>
 * Note also that a {@link Keys#TAB} is hit after having entered the text in the
 * input field in order to trigger the 'keyEvent' and makes the javascript associated
 * with the filed working properly.
 * </p>
 * @param parentLocator The locator to find the parent element in the current
 * page, if <code>null</code>, the element will be searched in the entire page
 * content
 * @param locator The locator to find the element in the current page or
 * from the given parent element if not <code>null</code>
 * @param text The text to type in the input element
 * @return The text web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #waitForElement(By, By)
 * @see WebBrowser#typeText(WebBrowserElement, String, Keys, boolean, int)
 */
protected WebBrowserElement typeText(final By parentLocator, final By locator, final String text) {
	WebBrowserElement element = waitForElement(parentLocator, locator);
	this.browser.typeText(element, text, Keys.TAB, true/*clear*/, shortTimeout());
	return element;
}

/**
 * Type a text into an input web element found relatively to a parent web element
 * using respective given locators.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the input field is not found before {@link #timeout()} seconds</li>
 * <li>if will fail if the input field does not turn enabled before {@link #shortTimeout()}
 * seconds</li>
 * </ul>
 * </p><p>
 * Note also that a {@link Keys#TAB} is hit after having entered the text in the
 * input field in order to trigger the 'keyEvent' and makes the javascript associated
 * with the filed working properly.
 * </p>
 * @param parentLocator The locator to find the parent element in the current
 * page, if <code>null</code>, the element will be searched in the entire page
 * content
 * @param locator The locator to find the element in the current page or
 * from the given parent element if not <code>null</code>
 * @param text The text to type in the input element
 * @param clear Tells whether the text needs to be cleared to type in the input element
 * @return The text web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #waitForElement(By, By)
 * @see WebBrowser#typeText(WebBrowserElement, String, Keys, boolean, int)
 */
protected WebBrowserElement typeText(final By parentLocator, final By locator, final String text, final boolean clear) {
	WebBrowserElement element = waitForElement(parentLocator, locator);
	this.browser.typeText(element, text, Keys.TAB, clear, shortTimeout());
	return element;
}

/**
 * Type a text into an input web element found in the current page using the
 * given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the input field is not found before {@link #timeout()} seconds</li>
 * <li>if will fail if the input field does not turn enabled before {@link #shortTimeout()}
 * seconds</li>
 * <li>the input element will be cleared prior entering the given text</li>
 * </ul>
 * </p><p>
 * Note also that a {@link Keys#TAB} is hit after having entered the text in the
 * input field in order to trigger the 'keyEvent' and makes the javascript associated
 * with the filed working properly.
 * </p>
 * @param locator The locator to find the input web element in the current
 * page
 * @param text The text to type in the input element
 * @return The text web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #waitForElement(By)
 * @see WebBrowser#typeText(WebBrowserElement, String, Keys, boolean, int)
 */
protected WebBrowserElement typeText(final By locator, final String text) {
	WebBrowserElement element = waitForElement(locator);
	this.browser.typeText(element, text, Keys.TAB, true/*clear*/, shortTimeout());
	return element;
}

/**
 * Type a text into an input web element found in the current page using the
 * given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the input field is not found before {@link #timeout()} seconds</li>
 * <li>if will fail if the input field does not turn enabled before {@link #shortTimeout()}
 * seconds</li>
 * </ul>
 * </p><p>
 * Note also that a {@link Keys#TAB} is hit after having entered the text in the
 * input field in order to trigger the 'keyEvent' and makes the javascript associated
 * with the filed working properly.
 * </p>
 * @param locator The locator to find the input web element in the current
 * page
 * @param text The text to type in the input element
 * @param clear Tells whether the text needs to be cleared to type in the input element
 * @return The text web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #waitForElement(By)
 * @see WebBrowser#typeText(WebBrowserElement, String, Keys, boolean, int)
 */
protected WebBrowserElement typeText(final By locator, final String text, final boolean clear) {
	WebBrowserElement element = waitForElement(locator);
	this.browser.typeText(element, text, Keys.TAB, clear, shortTimeout());
	return element;
}

/**
 * Type a text into an input web element found inside the given parent web element
 * using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the input field is not found before {@link #timeout()} seconds</li>
 * <li>if will fail if the input field does not turn enabled before {@link #shortTimeout()}
 * seconds</li>
 * <li>the input element will be cleared prior entering the given text</li>
 * </ul>
 * </p><p>
 * Note also that a {@link Keys#TAB} is hit after having entered the text in the
 * input field in order to trigger the 'keyEvent' and makes the javascript associated
 * with the filed working properly.
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locator The locator to find the input web element in the current
 * page
 * @param text The text to type in the input element
 * @return The text web element (as a {@link WebBrowserElement}) found
 * in the page
 *
 * @see #waitForElement(WebBrowserElement, By)
 * @see WebBrowser#typeText(WebBrowserElement, String, Keys, boolean, int)
 */
protected WebBrowserElement typeText(final WebBrowserElement parentElement, final By locator, final String text) {
	WebBrowserElement element = waitForElement(parentElement, locator);
	this.browser.typeText(element, text, Keys.TAB, true/*clear*/, shortTimeout());
	return element;
}

/**
 * Type a text into the given input web element found.
 * <p>
 * Note that:
 * <ul>
 * <li>if will fail if the input field does not turn enabled before {@link #shortTimeout()}
 * seconds</li>
 * <li>the input element will be cleared prior entering the given text</li>
 * </ul>
 * </p><p>
 * Note also that a {@link Keys#TAB} is hit after having entered the text in the
 * input field in order to trigger the 'keyEvent' and makes the javascript associated
 * with the filed working properly.
 * </p>
 * @param inputElement The web element to enter the text in
 * @param text The text to type in the input element
 *
 * @see WebBrowser#typeText(WebBrowserElement, String, Keys, boolean, int)
 */
protected void typeText(final WebBrowserElement inputElement, final String text) {
	this.browser.typeText(inputElement, text, Keys.TAB, true/*clear*/, shortTimeout());
}

/**
 * Verify that page user matches the expected one.
 * <p>
 * Check that user name displayed in the User Profile matches the current page user name.
 * </p>
 * <p>
 * Note that no error is raised when an inconsistency is first detected. Instead, a log in operation
 * with the page user is done to synchronize the page and the browser. However, a {@link ScenarioFailedError}
 * is eventually raised if the verification fails {@link #MAX_RECOVERY_TRIES} times.
 * </p>
 * @throws ScenarioFailedError if verification fails {@link #MAX_RECOVERY_TRIES} times
 */
protected void verifyPageUser() throws ScenarioFailedError {
	debugPrintEnteringMethod();

	// Check whether the current page is on the expected user or not
	WebBrowserElement loggedUserElement = getLoggedUserElement(false/*fail*/, shortTimeout());
	if (loggedUserElement != null && !matchDisplayedUser(getUser(), loggedUserElement)) {
		this.browser.takeScreenshotInfo("VerifyPageUser");
		println("INFO: User name '"+loggedUserElement.getText()+"' does not match expected one: '"+getUser().getName()+"'");
		println("     Workaround this issue by forcing a login with "+getUser());

		// It may be the case that a re-try will let us login properly. However, if there is a disconnect
		// between the server's info (name/id) and the properties files, we'll be stuck in a loop
		// between get(), verifyPageUser(), login(). To avoid that, only try MAX_RECOVERY_TRIES
		// to login/get/verify.
		if (this.verifyTries++ >= MAX_RECOVERY_TRIES) {
			throw new ScenarioFailedError("User with id '" + getUser().getId() + "' is not going to be able to login because their name '"
					+ getUser().getName() + "' does not match the server value of '" + loggedUserElement.getText() + "'.");
		}

		// Otherwise, try logging in again
		login(getUser(), true);
	} else {
		if (DEBUG) debugPrintln("		  -> OK");
	}
}

/**
 * Wait until have found the web element using the given locator.
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
 * @param locator The locator to find the element in the current page.
 * @return The web element as {@link WebBrowserElement}
 * @throws WaitElementTimeoutError if no element is found before the timeout
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
protected WebBrowserElement waitForElement(final By locator) {
	return this.browser.waitForElement(null, locator, true/* fail */, timeout(), true/* displayed */, true/* single */);
}

/**
 * Wait until have found the web element using the given locator.
 * <p>
 * Note that it will fail if:
 * <ol>
 * <li>the element is not found before {@link #timeout()} seconds</li>
 * <li>there's more than one element found</li>
 * </ol>
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @return The web element as {@link WebBrowserElement}
 * @throws ScenarioFailedError if no element was found before the timeout.
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
protected WebBrowserElement waitForElement(final By locator, final boolean displayed) {
	return this.browser.waitForElement(null, locator, true/*fail*/, timeout(), displayed, true/*single element expected*/);
}

/**
 * Wait until have found the web element using the given locator.
 * <p>
 * Note that it will fail if the element is not found before {@link #timeout()}
 * seconds
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param single Tells whether a single element is expected
 * @return The web element as {@link WebBrowserElement}
 * @throws ScenarioFailedError if no element was found before the timeout.
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
protected WebBrowserElement waitForElement(final By locator, final boolean displayed, final boolean single) {
	return this.browser.waitForElement(null, locator, true/*fail*/, timeout(), displayed, single);
}

/**
 * Wait until have found the web element using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if:
 * <ol>
 * <li>the element is not found before {@link #timeout()} seconds and asked to fail</li>
 * <li>there's more than one element found</li>
 * </ol></li>
 * <li>hidden element will be ignored</li>
 * </ul>
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param fail Tells whether to fail if the element is not found before timeout
 * @param time_out The time to wait before giving up the research
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws ScenarioFailedError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
protected WebBrowserElement waitForElement(final By locator, final boolean fail, final int time_out) {
	return this.browser.waitForElement(null, locator, fail, time_out, true /* displayed */, true /* single */);
}

/**
 * Wait until have found the web element using the given locator.
 * <p>
 * Note that it will fail if there's more than one element found.
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param fail Tells whether to fail if none of the elements is find before timeout
 * @param time_out The time to wait before giving up the research
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws ScenarioFailedError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
public WebBrowserElement waitForElement(final By locator, final boolean fail, final int time_out, final boolean displayed) {
	return this.browser.waitForElement(null, locator, fail, time_out, displayed, true/*single element expected*/);
}

/**
 * Wait until have found the web element using the given locator.
 *
 * @param locator The locator to find the element in the current page.
 * @param fail Tells whether to fail if none of the elements is find before timeout
 * @param time_out The time to wait before giving up the research
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @param single Tells whether a single element is expected
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws ScenarioFailedError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
protected WebBrowserElement waitForElement(final By locator, final boolean fail, final int time_out, final boolean displayed, final boolean single) {
	return this.browser.waitForElement(null, locator, fail, time_out, displayed, single);
}

/**
 * Wait until have found the web element relatively to a parent element using
 * the respective given locators.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if:
 * <ol>
 * <li>the element is not found before {@link #timeout()} seconds</li>
 * <li>there's more than one element found</li>
 * </ol></li>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param parentLocator The locator to find the parent element in the current
 * page, if <code>null</code>, the element will be searched in the entire page
 * content
 * @param locator The locator to find the element in the current page or
 * from the given parent element if not <code>null</code>
 * @return The web element as {@link WebBrowserElement}
 * @throws ScenarioFailedError if no element was found before the {@link #timeout()}.
 *
 * @see #waitForElement(By, By, boolean, int)
 */
protected WebBrowserElement waitForElement(final By parentLocator, final By locator) {
	return waitForElement(parentLocator, locator, true/*fail*/, timeout());
}

/**
 * Wait until have found the web element relatively to a parent element using
 * the respective given locators.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if there's more than one element found</li>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param parentLocator The locator to find the parent element in the current
 * page, if <code>null</code>, the element will be searched in the entire page
 * content
 * @param locator The locator to find the element in the current page or
 * from the given parent element if not <code>null</code>
 * @param fail Tells whether to fail if none of the elements is find before timeout
 * @param time_out The time to wait before giving up the research
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws ScenarioFailedError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
public WebBrowserElement waitForElement(final By parentLocator, final By locator, final boolean fail, final int time_out) {
	WebBrowserElement parentElement = parentLocator == null ? null : waitForElement(parentLocator);
	return this.browser.waitForElement(parentElement, locator, fail, time_out, true/*visible*/, true/*single element expected*/);
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
 */
protected WebBrowserElement waitForElement(final WebBrowserElement parentElement, final By locator) {
	return this.browser.waitForElement(parentElement, locator, true/* fail */, timeout(), true/* visible */, true/* single */);
}

/**
 * Wait until have found the web element using the given locator relatively
 * to the given parent element.
 * <p>
 * <ul>
 * Note that:
 * </ul>
 * <li>it will fail if there's more than one element found</li>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locator The locator to find the element in the current page.
 * @param fail Tells whether to fail if none of the elements is find before timeout
 * @param time_out The time to wait before giving up the research
 * @return The web element as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws ScenarioFailedError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForElement(WebBrowserElement, By, boolean, int, boolean, boolean)
 */
protected WebBrowserElement waitForElement(final WebBrowserElement parentElement, final By locator, final boolean fail, final int time_out) {
	return this.browser.waitForElement(parentElement, locator, fail, time_out, true/*visible*/, true/*single element expected*/);
}

/**
 * Wait until have found some elements (ie. at least one) web elements using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if:
 * <ol>
 * <li>no element is found before {@link #timeout()} seconds</li>
 * </ol></li>
 * <li>hidden element will be ignored</li>
 * </ul>
 * </p>
 * @param locator The locator to find the element in the current page.
 * @return The web elements list as {@link List} of {@link WebBrowserElement}
 * @throws ScenarioFailedError if no element was found before the timeout.
 *
 * @see WebBrowser#waitForElements(WebBrowserElement, By, boolean, int, boolean)
 */
protected List<WebBrowserElement> waitForElements(final By locator) {
	return this.browser.waitForElements(null, locator, true/*fail*/, timeout(), true/*visible*/);
}

/**
 * Wait until have found some elements (ie. at least one) web elements using the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param fail True if this should fail if the element is not found.
 * @return The web elements list as {@link List} of {@link WebBrowserElement}.
 * Might be empty if no element was found before the timeout and asked not to fail
 * @throws ScenarioFailedError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForElements(WebBrowserElement, By, boolean, int, boolean)
 */
protected List<WebBrowserElement> waitForElements(final By locator, final boolean fail) {
	return this.browser.waitForElements(null, locator, fail, timeout(), true/*visible*/);
}

/**
 * Wait until have found some elements (ie. at least one) web elements using
 * the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param fail Tells whether to fail if none of the locators is find before timeout
 * @param time_out The time to wait before giving up the research
 * @return The web elements list as {@link List} of {@link WebBrowserElement}.
 * Might be empty if no element was found before the timeout and asked not to fail
 * @throws ScenarioFailedError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForElements(WebBrowserElement, By, boolean, int, boolean)
 */
protected List<WebBrowserElement> waitForElements(final By locator, final boolean fail, final int time_out) {
	return this.browser.waitForElements(null, locator, fail, time_out, true/*visible*/);
}

/**
 * Wait until have found some elements (ie. at least one) web elements using
 * the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param time_out The time to wait before giving up the research
 * @return A non-empty web elements list as {@link List} of {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no element was found before the timeout.
 *
 * @see WebBrowser#waitForElements(WebBrowserElement, By, boolean, int, boolean)
 */
protected List<WebBrowserElement> waitForElements(final By locator, final int time_out) {
	return this.browser.waitForElements(null, locator, true/*fail*/, time_out, true/*visible*/);
}

/**
 * Wait until have found some elements (ie. at least one) web elements using
 * the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param locator The locator to find the element in the current page.
 * @param time_out The time to wait before giving up the research
 * @param displayed When <code>true</code> then only displayed element can be returned.
 * When <code>false</code> then the returned element can be either displayed or hidden.
 * @return A non-empty web elements list as {@link List} of {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no element was found before the timeout.
 *
 * @see WebBrowser#waitForElements(WebBrowserElement, By, boolean, int, boolean)
 */
protected List<WebBrowserElement> waitForElements(final By locator, final int time_out, final boolean displayed) {
	return this.browser.waitForElements(null, locator, true/*fail*/, time_out, displayed);
}

/**
 * Wait until have found some elements (ie. at least one) web elements using
 * the given locator.
 * <p>
 * Note that:
 * <ul>
 * <li>hidden element will be ignored</li>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locator The locator to find the element in the current page.
 * @return A non-empty web elements list as {@link List} of {@link WebBrowserElement}.
 * @throws ScenarioFailedError if no element was found before the timeout.
 *
 * @see WebBrowser#waitForElements(WebBrowserElement, By, boolean, int, boolean)
 */
protected List<WebBrowserElement> waitForElements(final WebBrowserElement parentElement, final By locator) {
	return this.browser.waitForElements(parentElement, locator, true/*fail*/, timeout(), true/*visible*/);
}

/**
 * Wait for the page initial load.
 *
 * @throws ServerMessageError If any server error message occus while waiting
 * for the page to be initially loaded.
 */
protected void waitInitialPageLoading() throws ServerMessageError {
	waitInitialPageLoading(true/*throwError*/);
}

/**
 * Wait for the page initial load.
 * <p>
 * At this highest level, waiting for the page load is first to wait for the root element
 * and then wait that it has at least one child element.
 * </p>
 * @param throwError Tells whether a {@link ServerMessageError} has to
 * be thrown if a server error is detected during the load operation.
 * @throws ServerMessageError If any server error message occurs while waiting
 * for the page to be initially loaded and it has been told to throw the error
 */
protected void waitInitialPageLoading(final boolean throwError) throws ServerMessageError {

	// Workaround to accept certificate when opening page using browser
	if (!throwError) {
		this.browser.acceptPrivateConnection();
	}

	// Get root web element
	WebBrowserElement rootElement = getRootElement();

	// Wait until something is displayed under root
	long time_out = openTimeout() * 1000 + System.currentTimeMillis();
	while (rootElement.getChildren().size() == 0) {
		if (System.currentTimeMillis() > time_out) {
			throw new WaitElementTimeoutError("Initial page loading never finish.");
		}
		sleep(1);
	}
}

/**
 * Wait for the page loading to be finished.
 * <p>
 * The default behavior is to wait for the status message to be triggered (ie.
 * waiting the message to appear, then waiting the message to vanish).
 * </p><p>
 * In case the message appearance was missed at the beginning, then it
 * automatically give up after {@link #shortTimeout()} seconds. No error is
 * raised in such a case, it just hopes that while waiting for the message which
 * never comes, the page had enough time to be completely loaded...
 * </p>
 */
protected void waitForLoadingPageEnd() {
	if (DEBUG) debugPrintln("		+ Waiting for loading page end");
	long waitTimeout = openTimeout() * 1000 + System.currentTimeMillis();	 // Timeout currentTimeMilliseconds
	while (!isLoaded()) {
		if (System.currentTimeMillis() > waitTimeout) {
			this.browser.takeScreenshotWarning("LoadTimeout_"+getClassSimpleName(getClass()));
			println("WARNING: Page "+this+" never finish to load!");
			println("	- browser URL: "+hidePasswordInLocation(this.browser.getCurrentUrl().replaceAll("%20", SPACE_STRING)));
			println("	- location: "+hidePasswordInLocation(this.location));
			println("	- page URL: "+hidePasswordInLocation(getTopology().getPageUrl(this.location).replaceAll("%20", SPACE_STRING)));
			println("	- stack trace: ");
			printStackTrace(2);
			println();
			break;
		}
		sleep(1);
	}
}

/**
 * Wait until have found at least one of the elements using the given locators.
 * <p>
 * Note that:
 * <ul>
 * <li>hidden element will be ignored</li>
 * </ul>
 * </p>
 * @param locators The locators to find the element in the current page.
 * @param fail Tells whether to fail if none of the elements is find before timeout
 * @param time_out The time to wait before giving up the research
 * @return The array of web elements as {@link WebBrowserElement} or <code>null</code>
 * if no element was found before the timeout and asked not to fail
 * @throws WaitElementTimeoutError if no element was found before the timeout and
 * asked to fail
 *
 * @see WebBrowser#waitForMultipleElements(WebBrowserElement, By[], boolean, int)
 * to have more details on how the returned array is filled with found elements
 */
protected WebBrowserElement[] waitForMultipleElements(final boolean fail, final int time_out, final By... locators) {
	return this.browser.waitForMultipleElements(null, locators, fail, time_out);
}

/**
 * Wait until have found at least one of the elements using the given locators.
 * <p>
 * Note that:
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * <li>hidden element will be ignored</li>
 * <ul>
 * </p>
 * @param locators The locators to find the element in the current page.
 * @return The array of web elements as {@link WebBrowserElement}
 * @throws WaitElementTimeoutError if no element was found before the timeout
 *
 * @see WebBrowser#waitForMultipleElements(WebBrowserElement, By[], boolean, int)
 * to have more details on how the returned array is filled with found elements
 */
public WebBrowserElement[] waitForMultipleElements(final By... locators) {
	return this.browser.waitForMultipleElements(null, locators, true/*fail*/, timeout());
}

/**
 * Wait until have found at least one of the elements relatively to the given
 * parent element using the given locators.
 * <p>
 * Note that:
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * <li>hidden element will be ignored</li>
 * <ul>
 * </p>
 * @param parentElement The parent element where to start to search from,
 * if <code>null</code>, then search in the entire page content
 * @param locators The locators to find the element in the current page.
 * @return The array of web elements as {@link WebBrowserElement}
 * @throws WaitElementTimeoutError if no element was found before the timeout
 *
 * @see WebBrowser#waitForMultipleElements(WebBrowserElement, By[], boolean, int)
 * to have more details on how the returned array is filled with found elements
 */
protected WebBrowserElement[] waitForMultipleElements(final WebBrowserElement parentElement, final By... locators) {
	return this.browser.waitForMultipleElements(parentElement, locators, true/*fail*/, timeout());
}

/**
 * Wait until have got one of the expected texts on of the given element.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if the element is not found before {@link #timeout()} seconds</li>
 * </ul>
 * </p>
 * @param element The text element to be read
 * @param fail Tells whether to fail if none of the elements is find before timeout
 * @param time_out The time to wait before giving up the research
 * @param texts The expected texts
 * @return One of the expected text as {@link String} or <code>null</code>
 * if element text never matches one before the timeout and asked not to fail
 * @throws ScenarioFailedError if element text never matches an expected ones
 * before the timeout and asked to fail
 *
 * @see WebBrowser#waitForText(WebBrowserElement, boolean, int, String...)
 */
public String waitForText(final WebBrowserElement element, final boolean fail, final int time_out, final String... texts) {
	return this.browser.waitForText(element, fail, time_out, texts);
}

/**
 * Wait until have got one of the expected texts on of the given element.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if none of the text is found before {@link #timeout()} seconds</li>
 * </ul>
 * </p>
 * @param pattern The comparison pattern to find the expected texts.
 * @param texts The expected texts
 * @return One of the expected text as {@link String} or <code>null</code>
 * if element text never matches one before the timeout and asked not to fail
 * @throws ScenarioFailedError if element text never matches an expected ones
 * before the timeout and asked to fail
 *
 * @see WebBrowser#waitForText(WebBrowserElement, boolean, int, String...)
 */
protected WebBrowserElement waitForTextPresent(final ComparisonPattern pattern, final String... texts) {
	return this.browser.waitForTextPresent(null, true/*fail*/, timeout(), true/*displayed*/, false/*multiple*/, pattern, texts);
}

/**
 * Wait until have got one of the expected texts on of the given element.
 * <p>
 * Note that:
 * <ul>
 * <li>it will fail if none of the text is found before {@link #timeout()} seconds</li>
 * </ul>
 * </p>
 * @param texts The expected texts
 * @return One of the expected text as {@link String} or <code>null</code>
 * if element text never matches one before the timeout and asked not to fail
 * @throws ScenarioFailedError if element text never matches an expected ones
 * before the timeout and asked to fail
 *
 * @see WebBrowser#waitForText(WebBrowserElement, boolean, int, String...)
 */
public WebBrowserElement waitForTextPresent(final String... texts) {
	return this.browser.waitForTextPresent(null, true/*fail*/, timeout(), true/*displayed*/, false/*first occurrence*/, ComparisonPattern.StartsWith, texts);
}

/**
 * Execute a workaround to avoid raising the given {@link ScenarioFailedError}
 * exception.
 * <p>
 * The default workaround is to refresh the page. Of course subclass might
 * either add some other actions or even replace it by more typical actions.
 * </p>
 * @param message The exception message
 * @throws ScenarioFailedError with the given message if no workaround is
 * possible (typically, when too many tries have been done...)
 */
protected void workaround(final String message) {
	WebPageWorkaround workaround = new WebPageWorkaround(this, message);
	workaround.execute();
}
}
