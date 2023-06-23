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

import static com.ibm.bear.qa.spot.wb.utils.FileUtil.readFileContent;
import static com.ibm.bear.qa.spot.wb.utils.FileUtil.writeFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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
public abstract class AbstractMetricsResults {
	/* Constants */
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy");
	protected static final String CURRENT_DATE = DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/* Fields*/
	protected String project;

public AbstractMetricsResults(final String projectName) {
	this.project = projectName;
}

private void appendHeaders(final StringBuilder builder) {

	String[][] headers = getHeaders();
	String[] firstHeaderLine = headers[0];
	int[] lengthes = new int[headers.length-1];
	for (int i=0; i<lengthes.length; i++) {
		lengthes[i] = headers[i+1].length;
	}
	builder.append("Project\tVersion\tDate\t");
	for (int i=0; i<firstHeaderLine.length; i++) {
		builder.append(firstHeaderLine[i]);
		if (lengthes.length == 0) {
			builder.append('\t');
		} else {
			for (int j=0; j<lengthes[i]; j++) {
				builder.append('\t');
			}
		}
	}
	builder.append(LINE_SEPARATOR);
	if (headers.length > 1) {
		builder.append("\t\t\t");
		for (int i=1; i<headers.length; i++) {
			for (String header: headers[i]) {
				builder.append(header);
				builder.append('\t');
			}
		}
		builder.append(LINE_SEPARATOR);
	}
}

abstract protected String[][] getHeaders();

protected File getFile(final File dir, final String type) {
	File fileDir1 = new File(dir, type);
	if (!fileDir1.exists()) {
		fileDir1.mkdir();
	}
	File fileDir = fileDir1;
	File file;
	if (this.project.startsWith("spot-core") || this.project.startsWith("spot-samples")) {
		System.out.println("Detected a SPOT core project '"+this.project+"', bundle generated metrics results in the single spot-core.txt file...");
		file = new File(fileDir, "spot-core.txt");
	} else if (this.project.endsWith("-pages")) {
		System.out.println("Detected a SPOT pages project '"+this.project+"', bundle generated metrics results in the single spot-pages.txt file...");
		file = new File(fileDir, "spot-pages.txt");
	} else if (this.project.endsWith("-scenarios")) {
		System.out.println("Detected a SPOT scenarios project '"+this.project+"', bundle generated metrics results in the single spot-scenarios.txt file...");
		file = new File(fileDir, "spot-scenarios.txt");
	} else {
		file = new File(fileDir, this.project+".txt");
	}
	System.out.println("Metric results for project '" + this.project+"' will be written in "+file.getAbsolutePath());
	return file;
}

protected void write(final File file, final String versionName, final String newLine) {
	StringBuilder builder = new StringBuilder();
	if (file.exists()) {
		try {
			String[] fileLines = readFileContent(file).split(System.getProperty("line.separator"));
			Arrays.sort(fileLines, 2, fileLines.length);
			boolean lineAdded = false;
			String prefix = this.project+"\t"+versionName;
			for (String line: fileLines) {
				if (line.startsWith(prefix)) {
					builder.append(newLine).append(LINE_SEPARATOR);
					lineAdded = true;
				} else {
					if (!lineAdded && line.compareTo(prefix) > 0) {
						builder.append(newLine).append(LINE_SEPARATOR);
						lineAdded = true;
					}
					builder.append(line).append(LINE_SEPARATOR);
				}
			}
			if (!lineAdded) {
				builder.append(newLine).append(LINE_SEPARATOR);
			}
		} catch (IOException e) {
			System.err.println("Cannot read content of file '"+file.getAbsolutePath()+"' although it exists!");
			e.printStackTrace();
		}
	} else {
		appendHeaders(builder);
		builder.append(newLine);
	}
	try {
		writeFile(file, builder.toString());
	} catch (IOException e) {
		System.err.println("Cannot write content of file "+file.getAbsolutePath()+"!");
		e.printStackTrace();
	}
}

abstract public File writeResult(final File dir, final String versionName);
}
