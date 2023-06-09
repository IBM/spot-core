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
package com.ibm.bear.qa.spot.core.utils;

/**
 * Annotation policy to close browsers.
 * <p>
 * The constants of this enumerated type describe the various policies for closing
 * browsers depending on the step or the test on which they are defined.
 * </p>
 */
public enum CloseBrowsersPolicy {
	/**
	 * Policy used to close browsers <b>after</b> each test of the step on which
	 * the annotation is defined.
	 * <p>
	 * Note that using this annotation on a test method will be equivalent to use
	 * {@link #AT_END} value.
	 * </p>
	 */
	ON_EACH_TEST,
	/**
	 * Policy used to close browsers <b>before</b> a test or a step on which
	 * the annotation is defined.
	 */
	AT_START,
	/**
	 * Policy used to close browsers <b>after</b> a test or a step on which
	 * the annotation is defined.
	 */
	AT_END,
}
