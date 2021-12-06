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
package com.ibm.bear.qa.spot.samples.tqa.elements;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintEnteringMethod;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.sleep;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;
import com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaLargeModalDialog;
import com.ibm.bear.qa.spot.samples.tqa.dialogs.ToolsQaModalDialog;
import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;

/**
 * Class to manage container behavior of <b>Modal Dialogs</b> item in <b>Alerts, Frame & Windows</b>
 * section of ToolsQA test page.
 * <p>
 * This class defines following public API methods of {@link ToolsQaTestDialogsContainer} interface:
 * <ul>
 * <li>{@link #openAndCloseSmallDialog()}: Open the Small Modal dialog and close it after 5 seconds.</li>
 * <li>{@link #openLargeModalDialog()}: Open the Large Modal dialog.</li>
 * </ul>
 * </p>
 */
public class ToolsQaTestDialogsContainer extends ToolsQaTestWindowsContainer implements com.ibm.bear.qa.spot.samples.tqa.api.ToolsQaTestDialogsContainer {

	/* Constants */
	// Locators
	private static final By CONTAINER_LOCATOR = By.id("modalWrapper");
	// Small modal dialog text
	private static final String SMALL_MODAL_CONTENT = "This is a small modal. It has very less content";
	// Large modal dialog text
	private static final String LARGE_MODAL_CONTENT = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";

public ToolsQaTestDialogsContainer(final ToolsQaTestPage page) {
	super(page, CONTAINER_LOCATOR);
}

@Override
public void openAndCloseSmallDialog() {
	debugPrintEnteringMethod();

	// Open small modal dialog
	ToolsQaModalDialog dialog = openDialog("Small", SMALL_MODAL_CONTENT);

	// Wait 5 seconds
	sleep(5);

	// Close dialog
	dialog.close();
}

private ToolsQaModalDialog openDialog(final String kind, final String expectedContent) throws ScenarioFailedError {

	// Get dialog opening button
	WebBrowserElement buttonElement = waitForMandatoryDisplayedPageElement(By.id("show"+kind+"Modal"));

	// Open dialog
	ToolsQaModalDialog dialog = new ToolsQaModalDialog(getPage(), kind);
	dialog.open(buttonElement);

	// Check title
	if (!dialog.getTitle().equals(kind+" Modal")) {
		throw new ScenarioFailedError("Unexpected dialog title '"+dialog.getTitle()+"' (expecting '"+kind+" Modal').");
	}

	// Check content
	if (!dialog.getContent().equals(expectedContent)) {
		throw new ScenarioFailedError("Unexpected dialog content '"+dialog.getContent()+"' (expecting '"+expectedContent+"').");
	}

	// Return opened dialog
	return dialog;
}

@Override
public ToolsQaLargeModalDialog openLargeModalDialog() {
	return openDialog("Large", LARGE_MODAL_CONTENT);
}
}
