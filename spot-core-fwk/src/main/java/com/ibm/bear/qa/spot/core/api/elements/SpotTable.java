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
package com.ibm.bear.qa.spot.core.api.elements;

import java.util.List;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Interface defining API for a table element (ie. having the <code>table</code> tag name).
 * <p>
 * This class defines following public API methods:
 * <ul>
 * <li>{@link #contains(String,String)}: Tells whether the given column contains a cell with the given text.</li>
 * <li>{@link #getColumnContent(String)}: Return the content of the table given column.</li>
 * <li>{@link #getColumnHeaders()}: Return the list of displayed columns.</li>
 * <li>{@link #getColumnsSize()}: Return the table columns size.</li>
 * <li>{@link #getContent()}: Return the content of the table as a list of strings list.</li>
 * <li>{@link #getHeaderIndex(String)}: Return the header index of the given column name.</li>
 * <li>{@link #getNumberOfRows()}: Get the number of rows of the table.</li>
 * <li>{@link #isColumnDisplayed(String)}: Check if the given column is displayed.</li>
 * </ul>
 * </p>
 */
public interface SpotTable {

/**
 * Tells whether the given column contains a cell with the given text.
 *
 * @param column The column to search the value
 * @param text The text to be searched
 * @return <code>true</code> if a cell element with given text is found,
 * <code>false</code> otherwise.
 * @throws ScenarioFailedError If the given column does not exist
 */
public boolean contains(final String column, final String text);

/**
 * Return the content of the table given column.
 *
 * @return The column content
 */
public List<String> getColumnContent(String column);

/**
 * Return the list of displayed columns.
 *
 * @return The column names list as a {@link List} of {@link String}.
 */
public List<String> getColumnHeaders();

/**
 * Return the table columns size.
 *
 * @return The size
 */
public int getColumnsSize();

/**
 * Return the content of the table as a list of strings list.
 *
 * @return The table content
 */
public List<List<String>> getContent();

/**
 * Return the header index of the given column name.
 *
 * @param column The column to be found
 * @return the 0-based index of the column as displayed in the grid
 *     or -1 if there's no column with the gievn name
 */
public int getHeaderIndex(final String column);

/**
 * Get the number of rows of the table.
 *
 * @return the number of rows of the table.
 */
public int getNumberOfRows();

/**
 * Check if the given column is displayed.
 *
 * @param columnTitle The title of the column to check for
 * @return <code>true</code> if the column is displayed, <code>false</code> otherwise
 */
public boolean isColumnDisplayed(final String columnTitle);

}
