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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.osgi.framework.Bundle;

import com.ibm.bear.qa.spot.wb.utils.FileUtil;
import com.ibm.bear.qa.spot.wb.utils.actions.AbstractAction;

public abstract class SpotAbstractCreateAction extends AbstractAction {

	enum ProjectType {
		Pages("main"), Scenarios("test");
		final String sourceFolder;
		ProjectType(final String folder) {
			this.sourceFolder = folder;
		}
		String getLabel() {
			return toString().toLowerCase();
		}
		String getProjectName(final String project) {
			return "spot-"+project+"-"+getLabel();
		}
	}

	/* Constants */

	/* Fields */
	// Generic info
	String projectShortName, projectVersion, projectTitle, packagePath, classPrefix;
	// Java model
	IJavaProject pagesProject, scenariosProject;
	// Eclipse model
	IFolder pagesRootFolder, scenariosRootFolder;
	// Bundle
	final Bundle bundle = Platform.getBundle("spot-workbench-utils");
	// Flag to request version number
	final boolean needVersion;

public SpotAbstractCreateAction(final boolean version) {
		this.needVersion = version;
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

	// Enter the projects short name
	InputDialog projectShortNameInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the project short name:\n(note use to for created projects name 'spot-<short name>-pages' 'spot-<short name>-scenarios')\nExample: testgen", "", null);
	int enteredName = projectShortNameInputDialog.open();
	if (enteredName == Window.CANCEL) return false;
	this.projectShortName = projectShortNameInputDialog.getValue();

	// Enter the project version
	if (this.needVersion) {
		InputDialog projectVersionInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the project version:\nExample: 5.1.0-SNAPSHOT", "", null);
		int enteredVersion = projectVersionInputDialog.open();
		if (enteredVersion == Window.CANCEL) return false;
		this.projectVersion = projectVersionInputDialog.getValue();
	}

	// Enter the project title
	InputDialog projectTitleInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the project title:\nExample: Test Generation", "", null);
	int enteredTitle = projectTitleInputDialog.open();
	if (enteredTitle == Window.CANCEL) return false;
	this.projectTitle = projectTitleInputDialog.getValue();

	// Enter the class prefix
	InputDialog classPrefixInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the classes prefix (valid Java class characters):\n(note that for scenarios project, 'Scenario' suffix will be added to this prefix when creating classes)\nExample: TestGen", "", null);
	int enteredPrefix = classPrefixInputDialog.open();
	if (enteredPrefix == Window.CANCEL) return false;
	this.classPrefix = classPrefixInputDialog.getValue();

	// Enter the package path
	InputDialog packagePathInputDialog = new InputDialog(getShell(), "SPOT Utils", "Enter the projects package path:\n (will be relative to package src/test/java/com/ibm/bear/qa/spot, use / for separator)\nExample: test/gen", "", null);
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

IFile createFile(final String filePath, final IFile ifile) throws Exception {
	URL fileUrl = FileLocator.toFileURL(this.bundle.getEntry(filePath));
	File file = new File(fileUrl.toURI());
	if (!file.exists()) {
		throw new RuntimeException("ERROR: Cannot create units because template file '"+filePath+"' is not accessible!!!");
	}
	String fileContent = readFileContent(file);
	try (InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes())) {
		ifile.create(inputStream, true, null);
	}
	return ifile;
}

private String readFileContent(final File file) throws IOException {
	String fileContent = FileUtil.readFileContent(file);
	fileContent = fileContent.replaceAll("%title%", this.projectTitle);
	fileContent = fileContent.replaceAll("Template", this.classPrefix);
	if (this.projectShortName != null) {
		fileContent = fileContent.replaceAll("shortname", this.projectShortName);
		fileContent = fileContent.replaceAll("template\\.url", this.projectShortName.toLowerCase()+".url");
	}
	if (this.projectVersion != null) {
		fileContent = fileContent.replaceAll("5.1.0-SNAPSHOT", this.projectVersion);
	}
	fileContent = fileContent.replaceAll("_topology", "topology");
	fileContent = fileContent.replaceAll("\\.template\\.", "."+this.packagePath.replace("/",  ".")+".");
	return fileContent;
}

void createUnits() {
	createUnits(ProjectType.Pages);
	createUnits(ProjectType.Scenarios);
}

void createUnits(final ProjectType projectType) {
	try {
		Enumeration<String> filePaths = this.bundle.getEntryPaths("/data/template/code/"+projectType.getLabel());
		while (filePaths.hasMoreElements()) {
			String filePath = filePaths.nextElement();
			IFolder rootFolder = projectType == ProjectType.Pages ? this.pagesRootFolder : this.scenariosRootFolder;
			createUnits(rootFolder, filePath);
		}
	}
	catch (Exception ex) {
		ex.printStackTrace();
		MessageDialog.openError(getShell(), "SPOT Utils", "Exception occurred while creating units of project '"+projectType.getProjectName(this.projectShortName)+"':\n"+ex.getMessage());
		return;
	}
}

void createUnits(final IFolder rootFolder, final String dirPath) throws Exception {
	String[] fileSegments= dirPath.split("/");
	IFolder packageFolder = rootFolder.getFolder(fileSegments[fileSegments.length-1]);
	if (!packageFolder.exists()) {
		packageFolder.create(true, true, null);
	}
	Enumeration<String> filePaths = this.bundle.getEntryPaths(dirPath);
	while (filePaths.hasMoreElements()) {
		File file = getFileFromPath(filePaths.nextElement());
		IFile ifile = packageFolder.getFile(file.getName().replace("Template", this.classPrefix));
		createCompilationUnit(file, ifile);
	}
}

private File getFileFromPath(final String filePath) throws IOException, URISyntaxException {
	URL fileUrl = FileLocator.toFileURL(this.bundle.getEntry(filePath));
	File file = new File(fileUrl.toURI());
	if (!file.exists()) {
		throw new RuntimeException("ERROR: Cannot create units because template file '"+filePath+"' is not accessible!!!");
	}
	return file;
}

private ICompilationUnit createCompilationUnit(final File file, final IFile ifile) throws Exception {
	String fileContent = readFileContent(file);
	try (InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes())) {
		ifile.create(inputStream, true, null);
		return JavaCore.createCompilationUnitFrom(ifile);
	}
}

void setPackageRootFolder(final IProject project, final ProjectType projectType) throws Exception {
	String folderPath = "src/"+projectType.sourceFolder+"/java/com/ibm/bear/qa/spot/"+this.packagePath;
	String[] segments = folderPath.split("/");
	IFolder folder = null;
	for (String segment: segments) {
		if (folder == null) {
			folder = project.getProject().getFolder(segment);
		} else {
			folder = folder.getFolder(segment);
		}
		if (!folder.exists()) {
			folder.create(true, true, null);
		}
	}
	switch (projectType) {
		case Pages:
			this.pagesRootFolder = folder;
			break;
		case Scenarios:
			this.scenariosRootFolder = folder;
			break;
	}
}
}
