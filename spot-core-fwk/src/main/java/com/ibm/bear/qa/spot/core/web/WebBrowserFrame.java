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

import org.openqa.selenium.WebDriver;

/**
 * Abstract class for frame used in browser page.
 * <p>
 * {@link WebDriver} API allow to select a browser frame either using an index,
 * a name or a web element. This abstract class allow user to switch to a frame
 * without having to know how to it's accessible through the driver (see {@link #switchTo()}).
 * </p><p>
 * It also allow to get the web element, the index or the name of the current frame.
 * </p>
 */
abstract public class WebBrowserFrame {

	/**
	 * The browser associated with the page.
	 * <p>
	 * It's necessary to provide it to children classes in case they want to perform
	 * some specific operation while the dialog is opened.
	 * </p>
	 */
	WebDriver driver;

WebBrowserFrame(final WebBrowser browser) {
    this.driver = browser.driver;
}

/**
 * Return the frame element.
 *
 * @return The frame element or <code>null</code> if the frame is not identified
 * with web element.
 */
WebBrowserElement getElement() {
	return null;
}

/**
 * Return the frame index.
 *
 * @return The frame index or <code>-1</code> if the frame is not identified
 * with an index.
 */
int getIndex() {
	return -1;
}

/**
 * Return the frame name.
 *
 * @return The frame name or <code>null</code> if the frame is not identified
 * with a name.
 */
String getName() {
	return null;
}

/**
 * Return whether the frame is still displayed or not.
 * <p>
 * Selenium does not offer any API to know whether a frame is still displayed or not,
 * hence we assume the frame is displayed by default during the frame instance
 * life cycle. Subclass might want to override this basic default behavior if there's
 * a better way to infer this piece of information.
 * </p>
 * @return Always <code>true</code>
 */
public boolean isDisplayed() {
	return true;
}

/**
 * Select current frame.
 */
abstract void switchTo();
}