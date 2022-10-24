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

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioImplementationError;

/**
 * Class to provide utilities around {@link String} .
 * <p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #cleanWhiteCharacters(String)}: Clean the given string from any white spaces characters.</li>
 * <li>{@link #compare(String,String,Comparison)}: Compare two given strings using the given comparison method.</li>
 * <li>{@link #convertLineDelimitersToUnix(String)}: Convert line delimiters of the given string to Unix instead of Windows.</li>
 * <li>{@link #equalsNonWhitespaces(String,String)}: Compare the two given strings ignoring all white spaces.</li>
 * <li>{@link #getSafeStringForPath(String)}: Return a string from the given one which will be safe to be used in file path.</li>
 * <li>{@link #hidePassword(String)}: Return the given password hidden.</li>
 * <li>{@link #hidePasswordInLocation(String)}: Return the location with hidden password.</li>
 * <li>{@link #incrementVersion(String,int)}: Increment the given version at the given index.</li>
 * <li>{@link #removeWhiteCharacters(String)}: Remove all white spaces from the given string.</li>
 * <li>{@link #removeWhiteCharacters(String,boolean)}: Remove all white spaces from the given string.</li>
 * <li>{@link #sortVersions(List)}: Sort a list of string assuming they all match standard versions pattern.</li>
 * </ul>
 * </p>
 */
public class StringUtils {
	/**
	 * Enumeration of all supported text comparison for the current timeout
	 */
	public enum Comparison {
		/** Check that the element text is equals to the expected one. */
		Equals("equals"),
		/** Check that the element text starts with the expected one. */
		StartsWith("starts with"),
		/** Check that the element text is the start of the expected one. */
		IsStartOf("is start of"),
		/** Check that the element text ends with the expected one. */
		EndsWith("ends with"),
		/** Check that the element text end is the end of the expected one. */
		IsEndOf("is end of"),
		/** Check that the element text contains the expected one. */
		Contains("contains"),
		/** Check that the element text matches the expected regular expression. */
		Regex("does not match regular expression"),
		/** Check that the element text json matches the expected text json. */
		Json_Equals("does not equal json");
		final private String label;
		Comparison(final String text) {
			this.label = text;
		}
		@Override
		public String toString() {
			return this.label;
		}
	}

	/**
 * Private class to handle digits in a {@link DigitalizedVersion digitalized version }.
 */
	private static class VersionDigit implements Comparable<VersionDigit> {
		String digit;
		int value;
		VersionDigit(final String text) throws NumberFormatException {
			this.digit = text;
			try {
				this.value = Integer.parseInt(text);
			}
			catch( @SuppressWarnings("unused") NumberFormatException nfe) {
				// Do not store any value when digit is not a number
				this.value = -1;
			}
		}
		@Override
		public int compareTo(final VersionDigit vd) {
			if (this.value == -1 || vd.value == -1) {
				return this.digit.compareTo(vd.digit);
			}
			return this.value - vd.value;
		}
		@Override
		public boolean equals(final Object o) {
			if (o instanceof VersionDigit) {
				return compareTo((VersionDigit) o) == 0;
			}
			return super.equals(o);
		}
		@Override
		public int hashCode() {
			return this.digit.hashCode();
		}
		@Override
		public String toString() {
			return this.digit;
		}

	}

	/**
 * Private class to digitalize string supposed to represent version.
 */
	private static class DigitalizedVersion implements Comparable<DigitalizedVersion>{
		VersionDigit[] digits;

		DigitalizedVersion(final String version) {
			super();
			digitalize(version);
		}

		/**
		 * Digitalize the given version assuming it matches usual <code>digit(.digit)*</code> pattern.
		 *
		 * @param version The version text
		 */
		private void digitalize(final String version) {
			StringTokenizer tk = new StringTokenizer(version, ".");
			this.digits = new VersionDigit[tk.countTokens()];
			int count = 0;
			while (tk.hasMoreTokens()) {
				this.digits[count++] = new VersionDigit(tk.nextToken());
			}
		}

		@Override
		public int compareTo(final DigitalizedVersion dv) {
			int minLength = Integer.min(this.digits.length, dv.digits.length);
			for (int i=0; i<minLength; i++) {
				int digitComparison = this.digits[i].compareTo(dv.digits[i]);
				if (digitComparison != 0) {
					return digitComparison;
				}
			}
			return 0;
		}
		@Override
		public boolean equals(final Object o) {
			if (o instanceof DigitalizedVersion) {
				return compareTo((DigitalizedVersion)o) == 0;
			}
			return super.equals(o);
		}
		@Override
		public int hashCode() {
			int hc = 0;
			for (int i=0; i<this.digits.length; i++) {
				hc += this.digits[i].hashCode() * 10^i;
			}
			return hc;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			String separator = EMPTY_STRING;
			for (VersionDigit digit: this.digits) {
				builder.append(separator).append(digit.digit);
				separator = ".";
			}
			return builder.toString();
		}
	}

private StringUtils() {
	// Non instanciable class
}

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
 * Compare two given strings using the given comparison method.
 *
 * @param first The first string to compare
 * @param second The second string to compare with
 * @param comparison The comparison method (see {@link Comparison})
 * @return <code>true</code> if the two strings matches according to the used comparison,
 * <code>false</code> otherwise
 */
public static boolean compare(final String first, final String second, final Comparison comparison) {
	switch (comparison) {
		case Equals:
			return first.equals(second);
		case StartsWith:
			return first.startsWith(second);
		case IsStartOf:
			return second.startsWith(first);
		case EndsWith:
			return first.endsWith(second);
		case IsEndOf:
			return second.endsWith(first);
		case Contains:
			return first.contains(second);
		case Regex:
			return Pattern.compile(second).matcher(first).matches();
		case Json_Equals:
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.readTree(first).equals(mapper.readTree(second));
			} catch (IOException e) {
				throw new ScenarioFailedError(e);
			}
		default:
			throw new ScenarioImplementationError("It well never go there, it's just to fix a compiler bug...");
	}
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
 * Return the given password hidden.
 *
 * @param password The password to be hidden
 * @return The hidden password.
 */
public static String hidePassword(final String password) {
	return (password == null ? "null" : password.charAt(0) + "******");
}

/**
 * Return the location with hidden password.
 *
 * @param location The location
 * @return The location without password in clear.
 */
public static String hidePasswordInLocation(final String location) {
	String newlocation = location;
	if (location != null) {
		int passwordEndIndex = location.indexOf('@');
		if (passwordEndIndex > 0) {
			int passwordStartIndex = location.indexOf(':', 6)+1;
			newlocation = location.substring(0, passwordStartIndex+1)  + "******" + location.substring(passwordEndIndex, location.length());
		}
	}
	return newlocation;
}

/**
 * Increment the given version at the given index.
 * <p>
 * The version expected format is <code>number(*.number)</code>
 * (e.g. <code>4.0.1</code> with index 0, 1 or 2)
 * </p>
 * @param version The version to be incremented (using format <code>number(*.number)</code>)
 * @param index The position of the number to increment. If negative, then last number is incremented.
 * @return The incremented version
 */
public static String incrementVersion(final String version, final int index) {
	debugPrintEnteringMethod("version", version, "index", index);

	// If no specified version used
	if (version == null) {
		String newVersion = index<0 ? "1.0" : "1";
		for (int i=0; i<index; i++) {
			newVersion += ".0";
		}
		return newVersion;
	}
	StringBuffer nextVersion = new StringBuffer();
	StringTokenizer tk = new StringTokenizer(version, ".");
	int count = 0;
	while (tk.hasMoreTokens()) {
		String digit = tk.nextToken();
		if (count > 0) {
			nextVersion.append(".");
		}
		if (count == index || !tk.hasMoreTokens()) {
			try {
				nextVersion.append(Integer.parseInt(digit) + 1);
			}
			catch(NumberFormatException e) {
				printException(e);
				throw new ScenarioFailedError("Incorrect format for the last found version '" + version + "' (expecting <number>(*.<number>)).");
			}
		} else {
			nextVersion.append(digit);
		}
		count++;
	}
	return nextVersion.toString();
}

/**
 * Remove all white spaces from the given string.
 *
 * @param str The string to be transformed
 * @return The transformed string
 */
public static String removeWhiteCharacters(final String str) {
	return removeWhiteCharacters(str, false);
}

/**
 * Remove all white spaces from the given string.
 * <p>
 * Note that current algorithm does not take into account '\' characters before
 * '"' characters to detect whether there's a string inside the given string.
 * Hence, if provided string contains backslashed double quotes the result of
 * white spaces removal might be not accurate...!
 * </p>
 * @param str The string to be transformed
 * @param skipStrings Flag to skip spaces removal from strings
 * @return The transformed string
 */
public static String removeWhiteCharacters(final String str, final boolean skipStrings) {
	char[] chars = str.toCharArray();
	StringBuffer buffer = new StringBuffer();
	boolean insideString = false;
	for (char ch: chars) {
		if (!(skipStrings && insideString) && (Character.isWhitespace(ch) || ch == '\u200c')) {
			// Got a whitespace character which must be removed
		} else {
			buffer.append(ch);
			if (ch == '"') {
				insideString = !insideString;
			}
		}
	}
	buffer.trimToSize();
	return buffer.toString();
}

/**
 * Sort a list of string assuming they all match standard versions pattern.
 * <p>
 * The version expected pattern is <code>number(*.number)</code>
 * (e.g. <code>4.0.1</code>).
 * </p><p>
 * This sort algorithm differs from standard string sorting by the fact it uses digit
 * integer comparison rather than strict string comparison. Then for example
 * version <code>1.10</code> will considered as higher than version <code>1.9</code>
 * although using string comparison would produce the opposite...
 * </p>
 * @param versions The versions list to be sorted
 * @return The sorted list
 */
public static List<String> sortVersions(final List<String> versions) {
	debugPrintEnteringMethod("versions", getTextFromList(versions));
	if (versions.size() == 0) {
		debugPrintln("No version to process, leave now...");
		return versions;
	}
	List<DigitalizedVersion> digitVersions = new ArrayList<>(versions.size());
	for (String version: versions) {
		DigitalizedVersion digitVersion = new DigitalizedVersion(version);
		digitVersions.add(digitVersion);
	}
	Collections.sort(digitVersions);
	List<String> sortedVersions = new ArrayList<>(versions.size());
	for (DigitalizedVersion dVersion: digitVersions) {
		sortedVersions.add(dVersion.toString());
	}
	return sortedVersions;
}
}
