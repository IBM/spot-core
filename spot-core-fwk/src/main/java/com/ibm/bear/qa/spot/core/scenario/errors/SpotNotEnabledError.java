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

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;
import com.ibm.bear.qa.spot.core.web.WebBrowser;

/**
 * Error raised when an action is attempted on a disabled element.
 * <p>
 * See an example in
 * {@link WebBrowser#clickButton(com.ibm.bear.qa.spot.core.web.WebBrowserElement,int,boolean)}
 * method.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #setDialog(SpotAbstractDialog)}: Set the dialog associated with the current error.</li>
 * </ul>
 * </p>
 */
public class SpotNotEnabledError extends ScenarioFailedError {

public SpotNotEnabledError(final String message) {
	super(message);
}

public SpotNotEnabledError(final String message, final boolean print) {
	super(message, print);
}

public SpotNotEnabledError(final String message, final SpotAbstractDialog dialog) {
	super(message, dialog);
}

public SpotNotEnabledError(final String message, final SpotAbstractDialog dialog, final boolean print) {
	super(message, dialog, print);
}

public SpotNotEnabledError(final Throwable ex) {
	super(ex);
}

public SpotNotEnabledError(final Throwable ex, final boolean print) {
	super(ex, print);
}

public SpotNotEnabledError(final Throwable ex, final SpotAbstractDialog dialog) {
	super(ex, dialog);
}

public SpotNotEnabledError(final Throwable ex, final SpotAbstractDialog dialog, final boolean print) {
	super(ex, dialog, print);
}

/**
 * Set the dialog associated with the current error.
 */
public void setDialog(final SpotAbstractDialog dialog) {
	this.dialog = dialog;
}

}
