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

/**
 * Visitor used to add the interface 'SaveablePage' on the parsed type.
 */
@SuppressWarnings("unchecked")
public class SaveablePageVisitor extends SaveAbstractVisitor {

public SaveablePageVisitor(final ICompilationUnit cu) {
	super(cu);
}

@Override
public boolean visit(final TypeDeclaration typeDeclaration) {

	// Check whether the type declaration already implements the interface or not
    List<Type> interfaces = typeDeclaration.superInterfaceTypes();
	for (Type interfaceType: interfaces) {
		if (interfaceType.isSimpleType()) {
			if (((SimpleType)interfaceType).getName().toString().equals(INTERFACE_NAME)) {
				return false;
			}
		}
		else if (interfaceType.isQualifiedType()) {
			if (((QualifiedType)interfaceType).getName().toString().equals(INTERFACE_NAME)) {
				return false;
			}
		}
	}

	// Create the new interface type node
	SimpleType newInterfaceType = this.ast.newSimpleType(this.ast.newSimpleName(INTERFACE_NAME));

	// Create the list rewriter
	ListRewrite listRewrite = this.rewriter.getListRewrite(typeDeclaration, TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);

	// Add the interface as the last of the list
	listRewrite.insertLast(newInterfaceType, null);

	// Store that the interface has been added (will be used to add corresponding import)
	this.needsInterfaceImport = true;

	// Print
	System.out.println("Interface was added '"+INTERFACE_NAME+"' to type "+typeDeclaration.getName());

	// It's not necessary to continue the unit parse
	return false;
}
}
