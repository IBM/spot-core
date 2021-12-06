/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.utils.ByUtils.ComparisonPattern;

/**
 * Class to handle select element.
 *
 * @see Select
 */
public class WebSelectElement extends WebElementWrapper {

public WebSelectElement(final WebPage page, final By locator, final WebBrowserFrame frame) {
	super(page, locator, frame);
	this.select = new Select(this.element);
}

	final Select select;

public WebSelectElement(final WebElementWrapper parent, final By selectBy) {
	super(parent, selectBy);
	this.select = new Select(this.element);
}

public WebSelectElement(final WebElementWrapper parent, final WebBrowserElement element) {
	super(parent, element);
	this.select = new Select(this.element);
}

public WebSelectElement(final WebPage page, final By selectBy) {
	super(page, selectBy);
	this.select = new Select(this.element);
}

public WebSelectElement(final WebPage page, final WebBrowserElement element) {
	super(page, element);
	this.select = new Select(this.element);
}

public WebSelectElement(final WebPage page, final WebBrowserElement parent, final By selectBy) {
	super(page, parent.waitShortlyForMandatoryDisplayedChildElement(selectBy));
	this.select = new Select(this.element);
}

/**
 * Clear all selected entries. this is only valid when the SELECT supports multiple selections.
 */
public void deselectAll() {
	this.select.deselectAll();
}

/**
 * Return all elements list.
 *
 * @return The list of option as a {@link List} of {@link WebBrowserElement}.
 */
public List<WebBrowserElement> getAllElements() {
	return WebBrowserElement.getList(this.select.getOptions());
}

/**
 * Return the select labels list.
 *
 * @return The list of option as a {@link List} of {@link String}.
 */
public List<String> getAllLabels() {
	List<WebElement> options = this.select.getOptions();
	List<String> labels = new ArrayList<String>();
	for (WebElement option : options) {
		labels.add(option.getText());
	}
	return labels;
}

/**
 * Return the selected elements list.
 *
 * @return The list of option as a {@link List} of {@link WebBrowserElement}.
 */
public List<WebBrowserElement> getAllSelectedElements() {
	return WebBrowserElement.getList(this.select.getAllSelectedOptions());
}

/**
 * Return the select values list.
 * <p>
 * Note that the returned list can be empty in case the options
 * have no value attribute.
 * </p>
 * @return The list of option as a {@link List} of {@link String}.
 */
public List<String> getAllValues() {
	List<WebElement> options = this.select.getOptions();
	List<String> values = new ArrayList<String>();
	for (WebElement option : options) {
		values.add(option.getAttribute("value"));
	}
	return values;
}

/**
 * Return the WebBrowserElement specified by the option label.
 *
 * @param option A {@link String} that represents the option to look for.
 * @return A {@link WebBrowserElement} identified by the option label specified. Returns null if the element is not found.
 */
public WebBrowserElement getOptionElement(final String option) {
	for (WebBrowserElement optionElement : getAllElements()) {
		if (optionElement.getText().equals(option)) {
			return optionElement;
		}
	}
	return null;
}

/**
 * Return the selected element.
 *
 * @return The first selected option as a {@link WebBrowserElement}.
 */
public WebBrowserElement getSelectedElement() {
	return (WebBrowserElement) this.select.getFirstSelectedOption();
}

/**
 * Return the label of the selected option.
 *
 * @return The selected option label as a {@link String}.
 */
@Override
public String getText() {
	return getSelectedElement().getText();
}

/**
 * Checks if a specific option is available for selection using its label.
 *
 * @param option A {@link String} that represents the option to look for.
 * @return <code>true</code> if the option is found; <code>false</code> otherwise.
 */
public boolean hasOption(final String option) {
	for (String label : getAllLabels()) {
		if (label.equals(option)) {
			return true;
		}
	}
	return false;
}

/**
 * Checks if a specific option is available for selection using its value.
 *
 * @param value A {@link String} that represents the option value to look for.
 * @return <code>true</code> if the option is found; <code>false</code> otherwise.
 */
public boolean hasValue(final String value) {
	for (String val : getAllValues()) {
		if (val.equals(value)) {
			return true;
		}
	}
	return false;
}

/**
 * Select the corresponding option using the given text.
 *
 * @param option The option to select in the list.
 * @throws NoSuchElementException If the option is not present in the list
 */
public void select(final String option) throws NoSuchElementException {
	this.select.selectByVisibleText(option);
}

/**
 * Select the corresponding option using the given text.
 *
 * @param option The option to select in the list.
 * @param pattern The pattern used for matching text.
 * @throws ScenarioFailedError If the option is not found or if it's found but disabled
 */
public void select(final String option, final ComparisonPattern pattern) throws ScenarioFailedError {

	// If pattern is equals, then it's equivalent to select it
	if (pattern.equals(ComparisonPattern.Equals)) {
		this.select.selectByVisibleText(option);
		return;
	}

	// Start from first option element to make sure that all elements are checked
	int index = 0;

	// Loop trough the option to compare them to the option with the given pattern
	List<WebElement> optionElements = this.select.getOptions();
	for (WebElement optionElement: optionElements) {
		// Select the option
		this.select.selectByIndex(index);
		String optionText = optionElement.getText();
		boolean found = false;
		switch (pattern) {
			case Equals:
				found = optionText.equals(option);
				break;
			case StartsWith:
				found = optionText.startsWith(option);
				break;
			case EndsWith:
				found = optionText.endsWith(option);
				break;
			case Contains:
				found = optionText.contains(option);
				break;
		}
		if (found) {
			if (!optionElement.isEnabled()) {
				throw new ScenarioFailedError("Cannot select option '"+optionText+"' because it's disabled.");
			}
			return;
		}
		// Fix for: https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/330554
		// The option we want is not this one.  Deselect this one.
		this.select.deselectByIndex(index++);
	}
	throw new ScenarioFailedError(option+" was not found in selection element "+this.element);
}

/**
 * Select the corresponding option using the given value.
 *
 * @param value The value of the option to be selected in the list.
 */
public void selectByValue(final String value) {
	this.select.selectByValue(value);
}
}
