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

import com.ibm.bear.qa.spot.core.scenario.ScenarioExecution;
import com.ibm.bear.qa.spot.test.gen.config.TemplateConfig;

/**
 * Class to manage the scenario execution.
 * <p>
 * This is the concrete class of this hierarchy which has to create the specific
 * scenario configuration and data object respectively in {@link #initConfig()} and
 * {@link #initData()} method.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getData()}: Return the scenario data to use during the run.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initConfig()}: Initialize the configuration.</li>
 * <li>{@link #initData()}: Override the superclass implementation to create the specific scenario data</li>
 * </ul>
 * </p>
 * @see TemplateScenarioData
 */
public class TemplateScenarioExecution extends ScenarioExecution {

@Override
protected void initConfig() {
	// A compilation error on line below would mean that the TemplateConfig has
	// either not been implemented or it was implemented using a different name
	this.config = new TemplateConfig();
}

/**
 * Override the superclass implementation to create the specific scenario data
 * object (see {@link TemplateScenarioData}).
 */
@Override
protected void initData() {
	this.data = new TemplateScenarioData();
}

@Override
public TemplateScenarioData getData() {
	return (TemplateScenarioData) super.getData();
}
}
