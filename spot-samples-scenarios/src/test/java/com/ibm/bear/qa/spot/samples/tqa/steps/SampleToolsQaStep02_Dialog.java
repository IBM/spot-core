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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.sleep;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaLargeModalDialog;
import com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaTestDialogsContainer;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaAbstractPage.Group;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;
import com.ibm.bear.qa.spot.samples.tqa.scenario.SampleToolsQaScenarioStep;
import com.ibm.bear.qa.spot.samples.tqa.scenario.SampleToolsQaScenarioStepRunner;

/**
 * This step tests the <b>Modal Dialogs</b> elements in <b>Alerts, Frame & Windows/<b> group of
 * <b>ToolsQA</b> site.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #test01_SmallModal()}: Test <b>Small Modal</b> dialog.</li>
 * <li>{@link #test02_LargeModal()}: Test <b>Large Modal</b> dialog.</li>
 * <li>{@link #test03_LargeModalCancelled()}: Test cancel on <b>Large Modal</b> dialog.</li>
 * </ul>
 * </p>
 */
@RunWith(SampleToolsQaScenarioStepRunner.class)
public class SampleToolsQaStep02_Dialog extends SampleToolsQaScenarioStep {

/**
 * Test <b>Small Modal</b> dialog.
 */
@Test
public void test01_SmallModal() {

	// Open test page on Alerts, Frame & Windows group
	ToolsQaTestPage testPage = getScenarioOperation().openTestPage(Group.Windows);

	// Select Modal Dialogs
	ToolsQaTestDialogsContainer dialogsTest = testPage.getDialogsTest();

	// Test open and close Small dialog
	dialogsTest.openAndCloseSmallDialog();
}

/**
 * Test <b>Large Modal</b> dialog.
 */
@Test
public void test02_LargeModal() {

	// Open test page on Alerts, Frame & Windows group
	ToolsQaTestPage testPage = getScenarioOperation().openTestPage(Group.Windows);

	// Select Modal Dialogs
	ToolsQaTestDialogsContainer dialogsTest = testPage.getDialogsTest();

	// Open Large Modal dialog
	ToolsQaLargeModalDialog largeModalDialog = dialogsTest.openLargeModalDialog();

	// Wait 5 seconds
	sleep(5);

	// Close dialog
	largeModalDialog.close();
}

/**
 * Test cancel on <b>Large Modal</b> dialog.
 */
@Test
public void test03_LargeModalCancelled() {

	// Open test page on Alerts, Frame & Windows group
	ToolsQaTestPage testPage = getScenarioOperation().openTestPage(Group.Windows);

	// Select Modal Dialogs
	ToolsQaTestDialogsContainer dialogsTest = testPage.getDialogsTest();

	// Open Large Modal dialog
	ToolsQaLargeModalDialog largeModalDialog = dialogsTest.openLargeModalDialog();

	// Wait just 1 second
	sleep(1);

	// Close dialog
	largeModalDialog.cancel();
}
}
