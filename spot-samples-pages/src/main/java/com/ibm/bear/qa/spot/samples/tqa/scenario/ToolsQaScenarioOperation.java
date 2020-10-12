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
package com.ibm.bear.qa.spot.samples.tqa.scenario;

import static com.ibm.bear.qa.spot.core.web.WebPage.openPage;

import com.ibm.bear.qa.spot.core.scenario.ScenarioOperation;
import com.ibm.bear.qa.spot.core.scenario.ScenarioStep;
import com.ibm.bear.qa.spot.samples.tqa.pages.*;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaAbstractPage.Group;
import com.ibm.bear.qa.spot.samples.tqa.topology.ToolsQaApplication;
import com.ibm.bear.qa.spot.samples.tqa.topology.ToolsQaTopology;

/**
 * Class to manage operations performed during a sample scenario execution.
 * <p>
 * Basically, an operation is a sequence of web pages API calls.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #openHomePage()}: Open the <b>ToolsQA</b> Home page.</li>
 * <li>{@link #openTestPage(Group)}: Open a <b>ToolsQA</b> test page on given group.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getCurrentPage()}: Return page currently displayed in browser.</li>
 * <li>{@link #getSamplesToolsQaApplication()}: Return the BA Studio application.</li>
 * <li>{@link #getTopology()}: Return the ToolsQA topology.</li>
 * </ul>
 * </p>
 */
public class ToolsQaScenarioOperation extends ScenarioOperation {

public ToolsQaScenarioOperation(final ScenarioStep step) {
	super(step);
}

/**
 * Return the BA Studio application.
 *
 * @return The application
 */
protected ToolsQaApplication getSamplesToolsQaApplication() {
	return getTopology().getToolsQaApplication();
}

/**
 * Return page currently displayed in browser.
 *
 * @return The page instance of <code>null</code> if browser hasn't been opened yet
 */
protected ToolsQaAbstractPage getCurrentPage() {
	return (ToolsQaAbstractPage) getCurrentPage(null);
}

/**
 * Return the ToolsQA topology.
 *
 * @return The topology as a {@link ToolsQaTopology}
 */
protected ToolsQaTopology getTopology() {
	return (ToolsQaTopology) getStep().getTopology();
}

/**
 * Open the <b>ToolsQA</b> Home page.
 *
 * @return The opened page instance
 */
public ToolsQaHomePage openHomePage() {

	// Check whether the previous page matches the given one
	ToolsQaAbstractPage currentPage = getCurrentPage();

	// Jump directly to the page if initial opening
	if (currentPage == null) {
		return openPage(getSamplesToolsQaApplication().getLocation(), getConfig(), null, ToolsQaHomePage.class);
	}

	// Return home page
	return currentPage.gotoHomePage();
}

/**
 * Open a <b>ToolsQA</b> test page on given group.
 *
 * @param group The group on which test page has to be opened
 * @return The opened page instance
 */
public ToolsQaTestPage openTestPage(final Group group) {

	// Check whether the previous page matches the given one
	ToolsQaAbstractPage currentPage = getCurrentPage();

	// Check whether the test page is already opened or not
	ToolsQaTestPage testPage;
	if (currentPage instanceof ToolsQaTestPage) {
		testPage = (ToolsQaTestPage) currentPage;
		testPage.select(group);
	} else {
		ToolsQaHomePage homePage = openHomePage();
		testPage = homePage.openTestPage(group);
	}

	// Return test page
	return testPage;
}
}
