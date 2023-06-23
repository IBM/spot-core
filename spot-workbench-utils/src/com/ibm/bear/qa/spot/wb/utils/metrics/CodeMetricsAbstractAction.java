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
package com.ibm.bear.qa.spot.wb.utils.metrics;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.ibm.bear.qa.spot.wb.utils.Activator;
import com.ibm.bear.qa.spot.wb.utils.actions.AbstractAction;

/**
 * Action to generate metric results on selected projects.
 */
public abstract class CodeMetricsAbstractAction extends AbstractAction {

	/* Constants */
	static final String DEFAULT_METRICS_DIR = "D:/Work/SPOT/metrics";
	static final String METRICS_DIR_PREF = "metrics.dir";
	static final String DEFAULT_SPOT_VERSION_NAME = "SPOT 2.0.0";
	static final String SPOT_VERSION_NAME_PREF = "spot.version.name";
	static final String DEFAULT_SPOT_VERSION_DATE = CodeMetricsResults.CURRENT_DATE;
	static final String SPOT_VERSION_DATE_PREF = "spot.version.date";

	/* Fields */
	protected String filterDir;
	protected String versionName, versionDate;

/*
 * Init file directory.
 *
 * TODO The preferences are not stored when exiting the workspace session,
 * they should be and restored when restarting the same workspace session...
 * Best would be to have a preferences page.
 */
@SuppressWarnings("all")
protected boolean initDir() {

	// Get preferences
	if (this.filterDir == null) {
		IEclipsePreferences preferences = new InstanceScope().getNode(Activator.PLUGIN_ID);
		this.filterDir = preferences.get(METRICS_DIR_PREF, DEFAULT_METRICS_DIR);
	}

	// Enter directory for metrics output
	DirectoryDialog dirDialog = new DirectoryDialog(getShell());
	dirDialog.setFilterPath(this.filterDir);
	dirDialog.setText("Directory selection dialog");
	dirDialog.setMessage("Select a directory to put metrics results files");
	String dirPath = dirDialog.open();
	if (dirPath == null) return false;
	this.filterDir = dirPath;

	// Store preferences
	IEclipsePreferences preferences = new InstanceScope().getNode(Activator.PLUGIN_ID);
	preferences.put(METRICS_DIR_PREF, this.filterDir);

	// Init OK
	return true;
}

/*
 * Init version and date names.
 *
 * TODO Have a preferences page.
 */
@SuppressWarnings("all")
protected boolean initVersionValues() {
	// Get preferences
	if (this.versionName == null) {
		IEclipsePreferences preferences = new InstanceScope().getNode(Activator.PLUGIN_ID);
		this.versionName = preferences.get(SPOT_VERSION_NAME_PREF, DEFAULT_SPOT_VERSION_NAME);
		this.versionDate = preferences.get(SPOT_VERSION_DATE_PREF, DEFAULT_SPOT_VERSION_DATE);
	}

	// Enter the version name
	InputDialog versionNameInputDialog = new InputDialog(getShell(), "SPOT Metrics", "Enter the CLM versionName", this.versionName, null);
	int validName = versionNameInputDialog.open();
	if (validName == Window.CANCEL) return false;
	this.versionName = versionNameInputDialog.getValue();

	// Enter the version date
	InputDialog versionDateInputDialog = new InputDialog(getShell(), "SPOT Metrics", "Enter the CLM version date", this.versionDate, null);
	int validDate = versionDateInputDialog.open();
	if (validDate == Window.CANCEL) return false;
	this.versionDate = versionDateInputDialog.getValue();

	// Store preferences
	IEclipsePreferences preferences = new InstanceScope().getNode(Activator.PLUGIN_ID);
	preferences.put(SPOT_VERSION_NAME_PREF, this.versionName);
	preferences.put(SPOT_VERSION_DATE_PREF, this.versionDate);

	// Init OK
	return true;
}
}