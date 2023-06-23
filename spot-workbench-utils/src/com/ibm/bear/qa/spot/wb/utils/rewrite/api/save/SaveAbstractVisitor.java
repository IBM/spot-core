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
package com.ibm.bear.qa.spot.wb.utils.rewrite.api.save;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;

/**
 * Common visitor to visit unit to replace save() API method call.
 */
@SuppressWarnings("unchecked")
public abstract class SaveAbstractVisitor extends SpotAbstractVisitor {

	/* Constants */
	static final String INTERFACE_NAME = "JazzSaveablePage";
	private static final String INTERFACE_QUALIFIED_NAME = "com.ibm.team.cspf.jazz.pages." + INTERFACE_NAME;
	private final static String[] PACKAGE_IMPORT = INTERFACE_QUALIFIED_NAME.split("\\.");

	/* Fields */
	// Flag to tell whether the interface import is needed or not
	boolean needsInterfaceImport = false;

public SaveAbstractVisitor(final ICompilationUnit cu) {
	super(cu);
}

public SaveAbstractVisitor(final CompilationUnit root, final ICompilationUnit cu) {
	super(root, cu);
}

/**
 * Add the import declaration to 'JazzSaveablePage' interface in case it has been
 * added as implementor of the unit type declaration.
 * <p>
 * Try to insert the declaration respecting the usual sort order used for organized
 * imports (ie. static, first, then java.* and finally sorted alphabetically).
 * </p>
 */
@Override
public void endVisit(final CompilationUnit compilationUnit) {
	if (this.needsInterfaceImport) {

		// Create new import declaration node for the interface
		ImportDeclaration newImportDeclaration = this.ast.newImportDeclaration();
		Name importName = this.ast.newName(PACKAGE_IMPORT);
		newImportDeclaration.setName(importName);

		// Get the list rewriter
		ListRewrite listRewrite = this.rewriter.getListRewrite(compilationUnit, CompilationUnit.IMPORTS_PROPERTY);

		// Loop on existing imports to find the best place to insert the new import
		for (ImportDeclaration importDeclaration: ((List<ImportDeclaration>)compilationUnit.imports())) {
			String importDeclarationName = importDeclaration.getName().toString();
			if (!importDeclaration.isStatic() && importDeclarationName.startsWith("com.ibm.team") && importDeclarationName.compareTo(INTERFACE_QUALIFIED_NAME) > 0) {
				listRewrite.insertBefore(newImportDeclaration, importDeclaration, null);
				break;
			}
		}
	}
	super.endVisit(compilationUnit);
}
}
