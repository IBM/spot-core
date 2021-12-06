/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
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

import java.util.*;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioImplementationError;

/**
 * Class to provide utilities around {@link Collection} and array that neither
 * {@link Collections} nor {@link Arrays} currently offers.
 * <p>
 * Availables methods of this utility class are:
 * <ul>
 * <li>{@link #addArrayToList(List, Object[])}: Add given array items to the given list.</li>
 * <li>{@link #checkEquality(List, List)}: Check whether given collections are equals or not.</li>
 * <li>{@link #getListFromArray(Object[])}: Return the given array as a list of same objects.</li>
 * </ul>
 * </p>
 */
public class CollectionsUtil {

/**
 * Add given array items to the given list.
 *
 * @param list The list to increase with array items
 * @param array The array to build as a list
 * @return The list as as {@link List}
 */
public static <T> List<T> addArrayToList(final List<T> list, final T[] array) {
	for (T item: array) {
		list.add(item);
	}
	return list;
}

/**
 * Check whether given collections are equals or not.
 *
 * @param first First collection to compare
 * @param second Second collection to compare
 */
public static void checkEquality(final List<String> first, final List<String> second) {
	debugPrintEnteringMethod();
	StringBuffer buffer = new StringBuffer();
	if (first == null || second == null) {
		throw new ScenarioFailedError("Unexpected null lst while checking equality between collections.");
	}
	int firstSize = first.size();
	int secondSize = second.size();
	int n = 0;
	if (firstSize != secondSize) {
		buffer.append("	Errors while checking lists equality:").append(LINE_SEPARATOR);
		buffer.append("	    "+ ++n + ") Collections have not the same size: first="+firstSize+", second="+secondSize).append(LINE_SEPARATOR);
	}
	int size = Math.min(firstSize, secondSize);
	for (int i=0; i<size; i++) {
		if (!first.get(i).equals(second.get(i))) {
			if (n==0) buffer.append("	Errors while checking lists equality:").append(LINE_SEPARATOR);
			buffer.append("	    "+ ++n + ") Strings at index "+i+" do not match: first='"+first.get(i)+"', second='"+second.get(i)+"'").append(LINE_SEPARATOR);
		}
	}
	if (firstSize > secondSize) {
		if (n==0) buffer.append("	Errors while checking lists equality:").append(LINE_SEPARATOR);
		buffer.append("	    "+ ++n + ") Following strings are only present in the first collection:").append(LINE_SEPARATOR);
		for (int i=secondSize; i<firstSize; i++) {
			buffer.append("		    - index "+i+": '"+first.get(i)+"'").append(LINE_SEPARATOR);
		}
	}
	else if (firstSize < secondSize) {
		if (n==0) buffer.append("	Errors while checking lists equality:").append(LINE_SEPARATOR);
		buffer.append("	    "+ ++n + ") Following strings are only present in the second collection:").append(LINE_SEPARATOR);
		for (int i=firstSize; i<secondSize; i++) {
			buffer.append("		    - index "+i+": '"+first.get(i)+"'").append(LINE_SEPARATOR);
		}
	}
	if (n > 0) {
		println(buffer.toString());
		throw new ScenarioFailedError("Error while comparing two collections which were expected to be equals. See console for more details on this error...");
	}
}

/**
 * Return the given array as a list of same objects.
 *
 * @param list The list to build as an array
 * @return The built array
 */
public static String[] getStringArrayFromList(final List<String> list) {
	String[] array = new String[list.size()];
	return list.toArray(array);
}

/**
 * Return the given array as a list of same objects.
 *
 * @param array The array to build as a list
 * @return The list as as {@link List}
 */
public static <T> List<T> getListFromArray(final T[] array) {
	List<T> list = new ArrayList<T>();
	for (T item: array) {
		list.add(item);
	}
	return list;
}

/**
 * Check whether given collections are equals or not.
 * @param firstTitle Title for the first list
 * @param first First collection to compare
 * @param secondTitle Title of the second list
 * @param second Second collection to compare
 *
 * @return a message displaying the lists if they are not equal, <code>null</code> otherwise.
 */
public static String sortListAndCheckEquality(final String firstTitle, final List<String> first, final String secondTitle, final List<String> second) {
	String title1 = firstTitle==null ? "first" : firstTitle;
	String title2 = secondTitle==null ? "second" : secondTitle;
	debugPrintEnteringMethod();
	debugPrintln(" - '"+title1+"': "+getTextFromList(first));
	debugPrintln(" - '"+title2+"': "+getTextFromList(second));
	int sizeFirst = first.size();
	int sizeSecond = second.size();
	StringBuilder message = null;
	List<String> sortedFirst = new ArrayList<String>(first);
	List<String> sortedSecond = new ArrayList<String>(second);
	Collections.sort(sortedFirst);
	Collections.sort(sortedSecond);
	List<String> onlyInFirst = new ArrayList<>();
	List<String> onlyInSecond = new ArrayList<>();
	String messageTitle = EMPTY_STRING;
	if (sizeFirst != sizeSecond) {
		messageTitle = " - Lists have not the same size: '"+title1+"'="+sizeFirst+", '"+title2+"'="+sizeSecond+LINE_SEPARATOR;
	}
	List<String> remainingFirst = new ArrayList<>(sortedFirst);
	for (int i=0; i<sizeFirst; i++) {
		final String item = sortedFirst.get(i);
		if (!sortedSecond.contains(item)) {
			onlyInFirst.add(item);
		}
	}
	if (!onlyInFirst.isEmpty()) {
		messageTitle += " - Some items are only in '"+title1+"' list" + LINE_SEPARATOR;
		remainingFirst.removeAll(onlyInFirst);
	}
	for (int i=0; i<sizeSecond; i++) {
		final String item = sortedSecond.get(i);
		if (!sortedFirst.contains(item)) {
			onlyInSecond.add(item);
		}
	}
	List<String> remainingSecond = new ArrayList<>(sortedSecond);
	if (!onlyInSecond.isEmpty()) {
		messageTitle += " - Some items are only in '"+title2+"' list" + LINE_SEPARATOR;
		remainingSecond.removeAll(onlyInSecond);
	}
	if (messageTitle.length() > 0) {
		message = new StringBuilder("Sorted lists comparison failed for following reasons:").append(LINE_SEPARATOR)
			.append(messageTitle)
			.append("Lists comparison details:").append(LINE_SEPARATOR)
			.append(" - initial '"+title1+"' list: "+getTextFromList(first)).append(LINE_SEPARATOR)
			.append(" - initial '"+title2+"' list: "+getTextFromList(second)).append(LINE_SEPARATOR)
			.append(" - sorted '"+title1+"' list: "+(first.equals(sortedFirst)?"already sorted":getTextFromList(sortedFirst))).append(LINE_SEPARATOR)
			.append(" - sorted '"+title2+"' list: "+(second.equals(sortedSecond)?"already sorted":getTextFromList(sortedSecond))).append(LINE_SEPARATOR);
		if (!onlyInFirst.isEmpty()) {
			message.append(" - only in '"+title1+"' list: "+getTextFromList(onlyInFirst)).append(LINE_SEPARATOR);
		}
		if (!onlyInSecond.isEmpty()) {
			message.append(" - only in '"+title2+"' list: "+getTextFromList(onlyInSecond)).append(LINE_SEPARATOR);
		}
		if (remainingFirst.equals(remainingSecond)) {
			message.append(" - remaining list: "+getTextFromList(remainingFirst)).append(LINE_SEPARATOR);
		} else {
			println("Unexpected remaining list!");
			message.append(" - remaining '"+title1+"' list: "+getTextFromList(remainingFirst)).append(LINE_SEPARATOR)
				.append(" - remaining '"+title2+"' list: "+getTextFromList(remainingSecond)).append(LINE_SEPARATOR);
			print(message.toString());
			throw new ScenarioImplementationError("Lists should be equals after having removed only items in both lists.");
		}
	}
	if (message != null) {
		return message.toString();
	}
	return null;
}

}
