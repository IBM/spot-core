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
package com.ibm.bear.qa.spot.wb.utils;

import java.text.NumberFormat;

/**
 * Class for utilities used for SPOT Utils plugin.
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #timeString(long)}: Returns a formatted string to display the given time as a duration.</li>
 * </ul>
 * </p>
 */
public class Utils {

	/* Time and date */
	public static final int ONE_MINUTE = 60000;
	public static final long ONE_HOUR = 3600000L;

/**
 * Returns a formatted string to display the given time as a duration.
 * <p>
 * Here's the returned string format:
 *	<ul>
 *	<li>"XXXms" if the duration is less than 0.1s (e.g. "43ms")</li>
 *	<li>"X.YYs" if the duration is less than 1s (e.g. "0.43s")</li>
 *	<li>"XX.Ys" if the duration is less than 1mn (e.g. "14.3s")</li>
 *	<li>"XXmn XXs" if the duration is less than 1h (e.g. "14mn 3s")</li>
 *	<li>"XXh XXmn XXs" if the duration is over than 1h (e.g. "1h 4mn 3s")</li>
 *	</ul>
 * </p><p>
 * <b>Warning</b>: This method has been stolen from <b>ScenarioUtils</b>, hence
 * any change done in this method should be reported to that class. If that's not
 * possible then a new method needs to be created.
 * </p>
 * @param time The time to format as a long.
 * @return The time as a human readable readable {@link String}.
 */
public static String timeString(final long time) {
	NumberFormat format = NumberFormat.getInstance();
	format.setMaximumFractionDigits(1);
	StringBuffer buffer = new StringBuffer();
	if (time == 0) {
		// print nothing
	} if (time < 100) { // less than 0.1s
		buffer.append(time);
		buffer.append("ms"); //$NON-NLS-1$
	} else if (time < 1000) { // less than 1s
		if ((time%100) != 0) {
			format.setMaximumFractionDigits(2);
		}
		buffer.append(format.format(time/1000.0));
		buffer.append("s"); //$NON-NLS-1$
	} else if (time < ONE_MINUTE) {  // less than 1mn
		if ((time%1000) == 0) {
			buffer.append(time/1000);
		} else {
			buffer.append(format.format(time/1000.0));
		}
		buffer.append("s"); //$NON-NLS-1$
	} else if (time < ONE_HOUR) {  // less than 1h
		buffer.append(time/ONE_MINUTE).append("mn "); //$NON-NLS-1$
		long seconds = time%ONE_MINUTE;
		buffer.append(seconds/1000);
		buffer.append("s"); //$NON-NLS-1$
	} else {  // more than 1h
		long h = time / ONE_HOUR;
		buffer.append(h).append("h "); //$NON-NLS-1$
		long m = (time % ONE_HOUR) / ONE_MINUTE;
		buffer.append(m).append("mn "); //$NON-NLS-1$
		long seconds = m%ONE_MINUTE;
		buffer.append(seconds/1000);
		buffer.append("s"); //$NON-NLS-1$
	}
	return buffer.toString();
}
}
