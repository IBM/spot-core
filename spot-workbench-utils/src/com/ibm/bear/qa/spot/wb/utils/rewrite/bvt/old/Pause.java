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

import org.eclipse.jdt.core.dom.*;

class Pause {
	AST ast;
	MethodInvocation method;
	Statement statement;
	Expression value;
	ASTNode[] comments;

static int sleepMillisecondsValue(final Expression argument) {
	switch (argument.getNodeType()) {
		case ASTNode.NUMBER_LITERAL:
			int millisec = Integer.parseInt(argument.toString());
			if (millisec >= 1000) {
				return millisec;
			}
			return millisec * 1000;
		case ASTNode.SIMPLE_NAME:
			final String identifier = ((SimpleName)argument).getIdentifier();
			if (identifier.equals("MILLIS_TO_SLEEP_BETWEEN_ACTIONS")) {
				return 1;
			}
			if (identifier.equals("ONE_SECOND")) {
				return 1000;
			} else if (identifier.equals("TWO_SECONDS")) {
				return 2000;
			} else if (identifier.equals("THREE_SECONDS")) {
				return 3000;
			} else if (identifier.equals("FOUR_SECONDS")) {
				return 4000;
			} else if (identifier.equals("FIVE_SECONDS")) {
				return 5000;
			}
			break;
		case ASTNode.INFIX_EXPRESSION:
			final InfixExpression infixExpression = (InfixExpression) argument;
			if (infixExpression.getOperator().equals(InfixExpression.Operator.TIMES)) {
				Expression left = infixExpression.getLeftOperand();
				Expression right = infixExpression.getRightOperand();
				if (left.getNodeType() == ASTNode.SIMPLE_NAME && ((SimpleName)left).getIdentifier().equals("MILLIS_TO_SLEEP_BETWEEN_ACTIONS")) {
					return Integer.parseInt(((NumberLiteral) right).getToken())*1000;
				}
				else if (right.getNodeType() == ASTNode.SIMPLE_NAME && ((SimpleName)right).getIdentifier().equals("MILLIS_TO_SLEEP_BETWEEN_ACTIONS")) {
					return Integer.parseInt(((NumberLiteral) left).getToken())*1000;
				}
			}
			break;
	}
	return -1;
}

public Pause(final AST ast, final MethodInvocation method, final ASTNode[] comments) {
	this.ast = ast;
	this.method = method;
	// Store the statement
	ASTNode parent = method.getParent();
	while (!(parent instanceof Statement)) {
		parent = parent.getParent();
	}
	this.statement = (Statement) parent;
	this.value = (Expression) method.arguments().get(0);
	this.comments = comments;
}

Expression newValue() throws ClassCastException {
	int milliseconds = sleepMillisecondsValue(this.value);
	if (milliseconds <= 0) {
	    return null;
    }
	int seconds = milliseconds / 1000;
	if ((milliseconds % 1000) != 0) {
		seconds++;
	}
	String methodName;
	switch (this.statement.getNodeType()) {
		case ASTNode.IF_STATEMENT:
			IfStatement ifStatement = (IfStatement) this.statement;
			ConditionalExpression conditionalExpression = this.ast.newConditionalExpression();
			conditionalExpression.setExpression((Expression) ASTNode.copySubtree(this.ast, ifStatement.getExpression()));
			Expression thenExpression = null;
			switch (ifStatement.getThenStatement().getNodeType()) {
				case ASTNode.BLOCK:
					Block thenBlock = (Block) ifStatement.getThenStatement();
					if (thenBlock.statements().size() == 1) {
						thenExpression = ((ExpressionStatement) thenBlock.statements().get(0)).getExpression();
					} else {
						throw new RuntimeException("Unexpected statements size.");
					}
					break;
				case ASTNode.EXPRESSION_STATEMENT:
					thenExpression = ((ExpressionStatement) ifStatement.getThenStatement()).getExpression();
					break;
				default:
					throw new RuntimeException("Unexpected node type.");
			}
			methodName = ((MethodInvocation) thenExpression).getName().toString();
			if (methodName.equals("pause") || methodName.equals("sleep")) {
				Expression methodArgument = this.ast.newNumberLiteral(seconds+"/*sec*/");
				conditionalExpression.setThenExpression(methodArgument);
				conditionalExpression.setElseExpression(this.ast.newNumberLiteral("0/*no pause*/"));
				return conditionalExpression;
			}
			break;
		case ASTNode.EXPRESSION_STATEMENT:
			// Check that the statement is a simple pause or sleep call
			Expression expression = ((ExpressionStatement)this.statement).getExpression();
			methodName = ((MethodInvocation) expression).getName().toString();
			if (methodName.equals("sleep") || methodName.equals("pause")) {
		        return this.ast.newNumberLiteral(seconds+"/*sec*/");
			}
			break;
	}
	return null;
}

@Override
public String toString() {
	return this.method.toString();
}
}