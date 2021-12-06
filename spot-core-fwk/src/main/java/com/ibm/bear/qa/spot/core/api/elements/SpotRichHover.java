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

/**
 * Interface defining API for a rich over element.
 * <p>
 * Following methods are available on such element:
 * <ul>
 * <li>{@link #hasImage()}: Returns whether the rich hover has an image or not.</li>
 *</ul>
 * </p>
 */
public interface SpotRichHover {

/**
 * Returns true if there is an image in the rich hover.
 *
 * @return <code>true</code> if an image exists, <code>false</code> otherwise
 */
public boolean hasImage();

}
