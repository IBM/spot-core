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
package com.ibm.bear.qa.spot.core.timeout;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.*;

import com.ibm.bear.qa.spot.core.scenario.errors.ScenarioFailedError;
import com.ibm.bear.qa.spot.core.scenario.errors.WaitElementTimeoutError;
import com.ibm.bear.qa.spot.core.web.WebBrowserElement;

/**
 * Abstract class to manage a timeout loop.
 * <p>
 * Currently two kinds of wait are managed (while and until) which allow for each
 * status to wait for the expected condition ({@link #getCondition()}) to become true or false.
 * </p><p>
 * See <code>waitWhile*</code> methods as in {@link WebBrowserElement} class
 * as examples of this class implementation (eg. {@link WebBrowserElement#waitWhileDisplayed(int, boolean)},
 * {@link WebBrowserElement#waitWhileNotDisplayed(int, boolean)}, etc.).
 * </p><p>
 * The loop can be controlled with 3 internal fields:
 * <ul>
 * <li>{@link #duration}: Maximum duration (ie. timeout) for the loop execution (in milliseconds)</li>
 * <li>{@link #pause}: The pause to do between each occurrence of the loop (in milliseconds)</li>
 * <li>{@link #fail}: Flag telling whether to raise a {@link WaitElementTimeoutError} if the timeout is reached or not.
 * In case not, then the called {@link #waitUntil(float)} or {@link #waitWhile(float)} method will return <code>false</code></li>
 * </ul>
 * </p><p>
 * The loop duration is provided while calling wait methods although the pause and fail are provided
 * while creating the instance. An additional field can also be set while creating the instance: the web
 * element on which the condition will be exercised. That typically allow to have predefined subclasses
 * for typical timeout wait operation as {@link SpotDisplayedTimeout} and {@link SpotEnabledTimeout}
 * for timeout on visible and enable status for any web element.
 * </p>
 */
public abstract class SpotAbstractTimeout {

	final protected WebBrowserElement element;
	private int duration;
	final private int pause;
	final private boolean fail;

/**
 * Create a timeout instance with default values.
 * <p>
 * This instance will have following characteristics:
 * <ul>
 * <li>no web element will be used by default</li>
 * <li>it will fail if the timeout is reached while looping</li>
 * <li>the loop will occur every 100 milliseconds</li>
 * </ul>
 */
public SpotAbstractTimeout() {
	this(null, /*fail:*/true, 100);
}

/**
 * Create a timeout instance with default values which won't fail if time expires.
 * <p>
 * This instance will have following characteristics:
 * <ul>
 * <li>no web element will be used by default</li>
 * <li>the loop will occur every 100 milliseconds</li>
 * </ul>
 * </p>
 * @param fail Tells whether to fail after the timeout expires
 */
public SpotAbstractTimeout(final boolean fail) {
	this(null, fail, 100);
}

/**
 * Create a timeout instance telling whether it should fail or not
 * and which pause to apply between each loop occurrence.
 *
 * @param fail Tells whether to fail after the timeout expires
 * @param pause The time in milliseconds to sleep for each waiting loop
 */
public SpotAbstractTimeout(final boolean fail, final int pause) {
	this(null, fail, pause);
}

/**
 * Create a timeout instance for a web element with default values.
 * <p>
 * This instance will have following characteristics:
 * <ul>
 * <li>it will fail if the timeout is reached while looping</li>
 * <li>the loop will occur every 100 milliseconds</li>
 * </ul>
 */
public SpotAbstractTimeout(final WebBrowserElement webElement) {
	this(webElement, /*fail:*/true, 100);
}

/**
 * Create a timeout instance for a web element telling whether it should fail or not.
 * <p>
 * This instance will have following characteristics:
 * <ul>
 * <li>the loop will occur every 100 milliseconds</li>
 * </ul>
 * </p>
 * @param webElement The element concerned by the timeout. Can be <code>null</code>.
 * @param fail Tells whether to fail after the timeout expires
 */
public SpotAbstractTimeout(final WebBrowserElement webElement, final boolean fail) {
	this(webElement, fail, 100);
}

/**
 * Create a timeout instance for a web element telling whether it should fail or not
 * and which pause to apply between each loop occurrence.
 *
 * @param webElement The element concerned by the timeout. Can be <code>null</code>.
 * @param fail Tells whether to fail after the timeout expires
 * @param pause The time in milliseconds to sleep for each waiting loop
 */
public SpotAbstractTimeout(final WebBrowserElement webElement, final boolean fail, final int pause) {
	this.element = webElement;
	this.fail = fail;
	this.pause = pause;
}

/**
 * Method to signify that the timeout has failed.
 * <p>
 * Default failure is just the error with a message telling that the condition failed.
 * The text for condition is provided by {@link #getConditionLabel()} method.
 * </p>
 * @param whileLoop Tells whether the failure occurred in a while loop or not (vs an until loop)
 * @throws ScenarioFailedError The failure, by default a {@link WaitElementTimeoutError} but
 * might be more severe depending on check done in timeout
 */
protected void fail(final boolean whileLoop) throws ScenarioFailedError {
	throw new WaitElementTimeoutError(getFailureMessage(whileLoop));
}

/**
 * Return the result of the condition evaluation.
 *
 * @return The condition result value
 */
abstract protected boolean getCondition();

/**
 * Return the text explaining the condition.
 *
 * @return The condition text
 */
abstract protected String getConditionLabel();

/**
 * Return the failure message to display when tiemout expires.
 *
 * @param whileLoop Tells which kind of loop was tested (until or while)
 * @return The failure message
 */
protected String getFailureMessage(final boolean whileLoop) {
	return "Condition \""+getConditionLabel() + "\" was still " + (whileLoop ? "true" : "false") + " after " + (this.duration/1000) + " seconds, give up.";
}

/**
 * Wait while or until on the condition (see {@link #getCondition()}) is true.
 *
 * @param whileLoop Tells whether the loop will be waiting while (<code>true</code>
 * 	the condition is <code>true</code> or until (<code>false</code>) the condition becomes <code>true</code>
 * @return <code>true</code> if the status turns into the expected state before timeout occurs
 * or <code>false</code> otherwise
 * @throws WaitElementTimeoutError If the status does not turn into the expected state before
 * the timeout occurs and the wait was expecting to fail in such case
 */
boolean wait(final boolean whileLoop) throws WaitElementTimeoutError {
	debugPrintEnteringMethod("whileLoop", whileLoop);
	boolean condition = getCondition();
	debugPrintln("		  -> condition '"+getConditionLabel()+"' is "+condition);
	long start = System.currentTimeMillis();
	long current = start;
	long timeout = start + this.duration;
	long waitDuration = 0L;
	while ((whileLoop && condition) || (!whileLoop && !condition)) {
		current = System.currentTimeMillis();
		if (current > timeout) {
			if (whileLoop) {
				debugPrintln("		  => condition is still true after "+waitDuration+"ms, give up!");
			} else {
				debugPrintln("		  => condition is still false after "+waitDuration+"ms, give up!");
			}
			if (this.fail) {
				fail(whileLoop);
			}
			return false;
		}
		pause(this.pause);
		condition = getCondition();
		waitDuration = System.currentTimeMillis() - start;
		debugPrintln("		  -> condition '"+getConditionLabel()+"' is "+condition+" after "+waitDuration+"ms...");
	}
	if (waitDuration == 0) {
		debugPrintln("		  => no wait as the condition was already "+condition+".");
	} else {
		debugPrintln("		  => it took "+waitDuration+"ms for the condition to become "+condition+".");
	}
	return true;
}

/**
 * Wait until the condition (see {@link #getCondition()}) becomes true.
 *
 * @param time The time value in seconds
 * @return <code>true</code> if the condition becomes true before timeout occurs,
 * <code>false</code> otherwise
 * @throws WaitElementTimeoutError If the condition does not become true before
 * the timeout occurs and a failure is expected in such case
 */
public boolean waitUntil(final float time) throws WaitElementTimeoutError {
	debugPrintEnteringMethod("time", time);
	this.duration = (int) time*1000;
	return wait(false);
}

/**
 * Wait while the condition (see {@link #getCondition()}) is true.
 *
 * @param time The time value in seconds
 * @return <code>true</code> if the condition becomes false before timeout occurs,
 * <code>false</code> otherwise
 * @throws WaitElementTimeoutError If the condition does not become false before
 * the timeout occurs and a failure is expected in such case
 */
public boolean waitWhile(final float time) throws WaitElementTimeoutError {
	debugPrintEnteringMethod("time", time);
	this.duration = (int) time*1000;
	return wait(true);
}
}