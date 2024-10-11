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
package com.ibm.bear.qa.spot.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to identify a test which blocks other steps or tests.
 * <p>
 * This annotation accept following argument:
 * <ul>
 * <li><b>values</b>: List of references to tests which are blocking the test in which the annotation is used.<br>
 * References should use following pattern: <code>&lt;<i>Step class name</i>&gt;.&lt;<i>Test name</i>&gt;</code></li>
 * <li><b>timeout</b>: The time in seconds to wait for all blockers message to arrive.If not precised, then default value of 15 mlinutes is used.</li>
 * <li><b>fail</b>: Tells whether the test should fail or not in case not all blockers message has arrived before the timeout expires.</li>
 * </ul>
 * </p><p>
 * Examples:
 * <pre>
 * @DependsOn(blockers={"FirstScenario_Step01.test01_DoSomething"})
 * @DependsOn(blockers={"FirstScenario_Step00.test02_DoSomething", "SecondScenario_Step02.test05_DoAnotherThing"})
 * @DependsOn(blockers={"FirstScenario_Step00.test02_DoSomething"}, timeout=60, fail=false)
 * </pre>
 * </p><p>
 * Note that using this annotation on a scenario test assumes that referenced tests exist and
 * that they <b>all</b> have a {@link Blocks} annotation pointing to the this scenario test.
 * Otherwise that can lead to block the scenario execution for at least 15 minutes (or the specified timeout).
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOn {
	String[] blockers();
	int timeout() default 15*60;
	boolean fail() default true;
}
