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
 * Error to report Server error message.
 */
public abstract class ServerMessageError extends ScenarioFailedError {

public ServerMessageError(final String message) {
	super(message);
}

public ServerMessageError(final String message, final SpotAbstractDialog dialog) {
	super(message, dialog);
}

/**
 * Returns the error message details if any.
 *
 * @return The server error message details as a {@link String} or
 * <code>null</code> if there's no details to show
 */
public abstract String getDetails();

/**
 * Returns the error message summary.
 *
 * @return The server error message summary as a {@link String}.
 */
public abstract String getSummary();

/**
 * Show the details of the Server error message.
 *
 * @return <code>true</code> if the details message has been shown,
 * <code>false</code> otherwise.
 */
abstract public boolean showDetails();
}
