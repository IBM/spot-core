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

import static com.ibm.bear.qa.spot.core.performance.PerfManager.PERFORMANCE_LOOPS;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.CollectionsUtil.getListFromCommaString;

import java.util.*;

import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioSynchronizationError;
import com.ibm.bear.qa.spot.core.utils.DependsOn;

/**
 * Manage scenario JUnit run.
 * <p>
 * This specific JUnit 4 runner allow to tightly control the scenario content
 * hierarchy by specifying the following parameters:
 * <ul>
 * <li>{@link #FIRST_STEP}: step from which the scenario has to start instead
 * of default one.</li>
 * <li>{@link #LAST_STEP}: step at which the scenario has to end instead
 * of default one.</li>
 * <li>{@link #FIRST_TEST}: test of the starting step from which the scenario
 * has to start instead of default one.</li>
 * <li>{@link #LAST_TEST}: test of the starting step at which the scenario
 * has to end instead of default one.</li>
 * <li>{@link #STEPS}: steps the scenario has to run</li>
 * </ul>
 * </p>
 */
public abstract class ScenarioRunner extends Suite {

	// Constants
	final static String FIRST_STEP = getParameterValue("firstStep");
	final static String LAST_STEP = getParameterValue("lastStep");
	final static String STEPS = getParameterValue("steps");
	final static List<String> STEPS_LIST = getListFromCommaString(STEPS);
	final static String FIRST_TEST = getParameterValue("firstTest");
	final static String LAST_TEST = getParameterValue("lastTest");
	final static SpotTestsList TESTS_LIST = new SpotTestsList("tests");
	final static SpotTestsList IGNORE_TESTS_LIST = new SpotTestsList("ignore.tests");

	// Data
	protected ScenarioExecution scenarioExecution;

	// Execution controls
	boolean stopOnFailure;
	private final Set<String> steps = new HashSet<String>();
	protected final List<Description> filteredSteps = new ArrayList<Description>();
	protected final List<Description> filteredTests = new ArrayList<Description>();
	protected Description firstStep = null, lastStep = null;
	protected Description firstTest = null, lastTest = null;

public ScenarioRunner(final Class< ? > klass, final RunnerBuilder builder) throws InitializationError {
	super(klass, builder);

	// Start execution
	try {
		startExecution();
	}
	catch (Throwable t) {
		printException(t);
		throw new ScenarioFailedError(t);
	}

	// Store scenario class
	getScenarioExecution().scenarioClass = getClassSimpleName(klass);

	// Show Selenium and Browser information
	getScenarioExecution().showInfo();

	// Build class filter
	Filter filter = new Filter() {

		@Override
		public String describe() {
			StringBuilder filterDescriptionBuilder = new StringBuilder("Filtering scenario step:").append(LINE_SEPARATOR);
			if (FIRST_STEP != null) {
				filterDescriptionBuilder.append("	- 'firstStep' argument set to \"").append(FIRST_STEP).append("\"");
			}
			if (LAST_STEP != null) {
				filterDescriptionBuilder.append("	- 'lastStep' argument set to \"").append(LAST_STEP).append("\"");
			}
			if (STEPS_LIST != null) {
				filterDescriptionBuilder.append("	- 'steps' argument set to \"").append(STEPS_LIST).append("\"");
			}
			if (FIRST_TEST != null) {
				filterDescriptionBuilder.append("	- 'firstTest' argument set to \"").append(FIRST_TEST).append("\"");
			}
			if (LAST_TEST != null) {
				filterDescriptionBuilder.append("	- 'lastTest' argument set to \"").append(LAST_TEST).append("\"");
			}
			if (TESTS_LIST != null) {
				filterDescriptionBuilder.append("	- 'tests' argument set to \"").append(TESTS_LIST).append("\"");
			}
			return filterDescriptionBuilder.toString();
		}

		/**
		 * First test whether the given step should be run or not depending on the
		 * controls arguments. If so, then check whether the test should be run.
		 */
		@Override
		public boolean shouldRun(final Description description) {
			return ScenarioRunner.this.shouldRun(description);
		}
	};

	// Store scenario tests count (before filtering)
	this.scenarioExecution.setTestCount(testCount());

	// Filter steps and tests
	try {
		filter(filter);
	} catch (NoTestsRemainException ntre) {
		printException(ntre);
		throw new ScenarioFailedError(ntre);
	}
}

/**
 * Ends the scenario execution.
 */
protected void endExecution() {
	this.scenarioExecution.shutdown();
}

/**
 * @return the scenarioExecution
 */
public ScenarioExecution getScenarioExecution() {
	return this.scenarioExecution;
}

/**
 * {@inheritDoc}
 * <p>
 * Override basic JUnit 4 implementation to:
 * <ol>
 * <li>Propagate the scenario execution object to steps and tests</li>
 * <li>Perform some needed stuff at the end of the scenario execution</li>
 * <ol>
 */
@Override
public void run(final RunNotifier notifier) {

	// Propagate config to step runners
	for (Runner runner : getChildren()) {
		// Check if there was an error while creating the step runner...
		if (runner instanceof ErrorReportingRunner) {
			ErrorReportingRunner error = (ErrorReportingRunner) runner;
			Description description = error.getDescription();
			throw new ScenarioFailedError(description.getChildren().get(0).getDisplayName());
		}
		// Stored scenario execution depends on step runner scenario
		ScenarioStepRunner stepRunner = (ScenarioStepRunner) runner;
		ScenarioExecution stepScenarioExecution = this.scenarioExecution.getStepScenarioExecution(stepRunner.getScenarioExecution());
		stepRunner.setScenarioExecution(stepScenarioExecution);
	}

	// Check synchronization
	if (this.scenarioExecution.shouldSynchronize && !this.scenarioExecution.canSynchronize) {
		throw new ScenarioSynchronizationError("Scenario should use synchronization but that could not be activated, hence stop execution.");
	}

	// Looping of scenarios for performance testing
	for (int i = 0; i < PERFORMANCE_LOOPS; i++) {
		// Run the scenario
		super.run(notifier);
	}

	// End execution
	endExecution();
}

/**
 * {@inheritDoc}
 * <p>
 * Skip the child run if it should stop.
 * </p>
 */
@Override
protected void runChild(final Runner runner, final RunNotifier notifier) {
	if (!getScenarioExecution().shouldStop()) {
		super.runChild(runner, notifier);
	}
}

/**
 * Start the scenario execution.
 * <p>
 * Subclasses needs to override this action in order to initialize their specific
 * configuration and data.
 * </p>
 */
abstract protected void startExecution();

/**
 * Method to filter steps when building the JUnit test suite hierarchy.
 *
 * @param classDescription The step class to be added to the test suite
 * @return <code>true</code> if the step should be run, <code>false</code>
 * otherwise.
 */
protected boolean stepShouldRun(final Description classDescription) {
	String testClassSimpleName = getClassSimpleName(classDescription.getClassName());
	if (STEPS_LIST.size() == 0) {
		if (this.firstStep == null && FIRST_STEP != null && FIRST_STEP.length() != 0) {
			if (classDescription.getAnnotation(DependsOn.class) != null) {
				String message = "Cannot start scenario from step "+classDescription.getClassName()+" because it depends on another step.";
				System.err.println(message);
				throw new ScenarioFailedError(message);
			}
			if (!testClassSimpleName.contains(FIRST_STEP)) {
				if (!this.filteredSteps.contains(classDescription)) {
					println("Filtering "+classDescription+ " due to 'firstStep' argument set to \""+FIRST_STEP+"\"");
					this.filteredSteps.add(classDescription);
				}
				return false;
			}
		}
		if (this.lastStep == null) {
			if (LAST_STEP != null && LAST_STEP.length() != 0) {
				if (testClassSimpleName.contains(LAST_STEP)) {
					this.lastStep = classDescription;
				}
			}
		} else if (!this.steps.contains(testClassSimpleName) && testClassSimpleName.compareTo(getClassSimpleName(this.lastStep.getClassName())) > 0) {
			if (!this.filteredSteps.contains(classDescription)) {
				println("Filtering "+classDescription+ " due to 'lastStep' argument set to \""+LAST_STEP+"\"");
				this.filteredSteps.add(classDescription);
			}
			return false;
		}
	} else {
		boolean found = false;
		for (String step: STEPS_LIST) {
			if (testClassSimpleName.contains(step)) {
				if (classDescription.getAnnotation(DependsOn.class) != null) {
					String message = "Cannot insert "+classDescription.getClassName()+" in steps list because it depends on another step.";
					System.err.println(message);
					throw new ScenarioFailedError(message);
				}
				found = true;
				break;
			}
		}
		if (!found) {
			if (!this.filteredSteps.contains(classDescription)) {
				println("Filtering "+classDescription+ " due to 'steps' argument set to \""+STEPS+"\"");
				this.filteredSteps.add(classDescription);
				// TODO Warn if runStart and/or runEnd is set
			}
			return false;
		}
	}
	this.steps.add(testClassSimpleName);
	return true;
}

/*
 * Check whether the given class or method description should run or not.
 */
boolean shouldRun(final Description description) {
	if (description.getMethodName() == null) {
		// Class description
		if (stepShouldRun(description)) {
			if (this.firstStep == null) {
				this.firstStep = description;
			}
			return true;
		}
	} else {
		// Method description
		if (testShouldRun(description)) {
			if (this.firstTest == null) {
				this.firstTest = description;
			}
			if (description.getAnnotation(DependsOn.class) != null) {
				this.scenarioExecution.shouldSynchronize = true;
			}
			return true;
		}
	}
	return false;
}

/**
 * Method to filter tests of the first step when building the JUnit test suite
 * hierarchy.
 *
 * @param methodDescription The test to be added to the test suite
 * @return <code>true</code> if the test should be run, <code>false</code>
 * otherwise.
 */
protected boolean testShouldRun(final Description methodDescription) {
	String testClassSimpleName = getClassSimpleName(methodDescription.getClassName());
	String testName = methodDescription.getMethodName();
	final String testQualifiedName = testClassSimpleName+"."+testName;
	boolean foundTest = false;
	if (TESTS_LIST.hasNoTest()) {
		if (this.firstTest == null && FIRST_TEST != null && FIRST_TEST.length() != 0) {
			if (!testName.contains(FIRST_TEST)) {
				println("Filtering " + testQualifiedName + " due to 'firstTest' argument set to \"" + FIRST_TEST + "\"");
				this.filteredTests.add(methodDescription);
				return false;
			}
		}
		if (this.lastTest == null) {
			if (LAST_TEST != null && LAST_TEST.length() != 0) {
				if (testName.contains(LAST_TEST)) {
					this.lastTest = methodDescription;
				}
			}
		} else {
			println("Filtering " + testQualifiedName + " due to 'lastTest' argument set to \"" + LAST_TEST + "\"");
			this.filteredTests.add(methodDescription);
			return false;
		}
	} else {
		if (TESTS_LIST.match(testClassSimpleName, testName)) {
			if (methodDescription.getAnnotation(DependsOn.class) != null) {
				String message = "Cannot insert "+testQualifiedName+" in tests list because it depends on another test.";
				System.err.println(message);
				throw new ScenarioFailedError(message);
			}
			/* Having the test not re-runnable does not mean we can't start from it...
			 * We should introduce a specific annotation to prevent test to start from a test (e.g. NotStartable)
			if (methodDescription.getAnnotation(NotRerunnable.class) != null) {
				String message = "Cannot insert "+testQualifiedName+" in tests list because it's not rerunnable.";
				System.err.println(message);
				throw new ScenarioFailedError(message);
			}
			*/
			foundTest = true;
		}
		if (!foundTest) {
			// Look for step reference in steps list only if it's not used in tests list
			if (TESTS_LIST.hasNoStepTest(testClassSimpleName)) {
				for (String step: STEPS_LIST) {
					if (testClassSimpleName.contains(step)) {
						foundTest = true;
						break;
					}
				}
			}
			if (!foundTest) {
				println("Filtering " + testQualifiedName + " due to 'tests' argument set to \"" + TESTS_LIST.value + "\"");
				this.filteredTests.add(methodDescription);
				return false;
			}
		}
	}
	if (IGNORE_TESTS_LIST.match(testClassSimpleName, testName)) {
		// Test has to be ignored
		if (foundTest) {
			println("WARNING: test "+testQualifiedName+" is both specified to be run in 'tests' parameter and to be ignored:");
			println("	- run through 'tests' parameter = "+TESTS_LIST.value);
			println("	- ignored through 'ignore.tests' parameter = "+IGNORE_TESTS_LIST.value);
			println("	Priority has been given to ignore parameter, hence it won't be run...");
		} else {
			println("Filtering " + testQualifiedName + " due to 'ignore.tests' argument set to \"" + IGNORE_TESTS_LIST.value + "\"");
		}
		this.filteredTests.add(methodDescription);
		return false;
	}
	if (this.firstTest == null) {
		this.firstTest = methodDescription;
		if (FIRST_TEST != null && FIRST_TEST.length() != 0) {
			if (methodDescription.getAnnotation(DependsOn.class) != null) {
				String message = "Cannot start scenario from test "+testQualifiedName+" because it depends on another test.";
				System.err.println(message);
				throw new ScenarioFailedError(message);
			}
			/* Having the test not re-runnable does not mean we can't start from it...
			 * We should introduce a specific annotation to prevent test to start from a test (e.g. NotStartable)
			if (methodDescription.getAnnotation(NotRerunnable.class) != null) {
				String message = "Cannot start scenario from test "+testQualifiedName+" because it's not rerunnable.";
				System.err.println(message);
				throw new ScenarioFailedError(message);
			}
			*/
		}
	}
	if (this.firstTest == null) {
		this.firstTest = methodDescription;
	}
	return true;
}
}
