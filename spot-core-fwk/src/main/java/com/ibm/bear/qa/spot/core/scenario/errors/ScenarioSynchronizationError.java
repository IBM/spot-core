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

/**
 * Error class used when a problem occurs in framework scenario synchronization.
 * <p>
 * Basically using this error intends to stop scenario execution immediately, typically
 * when synchronization is needed but JMS initialization failed for some reason...
 * </p>
 */
public class ScenarioSynchronizationError extends ScenarioFailedError {

public ScenarioSynchronizationError(final String message) {
	super(message);
}

public ScenarioSynchronizationError(final Throwable ex) {
	super(ex);
}

}
