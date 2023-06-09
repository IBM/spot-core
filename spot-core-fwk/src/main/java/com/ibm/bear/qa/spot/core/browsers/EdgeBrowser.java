/*********************************************************************
* Copyright (c) 2012, 2023 IBM Corporation and others.
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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterBooleanValue;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterIntValue;

import java.time.Duration;
import java.util.*;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

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
 * <li>{@link #initDriver()}: Initialize the driver corresponding to the current browser.</li>
 * <li>{@link #initProfile(User)}: Initialize the browser profile.</li>
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
 * @deprecated This method will be removed
 */
@Override
@Deprecated
public boolean acceptPrivateConnection() {
	/* DISABLED as it's now possible to use an option to tell Edge to accept private connection
	WebBrowserElement titleElement = waitForElement(null, By.tagName("h1"), false, 1, true, true);
	if (titleElement != null) {
		if (titleElement.getText().equals("Your connection isn't private")) {
			WebBrowserElement buttonElement = waitForMandatoryDisplayedElement(By.id("details-button"), 10);
			buttonElement.click();
			WebBrowserElement linkElement = waitForMandatoryDisplayedElement(By.id("proceed-link"), 10);
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
	*/
	return super.acceptPrivateConnection();
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
	// Enable the Java 11+ HTTP client
	System.setProperty("webdriver.http.factory", "jdk-http-client");

	// Create driver
	this.driver = new EdgeDriver(this.options);
	this.driver.manage().timeouts().implicitlyWait(Duration.ofMillis(250));
}

@Override
protected void initProfile(final User user) {

	// Created Edge options
	this.options = new EdgeOptions();

	// Build arguments list
	List<String> arguments = new ArrayList<String>();
	// Start browser in maximized mode by default
	if (getParameterBooleanValue("windowMax", true) && getParameterIntValue("windowWidth") == 0 && getParameterIntValue("windowHeight") == 0) {
		arguments.add("--start-maximized");
	}
	arguments.add("--ignore-certificate-errors");
	arguments.add("--lang=en"); // force English locale when starting Edge, TODO Not verified yet
	if (this.manager.isHeadless()) {
		arguments.add("--headless"); // TODO Not verified yet
		arguments.add("--disable-dev-shm-usage"); // overcome limited resource problems
	}

	// Set options arguments
	this.options.addArguments(arguments);

	// Set private mode for browser if requested
	if (this.manager.isInPrivateMode(user)) {
		this.options.addArguments("--inprivate");
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
