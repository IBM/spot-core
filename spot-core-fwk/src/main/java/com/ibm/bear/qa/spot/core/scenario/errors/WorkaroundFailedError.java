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
package com.ibm.bear.qa.spot.core.scenario.errors;

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;

/**
 * Class to manage error occurring when a workaround fails.
 */
public class WorkaroundFailedError extends RetryableError {

public WorkaroundFailedError(final String message) {
	super(message);
}

public WorkaroundFailedError(final String message, final boolean print) {
	super(message, print);
}

public WorkaroundFailedError(final String message, final SpotAbstractDialog dialog) {
	super(message, dialog);
}

public WorkaroundFailedError(final String message, final SpotAbstractDialog dialog, final boolean print) {
	super(message, dialog, print);
}

public WorkaroundFailedError(final Throwable ex) {
	super(ex);
}

public WorkaroundFailedError(final Throwable ex, final boolean print) {
	super(ex, print);
}

public WorkaroundFailedError(final Throwable ex, final SpotAbstractDialog dialog) {
	super(ex, dialog);
}

public WorkaroundFailedError(final Throwable ex, final SpotAbstractDialog dialog, final boolean print) {
	super(ex, dialog, print);
}

}
