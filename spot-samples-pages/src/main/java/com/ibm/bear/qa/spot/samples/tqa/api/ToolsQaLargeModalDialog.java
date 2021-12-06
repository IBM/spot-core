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

import com.ibm.bear.qa.spot.core.api.elements.SpotDialog;
import com.ibm.bear.qa.spot.core.api.elements.SpotWindow;

/**
 * Interface to manage API of <b>Large Modal</b> dialog opened from <b>Modal Dialogs</b>
 * test section.
 * <p>
 * This class currently defines no specific public API methods as all necessary actions
 * (as closing and canceling dialog) are already offered by the core layer.
 * </p>
 * @see ToolsQaTestDialogsContainer
 * @see SpotWindow
 */
public interface ToolsQaLargeModalDialog extends SpotDialog {

}
