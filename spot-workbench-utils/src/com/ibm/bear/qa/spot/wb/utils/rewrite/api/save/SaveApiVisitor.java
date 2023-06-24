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

import java.util.*;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

/**
 * Visitor which rewrites any method invocation to ScenarioStep.save(WebPage)
 * method to the WebPage.save() public method.
 * <p>
 * This method call will be replaced by calling the save() method of the SaveablePage
 * interface.
 * </p>
 * For example, the following code line: <pre>save(projectAreaPage);</pre>
 * will be replaced by:<pre>((SaveablePage)projectAreaPage).save();</pre>
 * </p><p>
 * In order to avoid to introduce any compiler error or warning, the declaration
 * class of the expression will be modified to implement the SaveablePage
 * interface (see {@link SaveablePageVisitor}).
 * </p><p>
 * Note that if the class of the caller expression overrides the save() method
 * then the cast to the interface won't be necessary. In such case,  the following
 * code line: <pre>save(propertiesPage);</pre>
 * will be simply replaced by:<pre>propertiesPage.save();</pre>

 * </p>
 */
public class SaveApiVisitor extends SaveAbstractVisitor {

	// Store the units which must implement the 'SaveablePage' interface
	List<ICompilationUnit> unitsToImplementInterface = new ArrayList<ICompilationUnit>();

protected SaveApiVisitor(final CompilationUnit root, final ICompilationUnit cu) {
	super(root, cu);
}

/*
 * Get the qualified name of the type declaration for the given node.
 */
private String getTypeDeclarationQualifiedName(final ASTNode node) {
	List<TypeDeclaration> typeDeclarations = getTypeDeclarationsList(node);
	StringBuilder builder = new StringBuilder();
	Collections.reverse(typeDeclarations);
	for (TypeDeclaration declaration: typeDeclarations) {
		if (builder.length() > 0) builder.append(".");
		builder.append(declaration.getName());
	}
	return builder.toString();
}

/*
 * Get the list of type declaration found in the node parent hierarchy.
 */
private List<TypeDeclaration> getTypeDeclarationsList(final ASTNode node) {
	ASTNode parentNode = node.getParent();
	List<TypeDeclaration> typeDeclarations = new ArrayList<TypeDeclaration>();
	while (parentNode.getNodeType() != ASTNode.COMPILATION_UNIT) {
		if (parentNode.getNodeType() == ASTNode.TYPE_DECLARATION) {
			typeDeclarations.add((TypeDeclaration) parentNode);
		}
		parentNode = parentNode.getParent();
	}
	return typeDeclarations;
}

/**
 * Check each method invocation to see if it matches the ScenarioStep.save() method.
 * <p>
 * If it matches, then the code pattern is expected to be as follows:<br>
 * <code>&nbsp;&nbsp;&nbsp;&nbsp;save(<i>expression</i>);</code>
 * <br>
 * and it will replace it with the following code pattern:<br>
 * <code>&nbsp;&nbsp;&nbsp;&nbsp;((SaveablePage)<i>expression</i>).save();</code>
 * </p>
 */
@Override
public boolean visit(final MethodInvocation methodInvocation) {

	// Get method name
	final String methodName = methodInvocation.getName().toString();

	// Check if pattern match ScenarioStep.save()
	if (methodName.equals("save") && methodInvocation.getExpression() == null && methodInvocation.arguments().size() == 1) {

		// Resolve binding (that's time costly but we need to be sure about the method invocation declaration class
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding typeBinding = methodBinding.getDeclaringClass();

		// Check that it's the ScenarioStep.save() call
		if (typeBinding.getName().equals("ScenarioStep")) {

			// Some info in the console
			System.out.println("Replace save() method invocation found in "+getTypeDeclarationQualifiedName(methodInvocation)+"."+methodName+"(): ");
			System.out.println("	- current: "+methodInvocation);

			// Resolve argument as we need its declaring class
			Expression argument = (Expression) methodInvocation.arguments().get(0);
			ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();

			// Add declaring of class of argument to the list as we will need to implement the SaveablePage interface (later)
			ICompilationUnit argumentUnit = (ICompilationUnit) argumentTypeBinding.getJavaElement().getParent();
			this.unitsToImplementInterface.add(argumentUnit);

			// Check a cast is needed or not
			boolean needCast = true;
			if (Modifier.isAbstract(argumentTypeBinding.getModifiers())) {
				// If the expression declaration class is abstract, we need to force a cast
				// because only leaf classes are supposed not to produce a Discouraged Access warning
			} else {
				// If the expression declaring class implements save() method,
				// then no cast is necessary
				for (IMethodBinding mBinding: argumentTypeBinding.getDeclaredMethods()) {
					if (mBinding.getName().equals("save") && mBinding.getParameterTypes().length == 0) {
						// Declaring class of the expression implements the save() method,
						// Hence cast should not be necessary
						needCast = false;
						break;
					}
				}
			}

			// New expression will depend whether the case is necessary or not
			Expression newExpression;
			if (needCast) {
				// Create the cast expression
				CastExpression castExpression = this.ast.newCastExpression();
				castExpression.setType(this.ast.newSimpleType(this.ast.newSimpleName(INTERFACE_NAME)));
				castExpression.setExpression((Expression) ASTNode.copySubtree(this.ast, argument));

				// Create the parenthesis expression as we need to put cast expression in parenthesis to be able to send the save()
				ParenthesizedExpression parenthesizedExpression = this.ast.newParenthesizedExpression();
				parenthesizedExpression.setExpression(castExpression);

				// The parenthesized expression will be the new expression
				newExpression = parenthesizedExpression;

				// Tells that the interface needs to be added to imports
				this.needsInterfaceImport = true;
			} else {
				// Copy the argument as it will become the new expression
				newExpression = (Expression) ASTNode.copySubtree(this.ast, argument);
			}

			// Create the new method invocation
			MethodInvocation newMethodInvocation = this.ast.newMethodInvocation();
			newMethodInvocation.setExpression(newExpression);
			newMethodInvocation.setName(this.ast.newSimpleName("save"));
			System.out.println("	- new: "+newMethodInvocation);

			// Replace the method invocation with new one
			this.rewriter.replace(methodInvocation, newMethodInvocation, null);

			// Increment the number of changes done while parsing the unit
			this.changes++;
		}
	}
	return true;
}
}