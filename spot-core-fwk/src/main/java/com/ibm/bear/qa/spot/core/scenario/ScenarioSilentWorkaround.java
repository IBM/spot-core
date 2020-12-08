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
package com.ibm.bear.qa.spot.core.scenario;

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Manage a workaround applied when running a scenario.
 * <p>
 * Unlike {@link ScenarioWorkaround}, the application of the workaround will be kept
 * in silence. In other words, neither a warning snapshot will be created nor a
 * message will be displayed in the console to as a part of this workaround.
 * </p>
 */
public abstract class ScenarioSilentWorkaround<P extends WebPage> extends ScenarioWorkaround<P> {

public ScenarioSilentWorkaround(final P page, final String msg) {
	this(page, msg, true, null);
}

public ScenarioSilentWorkaround(final P page, final String msg, final boolean fail) {
	this(page, msg, fail, null);
}

public ScenarioSilentWorkaround(final P page, final String msg, final boolean fail, final SpotAbstractDialog dialog) {
	super(page, msg, fail, dialog, false /* report */);
}

}
