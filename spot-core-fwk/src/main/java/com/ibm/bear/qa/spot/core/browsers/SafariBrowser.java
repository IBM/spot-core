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
package com.ibm.bear.qa.spot.core.browsers;

import java.time.Duration;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.safari.SafariDriver;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * The browser class when Safari browser is used to run the tests.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getVersion()}: Get the version information from the currently running browser.</li>
 * <li>{@link #isEdge()}: Tells whether the current browser is Microsoft Edge or not.</li>
 * <li>{@link #isFirefox()}: Tells whether the current browser is Firefox or not.</li>
 * <li>{@link #isGoogleChrome()}: Tells whether the current browser is Google Chrome or not.</li>
 * <li>{@link #isInternetExplorer()}: Tells whether the current browser is Internet Explorer or not.</li>
 * <li>{@link #isSafari()}: Tells whether the current browser is Safari or not.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initDriver()}: Init the driver corresponding to the current browser.</li>
 * <li>{@link #initProfile(User)}: Init the browser profile.</li>
 * </ul>
 * </p>
 */
public class SafariBrowser extends WebBrowser {

public SafariBrowser(final BrowsersManager manager) {
	super(manager);
}

@Override
protected Capabilities getCapabilities() {
	return ((SafariDriver)this.driver).getCapabilities();
}

@Override
public String getDriverInfo() {
	return "Safari driver is included in browser itself, hence same version";
}

@Override
protected void initDriver() {
	this.driver = new SafariDriver();
	this.driver.manage().timeouts().implicitlyWait(Duration.ofMillis(250));
}

@Override
protected void initProfile(final User user) {
	// Do nothing
}

@Override
public boolean isEdge() {
	return false;
}

@Override
public boolean isFirefox() {
	return false;
}

@Override
public boolean isGoogleChrome() {
	return false;
}

@Override
public boolean isInternetExplorer() {
	return false;
}

@Override
public boolean isSafari() {
	return true;
}
}
