/*
* Licensed Materials - Property of IBM
* 5725-B69 5655-Y17 5655-Y31 5724-X98 5724-Y15 5655-V82
* Copyright IBM Corp. 1987, 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/
package com.ibm.bear.qa.spot.template.scenario;

import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.ibm.bear.qa.spot.core.scenario.ScenarioRunner;

/**
 * Class to manage the scenario JUnit run.
 * <p>
 * This is the concrete class of this hierarchy which has to create the specific
 * scenario execution object (see subclass of {@link TemplateScenarioExecution} if any).
 * </p><p>
 * Secondarily, it also defines the name of the main suite which is displayed
 * in the JUnit view when launching it.
 * </p>
 */
public class TemplateScenarioRunner extends ScenarioRunner {

public TemplateScenarioRunner(final Class< ? > klass, final RunnerBuilder builder) throws InitializationError {
	super(klass, builder);
}

@Override
protected String getName() {
	return "Scenario name";
}

@Override
protected void startExecution() {
	this.scenarioExecution = new TemplateScenarioExecution();
}
}
