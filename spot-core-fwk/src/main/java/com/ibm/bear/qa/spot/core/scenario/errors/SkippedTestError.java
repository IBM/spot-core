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

import com.ibm.bear.qa.spot.core.utils.StepBlocker;

/**
 * Error raised when a test is skipped due to an error occurred in a previous step test.
 *
 * @see StepBlocker
 */
public class SkippedTestError extends ScenarioFailedError {

public SkippedTestError(final String message) {
	super(message);
}
}
