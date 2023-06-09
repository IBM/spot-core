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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.ibm.bear.qa.spot.core.scenario.errors.SkippedTestError;

/**
 * Annotation to identify a test blocking step execution when failing.
 * <p>
 * A <b>step blocker</b> test is intended to pass. In case it fails, then
 * the execution of remaining step tests will be skipped.<br>
 * Note that remaining tests will fail with specific {@link SkippedTestError} error.
 * </p><p>
 * Comparing to {@link FailureBlocker} annotation, that does not stop the entire
 * scenario execution but directly switch to next step execution when a failure
 * is encountered in a step test.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StepBlocker {

}
