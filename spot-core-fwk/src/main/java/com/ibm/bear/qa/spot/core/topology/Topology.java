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

package com.ibm.bear.qa.spot.core.topology;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;
import static com.ibm.bear.qa.spot.core.utils.StringUtils.hidePasswordInLocation;

import java.util.*;

import com.ibm.bear.qa.spot.core.config.User;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioMissingImplementationError;

/**
 * Manage the scenario topology. A topology is made of applications
 * (e.g. CLM applications: JTS, CCM, QM, RM, and LPA) which can be deployed
 * either on collocated or distributed machines.
 * <p>
 * The topology manages user login to each application and typically provides
 * service to know whether a login is necessary before going to a specific location.
 * </p>
 */
abstract public class Topology {

	// Applications and servers
	protected List <Application> applications = new ArrayList<Application>();
	protected Map<String, List<Application>> servers = new HashMap<String, List<Application>>();

/**
 * Initialize the topology from a given CLM version.
 */
public Topology() {

	// Store applications
	initApplications();

	// Store servers
	initServers();
}

/**
 * Add an application to the topology.
 *
 * @param application The application to add
 */
public void addApplication(final Application application) {
	for (Application appli: this.applications) {
		if (appli.getLocation().equals(application.getLocation())) {
			// The application has already been stored in topology, hence do nothing
			return;
		}
	}
	this.applications.add(application);
	updateServer(application);
}

/**
 * Add an application that will be associated with page opened with given class and URL.
 * <p>
 * Subclasses has to override this method which default behavior is to raise a
 * missing implementation error.
 * </p>
 * @param pageClassName The page class
 * @param url The page URL
 * @return The created and added application
 */
@SuppressWarnings("unused")
public Application addApplication(final String pageClassName, final String url) {
	throw new ScenarioMissingImplementationError(whoAmI());
}

/**
 * Return the application matching the given url address.
 *
 * @param url The address
 * @return The {@link Application} corresponding to the given address.
 * @throws ScenarioFailedError If not application is found for the given
 * address.
 */
public Application getApplication(final String url) {
	for (Application application: this.applications) {
		if (application.isApplicationFor(url)) {
			if (DEBUG) debugPrintln("		  -> found application '"+application+"' for URL: "+hidePasswordInLocation(url));
			return application;
		}
	}
	return null;
}
// TODO Possible other implementation which would be smarter to detect the better
// application if several would return true for isApplication(url)...
//
//public Application getApplication(final String url) {
//	Application matchingApplication = null;
//	double length = url.length();
//	double previousRatio = 0.0;
//	for (Application application: this.applications) {
//		if (application.isApplicationFor(url)) {
//			double ratio = application.location.length() / length;
//			if (ratio == 1) {
//				if (DEBUG) debugPrintln("		  -> return application '"+application+"'");
//				return application;
//			}
//			if (matchingApplication == null) {
//				matchingApplication = application;
//				if (DEBUG) debugPrintln("		  -> found matching application '"+application+"' for URL: "+url);
//			} else {
//				if (ratio > previousRatio) {
//					matchingApplication = application;
//				}
//			}
//		}
//	}
//	if (matchingApplication != null) {
//		if (DEBUG) debugPrintln("		  -> return application '"+matchingApplication+"'");
//		return matchingApplication;
//	}
//	return null;
//}

/**
 * Return the list of application titles
 *
 * @return All the supported applications during the test scenario as a {@link List}
 * of {@link Application}.
 */
public List<Application> getApplications() {
	return this.applications;
}

/**
 * Return the modified page URL if necessary.
 *
 * @return The page URL as a {@link String}.
 * @see Application#getPageUrl(String)
 */
public String getPageUrl(final String currentUrl) {
	if (currentUrl.startsWith("data")) {
		return currentUrl;
	}
	Application app = getApplication(currentUrl);
	if (app == null) {
		return currentUrl;
	}
	return app.getPageUrl(currentUrl);
}

/**
 * Initialize all topology applications.
 */
abstract protected void initApplications();


/**
 * Initialize topology servers.
 */
protected void initServers() {
	Collection<Application> list = this.applications;
	for (Application application: list) {
		updateServer(application);
	}
}

/**
 * Returns whether this topology is distributed across multiple servers
 *
 * @return <code>true</code> if the topology is distributed, <code>false</code> otherwise
 */
public abstract boolean isDistributed();

/**
 * Login the given user to the application matching the given location.
 * <p>
 * The login is propagated to other applications which are on the same server.
 * </p>
 *
 * @param location The location concerned by the login
 * @param user The user concerned by the login
 * @return <code>true</code> if the user was changed on the application from
 * which the location belongs to, <code>false</code> otherwise.
 */
public boolean login(final String location, final User user) {
	if (DEBUG) debugPrintln("		+ Login user "+user.getId()+" for all applications");

	// Get the application matching the given location
	Application application = getApplication(location);
	if (application == null) {
		throw new ScenarioFailedError("Cannot find any application at the given location: "+hidePasswordInLocation(location));
	}

	// Login to the application belonging the given location
	boolean login = application.login(user);

	// Propagate user login to other applications
	if (login) {
		// First propagate to other applications using same login operation
		for (Application appli: this.applications) {
			if (application.matchApplicationLoginOperationForUser(appli, user)) {
				appli.login(user);
			}
		}

		// Second propagate to other applications on same server
		List<Application> serverApplications = this.servers.get(application.getHost());
		if (serverApplications != null) {
			for (Application appli: serverApplications) {
				if (application.matchApplicationLoginOperationForUser(appli, user)) {
					appli.login(user);
				}
			}
		}
	}

	// Return whether there was a user change on the application
	return login;
}

/**
 * Logout the given user from the application matching the given location and
 * do the same for all other applications on the same server.
 *
 * @param location The location concerned by the logout
 */
public boolean logout(final String location) {
	if (DEBUG) debugPrintln("		+ Logout applications from "+location);

	// Get all application on the same server than the application
	Application application = getApplication(location);
	List<Application> serverApplications = this.servers.get(application.getHost());

	// Get applications needing login on the server
	boolean appliUserChanged = false;
	for (Application appli: serverApplications) {
		boolean changed = appli.logout();
		if (appli.equals(application)) {
			appliUserChanged = changed;
		}
	}

	// Return whether the user has change for the application
	return appliUserChanged;
}

/**
 * Logs out all applications, regardless of the user currently logged in.
 */
public void logoutApplications() {
	for (Application app : this.applications) {
		app.logout();
	}
}

/**
 * Returns whether the given user needs to login before accessing the given
 * location.
 *
 * @param location The location to go to
 * @param user The user to use when going to the location
 * @return <code>true</code> if the user has never accessed to the application
 * matching the location and neither to no any other application of the same server,
 * <code>false</code> otherwise.
 */
public boolean needLogin(final String location, final User user) {
	String hiddenPasswordLocation = hidePasswordInLocation(location);
	debugPrintEnteringMethod("location", hiddenPasswordLocation, "user", user);
	if (user == null) {
		throw new IllegalArgumentException("Need a user to decide whether login is needed or not.");
	}

	// If application does not need login, then return
	Application application = getApplication(location);
	if (application == null) {
		throw new ScenarioFailedError("Cannot find any application for user '"+user.getId()+"' with location: "+hiddenPasswordLocation);
	}
	if (!application.needLogin(user)) {
		return false;
	}

	// Propagate user login to other applications
	for (Application appli : this.applications) {
		if (application.matchApplicationLoginOperationForUser(appli, user)) {
			if (appli.isUserConnected(user)) {
				application.login(user);
				return false;
			}
		}
	}

	// None of the application is logged, it needs login
	if (DEBUG) debugPrintln("		  -> no application is already logged in on same server");
	return true;
}

/**
 * Return whether two applications are hosted on the same server.
 *
 * @param firstApp The first application to check
 * @param secondApp The second application to check
 *
 * @return <code>true</code> if the two applications are on the
 * same server, <code>false</code> otherwise
 */
public boolean onSameHost(final Application firstApp, final Application secondApp) {
	return firstApp.getHostUrl().equals(secondApp.getHostUrl());
}

/**
 * Update topology servers for the given application.
 *
 * @param application The application to add the server for
 */
protected void updateServer(final Application application) {
	List<Application> serverApplications = this.servers.get(application.getHost());
	if (serverApplications == null) {
		this.servers.put(application.getHost(), serverApplications = new ArrayList<Application>());
	}
	if (!serverApplications.contains(application)) {
		serverApplications.add(application);
	}
}
}