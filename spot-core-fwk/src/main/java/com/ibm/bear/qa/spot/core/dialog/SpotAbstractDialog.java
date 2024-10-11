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
package com.ibm.bear.qa.spot.core.dialog;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;

import com.ibm.bear.qa.spot.core.api.elements.SpotDialog;
import com.ibm.bear.qa.spot.core.scenario.errors.*;
import com.ibm.bear.qa.spot.core.timeout.SpotDisplayedTimeout;
import com.ibm.bear.qa.spot.core.timeout.SpotEnabledTimeout;
import com.ibm.bear.qa.spot.core.web.*;

/**
 * Abstract class for any window opened as a dialog in a browser page.
 * <p>
 * Following functionalities are specialized by the dialog:
 * <ul>
 * <li>{@link #open(WebBrowserElement)}: open the window by clicking on the
 * given web element.</li>
 * <li>{@link #selectDialogFrame()}: Sets the browser frame to the dialog frame.
 * </ul>
* </p><p>
 * Following operations are also specialized for dialogs:
 * <ul>
 * <li>{@link #closeAction(boolean)}: The action to perform to close the window.</li>
 * </ul>
 * </p><p>
 * <b>There's no public API for this class</b>.
 * </p><p>
 * Internal API methods accessible in the framework are:
 * <ul>
 * <li>{@link #cancelAll()}: Close all possible opened dialogs by clicking on Cancel button or equivalent.</li>
 * <li>{@link #isOpened()}: Check whether a dialog is opened or not.</li>
 * <li>{@link #open(WebBrowserElement)}: open the dialog by clicking on the given web element.</li>
 * <li>{@link #opened()}: Get the element on an already opened dialog.</li>
 * </ul>
 * </p><p>
 * Internal API methods accessible from subclasses are:
 * <ul>
 * <li>{@link #clickOnOpenElement(WebBrowserElement, int)}: Click on open element in order to open the dialog.</li>
 * <li>{@link #closeAction(boolean)}: The action to perform to close the dialog.</li>
 * <li>{@link #handleConfirmationPopup()}: Handle possible confirmation popup dialog.</li>
 * <li>{@link #selectDialogFrame()}: Sets the current browser frame to this dialog's frame.</li>
 * </ul>
  * </p>
 */
abstract public class SpotAbstractDialog extends SpotAbstractWindow implements SpotDialog {

	/**
	 *  Tells whether alerts should be checked and purged by when opening the dialog.
	 *  False by default.
	 *  TODO Add a way to change this default value
	 */
	private final boolean purgeAlerts = false;

/**
 * Create a new dialog instance belonging to the given page using given dialog locator.
 *
 * @param page The page which will own the dialog instance
 * @param locator The dialog web element locator
 */
public SpotAbstractDialog(final WebPage page, final By locator) {
	super(page, locator);
}

/**
 * Create a new dialog instance belonging to the given page using given dialog locator.
 *
 * @param page The page which will own the dialog instance
 * @param locator The dialog web element locator
 * @param frame The name of the frame used in the dialog
 */
public SpotAbstractDialog(final WebPage page, final By locator, final String frame) {
	super(page, locator, frame);
}

/**
 * Create a new dialog instance belonging to the given page using given dialog locator.
 *
 * @param page The page which will own the dialog instance
 * @param locator The dialog web element locator
 * @param frame The frame used in the dialog
 */
public SpotAbstractDialog(final WebPage page, final By locator, final WebBrowserFrame frame) {
	super(page, locator, frame);
}

/**
 * Click on open element in order to open the dialog.
 * <p>
 * Default is to click on the given element and put back the frame if it has been
 * reset or changed to be able to reach it.
 * </p>
 * @param openElement The element on which to click to open the dialog.
 * If <code>null</code>, then do nothing as the dialog is supposed to be already
 * opened.
 * @param workaround Count of retry while applying workaround when opening dialog
 */
private void clickOnOpenElement(final WebBrowserElement openElement, final int workaround) {
	if (DEBUG) debugPrintln("		+ Click on '"+openElement+"' to open the dialog");

	// If no element was given then do nothing
	if (openElement == null) {
		if (DEBUG) debugPrintln("		  -> do nothing as no element was given");
		return;
	}

	// Check whether the open element is still available while applying workaround
	if (workaround > 0 && !openElement.isDisplayed(false)) {
		if (DEBUG) debugPrintln("		  -> element is no longer displayed, the dialog might have been already opened, hence give up...");
		return;
	}

	// Select the open element frame if necessary
	WebBrowserFrame openElementFrame = openElement.getFrame();
	try {
		if (openElementFrame != this.frames[1]) {
			resetFrame();
			if (openElementFrame != null) {
				this.browser.selectFrame(openElementFrame);
			}
		}

		// Check whether the open element is still available while applying workaround
		if (workaround > 0 && !openElement.isDisplayed(false)) {
			if (DEBUG) debugPrintln("		  -> element is no longer displayed, the dialog might have been already opened, hence give up...");
			return;
		}

		// Check that the element is enabled (necessary for button)
		if (workaround == 0) {
			SpotEnabledTimeout timeout = new SpotEnabledTimeout(openElement, true);
			timeout.waitUntil(openDialogTimeout());
		}

		// Scroll if necessary
		scrollIfNecessary(openElement);

		// Check whether the open element is still available while applying workaround
		if (workaround > 0 && !openElement.isDisplayed(false)) {
			if (DEBUG) debugPrintln("		  -> element is no longer displayed, the dialog might have been already opened, hence give up...");
			return;
		}

		// Click on the element
		openElement.click();

		// Handle confirmation dialogs that might pop-up
		handleConfirmationPopup();
	}
	finally {
		// Reset frame if necessary
		if (openElementFrame != this.frames[1] && openElementFrame != null) {
			resetFrame();
		}
	}
}

/**
 * {@inheritDoc}
 * <p>
 * Initialize the dialog element in case it was never done before. This is necessary
 * to check that the dialog has finally vanished.
 * </p><p>
 * Such use case might happen when a dialog is opened by another kind of action
 * (e.g. save operation). Then, callers just want to close the dialog in case it has
 * been opened during the action...
 * </p>
 */
@Override
protected void close(final boolean validate) {

	// Initialize the element if it has not been done yet
	if (this.element == null) {
		setElement();
		if (this.element == null) {
			throw new WaitElementTimeoutError("Cannot find any dialog with corresponding locator: "+this.locator);
		}
	}

	// Now we can close safely
	super.close(validate);
}

/**
 * Wait for dialog to be closed before the specified timeout.
 *
 * @param seconds timeout in seconds before which the dialog should close
 */
public final void closedBeforeTimeout(final int seconds) {
	if (isOpenedBeforeTimeout(openDialogTimeout())) {
		waitWhileDisplayed(seconds);
	}
}

/**
 * Close dialog if it is opened before the specified timeout.
 *
 * @param seconds timeout in seconds before which dialog may open
 */
public final void closeIfOpenedBeforeTimeout(final int seconds) {
	if (isOpenedBeforeTimeout(seconds)) {
		opened();
		close();
	}
}

/**
 * {@inheritDoc}
 * <p>
 * A dialog is closed by clicking on the "Close" button
 * </p>
 */
@Override
protected void closeAction(final boolean validate) {
	try {
		clickButton(getCloseButtonLocator(validate), 1);
	}
	catch (SpotNotEnabledError snee) {
		snee.setDialog(this);
		throw snee;
	}
}

/**
 * Handle possible confirmation popup dialog.
 * <p>
 * Method designed to be overridden by subclasses. Sometimes during opening a
 * dialog, another pop-up dialog appears, e.g., to confirm before an action is
 * finished. In order to keep the opening of general dialogs at this class
 * level, need to push the ability to handle miscellaneous pop-ups down to the
 * sub-classes.
 * </p>
 */
protected void handleConfirmationPopup() {
	// do nothing;
}

@Override
public WebBrowserElement open(final WebBrowserElement webElement) {
	debugPrintEnteringMethod("webElement", webElement);

	// Store the link element
	this.openingElement = webElement;

	// Get list of already opened dialog IDs
	List<WebBrowserElement> alreadyOpenedDialogElements = getOpenedElements(0/* sec */);
	if (alreadyOpenedDialogElements.size() > 0) {
		throw new ScenarioFailedError("There are "+alreadyOpenedDialogElements.size()+" dialogs already opened before having clicked on opening element.");
	}

	// Click on element which opens the dialog
	clickOnOpenElement(this.openingElement, 0);
	long startTime = System.currentTimeMillis();

	// Wait for dialog web element
	setElement();

	// Check if dialog has been well opened, in case not, try to apply a workaround
	debugPrintln("		  -> timeout=" + (this.max * openDialogTimeout()) + " seconds");
	if (this.element == null) {
		debugPrintln("Workaround: click on " + this.openingElement + " to open dialog again as previous click didn't work...");
		try {
			debugPrintln("	-> Click a second time on the opening element "+this.openingElement.getLocator());
			clickOnOpenElement(this.openingElement, 1);
		}
		catch (ElementNotInteractableException ecie) {
			debugPrintln("Not interactable exception occurred during the second click:");
			debugPrintException(ecie);
			debugPrintln("Might be due because the dialog finally opened, we'll check that later...");
		}

		// Check if the menu can be found after the second click
		debugPrintln("	-> Try to store the dialog element a second time...");
		setElement();

		// Check whether the dialog has been finally found or not
		if (this.element == null) {
			debugPrintln("	-> Figure out whether a dialog is already opened...");
			WebBrowserElement dialogElement = this.browser.findElement(By.cssSelector("div[role='dialog']"));
			if (dialogElement != null) {
				println();
				println("Workaround applied when trying to open dialog "+getClassSimpleName(getClass())+":");
				printStackTrace(1);
				println();
				println("Although searched opened dialog was not found, an opened dialog has been found using standard css selector \"div[role='dialog']\"...");
				println("Hence, assume this is the correct one which was not found due to an invalid locator: \""+this.locator+"\"");
				println("Continue the scenario execution normally, but the invalid selector must be fixed !!!");
				this.element = dialogElement;
			} else {
				// Dialog is still not found, give up
				throw new WaitElementTimeoutError("Failing to open the dialog " + this);
			}
		} else {
			debugPrintln("	-> Wait to check whether the second click has opened another new dialog or not...");
			debugPrintln("	-> Compute a timeout using the time it took to open the first dialog...");
			int timeout = (int) ((System.currentTimeMillis() - startTime) / 1000) + shortTimeout();
			SpotDisplayedTimeout displayedTimeout = new SpotDisplayedTimeout(this.element, false);
			if (displayedTimeout.waitWhile(timeout)) {
				debugPrintln("	-> The initial dialog becomes stale, hence reset the dialog element...");
				this.element = null;
				debugPrintln("	-> Try to see if a new dialog has replaced it...");
				setElement();
				if (this.element == null) {
					throw new WaitElementTimeoutError("Failing to open the dialog " + this);
				}
			} else {
				// Search again for dialog elements as a second click might have opened two dialogs
				List<WebBrowserElement> openedDialogElements = getOpenedElements(0/* sec */);
				switch (openedDialogElements.size()) {
					case 2:
						// It seems there are 2 dialogs opened after having click a second time on opening button
						// hence close the first one
						debugPrintln("WARNING: " + openedDialogElements.size() + " dialogs have been found after having clicked on open element, keep the last one and close all others");
						WebBrowserElement windowElement = openedDialogElements.get(0);
						By buttonLocator = getCloseButtonLocator(false);
						if (buttonLocator == null) {
							throw new ScenarioFailedError("Several dialogs are opened and they cannot be be cancelled.");
						}
						debugPrintln("	-> close dialog '" + windowElement + "' by clicking on " + buttonLocator + " button.");
						windowElement.findElement(buttonLocator).click();
						this.element = openedDialogElements.get(1);
						debugPrintln("	-> keep dialog '" + this.element + "'.");
						sleep(1);
						break;
					case 1:
						this.element = openedDialogElements.get(0);
						break;
					default:
						throw new ScenarioFailedError("There are "+alreadyOpenedDialogElements.size()+" dialogs already opened before having clicked on opening element.");
				}
			}
		}
	}

	// Wait for extra loading end if necessary
	waitForLoadingEnd();

	// Purge alerts if any
	if (this.purgeAlerts) {
		if (this.browser.purgeAlerts("Open dialog " + this.locator + "from " + this.page) > 0) {
			if (!this.element.isDisplayed(false)) {
				// Workaround
				debugPrintln("Workaround: The dialog was closed while purging alerts, try to open it again...");
				return open(this.openingElement);
			}
		}
	}

	// Return the opened dialog
	return this.element;
}

/**
 * Return the timeout to wait for the dialog to be opened after having clicked on opening element.
 * <p>
 * Default value for this timeout is {@link #shortTimeout()}.
 * </p>
 * @return The timeout
 */
protected int openDialogTimeout() {
	return shortTimeout();
}

/**
 * Get the element on an already opened dialog.
 *
 * @return The dialog web element
 * @throws WaitElementTimeoutError If the dialog is not opened before the dialog
 * timeout (see {@link #openDialogTimeout()})
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
public WebBrowserElement opened() throws WaitElementTimeoutError {
	debugPrintEnteringMethod();
	setElement();
	if (this.element == null) {
		throw new WaitElementTimeoutError("Cannot find any dialog with corresponding locator: "+this.locator);
	}
	waitForLoadingEnd();
	return this.element;
}

/**
 * Get the element on an already opened dialog.
 *
 * @param seconds The number of seconds to wait for the window to be opened.
 * @param fail Tells whether to fail or not if the dialog is not opened after the delay has expired
 * @return The dialog web element or <code>null</code> if the dilaog is not opened
 * and not asked to fail
 * @throws WaitElementTimeoutError If the dialog is not opened before the given timeout
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
public WebBrowserElement openedBeforeTimeout(final int seconds, final boolean fail) throws WaitElementTimeoutError {
	if (isOpenedBeforeTimeout(seconds)) {
		return opened();
	}
	if (fail) {
		throw new WaitElementTimeoutError("Cannot find any dialog with corresponding xpath: "+this.locator);
	}
	return null;
}

/**
 * Sets the current browser frame to this dialog's frame.
 */
protected void selectDialogFrame() {
	this.browser.selectFrame(getFrame());
}

private void setElement() {

	// Get list of opened dialog elements
	List<WebBrowserElement> openedDialogElements = getOpenedElements(openDialogTimeout());

	// Go through the map to see if there's any doubled opened dialog
	final int size = openedDialogElements.size();
	switch (size) {
		case 1:
			// We got it, store in window and leave the loop
			this.element = openedDialogElements.get(0);
			break;
		case 0:
			// No opened dialog was found
			debugPrintln("WARNING: No opened dialog was found after having clicked on open element.");
			break;
		default:
			throw new ScenarioFailedError(size+" dialogs are opened at the same time.");
	}
}

/**
 * Wait for the window content to be loaded.
 * <p>
 * Default is to do nothing.
 * </p>
 */
protected void waitForLoadingEnd() {
	// Do nothing
}

/**
 * {@inheritDoc}
 * <p>
 * Overridden to set the element in case it's <code>null</code>
 * </p>
 */
@Override
protected void waitWhileDisplayed(final int seconds) throws WaitElementTimeoutError {
	if (this.element == null) {
		setElement();
		if (this.element == null) {
			throw new WaitElementTimeoutError("Cannot find any dialog with corresponding locator: "+this.locator);
		}
	}
	super.waitWhileDisplayed(seconds);
}
}
