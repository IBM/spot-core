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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import com.ibm.bear.qa.spot.wb.utils.rewrite.RewriteAction;

/**
 * Class implementing an action which writes the public and internal API methods
 * in the type declaration javadoc comment of the classes of the selected java element.
 * <p>
 * Basically the action is traversing the class AST to detect public and protected
 * method declarations and add a reference to each method in a specific paragraph
 * of the class javadoc comment.
 * </p><p>
 * Already existing method references are removed and replaced by current
 * accurate ones. Note that the entire content of the 2 paragraphs including these method
 * references are removed, all other paragraphs are kept. The added or updated method
 * references are moved at the end of the javadoc comment.
 * </p><p>
 * The selected element can be any {@link IJavaElement Java element} in the
 * package explorer. Its content is explored to find all Java classes and execute
 * the corresponding action on each of them.
 * </p>
 */
public class UpdateCopyrightsAction extends RewriteAction<SpotClassCopyrightsVisitor> {

	private static final String CURRENT_YEAR_STRING;
	static {
		Date currentDate = new Date(System.currentTimeMillis());
		SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy");
		CURRENT_YEAR_STRING = dateYearFormat.format(currentDate);
	}
	private final static String COPYRIGHTS_COMMENT = "/*********************************************************************" + LINE_SEPARATOR +
			"* Copyright (c) 2012, "+CURRENT_YEAR_STRING+" IBM Corporation and others." + LINE_SEPARATOR +
			"*" + LINE_SEPARATOR +
			"* This program and the accompanying materials are made" + LINE_SEPARATOR +
			"* available under the terms of the Eclipse Public License 2.0" + LINE_SEPARATOR +
			"* which is available at https://www.eclipse.org/legal/epl-2.0/" + LINE_SEPARATOR +
			"*" + LINE_SEPARATOR +
			"* SPDX-License-Identifier: EPL-2.0" + LINE_SEPARATOR +
			"*" + LINE_SEPARATOR +
			"* Contributors:" + LINE_SEPARATOR +
			"*     IBM Corporation - initial API and implementation" + LINE_SEPARATOR +
			"**********************************************************************/";

@Override
protected SpotClassCopyrightsVisitor createVisitor(final CompilationUnit astRoot, final ICompilationUnit cu) {
	return new SpotClassCopyrightsVisitor(astRoot, cu);
}

@Override
protected void finalizeRewrite(final SpotClassCopyrightsVisitor visitor) throws Exception {
	String unitContents = visitor.unit.getBuffer().getContents();
	if (visitor.copyrightComment == null) {
		unitContents = COPYRIGHTS_COMMENT + unitContents;
		visitor.unit.getBuffer().setContents(unitContents);
	} else {
		int commentLength = visitor.copyrightComment.getLength();
		visitor.unit.getBuffer().replace(0, commentLength, COPYRIGHTS_COMMENT);
	}
}

@Override
protected String getActionMessage() {
	return "Check and fix classes Copyrights comment";
}

@Override
protected String getSkippedElementsMessage() {
	return "Following elements were not in a spot-core repository project, hence they have not been processed:";
}

@Override
protected boolean skipElement(final IJavaElement javaElement) {
	String projectName = javaElement.getJavaProject().getElementName();
	switch (projectName) {
		case "spot-core":
		case "spot-samples-pages":
		case "spot-samples-scenarios":
			return false;
	}
	return !MessageDialog.open(MessageDialog.CONFIRM, getShell(), "SPOT Utils", "Are you sure you want to update Copyrights with EPL-2.0 license for "+javaElement.getElementName(), SWT.NONE);
}

}
