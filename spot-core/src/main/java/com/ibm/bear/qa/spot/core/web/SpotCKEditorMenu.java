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

import org.openqa.selenium.By;

/**
 * Manage a menu used from a CKE Editor.
 *<p>
 * Following methods are overridden on this menu:
 * <ul>
 * <li>{@link #getItemElementsLocator()}: Returns the locator of the menu item elements.</li>
 * <li>{@link #getItemLocator(String)}: Returns the locator for the given item.</li>
 * </ul>
 * </p>
 */
public class SpotCKEditorMenu extends WebMenu {

public SpotCKEditorMenu(final WebPage page) {
	super(page, By.xpath("//iframe[contains(@class,'cke_panel_frame')]"), false, true);
}

@Override
protected By getItemElementsLocator() {
	return By.xpath("//a[contains(@class,'cke_menubutton')]");
}

@Override
protected By getItemLocator(final String itemLabel) {
	return By.xpath("//a[.//span[@class='cke_menubutton_label' and text()='"+itemLabel+"']]");
}
}
