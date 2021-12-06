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
package com.ibm.bear.qa.spot.samples.tqa.pages;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintEnteringMethod;

import java.util.List;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;
import com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaTestDialogsContainer;
import com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaTestTextBoxContainer;
import com.ibm.bear.qa.spot.samples.tqa.elements.*;
import com.ibm.bear.qa.spot.samples.tqa.elements.ToolsQaTestElementsContainer.ElementsItem;
import com.ibm.bear.qa.spot.samples.tqa.elements.ToolsQaTestWindowsContainer.WindowsItem;
import com.ibm.bear.qa.spot.samples.tqa.menus.ToolsQaGroupMenu;

/**
 * Class to manage the <b>ToolsQA Test</b> web page.
 * <p>
 * This class defines following API methods:
 * <ul>
 * <li>{@link #getDialogsTest()}: Select <b>Model Dialogs</b> group of <b>Alerts, Frame & Windows</b> group.</li>
 * <li>{@link #getTextBoxTest()}: Select <b>Text Box</b> group of <b>Elements</b> group.</li>
 * <li>{@link #select(Group)}: Select the given group and expand it</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #isLoaded()}: Return whether the page is currently loaded or not.</li>
 * <li>{@link #load()}: Load the current page into the browser.</li>
 * </ul>
 * </p>
 */
public class ToolsQaTestPage extends ToolsQaAbstractPage {

	/* Fields */
	// The container displayed in the page
	protected ToolsQaTestContainer container;

/**
 * Create an instance of the page using the given URL, configuration, user and data.
 * <p>
 * The information passed to the page is the name of the DW Job.
 * </p>
 * @param url The page URL
 * @param config The scenario configuration
 * @param user The user logged to the page
 * @param infos The page information to be stored
 */
public ToolsQaTestPage(final String url, final Config config, final User user, final String... infos) {
	super(url, config, user, infos);
}

/**
 * Select <b>Model Dialogs</b> group of <b>Alerts, Frame & Windows</b> group.
 *
 * @return The API to test group content
 */
public ToolsQaTestDialogsContainer getDialogsTest() {
	debugPrintEnteringMethod();

	// Select group
	ToolsQaGroupMenu menu = openGroupMenu(Group.Windows);
	menu.clickItem(WindowsItem.ModalDialogs.getLabel());

	// Set container
	this.container = ToolsQaTestWindowsContainer.createInstance(this, WindowsItem.ModalDialogs);

	// Return container API
	return (ToolsQaTestDialogsContainer) this.container;
}

private WebBrowserElement getGroupElement(final Group group) {
	return waitForMandatoryDisplayedElement(By.xpath("//div[@class='element-group' and .//div[@class='header-text' and text()='"+group.getLabel()+"']]"));
}

/**
 * Select <b>Text Box</b> group of <b>Elements</b> group.
 *
 * @return The API to test group content
 */
public ToolsQaTestTextBoxContainer getTextBoxTest() {
	debugPrintEnteringMethod();

	// Select group
	ToolsQaGroupMenu menu = openGroupMenu(Group.Elements);
	menu.clickItem(ElementsItem.TextBox.getLabel());

	// Set container
	this.container = ToolsQaTestElementsContainer.createInstance(this, ElementsItem.TextBox);

	// Return container API
	return (ToolsQaTestTextBoxContainer) this.container;
}

@Override
protected boolean isLoaded() {

	// If page is basically loaded, then check more precisely as its content
	// to tell whether the entire page is loaded or not
	if (super.isLoaded()) {
		return waitForContainerElement(false) != null;
	}

	// Page is not loaded yet
	return false;
}

@Override
protected void load() {

	// Perform generic page load
	super.load();

	// Wait for specific page content
	waitForContainerElement(true);
}

private ToolsQaGroupMenu openGroupMenu(final Group group) {

	// Get group elements
	List<WebBrowserElement> groupElements = waitForMandatoryDisplayedElements(By.className("element-group"));

	// Look for given group
	for (WebBrowserElement groupElement: groupElements) {

		// Get text and button elements
		WebBrowserElement textElement = groupElement.waitShortlyForMandatoryDisplayedChildElement(By.className("header-text"));
		WebBrowserElement buttonElement = groupElement.waitShortlyForMandatoryDisplayedChildElement(By.xpath(".//div[@class='icon']"));

		// If group is found, select it and expand it
		if (textElement.getText().contains(group.getLabel())) {
			// Get menu element
			WebBrowserElement menuElement = groupElement.waitShortlyForMandatoryChildElement(By.tagName("ul"));

			// Select group and expand it
			ToolsQaGroupMenu groupMenu = new ToolsQaGroupMenu(this, menuElement);
			groupMenu.open(buttonElement);

			// Return opened menu
			return groupMenu;
		}

		// Close opened groups
		WebBrowserElement listElement = groupElement.waitShortlyForMandatoryChildElement(By.className("element-list"));
		if (listElement.getAttributeClass().contains("show")) {
			buttonElement.click();
		}
	}

	// We should have found the group
	throw new ScenarioFailedError("Cannot find group '"+group+"'.");
}

/**
 * Select the given group and expand it
 *
 * @param group The group to be selected
 */
public void select(final Group group) {
	debugPrintEnteringMethod();
	openGroupMenu(group);
}

private WebBrowserElement waitForContainerElement(final boolean fail) {
	return this.browser.waitForElement(null, By.className("container"), fail, fail ? 1 : timeout(), /*displayed:*/ true, /*single:*/ true);
}
}
