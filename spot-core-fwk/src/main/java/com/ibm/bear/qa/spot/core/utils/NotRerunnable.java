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
 * Annotation to identify a test which cannot be rerunnable.
 * <p>
 * It means that automatic rerun will not occur in case of test failure.<br>
 * It also means that scenario cannot start from such annotated test.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NotRerunnable {

}
