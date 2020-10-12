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
package com.ibm.bear.qa.spot.samples.tqa.topology;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterValue;

import com.ibm.bear.qa.spot.core.topology.Topology;

/**
 * Class to manage the Topology for <b>ToolsQA</b> pages.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getToolsQaApplication()}: Returns the <b>ToolsQA</b> application.</li>
 * <li>{@link #isDistributed()}: Returns whether this topology is distributed across multiple servers</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initApplications()}: Initialize all topology applications.</li>
 * </ul>
 * </p>
 */
public class ToolsQaTopology extends Topology {

/**
 * Returns the <b>Tools QA</b> application.
 *
 * @return The application
 */
public ToolsQaApplication getToolsQaApplication() {
	return (ToolsQaApplication) this.applications.get(0);
}

@Override
protected void initApplications() {
	String url = getParameterValue("toolsQA.url", "https://demoqa.com");
	addApplication(new ToolsQaApplication(url));
}

@Override
public boolean isDistributed() {
	return false;
}
}
