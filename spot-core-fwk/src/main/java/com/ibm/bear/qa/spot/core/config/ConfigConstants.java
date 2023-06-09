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
package com.ibm.bear.qa.spot.core.config;

/**
 * This interface defines constants used by framework configuration.
 */
public interface ConfigConstants {

	// Properties used for screenshots directories
	String SELENIUM_SCREENSHOT_NONFAILURE_DIR_ID = "selenium.screenshot.nonfailure.dir";
	String PASSING_SCREENSHOTS_DIRECTORY_ID = "passingScreenshotsDirectory";
	String SELENIUM_SCREENSHOT_DIR_ID = "selenium.screenshot.dir";
	String FAILURE_SCREENSHOTS_DIRECTORY_ID = "failureScreenshotsDirectory";
}
