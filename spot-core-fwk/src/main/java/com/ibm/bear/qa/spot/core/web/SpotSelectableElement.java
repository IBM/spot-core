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
 * <li>{@link #action(Actions)}: Perform the given action on the selectable element.</li>
 * <li>{@link #click()}: Click on the element without taking into account its selection status.</li>
 * <li>{@link #clickAndWaitForSelection()}: Click on element and wait for the element to be selected.</li>
 * <li>{@link #getText()}: Return the text of the wrapped element.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #getSelectionElement()}: Return the element to perform the selection operation.</li>
 * <li>{@link #initSelectionElement()}: Initialize the element to perform the selection operation.</li>
 * <li>{@link #waitUntilSelection(boolean,boolean)}: Wait until the selection matches the given status.</li>
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
 * Create a selectable element in the given parent using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotSelectableElement(final WebElementWrapper parent, final By locator) {
	this(parent, locator, null);
}

/**
 * Create a selectable element in the given parent using the wrapped and selection
 * web elements found by the given locators.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotSelectableElement(final WebElementWrapper parent, final By locator, final By selectionLocator) {
	super(parent, locator);
	this.selectionLocator = selectionLocator;
}

/**
 * Create a selectable element in the given parent using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param wwElement The wrapped web element
 */
public SpotSelectableElement(final WebElementWrapper parent, final WebBrowserElement wwElement) {
	this(parent, wwElement, null);
}

/**
 * Create a selectable element in the given parent using the given wrapped web
 * element and the selection web element found using the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param parent The element wrapper in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotSelectableElement(final WebElementWrapper parent, final WebBrowserElement wwElement, final By selectionLocator) {
	super(parent, wwElement);
	this.selectionLocator = selectionLocator;
}

/**
 * Create a selectable element in the given page using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p>
 * @param page The page in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotSelectableElement(final WebPage page, final By locator) {
	this(page, locator, null);
}

/**
 * Create a selectable element in the given page using the wrapped and selection
 * web elements found by the given locators.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param page The page in which the selectable element is located
 * @param locator The locator to find the wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotSelectableElement(final WebPage page, final By locator, final By selectionLocator) {
	super(page, locator);
	this.selectionLocator = selectionLocator;
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the created wrapper itself.
 * </p>
 * @param page The page in which the selectable element is located
 * @param wwElement The wrapped web element
 */
public SpotSelectableElement(final WebPage page, final WebBrowserElement wwElement) {
	this(page, wwElement, null);
}

/**
 * Create a selectable element in the given page using the given wrapped web
 * element and the selection web element found using the given locator.
 * <p>
 * When using this constructor, it's assumed that selection operations are managed
 * by the selection element.
 * </p><p>
 * <b>Important</b>: The locator for the selection web element is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param page The page in which the selectable element is located
 * @param wwElement The wrapped web element
 * @param selectionLocator The locator to find the selection web element
 */
public SpotSelectableElement(final WebPage page, final WebBrowserElement wwElement, final By selectionLocator) {
	super(page, wwElement);
	this.selectionLocator = selectionLocator;
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
 * Click on the element without taking into account its selection status.
 *
 * @return The selection state of the element after the click occurred
 */
public boolean click() {
	debugPrintEnteringMethod();
	getSelectionElement().click();
	if (this.element.browser.isFirefox() && this.element.browser.getVersion().startsWith("78")) {
		// for an unknown reason, Firefox needs 2 clicks on the element to actually select it...!?
		debugPrintln("		  -> Double click for Firefox browser");
		pause(200);
		getSelectionElement().click();
	}
	return isSelected();
}

/**
 * Click on element and wait for the element to be selected.
 */
public void clickAndWaitForSelection() {
	clickAndWaitSelection(true);
}

private void clickAndWaitSelection(final boolean selected) {
	pause(200);
	debugPrintln("		  -> Click on element to select it...");
	getSelectionElement().click();
	if (this.element.browser.isFirefox() && this.element.browser.getVersion().startsWith("78") && !waitUntilSelection(selected, false, 1)) {
		// for an unknown reason, Firefox needs 2 clicks on the element to actually select it...!?
		debugPrintln("		  -> Double click for Firefox browser");
		pause(200);
		getSelectionElement().click();
		pause(600);
		if (!waitUntilSelection(selected, false)) {
			// In certain circumstance, even the double click does not work, hence try to workaround it by entering Return on the element
			debugPrintln("		  -> Double click was inefficient for Firefox browser");
			debugPrintln("		  -> Apply workaround to send ENTER key to the node element...");
			this.element.sendKeys(Keys.RETURN);
			pause(200);
			getSelectionElement().click();
			pause(800);
		}
	}
	debugPrintln("		  -> Wait that element becomes selected...");
	waitUntilSelection(selected, true);
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
	this.selectionElement = this.selectionLocator == null ? this.element : this.element.waitShortlyForMandatoryDisplayedChildElement(this.selectionLocator);
}

@Override
public boolean isSelected() throws ScenarioFailedError {
	return getSelectionElement().isSelected();
}

@Override
public void select() throws ScenarioFailedError {
	debugPrintEnteringMethod();
	if (isSelected()) {
		debugPrintln("		  -> Element is already selected, do nothing...");
		return;
	}
	clickAndWaitSelection(true);
}

@Override
public boolean toggle() throws ScenarioFailedError {
	boolean selected = isSelected();
	clickAndWaitSelection(!selected);
	return !selected;
}

@Override
public void unselect() throws ScenarioFailedError {
	debugPrintEnteringMethod();
	if (!isSelected()) {
		debugPrintln("		  -> Element is already unselected, do nothing...");
		return;
	}
	clickAndWaitSelection(false);
}

/**
 * Wait until the selection matches the given status.
 *
 * @param selected The expected selection status
 * @param fail Tells whether to fail or not if the status is not the expected one
 * after the timeout has expired
 * @return <code>true</code> if the selection status matches the expected one
 * or <code>false</code> if it does not with no failure
 */
protected boolean waitUntilSelection(final boolean selected, final boolean fail) {
	return waitUntilSelection(selected, fail, 3);
}

private boolean waitUntilSelection(final boolean selected, final boolean fail, final int seconds) {
	SpotAbstractTimeout timeout = new SpotAbstractTimeout(fail) {
		@Override
		protected boolean getCondition() {
			return isSelected() == selected;
		}
		@Override
		protected String getConditionLabel() {
			return "Selection element is "+(selected?"selected":"unselected");
		}
	};
	return timeout.waitUntil(seconds);
}
}
