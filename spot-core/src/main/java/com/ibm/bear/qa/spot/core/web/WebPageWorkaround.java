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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

import com.ibm.bear.qa.spot.core.scenario.ScenarioWorkaround;

public class WebPageWorkaround extends ScenarioWorkaround<WebPage> {

public WebPageWorkaround(final WebPage page, final String msg) {
	super(page, msg);
}

public WebPageWorkaround(final WebPage page, final String msg, final boolean fail) {
	super(page, msg, fail);
}

@Override
public WebBrowserElement execute() {
	debugPrintln("Workaround: try to refresh the entire page...");
	this.page.refresh();
	return null;
}

}
