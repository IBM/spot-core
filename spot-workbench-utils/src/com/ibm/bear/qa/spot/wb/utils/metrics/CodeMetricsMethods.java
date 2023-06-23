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
package com.ibm.bear.qa.spot.wb.utils.metrics;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Class for code methods metric.
 * <p>
 * It stores four counters for constructors: one for each visibility. It also stores
 * five counters for methods: one for each visibility + one for abstract methods.
 * </p>
 */
class CodeMetricsMethods extends CodeMetrics {

	int[] constructors= new int[4];
	int[] methods = new int[5];

void add(final MethodDeclaration node) {
	int modifiers = node.getModifiers();
	if (node.isConstructor()) {
		if (Modifier.isPublic(modifiers)) {
			this.constructors[0]++;
		} else if (Modifier.isProtected(modifiers)) {
			this.constructors[1]++;
		} else if (Modifier.isPrivate(modifiers)) {
			this.constructors[3]++;
		} else {
			this.constructors[2]++;
		}
	} else {
		if (Modifier.isPublic(modifiers)) {
			this.methods[0]++;
		} else if (Modifier.isProtected(modifiers)) {
			this.methods[1]++;
		} else if (Modifier.isPrivate(modifiers)) {
			this.methods[3]++;
		} else {
			this.methods[2]++;
		}
		if (Modifier.isAbstract(modifiers) && node.getBody() == null) {
			this.methods[4]++;
		}
	}
}

@Override
boolean isMethods() {
	return true;
}

}
