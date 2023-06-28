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

import com.ibm.bear.qa.spot.core.scenario.ScenarioStepRunner;

/**
 * Class to manage a scenario step JUnit run.
 * <p>
 * This is the concrete class of this hierarchy which has to create the specific
 * scenario execution object (see subclass of {@link TemplateScenarioExecution} if any)
 * in the case the scenario step is executed as a single JUnit test class.
 * </p><p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #startExecution()}: Start the scenario execution.</li>
 * </ul>
 * </p>
 */
public class TemplateScenarioStepRunner extends ScenarioStepRunner {

public TemplateScenarioStepRunner(final Class< ? > klass) throws InitializationError {
	super(klass);
}

@Override
protected void startExecution() {
	this.scenarioExecution = new TemplateScenarioExecution();
}

}
