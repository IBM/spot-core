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
package com.ibm.bear.qa.spot.core.timeout;

import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * Class to manage timeout while waiting that a {@link WebBrowserElement web element}
 * becomes either enabled or disabled.
 * <p>
 * The enabled or disabled state is obtained by getting the <code>disabled</code>
 * attribute value which is supposed to be either <code>null</code> or <code>false</code>
 * when the element is enabled.
 * </p>
 */
public class SpotAttributeEnabledTimeout extends SpotAttributeContainsTimeout {

/**
 * Create a timeout to wait for <code>disabled</b> attribute of given element
 * to become either <code>null</code> or <code>false</code>.
 * <p>
 * Timeout behavior is to fail when it expires without having the expected state.
 * </p>
 * @param webElement The element to test
 */
public SpotAttributeEnabledTimeout(final WebBrowserElement webElement) {
	super(webElement, "disabled", "false", null);
}
}
