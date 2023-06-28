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
package com.ibm.bear.qa.spot.template.config;

import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.template.topology.TemplateTopology;

/**
 * Class to manage the configuration for <b>%title%</b> scenarios.
 * <p>
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getTopology()}: Return the topology used while running the scenario.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initTimeouts()}: Initialize the timeouts.</li>
 * <li>{@link #initTopology()}: Initialize the topology.</li>
 * </ul>
 * </p>
 */
public class TemplateConfig extends Config {

public TemplateConfig() {
}

@Override
protected void initTimeouts() {
	this.timeouts = new TemplateTimeouts();
}

@Override
protected void initTopology() {
	this.topology = new TemplateTopology();
}

@Override
public TemplateTopology getTopology() {
	return (TemplateTopology) super.getTopology();
}
}
