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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioMissingImplementationError;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * The specialized class when Internet Explorer browser is used to run the tests.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getVersion()}: Get the version information from the currently running browser.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initDriver()}: Init the driver corresponding to the current browser.</li>
 * <li>{@link #initProfile()}: Init the browser profile.</li>
 * </ul>
 * </p>
 */
@Deprecated
public class InternetExplorerBrowser extends WebBrowser {

public InternetExplorerBrowser(final BrowsersManager manager) {
	super(manager);
}

@Override
protected void initDriver() {
	System.setProperty("webdriver.ie.driver", this.manager.getPath());
	DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
	ieCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
	// Create driver
	/* Selenium 2.53.1
	this.driver = new InternetExplorerDriver(ieCapabilities);
	*/
	this.driver = new InternetExplorerDriver(new InternetExplorerOptions(ieCapabilities));
}

@Override
protected Capabilities getCapabilities() {
	return ((InternetExplorerDriver)this.driver).getCapabilities();
}

@Override
public String getDriverInfo() {
	throw new ScenarioMissingImplementationError("Internet explorer is deprecated and will be removed soon...");
}

@Override
protected void initProfile() {
	// Do nothing
}
}
