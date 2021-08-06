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
package com.ibm.bear.qa.spot.core.browsers;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.sleep;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.web.WebBrowser;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * The specialized class when MS Edge browser is used to run the tests.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #acceptPrivateConnection()}: Accept private connection if any.</li>
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
 * <li>{@link #initProfile()}: Init the browser profile.</li>
 * </ul>
 * </p>
 */
public class EdgeBrowser extends WebBrowser {

	/* Fields */
	private EdgeOptions options;

public EdgeBrowser(final BrowsersManager manager) {
	super(manager);
}

/**
 * Accept private connection if any.
 *
 * @return <code>true</code> if a private connection was accepted,
 * <code>false</code> otherwise
 */
@Override
public boolean acceptPrivateConnection() {
	WebBrowserElement titleElement = waitForElement(null, By.tagName("h1"), /*fail: */false, 1, /*displayed: */true, /*single: */true);
	if (titleElement != null) {
		if (titleElement.getText().equals("Your connection isn't private")) {
			WebBrowserElement buttonElement = waitForElement(By.id("details-button"), 10);
			buttonElement.click();
			WebBrowserElement linkElement = waitForElement(By.id("proceed-link"), 10);
			linkElement.click();
			sleep(2);
			if (buttonElement.isDisplayed()) {
				buttonElement.click();
				linkElement.click();
			}
			return true;
		}
	}
	return false;
}

@Override
protected Capabilities getCapabilities() {
	return ((EdgeDriver)this.driver).getCapabilities();
}

@Override
@SuppressWarnings("rawtypes")
public String getDriverInfo() {
	Capabilities capabilities = getCapabilities();
	String info = "Edge driver version: ";
	Map msedge = (Map) capabilities.getCapability("msedge");
	return info + msedge.get("msedgedriverVersion");
}

@Override
protected void initDriver() {

	// Set driver properties
	System.setProperty("webdriver.edge.driver", this.manager.getPath());

	// Create driver
	this.driver = new EdgeDriver(this.options);
	this.driver.manage().timeouts().implicitlyWait(250, TimeUnit.MILLISECONDS);
}

@Override
protected void initProfile() {

	// Created Edge options
	this.options = new EdgeOptions();

	// Set private mode for browser if requested
	if (this.manager.isInPrivateMode()) {
		throw new ScenarioFailedError("MS Edge is not supporting private mode with Selenium 3.");
	}
}

@Override
public boolean isEdge() {
	return true;
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
	return false;
}
}
