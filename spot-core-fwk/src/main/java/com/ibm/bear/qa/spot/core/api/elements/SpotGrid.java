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
package com.ibm.bear.qa.spot.core.api.elements;

import java.util.List;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioMissingImplementationError;

/**
 * Interface for a table grid element.
 * <p>
 * Following methods are available on such element:
 * <ul>
 * <li>{@link #applySortMode(String,SortMode)}: Apply the given sort mode to the given column.</li>
 * <li>{@link #getColumnSortMode(String)}: Return the sort mode of the column macthing the given name.</li>
 * <li>{@link #getSortedColumn()}: Return the name of the column which has a sorting activated.</li>
 * </ul>
 * </p>
 */
public interface SpotGrid extends SpotTable {
	/**
	 * Enum representing the 3 different sorting states: <b>Ascending</b>, <b>Descending</b> and <b>No Sort</b> to return to original sorting.
	 */
	public enum SortMode {
		/**
		 * Mode used to sort a table column in the ascending order.
		 */
		Ascending("Ascending"),
		/**
		 * Mode used to sort a table column in the descending order.
		 */
		Descending("Descending"),
		/**
		 * Mode used to specify that a table column in not sorted.
		 */
		NoSort("No sort", "none");

		/**
		 * Get the enumeration value for the given attribute value.
		 *
		 * @param attributeValue The attribute value
		 * @return The corresponding enum value
		 * @throws ScenarioMissingImplementationError If the given attribute value does not match any enumeration value
		 */
		public static SortMode fromAttribute(final String attributeValue) {
	        for (SortMode mode: values()) {
	        	if (mode.attribute.equals(attributeValue)) {
	        		return mode;
	        	}
	        }
	        throw new ScenarioMissingImplementationError("Attribute value '"+attributeValue+"' is not a known 'aria-sort' value value for SortMode enumeration!");
        }

		/**
		 * Get the enumeration value for the given text.
		 *
		 * @param text The text of the enumeration
		 * @return The corresponding enum value
		 * @throws ScenarioMissingImplementationError If the given text does not match any enumeration label
		 */
		public static SortMode fromText(final String text) {
	        for (SortMode mode: values()) {
	        	if (mode.label.equals(text)) {
	        		return mode;
	        	}
	        }
	        throw new ScenarioMissingImplementationError("Text '"+text+"' is not a known label for SortMode enumeration!");
        }

		/* Fields */
		private final String label;
		private final String attribute;

		SortMode(final String value) {
			this(value,value.toLowerCase());
		}

		SortMode(final String value, final String attrib) {
			this.label = value;
			this.attribute = attrib;
		}

		@Override
		public String toString() {
			return this.label;
		}
	}

/**
 * Apply the given sort mode to the given column.
 *
 * @param column The column on which to apply the sort mode
 * @param mode The sort mode to apply
 * @throws ScenarioFailedError If the column is not sortable or if there's no column
 * in the current table matching the given name
 */
void applySortMode(final String column, final SortMode mode) throws ScenarioFailedError;

/**
 * Return the sort mode of the column matching the given name.
 *
 * @param column The name of the column
 * @return The column names list as a {@link List} of {@link String} or <code>null</code>
 * if the column is not sortable in the current table
 * @throws ScenarioFailedError If there's no column in the current table matching the given name
 */
SortMode getColumnSortMode(final String column) throws ScenarioFailedError;

/**
 * Return the name of the column which has a sorting activated.
 *
 * @return The column name as a {@link String} or <code>null</code>
 * if there's no sorted column in the current table
 */
String getSortedColumn();
}
