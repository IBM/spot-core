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
package com.ibm.bear.qa.spot.core.performance;

import static com.ibm.bear.qa.spot.core.performance.PerfManager.USER_ACTION_NOT_PROVIDED;

import java.util.ArrayList;

import com.ibm.bear.qa.spot.core.performance.PerfManager.RegressionType;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Performance Result class, for storing the information on a single performance
 * page/action result.
 * <p>
 * The performance result class stores the information for a "unique"
 * page/action and the performance timings gathered. Each individual PerfResult
 * has a set of properties (titles and other identifier information) and a
 * perfTimeList, which is a string of all the performance response times
 * gathered for this particular PerfResult.
 * <ul>
 * <li>{@link #stepName}: step name for the current result</li>
 * <li>{@link #testName}: test name for the current result</li>
 * <li>{@link #url}: url for the current result</li>
 * <li>{@link #pageTitle}: page title for the current result</li>
 * <li>{@link #userActionName}: optional user defined action name for the
 * current result</li>
 * <li>{@link #clientTimes}: client times; measured after server has finished sending data.</li>
 * <li>{@link #serverTimes}: server times; measured from first client click till server finishes sending data.</li>
 * <li>{@link #regressionTimes}: regression times; measurement based off the regressionType.  If the regressionType
 * is "server" then the regressionTimes include only server time.  If the regressionType is "client" then the
 * regressionTimes include client+server time.</li>
 * <li>{@link #timeDateStamps}: time/date stamps taken at the start of the user action (start of server time measurment)</li>
 * <li>{@link #regressionType}: the type of regression measurement (server/client) which should be measured.</li>
 * </ul>
 * </p>
 */
public class PerfResult {

// Global Variables
String stepName = "No step name provided";
String testName = "No test name provided";
String url = "No URL provided";
String pageTitle = "No page title provided";
String userActionName = "No user action name provided";
ArrayList<Double> clientTimes, serverTimes, regressionTimes;
ArrayList<String> timeDateStamps;
RegressionType regressionType;

public enum TimeType { Regression, Client, Server }

public PerfResult (final String stepName, final String testName, final String url, final String pageTitle, final String userActionName, final RegressionType regressionType, final double serverTime, final double clientTime, final long timeDateStamp){
	// Set Variables
	this.stepName = stepName;
	this.testName = testName;
	this.url = url;
	this.pageTitle = pageTitle;
	this.userActionName = userActionName;
	this.regressionType = regressionType;
	this.serverTimes = new ArrayList<Double>();
	this.clientTimes = new ArrayList<Double>();
	this.regressionTimes = new ArrayList<Double>();
	this.timeDateStamps = new ArrayList<String>();
	addResponseTime(serverTime, clientTime, timeDateStamp);
}

/**
 * Add a new response time to the perfTimeList for the perf result
 */
public void addResponseTime(final double serverTime, final double clientTime, final long timeDateStamp) {
	this.serverTimes.add(Double.valueOf(serverTime));
	this.clientTimes.add(Double.valueOf(clientTime));
	this.regressionTimes.add(Double.valueOf(PerfResult.getRegressionValue(serverTime,clientTime,this.regressionType)));
	this.timeDateStamps.add(TaskDataWriter.timestamp2(timeDateStamp,true));
}

/**
 * Check if the current result matches the input values.  This method's logic
 * effectively manages how results are aggregated and matched together.
 *
 * Returns true if the input names match the current result.
 * Returns false if any of the input names do not match the current result.
 *
 * @param stepNameInput : String of the stepName to match.
 * @param testNameInput : String of the testName to match.
 * @param pageTitleInput : String of the pageTitle to match.
 * @param urlInput : String of the url to match.
 * @param userActionNameInput : String of the userActionName to match.
 *
 * @return The matching results as {@link Boolean}.
 */
public boolean doesResultMatch(final String stepNameInput, final String testNameInput,
		final String pageTitleInput, final String urlInput, final String userActionNameInput) {
	if (this.userActionName.equals(USER_ACTION_NOT_PROVIDED)
			&& this.stepName.equals(stepNameInput)
			&& this.testName.equals(testNameInput)
			&& (this.pageTitle.replaceAll("[^a-zA-Z]", "")).equals(pageTitleInput.replaceAll("[^a-zA-Z]", ""))
			&& (this.url.replaceAll("(_([0-9A-Za-z-_]{22}))|(%[0-9A-F]{2})|[^a-zA-Z]", "")).equals(urlInput.replaceAll("(_([0-9A-Za-z-_]{22}))|(%[0-9A-F]{2})|[^a-zA-Z]", ""))) {
		return true;
	} else if (!this.userActionName.equals(USER_ACTION_NOT_PROVIDED)
			&& this.stepName.equals(stepNameInput)
			&& this.testName.equals(testNameInput)
			&& this.pageTitle.equals(pageTitleInput)
			&& this.userActionName.equals(userActionNameInput)) {
		return true;
	}
	return false;
}

/**
 * Get the last timeDateStamp
 *
 * @return The last time/date stamp as {@link String}.
 */
public String getLastTimeDateStamp() {
	return this.timeDateStamps.get(this.timeDateStamps.size()-1);
}

/**
 * Get the last regression time
 *
 * @return The last regression time as {@link Double}.
 */
public Double getLastRegressionTime() {
	return this.regressionTimes.get(this.regressionTimes.size()-1);
}

/**
 * Get the pageTitle
 *
 * @return The pageTitle as {@link String}.
 */
public String getPageTitle() {
	return this.pageTitle;
}

/**
 * Get the regression type
 *
 * @return The regression type as {@link RegressionType}.
 */
public RegressionType getRegressionType() {
	return this.regressionType;
}

/**
 * Get the regression value based off the regression type
 *
 * @return The regression value as {@link Double}.
 */
public static double getRegressionValue(final double serverTime, final double clientTime, final RegressionType regType) {
	if (regType==RegressionType.Server) {
		return serverTime;
	} else if (regType==RegressionType.Client ) {
		return serverTime+clientTime;
	} else {
		// Valid regression type was not provided
		return 0;
	}
}

ArrayList<String> getResults(final TimeType timeType) {

	// Put results strings at the beginning of the array
	ArrayList<String> perfResults = new ArrayList<String>();
	perfResults.add(this.stepName);
	perfResults.add(this.testName);
	perfResults.add(this.url);
	perfResults.add(this.pageTitle);
	perfResults.add(this.userActionName);
	perfResults.add(regressionTypeToString(this.regressionType));

	// Init time numbers array
	ArrayList<Double> times = null;
	switch (timeType) {
		case Client:
			times = this.clientTimes;
			break;
		case Server:
			times = this.serverTimes;
			break;
		case Regression:
			times = this.regressionTimes;
			break;
		default:
			throw new ScenarioFailedError("Mask a JDT compiler issue.");
	}

	// Add time numbers at the end of the array
	for (int i=0; i<times.size(); i++) {
		perfResults.add(Double.toString(Timer.round(times.get(i).doubleValue(),5)));
	}

	// Return the string array
	return perfResults;
}

/**
 * Get the stepName
 *
 * @return The stepName as {@link String}.
 */
public String getStepName() {
	return this.stepName;
}

/**
 * Get the testName
 *
 * @return The testName as {@link String}.
 */
public String getTestName() {
	return this.testName;
}

/**
 * Get url
 *
 * @return The url as {@link String}.
 */
public String getUrl() {
	return this.url;
}

/**
 * Get the userActionName
 *
 * @return The userActionName as {@link String}.
 */
public String getUserActionName() {
	return this.userActionName;
}

/**
 * Returns the regressionType as a string
 *
 * @return The regressionType as {@link String}.
 */
public static String regressionTypeToString(final RegressionType regressionType) {
	switch (regressionType) {
		case Client:
			return "Client";
		case Server:
			return "Server";
		default:
			return "Incorrect type specified";
	}
}
}
