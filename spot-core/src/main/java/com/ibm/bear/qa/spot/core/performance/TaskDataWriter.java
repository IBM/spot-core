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

import java.text.DecimalFormat;
import java.util.Calendar;


/**
 * Task Data Writer class, for writing task data results to disk
 * <p>
 * The task data writer class class is designed to provide a simple way to write task data files and provide
 * input methods for results.
 * <ul>
 * <li>{@link #write(PerfResult)}: Write PerfResult to file.</li>
 * </ul>
 * </p>
 */

public class TaskDataWriter extends CsvWriter {

public TaskDataWriter (final String filePathName){
	super(filePathName, ',');
	this.writeNext(new String[]{
			"Step Name", 
			"Test Name", 
			"User Action Name", 
			"URL", 
			"Page Title", 
			"Measurement Type", 
			"Response Time", 
			"DateStamp" });
}


/**
 * Write the given result out to the cvsFile
 *
 */
public void write(PerfResult result){
	this.writeNext(new String[]{
			result.getStepName(),
			result.getTestName(),
			result.getUserActionName(),
			result.getUrl(),
			result.getPageTitle(), 
			PerfResult.regressionTypeToString(result.getRegressionType()),
			Double.toString(Timer.round(result.getLastRegressionTime().doubleValue(),5)),
			result.getLastTimeDateStamp()});
}

/**
 * Return string from time in format dd/MON/yyyy hh:mm:ss.mmm (Excel friendly)
 * NOTE: Method copied from RPP framework from StringUtils.  Updated to clear
 * all boxing errors and unused month variables.
 * 
 * @param time
 * @return formatted string
 */
public static String timestamp2(long time, boolean showSecsMillis) {
	DecimalFormat fmt2 = new DecimalFormat("##");
	fmt2.setMinimumIntegerDigits(2);

	Calendar cs = Calendar.getInstance();
	cs.setTimeInMillis(time);
	
	Integer yy = new Integer(cs.get(Calendar.YEAR));
	Integer mon = new Integer(cs.get(Calendar.MONTH) + 1);
	Integer dd = new Integer(cs.get(Calendar.DAY_OF_MONTH));
	Integer hh = new Integer(cs.get(Calendar.HOUR_OF_DAY));
	Integer mins = new Integer(cs.get(Calendar.MINUTE));
	String name = 
		mon + "/" + fmt2.format(dd) + "/" + fmt2.format(yy) +
		" " + fmt2.format(hh) + ":" + fmt2.format(mins);
	if (showSecsMillis) {
		DecimalFormat fmt3 = new DecimalFormat("###");
		fmt3.setMinimumIntegerDigits(3);
		Integer secs = new Integer(cs.get(Calendar.SECOND));
		Integer msecs = new Integer(cs.get(Calendar.MILLISECOND));
		name += ":" + fmt2.format(secs) + "." + fmt3.format(msecs);
	}
	return name;
}

}
