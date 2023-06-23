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

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;

public class SpotMethodSummaryVisitor extends SpotAbstractVisitor {

	IMethodBinding method;
	String methodJavadocSummary;

public SpotMethodSummaryVisitor(final ITypeRoot cu, final IMethodBinding binding) {
	super(cu);
	this.method = binding;
}

@Override
public boolean visit(final MethodDeclaration node) {
	if (node.resolveBinding().isEqualTo(this.method)) {
		this.methodJavadocSummary = getJavadocSummary(node.getJavadoc());
	}
	return false;
}

}
