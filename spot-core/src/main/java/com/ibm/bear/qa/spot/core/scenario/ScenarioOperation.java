/*********************************************************************
* Copyright (c) 2012, 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.scenario;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Abstract class to manage an operation performed during the scenario execution.
 * <p>
 * An action is a sequence of product API calls. At this level of the hierarchy
 * only common actions unrelated to a product are obviously implemented...
 * </p><p>
 * The action knows the step in which it's used allowing easy access to all
 * framework information and capabilities through {@link ScenarioData} and
 * {@link ScenarioStep} classes.
 * </p><p>
 * Following internal methods are defined:
 * <ul>
 * <li>{@link #getBrowserManager()}: Return the browser manager.</li>
 * <li>{@link #getConfig()}: Return the scenario configuration to use during the run.</li>
 * <li>{@link #getCurrentPage(User)}: Return current page displayed in the browser associated with the given user.</li>
 * <li>{@link #getStep()}: Return the step which owns the current operation.</li>
 * </ul>
 * </p>
 */
public abstract class ScenarioOperation {

	private final ScenarioStep step;

/**
 * Create an operation instance belonging to the givens step.
 *
 * @param step The step the operation will belong to
 */
public ScenarioOperation(final ScenarioStep step) {
	super();
	this.step = step;
}

/**
 * Return the browser manager.
 *
 * @see BrowsersManager#getInstance()
 */
protected BrowsersManager getBrowserManager() {
	return BrowsersManager.getInstance();
}

/**
 * Return the scenario configuration to use during the run.
 *
 * @see ScenarioStep#getConfig()
 */
protected Config getConfig() {
	return this.step.getConfig();
}

/**
 * Return current page displayed in the browser associated with the given user.
 *
 * @see ScenarioStep#getCurrentPage(User)
 */
protected WebPage getCurrentPage(final User user) {
	return this.step.getCurrentPage(user);
}

/**
 * Return the step which owns the current operation.
 *
 * @return The step
 */
protected ScenarioStep getStep() {
	return this.step;
}
}
