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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.bear.qa.spot.wb.utils.Utils;
import com.ibm.bear.qa.spot.wb.utils.actions.AbstractAction;

/**
 * Abstract class for SPOT Utils actions.
 * <p>
 * Following common functionalities are available in this class:
 * <ul>
 * <li>{@link #createVisitor(CompilationUnit,ICompilationUnit)}: Create the visitor which will be
 * used to rewrite the given unit using the given AST root.</li>
 * <li>{@link #displayFinalMessage(long)}: Display final message.</li>
 * <li>{@link #finalizeRewrite(SpotAbstractVisitor)}: Method to finalize rewrite operation.</li>
 * <li>{@link #format(ICompilationUnit)}: Format the given compilation unit after having being
 * rewritten.</li>
 * <li>{@link #getFinalMessage()}: Return the message to display at the end of the action.</b>
 * <li>{@link #resetCounters()}: Reset counters after having run the action once.</li>
 * <li>{@link #run(IAction)}: Execute the action on the selected objects.</li>
 * <li>{@link #updateCounters(SpotAbstractVisitor)}: Update action counters after the visitor has
 * been parsed and rewrite operation done.</li>
 * <li>{@link #valid(AbstractTypeDeclaration)}: Tells whether it's valid to rewrite the given
 * compilation type.</li>
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #run(IAction)}: Execute the action on the selected objects.</li>
 * </ul>
 * </p><p>
 * It also defines or overrides following methods:
 * <ul>
 * <li>{@link #createVisitor(CompilationUnit,ICompilationUnit)}: Create the visitor which will be used to rewrite the given unit using the given AST root.</li>
 * <li>{@link #displayFinalMessage(long)}: Display final message.</li>
 * <li>{@link #finalizeRewrite(SpotAbstractVisitor)}: Method to finalize rewrite operation.</li>
 * <li>{@link #format(ICompilationUnit)}: Format the given compilation unit after having being rewritten.</li>
 * <li>{@link #getActionMessage()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getFinalMessage()}: Return the message to display at the end of the action.</li>
 * <li>{@link #getModifiedUnits()}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #getTaskMessage(IJavaElement)}: TODO Add a javadoc with a meaningful summary to this method !</li>
 * <li>{@link #resetCounters()}: Reset counters after having run the action once.</li>
 * <li>{@link #updateCounters(SpotAbstractVisitor)}: Update action counters after the visitor has been parsed and rewrite operation done.</li>
 * <li>{@link #valid(AbstractTypeDeclaration)}: Tells whether it's valid to rewrite the given compilation type.</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("restriction")
public abstract class RewriteAction<CV extends SpotAbstractVisitor> extends AbstractAction implements IEditorActionDelegate {

	// Global counters
	protected int unitsCount = 0;
	protected List<ICompilationUnit> modifiedUnits = new ArrayList<>();

	// Current editor
	final List<ITextEditor> editorsToBeReverted = new ArrayList<ITextEditor>();

	// Edited unit
	private ICompilationUnit editedUnit;

	// Skipped units
	private final List<IJavaElement> skipped = new ArrayList<>();

/**
 * Begin the task for the current action.
 *
 * @param monitor The monitor to use for the task
 * @param size The number of steps in the tasks
 */
//abstract protected void beginActionTask(final IProgressMonitor monitor, int size);

/**
 * Create the visitor which will be used to rewrite the given unit using the given AST root.
 * <p>
 * Note that creating the visitor does not mean it is parsed. That will be done in
 * the {@link #rewrite(ICompilationUnit)} method.
 * </p><p>
 * This method is a hook to allow subclass to create their own visitor.
 * </p>
 * @param astRoot The AST root to use for the parse. Usually the result of the convertion
 * of the compilation unit to AST node (see {@link #createCompilationUnitAST(ICompilationUnit)}).
 * @param unit The compilation unit to rewrite.
 * @return The created visitor.
 */
abstract protected CV createVisitor(final CompilationUnit astRoot, final ICompilationUnit unit);

/**
 * Display final message.
 * <p>
 * By default it displays the message got by using {@link #getFinalMessage()} method
 * in a specific instance of the console view.
 * </p><p>
 * Subclass might want to override this method to replace by a specific behavior.
 * </p>
 * @param startTime The time at which the action was launched. Allow to display
 * the time it took to execute the corresponding action.
 */
protected void displayFinalMessage(final long startTime) {

	// Display message
	StringBuffer messageBuffer = getFinalMessage();
	if (messageBuffer != null && messageBuffer.length() > 0) {
		try(MessageConsoleStream out  = getConsole().newMessageStream()) {
			String message = messageBuffer.toString();
			out.println();
			out.println("--------------------------------------------");
			out.println(message);
			out.println("Time: "+Utils.timeString(System.currentTimeMillis()-startTime));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/**
 * Method to finalize rewrite operation.
 * <p>
 * By default do nothing, all the rewrite operation is done using
 * {@link #rewrite(ICompilationUnit)} method.
 * </p><p>
 * However, subclasses might want to perform additional rewriting on other
 * compilation unit(s)...
 * </p>
 * @param visitor The visitor on which initial parsin has been done
 * @throws Exception If any error occurs during the rewriting operation
 */
protected void finalizeRewrite(final CV visitor) throws Exception {
	// Do nothing
}

/**
 * Format the given compilation unit after having being rewritten.
 * <p>
 * By default, no formatting is done.
 * </p>
 * @param unit The unit to apply the format operation on
 * @return <code>null</code> by default or the formatted code if any
 * @throws Exception
 */
protected String format(final ICompilationUnit unit) throws Exception {
	return null;
}

abstract protected String getActionMessage();

/**
 * Return the message to display at the end of the action.
 *
 * @return The message string buffer or <code>null</code>
 * if no message needs to be displayed.
 */
protected StringBuffer getFinalMessage() {
	if (this.unitsCount == 0) return null;
	StringBuffer messageBuilder = new StringBuffer();
	if (getModifiedUnits() == 0) {
		messageBuilder.append("No java file has been modified.");
	} else {
		if (this.unitsCount == 1) {
			messageBuilder.append("The class "+this.modifiedUnits.get(0).getElementName()+" has been updated.");
		} else {
			int size = this.modifiedUnits.size();
			messageBuilder.append(size).append(" java file");
			if (size == 1) {
				messageBuilder.append(" has");
			} else {
				messageBuilder.append("s have");
			}
			messageBuilder.append(" been updated:\n");
			for (ICompilationUnit unit: this.modifiedUnits) {
				messageBuilder.append(" - ").append(unit.getElementName());
				toStringAncestors(unit, messageBuilder);
				messageBuilder.append("]\n");
			}
		}
	}
	return messageBuilder;
}

protected int getModifiedUnits() {
	return this.modifiedUnits.size();
}

@Override
protected IJavaElement[] getSelectedJavaElements() throws JavaModelException {
	if (this.editedUnit != null) {
		if (skipElement(this.editedUnit)) {
			this.skipped.add(this.editedUnit);
			return new IJavaElement[0];
		}
		return new IJavaElement[] { this.editedUnit };
	}
	return getSelectedJavaElements(getSelectedObjects());
}

@SuppressWarnings({ "rawtypes" })
@Override
protected List getSelectedObjects() {
	List<IJavaElement> selectedElements = new ArrayList<IJavaElement>();
	for (Object obj: super.getSelectedObjects()) {
		IJavaElement javaElement = (IJavaElement) obj;
		switch (javaElement.getElementType()) {
			case IJavaElement.TYPE:
			case IJavaElement.COMPILATION_UNIT:
			case IJavaElement.PACKAGE_FRAGMENT:
			case IJavaElement.JAVA_PROJECT:
				if (skipElement(javaElement)) {
					this.skipped.add(javaElement);
				} else {
					selectedElements.add(javaElement);
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				if (skipElement(javaElement)) {
					this.skipped.add(javaElement);
				} else {
					IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) javaElement;
					if (!fragmentRoot.isArchive() && !fragmentRoot.isExternal()) {
						selectedElements.add(javaElement);
						break;
					}
				}
				break;
			default:
				break;
		}
	}
	return selectedElements;
}

protected String getSkippedElementsMessage() {
	return "Following selected elements have not been processed:";
}

protected String getTaskMessage(final IJavaElement javaElement) {
	StringBuffer message = new StringBuffer(getActionMessage());
	if (javaElement != null) {
		message.append(" for ").append(javaElement.getElementName()).append(" ").append(getJavaType(javaElement));
	}
	message.append("...");
	return message.toString();
}

/**
 * Reset counters after having run the action once.
 */
protected void resetCounters() {
	this.modifiedUnits = new ArrayList<>();
	this.unitsCount = 0;
}

void rewrite(final ICompilationUnit unit) throws Exception, JavaModelException {

	// Increment processed unit counter
	this.unitsCount++;

	// Get editor opened on compilation unit
	ITextEditor currentEditor = null;
	boolean editorWasDirty = false;
	IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
	for (IWorkbenchWindow ww: workbenchWindows) {
		for (IWorkbenchPage page: ww.getPages()) {
			IEditorPart ePart = page.getActiveEditor();
			if (ePart != null) {
				String title = ePart.getTitle();
				if (unit.getElementName().startsWith(title)) {
					currentEditor = (ITextEditor) ePart;
					editorWasDirty = currentEditor.isDirty();
				}
			}
		}
	}

	// Rewrite only if it's a subclass of BvtTestCase
	CompilationUnit astRoot = createCompilationUnitAST(unit);
	if (valid((AbstractTypeDeclaration)astRoot.types().get(0))) {

		// Rewrite code if necessary
		CV visitor = createVisitor(astRoot, unit);
		visitor.parse();
		String contents = visitor.evaluateRewrite();
		if (contents != null && contents.length() > 0) {
			unit.getBuffer().setContents(contents);
		}

		// Hook for rewriting finalization
		finalizeRewrite(visitor);

		// Hook for formatting
		String formattedContents = format(unit);
		if (formattedContents != null && formattedContents.length() > 0) {
			unit.getBuffer().setContents(formattedContents);
		}

		// Save changes only if necessary
		if (visitor.source.equals(formattedContents)) {
			// If unit was edited and the editor was clean then we need to revert the unnecessary changes
			if (currentEditor != null && !editorWasDirty) {
				this.editorsToBeReverted.add(currentEditor);
			}
		} else {
			if (unit.isWorkingCopy()) {
				unit.commitWorkingCopy(false, null);
			} else {
				unit.save(null, true);
			}

			// Update counters
			updateCounters(visitor);
		}
	}
}

/**
 * Execute the action on the selected objects.
 *
 * @param action The action to execute
 */
@Override
public void run(final IAction action) {

	// Get all units
	final Map<IJavaElement, List<ICompilationUnit>> selectedUnits = getAllUnitsInSelection();

	// Create runnable
	IRunnableWithProgress runnable = new IRunnableWithProgress() {
		@Override
		public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {

				// Init task title and size
				int size = 0;
				for (IJavaElement javaElement: selectedUnits.keySet()) {
					size += selectedUnits.get(javaElement).size();
				}
				monitor.beginTask(getTaskMessage(null), size);

				// Perform rewrite action for each select java element
				for (IJavaElement javaElement: selectedUnits.keySet()) {
					monitor.setTaskName(getTaskMessage(javaElement));
					List<ICompilationUnit> units = selectedUnits.get(javaElement);
					int unitsSize = units.size();
					int count = 1;
					for (ICompilationUnit unit: units) {
						String subtaskTitle = "Processing unit "+(size==1 ? "" : count+"/"+unitsSize)+": "+unit.getElementName()+"...";
						monitor.subTask(subtaskTitle);
						rewrite(unit);
						if (monitor.isCanceled()) {
							return;
						}
						monitor.worked(1);
						count++;
					}
				}

				// We're done now
				monitor.done();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	};

	// Execute the action
	long startTime = System.currentTimeMillis();
	ProgressMonitorDialog progress = new ProgressMonitorDialog(this.shell);
	try {
		progress.run(true, true, runnable);
	} catch (@SuppressWarnings("unused") Exception e) {
		// skip
	}

	// Revert editor changes if necessary
	Display.getDefault().asyncExec(new Runnable() {
	    @Override
		public void run() {
			for (ITextEditor editor: RewriteAction.this.editorsToBeReverted) {
				editor.doRevertToSaved();
			}
	    }
	});

	// Final message
	displayFinalMessage(startTime);

	// If elements have been skipped, open warning dialog
	if (this.skipped.size() > 0) {
		StringBuilder builder = new StringBuilder(getSkippedElementsMessage()).append(LINE_SEPARATOR);
		for (IJavaElement javaElement: this.skipped) {
			builder.append(" - ").append(javaElement.getElementName()).append(LINE_SEPARATOR);
		}
		MessageDialog.openWarning(getShell(), "SPOT Utils", builder.toString());
		this.skipped.clear();
	}


    // Reset counters
    resetCounters();
}

@Override
public void setActiveEditor(final IAction arg0, final IEditorPart editorPart) {
	if (editorPart != null) {
		this.part = editorPart;
		IViewPartInputProvider inputProvider = (IViewPartInputProvider) editorPart;
		this.editedUnit= (ICompilationUnit) inputProvider.getViewPartInput();
	} else {
		this.part = null;
		this.editedUnit = null;
	}
}

@SuppressWarnings("unused")
protected boolean skipElement(final IJavaElement javaElement) {
	return false;
}

/**
 * Update action counters after the visitor has been parsed and rewrite operation done.
 *
 * @param visitor The visitor used for the parse which typically stored the number
 * of changes done.
 */
protected void updateCounters(final CV visitor) {
	this.modifiedUnits.add((ICompilationUnit)visitor.unit);
}

/**
 * Tells whether it's valid to rewrite the given compilation type.
 * <p>
 * Default is to assume that it's valid to rewrite the type.
 * </p>
 * @param typeDeclaration The type to rewrite
 * @return <code>true</code> if the type can be rewritten, <code>false</code>
 * otherwise.
 */
protected boolean valid(final AbstractTypeDeclaration typeDeclaration) {
	return true;
}

}