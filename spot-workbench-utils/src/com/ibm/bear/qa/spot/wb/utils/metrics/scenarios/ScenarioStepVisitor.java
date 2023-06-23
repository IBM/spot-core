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

/**
 * Visitor called when parsing a scenario step unit.
 */
public class ScenarioStepVisitor extends ASTVisitor {

	/*
	 * Not used yet.
	 */
	List<String> comments = new ArrayList<String>();
	/*
	 * Not used yet. Will be necessary to retrieve comments content.
	 */
	protected String source;

	/**
	 * List of tests found in the step.
	 * <p>
	 * These are method which have the @Test annotation.
	 * </p>
	 */
	List<String> tests = new ArrayList<String>();

	/*
	 * No longer used. Might be useful if we want to analyse the method
	 * delcaration in order to filter some test with the @Test annotation
	 * (e.g. empty methods, @Ignore annotated, etc.)
	 */
	boolean storeTests = false;


@SuppressWarnings("unused")
ScenarioStepVisitor(final ICompilationUnit cu) {
	// Activate following code to store the source
//	try {
//		this.source = cu.getSource();
//	} catch (JavaModelException e) {
//		e.printStackTrace();
//	}
}

/*
 * Store javadoc comments in the list.
 * TODO Not used yet
 */
//@SuppressWarnings("unchecked")
//@Override
//public void endVisit(final CompilationUnit node) {
//	super.endVisit(node);
//
//	// Add comments
//	List<Comment> commentList = node.getCommentList();
//	for (Comment comment: commentList) {
//		if (comment.isDocComment()) {
//			this.comments.add(nodeSource(comment));
//		}
//	}
//}

/*
 * Get the source matching the given AST node.
 */
//private String nodeSource(final ASTNode node) {
//	return this.source.substring(node.getStartPosition(), node.getStartPosition()+node.getLength());
//}

/*
 * Exit annotation node, relax the tests storage flag.
 */
@Override
public void endVisit(final MarkerAnnotation node) {
	this.storeTests = false;
	super.endVisit(node);
}

/*
 * Check the annotation name and store its parent node name in case this
 * is the @Test annotation.
 */
@Override
public boolean visit(final MarkerAnnotation node) {
	if (node.getTypeName().getFullyQualifiedName().equals("Test")) {
		this.tests.add(((MethodDeclaration)node.getParent()).getName().getIdentifier());
		this.storeTests = true;
		return super.visit(node);
	}
	return false;
}
}
