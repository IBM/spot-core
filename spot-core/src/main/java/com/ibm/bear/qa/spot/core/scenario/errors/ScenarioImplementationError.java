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
 * A specific error to handle incorrect implementation error while running a scenario.
 */
public class ScenarioImplementationError extends ScenarioFailedError {

public ScenarioImplementationError() {
	super("This part of code should never be called or used. Please check your scenario design and implementation.");
}

public ScenarioImplementationError(final String message) {
	super(message);
}

public ScenarioImplementationError(final Exception exception) {
	super(exception);
}

public ScenarioImplementationError(final Exception exception, final boolean print) {
	super(exception, print);
}
}
