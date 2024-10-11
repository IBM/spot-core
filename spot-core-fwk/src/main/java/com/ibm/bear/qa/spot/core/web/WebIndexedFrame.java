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
package com.ibm.bear.qa.spot.core.web;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.DEBUG;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

/**
 * Class to manage browser frame identified with an index.
 */
public class WebIndexedFrame extends WebBrowserFrame {

	/**
	 * The index of the current frame.
	 */
	private int index = -1;

WebIndexedFrame(final WebBrowser browser, final int index) {
	super(browser);
    this.index = index;
}

@Override
public boolean equals(final Object obj) {
	if (obj instanceof WebIndexedFrame) {
		WebIndexedFrame frame = (WebIndexedFrame) obj;
		return frame.index == this.index;
	}
	return super.equals(obj);
}

@Override
int getIndex() {
	return this.index;
}

@Override
public int hashCode() {
	return this.index;
}

/**
 * Select current frame.
 */
@Override
void switchTo() {
	if (DEBUG) debugPrintln("		+ Switch to "+this);
	this.driver.switchTo().defaultContent();
	this.driver.switchTo().frame(this.index);

}

@Override
public String toString() {
	return "Frame indexed "+this.index;
}

}
