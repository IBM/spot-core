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

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.samples.tqa.pages.ToolsQaTestPage;

/**
 * Abstract class to manage common behavior for any container of <b>Elements</b>
 * section of ToolsQA test page.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #createInstance(ToolsQaTestPage,ElementsItem)}: Create a container instance belonging to the given page matching the given item.</li>
 * </ul>
 * </p>
 */
public abstract class ToolsQaTestElementsContainer extends ToolsQaTestContainer {

	/**
	 * Items available for <b>Elements</b> tests section.
	 */
	public enum ElementsItem {
		TextBox("Text Box", ToolsQaTestTextBoxContainer.class),
		CheckBox("Check Box", null),
		RadioButton("Radio Button", null),
		WebTables("Web Tables", null),
		Buttons("Buttons", null),
		Links("Links", null),
		Images("Broken Links - Images", null),
		Download("Upload and Download", null),
		DynamicProperties("Dynamic Properties", null);
		final String label;
		final Class<? extends ToolsQaTestElementsContainer> containerClass;
		ElementsItem(final String label, final Class<? extends ToolsQaTestElementsContainer> clazz) {
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
public static ToolsQaTestElementsContainer createInstance(final ToolsQaTestPage page, final ElementsItem item) {
	return (ToolsQaTestElementsContainer) createInstance(page, item.containerClass, item.getLabel());
}

ToolsQaTestElementsContainer(final ToolsQaTestPage page, final By locator) {
	super(page, locator);
}
}
