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
package com.ibm.bear.qa.spot.core.api.elements;

import java.util.List;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioImplementationError;

/**
 * Interface defining API for a tree node element (eg. having the <code>dijitTreeNode</code>
 * attribute class).
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * </ul>
 * </p><p>
 * This class defines following public API methods of {@link SpotTreeNode} interface:
 * <ul>
 * <li>{@link #collapse()}: Collapse the current node.</li>
 * <li>{@link #expand()}: Expand the current node.</li>
 * <li>{@link #getChildrenLabels()}: Return the labels list of current node children.</li>
 * <li>{@link #getLabel()}: Return the node label.</li>
 * <li>{@link #getNode(String)}: Get the node for the given path.</li>
 * <li>{@link #getParentNode()}: Returns the parent node of the current node.</li>
 * <li>{@link #getRootNode()}: Returns the tree root node.</li>
 * <li>{@link #getSelectedNode()}: Return the selected node in the tree relatively to the current node.</li>
 * <li>{@link #getVisibleLabels()}: Return the list of visible labels in the tree relatively to the current node.</li>
 * <li>{@link #getVisibleNodes()}: Return the list of visible nodes in the tree relatively to the current node.</li>
 * <li>{@link #isSelected()}: Returns whethert the current node is selected or not.</li>
 * <li>{@link #searchNode(String)}: Searches the node with the given name in the hierarchy starting from current node.</li>
 * <li>{@link #select()}: Select the current node.</li>
 * <li>{@link #selectNode(String)}: Selects the node in the tree matching the given path relatively to the current node.</li>
 * </ul>
 * </p>
 */
public interface SpotTreeNode {

/**
 * Collapse the current node.
 * <p>
 * This is a no-op if the current node is a leaf.
 * </p>
 */
void collapse();

/**
 * Expand the current node.
 * <p>
 * This is a no-op if the current node is a leaf.
 * </p>
 */
void expand();

/**
 * Return all nodes labels of the current node.
 *
 * @return All nodes as a {@link List} of {@link String}.
 */
List<String> getAllLabels() ;

/**
 * Return the labels list of current node children.
 *
 * @return The labels list
 */
List<String> getChildrenLabels();

/**
 * Return the node label.
 *
 * @return The label
 */
String getLabel();

/**
 * Get the node for the given path.
 * <p>
 * The node research will start from the tree root node if the given path
 * starts with the path separator (eg. "/aaa/bbb/ccc"). In a contrary, the
 * research will occur relatively to the current node if the given path does
 * not start with the path separator (eg. "aaa/bbb/ccc").
 * </p><p>
 * If path has several segments, then the search occurs recursively through
 * sub-nodes levels to match <b>each</b> segment of the given path.
 * </p>
 * @param nodePath The node path. Might be a simple name or a fully-qualified
 * path using '/' character for segment delimiter.
 * @return The found node or <code>null</code> if no node was not found
 * for the given node path.
 */
SpotTreeNode getNode(final String nodePath);

/**
 * Returns the parent node of the current node.
 *
 * @return The parent node or <code>null</code> if current node is the root.
 */
SpotTreeNode getParentNode();

/**
 * Returns the tree root node.
 *
 * @return The root node
 */
SpotTreeNode getRootNode();

/**
 * Return the selected node in the tree relatively to the current node.
 * <p>
 * Note that this method does not modify the tree expansion. That means
 * no expand/collapse is done nor select while running it.
 * </p>
 * @return The selected node or <code>null</code> if none was selected
 * relatively to the current node
 */
SpotTreeNode getSelectedNode();

/**
 * Return the list of visible labels in the tree relatively to the current node.
 *
 * @return The labels list
 */
List<String> getVisibleLabels();

/**
 * Return the list of visible nodes in the tree relatively to the current node.
 *
 * @return The nodes list
 */
List<SpotTreeNode> getVisibleNodes();

/**
 * Returns whethert the current node is selected or not.
 *
 * @return <code>true</code> if the node is selected, <code>false</code> otherwise.
 */
boolean isSelected();

/**
 * Searches the node with the given name in the hierarchy starting from current node.
 * <p>
 * There's no indication if the returned node is the unique one with the given
 * name in the hierarchy, as the first matching node (deepest first) is returned.
 * </p><p>
 * WARNING: This method expands each node encountered in the tree until
 * having found the searched node. Hence that represent a large amount of
 * time to execute this method in case of big trees as usually server requests
 * are necessary to get each node children.
 * </p>
 * @param name The node to search in the hierarchy. It must be a simple name.
 * @return The first node matching the given name or <code>null</code> if none was found.
 * @throws ScenarioImplementationError If the given node contains path separator
 */
SpotTreeNode searchNode(final String name) throws ScenarioImplementationError;

/**
 * Select the current node by clicking on it.
 */
void select();

/**
 * Selects the node in the tree matching the given path relatively to the current node.
 * <p>
 * The node research will start from the tree root node if the given path
 * starts with the path separator (eg. "/aaa/bbb/ccc"). In a contrary, the
 * research will occur relatively to the current node if the given path does
 * not start with the path separator (eg. "aaa/bbb/ccc").
 * </p><p>
 * If path has several segments, then the search occurs recursively through
 * sub-nodes levels to match <b>each</b> segment of the given path.
 * </p>
 * @param nodePath The node path. Might be a simple name or a fully-qualified
 * path using '/' character for segment delimiter.
 * @return The selected node
 * @throws ScenarioFailedError if no folder was not found for the given folder path.
 * @see #getNode(String)
 */
SpotTreeNode selectNode(final String nodePath) throws ScenarioFailedError;

}
