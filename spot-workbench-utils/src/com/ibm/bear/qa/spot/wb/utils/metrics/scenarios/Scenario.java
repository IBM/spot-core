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
 * Model object to handle scenario information.
 */
public class Scenario extends ScenarioObject {

	public enum Type {
		Undefined,
		Private,
		Pipeline,
		Monitored,
		Demo,
		Perfs;
		public static Type fromText(final String text) {
			for (Type type: values()) {
				String typeName = type.toString();
				if (typeName.equalsIgnoreCase(text)) {
					return type;
				}
			}
			return Undefined;
		}
	}

	final Type type;
	final int runs;
	final boolean maintenance;
	List<ScenarioStep> steps = new ArrayList<ScenarioStep>();

public Scenario(final ICompilationUnit scenario, final Type scenarioType, final int scenarioRuns, final boolean maintenance) {
	super(scenario);
	this.type = scenarioType;
	this.runs = scenarioRuns;
	this.maintenance = maintenance;
}

public ScenarioStep addStep(final ICompilationUnit stepUnit) {
	ScenarioStep step = new ScenarioStep(stepUnit);
	this.steps.add(step);
	return step;
}

public int getRuns() {
	return this.runs;
}

public List<ScenarioStep> getSteps() {
	return this.steps;
}

public int getTestsNumber() {
	int number = 0;
	for (ScenarioStep step: this.steps) {
		number += step.tests.size();
	}
	return number;
}

public Type getType() {
	return this.type;
}

public boolean isCountable() {
	return this.type != Type.Demo && this.type != Type.Undefined;
}

public boolean isMaintenance() {
	return this.maintenance;
}
}
