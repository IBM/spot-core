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
package com.ibm.bear.qa.spot.core.timeout;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.EMPTY_STRING;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.WaitElementTimeoutError;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * Class to manage timeout while waiting that a {@link WebBrowserElement web element}
 * has a specific attribute containing a specific text.
 * <p>
 * For this class the condition is:
 * <pre>
 * String attributeValue = this.element.getAttribute(this.attributeName);
 * return attributeValue != null && attributeValue.contains(this.text);
 * </pre>
 * </p><p>
 * This class overrides the two following abstract methods:
 * <ul>
 * <li>{@link #getCondition()}: Return the result of the condition evaluation.</li>
 * <li>{@link #getConditionLabel()}: Return the text explaining the condition.</li>
 * </ul>
 * </p>
 */
public class SpotAttributeContainsTimeout extends SpotAbstractTimeout {

	/* Fields */
	/**
	 * The name of the attribute to be tested.
	 */
	String attributeName;

	/**
	 * The possible texts of the attribute.
	 */
	String[] possibleTexts;

	/**
	 * The value of the attribute when the timeout expires or the value matches
	 * the expected.
	 */
	String attributeValue;

/**
 * Create a timeout to wait for given attribute of given element to be match the given text.
 * <p>
 * Timeout behavior is to fail when it expires without having the expected state.
 * </p>
 * @param webElement The element to test
 * @param attribute The attribute to be tested in the element
 * @param possibleTexts The expected possible texts of the attribute. It can be <code>null</code>
 */
public SpotAttributeContainsTimeout(final WebBrowserElement webElement, final String attribute, final String... possibleTexts) {
	this(webElement, attribute, /*fail:*/true, possibleTexts);
}
/**
 * Create a timeout to wait for given attribute of given element to be match the given text.
 * <p>
 * Timeout behavior when it expires without having the expected state depends
 * on given <b>fail</b> argument.
 * </p>
 * @param webElement The element to test
 * @param attribute The attribute to be tested in the element
 * @param fail Tells whether an error is raised when timeout expires or not
 * @param possibleTexts The expected possible texts of the attribute. It can be <code>null</code>
 */
public SpotAttributeContainsTimeout(final WebBrowserElement webElement, final String attribute, final boolean fail, final String... possibleTexts) {
	super(webElement, fail);
	this.attributeName = attribute;
	this.possibleTexts = possibleTexts;
}

public String getAttributeValue() {
	return this.attributeValue;
}

@Override
protected boolean getCondition() {
	this.attributeValue = this.element.getAttribute(this.attributeName);
	if (this.possibleTexts == null || this.possibleTexts.length == 0) {
		return this.attributeValue == null;
	}
	for (String text: this.possibleTexts) {
		if (this.attributeValue == null) {
			if (text == null) {
				return true;
			}
		} else if (text != null && this.attributeValue.contains(text)) {
			return true;
		}
	}
	return false;
}

@Override
protected String getConditionLabel() {
	StringBuffer buffer = new StringBuffer("Attribute '")
			.append(this.attributeName)
			.append("' ");
	if (this.possibleTexts == null || this.possibleTexts.length == 0) {
		buffer.append(" is null");
	} else {
		buffer.append(" contains '");
		String separator = EMPTY_STRING;
		for (String text: this.possibleTexts) {
			buffer.append(separator).append(text);
			separator = "' or '";
		}
		buffer.append("'");
	}
	return buffer.toString();
}

@Override
protected void fail(final boolean whileLoop) throws WaitElementTimeoutError {
	throw new ScenarioFailedError(getFailureMessage(whileLoop));
}
}
