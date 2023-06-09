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

/**
 * A specific error to handle login error while running a scenario.
 */
public class BrowserConnectionError extends BrowserError {

public BrowserConnectionError(final String message) {
	super(message);
}

public BrowserConnectionError(final Throwable ex) {
	super(ex);
}
}
