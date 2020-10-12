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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.ibm.bear.qa.spot.core.api.elements.SpotSelectable;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.timeout.SpotAbstractTimeout;

/**
 * Class to handle a selectable web element in a web page.
 * <p>
 * </p><p>
 * This class defines following public API methods of {@link SpotSelectable} interface:
 * <ul>
 * <li>{@link #isSelected()}: Returns whether the associated web element is selected or not.</li>
 * <li>{@link #select()}: Select the associated web element.</li>
 * <li>{@link #toggle()}: Toggle the associated web element.</li>
 * <li>{@link #unselect()}: Unselect the associated web element.</li>
 * </ul>
 * </p><p>
 * This class also defines following internal API methods:
 * <ul>
 * <li>{@link #getText()}: Return the text of the expandable element.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getSelectionElement()}: Return the element to perform the selection operation.</li>
 * <li>{@link #initSelectionElement()}: Initialize the element to perform the selection operation.</li>
 * </ul>
 * </p>
 */
public class SpotSelectableElement extends WebElementWrapper implements SpotSelectable {

	/**
	 * Possible selectable actions.
	 */
	public enum Actions { Select, Toggle, Unselect }

	/**
	 * The locator to find the selection element.
	 * <p>
	 * If <code>null</code>, then it's assumed there's no specific element
	 * for the selection and wrapped element will be used instead.
	 * </p><p>
	 * Note that this locator must be relative to the wrapped element.
	 * </p>
	 */
	protected final By selectionLocator;

	/**
	 * The element used for the selection. If <code>null</code>, then it's
	 * assumed that wrapped element itself performs the selection.
	 */
	protected WebBrowserElement selectionElement;

/**
 * Create an selectable element in the given page using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that the wrapped web element is also
 * used for the expanse and collapse operations (ie. by simply clicking on it).
 * </p>
 * @param page The page in which the expandable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotSelectableElement(final WebPage page, final By locator) {
	this(page, locator, null);
}

/**
 * Create an selectable element in the given page using the wrapped and expansion
 * web elements found by the given locators.
 * <p>
 * <b>Important</b>: The locator for the expansion web element is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param page The page in which the expandable element is located
 * @param locator The locator to find the wrapped web element
 * @param expansionLocator The locator to find the expansion web element
 */
public SpotSelectableElement(final WebPage page, final By locator, final By expansionLocator) {
	super(page, locator);
	this.selectionLocator = expansionLocator;
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that the wrapped web element is also
 * used for the expanse and collapse operation (ie. by simply clicking on it).
 * </p>
 * @param page The page in which the expandable element is located
 * @param webElement The wrapped web element
 */
public SpotSelectableElement(final WebPage page, final WebBrowserElement webElement) {
	this(page, webElement, null);
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element and the expansion web element found using the given locator.
 * <p>
 * <b>Important</b>: The locator for the expansion web element is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param page The page in which the expandable element is located
 * @param webElement The wrapped web element
 * @param expansionLocator The locator to find the expansion web element
 */
public SpotSelectableElement(final WebPage page, final WebBrowserElement webElement, final By expansionLocator) {
	super(page, webElement);
	this.selectionLocator = expansionLocator;
}

/**
 * Perform the given action on the selectable element.
 *
 * @param type The type of action to perform
 * @return The new status of the selectable element
 */
public boolean action(final Actions type) {
	switch (type) {
		case Select:
			select();
			break;
		case Toggle:
			toggle();
			break;
		case Unselect:
			unselect();
			break;
	}
	return isSelected();
}

/**
 * Return the element to perform the selection operation.
 * <p>
 * If {@link #selectionLocator} is defined then its the element found with
 * that locator which is used. If not, then it's the wrapped element which
 * is used.
 *</p>
 * @return the element
 */
final protected WebBrowserElement getSelectionElement() {
	if (this.selectionElement == null) {
		initSelectionElement();
	}
	return this.selectionElement;
}

@Override
public String getText() {
	return getSelectionElement().getText();
}

/**
 * Initialize the element to perform the selection operation.
 * <p>
 * If {@link #selectionLocator} is defined then its the element found with
 * that locator which is used. If not, then it's the wrapped element which
 * is used.
 *</p><p>
 * Sub-class might want to override this method in case the selection element
 * initialisation would be a more complex operation.
 * </p>
 */
protected void initSelectionElement() {
	this.selectionElement = this.selectionLocator == null ? this.element : this.element.waitForMandatoryElement(this.selectionLocator);
}

@Override
public boolean isSelected() throws ScenarioFailedError {
	return getSelectionElement().isSelected();
}

@Override
public void select() throws ScenarioFailedError {
	debugPrintEnteringMethod();
	pause(200);
	getSelectionElement().click();
	if (this.element.browser.isFirefox()) {
		// for an unknown reason, Firefox needs 2 clicks on the element to actually selects it...!?
		debugPrintln("		  -> Double click for Firefox browser");
		pause(200);
		getSelectionElement().click();
		pause(600);
		// In certain circumstance, even the double click does not work, hence try to workaround it by entering Return on the element
		if (waitUntilSelection(true, false)) {
			return;
		}
		debugPrintln("		  -> Double click was inefficient for Firefox browser");
		debugPrintln("		  -> Apply workaround to send ENTER key to the node element...");
		this.element.sendKeys(Keys.RETURN);
		pause(200);
		getSelectionElement().click();
		pause(800);
	}
	waitUntilSelection(true, true);
}

protected boolean waitUntilSelection(final boolean selected, final boolean fail) {
	SpotAbstractTimeout timeout = new SpotAbstractTimeout(fail) {
		@Override
		protected String getConditionLabel() {
			return "Selection element is "+(selected?"selected":"unselected");
		}
		@Override
		protected boolean getCondition() {
			return isSelected() == selected;
		}
	};
	return timeout.waitUntil(3);
}

@Override
public boolean toggle() throws ScenarioFailedError {
	boolean selected = isSelected();
	getSelectionElement().click();
	waitUntilSelection(!selected, true);
	return !selected;
}

@Override
public void unselect() throws ScenarioFailedError {
	if (isSelected()) {
		getSelectionElement().select();
	} else {
		debugPrintln("		  -> the element is already not selected do nothing...");
	}
	waitUntilSelection(false, true);
}
}
