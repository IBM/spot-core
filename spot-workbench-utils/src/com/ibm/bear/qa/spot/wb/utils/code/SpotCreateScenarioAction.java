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

import java.io.*;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.osgi.framework.Bundle;

import com.ibm.bear.qa.spot.wb.utils.FileUtil;
import com.ibm.bear.qa.spot.wb.utils.actions.AbstractAction;

public class SpotCreateScenarioAction extends AbstractAction {

	/* Constants */

	/* Fields */
	// Scenario info
	String scenarioPrefix, scenarioName, packagePath;
	// Java model
	IJavaProject project;
	// Eclipse model
	IFolder rootFolder;

public SpotCreateScenarioAction() {
}

/*
 * Init file directory.
 *
 * TODO The preferences are not stored when exiting the workspace session,
 * they should be and restored when restarting the same workspace session...
 * Best would be to have a preferences page.
 */
@SuppressWarnings("all")
protected boolean initValues() {

	// Enter the scenario prefix
	InputDialog scenarioPrefixInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the scenario classes prefix (valid Java class characters):\n(note that 'Scenario' suffix will be added to this prefix when creating classes)\nExample: TestGeneration", "", null);
	int enteredScenarioPrefix = scenarioPrefixInputDialog.open();
	if (enteredScenarioPrefix == Window.CANCEL) return false;
	this.scenarioPrefix = scenarioPrefixInputDialog.getValue();

	// Enter the scenario description
	InputDialog scenarioNameInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the scenario description:\n(keep it short because it's displayed in JUnit view)\nExample: Test Scenario Generation", "", null);
	int enteredScenarioName = scenarioNameInputDialog.open();
	if (enteredScenarioName == Window.CANCEL) return false;
	this.scenarioName = scenarioNameInputDialog.getValue();

	// Enter the package path
	InputDialog packagePathInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the scenario package path:\n (will be relative to package src/test/java/com/ibm/bear/qa/spot, use / for separator)\nExample: test/gen", "", null);
	int enteredPackagePath = packagePathInputDialog.open();
	if (enteredPackagePath == Window.CANCEL) return false;
	this.packagePath = packagePathInputDialog.getValue();
	if (this.packagePath.isEmpty()) {
		MessageDialog.openError(getShell(), "SPOT Utils", "Package cannot be empty");
		return false;
	}

	// Init OK
	return true;
}

@Override
public void run(final IAction arg0) {

	// Init values
	if (!initValues()) {
		return;
	}

	// Create folders
	this.project = (IJavaProject) getSelectedObject();
	try {
		this.rootFolder = getPackageFolder("src/test/java/com/ibm/bear/qa/spot/"+this.packagePath);
	} catch (Exception ex) {
		ex.printStackTrace();
		return;
	}

	// Create files
	try {
		createFiles();
	} catch (Exception ex) {
		ex.printStackTrace();
		return;
	}
}

private void createFiles() throws Exception {
	Bundle bundle = Platform.getBundle("spot-workbench-utils");
	createUnits(bundle, "scenario");
	createUnits(bundle, "steps");
}

private void createUnits(final Bundle bundle, final String folderName) throws Exception {
	Enumeration<String> filePaths = bundle.getEntryPaths("/data/template/"+folderName);
	while (filePaths.hasMoreElements()) {
		String filePath = filePaths.nextElement();
		URL fileUrl = FileLocator.toFileURL(bundle.getEntry(filePath));
		File file = new File(fileUrl.toURI());
		if (!file.exists()) {
			System.out.println("ERROR: Cannot create units because template file '"+filePath+"' is not accessible!!!");
			System.out.println(" - file URL is: "+fileUrl);
			System.out.println(" - file is: "+file);
			return;
		}
		IFolder packageFolder = this.rootFolder.getFolder(folderName);
		if (!packageFolder.exists()) {
			packageFolder.create(true, true, null);
		}
		createCompilationUnit(file, packageFolder.getFile(file.getName().replace("Template", this.scenarioPrefix)));
	}
}

private ICompilationUnit createCompilationUnit(final File file, final IFile ifile) throws Exception {
	String fileContent = FileUtil.readFileContent(file);
	String newContent = fileContent.replaceAll(".template.", "."+this.packagePath.replace("/",  ".")+".");
	newContent = newContent.replaceAll("Template", this.scenarioPrefix);
	newContent = newContent.replaceAll("Scenario name", this.scenarioName);
	try (InputStream inputStream = new ByteArrayInputStream(newContent.getBytes())) {
		ifile.create(inputStream, true, null);
		return JavaCore.createCompilationUnitFrom(ifile);
	}
}

private IFolder getPackageFolder(final String folderPath) throws Exception {
	String[] segments = folderPath.split("/");
	IFolder folder = null;
	for (String segment: segments) {
		if (folder == null) {
			folder = this.project.getProject().getFolder(segment);
		} else {
			folder = folder.getFolder(segment);
		}
		if (!folder.exists()) {
			folder.create(true, true, null);
		}
	}
	return folder;
}
}
