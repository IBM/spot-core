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

import java.util.StringTokenizer;

/**
 * Abstract class for code metric.
 */
class CodeMetrics {

int[] countBlankLines(final String source, final boolean comment) {
	StringTokenizer tokenizer = new StringTokenizer(source, "\r\n");
	int[] lines = new int[2]; // normal, empty
	while (tokenizer.hasMoreTokens()) {
		String line = tokenizer.nextToken().trim();
		if (line.toLowerCase().contains("copyright")) {
			return null;
		}
		if (line.isEmpty() || (comment && line.equals("*"))) {
			lines[1]++; // blank lines
		} else {
			lines[0]++; // non-blank line
		}
	}
	return lines;
}

boolean isComments() {
	return false;
}

boolean isMethods() {
	return false;
}

boolean isTypes() {
	return false;
}
}
