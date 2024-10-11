/*********************************************************************
* Copyright (c) 2012, 2024 IBM Corporation and others.
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
package com.ibm.bear.qa.spot.core.api;

/**
 */
public interface SpotUser {

/**
 * Return the user email address.
 *
 * @return The email address as a {@link String}
 */
public String getEmail();

/**
 * Return the user ID.
 *
 * @return The ID as a {@link String}
 */
public String getId();

/**
 * Return the user name.
 *
 * @return The name as a {@link String}
 */
public String getName();

/**
 * Return the user password.
 *
 * @return The password as a {@link String}
 */
public String getPassword();

/**
 * Return whether the user matches the given one.
 *
 * @return <code>true</code> if users have same ID, <code>false</code> otherwise.
 */
public boolean matches(SpotUser user);
}
