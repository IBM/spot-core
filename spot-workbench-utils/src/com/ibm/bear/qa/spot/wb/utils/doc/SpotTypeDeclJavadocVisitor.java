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

import java.util.*;

import org.eclipse.jdt.core.dom.*;

/**
 * AST visitor to read a Type Declaration javadoc comment and identify paragraphs
 * dedicated to method declaration references.
 * <p>
 *
 * </p>
 */
public class SpotTypeDeclJavadocVisitor extends ASTVisitor {

	/**
	 * Internal class to hold an HTML element declared in the javadoc comment.
	 * <p>
	 * HTML elements can be nested and have children.
	 * </p><p>
	 * Warning: This class does not intend to represent all possible HTML element.
	 * It's a convenient class to manage paragraphs and tables usually used in
	 * framework type declaration javadoc comment.
	 * </p>
	 */
	abstract class HtmlElement {
		int startPosition, endPosition = -1;
		final HtmlElement parent;
		final Stack<HtmlElement> children = new Stack<HtmlElement>();

		/**
		 * Create an HTML element instance, potentially nested in another element
		 * with given starting and end positions.
		 *
		 * @param element The parent element or <code>null</code> if this is a root element
		 * @param startPosition The element start position in the javadoc comment
		 * @param endPosition The element end position in the javadoc comment
		 */
		protected HtmlElement(final HtmlElement element, final int startPosition, final int endPosition) {
			this.parent = element;
			if (element != null) {
				this.parent.children.add(this);
			}
			this.startPosition = startPosition;
			this.endPosition = endPosition;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Assume that two HTML elments are equals when they have same start position.
		 * </p>
		 */
		@Override
		public boolean equals(final Object o) {
			if (o instanceof HtmlElement) {
				return this.startPosition == ((HtmlElement) o).startPosition;
			}
			return super.equals(o);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Use element start position as hash code.
		 * </p>
		 */
		@Override
		public int hashCode() {
			return this.startPosition;
		}

		/**
		 * Tells whether the HTML element is closed or not.
		 * <p>
		 * Closed means that the corresponding tag closed has been read at the same nested level.
		 * </p>
		 * @return <code>true</code> if the element is closed, <code>false</code> otherwise
		 */
		boolean isClosed() {
			return this.endPosition > 0;
		}

		/**
		 * Tells whether the HTML element is a paragraph or not.
		 * <p>
		 * The corresponding element will have a <b>p</b> tag name.
		 * </p>
		 * @return <code>true</code> if the element is a paragraph, <code>false</code> otherwise
		 */
		boolean isParagraph() {
			return false;
		}

		/**
		 * Tells whether the HTML element is a table or not.
		 * <p>
		 * The corresponding element will have a <b>ul</b> tag name.
		 * </p>
		 * @return <code>true</code> if the element is a table, <code>false</code> otherwise
		 */
		boolean isTable() {
			return false;
		}

	}

	/**
	 * A pargraph HTML element.
	 * <p>
	 * Note that for the kind of framework javadoc comment supposed to be visited
	 * here, we assume that it's simply HTML element with <b>p</ul> tag name.
	 * </p>
	 */
	class Paragraph extends HtmlElement {
		protected Paragraph(final HtmlElement element, final int startPosition) {
			super(element, startPosition, -1);
		}
		@Override
		boolean isParagraph() {
			return true;
		}
	}

	/**
	 * A table HTML element.
	 * <p>
	 * Note that for the kind of framework javadoc comment supposed to be visited
	 * here, we assume that it's simply HTML element with <b>ul</ul> tag name.
	 * </p>
	 */
	class Table extends HtmlElement {
		protected Table(final HtmlElement element, final int startPosition) {
			super(element, startPosition, -1);
		}
		@Override
		boolean isTable() {
			return true;
		}
	}

	/* Fields */
	// Tells whether an error occurred during the javadoc comment visit
	boolean error = false;
	// The list of root HTML elements identified during the javadoc comment visit
	final private List<HtmlElement> elements = new ArrayList<>();
	// The list of root method references identified during the javadoc comment visit
	final List<MethodRef> methods = new ArrayList<>();
	// The list HTML elements to be removed (ie. which contains method references)
	final private List<HtmlElement> methodRefParagraphs = new ArrayList<>();
	// The current HTML elements stack
	private Stack<HtmlElement> elementsStack = new Stack<>();
	// Previous fragment
	ASTNode previousFragment = null;
	// Existing references
	Map<String, String> existingMethodReferences = new HashMap<>();

public SpotTypeDeclJavadocVisitor() {
	super(true);
}

/**
 * {@inheritDoc}
 * <p>
 * Check each method reference and set its encapsulating HTML root element
 * as to be deleted.
 * </p>
 */
@Override
public void endVisit(final Javadoc node) {
	super.endVisit(node);
	if (this.existingMethodReferences.size() > 0) {
		for (MethodRef method: this.methods) {
			for (HtmlElement element: this.elements) {
				if (method.getStartPosition() > element.startPosition && (method.getStartPosition()+method.getLength()) < element.endPosition) {
					if (!this.methodRefParagraphs.contains(element)) {
						this.methodRefParagraphs.add(element);
					}
					break;
				}
			}
		}
	}
}

/**
 * Return the list of root HTML elements which contain method references.
 *
 * @return The elements list
 */
public List<HtmlElement> getMethodRefParagraphs() {
	return this.methodRefParagraphs;
}

/**
 * {@inheritDoc}
 * <p>
 * Just add the method reference to the list.
 * </p>
 */
@Override
public boolean visit(final MethodRef node) {
	this.methods.add(node);
	this.previousFragment = node;
	return false;
}

/**
 * {@inheritDoc}
 * <p>
 * Continue the tag element visit only if the tag element has no name
 * or if it's nested (in order to process method references)
 * </p>
 */
@Override
public boolean visit(final TagElement node) {
	return node.getTagName() == null || node.isNested();
}

/**
 * {@inheritDoc}
 * <p>
 * Process following operation depending on text element content:
 * <ul>
 * <li><code>&lt;ul&gt;</code>: push a new HTML table element in the elements stack</li>
 * <li><code>&lt;/p&gt;&lt;p&gt;</code>: perform following actions
 * <ol>
 * <li>pop the last element from the stack</li>
 * <li>check that this element is HTML paragraph element</li>
 * <li>add this element in the list if it's a root element</li>
 * <li>push a new a new HTML pargraph element in the elements stack</li>
 * </ol></li>
 * <li><code>&lt;p&gt;</code>: push a new HTML paragraph element in the elements stack</li>
 * <li><code>&lt;/ul&gt;</code>: perform following actions
 * <ol>
 * <li>pop the last element from the stack</li>
 * <li>check that this element is HTML table element</li>
 * <li>close this element</li>
 * <li>add this element in the list if it's a root element</li>
 * </ol></li>
 * <li><code>&lt;/p&gt;</code>: perform following actions
 * <ol>
 * <li>pop the last element from the stack</li>
 * <li>check that this element is HTML paragraph element</li>
 * <li>close this element</li>
 * <li>add this element in the list if it's a root element</li>
 * </ol></li>
 * </ul>
 * </p>
 * TODO Improve the behavior when the paragraph and/or table
 * HTML elements are not closed...
 */
@Override
public boolean visit(final TextElement node) {

	// Get stack last element
	HtmlElement lastElement = this.elementsStack.isEmpty() ? null : this.elementsStack.lastElement();

	// Add corresponding child
	try {
		String text = node.getText();
		if (text.equals("<ul>")) {
			this.elementsStack.push(new Table(lastElement, node.getStartPosition()));
		}
		else if (text.equals("</p><p>")) {
			if (this.elementsStack.isEmpty()) {
				this.error = true;
				return false;
			}
			if (lastElement == null || !lastElement.isParagraph()) {
				this.error = true;
				return false;
			}
			lastElement = this.elementsStack.pop();
			int position = node.getStartPosition()+4;
			lastElement.endPosition = position;
			if (this.elementsStack.isEmpty()) {
				if (lastElement.parent != null) {
					this.error = true;
					return false;
				}
				this.elements.add(lastElement);
			}
			this.elementsStack.push(new Paragraph(lastElement.parent, position));
		}
		else if (text.equals("<p>")) {
			this.elementsStack.push(new Paragraph(lastElement, node.getStartPosition()));
		}
		else if (text.equals("</ul>")) {
			if (this.elementsStack.isEmpty()) {
				this.error = true;
				return false;
			}
			if (lastElement == null || !lastElement.isTable()) {
				this.error = true;
				return false;
			}
			lastElement = this.elementsStack.pop();
			lastElement.endPosition = node.getStartPosition()+node.getLength();
			if (this.elementsStack.isEmpty()) {
				if (lastElement.parent != null) {
					this.error = true;
					return false;
				}
				this.elements.add(lastElement);
			}
		}
		else if (text.equals("</p>")) {
			if (this.elementsStack.isEmpty()) {
				this.error = true;
				return false;
			}
			if (lastElement == null || !lastElement.isParagraph()) {
				this.error = true;
				return false;
			}
			lastElement = this.elementsStack.pop();
			lastElement.endPosition = node.getStartPosition()+node.getLength();
			if (this.elementsStack.isEmpty()) {
				if (lastElement.parent != null) {
					this.error = true;
					return false;
				}
				this.elements.add(lastElement);
			}
		}
		else if (text.trim().startsWith(":") && text.trim().endsWith("</li>") && this.previousFragment != null && this.previousFragment.getNodeType() == ASTNode.METHOD_REF) {
			this.existingMethodReferences.put(this.previousFragment.toString(), text.trim().substring(1, text.trim().length()-5).trim());
		}
		return false;
	}
	finally {
		this.previousFragment = null;
	}
}

String getSummary(final MethodRef methodRef) {
	return this.existingMethodReferences.get(methodRef.toString());
}
}
