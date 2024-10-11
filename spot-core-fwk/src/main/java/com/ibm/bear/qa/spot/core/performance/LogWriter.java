/*********************************************************************
* Copyright (c) 2012, 2024 IBM Corporation and others.
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

import java.io.FileWriter;
import java.io.IOException;

/**
 * Log Writer class, for writing performance debug messages to disk
 * <p>
 * The Log writer class is designed to be used by a PerfManager to write debug messages out to disk
 * <ul>
 * <li>{@link #close()}: Close the file.</li>
 * <li>{@link #open}: Open the file.</li>
 * <li>{@link #writeNext(String)}: Write next string to file.</li>
 * </ul>
 * </p>
 */

public class LogWriter {

//Global Variables
FileWriter writer;
final String filePathName;

public LogWriter (final String filePathName){
	this.filePathName = filePathName;
	try {
		this.writer = new FileWriter(filePathName,true);
	}
	catch (IOException e) {
		System.out.println("Performance file location not found, please create folders appropropriately.");
		e.printStackTrace();
	}
}

/**
 * Close the file
 */
public void close() {
	try {
	    this.writer.close();
    } catch (IOException e) {
    	System.out.println("Error occured while closing csv file.");
	    e.printStackTrace();
    }
}

/**
 * Flush the file
 */
public void flush() {
	try {
	    this.writer.flush();
    } catch (IOException e) {
    	System.out.println("Error occured while closing csv file.");
	    e.printStackTrace();
    }
}

/**
 * Open the file.
 */
public void open() {
	try {
		this.writer = new FileWriter(this.filePathName,true);
	} catch (IOException e) {
		System.out.println("Error occured while openning csv file.");
		e.printStackTrace();
	}
}

/**
 * Write next string array to file.
 *
 * @param content The string to write on the line
 */
public void writeNext(final String content) {
	try {
		this.writer.append(content);
		this.writer.append(LINE_SEPARATOR);
		this.writer.flush();
	} catch (IOException e) {
		System.out.println("Error occured while writing to log file.");
		e.printStackTrace();
	}
}

}
