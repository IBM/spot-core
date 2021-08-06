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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.FileUtil.getDir;
import static com.ibm.bear.qa.spot.core.utils.FileUtil.rmdir;
import static com.ibm.bear.qa.spot.core.web.WebBrowser.JAVASCRIPT_ERROR_ALERT_PATTERN;
import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;
import static javax.naming.Context.PROVIDER_URL;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.*;

import org.apache.activemq.broker.BrokerService;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.ibm.bear.qa.spot.core.browser.BrowsersManager;
import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.performance.PerfManager;
import com.ibm.bear.qa.spot.core.scenario.errors.*;
import com.ibm.bear.qa.spot.core.timeout.SpotAbstractTimeout;
import com.ibm.bear.qa.spot.core.topology.Topology;
import com.ibm.bear.qa.spot.core.utils.*;
import com.ibm.bear.qa.spot.core.web.WebBrowser;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Manage scenario execution.
 * <p>
 * This class is responsible to initialize and store the configuration and the data.
 * </p><p>
 * It also controls the scenario behavior when failure occurs using following
 * arguments:
 * <ul>
 * <li>{@link #STOP_ON_FAILURE_ID}: flag to tell whether the scenario can
 * continue after a test failure or should stop immediately. By default, the
 * execution will continue after a failure, but setting this argument to <code>true</code>
 * will make it stop at the first failure.</li>
 * <li>{@link #FAILURES_THRESHOLD_ID}: Number of tolerated failure coming
 * from selenium WebDriver API (ie. when a {@link WebDriverException} is raised)
 * when running the <b>entire</b> scenario.<br>
 * Above the threshold, the test will fail, otherwise it will run it again in case
 * it was a transient problem. In the latter case, a snapshot will be taken and
 * put in the warning directory and the failure stack trace will be written in the
 * console output.</li>
 * <li>{@link #ALERTS_THRESHOLD_ID}: Number of tolerated alerts when running
 * the <b>entire</b> scenario.<br>
 * Above the threshold, the test will fail, otherwise it will accept the alert and
 * continue the execution.</li>
 * <li>{@link #RETRIABLES_THRESHOLD_ID}: Number of tolerated timeouts failure
 * when running the <b>entire</b> scenario.<br>
 * Above the threshold, the test will fail, otherwise it will run it again in case
 * it was a transient problem. In the latter case, a snapshot will be taken and
 * put in the warning directory and the failure stack trace will be written in the
 * console output.</li>
 * </ul>
 * </p><p>
 * Another important thing done by this class is to store the current {@link WebPage page}
 * to be able to pass it from test to test inside a scenario step and also from step
 * to step inside the scenario. That allow easy transition between tests when
 * a test ends on the same page where the following one starts.
 * </p>
 */
public abstract class ScenarioExecution {

	/**
	 * Timeout class to wait for test dependencies.
	 */
	class SpotDependsOnTimeout extends SpotAbstractTimeout {
		final List<String> blockers;
		final String queueName;
		MessageConsumer messageConsumer;

		/**
		 * Create the timeout instance.
		 * <p>
		 * This timeout is used when {@link DependsOn} annotation is specified on given test.
		 * It stores the list of blocker tests in order to wait for a message on each of them during
		 * the waiting loop. It also creates the consumer as only one is necessary even if several
		 * messages are expected. This consumer is based on a queue which name is based on
		 * given test id (<code>&lt;Step class simple name&gt;.&lt;test name&gt;</code>).
		 * </p>
		 * @param test The test description using the timeout
		 * @param context The naming context to retrieve dynamic queues
		 * @param session The queue session to use to receive message
		 * @param depends The annotation set on the test for dependency
		 * @throws ScenarioSynchronizationError If exception occurs while creating the timeout instance
		 */
		SpotDependsOnTimeout(final Description test, final Context context, final QueueSession session, final DependsOn depends) throws ScenarioSynchronizationError {
			super(depends.fail(), 10000);
			this.blockers = getListFromArray(depends.blockers());
			this.queueName = getClassSimpleName(test.getClassName())+"."+test.getMethodName();
			println("		+ INFO: Wait in queue'"+this.queueName+"' for message from "+getTextFromList(this.blockers));
			try {
				Queue queue = (Queue) context.lookup("dynamicQueues/"+this.queueName);
				debugPrintln("	- create message consumer ...");
				this.messageConsumer = session.createConsumer(queue);
			}
			catch (Exception ex) {
				throw new ScenarioSynchronizationError(ex);
			}
		}

		@Override
		protected void fail(final boolean whileLoop) throws ScenarioFailedError {
			throw new ScenarioFailedError(getFailureMessage(whileLoop));
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Check with not wait whether a message has been received by the consumer.
		 * If so, then remove message from the list the blocker which has sent the message.
		 * </p>
		 * @throws ScenarioSynchronizationError If an exception occurs while waiting for the message
		 * @throws ScenarioFailedError If the blocker which has sent the message does not belong
		 * to the blockers list
		 */
		@Override
		protected boolean getCondition() throws ScenarioSynchronizationError, ScenarioFailedError {
			try {
				debugPrintln("	- check message arrived in queue '"+this.queueName+"'...");
				Message message = this.messageConsumer.receiveNoWait();
				if (message != null) {
					String testId = message.getStringProperty("test");
					println("		+ INFO: receive message from '"+testId+"'");
					if (!this.blockers.remove(testId)) {
						throw new ScenarioFailedError("Unexpected test ID '"+testId+"' received in queue '"+this.queueName+"'.");
					}
				}
				return this.blockers.size() > 0;
			}
			catch (JMSException jmse) {
				throw new ScenarioSynchronizationError(jmse);
			}
		}

		@Override
		protected String getConditionLabel() {
			return "Not all blockers messages has arrived";
		}
	}

	/* Constants */
	// Synchronization
	private final static String SPOT_SYNCHRO = "spot.synchro";
	private static final String SPOT_SYNCHRO_SCENARIOS = "spot.synchro.scenarios";
	private static final String SPOT_SYNCHRO_SHUTDOWN_TIMEOUT = "spot.synchro.shutdown.timeout";
	private static final String DYNAMIC_QUEUES_SHUTDOWN = "dynamicQueues/shutdown";
	// Control the execution after a failure
	private final static String STOP_ON_FAILURE_ID = "stopOnFailure";
	private final static String FAILURES_THRESHOLD_ID = "failuresThreshold";
	private final static int DEFAULT_FAILURES_THRESHOLD = 2;
	private final static String ALERTS_THRESHOLD_ID = "alertsThreshold";
	private final static int DEFAULT_ALERTS_THRESHOLD = 10;
	private final static String RETRIABLES_THRESHOLD_ID = "retriablesThreshold";
	private final static String RETRIABLES_FAILURES_THRESHOLD_ID = "retriablesFailuresThreshold";
	private final static String MULTIPLES_THRESHOLD_ID = "multiplesThreshold";
	private final static String BROWSER_ERRORS_THRESHOLD_ID = "browserErrorsThreshold";
	private final static int DEFAULT_RETRIABLES_THRESHOLD = 2;
	private final static int DEFAULT_RETRIABLES_FAILURES_THRESHOLD = 3;
	private final static int DEFAULT_MULTIPLES_THRESHOLD = 2;
	private final static int DEFAULT_BROWSER_ERRORS_THRESHOLD = 2;
	private final static String STOP_ON_EXCEPTION_ID = "stopOnException";
	private final static String PAUSE_EXECUTION_ID = "pause.execution";

	// Execution control
	private final boolean stopOnFailure;
	private final int failuresThreshold;
	private int failures;
	private final int alertsThreshold;
	private int alerts = 0;
	private final int retriablesThreshold;
	private int retriables = 0;
	private final int retriablesFailuresThreshold;
	private int retriablesFailures = 0;
	private final int multiplesThreshold;
	private int multiples;
	private final int browserErrorsThreshold;
	private int browserErrors = 0;
	private boolean shouldStop = false;
	private boolean singleStep = false;
	private List<Description> mandatoryTests = new ArrayList<Description>();
	private boolean slowServer = false;
	private final boolean stopOnException;
	private final boolean closeBrowserOnExit;
	private final int pauseExec = getParameterIntValue(PAUSE_EXECUTION_ID);
	private String stopStepExecution = null;

	// Configuration
	protected Config config;

	// Data
	protected ScenarioData data;

	// Scenario info
	protected String scenarioClass;
	protected String stepName;
	protected String testName;

	// Synchronization
	final boolean testSynchronization;
	boolean shouldSynchronize;
	final boolean canSynchronize;
	private QueueSession queueSession;
	private Context namingContext;
	private Queue scenarioQueue;
	private boolean synchroMaster = false;
	private BrokerService brokerService;

public ScenarioExecution() {

	// Init execution controls
	this.stopOnFailure = getParameterBooleanValue(STOP_ON_FAILURE_ID, false);
	this.stopOnException = getParameterBooleanValue(STOP_ON_EXCEPTION_ID, this.stopOnFailure);
	this.failuresThreshold = getParameterIntValue(FAILURES_THRESHOLD_ID, DEFAULT_FAILURES_THRESHOLD);
	this.alertsThreshold = getParameterIntValue(ALERTS_THRESHOLD_ID, DEFAULT_ALERTS_THRESHOLD);
	this.retriablesThreshold = getParameterIntValue(RETRIABLES_THRESHOLD_ID, DEFAULT_RETRIABLES_THRESHOLD);
	this.retriablesFailuresThreshold = getParameterIntValue(RETRIABLES_FAILURES_THRESHOLD_ID, DEFAULT_RETRIABLES_FAILURES_THRESHOLD);
	this.browserErrorsThreshold = getParameterIntValue(BROWSER_ERRORS_THRESHOLD_ID, DEFAULT_BROWSER_ERRORS_THRESHOLD);
	this.multiplesThreshold = getParameterIntValue(MULTIPLES_THRESHOLD_ID, DEFAULT_MULTIPLES_THRESHOLD);
	this.closeBrowserOnExit = getParameterBooleanValue("closeBrowserOnExit", true);

	// Init Config
	initConfig();

	// Init data
	initData();

	// Init JMS objects
	this.testSynchronization = getParameterBooleanValue(SPOT_SYNCHRO, false);
	if (this.testSynchronization) {
		this.canSynchronize = synchroInitialize();
	} else {
		this.canSynchronize = false;
		debugPrintln("INFO: Test synchronization is disabled, hence all @DependsOn and @Blocks annotation will be ignored.");
	}
}

/**
 * Add a list of mandatory tests.
 *
 * @param tests The mandatory tests to add
 */
public void addMandatoryTests(final List<FrameworkMethod> tests) {
	for (FrameworkMethod method: tests) {
		Description description = Description.createTestDescription(method.getMethod().getDeclaringClass(), method.getName(), method.getAnnotations());
		this.mandatoryTests.add(description);
	}
}

/*
 * Helper method to check if the server speed is considered normal (according to
 * pipeline averages) or slower than normal.  Individual tests can trigger this
 * check with the {@link CheckServerSpeed} annotation; passing in the max time
 * (in seconds) that is considered normal for that test in the pipeline.
 * <p></p>
 * This method sets the {@link slowServer} attribute. When a test takes longer than
 * normal, the attribute is set to <code>true</code>.  When a test takes less than expected,
 * the attribute is set (or reset) to <code>false</code>.
 * </p>
 * @param start Start time of the test - used to determine how long the test has taken
 * @param timeLimit Maximum number of seconds that this test should have taken; tests taking longer
 * indicate a slower server.
 */
private void checkServerSpeed(final long start, final int timeLimit) {
	if (DEBUG) println("		+ Checking server speed: this test should take less than '" + timeLimit + "' seconds.");

	if (getElapsedTime(start) > timeLimit*1000) {
		this.slowServer = true;
		if (DEBUG) println("		> Server is considered *slow* because this test took longer '" + timeLimit + "' seconds.");
		return;
	}

	// If we get here, the server is behaving normally.
	this.slowServer = false;
	if (DEBUG) println("		> Server is considered *normal* because this test took less than '" + timeLimit + "' seconds.");
}

/**
 * Do some cleanup usually before re-run the test when it failed.
 * <p>
 * Default clean up is to clear pages cache.
 * </p>
 * @param t The failure which requires a clean-up
 */
protected void cleanUp(final Throwable t) {
	println("		-> Cleanup test by clearing the pages cache.");
	if (getBrowser() == null) {
		println("WARNING: There's no browser available to cleanup !");
	} else {
		getBrowser().clearCache();
	}
}

/**
 * Return the current browser used while executing current test.
 *
 * @return The browser or <code>null</code> if no browser is opened
 */
public WebBrowser getBrowser() {
	return BrowsersManager.getInstance().getCurrentBrowser();
}

/**
 * Return the scenario configuration to use during the run.
 *
 * @return The scenario {@link Config}.
 */
public Config getConfig() {
	return this.config;
}

/**
 * Return the current browser page used while executing current test.
 *
 * @return The page or <code>null</code> if no browser is opened or no page
 * has been opened yet
 */
public WebPage getCurrentPage() {
	return getBrowser() == null ? null : getBrowser().getCurrentPage();
}

/**
 * Return the scenario data to use during the run.
 *
 * @return The scenario data as {@link ScenarioData}.
 */
public ScenarioData getData() {
	return this.data;
}

private PerfManager getPerfManager() {
	if (getBrowser() != null) {
		return getBrowser().getPerfManager();
	}
	return null;
}

private String getShouldStopReason(final boolean mandatoryTest) {
	String reason = EMPTY_STRING;
	if (this.stopOnFailure) {
		reason = "stopOnFailure=true";
	}
	if (mandatoryTest) {
		if (reason.length() > 0) reason += " and";
		reason += "it's a mandatory test";
	}
	return reason;
}

/**
 * Return the scenario topology used during the run.
 *
 * @return The scenario {@link Topology}.
 */
public Topology getTopology() {
	return this.config.getTopology();
}

private void handleAlert(final Description description, final WebDriverException wde) {
	printException(wde);
	if (getBrowser() == null) {
		println("WARNING: There's no browser available to handle alert!");
	} else {
		getBrowser().purgeAlert("Running test "+this.testName, 0);
	}
	if (this.alerts > this.alertsThreshold) {
		takeScreenshotFailure();
		this.shouldStop = this.stopOnFailure || this.mandatoryTests.contains(description);
		throw wde;
	}
	println("WORKAROUND: Try to run the test again in case the alert was a transient issue...");
	takeScreenshotWarning(); // take a snapshot just to notify the warning
	this.alerts++;
}

/**
 * Initialize the configuration.
 * <p>
 * That needs to be overridden by the specific scenario to instantiate its own
 * object.
 * </p>
 */
abstract protected void initConfig();

/**
 * Initialize the data.
 * <p>
 * That needs to be overridden by the specific scenario to instantiate its own
 * object.
 * </p>
 */
abstract protected void initData();

/**
 * @return the singleStep
 */
public boolean isSingleStep() {
	return this.singleStep;
}

/**
 * Return whether or not a server is considered slow.
 *
 * @return <code>true</code> if the server is considered slow; <code>false</code> otherwise.
 */
public boolean isSlowServer() {
	return this.slowServer;
}

/**
 * Manage the given failure.
 * <p>
 * Default behavior is to:
 * <ol>
 * <li>Print failure stack trace</li>
 * <li>Purge alerts</li>
 * <li>Print failure message</li>
 * <li>Store current page and reset it.</p>
 * </lo>
 * </p><p>
 * Specific scenarios might want to override this method to add specific behavior.
 * </p>
 * @param start The test starting time
 * @param t The failure to be managed
 * @param isNotRerunnable Tells whether the current test is rerunnable or not.
 * @param snapshotLevel The level of snapshot to be taken. If negative then no snapshot will be taken
 */
protected void manageFailure(final long start, final Throwable t, final boolean isNotRerunnable, final int snapshotLevel) {

	// Print failure stack trace
	printException(t);
	String message = t.getMessage();

	// Purge alert if any
	if (!(t instanceof UnreachableBrowserException)) {
		if (getBrowser() == null) {
			println("WARNING: There's no browser available to purge alert!");
		} else {
			getBrowser().purgeAlerts("After having got exception '" + message + "'");
		}
	}

	// Print info
	println("	  -> KO (in " + elapsedTimeString(start) + ")");
	println("		due to: " + message);

	// If a CLM Server error occurs display the error message
	if (t instanceof ServerMessageError) {
		ServerMessageError sme = (ServerMessageError) t;
		println("SERVER ERROR MESSAGE:");
		println(sme.getSummary());
		final String smeDetails = sme.getDetails();
		if (smeDetails != null) {
			println(smeDetails);
		}
	}

	// Return now when test is not re-runnable
	if (isNotRerunnable) {
		takeScreenshotFailure();
		return;
	}

	// Take a snapshot if specified
	switch (snapshotLevel) {
		case 0:
			takeScreenshotInfo();
			break;
		case 1:
			takeScreenshotWarning();
			break;
		case 2:
			takeScreenshotFailure();
			break;
		default:
			break;
	}

	// Clear up before eventually re-run the test
	cleanUp(t);
}

private void pauseExecution() {
	if (this.pauseExec > 0) {
		sleep(this.pauseExec);
	}
}

/**
 * Run the current test and take specific actions when some typical exception
 * or error occurs (e.g. take a snapshot when a error occurs, retry when allowed).
 * <p>
 * <b>Design Needs finalization</b>
 * </p>
 */
public void rerunTest(final Statement statement, final Description description) throws Throwable {
	if (description.getAnnotation(NotRerunnable.class) != null) {
		throw new ScenarioFailedError("It was specified that test '"+description.getMethodName()+" was not rerunnable, give up right now...");
	}
	runTest(statement, description);
}

/*
 * Restart the browser assuming the current session has gone...
 */
private void restartBrowser(final WebPage currentPage) {

	// Open new browser session
	println("		  -> Restarting browser to try to see if it accidentally died...");
	BrowsersManager.getInstance().openNewBrowser(currentPage.getUser());
	sleep(1);

	// Clear login data
	User user = currentPage.getUser();
	getTopology().logoutApplications();

	// Reload current page
	println("		  -> Reopening current page: "+currentPage.getLocation()+"...");
	WebPage.reopenPage(currentPage, user);
	sleep(1);
}

/**
 * Run the current test and take specific actions when some typical exception
 * or error occurs (e.g. take a snapshot when a error occurs, retry when allowed).
 * <p>
 * <b>Design Needs finalization</b>
 * </p>
 */
public void runTest(final Statement statement, final Description description) throws Throwable {

	// Store names
	setStepName(description.getClassName());
	setTestName(description.getMethodName());

	// Test whether the test should be skipped due to a previous step blocker test or not
	if (this.stopStepExecution != null) {
		if (this.stopStepExecution.equals(this.stepName)) {
			String message = "Test case '"+this.testName+"' is skipped due to previous test has failed and was a step blocker";
			println("	- "+TIME_FORMAT.format(new Date(System.currentTimeMillis()))+": "+message+"!");
			println("	  -> Unknown result");
			throw new SkippedTestError(message);
		}
		this.stopStepExecution = null;
	}

	// Store performances information if necessary
	PerfManager perfManager = getPerfManager();
	if (perfManager != null) {
		perfManager.setStepName(this.stepName);
		perfManager.setTestName(this.testName);
	}

	// Check dependencies
	try {
		synchroCheckTestDependencies(description);
	}
	catch (ScenarioSynchronizationError sse) {
		println("Scenario execution is stopped due to following synchronization error:");
		printException(sse);
		throw sse;
	}

	// Run test and take snapshots if a failure or error occurs
	long start = System.currentTimeMillis();
	boolean isNotRerunnable = description.getAnnotation(NotRerunnable.class) != null;
	try {
		statement.evaluate();
		// If we're here, the test passed.

		// Individual tests may be annotated to check server speed.
		CheckServerSpeed annotation = description.getAnnotation(CheckServerSpeed.class);
		if (annotation != null) {
			checkServerSpeed(start, annotation.value());
		}

		println("	  -> OK (in "+elapsedTimeString(start)+")");
		pauseExecution();
	}
	catch (UnhandledAlertException uae) {
		handleAlert(description, uae);
		runTest(statement, description);
	}
	catch (UnreachableBrowserException ube) {

		// Manage failure
		WebPage currentPage = getCurrentPage();
		manageFailure(start, ube, isNotRerunnable || currentPage == null, /*snapshotLevel:*/-1);
		if (this.browserErrors >= this.browserErrorsThreshold || currentPage == null) {
			println("		  -> The browser seems to have a problem which cannot be workarounded by just a restart, give up!");
			this.shouldStop = true;
			throw ube;
		}
		// Restart the browser on the current page
		restartBrowser(currentPage);
		this.browserErrors++;
		// Re-run the test
		println("		  -> Re-run the test...");
		runTest(statement, description);
	}
	catch (WebDriverException wde) {
		String message = wde.getMessage();
		if (message.matches(JAVASCRIPT_ERROR_ALERT_PATTERN)) {
			handleAlert(description, wde);
		} else if (message.startsWith("Failed to connect to binary FirefoxBinary") || message.startsWith("chrome not reachable")) {
			WebPage currentPage = getCurrentPage();
			manageFailure(start, wde, isNotRerunnable || currentPage == null, /*snapshotLevel:*/-1);
			if (this.browserErrors >= this.browserErrorsThreshold || currentPage == null) {
				println("		  -> The browser seems to have a problem which cannot be workarounded by just a restart, give up!");
				this.shouldStop = true;
				throw wde;
			}
			// Restart the browser on the current page
			sleep(2);
			restartBrowser(currentPage);
			this.browserErrors++;
		} else {
			WebPage currentPage = getCurrentPage();
			boolean shouldFail = this.failures >= this.failuresThreshold;
			manageFailure(start, wde, isNotRerunnable || getBrowser() == null, /*snapshotLevel:*/shouldFail ? 2 : 1);
			if (shouldFail) {
				this.shouldStop = this.stopOnFailure || this.mandatoryTests.contains(description);
				if (!this.shouldStop && description.getAnnotation(StepBlocker.class) != null) {
					this.stopStepExecution = this.stepName;
				}
				throw wde;
			}
			println("WORKAROUND: Try to run the test again in case this was a transient issue...");
			this.failures++;
			// Refresh browser in case that can help...
			println("	1) Close all other browser windows if necessary...");
			getBrowser().closeOtherWindows();
			println("	2) Refresh the browser...");
			getBrowser().refreshManagingLogin(currentPage);
		}
		// Re-run the test
		println("	3) Re-run the test...");
		runTest(statement, description);
	}
	catch (RetryableError pte) {
		WebPage currentPage = getCurrentPage();
		boolean shouldFail = this.retriables >= this.retriablesThreshold;
		boolean cannotRerun = isNotRerunnable || getBrowser() == null;
		int snapshotLevel = (shouldFail || !cannotRerun) ? 2 : 1;
		manageFailure(start, pte, cannotRerun, snapshotLevel);
		if (shouldFail) {
			println("Too many retryable errors occurred during test execution, hence give up.");
			this.shouldStop = this.stopOnFailure || this.mandatoryTests.contains(description) || ++this.retriablesFailures >= this.retriablesFailuresThreshold;
			if (!this.shouldStop && description.getAnnotation(StepBlocker.class) != null) {
				this.stopStepExecution = this.stepName;
			}
			throw pte;
		}
		if (cannotRerun) {
			println("Unfortunately current test cannot be rerun, hence give up.");
			this.shouldStop = this.stopOnFailure || this.mandatoryTests.contains(description);
			if (!this.shouldStop && description.getAnnotation(StepBlocker.class) != null) {
				this.stopStepExecution = this.stepName;
			}
			throw pte;
		}
		println("WORKAROUND: Try to run the test again in case this was a transient issue...");
		// Refresh browser in case that can help...
		println("	1) Close all other browser windows if necessary...");
		getBrowser().closeOtherWindows();
		println("	2) Refresh the browser...");
		getBrowser().refreshManagingLogin(currentPage);
		this.retriables++;
		// Re-run the test
		println("	3) Re-run the test (retry " + this.retriables + "/" + this.retriablesThreshold +  ") ...");
		runTest(statement, description);
	}
	catch (MultipleVisibleElementsError mvee) {
		boolean shouldFail = this.multiples >= this.multiplesThreshold;
		manageFailure(start, mvee, isNotRerunnable || getBrowser() == null, /*snapshotLevel:*/shouldFail ? 2 : 1);
		if (shouldFail) {
			println("Too many multiple elements errors occurred during scenario execution, give up.");
			this.shouldStop = this.stopOnFailure || this.mandatoryTests.contains(description);
			if (!this.shouldStop && description.getAnnotation(StepBlocker.class) != null) {
				this.stopStepExecution = this.stepName;
			}
			throw mvee;
		}
		println("WORKAROUND: Try to run the test again in case this was a transient issue...");
		// Refresh browser in case that can help...
		println("WORKAROUND: Refresh the browser...");
		getBrowser().refresh();
		this.multiples++;
		// Re-run the test
		println("		  -> Re-run the test...");
		runTest(statement, description);
	}
	catch (ServerMessageError sme) {
		manageFailure(start, sme, false/*test won't be rerun*/, /*snapshotLevel:*/2);
		this.shouldStop = this.stopOnFailure || this.mandatoryTests.contains(description);
		if (!this.shouldStop && description.getAnnotation(StepBlocker.class) != null) {
			this.stopStepExecution = this.stepName;
		}
		throw sme;
	}
	catch (BrowserConnectionError | ExistingDataError | ScenarioImplementationError | ScenarioMissingImplementationError ex) {
		manageFailure(start, ex, false/*test won't be rerun*/, /*snapshotLevel:*/2);
		this.shouldStop = true;
		throw ex;
	}
	catch (BrowserError be) {
		if (be.isFatal() || getBrowser() == null) {
			println("Fatal error while trying to open browser, stop scenario execution!");
			this.shouldStop = true;
			throw be;
		}
		WebPage currentPage = getCurrentPage();
		boolean shouldFail = this.browserErrors >= this.browserErrorsThreshold || currentPage == null;
		manageFailure(start, be, isNotRerunnable || currentPage == null, /*snapshotLevel:*/shouldFail ? 2 : 1);
		if (shouldFail) {
			println("Too many browser errors occurred during scenario execution, give up.");
			this.shouldStop = true;
			throw be;
		}
		// Restart the browser on the current page
		if (be instanceof BrowserConnectionError) {
			restartBrowser(currentPage);
		}
		this.browserErrors++;
		// Re-run the test
		println("		  -> Re-run the test...");
		rerunTest(statement, description);
	}
	catch (Error err) {
		// Basic failure management for any kind of other error (including ScenarioFailedError)
		manageFailure(start, err, false/*test won't be rerun*/, /*snapshotLevel:*/2);
		boolean mandatoryTest = this.mandatoryTests.contains(description);
		this.shouldStop = this.stopOnFailure || mandatoryTest;
		if (this.shouldStop) {
			String reason = getShouldStopReason(mandatoryTest);
			println("ERROR: Unexpected error encountered while running current test, scenario execution will be aborted as "+reason+"!");
		}
		else if (description.getAnnotation(StepBlocker.class) != null) {
			this.stopStepExecution = this.stepName;
		}
		throw err;
	}
	catch (Exception ex) {
		// Basic exception management
		manageFailure(start, ex, false/*test won't be rerun*/, /*snapshotLevel:*/2);
		boolean mandatoryTest = this.mandatoryTests.contains(description);
		this.shouldStop = this.stopOnException || mandatoryTest;
		if (this.shouldStop) {
			String reason = getShouldStopReason(mandatoryTest);
			println("ERROR: Unexpected exception encountered while running current test, scenario execution will be aborted because "+reason+"!");
		}
		else if (description.getAnnotation(StepBlocker.class) != null) {
			this.stopStepExecution = this.stepName;
		}
		throw ex;
	}

	// Send execution message
	synchroSendTestExecutionMessage(description);
}

///**
// * Set the current scenario page.
// */
//public void setPage(final WebPage page) {
//	this.page = page;
//}

///**
// * @param shouldStop the shouldStop to set
// */
//public void setShouldStop(final boolean shouldStop) {
//	this.shouldStop = shouldStop;
//}

/**
 * @param singleStep the singleStep to set
 */
public void setSingleStep(final boolean singleStep) {
	this.singleStep = singleStep;
}

void setStepName(final String className) {
	this.stepName = ScenarioUtils.getClassSimpleName(className);
}

void setTestName(final String methodName) {
	if (this.testName == null || !this.testName.equals(methodName)) {
		// Initialize the retriables counter for the current test
		this.retriables = 0;
	}
	this.testName = methodName;
}

/**
 * @return the shouldStop
 */
public boolean shouldStop() {
	return this.shouldStop;
}

/**
 * Show some general information in the console.
 * <p>
 * The displayed information are:
 * <ul>
 * <li>Selenium version</li>
 * </ul>
 * </p>
 */
public void showInfo() {
	println("Selenium information:");
	println("	- " + ScenarioUtils.getSeleniumVersion());
}

/**
 * Ends the scenario execution.
 */
public void shutdown() {

	// Print if the scenario execution has been stopped at some point
	if (this.shouldStop) {
		println("Scenario execution has been stopped due to errors above and following configuration:");
		println("	- Stop on execution: "+this.stopOnException);
		println("	- Stop on failure: "+this.stopOnFailure);
		println("	- Stop on retriables errors: " + this.retriablesFailures + " / " + this.retriablesFailuresThreshold);
		println("	- Mandatory tests: "+getTextFromList(this.mandatoryTests));

	}

	// Manage synchronization during shutdown
	if (this.canSynchronize) {
		if (this.synchroMaster) {
			// If scenario is synchronization master, then wait for all dependent running scenarios to shutdown
			// First get list of synchronized scenarios
			String spotSynchScenarios = getParameterValue(SPOT_SYNCHRO_SCENARIOS);
			if (spotSynchScenarios == null) {
				println("WARNING: No scenario was declared while running this synchronized scenario!");
				println("That means there's no insurance that synchronized scenarios execution have worked properly...");
				println("Property '"+SPOT_SYNCHRO_SCENARIOS+"' should specify the list of synchronized scenarios.");
			} else {
				// Second wait for all synchronized to be finish before ending the process (as it owns the broker necessary to send and receive messages...)
				synchroWaitForOtherRunningScenariosShutdown(spotSynchScenarios.split(", "));

				// Broker cleanup
				try {
					this.brokerService.stop();
					File mqDataDir = getDir("activemq-data");
					if (mqDataDir.exists()) {
						rmdir(mqDataDir);
					}
				}
				catch (Exception ex) {
					debugPrintln("Following Exception occurred while cleaning MQ data directory:");
					debugPrintException(ex);
					debugPrintln("-> Skip it and continue assuming that it would be OK...");
				}
			}
		} else {
			// Send message into shutdown queue in order to let master know that the scenario has finished
			try {
				synchroSendMessageToQueue(this.scenarioQueue, this.scenarioClass);
			} catch (JMSException jmse) {
				println("WARNING: Following exception was caught during scenario execution shutdown:");
				printException(jmse);
				println("That means synchronized scenarios execution will surely be broken!");
			}
		}
	}

	// Close debug
	ScenarioUtils.debugClose();

	// Close browsers
	try {
		if (this.closeBrowserOnExit) {
			BrowsersManager.getInstance().shutdown();
		} else {
			println("INFO: Browsers have been kept opened in order to continue to use its session for further investigation or manual tests...");
		}
	}
	catch (@SuppressWarnings("unused") UnreachableBrowserException ube) {
		// Skip as browser was already dead.
	}
	catch (Exception ex) {
		println("Exception '"+ex.getMessage()+"' has been skipped while closing browser...");
	}
}

/**
 * Check test dependencies.
 * <p>
 * Look for {@link DependsOn} annotation on given test. If any, then wait to receive a message
 * for all referenced blocker tests. See {@link SpotDependsOnTimeout} for more details on how
 * this wait is performed.
 * </p><p>
 * Note that this is a no-op if no test has to be synchronized or test has no {@link DependsOn} annotation.
 * </p>
 * @param description Test description
 * @throws JMSException On any queue and/or message issue
 * @throws NamingException On any context naming issue
 * @throws ScenarioFailedError If one of received message does not match a blocker test
 */
private void synchroCheckTestDependencies(final Description description) throws JMSException, NamingException, ScenarioFailedError {
	if (!this.shouldSynchronize) return; // avoid polluting the log when nothing has to be synchronized
	debugPrintEnteringMethod("description", description);
	DependsOn dependsOn = description.getAnnotation(DependsOn.class);
	if (dependsOn != null) {
		debugPrintln("		  -> found DependsOn annotation");
		try {
			long start = System.currentTimeMillis();
			SpotDependsOnTimeout timeout = new SpotDependsOnTimeout(description, this.namingContext, this.queueSession, dependsOn);
			timeout.waitWhile(dependsOn.timeout());
			println("		+ INFO: all messages received after "+timeString(System.currentTimeMillis()-start));
		}
		catch (ScenarioSynchronizationError sse) {
			if (sse.getMessage().endsWith("The Session is closed")) {
				debugPrintln("		  -> Queue session is closed, assuming that blocker has sent the message.");
			} else {
				throw sse;
			}
		}
	}
}

/**
 * Initialize scenario synchronization mechanism.
 * <p>
 * This initialization is skipped if {@link #SPOT_SYNCHRO} property is set to <code>false</code>
 * </p>
 */
private boolean synchroInitialize() {
	try {
		// Create broker
		this.brokerService = new BrokerService();
		this.brokerService.addConnector("tcp://localhost:61616");
		this.brokerService.start();
		this.synchroMaster = true;
	}
	catch (IOException ioe) {
		if (!ioe.getCause().getMessage().equals("Address already in use: JVM_Bind")) {
			debugPrintln("Disable scenarios synchronization due to following exception:");
			debugPrintException(ioe);
			return false;
		}
	}
	catch (Exception ex) {
		debugPrintln("Disable scenarios synchronization due to following exception:");
		debugPrintException(ex);
		return false;
	}

	// Create JMS objects
	try {
		Properties props = new Properties();
		props.setProperty(INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty(PROVIDER_URL, "tcp://localhost:61616");
		this.namingContext = new InitialContext(props);
		debugPrintln("Creating JMS objects:");
		debugPrintln("	- lookup conection factory...");
		QueueConnectionFactory connectionFactory = (QueueConnectionFactory)this.namingContext.lookup("QueueConnectionFactory");
		debugPrintln("	- queue connection...");
		QueueConnection queueConnection = connectionFactory.createQueueConnection();
		debugPrintln("	- start connection...");
		queueConnection.start();
		debugPrintln("	- queue session...");
		this.queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		debugPrintln("	- shutdown queue...");
		this.scenarioQueue = (Queue) this.namingContext.lookup(DYNAMIC_QUEUES_SHUTDOWN);
		debugPrintln("=> creation finished.");
	}
	catch (Exception ex) {
		debugPrintln("Disable scenarios synchronization due to following exception:");
		debugPrintException(ex);
		return false;
	}
	return true;
}

/**
 * Send a text message to given queue.
 *
 * @param queue The queue to send message
 * @param text The message text
 * @throws JMSException Raise exception if problem occurs with queue
 */
private void synchroSendMessageToQueue(final Queue queue, final String text) throws JMSException {
	synchroSendMessageToQueue(queue, null, null, text);
}

/**
 * Send a text message to given queue with a key property value.
 *
 * @param queue The queue to send message
 * @param key The property key. If <code>null</code> then no property is sent.
 * @param value The property value
 * @param text The message text
 * @throws JMSException Raise exception if problem occurs with queue
 */
private void synchroSendMessageToQueue(final Queue queue, final String key, final String value, final String text) throws JMSException {
	TextMessage message = this.queueSession.createTextMessage();
	message.setText(text);
	if (key != null) {
		message.setStringProperty(key, value);
	}
	println("		+ INFO: Following messages have been sent:");
	println("			* queue: "+queue.getQueueName());
	println("			* text: "+text);
	if (key != null) {
		println("			* property: "+key+"="+value);
	}
	this.queueSession.createProducer(queue).send(message);
}

/**
 * Send test execution message.
 * <p>
 * Signal to all dependent tests that the test is executed.
 * </p><p>
 * Note that this is a no-op if no test has to be synchronized or test has no {@link Blocks} annotation.
 * </p>
 * @param description Test description
 * @throws JMSException On any queue and/or message issue
 * @throws NamingException On any context naming issue
 */
private void synchroSendTestExecutionMessage(final Description description) throws JMSException, NamingException {
	if (!this.testSynchronization) return; // avoid polluting the log when nothing has to be synchronized
	debugPrintEnteringMethod("description", description);
	List<Queue> queues = new ArrayList<>();
	debugPrintln("		  -> Looking for blocked tests:");
	Blocks blocks = description.getAnnotation(Blocks.class);
	if (blocks != null) {
		debugPrintln("			- found Blocks annotation");
		for (String queueName: blocks.value()) {
			debugPrintln("			  + create '"+queueName+"' queue...");
			Queue queue = (Queue) this.namingContext.lookup("dynamicQueues/"+queueName);
			queues.add(queue);
		}
	}
	if (!queues.isEmpty()) {
		for (Queue queue: queues) {
			String testReference = getClassSimpleName(description.getClassName())+"."+description.getMethodName();
			synchroSendMessageToQueue(queue, "test", testReference, "Executed");
		}
	}
}

/**
 * Wait for other running scenarios to shutdown before leaving.
 * <p>
 *
 * @param runningScenarios List of running scenarios (
 */
private void synchroWaitForOtherRunningScenariosShutdown(final String[] runningScenarios) {
	List<String> shutdownScenarios = getListFromArray(runningScenarios);
	try {
		debugPrintln("	- create message consumer ...");
		MessageConsumer messageConsumer = this.queueSession.createConsumer(this.scenarioQueue);
		final int shutdownTimeout = getParameterIntValue(SPOT_SYNCHRO_SHUTDOWN_TIMEOUT, 60);
		long timeout = shutdownTimeout * 60000L; // Default timeout is 60 minutes
		println("		+ INFO: Wait for shutdown messages ("+getTextFromList(runningScenarios)+") to arrive in queue '"+this.scenarioQueue.getQueueName()+"' (timeout="+shutdownTimeout+" minutes)...");
		long start = System.currentTimeMillis();
		while (shutdownScenarios.size() > 0) {
			TextMessage message = (TextMessage) messageConsumer.receive(timeout);
			if (message == null) {
				println("WARNING: Timeout has expired and NO message was received from following scenarios: "+getTextFromList(shutdownScenarios));
				println("Hence, there's no insurance that synchronized scenarios execution has worked properly...");
				return;
			}
			String text = message.getText();
			println("			* received: "+text);
			if (!shutdownScenarios.remove(text)) {
				println("WARNING: Scenario '"+text+"' was NOT declared as synchronized with current scenario '"+this.scenarioClass+"', but a shutdown message was received from it!");
				println("Please ensure that 'spot.synch.scenarios' property include '"+text+"' scenario...");
				println("Otherwise there's no insurance that synchronized scenarios execution has worked properly...");
			}
		}
		println("		+ INFO: all shutdown messages received after "+timeString(System.currentTimeMillis()-start));
	}
	catch (Exception ex) {
		debugPrintln("Following exception was caught and ignored during scenario execution shutdown:");
		debugPrintException(ex);
		debugPrintln("However, that surely means synchronized scenarios execution was not working properly!");
	}
}

/**
 * Takes a failure snapshot.
 */
public void takeScreenshotFailure() {
	// Print warning message in console
	println("		+ one snaphot taken when the failure occured:");
	// Take snapshot
	if (getBrowser() == null) {
		println("WARNING: There's no browser available to take a snapshot!");
	} else {
		getBrowser().takeScreenshotFailure(toString());
	}
}

/**
 * Takes an information snapshot.
 */
public void takeScreenshotInfo() {
	takeScreenshotInfo(null);
}

/**
 * Takes a graph snapshot.
 *
 * @param text Text to display in the console when taking the snapshot
 */
public void takeScreenshotInfo(final String text) {
	// Print warning message in console
	println("		+ A snapshot has been taken:");
	if (text != null) {
		println("		-> Reason: " + text);
	}
	// Take snapshot
	if (getBrowser() == null) {
		println("WARNING: There's no browser available to take a snapshot!");
	} else {
		getBrowser().takeScreenshotInfo(toString());
	}
}

/**
 * Takes a warning snapshot.
 */
public void takeScreenshotWarning() {
	takeScreenshotWarning(null);
}

/**
 * Takes a warning snapshot, adding text to console for reason.
 *
 * @param text Reason the snapshot was taken.
 */
public void takeScreenshotWarning(final String text) {
	// Print warning message in console
	println("		+ A snapshot has been taken:");
	if (text != null) {
		println("		-> Reason: " + text);
	}
	// Take snapshot
	if (getBrowser() == null) {
		println("WARNING: There's no browser available to take a snapshot!");
	} else {
		getBrowser().takeScreenshotWarning(toString());
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Return the current step.test names
 * </p>
 */
@Override
public String toString() {
	return this.stepName + "." + this.testName;
}
}
