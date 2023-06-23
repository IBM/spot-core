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
package com.ibm.bear.qa.spot.wb.utils.rewrite;

import java.util.Map;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

/**
 * Abstract class for <b>SPOT Utils</b> AST visitor.
 * <p>
 * This class defines following common methods for all SPOT visitors:
 * <ul>
 * <li>{@link #convert(ITypeRoot)}: Convert the given type root into a AST compilation unit node.</p>
 * <li>{@link #evaluateRewrite()}: Write all changes currently stored in the rewriter and return the modified unti source.</p>
 * <li>{@link #parse()}: Parse the entire unit.</p>
 * <li>
 * </ul>
 */
public abstract class SpotAbstractVisitor extends ASTVisitor {

	/* Constants */
	protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

/**
 * Convert the given type root into a AST compilation unit node.
 *
 * @param typeRoot The type root to convert
 * @return The AST node as a {@link CompilationUnit}.
 */
@SuppressWarnings("deprecation")
public static CompilationUnit convert(final ITypeRoot typeRoot) {
	ASTParser parser = ASTParser.newParser(AST.JLS8);
	parser.setSource(typeRoot);
	parser.setResolveBindings(true);
	return (CompilationUnit) parser.createAST(null);
}

	/* Fields */
	public AST ast;
	public CompilationUnit astRoot;
	public ASTRewrite rewriter;
	public ITypeRoot unit;
	public String source;
	public int changes = 0;

protected SpotAbstractVisitor(final CompilationUnit root, final ITypeRoot typeRoot) {
	this.ast = root.getAST();
	this.astRoot = root;
	this.rewriter = ASTRewrite.create(this.ast);
	this.unit = typeRoot;
	try {
	    this.source = typeRoot.getSource();
    } catch (JavaModelException e) {
	    e.printStackTrace();
    }
}

protected SpotAbstractVisitor(final ITypeRoot typeRoot) {
	this(convert(typeRoot), typeRoot);
}

protected String getJavadocSummary(final Javadoc javadoc) {
	if (javadoc != null) {
		TagElement firstTag = (TagElement) javadoc.tags().get(0);
		if (firstTag.getTagName() == null) {
			ASTNode firstFragment = (ASTNode) firstTag.fragments().get(0);
			if (firstFragment instanceof TextElement) {
				return ((TextElement) firstFragment).getText();
			}
		}
	}
	return null;
}

/**
 * Write all changes currently stored in the rewriter and return the modified unti source.
 *
 * @return The new unit source contents after having applied all the changes.
 * @throws Exception If anything wrong occurs while rewriting the source
 */
@SuppressWarnings({ "rawtypes" })
public String evaluateRewrite() throws Exception {
	if (this.ast.modificationCount() > 0) {
		Document document= new Document(this.source);
		final Map options = JavaCore.getOptions();
		TextEdit res= this.rewriter.rewriteAST(document, options);
		res.apply(document);
		return document.get();
	}
	return null;
}

/**
 * Parse the entire unit.
 */
public void parse() {
	this.astRoot.accept(this);
}
}
