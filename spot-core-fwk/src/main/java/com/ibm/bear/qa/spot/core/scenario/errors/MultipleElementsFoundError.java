/*********************************************************************
* Copyright (c) 2012, 2023 IBM Corporation and others.
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

/**
 * Class to distinguish error when several elements are found although only one was expected.
 */
public class MultipleElementsFoundError extends ScenarioFailedError {

/**
 * Create a multiple visible elements error, including information about the locator being used
 * and how many elements have been found.
 *
 * @param elements List of elements found
 */
public MultipleElementsFoundError(final List<WebBrowserElement> elements) {
	this("Unexpected multiple elements found.", elements);
}

/**
 * Create a multiple visible elements error, including a specific message, information about the locator being
 * used and how many elements have been found.
 *
 * @param message Specific message
 * @param elements List of elements found
 */
public MultipleElementsFoundError(final String message, final List<WebBrowserElement> elements) {
	super(message + LINE_SEPARATOR
		+ "			-> element: " + elements.get(0).getLocator() + LINE_SEPARATOR
		+ "			-> # found: " + elements.size());
}
}
