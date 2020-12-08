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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.pause;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.api.elements.SpotExpandable;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.SpotTreeNodeExpansionError;

/**
 * Class to handle an expandable web element in a web page.
 * <p>
 * By default the expansion mechanism of the web element is managed by its
 * <code>aria-expanded</code> attribute. If the wrapped web element does
 * not have this attribute, then {@link #isExpandable()} and {@link #isExpanded()}
 * methods must be overridden in the corresponding subclass.
 * </p><p>
 * This class defines following public API methods of {@link SpotExpandable} interface:
 * <ul>
 * <li>{@link #collapse()}: Expand the current web element.</li>
 * <li>{@link #expand()}: Expand the current web element.</li>
 * <li>{@link #isExpandable()}: Returns whether the current wrapped web element is expandable or not.</li>
 * <li>{@link #isExpanded()}: Returns whether the current wrapped web element is expanded or not.</li>
 * <li>{@link #toggle()}: Expand the current web element.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #clickOnExpansionElement(boolean)}: Click on the expansion element in order to toggle the expandable element.</li>
 * <li>{@link #getExpandableAttribute()}: Return the expandable attribute.</li>
 * <li>{@link #waitForActionEnd(boolean)}: Wait for the performed action to be finished.</li>
 * </ul>
 * </p>
 */
public class SpotExpandableElement extends WebElementWrapper implements SpotExpandable {

	/**
	 * The web element on which to click in order to perform expanse operations.
	 * <p>
	 * When <code>null</code>, it's assumed that the expansion element is the wrapped element itself.
	 * </p>
	 */
	protected WebBrowserElement expansionElement;

/**
 * Create an expandable element in the given page using the wrapped web element
 * found by the given locator.
 * <p>
 * When using this constructor, it's assumed that the wrapped web element is also
 * used for the expanse and collapse operations (ie. by simply clicking on it).
 * </p>
 * @param page The page in which the expandable element is located
 * @param locator The locator to find the wrapped web element
 */
public SpotExpandableElement(final WebPage page, final By locator) {
	this(page, locator, null);
}

/**
 * Create an expandable element in the given page using the wrapped and expansion
 * web elements found by the given locators.
 * <p>
 * <b>Important</b>: The expansion web element locator is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param page The page in which the expandable element is located
 * @param locator The locator to find the wrapped web element
 * @param expansionLocator The locator to find the expansion web element
 */
public SpotExpandableElement(final WebPage page, final By locator, final By expansionLocator) {
	super(page, locator);
	this.expansionElement = expansionLocator == null ? this.element : waitForMandatoryElement(expansionLocator, shortTimeout());
}

/**
 * Create an expandable element in the given page using the given wrapped web
 * element.
 * <p>
 * When using this constructor, it's assumed that the wrapped web element is also
 * used for the expanse and collapse operation (ie. by simply clicking on it).
 * </p>
 * @param page The page in which the expandable element is located
 * @param webElement The wrapped web element
 */
public SpotExpandableElement(final WebPage page, final WebBrowserElement webElement) {
	this(page, webElement, null);
}

/**
 * Create an expandable element in the given page using the given wrapped web
 * element and the expansion web element found using the given locator.
 * <p>
 * <b>Important</b>: The expansion web element locator is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param page The page in which the expandable element is located
 * @param webElement The wrapped web element
 * @param expansionLocator The locator to find the expansion web element
 */
public SpotExpandableElement(final WebPage page, final WebBrowserElement webElement, final By expansionLocator) {
	this(page, webElement, null, expansionLocator);
}

/**
 * Create an expandable element in the given page and frame using the given wrapped web
 * element and the expansion web element found using the given locator.
 * <p>
 * <b>Important</b>: The expansion web element locator is assumed to be relative
 * to the wrapped web element.
 * </p>
 * @param page The page in which the expandable element is located
 * @param webElement The wrapped web element
 * @param frame The frame that the element belongs
 * @param expansionLocator The locator to find the expansion web element
 */
public SpotExpandableElement(final WebPage page, final WebBrowserElement webElement, final WebBrowserFrame frame, final By expansionLocator) {
	super(page, webElement, frame);
	this.expansionElement = expansionLocator == null ? this.element : waitForMandatoryElement(expansionLocator, shortTimeout());
}

/**
 * Click on the expansion element in order to toggle the expandable element.
 * <p>
 * The default behavior is to click on the expansion element after having made it
 * visible assuming that the same web element is used for expansion and collapsing
 * actions. If this default behavior is not true, then a peculiar subclass might want
 * to override this method and perform different actions while expanding or collapsing
 * the current object.
 * </p>
 * @param expand Tells whether the click is for an expansion or a collapse
 */
protected void clickOnExpansionElement(final boolean expand) {
	// TODO The scrollIntoView screwed up the page container display
	// That needs to be replace by something else...
//	this.expansionElement.scrollIntoView();
	this.expansionElement.makeVisible().click();
}

/**
 * Expand the current web element.
 * <p>
 * If the web element is already expanded, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
@Override
public void collapse() throws ScenarioFailedError {
	debugPrintln("		+ Collapse expandable web element "+this.element);
	performAction(false);
}

/**
 * Expand the current web element.
 * <p>
 * If the web element is already expanded, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
@Override
public void expand() throws ScenarioFailedError {
	debugPrintln("		+ Expand expandable web element "+this.element);
	performAction(true);
}

/**
 * Return the expandable attribute.
 * <p>
 * If the web element has no specific attribute for the expansion status, then
 * subclass must ignore this method and override both {@link #isExpandable()}
 * and {@link #isExpanded()}.
 * </p>
 * @return The attribute value as a {@link String}.
 */
protected String getExpandableAttribute() {
	return this.element.getAttribute("aria-expanded");
}

/**
 * Returns whether the current wrapped web element is expandable or not.
 * <p>
 * Subclass must override this method if the web element has no specific
 * expandable attribute.
 * </p>
 * <p>
 * Note that this method is only useful with trees (otherwise it should
 * always return <code>true</code>.
 * </p>
 * @return <code>true</code> if the current node is expandable, <code>false</code>
 * otherwise.
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
@Override
public boolean isExpandable() throws ScenarioFailedError {
	return getExpandableAttribute() != null;
}

/**
 * Returns whether the current wrapped web element is expanded or not.
 * <p>
 * Subclass must override this method if the web element has no specific
 * expandable attribute.
 * </p>
 * @return <code>true</code> if the current node is expanded, <code>false>/code>
 * otherwise.
 * @throws SpotTreeNodeExpansionError If the wrapped web element does not have
 * any expandable attribute.
 */
@Override
public boolean isExpanded() throws SpotTreeNodeExpansionError {
	String expandAttribute = getExpandableAttribute();
	if (expandAttribute == null) {
		throw new SpotTreeNodeExpansionError("Expansion web element "+this.element+" has no 'aria-expanded' attribute.");
	}
	return "true".equals(expandAttribute);
}

/**
 * Perform the given action.
 * <p>
 * Note that nothing is done if the current element is already in the expected state.
 * </p>
 * @param expand Expand the current element if <code>true</code>
 * collapse it otherwise.
 * @throws ScenarioFailedError If the current element is not at the expected
 * state after one second after the actions was performed.
 */
private void performAction(final boolean expand) throws ScenarioFailedError {

	// Do nothing if it's already in the desired expansion state
	if (isExpanded() == expand) {
		return;
	}

	// Perform action
	clickOnExpansionElement(expand);

	// Raise an error if the expansion is not in the desired state
	waitForActionEnd(expand);
}

/**
 * Expand the current web element.
 * <p>
 * If the web element is already expanded, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
@Override
public final void toggle() throws ScenarioFailedError {
	debugPrintln("		+ Toggle expandable web element "+this.element);
	if (isExpanded()) {
		collapse();
	} else {
		expand();
	}
}

/**
 * Wait for the performed action to be finished.
 *
 * @param expand The expected state for the current element.
 */
protected void waitForActionEnd(final boolean expand) {
	long timeout = 1000 + System.currentTimeMillis();
	final StringBuilder nameBuilder = new StringBuilder("element '")
		.append(getText())
		.append("' (")
		.append(this.element.getFullLocator());
	if (this.element != this.expansionElement) { // != is intentional
		nameBuilder.append(" using its expansion element: ").append(this.expansionElement.getFullLocator());
	}
	nameBuilder.append(")");
	boolean workaround = false;
	while (isExpanded() != expand) {
		if (System.currentTimeMillis() > timeout) {
			if (workaround) {
				StringBuilder messageBuilder = new StringBuilder("Cannot ")
					.append(expand ? "expand " : "collapse ")
					.append(nameBuilder);
				throw new ScenarioFailedError(messageBuilder.toString());
			}
			workaround = true;
			debugPrintln("WARNING: "+(expand?"Expansion":"Collapsing")+" did NOT work for expandable "+nameBuilder+"!");
			debugPrintln("	=> Retry to perform the action one second after the initial action...");
			clickOnExpansionElement(expand);
			timeout = 1000 + System.currentTimeMillis();
		}
		pause(100);
	}
}
}
