/*
* Licensed Materials - Property of IBM
* 5725-B69 5655-Y17 5655-Y31 5724-X98 5724-Y15 5655-V82
* Copyright IBM Corp. 1987, 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/
package com.ibm.bear.qa.spot.template.steps;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.utils.FailureBlocker;
import com.ibm.bear.qa.spot.test.gen.pages.TemplateHomePage;
import com.ibm.bear.qa.spot.template.scenario.TemplateScenarioStep;
import com.ibm.bear.qa.spot.template.scenario.TemplateScenarioStepRunner;

/**
 * First step of the %title% scenario.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #test01_OpenHomePage()}: Open the application home page.</li>
 * <li>{@link #test02_ToBeContinued()}: Continue the implementation of the scenario tests....</li>
 * </ul>
 * </p>
 */
@RunWith(TemplateScenarioStepRunner.class)
public class TemplateStep01 extends TemplateScenarioStep {

/**
 * Open the application home page.
 */
@Test
// Following annotation tells framework that if test fails then scenario execution must be stopped
// Comment or remove it if this is not the expected behavior
@FailureBlocker
public void test01_OpenHomePage() {

	// Open the application Home page
	TemplateHomePage homePage = getScenarioOperation().openHomePage();

	// Check opened page title
	String documentTitle = homePage.getDocumentTitle();
	String expectedTitle = getData().getExpectedDocumentTitle();
	if (documentTitle.equals(expectedTitle)) {
		throw new ScenarioFailedError("Unexpected document title '"+documentTitle+"', expecting '"+expectedTitle+"'!");
	}
}

/**
 * Continue the implementation of the scenario tests....
 */
@Test
public void test02_ToBeContinued() {
}
}
