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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * Performance Manager class, for gathering and storing the information for multiple performance page/action result.
 * <p>
 * The performance manager class gathers and stores the information for multiple performance results.
 * Gathering of the results is called from a general instrumentation in the ClmWebPage where
 * step, test, and page/element details are recorded along with the response time the action took.
 * All of this data is managed inside of the performance manager class and at the end of the run written
 * out to a csv file.
 * <ul>
 * <li>{@link #PERFORMANCE_ENABLED}: Optional user provided value to enable/disable performance gathering.</li>
 * <li>{@link #PERFORMANCE_FILE_LOCATION}: Optional user provided value to set the csv file output location/file.</li>
 * <li>{@link #PERFORMANCE_DEBUG_ENABLED}: Optional user provided value to enable/disable performance debug messages/images.</li>
 * <li>{@link #PERFORMANCE_DEBUG_LOCATION}: Optional user provided value to set the debug folder output location.</li>
 * <li>{@link #PERFORMANCE_DELAY_WINDOW}: Optional user provided value to change the default delay window during client monitoring.</li>
 * <li>{@link #PERFORMANCE_MAXIMUM_DELAY_WINDOW}: Optional user provided value to change the default maximum delay window during client monitoring.</li>
 * <li></li>
 * <li>{@link #taskDataWriter}:Task data writer for writing the final results to file.</li>
 * <li>{@link #debugLogWriter}:Debug log writer for writing debug messages to file.</li>
 * <li>{@link #perfResults}: ArrayList of the PerfResults gathered by the manager.</li>
 * <li>{@link #stepName}: step name for the current result.</li>
 * <li>{@link #testName}: test name for the current result.</li>
 * <li>{@link #userActionName}: optional user defined action name for the current result.</li>
 * <li>{@link #regressionType}: the type of regression measurement (server/client) which should be measured.</li>
 * <li>{@link #regressionTypeLocked}: boolean which indicates if the regression type is locked and should not be changed.</li>
 * <li>{@link #clientTimer}: client stop watch timer.</li>
 * <li>{@link #serverTimer}: server stop watch timer.</li>
 * <li>{@link #clientLoadTime}: current client load time measurement.</li>
 * <li>{@link #serverLoadTime}: current server load time measurement.</li>
 * <li>{@link #timeDateStamp}: time/date stamp taken at start of server measurement.</li>
 * <li>{@link #pageLoading}: boolean which indicates if the current page is loading.</li>
 * </ul>
 * </p>
 */
public class PerfManager {

/* Constants */
public static final String USER_ACTION_NOT_PROVIDED = "User action name not provided";

// User Provided Variables
final static String PERFORMANCE_DEFAULT_FILE = System.getProperty("user.dir")+File.separator+"DefaultPerfResults.csv";
final static String PERFORMANCE_DEFAULT_DEBUG_FILE = System.getProperty("user.dir")+File.separator+"DefaultPerfDebugLog.log";
final static String PERFORMANCE_DEFAULT_DEBUG = System.getProperty("user.dir")+File.separator;
final static int DEFAULT_DELAY_WINDOW = 5;
final static int DEFAULT_MAX_DELAY_WINDOW = 60;
public final static boolean PERFORMANCE_ENABLED = getParameterBooleanValue("performanceEnabled", false);
final static String PERFORMANCE_FILE_LOCATION = (getParameterValue("performanceFileLocation", PERFORMANCE_DEFAULT_FILE)).replaceAll(".csv", COMPACT_DATE_STRING+".csv");
final static String PERFORMANCE_DEBUG_FILE_LOCATION = (getParameterValue("performanceDebugFileLocation", PERFORMANCE_DEFAULT_DEBUG_FILE)).replaceAll(".log", COMPACT_DATE_STRING+".log");
final static boolean PERFORMANCE_DEBUG_ENABLED = getParameterBooleanValue("performanceDebugEnabled",false);
final static String PERFORMANCE_DEBUG_LOCATION = getParameterValue("performanceDebugLocation", PERFORMANCE_DEFAULT_DEBUG);
final static int PERFORMANCE_DELAY_WINDOW = getParameterIntValue("performanceDelayWindow", DEFAULT_DELAY_WINDOW)*1000; // Convert user input in seconds to milliseconds
final static int PERFORMANCE_MAXIMUM_DELAY_WINDOW = getParameterIntValue("performanceMaximumDelayWindow", DEFAULT_MAX_DELAY_WINDOW)*1000; // Convert user input in seconds to milliseconds
public final static int PERFORMANCE_LOOPS = getParameterIntValue("performanceNumberOfLoops", 1);

// Global Variables
final ArrayList<PerfResult> perfResults = new ArrayList<PerfResult>();
String stepName = "Step name not provided";
String testName = "Test name not provided";
String userActionName = USER_ACTION_NOT_PROVIDED;
RegressionType regressionType = RegressionType.Server;
boolean regressionTypeLocked = false;
Timer clientTimer = new Timer();
double clientLoadTime = 0;
Timer serverTimer = new Timer();
double serverLoadTime = 0;
long timeDateStamp = 0;
boolean pageLoading = false;
final WebBrowser browser;
final LogWriter debugLogWriter;
final TaskDataWriter taskDataWriter;

public enum RegressionType { Client, Server }

/**
 * Create an instance of the performance manager using the given browser.
 * <p>
 * <b>Warning</b>: this method returns <code>null</code> if the performance
 * are not activated during the scenario execution (see {@link #PERFORMANCE_ENABLED}).
 * </p>
 * @param browser The browser using the current performance manager
 * @return The created instance as a {@link PerfManager} or <code>null</code>
 * if the performances are not managed during the scenario execution.
 */
public static PerfManager createInstance(final WebBrowser browser) {
	if (PERFORMANCE_ENABLED) {
		return new PerfManager(browser);
	}
	return null;
}

private PerfManager(final WebBrowser browser){
	this.browser = browser;
	this.taskDataWriter = new TaskDataWriter(PERFORMANCE_FILE_LOCATION);

	// Only create debugLogWriter if debug is enabled
	if (PERFORMANCE_DEBUG_ENABLED) {
		this.debugLogWriter = new LogWriter(PERFORMANCE_DEBUG_FILE_LOCATION);
	} else {
		this.debugLogWriter = null;
	}
}

/**
 * Add a new performance timing result to the PerfManager.
 */
public void addPerfResult(final String pageTitle, final String url) {

	// Variables
	PerfResult perfResult = null;
	int targetResultNum = 0;

	// Do nothing is the server load time was 0 in which case the timer was never started and the result is invalid
	if(this.serverLoadTime!=0){

		// Determine if a new result is needed or which result to update
		if (this.perfResults.size() != 0) {
			for (targetResultNum = 0; targetResultNum < this.perfResults.size(); targetResultNum++) {
				PerfResult result = this.perfResults.get(targetResultNum);
				// Check if the result matches the input names and set perfResult if a match is found
				if (result.doesResultMatch(this.stepName, this.testName, pageTitle, url, this.userActionName)){
					perfResult = result;
					break;
				}
			}
		}

		// Add new result, or update target result in perfResults array
		if (perfResult == null) {
			perfResult = new PerfResult(this.stepName,
				this.testName,
				url,
				pageTitle,
				this.userActionName,
				this.regressionType,
				this.serverLoadTime,
				this.clientLoadTime,
				this.timeDateStamp);
			this.perfResults.add(perfResult);
			if (PERFORMANCE_DEBUG_ENABLED) {
				this.debugLogWriter.writeNext("New Result: " + this.stepName + " " + this.testName
					+ " " + pageTitle + " " + this.userActionName + " " + Timer.round(this.serverLoadTime + this.clientLoadTime, 2));
			}
		} else {
			perfResult.addResponseTime(this.serverLoadTime, this.clientLoadTime,this.timeDateStamp);
			this.perfResults.set(targetResultNum, perfResult);
			if (PERFORMANCE_DEBUG_ENABLED) {
				this.debugLogWriter.writeNext("Duplicate Result: " + perfResult.getStepName() + " "
						+ perfResult.getTestName() + " " + perfResult.getPageTitle() + " " + perfResult.getUserActionName() + " "
						+ Timer.round(this.serverLoadTime + this.clientLoadTime, 2));
			}
		}

		// Write last result to taskDataWriter
		this.taskDataWriter.write(perfResult);

		// Reset for next transaction
		reset();
	}
}

/**
 * Close the writers
 */
public void close() {
	this.taskDataWriter.close();
	if (PERFORMANCE_DEBUG_ENABLED) {
		this.debugLogWriter.close();
	}
}

/**
 * End serverTimer and properly set serverLoadTime to the total time recorded.
 */
public void endServerTimer(){
	this.serverTimer.end();
	this.serverLoadTime = this.serverTimer.getTotalTime();
	this.timeDateStamp = this.serverTimer.getTimeDateStamp();
	if (PERFORMANCE_DEBUG_ENABLED) {
		this.debugLogWriter.writeNext("Web Page Server Load Took: "+this.serverLoadTime);
	}
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
 * Close the writer when writing is complete.
 */
public void loadClient() {

	// Variables
	this.clientLoadTime = 0;
	byte[] last;
	byte[] current;
	byte[] oscillation;
	long loopStart;
	long loopLength;

	// End the server timer and start the client timer
	endServerTimer();

	// Do nothing is the server load time was 0 in which case the timer was never started and the result is invalid
	if(this.serverLoadTime!=0){
		// Set page loading status
		setPageLoading(true);

		// Start client timer
		this.clientTimer.start();

		try {
			// Set endTime
			long endTime = System.currentTimeMillis() + PERFORMANCE_DELAY_WINDOW;
			long maxEndTime = System.currentTimeMillis() + PERFORMANCE_MAXIMUM_DELAY_WINDOW;

			// Set current snapshot to last image and sleep for 0.1 sec
			last = this.browser.takeScreenshotsBytes();
			oscillation = last;
			if (PERFORMANCE_DEBUG_ENABLED) {
				String fileName = PERFORMANCE_DEBUG_LOCATION + System.currentTimeMillis() + "_" + this.stepName + "_"
				        + this.testName + "_" + this.userActionName + "_" + PerfResult.regressionTypeToString(this.regressionType)
				        + "TYPE" + "_BASE_" + this.serverLoadTime + ".jpg";
				try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
					fileOutputStream.write(last);
				}
			}

			pause(100);

			while ((loopStart = System.currentTimeMillis()) < endTime || loopStart > maxEndTime) {

				// Set current image
				current = this.browser.takeScreenshotsBytes();

				// Check if Screenshots Match
				if (!Arrays.equals(last, current)) {

					// Something has changed, check oscillation to ensure we are
					// not oscillating from a blinking update
					if (!Arrays.equals(oscillation, current)) {
						// Record the current time and reset the end time
						this.clientTimer.end();
						this.clientLoadTime = this.clientTimer.getTotalTime();
						endTime = System.currentTimeMillis() + PERFORMANCE_DELAY_WINDOW;
						if (PERFORMANCE_DEBUG_ENABLED) {
							this.debugLogWriter.writeNext("Client change detected at: " + this.clientLoadTime);
							String fileName = PERFORMANCE_DEBUG_LOCATION + System.currentTimeMillis() + "_"
						        + this.stepName + "_" + this.testName + "_" + this.userActionName + "_"
						        + PerfResult.regressionTypeToString(this.regressionType)
						        + "TYPE" + "_CHANGE_" + this.clientLoadTime + ".jpg";
							try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
								fileOutputStream.write(current);
							}
						}
						oscillation = last;
						last = current;
					}
				}

				// Check the loop length and sleep any excess time to reach a
				// tenth of a second
				loopLength = System.currentTimeMillis() - loopStart;
				// System.out.println("Image comparison took: "+loopLength+" ms");
				if (loopLength < 100) {
					pause(100 - loopLength);
				}
			}
		} catch (IOException e) {
			println("Problem writing screenshots to disk occured, please make sure directory exists.");
			e.printStackTrace();
		}

		setPageLoading(false);
		if (PERFORMANCE_DEBUG_ENABLED) {
			this.debugLogWriter.writeNext("Web Page Client Load Took: " + this.clientLoadTime);
		}
	}
}

/**
 * Reset the user action name, regression type, load times, and timers.
 */
public void reset() {
	// Reset User Action Title
	this.userActionName = USER_ACTION_NOT_PROVIDED;

	// Reset regression type lock
	resetRegressionType();

	// Reset load times
	this.serverLoadTime = 0;
	this.clientLoadTime = 0;
	this.timeDateStamp = 0;

	// Reset timers;
	this.serverTimer.reset();
	this.clientTimer.reset();
}

/**
 * Reset the regression type and clear the lock.
 */
public void resetRegressionType() {
	this.regressionType = RegressionType.Server;
	this.regressionTypeLocked = false;
}

/**
 * Set the default regression type to provided regressionType.
 * @param regressionType : Regression type to apply.
 * @param override : True will override and lock the regression type,
 * while false will only change the regression type if it is not locked.
 */
public void setRegressionType(final RegressionType regressionType, final boolean override) {
	if (override) {
		this.regressionType = regressionType;
		this.regressionTypeLocked = true;
	} else if (!this.regressionTypeLocked) {
		this.regressionType = regressionType;
	}
}

/**
 * Set pageLoading to true/false
 */
public void setPageLoading(final boolean loadingState){
	this.pageLoading = loadingState;
}

/**
 * Set step name to keep track of the current step name.
 */
public void setStepName(final String currentStepName){
	this.stepName = currentStepName;
}

/**
 * Set test name to keep track of the current test name.
 */
public void setTestName(final String currentTestName){
	this.testName = currentTestName;
}

/**
 * Set userActionName to provided name
 */
public void setUserActionName(final String name) {
	// Reset the timers to mark a new action is taking place
	reset();

	// Set user action name
	this.userActionName = name;
}

/**
 * Start serverTimer if page is NOT loading
 */
public void startServerTimer(){
	if (!this.pageLoading) {
		this.serverTimer.start();
	}
}

}
