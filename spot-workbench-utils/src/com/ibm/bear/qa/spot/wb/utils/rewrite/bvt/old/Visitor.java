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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;
import com.ibm.bear.qa.spot.wb.utils.rewrite.bvt.old.SeleniumActionVisitor.BvtMethod;

@SuppressWarnings("unchecked")
public class Visitor extends SpotAbstractVisitor {

	class BvtTimeoutLoop {
		ForStatement loop;
//		List<MethodInvocation> bvtMethods = null;
		BvtMethod bvtMethod = null;

		public BvtTimeoutLoop(final ForStatement forStatement) {
	        this.loop = forStatement;
	        readLoopBlock((Block)forStatement.getBody());
        }

		boolean readLoopBlock(final Block block) {
	    	List<Statement> forBlockStatements = block.statements();

	    	// Get failure statement from first expression which should be the timeout condition to fail
    		Statement failureStatement = null;
    		Expression timeoutExpression = null;
    		IfStatement ifStatement = (IfStatement) forBlockStatements.get(0);
    		if (ifStatement.getElseStatement() == null) {
    			InfixExpression infixExpression = (InfixExpression) ifStatement.getExpression();
    			if (infixExpression.getLeftOperand().toString().equals("second") &&
    				infixExpression.getOperator().equals(Operator.GREATER_EQUALS)) {
		    		failureStatement = ifStatement.getThenStatement();
		    		timeoutExpression = infixExpression.getRightOperand();
	    		}
	    	}
	    	if (failureStatement == null || timeoutExpression == null) {
                return false;
            }

	    	// Get selenium actions
    		Block tryBlock = ((TryStatement) forBlockStatements.get(1)).getBody();
	    	List<Statement> tryBlockStatements = tryBlock.statements();
	    	if (tryBlockStatements.size() == 1) {
		    	switch (tryBlockStatements.get(0).getNodeType()) {
		    		case ASTNode.IF_STATEMENT: // typically wait for element or text present
			    		IfStatement tryIfStatement = (IfStatement) tryBlockStatements.get(0);
			    		if (tryIfStatement.getElseStatement() == null) {
			    			Statement tryIfStatementThenStatement = tryIfStatement.getThenStatement();
			    			boolean validThenStatement = tryIfStatementThenStatement.getNodeType() == ASTNode.BREAK_STATEMENT;
			    			if (!validThenStatement && tryIfStatementThenStatement.getNodeType() == ASTNode.BLOCK) {
			    				Block thenStatementBlock = (Block) tryIfStatementThenStatement;
			    				validThenStatement = thenStatementBlock.statements().size() == 1 && ((ASTNode)thenStatementBlock.statements().get(0)).getNodeType() == ASTNode.BREAK_STATEMENT;
			    			}
			    			if (validThenStatement) {
								SeleniumActionVisitor seleniumActionVisitor = new SeleniumActionVisitor(Visitor.this, timeoutExpression);
								tryIfStatement.getExpression().accept(seleniumActionVisitor);
//								return (this.bvtMethods = seleniumActionVisitor.bvtMethods) != null;
								return (this.bvtMethod = seleniumActionVisitor.bvtMethod) != null;
			    			}
			    		}
			    		break;
		    	}
	    	}
	    	return false;
		}

		boolean isValid() {
//            return this.bvtMethods != null && this.bvtMethods.size() > 0;
			return this.bvtMethod != null;
        }

		@Override
        public String toString() {
	        return this.loop.toString();
        }

		void replace(final MethodInvocation method) {
	        if (isValid()) {
	        	// Init
	        	int size = 1;

	        	// Get comments before the loop
				ASTNode[] comments = getLeadingComments(this.loop);
				ASTNode[] nodes;
				int start = 0;
				if (comments == null) {
					nodes = new ASTNode[size];
				} else {
					start = comments.length;
					nodes = new ASTNode[size+start];
					System.arraycopy(comments, 0, nodes, 0, start);
				}

				// Modify first method invocation if necessary
				if (method != null) {
					final String methodName = method.getName().toString();
					if (isReplaceableMethod(methodName)) {
						final String bvtMethodName = this.bvtMethod.method.getName().toString();
						final String bvtMethodArgument = this.bvtMethod.method.arguments().get(0).toString();
						final String methodArgument = method.arguments().get(0).toString();
						if (bvtMethodName.equals("waitForElementPresent") && bvtMethodArgument.equals(methodArgument)) {
							replaceMethod(method, bvtMethodArgument, comments, null);
							return;
						}
						else if (bvtMethodName.equals("waitForElementAbsent")) {
							final String argument = methodArgument.substring(0, methodArgument.length()-2)+" and @disabled='']\"";
							if (bvtMethodArgument.equals(methodArgument) || (bvtMethodArgument.equals(argument))) {
								replaceMethod(method, bvtMethodArgument, comments, methodName.equals("click")?"clickButton":null);
								return;
							}
						}
					}
				}

				// Test if there's an additional variable declaration
				if (this.bvtMethod.locators != null) {
					if (start == 1) {
						ASTNode comment = nodes[0];
						nodes = new ASTNode[size+2];
						nodes[0] = comment;
					} else {
						nodes = new ASTNode[size+1];
					}
					nodes[start] = Visitor.this.ast.newExpressionStatement(this.bvtMethod.locators);
					start++;
				}
//	        	for (int i=start; i<nodes.length; i++) {
//					nodes[i] = BvtRewriteVisitor.this.ast.newExpressionStatement(this.bvtMethods.get(i-start));
					nodes[start] = Visitor.this.ast.newExpressionStatement(this.bvtMethod.method);
//	        	}
	        	ASTNode groupNode = Visitor.this.rewriter.createGroupNode(nodes);
	        	// Replace the loop with the group
//				BvtRewriteVisitor.this.rewriter.replace(this.loop, groupNode, null);
				Visitor.this.rewriter.replace(this.loop, groupNode, null);
	        }
        }

		@SuppressWarnings("unused")
        private boolean replaceMethod(final MethodInvocation method, final String bvtMethodArgument, final ASTNode[] comments, final String newName) {

			// Remove the loop
			replaceWithComments(this.loop, comments);

        	// Remove the pause if any
        	Expression newPauseValue = null;
        	if (Visitor.this.pause != null) {
	        	replaceWithComments(Visitor.this.pause.statement, Visitor.this.pause.comments);
    	    	newPauseValue = Visitor.this.pause.newValue();
        	}

        	// Create new method
       		MethodInvocation newMethod = (MethodInvocation) ASTNode.copySubtree(Visitor.this.ast, method);
       		if (newName != null) {
       			newMethod.setName(Visitor.this.ast.newSimpleName(newName));
	        	if (newPauseValue == null) {
       				Visitor.this.rewriter.replace(method, newMethod, null);
       				return true;
//	        		return newMethod;
	        	}
       		}

       		// Add pause argument if any
        	if (newPauseValue != null) {
        		switch (method.arguments().size()) {
        			case 1:
        				newMethod.arguments().add(newPauseValue);
        				Visitor.this.rewriter.replace(method, newMethod, null);
        				return true;
//		        		return newMethod;
        			case 2:
        				ASTNode firstArgument = ASTNode.copySubtree(Visitor.this.ast, (ASTNode) method.arguments().get(0));
        				newMethod.arguments().add(firstArgument);
        				newMethod.arguments().add(newPauseValue);
        				Visitor.this.rewriter.replace(method, newMethod, null);
        				return true;
//		        		return newMethod;
        		}
        	}
	        return false;
        }

		private void replaceWithComments(final ASTNode node, final ASTNode[] comments) {
	        if (comments == null) {
	        	Visitor.this.rewriter.remove(node, null);
			} else {
				if (comments.length == 1) {
		        	Visitor.this.rewriter.replace(node, comments[0], null);
				} else {
		        	ASTNode groupComments = Visitor.this.rewriter.createGroupNode(comments);
		        	Visitor.this.rewriter.replace(node, groupComments, null);
				}
			}
        }
	}

static String getSeconds(final int value) {
	switch (value) {
		case 1:
			return "ONE_SECOND";
		case 2:
			return "TWO_SECONDS";
		case 3:
			return "THREE_SECONDS";
		case 4:
			return "FOUR_SECONDS";
		case 5:
			return "FIVE_SECONDS";
		default:
			return "XXX_SECONDS";
	}
}

	BvtTimeoutLoop timeoutLoop;
	Pause pause;
	Block currentBlock = null;

@SuppressWarnings("rawtypes")
ASTNode[] getLeadingComments(final ASTNode node) {
	ASTNode[] comments = null;
	int firstCommentIndex = Visitor.this.astRoot.firstLeadingCommentIndex(node);
	int start = 0;
	if (firstCommentIndex >= 0) {
		final List commentList = Visitor.this.astRoot.getCommentList();
		final int size = commentList.size();
		start = 1;
		int index = firstCommentIndex + start;
		while (index < size && ((Comment) commentList.get(index)).getStartPosition() < node.getStartPosition()) {
			start++;
			index++;
		}
		comments = new ASTNode[start];
		for (int c=0; c<start; c++) {
			Comment comment = (Comment) commentList.get(firstCommentIndex+c);
			String commentString = Visitor.this.source.substring(comment.getStartPosition(), comment.getStartPosition()+comment.getLength()) + LINE_SEPARATOR;
			comments[c] = Visitor.this.rewriter.createStringPlaceholder(commentString, comment.getNodeType());
		}
	}
	return comments;
}

boolean isReplaceableMethod(final String methodName) {
	return methodName.equals("click") ||
		methodName.equals("mouseOver") ||
		methodName.equals("type") ||
		methodName.equals("select");
}

Visitor(final CompilationUnit root, final ICompilationUnit cu) {
	super(root, cu);
}

@Override
public boolean visit(final MethodDeclaration node) {
	this.timeoutLoop = null;
	return true;
}

@Override
public boolean visit(final Block block) {
	this.currentBlock = block;
	return super.visit(block);
}

@Override
public void endVisit(final MethodDeclaration node) {
	if (this.timeoutLoop!= null) {
		this.timeoutLoop.replace(null);
	}
	this.pause = null;
	this.timeoutLoop = null;
}

@Override
public boolean visit(final MethodInvocation method) {
	final String methodName = method.getName().toString();
	if (methodName.equals("click") && method.getExpression() == null) {
		if (this.timeoutLoop == null) {
			if (method.arguments().size() == 2) {
				int pauseMilliseconds = 0;
				if (this.pause != null) {
					pauseMilliseconds = Pause.sleepMillisecondsValue((Expression)this.pause.method.arguments().get(0));
					this.rewriter.remove(this.pause.statement, null);
				}
				final Expression seconds = (Expression) method.arguments().get(1);
				if (pauseMilliseconds > 0 || seconds.getNodeType() != ASTNode.NUMBER_LITERAL || Integer.parseInt(seconds.toString()) >= 1000) {
					int milliseconds = Pause.sleepMillisecondsValue(seconds) + pauseMilliseconds;
					if (milliseconds > 0) {
						MethodInvocation newMethod = this.ast.newMethodInvocation();
						newMethod.setName(this.ast.newSimpleName(methodName));
						ASTNode firstArgument = ASTNode.copySubtree(Visitor.this.ast, (ASTNode) method.arguments().get(0));
						newMethod.arguments().add(firstArgument);
						newMethod.arguments().add(this.ast.newNumberLiteral((milliseconds/1000)+"/*sec*/"));
						Visitor.this.rewriter.replace(method, newMethod, null);
						return false;
					}
				}
			}
		}
	}
	if (methodName.equals("sleep")) {
		this.pause = new Pause(this.ast, method, getLeadingComments(method));
	    return false;
	}
	if (methodName.equals("fail")) {
		if (method.getExpression() == null) {
			ASTNode grandParent = method.getParent().getParent();
			if (grandParent.getNodeType() != ASTNode.METHOD_DECLARATION || !((MethodDeclaration)grandParent).getName().toString().equals("cancelExecution")) {
				ASTNode newArgument = ASTNode.copySubtree(this.ast, (ASTNode) method.arguments().get(0));
				MethodInvocation newMethod = this.ast.newMethodInvocation();
				newMethod.setName(this.ast.newSimpleName("cancelExecution"));
				newMethod.arguments().add(newArgument);
				this.rewriter.replace(method, newMethod, null);
			    return false;
			}
		}
	} else if (methodName.equals("pause")) {
		this.pause = new Pause(this.ast, method, getLeadingComments(method));
		return false;
	}
	if (this.timeoutLoop != null) {
		this.timeoutLoop.replace(method);
		this.timeoutLoop = null;
		this.pause = null;
	}
	if (String.valueOf(method.getExpression()).equals("this.selenium")) {
		if (!this.unit.getElementName().equals("BvtTestCase.java")) {
			if (isReplaceableMethod(methodName)) {
				MethodInvocation newMethod = (MethodInvocation) ASTNode.copySubtree(this.ast, method);
				newMethod.setExpression(null);
				replaceMethod(method, newMethod);
				return false;
			}
		}
	}
	if (this.pause != null) {
		MethodInvocation pauseMethod = this.pause.method;
		if (pauseMethod.getExpression() != null && pauseMethod.getExpression().toString().equals("Thread")) {
			Expression argument = (Expression) pauseMethod.arguments().get(0);
			int seconds = Pause.sleepMillisecondsValue(argument);
			if (seconds > 0) {
				MethodInvocation newPauseMethod = this.ast.newMethodInvocation();
				newPauseMethod.setName(this.ast.newSimpleName("pause"));
				newPauseMethod.arguments().add(this.ast.newSimpleName(getSeconds(seconds)));
				this.rewriter.replace(pauseMethod, newPauseMethod, null);
			}
		}
		this.pause = null;
	}
	return true;
}

private void replaceMethod(final MethodInvocation method, final MethodInvocation newMethod) {
	int commentIndex;
	commentIndex = this.astRoot.firstLeadingCommentIndex(method);
	if (commentIndex == -1) {
		// no comment before the method invocation, just replace it
		this.rewriter.replace(method, newMethod, null);
	} else {
		Comment comment = (Comment) this.astRoot.getCommentList().get(commentIndex);
		final String commentString = this.source.substring(comment.getStartPosition(), comment.getStartPosition()+comment.getLength());
		String newMethodString = commentString + LINE_SEPARATOR + newMethod;
		this.rewriter.replace(method, this.rewriter.createStringPlaceholder(newMethodString, method.getNodeType()), null);
	}
}

/**
 * Replace all timeout loop. Only works for for loop at top level of method declaration...
 */
@Override
public boolean visit(final ForStatement forStatement) {
	if (this.timeoutLoop!= null) {
		this.timeoutLoop.replace(null);
	}
	this.pause = null;
	this.timeoutLoop = null;
	/*
	 *  Timeout loop are defined as followed
	 *  for (int second=0;;second++) {...}
	 */
	List<Expression> initializers = forStatement.initializers();
	List<Expression> updaters = forStatement.updaters();
	if (initializers.size() == 1 && initializers.get(0).toString().equals("int second=0") &&
		forStatement.getExpression() == null &&
		updaters.size() == 1 && updaters.get(0).toString().equals("second++")) {
		// We're in a typical BVT waiting loop, create the specific object
		// and continue the visit...
		try {
	        this.timeoutLoop = new BvtTimeoutLoop(forStatement);
        } catch (@SuppressWarnings("unused") ClassCastException cce) {
	        // Not possible to replace the loop, skip
        }
	}
	// Do not visit children
	return false;
}

@Override
public boolean visit(final TryStatement node) {
	if (node.getParent().getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
		this.timeoutLoop = null;
		return true;
	}
	return super.visit(node);
}

}