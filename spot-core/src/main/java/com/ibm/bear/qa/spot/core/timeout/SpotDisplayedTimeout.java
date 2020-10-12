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
 * gets either displayed or hidden.
 * <p>
 * For this class the condition is:
 * <pre>
 * this.element.isDisplayed(false);
 * </pre>
 * </p><p>
 * This class overrides the two following abstract methods:
 * <ul>
 * <li>{@link #getCondition()}: Return the result of the condition evaluation.</li>
 * <li>{@link #getConditionLabel()}: Return the text explaining the condition.</li>
 * </ul>
 * </p>
 */
public class SpotDisplayedTimeout extends SpotAbstractTimeout {

public SpotDisplayedTimeout(final WebBrowserElement webElement, final boolean fail) {
	super(webElement, fail);
}

@Override
protected boolean getCondition() {
	return this.element.isDisplayed(/* recovery: */false);
}

@Override
protected String getConditionLabel() {
	return "Element is displayed";
}
}
