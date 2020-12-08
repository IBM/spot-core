/*********************************************************************
* Copyright (c) 2012, 2020 IBM Corporation and others.
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

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.FileUtil.getDir;

import java.io.File;
import java.util.*;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Class to manage framework properties.
 * <p>
 * No property is stored here but instead this class manages all properties files defined using
 * <code>paramFilesDir</code> and <code>paramFilesPath</code> properties.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getProperty(String)}: Return the value for the given property.</li>
 * <li>{@link #init(String,String)}: Initialize the properties to manage.</li>
 * <li>{@link #toString()}: Answers a string containing a concise, human-readable</li>
 * </ul>
 * </p>
 */
public class ScenarioParametersManager extends ScenarioParameters {

	/**
	 * Store whether properties overwriting is allowed for the current properties file.
	 *
	 * Default is to forbid properties overriding.
	 */
	enum DuplicationMode { OVERWRITE, IGNORE, FORBIDDEN }
	DuplicationMode mode;
	{
		String duplicationModeValue = System.getProperty(DUPLICATION_MODE_ID);
		if (duplicationModeValue != null) {
			switch (duplicationModeValue.toUpperCase()) {
				case "OVERWRITE":
					this.mode = DuplicationMode.OVERWRITE;
					break;
				case "IGNORE":
					this.mode = DuplicationMode.IGNORE;
					break;
				case "FORBIDDEN":
					this.mode = DuplicationMode.FORBIDDEN;
					break;
				default:
					throw new ScenarioFailedError("Value '"+duplicationModeValue+"' for 'duplicationMode' property is invalid'. Only 'overwrite', 'ignore' or 'forbidden' are accepted!");
			}
		} else {
			this.mode = DuplicationMode.FORBIDDEN;
		}
	}

	// Store all files which are nested more than once.
	final Map<ScenarioParametersFile, List<ScenarioParametersFile>> allDuplicatedFiles = new HashMap<>();

	// Store in which file all properties are defined.
	final Map<String, ScenarioParametersFile> definedProperties = new HashMap<String, ScenarioParametersFile>();

	// Store all properties which are assigned more than once.
	// List of files in which they are declared are stored for each properties to
	// allow easy debugging when such problem is encountered.
	// Note that scenario might be stopped or continued when such duplication
	// is detected depending on the 'duplicationMode' property value.
	final Map<String, List<ScenarioParametersFile>> duplicatedProperties = new HashMap<String, List<ScenarioParametersFile>>();

	// Properties that we do not want to show content neither in the console nor in the debug file (typically password value)
	final List<String> hiddenProperties = new ArrayList<>();

	// Store all hierarchy bundled properties
	Properties bundledProperties;

	// Tell whether parameters have been checked or not
	private boolean checked = false;

	// Root dir and files path stored for display convenience
	private File rootDir;
	private String filesPath;

public ScenarioParametersManager() {
	super(null);
}

/**
 * Check whether a file has been already read or not.
 *
 * @param paramFile The parameters file to be checked
 */
boolean alreadyRead(final ScenarioParametersFile paramFile) {
	List<ScenarioParametersFile> allParameterFiles = getAllParameterFiles();
	int index = allParameterFiles.indexOf(paramFile);
	if (index >= 0) {
		List<ScenarioParametersFile> duplicatedFiles = this.allDuplicatedFiles.get(paramFile);
		if (duplicatedFiles == null) {
			this.allDuplicatedFiles.put(allParameterFiles.get(index), duplicatedFiles = new ArrayList<>());
		}
		duplicatedFiles.add(paramFile);
		return true;
	}
	return false;
}

/**
 * Check all properties defined by current object and all its children recursively.
 * <p>
 * The returned properties contains the properties defined in the current file but
 * also all properties defined in all nested files recusirvely.
 * </p><p>
 * There's no duplicate in the stored bundled properties. All duplications found while
 * parsing the files hierarchy have been identified and will be raised as warnings
 * and/or errors.
 * </p><p>
 * Nested files found several times while parsing the hierarchy raise only a warning.
 * Properties which are declared several times raise either a warning or an error
 * depending on the <code>duplicationMode</code> property value.
 * </p><p>
 * Messages with warnings and errors are displayed in the console with the
 * properties in which the issue occurs for each of them. That should help scenarios
 * writers to debug any of them quite easily.
 * </p><p>
 * Scenario can continue if there are only warnings, but it will be stopped if there's
 * at least one error as a {@link ScenarioFailedError} will be raised in such case.
 * </p>
 * @throws ScenarioFailedError If there's at least one unauthorized property
 * overriding detected
 */
private void check() throws ScenarioFailedError {

	// Bundle properties
	initBundledProperties();

	// Raise warnings for duplicated files
	boolean hasWarnings = false;
	StringBuilder builder = new StringBuilder("Errors/Warnings have been found while parsing properties file(s) of current scenario:").append(LINE_SEPARATOR);
	if (this.allDuplicatedFiles.size() > 0) {
		builder.append("	- Following properties files are duplicated:").append(LINE_SEPARATOR);
		for (ScenarioParametersFile file: this.allDuplicatedFiles.keySet()) {
			builder.append("		+  ").append(file).append(LINE_SEPARATOR);
			builder.append("		  -> defined in ").append(file.parent).append(LINE_SEPARATOR);
			for (ScenarioParametersFile duplicatedFile: this.allDuplicatedFiles.get(file)) {
				builder.append("		  -> duplicated in ").append(duplicatedFile.parent);
			}
		}
		hasWarnings = true;
	}

	// Raise warnings/error for duplicated properties
	if (this.duplicatedProperties.size() > 0) {
		Map<String, List<ScenarioParameters>> allErrors = new HashMap<String, List<ScenarioParameters>>();
		builder.append("	- Some properties are duplicated:").append(LINE_SEPARATOR);
		for (String propertyName: this.duplicatedProperties.keySet()) {
			List<ScenarioParametersFile> parameters = this.duplicatedProperties.get(propertyName);
			String storedValue = (String) this.bundledProperties.get(propertyName);
			builder.append("		+ property '").append(propertyName).append("'").append(LINE_SEPARATOR);
			builder.append("			* defined in ").append(this.definedProperties.get(propertyName)).append(LINE_SEPARATOR);
			builder.append("			* value='").append(storedValue).append("'").append(LINE_SEPARATOR);
			for (ScenarioParametersFile paramFile: parameters) {
				String value = (String) paramFile.properties.get(propertyName);
				builder.append("			* is duplicated in ").append(paramFile).append(LINE_SEPARATOR);
				builder.append("			* value='").append(value).append(LINE_SEPARATOR);
				if (storedValue.equals(value)) {
					builder.append("			=> value is the same, hence no action is taken, but properties file might be cleaned up...");
				} else {
					switch (this.mode) {
						case IGNORE:
							builder.append("			=> value is different, only initial one ("+storedValue+") is kept...");
							break;
						case OVERWRITE:
							builder.append("			=> value is different, new one ("+value+") replace the initial one ("+storedValue+")...");
							this.bundledProperties.put(propertyName, value);
							this.definedProperties.put(propertyName, paramFile);
							storedValue = value;
							break;
						case FORBIDDEN:
							builder.append("			=> ERROR (i.e., scenario will be stopped!)");
							List<ScenarioParameters> errors = allErrors.get(propertyName);
							if (errors == null) {
								allErrors.put(propertyName, errors = new ArrayList<ScenarioParameters>());
							}
							errors.add(paramFile);
							break;
					}
				}
				builder.append(LINE_SEPARATOR);
			}
		}

		// Print specific messages for errors
		int errorsSize = allErrors.size();
		if (errorsSize > 0) {

			// Print files which have errors
			builder.append("	- To avoid the scenario stopping due to duplicate values:").append(LINE_SEPARATOR);
			builder.append("		+ Either remove the duplicated property").append(LINE_SEPARATOR);
			builder.append("		+ Or change the duplication mode property 'duplicationMode' to either 'overwrite' or 'ignore' in following files:").append(LINE_SEPARATOR);
			Set<ScenarioParameters> allParams = new HashSet<ScenarioParameters>();
			for (String propertyName: allErrors.keySet()) {
				allParams.addAll(allErrors.get(propertyName));
			}
			for (ScenarioParameters params: allParams) {
				builder.append("		  -> ").append(params).append(LINE_SEPARATOR);
			}

			// Print error message to console
			println("========================================");
			println(builder.toString());

			// Build error message
			String prefix;
			if (errorsSize == 1) {
				String propertyName = allErrors.keySet().iterator().next();
				prefix = "Property '"+propertyName+"' is defined in "+(allParams.size()+1);
			} else {
				prefix = "Several properties are defined in";
			}

			// Print all read properties
			this.checked = true;
			println(toString());

			// Stop scenario execution
			throw new ScenarioFailedError(prefix+" different property files.");
		}
		hasWarnings = true;
	}

	// Print warning message to console if any
	if (hasWarnings) {
		println("========================================");
		print(builder.toString());
	}

	// Print all read properties
	this.checked = true;
	println("========================================");
	print(printAllProperties());
	println("========================================");
}

/**
 * Return the value for the given property.
 *
 * @param name The property name
 * @return The property value or <code>null</code> if the property is not defined
 * as a scenario parameter.
 */
public String getProperty(final String name) {
	if (this.bundledProperties == null) {
		initBundledProperties();
	}
	return this.bundledProperties.getProperty(name);
}

@Override
ScenarioParametersManager getRoot() {
	return this;
}

/**
 * Initialize the properties to manage.
 *
 * @param rootDirString The root dir for parameters files
 * @param paths The concatenated paths of properties files
 */
public void init(final String rootDirString, final String paths) {
	this.rootDir = getDir(rootDirString);
	this.filesPath = paths;

	// Read each specified parameters file (adding them as child of root)
	StringTokenizer pathTokenizer = new StringTokenizer(paths, ";");
	while (pathTokenizer.hasMoreTokens()) {
		ScenarioParametersFile scenarioParametersFile = new ScenarioParametersFile(this, this.rootDir, pathTokenizer.nextToken());
		scenarioParametersFile.readFile();
	}

	// Check whole properties
	check();
}

private void initBundledProperties() {
	this.bundledProperties = new Properties();

	// Build bundled properties based on all managed files
	for (ScenarioParametersFile paramFile: getAllParameterFiles()) {
		Properties childrenProperties = paramFile.properties;
		for (Object key : childrenProperties.keySet()) {

			// Get child property value
			String childPropertyName = (String) key;
			String childPropertyValue = (String) childrenProperties.get(childPropertyName);

			// Set property safely (ie. it will be identified as duplicated if it's already defined)
			if (this.bundledProperties.containsKey(key)) {
				// Get already duplicated properties
				List<ScenarioParametersFile> parameters = this.duplicatedProperties.get(key);
				if (parameters == null) {
					this.duplicatedProperties.put(childPropertyName, parameters = new ArrayList<ScenarioParametersFile>());
				}
				parameters.add(paramFile);
			} else {
				this.bundledProperties.put(childPropertyName, childPropertyValue);
				this.definedProperties.put(childPropertyName, paramFile);
			}
		}
	}
}

/**
 * Print all currently managed properties.
 *
 * @return The properties list per parameters files
 */
public String printAllProperties() {
	StringBuilder builder= new StringBuilder(toString());
	if (this.checked) {
		Map<ScenarioParametersFile, List<String>> map = new HashMap<>();
		for (String property: this.definedProperties.keySet()) {
			ScenarioParametersFile paramFile = this.definedProperties.get(property);
			List<String> fileProperties = map.get(paramFile);
			if (fileProperties == null) {
				map.put(paramFile, fileProperties = new ArrayList<>());
			}
			fileProperties.add(property);
		}
		builder.append("-------------------------").append(LINE_SEPARATOR);
		for (ScenarioParametersFile paramFile: getAllParameterFiles()) {
			builder.append(" - in ").append(paramFile).append(":").append(LINE_SEPARATOR);
			if (paramFile.parent != this) {
				builder.append("	(nested in: ").append(paramFile.parent).append(")").append(LINE_SEPARATOR);

			}
			List<String> fileProperties = map.get(paramFile);
			if (fileProperties == null) {
				builder.append("	(no properties defined)").append(LINE_SEPARATOR);
			} else {
				for (String property: fileProperties) {
					String propertyValue = (String) this.bundledProperties.get(property);
					if (this.hiddenProperties.contains(property)) {
						propertyValue = propertyValue.charAt(0) + "*******";
					}
					builder.append("	+ ").append(property).append("=").append(propertyValue).append(LINE_SEPARATOR);
				}
			}
		}
	}
	return builder.toString();
}

/**
 * Return the printable value for the given property.
 * <p>
 * Note that this method is similar to {@link #getProperty(String)} but
 * differ in the fact that it does not display the value for hidden properties.
 * Instead it prints the first character of the real value followed by stars
 * in order to hide the end of the string.
 * </p><p>
 * It also allow to print a <code><not defined></code> when the property
 * is not defined.
 * </p>
 * @param name The property name
 * @return The property value to be printed
 */
public String printProperty(final String name) {
	String value = getProperty(name);
	if (value == null) return "!!! not defined !!!";
	if (this.hiddenProperties.contains(name)) {
		value = value.charAt(0) + "******";
	}
	return value;
}

@Override
public String toString() {
	StringBuilder builder= new StringBuilder("ScenarioParametersManager (rootDir=")
			.append(this.rootDir)
			.append(", filesPath=")
			.append(this.filesPath)
			.append(")")
			.append(LINE_SEPARATOR);
	return builder.toString();
}
}
