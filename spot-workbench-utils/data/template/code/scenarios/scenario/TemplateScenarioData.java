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

import com.ibm.bear.qa.spot.core.scenario.ScenarioData;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioMissingImplementationError;

/**
 * Class to manage data needed while running the scenario.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getExpectedDocumentTitle()}: Return the expected document title of the application home page.</li>
 * </ul>
 * </p><p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #initUsers()}: Initialize users which will be used all over the scenario steps.</li>
 * </ul>
 * </p>
 */
public class TemplateScenarioData extends ScenarioData {

public TemplateScenarioData() {
	super();
}

/**
 * Return the expected document title of the application home page.
 *
 * @return The expected title
 */
public String getExpectedDocumentTitle() {
	return "To Be Defined";
}

@Override
protected void initUsers() {
	throw new ScenarioMissingImplementationError("Scenario data should implement a specific initUsers() method. If no user is needed, then just delete this line...");
}
}
