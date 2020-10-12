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
 * Class to manage intermittent error which would deserve a test retry when such
 * error occurs.
 */
public class IntermittentError extends RetryableError {

public IntermittentError(final String message) {
	super(message);
}

public IntermittentError(final Throwable ex) {
	super(ex);
}
}
