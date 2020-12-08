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
package com.ibm.bear.qa.spot.core.browser;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.WebElement;

import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * This interface defines constants used for browsers.
 */
public interface BrowserConstants {

	// Parameters IDs
	String BROWSER_PATH_ID = "browserPath";
	String BROWSER_PROFILE_ID = "browserProfile";
	String BROWSER_KIND_ID = "browserKind";
	String BROWSER_LOCALE_ID = "browserLocale";
	String BROWSER_HEADLESS_ID = "browserHeadless";

	// Browser kinds
	int BROWSER_KIND_FIREFOX = 1;
	int BROWSER_KIND_IEXPLORER = 2;
	int BROWSER_KIND_GCHROME = 3;
	int BROWSER_KIND_MSEDGE = 4;
	int BROWSER_KIND_SAFARI = 5;

	// Browser Versions
	String FIREFOX_52ESR = "52.9.0";
	String FIREFOX_60ESR = "60.9.0";

	// Browser default download directory
	String BROWSER_DOWNLOAD_DIR_ID = "download.dir";

	// Screenshots directories
	String SPOT_SCREENSHOT_DIR_ID = "spot.screenshots.dir";
	String SPOT_SCREENSHOT_DIR_DEFAULT = "screenshots";
	int INFO_SCREENSHOT = 0;
	int WARNING_SCREENSHOT = 1;
	int FAILURE_SCREENSHOT = 2;

	// Browser sessions
	String NEW_BROWSER_SESSION_PER_USER = "newBrowserSessionPerUser";
	String SINGLE_BROWSER_PER_USER_ID = "browser.single.per.user";

	// Window size
	int DEFAULT_HEIGHT = 900;
	int MIN_HEIGHT = 900;
	int MAX_HEIGHT = 1200;
	int DEFAULT_WIDTH = 1600;
	int MIN_WIDTH = 1200;
	int MAX_WIDTH = 1920;

	// Others
	public static final List<WebElement> NO_ELEMENT_FOUND = Collections.emptyList();
	public static final List<WebBrowserElement> NO_BROWSER_ELEMENT_FOUND = Collections.emptyList();
}
