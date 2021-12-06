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

import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.ibm.bear.qa.spot.core.scenario.ScenarioRunner;

/**
 * Manage a Sample Tools QA scenario JUnit run.
 * <p>
 * This is the concrete class of this hierarchy which has to create the specific scenario execution
 * object (see subclass of {@link SampleToolsQaScenarioExecution} if any).
 * </p><p>
 * Secondarily, it also defines the name of the main suite which is displayed in the JUnit view when
 * launching it.
 * </p><p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #getName()}: Returns a name used to describe this Runner</li>
 * <li>{@link #startExecution()}: Start the scenario execution.</li>
 * </ul>
 * </p>
 */
public class SampleToolsQaScenarioRunner extends ScenarioRunner {

public SampleToolsQaScenarioRunner(final Class< ? > klass, final RunnerBuilder builder) throws InitializationError {
	super(klass, builder);
}

@Override
protected String getName() {
	return "Sample Tools QA Demo";
}

@Override
protected void startExecution() {
	this.scenarioExecution = new SampleToolsQaScenarioExecution();
}
}
