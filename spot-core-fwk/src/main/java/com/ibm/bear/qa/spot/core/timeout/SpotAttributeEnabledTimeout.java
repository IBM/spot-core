/*
* Licensed Materials - Property of IBM
* 5725-B69 5655-Y17 5655-Y31 5724-X98 5724-Y15 5655-V82
* Copyright IBM Corp. 1987, 2021. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/
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
