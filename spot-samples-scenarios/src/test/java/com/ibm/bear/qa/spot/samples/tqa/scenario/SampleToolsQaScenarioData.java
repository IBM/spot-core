/*********************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.samples.tqa.scenario;

import com.ibm.bear.qa.spot.core.scenario.ScenarioData;

/**
 * Manage data needed while running an Sample Tools QA scenario.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #getCurrentAddress()}: Return the current address.</li>
 * <li>{@link #getEmail()}: Return the email.</li>
 * <li>{@link #getFullName()}: Return the full name.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #initUsers()}: Initialize users which will be used all over the scenario steps.</li>
 * </ul>
 * </p>
 */
public class SampleToolsQaScenarioData extends ScenarioData {


public SampleToolsQaScenarioData() {
}
/**
 * Return the current address.
 *
 * @return The current address.
 */
public String getCurrentAddress() {
	return "IBM France Paris Lab\n" +
		"sis Immeuble Renoir\n" +
		"rue d'Arsonval\n" +
		"Orsay, 91400\n" +
		"France";
}

/**
 * Return the email.
 *
 * @return The email.
 */
public String getEmail() {
	return "john_doe@gmail.com";
}

/**
 * Return the full name.
 *
 * @return The full name.
 */
public String getFullName() {
	return "John Doe";
}

@Override
protected void initUsers() {
	// No user for sample scenario
}
}
