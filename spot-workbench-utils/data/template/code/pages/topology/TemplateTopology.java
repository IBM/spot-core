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
package com.ibm.bear.qa.spot.template.topology;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterValue;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.topology.Topology;

/**
 * Class to manage the Topology for <b>%title%</b> pages.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getTemplateApplication()}: Returns the <b>%title%</b> application.</li>
 * <li>{@link #isDistributed()}: Returns whether this topology is distributed across multiple servers</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initApplications()}: Initialize all topology applications.</li>
 * </ul>
 * </p>
 */
public class TemplateTopology extends Topology {

/**
 * Returns the <b>%title%</b> application.
 *
 * @return The application
 */
public TemplateApplication getTemplateApplication() {
	return (TemplateApplication) this.applications.get(0);
}

@Override
protected void initApplications() {
	String url = getParameterValue("template.url");
	if (url == null) {
		throw new ScenarioFailedError("Missing 'template.url' property definition. Set its value in params/topology.properties file...");
	}
	addApplication(new TemplateApplication(url));
}

@Override
public boolean isDistributed() {
	return false;
}
}
