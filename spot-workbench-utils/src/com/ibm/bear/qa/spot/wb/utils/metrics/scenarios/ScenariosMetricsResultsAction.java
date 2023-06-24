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
package com.ibm.bear.qa.spot.wb.utils.metrics.scenarios;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.ibm.bear.qa.spot.wb.utils.metrics.CodeMetricsAbstractAction;

/**
 * Action to generate metrics for all scenarios present in current workspace.
 */
public class ScenariosMetricsResultsAction extends CodeMetricsAbstractAction {

	/* Fields */
	List<String> resultFiles = new ArrayList<String>();

/**
 * Method called when the corresponding action item in the popup-menu is selected.
 */
@Override
public void run(final IAction action) {

	// Init values
	if (!initDir()) {
		return;
	}

	try {
		// Get all units for selected projects
		final File dir = new File(this.filterDir);
		final Map<IJavaProject, List<ICompilationUnit>> mapUnits = getProjectsUnitsMap(/*scenarios:*/true);

		// For each selected projects
		this.resultFiles.clear();
		for (final IJavaProject javaProject: mapUnits.keySet()) {

			// Create runnable
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// Get all scenario units
						// Get all project units
						List<ICompilationUnit> projectScenarios = mapUnits.get(javaProject);
						if (projectScenarios.size() == 0) {
							return;
						}
						String projectName = javaProject.getElementName();
						monitor.beginTask("Computing Metrics for "+projectName, projectScenarios.size());

						// Set version
						String version = ScenariosMetricsResultsAction.this.getProjectVersion(javaProject);

						// Init task title and size
						monitor.beginTask("Computing Scenarios Metrics for project "+projectName+"...", projectScenarios.size());

						// Get metrics for each scenario unit
						ScenarioMetricsResults scenarioResults = new ScenarioMetricsResults(projectName);
						for (ICompilationUnit unit : projectScenarios) {
							String subtaskTitle = "Scanning SPOT scenario " + unit.getElementName();
							monitor.subTask(subtaskTitle + "...");
							// Parse unit using a visitor
							ScenarioVisitor visitor = new ScenarioVisitor(unit);
							CompilationUnit astRoot = createCompilationUnitAST(unit);
							astRoot.accept(visitor);
							scenarioResults.addScenario(visitor, monitor);
							if (monitor.isCanceled()) {
								return;
							}
							monitor.worked(1);
						}
						monitor.done();

						// Write result
						File resultFile = scenarioResults.writeResult(dir, version);
						if (resultFile != null) {
							if (!ScenariosMetricsResultsAction.this.resultFiles.contains(resultFile.getAbsolutePath())) {
								ScenariosMetricsResultsAction.this.resultFiles.add(resultFile.getAbsolutePath());
							}
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
			};

			// Execute the action
			ProgressMonitorDialog progress = new ProgressMonitorDialog(this.shell);
			try {
				progress.run(true, true, runnable);
			} catch (@SuppressWarnings("unused") Exception e) {
				// skip
			}
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
}