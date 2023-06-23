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

import java.util.List;

import org.eclipse.jdt.core.dom.*;

import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;

@SuppressWarnings({"unchecked", "hiding","rawtypes"})
public class SeleniumActionVisitor extends SpotAbstractVisitor {

//	List<MethodInvocation> bvtMethods = new ArrayList<MethodInvocation>();
	Expression timeout;
	Pause pause;
	class BvtMethod {
		MethodInvocation method;
		boolean negative = false;
		Expression equals;
		VariableDeclarationExpression locators;
		void setMethod(final AST ast, final MethodInvocation seleniumMethod, final Expression timeout) {
			final String seleniumMethodName = seleniumMethod.getName().toString();
			if (seleniumMethodName.equals("isElementPresent")) {
				this.method = ast.newMethodInvocation();
				this.method.setName(ast.newSimpleName(this.negative? "waitForElementAbsent" : "waitForElementPresent"));
				this.method.arguments().add(ASTNode.copySubtree(ast, (ASTNode) seleniumMethod.arguments().get(0)));
				if (timeout != null && !timeout.toString().equals("TIMEOUT")) {
					this.method.arguments().add(ASTNode.copySubtree(ast, timeout));
				}
			}
			if (seleniumMethodName.equals("isTextPresent")) {
				this.method = ast.newMethodInvocation();
				this.method.setName(ast.newSimpleName(this.negative ? "waitForTextAbsent" : "waitForTextPresent"));
				this.method.arguments().add(ASTNode.copySubtree(ast, (ASTNode) seleniumMethod.arguments().get(0)));
				if (timeout != null && !timeout.toString().equals("TIMEOUT")) {
					this.method.arguments().add(ASTNode.copySubtree(ast, timeout));
				}
			}
			if (seleniumMethodName.equals("getValue") && this.equals != null) {
				this.method = ast.newMethodInvocation();
				this.method.setName(ast.newSimpleName("waitForValue"));
				this.method.arguments().add(ASTNode.copySubtree(ast, this.equals));
				this.method.arguments().add(ASTNode.copySubtree(ast, (ASTNode) seleniumMethod.arguments().get(0)));
			}
			if (seleniumMethodName.equals("isVisible")) {
				this.method = ast.newMethodInvocation();
				this.method.setName(ast.newSimpleName("waitForVisible"));
				this.method.arguments().add(ASTNode.copySubtree(ast, (ASTNode) seleniumMethod.arguments().get(0)));
			}
		}
		void updateMethod(final AST ast, final MethodInvocation seleniumMethod, final Expression timeout) {
			final String seleniumMethodName = seleniumMethod.getName().toString();
			String kind = null;
			// Update single locator method
			if (seleniumMethodName.startsWith("isElement")) {
				kind = "Element";
			} else if (seleniumMethodName.startsWith("isText")) {
				kind = "Text";
			}
			if (kind != null && seleniumMethodName.endsWith("Absent") == this.negative && seleniumMethodName.endsWith("Present") == !this.negative) {
				List arguments = this.method.arguments();
                initLocators(ast, seleniumMethod, (Expression) arguments.get(0));
                this.method.setName(ast.newSimpleName(this.negative? "waitFor"+kind+"sAbsent" : "waitFor"+kind+"sPresent"));
                resetMethodArguments(ast, timeout, arguments);
				return;
			}
			// Update multiple locators method
			if (seleniumMethodName.startsWith("waitForElements")) {
				kind = "Element";
			} else if (seleniumMethodName.startsWith("waitForTexts")) {
				kind = "Text";
			} else {
			kind = null;
			}
			if (kind != null && seleniumMethodName.endsWith("Absent") == this.negative && seleniumMethodName.endsWith("Present") == !this.negative) {
				List arguments = this.method.arguments();
                ArrayInitializer arrayInitializer;
                if (this.locators == null) {
                	initLocators(ast, seleniumMethod, (Expression) arguments.get(arguments.size()-1));
                	resetMethodArguments(ast, timeout, arguments);
                } else {
                	VariableDeclarationFragment fragment = (VariableDeclarationFragment) this.locators.fragments().get(0);
                	arrayInitializer = (ArrayInitializer) fragment.getInitializer();
                	arrayInitializer.expressions().add(ASTNode.copySubtree(ast, (ASTNode) seleniumMethod.arguments().get(0)));
                }
				return;
			}
			System.err.println("Cannot update "+this.method+" with "+seleniumMethod+", hence reset the bvt method!");
			this.method = null;
			this.locators = null;
			this.equals = null;
		}
        private void resetMethodArguments(final AST ast, final Expression timeout, final List arguments) {
	        switch (arguments.size()) {
	        	case 1:
	        		this.method.arguments().clear();
	        		this.method.arguments().add(ast.newSimpleName("TEXT_LOCATORS"));
	        		break;
	        	case 2:
	        		if (this.method.arguments().get(1).toString().equals(timeout.toString())) {
	        			this.method.arguments().clear();
	        			this.method.arguments().add(ASTNode.copySubtree(ast, timeout));
	        			this.method.arguments().add(ast.newSimpleName("TEXT_LOCATORS"));
	        		}
	        		break;
	        }
        }
        @SuppressWarnings("deprecation")
		private void initLocators(final AST ast, final MethodInvocation seleniumMethod, final Expression firstArgument) {
	        VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
	        fragment.setName(ast.newSimpleName("TEXT_LOCATORS"));
	        fragment.setExtraDimensions(1);
	        ArrayCreation arrayCreation = ast.newArrayCreation();
	        arrayCreation.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String")), 1));
	        ArrayInitializer arrayInitializer = ast.newArrayInitializer();
	        arrayInitializer.expressions().add(ASTNode.copySubtree(ast, firstArgument));
	        arrayInitializer.expressions().add(ASTNode.copySubtree(ast, (ASTNode) seleniumMethod.arguments().get(0)));
	        arrayCreation.setInitializer(arrayInitializer);
	        fragment.setInitializer(arrayCreation);
	        this.locators = ast.newVariableDeclarationExpression(fragment);
	        this.locators.setType(ast.newSimpleType(ast.newSimpleName("String")));
        }
	}
	BvtMethod bvtMethod;


SeleniumActionVisitor(final Visitor rewriteVisitor, final Expression timeout) {
	super(rewriteVisitor.astRoot, rewriteVisitor.unit);
	this.timeout = timeout;
	this.pause = rewriteVisitor.pause;
}


private void addMethod(final MethodInvocation seleniumMethod) {
//	getBvtMethod().setMethod(this.ast, seleniumMethod, this.timeout);
//	if (this.bvtMethod.method != null) {
//		this.bvtMethods.add(this.bvtMethod.method);
//	}
//	this.bvtMethod = null;
	if (getBvtMethod().method == null) {
		this.bvtMethod.setMethod(this.ast, seleniumMethod, this.timeout);
	} else {
		this.bvtMethod.updateMethod(this.ast, seleniumMethod, this.timeout);
	}
}

@Override
public boolean visit(final MethodInvocation method) {
	if (method.getExpression().getNodeType() == ASTNode.FIELD_ACCESS) {
		FieldAccess sender = (FieldAccess) method.getExpression();
		if (sender.getName().toString().equals("selenium")) {
			addMethod(method);
			return false;
		}
	}
	final String methodName = method.getName().toString();
	if (methodName.equals("equals")) {
		getBvtMethod().equals = method.getExpression();
	}
	return super.visit(method);
}


@Override
public boolean visit(final PrefixExpression prefixExpression) {
	getBvtMethod().negative = true;
	return super.visit(prefixExpression);
}

private BvtMethod getBvtMethod() {
	if (this.bvtMethod == null) {
		this.bvtMethod = new BvtMethod();
	}
	return this.bvtMethod;
}


}