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

import java.text.NumberFormat;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriverException;

import com.ibm.bear.qa.spot.core.scenario.errors.*;

/**
 * Class to manage counters during scenario execution.
 * <p>
 * Basically this class manages both failures and tests counters.
 * </p><p>
 * For failures, it counts:
 * <ul>
 * <li>scenario failures: {@link ScenarioFailedError} raised</li>
 * <li>scenario alerts: {@link Alert} raised</li>
 * <li>scenario retriable errors: {@link RetryableError} raised</li>
 * <li>scenario multiple elements error: {@link MultipleElementsFoundError} raised</li>
 * <li>scenario browser errors: {@link BrowserConnectionError} raised</li>
 * </ul>
 * </p><p>
 * When corresponding failures reach following thresholds, then it raises
 * associated error:
 * <ul>
 * <li>{@link #FAILURES_THRESHOLD_ID}: Number of tolerated failure coming
 * from selenium WebDriver API (ie. when a {@link WebDriverException} is raised)
 * when running the <b>entire</b> scenario.<br>
 * Above the threshold, the test will fail, otherwise it will run it again in case
 * it was a transient problem. In the latter case, a snapshot will be taken and
 * put in the warning directory and the failure stack trace will be written in the
 * console output.</li>
 * <li>{@link SpotScenarioExecutionCounters#ALERTS_THRESHOLD_ID}: Number of tolerated alerts when running
 * the <b>entire</b> scenario.<br>
 * Above the threshold, the test will fail, otherwise it will accept the alert and
 * continue the execution.</li>
 * <li>{@link SpotScenarioExecutionCounters#RETRIABLE_ERRORS_THRESHOLD_ID}: Number of tolerated timeouts failure
 * when running the <b>entire</b> scenario.<br>
 * Above the threshold, the test will fail, otherwise it will run it again in case
 * it was a transient problem. In the latter case, a snapshot will be taken and
 * put in the warning directory and the failure stack trace will be written in the
 * console output.</li>
 * </ul>
 * </p><p>
 * For test counters, it counts the total, executed, skipped, succeeded and failed tests.
 * </p>
 */
public class SpotScenarioExecutionCounters {

	/* Constants */
	private final static String FAILURES_THRESHOLD_ID = "failuresThreshold";
	private final static int DEFAULT_FAILURES_THRESHOLD = 2;
	private final static String ALERTS_THRESHOLD_ID = "alertsThreshold";
	private final static int DEFAULT_ALERTS_THRESHOLD = 10;
	private final static String RETRIABLE_ERRORS_THRESHOLD_ID = "retriableErrorsThreshold";
	private final static String RETRIABLE_FAILURES_THRESHOLD_ID = "retriableFailuresThreshold";
	private final static String MULTIPLES_THRESHOLD_ID = "multiplesThreshold";
	private final static String BROWSER_ERRORS_THRESHOLD_ID = "browserErrorsThreshold";
	private final static int DEFAULT_RETRIABLES_THRESHOLD = 2;
	private final static int DEFAULT_RETRIABLES_FAILURES_THRESHOLD = 5;
	private final static int DEFAULT_MULTIPLES_THRESHOLD = 2;
	private final static int DEFAULT_BROWSER_ERRORS_THRESHOLD = 2;

	// Failure thresholds
	final int failuresThreshold;
	final int alertsThreshold;
	final int retriableErrorsThreshold;
	final int retriableFailuresThreshold;
	final int multiplesThreshold;
	final int browserErrorsThreshold;

	// Failure counters
	int failures;
	int alerts = 0;
	int retriables = 0;
	int retriablesFailures = 0;
	int multiples;
	int browserErrors = 0;

	// Tests counters
	int executedTests = 0;
	int succeededTests = 0;
	int failedTests = 0;
	int testCount = 0;
	int skippedTests = 0;

SpotScenarioExecutionCounters() {
	super();
	this.failuresThreshold = getParameterIntValue(FAILURES_THRESHOLD_ID, DEFAULT_FAILURES_THRESHOLD);
	this.alertsThreshold = getParameterIntValue(ALERTS_THRESHOLD_ID, DEFAULT_ALERTS_THRESHOLD);
	this.retriableErrorsThreshold = getParameterIntValue(RETRIABLE_ERRORS_THRESHOLD_ID, DEFAULT_RETRIABLES_THRESHOLD);
	this.retriableFailuresThreshold = getParameterIntValue(RETRIABLE_FAILURES_THRESHOLD_ID, DEFAULT_RETRIABLES_FAILURES_THRESHOLD);
	this.browserErrorsThreshold = getParameterIntValue(BROWSER_ERRORS_THRESHOLD_ID, DEFAULT_BROWSER_ERRORS_THRESHOLD);
	this.multiplesThreshold = getParameterIntValue(MULTIPLES_THRESHOLD_ID, DEFAULT_MULTIPLES_THRESHOLD);
}

public int getFailedTests() {
	return this.failedTests;
}

/**
 * Tells whether the max alerts threshold has been reached or not.
 * <p>
 * Note that calling this method increases the alerts counter when the threshold is not reached yet...
 * </p>
 *
 * @return <code>true</code> if the threshold is reached, <code>false</code> otherwise
 */
boolean hasMaxAlerts() {
	if (this.alerts >= this.alertsThreshold) {
		return true;
	}
	this.alerts++;
	return false;
}

/**
 * Tells whether the max browser errors threshold has been reached or not.
 * <p>
 * Note that calling this method increases the browser errors counter when the threshold is not reached yet...
 * </p>
 *
 * @return <code>true</code> if the threshold is reached, <code>false</code> otherwise
 */
boolean hasMaxBrowserErrors() {
	if (this.browserErrors >= this.browserErrorsThreshold) {
		return true;
	}
	this.browserErrors++;
	return false;
}

/**
 * Tells whether the max failures threshold has been reached or not.
 * <p>
 * Note that calling this method increases the failures counter when the threshold is not reached yet...
 * </p>
 *
 * @return <code>true</code> if the threshold is reached, <code>false</code> otherwise
 */
boolean hasMaxFailures() {
	if (this.failures >= this.failuresThreshold) {
		return true;
	}
	this.failures++;
	return false;
}

/**
 * Tells whether the max multiple errors threshold has been reached or not.
 * <p>
 * Note that calling this method increases the multiple errors counter when the threshold is not reached yet...
 * </p>
 *
 * @return <code>true</code> if the threshold is reached, <code>false</code> otherwise
 */
boolean hasMaxMultiples() {
	if (this.multiples >= this.multiplesThreshold) {
		return true;
	}
	this.multiples++;
	return false;
}

/**
 * Tells whether the max retriable errors threshold has been reached or not.
 * <p>
 * Note that calling this method increases the retriable errors counter when the threshold is not reached yet...
 * </p>
 *
 * @return <code>true</code> if the threshold is reached, <code>false</code> otherwise
 */
boolean hasMaxRetriableErrors() {
	if (this.retriables >= this.retriableErrorsThreshold) {
		return true;
	}
	this.retriables++;
	return false;
}

/**
 * Tells whether the max retriable failures threshold has been reached or not.
 * <p>
 * Note that calling this method increases the retriable failures counter when the threshold is not reached yet...
 * </p>
 *
 * @return <code>true</code> if the threshold is reached, <code>false</code> otherwise
 */
boolean hasMaxRetriableFailures() {
	if (this.retriablesFailures >= this.retriableFailuresThreshold) {
		return true;
	}
	this.retriablesFailures++;
	return false;
}

/**
 * Print the current scenario execution results.
 * <p>
 * Execution results displays following counters:
 * <ul>
 * <li>total: Number of tests defined in scenario</li>
 * <li>skipped: Number of skipped (or ignored) tests. Displayed only if there are any during scenario execution)</li>
 * <li>executed: Number of actually executed tests (less than total if some properties hide some of them)</li>
 * <li>succeeded: Number of passed (green) tests</li>
 * <li>failed: Number of failed (red) tests</li>
 * </ul>
 * </p>
 * <p>
 * It also displays following ratios:
 * <ul>
 * <li>Execution ratio: Number of executed tests regarding defined tests (ie <code>executed/total</code>)</li>
 * <li>Success ratio: Number of passed tests regarding executed tests (ie <code>succeeded/executed</code>)</li>
 * <li>Global ratio: Number of passed/tests regarding defined tests (ie <code>succeeded/total</code>)</li>
 * </ul>
 */
public void printExecutionResults() {
	double execRatio = (1.0 * this.executedTests) / this.testCount;
	double successRatio = this.executedTests==0 ? 0 : (1.0 * this.succeededTests) / this.executedTests;
	double globalRatio = (1.0 * this.succeededTests) / this.testCount;
	NumberFormat format = NumberFormat.getPercentInstance();
	format.setMaximumFractionDigits(1);
	String executed = format.format(execRatio);
	String success = format.format(successRatio);
	String global = format.format(globalRatio);
	println();
	println("Scenario final results: ");
	print("	tests: " + this.testCount + " defined");
	if (this.skippedTests > 0) {
		print(" (" + this.skippedTests + " skipped)");
	}
	println(", " + this.executedTests + " executed, " + this.succeededTests + " succeeded, " + this.failedTests + " failed");
	println("	ratio: " + executed + " executed, " + success + " success, " + global + " global");
}
}
