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

import java.util.Iterator;

import org.eclipse.jdt.core.dom.*;

/**
 * AST visitor for serializing an Javadoc comment in a quick fashion.
 * <p>
 * This visitor is strongly inspired from JDT/Core NaiveASTFlattener visitor.
 * However, it restrict the method overriding to {@link IDocElement} as
 * the purpose it's just to flatten the Javadoc objects hierarchy to an usable
 * text which can be put in java class clode...
 * </p>
 * @deprecated Not used...
 */
@Deprecated
public class SpotJavadocFlattener extends ASTVisitor {

	final StringBuffer buffer = new StringBuffer();
	ASTNode[] previousElements = new ASTNode[10];
	int level = -1;

public SpotJavadocFlattener() {
	super(true);
}

/**
 * Returns the string accumulated in the visit.
 *
 * @return the flattened javadoc
 */
public String getResult() {
	return this.buffer.toString();
}

/**
 * {@inheritDoc}
 * <p>
 * End the visit of a javadoc element.
 * </p><p>
 * In such case it needs to close javadoc comment text.
 * </p>
 */
@Override
public void endVisit(final Javadoc node) {
	super.endVisit(node);
	this.buffer.append("\n */");//$NON-NLS-1$
}

/**
 * {@inheritDoc}
 * <p>
 * End the visit of a tag element.
 * </p><p>
 * In such case it needs to close the brace if the tag was nested,
 * clean previous element and decrease the level.
 * </p>
 */
@Override
public void endVisit(final TagElement node) {
	super.endVisit(node);
	if (node.isNested()) {
		this.buffer.append("}");//$NON-NLS-1$
	} else {
		this.previousElements[this.level--] = null;
	}
}

/**
 * {@inheritDoc}
 * <p>
 * End the visit of a text element.
 * </p><p>
 * In such case it just needs to store current node
 * as previous element.
 * </p>
 */
@Override
public void endVisit(final TextElement node) {
	super.endVisit(node);
	this.previousElements[this.level] = node;
}

/**
 * {@inheritDoc}
 * <p>
 * End the visit of a method reference.
 * </p><p>
 * In such case it just needs to store current node
 * as previous element.
 * </p>
 */
@Override
public void endVisit(final MethodRef node) {
	super.endVisit(node);
//	this.buffer.append(")");//$NON-NLS-1$
	this.previousElements[this.level] = node;
}

/**
 * {@inheritDoc}
 * <p>
 * End the visit of a method reference parameter.
 * </p><p>
 * In such case it just needs to store current node
 * as previous element.
 * </p>
 */
@Override
public void endVisit(final MethodRefParameter node) {
	super.endVisit(node);
	this.previousElements[this.level] = node;
}

/**
 * {@inheritDoc}
 * <p>
 * Start the visit of a javadoc element.
 * </p><p>
 * In such case it just needs to open the javadoc comment text
 * and continue to visit the AST hierarchy.
 * </p>
 */
@Override
public boolean visit(final Javadoc node) {
	this.buffer.append("/** ");//$NON-NLS-1$
//	for (Iterator it = node.tags().iterator(); it.hasNext(); ) {
//		ASTNode e = (ASTNode) it.next();
//		e.accept(this);
//	}
//	this.buffer.append("\n */\n");//$NON-NLS-1$
//	return false;
	return super.visit(node);
}

/**
 * {@inheritDoc}
 * <p>
 * Start the visit of a tag element.
 * </p><p>
 * In such case it first needs to either open the brace if the tag is nested
 * or jump to next line if it's not.
 * </p><p>
 * Then, it add the tag name if any to the comment text and continue
 * to visit the AST hierarchy.
 * </p>
 */
@Override
public boolean visit(final TagElement node) {

	// See NaiveASTFlattener implementation
	if (node.isNested()) {
		// nested tags are always enclosed in braces
		this.buffer.append("{");//$NON-NLS-1$
	} else {
		this.level++;
		// top-level tags always begin on a new line
		this.buffer.append("\n * ");//$NON-NLS-1$
	}
//	boolean previousRequiresWhiteSpace = false;
	if (node.getTagName() != null) {
		this.buffer.append(node.getTagName());
		this.buffer.append(' ');
//		previousRequiresWhiteSpace = true;
	}
//	boolean previousRequiresNewLine = false;
//	for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
//		ASTNode e = (ASTNode) it.next();
//		// Name, MemberRef, MethodRef, and nested TagElement do not include white space.
//		// TextElements don't always include whitespace, see <https://bugs.eclipse.org/206518>.
//		boolean currentIncludesWhiteSpace = false;
//		if (e instanceof TextElement) {
//			String text = ((TextElement) e).getText();
//			if (text.length() > 0 && Character.isWhitespace(text.charAt(0))) {
//				currentIncludesWhiteSpace = true; // workaround for https://bugs.eclipse.org/403735
//			}
//		}
//		if (previousRequiresNewLine && currentIncludesWhiteSpace) {
//			this.buffer.append("\n * ");//$NON-NLS-1$
//		}
//		previousRequiresNewLine = currentIncludesWhiteSpace;
//		// add space if required to separate
//		if (previousRequiresWhiteSpace && !currentIncludesWhiteSpace) {
//			this.buffer.append(" "); //$NON-NLS-1$
//		}
//		e.accept(this);
//		previousRequiresWhiteSpace = !currentIncludesWhiteSpace && !(e instanceof TagElement);
//	}
//	if (node.isNested()) {
//		this.buffer.append("}");//$NON-NLS-1$
//	}
	return super.visit(node);
}

/**
 * {@inheritDoc}
 * <p>
 * Start the visit of a text element.
 * </p><p>
 * In such case it first needs to jump to next line if previous element
 * already is a text element (indicating that there was a line break
 * in the comment text).
 * </p><p>
 * Then, it add the text content to the comment text and stop
 * to visit the AST hierarchy as such node is not supposed to have
 * any child.
 * </p>
 */
@Override
public boolean visit(final TextElement node) {
	if (this.previousElements[this.level] instanceof TextElement) {
		// two consecutive text element needs to be separated by a new line
		this.buffer.append("\n * ");//$NON-NLS-1$
	}
	this.buffer.append(node.getText());
	return false;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MemberRef)
 */
@Override
public boolean visit(final MemberRef node) {
	this.buffer.append("#");//$NON-NLS-1$
	if (node.getQualifier() != null) {
		node.getQualifier().accept(this);
	}
	node.getName().accept(this);
	return false;
}

/**
 * {@inheritDoc}
 * <p>
 * Start the visit of a method reference.
 * </p><p>
 * In such case it needs to start to print the method reference as meaningful
 * java syntax inside the comment text until opening parenthesis.
 * </p><p>
 * Then, it continues to visit the AST hierarchy in order to add
 * parameters if any.
 * </p>
 */
@SuppressWarnings("rawtypes")
@Override
public boolean visit(final MethodRef node) {
	if (node.getQualifier() != null) {
		node.getQualifier().accept(this);
	}
	this.buffer.append("#");//$NON-NLS-1$
	node.getName().accept(this);
	this.buffer.append("(");//$NON-NLS-1$
	for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
		MethodRefParameter e = (MethodRefParameter) it.next();
		e.accept(this);
		if (it.hasNext()) {
			this.buffer.append(",");//$NON-NLS-1$
		}
	}
	this.buffer.append(")");//$NON-NLS-1$
	return false;
}

@Override
public boolean visit(final QualifiedName node) {
	node.getQualifier().accept(this);
	this.buffer.append(".");//$NON-NLS-1$
	node.getName().accept(this);
	return false;
}

@Override
public boolean visit(final SimpleName node) {
	this.buffer.append(node.getIdentifier());
	return false;
}


/**
 * {@inheritDoc}
 * <p>
 * Start the visit of a method reference parameter element.
 * </p><p>
 * In such case it first needs to add a comma separator in case this
 * is not the first parameter printed for that method reference.
 * </p><p>
 * Then, it add the parameter type to the comment text and stops
 * to visit the AST hierarchy as this node is not supposed to have children.
 * </p>
 */
//@SuppressWarnings("deprecation")
@Override
public boolean visit(final MethodRefParameter node) {
//	if (this.previousElements[this.level] != null) {
//		this.buffer.append(",");//$NON-NLS-1$
//	}
	node.getType().accept(this);
	if (node.getAST().apiLevel() >= AST.JLS3) {
		if (node.isVarargs()) {
			this.buffer.append("...");//$NON-NLS-1$
		}
	}
	return false;
}

@Override
public boolean visit(final PrimitiveType node) {
	this.buffer.append(node.getPrimitiveTypeCode().toString());
	return false;
}

@Override
public boolean visit(final ArrayType node) {
	node.getElementType().accept(this);
	this.buffer.append("[]");//$NON-NLS-1$
	return false;
}

@Override
public boolean visit(final SimpleType node) {
	node.getName().accept(this);
	return false;
}

}
