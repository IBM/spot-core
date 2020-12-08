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
package com.ibm.bear.qa.spot.core.nls;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.getParameterValue;
import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.println;

import java.util.*;

/**
 * Class to manage scenario NLS messages.
 */
public abstract class NlsMessages {

	/**
	 * Supported locales to run scenario.
	 */
	public enum Supported {
		US(Locale.US);
		Locale locale;
		Supported(final Locale loc) {
			this.locale = loc;
		}
		protected Locale getLocale() {
        	return this.locale;
        }
		@Override
        public String toString() {
	        return this.locale.toString();
        }
	}

	/* Fields */
	private final Locale scenarioLocale;
	protected final ResourceBundle scenarioBundle;

public NlsMessages() {

	// Init locale
	String locale = getParameterValue("locale");
	Locale selectedLocale = null;
	if (locale == null) {
		String country = getParameterValue("locale.country");
		String language = getParameterValue("locale.language");
		if (language == null) {
			if (country != null) {
				println("locale.country argument has been set to '"+country+"' but it will be ignored as no language was specified!");
			}
			selectedLocale = Locale.getDefault();
		} else if (country == null) {
			selectedLocale = new Locale(language);
		} else {
			selectedLocale = new Locale(language, country);
		}
	} else {
		selectedLocale = Supported.valueOf(locale).getLocale();
	}
	this.scenarioLocale = selectedLocale;

	// Init bundle
	this.scenarioBundle = ResourceBundle.getBundle(bundleName(), this.scenarioLocale);
}

/**
 * The bundle name of the scenario messages.
 * <p>
 * Note that this name must include both the package and the properties file name
 * </p>
 * @return The bundle name as a {@link String}.
 */
abstract protected String bundleName();

/**
 * Return the NLS string value for the given key using the scenario locale.
 * <p>
 * If the key is not defined, returns the key surrounded by '!' not signify that
 * the corresponding key is not supported yet.
 * </p>
 * @param key The key of the searched string in messages properties file.
 * @return The string value as a {@link String}.
 */
public final String getNLSString(final String key) {
	try {
		return this.scenarioBundle.getString(key);
	}
	catch (@SuppressWarnings("unused") MissingResourceException e) {
		String fakeValue = '!' + key + '!';
		println("WARNING: There's no string value for key '"+key+"', using a fake value instead: "+fakeValue);
		return fakeValue;
	}
}

}
