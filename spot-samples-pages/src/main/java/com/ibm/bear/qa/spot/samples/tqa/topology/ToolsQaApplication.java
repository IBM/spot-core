/*********************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.samples.tqa.topology;

import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.topology.Application;
import com.ibm.bear.qa.spot.core.web.SpotAbstractLoginOperation;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Class to manage the <b>ToolsQA</b> web application.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getLoginOperation(WebPage,User)}: Return the login operation which should be used with the current application.</li>
 * </ul>
 * </p>
 */
public class ToolsQaApplication extends Application {

public ToolsQaApplication(final String url) {
	super(url);
}

/**
 * {@inheritDoc}
 *
 * There's no login to Tools QA page, hence return always <code>null</code>
 */
@Override
public SpotAbstractLoginOperation getLoginOperation(final WebPage page, final User appUser) {
	return null;
}
}
