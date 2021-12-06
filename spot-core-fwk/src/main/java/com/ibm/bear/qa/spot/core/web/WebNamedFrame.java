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
package com.ibm.bear.qa.spot.core.web;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.DEBUG;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

/**
 * Class to manage browser frame identified with a name.
 */
public class WebNamedFrame extends WebBrowserFrame {

	/**
	 * The name of the current frame.
	 */
	private String name;

protected WebNamedFrame(final WebBrowser browser, final String name) {
	super(browser);
    this.name = name;
}

@Override
public boolean equals(final Object obj) {
	if (obj instanceof WebNamedFrame) {
		WebNamedFrame frame = (WebNamedFrame) obj;
		return this.name.equals(frame.name);
	}
	if (obj instanceof String) {
		return this.name.equals(obj);
	}
	return super.equals(obj);
}

@Override
String getName() {
	return this.name;
}

@Override
public int hashCode() {
	return this.name.hashCode();
}

/**
 * Select current frame.
 */
@Override
void switchTo() {
	if (DEBUG) debugPrintln("		+ Switch to "+this);
	this.driver.switchTo().defaultContent();
	this.driver.switchTo().frame(this.name);

}

@Override
public String toString() {
	return "Frame named '"+this.name+"'";
}

}
