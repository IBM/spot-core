/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.params;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.DEBUG;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrintln;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for scenario parameters.
 * <p>
 * Basically, it stores the parent in order to be able to walk through
 * the managed properties files tree.
 * </p>
 */
abstract class ScenarioParameters {

	/* Constants */
	/**
	 * Tells how the to behave when encountering a duplicated property in a nested file.
	 * Basically, there are 3 possible behaviors:
	 * <ol>
	 * <li>"overwrite": overwrite previously read property (last read value wins)</li>
	 * <li>"ignore": ignore overriding property (first read value wins)</li>
	 * <li>"forbidden": forbid property overriding (raise an error when one is detected)</li>
	 * </ol>
	 * Default is to forbid property overriding.
	 */
	static final String DUPLICATION_MODE_ID = "duplicationMode";

	/* Fields */
	// Parent properties file for managed parameters files. Can be null. Cannot be reassigned.
	// Helps to determine in which file a duplication occurs.
	final ScenarioParameters parent;

	// Managed children files. It's never null but can be empty. Cannot be reassigned.
	// Allow recursing while getting all nested properties of a properties file.
	final List<ScenarioParametersFile> managedParams = new ArrayList<ScenarioParametersFile>();

/**
 * Create a instance of {@link ScenarioParametersFile} using a directory and a relative path.
 * <p>
 * A parent file must be provided for nested properties file.
 * </p>
 * @param parent The parent in case of a nested parameters
 */
ScenarioParameters(final ScenarioParameters parent) {
	if (DEBUG) {
		debugPrintln("Creating scenario parameters");
		if (parent != null) debugPrintln(" (parent: " + parent+ ")");
	}
	this.parent = parent;
}

ScenarioParametersManager getRoot() {
	return this.parent.getRoot();
}

List<ScenarioParametersFile> getAllParameterFiles() {
	List<ScenarioParametersFile> allFiles = new ArrayList<>();
	for (ScenarioParametersFile paramFile: this.managedParams) {
		allFiles.add(paramFile);
		allFiles.addAll(paramFile.getAllParameterFiles());
	}
	return allFiles;
}
}
