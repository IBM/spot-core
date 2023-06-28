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

import java.io.File;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

public class SpotCreateNewProjectsAction extends SpotAbstractCreateAction {

	/* Fields */
	// Scenario folder
	IFolder spotFwkFolder, spotScnFolder;

public SpotCreateNewProjectsAction() {
		super(true);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject wProject: workspaceRoot.getProjects()) {
			switch (wProject.getName()) {
				case "spot-pages":
					this.spotFwkFolder = wProject.getFolder("spot-fwk");
					break;
				case "spot":
					this.spotScnFolder = wProject.getFolder("spot-scn");
					break;
			}
		}
		/*
		if (this.spotFwkFolder == null) {
			MessageDialog.openWarning(getShell(), "SPOT Utils", "spot pages hierarchy was not found in workspace, hence projects will be generated in workspace instead of git repo tree.");
		}
		if (this.spotScnFolder == null) {
			MessageDialog.openWarning(getShell(), "SPOT Utils", "spot scenarios hierarchy was not found in workspace, hence projects will be generated in workspace instead of git repo tree.");
		}
		*/
	}

/*
 * Init file directory.
 *
 * TODO The preferences are not stored when exiting the workspace session,
 * they should be and restored when restarting the same workspace session...
 * Best would be to have a preferences page.
 */
@Override
@SuppressWarnings("all")
protected boolean initValues() {

	// Enter other informations
	return super.initValues();
}

@Override
public void run(final IAction arg0) {

	// Init values
	if (!initValues()) {
		return;
	}

	// Create folders
	if (!createProjects()) {
		return;
	}

	// Create files
	createUnits();
}

private boolean createProjects() {
	return createProject(ProjectType.Pages) && createProject(ProjectType.Scenarios);

}
private boolean createProject(final ProjectType projectType) {

	// Create Eclipse Java project
	String projectName = projectType.getProjectName(this.projectShortName);
	try {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject(projectName);
		if (project.exists()) {
			project.delete(true, null);
		}
		IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
		projectDescription.setNatureIds(new String[] { JavaCore.NATURE_ID, "org.eclipse.m2e.core.maven2Nature" });
		IFolder parentFolder = projectType == ProjectType.Pages ? this.spotFwkFolder : this.spotScnFolder;
		if (parentFolder != null) {
			IFolder newProjectFolder = parentFolder.getFolder(projectName);
			File newProjectFile = new File(newProjectFolder.getLocationURI());
			projectDescription.setLocation(new Path(newProjectFile.getAbsolutePath()));
		}
		ICommand[] buildSpec = new ICommand[2];
		buildSpec[0] = projectDescription.newCommand();
		buildSpec[0].setBuilderName(JavaCore.BUILDER_ID);
		buildSpec[1] = projectDescription.newCommand();
		buildSpec[1].setBuilderName("org.eclipse.m2e.core.maven2Builder");
		projectDescription.setBuildSpec(buildSpec);
		project.create(projectDescription, null);
		project.open(null);
//		IProjectDescription description = project.getDescription();
//		project.setDescription(description, null);
		createFile("/data/template/.classpath", project.getFile(".classpath"));
		createFile("/data/template/"+projectType.getLabel()+"/pom.xml", project.getFile("pom.xml"));
		if (projectType == ProjectType.Scenarios) {
			IFolder launchesFolder = project.getFolder("launches");
			launchesFolder.create(true, true, null);
			IFile launchFile = createFile("/data/template/launches/TemplateScenario_(chrome).launch", launchesFolder.getFile("TemplateScenario_(chrome).launch"));
			String newLaunchFilePath = launchFile.getFullPath().toString().replaceAll("TemplateScenario_", this.classPrefix+"Scenario ");
			launchFile.move(new Path(newLaunchFilePath), true, null);
			System.out.println("Launch file path: "+launchFile.getFullPath());
			IFolder paramsFolder = project.getFolder("params");
			paramsFolder.create(true, true, null);
			createFile("/data/template/params/.gitignore", paramsFolder.getFile(".gitignore"));
			createFile("/data/template/params/common.properties", paramsFolder.getFile("common.properties"));
			createFile("/data/template/params/topology.properties", paramsFolder.getFile("topology.properties"));
		}
		JavaCore.create(project);
		setPackageRootFolder(project, projectType);
	} catch (Exception ex) {
		ex.printStackTrace();
		MessageDialog.openError(getShell(), "SPOT Utils", "Exception occurred while creating project '"+projectName+"':\n"+ex.getMessage());
		return false;
	}
	return true;
}
}
