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
package com.ibm.bear.qa.spot.wb.utils.actions;

import java.io.File;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.console.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract class for all <b>SPOT Utils</b> actions.
 */
@SuppressWarnings({
        "rawtypes",
        "restriction" })
public abstract class AbstractAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {

	/* Constants */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/* Fields */
	protected IStructuredSelection selection;
	protected IWorkbenchPart part;
	protected IWorkbenchWindow window;
	protected Shell shell;
	protected boolean resolveBindings = false;
	MessageConsole console = null;

protected void copyToClipboard(final String clipboardText) {
	Clipboard clipboard = new Clipboard(getShell().getDisplay());
	TextTransfer textTransfer = TextTransfer.getInstance();
	clipboard.setContents(new Object[] { clipboardText }, new Transfer[] { textTransfer } );
	clipboard.dispose();
}

@SuppressWarnings("deprecation")
protected CompilationUnit createCompilationUnitAST(final ICompilationUnit unit) {
	ASTParser parser = ASTParser.newParser(AST.JLS8);
	parser.setSource(unit);
	parser.setResolveBindings(this.resolveBindings);
	return (CompilationUnit) parser.createAST(null);
}

@SuppressWarnings("deprecation")
protected CompilationUnit createCompilationUnitAST(final String content) {
	ASTParser parser = ASTParser.newParser(AST.JLS8);
	parser.setSource(content.toCharArray());
	parser.setResolveBindings(this.resolveBindings);
	return (CompilationUnit) parser.createAST(null);
}

/**
 * Free local handlers.
 *
 * @see IWorkbenchWindowActionDelegate#dispose
 */
@Override
public void dispose() {
	this.window = null;
	this.part = null;
}

/**
 * Search in the workspace for a compilation unit with the given fully qualified name.
 *
 * @param fullyQualifiedName The type qualified name to find
 * @return The found compilation unit or <code>null</code> if none was found
 * @throws JavaModelException If any error occurs while parsing workspace Java projects
 */
protected ICompilationUnit findCompilationUnit(final String fullyQualifiedName) throws JavaModelException {
	IJavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
	for (IJavaProject javaProject: javaModel.getJavaProjects()) {
		IType type = javaProject.findType(fullyQualifiedName);
		if (type != null) {
			return type.getCompilationUnit();
		}
	}
	return null;
}

protected List<ICompilationUnit> getAllScenarios() throws JavaModelException {
	return getAllScenarios(null);
}

protected List<ICompilationUnit> getAllScenarios(final List<IJavaProject> projects) throws JavaModelException {
	IJavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
	IJavaProject[] allJavaProjects = projects == null ? javaModel.getJavaProjects() : projects.toArray(new IJavaProject[0]);
	List<ICompilationUnit> allScenarios = new ArrayList<ICompilationUnit>();
	for (IJavaProject javaProject: allJavaProjects) {
		String projectName = javaProject.getElementName();
		if (projectName.startsWith("spot") && projectName.endsWith("scenarios")) {
			allScenarios.addAll(searchScenarioCompilationUnits(javaProject));
		}
	}
	return allScenarios;
}

protected Map<IJavaElement, List<ICompilationUnit>> getAllUnitsInSelection() {
	try {
		IJavaElement[] elements = getSelectedJavaElements();
		Map<IJavaElement, List<ICompilationUnit>> mapUnits = new HashMap<IJavaElement, List<ICompilationUnit>>();
		for (IJavaElement javaElement: elements) {
			mapUnits.put(javaElement, getUnits(javaElement));
		}
		return mapUnits;
	}
	catch (JavaModelException jme) {
		throw new RuntimeException(jme);
	}
}

/**
 * Return all compilation units available in the given Java project.
 *
 * @param javaProject The project to look units for
 * @param scenario TODO
 * @return The list of found units
 * @throws JavaModelException If any error occurs while parsing the Java project
 */
protected List<ICompilationUnit> getCompilationUnits(final IJavaProject javaProject, final boolean scenario) throws JavaModelException {
	final List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
	for (IPackageFragmentRoot fragmentRoot: javaProject.getPackageFragmentRoots()) {
		if (!fragmentRoot.isArchive()) {
			units.addAll(getCompilationUnits(fragmentRoot, scenario));
		}
	}
	return units;
}

/**
 * Return all compilation units available in the given Java package fragment.
 *
 * @param fragment The package fragment to look units for
 * @param scenario TODO
 * @return The list of found units
 * @throws JavaModelException If any error occurs while parsing the package
 */
protected List<ICompilationUnit> getCompilationUnits(final IPackageFragment fragment, final boolean scenario) throws JavaModelException {
	final List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
	for (ICompilationUnit cu: fragment.getCompilationUnits()) {
		String unitName = cu.getElementName();
		if (!scenario || unitName.endsWith("Scenario.java")) {
			units.add(cu);
		}
	}
	return units;
}

/**
 * Return all compilation units available in the given Java package fragment root.
 *
 * @param fragmentRoot The package fragment root to look units for
 * @param scenario TODO
 * @return The list of found units
 * @throws JavaModelException If any error occurs while parsing the package
 */
protected List<ICompilationUnit> getCompilationUnits(final IPackageFragmentRoot fragmentRoot, final boolean scenario) throws JavaModelException {
	final List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
	for (IJavaElement javaElement: fragmentRoot.getChildren()) {
		if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			units.addAll(getCompilationUnits((IPackageFragment) javaElement, scenario));
		}
	}
	return units;
}

protected MessageConsole getConsole() {
	if (this.console == null) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if ("SPOT Utils".equals(existing[i].getName())) {
				this.console = (MessageConsole) existing[i];
				showConsole(this.console);
				return this.console;
			}
		}
		// no console found, so create a new one
		this.console = new MessageConsole("SPOT Utils", null);
		conMan.addConsoles(new IConsole[] { this.console });
		showConsole(this.console);
	}
	return this.console;
}

/**
 * Return selected Java elements or all Java projects if selection is empty.
 *
 * @return The selected java elements
 * @throws JavaModelException
 */
protected IJavaElement[] getSelectedJavaElements() throws JavaModelException {
	if (getSelectedObject() == null) {
		IJavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
		return javaModel.getJavaProjects();
	}
	return getSelectedJavaElements(this.selection.toList());
}

/**
 * Return selected Java elements.
 *
 * @return The selected java elements
 */
protected IJavaElement[] getSelectedJavaElements(final List list) {
	Object[] objects = list.toArray();
	int length = objects.length;
	IJavaElement[] elements = new IJavaElement[length];
	for (int i = 0; i < length; i++) {
		elements[i] = (IJavaElement) objects[i];
	}
	return elements;
}

/**
 * Return the object of the selection on which the action has to be performed.
 *
 * @return The first element of the selection or null if empty
 */
protected Object getSelectedObject() {
	List list = getSelectedObjects();
	if (list.size() > 0) {
		return list.get(0);
	}
	return null;
}

/**
 * Return the list of selected objects on which the action has to be performed.
 *
 * @return The objects list
 */
protected List getSelectedObjects() {
	if (this.selection == null) {
		System.out.println("Warning: No selection.");
		return Collections.emptyList();
	}
//	if (this.selection.isEmpty()) {
//		System.out.println("Warning: Selection is empty.");
//	}
	return this.selection.toList();
}

protected List<IJavaProject> getSelectedProjects() {
	List selectedObjects = getSelectedObjects();
	List<IJavaProject> projects = new ArrayList<IJavaProject>();
	for (Object selected: selectedObjects) {
		if (selected instanceof IJavaProject) {
			projects.add((IJavaProject)selected);
		}
	}
	if (projects.isEmpty()) {
		return null;
	}
	return projects;
}

//@Override
//public void run(final IAction action) {
//	try {
//		// Try to run action on selected object
//		if (getSelectedObject() != null) {
//			run(getSelectedObject());
//		}
//	}
//	catch (Exception ex) {
//		ex.printStackTrace();
//	}
//}

///**
// * Run the action on selected object.
// *
// * @param obj Object The selected object
// */
//protected void run(final Object obj) throws Exception {
//	// do nothing
//}

protected List<ICompilationUnit> getSelectedUnits() {
	List selectedObjects = getSelectedObjects();
	List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
	for (Object selected: selectedObjects) {
		if (selected instanceof ICompilationUnit) {
			units.add((ICompilationUnit)selected);
		}
	}
	if (units.isEmpty()) {
		return null;
	}
	return units;
}

/**
 * Get stored shell. Useful if some dialogs has to be displayed.
 */
public Shell getShell() {
	return this.shell;
}

/**
 * Return all compilation units which belongs to the given Java element.
 *
 * @param element The java element to look into
 * @return The list of units found
 * @throws JavaModelException If any error occurs while parsing the given java element
 */
protected List<ICompilationUnit> getUnits(final IJavaElement element) throws JavaModelException {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT:
			return getCompilationUnits((IJavaProject)element, false);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
    		return getCompilationUnits((IPackageFragmentRoot)element, false);
		case IJavaElement.PACKAGE_FRAGMENT:
    		return getCompilationUnits((IPackageFragment)element, false);
		case IJavaElement.TYPE:
			List<ICompilationUnit> units = new ArrayList<ICompilationUnit>(1);
			units.add(((IType) element).getCompilationUnit());
			return units;
		case IJavaElement.COMPILATION_UNIT:
			units = new ArrayList<ICompilationUnit>(1);
			units.add((ICompilationUnit) element);
			return units;
		default:
			throw new RuntimeException("Unsupported java type: "+element.getElementType());
	}
}

protected String getJavaType(final IJavaElement javaElement) {
	switch(javaElement.getElementType()) {
		case IJavaElement.JAVA_PROJECT:
			return "project";
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
    		return "fragment root";
		case IJavaElement.PACKAGE_FRAGMENT:
    		return "package";
		case IJavaElement.TYPE:
			return "type";
		case IJavaElement.COMPILATION_UNIT:
			return "compilation unit";
		default:
			throw new RuntimeException("Unsupported java type: "+javaElement.getElementType());
	}
}

protected Map<IJavaProject, List<ICompilationUnit>> getProjectsUnitsMap(final boolean scenario) throws JavaModelException {
    IJavaElement[] elements = getSelectedJavaElements();
	Map<IJavaProject, List<ICompilationUnit>> mapUnits = new HashMap<IJavaProject, List<ICompilationUnit>>();
	for (IJavaElement javaElement: elements) {
		IJavaProject javaProject = javaElement.getJavaProject();
		List<ICompilationUnit> projectUnits = mapUnits.get(javaProject);
		if (projectUnits == null) {
			mapUnits.put(javaProject, projectUnits = new ArrayList<ICompilationUnit>());
		}
		switch (javaElement.getElementType()) {
			case IJavaElement.JAVA_PROJECT:
				String projectName = javaProject.getElementName();
				if (projectName.startsWith("spot")) {
					if (!scenario || projectName.endsWith("scenarios")) {
						projectUnits.addAll(getCompilationUnits(javaProject, scenario));
					}
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				projectUnits.addAll(getCompilationUnits((IPackageFragmentRoot) javaElement, scenario));
				break;
			case IJavaElement.PACKAGE_FRAGMENT:
				projectUnits.addAll(getCompilationUnits((IPackageFragment) javaElement, scenario));
				break;
		}
	}
	return mapUnits;
}

/**
 * Return the workbench window associated with the current action.
 * <p>
 * Note that if it has not been initialized in {@link #setActivePart(IAction, IWorkbenchPart)}
 * method, then it uses the workbench current active window.
 * </p>
 * @return the window
 */
protected IWorkbenchWindow getWindow() {
	if (this.window == null) {
		if (this.part != null) {
			this.window = this.part.getSite().getWorkbenchWindow();
		}
//		if (this.window == null) {
//			for (IWorkbenchWindow w: PlatformUI.getWorkbench().getWorkbenchWindows()) {
//				w.getActivePage().getActiveEditor();
//			}
//		}
	}
	return this.window;
}

/**
 * Store associated window and its shell.
 *
 * @see IWorkbenchWindowActionDelegate#init
 */
@Override
public void init(final IWorkbenchWindow win) {
	this.window = win;
	this.shell = win.getShell();
}

private List<ICompilationUnit> searchScenarioCompilationUnits(final IJavaProject javaProject) throws JavaModelException {
	List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
	for (IPackageFragment packageFragment: javaProject.getPackageFragments()) {
		for (ICompilationUnit unit: packageFragment.getCompilationUnits()) {
			String unitName = unit.getElementName();
			if (unitName.endsWith("Scenario.java") /*&& !unitName.equals("DemoScenario.java")*/) {
				units.add(unit);
			}
		}
	}
	return units;
}

/**
 * Store the selection if it is structured.
 * @see IActionDelegate#selectionChanged
 */
@Override
public void selectionChanged(final IAction action, final ISelection select) {
	try {
		this.selection = (IStructuredSelection) select;
	}
	catch (@SuppressWarnings("unused") ClassCastException e) {
	}
}

/**
 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 */
@Override
public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	this.part = targetPart;
	IWorkbenchPartSite site = targetPart.getSite();
	this.window = site.getWorkbenchWindow();
	this.shell = site.getShell();
}

protected void showConsole(final IConsole cons) {
	IWorkbenchPage page = getWindow().getActivePage();
	String id = IConsoleConstants.ID_CONSOLE_VIEW;
	try {
		IConsoleView view = (IConsoleView) page.showView(id);
		view.display(cons);
	} catch (PartInitException e) {
		e.printStackTrace();
	}
}

protected void toStringAncestors(final IJavaElement element, final StringBuffer buffer) {
	JavaElement parentElement = (JavaElement)element.getParent();
	if (parentElement != null && parentElement.getParent() != null) {
		buffer.append(" [in "); //$NON-NLS-1$
		buffer.append(parentElement.getElementName());
		toStringAncestors(parentElement, buffer);
		buffer.append("]"); //$NON-NLS-1$
	}
}


public String getProjectVersion(final IJavaProject javaProject) {
	URI pomFileUri = javaProject.getProject().getFile("pom.xml").getLocationURI();
	File pomFile = new File(pomFileUri.getPath());
	if (pomFile.exists()) {
		try {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(pomFile);
			doc.getDocumentElement().normalize();
			NodeList projectNodes = (NodeList) doc.getChildNodes().item(0);
			for (int idx=0; idx<projectNodes.getLength(); idx++) {
				Node childNode = projectNodes.item(idx);
				switch(childNode.getNodeName()) {
					case "artifactId":
						if (!childNode.getTextContent().equals(javaProject.getElementName())) {
							System.err.println("Unexpected artifactId "+childNode.getTextContent()+" for Java project "+javaProject.getElementName()+"!");
							return null;
						}
						break;
					case "version":
						return childNode.getTextContent();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	System.err.println("Cannot figure out what was project "+javaProject.getElementName()+" version!");
	return "???";
}
}
