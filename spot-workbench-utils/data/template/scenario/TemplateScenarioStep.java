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

import com.ibm.bear.qa.spot.core.scenario.ScenarioStep;


/**
 * Class to manage common behavior of any scenario step.
 */
public abstract class TemplateScenarioStep extends ScenarioStep {

@Override
public TemplateScenarioData getData() {
	return (TemplateScenarioData) super.getData();
}
}