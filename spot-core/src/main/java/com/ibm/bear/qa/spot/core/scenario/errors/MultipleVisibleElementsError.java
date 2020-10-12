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
package com.ibm.bear.qa.spot.core.scenario.errors;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.LINE_SEPARATOR;

import java.util.List;

import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

public class MultipleVisibleElementsError extends ScenarioFailedError {

/**
 * Create a multiple visible elements error with a generic message.
 * @deprecated as of 6.0.0; use {@link #MultipleVisibleElementsError(List)}
 * to provide more debugging information.
 */
@Deprecated
public MultipleVisibleElementsError() {
	this("Found several visible elements.");
}

/**
 * Create a multiple visible elements error with a specific message.
 * @deprecated as of 6.0.0; use {@link #MultipleVisibleElementsError(List)}
 * or {@link #MultipleVisibleElementsError(String, List)} to provide more
 * debugging information.
 */
@Deprecated
public MultipleVisibleElementsError(final String message) {
	super(message);
}

/**
 * Create a multiple visible elements error, including information about the locator being used
 * and how many elements have been found.
 *
 * @param elements List of elements found
 */
public MultipleVisibleElementsError(final List<WebBrowserElement> elements) {
	this("Unexpected multiple elements found.", elements);
}

/**
 * Create a multiple visible elements error, including a specific message, information about the locator being
 * used and how many elements have been found.
 *
 * @param message Specific message
 * @param elements List of elements found
 */
public MultipleVisibleElementsError(final String message, final List<WebBrowserElement> elements) {
	super(message + LINE_SEPARATOR
		+ "			-> element: " + elements.get(0).getLocator() + LINE_SEPARATOR
		+ "			-> # found: " + elements.size());
}

/**
 * Create a multiple visible elements error with a specific message.
 * @deprecated as of 6.0.0; use {@link #MultipleVisibleElementsError(List)}
 * or {@link #MultipleVisibleElementsError(String, List)} to provide more
 * debugging information.
 * TODO If necessary, create a constructor that takes a throwable and additional
 * debugging information
 */
@Deprecated
public MultipleVisibleElementsError(final Throwable ex) {
	super(ex);
}
}
