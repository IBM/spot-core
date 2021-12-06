/*********************************************************************
* Copyright (c) 2020, 2021 IBM Corporation and others.
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
 * Interface to manage API of <b>Modal Dialogs</b> content in <b>Alerts, Frame & Windows</b>
 * section of the ToolsQA test page.
 * <p>
 * This class defines following public API methods of {@link ToolsQaTestDialogsContainer} interface:
 * <ul>
 * <li>{@link #openAndCloseSmallDialog()}: Open the Small Modal dialog and close it after 5 seconds.</li>
 * <li>{@link #openLargeModalDialog()}: Open the Large Modal dialog.</li>
 * </ul>
 * </p>
 */
public interface ToolsQaTestDialogsContainer {

/**
 * Open the Small Modal dialog and close it after 5 seconds.
 * <p>
 * Note that this method also checks dialog title and message.
 * </p>
 */
void openAndCloseSmallDialog();

/**
 * Open the Large Modal dialog.
 * <p>
 * Note that this method also checks dialog title and message.
 * </p>
 * @return The opened dialog API
 */
ToolsQaLargeModalDialog openLargeModalDialog();
}
