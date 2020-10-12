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
package com.ibm.bear.qa.spot.core.scenario;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.*;

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;
import com.ibm.bear.qa.spot.core.scenario.errors.WaitElementTimeoutError;
import com.ibm.bear.qa.spot.core.web.*;

/**
 * Manage workaround applied when running a scenario.
 * <p>
 * </p>
 * Design Need to be finalized Workaround
 */
abstract public class ScenarioWorkaround<P extends WebPage> {

	/* Constants */
	private final static Set<String> WORKAROUNDED_PAGES = new HashSet<String>();

	/* Fields */
	private String message;
	private boolean shouldFail;
	private long id;
	protected P page;
	protected SpotAbstractDialog dialog;

public ScenarioWorkaround(final P page, final String msg) {
	this(page, msg, true, null);
}

public ScenarioWorkaround(final P page, final String msg, final boolean fail) {
	this(page, msg, fail, null);
}

public ScenarioWorkaround(final P page, final String msg, final boolean fail, final SpotAbstractDialog dialog) {
	this(page, msg, fail, dialog, true /* report */);
}

public ScenarioWorkaround(final P page, final String msg, final boolean fail, final SpotAbstractDialog dialog, final boolean report) {
	this.message = msg;
	this.page = page;
	this.dialog = dialog;
	this.id = System.currentTimeMillis();
	this.shouldFail = fail;

	if (report) {
		println("WORKAROUND: " + this.message);
		page.takeSnapshotWarning(getClassSimpleName(page.getClass()) + "_Workaround");
	}

	if (WORKAROUNDED_PAGES.contains(page.getLocation())) {
		if (fail) {
			if (dialog != null) {
				dialog.cancel();
			}
			throw new WaitElementTimeoutError(this.message);
		}
	} else {
		WORKAROUNDED_PAGES.add(page.getLocation());
	}
}

public ScenarioWorkaround(final P page, final String msg, final SpotAbstractDialog dialog) {
	this(page, msg, true, dialog);
}

/**
 * Execute an action to workaround the failure.
 * <p>
 * Subclass has to specify what to do to workaround the problem.
 * </p>
 */
abstract public WebBrowserElement execute();

/**
 * Return the web browser.
 *
 * @return The browser as a {@link WebBrowser}.
 */
public WebBrowser getBrowser() {
	return this.page.getBrowser();
}

/**
 * Get the workaround timestamp.
 *
 * @return The timestamp as a {@link String} with 'YYYYMMDD-HHMMSS' format.
 */
public String getTimestamp() {
	return COMPACT_DATE_FORMAT.format(new Date(this.id));
}

/**
 * Returns whether the current workaround should raise a failure at the end of the test execution.
 *
 * @return <code>true</code> if the workaround should raise a failure, <code>false</code> otherwise.
 */
public boolean shouldFail() {
	return this.shouldFail;
}

@Override
public String toString() {
	final String kind = this.shouldFail ? "failure" : "normal";
	return ("WORKAROUND: time creation=" + getTimestamp() + "', message= '" + this.message + "', kind=" + kind);
}
}
