/*********************************************************************
* Copyright (c) 2012, 2023 IBM Corporation and others.
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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.DEBUG;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

import java.util.ArrayList;
import java.util.List;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to manage browser frame embedded in another frame.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getDisplayedParent()}: Switch to parent frame.</li>
 * <li>{@link #getParentFrame()}: Get the parent frame.</li>
 * <li>{@link #isDisplayed()}: Return whether the frame is still displayed or not.</li>
 * <li>{@link #switchToParent()}: Switch to parent frame.</li>
 * </ul>
 * </p>
 */
public class WebEmbeddedFrame extends WebElementFrame {

	private WebElementFrame parentFrame;

public WebEmbeddedFrame(final WebBrowser browser, final WebBrowserFrame frame, final WebBrowserElement element) {
	super(browser, element);
	WebBrowserFrame browserFrame = frame;
	if (browserFrame == null) {
		throw new ScenarioFailedError("An embedded frame must have a parent.");
	}
	if (browserFrame instanceof WebElementFrame) {
		this.parentFrame = (WebElementFrame) browserFrame;
	} else {
		throw new ScenarioFailedError("Invalid class for parent frame: "+browserFrame.getClass());
	}
}

/**
 * Switch to parent frame.
 * <p>
 * Note that if parent frame is no longer displayed, this method safely walk through
 * frames hierarchy until it finds a displayed frame. If all frames are no longer displayed
 * in the hierarchy, then no frame will be selected at the end of the method operation.
 * </p>
 * @return The selected parent frame as a {@link WebElementFrame} or <code>null</code>
 * if no valid frame was found in the frame parents hierarchy
 */
public WebElementFrame getDisplayedParent() {
	if (DEBUG) debugPrintln("		+ Switch to "+this);

	// Reset frame first
	this.driver.switchTo().defaultContent();

	// Get parent frames hierarchy
	List<WebElementFrame> framesHierarchy = getFramesHierarchy();

	// Switch to first valid parent frame
	WebElementFrame displayedFrame = null;
	for (WebElementFrame frame: framesHierarchy) {
		if (frame.getElement().isDisplayed(false)) {
			displayedFrame = frame;
		} else {
			return displayedFrame;
		}
	}

	// No valid frame was found in hierarchy
	return null;
}

private List<WebElementFrame> getFramesHierarchy() {
	WebElementFrame rootParent = this.parentFrame;
	List<WebElementFrame> framesHierarchy = new ArrayList<>();
	framesHierarchy.add(rootParent);
	while (rootParent instanceof WebEmbeddedFrame) {
		rootParent = ((WebEmbeddedFrame) this.parentFrame).parentFrame;
		if (rootParent != null) {
			framesHierarchy.add(0, rootParent);
		}
	}
	return framesHierarchy;
}

/**
 * Get the parent frame.
 *
 * @return The parent frame
 */
public WebElementFrame getParentFrame() {
	return this.parentFrame;
}

@Override
public boolean isDisplayed() {
	List<WebElementFrame> framesHierarchy = getFramesHierarchy();
	for (WebElementFrame frame: framesHierarchy) {
		if (!frame.getElement().isDisplayed(false)) {
			return false;
		}
	}
	return super.isDisplayed();
}

@Override
void switchTo() {
	switchToParent();
	this.driver.switchTo().frame(getElement().getWebElement());
}

/**
 * Switch to parent frame.
 *
 * @return The selected parent frame as a {@link WebElementFrame}.
 */
public WebElementFrame switchToParent() {
	if (DEBUG) debugPrintln("		+ Switch to "+this);
	this.parentFrame.switchTo();
	return this.parentFrame;
}

}
