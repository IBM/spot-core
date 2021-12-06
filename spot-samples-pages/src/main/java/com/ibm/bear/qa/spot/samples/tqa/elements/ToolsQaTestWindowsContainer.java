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

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;

/**
 * Abstract class to manage common behavior for any container of <b>Alerts, Frame & Windows</b>
 * section of ToolsQA test page.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #createInstance(ToolsQaTestPage,WindowsItem)}: Create a container instance belonging to the given page matching the given item.</li>
 * </ul>
 * </p>
 */
public abstract class ToolsQaTestWindowsContainer extends ToolsQaTestContainer {

	/**
	 * Items available for <b>Alerts, Frame & Windows</b> tests section.
	 */
	public enum WindowsItem {
		BrowserWindows("Browser Windows", null),
		Alerts("Alerts", null),
		Frames("Frames", null),
		NestedFrames("Nested Frames", null),
		ModalDialogs("Modal Dialogs", ToolsQaTestDialogsContainer.class);
		final String label;
		final Class<? extends ToolsQaTestWindowsContainer> containerClass;
		WindowsItem(final String label, final Class<? extends ToolsQaTestWindowsContainer> clazz) {
			this.label = label;
			this.containerClass = clazz;
		}
		public String getLabel() {
			return this.label;
		}
	}

/**
 * Create a container instance belonging to the given page matching the given item.
 *
 * @param page The page which owns the container created instance
 * @param item
 * @return The created specific container instance
 */
public static ToolsQaTestWindowsContainer createInstance(final ToolsQaTestPage page, final WindowsItem item) {
	return (ToolsQaTestWindowsContainer) createInstance(page, item.containerClass, item.getLabel());
}

ToolsQaTestWindowsContainer(final ToolsQaTestPage page, final By locator) {
	super(page, locator);
}
}
