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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.ibm.bear.qa.spot.core.api.elements.SpotDialog;
import com.ibm.bear.qa.spot.core.scenario.errors.*;
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

	// Check whether the open element is still avaialble while applying workaround
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

		// Check whether the open element is still avaialble while applying workaround
		if (workaround > 0 && !openElement.isDisplayed(false)) {
			if (DEBUG) debugPrintln("		  -> element is no longer displayed, the dialog might have been already opened, hence give up...");
			return;
		}

		// Check that the element is enabled (necessary for button)
		if (workaround == 0) {
			SpotEnabledTimeout timeout = new SpotEnabledTimeout(openElement, true);
			timeout.waitUntil(shortTimeout());
		}

		// Check whether the open element is still avaialble while applying workaround
		if (workaround > 0 && !openElement.isDisplayed(false)) {
			if (DEBUG) debugPrintln("		  -> element is no longer displayed, the dialog might have been already opened, hence give up...");
			return;
		}

		// Scroll if necessary
		scrollIfNecessary(openElement);

		// Check whether the open element is still avaialble while applying workaround
		if (workaround > 0 && !openElement.isDisplayed(false)) {
			if (DEBUG) debugPrintln("		  -> element is no longer displayed, the dialog might have been already opened, hence give up...");
			return;
		}

		// Click on the element
		openElement.click();

		// Handle confirmation dialogs that might pop-up. New since 5.0.2, only implemented where required.
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
		setElement(null);
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
	if (isOpenedBeforeTimeout(shortTimeout())) {
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

private List<String> getAlreadyOpenedDialogIDs() {

	// Get list of opened dialog elements
   	List<WebBrowserElement> dialogElements = getOpenedElements(0/*sec*/);

   	// Return the IDs list
   	List<String> dialogIDs = new ArrayList<String>(dialogElements.size());
   	Iterator<WebBrowserElement> iterator = dialogElements.iterator();
   	while (iterator.hasNext()) {
   		dialogIDs.add(iterator.next().getAttribute("id"));
   	}
   	return dialogIDs;
}

/**
 * Handle possible confirmation popup dialog.
 * <p>
 * Method designed to be overridden by subclasses. Sometimes during opening a
 * dialog, another pop-up dialog appears, e.g., to confirm before an action is
 * finished. In order to keep the opening of general dialogs at this class
 * level, need to push the ability to handle miscellaneous pop-ups down to the
 * sub-classes. Those sub-class implementations should be limited to specific
 * cases, e.g., a confirmation dialog that appears (as of 5.0.2) when adding
 * test cases to a test suite.
 * </p>
 * @since 5.0.2
 */
protected void handleConfirmationPopup() {
	// do nothing;
}

@Override
public WebBrowserElement open(final WebBrowserElement webElement) {
	if (DEBUG) debugPrintln("		+ Open "+getClassSimpleName(getClass())+" dialog");

	// Store the link element
	this.openingElement = webElement;

	// Get list of already opened dialog IDs
   	List<String> alreadyOpenedDialogIDs = getAlreadyOpenedDialogIDs();

	// Click on element which opens the dialog
	clickOnOpenElement(this.openingElement, 0);

	// Wait one second
//	sleep(1);

	// Wait for dialog web element
	//	this.element = this.browser.waitForElement(this.parent, this.findBy, false, shortTimeout(), true/*visible*/, false/*first occurrence*/);
	setElement(alreadyOpenedDialogIDs);

	// Loop until having got the web element
	if (DEBUG) debugPrintln("		  -> timeout="+(this.max*shortTimeout())+" seconds");
	int count = 0;
	while (this.element == null) {
		if (count++ > this.max) {
			throw new WaitElementTimeoutError("Failing to open the dialog "+this);
		}
		// Workaround
		debugPrintln("Workaround: click on "+this.openingElement+" to open dialog again as previous click didn't work...");
	   	try {
	        clickOnOpenElement(this.openingElement, count);
	    }
	   	catch (WebDriverException wde) {
			// Workaround
	    	debugPrintException(wde);
			debugPrintln("Workaround: exception occurred during the click might be because the dialog finally opened!?");
	    }
	   	// Disabled code which would allow to manage several successive click on the opening element...
	   	// TODO See if we discard it or enabled it at some point
//	   	List<WebBrowserElement> windowElements = this.browser.waitForElements(this.parent, this.findBy, false, shortTimeout(), true/*visible*/);
//	   	final int size = windowElements.size();
//	   	switch (size) {
//	   		case 1:
//	   			// We got it, store in window and leave the loop
//	   			this.element = windowElements.get(0);
//	   			break;
//	   		case 0:
//	   			// Still not found, the loop will continue
//	   			break;
//	   		case 2:
//	   			// Apparently, the first dialog finally opened but the second click opened
//	   			// another dialog. So, keep the first one and close the second
//	   			this.element = windowElements.get(1);
//	   			WebBrowserElement firstWindow = windowElements.get(0);
//	   			firstWindow.findElement(By.xpath(".//button[text()='Cancel']")).click();
//	   			sleep(2);
//	   			break;
//	   		default:
//	   			throw new ScenarioFailedError("Too many dialogs opened.");
//	   	}
	    setElement(alreadyOpenedDialogIDs);
	}

	// Wait for extra loading end if necessary
	waitForLoadingEnd();

	// Purge alerts if any
	if (this.purgeAlerts) {
		if (this.browser.purgeAlerts("Open dialog "+this.locator+"from "+this.page) > 0) {
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
 * Get the element on an already opened dialog.
 *
 * @return The dialog web element
 * @throws WaitElementTimeoutError If the dialog is not opened before the given timeout
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
public WebBrowserElement opened() throws WaitElementTimeoutError {
	debugPrintEnteringMethod();
	setElement(null);
	if (this.element == null) {
		throw new WaitElementTimeoutError("Cannot find any dialog with corresponding xpath: "+this.locator);
	}
	return this.element;
}

/**
 * Get the element on an already opened dialog.
 *
 * @param seconds The number of seconds to wait for the window to be opened.
 * @return The dialog web element
 * @throws WaitElementTimeoutError If the dialog is not opened before the given timeout
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
public WebBrowserElement openedBeforeTimeout(final int seconds) throws WaitElementTimeoutError {
	if (isOpenedBeforeTimeout(seconds)) {
		return opened();
	}
	throw new WaitElementTimeoutError("Cannot find any dialog with corresponding xpath: "+this.locator);
}

/**
 * Sets the current browser frame to this dialog's frame.
 */
protected void selectDialogFrame() {
	this.browser.selectFrame(getFrame());
}

private void setElement(final List<String> alreadyOpenedDialogIDs) {

	// Get list of opened dialog elements
   	List<WebBrowserElement> openedDialogElements = getOpenedElements(1/*sec*/);

   	// Keep only dialogs which where not already opened
   	Iterator<WebBrowserElement> iterator = openedDialogElements.iterator();
   	List<WebBrowserElement> dialogElements = new ArrayList<WebBrowserElement>();
   	while (iterator.hasNext()) {
   		WebBrowserElement openedDialogElement = iterator.next();
   		String dialogID = openedDialogElement.getAttribute("id");
   		if (alreadyOpenedDialogIDs == null || !alreadyOpenedDialogIDs.contains(dialogID)) {
   			dialogElements.add(openedDialogElement);
   		}
   	}

   	// Go through the map to see if there's any doubled opened dialog
   	final int size = dialogElements.size();
   	switch (size) {
   		case 1:
   			// We got it, store in window and leave the loop
   			this.element = dialogElements.get(0);
   			break;
   		case 0:
   			// No opened dialog was found
   			debugPrintln("WARNING: No opened dialog was found after having clicked on open element.");
   			break;
   		default:
   			// Apparently, there are several dialogs opened, hence close all but the last one
   			debugPrintln("WARNING: "+size+" dialogs have been found after having clicked on open element, keep the last one and close all others");
   			for (int i=0; i<size-1; i++) {
	   			WebBrowserElement windowElement = dialogElements.get(i);
	   			By buttonLocator = getCloseButtonLocator(false);
	   			if (buttonLocator == null) {
	   				throw new ScenarioFailedError("Several dialogs are opened and they cannot be be cancelled.");
	   			}
   				debugPrintln("	-> close dialog '"+windowElement+"' by clicking on "+buttonLocator+" button.");
	   			windowElement.findElement(buttonLocator).click();
   			}
   			this.element = dialogElements.get(size-1);
			debugPrintln("	-> keep dialog '"+this.element+"'.");
   			sleep(2);
   			break;
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
		setElement(null);
	}
	super.waitWhileDisplayed(seconds);
}
}
