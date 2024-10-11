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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterValue;

import java.util.*;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to manage list of tests defined from a parameter list
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #hasNoStepTest(String)}: Tells whether there is no reference to the given step class in tests list or not.</li>
 * <li>{@link #hasNoTest()}: Tells whether there no specified tests or not.</li>
 * <li>{@link #match(String,String)}: Returns whether one of the specified test matches the given test.</li>
 * </ul>
 * </p>
 */
public class SpotTestsList {

	final String param, value;
	final List<String> tests = new ArrayList<>();

SpotTestsList(final String p) {
	this.param = p;
	this.value = getParameterValue(this.param);
	init();
}

private void init() throws ScenarioFailedError {
	if (this.value != null) {
		StringTokenizer tokenizer = new StringTokenizer(this.value, ",");
		while (tokenizer.hasMoreTokens()) {
			this.tests.add(tokenizer.nextToken());
		}
	}
}

/**
 * Tells whether there is no reference to the given step class in tests list or not.
 *
 * @return <code>true</code> if no test refers to the given step class,
 * <code>false</code> otherwise
 */
public boolean hasNoStepTest(final String testClassSimpleName) {

	for (String test: this.tests) {
		String[] items = test.split(":|\\.");
		switch (items.length) {
			case 1:
				return false;
			case 2:
				if (testClassSimpleName.toLowerCase().contains(items[0].toLowerCase())) {
					return false;
				}
				break;
			default:
				throw new ScenarioFailedError("Invalid '" + this.param + "' property content (invalid pattern for test: '" + test + "'): " + this.value);
		}
	}
	return true;
}

/**
 * Tells whether there no specified tests or not.
 *
 * @return <code>true</code> if no test was specified (ie. the argument
 * <code>tests</code> was not used while running the scenario), <code>false</code>
 * otherwise
 */
public boolean hasNoTest() {
	return this.tests.size() == 0;
}
/**
 * Returns whether one of the specified test matches the given test.
 *
 * @param stepName The step of the given test
 * @param testName The test name
 * @return <code>true</code> if one of the specified test pattern in the parameter
 * list matches the given test, <code>false</code> otherwise
 */
public boolean match(final String stepName, final String testName) {
	for (String testPattern : this.tests) {
		String[] items = testPattern.split(":|\\.");
		String testStep = "*";
		String test = null;
		switch (items.length) {
			case 1:
				test = items[0];
				break;
			case 2:
				testStep = items[0];
				test = items[1];
				break;
			default:
				throw new ScenarioFailedError("Invalid '" + this.param + "' property content (invalid pattern for test: '" + test + "'): " + this.value);
		}

		if ((test.equals("*") || testName.toLowerCase().contains(test.toLowerCase())) && (testStep.equals("*") || stepName.toLowerCase().contains(testStep.toLowerCase()))) {
			return true;
		}
	}
	return false;
}
}