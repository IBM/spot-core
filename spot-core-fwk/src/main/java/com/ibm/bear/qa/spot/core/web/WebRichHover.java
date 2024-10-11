/*********************************************************************
* Copyright (c) 2012, 2024 IBM Corporation and others.
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
import org.openqa.selenium.Keys;

import com.ibm.bear.qa.spot.core.api.elements.SpotRichHover;

/**
 * Abstract class for any window opened as a rich hover in a browser page.
 * <p>
 * Public API for this class is defined in {@link SpotRichHover} interface.
 * </p><p>
 * Internal API methods accessible from subclasses are:
 * <ul>
 * <li>{@link #closeAction(boolean)}: The action to perform to close the window.</li>
 * <li>{@link #getCloseButtonLocator(boolean)}: The button to close the hover.</li>
 * <li>{@link #getImageSrcAttribute()}: Return the 'src' attribute of the hover image.</li>
 * <li>{@link #getTitleLinkXpath()}: Return the xpath for the the title link element.</li>
 * </ul>
 * </p>
 */
abstract public class WebRichHover<P extends WebPage> extends WebLinkHover<P> implements SpotRichHover {

public WebRichHover(final WebPage page, final By locator) {
	super(page, locator);
}

/**
 * The action to perform to close the window.
 * <p>
 * A dialog is closed by clicking on the "Close" button (see {@link #getCloseButtonLocator(boolean)})
 * </p>
 */
@Override
protected void closeAction(final boolean cancel) {

	// Get the close link element
	resetFrame();
	WebBrowserElement closeLinkElement = this.browser.findElement(getCloseButtonLocator(cancel), false/*no recovery*/);

	// Click on the link element
	if (closeLinkElement != null && closeLinkElement.isDisplayed(false)) {
		closeLinkElement.click(/*recovery:*/false);
	} else {
		this.openingElement.sendKeys(false/*recovery*/, Keys.ESCAPE);
	}
}

@Override
protected By getCloseButtonLocator(final boolean validate) {
//	return "//a[@dojoattachpoint='_closeButton']";
	return By.cssSelector("a[dojoattachpoint='_closeButton']");
}

/**
 * Return the 'src' attribute of the hover image.
 * <p>
 * By default the hover is supposed to have no image, hence this method returns
 * <code>null</code> at this top hierarchy level.
 * </p>
 * @return The attribute value or <code>null</code> if the hover has no image
 */
protected String getImageSrcAttribute() {
	return null;
}

/**
 * Return the xpath for the the title link element.
 *
 * @return The xpath as a {@link String}.
 */
@Override
protected String getTitleLinkXpath() {
	return ".//a[@dojoattachpoint='titleLink']";
}

/**
 * Returns true if there is an image in the rich hover.
 * <p>
 * By default a rich hover is not supposed to have an image. If a subclass wants
 * to override this behavior, then it has to override the {@link #getImageSrcAttribute()}
 * method.
 * </p>
 * @return <code>true</code> if an image exists, <code>false</code> otherwise
 */
@Override
public boolean hasImage() {
	String srcAttribute = getImageSrcAttribute();
	return srcAttribute != null && !srcAttribute.isEmpty();
}
}
