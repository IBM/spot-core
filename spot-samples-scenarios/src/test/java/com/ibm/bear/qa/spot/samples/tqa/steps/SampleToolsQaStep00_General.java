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
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaHomePage;
import com.ibm.bear.qa.spot.samples.tqa.scenario.SampleToolsQaScenarioStep;
import com.ibm.bear.qa.spot.samples.tqa.scenario.SampleToolsQaScenarioStepRunner;

/**
 * This step tests general behavior of<b>ToolsQA</b> site.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #test01_CheckDocumentTitle()}: Test <b>Full Name</b> simple input field.</li>
 * </ul>
 * </p>
 */
@RunWith(SampleToolsQaScenarioStepRunner.class)
public class SampleToolsQaStep00_General extends SampleToolsQaScenarioStep {

	// Constants
	private static final String HOME_PAGE_DOCUMENT_TITLE = "ToolsQA";

/**
 * Test <b>Full Name</b> simple input field.
 */
@Test
public void test01_CheckDocumentTitle() {

	// Open Tools QA home page
	ToolsQaHomePage homePage = getScenarioOperation().openHomePage();

	// Check page document title
	String pageDocumentTitle = homePage.getDocumentTitle();
	if (!pageDocumentTitle.equals(HOME_PAGE_DOCUMENT_TITLE)) {
		throw new ScenarioFailedError("Unexpected Home page document title: "+pageDocumentTitle+" (expecting '"+HOME_PAGE_DOCUMENT_TITLE+"')");
	}

	// Check page title
	String pageTitle = homePage.getTitle();
	if (!pageTitle.equals(pageDocumentTitle)) {
		throw new ScenarioFailedError("Unexpected Home page title: "+pageTitle+" (expecting '"+pageDocumentTitle+"')");
	}
}
}
