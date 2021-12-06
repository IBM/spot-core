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

import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * Class to manage timeout while waiting that the {@link WebBrowserElement web element}
 * gets enabled or not.
 * <p>
 * For this class the condition is:
 * <pre>
 * this.element.isEnabled(false);
 * </pre>
 * </p><p>
 * This class overrides the two following abstract methods:
 * <ul>
 * <li>{@link #getCondition()}: Return the result of the condition evaluation.</li>
 * <li>{@link #getConditionLabel()}: Return the text explaining the condition.</li>
 * </ul>
 * </p>
 */
public class SpotEnabledTimeout extends SpotAbstractTimeout {

/**
 * Create a timeout to wait for given element to be enabled/disabled.
 * <p>
 * Timeout behavior is to fail when it expires without having the expected state.
 * </p>
 * @param webElement The element to test
 */
public SpotEnabledTimeout(final WebBrowserElement webElement) {
	super(webElement, true);
}
/**
 * Create a timeout to wait for given element to be enabled/disabled.
 * <p>
 * Timeout behavior when it expires without having the expected state depends
 * on given <b>fail</b> argument.
 * </p>
 * @param webElement The element to test
 * @param fail Tells whether to raise an error if condition fails
 */
public SpotEnabledTimeout(final WebBrowserElement webElement, final boolean fail) {
	super(webElement, fail);
}

@Override
protected boolean getCondition() {
	return this.element.isEnabled(/*recovery:*/false);
}

@Override
protected String getConditionLabel() {
	return "Element is enabled";
}

}
