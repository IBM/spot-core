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
package com.ibm.bear.qa.spot.core.web;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.DEBUG;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to manage browser frame embedded in another frame.
 */
public class WebEmbeddedFrame extends WebElementFrame {

	private WebElementFrame parentFrame;

public WebEmbeddedFrame(final WebBrowser browser, final WebBrowserFrame frame, final WebBrowserElement element) {
	super(browser, element);
	WebBrowserFrame browserFrame = frame;
	if (browserFrame == null) {
		throw new ScenarioFailedError("An embedded frame must have a parent.");
	}
	if (browserFrame instanceof WebElementFrame) {
		this.parentFrame = (WebElementFrame) browserFrame;
	} else {
		throw new ScenarioFailedError("Invalid class for parent frame: "+browserFrame.getClass());
	}
}

@Override
void switchTo() {
	switchToParent();
	this.driver.switchTo().frame(getElement().getWebElement());
}

/**
 * Switch to parent frame.
 *
 * @return The selected parent frame as a {@link WebElementFrame}.
 */
public WebElementFrame switchToParent() {
	if (DEBUG) debugPrintln("		+ Switch to "+this);
	this.parentFrame.switchTo();
	return this.parentFrame;
}

}
