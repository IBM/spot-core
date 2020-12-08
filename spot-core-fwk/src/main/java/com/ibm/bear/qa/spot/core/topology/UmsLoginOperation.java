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
package com.ibm.bear.qa.spot.core.topology;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.println;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.timeout.SpotTextTimeout;
import com.ibm.bear.qa.spot.core.web.*;

/**
 * Class to manage a login using <b>User Management Service (UMS)</b>.
 * <p>
 * Such login operation is simply entering the user ID, its password and click on the Login button.
 * </p><p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #checkInvalidLoginMessage()}: Check the whether the login operation failed or not.</li>
 * <li>{@link #getLoginButtonLocator()}: Return the locator for the login button to click on to perform the login.</li>
 * <li>{@link #getUserIdLocator()}: Return the locator for the web element to enter user ID.</li>
 * <li>{@link #getUserPasswordLocator()}: Return the locator for the web element to enter user password.</li>
 * <li>{@link #performLogin()}: Perform the login operation on the associated page.</li>
 * </ul>
 * </p>
 */
public class UmsLoginOperation extends SpotAbstractLoginOperation {

	class DisplayedLoginTimeout extends SpotTextTimeout {
		public DisplayedLoginTimeout() {
			super("login", Comparison.EndsWith, true);
		}
		@Override
		public String getText() {
			return getBrowser().getCurrentUrl();
		}
	}

public UmsLoginOperation(final WebPage webPage, final User pageUser) {
	super(webPage, pageUser);
}

@Override
protected void checkInvalidLoginMessage() {
	// Do nothing
}

/**
 * Return the browser associated with the current page.
 *
 * @return The browser as {@link WebBrowser}
 */
public WebBrowser getBrowser() {
	return this.page.getBrowser();
}

@Override
protected By getLoginButtonLocator() {
	return By.cssSelector("[type='submit']");
}

@Override
protected By getUserIdLocator() {
	return By.cssSelector("[name='j_username']");
}

@Override
protected By getUserPasswordLocator() {
	return By.cssSelector("[name='j_password");
}

/**
 * {@inheritDoc}
 * <p>
 * Check whether an error message is displayed in the page prior the login operation.
 * If so, then cancel the scenario execution.
 * </p>
 */
@Override
protected void beforeLogin() throws ScenarioFailedError {
	// Check application error message
	WebBrowserElement errorMessageElement = waitForElement(By.tagName("h1"), false, 1);
	if (errorMessageElement != null) {
		final String errorMessage = errorMessageElement.getText();
		WebBrowserElement alertInfoElement = waitForElement(By.className("alert-info"), false, 0);
		if (alertInfoElement != null) {
			println("ERROR: "+errorMessage);
			println("Alert info:");
			println(alertInfoElement.getText());
			throw new ScenarioFailedError("Got fatal error prior performing login: "+errorMessage);
		}
	}
}

@Override
protected void storeUserAsLoggedInApplication() {

	// Wait while login page is displayed
	DisplayedLoginTimeout timeout = new DisplayedLoginTimeout();
	timeout.waitWhile(5);

	// Store application as soon as login page has vanished
	super.storeUserAsLoggedInApplication();
}

}
