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
import com.ibm.bear.qa.spot.core.utils.SpotFactory;
import com.ibm.bear.qa.spot.core.web.WebBrowser;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Manage a list of tests to execute in a scenario step.
 * <p>
 * Scenario may have several steps which are defined using a specific {@link ScenarioRunner} and a
 * list of classes as argument of {@link SuiteClasses} annotation.
 * </p><p>
 * The step provides easy access to scenario configuration and data through its
 * {@link ScenarioExecution} stored instance.
 * </p><p>
 * This step is connected to a web page. The page might be stored by the step when loaded. If so,
 * it's automatically stored to the {@link ScenarioExecution} at the end of the test execution to
 * allow next test or step to have the last page used by previous step in hand when starting.
 * </p><p>
 * The step also stores all workaround used during the tests and can provide information about them.
 * </p>
 * Design: To be finalized
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #closePage(User,Class)}: Close the opened page of given class for the given user.</li>
 * <li>{@link #getCurrentPage(User)}: Return current page displayed in the browser associated with the given user.</li>
 * <li>{@link #getData()}: Return the scenario data to use during the run.</li>
 * <li>{@link #getTopology()}: Return the scenario topology used during the run.</li>
 * <li>{@link #setUpStep()}: Setup for each step of the scenario.</li>
 * <li>{@link #setUpTest()}: Setup executed at the beginning of each test step.</li>
 * <li>{@link #sleepIfSlowServer(int)}: Sleep for a given number of seconds if the server is considered slow.</li>
 * <li>{@link #tearDownTest()}: Tear down executed at the end of each test step.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #deleteAllCookies(User)}: Delete all cookies on browser used by given user.</li>
 * <li>{@link #getBrowserManager()}: Return the browser manager.</li>
 * <li>{@link #getBrowserOpened(User)}: Get the browser used and opened for the given user..</li>
 * <li>{@link #getConfig()}: Return the scenario configuration to use during the run.</li>
 * <li>{@link #getOperation(Class)}: Return the operation corresponding to the given class.</li>
 * <li>{@link #getScenarioExecution()}: Return the scenario execution.</li>
 * <li>{@link #isStoringOperations()}: Tells whether current step stores operations used in during its tests execution.</li>
 * <li>{@link #runsWithFirefoxBrowser()}: Tell whether test execution is using Firefox browser or not.</li>
 * </ul>
 * </p>
 */
public abstract class ScenarioStep {

/**
 * Define the rule to execute a test for a scenario.
 * <p>
 * Basically, it simply delegates the execution to
 * {@link ScenarioExecution#runTest(Statement, Description)} method.
 * </p>
 */
	class ScenarioStepRule implements TestRule {

		/**
		 * Define the actions to be taken while executing a scenario step.
		 */
		final class ScenarioStepRuleStatement extends Statement {
			private final Statement statement;
			private final Description description;

			ScenarioStepRuleStatement(final Statement statement, final Description description) {
				this.statement = statement;
				this.description = description;
			}

			/**
			 * {@inheritDoc}
			 * <p>
			 * Run a step action (ie. a test) by delegating the execution to
			 * {@link ScenarioExecution#runTest(Statement, Description)} method.
			 * </p>
			 */
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

	/**
	 * Setup for each step of the scenario.
	 */
	@BeforeClass
	public static void setUpStep() {
		FIRST_TEST = true;
	}

	// Execution
	ScenarioExecution scenarioExecution;

	/**
	 * The rule to execute the scenario step.
	 */
	@Rule
	public ScenarioStepRule stepRule = new ScenarioStepRule();

/**
 * Close the opened page of given class for the given user.
 * <p>
 * It's a no-op if there's no existing such page for the given user.
 * </p>
 *
 * @param user The user
 * @param expectedPageClass The page expected class
 * @return <code>true</code> if a browser has an opened page on the given user
 * and it successfully closed it, <code>false</code> otherwise
 */
@SuppressWarnings("rawtypes")
public boolean closePage(final User user, final Class<? extends WebPage> expectedPageClass) {
	WebBrowser browser = getBrowserManager().getBrowser(user, false);
	if (browser != null) {
		WebPage currentPage = browser.getPage(expectedPageClass);
		if (currentPage != null && user.equals(currentPage.getUser())) {
			if (expectedPageClass != null) {
				Class pageClass = currentPage.getClass();
				while (pageClass != null) {
					if (pageClass.equals(expectedPageClass)) {
						browser.closePage(currentPage);
						return true;
					}
					pageClass = pageClass.getSuperclass();
				}
				return false;
			}
			browser.closePage(currentPage);
			return true;
		}
	}
	return false;
}

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
 * Get the browser used and opened for the given user..
 *
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
 * Return the operation corresponding to the given class.
 * <p>
 * If operation is not already stored then creates it and store it in scenario
 * execution operations list. Otherwise return a unique instance allowing steps
 * to share them during the scenario execution.
 * </p>
 * @param <O> The operation class
 * @param operationClass The operation class
 * @return The operation unique instance
 */
protected <O extends ScenarioOperation> O getOperation(final Class<O> operationClass) {
	if (isStoringOperations()) {
		@SuppressWarnings("unchecked")
		O operation = (O) this.scenarioExecution.getOperations().get(operationClass);
		if (operation == null) {
			operation = SpotFactory.createOperationInstance(operationClass, this);
			this.scenarioExecution.getOperations().put(operationClass, operation);
		} else {
			operation.setStep(this);
		}
		return operation;
	}
	return SpotFactory.createOperationInstance(operationClass, this);
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
 * Tells whether current step stores operations used in during its tests execution.
 * <p>
 * Default is to not store operations. Subclasses has to override this method to change
 * steps default behavior.
 * </p>
 * @return <code>true</code> if current step stores operations, <code>false</code>
 * otherwise
 */
protected boolean isStoringOperations() {
	return false;
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
