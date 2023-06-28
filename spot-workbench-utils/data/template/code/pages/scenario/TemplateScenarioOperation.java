/*********************************************************************
* Copyright (c) 2020, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.template.scenario;

import static com.ibm.bear.qa.spot.core.web.WebPage.openPage;

import com.ibm.bear.qa.spot.core.scenario.ScenarioOperation;
import com.ibm.bear.qa.spot.core.scenario.ScenarioStep;
import com.ibm.bear.qa.spot.template.pages.TemplateHomePage;
import com.ibm.bear.qa.spot.template.topology.TemplateApplication;
import com.ibm.bear.qa.spot.template.topology.TemplateTopology;

/**
 * Class to manage operations performed during a <b>%title%</b> scenario execution.
 * <p>
 * Basically, an operation is a sequence of web pages API calls.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #openHomePage()}: Open the <b>%title%</b> Home page.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getCurrentPage()}: Return page currently displayed in browser.</li>
 * <li>{@link #getTemplateApplication()}: Return the <b>%title%</b> application.</li>
 * <li>{@link #getTopology()}: Return the <b>%title%</b> topology.</li>
 * </ul>
 * </p>
 */
public class TemplateScenarioOperation extends ScenarioOperation {

public TemplateScenarioOperation(final ScenarioStep step) {
	super(step);
}

/**
 * Return the <b>%title%</b> application.
 *
 * @return The application
 */
protected TemplateApplication getTemplateApplication() {
	return getTopology().getTemplateApplication();
}

/**
 * Return page currently displayed in browser.
 *
 * @return The page instance of <code>null</code> if browser hasn't been opened yet
 */
protected TemplateHomePage getCurrentPage() {
	return (TemplateHomePage) getCurrentPage(null);
}

/**
 * Return the <b>%title%</b> topology.
 *
 * @return The topology as a {@link TemplateTopology}
 */
protected TemplateTopology getTopology() {
	return (TemplateTopology) getStep().getTopology();
}

/**
 * Open the <b>%title%</b> Home page.
 *
 * @return The opened page instance
 */
public TemplateHomePage openHomePage() {

	// Check whether the previous page matches the given one
	TemplateHomePage currentPage = getCurrentPage();

	// Jump directly to the page if initial opening
	if (currentPage == null) {
		return openPage(getTemplateApplication().getLocation(), getConfig(), null, TemplateHomePage.class);
	}

	// Return home page
	return currentPage;
}
}
