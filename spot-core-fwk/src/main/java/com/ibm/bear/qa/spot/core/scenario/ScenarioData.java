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
package com.ibm.bear.qa.spot.core.scenario;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.FileUtil.*;
import static com.ibm.bear.qa.spot.core.utils.StringUtils.convertLineDelimitersToUnix;
import static com.ibm.bear.qa.spot.core.utils.StringUtils.getSafeStringForPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.ibm.bear.qa.spot.core.api.SpotUser;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Manage scenario general data.
 * <p>
 * A this root level, two data are defined or expected:
 * <ol>
 * <li><code>testPrefix</code>: this is a prefix which will be applied to all properties
 * of each user.<br>
 * TODO Give an example...</li>
 * <li>Test User: Provide access to a default test user that subclasses will initialize
 * (see {@link #initUsers()})</li>
 * </ol>
 * </p><p>
 * As this class is supposed to be overridden in each scenario layer, we are not
 * using interface to define API. Public and protected methods are considered as
 * API, hence supported in the long term.
 * </p><p>
 * Following public API methods are available:
 * <ul>
 * <li>{@link #getDataRootDir()}: Return the root directory where all data related artifacts of the test plug-in are located.</li>
 * <li>{@link #getPrefix()}: Return the prefix to apply to all data created during scenario execution (project, users, etc.).</li>
 * <li>{@link #getUsers()}: Return users basically used while running the scenario.</li>
 * </ul>
 * </p><p>
 * Following internal methods are available:
 * <ul>
 * <li>{@link #initPrefix()}: Init prefix which will be used for all users.</li>
 * </ul>
 * </p>
 */
public abstract class ScenarioData implements ScenarioDataConstants {

	/**
	 * Possible strategies while encountering existing data during artifact creation.
	 */
	public enum DataCreationBehavior {
		/** Raise an error when the artifact to be created already exist (default) */
		ERROR,
		/** Reuse the existing artifact instead of creating it */
		REUSE,
		/** Delete the existing artifact prior creating it */
		DELETE
	}

	/* Constants */
	private static final String DATA_CREATION_BEHAVIOR = getParameterValue("data.creation.behavior");

	/* Fields */
	// General data
	protected String prefix;

	// Users
	final protected List<SpotUser> users = new ArrayList<>();

	// Existing data
	protected DataCreationBehavior dataCreationBehavior;

protected ScenarioData() {
	initPrefix();
	initUsers();
	if (DATA_CREATION_BEHAVIOR != null) {
		switch (DATA_CREATION_BEHAVIOR.toUpperCase()) {
			case "ERROR":
				this.dataCreationBehavior = DataCreationBehavior.ERROR;
				break;
			case "REUSE":
				this.dataCreationBehavior = DataCreationBehavior.REUSE;
				break;
			case "DELETE":
				this.dataCreationBehavior = DataCreationBehavior.DELETE;
				break;
			default:
				throw new ScenarioFailedError("'"+DATA_CREATION_BEHAVIOR+"' is not a valid data creation behavior! Expecting 'ERROR', 'REUSE' or 'DELETE (ignoring case)'...");
		}
	}
}

/**
 * Return the general strategy to follow when creating artifact during scenario execution.
 * <p>
 * Note that scenario might not strictly follow the strategy. This behavior is only provided
 * to allow scenario to have coherent behavior while creating artifacts along their execution.
 * </p>
 * @see DataCreationBehavior
 * @return The expected behavior
 */
public DataCreationBehavior getDataCreationBehavior() {
	if (this.dataCreationBehavior == null) {
		return DataCreationBehavior.ERROR;
	}
	return this.dataCreationBehavior;
}

/**
 * Return the root directory where all data related artifacts of the test plug-in
 * are located.
 *
 * @return The root directory where all data related artifacts
 * of the test plug-in are located as a {@link String} or <code>null</code> if
 * there's no specific data used by the scenario.
 */
public String getDataRootDir() {
	return DATA_ROOT_DIR;
}

/**
 * Return the output folder path.
 *
 * @return The output folder path as a {@link String}.
 */
public String getOutputFolder(){
	return OUTPUT_DIR;
}

/**
 * Return the prefix to use when creating an application item.
 *
 * @return The prefix as a {@link String}
 */
public String getPrefix() {
	return this.prefix;
}

/**
 * Return users used while running the scenario.
 *
 * @return The test user as {@link SpotUser}.
 */
public List<SpotUser> getUsers() {
	return this.users;
}

/**
 * Init prefix which will be used for all users.
 * <p>
 * To set an explicit prefix set its value using the scenario argument
 * {@link ScenarioDataConstants#TEST_PREFIX_PARAM_ID}.
 * </p><p>
 * By default there's no prefix, ie. it's an empty string.
 * </p><p>
 * Note that if {@link ScenarioDataConstants#RANDOM_PREFIX_PARAM_ID} argument
 * is <code>true</code>, the prefix will be initialized with a random long value got
 * from current time.
 * </p>
 */
protected void initPrefix() {
	if (getParameterBooleanValue(RANDOM_PREFIX_PARAM_ID)) {
		// Assign a random prefix if requested by user.
		this.prefix = Long.toString(System.currentTimeMillis());
	} else {
		// Otherwise, initialize the prefix to the given or
		// default appropriately.
		this.prefix = getParameterValue(TEST_PREFIX_PARAM_ID, TEST_PREFIX_PARAM_DEFAULT_VALUE);
	}
}

/**
 * Initialize users which will be used all over the scenario steps.
 */
abstract protected void initUsers();

/**
 * Find folders matching the given name in current project folder and its hierarchy.
 * <p>
 * Note that searching in current folder hierarchy stops at scenarios root folder
 * (ie. <code>spot-scn</code>).
 * </p>
 * @param folderName The folder name
 * @return The folders list which might be empty if no folder is found
 */
protected List<File> findFolders(final String folderName) {
	debugPrintEnteringMethod("folderName", folderName);
	List<File> foundFiles = findFileInAscendantHierarchy(folderName, "spot-scn", new FileFilter() {
		@Override
		public boolean accept(final File pathname) {
			String name = pathname.getName();
			return pathname.isDirectory() &&
			        name.charAt(0) != '.' &&
			        !name.equals("src") &&
			        !name.equals("debug") &&
			        !name.equals("params") &&
			        !name.equals("launches") &&
			        !name.equals("screenshots") &&
			        !name.equals("scripts") &&
			        !name.equals("target");
		}
	});
	return foundFiles;
}

/**
 * Get the file or folder matching the given name in any of the given folders.
 * <p>
 * Note that using this method expects to find one and only one matching file.
 * </p>
 * @param fileName The file or folder name
 * @param folders The folders list where the file is supposed to be
 * @return The found file or folder
 * @throws ScenarioFailedError if the file or folder is not found in current folder and its hierarchy
 * or if several files are found
 */
protected File getFolderFile(final String fileName, final List<File> folders) throws ScenarioFailedError {
	debugPrintEnteringMethod("fileName", fileName, "folders", getTextFromList(folders));
	List<File> files = new ArrayList<>();
	for (File projectsFolder: folders) {
		debugPrintln("		  -> looking for files in "+projectsFolder);
		files.addAll(findFile(getSafeStringForPath(fileName), projectsFolder, null));
	}
	debugPrintln("		  -> found "+files.size()+" files: "+getTextFromList(files));
	switch (files.size()) {
		case 0:
			throw new ScenarioFailedError("Cannot find file "+fileName+" in any of the projects folders ("+getTextFromList(folders)+")");
		case 1:
			return files.get(0);
		default:
			throw new ScenarioFailedError("Too many files "+fileName+" ("+getTextFromList(files)+" in all projects folders ("+getTextFromList(folders)+")");
	}
}

/**
 * Get the content of the file found using {@link #getFolderFile(String, List)} method.
 *
 * @param fileName The file or folder name
 * @param folders The folders list where the file is supposed to be
 * @return The file content
 * @throws ScenarioFailedError if the file is not found in found folders or if several
 * files are found of if found file is a directory
 */
protected String getFolderFileContent(final String fileName, final List<File> folders) throws ScenarioFailedError {
	debugPrintEnteringMethod("fileName", fileName, "folders", getTextFromList(folders));
	if (fileName == null) {
		return null;
	}
	File file = getFolderFile(fileName, folders);
	if (file.isDirectory()) {
		throw new ScenarioFailedError("Cannot get the content of "+fileName+" as this is a directory.");
	}
	try {
		return convertLineDelimitersToUnix(readFileContent(file));
	} catch (IOException e) {
		throw new ScenarioFailedError(e);
	}

}
}
