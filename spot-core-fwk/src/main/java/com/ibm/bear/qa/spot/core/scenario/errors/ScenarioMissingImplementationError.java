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
 * A specific error to handle missing implementation error while running a scenario.
 */
public class ScenarioMissingImplementationError extends ScenarioFailedError {

public ScenarioMissingImplementationError(final String message) {
	super(message);
}

public ScenarioMissingImplementationError(final Throwable ex) {
	super(ex);
}

public ScenarioMissingImplementationError(final Throwable ex, final boolean print) {
	super(ex, print);
}

public ScenarioMissingImplementationError(final StackTraceElement whoAmI) {
	super("Missing implementation of "+whoAmI.toString());
}
}
