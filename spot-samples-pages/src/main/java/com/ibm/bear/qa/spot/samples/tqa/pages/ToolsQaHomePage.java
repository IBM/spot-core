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
package com.ibm.bear.qa.spot.samples.tqa.pages;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintEnteringMethod;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * Class to manage the <b>ToolsQA Home</b> web page.
 * <p>
 * This class defines following API methods:
 * <ul>
 * <li>{@link #gotoHomePage()}: Go to <b>ToolsQA</b> Home page by clicking on the header link.</li>
 * <li>{@link #openTestPage(Group)}: Open page of given group.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #isLoaded()}: Return whether the page is currently loaded or not.</li>
 * <li>{@link #load()}: Load the current page into the browser.</li>
 * </ul>
 * </p>
 */
public class ToolsQaHomePage extends ToolsQaAbstractPage {

/**
 * Create an instance of the page using the given URL, configuration and user.
 *
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 */
public ToolsQaHomePage(final String url, final Config config, final User user) {
	super(url, config, user);
}

/**
 * {@inheritDoc}
 *<p>
 * Do nothing as we're already on Home page...
 * </p>
 */
@Override
public ToolsQaHomePage gotoHomePage() {
	return this;
}

@Override
protected boolean isLoaded() {

	// If page is basically loaded, then check more precisely to its content
	// to tell whether the entire page is actually loaded or not
	if (super.isLoaded()) {
		WebBrowserElement bodyElement = waitForBodyElement(false);
		return bodyElement != null;
	}

	// Page is not loaded yet
	return false;
}

@Override
protected void load() {

	// Perform generic page load
	super.load();

	// Wait for specific page content
	waitForBodyElement(true);
}

/**
 * Open page of given group.
 *
 * @param group The group to open page on
 * @return The home group page
 */
public ToolsQaTestPage openTestPage(final Group group) {
	debugPrintEnteringMethod("group", group);

	// Get card link element
	WebBrowserElement linkElement = waitForMandatoryDisplayedElement(By.xpath("//h5[text()='"+group.getLabel()+"']")).getParent();

	// Open new page using link
	return openPageUsingLink(linkElement, ToolsQaTestPage.class, group.toString());
}

private WebBrowserElement waitForBodyElement(final boolean fail) {
	return this.browser.waitForElement(null, By.className("home-body"), fail, fail ? 1 : timeout(), /*displayed:*/ true, /*single:*/ true);
}
}
