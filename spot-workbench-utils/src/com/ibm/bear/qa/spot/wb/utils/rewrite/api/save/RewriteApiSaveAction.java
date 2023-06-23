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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.ibm.bear.qa.spot.wb.utils.rewrite.RewriteAction;

/**
 * Action used to rewrite all method invocations to ClmScenarioStep.save(JazzWebPage) method.
 */
public class RewriteApiSaveAction extends RewriteAction<SaveApiVisitor> {

	// Specific counter
	int addedInterfaces = 0;

@Override
protected String getActionMessage() {
	return "Replacing save() API method calls";
}

@Override
protected SaveApiVisitor createVisitor(final CompilationUnit astRoot, final ICompilationUnit unit) {
	return new SaveApiVisitor(astRoot, unit);
}

@Override
protected int getModifiedUnits() {
	return super.getModifiedUnits() + this.addedInterfaces;
}

@Override
protected StringBuffer getFinalMessage() {
	StringBuffer messageBuilder = super.getFinalMessage();
	if (getModifiedUnits() > 0) {
		if (this.modifiedUnits.size() == 0) {
			throw new RuntimeException("Unexpected use case!");
		}
		messageBuilder.append(LINE_SEPARATOR);
		switch (this.addedInterfaces) {
			case 0:
				messageBuilder.append("No java file was");
				break;
			case 1:
				messageBuilder.append("to add 'JazzSaveablePage' interface.");
				break;
			default:
				messageBuilder.append(this.addedInterfaces).append(" java files have been");
				break;
		}
		messageBuilder.append(" modified to add 'JazzSaveablePage' interface.");
	}
	return messageBuilder;
}

/**
 * Parse all units where the interface needs to be implemented.
 */
@Override
protected void finalizeRewrite(final SaveApiVisitor visitor) throws Exception {
	for (ICompilationUnit unitToImplementInterface: visitor.unitsToImplementInterface) {
		SaveablePageVisitor unitVisitor = new SaveablePageVisitor(unitToImplementInterface);
		unitVisitor.parse();
		String contents = unitVisitor.evaluateRewrite();
		if (contents != null && contents.length() > 0) {
			unitToImplementInterface.getBuffer().setContents(contents);
			if (unitToImplementInterface.hasUnsavedChanges()) {
				unitToImplementInterface.save(null, true);
			}
		}
		if (unitVisitor.needsInterfaceImport) {
			this.addedInterfaces++;
		}
	}
}

@Override
protected void resetCounters() {
	// Reset default visitor counters
	super.resetCounters();

	// Reset specific counters
	this.addedInterfaces = 0;
}
}