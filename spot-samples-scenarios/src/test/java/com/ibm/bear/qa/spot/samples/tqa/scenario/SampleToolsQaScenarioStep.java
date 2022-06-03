/*********************************************************************
* Copyright (c) 2020, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.samples.tqa.scenario;

import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.ScenarioStep;
import com.ibm.bear.qa.spot.samples.config.SpotSamplesConfig;

/**
 * Class to manage common behavior of any Sample Tools QA scenario step.
 * <p>
 * Internal methods overridden by this step are:
 * <ul>
 * </ul>
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getData()}: Return the scenario data to use during the run.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getConfig()}: Return the scenario configuration to use during the run.</li>
 * <li>{@link #getToolsQaOperation()}: Return a new scenario operation.</li>
 * <li>{@link #getUser()}: Return the user used by scenario.</li>
 * </ul>
 * </p>
 */
public abstract class SampleToolsQaScenarioStep extends ScenarioStep {

@Override
protected SpotSamplesConfig getConfig() {
	return (SpotSamplesConfig) super.getConfig();
}

@Override
public SampleToolsQaScenarioData getData() {
	return (SampleToolsQaScenarioData) super.getData();
}

/**
 * Return the scenario operation.
 *
 * @return The operation
 */
protected ToolsQaScenarioOperation getToolsQaOperation() {
	return getOperation(ToolsQaScenarioOperation.class);
}

/**
 * Return the user used by scenario.
 *
 * @return The user
 */
protected User getUser() {
	return (User) getData().getUsers().get(0);
}
}