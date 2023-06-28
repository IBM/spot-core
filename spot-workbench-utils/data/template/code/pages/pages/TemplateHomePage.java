/*********************************************************************
* Copyright (c) 2020, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.template.pages;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.whoAmI;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioMissingImplementationError;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Class to manage the <b>%title% Home</b> web page.
 * <p>
 * This class defines following API methods:
 * <ul>
 * </ul>
 * </p><p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #getLoggedUserElementLocator()}: Return the locator of the web element displaying the logged user name.</li>
 * <li>{@link #getRootElementLocator()}: Return the locator for the root web element of the current web page.</li>
 * <li>{@link #isLoaded()}: Return whether the page is currently loaded or not.</li>
 * <li>{@link #load()}: Load the current page into the browser.</li>
 * <li>{@link #matchDisplayedUser(User,WebBrowserElement)}: Return whether the displayed user matches the user name or not.</li>
 * <li>{@link #performLogout()}: Logout the page from current user to new user.</li>
 * </ul>
 * </p>
 */
public class TemplateHomePage extends WebPage {

/**
 * Create an instance of the page using the given URL, configuration and user.
 * <p>
 * Keep this constructor when the page does not need any specific additional data.
 * </p><p>
 * Warning: It's strongly recommended not to implement both constructors
 * w/o data (see {@link #TemplateHomePage(String, Config, User, String...)}).
 * </p>
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 */
public TemplateHomePage(final String url, final Config config, final User user) {
	super(url, config, user);
}
/**
 * Create an instance of the page using the given URL, configuration, user and data.
 * <p>
 * Keep this constructor when the page does need specific additional data
 * (e.g. an expected title to be checked while opening the page).
 * </p><p>
 * Warning: It's strongly recommended not to implement both constructors
 * w/o data (see {@link #TemplateHomePage(String, Config, User)}).
 * </p>
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 * @param infos The page information to be stored
 */
public TemplateHomePage(final String url, final Config config, final User user, final String... infos) {
	super(url, config, user);
}

@Override
protected By getLoggedUserElementLocator() {
	// By default assume there's no logged user information displayed on page
	// Modify this method to return a locator if that piece of information can be accessed
	// through a page element
	return null;
}

@Override
protected By getRootElementLocator() {
	throw new ScenarioMissingImplementationError("A locator must be specified for the page root element");
//	 Example of generic root element:
//	return By.tagName("body");
}

@Override
protected boolean isLoaded() {

	// If page is basically loaded, then check more precisely to its content
	// to tell whether the entire page is actually loaded or not
	if (super.isLoaded()) {
		// Example of check for more precise content
//		WebBrowserElement containerElement = waitForPotentialDisplayedElementWithTimeout(By.className("container"), 1);
//		return containerElement != null;
		return true;
	}

	// Page is not loaded yet
	return false;
}

@Override
protected void load() {

	// Perform generic page load
	super.load();

	// Example of wait for specific page content
//	waitForMandatoryDisplayedElement(By.className("container"));
}

@Override
protected boolean matchDisplayedUser(final User pageUser, final WebBrowserElement loggedUserElement) {
	// Modify the implementation of this method if page display logged user information
	// to tell whether it matches the given user or not
	return false;
}


@Override
protected void performLogout() {
	// Assuming there's no logout possible on current page
	// Implement specific code here if the page offers this capability
	throw new ScenarioMissingImplementationError(whoAmI());
}
}
