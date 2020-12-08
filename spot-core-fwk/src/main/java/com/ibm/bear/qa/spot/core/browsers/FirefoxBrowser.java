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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintEnteringMethod;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;
import static org.openqa.selenium.firefox.GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY;

import java.io.File;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.remote.CapabilityType;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.scenario.errors.BrowserError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.utils.FileUtil;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * The specialized class when Firefox browser is used to run the tests.
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
public class FirefoxBrowser extends WebBrowser {

	// Firefox specific info
	FirefoxProfile firefoxProfile;

public FirefoxBrowser(final BrowsersManager manager) {
	super(manager);
}

@Override
protected Capabilities getCapabilities() {
	return ((FirefoxDriver)this.driver).getCapabilities();
}

@Override
public String getDriverInfo() {
	Capabilities capabilities = getCapabilities();
	String info = "Gecko driver version: ";
	return info + capabilities.getCapability("moz:geckodriverVersion");
}

private void initDownloadDir() {

	// Don't show download manager
	this.firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);

	// # 0 means to download to the desktop,
	// 1 means to download to the default "Downloads" directory,
	// 2 means to use the directory you specify in "browser.download.dir"
	this.firefoxProfile.setPreference("browser.download.folderList", 2);

	// Without asking a location, download files to the directory specified in browser.download.folderList
	this.firefoxProfile.setPreference("browser.download.useDownloadDir", true);

	// Set download directory.
	this.firefoxProfile.setPreference("browser.download.dir", getDownloadDir());

	// Never ask when saving zip files
	this.firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk",
	    "application/x-xpinstall;application/x-zip;application/x-zip-compressed;application/octet-stream;application/zip;application/pdf;application/msword;text/plain;application/octet");
}

@Override
protected void initDriver() {
	debugPrintEnteringMethod();

	// Initialize Gecko driver if specified
	String geckoDriverPath = System.getProperty(GECKO_DRIVER_EXE_PROPERTY);
	boolean useGeckoDriver = geckoDriverPath != null && geckoDriverPath.trim().length() > 0;
	if (useGeckoDriver) {
		// Check executable
		File geckoDriverFile = new File(geckoDriverPath);
		if (!geckoDriverFile.exists()) {
			throw new ScenarioFailedError("Gecko driver executable '"+geckoDriverPath+" defined by System property '"+GECKO_DRIVER_EXE_PROPERTY+"' does not exist! Check that property value points to a valid gecko driver executable file.");
		}
		if (!geckoDriverFile.canExecute()) {
			throw new ScenarioFailedError("Gecko driver executable '"+geckoDriverPath+" defined by System property '"+GECKO_DRIVER_EXE_PROPERTY+"' does not seem to be an executable file! Check that property value points to a valid gecko driver executable file.");
		}
		debugPrintln("		  -> Using Gecko driver located at: "+geckoDriverFile.getAbsolutePath());

		// Disable Gecko log output in error console
		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
	} else {
		throw new BrowserError("Firefox since version 60 cannot work without having set '"+GECKO_DRIVER_EXE_PROPERTY+"'!", /*fatal:*/true);
	}

	// Initialize Firefox binary
	FirefoxBinary firefoxBinary = this.manager.getPath() == null ? new FirefoxBinary() : new FirefoxBinary(new File(this.manager.getPath()));
	if (this.manager.isHeadless()) {
		firefoxBinary.addCommandLineOptions("--headless");
	}

	// Initialize Firefox capabilities
	MutableCapabilities firefoxCapabilities = new MutableCapabilities();
	firefoxCapabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

	// Initialize Firefox options
	FirefoxOptions firefoxOptions = new FirefoxOptions()
//			.setLogLevel(FirefoxDriverLogLevel.WARN)
			.setProfile(this.firefoxProfile)
			.setBinary(firefoxBinary);
			// TODO Uncomment when version Selenium 4 will be used (see https://github.com/SeleniumHQ/selenium/issues/5540)
//			.addPreference("browser.link.open_newwindow.restriction", 0)
//			.addPreference("browser.link.open_newwindow", 1)
	firefoxOptions.merge(firefoxCapabilities);
	debugPrintln("		  -> firefox options: "+firefoxOptions);

	// Create the driver
	this.driver = new FirefoxDriver(firefoxOptions);
}

@Override
protected void initProfile() {

	// Set profile
	if (this.manager.getProfile() == null) {
		this.firefoxProfile = new FirefoxProfile();
	} else {
		File dir = FileUtil.createDir(this.manager.getProfile());
		if (dir == null) {
			throw new ScenarioFailedError("Cannot create firefox profile at "+this.manager.getProfile()+"!");
		}
		this.firefoxProfile = new FirefoxProfile(dir);
	}
	this.firefoxProfile.setPreference("enableNativeEvents", false);

	// Init download dir if necessary
	if (hasDownloadDir()) {
		initDownloadDir();
	}

	// Set browser locale
	if (this.manager.getLocale() != null) {
		this.firefoxProfile.setPreference("intl.accept_languages", this.manager.getLocale());
	}
}

@Override
public boolean isEdge() {
	return false;
}

@Override
public boolean isFirefox() {
	return true;
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
