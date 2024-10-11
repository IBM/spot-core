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
package com.ibm.bear.qa.spot.core.scenario;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.web.WebBrowser;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Abstract class to manage an operation performed during the scenario execution.
 * <p>
 * An action is a sequence of product API calls. At this level of the hierarchy only common actions
 * unrelated to a product are obviously implemented...
 * </p><p>
 * The action knows the step in which it's used allowing easy access to all framework information
 * and capabilities through {@link ScenarioData} and {@link ScenarioStep} classes.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getBrowser(User)}: Return the browser associated with the given user.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getBrowserManager()}: Return the browser manager.</li>
 * <li>{@link #getConfig()}: Return the scenario configuration to use during the run.</li>
 * <li>{@link #getCurrentPage(User)}: Return current page displayed in the browser associated with the given user.</li>
 * <li>{@link #getStep()}: Return the step which owns the current operation.</li>
 * <li>{@link #setOpenBrowser(boolean)}: Tells the operation to open or not a new browser if no browser is associated with a user.</li>
 * </ul>
 * </p>
 */
public abstract class ScenarioOperation {

	/* Fields */
	// Step associated with the current operation
	private ScenarioStep step;
	//  Flag to tell to open or not a new browser while getting one for a user which has no associated browser
	private boolean openBrowser = true;

/**
 * Create an operation instance belonging to the given step.
 *
 * @param step The step the operation will belong to
 */
public ScenarioOperation(final ScenarioStep step) {
	this.step = step;
}

/**
 * Return the browser associated with the given user.
 * <p>
 * Note that if no browser is currently associated with the user,
 * the returned browser (ie. the browser that will be associated with the user)
 * depends on {@link #openBrowser} flag. If it's <code>true</code>, then a
 * new browser will be opened and associated with the user. If it's <code>false</code>
 * then the currently opened browser will be associated with the user.
 * </p>
 *
 * @param user The user we want the browser associated with
 * @return The browser associated with the given user
 * @see BrowsersManager#getBrowser(User, boolean)
 */
public WebBrowser getBrowser(final User user) {
	return getBrowserManager().getBrowser(user, this.openBrowser);
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


/**
 * Tells the operation to open or not a new browser if no browser is associated with a user.
 *
 * @param openBrowser The flag value
 */
protected void setOpenBrowser(final boolean openBrowser) {
	this.openBrowser = openBrowser;
}

/**
 * Change the step associated with the operation.
 * <p>
 * This setter is necessary when operations are shared among scenario steps.
 * </p>
 * @param scenarioStep The new step associated with the operation
 */
void setStep(final ScenarioStep scenarioStep) {
	this.step = scenarioStep;
}
}
