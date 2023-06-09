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
package com.ibm.bear.qa.spot.core.scenario.errors;

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;

/**
 * Abstract class to have a common parent to error which would
 * allow the framework to attempt a test retry when it occurs.
 */
public abstract class RetryableError extends ScenarioFailedError {

public RetryableError(final String message) {
	super(message);
}

public RetryableError(final String message, final boolean print) {
	super(message, print);
}

public RetryableError(final String message, final SpotAbstractDialog dialog) {
	super(message, dialog);
}

public RetryableError(final String message, final SpotAbstractDialog dialog, final boolean print) {
	super(message, dialog, print);
}

public RetryableError(final Throwable ex) {
	super(ex);
}

public RetryableError(final Throwable ex, final boolean print) {
	super(ex, print);
}

public RetryableError(final Throwable ex, final SpotAbstractDialog dialog) {
	super(ex, dialog);
}

public RetryableError(final Throwable ex, final SpotAbstractDialog dialog, final boolean print) {
	super(ex, dialog, print);
}

}
