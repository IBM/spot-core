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
package com.ibm.bear.qa.spot.core.browsers;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.print;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.println;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.web.WebBrowser;
/**
 * The specialized class when Google Chrome browser is used to run the tests.
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
 * <li>{@link #initProfile()}: Init the browser profile.</li>
 * </ul>
 * </p>
 */
public class GoogleChromeBrowser extends WebBrowser {

	/* Fields */
	private ChromeOptions options;

public GoogleChromeBrowser(final BrowsersManager manager) {
	super(manager);
}

@Override
protected Capabilities getCapabilities() {
	return ((ChromeDriver)this.driver).getCapabilities();
}

@Override
@SuppressWarnings("rawtypes")
public String getDriverInfo() {
	Capabilities capabilities = getCapabilities();
	String info = "Chrome driver version: ";
	Map chrome = (Map) capabilities.getCapability("chrome");
	return info + chrome.get("chromedriverVersion");
}

private void initDownloadDir() {

	// Set experimental options
	Map<String, Object> prefs = new HashMap<String, Object>();
	// Default download directory
	prefs.put("download.default_directory", getDownloadDir());
	prefs.put("download.directory_upgrade", new Boolean(true));
	// No prompt while download a file
	prefs.put("download.prompt_for_download", new Boolean(false));
	// Set above options
	this.options.setExperimentalOption("prefs", prefs);
}

@Override
protected void initDriver() {

	// Set driver properties
	System.setProperty("webdriver.chrome.driver", this.manager.getPath());
	System.setProperty("webdriver.chrome.silentOutput", "true");

    // Create driver
	this.driver = new ChromeDriver(this.options);
	this.driver.manage().timeouts().implicitlyWait(250, TimeUnit.MILLISECONDS);
}

@Override
protected void initProfile() {

	// Set options
	this.options = new ChromeOptions();

	List<String> arguments = new ArrayList<String>();
	// Start browser in maximized mode
	arguments.add("--start-maximized");
	arguments.add("--ignore-certificate-errors");
	arguments.add("--lang=en"); // force English locale when starting Chrome
	if (this.manager.isHeadless()) {
		arguments.add("--headless");
		arguments.add("--disable-dev-shm-usage"); // overcome limited resource problems
	}

	// A default download directory can not be set via the setExperimentalOptions method
	// if a custom profile is used for the test execution. Therefore, a profile directory
	// is only specified for the test execution if a default download directory is not
	// provided. In other words, the default profile will be used for the test execution
	// if a default download directory is provided.
	if (this.manager.getProfile() != null) {
		if (hasDownloadDir()) {
			print("		+ A download directory for the browser is specified via parameter ");
			print(BROWSER_DOWNLOAD_DIR_ID);
			print(". Therefore, the default browser profile is used for the test execution even though a custom browser profile is provided via parameter ");
			println(BROWSER_PROFILE_ID);
		} else {
			arguments.add("--user-data-dir=" + this.manager.getProfile());
		}
	}

	this.options.addArguments(arguments);

	// Init download dir if necessary
	if (hasDownloadDir()) {
		initDownloadDir();
	}

	// Set private mode for browser if requested
	if (this.manager.isInPrivateMode()) {
		this.options.addArguments("--incognito");
	}
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
	return true;
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
