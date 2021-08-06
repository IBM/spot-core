/*********************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.samples.tqa.steps;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaTestTextBoxContainer;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaAbstractPage.Group;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;
import com.ibm.bear.qa.spot.samples.tqa.scenario.SampleToolsQaScenarioStep;
import com.ibm.bear.qa.spot.samples.tqa.scenario.SampleToolsQaScenarioStepRunner;

/**
 * This step tests the <b>Text Box</b> elements in <b>Elements/<b> group of <b>ToolsQA</b> site.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #test01_FullName()}: Test <b>Full Name</b> simple input field.</li>
 * <li>{@link #test02_Email()}: Test <b>Email</b> simple input field.</li>
 * <li>{@link #test03_CurrentAddress()}: Test <b>Current Address</b> textarea field.</li>
 * </ul>
 * </p>
 */
@RunWith(SampleToolsQaScenarioStepRunner.class)
public class SampleToolsQaStep01_TextBox extends SampleToolsQaScenarioStep {

/**
 * Test <b>Full Name</b> simple input field.
 */
@Test
public void test01_FullName() {

	// Open test page on Elements group
	ToolsQaTestPage testPage = getScenarioOperation().openTestPage(Group.Elements);

	// Select Text Box
	ToolsQaTestTextBoxContainer textBoxTest = testPage.getTextBoxTest();

	// Test Full Name
	String expectedFullName = getData().getFullName();
	textBoxTest.setFullName(expectedFullName);
	String enteredFullName = textBoxTest.getFullName();
	if (!enteredFullName.equals(expectedFullName)) {
		throw new ScenarioFailedError("Unexpected full name for text box: "+enteredFullName+" (expecting '"+expectedFullName+"')");
	}
}

/**
 * Test <b>Email</b> simple input field.
 * <p>
 * Difference with previous input field is that there's a non empty placeholder value.
 * </p>
 */
@Test
public void test02_Email() {

	// Open test page on Elements group
	ToolsQaTestPage testPage = getScenarioOperation().openTestPage(Group.Elements);

	// Select Text Box
	ToolsQaTestTextBoxContainer textBoxTest = testPage.getTextBoxTest();

	// Test Email
	String expectedEmail = getData().getEmail();
	textBoxTest.setEmail(expectedEmail);
	String enteredEmail = textBoxTest.getEmail();
	if (!enteredEmail.equals(expectedEmail)) {
		throw new ScenarioFailedError("Unexpected email for text box: "+enteredEmail+" (expecting '"+expectedEmail+"')");
	}
}

/**
 * Test <b>Current Address</b> textarea field.
 */
@Test
public void test03_CurrentAddress() {

	// Open test page on Elements group
	ToolsQaTestPage testPage = getScenarioOperation().openTestPage(Group.Elements);

	// Select Text Box
	ToolsQaTestTextBoxContainer textBoxTest = testPage.getTextBoxTest();

	// Test Current Address
	String expectedAddress = getData().getCurrentAddress();
	textBoxTest.setCurrentAddress(expectedAddress);
	String enteredAddress = textBoxTest.getCurrentAddress();
	if (!enteredAddress.equals(expectedAddress)) {
		throw new ScenarioFailedError("Unexpected current address for text box: "+enteredAddress+" (expecting '"+expectedAddress+"')");
	}
}
}
