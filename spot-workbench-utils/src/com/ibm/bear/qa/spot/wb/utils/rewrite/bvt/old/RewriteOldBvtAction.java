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
package com.ibm.bear.qa.spot.wb.utils.rewrite.bvt.old;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import com.ibm.bear.qa.spot.wb.utils.rewrite.RewriteAction;

/**
 *
 */
public class RewriteOldBvtAction extends RewriteAction<Visitor> {

@Override
protected String getActionMessage() {
	return "Rewrite old BVT code";
}

@Override
protected Visitor createVisitor(final CompilationUnit astRoot, final ICompilationUnit unit) {
	return new Visitor(astRoot, unit);
}

@Override
protected String format(final ICompilationUnit unit) throws Exception {
	FormatVisitor formatter = new FormatVisitor(unit);
	formatter.parse();
	return formatter.evaluateRewrite();
}

@Override
protected boolean valid(final AbstractTypeDeclaration typeDeclaration) {
	ITypeBinding typeBinding = typeDeclaration.resolveBinding();
	while (typeBinding != null) {
		if (typeBinding.getQualifiedName().equals("com.ibm.team.fvt.clm.bvt.BvtTestCase")) {
			return true;
		}
		typeBinding = typeBinding.getSuperclass();
	}
	return false;
}
}