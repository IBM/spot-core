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
package com.ibm.bear.qa.spot.core.web;

import org.openqa.selenium.By;

/**
 * Abstract class to manage web page content areas.
 * <p>
 * No specific feature implemented at the basic framework level.
 * </p>
 */
public abstract class WebContentArea extends WebElementWrapper {

public WebContentArea(final WebElementWrapper parent, final By selectBy) {
	super(parent, selectBy);
}

public WebContentArea(final WebElementWrapper parent, final WebBrowserElement element, final WebBrowserFrame frame) {
	super(parent, element, frame);
}

public WebContentArea(final WebElementWrapper parent, final WebBrowserElement element) {
	super(parent, element);
}

public WebContentArea(final WebPage page, final By findBy, final WebBrowserFrame frame) {
	super(page, findBy, frame);
}

public WebContentArea(final WebPage page, final By findBy) {
	super(page, findBy);
}

public WebContentArea(final WebPage page, final WebBrowserElement element, final WebBrowserFrame frame) {
	super(page, element, frame);
}

public WebContentArea(final WebPage page, final WebBrowserElement element) {
	super(page, element);
}

public WebContentArea(final WebPage page, final WebBrowserFrame frame) {
	super(page, frame);
}

public WebContentArea(final WebPage page) {
	super(page);
}
}
