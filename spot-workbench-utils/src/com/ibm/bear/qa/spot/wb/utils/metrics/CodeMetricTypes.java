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

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Class for code types metric.
 * <p>
 * It stores three counters for types: Classes, Interfaces and Locals. It also
 * stores two counters for lines: Code and Blank.
 * </p>
 */
class CodeMetricTypes extends CodeMetrics {

	int[] types = new int[4];
	int[] lines = new int[2];

@SuppressWarnings("unused")
void add(final AnonymousClassDeclaration node) {
	this.types[3]++;
}

void add(final TypeDeclaration node, final String source) {
	if (node.isLocalTypeDeclaration()) {
		this.types[2]++;
	} else {
		if (node.isInterface()) {
			this.types[1]++;
		} else {
			this.types[0]++;
		}
		this.lines = countBlankLines(source, false);
	}
}

@Override
boolean isTypes() {
	return true;
}

}
