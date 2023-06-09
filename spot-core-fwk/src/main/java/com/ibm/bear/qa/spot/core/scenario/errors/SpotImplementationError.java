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
 * A specific error to handle incorrect implementation error in framework.
 * <p>
 * This kind of error should hopefully <b>never</b> occur. It's just a safe guard
 * against wrong code paths used in framework...
 * </p>
 */
public class SpotImplementationError extends ScenarioFailedError {

public SpotImplementationError() {
	super("This part of framework code should never be called or used. Please contact framework owners about this error.");
}

public SpotImplementationError(final String message) {
	super(message+" Please contact framework owners about this error.");
}
}
