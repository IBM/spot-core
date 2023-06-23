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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.*;

import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;

public class FormatVisitor extends SpotAbstractVisitor {

MultiTextEdit rootEdit;
List<Comment> comments;

@SuppressWarnings("unchecked")
FormatVisitor(final ICompilationUnit unit) {
	super(unit);
	this.rootEdit = new MultiTextEdit(0, this.source.length());
	this.comments = this.astRoot.getCommentList();
}

@Override
public String evaluateRewrite() throws Exception {
	Document document= new Document(this.source);
	try {
		this.rootEdit.apply(document, TextEdit.NONE);
	} catch (BadLocationException e) {
		Assert.isTrue(false, "Formatter created edits with wrong positions: " + e.getMessage()); //$NON-NLS-1$
	}
	return document.get();
}

@Override
public boolean visit(final Block block) {

	// Reduce empty lines
	boolean firstNode = true;
	for (Object node: block.statements()) {
		// Add new line if statement has some comments before
		Statement statement = (Statement) node;
		int currentLine = this.astRoot.getLineNumber(this.astRoot.getExtendedStartPosition(statement));
		int lineStartPosition = this.astRoot.getPosition(currentLine, 0);
		// Remove empty lines if any
		int previousLine = currentLine - 1;
		int previousLineStartPosition = this.astRoot.getPosition(previousLine, 0);
		String spaces = this.source.substring(previousLineStartPosition, lineStartPosition);
		int deleteStartPosition = -1;
		while (spaces.trim().length() == 0) {
			deleteStartPosition = previousLineStartPosition;
			previousLineStartPosition = this.astRoot.getPosition(--previousLine, 0);
			spaces = this.source.substring(previousLineStartPosition, lineStartPosition);
		}
		// If there are some comments before the statement, insert a new line
		final int commentIndex = this.astRoot.firstLeadingCommentIndex(statement);
		Comment comment = commentIndex < 0 ? null : this.comments.get(commentIndex);
		if (comment != null && comment.isLineComment()) {
			if (this.astRoot.getColumnNumber(comment.getStartPosition()) == 0) {
				// Ignore line comment starting at column 0
				comment = null;
			}
		}
		if (comment != null) {
			if (deleteStartPosition > 0) {
				int delta = lineStartPosition-deleteStartPosition;
				if (delta > 2) {
					this.rootEdit.addChild(new ReplaceEdit(deleteStartPosition, delta-2, ""));
				}
			} else if (!firstNode) {
				this.rootEdit.addChild(new InsertEdit(lineStartPosition, LINE_SEPARATOR));
			}
		}
		else if (deleteStartPosition > 0) {
			this.rootEdit.addChild(new ReplaceEdit(deleteStartPosition, lineStartPosition-deleteStartPosition, ""));
		}
		firstNode = false;
	}
	return true;
}

}
