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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Model object to handle scenario step information.
 */
public class ScenarioStep extends ScenarioObject {

	List<String> tests;

public ScenarioStep(final ICompilationUnit step) {
	super(step);
}

public List<String> getTests() {
	return this.tests;
}

public void setTests(final List<String> list) {
	this.tests = new ArrayList<String>(list);
}
}
