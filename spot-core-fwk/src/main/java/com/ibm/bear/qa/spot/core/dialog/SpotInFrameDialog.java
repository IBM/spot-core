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
package com.ibm.bear.qa.spot.core.dialog;

import java.util.List;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.web.*;

/**
 * Abstract class for any dialog opened in a frame.
 * <p>
 * Basically all superclass method which needs to access a web element are
 * overridden to select the browser frame before performing the corresponding action.
 */
abstract public class SpotInFrameDialog extends SpotAbstractDialog {

public SpotInFrameDialog(final WebPage page, final By findBy, final WebBrowserFrame frame) {
	super(page, findBy, frame);
}

/**
 * {@inheritDoc}
 * <p>
 * Select the frame before clicking on the button.
 * </p>
 */
@Override
protected WebBrowserElement clickButton(final By buttonLocator) {
	selectFrame();
	return super.clickButton(buttonLocator);
}

/**
 * {@inheritDoc}
 * <p>
 * Select the frame before performing the close action.
 * </p>
 */
@Override
protected void closeAction(final boolean cancel) {
	selectFrame();
	super.closeAction(cancel);
}

/**
 * {@inheritDoc}
 * <p>
 * Select the frame before looking for the element.
 * </p>
 */
@Override
public WebBrowserElement findElement(final By by, final boolean recovery) {
	selectFrame();
	return super.findElement(by, recovery);
}

/**
 * {@inheritDoc}
 * <p>
 * Select the frame before looking for the element.
 * </p>
 */
@Override
protected WebBrowserElement findElement(final String xpath, final boolean recovery) {
	selectFrame();
	return findElement(By.xpath(xpath), recovery);
}

/**
 * {@inheritDoc}
 * <p>
 * Select the frame before looking for opened dialogs.
 * </p>
 */
@Override
protected List<WebBrowserElement> getOpenedElements(final int seconds) {
	selectFrame();
	return super.getOpenedElements(seconds);
}

/**
 * {@inheritDoc}
 * <p>
 * Select the frame before opening the dialog.
 * </p>
 */
@Override
public WebBrowserElement open(final WebBrowserElement openElement) {
	selectFrame();
	return super.open(openElement);
}

/**
 * {@inheritDoc}
 * <p>
 * Select the frame before waiting for the element.
 * </p>
 * @throws ScenarioFailedError If the frame argument is true as the frame is
 * already known.
 * @deprecated Since waitFor*Element methods renaming
 */
@Deprecated
@Override
protected WebBrowserElement waitForMandatoryElement(final By elemLocator, final int timeout, final boolean frame) {
	if (frame) {
		throw new ScenarioFailedError("Should not find element in frames in framed dialog.");
	}

	// Select frame
	selectFrame();

	// Wait for the element
	return super.waitForMandatoryElement(elemLocator, timeout, frame);
}

}
