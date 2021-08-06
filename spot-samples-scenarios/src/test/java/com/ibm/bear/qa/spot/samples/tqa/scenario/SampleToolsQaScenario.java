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

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.bear.qa.spot.core.utils.SpotScenario;
import com.ibm.bear.qa.spot.samples.tqa.steps.*;

/**
 * Scenario to test basic functionalities of <b>Sample Tools QA</b>.
 */
@SpotScenario
@RunWith(SampleToolsQaScenarioRunner.class)
@SuiteClasses({
	SampleToolsQaStep00_General.class,
	SampleToolsQaStep01_TextBox.class,
	SampleToolsQaStep02_Dialog.class,
})
public class SampleToolsQaScenario {
}
