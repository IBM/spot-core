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
package com.ibm.bear.qa.spot.samples.tqa.menus;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.web.*;

/**
 * Class to manage a ToolsQA page section menu items.
 * <p>
 * This class defines or overrides following methods:
 * <ul>
 * <li>{@link #getItemElementsLocator()}: Returns the locator of the menu item elements.</li>
 * <li>{@link #getItemLocator(String)}: Returns the locator for the given item.</li>
 * <li>{@link #getMenuElement(int)}: Return the menu element.</li>
 * <li>{@link #scrollIfNecessary(WebBrowserElement)}: Scroll the window from give open element height if it's hidden by the fixed</li>
 * </ul>
 * </p>
 */
public class ToolsQaGroupMenu extends WebMenu {

/**
 * Create an menu instance belonging to the given page and wrapping the given
 * web element.
 *
 * @param page The page which owns the menu instance
 * @param element The menu web element in the page
 */
public ToolsQaGroupMenu(final WebPage page, final WebBrowserElement element) {
	super(page, element);
}

@Override
protected By getItemElementsLocator() {
	return By.tagName("li");
}

/**
 * {@inheritDoc}
 * <p>
 * Items are not found using specific locator but using menu web element children
 * </p>
 * @return Always <code>null</code>
 */
@Override
protected By getItemLocator(final String itemLabel) {
	return null;
}

@Override
protected WebBrowserElement getMenuElement(final int seconds) {
	return this.element;
}

@Override
protected void scrollIfNecessary(final WebBrowserElement openElement) {
	openElement.moveToElement();
	this.browser.scrollWindowBy(/*x:*/0, /*y:*/100);
}
}
