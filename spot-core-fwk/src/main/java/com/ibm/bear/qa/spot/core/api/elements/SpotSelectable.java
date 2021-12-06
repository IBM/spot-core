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

import org.openqa.selenium.WebElement;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.web.SpotSelectableElement;

/**
 * Interface for a selectable element.
 * <p>
 * The usual elements handled by this interface are check-boxes. Framework
 * offers a default concrete implementation of this interface in {@link SpotSelectableElement}
 * as Selenium already offers a default API for such element: {@link WebElement#click()}
 * and {@link WebElement#isSelected()}.
 * </p><p>
 * Unfortunately, this method does not always work for all actually selectable
 * element and without framework implementation one would often have to implement
 * the element selection test in a specific manner.
 * </p><p>
 * Following methods are available on such element:
 * <ul>
 * <li>{@link #isSelected()}: Returns whether the current wrapped web element is selected or not.</li>
 * <li>{@link #select()}: Select the current wrapped web element.</li>
 * <li>{@link #toggle()}: Toogle the current wrapped web element.</li>
 * <li>{@link #unselect()}: Unselect the current wrapped web element.</li>
 *</ul>
 * </p>
 */
public interface SpotSelectable {

/**
 * Select the associated web element.
 * <p>
 * If the web element is already selected, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-selected</code> attribute.
 */
void select() throws ScenarioFailedError;

/**
 * Unselect the associated web element.
 * <p>
 * If the web element already not selected, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-selected</code> attribute.
 */
void unselect() throws ScenarioFailedError;

/**
 * Returns whether the associated web element is selected or not.
 * <p>
 * By default this method returns the result of the {@link WebElement#isSelected()}
 * method applied on the wrapped element.
 * </p>
 * @return <code>true</code> if the current node is expanded, <code>false>/code>
 * otherwise.
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-selected</code> attribute.
 */
boolean isSelected() throws ScenarioFailedError;

/**
 * Toggle the associated web element.
 * <p>
 * If the web element is selected, then it unselects it. Otherwise it selects it.
 * </p>
 * @return The new status of the selectable element
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-selected</code> attribute.
 */
boolean toggle() throws ScenarioFailedError;
}
