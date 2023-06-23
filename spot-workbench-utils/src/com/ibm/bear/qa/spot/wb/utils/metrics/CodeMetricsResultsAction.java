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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.bear.qa.spot.wb.utils.FileUtil;

/**
 * Action to generate metric results on selected projects.
 */
public class CodeMetricsResultsAction extends CodeMetricsAbstractAction {

	/* Fields */
	List<String> resultFiles = new ArrayList<String>();

@Override
public void run(final IAction action) {

	// Init values
	if (!initDir()) {
		return;
	}

	try {

		// Get all units for selected projects
		final File dir = new File(this.filterDir);
		final Map<IJavaProject, List<ICompilationUnit>> mapUnits = getProjectsUnitsMap(false);

		// For each selected projects
		this.resultFiles.clear();
		for (final IJavaProject javaProject: mapUnits.keySet()) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// Get all project units
						List<ICompilationUnit> units = mapUnits.get(javaProject);
						String projectName = javaProject.getElementName();
						monitor.beginTask("Computing Metrics for "+projectName, units.size());

						// Set version
						String version = CodeMetricsResultsAction.this.getProjectVersion(javaProject);

						// Get metrics for each unit
						CodeMetricsResults metricResults = new CodeMetricsResults(projectName);
						for (ICompilationUnit unit: units) {
							monitor.subTask("Scanning "+unit.getElementName()+"...");
							CodeMetricsResultsVisitor visitor = new CodeMetricsResultsVisitor(unit);
							@SuppressWarnings("synthetic-access")
							CompilationUnit astRoot = createCompilationUnitAST(unit);
							astRoot.accept(visitor);
							metricResults.addCompilationUnit(visitor);
							if (monitor.isCanceled()) {
								return;
							}
							monitor.worked(1);
						}
						monitor.done();

						// Write result
						File resultFile = metricResults.writeResult(dir, version);
						if (resultFile != null) {
							if (!CodeMetricsResultsAction.this.resultFiles.contains(resultFile.getAbsolutePath())) {
								CodeMetricsResultsAction.this.resultFiles.add(resultFile.getAbsolutePath());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			ProgressMonitorDialog progress = new ProgressMonitorDialog(this.shell);
			try {
				progress.run(true, true, runnable);
			} catch (@SuppressWarnings("unused") Exception e) {
				// skip
			}
//			MessageDialog.openInformation(getShell(), "SPOT Metrics", "Metrics for plugin "+javaProject.getElementName()+" has been added to file "+this.resultFile.getAbsolutePath());
//			System.out.println("Metrics for plugin "+javaProject.getElementName()+" has been added to file "+this.resultFile.getAbsolutePath());
		}
	} catch (JavaModelException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	StringBuilder builder = new StringBuilder("Following metrics files have been written:").append(LINE_SEPARATOR);
	for (String filePath: this.resultFiles) {
		builder.append(" - ").append(filePath).append(LINE_SEPARATOR);
	}
	MessageDialog.openInformation(getShell(), "SPOT Metrics", builder.toString());
}

protected void setVersion(final IJavaProject javaProject) {
	URI pomFileUri = javaProject.getProject().getFile("pom.xml").getLocationURI();
	File pomFile = new File(pomFileUri.getPath());
	this.versionName = "???";
	if (pomFile.exists()) {
		try {
			String pomContent = FileUtil.readFileContent(pomFile);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(pomFile);
			doc.getDocumentElement().normalize();
			NodeList projectNodes = (NodeList) doc.getChildNodes().item(0);
			for (int idx=0; idx<projectNodes.getLength(); idx++) {
				Node childNode = projectNodes.item(idx);
				switch(childNode.getNodeName()) {
					case "artifactId":
						if (!childNode.getTextContent().equals(javaProject.getElementName())) {
							System.err.println("Unexpected artifactId "+childNode.getTextContent()+" for Java project "+javaProject.getElementName()+"!");
							return;
						}
						break;
					case "version":
						this.versionName=childNode.getTextContent();
						return;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	System.err.println("Cannot figure out what was project "+javaProject.getElementName()+" version!");
	this.versionName = "???";
}

}