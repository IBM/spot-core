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
import org.eclipse.jdt.core.dom.*;

import com.ibm.bear.qa.spot.wb.utils.metrics.scenarios.Scenario.Type;

/**
 * Visitor called when parsing a scenario unit.
 */
public class ScenarioVisitor extends ASTVisitor {

	/**
	 * Steps list storage.
	 */
	List<SimpleType> steps = new ArrayList<SimpleType>();

	/**
	 * Import declarations list storage.
	 */
	List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();

	/**
	 * Scenario unit.
	 */
	protected ICompilationUnit scenarioUnit;

	/**
	 * The scenario type.
	 */
	Type scenarioType = Type.Undefined;
	int scenarioRuns = 1;
	boolean scenarioMaintenance = true;

ScenarioVisitor(final ICompilationUnit cu) {
	this.scenarioUnit = cu;
}

/*
 * Add the parse import delcaration to the list.
 */
@Override
public boolean visit(final ImportDeclaration node) {
	this.imports.add(node);
	return super.visit(node);
}

/*
 * Entering a single member annotation.
 * If it's the @SuiteClasses annotation then analyse its content to store
 * the list of step types defined in it.
 * If it's the @SpotScenario annotation then store the scenario kind
 */
@SuppressWarnings("unchecked")
@Override
public boolean visit(final SingleMemberAnnotation node) {
	String annotationName = node.getTypeName().getFullyQualifiedName();
	if (annotationName.equals("SuiteClasses")) {
		ArrayInitializer initializer = (ArrayInitializer) node.getValue();
		List<TypeLiteral> expressions = initializer.expressions();
		for (TypeLiteral typeLiteral: expressions) {
			SimpleType type = (SimpleType) typeLiteral.getType();
			this.steps.add(type);
		}
	} else if (annotationName.equals("SpotScenario")) {
		String typeName = ((StringLiteral)node.getValue()).getLiteralValue();
		this.scenarioType = Type.fromText(typeName);
		if (this.scenarioType == null) {
			System.err.println("SpotScenario(\""+typeName+"\") found in "+this.scenarioUnit.getElementName()+" does not refer to a valid Scenario type.");
		}
		switch(this.scenarioType) {
			case Pipeline:
			case Monitored:
				this.scenarioRuns = 1;
				break;
			default:
				break;
		}
	}
	return false;
}

@Override
public boolean visit(final MarkerAnnotation node) {
	String annotationName = node.getTypeName().getFullyQualifiedName();
	if (annotationName.equals("SpotScenario")) {
		this.scenarioType = Type.Private;
	}
	return false;
}

@SuppressWarnings("rawtypes")
@Override
public boolean visit(final NormalAnnotation node) {
	String annotationName = node.getTypeName().getFullyQualifiedName();
	if (annotationName.equals("SpotScenario")) {
		List values = node.values();
		for (Object val: values) {
			MemberValuePair valuePair = (MemberValuePair) val;
			String valueName = valuePair.getName().getIdentifier();
			Expression value = valuePair.getValue();
			if (valueName.equals("value")) {
				String typeName = ((StringLiteral)value).getLiteralValue();
				this.scenarioType = Type.fromText(typeName);
				if (this.scenarioType == Type.Undefined) {
					System.err.println("SpotScenario(\""+typeName+"\") found in "+this.scenarioUnit.getElementName()+" does not refer to a valid Scenario type.");
				}
			} else if (valueName.equals("runs")) {
//				String runsValue;
//				if (value.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
//					PrefixExpression prefixExpression = (PrefixExpression)value;
//					runsValue = prefixExpression.getOperator().toString()+((NumberLiteral)prefixExpression.getOperand()).getToken();
//				} else {
//					runsValue = ((NumberLiteral)value).getToken();
//				}
				this.scenarioRuns = Integer.parseInt(value.toString());
			} else if (valueName.equals("maintenance")) {
				this.scenarioMaintenance = Boolean.parseBoolean(value.toString());
			}
		}
		switch(this.scenarioType) {
			case Monitored:
			case Pipeline:
				if (this.scenarioRuns == 0) {
					this.scenarioRuns = 1;
				}
				break;
			default:
				break;
		}
	}
	return false;
}
}
