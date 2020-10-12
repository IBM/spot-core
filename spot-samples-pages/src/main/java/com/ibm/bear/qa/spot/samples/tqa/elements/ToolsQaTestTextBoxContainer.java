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
package com.ibm.bear.qa.spot.samples.tqa.elements;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintEnteringMethod;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.web.WebBrowserElement;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;

/**
 * Class to manage container behavior of <b>Text Box</b> item in <b>Elements</b> section of ToolsQA
 * test page.
 * <p>
 * This class defines following public API methods of {@link ToolsQaTestTextBoxContainer} interface:
 * <ul>
 * <li>{@link #getCurrentAddress()}: Return the content of the <b>Current Address</b> field.</li>
 * <li>{@link #getEmail()}: Return the content of the <b>Email</b> field.</li>
 * <li>{@link #getFullName()}: Return the content of the <b>Full Name</b> field.</li>
 * <li>{@link #setCurrentAddress(String)}: Set the content of the <b>Current Address</b> field with the given text.</li>
 * <li>{@link #setEmail(String)}: Set the content of the <b>Email</b> field with the given text.</li>
 * <li>{@link #setFullName(String)}: Set the content of the <b>Full Name</b> field with the given text.</li>
 * </ul>
 * </p>
 */
public class ToolsQaTestTextBoxContainer extends ToolsQaTestElementsContainer implements com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaTestTextBoxContainer {

	/* Constants */
	// Locators
	private static final By CONTAINER_LOCATOR = By.className("text-field-container");
	private static final By FULL_NAME_LOCATOR = By.id("userName");
	private static final By EMAIL_LOCATOR = By.id("userEmail");
	private static final By CURRENT_ADDRESS_LOCATOR = By.id("currentAddress");

public ToolsQaTestTextBoxContainer(final ToolsQaTestPage page) {
	super(page, CONTAINER_LOCATOR);
}

@Override
public String getCurrentAddress() {
	debugPrintEnteringMethod();
	WebBrowserElement textElement = waitForElement(CURRENT_ADDRESS_LOCATOR);
	return textElement.getAttributeValue("value");
}

@Override
public String getEmail() {
	debugPrintEnteringMethod();
	WebBrowserElement textElement = waitForElement(EMAIL_LOCATOR);
	return textElement.getAttributeValue("value");
}

@Override
public String getFullName() {
	debugPrintEnteringMethod();
	WebBrowserElement textElement = waitForElement(FULL_NAME_LOCATOR);
	return textElement.getAttributeValue("value");
}

@Override
public void setCurrentAddress(final String text) {
	debugPrintEnteringMethod("text", text);
	typeText(CURRENT_ADDRESS_LOCATOR, text);
}

@Override
public void setEmail(final String email) {
	debugPrintEnteringMethod("email", email);
	typeText(EMAIL_LOCATOR, email);
}

@Override
public void setFullName(final String name) {
	debugPrintEnteringMethod("name", name);
	typeText(FULL_NAME_LOCATOR, name);
}
}
