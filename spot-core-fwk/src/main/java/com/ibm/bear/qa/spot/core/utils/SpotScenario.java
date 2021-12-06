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
package com.ibm.bear.qa.spot.core.utils;

/**
 * Annotation to identify a framework scenario.
 * <p>
 * This annotation accept following argument:
 * <ul>
 * <li><b>type</b>: The framework scenario type, all these values are exclusive.
 * <ul>
 * <li><b>"private"</b>: Used for a valid framework scenario supposed to be run in private by the team who created it. This is the default value</li>
 * <li><b>"pipeline"</b>: Used for a valid framework scenario supposed to be run in the CT pipeline.</li>
 * <li><b>"monitored"</b>: Used for a valid framework scenario supposed to be run in the CT pipeline <b>and monitored</b>. That means its results will be reported in a dashboard.</li>
 * <li><b>"perfs"</b>: Used for a valid framework scenario used for performances purpose.</li>
 * <li><b>"demo"</b>: Used for a valid framework scenario created for demonstration purpose.</li>
 * </ul>
 * </li>
 * <li><b>runs</b>: The number of runs in a development CT pipeline. This argument only has a meaning when scenario type is either <code>"pipeline"</code> or <code>"monitored"</code>.</li>
 * <li><b>mainenance</b>: The number of runs in a maintenance CT pipeline. This argument only has a meaning when scenario type is <code>"monitored"</code>.</li>
 * </ul>
 * </li>
 * </ul>
 * </p><p>
 * So, this annotation can be used in four different ways:
 * <ul>
 * <li><code>&#064;SpotScenario</code>: Valid framework scenario which does not run in CT pipeline.</li>
 * <li><code>&#064;SpotScenario("monitored")</code>: Valid framework scenario run once in CT pipeline and <b>monitored</b>.</li>
 * <li><code>&#064;SpotScenario(value="monitored", runs=2)</code>: Valid framework scenario run twice in CT pipeline, both runs are <b>monitored</b>.</li>
 * <li><code>&#064;SpotScenario("pipeline")</code>: Valid framework scenario run once in CT pipeline but <b>NOT monitored</b>.</li>
 * <li><code>&#064;SpotScenario("demo")</code>: Valid framework scenario class run only for demonstration purpose (ie. not in CT pipeline).</li>
 * <li><code>&#064;SpotScenario("perfs")</code>: Valid framework scenario class run only for performances purpose (ie. not in CT pipeline).</li>
 * </ul>
 * </p>
 */
public @interface SpotScenario {
	String value() default "pipeline";
	int runs() default 1;
	boolean maintenance() default false;
}
