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
 * Interface defining API for a window element.
 * <p>
 * Following methods are available on such element:
 * <ul>
 * <li>{@link #cancel()}: Close the window by clicking on the cancel button (usually the 'Cancel' button).</li>
 * <li>{@link #cancelAll()}: Close all possible opened dialogs by clicking on Cancel button or equivalent.</li>
 * <li>{@link #close()}: Close the window by clicking on the close button (usually the 'OK' button).</li>
 * <li>{@link #isCloseable()}: Tells whether the window can be closed or not.</li>
 * <li>{@link #isOpened()}: Check whether a window is opened or not.</li>
 * <li>{@link #isOpenedBeforeTimeout(int)}:  Check whether a window is opened or not during the given amount of seconds.</li>
 *</ul>
 * </p>
 */
public interface SpotWindow {

/**
 * Close the window by clicking on the cancel button (usually the 'Cancel' button).
 */
void cancel();

/**
 * Close all possible opened windows.
 * <p>
 * This is a no-op if there's no window opened.
 * </p>
 */
void cancelAll();

/**
 * Close the window by clicking on the close button (usually the 'OK' button).
 */
void close();

/**
 * Tells whether the window can be closed or not.
 * <p>
 * By default it's checking whether the close button is enabled or not.
 * </p>
 * @return <code>true</code> if there's a enabled close button, <code>false</code> otherwise
 */
boolean isCloseable();

/**
 * Check whether a window is opened or not.
 * <p>
 * Note that this method will also return true in case several windows are opened.
 * </p><p>
 * Searching for opened windows lasts one second.
 * </p>
 * @return <code>true</code> if at least one window is opened, <code>false</code>
 * otherwise.
 */
boolean isOpened();

/**
 * Check whether a window is opened or not during the given amount of seconds.
 * <p>
 * Note that this method will also return true in case several windows are opened.
 * </p>
 * @param seconds The number of seconds to wait for the window to be opened.
 * @return <code>true</code> if at least one window is opened, <code>false</code>
 * otherwise.
 */
boolean isOpenedBeforeTimeout(final int seconds);

}
