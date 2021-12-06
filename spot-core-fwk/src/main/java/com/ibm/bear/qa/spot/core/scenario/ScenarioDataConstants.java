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

public interface ScenarioDataConstants {

	// Test prefix
	String TEST_PREFIX_PARAM_DEFAULT_VALUE = EMPTY_STRING;
	String TEST_PREFIX_PARAM_ID = "testPrefix";
	String RANDOM_PREFIX_PARAM_ID = "randomPrefix";

	// Default Test User
	String TEST_USER_ID = "test";
	String TEST_USERID = "CDUser";

	/**
	 * Define root directory where all data related artifacts of the test plug-in are located.
	 */
	String DATA_ROOT_DIR = getParametersValue("dataRootDir");


	/**
	 * Define the directory where output files are to be put by default.
	 */
	String OUTPUT_DIR = getParameterValue("output.dir", "./output");
}
