/*********************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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

import com.ibm.bear.qa.spot.core.scenario.ScenarioExecution;
import com.ibm.bear.qa.spot.samples.config.SpotSamplesConfig;

/**
 * Manage a Sample Tools QA scenario execution.
 * <p>
 * This is the concrete class of this hierarchy which has to create the specific scenario
 * configuration and data object respectively in {@link #initConfig()} and {@link #initData()}
 * method.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getConfig()}: Return the scenario configuration to use during the run.</li>
 * <li>{@link #getData()}: Return the scenario data to use during the run.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initConfig()}: Initialize the configuration.</li>
 * <li>{@link #initData()}: Initialize the data.</li>
 * </ul>
 * </p>
 * 
 * @see SampleToolsQaScenarioData
 */
public class SampleToolsQaScenarioExecution extends ScenarioExecution {

public SampleToolsQaScenarioExecution() {
}

@Override
public SpotSamplesConfig getConfig() {
	return (SpotSamplesConfig) super.getConfig();
}

@Override
public SampleToolsQaScenarioData getData() {
	return (SampleToolsQaScenarioData) super.getData();
}

@Override
protected void initConfig() {
	this.config = new SpotSamplesConfig();
}

@Override
protected void initData() {
	this.data = new SampleToolsQaScenarioData();
}
}
