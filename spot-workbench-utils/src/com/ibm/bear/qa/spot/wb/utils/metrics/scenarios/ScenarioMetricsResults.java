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
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.core.JavaModelManager;

import com.ibm.bear.qa.spot.wb.utils.metrics.AbstractMetricsResults;

/**
 * Manage scenarios metric results.
 * <p>
 * It stores following information in order to be able to generate the final results
 * in the specified flat text file:
 * <ul>
 * <li>scenarios: the list of {@link Scenario scenarios} get while looking for classes in workspace.</li>
 * </p><p>
 * The flat file generated at the end of the metrics scan is built as follows:
 * <pre>
 * Version		Date		Scenarios	Steps	Tests
 * CLM 6.0.0 S8	01/20/2014	X	Y	Z
 * 				&lt;name 1&gt;	S1	T2
 * ...
 * 				&lt;name N&gt;	Sn	Tn
 * </pre>
 * Where on the first line, <b>X</b> is the number of scenarios, <b>Y</b> the total
 * of steps and <b>Z</b> the total of tests for that version.<br>
 * On following lines, for each scenario of that version, <b>Sn</b> is the number
 * of steps and <b>Tn</b> the number of tests for the corresponding scenario.
 */
@SuppressWarnings("restriction")
public class ScenarioMetricsResults extends AbstractMetricsResults {


	/* Constants */
	private static final String[][] HEADERS = {
			{ "Scenarios", "Steps", "Tests"/*, "Runs", "Monitored" */ },
	};

	/**
	 * The scenarios encountered while looking for classes in the workspace.
	 */
	List<Scenario> scenarios = new ArrayList<Scenario>();

	/*
	 * Internal maps to store known information in order to improved performances.
	 * They allow to avoid to search same fragment or unit twice in the workspace.
	 */
	Map<ImportDeclaration, IPackageFragment> knownFragments = new HashMap<ImportDeclaration, IPackageFragment>();
	Map<ImportDeclaration, ICompilationUnit> knownUnits= new HashMap<ImportDeclaration, ICompilationUnit>();

public ScenarioMetricsResults(final String projectName) {
	super(projectName);
}

/*
 * Look for the given import in the workspace.
 */
protected IPackageFragment getImportFragment(final String importName) throws JavaModelException {

	// Get java project of the given import name
	IJavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
	int lastDot = importName.lastIndexOf('.');
	IJavaProject parentProject = javaModel.getJavaProject(importName.substring(0, lastDot));

	// If project is found get fragment directly from it
	if (parentProject.exists()) {
		IPackageFragmentRoot srcRoot = parentProject.findPackageFragmentRoot(parentProject.getPath().append("/src"));
		IPackageFragment fragment = srcRoot.getPackageFragment(importName);
		if (fragment.exists()) {
			return fragment;
		}
	}

	// Otherwise look for the fragment in the entire workspace
	for (IJavaProject javaProject: javaModel.getJavaProjects()) {
		for (IPackageFragmentRoot fragmentRoot: javaProject.getPackageFragmentRoots()) {
			if (fragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IPackageFragment fragment = fragmentRoot.getPackageFragment(importName);
				if (fragment.exists()) {
					return fragment;
				}
			}
		}
	}

	// Somthing wrong happened, we should have found the corresponding package
	throw new RuntimeException("Cannot find fragment for import: "+importName);
}

/**
 * Add a scenario to results.
 *
 * @param visitor The visitor used to parse the scenario. It contains interesting
 * information as the scenario unit and the steps list.
 * @param monitor The monitor to report progress
 * @throws JavaModelException In case something wrong happened while looking
 * for Java Model information
 */
Scenario addScenario(final ScenarioVisitor visitor, final IProgressMonitor monitor) throws JavaModelException {

	// Build and add scenario to results list
	Scenario scenario = new Scenario(visitor.scenarioUnit, visitor.scenarioType, visitor.scenarioRuns, visitor.scenarioMaintenance);
	this.scenarios.add(scenario);

	// Setup progress subtask (almost invisible as too fast in fact...)
	if (monitor != null) {
		String subtaskTitle = "Scenario "+scenario.getName();
		monitor.subTask(subtaskTitle);
	}

	/*
	 * We now need to resolve information contained in the scenario visitor as
	 * they are only names at this point.
	 *
	 * Resolve information means to find the qualified name of each scenario step
	 * found in the @SuiteClasses annotation in order to find the corresponding
	 * compilation unit in the Java Model.
	 *
	 * For that, we'll first resolve the fragments from the import list and then
	 * for import on demand, we'll see if they contains one of the step name.
	 */
	// Set scenario fragment and root (source folder)
	IPackageFragment scenarioFragment = (IPackageFragment) visitor.scenarioUnit.getParent();
	IPackageFragmentRoot scenarioFragmentRoot = (IPackageFragmentRoot) scenarioFragment.getParent();

	// Initialize fragments and units list
	// The first obvious fragment is the scenario's one (where steps might have been put)
	List<IPackageFragment> fragments = new ArrayList<IPackageFragment>();
	fragments.add(scenarioFragment);
	List<ICompilationUnit> stepUnits = new ArrayList<ICompilationUnit>();

	// Resolve import fragments
	for (ImportDeclaration importDeclaration: visitor.imports) {

		// Get fully qualified name from import declaration AST node
		String importName = importDeclaration.getName().getFullyQualifiedName();

		// Some imports are skipped as JUnit or Runner classes (from SPOT)
		if (!importName.startsWith("org.junit") && !importName.contains("Runner") && !importName.startsWith("java.")) {

			// Check whether the import is on demand (ie. finishes with a '*')
			if (importDeclaration.isOnDemand()) {

				// Check if the fragment is already known
				IPackageFragment stepFragment = this.knownFragments.get(importDeclaration);
				if (stepFragment == null) {
					// Try to get fragment from scenario src folder
					stepFragment = scenarioFragmentRoot.getPackageFragment(importName);

					// If it's not found in scenario plugin then try a larger scope
					if (!stepFragment.exists()) {
						stepFragment = getImportFragment(importName);
					}

					// Add the found fragment the known ones
					this.knownFragments.put(importDeclaration, stepFragment);
				}

				// Add the found fragment to the list
				fragments.add(stepFragment);
			} else {
				// Check if the compilation unit is already known
				ICompilationUnit stepUnit = this.knownUnits.get(importDeclaration);
				if (stepUnit == null) {

					// Get last dot position from import name to be able to extract the package and class names from it
					int lastDot = importName.lastIndexOf('.');

					// Check if the fragment is already known
					IPackageFragment stepFragment = this.knownFragments.get(importDeclaration);
					if (stepFragment == null) {

						// Extract package name from import
						String packageName = importName.substring(0, lastDot);

						// Try to get fragment from scenario src folder
						stepFragment = scenarioFragmentRoot.getPackageFragment(packageName);

						// If it's not found in scenario plugin then try a larger scope
						if (!stepFragment.exists()) {
							stepFragment = getImportFragment(packageName);
						}

						// Add the found fragment the known ones
						this.knownFragments.put(importDeclaration, stepFragment);
					}

					// Extract the class name from import declaration
					String className = importName.substring(lastDot+1);

					// Get compilation from found fragment
					stepUnit = stepFragment.getCompilationUnit(className+".java");
					if (!stepUnit.exists()) {
						// We should have found the comilation there
						throw new RuntimeException("Unit "+className+".java has not been found.");
					}

					// Add the found untit to the known ones
					this.knownUnits.put(importDeclaration, stepUnit);
				}

				// Add the found unit to the list
				stepUnits.add(stepUnit);
			}
		}
	}

	// We are ready to resolve the step units
	stepsLoop: for (SimpleType stepType: visitor.steps) {

		// Get unit file name
		String unitFileName = stepType.getName().getFullyQualifiedName()+".java";

		// Check if we already knows the unit
		for (ICompilationUnit stepUnit: stepUnits) {

			// Add the step information to scenario if the unit matches one in the list
			if (stepUnit.getElementName().equals(unitFileName)) {
				addStep(scenario, stepUnit, monitor);
				continue stepsLoop;
			}
		}

		// Look for the unit in the resolved fragments
		ICompilationUnit stepUnit = null;
		for (IPackageFragment fragment: fragments) {

			// Get unit from fragment
			stepUnit = fragment.getCompilationUnit(unitFileName);

			// If the unit belongs to the fragment, add step information to scenario
			if (stepUnit.exists()) {
				addStep(scenario, stepUnit, monitor);
				break;
			}
		}

		// Something wrong happened if the unit was not found
		if (stepUnit == null) {
			throw new RuntimeException("Step "+stepType+" has not been found.");
		}
	}

	// Return created scenario object
	return scenario;
}

/*
 * Add the step information to the given scenario
 */
private void addStep(final Scenario scenario, final ICompilationUnit stepUnit, final IProgressMonitor monitor) {

	// Create step object
	ScenarioStep step = scenario.addStep(stepUnit);

	// Update progress
	if (monitor != null) {
		monitor.subTask("Scenario step "+scenario.getName()+"."+step.getName());
	}

	// Parse the step unit to complete information (as tests)
	ScenarioStepVisitor stepVisitor = new ScenarioStepVisitor(stepUnit);
	CompilationUnit astRoot = convert(stepUnit);
	astRoot.accept(stepVisitor);

	// Store found information in step object
	step.setTests(stepVisitor.tests);
}

/*
 * Convert the given Java Model compilation unit to AST DOM unit.
 * That will prepare the unit parsing.
 */
@SuppressWarnings("deprecation")
private CompilationUnit convert(final ICompilationUnit unit) {
	ASTParser parser = ASTParser.newParser(AST.JLS4);
	parser.setSource(unit);
	parser.setResolveBindings(false);
	return (CompilationUnit) parser.createAST(null);
}

/*
 * Write result to a flat text file.
 * <p>
 * Note that it appends the results if the files already exists.
 * </p>
 * @param dir The directory where the file will be written
 * @param versionName The version name against which the tool has been run
 * @param versionDate The version data
 * @return The file which has been created or modified.
 */
/*
File writeResult(final File dir, final String versionName, final String versionDate) {

	// Create file
	File file = new File(dir, "Scenarios Metrics.txt");

	// Compute steps and tests numbers
	int allSteps = 0;
	int allTests = 0;
	for (Scenario scenario: this.scenarios) {
		allSteps += scenario.steps.size();
		allTests += scenario.getTestsNumber();
	}

	// Print result in file
	try {
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file, true)), false);
		try {
			System.out.print(versionName);
			System.out.print("\t");
			System.out.print(versionDate);
			System.out.print('\t');
			// Print scenarios number
			System.out.print(this.scenarios.size());
			// Print steps number
			System.out.print('\t');
			System.out.print(allSteps);
			// Print test number
			System.out.print('\t');
			System.out.print(allTests);
			// New line
			System.out.println();
//			for (Scenario scenario: this.scenarios) {
//				System.out.print("\t\t");
//				System.out.print(scenario.getName());
//				System.out.print('\t');
//				System.out.print(scenario.steps.size());
//				System.out.print('\t');
//				System.out.print(scenario.getTestsNumber());
//				System.out.println();
//			}
		}
		finally {
			System.out.close();
		}
	}
	catch (FileNotFoundException e) {
		System.err.println("Cannot create stream for log: " + e.getMessage());
	}
	return file;
}
*/

/**
 * Return all steps declared in the scenarios list.
 * <p>
 * Note that this method does not take care of duplicate. Hence, that a representative
 * number of steps which would be executed if all the scenarios of the list would be run.
 * </p>
 * @return The sum of all scenario list steps
 */
int getAllRunSteps() {
	int allSteps = 0;
	for (Scenario scenario: this.scenarios) {
		if (scenario.isCountable()) {
			allSteps += scenario.steps.size();
		}
	}
	return allSteps;
}

/**
 * Return all tests declared in all steps of the scenarios list.
 * <p>
 * Note that this method does not take care of duplicate. Hence, that a representative
 * number of tests which would be executed if all the scenarios of the list would be run.
 * </p>
 * @return The sum of all scenario list steps
 */
int getAllRunTests() {
	int allTests = 0;
	for (Scenario scenario: this.scenarios) {
		if (scenario.isCountable()) {
			allTests += scenario.getTestsNumber();
		}
	}
	return allTests;
}

/**
 * Return all steps implemented in the scenarios list.
 * <p>
 * Note that this method does take care of duplicate. Hence, that a representative
 * number of steps which have been actually implemented for all the scenarios list.
 * </p>
 * @return The number of all distinct steps in the scenarios list
 */
int getAllImplementedSteps() {
	return getImplementedSteps().size();
}

/**
 * Return all tests implemented in the scenarios list.
 * <p>
 * Note that this method does take care of duplicate. Hence, that a representative
 * number of tests which have been actually implemented for all the scenarios list.
 * </p><p>
 * Note also that it currently assumes that all tests are different as soon as they
 * are in different step.
 * </p>
 * @return The list of all distinct steps in the scenarios list
 */
int getAllImplementedTests() {
	Set<ScenarioStep> implementedSteps = getImplementedSteps();
	int allTests = 0;
	for (ScenarioStep step: implementedSteps) {
		allTests += step.getTests().size();
	}
	return allTests;
}

/**
 * Return all steps implemented in the scenarios list.
 * <p>
 * Note that this method does take care of duplicate. Hence, that a representative
 * number of steps which have been actually implemented for all the scenarios list.
 * </p>
 * @return The list of all distinct steps in the scenarios list
 */
List<Scenario> getCountableScenarios() {
	List<Scenario> countableScenarios = new ArrayList<Scenario>();
	for (Scenario scenario: this.scenarios) {
		if (scenario.isCountable()) {
			countableScenarios.add(scenario);
		}
	}
	return countableScenarios;
}

/**
 * Return all steps implemented in the scenarios list.
 * <p>
 * Note that this method does take care of duplicate. Hence, that a representative
 * number of steps which have been actually implemented for all the scenarios list.
 * </p>
 * @return The list of all distinct steps in the scenarios list
 */
Set<ScenarioStep> getImplementedSteps() {
	Set<ScenarioStep> implementedSteps = new HashSet<ScenarioStep>();
	for (Scenario scenario: this.scenarios) {
		if (scenario.isCountable()) {
			implementedSteps.addAll(scenario.steps);
		}
	}
	return implementedSteps;
}

@Override
protected String[][] getHeaders() {
	return HEADERS;
}

@Override
public File writeResult(final File dir, final String versionName) {
	if (this.scenarios.isEmpty()) {
		return null;
	}

	StringBuilder newLine = new StringBuilder();

	for (Scenario scenario : this.scenarios) {
		if (newLine.length() > 0) {
			newLine.append(LINE_SEPARATOR);
		}
		newLine.append(this.project)
			.append("\t")
			.append(versionName)
			.append("\t")
			.append(CURRENT_DATE)
			.append("\t");
		switch (scenario.getType()) {
			case Pipeline:
			case Monitored:
			case Private:
			case Perfs:
				newLine.append(scenario.getName())
				    .append('\t')
				    .append(scenario.steps.size())
				    .append('\t')
				    .append(scenario.getTestsNumber());
//				    .append('\t')
//				    .append(scenario.getRuns())
//				    .append('\t')
//				    .append(scenario.maintenance ? 1 : 0)
//				    .append(LINE_SEPARATOR);
				break;
			case Demo:
				System.out.println("Skipping demo scenario " + scenario.getName());
				// skip
				break;
			case Undefined:
				System.out.println("Skipping undefined scenario " + scenario.getName());
				// skip
				break;
		}
	}

	// Write new file content
	File file = getFile(dir, "scenarios");
	write(file, versionName, newLine.toString());
	return file;
}
}
