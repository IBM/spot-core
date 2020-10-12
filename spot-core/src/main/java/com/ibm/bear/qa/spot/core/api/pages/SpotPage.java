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
package com.ibm.bear.qa.spot.core.api.pages;

import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * Interface to define framework web page object API.
 * <p>
 * Available API methods on such object are:
 * <ul>
 * <li>{@link #getLocation()}: Return the page location used when creating the object.</li>
 * <li></li>
 * </p>
 */
public interface SpotPage {

/**
 * Return the page location used when creating it.
 * <p>
 * Note that it can be slightly different from the browser URL.
 * </p>
 * @return The page location
 */
String getLocation();

/**
 * Return the page title.
 *
 * @return The page title
 */
String getTitle();

/**
 * Refresh the page content using {@link WebBrowser#refresh()} and wait for
 * the page to be loaded.
 */
void refresh();
}
