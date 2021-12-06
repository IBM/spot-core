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

import com.ibm.bear.qa.spot.core.scenario.ScenarioExecution;
import com.ibm.bear.qa.spot.core.scenario.ScenarioStep;

/**
 * Annotation to identify non-failure blocker test in failure blocker step.
 * <p>
 * When this annotation is used on a test, it relaxes the blocking status got from
 * the step (if any). Then, even if the entire step is considered as failure blocker,
 * the test tagged with the current annotation will <b>not</b> stop the scenario
 * execution in case it fails (except if the STOP_ON_FAILURE_ID argument
 * (see {@link ScenarioExecution}) value is set to <code>true</code>.
 * </p><p>
 * Note that this annotation is necessary only  when a {@link FailureBlocker}
 * annotation has been used on a step class (ie. subclass of {@link ScenarioStep}).
 * Otherwise that does not bring any additional behavior.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FailureRelaxer {

}
