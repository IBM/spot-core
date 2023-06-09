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
package com.ibm.bear.qa.spot.core.nls;

/**
 * Class to manage NLS messages used in SPOT Core framework.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getDialogApplyButtonLabel()}: Return the dialog <b>Apply</b> button label.</li>
 * <li>{@link #getDialogCancelButtonLabel()}: Return the dialog <b>Cancel</b> button label.</li>
 * <li>{@link #getDialogCreateButtonLabel()}: Return the dialog <b>Create</b> button label.</li>
 * <li>{@link #getDialogSaveButtonLabel()}: Return the dialog <b>Save</b> button label.</li>
 * </ul>
 * </p>
 * <p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #bundleName()}: The bundle name of the scenario messages.</li>
 * </ul>
 * </p>
 */
public class SpotNlsMessages extends NlsMessages implements SpotNlsMessageKeys {

	/* Constants */
	private final static String BUNDLE_NAME = "com.ibm.bear.qa.spot.core.nls.messages"; //$NON-NLS-1$

@Override
protected String bundleName() {
	return BUNDLE_NAME;
}

/**
 * Return the dialog <b>Apply</b> button label.
 * <p>
 * Note that this description is NLS compatible and will depend on the Locale selected when running the scenario.
 * </p>
 *
 * @return The corresponding NLS string
 */
public String getDialogApplyButtonLabel() {
	return getNLSString(SPOT_DIALOG_APPLY_LABEL);
}

/**
 * Return the dialog <b>Cancel</b> button label.
 * <p>
 * Note that this description is NLS compatible and will depend on the Locale selected when running the scenario.
 * </p>
 *
 * @return The corresponding NLS string
 */
public String getDialogCancelButtonLabel() {
	return getNLSString(SPOT_DIALOG_CANCEL_LABEL);
}

/**
 * Return the dialog <b>Create</b> button label.
 * <p>
 * Note that this description is NLS compatible and will depend on the Locale selected when running the scenario.
 * </p>
 *
 * @return The corresponding NLS string
 */
public String getDialogCreateButtonLabel() {
	return getNLSString(SPOT_DIALOG_CREATE_LABEL);
}

/**
 * Return the dialog <b>Ok</b> button label.
 * <p>
 * Note that this description is NLS compatible and will depend on the Locale selected when running the scenario.
 * </p>
 *
 * @return The corresponding NLS string
 */
public String getDialogOkButtonLabel() {
	return getNLSString(SPOT_DIALOG_OK_LABEL);
}

/**
 * Return the dialog <b>Save</b> button label.
 * <p>
 * Note that this description is NLS compatible and will depend on the Locale selected when running the scenario.
 * </p>
 *
 * @return The corresponding NLS string
 */
public String getDialogSaveButtonLabel() {
	return getNLSString(SPOT_DIALOG_SAVE_LABEL);
}
}
