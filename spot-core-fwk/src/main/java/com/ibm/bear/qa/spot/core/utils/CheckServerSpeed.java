/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to identify whether a test will also check whether or not the server
 * is considered slow; argument indicates the max time (in seconds) that this
 * specific test should take.  A test that takes longer indicates a slower server;
 * a test that takes less time indicates the server is behaving normally.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckServerSpeed {
	// Max time for some specific test, in seconds.
	int value();
}
