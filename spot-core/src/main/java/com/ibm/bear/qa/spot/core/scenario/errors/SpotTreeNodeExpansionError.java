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
package com.ibm.bear.qa.spot.core.scenario.errors;

import com.ibm.bear.qa.spot.core.dialog.SpotAbstractDialog;

/**
 * Error raised when there's an error while expanding framework tree node.
 */
public class SpotTreeNodeExpansionError extends RetryableError {

public SpotTreeNodeExpansionError(final String message) {
	super(message);
}

public SpotTreeNodeExpansionError(final String message, final boolean print) {
	super(message, print);
}

public SpotTreeNodeExpansionError(final String message, final SpotAbstractDialog dialog) {
	super(message, dialog);
}

public SpotTreeNodeExpansionError(final String message, final SpotAbstractDialog dialog, final boolean print) {
	super(message, dialog, print);
}

public SpotTreeNodeExpansionError(final Throwable ex) {
	super(ex);
}

public SpotTreeNodeExpansionError(final Throwable ex, final boolean print) {
	super(ex, print);
}

public SpotTreeNodeExpansionError(final Throwable ex, final SpotAbstractDialog dialog) {
	super(ex, dialog);
}

public SpotTreeNodeExpansionError(final Throwable ex, final SpotAbstractDialog dialog, final boolean print) {
	super(ex, dialog, print);
}

}
