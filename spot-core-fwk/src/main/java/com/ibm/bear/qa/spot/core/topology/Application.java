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
package com.ibm.bear.qa.spot.core.topology;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.SpotImplementationError;
import com.ibm.bear.qa.spot.core.web.SpotAbstractLoginOperation;
import com.ibm.bear.qa.spot.core.web.WebPage;

/**
 * Abstract class for a topology application.
 * <p>
 * An application is identified by its {@link #location} which is assumed to be the prefix for any
 * web page URL of this application.
 * </p><p>
 * It's also assumed that this location is the concatenation of two strings:
 * <ol>
 * <li>the server address: expected format is
 * <code>https:<i>Server_DNS_Name</i>:<i>port_value</i></code> </li>
 * <li>the context root: usually a simple name</li>
 * </ol>
 * </p><p>
 * A user might be stored in the application let the topology know who is connected to this
 * application.
 * </p><p>
 * An application is responsible to provide web pages address to client.
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #equals(Object)}: Compares the argument to the receiver, and answers true</li>
 * <li>{@link #getContextRoot()}: Return the context root of the application.</li>
 * <li>{@link #getHost()}: Return the host of the machine on which the current application is installed.</li>
 * <li>{@link #getHostUrl()}: Return the host URL of the machine on which the current application is installed.</li>
 * <li>{@link #getLocation()}: The application location.</li>
 * <li>{@link #getLoginOperation(WebPage,User)}: Return the login operation which should be used with the current application.</li>
 * <li>{@link #getName()}: Returns the application name.</li>
 * <li>{@link #getPageUrl(String)}: Return the modified page URL if necessary.</li>
 * <li>{@link #getPageUrlWithAdditionalPath(String)}: Return a page URL with an optional additional path.</li>
 * <li>{@link #getProductName()}: Returns the application product name.</li>
 * <li>{@link #getSuffix()}: Returns the application suffix.</li>
 * <li>{@link #getTitle()}: Returns the application title.</li>
 * <li>{@link #getType()}: Returns the application type.</li>
 * <li>{@link #getTypeSuffix()}: Returns the type suffix.</li>
 * <li>{@link #getUserInfo()}: Return the user info used in application URL.</li>
 * <li>{@link #hasNoUser()}: Tells whether the application has an user connected or not.</li>
 * <li>{@link #hashCode()}: Answers an integer hash code for the receiver. Any two</li>
 * <li>{@link #isUserConnected(User)}: Tells whether the given user is connected to the current application or not.</li>
 * <li>{@link #login(User)}: Login the given user to the application.</li>
 * <li>{@link #logout()}: Logout all users from the application.</li>
 * <li>{@link #logout(User)}: Logout the given user from the application.</li>
 * <li>{@link #needLogin(User)}: Tells whether the current application would need login for the given user.</li>
 * <li>{@link #setName(String)}: Set the application name.</li>
 * <li>{@link #toString()}: Answers a string containing a concise, human-readable</li>
 * <li>{@link #useUms()}: Returns if the application is using UMS.</li>
 * </ul>
 * </p><p>
 * This class also defines or overrides following methods:
 * <ul>
 * <li>{@link #isApplicationFor(String)}: Tells whether the given URL address matches the current application or not.</li>
 * </ul>
 * </p>
 */
abstract public class Application {

	// The url prefix for any web pages of this application
	protected URL url;
	final String location, shortLocation;
	String contextRoot;

	// The user connected to the application
	final protected List<User> users = new ArrayList<>();
	private String name;

	// Flag to indicate whether the application is using UMS or not
	final protected boolean useUms;

protected Application(final String url, final boolean ums) {
	try {
		this.url = new URL(url);
		String path = this.url.getPath();
		String filePath = EMPTY_STRING;
		if (path.length() > 0) {
			StringTokenizer pathTokenizer = new StringTokenizer(path, "/");
			StringBuilder safePath = new StringBuilder();
			while (pathTokenizer.hasMoreTokens()) {
				this.contextRoot = pathTokenizer.nextToken();
				safePath.append('/').append(this.contextRoot);
			}
			filePath = safePath.toString();
			if (pathTokenizer.countTokens() > 0) {
				debugPrintln("Info: path for application '"+this.url+"' contains several segments. Context root ("+this.contextRoot+") was initialized with first one.");
			}
		}
		String appLocation = new URL(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), filePath).toExternalForm();
		this.shortLocation = appLocation;
		if (this.url.getQuery() != null) {
			appLocation += "?" + this.url.getQuery();
		}
		if (this.url.getRef() != null) {
			appLocation += "#" + this.url.getRef();
		}
		this.location = appLocation;
	}
	catch (MalformedURLException e) {
		throw new ScenarioFailedError(e.getMessage());
	}
	this.useUms = ums;
}

protected Application(final String url) {
	this(url, false);
}

@Override
public boolean equals(final Object obj) {
	if (obj instanceof Application) {
		Application application = (Application) obj;
		return this.location.equals(application.location);
	}
	return super.equals(obj);
}

/**
 * Return the context root of the application.
 *
 * @return The context root as a {@link String}.
 */
public String getContextRoot() {
	return this.contextRoot;
}

/**
 * Return the host of the machine on which the current application is installed.
 * <p>
 * The returned string is the hostname in the expected application location URL format:<br>
 * <i>&lt;protocol&gt;</i>://<b><i>&lt;hostname&gt;</i></b>:<i>&lt;port&gt;</i>
 * </p>
 * @return The host name as a {@link String}
 */
public String getHost() {
	return this.url.getHost();
}

/**
 * Return the host URL of the machine on which the current application is installed.
 * <p>
 * The returned string is kind of <i>&lt;protocol&gt;</i>://<i>&lt;hostname&gt;</i>:<i>&lt;port&gt;</i>
 * e.g. <pre>https://fit-vm12-96.rtp.raleigh.ibm.com:9443</pre>
 * </p>
 * @return The host url as a {@link String}
 */
public String getHostUrl() {
	String hostUrl = this.url.getProtocol()+"://"+getHost();
	if (this.url.getPort() >= 0) {
		hostUrl += ":" + this.url.getPort();
	}
	return hostUrl;
}

/**
 * The application location.
 *
 * @return The location as a {@link String}.
 */
public String getLocation() {
	return this.location;
}

/**
 * Return the login operation which should be used with the current application.
 * <p>
 * This method might return <code>null</code> in case no login operation is
 * necessary to access the application (typically when using basic auth in
 * application URL).
 * </p>
 * @param page The page on which the login operation will occur
 * @param appUser The user which needs to be logged to the application
 * @return The login operation to be used or <code>null</code> if there's no
 * necessary login operation
 */
public abstract SpotAbstractLoginOperation getLoginOperation(WebPage page, User appUser);

/**
 * Returns the application name.
 *
 * @return The name as a {@link String}.
 */
public String getName() {
	if (this.name == null) {
		String className = getClassSimpleName(getClass());
		this.name = className.substring(0, className.indexOf("App")).toUpperCase();
	}
	return this.name;
}

/**
 * Return the modified page URL if necessary.
 * <p>
 * Default is not to modify the page url.
 * </p>
 * @return The page URL as a {@link String}.
 */
public String getPageUrl(final String pageUrl) {
	return pageUrl;
}

/**
 * Return a page URL with an optional additional path.
 *
 * @param path The path to add to the application URL to get page
 * @return The page URL
 */
public String getPageUrlWithAdditionalPath(final String path) {
	String pageUrl;
	try {
		String newPath = (path == null || path.isEmpty()) ? this.url.getPath() : (this.url.getPath().isEmpty() ? path : path+"/"+this.url.getPath());
		pageUrl = new URL(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), newPath).toExternalForm();
	} catch (MalformedURLException ex) {
		throw new ScenarioFailedError(ex);
	}
	if (this.url.getQuery() != null) {
		pageUrl += "?" + this.url.getQuery();
	}
	if (this.url.getRef() != null) {
		pageUrl += "#" + this.url.getRef();
	}
	if (this.url.getUserInfo() != null) {
		pageUrl = this.url.getProtocol()+"://"+this.url.getUserInfo()+"@"+pageUrl.substring(pageUrl.indexOf("://")+3);
	}
	return pageUrl;
}

/**
 * Returns the application product name.
 *
 * @return The application product name as a {@link String}.
 */
public String getProductName() {
	throw new ScenarioFailedError(this+" has no associated product.");
}

/**
 * Returns the application suffix.
 * <p>
 * Default is no suffix.
 * </p>
 * @return The application suffix as a {@link String}
 */
public String getSuffix() {
	return EMPTY_STRING;
}

/**
 * Returns the application title.
 *
 * @return The application title as a {@link String}.
 */
public String getTitle() {
	return toString();
}

/**
 * Returns the application type.
 *
 * @return The application type as a {@link String}.
 */
public String getType() {
	return getTitle();
}

/**
 * Returns the type suffix.
 * <p>
 * Default is no suffix.
 * </p>
 * @return The type suffix as a {@link String}
 */
public String getTypeSuffix() {
	return EMPTY_STRING;
}

/**
 * Return the user info used in application URL.
 *
 * @return The user information as <code>"&lt;user ID&gt;:&lt;user pwd&gt;@"</code>
 * or <code>null</code> if no user info was used in application URL
 */
public String getUserInfo() {
	String userInfo = this.url.getUserInfo();
	return userInfo == null ? EMPTY_STRING : userInfo+"@";
}

@Override
public int hashCode() {
	return this.location.hashCode();
}

/**
 * Tells whether the application has an user connected or not.
 *
 * @return <code>true</code> if no user is connected,
 * <code>false</code> otherwise
 */
public boolean hasNoUser() {
	return this.users.isEmpty();
}

/**
 * Tells whether the given URL address matches the current application or not.
 *
 * @param pageUrl The URL address
 * @return <code>true</code> if the address belongs to the current application,
 * <code>false</code> otherwise
 */
protected boolean isApplicationFor(final String pageUrl) {
	try {
		URL testUrl = new URL(pageUrl);
		testUrl = new URL(testUrl.getProtocol(), testUrl.getHost(), testUrl.getPort(), testUrl.getPath());
		return testUrl.toExternalForm().startsWith(this.shortLocation);
	}
	catch (@SuppressWarnings("unused") MalformedURLException ex) {
		// skip
	}
	return false;
}

/**
 * Tells whether the given user is connected to the current application or not.
 *
 * @param user The user to check connection
 * @return <code>true</code> if the user is already connected,
 * <code>false</code> otherwise or if user is <code>null</code>
 */
public boolean isUserConnected(final User user) {
	return user == null ? false : this.users.contains(user);
}

/**
 * Login the given user to the application.
 *
 * @param user The user which would be connected to the application
 * @return <code>true</code> if the user was changed on the current application,
 * <code>false</code> otherwise.
 */
public boolean login(final User user) {
	debugPrintEnteringMethod("user", user);
	if (user == null) {
		throw new SpotImplementationError("Unexpected attempt to login in application with a null user.");
	}
	if (this.users.contains(user)) {
		if (DEBUG) debugPrintln("		  -> nothing was done as user was already logged in.");
		return false;
	}
	this.users.add(user);
	return true;
}

/**
 * Logout all users from the application.
 *
 * @return <code>true</code> if at least one user was logged out
 * or <code>false</code> if no user was connected.
 */
public boolean logout() {
	debugPrintEnteringMethod();
	if (this.users.isEmpty()) {
		debugPrintln("		  -> no user was connected, hence do nothing...");
		return false;
	}
	debugPrintln("		  -> "+this.users.size()+" user are disconnected from "+this);
	this.users.clear();
	return true;
}

/**
 * Logout the given user from the application.
 *
 * @param user The user to be disconnected
 * @return <code>true</code> if the user was logged out from the current
 * application, <code>false</code> otherwise.
 */
public boolean logout(final User user) {
	debugPrintEnteringMethod("user", user);
	if (user == null) {
		debugPrintln("		  -> no user was connected, hence do nothing...");
	} else {
		int usrIdx = this.users.indexOf(user);
		if (usrIdx >= 0) {
			if (DEBUG) debugPrintln("		  -> user is deconnected from "+this);
			this.users.remove(usrIdx);
			return true;
		}
	}
	return false;
}

/**
 * Tells whether the current application would need login for the given user.
 *
 * @param user The user which would be connected to the application
 * @return <code>true</code> if the user implied a login operation if it would
 * connect to the application, <code>false</code> otherwise
 */
public boolean needLogin(final User user) {
	debugPrintEnteringMethod("user", user);
	if (user == null) {
		debugPrintln("		  -> no user to connect to, hence do nothing...");
		return false;
	}
	boolean needLogin = !this.users.contains(user);
	debugPrintln("		  -> user does "+(needLogin?"":"not")+" need login to "+this);
	return needLogin;
}

/**
 * Set the application name.
 *
 * @param name The name to be set for the current application
 * @noreference Framework internal API, this method must not be used by any scenario test.
 */
public void setName(final String name) {
	this.name = name;
}

@Override
public final String toString() {
	StringBuilder builder = new StringBuilder(getName()).append(" Application (location=").append(this.location);
	if (this.users.isEmpty()) {
		builder.append(", no user logged in)");
	} else {
		builder.append(", users logged in: ");
		String separator = EMPTY_STRING;
		for (User user: this.users) {
			builder.append(separator).append(user.getId());
			separator = ", ";
		}
		builder.append(")");
	}
	return builder.toString();
}

/**
 * Returns if the application is using UMS.
 *
 * @return <code>true</code> if the application is using UMS,
 * <code>false</code> otherwise.
 */
public boolean useUms() {
	return this.useUms;
}
}