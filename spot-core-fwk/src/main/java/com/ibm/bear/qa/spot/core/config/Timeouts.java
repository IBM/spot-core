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
package com.ibm.bear.qa.spot.core.config;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterIntValue;

/**
 * General timeouts used while running a scenario.
 * <p>
 * Current available timeouts are:
 * <ul>
 * <li><code>"timeout"</code>: {@link #DEFAULT_TIMEOUT}</li>
 * <li><code>"timeoutShort"</code>: {@link #SHORT_TIMEOUT}</li>
 * <li><code>"timeoutToOpenPage"</code>: {@link #OPEN_PAGE_TIMEOUT}</li>
 * <li><code>"timeoutCloseDialog"</code>: {@link #CLOSE_DIALOG_TIMEOUT}</li>
 * <li><code>"delayAfterClickLink"</code>: {@link #DELAY_AFTER_CLICK_LINK_TIMEOUT}</li>
 * <li><code>"delayBeforeClickLink"</code>: {@link #DELAY_BEFORE_CLICK_LINK_TIMEOUT}</li>
 * </ul>
 * </p>
 */
abstract public class Timeouts {

	// Constants
	private final static int DEFAULT_MAIN_TIMEOUT = 60;

	/**
	 * Default timeout used all over the framework (e.g. while searching a web element
	 * in a page).
	 * <p>
	 * The value is 45 seconds.
	 * </p>
	 */
	static final public int DEFAULT_TIMEOUT = getParameterIntValue("timeout", DEFAULT_MAIN_TIMEOUT);

	/**
	 * Short timeout used while searching web element.
	 * <p>
	 * The value is 10 seconds.
	 * </p>
	 */
	static final public int SHORT_TIMEOUT = getParameterIntValue("timeoutShort", 10);

	/**
	 * Timeout used to wait for a page to be opened.
	 * <p>
	 * The value is 20 seconds.
	 * </p>
	 */
	static final public int OPEN_PAGE_TIMEOUT = getParameterIntValue("timeoutToOpenPage", 30);

	/**
	 * Timeout used to wait for a dialog to be closed.
	 * <p>
	 * The value is the same than {@link #OPEN_PAGE_TIMEOUT}.
	 * </p>
	 */
	static final public int CLOSE_DIALOG_TIMEOUT = getParameterIntValue("timeoutCloseDialog", OPEN_PAGE_TIMEOUT);

	/**
	 * Timeout used to a delay after having clicked on a link element.
	 * <p>
	 * Expressed in milli-seconds, default value is 500.
	 * </p>
	 */
	final public static int DELAY_AFTER_CLICK_LINK_TIMEOUT = getParameterIntValue("delayAfterClickLink", 500);

	/**
	 * Timeout used to a delay before clicking on a link element.
	 * <p>
	 * Expressed in milli-seconds, default value is 500.
	 * </p>
	 */
	final public static int DELAY_BEFORE_CLICK_LINK_TIMEOUT = getParameterIntValue("delayBeforeClickLink", 500);
}