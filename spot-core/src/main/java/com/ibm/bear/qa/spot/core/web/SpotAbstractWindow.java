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

import static com.ibm.bear.qa.spot.core.performance.PerfManager.PERFORMANCE_ENABLED;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.List;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.api.elements.SpotWindow;
import com.ibm.bear.qa.spot.core.config.Config;
import com.ibm.bear.qa.spot.core.performance.PerfManager.RegressionType;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.WaitElementTimeoutError;

/**
 * Abstract class for any window opened in a browser page.
 * <p>
 * All necessary information to find the window in the page has to be given when
 * creating an instance of this class. Then, it will be possible to open it at any time,
 * but also to recover it if troubles occur during the opening operation (typically
 * if the window does not show up after having performed the expected operation...).
 * </p><p>
 * Public API for this class is defined in {@link SpotWindow} interface.
 * </p><p>
 * Internal API methods accessible in the framework are:
 * <ul>
 * <li>{@link #open(WebBrowserElement)}: open the window by clicking on the given web element.</li>
 * </ul>
 * </p><p>
 * Internal API methods accessible from subclasses are:
 * <ul>
 * <li>{@link #checkErrorMessage()}: Check whether an error message is displayed or not.</li>
 * <li>{@link #closeAction(boolean)}: The action to perform to close the window.</li>
 * <li>{@link #closeTimeout()}: Time allowed to close the window.</li>
 * <li>{@link #getCloseButtonLocator(boolean)}: Return the locator of the button to close the window.</li>
 * <li>{@link #open(WebBrowserElement)}: open the window by clicking on the given web element.</li>
 * </ul>
 * </p>
 */
abstract public class SpotAbstractWindow extends WebElementWrapper implements SpotWindow, WebConstants {

	/**
	 *  The locator to find the opened window in the web page.
	 */
	protected final By locator;

	/**
	 * The maximum number of attempts while searching the window.
	 */
	protected final int max;

	/**
	 * The element used to open the window.
	 */
	protected WebBrowserElement openingElement;

public SpotAbstractWindow(WebElementWrapper parent, By locator) {
	super(parent, (WebBrowserElement) null);
	this.locator = locator;
	this.max = WebBrowserElement.MAX_RECOVERY_ATTEMPTS;
}

public SpotAbstractWindow(final WebPage page, final By locator) {
	this(page, locator, (WebBrowserFrame) null);
}

public SpotAbstractWindow(final WebPage page, final By locator, final String frame) {
	this(page, locator, new WebNamedFrame(page.getBrowser(), frame));
}

public SpotAbstractWindow(final WebPage page, final By locator, final WebBrowserFrame frame) {
	super(page, frame);
	this.locator = locator;
	this.max = WebBrowserElement.MAX_RECOVERY_ATTEMPTS;
}

public SpotAbstractWindow(final WebPage page, final WebBrowserElement element) {
	super(page, element);
	this.locator = element.locator;
	this.max = WebBrowserElement.MAX_RECOVERY_ATTEMPTS;
}

@Override
public final void cancel() {
	close(false/*cancel*/);
}

@Override
public void cancelAll() {
	if (DEBUG) debugPrintln("		+ Close all possible opened dialogs.");

	// Get all opened dialogs
	List<WebBrowserElement> openedDialogElements = getOpenedElements(1/*seconds*/);

	// Close them all
	for (WebBrowserElement dialogElement: openedDialogElements) {
		By closeButtonLocator = getCloseButtonLocator(false);
		WebBrowserElement buttonElement = dialogElement.waitForElement(closeButtonLocator, 1/*sec*/);
		if (buttonElement == null) {
			throw new ScenarioFailedError("Cannot close dialog '"+this.locator+"' as button '"+closeButtonLocator+"' was not found.");
		}
		buttonElement.click();
	}
}

@Override
public final void close() {
	close(true/*validate*/);
}

/**
 * Check whether an error message is displayed or not.
 * <p>
 * Default is to do nothing. Subclasses have to implement specific code
 * to get the error message displayed in the window.
 * </p>
 * @throws ScenarioFailedError With displayed message if any.
 */
protected void checkErrorMessage() throws ScenarioFailedError {
	// Do nothing by default
}

/**
 * Close the dialog.
 *
 * @param validate Tells whether the close action is to validate or to cancel.
 * @see #closeAction(boolean)
 */
protected void close(final boolean validate) {
	// Start server time if performances are managed
	if (PERFORMANCE_ENABLED) {
		this.page.startPerfManagerServerTimer();
	}

	// Perform the close action
	closeAction(validate);

	// Wait for the window to vanish
	try {
		waitWhileDisplayed(closeTimeout());
	}
	catch (WaitElementTimeoutError wete) {
		// Workaround for Safari as close action might be inefficient on first attempt
		if (this.browser.isSafari()) {
			closeAction(validate);
			waitWhileDisplayed(closeTimeout());
		} else {
			throw wete;
		}
	}

	// Go back to browser frame used before opening the window
    if (this.frames[0] == null || this.frames[0].isDisplayed()) {
	    switchToBrowserFrame();
    } else {
    	debugPrintln("Warning: Browser frame is no longer displayed, hence reset it while closing browser.");
    	this.browser.resetFrame();
    }

	// Add performance result
	if (PERFORMANCE_ENABLED) {
		this.page.addPerfResult(RegressionType.Server, this.page.getTitle());
	}

	// Last pause to be sure the window will be over
//	pause(500);
}

/**
 * The action to perform to close the window.
 *
 * @param validate Tells whether the close action is to validate or to cancel.
 */
protected abstract void closeAction(boolean validate);

/**
 * Time allowed to close the window.
 * <p>
 * Default value is got from {@link Config#closeDialogTimeout()}.
 * </p>
 * @return The timeout in seconds as an <code>int</code>.
 */
protected int closeTimeout() {
	return getConfig().closeDialogTimeout();
}

/**
 * Return the locator for the button to close the window.
 *
 * @param validate Tells whether the close action is to validate or to cancel.
 * @return The xpath of the button as a {@link String}
 */
protected abstract By getCloseButtonLocator(boolean validate);

/**
 * Get opened elements.
 *
 * @param seconds Time to wait for the elements
 * @return The list of opened dialog elements as a {@link List} of {@link WebBrowserElement}
 */
protected List<WebBrowserElement> getOpenedElements(final int seconds) {
	return this.browser.waitForElements(getParentElement(), this.locator, false, seconds, true/*visible*/);
}

@Override
public boolean isCloseable() {

	// Get web element for close button
	WebBrowserElement closeButtonElement = findElement(getCloseButtonLocator(true/*validate*/), /*recovery:*/false);

	// If button is found then return whether it's enabled or not
	if (closeButtonElement != null) {
		return closeButtonElement.isEnabled();
	}

	// No button found, hence it's not closeable
	return false;
}

@Override
public boolean isOpened() {
	return isOpenedBeforeTimeout(1/*sec*/);
}

@Override
public boolean isOpenedBeforeTimeout(final int seconds) {
	if (DEBUG) debugPrintln("		+ Check whether a window is opened or not");
	List<WebBrowserElement> openedElements = getOpenedElements(seconds);
	return openedElements.size() > 0;
}

/**
 * Open the window by clicking on the given web element.
 *
 * @param openElement The element on which to perform the open action.
 * @return The web element matching the opened window as a {@link WebBrowserElement}.
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
abstract public WebBrowserElement open(final WebBrowserElement openElement);

/**
 * Scroll the window from give open element height if it's hidden by the fixed
 * navbar web element.
 *
 * @param openElement The web element used to open the current window
 */
protected void scrollIfNecessary(final WebBrowserElement openElement) {
	if (DEBUG) debugPrintln(DEBUG_ENTERING_METHOD_INDENTATION+" (openElement="+openElement+")");

	// Skip scroll if the open element is in frame
	if (openElement.isInFrame()) {
		return;
	}

	// Scroll if necessary
	getPage().scrollToMakeElementVisible(openElement);
}
}
