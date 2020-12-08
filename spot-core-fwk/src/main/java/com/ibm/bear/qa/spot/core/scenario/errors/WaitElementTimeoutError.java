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

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;

/**
 * Error sent when a timeout is reached while waiting for an information in
 * the corresponding page (e.g. a web element).
 */
public class WaitElementTimeoutError extends RetryableError {

public WaitElementTimeoutError(final String message) {
	super(message);
}

public WaitElementTimeoutError(final String message, final SpotAbstractDialog dialog) {
	super(message, dialog);
}

public WaitElementTimeoutError(final Throwable ex) {
	super(ex);
}

public WaitElementTimeoutError(final Throwable ex, final SpotAbstractDialog dialog) {
	super(ex, dialog);
}

}
