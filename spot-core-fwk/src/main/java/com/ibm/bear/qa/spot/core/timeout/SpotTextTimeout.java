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
package com.ibm.bear.qa.spot.core.timeout;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.EMPTY_STRING;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.println;
import static com.ibm.bear.qa.spot.core.utils.StringUtils.compare;

import com.ibm.bear.qa.spot.core.scenario.errors.*;
import com.ibm.bear.qa.spot.core.utils.StringUtils.Comparison;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * Class to manage timeout while waiting that an {@link WebBrowserElement web element}
 * text matches a condition.
 * <p>
 * An expected text is provided and element text is compared in one of the
 * possible ways offered by the enum {@link Comparison}. By default
 * this is the {@link Comparison#Equals} which is used but it might be
 * changed by using a constructor allowing another one.
 * </p><p>
 * It means that for this class the condition will be:
 * <ul>
 * <li>for {@link Comparison#Equals}:
 * <pre>
 * this.element.getText(false).equals(this.expectedText);
 * </pre>
 * </li>
 * <li>for {@link Comparison#StartsWith}:
 * <pre>
 * this.element.getText(false).startsWith(this.expectedText);
 * </pre>
 * </li>
 * <li>for {@link Comparison#IsStartOf}:
 * <pre>
 * this.expectedText.startsWith(this.element.getText());
 * </pre>
 * </li>
 * <li>for {@link Comparison#EndsWith}:
 * <pre>
 * this.element.getText(false).endsWith(this.expectedText);
 * </pre>
 * </li>
 * <li>for {@link Comparison#IsEndOf}:
 * <pre>
 * this.expectedText.endsWith(this.element.getText());
 * </pre>
 * </li>
 * <li>for {@link Comparison#Contains}:
 * <pre>
 * this.element.getText(false).contains(this.expectedText);
 * </pre>
 * </li>
 * </ul>
 * </p><p>
 * This class overrides the two following abstract methods:
 * <ul>
 * <li>{@link #getCondition()}: Return the result of the condition evaluation.</li>
 * <li>{@link #getConditionLabel()}: Return the text explaining the condition.</li>
 * </ul>
 * </p>
 */
public class SpotTextTimeout extends SpotAbstractTimeout {

	/* Fields */
	// The text that the element text will be compared to
	final private String expectedText;
	// The current text of the element (stored for restitution later)
	private String currentText;
	// The comparison to be done between element text and expected one
	final Comparison comparison;
	// Recovery while getting element text
	boolean recovery = false;

/**
 * Create a timeout instance which will test whether a text is empty or not.
 * <p>
 * Note that following important behavior with the created instance:
 * <ol>
 * <li>this constructor does not initialize the element, hence it must be be used
 * only by a subclass which overrides the {@link #getText()} method
 * (explaining why its protected instead of public...)</li>
 * <li>a {@link WaitElementTimeoutError} will be raised when the timeout expires</li>
 * <li>as no text is specified, the condition is to test that the overrided {@link #getText()}
 * method is empty or not</li>
 * </ol>
 * </p>
 */
protected SpotTextTimeout() {
	this(EMPTY_STRING);
}

/**
 * Create a timeout instance which will test whether a text is equals to the given text.
 * <p>
 * Note that following important behavior with the created instance:
 * <ol>
 * <li>this constructor does not initialize the element, hence it must be be used
 * only by a subclass which overrides the {@link #getText()} method
 * (explaining why its protected instead of public...)</li>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * <li>a {@link WaitElementTimeoutError} will be raised when the timeout expires</li>
 * </ol>
 * </p>
 * @param text The expected text to be compared with
 */
protected SpotTextTimeout(final String text) {
	super();
	this.expectedText = text;
	this.comparison = Comparison.Equals;
}

/**
 * Create a timeout instance which will test whether a text is equals to the given text.
 * <p>
 * Note that following important behavior with the created instance:
 * <ol>
 * <li>this constructor does not initialize the element, hence it must be be used
 * only by a subclass which overrides the {@link #getText()} method
 * (explaining why its protected instead of public...)</li>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * <li>a {@link WaitElementTimeoutError} will be raised when the timeout expires</li>
 * </ol>
 * </p>
 * @param text The expected text to be compared with
 */
protected SpotTextTimeout(final String text, final Comparison comparison, final boolean fail) {
	super(null, fail);
	this.expectedText = text;
	this.comparison = comparison;
}

/**
 * Create a timeout instance which will test whether a text is equals to the given text.
 * <p>
 * <ol>
 * <li>this constructor does not initialize the element, hence it must be be used
 * only by a subclass which overrides the {@link #getText()} method
 * (explaining why its protected instead of public...)</li>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * </ol>
 * </p>
 * @param text The expected text to be equals to
 * @param fail Flag to indicate whether a {@link WaitElementTimeoutError} will be raised when the timeout
 * expires or not.
 */
protected SpotTextTimeout(final String text, final boolean fail) {
	super(null, fail);
	this.expectedText = text;
	this.comparison = Comparison.Equals;
}

/**
 * Create a timeout instance which will test whether the element text is equals to the given text.
 * <p>
 * <ol>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * <li>a {@link WaitElementTimeoutError} will be raised when the timeout expires</li>
 * </ol>
 * </p>
 * @param text The expected text to be equals to
 * @param compare The text comparison to use, see {@link Comparison} enum
 * @param webElement The element used for the text comparison
 * </p>
 */
public SpotTextTimeout(final String text, final Comparison compare, final WebBrowserElement webElement) {
	super(webElement);
	this.expectedText = text;
	this.comparison = compare;
}

/**
 * Create a timeout instance which will test whether the element text is equals to the given text.
 * <p>
 * <ol>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * <li>a {@link WaitElementTimeoutError} will be raised when the timeout expires</li>
 * </ol>
 * </p>
 * @param text The expected text to be equals to
 * @param webElement The element used for the text comparison
 * </p>
 */
public SpotTextTimeout(final String text, final WebBrowserElement webElement) {
	super(webElement);
	this.expectedText = text;
	this.comparison = Comparison.Equals;
}

/**
 * Create a timeout instance which will test whether the element text is equals to the given text.
 * <p>
 * <ol>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * </ol>
 * </p>
 * @param text The expected text to be equals to
 * @param webElement The element used for the text comparison
 * @param fail Flag to indicate whether a {@link WaitElementTimeoutError} will be raised when the timeout
 * expires or not.
 * </p>
 */
public SpotTextTimeout(final String text, final WebBrowserElement webElement, final boolean fail) {
	super(webElement, fail);
	this.expectedText = text;
	this.comparison = Comparison.Equals;
}

/**
 * Create a timeout instance which will test whether the element text is equals to the given text.
 * <p>
 * <ol>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * </ol>
 * </p>
 * @param text The expected text to be equals to
 * @param compare The text comparison to use, see {@link Comparison} enum
 * @param webElement The element used for the text comparison
 * @param fail Flag to indicate whether a {@link WaitElementTimeoutError} will be raised when the timeout
 * expires or not.
 * </p>
 */
public SpotTextTimeout(final String text, final Comparison compare, final WebBrowserElement webElement, final boolean fail) {
	super(webElement, fail);
	this.expectedText = text;
	this.comparison = compare;
}

/**
 * Create a timeout instance which will test whether the element text is empty or not.
 * <p>
 * <ol>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * <li>a {@link WaitElementTimeoutError} will be raised when the timeout expires</li>
 * </ol>
 * </p>
 * @param webElement The element used for the text comparison
 * </p>
 */
public SpotTextTimeout(final WebBrowserElement webElement) {
	this(EMPTY_STRING, webElement);
}

/**
 * Create a timeout instance which will test whether the element text is empty or not.
 * <p>
 * <ol>
 * <li>default {@link Comparison#Equals} text comparison is used by the timeout</li>
 * </ol>
 * </p>
 * @param webElement The element used for the text comparison
 * @param fail Flag to indicate whether a {@link WaitElementTimeoutError} will be raised when the timeout
 * </p>
 */
public SpotTextTimeout(final WebBrowserElement webElement, final boolean fail) {
	this(EMPTY_STRING, webElement, fail);
}

@Override
protected void fail(final boolean whileLoop) throws WaitElementTimeoutError {
	if (!whileLoop) {
		// If the until loop fails, that means condition is false
		// Hence, display both strings to allow easy debugging
		println("ERROR: Timeout occured before expected text matches:");
		println("	- Expected text: "+this.expectedText);
		println("	- Actual   text: "+getText());
	}
	throw new ScenarioFailedError(getFailureMessage(whileLoop));
}

@Override
protected boolean getCondition() {
	this.currentText = getText();
	return compare(this.currentText, this.expectedText, this.comparison);
}

@Override
protected String getConditionLabel() {
	if (this.expectedText.length() == 0) {
		return "Element has no text";
	}
	return "Element text "+this.comparison+" '"+this.expectedText+"'";
}

/**
 * Return the current text used when performing comparison.
 * <p>
 * It differs from {@link #getText()} in the way that it's stored while doing
 * the comparison and can be used later to understand why the timeout was
 * raised.
 * </p>
 * @return The text used for the comparison
 */
public String getCurrentText() {
	return this.currentText;
}

/**
 * Return the text which will be compared with {@link #expectedText}.
 * <p>
 * By default it's the element text but subclass may need to override it
 * </p>
 */
protected String getText() {
	if (this.element == null) {
		throw new ScenarioImplementationError("Cannot get text without having set an element in the timeout.");
	}
	return this.element.getText(this.recovery);
}
}
