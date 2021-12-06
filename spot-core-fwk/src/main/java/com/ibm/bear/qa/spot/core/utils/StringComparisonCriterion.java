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

/**
 * Enumeration to define a list of comparison criteria for matching
 * two strings.
 */
public enum StringComparisonCriterion {

	EQUALS {
		/**
		 * Compare the two given strings for equality.<br>
		 * <p>The comparison is done as follows: <b>text1.equals(text2)</b></p>
		 *
		 * @param text1 First string
		 * @param text2 Second string
		 * @return <b>true</b> if the 2 strings are equal or <b>false</b> otherwise.
		 */
		@Override
		public boolean compare(final String text1, final String text2) {
			return text1.equals(text2);
		}

		@Override
		public String toString() {
			return "equals";
		}
	},

	ENDSWITH {
		/**
		 * Compare if the first string ends with the second string.<br>
		 * <p>The comparison is done as follows: <b>text1.endsWith(text2)</b></p>
		 *
		 * @param text1 First string
		 * @param text2 Second string
		 * @return <b>true</b> if the first string ends with the second string or <b>false</b> otherwise.
		 */
		@Override
		public boolean compare(final String text1, final String text2) {
			return text1.endsWith(text2);
		}

		@Override
		public String toString() {
			return "ends with";
		}
	},

	STARTSWITH {
		/**
		 * Compare if the first string starts with the second string.<br>
		 * <p>The comparison is done as follows: <b>text1.startsWith(text2)</b></p>
		 *
		 * @param text1 First string
		 * @param text2 Second string
		 * @return <b>true</b> if the first string starts with the second string or <b>false</b> otherwise.
		 */
		@Override
		public boolean compare(final String text1, final String text2) {
			return text1.startsWith(text2);
		}

		@Override
		public String toString() {
			return "starts with";
		}
	},

	CONTAINS {
		/**
		 * Compare if the first string contains the second string.<br>
		 * <p>The comparison is done as follows: <b>text1.contains(text2)</b></p>
		 *
		 * @param text1 First string
		 * @param text2 Second string
		 * @return <b>true</b> if the first string contains the second string or <b>false</b> otherwise.
		 */
		@Override
		public boolean compare(final String text1, final String text2) {
			return text1.contains(text2);
		}

		@Override
		public String toString() {
			return "contains";
		}
	};

	/**
	 * Compare the two given strings in the corresponding criterion.<br>
	 *
	 * @param text1 First string
	 * @param text2 Second string
	 * @return <b>true</b> if the 2 strings match in the corresponding criterion or <b>false</b> otherwise.
	 */
	public abstract boolean compare(final String text1, final String text2);
}
