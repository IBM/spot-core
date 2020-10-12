/*********************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.samples.tqa.api;

/**
 * Interface to manage API of <b>Text Box</b> content in <b>Elements</b> section of ToolsQA test
 * page.
 * <p>
 * This class defines following public API methods of {@link ToolsQaTestTextBoxContainer} interface:
 * <ul>
 * <li>{@link #getCurrentAddress()}: Return the content of the <b>Current Address</b> field.</li>
 * <li>{@link #getEmail()}: Return the content of the <b>Email</b> field.</li>
 * <li>{@link #getFullName()}: Return the content of the <b>Full Name</b> field.</li>
 * <li>{@link #setCurrentAddress(String)}: Set the content of the <b>Current Address</b> field with the given text.</li>
 * <li>{@link #setEmail(String)}: Set the content of the <b>Email</b> field with the given text.</li>
 * <li>{@link #setFullName(String)}: Set the content of the <b>Full Name</b> field with the given text.</li>
 * </ul>
 * </p>
 */
public interface ToolsQaTestTextBoxContainer {

/**
 * Return the content of the <b>Current Address</b> field.
 *
 * @return The field content
 */
String getCurrentAddress();

/**
 * Return the content of the <b>Email</b> field.
 *
 * @return The field content
 */
String getEmail();

/**
 * Return the content of the <b>Full Name</b> field.
 *
 * @return The field content
 */
String getFullName();

/**
 * Set the content of the <b>Current Address</b> field with the given text.
 *
 * @param text The text to set in the field
 */
void setCurrentAddress(String text);

/**
 * Set the content of the <b>Email</b> field with the given text.
 *
 * @param text The text to set in the field
 */
void setEmail(String text);

/**
 * Set the content of the <b>Full Name</b> field with the given text.
 *
 * @param text The text to set in the field
 */
void setFullName(String text);

}
