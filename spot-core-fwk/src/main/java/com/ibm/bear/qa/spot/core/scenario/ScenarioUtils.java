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
package com.ibm.bear.qa.spot.core.scenario;

import static com.ibm.bear.qa.spot.core.utils.FileUtil.createDir;

import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.ibm.bear.qa.spot.core.nls.SpotNlsMessages;
import com.ibm.bear.qa.spot.core.params.ScenarioParametersFile;
import com.ibm.bear.qa.spot.core.params.ScenarioParametersManager;
import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;

/**
 * Utils for Scenario tests execution.
 * <p>
 * The first utility is to read Scenario parameters.<br>
 * They can be defined either in a properties files specified by the System
 * properties {@link #PARAM_FILES_DIR_ID} and {@link #PARAM_FILES_PATH_ID}
 * or directly by System properties specified in the VM arguments tab of launch configurations.
 * </p><p>
 * The second utility is to provide debug function allowing to dump debug
 * information either in the console or in a file if the System property
 * {@link #DEBUG_DIRECTORY} is set.
 * </p><p>
 * This class also provides following utilities:
 * <ul>
 * <li>{@link #elapsedTimeString(long)}: Returns a string to display the elasped time since the given start point.</li>
 * </ul>
 */
public class ScenarioUtils {

	/* Directories */
	public static final String USER_DIR_ID = "user.dir";
	public static final String USER_HOME_ID = "user.home";

	/* Common characters */
	public static final char SPACE_CHAR = ' ';
	public static final char PATH_SEPARATOR_CHAR = '/';
	public static final char QUOTE = '\"';

	/* Common strings */
	public static final String EMPTY_STRING = "";
	public static final String SPACE_STRING = " ";
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String PATH_SEPARATOR = "/";
	public static final String DEBUG_ENTERING_METHOD_INDENTATION = "		+";
	private static final int DEBUG_ENTERING_METHOD_INDENTATION_LENGTH = DEBUG_ENTERING_METHOD_INDENTATION.length();
	public static final String DEBUG_ENTERING_METHOD_TEXT = DEBUG_ENTERING_METHOD_INDENTATION + " Entering method %s";

	// NLS Messages
	public static final SpotNlsMessages NLS_MESSAGES = new SpotNlsMessages();

	/* Data */
	public static final String[] NO_DATA = new String[0];

	/* Internal */
//	private final static List<String> PRINT_PARAMS = new ArrayList<String>();

	/* Time and date */
	public static final int ONE_MINUTE = 60000;
	public static final long ONE_HOUR = 3600000L;
	public static final SimpleDateFormat COMPACT_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$
	public static final SimpleDateFormat NORMAL_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$
	public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat();
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
	public static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$

	/* Date kept for log filtering. Compact string used to timestamp created files */
	public static final Date SCENARIO_START_TIME = new Date(System.currentTimeMillis());
	public static final String COMPACT_DATE_STRING = COMPACT_DATE_FORMAT.format(SCENARIO_START_TIME);
	public static final String NORMAL_DATE_STRING = NORMAL_DATE_FORMAT.format(SCENARIO_START_TIME);
	public static final String SIMPLE_DATE_STRING = SIMPLE_DATE_FORMAT.format(SCENARIO_START_TIME);

	/* Recovery */
	public static final int MAX_RECOVERY_TRIES = 5;

	/**
	 * Global flag whether to print output on console or not.
	 *
	 * Default is <code>true</code>.
	 */
	public static final boolean PRINT = System.getProperty("print") == null || System.getProperty("print").equals("true");

	/**
	 * Global flag whether to print debug information on console or not.
	 * <p>
	 * Default is <code>true</code>.
	 * </p>
	 * @category debug parameters
	 */
	public static final boolean DEBUG = System.getProperty("debug") == null || System.getProperty("debug").equals("true");

	/**
	 * Returns the directory to use to store debug file.
	 * <p>
	 * To specify it, then use the following parameter:
	 * <ul>
	 * <li><b>Name</b>: <code>debug.dir</code></li>
	 * <li><b>Value</b>: <code>String</code>, a valid directory name matching
	 * the OS on which you're running the scenario<br></li>
	 * <li><b>Default</b>: <i>none</i></li>
	 * <li><b>Usage</b>:
	 * <ul>
	 * <li><code>debug.dir=C:\tmp\selenium\failures</code> in the properties file</li>
	 * <li><code>-Ddebug.dir=C:\tmp\selenium\failures</code> in the VM Arguments
	 * field of the launch configuration.</li>
	 * </ul></li>
	 * </ul>
	 * </p><p>
	 * Note that this parameter is ignored even if specified when {@link #DEBUG}
	 * parameter is set to <code>false</code>.
	 * </p>
	 */
	public final static String DEBUG_DIRECTORY;
	static {
		String dir = null;
		dir = System.getProperty("spot.debug.dir", "debug");
		if (dir != null && dir.trim().length() > 0) {
			if (dir.indexOf(File.separatorChar) < 0) {
				dir = System.getProperty(USER_DIR_ID) + File.separator + dir;
			}
		} else {
			dir = null;
		}
		DEBUG_DIRECTORY = dir;
	}

	/**
	 * Parameter telling which directory to use to put debug file.
	 * <p>
	 * Name: <code>"log.file.name"</code><br>
	 * Value: <code>String</code>, a valid file name matching the OS on which you're running the BVT test<br>
	 * Default value: <i>debug.log</i></br>
	 * Usage: <code>-Dlog.file.name=my_debug_file.log</code> in the VM Arguments
	 * field of the launch configuration.
	 * </p></p>
	 * Note that this parameter is ignored if {@link #DEBUG} parameter
	 * is set to <code>false</code>.
	 * </p>
	 * @category debug parameters
	 */
	public final static String DEBUG_LOG_FILE_NAME = System.getProperty("log.file.name", "debug_"+COMPACT_DATE_STRING+".log");

	/* Parameters and data */
	/**
	 * The root path of the directory where the properties files are put.
	 * Can be either absolute or relative path. If relative, the root is
	 * the scenario project directory.
	 */
	private static final String PARAM_FILES_DIR_ID = "paramFilesDir";

	/**
	 * The paths of the properties files. Each file path must be relative to the
	 * paramFilesDir specified directory. ";" (no space) separated.
	 * Can be either relative to 'paramFilesDir' directory or absolute path.
	 */
	private static final String PARAM_FILES_PATH_ID = "paramFilesPath";

	/**
	 * Properties manager where parameters and their value are stored.
	 * <p>
	 * All parameters used to run a scenario can be defined in a properties file.
	 * That allow easy definition and exchanges among testers and test environments.
	 * </p><p>
	 * The manager supports nested properties files definition. If such nested files are
	 * defined, it checks whether any property is overridden. If so, depending on the
	 * <code>duplicationMode</code> property value (@see {@link ScenarioParametersFile})
	 * it can either stops the scenario execution by raising an error or displays a simple warning
	 * in the console showing where the properties have been overridden and whitch
	 * strategy has been used when encountering such case (override or ignore).
	 * </p><p>
	 * The properties file paths can be specified using {@link #PARAM_FILES_DIR_ID}
	 * and {@link #PARAM_FILES_PATH_ID} System properties.
	 * </p>
	 * @see ScenarioParametersManager
	 */
	private static final ScenarioParametersManager PARAMETERS_MANAGER;
	static {
		// Initialize debug
		debugOpen();

		try {
			// Get files pathes
			String filesPath = System.getProperty(PARAM_FILES_PATH_ID);
			if (filesPath == null) {
				// No files was specified, hence it will only use System properties
				PARAMETERS_MANAGER = null;
			} else {
				// Initialize scenario root parameters
				PARAMETERS_MANAGER = new ScenarioParametersManager();

				// Get root dir (use 'user.dir' system property as default value).
				String rootDirString = System.getProperty(PARAM_FILES_DIR_ID, System.getProperty("user.dir"));

				// Initialize scenario properties through its manager
				PARAMETERS_MANAGER.init(rootDirString, filesPath);
			}
		}
		catch (Throwable th) {
			debugClose();
			throw th;
		}
	}

	/**
	 * Parameter telling which directory to use to put log of server error messages.
	 * <p>
	 * Name: <code>"server.errors.file.name"</code><br>
	 * Value: <code>String</code>, a valid file name matching the OS on which you're running the BVT test<br>
	 * Usage: <code>-Dserver.errors.file.name=my_server_error_file.log</code> in the VM Arguments field of the launch configuration.
	 * </p>
	 *
	 */
	public final static String SERVER_ERRORS_FILE_NAME = getParameterValue("server.errors.file.name", "logErrors_" + COMPACT_DATE_STRING + ".log");

	/**
	 * Flag to tell framework to use environment variables when looking for a property value.
	 * <p>
	 * Name: <code>"use.env.variables"</code><br>
	 * Value: <code>boolean</code><br>
	 * Default: <code>true</code> which means that by default environment variables are used when looking for properties value<br>
	 * Usage: <code>-Duse.env.variables=false</code> in the VM Arguments field of the launch configuration.
	 * </p>
	 *
	 */
	public final static boolean USE_ENV_VARIABLES = getParameterBooleanValue("use.env.variables", true);
	private static final Map<String, String> ENV_VARIABLES = System.getenv();

	/*
	 * Stream to store debug information.
	 */
	private static PrintWriter LOG_WRITER;
	private static StringWriter STR_WRITER;

	/*
	 * Indentations for debug print purposes.
	 */
	private final static String ONE_INDENT_TAB_WITH_PREFIX = "\t- ";
	private final static String TWO_INDENT_TAB_WITH_PREFIX = "\t\t+ ";
	private final static String THREE_INDENT_TAB_WITH_PREFIX = "\t\t\t* ";

/**
 * Close the debug stream.
 */
public static void debugClose() {
	if (DEBUG_DIRECTORY != null) {
		LOG_WRITER.println("**********  Close Debug Session: "+COMPACT_DATE_STRING+"  ********");
		LOG_WRITER.close();
		if (STR_WRITER == null) {
			if (DEBUG) {
				System.out.println();
				System.out.print("Debug information have been written to '");
				System.out.print(DEBUG_DIRECTORY);
				System.out.print(File.separator);
				System.out.print(DEBUG_LOG_FILE_NAME);
				System.out.println("'");
			} else {
				System.out.println();
				System.out.print("WARNING: No debug information written due argument debug="+DEBUG);
			}
		} else {
			System.out.println();
			System.out.println("**********  DEBUG INFORMATION **********");
			System.out.println(STR_WRITER.toString());
		}
	}
}

/**
 * Open debug stream.
 */
public static void debugOpen() {
	if (DEBUG_DIRECTORY == null) {
		LOG_WRITER = new PrintWriter(System.out, false);
	} else {
		// Close previous file if any
		if (LOG_WRITER != null) {
			LOG_WRITER.close();
			LOG_WRITER = null;
			STR_WRITER = null;
		}
		// Create directory for debug file
		File dir = createDir(DEBUG_DIRECTORY);
		// Open debug file for writing
		if (dir != null) {
			File file = new File(dir, DEBUG_LOG_FILE_NAME);
			try {
				LOG_WRITER = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file, false)), false);
				LOG_WRITER.println("**********  Open Debug Session: "+COMPACT_DATE_STRING+"  ********");
			}
			catch (IOException e) {
				System.err.println("Cannot create stream for log: " + e.getMessage());
			}
		}
		// If file was not opened, then use a simple string instead.
		// In that case, info will be written in the console at the end of the execution
		if (LOG_WRITER == null) {
			STR_WRITER = new StringWriter();
			LOG_WRITER = new PrintWriter(STR_WRITER);
			LOG_WRITER.println("**********  Open Debug Session: "+COMPACT_DATE_STRING+"  ********");
		}
	}
}

/**
 * Print a text in the debug stream.
 *
 * @param text The line to print in the stream.
 */
public static void debugPrint(final String text) {
	if (!DEBUG) return;
	LOG_WRITER.print(formatDebugLine(text));
	if (DEBUG_DIRECTORY == null) {
		LOG_WRITER.flush();
	}
}

/**
 * Print a new line in the debug stream when entering a method.
 */
public static void debugPrintEnteringMethod() {
	if (!DEBUG) return;
	debugPrint(DEBUG_ENTERING_METHOD_TEXT);
	debugPrintln("():");
}

/**
 * Print a new line in the debug stream when entering a method.
 *
 * @param params The method parameters to print
 */
public static void debugPrintEnteringMethod(final Object... params) {
	if (!DEBUG) return;
	debugPrint(DEBUG_ENTERING_METHOD_TEXT);
	int i=0;
	for (Object param: params) {
		if (i==0) {
			debugPrint("("+param);
		} else if ((i % 2) == 0) {
			debugPrint(", "+param);
		} else {
			debugPrint("="+param);
		}
		i++;
	}
	debugPrintln("):");
}

/**
 * Print only meaningful element of an exception statck trace
 *
 * @param t The exception
 */
public static void debugPrintException(final Throwable t) {
	if (!DEBUG) return;
	StringBuilder builder = new StringBuilder(getClassSimpleName(t.getClass())).append(": ");
	final String message = t.getMessage();
	if (message == null) {
		builder.append("no message");
	} else {
		int idx = message.indexOf('\n');
		if (idx < 0) {
			builder.append(message);
		} else {
			builder.append(message, 0, idx);
		}
	}
	debugPrintln(builder.toString());
	debugPrintStackTrace(t.getStackTrace(), 1);
}

/**
 * Debug method to print expected strings of a given kind of HTML element.
 */
public static void debugPrintExpectedStrings(final String kind, final String status, final String... strings) {
	if (!DEBUG) return;
	int length = strings.length;
	if (length == 1) {
		debugPrintln("		+ expecting following " + kind + " to be " + status + ": \"" + strings[0] + "\"");
	} else {
		debugPrintln("		+ expecting one of following " + kind + "s to be " + status + ":");
		for (int i = 0; i < length; i++) {
			debugPrintln("			" + (i + 1) + ") \"" + strings[i] + "\"");
		}
	}
}

/**
 * Print a new line in the debug stream.
 */
public static void debugPrintln() {
	if (!DEBUG) return;
	LOG_WRITER.println();
	if (DEBUG_DIRECTORY == null) {
		LOG_WRITER.flush();
	}
}

/**
 * Print a line in the debug stream.
 *
 * @param text The line to print to the stream.
 */
public static void debugPrintln(final String text) {
	if (!DEBUG) return;
	if (LOG_WRITER != null) {
		LOG_WRITER.println(formatDebugLine(text));
		if (DEBUG_DIRECTORY == null) {
			LOG_WRITER.flush();
		}
	}
}

/**
 * Print a line in the debug stream.
 *
 * @param text The line to print in the stream.
 */
public static void debugPrintln(final String... text) {
	if (!DEBUG) return;
	for (String str: text) {
		LOG_WRITER.print(str);
	}
	LOG_WRITER.println();
	if (DEBUG_DIRECTORY == null) {
		LOG_WRITER.flush();
	}
}

/**
 * Print an indented line in the debug stream.
 *
 * @param text The line to print in the stream.
 */
public static void debugPrintln(final String text, final int indent) {
	printIndent(indent);
	debugPrintln(text);
}

/**
 * Print only meaningful element of the given stack trace for the caller
 *
 * @param tabs The number of tabs to indent each element
 */
public static void debugPrintStackTrace(final int tabs) {
	debugPrintStackTrace(new Exception().getStackTrace(), tabs);
}

/**
 * Print only meaningful element of the given stack trace
 *
 * @param elements The full stack trace elements
 */
public static void debugPrintStackTrace(final StackTraceElement[] elements) {
	debugPrintStackTrace(elements, 0);
}

/**
 * Print only meaningful element of the given stack trace
 *
 * @param elements The full stack trace elements
 * @param tabs The number of tabs to indent each element
 */
public static void debugPrintStackTrace(final StackTraceElement[] elements, final int tabs) {
	if (!DEBUG) return;
	for (StackTraceElement element : elements) {
		String elementClassName = element.getClassName();
		if (elementClassName.startsWith("com.ibm") && !elementClassName.startsWith("com.ibm.bear.qa.spot.core.scenario.Scenario")) {
			debugPrintln(element.toString(), tabs);
		}
	}
}

/**
 * Returns a string to display the elapsed time since the given start point.
 *
 * @param start The start of the time measure.
 * @return The elapsed time as a human readable {@link String}.
 */
public static String elapsedTimeString(final long start) {
	return timeString(getElapsedTime(start));
}

/**
 * Get the elapsed time since the given start point.
 *
 * @param start The start of the time measure.
 * @return The elapsed time as a long.
 */
public static long getElapsedTime(final long start) {
	return System.currentTimeMillis()-start;
}

/**
 * Format a given line before being written to debug output stream.
 * <p>
 * Check to see if this is formed like a typical first line, e.g., starting with
 * {@value #DEBUG_ENTERING_METHOD_INDENTATION}
 * If so, add some extra debug information.
 * </p>
 * @param line The line to print
 */
private static String formatDebugLine(final String line) {
	// Check whether the line is starting with appropriate prefix
	if (line.startsWith(DEBUG_ENTERING_METHOD_INDENTATION)) {
		// Get the Class.method() name
		StackTraceElement[] stackElements = whoCallsMe();
		if (stackElements.length > 0) {
			String classMethod = getClassSimpleName(stackElements[0].getClassName()) + "." + stackElements[0].getMethodName();
			// Format the line
			if (line.equals(DEBUG_ENTERING_METHOD_TEXT)) {
				return String.format(DEBUG_ENTERING_METHOD_TEXT, classMethod);
			}
			return DEBUG_ENTERING_METHOD_INDENTATION + SPACE_STRING + classMethod + ": "+line.substring(DEBUG_ENTERING_METHOD_INDENTATION_LENGTH).trim();
		}
	}
	return line;
}

/**
 * Return the class name without package prefix.
 *
 * @return the simple class name as a String.
 */
public static String getClassSimpleName(final Class<?> clazz) {
	return getClassSimpleName(clazz.getName());
}

/**
 * Return the class name without package prefix.
 *
 * @return the simple class name as a String.
 */
public static String getClassSimpleName(final String className) {
	String classSimpleName = className;
	int lastDot = classSimpleName.lastIndexOf('.');
	if (lastDot != -1) {
		classSimpleName = classSimpleName.substring(lastDot + 1);
	}
	return classSimpleName;
}

/**
 * Return the given environment variable value.
 *
 * @param name The name of the variable
 * @return The environment variable value if found, <code>null</code> otherwise
 */
public static String getEnvVariableValue(final String name) {
	return getSimilarEnvVariableValue(name, false);
}


/**
 * Return the given or similar environment variable value.
 * <p>
 * This method can extend the search to similar name of environment variables.<br>
 * This is due to the fact that dot character ('.') is not allowed for Linux environment
 * variable names and names are not case sensitive for Windows environment
 * variable names.<br>
 * Hence, this method can still return a value even if the name of the environment
 * variable is not exactly the same than the given property name.
 * </p><p>
 * For example, a value will be returned for <code>foo.bar</code> property if:
 * <ol>
 * <li>on Windows system: environment variable <code>FOO.BAR</code> is defined</li>
 * <li>on Linux system: environment variable <code>foo_bar</code> is defined</li>
 * </ol>
 * </p>
 * @param name The name of the variable
 * @param searchForSimilarName Flag to tell whether search can be extended to similar names
 * @return The environment variable value if found, <code>null</code> otherwise
 */
protected static String getSimilarEnvVariableValue(final String name, final boolean searchForSimilarName) {
	String envValue = ENV_VARIABLES.get(name);
	if (envValue == null && searchForSimilarName) {
		switch (getOsName()) {
			case "win":
				envValue = ENV_VARIABLES.get(name.toUpperCase());
				break;
			case "lnx":
				if (name.indexOf('.') > 0) {
					envValue = getSimilarEnvVariableValue(name.replaceAll("\\.", "_"), false);
				}
				break;
			default:
				break;
		}
	}
	return envValue;
}

/**
 * Return a list of strings from the given objects array.
 * <p>
 * Note that <code>null</code> slots are automatically skipped,
 * hence the return list might have not the same number than provided
 * array.
 * </p>
 * @param array The objects array
 * @return The strings list
 */
public static List<String> getListFromArray(final Object[] array) {
	List<String> list = new ArrayList<>();
	if (array.length > 0) {
		for (Object obj: array) {
			if (obj != null) {
				list.add(obj.toString());
			}
		}
	}
	return list;
}

/**
 * Return the current OS type.
 *
 * @return The OS type
 */
public static String getOsName() {
	String osName = System.getProperty("os.name");
	if (osName.toLowerCase().startsWith("windows")) {
		return "win";
	}
	if (osName.equalsIgnoreCase("linux")) {
		return "lnx";
	}
	if (osName.toLowerCase().contains("mac")) {
		return "mac";
	}
	return "unknown";
}

/**
 * Return the boolean value from the System property value set in
 * the launch config.
 *
 * @param name The parameter name
 * @return The value as an <code>boolean</code> corresponding to the system
 * property or <code>false</code> if it is not defined.
 */
public static boolean getParameterBooleanValue(final String name) {
	return getParameterBooleanValue(name, false);
}

/**
 * Return the boolean value from the System property value set in
 * the launch config.
 *
 * @param name The parameter name
 * @param defaultValue The value returned if the system property is not defined.
 * @return The value as an <code>boolean</code> corresponding to the system
 * property or the default value if it is not defined.
 */
public static boolean getParameterBooleanValue(final String name, final boolean defaultValue) {
	String parameterValue = getParameterValue(name);
	if (parameterValue == null) return defaultValue;
	return parameterValue.equals("true");
}

/**
 * Return the double value from the System property value set in
 * the launch config.
 *
 * @param name The parameter name
 * @return The value as an <code>double</code> corresponding to the system
 * property or the default value if it is not defined or if the corresponding system
 * property does not define a valid double.
 */
public static double getParameterDoubleValue(final String name) {
	return getParameterDoubleValue(name, 1.0);
}

/**
 * Return the double value from the System property value set in
 * the launch config.
 *
 * @param name The parameter name
 * @param defaultValue The value returned if the system property is not defined.
 * @return The value as an <code>double</code> corresponding to the system
 * property or the default value if it is not defined or if the corresponding system
 * property does not define a valid double.
 */
public static double getParameterDoubleValue(final String name, final double defaultValue) {
	String parameterValue = getParameterValue(name);
	if (parameterValue == null || parameterValue.trim().isEmpty()) return defaultValue;
	try {
		return Double.parseDouble(parameterValue);
	}
	catch (NumberFormatException nfe) {
		// if property is not a valid integer value, then keep the default value
		System.err.println("The specified value for parameter '"+name+"' is not a valid integer! ("+nfe+")");
		System.err.println(defaultValue+" default value will be used instead...");
	}
	return defaultValue;
}

/**
 * Return the integer value from the System property value set in
 * the launch config.
 *
 * @param name The parameter name
 * @return The value as an <code>int</code> corresponding to the system
 * property or <code>0</code> if it is not defined.
 * @throws NumberFormatException If the corresponding system property
 * does not define a valid integer.
 */
public static int getParameterIntValue(final String name) {
	return getParameterIntValue(name, 0);
}

/**
 * Return the integer value from the System property value set in
 * the launch config.
 *
 * @param name The parameter name
 * @param defaultValue The value returned if the system property is not defined.
 * @return The value as an <code>int</code> corresponding to the system
 * property or the default value if it is not defined or if the corresponding system
 * property does not define a valid integer.
 */
public static int getParameterIntValue(final String name, final int defaultValue) {
	String parameterValue = getParameterValue(name);
	if (parameterValue == null || parameterValue.trim().isEmpty()) return defaultValue;
	try {
		return Integer.parseInt(parameterValue);
	}
	catch (NumberFormatException nfe) {
		// if property is not a valid integer value, then keep the default value
		System.err.println("The specified value for parameter '"+name+"' is not a valid integer! ("+nfe+")");
		System.err.println(defaultValue+" default value will be used instead...");
	}
	return defaultValue;
}

/**
 * Return the string value from the first defined System property from the list
 * set in the launch config.
 *
 * @param names A list of possible parameter names
 * @return The value as a {@link String} corresponding to the first defined
 * system property defined or <code>null</code> if none was found.
 */
public static String getParametersValue(final String... names) {
	for (String name: names) {
		String value = getProperty(name);
		if (value != null) {
//			if (DEBUG) printReadParameter(name, value);
			return value;
		}
	}
	return null;
}

/**
 * Return the parameter string value from the System property set in
 * the launch config.
 *
 * @param name The parameter name
 * @return The value as a {@link String} corresponding to the system property
 * or <code>null</code> if the system property is not defined.
 */
public static String getParameterValue(final String name) {
	return getParameterValue(name, null);
}

/**
 * Return the parameter string value from the System property set in
 * the launch config.
 * <p>
 * Note that the property is expected to be defined. In case it's not then
 * an error is raised.
 * </p>
 * @param name The parameter name
 * @return The value as a {@link String} corresponding to the system property
 * @throws ScenarioFailedError if the value corresponding to the system
 * property is <code>null</code>
 */
public static String getParameterMandatoryValue(final String name) throws ScenarioFailedError {
	String value = getParameterValue(name, null);
	if (value == null) {
		throw new ScenarioFailedError("Required parameter " + name + " is null!");
	}
	return value;
}

/**
 * Return the parameter string value from the System property set in
 * the launch config.
 *
 * @param name The parameter name
 * @param defaultValue The value returned if the parameter is not defined.
 * @return The value as a {@link String} corresponding to the system property
 * or <code>defaultValue</code> if the system property is not defined.
 */
public static String getParameterValue(final String name, final String defaultValue) {
	String value = getProperty(name);
	if (value == null) return defaultValue;
//	if (DEBUG) printReadParameter(name, value);
	return value;
}

/**
 * Return the parameter string value from the System property set in
 * the launch config.
 *
 * @param name The parameter name
 * @param defaultValue The value returned if the parameter is not defined.
 * @return The value as a {@link String} corresponding to the system property
 * or <code>defaultValue</code> if the system property is not defined.
 */
public static String[] getParameterValues(final String name, final String defaultValue) {
	String value = getProperty(name);
	if (value == null) {
		value = defaultValue;
	} else {
//		if (DEBUG) printReadParameter(name, value);
	}
	return value.split(",");
}


/**
 * Return the value of the the given property.
 * <p>
 * Try first to get it from {@link System} properties. If not defined, then try
 * to get it from {@link #PARAMETERS_MANAGER}. If not defined and use of
 * environment variables is allowed, then try to look for an environment variable
 * with similar name. If still not defined then return <code>null</code>.
 * </p>
 * @param name The parameter name
 * @return The string corresponding to the parameter value or <code>null</code>
 * if the parameter is not defined.
 */
private static String getProperty(final String name) {
	String value = System.getProperty(name);
	if (value != null) return value;
	if (PARAMETERS_MANAGER != null) {
		value = PARAMETERS_MANAGER.getProperty(name);
	}
	if (value == null && USE_ENV_VARIABLES) {
		value = getSimilarEnvVariableValue(name, true);
	}
	return value;
}

/**
 * Get the Selenium build and version information.
 *
 * @return Selenium build and version information.
 */
public static String getSeleniumVersion() {
	WebDriverException exception = new WebDriverException("info");
	return "Selenium " + exception.getBuildInformation();
}

/**
 * Return the given booleans list as a flat text separated with comma.
 *
 * @param booleans The booleans list
 * @param trueString The text for true value
 * @param falseString The text for false value
 * @return The text as as {@link String}
 */
public static String getTextFromBooleans(final boolean[] booleans, final String trueString, final String falseString) {
	return getTextFromBooleans(booleans, ",", trueString, falseString);
}

/**
 * Return the given booleans list as a flat text separated with given separator.
 *
 * @param booleans The booleans list
 * @param separator The separator in the text list
 * @param trueString The text for true value
 * @param falseString The text for false value
 * @return The text as as {@link String}
 */
public static String getTextFromBooleans(final boolean[] booleans, final String separator, final String trueString, final String falseString) {
	if (booleans == null) {
		return "null";
	}
	final StringBuilder builder = new StringBuilder();
	boolean first = true;
	for (boolean bool: booleans) {
		if (!first) builder.append(separator);
		builder.append(bool?trueString:falseString);
		first = false;
	}
	return builder.toString();
}

/**
 * Return the given objects list as a flat text separated with comma.
 *
 * @return The text as as {@link String}
 */
public static String getTextFromList(final List<?> object) {
	return getTextFromList(object, ", ");
}

/**
 * Return the given objects list as a flat text separated with the given separator.
 *
 * @param strings The list of strings
 * @param separator String to use to separate strings
 * @return The text as as {@link String}
 */
public static String getTextFromList(final List<?> strings, final String separator) {
	Object[] array = new Object[strings.size()];
	strings.toArray(array);
	return getTextFromList(array, separator);
}

/**
 * Return the given objects list as a flat text separated with comma.
 *
 * @return The text as as {@link String}
 */
public static String getTextFromList(final Set<?> strings) {
	return getTextFromList(new ArrayList<Object>(strings), ", ");
}

/**
 * Return the given objects list as a flat text separated with comma.
 *
 * @return The text as as {@link String}
 */
public static String getTextFromList(final Object[] strings) {
	return getTextFromList(strings, ", ");
}

/**
 * Return the given objects list as a flat text separated with the given separator.
 *
 * @param objects The list of objects
 * @param separator String to use to separate strings
 * @return The text as as {@link String}
 */
public static String getTextFromList(final Object[] objects, final String separator) {
	if (objects == null) {
		return "null";
	}
	final StringBuilder builder = new StringBuilder();
	boolean first = true;
	for (Object obj: objects) {
		if (!first) builder.append(separator);
		builder.append(obj);
		first = false;
	}
	return builder.toString();
}

/**
 * Tells whether the current OS is a Mac or not.
 *
 * @return <code>true</code> if OS is Mac, <code>false</code> otherwise
 */
public static boolean isMacOs() {
	return getOsName().equals("mac");
}

/**
 * Tells whether the current OS is a Windows or not.
 *
 * @return <code>true</code> if OS is Windows, <code>false</code> otherwise
 */
public static boolean isWinOs() {
	return getOsName().equals("win");
}

/**
 * Pause during the given milli-seconds time.
 *
 * @param millisecs
 */
public static void pause(final long millisecs) {
	try {
		Thread.sleep(millisecs);
	} catch (@SuppressWarnings("unused") InterruptedException ie) {
		// skip
	}
}

/**
 * Print a text to the console. The output is done iff the {@link #PRINT} flag
 * is set.
 *
 * @param text The text to print to the console.
 */
public static void print(final Object text) {
	if (PRINT) System.out.print(text);
	if (DEBUG && (!PRINT || DEBUG_DIRECTORY != null)) debugPrint(text.toString());
}

/**
 * Print only meaningful element of an exception statck trace
 *
 * @param t The exception
 */
public static void printException(final Throwable t) {
	StringBuilder builder = new StringBuilder(getClassSimpleName(t.getClass())).append(": ");
	final String message = t.getMessage();
	if (message != null) {
		int idx = message.indexOf('\n');
		if (idx < 0) {
			builder.append(message);
		} else {
			builder.append(message, 0, idx);
		}
	}
	println(builder.toString());
	printStackTrace(t.getStackTrace(), 1);
}

private static void printIndent(final int indent) {
	switch (indent) {
		case 1:
			LOG_WRITER.print(ONE_INDENT_TAB_WITH_PREFIX);
			break;
		case 2:
			LOG_WRITER.print(TWO_INDENT_TAB_WITH_PREFIX);
			break;
		case 3:
			LOG_WRITER.print(THREE_INDENT_TAB_WITH_PREFIX);
			break;
		default:
			for (int i=0; i<indent; i++) {
				LOG_WRITER.print("\t");
			}
			LOG_WRITER.print("->");
			break;
	}
}

/**
 * Print a empty line to the console. The output is done iff the {@link #PRINT}
 * flag is set.
 */
public static void println() {
	if (PRINT) System.out.println();
	if (DEBUG && (!PRINT || DEBUG_DIRECTORY != null)) debugPrintln();
}

/**
 * Print a text with a new line at the end to the console. The output is done
 * iff the {@link #PRINT} flag is set.
 *
 * @param text The text to print to the console.
 */
public static void println(final Object text) {
	if (PRINT) System.out.println(text);
	if (DEBUG && (!PRINT || DEBUG_DIRECTORY != null)) debugPrintln(String.valueOf(text));
}

//private static void printReadParameter(final String name, final String value) {
//	if (PRINT_PARAMS.isEmpty()) {
//		println("Read parameters while running framework scenario:");
//	}
//	if (!PRINT_PARAMS.contains(name)) {
//		PRINT_PARAMS.add(name);
//		println("	- '"+name+"' value="+value);
//	}
//}

/**
 * Print only meaningful element of the given stack trace for the caller
 *
 * @param tabs The number of tabs to indent each element
 */
public static void printStackTrace(final int tabs) {
	StackTraceElement[] elements = new Exception().getStackTrace();
	printStackTrace(elements, tabs, /*start:*/1);
}

/**
 * Print only meaningful element of the given stack trace.
 *
 * @param elements The full stack trace elements
 * @param tabs The number of tabs to indent each element
 */
public static void printStackTrace(final StackTraceElement[] elements, final int tabs) {
	printStackTrace(elements, tabs, /*start:*/0);
}

/**
 * Print only meaningful element of the given stack trace starting from given slot.
 *
 * @param elements The full stack trace elements
 * @param tabs The number of tabs to indent each element
 * @param start The index of the first element in the stack trace to start with in the print
 */
public static void printStackTrace(final StackTraceElement[] elements, final int tabs, final int start) {
	print(cleanStackTrace(elements, tabs, start));
}

/**
 * Clean given stack trace with only framework meaningful elements.
 *
 * @param elements The full stack trace elements
 * @param tabs The number of tabs to indent each element
 * @param start The index of the first element in the stack trace to start with in the print
 * @return The built string with the stack trace
 */
public static String cleanStackTrace(final StackTraceElement[] elements, final int tabs, final int start) {
	int length = elements.length;
	StringBuilder builder = new StringBuilder();
	for (int i=start; i<length; i++) {
		StackTraceElement element = elements[i];
		String elementClassName = element.getClassName();
		if (elementClassName.startsWith("com.ibm") && !elementClassName.startsWith("com.ibm.bear.qa.spot.core.scenario.Scenario")) {
			for (int t=0; t<tabs; t++) {
				builder.append('\t');
			}
			builder.append(element.toString()).append(LINE_SEPARATOR);
		}
	}
	return builder.toString();
}

/**
 * Print the starting point for the given test case.
 *
 * @param stepName The scenario step
 */
public static void printStepStart(final String stepName) {
	StringBuilder builder = new StringBuilder(LINE_SEPARATOR)
	    .append("Starting execution of BVT test case '")
	    .append(stepName)
	    .append("' at ")
	    .append(TIME_FORMAT.format(new Date(System.currentTimeMillis())))
	    .append(LINE_SEPARATOR)
	    .append("======================================");
	final int length = stepName.length();
	for (int i = 0; i < length; i++) {
		builder.append('=');
	}
	final String text = builder.toString();
	if (PRINT) {
		System.out.println(text);
	}
	if (DEBUG && (!PRINT || DEBUG_DIRECTORY != null)) {
		debugPrintln(text);
	}
}

/**
 * Sleep during the given seconds time.
 *
 * @param seconds The number of seconds to sleep.
 */
public static void sleep(final int seconds) {
	try {
		Thread.sleep(seconds * 1000);
	} catch (@SuppressWarnings("unused") InterruptedException ie) {
		// skip
	}
}

/**
 * Returns a string to display the given time as a duration
 * formatted as:
 *	<ul>
 *	<li>"XXXms" if the duration is less than 0.1s (e.g. "43ms")</li>
 *	<li>"X.YYs" if the duration is less than 1s (e.g. "0.43s")</li>
 *	<li>"XX.Ys" if the duration is less than 1mn (e.g. "14.3s")</li>
 *	<li>"XXmn XXs" if the duration is less than 1h (e.g. "14mn 3s")</li>
 *	<li>"XXh XXmn XXs" if the duration is over than 1h (e.g. "1h 4mn 3s")</li>
 *	</ul>
 *
 * @param time The time to format as a long.
 * @return The time as a human readable readable {@link String}.
 */
public static String timeString(final long time) {
	NumberFormat format = NumberFormat.getInstance();
	format.setMaximumFractionDigits(1);
	StringBuffer buffer = new StringBuffer();
	if (time == 0) {
		// print nothing
	} if (time < 100) { // less than 0.1s
		buffer.append(time);
		buffer.append("ms"); //$NON-NLS-1$
	} else if (time < 1000) { // less than 1s
		if ((time%100) != 0) {
			format.setMaximumFractionDigits(2);
		}
		buffer.append(format.format(time/1000.0));
		buffer.append("s"); //$NON-NLS-1$
	} else if (time < ONE_MINUTE) {  // less than 1mn
		if ((time%1000) == 0) {
			buffer.append(time/1000);
		} else {
			buffer.append(format.format(time/1000.0));
		}
		buffer.append("s"); //$NON-NLS-1$
	} else if (time < ONE_HOUR) {  // less than 1h
		buffer.append(time/ONE_MINUTE).append("mn "); //$NON-NLS-1$
		long seconds = time%ONE_MINUTE;
		buffer.append(seconds/1000);
		buffer.append("s"); //$NON-NLS-1$
	} else {  // more than 1h
		long h = time / ONE_HOUR;
		buffer.append(h).append("h "); //$NON-NLS-1$
		long m = (time % ONE_HOUR) / ONE_MINUTE;
		buffer.append(m).append("mn "); //$NON-NLS-1$
		long seconds = m%ONE_MINUTE;
		buffer.append(seconds/1000);
		buffer.append("s"); //$NON-NLS-1$
	}
	return buffer.toString();
}

/**
 * Return a list of {@link String}s from a list of {@link WebElement}s.
 *
 * @param elements the list of web elements to extract text from.
 * @return A list of Strings representing the text of the WebElements
 */
public static List<String> toStrings(final List<? extends WebElement> elements) {
	return toStrings(elements, false);
}

/**
 * Return a list of {@link String}s from a list of {@link WebElement}s.
 *
 * @param elements the list of web elements to extract text from.
 * @param filterEmpty true if you want the list to be filtered of any null or "" elements
 * @return A list of Strings representing the text of the WebElements
 */
public static List<String> toStrings(final List<? extends WebElement> elements, final boolean filterEmpty) {
	List<String> strings = new ArrayList<String>(elements.size());
	for (WebElement webElement : elements) {
		String string = webElement.getText();
		if (!filterEmpty || (string != null && string.length() > 0)) {
			strings.add(string);
		}
	}
	return strings;
}

/**
 * Wait the given amount of seconds and print a dot in the console every 10 seconds.
 * <p>
 * For example, if <code>seconds=60</code> then the method will wait one minute
 * while printing a dot in the console every 10 seconds.
 * </p>
 * @param seconds The total number of seconds to wait before returning
 */
public static void waitSeveralSeconds(final int seconds) {
	waitSeveralSeconds(seconds, 10);
}

/**
 * Wait the given amount of seconds and print a dot in the console
 * every given period of seconds.
 *
 * @param seconds The total number of seconds to wait before returning
 * @param period The period of dot printing. No dot are printed if this number is less or equals to 0
 */
public static void waitSeveralSeconds(final int seconds, final int period) {
	if (seconds == 0) return;
	System.out.print("		-> wait "+seconds+" second");
	if (seconds > 1) {
		System.out.print('s');
	}
	for (int i=0, p=0; i<seconds; i++) {
		sleep(1);
		if (period > 0 && ++p >= period) {
			System.out.print(".");
			System.out.flush();
			p=0;
		}
	}
	System.out.println();
}

/**
 * Return the name of the caller from which this method is used.
 */
public static StackTraceElement whoAmI() {
	StackTraceElement[] elements = new Exception().getStackTrace();
	return elements[1]; // Skip first element which is the ScenarioUtils current method
}

/**
 * Return the simple name of the caller from which this method is used.
 */
public static String whoAmIAsSimpleString() {
	StackTraceElement whoCallMe = new Exception().getStackTrace()[1];
	String className = whoCallMe.getClassName();
	className = className.substring(className.lastIndexOf('.')+1);
	return className+"."+whoCallMe.getMethodName()+"()";
}

/**
 * Return the name of the caller from which this method is used.
 */
public static StackTraceElement[] whoCallsMe() {
	StackTraceElement[] elements = new Exception().getStackTrace();
	StackTraceElement[] callers = new StackTraceElement[elements.length];
	int count = 0, length = 0;
	for (StackTraceElement element : elements) {
		if (count < 2) {
			// Skip the first two elements: ScenarioUtils method and calling
			// method
		} else {
			String elementClassName = element.getClassName();
			if (elementClassName.startsWith("com.ibm") && !elementClassName.startsWith("com.ibm.bear.qa.spot.core.scenario.Scenario")) {
				callers[length++] = element;
			}
		}
		count++;
	}
	System.arraycopy(callers, 0, callers = new StackTraceElement[length], 0, length);
	return callers;
}
}
