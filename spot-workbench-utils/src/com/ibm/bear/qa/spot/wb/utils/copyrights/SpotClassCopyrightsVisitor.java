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
package com.ibm.bear.qa.spot.wb.utils.copyrights;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.text.edits.MultiTextEdit;

import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;

/**
 * AST Visitor to extract method references to public and protected method
 * declarations and put them in specific paragraphs of the javadoc class.
 *
 * TODO Replace existing method reference instead of adding them blindly
 */
public class SpotClassCopyrightsVisitor extends SpotAbstractVisitor {

	Comment copyrightComment;
	final MultiTextEdit rootEdit;

protected SpotClassCopyrightsVisitor(final CompilationUnit root, final ICompilationUnit cu) {
	super(root, cu);
	this.rootEdit = new MultiTextEdit(0, this.source.length());
}

@SuppressWarnings("unchecked")
@Override
public boolean visit(final CompilationUnit node) {
	List<Comment> comments = node.getCommentList();
	if (comments.size() > 0) {
		Comment firstComment = comments.get(0);
		if (firstComment.getStartPosition() == 0) {
			String commentText = this.source.substring(firstComment.getStartPosition(), firstComment.getLength());
			if (commentText.contains("Copyright")) {
				this.copyrightComment = firstComment;
			}
		}
	}

	// No need to traverse the compilation unit
	return false;
}

}
