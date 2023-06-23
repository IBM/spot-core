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

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH;

import java.util.*;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.*;

import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;

/**
 * AST Visitor to extract method references to public and protected method
 * declarations and put them in specific paragraphs of the javadoc class.
 *
 * TODO Replace existing method reference instead of adding them blindly
 */
@SuppressWarnings("restriction")
public class SpotClassDocFormatVisitor extends SpotAbstractVisitor {

	final MultiTextEdit rootEdit;
	List<MethodDeclaration> missingJavadocMethods = new ArrayList<>();
	List<MethodDeclaration> publicMethods = new ArrayList<>();
	List<MethodDeclaration> protectedMethods = new ArrayList<>();

protected SpotClassDocFormatVisitor(final ITypeRoot typeRoot) {
	super(typeRoot);
	this.rootEdit = new MultiTextEdit(0, this.source.length());
}

/**
 * Clean formatted javadoc text;
 * <p>
 * Cleaning means to put method reference on a single line after
 * having formatted the javadoc comment. That makes the reading
 * easier when having a quick look at type declaration javadoc.
 * </p><p>
 * It also concatenes closing paragraph line followed by opening one.
 * </p>
 * @param formattedJavadoc The formatted javadoc
 * @return The cleaned formatted javadoc
 */
private String cleanFormattedJavadoc(final String formattedJavadoc) {
	StringBuffer buffer = new StringBuffer();
	int idxLi = formattedJavadoc.indexOf("<li>", formattedJavadoc.indexOf("This class defines following internal API methods:")+50);
	int start = 0, end = 0;
	while (idxLi > 0) {
		buffer.append(formattedJavadoc, start, end=idxLi+4);
		int endLi = formattedJavadoc.indexOf("</li>", end);
		if (endLi < 0) break;
		buffer.append(formattedJavadoc.substring(end, endLi+5).replaceAll(LINE_SEPARATOR+" \\* ", " "));
		start = endLi+5;
		idxLi = formattedJavadoc.indexOf("<li>", start);
	}
	buffer.append(formattedJavadoc, start, formattedJavadoc.length());
	return buffer.toString()
			.replace(LINE_SEPARATOR+" * </p>"+LINE_SEPARATOR+" * <p>"+LINE_SEPARATOR,	LINE_SEPARATOR+" * </p><p>"+LINE_SEPARATOR)
			.replace("} : ", "}: ");
}

/**

/**
 * Finalize visit of type declaration.
 * <p>
 * When finishing to visit type declaration, it's time to:
 * <ol>
 * <li>check for methods with no javadoc</li>
 * <li>update the type declaration javadoc comment with information
 * got during the AST parsing</li>
 * </ol>
 */
@Override
public void endVisit(final TypeDeclaration node) {
	if (node.getJavadoc() != null) {
		udpateTypeDeclarationJavadoc(node);
	}
	super.endVisit(node);
}

@Override
public String evaluateRewrite() throws Exception {
	if (this.ast.modificationCount() > 0) {
		Document document= new Document(this.source);
		this.rootEdit.apply(document);
		return document.get();
	}
	return null;
}

/**
 * Update the type declaration javadoc comment with method references.
 *
 * @param node The type declaration node
 */
protected void udpateTypeDeclarationJavadoc(final TypeDeclaration node) {

	// Flatten the javadoc keeping the existing broken lines
	Javadoc javadoc = node.getJavadoc();
	String javadocText = javadoc.toString().trim();

	// Format the javadoc text
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter();
	Map<String, String> options = JavaCore.getOptions();
	options.put(FORMATTER_COMMENT_LINE_LENGTH, "100");
	codeFormatter.setOptions(options);
	TextEdit javadocFormatEdit = codeFormatter.format(CodeFormatter.K_JAVA_DOC, javadocText, 0, javadocText.length()-1, 0, LINE_SEPARATOR);
	Document doc = new Document(javadocText);
	try {
		javadocFormatEdit.apply(doc);
	} catch (BadLocationException e) {
		Assert.isTrue(false, "Javadoc formatting created edits with wrong positions: " + e.getMessage()); //$NON-NLS-1$
	}
	String cleanedFormattedjavadoc = cleanFormattedJavadoc(doc.get());

	// Modify the root edit by modifying existing one
	ReplaceEdit textEdit = new ReplaceEdit(javadoc.getStartPosition(), javadoc.getLength(), cleanedFormattedjavadoc);
	this.rootEdit.addChild(textEdit);
}
}
