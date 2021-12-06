/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.api;

import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.topology.Topology;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

public interface SpotConfig {

/**
 * Return the timeout used to wait for a dialog to be closed.<br>
 * This time is expressed in <b>seconds</b>.
 *
 * @return The timeout as a <code>int</code>.
 */
public int closeDialogTimeout();

/**
 * Get the browser used for a user.
 *
 * @param user The user using the browser
 * @return The browser instance
 */
public WebBrowser getBrowser(User user);

/**
 * Return the timeout used to wait for a page to be loaded.<br>
 * This time is expressed in <b>seconds</b>.
 *
 * @return The timeout as a <code>int</code>.
 */
public int getOpenPageTimeout();

/**
 * Return the timeout used to wait for short run operation.<br>
 * This time is expressed in <b>seconds</b>.
 *
 * @return The timeout as a <code>int</code>.
 */
public int getShortTimeout();

/**
 * Return the default timeout used to wait for an expected element in
 * the current web page. This time is expressed in <b>seconds</b>.
 *
 * @return The timeout as a <code>int</code>.
 */
public int getDefaultTimeout();

/**
 * Return the topology used while running the scenario.
 *
 * @return The topology as {@link Topology}.
 */
public Topology getTopology();
}
