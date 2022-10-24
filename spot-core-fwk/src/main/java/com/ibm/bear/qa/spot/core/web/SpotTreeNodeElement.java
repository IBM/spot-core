/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.web;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.util.*;

import org.openqa.selenium.By;

import com.ibm.bear.qa.spot.core.api.elements.SpotTreeNode;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioImplementationError;

/**
 * Class to handle a node web element in a web page. A node may contain multiple nodes, which may be
 * several levels deep.
 * <p>
 * This class defines following public API methods of {@link SpotTreeNode} interface:
 * <ul>
 * <li>{@link #collapse()}: Collapse the current node.</li>
 * <li>{@link #expand()}: Expand the current node.</li>
 * <li>{@link #getAllLabels()}: Return all nodes labels of the current node.</li>
 * <li>{@link #getChildrenLabels()}: Return the labels list of current node children.</li>
 * <li>{@link #getLabel()}: Return the node label.</li>
 * <li>{@link #getNode(String)}: Get the node for the given path.</li>
 * <li>{@link #getParentNode()}: Returns the parent node of the current node.</li>
 * <li>{@link #getRootNode()}: Returns the tree root node.</li>
 * <li>{@link #getSelectedNode()}: Return the selected node in the tree relatively to the current node.</li>
 * <li>{@link #getVisibleLabels()}: Return the list of visible nodes in the tree from the current node.</li>
 * <li>{@link #getVisibleNodes()}: Return the list of visible nodes in the tree from the current node.</li>
 * <li>{@link #isSelected()}: Returns whethert the current node is selected or not.</li>
 * <li>{@link #searchNode(String)}: Searches the node with the given name in the hierarchy starting from current node.</li>
 * <li>{@link #select()}: Select the current node.</li>
 * <li>{@link #selectNode(String)}: Selects the node in the tree matching the given path relatively to the current node.</li>
 * </ul>
 * </p><p>
 * This class also defines following internal API methods:
 * <ul>
 * <li>{@link #getAllLeafNodes()}: Return all leaf nodes of the current node.</li>
 * <li>{@link #getAllLeavesPath()}: Return paths for all leaves of the current node.</li>
 * <li>{@link #getChildren()}: Returns the node's children.</li>
 * <li>{@link #getChildrenNodes()}: Return children nodes of the current node.</li>
 * <li>{@link #getPath()}: Returns the path of the current node.</li>
 * <li>{@link #getText()}: Return the text of the expandable element.</li>
 * <li>{@link #getVisibleChildren()}: Returns the node's children.</li>
 * <li>{@link #getVisibleElements()}: Return the list of visible nodes in the tree from the current node.</li>
 * <li>{@link #isExpanded()}: Returns whether the current node is expanded or not.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #createChildNodeElement(WebBrowserElement)}: Create the node element instance from the given web element.</li>
 * <li>{@link #getChild(String)}: Returns the node's child matching the given name.</li>
 * <li>{@link #getChildrenElements()}: Returns the children web elements list.</li>
 * <li>{@link #getContainerElement()}: Returns the web element containing children elements.</li>
 * <li>{@link #getExpandableElement()}: Returns the element used to expand the tree node.</li>
 * <li>{@link #getRowNodeElement()}: Returns the tree node element.</li>
 * <li>{@link #getSelectableElement()}: Returns the element used to select the tree node.</li>
 * <li>{@link #initExpandableElement()}: Initialize the expandable element using the corresponding locator.</li>
 * <li>{@link #initRowNodeElement()}: Initialize the expandable element using the corresponding locator.</li>
 * <li>{@link #initSelectableElement()}: Initialize the expandable element using the corresponding locator.</li>
 * <li>{@link #isContainer()}: Returns whether the current node is a container or not.</li>
 * <li>{@link #isLeaf()}: Returns whether the current node is a leaf or not.</li>
 * </ul>
 * </p>
 */
public abstract class SpotTreeNodeElement extends WebElementWrapper implements SpotTreeNode {

//	/**
//	 * The parent of the current folder, <code>null</code> for root element.
//	 */
//	final private SpotFolderElement parent;

	/**
	 * The text of the current folder
	 */
	protected String text;

	/**
	 * The path of the current folder
	 */
	private String path;

	/**
	 * The locator to find the row node element.
	 * <p>
	 * If <code>null</code>, then it's assumed there's no specific element
	 * for the folder node and wrapped element will be used instead.
	 * </p><p>
	 * Note that this locator must be relative to the wrapped element.
	 * </p>
	 */
	protected final By rowNodeLocator;

	/**
	 * The element used for the row node. If <code>null</code>, then it's
	 * assumed that the row node is tree node itself.
	 */
	protected WebBrowserElement rowNodeElement;

	/**
	 * The locator to find the selectable element.
	 * <p>
	 * If <code>null</code>, then it's assumed there's no specific element to
	 * select the folder and the wrapped element will be used instead.
	 * </p><p>
	 * Note that this locator must be relative to the row node element.
	 * </p>
	 */
	protected final By selectableLocator;

	/**
	 * The element used to select the folder. If <code>null</code>, then it's
	 * assumed that the folder selection is done by clicking on the tree node element.
	 */
	protected SpotSelectableElement selectableElement;

	/**
	 * The locator to find the expandable element.
	 * <p>
	 * If <code>null</code>, then it's assumed there's no specific element to
	 * expand the folder and clicking on node element is enough to expand
	 * and collapse the node.
	 * </p><p>
	 * Note that this locator must be relative to the row node element.
	 * </p>
	 */
	protected final By expandableLocator;

//	/**
//	 * The locator to find the web element to expand/collapse the
//	 * expandable element if any.
//	 * <p>
//	 * If <code>null</code>, then it's assumed
//	 * there's no specific element for the expandable element expansion and
//	 * selecting it (ie. clicking on it) is enough to expand and collapse it.
//	 * </p><p>
//	 * Note that this locator must be relative to the {@link #expandableElement}
//	 * found using the {@link #expandableLocator} while creating it in the
//	 * {@link #initExpandableElement()} method.
//	 * </p>
//	 */
//	protected final By expansionLocator;

	/**
	 * The element used to expand the folder. If <code>null</code>, then it's
	 * assumed that the folder expansion is done by clicking on node element.
	 */
	protected SpotExpandableElement expandableElement;

	/**
	 * The locator to find the parent element of the folder's children elements.
	 * <p>
	 * If <code>null</code>, then {@link #getChildrenElements()} method has
	 * to be overridden.
	 * </p><p>
	 * Note that this locator must be relative to the node element.
	 * </p>
	 */
	protected final By containerLocator;

	/**
	 * The element used to get the folder children. If <code>null</code>, then
	 * subclass has to override {@link #getChildrenElements()} method.
	 */
	protected WebBrowserElement containerElement;

public SpotTreeNodeElement(final WebPage page, final WebBrowserElement element, final By node, final By selection, final By expansion, final By container, final SpotTreeNodeElement parentNode) {
	super(page, element);
	this.parent = parentNode;
	this.rowNodeLocator = node;
	this.selectableLocator = selection;
	this.expandableLocator = expansion;
	this.containerLocator = container;
}

@Override
public void collapse() {
	debugPrintEnteringMethod();
	getExpandableElement();
	if (this.expandableElement != null && isContainer()) {
		this.expandableElement.collapse();
	}
}

/**
 * Create the node element instance from the given web element.
 *
 * @param webElement The child node web element
 * @return The created node element
 */
abstract protected SpotTreeNodeElement createChildNodeElement(final WebBrowserElement webElement);

@Override
public void expand() {
	debugPrintEnteringMethod();
	getExpandableElement();
	if (this.expandableElement != null && isContainer()) {
		this.expandableElement.expand();
	}
}

/**
 * Return all leaf nodes of the current node.
 *
 * @return AThe list of leaf nodes
 */
public List<SpotTreeNode> getAllLeafNodes() {

	// Init list
	List<SpotTreeNode> allLeaves = new ArrayList<>();

	// Get the children
	List<SpotTreeNodeElement> children = getChildren();

	// Complete the all leaves list
	for (SpotTreeNodeElement child : children) {
		if (child.isContainer()) {
	    	allLeaves.addAll(child.getAllLeafNodes());
	    } else {
	    	child.element.makeVisible();
	    	child.getPath();
	    	allLeaves.add(child);
	    }
    }

	// Return the list.
	return allLeaves;
}

/**
 * Return paths for all leaves of the current node.
 *
 * @return The paths list of all leaves
 */
public List<String> getAllLeavesPath() {

	// Init list
	List<String> allPaths = new ArrayList<String>();

	// Get the children
	List<SpotTreeNodeElement> children = getChildren();

	// Complete the all leaves list
	for (SpotTreeNodeElement child : children) {
		if (child.isContainer()) {
	    	allPaths.addAll(child.getAllLeavesPath());
	    } else {
	    	child.element.makeVisible();
	    	allPaths.add(child.getPath());
	    }
    }

	// Return the list.
	return allPaths;
}

@Override
public List<String> getAllLabels() {

	// Init list
	List<String> allNodes = new ArrayList<String>();

	// Get the children
	List<SpotTreeNodeElement> children = getChildren();

	// Complete the all leaves list
	for (SpotTreeNodeElement child : children) {
		child.element.makeVisible();
    	allNodes.add(child.getText());

		if (child.isContainer()) {
	    	allNodes.addAll(child.getAllLabels());
	    }
    }

	// Return the list.
	return allNodes;
}

/**
 * Returns the node's child matching the given name.
 *
 * @param node	Name of the child node.
 * @return The matching child node or <code>null</code>
 * if current node has no child with the given name.
 */
protected SpotTreeNodeElement getChild(final String node) {
	// Get the children.
	List<SpotTreeNodeElement> children = getChildren();
	for (SpotTreeNodeElement child : children) {
		String childNodeName = child.getText();
	    if (childNodeName.equals(node)) {
	    	return child;
	    }
    }

	// No match found.
	return null;
}

/**
 * Returns the node's children.
 * <p>
 * Note that this method expand the node to discover all nodes under it.
 * </p>
 * @return The children list
 */
final public List<SpotTreeNodeElement> getChildren() {
	return getChildren(true);
}

private List<SpotTreeNodeElement> getChildren(final boolean expand) {
	debugPrintEnteringMethod();

	// Expand node first if specified
	if (expand) {
		expand();
	}

	// Look for children elements
	if (getContainerElement().isDisplayed()) {
		List<WebBrowserElement> childrenElements = getChildrenElements();
		List<SpotTreeNodeElement> childrenNodeElements = new ArrayList<SpotTreeNodeElement>(childrenElements.size());
		for (WebBrowserElement childElement : childrenElements) {
			SpotTreeNodeElement childNodeElement = createChildNodeElement(childElement);
			childrenNodeElements.add(childNodeElement);
	    }
		return childrenNodeElements;
	}

	// No child
	return Collections.emptyList();
}

/**
 * Returns the children web elements list.
 *
 * @return The children as a {@link List} of {@link WebBrowserElement}.
 */
protected List<WebBrowserElement> getChildrenElements() {
	return getContainerElement().getChildren();
}

@Override
public List<String> getChildrenLabels() {
	debugPrintEnteringMethod();
	List<String> labels = new ArrayList<>();
	for (SpotTreeNodeElement child: getChildren()) {
    	labels.add(child.getText());
    }
	return labels;
}

/**
 * Return children nodes of the current node.
 *
 * @return Children nodes as a {@link List} of {@link String}.
 */
public List<String> getChildrenNodes() {

	// Get the children
	List<SpotTreeNodeElement> children = getChildren();
	List<String> childrenNodes = new ArrayList<String>(children.size());

	// Complete the all leaves list
	for (SpotTreeNodeElement child : children) {
    	child.element.makeVisible();
    	childrenNodes.add(child.getText());
    }

	// Return the list.
	return childrenNodes;
}

/**
 * Returns the web element containing children elements.
 *
 * @return The container web element as a {@link WebBrowserElement}.
 */
protected WebBrowserElement getContainerElement() {
	if (this.containerElement == null) {
		if (this.containerLocator == null) {
			this.containerElement = this.element;
		} else {
			this.containerElement = waitShortlyForMandatoryChildElement(this.containerLocator);
		}
	}
	return this.containerElement;
}

/**
 * Returns the element used to expand the tree node.
 *
 * @return The expandable element or<code>null</code>
 * if there's no specific expandable element
 */
protected SpotExpandableElement getExpandableElement() {
	if (this.expandableElement == null && this.expandableLocator != null) {
		initExpandableElement();
	}
	return this.expandableElement;
}

@Override
public String getLabel() {
	return getText();
}

@Override
public SpotTreeNode getNode(final String nodePath) {
	debugPrintEnteringMethod("nodePath", nodePath);

	// If path is absolute
	if (nodePath.charAt(0) == PATH_SEPARATOR_CHAR && getRootNode() != this) { // != is intentional
		return getRootNode().getNode(nodePath);
	}

	// Get path first segment
	int idx = nodePath.indexOf(PATH_SEPARATOR);
	String firstChildName = nodePath;
	if (idx == 0) {
		idx = nodePath.indexOf(PATH_SEPARATOR, 1);
		if (idx < 0) {
			firstChildName = nodePath.substring(1);
		} else {
			firstChildName = nodePath.substring(1, idx);
		}
	} else if (idx > 0) {
		firstChildName = nodePath.substring(0, idx);
	}

	// Search for matching node's child.
	SpotTreeNodeElement childNode = getChild(firstChildName);

	// Return if on last segment or no child has been found
	if (idx < 0 || childNode == null) {
		return childNode;
	}

	// Recurse to sub-level
	return childNode.getNode(nodePath.substring(idx+1));
}

@Override
public SpotTreeNode getParentNode() {
	return (SpotTreeNodeElement) this.parent;
}

/**
 * Returns the path of the current node.
 * <p>
 * The path is made of all containing nodes texts separated by /'s plus the node
 * text (e.g. "/Custom Reports/Sample Report Definitions/CCM").
 * </p>
 * TODO: It seems to be a really costly operation, needs to be improved...
 */
public String getPath() {
	if (this.path == null) {
		if (this.parent == null) {
			return EMPTY_STRING;
		}
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(((SpotTreeNodeElement)getParentNode()).getPath());
		pathBuilder.append('/').append(getText());
		this.path = pathBuilder.toString();
	}
	return this.path;
}

@Override
public SpotTreeNode getRootNode() {
	if (this.parent == null) {
		return this;
	}
	return getParentNode().getRootNode();
}

/**
 * Returns the tree node element.
 *
 * @return The tree node element
 */
protected WebBrowserElement getRowNodeElement() {
	if (this.rowNodeElement == null) {
		initRowNodeElement();
	}
	return this.rowNodeElement;
}

/**
 * Returns the element used to select the tree node.
 *
 * @return The selectable element or<code>null</code>
 * if there's no specific expandable element
 */
protected SpotSelectableElement getSelectableElement() {
	if (this.selectableElement == null && this.selectableLocator != null) {
		initSelectableElement();
	}
	return this.selectableElement;
}

@Override
public SpotTreeNode getSelectedNode() {
	List<SpotTreeNodeElement> visibleElements = getVisibleElements();
	for (SpotTreeNodeElement nodeElement: visibleElements) {
		if (nodeElement.isSelected()) {
			return nodeElement;
		}
	}
	return null;
}

/**
 * {@inheritDoc}
 * <p>
 * Overrides to return the text of the row node element.
 * </p>
 */
@Override
public String getText() {
	if (this.text == null) {
		this.text = getRowNodeElement() == null ? EMPTY_STRING : this.rowNodeElement.getText();
	}
	return this.text;
}

/**
 * Returns the node's children.
 * <p>
 * Note that this method does not expand the current node.
 * </p>
 * @return The list of visible children
 */
final public List<SpotTreeNodeElement> getVisibleChildren() {
	return getChildren(false);
}

/**
 * Return the list of visible nodes in the tree from the current node.
 *
 * @return The nodes list
 */
public List<SpotTreeNodeElement> getVisibleElements() {
	debugPrintEnteringMethod();

	// Init list
	List<SpotTreeNodeElement> visibleElements = new ArrayList<>();

	// Get the children
	List<SpotTreeNodeElement> children = getVisibleChildren();

	// Recurse on children if any
	for (SpotTreeNodeElement child : children) {
    	visibleElements.add(child);

		if (child.isContainer()) {
	    	visibleElements.addAll(child.getVisibleElements());
	    }
    }

	// Return the list.
	return visibleElements;
}

/**
 * Return the list of visible nodes in the tree from the current node.
 *
 * @return The nodes list
 */
@Override
public List<String> getVisibleLabels() {
	debugPrintEnteringMethod();
	List<String> labels = new ArrayList<>();
	for (SpotTreeNodeElement visibleElement: getVisibleElements()) {
    	labels.add(visibleElement.getText());
    }
	return labels;
}

/**
 * Return the list of visible nodes in the tree from the current node.
 *
 * @return The nodes list
 */
@Override
public List<SpotTreeNode> getVisibleNodes() {
	debugPrintEnteringMethod();

	List<SpotTreeNode> visibleNodes = new ArrayList<>();
	for (SpotTreeNodeElement visibleElement: getVisibleElements()) {
    	visibleNodes.add(visibleElement);
    }
	return visibleNodes;
}

/**
 * Initialize the expandable element using the corresponding locator.
 */
protected void initExpandableElement() {
	this.expandableElement = new SpotExpandableElement(getPage(), getRowNodeElement(), this.expandableLocator);
}

/**
 * Initialize the expandable element using the corresponding locator.
 */
protected void initRowNodeElement() {
	if (this.rowNodeLocator == null) {
		this.rowNodeElement = this.element;
	} else {
		this.rowNodeElement = this.element.waitShortlyForMandatoryDisplayedChildElement(this.rowNodeLocator);
	}
}

/**
 * Initialize the expandable element using the corresponding locator.
 */
protected void initSelectableElement() {
	this.selectableElement = new SpotSelectableElement(getPage(), getRowNodeElement(), this.selectableLocator);
}

/**
 * Returns whether the current node is a container or not.
 *
 * @return <code>true</code> if the current node is a container, <code>false</code>
 * if it's a leaf.
 */
protected boolean isContainer() {
	getExpandableElement();
	if (this.expandableElement != null) {
		return this.expandableElement.isExpandable();
	}
	throw new ScenarioFailedError("Class "+getClassSimpleName(getClass())+" should override isContainer() method.");
}

/**
 * Returns whether the current node is expanded or not.
 *
 * @return <code>true</code> if the current node is a container and it's expanded,
 * <code>false</code> otherwise.
 */
public boolean isExpanded() {
	if (isContainer()) {
		return this.expandableElement.isExpanded();
	}
	debugPrintln("		+ Current node element '"+getText()+"' is not a container, hence it's considered as not expanded...");
	return false;
}

/**
 * Returns whether the current node is a leaf or not.
 *
 * @return <code>true</code> if the current node is a leaf, <code>false</code>
 * if it's a container.
 */
protected boolean isLeaf() {
	return !isContainer();
}

@Override
public boolean isSelected() {
	return getSelectableElement().isSelected();
}

@Override
public SpotTreeNode searchNode(final String node) throws ScenarioImplementationError {
	debugPrintEnteringMethod("node", node);

	// Check whether the nodePath includes separators
	int idx = node.indexOf(PATH_SEPARATOR);
	if (idx >= 0) {
		throw new ScenarioImplementationError("Canot search for a node path. Use getNode(String) method instead.");
	}

	// Recurse to find the node name
	List<SpotTreeNodeElement> children = getChildren();

	// Check children first
	for (SpotTreeNodeElement child : children) {
		if (child.getText().equals(node)) {
			return child;
		}
    }

	// Recurse sub-levels if still not found
	for (SpotTreeNodeElement child : children) {
		if (child.isContainer()) {
	    	SpotTreeNode searchedNode = child.searchNode(node);
	    	if (searchedNode != null) {
	    		return searchedNode;
    		}
		}
    }

	// No node was found.
	return null;
}

@Override
public void select() {
	getSelectableElement().clickAndWaitForSelection();
}

@Override
public SpotTreeNode selectNode(final String nodePath) throws ScenarioFailedError {
	debugPrintEnteringMethod("nodePath", nodePath);
	SpotTreeNode treeNode = getNode(nodePath);
	if (treeNode == null) {
		throw new ScenarioFailedError("No node '"+nodePath+"' has been found in the current node tree '"+getLabel()+"'.");
	}
	treeNode.select();
	return treeNode;
}
}
