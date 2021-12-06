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
package com.ibm.bear.qa.spot.core.config;

/**
 * This interface defines constants used for users.
 */
public interface UserConstants {
	String USERID_ID = "UserID";
	String USERNAME_ID = "Username";
	String PASSWORD_ID = "Password";
	String EMAIL_ID = "Email";
	String MAIL_DOMAIN_ID = "mailDomain";

	// Default domain
	String USER_DEFAULT_EMAIL_DOMAIN_PROPERTY = "user.default.email.domain";
	String USER_DEFAULT_EMAIL_DOMAIN_VALUE = "@ibm.com";
}
