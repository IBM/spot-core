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
package com.ibm.bear.qa.spot.core.utils;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.StringUtils.compare;

import java.util.*;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.utils.StringUtils.Comparison;

public class MapUtil {


/**
 * Check given comparison for given collections.
 *
 * @param first First collection to compare
 * @param second Second collection to compare
 * @throws ScenarioFailedError If comparison between collections fails
 * TODO To Be Continued
 */
public static void checkComparison(final Map<String, String> first, final Map<String, String> second, final Comparison comparison) throws ScenarioFailedError {
	debugPrintEnteringMethod();
	StringBuffer buffer = new StringBuffer();

	// Check null map
	if (first == null || second == null) {
		throw new ScenarioFailedError("Unexpected null map while checking "+comparison+" between maps.");
	}

	// Check maps size
	int firstSize = first.size();
	int secondSize = second.size();
	int n = 0;
	if (firstSize != secondSize) {
		buffer.append("	Errors while checking string maps "+comparison+":").append(LINE_SEPARATOR);
		buffer.append("	    "+ ++n + ") Maps have not the same size: first="+firstSize+", second="+secondSize).append(LINE_SEPARATOR);
	}

	// Check missing keys
	List<String> commonKeys = new ArrayList<>();
	List<String> missingKeys = new ArrayList<>();
	for (String firstKey: first.keySet()) {
		boolean found = false;
		for (String secondKey: second.keySet()) {
			if (compare(firstKey, secondKey, comparison)) {
				commonKeys.add(firstKey);
				found = true;
				break;
			}
		}
		if (!found) {
			missingKeys.add(firstKey);
		}
	}
	if (missingKeys.size() > 0) {
		if (n==0) buffer.append("	Errors while checking string maps "+comparison+":").append(LINE_SEPARATOR);
		buffer.append("	    "+ ++n + ") Following keys are only present in the first map: ");
		buffer.append(getTextFromList(missingKeys)).append(LINE_SEPARATOR);
	}
	missingKeys = new ArrayList<>();
	for (String secondKey: second.keySet()) {
		if (!commonKeys.contains(secondKey)) {
			missingKeys.add(secondKey);
		}
	}
	if (missingKeys.size() > 0) {
		if (n==0) buffer.append("	Errors while checking string maps "+comparison+":").append(LINE_SEPARATOR);
		buffer.append("	    "+ ++n + ") Following keys are only present in the second map: ");
		buffer.append(getTextFromList(missingKeys)).append(LINE_SEPARATOR);
	}

	// Check common keys
	for (String commonKey: commonKeys) {
		String firstValue = first.get(commonKey);
		String secondValue = second.get(commonKey);
		if (!compare(firstValue, secondValue, comparison)) {
			if (n==0) buffer.append("	Errors while checking string maps "+comparison+":").append(LINE_SEPARATOR);
			buffer.append("	    "+ ++n + ") Strings at key "+commonKey+" do not match: first='"+firstValue+"', second='"+secondValue+"'").append(LINE_SEPARATOR);
		}
	}
	if (n > 0) {
		println(buffer.toString());
		throw new ScenarioFailedError("Error while comparing two collections for "+comparison+". See console for more details on this error...");
	}
}

/**
 * Check whether given strings map are equals or not.
 *
 * @param first First collection to compare
 * @param second Second collection to compare
 * @throws ScenarioFailedError If collections are not equals
 */
public static void checkEquality(final Map<String, String> first, final Map<String, String> second) throws ScenarioFailedError {
	checkComparison(first, second, Comparison.Equals);
}
}
