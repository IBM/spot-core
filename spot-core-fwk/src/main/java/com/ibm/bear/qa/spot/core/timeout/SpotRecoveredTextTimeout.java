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
package com.ibm.bear.qa.spot.core.timeout;

import com.ibm.bear.qa.spot.core.utils.StringUtils.Comparison;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

public class SpotRecoveredTextTimeout extends SpotTextTimeout {

public SpotRecoveredTextTimeout(final String text) {
	super(text);
	this.recovery = true;
}

public SpotRecoveredTextTimeout(final String text, final Comparison compare, final WebBrowserElement webElement) {
	super(text, compare, webElement);
	this.recovery = true;
}

public SpotRecoveredTextTimeout(final String text, final WebBrowserElement webElement) {
	super(text, webElement);
	this.recovery = true;
}

public SpotRecoveredTextTimeout(final String text, final WebBrowserElement webElement, final boolean fail) {
	super(text, webElement, fail);
	this.recovery = true;
}

public SpotRecoveredTextTimeout(final String text, final Comparison compare, final WebBrowserElement webElement, final boolean fail) {
	super(text, compare, webElement, fail);
	this.recovery = true;
}

public SpotRecoveredTextTimeout(final WebBrowserElement webElement) {
	super(webElement);
	this.recovery = true;
}

public SpotRecoveredTextTimeout(final WebBrowserElement webElement, final boolean fail) {
	super(webElement, fail);
	this.recovery = true;
}

}
