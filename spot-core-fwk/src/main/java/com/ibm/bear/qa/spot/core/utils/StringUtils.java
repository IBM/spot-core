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
package com.ibm.bear.qa.spot.core.utils;

/**
 * Class to provide utilities around {@link String}.
 * <p>
 * Availables methods of this utility class are:
 * <ul>
 * <li>{@link #cleanWhiteCharacters(String)}: Clean the given string from any white spaces characters.</li>
 * <li>{@link #convertLineDelimitersToUnix(String)}: Convert line delimiters of the given string to Unix instead of Windows.</li>
 * <li>{@link #equalsNonWhitespaces(String, String)}: Compare the two given strings ignoring all white spaces.</li>
 * <li>{@link #getSafeStringForPath(String)}: Return a string from the given one which will be safe to be used in file path.</li>
 * <li>{@link #removeWhiteCharacters(String)}: Remove all white spaces from the given string.</li>
 * </ul>
 * </p>
 */
public class StringUtils {

/**
 * Clean the given string from any white spaces characters.
 *
 * @param str The string to be cleaned
 * @return The cleaned string
 */
public static String cleanWhiteCharacters(final String str) {
	char[] chars = str.toCharArray();
	StringBuffer buffer = new StringBuffer();
	StringBuffer blanks = new StringBuffer();
	for (char ch: chars) {
		if (Character.isWhitespace(ch)) {
			if (ch == ' ' || ch == '\t') {
				blanks.append(ch);
			} else if (ch == '\r' || ch == '\n') {
				blanks = new StringBuffer();
				buffer.append(ch);
			} else {
				blanks.append(' ');
			}
		} else if (ch == '\u200c') {
			// skip
		} else {
			buffer.append(blanks);
			buffer.append(ch);
			blanks = new StringBuffer();
		}
	}
	return buffer.toString();
}

/**
 * Convert line delimiters of the given string to Unix instead of Windows.
 * <p>
 * Note that the will be a no-op if the string has no line delimiters or none
 * of them are Windows delimiters.
 * </p>
 * @param str The string to be converted
 * @return The converted string
 */
public static String convertLineDelimitersToUnix(final String str) {
	return str.replaceAll("\r\n", "\n");
}

/**
 * Compare the two given strings ignoring all white spaces.
 *
 * @param str1 The first string to be compared
 * @param str2 The second string to be compared
 * @return <code>true</code> if strings have all non-whitespaces characters equals,
 * <code>false</code> otherwise
 */
public static boolean equalsNonWhitespaces(final String str1, final String str2) {
	String nwss1 = removeWhiteCharacters(str1);
	String nwss2 = removeWhiteCharacters(str2);
	return nwss1.equals(nwss2);
}

/**
 * Return a string from the given one which will be safe to be used in file path.
 * <p>
 * Currently this is a simple algorithm used in this method. It just replaces
 * white spaces with an underscore ('-').
 * </p>
 * @param str The string to be used for path
 * @return A usable string for file path
 */
public static String getSafeStringForPath(final String str) {
	return str.replaceAll(" ", "_");
}

/**
 * Remove all white spaces from the given string.
 *
 * @param str The string to be transformed
 * @return The transformed string
 */
public static String removeWhiteCharacters(final String str) {
	char[] chars = str.toCharArray();
	StringBuffer buffer = new StringBuffer();
	for (char ch: chars) {
		if (!Character.isWhitespace(ch) && ch != '\u200c') {
			buffer.append(ch);
		}
	}
	buffer.trimToSize();
	return buffer.toString();
}

private StringUtils() {
	// Non instanciable class
}
}
