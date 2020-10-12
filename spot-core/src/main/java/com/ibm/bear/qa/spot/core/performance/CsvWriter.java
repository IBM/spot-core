/*********************************************************************
* Copyright (c) 2012, 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.performance;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.LINE_SEPARATOR;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.QUOTE;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Csv Writer class, for writing csv results to disk
 * <p>
 * The Csv writer class is designed to provide a simple way to write Csv files and provide
 * input methods for common array types.
 * <ul>
 * <li>{@link #writeNext(ArrayList)}: Write next Array List of strings to file.</li>
 * <li>{@link #writeNext(String[])}: Write next string array to file.</li>
 * </ul>
 * </p>
 */

public class CsvWriter extends LogWriter {

//Global Variables
final char breakCharacter;

public CsvWriter (final String filePathName, final char breakCharacter){
	super(filePathName);
	this.breakCharacter = breakCharacter;
}

/**
 * Write next string array to file.
 *
 * @param csvArray The array of string to write on the line
 */
public void writeNext(final String[] csvArray) {
	try {
		for (int i = 0; i < csvArray.length; i++) {
			if (i > 0) this.writer.append(this.breakCharacter);
			this.writer.append(QUOTE).append(csvArray[i]).append(QUOTE);
		}
		this.writer.append(LINE_SEPARATOR);
		this.writer.flush();
	} catch (IOException e) {
		System.out.println("Error occured while writing to csv file.");
		e.printStackTrace();
	}
}

/**
 * Write next string array to file.
 *
 * @param csvArray The array of string to write on the line
 */
public void writeNext(final ArrayList<String> csvArray) {
	try {
		for (int i = 0; i < csvArray.size(); i++) {
			if (i > 0) this.writer.append(this.breakCharacter);
			this.writer.append(QUOTE).append(csvArray.get(i)).append(QUOTE);
		}
		this.writer.append(LINE_SEPARATOR);
		this.writer.flush();
	} catch (IOException e) {
		System.out.println("Error occured while writing to csv file.");
		e.printStackTrace();
	}
}
}
