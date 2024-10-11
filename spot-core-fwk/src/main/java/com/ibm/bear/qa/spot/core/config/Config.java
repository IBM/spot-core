/*********************************************************************
* Copyright (c) 2012, 2024 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.config;

import static com.ibm.bear.qa.spot.core.config.Timeouts.*;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getClassSimpleName;

import com.ibm.bear.qa.spot.core.api.SpotConfig;
import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.topology.Topology;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * Manage the configuration needed to use the framework page objects.
 * <p>
 * To use framework page objects, following parameters need to be defined:
 * <ul>
 * <li><b>timeouts</b>: Timeouts on different tests operations, e.g. open a web page (see {@link Timeouts} for more details).</li>
 * <li><b>topology</b>: The topology of the CLM applications (see {@link Topology} for more details).</li>
 * <li><b>screenshots directory</b>: the directories where screenshots taken during the scenario has to be put. There are two directories, one for screenshots taken when a failure occurs and one for screenshots taken just for information.</li>
 * </ul>
 * </p><p>
 * This class defines following public API methods of {@link SpotConfig} interface:
 * <ul>
 * <li>{@link #closeDialogTimeout()}: Return the timeout used to wait for a dialog to be closed.<br> </li>
 * <li>{@link #getBrowser(User)}: Get the browser used for a user.</li>
 * <li>{@link #getDefaultTimeout()}: Return the default timeout used to wait for an expected element in</li>
 * <li>{@link #getOpenPageTimeout()}: Return the timeout used to wait for a page to be loaded.</li>
 * <li>{@link #getShortTimeout()}: Return the timeout used to wait for short run operation.</li>
 * <li>{@link #getTopology()}: Return the topology used while running the scenario.</li>
 * </ul>
 * </p><p>
 * This class also defines following internal API methods:
 * <ul>
 * <li>{@link #getBrowserManager()}: Return the browsers manager used while using the framework page objects.</li>
 * <li>{@link #toString()}: Answers a string containing a concise, human-readable</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initTimeouts()}: Initialize the timeouts.</li>
 * <li>{@link #initTopology()}: Initialize the topology.</li>
 * </ul>
 * </p>
 */
abstract public class Config implements SpotConfig {

	/* Fields */
	// Scenario topology
	protected Topology topology;
	// Defined timeouts
	protected Timeouts timeouts;

public Config() {
	initTopology();
	initTimeouts();
}


/**
 * Return the timeout used to wait for a dialog to be closed.<br>
 * This time is expressed in <b>seconds</b>.
 *
 * @return The timeout as a <code>int</code>.
 */
@Override
public int closeDialogTimeout() {
	return CLOSE_DIALOG_TIMEOUT;
}

@Override
public WebBrowser getBrowser(final User user) {
	return BrowsersManager.getInstance().getBrowserOpened(user);
}

/**
 * Return the browsers manager used while using the framework page objects.
 *
 * @return The browser manager
 */
public BrowsersManager getBrowserManager() {
	return BrowsersManager.getInstance();
}

/**
 * Return the default timeout used to wait for an expected element in
 * the current web page. This time is expressed in <b>seconds</b>.
 *
 * @return The timeout as a <code>int</code>.
 */
@Override
public int getDefaultTimeout() {
	return DEFAULT_TIMEOUT;
}

/**
 * Return the timeout used to wait for a page to be loaded.
 *
 * @return The timeout expressed in <b>seconds</b>.
 */
@Override
public int getOpenPageTimeout() {
	return OPEN_PAGE_TIMEOUT;
}

/**
 * Return the timeout used to wait for short run operation.
 *
 * @return The timeout expressed in <b>seconds</b>.
 */
@Override
public int getShortTimeout() {
	return SHORT_TIMEOUT;
}

/**
 * Return the topology used while running the scenario.
 *
 * @return The topology as {@link Topology}.
 */
@Override
public Topology getTopology() {
	return this.topology;
}

/**
 * Initialize the timeouts.
 * <p>
 * That needs to be overridden by the specific scenario to instantiate its own
 * object.
 * </p>
 */
abstract protected void initTimeouts();

/**
 * Initialize the topology.
 * <p>
 * That needs to be overridden by the specific scenario to instantiate its own
 * object.
 * </p>
 */
abstract protected void initTopology();


@Override
public String toString() {
	return getClassSimpleName(getClass()) + " using topology " + getClassSimpleName(this.topology.getClass());
}


}