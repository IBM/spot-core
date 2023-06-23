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
package com.ibm.bear.qa.spot.wb.utils.metrics;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;

/**
 * Visitor to build Metric Results.
 */
public class CodeMetricsResultsVisitor extends ASTVisitor {

	CodeMetricTypes metricTypes = new CodeMetricTypes();
	CodeMetricsMethods metricMethods = new CodeMetricsMethods();
	CodeMetricsComments metricComments = new CodeMetricsComments();

	protected String source;

CodeMetricsResultsVisitor(final ICompilationUnit cu) {
	try {
		this.source = cu.getSource();
	} catch (JavaModelException e) {
		e.printStackTrace();
	}
}

@SuppressWarnings("unchecked")
@Override
public void endVisit(final CompilationUnit node) {
	super.endVisit(node);

	// Add comments
	List<Comment> commentList = node.getCommentList();
	for (Comment comment: commentList) {
		if (comment.isLineComment()) {
			// Check whether the comment is written at line begins
			boolean wholeLine = true;
			for (int pos=comment.getStartPosition()-1; pos >= 0; pos--) {
				if (this.source.charAt(pos) == '\n') {
					break;
				}
				if (!Character.isWhitespace(this.source.charAt(pos))) {
					wholeLine = false;
					break;
				}
			}
			this.metricComments.addLine(wholeLine);
		}
		else if (comment.isBlockComment()) {
			this.metricComments.addBlock(nodeSource(comment));
		}
		else if (comment.isDocComment()) {
			this.metricComments.addJavadoc(nodeSource(comment));
		}
	}
}

private String nodeSource(final ASTNode node) {
	return this.source.substring(node.getStartPosition(), node.getStartPosition()+node.getLength());
}

@Override
public boolean visit(final AnonymousClassDeclaration node) {
	this.metricTypes.add(node);
	return super.visit(node);
}

@Override
public boolean visit(final MethodDeclaration node) {
	this.metricMethods.add(node);
	return super.visit(node);
}

@Override
public boolean visit(final TypeDeclaration node) {
	this.metricTypes.add(node, nodeSource(node));
	return super.visit(node);
}

}
