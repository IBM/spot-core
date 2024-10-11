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
package com.ibm.bear.qa.spot.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.ibm.bear.qa.spot.core.scenario.ScenarioExecution;
import com.ibm.bear.qa.spot.core.scenario.ScenarioStep;

/**
 * Annotation to identify step and/or test blocking scenario execution when
 * failing.
 * <p>
 * A <b>failure blocker</b> test must pass. In case it fails, then the scenario
 * execution will stop whatever the STOP_ON_FAILURE_ID argument value is
 * (see {@link ScenarioExecution}).
 * </p><p>
 * When set on a step class (ie. subclass of {@link ScenarioStep}), it means
 * that <b>all</b> tests will be considered as failure blockers. This can be
 * overridden for any test in such step by using the {@link FailureRelaxer} interface.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FailureBlocker {

}
