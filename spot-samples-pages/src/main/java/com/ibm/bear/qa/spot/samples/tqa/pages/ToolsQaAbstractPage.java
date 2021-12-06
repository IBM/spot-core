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
package com.ibm.bear.qa.spot.samples.tqa.pages;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioImplementationError;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Abstract class to manage common behavior among any ToolsQA web page.
 * <p>
 * This class defines following API methods:
 * <ul>
 * <li>{@link #gotoHomePage()}: Go to <b>ToolsQA</b> Home page by clicking on the header link.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getLoggedUserElementLocator()}: Return the locator of the web element displaying the logged user name.</li>
 * <li>{@link #getRootElementLocator()}: Return the locator for the root web element of the current web page.</li>
 * <li>{@link #matchDisplayedUser(User,WebBrowserElement)}: Return whether the displayed user matches the user name or not.</li>
 * <li>{@link #performLogout()}: Logout the page from current user to new user.</li>
 * </ul>
 * </p>
 */
public abstract class ToolsQaAbstractPage extends WebPage {

	/**
	 * Enumeration to manage groups displayed on Home page.
	 */
	public enum Group {
		Elements,
		Forms,
		Windows("Alerts, Frame & Windows"),
		Widgets,
		Interactions,
		BookStore("Book Store Application");
		private String label;
		Group() {
			this(null);
			this.label = toString();
		}
		Group(final String text) {
			this.label = text;
		}
		public String getLabel() {
			return this.label;
		}
	}

/**
 * Create an instance of the page using the given URL, configuration and user.
 * <p>
 * Using this constructor means that the page instance won't have any data.
 * </p><p>
 * On a concrete subclass, it's strongly recommended not to implement at the same time
 * this constructor and the constructor with data ({@link #ToolsQaAbstractPage(String, Config, User, String...)}).
 * Then framework can know whether it can automatically check some data in the page or not.
 * </p>
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 */
public ToolsQaAbstractPage(final String url, final Config config, final User user) {
	super(url, config, user);
}

/**
 * Create an instance of the page using the given URL, configuration, user and data.
 * <p>
 * On a concrete subclass, it's strongly recommended not to implement at the same time
 * this constructor and the constructor without any data ({@link #ToolsQaAbstractPage(String, Config, User)}).
 * Then framework can know whether it can automatically check some data in the page or not.
 * </p>
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 */
public ToolsQaAbstractPage(final String url, final Config config, final User user, final String... infos) {
	super(url, config, user, infos);
}

/**
 * {@inheritDoc}
 * <p>
 * Tools QA web page does not have any logged user.
 * </p>
 * @return Always <code>null</code>
 */
@Override
protected By getLoggedUserElementLocator() {
	return null;
}

@Override
protected By getRootElementLocator() {
	return By.id("app");
}

@Override
protected boolean matchDisplayedUser(final User pageUser, final WebBrowserElement loggedUserElement) {
	throw new ScenarioImplementationError("ToolsQA page does not have any logged user.");
}

@Override
protected void performLogout() {
	throw new ScenarioImplementationError("ToolsQA page does not have any login operation.");
}

/**
 * Go to <b>ToolsQA</b> Home page by clicking on the header link.
 *
 * @return The opened Home page
 */
public ToolsQaHomePage gotoHomePage() {
	return openPageUsingLink(By.cssSelector("header>a"), getUser(), ToolsQaHomePage.class);
}
}
