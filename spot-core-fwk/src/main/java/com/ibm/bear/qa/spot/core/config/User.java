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
package com.ibm.bear.qa.spot.core.config;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterValue;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.println;

import java.util.Base64;

import com.ibm.bear.qa.spot.core.api.SpotUser;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * User connected to an application while going to a web page.
 * <p>
 * User is defined by an ID which is a unique string. It has a name, a password and
 * an email address.
 * </p>
 */
public class User implements UserConstants, SpotUser {

	// User info
	String id;
	String name;
	String password;
	String email;

	// Encryption
	private boolean encrypted = false;

/**
 * Create a user instance using the given prefix.
 * <p>
 * Note that prefix must not be <code>null</code>. Then information for user are
 * got from following system properties:
 * <ul>
 * <li><code><i>prefix</i>Username</code>: The user name</li>
 * <li><code><i>prefix</i>UserID</code>: The user ID</li>
 * <li><code><i>prefix</i>Password</code>: The user password</li>
 * <li><code><i>prefix</i>Email</code>: The user e-mail</li>
 * </ul>
 * <b>Warning: the properties names are case sensitive.</b>
 * </p><p>
 * Note also that user password won't be encrypted.
 * </p>
 * @param prefix The prefix used to find user properties
 * @throws ScenarioFailedError If either name, id or password property is not defined
 */
protected User(final String prefix) {
	this(prefix, null);
}

/**
 * Create a user instance using the given prefix and password encryption if specified.
 * <p>
 * Note that prefix must not be <code>null</code>. Then information for user are
 * got from following system properties:
 * <ul>
 * <li><code><i>prefix</i>Username</code>: The user name</li>
 * <li><code><i>prefix</i>UserID</code>: The user ID</li>
 * <li><code><i>prefix</i>Password</code>: The user password</li>
 * <li><code><i>prefix</i>Email</code>: The user e-mail</li>
 * </ul>
 * <b>Warning: the properties names are case sensitive.</b>
 * </p>
 * @param prefix The prefix used to find user properties
 * @param encrypted Tells whether the password must be encrypted or not
 * @throws ScenarioFailedError If either name, id or password property is not defined
 */
protected User(final String prefix, final boolean encrypted) {
	this(prefix);
	this.encrypted = encrypted;
}

/**
 * Create a user instance using either the given prefix or the given user name.
 * <p>
 * If prefix is not <code>null</code>, then information for user are
 * got from following system properties:
 * <ul>
 * <li><code><i>prefix</i>Username</code>: The user name</li>
 * <li><code><i>prefix</i>UserID</code>: The user ID</li>
 * <li><code><i>prefix</i>Password</code>: The user password</li>
 * <li><code><i>prefix</i>Email</code>: The user e-mail</li>
 * </ul>
 * <b>Warning: the properties names are case sensitive.</b>
 * </p><p>
 * If prefix is <code>null</code> or if prefixed properties are not defined,
 * then user argument is used to initialize the user name, id, password and email.
 * For the email value, a default domain value is got from
 * {@link UserConstants#USER_DEFAULT_EMAIL_DOMAIN_PROPERTY} property.
 * </p>
 * @param prefix The prefix used to find user properties
 * @param user Tells whether the password must be encrypted or not
 * @throws ScenarioFailedError If either name, id or password value is <code>null</code>
 */
public User(final String prefix, final String user) {
	String defaultDomain = getParameterValue(USER_DEFAULT_EMAIL_DOMAIN_PROPERTY, USER_DEFAULT_EMAIL_DOMAIN_VALUE);
	if (prefix == null) {
		this.name = user;
		this.id = user;
		this.password = user;
		this.email = user+defaultDomain;
	} else {
		this.name = getParameterValue(prefix+USERNAME_ID, user);
		this.id = getParameterValue(prefix+USERID_ID, this.name);
		this.password = getParameterValue(prefix+PASSWORD_ID);
		// Discard password default initialization as that can lead to invalid login...
		// Hence that will raise an SFE instead.
//		if (this.password == null) {
//			this.password = this.id;
//		}
		this.email = getParameterValue(prefix+EMAIL_ID, this.id+getParameterValue(MAIL_DOMAIN_ID, defaultDomain));
	}

	// Check that we got at least an ID, a name and a password
	if (this.name == null || this.id == null || this.password == null) {
		StringBuilder messageBuilder = new StringBuilder("Invalid user information: ");
		messageBuilder.append(" id=").append(this.id == null ? "<!missing!>" : this.id);
		messageBuilder.append(" name=").append(this.name == null ? "<!missing!>" : this.name);
		messageBuilder.append(" pwd=").append(this.password == null ? "<!missing!>" : this.password);
		throw new ScenarioFailedError(messageBuilder.toString());
	}

	// Warn if email has not been defined
	if (this.email == null) {
		println("Warning: no email has been defined for user "+this.id);
	}
}

/**
 * Create a user instance with the given ID, name, password and email.
 *
 * @param userId The user ID
 * @param userName The user name
 * @param pwd The user password
 * @param mail The use e-mail
 */
protected User(final String userId, final String userName, final String pwd, final String mail) {
	this.id = userId;
	this.name = userName;
	this.password = pwd;
	this.email = mail;
}

@Override
public boolean equals(final Object obj) {
	if (obj instanceof User) {
	    return ((User)obj).id.equals(this.id);
	}
	return false;
}

/**
 * Return the decrypted user password.
 * <p>
 * <b>Warning</b>: This method must not be used outside the framework.
 * It also must not be used to print the password neither in the console nor
 * in the debug log file.
 * </p><p>
 * Note that this is a no-op if the password is not encrypted.
 * </p>
 * @return The decrypted password as a {@link String}
 * @since 6.0
 */
final public String getDecryptedPassword() {
	if (this.encrypted) {
		return new String(Base64.getDecoder().decode(this.password.getBytes()));
	}
	return getPassword();
}

/**
 * Return the user email address.
 *
 * @return The email address as a {@link String}
 */
@Override
final public String getEmail() {
	return this.email;
}

/**
 * Return the user ID.
 *
 * @return The ID as a {@link String}
 */
@Override
final public String getId() {
	return this.id;
}

/**
 * Return the user name.
 *
 * @return The name as a {@link String}
 */
@Override
final public String getName() {
	return this.name;
}

/**
 * Return the user password.
 *
 * @return The password as a {@link String}
 */
@Override
final public String getPassword() {
	return this.password;
}

@Override
public int hashCode() {
    return this.id.hashCode();
}

@Override
public boolean matches(final SpotUser user) {
    return this.id.equals(((User)user).id);
}

@Override
public String toString() {
	return "User id=" + this.id + ", name=" + this.name + ", passwd=" + this.password.charAt(0) + "*******" + (this.email == null ? "" : ", mail=" + this.email);
}
}