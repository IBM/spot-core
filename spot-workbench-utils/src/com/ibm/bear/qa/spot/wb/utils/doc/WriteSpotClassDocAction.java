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
package com.ibm.bear.qa.spot.wb.utils.doc;

import java.util.*;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.ibm.bear.qa.spot.wb.utils.rewrite.RewriteAction;

/**
 * Class implementing an action which writes the public and internal API methods
 * in the type declaration javadoc comment of the classes of the selected java element.
 * <p>
 * Basically the action is traversing the class AST to detect public and protected
 * method declarations and add a reference to each method in a specific paragraph
 * of the class javadoc comment.
 * </p><p>
 * Already existing method references are removed and replaced by current
 * accurate ones. Note that the entire content of the 2 paragraphs including these method
 * references are removed, all other paragraphs are kept. The added or updated method
 * references are moved at the end of the javadoc comment.
 * </p><p>
 * The selected element can be any {@link IJavaElement Java element} in the
 * package explorer. Its content is explored to find all Java classes and execute
 * the corresponding action on each of them.
 * </p>
 */
public class WriteSpotClassDocAction extends RewriteAction<SpotClassDocVisitor> {

	private List<ICompilationUnit> classesMissingJavadoc = new ArrayList<>();
	private Map<ICompilationUnit, List<MethodDeclaration>> methodsMissingJavadoc = new HashMap<>();

@Override
protected CompilationUnit createCompilationUnitAST(final ICompilationUnit cu) {
	this.resolveBindings = true;
	return super.createCompilationUnitAST(cu);
}

@Override
protected SpotClassDocVisitor createVisitor(final CompilationUnit astRoot, final ICompilationUnit cu) {
	return new SpotClassDocVisitor(astRoot, cu);
}

@Override
protected void finalizeRewrite(final SpotClassDocVisitor visitor) throws Exception {
	if (!visitor.hadJavadoc) {
		this.classesMissingJavadoc.add((ICompilationUnit)visitor.unit);
	}
	if (!visitor.missingJavadocMethods.isEmpty()) {
		this.methodsMissingJavadoc.put((ICompilationUnit)visitor.unit, visitor.missingJavadocMethods);
	}
}

@Override
protected String format(final ICompilationUnit cu) throws Exception {
	SpotClassDocFormatVisitor formatter = new SpotClassDocFormatVisitor(cu);
	formatter.parse();
	return formatter.evaluateRewrite();
}

@Override
protected String getActionMessage() {
	return "Check and fix classes javadoc comment";
}

/**
 * {@inheritDoc}
 * <p>
 * Do not open dialog at the end of the action when there
 * was only a single class modified.
 * </p>
 */
@Override
protected StringBuffer getFinalMessage() {
	StringBuffer finalMessage = super.getFinalMessage();
	if (finalMessage == null) return null;
	int size = this.classesMissingJavadoc.size();
	if (size > 0) {
		finalMessage.append("\nFollowing class");
		if (size == 1) {
			finalMessage.append(" is");
		} else {
			finalMessage.append("es are");
		}
		finalMessage.append(" missing javadoc on their declaration:\n");
		for (ICompilationUnit cu: this.classesMissingJavadoc) {
			finalMessage.append(" - ").append(cu.getElementName());
			toStringAncestors(cu, finalMessage);
			finalMessage.append("]\n");
		}
	}
	size = this.methodsMissingJavadoc.size();
	if (size > 0) {
		finalMessage.append("\nFollowing class");
		if (size == 1) {
			finalMessage.append(" has");
		} else {
			finalMessage.append("es have");
		}
		finalMessage.append(" method declarations without any javadoc:\n");
		for (ICompilationUnit cu: this.methodsMissingJavadoc.keySet()) {
			finalMessage.append(" - ").append(cu.getElementName());
			toStringAncestors(cu, finalMessage);
			finalMessage.append("]\n");
			for (MethodDeclaration method: this.methodsMissingJavadoc.get(cu)) {
				finalMessage.append("     + ").append(method.getName()).append("\n");
			}
		}
	}
	return finalMessage;
}

@Override
protected void resetCounters() {
	super.resetCounters();
	this.methodsMissingJavadoc = new HashMap<>();
	this.classesMissingJavadoc = new ArrayList<>();
}
}
