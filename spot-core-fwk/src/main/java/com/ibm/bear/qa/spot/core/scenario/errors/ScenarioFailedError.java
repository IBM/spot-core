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
package com.ibm.bear.qa.spot.core.scenario.errors;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import com.ibm.bear.qa.spot.core.api.elements.SpotDialog;
import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;
import com.ibm.bear.qa.spot.core.scenario.ScenarioUtils;

import junit.framework.AssertionFailedError;

/**
 * Manage scenario failure.
 * <p>
 * Nothing special is done, this class has just been created to let users identify
 * this framework specific error and catch it if necessary.
 * </p>
 * Design: To be finalized
 */
public class ScenarioFailedError extends AssertionFailedError {
	final Throwable error;
	protected SpotAbstractDialog dialog;

public ScenarioFailedError(final String message) {
	this(message, false);
}

public ScenarioFailedError(final String message, final boolean print) {
	this(message, null, print);
}

public ScenarioFailedError(final String message, final SpotAbstractDialog dialog) {
	this(message, dialog, false);
}

public ScenarioFailedError(final String message, final SpotAbstractDialog dialog, final boolean print) {
	super(message);
	if (print) {
		println(message);
		ScenarioUtils.printStackTrace(1);
	} else {
		debugPrintln(message);
	}
	this.error = null;
	this.dialog = dialog;
}

public ScenarioFailedError(final Throwable ex) {
	this(ex, false);
}

public ScenarioFailedError(final Throwable ex, final boolean print) {
	this(ex, null, print);
}

public ScenarioFailedError(final Throwable ex, final SpotAbstractDialog dialog) {
	this(ex, dialog, false);
}

public ScenarioFailedError(final Throwable ex, final SpotAbstractDialog dialog, final boolean print) {
	super(ex.getMessage() == null ? getClassSimpleName(ex.getClass()) : ex.getMessage());
	this.error = ex;
	this.dialog = dialog;
	if (print) {
		printException(ex);
	} else {
		debugPrintException(ex);
	}
}

/**
 * Cancels the dialog where the error msg is displayed.
 */
public void cancel() {
	if (this.dialog != null) {
		this.dialog.cancel();
	}
}

/**
 * Return the associated dialog.
 *
 * @return the dialog or <code>null</code> if no dialog is associated with the error
 */
public SpotDialog getDialog() {
	return this.dialog;
}
}
