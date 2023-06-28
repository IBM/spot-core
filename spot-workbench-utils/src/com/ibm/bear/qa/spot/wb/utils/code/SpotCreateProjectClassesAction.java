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
package com.ibm.bear.qa.spot.wb.utils.code;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

public class SpotCreateProjectClassesAction extends SpotAbstractCreateAction {
	/* Constants */

	/* Fields */
	// Project type
	ProjectType projectType;

public SpotCreateProjectClassesAction() {
	super(false);
}

@Override
public void run(final IAction arg0) {

	// Init values
	if (!initValues()) {
		return;
	}

	// Create package root folder
	if (!createPackageRootFolder()) {
		return;
	}

	// Create files
	createUnits(this.projectType);
}

private boolean createPackageRootFolder() {
	IProject project = ((IJavaProject) getSelectedObject()).getProject();
	String projectName = project.getName();
	if (projectName.endsWith("-pages")) {
		this.projectType = ProjectType.Pages;
	}
	else if (projectName.endsWith("-scenarios")) {
		this.projectType = ProjectType.Scenarios;
	} else {
		MessageDialog.openError(getShell(), "SPOT Utils", "Cannot create SPOT classes for project "+projectName+"!\nExpecting either a pages project ('spot-*-pages') or a scenarios project ('spot-*-scenarios')...");
	}
	try {
		setPackageRootFolder(project, this.projectType);
	} catch (Exception ex) {
		ex.printStackTrace();
		MessageDialog.openError(getShell(), "SPOT Utils", "Exception occurred while creating project folders :\n"+ex.getMessage());
		return false;
	}
	return true;
}
}
