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
package com.ibm.bear.qa.spot.core.timeout;

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

	String attributeName, text;
	String attributeValue;

public SpotAttributeContainsTimeout(final WebBrowserElement webElement, final String attribute, final String contains) {
	super(webElement, /*fail:*/true);
	this.attributeName = attribute;
	this.text = contains;
}

public String getAttributeValue() {
	return this.attributeValue;
}

@Override
protected boolean getCondition() {
	this.attributeValue = this.element.getAttribute(this.attributeName);
	if (this.text == null) {
		return this.attributeValue == null;
	}
	return this.attributeValue != null && this.attributeValue.contains(this.text);
}

@Override
protected String getConditionLabel() {
	String label = "Attribute '"+this.attributeName+"' ";
	if (this.text == null) {
		label += " is null";
	} else {
		label += " contains '"+this.text+"'";
	}
	return label;
}

@Override
protected void fail(final boolean whileLoop) throws WaitElementTimeoutError {
	throw new ScenarioFailedError(getFailureMessage(whileLoop));
}
}
