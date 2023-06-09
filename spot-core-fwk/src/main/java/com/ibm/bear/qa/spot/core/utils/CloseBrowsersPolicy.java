/*
* Licensed Materials - Property of IBM
* 5725-B69 5655-Y17 5655-Y31 5724-X98 5724-Y15 5655-V82
* Copyright IBM Corp. 1987, 2022. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/
package com.ibm.bear.qa.spot.core.utils;

/**
 * Annotation policy to close browsers.
 * <p>
 * The constants of this enumerated type describe the various policies for closing
 * browsers depending on the step or the test on which they are defined.
 * </p>
 */
public enum CloseBrowsersPolicy {
	/**
	 * Policy used to close browsers <b>after</b> each test of the step on which
	 * the annotation is defined.
	 * <p>
	 * Note that using this annotation on a test method will be equivalent to use
	 * {@link #AT_END} value.
	 * </p>
	 */
	ON_EACH_TEST,
	/**
	 * Policy used to close browsers <b>before</b> a test or a step on which
	 * the annotation is defined.
	 */
	AT_START,
	/**
	 * Policy used to close browsers <b>after</b> a test or a step on which
	 * the annotation is defined.
	 */
	AT_END,
}
