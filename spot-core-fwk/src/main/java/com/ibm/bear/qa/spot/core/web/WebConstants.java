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
package com.ibm.bear.qa.spot.core.web;

import org.openqa.selenium.By;

/**
 * Usual constants used by framework web objects.
 */
public interface WebConstants {

	// Common strings
	public static final String OK = "OK";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String ENABLED = "enabled";
	public static final String DISABLED = "disabled";
	public static final String NAME = "Name";
	public static final String TITLE = "title";

	// Common locators
	public static final By TAG_NAME_H1 = By.tagName("h1");
}
