/*********************************************************************
* Copyright (c) 2012, 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.web;

import java.util.List;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioImplementationError;

/**
 * Class to manage menus relying on items list for accessing elements.
 * <p>
 * Default implementation is to have items list as <b>direct</b> child
 * of the menu web element. One can override this default behavior
 * by creating sub-classes which would override the {@link #getItemElements()}
 * method to implement of more specific way to get this items list.
 * </p><p>
 * Public methods overridden on this element are:
 * <ul>
 * <li>{@link #getItemElements()}: Returns the list of item elements of the current menu.</li>
 * </ul>
 * </p><p>
 * Internal methods overridden on this element are:
 * <ul>
 * <li>{@link #getItemElementsLocator()}: Returns the locator of the menu item elements.</li>
 * <li>{@link #getItemLocator(String)}: Returns the locator for the given item.</li>
 * </ul>
 * </p>
 */
public class SpotItemsListMenu extends WebMenu {

public SpotItemsListMenu(final WebElementWrapper parent, final By locator) {
	super(parent, locator);
}

public SpotItemsListMenu(final WebPage page, final By locator) {
	super(page, locator);
}

@Override
public List<WebBrowserElement> getItemElements() {
	return this.element.getChildren();
}

@Override
protected By getItemElementsLocator() {
	throw new ScenarioImplementationError("This method should never be reached.");
}

/**
 * {@inheritDoc}
 * <p>
 * Return null in order to find a menu item using {@link #getItemElements()} method instead.
 * </p>
 */
@Override
protected By getItemLocator(final String itemLabel) {
	return null;
}
}
