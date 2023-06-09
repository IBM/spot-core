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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getClassSimpleName;

import java.lang.annotation.Annotation;
import java.util.*;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.*;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioImplementationError;
import com.ibm.bear.qa.spot.core.utils.FailureBlocker;
import com.ibm.bear.qa.spot.core.utils.FailureRelaxer;

/**
 * Manage scenario step JUnit run.
 * <p>
 * This specific JUnit 4 runner allow to identify critical tests which will imply
 * a scenario execution stop if a failure would occur in those tests.
 * </p><p>
 * Note that it also filter tests using {@link ScenarioRunner} constants values.
 * However, this is temporary as with next JUnit 4 version (ie. 4.10), that should
 * not be longer necessary to do it here...
 * </p>
 */
public abstract class ScenarioStepRunner extends BlockJUnit4ClassRunner {

	/**
	 * The step tests sorter class.
	 * <p>
	 * Default is to sort tests using alphabetical ascending order.
	 * </p>
	 */
	class ScenarioStepTestsSorter extends Sorter {

		public ScenarioStepTestsSorter(final Comparator<Description> comparator) {
			super(comparator);
		}

	}

	/**
	 * The test comparator.
	 * <p>
	 * Strangely, having set the runner sorter seems not to be enough with JUnit 4.8.1. We need this additional comparator to sort the children list (see {@link #getChildren()}).
	 * </p>
	 */
	final Comparator<Description> testsComparator = new Comparator<Description>() {
		@Override
		public int compare(final Description d1, final Description d2) {
			return d1.getMethodName().compareTo(d2.getMethodName());
		}
	};

	/**
	 * The test sorter used by JUnit parent classes (see {@link ParentRunner}).
	 */
	protected Sorter testsSorter;

	protected ScenarioExecution scenarioExecution;
	private List<FrameworkMethod> criticalTests;
	private boolean shouldEndExecution = false;
	private int testCounter = 0;

public ScenarioStepRunner(final Class< ? > klass) throws InitializationError {
	super(klass);
	sort(new Sorter(this.testsComparator));

	// Extract critical tests
	this.criticalTests = extractCriticalTests();

	// Start step execution
	startExecution();
}

/**
 * {@inheritDoc}
 * <p>
 * Store the scenario execution object inside the created test.
 * </p>
 */
@Override
protected Object createTest() throws Exception {
    Object test = super.createTest();
    if (test instanceof ScenarioStep) {
    	((ScenarioStep)test).scenarioExecution = this.scenarioExecution;
    } else {
    	throw new ScenarioImplementationError("All created tests should be a subclass of ScenarioStep.");
    }
    return test;
}

private List<FrameworkMethod> extractCriticalTests() {
	// Check whether the step is mandatory
	TestClass klass = getTestClass();
	boolean mandatoryClass = false;
	for (Annotation annotation: klass.getAnnotations()) {
		if (annotation.annotationType().equals(FailureBlocker.class)) {
			mandatoryClass = true;
			break;
		}
	}

	// Check whether there are any mandatory tests
	List<FrameworkMethod> tests = klass.getAnnotatedMethods(Test.class);
	List<FrameworkMethod> mandatoryTests;
	if (mandatoryClass) {
		mandatoryTests = new ArrayList<FrameworkMethod>(tests);
		mandatoryTests.removeAll(klass.getAnnotatedMethods(FailureRelaxer.class));
	} else {
		mandatoryTests = new ArrayList<FrameworkMethod>();
		for (FrameworkMethod mandatoryTest: klass.getAnnotatedMethods(FailureBlocker.class)) {
			if (tests.contains(mandatoryTest)) {
				mandatoryTests.add(mandatoryTest);
			}
		}
	}
	return mandatoryTests;
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
 * Skip the child run if it should stop.
 * </p>
 */
@Override
protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
	if (this.scenarioExecution == null) {
		throw new RuntimeException("Cannot run SPOT test using this way. You need to run the entire scenario using -Dsteps="+getClassSimpleName(method.getDeclaringClass())+" -Dtests="+method.getName()+" properties...");
	}
	if (this.testCounter == 0) {
		this.scenarioExecution.checkClosingBrowser(method, /*lastTest:*/false);
	}
	if (!this.scenarioExecution.shouldStop()) {
		if (isIgnored(method)) {
			this.scenarioExecution.addSkippedTest();
		}
		super.runChild(method, notifier);
	}
	if (++this.testCounter == testCount()) {
		this.scenarioExecution.checkClosingBrowser(method, /*lastTest:*/true);
		if (this.shouldEndExecution) {
			this.scenarioExecution.shutdown();
		}
	}
}

/**
 * Store the scenario execution.
 *
 * @param execution the scenarioExecution to set
 */
public void setScenarioExecution(final ScenarioExecution execution) {
	this.scenarioExecution = execution;

	// Add the current step mandatory tests to the scenario execution
	this.scenarioExecution.addMandatoryTests(this.criticalTests);
}

/**
 * Start the scenario execution.
 * <p>
 * Subclasses needs to override this action in order to initialize their specific
 * configuration and data.
 * </p>
 */
abstract protected void startExecution();
}
