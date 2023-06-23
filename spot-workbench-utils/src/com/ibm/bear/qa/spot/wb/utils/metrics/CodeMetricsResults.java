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

import java.io.File;

/**
 * Class for metric results.
 * <p>
 * It stores following counters:
 * <ul>
 * <li>types: total, class, interfaces, locals</li>
 * <li>constructors: total, public, protected, default, private</li>
 * <li>methods: total, public, protected, default, private, abstract</li>
 * <li>lines: total, code, javadoc, block, comment, blank</li>
 * <li>comments: total, javadoc, block, line</li>
 * </p><p>
 * It can write to stored result in a file with the following columns:
 * <pre>
 * Version, Date, Types (4), Constructors (5), Methods (6), Lines (4), Comments (4)
 * </pre
 */
public class CodeMetricsResults extends AbstractMetricsResults {
	/* Constants */
	private static final String[][] HEADERS = {
			{ "Types", "Constructors", "Methods", "Lines", "Comments" },
			{ "Total", "Classes", "Interfaces", "Locals", "Anonymous" },
			{ "Total", "Public", "Protected", "Default", "Private"},
			{ "Total", "Public", "Protected", "Default", "Private", "Abstract" },
			{ "Total", "Code", "Javadoc", "Block", "Comment", "Blank" },
			{ "Total", "Javadoc", "Block", "Line" },
	};

	/* Fields*/
	int[] types = new int[5]; 			// total, class, interfaces, locals, anonymous
	int[] constructors = new int[5];	// total, public, protected, default, private
	int[] methods = new int[6];		// total, public, protected, default, private, abstract
	int[] lines = new int[6];				// total, code, javadoc, block, comment, blank
	int[] comments = new int[4]; 	// total, javadoc, block, line

public CodeMetricsResults(final String projectName) {
	super(projectName);
}

void addCompilationUnit(final CodeMetricsResultsVisitor visitor) {

	// Add types
	for (int i=0; i<visitor.metricTypes.types.length; i++) {
		this.types[i+1] += visitor.metricTypes.types[i];
		this.types[0] += visitor.metricTypes.types[i];
	}

	// Add lines
	for (int i=0; i<visitor.metricTypes.lines.length; i++) {
		if (i == 0) {
			this.lines[1] += visitor.metricTypes.lines[i]; // include comments as well...
		} else {
			this.lines[5] += visitor.metricTypes.lines[i];
		}
		this.lines[0] += visitor.metricTypes.lines[i];
	}

	// Add constructors
	for (int i=0; i<visitor.metricMethods.constructors.length; i++) {
		this.constructors[i+1] += visitor.metricMethods.constructors[i];
		this.constructors[0] += visitor.metricMethods.constructors[i];
	}

	// Add methods
	for (int i=0; i<visitor.metricMethods.methods.length; i++) {
		this.methods[i+1] += visitor.metricMethods.methods[i];
		this.methods[0] += visitor.metricMethods.methods[i];
	}

	// Add comments
	for (int i=0; i<visitor.metricComments.comments.length; i++) {
		this.comments[i+1] += visitor.metricComments.comments[i];
		this.comments[0] += visitor.metricComments.comments[i];
	}

	// Add comment lines
	for (int i=0; i<visitor.metricComments.lines.length; i++) {
		if (i<(visitor.metricComments.lines.length-1)) {
			this.lines[2+i] += visitor.metricComments.lines[i];
			this.lines[1] -= visitor.metricComments.lines[i]; // remove comment lines from code lines
		} else {
			this.lines[5] += visitor.metricComments.lines[i];
		}
//		this.lines[0] += visitor.metricComments.lines[i];
	}
}

@Override
public File writeResult(final File dir, final String versionName) {
	if (this.types[0] == 0) {
		return null;
	}

	StringBuilder newLine = new StringBuilder(this.project)
		.append("\t")
		.append(versionName)
		.append("\t")
		.append(CURRENT_DATE);

	// Add types
	int typesLength = this.types.length;
	for (int i=0; i<typesLength; i++) {
		newLine.append('\t');
		newLine.append(this.types[i]);
	}

	// Add Constructors
	for (int i=0; i<this.constructors.length; i++) {
		newLine.append('\t');
		newLine.append(this.constructors[i]);
	}

	// Add Methods
	for (int i=0; i<this.methods.length; i++) {
		newLine.append('\t');
		newLine.append(this.methods[i]);
	}

	// Add lines
	for (int i=0; i<this.lines.length; i++) {
		newLine.append('\t');
		newLine.append(this.lines[i]);
	}

	// Add comments
	for (int i=0; i<this.comments.length; i++) {
		newLine.append('\t');
		newLine.append(this.comments[i]);
	}

	// Write new file content
	File file = getFile(dir, "code");
	write(file, versionName, newLine.toString());
	return file;
}

@Override
protected String[][] getHeaders() {
	return HEADERS;
}
}
