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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.MultiTextEdit;

import com.ibm.bear.qa.spot.wb.utils.doc.SpotTypeDeclJavadocVisitor.HtmlElement;
import com.ibm.bear.qa.spot.wb.utils.rewrite.SpotAbstractVisitor;

/**
 * AST Visitor to extract method references to public and protected method
 * declarations and put them in specific paragraphs of the class javadoc.
 *
 * TODO Replace existing method reference instead of adding them blindly
 */
public class SpotClassDocVisitor extends SpotAbstractVisitor {

	final class MethodDeclarationComparator implements Comparator<MethodDeclaration> {
		@Override
		public int compare(final MethodDeclaration md1, final MethodDeclaration md2) {
			return md1.getName().toString().compareTo(md2.getName().toString());
		}
	}

	private static final String TODO_SUMMARY_METHOD = "TODO Add a javadoc with a meaningful summary to this method !";
	private static final ASTMatcher AST_MATCHER = new ASTMatcher();
	final MultiTextEdit rootEdit;
	List<MethodDeclaration> missingJavadocMethods = new ArrayList<>();
	Map<String, List<MethodDeclaration>> interfaceMethods = new HashMap<>();
	List<MethodDeclaration> publicMethods = new ArrayList<>();
	List<MethodDeclaration> protectedMethods = new ArrayList<>();
	private SpotTypeDeclJavadocVisitor typeDeclarationJavadocVisitor;
//	String initialJavadoc;
	boolean hadJavadoc = false;

protected SpotClassDocVisitor(final CompilationUnit root, final ICompilationUnit cu) {
	super(root, cu);
	this.rootEdit = new MultiTextEdit(0, this.source.length());
}

/**
 * Add documented methods references tables in the given javadoc tag.
 * <p>
 * This method assumes that the given tag is the first one of the
 * javadoc (ie. it has a <code>null</code> tag name).
 * </p>
 * @param tag The javadoc empty tag to which method references has to be added
 *
 * TODO Replace existing method reference instead of adding them blindly
 */
private void addAllMethodReferences(final TagElement tag) {

	ListRewrite rewrittenFragments = this.rewriter.getListRewrite(tag, TagElement.FRAGMENTS_PROPERTY);

	// Open the paragraph section
	TextElement text = (TextElement) this.rewriter.createStringPlaceholder("<p>", ASTNode.TEXT_ELEMENT);
	rewrittenFragments.insertLast(text, null);

	// Extract interface methods from public methods
	if (this.publicMethods.size() > 0) {
		extractInterfaceMethodsFromPublicOnes();
	}

	// Add interface method references table if any
	String also = "";
	if (this.interfaceMethods.size() > 0) {
		for (String interfaceName: this.interfaceMethods.keySet()) {
			text = (TextElement) this.rewriter.createStringPlaceholder("This class "+also+"defines following public API methods of {@link "+interfaceName+"} interface:", ASTNode.TEXT_ELEMENT);
			rewrittenFragments.insertLast(text, null);
			addMethodReferences(tag, rewrittenFragments, this.interfaceMethods.get(interfaceName));
			also = "also ";
		}
	}

	// Add public method references table if any
	if (this.publicMethods.size() > 0) {

		// Insert paragraphs separation if necessary
		if (this.interfaceMethods.size() > 0) {
			text = (TextElement) this.rewriter.createStringPlaceholder("</p><p>", ASTNode.TEXT_ELEMENT);
			rewrittenFragments.insertLast(text, null);
		}

		// Add public method references table
		text = (TextElement) this.rewriter.createStringPlaceholder("This class "+also+"defines following internal API methods:", ASTNode.TEXT_ELEMENT);
		rewrittenFragments.insertLast(text, null);
		addMethodReferences(tag, rewrittenFragments, this.publicMethods);
		also = "also ";
	}

	// Add protected methods references if any
	if (this.protectedMethods.size() > 0) {

		// Insert paragraphs separation if necessary
		if (this.interfaceMethods.size() > 0 || this.publicMethods.size() > 0) {
			text = (TextElement) this.rewriter.createStringPlaceholder("</p><p>", ASTNode.TEXT_ELEMENT);
			rewrittenFragments.insertLast(text, null);
		}

		// Add protected method references table
		text = (TextElement) this.rewriter.createStringPlaceholder("This class "+also+"defines or overrides following methods:", ASTNode.TEXT_ELEMENT);
		rewrittenFragments.insertLast(text, null);
		addMethodReferences(tag, rewrittenFragments, this.protectedMethods);
	}

	// Close the paragraph section
	text = (TextElement) this.rewriter.createStringPlaceholder("</p>", ASTNode.TEXT_ELEMENT);
	rewrittenFragments.insertLast(text, null);
}

/**
 * Add references in given javadoc text tag for the given methods list.
 *
 * @param textTag The javadoc text tag to add method references
 * @param rewrittenFragments The rewriter list to add new method references
 * @param methods The methods to add references
 */
@SuppressWarnings("unchecked")
private void addMethodReferences(final TagElement textTag, final ListRewrite rewrittenFragments, final List<MethodDeclaration> methods) {

	// Add table creation html tag
	TextElement text = (TextElement) this.rewriter.createStringPlaceholder("<ul>", ASTNode.TEXT_ELEMENT);
	rewrittenFragments.insertLast(text, null);

	// Sort methods list
	Collections.sort(methods, new MethodDeclarationComparator());

	// Add table entry for each given method
	for (MethodDeclaration method: methods) {

		// Add table entry html tag
		text = (TextElement) this.rewriter.createStringPlaceholder("<li>", ASTNode.TEXT_ELEMENT);
		rewrittenFragments.insertLast(text, null);

		// Create method reference tag
		TagElement linkTag = this.ast.newTagElement();
		linkTag.setTagName("@link");
		MethodRef methodRef = this.ast.newMethodRef();
		methodRef.setName(this.ast.newSimpleName(method.getName().toString()));
		for (Object obj: method.parameters()) {
			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) obj;
			MethodRefParameter refParameter = this.ast.newMethodRefParameter();
			Type type;
			IVariableBinding variableBinding = variableDeclaration.resolveBinding();
			if (variableBinding.getType().isTypeVariable()) {
				ITypeBinding erasureBinding = variableBinding.getType().getErasure();
				type = this.ast.newSimpleType(this.ast.newSimpleName(erasureBinding.getName()));
			} else {
				type = getParameterType(variableDeclaration.getType());
			}
			refParameter.setType(type);
			refParameter.setVarargs(variableDeclaration.isVarargs());
			methodRef.parameters().add(refParameter);
		}

		// Add method reference tag
		linkTag.fragments().add(methodRef);
		textTag.fragments().add(linkTag);
		rewrittenFragments.insertLast(linkTag, null);

		// Add method javadoc summary and add closing table entry html tag
		String methodSummary = getJavadocSummary(method.getJavadoc());
		if (methodSummary == null) {
			methodSummary = getDefaultSummaryMethod(methodRef);
		}
		text = (TextElement) this.rewriter.createStringPlaceholder(": "+methodSummary+"</li>", ASTNode.TEXT_ELEMENT);
		rewrittenFragments.insertLast(text, null);
	}

	// Add closing table html tag
	text = (TextElement) this.rewriter.createStringPlaceholder("</ul>", ASTNode.TEXT_ELEMENT);
	rewrittenFragments.insertLast(text, null);
}

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
//	checkEmptyJavadoc();
	if (this.interfaceMethods.size() > 0 || this.publicMethods.size() > 0 || this.protectedMethods.size() > 0) {
		udpateTypeDeclarationJavadoc(node);
	}
	super.endVisit(node);
}

private void extractInterfaceMethodsFromPublicOnes() {
	Map<String, List<MethodDeclaration>> extractedMethods = new HashMap<>();
	for (MethodDeclaration method: this.publicMethods) {
		IMethodBinding methodBinding = method.resolveBinding();
		ITypeBinding declaringClassBinding = methodBinding.getDeclaringClass();
		ITypeBinding[] interfaces = declaringClassBinding.getInterfaces();
		if (interfaces.length > 0) {
			interfaceLoop: for (ITypeBinding interfaceBinding: interfaces) {
				for (IMethodBinding interfaceMethodBinding: interfaceBinding.getDeclaredMethods()) {
					if (methodBinding.overrides(interfaceMethodBinding)) {
						List<MethodDeclaration> methods = extractedMethods.get(interfaceBinding.getName());
						if (methods == null) {
							extractedMethods.put(interfaceBinding.getName(), methods = new ArrayList<>());
						}
						methods.add(method);
						break interfaceLoop;
					}
				}
			}
		}
	}
	if (extractedMethods.size() > 0) {
		for (List<MethodDeclaration> methods: extractedMethods.values()) {
			this.publicMethods.removeAll(methods);
		}
		this.interfaceMethods.putAll(extractedMethods);
	}
}

/**
 * Get default summary method.
 * <p>
 * If method reference is for an overridden method, then searches in class hierarchy
 * for the default method summary.
 * </p><p>
 * If superclass method has no summary, the check whether the method was already
 * referenced in class javadoc. if so, then use the previously written summary.
 * </p><p>
 * In case no superclass method or existing summary was found, then return
 * the default todo summary.
 * </p>
 * @param methodRef the method reference
 * @return The default summary to use
 */
private String getDefaultSummaryMethod(final MethodRef methodRef) {
	for (MethodDeclaration method: this.missingJavadocMethods) {
		if (method.getName().subtreeMatch(AST_MATCHER, methodRef.getName())) {
			if (method.parameters().size() == methodRef.parameters().size()) {
				boolean sameMethod = true;
				for (int i=0; i<method.parameters().size(); i++) {
					ASTNode param1 = (ASTNode) method.parameters().get(i);
					ASTNode param2 = (ASTNode) methodRef.parameters().get(i);
					if (param1.toString().equals(param2.toString())) {
						sameMethod = false;
					}
				}
				if (sameMethod) {
					IMethodBinding methodBinding = method.resolveBinding();
					ITypeBinding superTypeBinding = methodBinding.getDeclaringClass().getSuperclass();
					String methodSummaryFromHierarchy = getMethodJavadocSummaryFromHierarchy(methodBinding, superTypeBinding);
					if (methodSummaryFromHierarchy != null) {
						this.missingJavadocMethods.remove(method);
						return methodSummaryFromHierarchy;
					}
					for (ITypeBinding interfaceBinding: methodBinding.getDeclaringClass().getInterfaces()) {
						methodSummaryFromHierarchy = getMethodJavadocSummaryFromHierarchy(methodBinding, interfaceBinding);
						if (methodSummaryFromHierarchy != null) {
							this.missingJavadocMethods.remove(method);
							return methodSummaryFromHierarchy;
						}
					}
				}
			}
		}
	}
	if (this.typeDeclarationJavadocVisitor != null) {
		String methodSummary = this.typeDeclarationJavadocVisitor.getSummary(methodRef);
		if (methodSummary != null) {
			return methodSummary;
		}
	}
	return TODO_SUMMARY_METHOD;
}

private String getMethodJavadocSummaryFromHierarchy(final IMethodBinding methodBinding, final ITypeBinding typeBinding) {
	ITypeBinding supertypeBinding = typeBinding;
	while (supertypeBinding != null) {
		for (IMethodBinding superMethodBinding: supertypeBinding.getDeclaredMethods()) {
			if (methodBinding.overrides(superMethodBinding)) {
				IJavaElement superclassJavaElement = supertypeBinding.getJavaElement().getParent();
				SpotMethodSummaryVisitor methodDeclarationsVisitor = new SpotMethodSummaryVisitor((ITypeRoot) superclassJavaElement, superMethodBinding);
				methodDeclarationsVisitor.parse();
				if (methodDeclarationsVisitor.methodJavadocSummary != null) {
					return methodDeclarationsVisitor.methodJavadocSummary;
				}
			}
		}
		supertypeBinding = supertypeBinding.getSuperclass();
	}
	return null;
}

private Type getParameterType(final Type variableType) {
	Type type;
	if (variableType.isPrimitiveType()) {
		Code typeCode = PrimitiveType.toCode(variableType.toString());
		type = this.ast.newPrimitiveType(typeCode);
	} else if (variableType.isSimpleType()) {
		type = this.ast.newSimpleType(this.ast.newSimpleName(variableType.toString()));
	} else if (variableType.isArrayType()) {
		ArrayType arrayType = (ArrayType) variableType;
		type = this.ast.newArrayType(getParameterType(arrayType.getElementType()));
	} else if (variableType.isParameterizedType()) {
		ParameterizedType paramType = (ParameterizedType) variableType;
		type = this.ast.newSimpleType(this.ast.newSimpleName(paramType.getType().toString()));
	} else {
		throw new RuntimeException("Cannot handle parameter type "+variableType+" in "+this.unit.getElementName());
	}
	return type;
}

/**
 * Update the type declaration javadoc comment with method references.
 *
 * @param node The type declaration node
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
protected void udpateTypeDeclarationJavadoc(final TypeDeclaration node) {

	// Get type declaration javadoc and remove existing method references if any
	Javadoc typeDeclarationJavadoc = node.getJavadoc();
	TagElement firstTagElement = this.ast.newTagElement();
	if (typeDeclarationJavadoc == null) {
		// No javadoc exist, create it...
		typeDeclarationJavadoc = this.ast.newJavadoc();
		typeDeclarationJavadoc.tags().add(firstTagElement);
		this.rewriter.set(node, TypeDeclaration.JAVADOC_PROPERTY, typeDeclarationJavadoc, null);
	} else {
		ListRewrite rewrittenTags = this.rewriter.getListRewrite(typeDeclarationJavadoc, Javadoc.TAGS_PROPERTY);
		this.typeDeclarationJavadocVisitor = new SpotTypeDeclJavadocVisitor();
		typeDeclarationJavadoc.accept(this.typeDeclarationJavadocVisitor);
		List tags = typeDeclarationJavadoc.tags();
		if (tags.size() == 0) {
			Javadoc newJavadoc = this.ast.newJavadoc();
			newJavadoc.tags().add(firstTagElement);
			this.rewriter.replace(typeDeclarationJavadoc, newJavadoc, null);
		} else {
			TagElement currentTagElement = (TagElement) tags.get(0);
			if (currentTagElement.getTagName() != null) {
				rewrittenTags.insertFirst(firstTagElement, null);
			} else {
				if (!this.typeDeclarationJavadocVisitor.error && this.typeDeclarationJavadocVisitor.getMethodRefParagraphs().size() > 0) {
					// Some method references have been found, do not add fragment root html paragraphs containing them
					ASTNode previousFragment = null;
					boolean previousWasAdded = false;
					for (Object obj: currentTagElement.fragments()) {
						ASTNode fragment = (ASTNode) obj;
						int startPosition = fragment.getStartPosition();
						int endPosition = startPosition + fragment.getLength();

						// Check whether the fragment is inside a removed html paragraph
						boolean toBeAdded = true;
						for (HtmlElement element: this.typeDeclarationJavadocVisitor.getMethodRefParagraphs()) {
							if (startPosition == element.startPosition || (startPosition > element.startPosition && endPosition <= element.endPosition)) {
								toBeAdded = false;
								break;
							}
						}
						if (toBeAdded) {
							// fragment should not be removed, hence add it
							int fragmentStart = fragment.getStartPosition();
							int fragmentEnd = fragmentStart + fragment.getLength();
							if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
								firstTagElement.fragments().add(this.rewriter.createStringPlaceholder(this.source.substring(fragmentStart, fragmentEnd), ASTNode.TEXT_ELEMENT));
							} else {
								TextElement textElement = (TextElement) fragment;
								if (!previousWasAdded && textElement.getText().equals("</p><p>")) {
									// Previous text element was removed but current one contains ending paragraph for previous text
									// In such case replace the text element with a single starting paragraph
									TextElement newText = this.ast.newTextElement();
									newText.setText("<p>");
									firstTagElement.fragments().add(newText);
								} else {
									TextElement newText = this.ast.newTextElement();
									newText.setText(this.source.substring(fragmentStart, fragmentEnd));
									firstTagElement.fragments().add(newText);
								}
							}
						} else {
							// Current fragment needs to be removed
							if (previousFragment != null && previousWasAdded) {
								if (previousFragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
									TextElement previousTextElement = (TextElement) previousFragment;
									if (previousTextElement.getText().equals("</p><p>")) {
										// if previous text element was added but includes current paragraph starting
										// then replace it by a single paragraph closing (except if it was alaredy replaced by a single paragraph opening)
										List fragments = firstTagElement.fragments();
										TextElement lastElement = (TextElement) fragments.remove(fragments.size()-1);
										if (!lastElement.getText().equals("<p>")) {
											TextElement newText = this.ast.newTextElement();
											newText.setText("</p>");
											fragments.add(newText);
										}
									}
								}
							}
						}
						previousWasAdded = toBeAdded;
						previousFragment = fragment;
					}
				} else {
					// Current javadoc has no method references, just rebuild it (necessary to have the javadoc rewriting when adding method references)
					for (Object obj: currentTagElement.fragments()) {
						ASTNode fragment = (ASTNode) obj;
						int fragmentStart = fragment.getStartPosition();
						int fragmentEnd = fragmentStart + fragment.getLength();
						if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
							firstTagElement.fragments().add(this.rewriter.createStringPlaceholder(this.source.substring(fragmentStart, fragmentEnd), ASTNode.TEXT_ELEMENT));
						} else {
							TextElement newText = this.ast.newTextElement();
							newText.setText(this.source.substring(fragmentStart, fragmentEnd));
							firstTagElement.fragments().add(newText);
						}
					}
				}
				if (firstTagElement.fragments().size() == 0) {
					Javadoc newJavadoc = this.ast.newJavadoc();
					newJavadoc.tags().add(firstTagElement);
					this.rewriter.replace(typeDeclarationJavadoc, newJavadoc, null);
				} else {
					rewrittenTags.replace(currentTagElement, firstTagElement, null);
				}
			}
		}
	}

	// Build new javadoc content with references to public and protected type methods
	addAllMethodReferences(firstTagElement);

	// Increment changes counter
	this.changes++;
}

/**
 * {@inheritDoc}
 * <p>
 * Do not parse annotation type declaration as we only want
 * to have methods declared for the main type.
 * </p>
 */
@Override
public boolean visit(final AnnotationTypeDeclaration node) {
	return false;
}

/**
 * {@inheritDoc}
 * <p>
 * Do not parse anonymous class declaration as we only want
 * to have methods declared for the main type.
 * </p>
 */
@Override
public boolean visit(final AnonymousClassDeclaration node) {
	return false;
}

/**
 * {@inheritDoc}
 * <p>
 * Do not parse enumeration declaration as we only want
 * to have methods declared for the main type.
 * </p>
 */
@Override
public boolean visit(final EnumDeclaration node) {
	return false;
}

/**
 * {@inheritDoc}
 * <p>
 * When entering a method declaration, then perform following actions if the method
 * is not a constructor:
 * <ol>
 * <li>Check whether the given declaration node has a javadoc or not.</li>
 * <li>Store the method declaration in appropriate list (ie. either public or protected)</li>
 * </ol>
 * </p>
 */
@Override
public boolean visit(final MethodDeclaration node) {
	if (!node.isConstructor()) {
		Javadoc javadoc = node.getJavadoc();
		int modifiers = node.getModifiers();
		if (Modifier.isPublic(modifiers)) {
			if (javadoc == null) {
				this.missingJavadocMethods.add(node);
			}
			this.publicMethods.add(node);
		}
		else if (Modifier.isProtected(modifiers)) {
			if (javadoc == null) {
				this.missingJavadocMethods.add(node);
			}
			this.protectedMethods.add(node);
		} else {
			if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) node.getParent();
				if (typeDeclaration.isInterface()) {
					if (javadoc == null) {
						this.missingJavadocMethods.add(node);
					}
					String typeDeclarationName = typeDeclaration.getName().toString();
					List<MethodDeclaration> methods = this.interfaceMethods.get(typeDeclarationName);
					if (methods == null) {
						this.interfaceMethods.put(typeDeclarationName, methods = new ArrayList<>());
					}
					methods.add(node);
				}
			}
		}
	}
	return false;
}

/**
 * {@inheritDoc}
 * <p>
 * Check whether the type declaration has a javadoc comment or not.
 * </p>
 */
@Override
public boolean visit(final TypeDeclaration node) {
	// Only visit main type
	if (node.getParent() == this.astRoot) {
		this.hadJavadoc = node.getJavadoc() != null;
		return true;
	}
	// Inner classes are ignored
	return false;
}

}
