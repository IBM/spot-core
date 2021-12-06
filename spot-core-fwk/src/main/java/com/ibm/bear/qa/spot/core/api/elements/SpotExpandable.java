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

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Interface for an expandable element.
 * <p>
 * Following methods are available on such element:
 * <ul>
 * <li>{@link #collapse()}: Collapse the current wrapped web element.</li>
 * <li>{@link #expand()}: Expand the current wrapped web element.</li>
 * <li>{@link #isExpandable()}: Returns whether the current wrapped web element is expandable or not.</li>
 * <li>{@link #isExpanded()}: Returns whether the current wrapped web element is expandable or not.</li>
 * <li>{@link #toggle()}: Toggle the current web element.</li>
 *</ul>
 * </p>
 */
public interface SpotExpandable {

/**
 * Collapse the current web element.
 * <p>
 * If the web element is already expanded, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
void collapse() throws ScenarioFailedError;

/**
 * Expand the current web element.
 * <p>
 * If the web element is already expanded, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
void expand() throws ScenarioFailedError;

/**
 * Returns whether the current wrapped web element is expandable or not.
 * <p>
 * Subclass must override this method if the web element has no specific
 * expandable attribute.
 * </p>
 * @return <code>true</code> if the current node is expanda, <code>false>/code>
 * otherwise.
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
boolean isExpandable() throws ScenarioFailedError;

/**
 * Returns whether the current wrapped web element is expanded or not.
 * <p>
 * Subclass must override this method if the web element has no specific
 * expandable attribute.
 * </p>
 * @return <code>true</code> if the current node is expanded, <code>false>/code>
 * otherwise.
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
boolean isExpanded() throws ScenarioFailedError;

/**
 * Toggle the current web element.
 * <p>
 * If the web element is already expanded, then nothing happens.
 * </p>
 * @throws ScenarioFailedError If the wrapped web element does not have
 * the <code>aria-expanded</code> attribute.
 */
void toggle() throws ScenarioFailedError;
}
