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
package com.ibm.bear.qa.spot.core.scenario;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.Date;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.Statement;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.topology.Topology;
import com.ibm.bear.qa.spot.core.web.WebBrowser;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Manage a list of tests to execute in a scenario step.
 * <p>
 * Scenario may have several steps which are defined using a specific {@link ScenarioRunner}
 * and a list of classes as argument of {@link SuiteClasses} annotation.
 * </p><p>
 * The step provides easy access to scenario configuration and data through its
 * {@link ScenarioExecution} stored instance.
 * </p><p>
 * This step is connected to a web page. The page might be stored by the step
 * when loaded. If so, it's automatically stored to the {@link ScenarioExecution}
 * at the end of the test execution to allow next test or step to have the last
 * page used by previous step in hand when starting.
 * </p><p>
 * The step also stores all workaround used during the tests and can provide
 * information about them.
 * </p>
 * Design: To be finalized
 */
public class ScenarioStep {

	class ScenarioStepRule implements TestRule {

		final class ScenarioStepRuleStatement extends Statement {
			private final Statement statement;
			private final Description description;

			ScenarioStepRuleStatement(final Statement statement, final Description description) {
				this.statement = statement;
				this.description = description;
			}

			@Override
			public void evaluate() throws Throwable {
				ScenarioStep.this.scenarioExecution.runTest(this.statement, this.description);
			}
		}

        @Override
        public Statement apply(final Statement base, final Description description) {
	        return new ScenarioStepRuleStatement(base, description);
        }
	}

	// Step info
	protected static boolean FIRST_TEST = true;

	@BeforeClass
	public static void setUpStep() {
		FIRST_TEST = true;
	}

	// Execution
	ScenarioExecution scenarioExecution;

	@Rule
	public ScenarioStepRule stepRule = new ScenarioStepRule();

/**
 * Delete all cookies on browser used by given user.
 *
 * @param user The user for which browser cookies should be deleted
 */
protected void deleteAllCookies(final User user) {
	WebBrowser currentBrowser = getBrowserManager().getCurrentBrowser();
	if (currentBrowser != null && getCurrentPage(user) != null) {
		println("WARNING: Delete all cookies and relogin to avoid connection timeout issue...");
		currentBrowser.deleteAllCookies();
		currentBrowser.refreshManagingLogin(getCurrentPage(user));
	}
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
 * @see BrowsersManager#getBrowserOpened(User)
 */
protected WebBrowser getBrowserOpened(final User user) {
	return BrowsersManager.getInstance().getBrowserOpened(user);
}

/**
 * Return the scenario configuration to use during the run.
 *
 * @see ScenarioExecution#getConfig()
 */
protected Config getConfig() {
	return this.scenarioExecution.config;
}

/**
 * Return current page displayed in the browser associated with the given user.
 *
 * @param user The user using the browser
 * @return The web page or <code>null</code> if no page is currently displayed
 */
public WebPage getCurrentPage(final User user) {
	return getBrowserManager().getBrowserOpened(user).getCurrentPage();
}

/**
 * Return the scenario data to use during the run.
 *
 * @see ScenarioExecution#getData()
 */
public ScenarioData getData() {
	return this.scenarioExecution.data;
}

/**
 * Return the scenario execution.
 *
 * @return The scenario execution as a {@link ScenarioExecution}.
 */
protected ScenarioExecution getScenarioExecution() {
	return this.scenarioExecution;
}

/**
 * Return the scenario topology used during the run.
 *
 * @see ScenarioExecution#getTopology()
 */
public Topology getTopology() {
	return getConfig().getTopology();
}

/**
 * Tell whether test execution is using Firefox browser or not.
 *
 * @return <code>true</code> if Firefox browser is used, <code>false</code> otherwise
 */
protected boolean runsWithFirefoxBrowser() {
	return getBrowserManager().getCurrentBrowser().isFirefox();
}

/**
 * Setup executed at the beginning of each test step.
 * <p>
 * So far, it only displays the step title when it's the first test and the test
 * title in the console.
 * </p>
 */
@Before
public void setUpTest() {

	// Print step title
	if (FIRST_TEST) {
		printStepStart(getScenarioExecution().stepName);
	}

	// Test case starting point
	println("	- "+TIME_FORMAT.format(new Date(System.currentTimeMillis()))+": start test case '"+getScenarioExecution().testName+"'...");
}

/**
 * Sleep for a given number of seconds if the server is considered slow.
 * <p>
 * Nothing happens if the server is not considered slow.
 * </p>
 * @param time Number of seconds the execution will sleep, if the server
 * is considered slow.
 */
public void sleepIfSlowServer(final int time) {
	if (this.scenarioExecution.isSlowServer()) {
		if (DEBUG) println("		+ Slow server: sleeping for '" + time + "' seconds.");
		sleep(time);
	}
}

/**
 * Tear down executed at the end of each test step.
 * <p>
 * So far, it turn off the first step flag and stores the current page to the
 * scenario execution to pass it to next test.
 * </p>
 */
@After
public void tearDownTest() throws Exception {
	FIRST_TEST = false;
}
}
