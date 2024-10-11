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

import java.util.StringTokenizer;

import org.openqa.selenium.By;

/**
 * Utility class to create {@link By} locator mechanism.
 * <p>
 * This class contains following API methods:
 * <ul>
 * <li>{@link #fixLocator(By)}: Check whether the locator need to be fixed.</li>
 * <li>{@link #getLocatorString(By)}: Return the string content for the given locator.</li>
 * <li>{@link #xpathCompareWithText(ComparisonPattern, String, boolean)}: Return a xpath string to compare the given text using the given pattern.</li>
 * <li>{@link #xpathMatchingItemText(String, ComparisonPattern, String)}: Return a xpath to match the given text using the given prefix and pattern.</li>
 * <li>{@link #xpathMatchingItemText(String, String)}: Return a xpath equals to the given text using the given prefix.</li>
 * <li>{@link #xpathMatchingText(ComparisonPattern, String)}: Return a xpath to match the given text using the given pattern.</li>
 * <li>{@link #xpathMatchingTexts(ComparisonPattern, boolean, String...)}: Return a xpath to match the given texts using the given pattern.</li>
 * <li>{@link #xpathQuotedString(String)}: Return a xpath string safely taking into account whether the given string contains any combination of quotation marks.</li>
 * <li>{@link #xpathStringForTextComparison(ComparisonPattern, String)}:  Return a xpath string to compare the given text using the given pattern.</li>
 * </ul>
 * </p>
 */
public class ByUtils {

	/**
	 * Comparison pattern.
	 */
	public enum ComparisonPattern {
		Equals,
		StartsWith,
		EndsWith,
		Contains,
	}

	private static final String NORMALIZE_SPACE_TEXT = "normalize-space(text())";

/**
 * Check whether the locator need to be fixed.
 *
 * @param locator The locator to be fixed if necessary
 */
public static By fixLocator(final By locator) {
	if (locator instanceof By.ById) {
		String locatorString = getLocatorString(locator);
		if (locatorString.indexOf(SPACE_CHAR) >= 0) {
			StringTokenizer tokenizer = new StringTokenizer(locatorString, SPACE_STRING);
			StringBuilder xpath = new StringBuilder("//*[");
			while (tokenizer.hasMoreTokens()) {
				xpath.append("contains(@id, '");
				xpath.append(tokenizer.nextToken());
				xpath.append("')");
				if (tokenizer.hasMoreTokens()) {
					xpath.append(" and ");
				}
			}
			xpath.append("]");
			if (DEBUG) debugPrintln("			-> locator '"+locator+"' had spaces, hence replacing it with '"+xpath+"'");
			return By.xpath(xpath.toString());
		}
	}
	return locator;
}

/**
 * Return the string content for the given locator.
 *
 * @param locator The locator to get the string
 * @return The locator string content as a {@link String}.
 */
public static String getLocatorString(final By locator) {
	String locatorString = locator.toString();
	return locatorString.replaceAll(": ", "(\"")+"\")";
}

/**
 * Return a xpath string to compare the given text using the given pattern.
 * <p>
 * Special addition is done to the xpath string if the comparison is done for a CLM
 * resource.
 * </p>
 * @param pattern The pattern used for matching text.
 * @param text The text to compare with
 * @param isClmResource Tells whether the text comparison concerns a CLM resource or not
 */
public static String xpathCompareWithText(final ComparisonPattern pattern, final String text, final boolean isClmResource) {

	// Add the text comparison to xpath builder
	StringBuilder comparisonBuilder = new StringBuilder();
	switch (pattern) {
		case Equals:
			comparisonBuilder.append("normalize-space(text())='").append(text).append("'");
			break;
		case StartsWith:
			comparisonBuilder.append("starts-with(normalize-space(text()),'").append(text).append("')");
			break;
		case Contains:
			comparisonBuilder.append("contains(normalize-space(text()),'").append(text).append("')");
			break;
		case EndsWith:
			comparisonBuilder.append("substring(normalize-space(text()), string-length(normalize-space(text())) - ").append(text.length()-1).append(") = '").append(text).append("'");
			break;
		default:
			throw new IllegalArgumentException("Invalid xpath kind: "+pattern);
	}

	// If item text comparison, then comparison will be special if text does not include the ID
	if (isClmResource) {

		// Check whether the given text match the "ID: summary" or not
		boolean isIdSummary = false;
		int idx = text.indexOf(':');
		if (idx > 0) {
			try {
				// Parse integer at the beginning of the text
				Integer.parseInt(text.substring(0, idx));

				// If no exception occurs, that means the given text matches the "ID: summary"
				isIdSummary = true;
				// Hence comparison can be on the full text => does not change text xpath
			}
			catch (@SuppressWarnings("unused") NumberFormatException nfe) {
				// The text does not match "ID: Summary" pattern
			}
		} else {
			// The text does not match "ID: Summary" pattern
		}

		// Replace xpath text if item does not match "ID: Summary"
		if (!isIdSummary) {
			// The text comparison will remove the ID prefix in potential link web elements
			int start = 0;
			int index;
			while ((index = comparisonBuilder.indexOf(NORMALIZE_SPACE_TEXT, start)) >= 0) {
				int end  = index + NORMALIZE_SPACE_TEXT.length();
				comparisonBuilder.replace(index, end, "substring-after("+NORMALIZE_SPACE_TEXT+",': ')");
				start = end;
			}
		}
	}

	// Return the comparison string to xpath
	return comparisonBuilder.toString();
}

/**
 * Return a xpath to match the given text using the given prefix and pattern.
 *
 * @param pattern The pattern used for matching text.
 * @param text The text to match
 * @return The xpath as a {@link By}.
 */
public static By xpathMatchingItemText(final String xpathPrefix, final ComparisonPattern pattern, final String text) {
	return xpathMatchingTexts(xpathPrefix, pattern, true/*item*/, true/*all*/, text);
}

/**
 * Return a xpath equals to the given text using the given prefix.
 *
 * @param text The text to match
 * @return The xpath as a {@link By}.
 */
public static By xpathMatchingItemText(final String xpathPrefix, final String text) {
	return xpathMatchingTexts(xpathPrefix, ComparisonPattern.Equals, true/*item*/, true/*all*/, text);
}

/**
 * Return a xpath to match the given text using the given pattern.
 *
 * @param pattern The pattern used for matching text.
 * @param text The text to match
 * @return The xpath as a {@link By}.
 */
public static By xpathMatchingText(final ComparisonPattern pattern, final String text) {
	return xpathMatchingTexts(".//*[", pattern, false/*item*/, true/*all*/, text);
}

/**
 * Return a xpath to match the given texts using the given pattern.
 * <p>
 * Note that if there are several texts provided, one can specify whether all of
 * them must match or only one is enough.
 * </p>
 * @param pattern The pattern used for matching text.
 * @param all Tells whether all texts must match or only one is enough. Ignored
 * if only one text is given.
 * @param texts The list of texts to match
 * @return The xpath as a {@link By}.
 */
public static By xpathMatchingTexts(final ComparisonPattern pattern, final boolean all, final String... texts) {
	return xpathMatchingTexts(".//*[", pattern, false, all, texts);
}

private static By xpathMatchingTexts(final String xpathPrefix, final ComparisonPattern pattern, final boolean item, final boolean all, final String... texts) {

	// Initiate the xpath builder
	StringBuilder xpathBuilder = new StringBuilder(xpathPrefix);

	// Add all strings to the builder
	String separator = "";
	for (String text: texts) {
		xpathBuilder
			.append(separator)
			.append(xpathCompareWithText(pattern, text, item));
		separator = all ? " and " : " or ";
	}

	// Finalize the xpath build
	xpathBuilder.append("]");

	// Return the locator mechanism
	return By.xpath(xpathBuilder.toString());
}

/**
 * Return a xpath string safely taking into account whether the given string contains
 * any combination of quotation marks.
 * <p>
 * This method will have following behavior:
 * <ol>
 * <li>returns the given string quoted with apostrophes if the given
 * string does not contain any apostrophe.</li>
 * <li>returns the given string quoted with quotes if the given
 * string does not contain any quote.</li>
 * <li>returns a concatenation of strings quoteds by quotes for parts wihtout any quotes
 * and quoted by apostrophes for quotes.</li>
 * </ol>
 * </p><p>
 * Here are some examples to highlight these different behaviors:
 * <ol>
 * <li>for the string: <pre>I'm reading Harry Potter</pre> this method will
 * return the following xpath:<pre>"I'm reading Harry Potter"</pre></li>
 * <li>for the string: <pre>I am reading "Harry Potter"</pre> this method will
 * return the following xpath:<pre>'I am reading "Harry Potter"'</pre></li>
 * <li>for the string: <code>I'm reading "Harry Potter"</code> this method will
 * return the following xpath: <pre>concat("I'm reading ",'"',"Harry Potter",'"')</pre></li>
 * </ol>
 * </p>
 * @param xpathString String to search for quotation marks
 * @return The xpath string to be used safely as a parameter of {@link By#xpath(String)} method.
 */
public static String xpathQuotedString(final String xpathString) {
	if (DEBUG) debugPrintln("		+ (insertString="+xpathString+")");

	// If string has no quotes then return a string surrounded by quotes
	if (!xpathString.contains("\"")) {
		StringBuilder xpathBuilder = new StringBuilder("\"").append(xpathString).append("\"");
		if (DEBUG) debugPrintln("		  -> there's no quote in the string, returned string is: "+xpathBuilder);
		return xpathBuilder.toString();
	}

	// If string has no apostrophes then return a string surrounded by apostrophes
	if (!xpathString.contains("'")) {
		StringBuilder xpathBuilder = new StringBuilder("'").append(xpathString).append("'");
		if (DEBUG) debugPrintln("		  -> there's no apostrophe in the string, returned string is: "+xpathBuilder);
		return xpathBuilder.toString();
	}

	// There's a mix of apostrophes and quotes, hence the string so must use xpath concat method
	StringBuilder xpathBuilder = new StringBuilder("concat(");

	// Going to look for " as they are less likely than ' in our string so will minimise number of arguments to concat.
	StringTokenizer tokenizer = new StringTokenizer(xpathString, "\"", true); // tokenizer returns delimiters
	int quotes = 0, tokens = 0;
	while (tokenizer.hasMoreTokens()) {

		// Get next token
		final String nextToken = tokenizer.nextToken();

		// Check whether the token is a delimiter
		if (nextToken.equals("\"")) {

			// Check whether this is a contiguous delimiter or not
			if (quotes == 0) {
				// Not a contiguous delimiter...
				// ...need first close previous token if any
				if (tokens > 0) {
					xpathBuilder.append("\", ");
				}
				//...then opens apostrophe to surround delimiter
				xpathBuilder.append("'");
			}

			// Add delimiter to final string
			xpathBuilder.append(nextToken);

			// Close the apostrophe in case of last token
			if (!tokenizer.hasMoreTokens()) {
				xpathBuilder.append("'");
			}

			// Increment the counter for contiguous delimiter
			quotes++;
		} else {
			// If there was delimiters before the token, then close the apostrophe which surround them
			if (quotes > 0) {
				xpathBuilder.append("', ");
			}

			// Open the quote and add token to final string
			xpathBuilder.append("\"").append(nextToken);

			// Close the quote in case of last token
			if (!tokenizer.hasMoreTokens()) {
				xpathBuilder.append("\"");
			}

			// Reset the contiguous delimiters counter
			quotes=0;
		}

		// Increment the number of tokens
		tokens++;
	}

	// Finalize the xpath and return it
	xpathBuilder.append(")");
	if (DEBUG) debugPrintln("		  -> there's was a mix of apostrophes and quotes in the string, returned string is: "+xpathBuilder);
	return xpathBuilder.toString();
}

/**
 * Return a xpath string to compare the given text using the given pattern.
 *
 * @param pattern The pattern used for matching text.
 * @param text The text to compare with
 */
public static String xpathStringForTextComparison(final ComparisonPattern pattern, final String text) {
	return xpathCompareWithText(pattern, text, false/*isClmResource*/);
}
}
