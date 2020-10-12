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
package com.ibm.bear.qa.spot.core.scenario.errors;

/**
 * Class to manage browser errors.
 */
public class BrowserError extends ScenarioFailedError {

	final boolean fatal;

public BrowserError(final String message) {
	super(message);
	this.fatal = false;
}

public BrowserError(final String message, final boolean fatal) {
	super(message);
	this.fatal = fatal;
}

public BrowserError(final Throwable ex) {
	super(ex);
	this.fatal = false;
}

/**
 * Tell whether the error is fatalor not.
 *
 * @return <code>true</code> if the error is fatal, <code>false</code> otherwise
 */
public boolean isFatal() {
	return this.fatal;
}
}
