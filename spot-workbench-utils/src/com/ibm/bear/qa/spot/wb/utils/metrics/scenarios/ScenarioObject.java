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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;

/**
 * Model object to handle a scenario object (ie. either a scenario or a step).
 */
public abstract class ScenarioObject implements Comparable<ScenarioObject> {

	/*
	 * Not used yet, just added here to allow the metric tool to also generate
	 * javadoc documentation and help to update scenarios catalogue wiki pages...
	 */
	Javadoc javadoc;

	/**
	 * The associated compilation unit (ie. the java file in JDT/Core Java Model).
	 */
	ICompilationUnit unit;

	/**
	 * The object name. That's basically the compilation unit name without the
	 * ".java" extension.
	 */
	String name;

public ScenarioObject(final ICompilationUnit unit) {
	super();
	this.unit = unit;
}

@Override
public int compareTo(final ScenarioObject obj) {
	return this.name.compareTo(obj.name);
}

@Override
public boolean equals(final Object o) {
	if (o instanceof ScenarioObject) {
		return this.name.equals(((ScenarioObject)o).name);
	}
	return super.equals(o);
}

/**
 * Return the object name.
 *
 * @return The name
 */
public String getName() {
	if (this.name == null) {
		this.name = this.unit.getElementName().replaceAll(".java", "");
	}
	return this.name;
}

@Override
public int hashCode() {
	if (getName() != null) {
		return this.name.hashCode();
	}
	return super.hashCode();
}

/**
 * TODO Not used yet...
 *
 * @param javadoc
 */
public void setJavadoc(final Javadoc javadoc) {
	this.javadoc = javadoc;
}

@Override
public String toString() {
	if (getName() != null) {
		return this.name;
	}
	return super.toString();
}
}
