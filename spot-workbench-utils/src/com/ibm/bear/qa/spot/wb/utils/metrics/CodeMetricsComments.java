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

/**
 * Class for code comments metric.
 * <p>
 * It stores three counters for comment kinds: Javadoc, Block and Line and four
 * counters for comment lines: Javadoc, Block, Line and Blank.
 * </p>
 */
class CodeMetricsComments extends CodeMetrics {

	int[] comments = new int[3];
	int[] lines = new int[4]; // javadoc, block, line, blank

void addBlock(final String source) {

	// Count blank lines, but skip if its the IBM copyright comment
	int[] blankLines = countBlankLines(source, true);
	if (blankLines == null) {
		return;
	}

	// Count comments
	this.comments[1]++;

	// Count lines
	this.lines[1] += blankLines[0];
	this.lines[3] += blankLines[1];
}

void addJavadoc(final String source) {

	// Count lines
	int[] blankLines = countBlankLines(source, true);
	if (blankLines == null) {
		// Javadoc to skip (e.g. Copyrights one)
		return;
	}
	this.lines[0] += blankLines[0];
	this.lines[3] += blankLines[1];

	// Count comments
	this.comments[0]++;
}

void addLine(final boolean wholeLine) {
	this.comments[2]++;
	if (wholeLine) this.lines[2]++;
}

@Override
boolean isComments() {
	return true;
}

}
