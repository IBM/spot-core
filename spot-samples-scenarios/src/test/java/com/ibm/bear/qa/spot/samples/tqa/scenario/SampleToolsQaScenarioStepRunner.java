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

import org.junit.runners.model.InitializationError;

import com.ibm.bear.qa.spot.core.scenario.ScenarioStepRunner;

/**
 * Manage a Sample Tools QA scenario step JUnit run.
 * <p>
 * This is the concrete class of this hierarchy which has to create the specific scenario execution
 * object (see subclass of {@link SampleToolsQaScenarioExecution} if any) in the case the scenario
 * step is executed as a single JUnit test class.
 * </p><p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #startExecution()}: Start the scenario execution.</li>
 * </ul>
 * </p>
 */
public class SampleToolsQaScenarioStepRunner extends ScenarioStepRunner {

public SampleToolsQaScenarioStepRunner(final Class< ? > klass) throws InitializationError {
	super(klass);
}

@Override
protected void startExecution() {
	this.scenarioExecution = new SampleToolsQaScenarioExecution();
}
}
