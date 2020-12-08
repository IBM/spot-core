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
import static com.ibm.bear.qa.spot.core.utils.FileUtil.getFile;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.ibm.bear.qa.spot.core.params.ScenarioParametersManager.DuplicationMode;

/**
 * Class to handle properties file read while running a framework scenario.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #equals(Object)}: Compares the argument to the receiver, and answers true</li>
 * <li>{@link #hashCode()}: Answers an integer hash code for the receiver. Any two</li>
 * <li>{@link #toString()}: Answers a string containing a concise, human-readable</li>
 * </ul>
 * </p>
 */
public class ScenarioParametersFile extends ScenarioParameters {

	/* Constants */
	/**
	 * Tells which property should not have their value displayed neither in the console nor in the log.
	 */
	private static final String HIDDEN_PROPERTIES_ID = "hidden";

	/**
	 * Each property file can define a nested directory, relative to the current property file directory.
	 */
	private static final String NESTED_PARAM_FILES_DIR_ID = "nestedParamFilesDir";

	/**
	 * Each property file can define a nested file using relative paths to the nestedParamFilesDir property.
	 * <p>
	 * This is expected to be a list of paths semi-colon (";") separated with no space
	 * </p>
	 */
	private static final String NESTED_PARAM_FILES_PATH_ID = "nestedParamFilesPath";

	/* Fields */
	// The directory where the file are supposed to be located. Can be null.
	private final File dir;

	// The associated system file. Cannot be neither null nor reassigned.
	private final File file;

	// Store nested properties files
	final private Map<File, List<String>> nestedFiles = new HashMap<>();

	/*
	 * The properties read in the file.
	 *
	 * That does not include the properties of nested files or
	 * property for duplication mode.
	 */
	Properties properties;

/**
 * Create a instance of {@link ScenarioParametersFile} using a directory and a relative path.
 * <p>
 * A parent file must be provided for nested properties file.
 * </p>
 * @param parent The parent in case of a nested file
 * @param propertiesFile The properties file which contains the scenario argument
 */
ScenarioParametersFile(final ScenarioParameters parent, final File filesDir, final String propertiesFile) {
	super(parent);
	if (DEBUG) {
		debugPrintln("	- dir: '" + filesDir.getPath() + "'");
		debugPrintln("	- path: '" + propertiesFile + "'");
	}
	this.dir = filesDir;
	this.file = getFile(filesDir.getPath(), propertiesFile);
}

@Override
public boolean equals(final Object o) {
	if (o instanceof ScenarioParametersFile) {
		ScenarioParametersFile spf = (ScenarioParametersFile)o;
		return this.dir.equals(spf.dir) && this.file.equals(spf.file);
	}
	return super.equals(o);
}

/**
 * Get the canonical path of the system file associated with current properties file.
 * <p>
 * As the associated file is defined from relative path of a given directory, the
 * absolute path might have some up and down. The canonical path allow to get
 * a clear absolute path to easily find the corresponding file on the OS.
 * </p>
 * @return The path as a {@link String}.
 */
String getCanonicalPath() {
	try {
	    return this.file.getCanonicalPath();
    } catch (@SuppressWarnings("unused") IOException e) {
    	debugPrintln("Warning: cannot get canonical path of properties file, hence return absolute path instead.");
	    return this.file.getAbsolutePath();
    }
}

@Override
public int hashCode() {
	return this.dir.hashCode() + this.file.hashCode();
}

/*
 * Method to read in properties files. Each file can have nested files paths
 * (with ; separated file names). This method reads in the entire file, and
 * then recursively reads in each new file. The recursion is depth-first, e.g.,
 * if the filePath contains two file names, it will read in the first file name
 * (and any files named in there) until no more files can be read, then
 * it will read in the second file and travel down through any files listed in there.
 *
 * Note that a properties file will be read only once, even if it's discovered as nested
 * file into several different properties file.
 */
void readFile() {
	debugPrintEnteringMethod();

	// Check if param file has been already read
	if (getRoot().alreadyRead(this)) {
		return;
	}
	this.parent.managedParams.add(this);

	// Read file content
	readProperties();

	// Look for nested files to read in
	for (Entry<File, List<String>> filesInfo: this.nestedFiles.entrySet()) {
		File fileDir = filesInfo.getKey();
		for (String filePath: filesInfo.getValue()) {
			ScenarioParametersFile nestedFile = new ScenarioParametersFile(this, fileDir, filePath);
			nestedFile.readFile();
		}
	}
}

/*
 * Initialize properties with file content.
 */
private void readProperties() {
	this.properties = new Properties();
	try(FileInputStream stream = new FileInputStream(this.file)) {
		println("The parameters properties file '"+this.file.getCanonicalPath()+"' has been found.");
		this.properties.load(stream);
	} catch (IOException ioe) {
		throw new RuntimeException(ioe);
	}

	// Check override flag and remove it if defined
	String duplicationMode = (String) this.properties.remove(DUPLICATION_MODE_ID);
	if (duplicationMode != null) {
		getRoot().mode = DuplicationMode.valueOf(duplicationMode.toUpperCase());
		if (DEBUG) debugPrintln("	-> "+DUPLICATION_MODE_ID+": " + getRoot().mode);
	}

	// Check hidden flag and remove it if defined
	String hidden = (String) this.properties.remove(HIDDEN_PROPERTIES_ID);
	boolean hideAll = this.file.getName().contains("password");
	List<String> fileHiddenProperties = new ArrayList<String>();
	if (hidden != null && !hideAll) {
		if (DEBUG) debugPrintln("	-> "+HIDDEN_PROPERTIES_ID+"=" + hidden);
		StringTokenizer propertiesTokenizer = new StringTokenizer(hidden, " ,;");
		while (propertiesTokenizer.hasMoreTokens()) {
			String nextToken = propertiesTokenizer.nextToken();
			if (nextToken.equals("*")) {
				hideAll = true;
			} else {
				fileHiddenProperties.add(nextToken);
			}
		}
	}

	// Scan properties to extract nested files properties
	Set<String> propertyNames = this.properties.stringPropertyNames();
	List<String> propertiesToBeRemoved = new ArrayList<String>();
	for (String propertyName: propertyNames) {
		if (propertyName.startsWith(NESTED_PARAM_FILES_PATH_ID)) {
			propertiesToBeRemoved.add(propertyName);
			String propertySuffix = propertyName.replaceAll(NESTED_PARAM_FILES_PATH_ID, EMPTY_STRING);
			String nestedFilesDirPropertyName = NESTED_PARAM_FILES_DIR_ID+propertySuffix;
			String nestedFilesDirPropertyValue = this.properties.getProperty(nestedFilesDirPropertyName);
			File nestedFilesDir;
			if (nestedFilesDirPropertyValue == null) {
				nestedFilesDir = this.dir;
			} else {
				nestedFilesDir = getDir(this.dir==null ? null : this.dir.getPath(), nestedFilesDirPropertyValue);
				propertiesToBeRemoved.add(nestedFilesDirPropertyName);
			}
			List<String> files = this.nestedFiles.get(nestedFilesDir);
			if (files == null) {
				this.nestedFiles.put(nestedFilesDir, files = new ArrayList<>());
			}
			String nestedFilesPath = this.properties.getProperty(propertyName);
			StringTokenizer pathTokenizer = new StringTokenizer(nestedFilesPath, ";");
			while (pathTokenizer.hasMoreTokens()) {
				String nestedFilePath = pathTokenizer.nextToken();
				files.add(nestedFilePath);
			}
		}
		else if (!hideAll && !fileHiddenProperties.contains(propertyName) && propertyName.toLowerCase().contains("password")) {
			fileHiddenProperties.add(propertyName);
		}
	}

	// Remove these files properties
	for (String propertyName: propertiesToBeRemoved) {
		this.properties.remove(propertyName);
	}

	// Update hidden properties if necessary
	if (hideAll) {
		List<String> hiddenProperties = getRoot().hiddenProperties;
		for (String propertyName: this.properties.stringPropertyNames()) {
			if (!hiddenProperties.contains(propertyName)) {
				hiddenProperties.add(propertyName);
			}
		}
	}
	else if (fileHiddenProperties.size() > 0) {
		getRoot().hiddenProperties.addAll(fileHiddenProperties);
	}
}

@Override
public String toString() {
	return "Properties file '"+getCanonicalPath()+"'";
}
}