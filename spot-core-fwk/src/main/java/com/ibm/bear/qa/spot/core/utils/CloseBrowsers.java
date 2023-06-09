/*********************************************************************
* Copyright (c) 2012, 2023 IBM Corporation and others.
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
 * Annotation to identify a step or a test on which all opened browsers should be closed.
 * <p>
 * This annotation accept a policy parameter which default is to close browsers
 * at the end of the step or test on which the annotation is defined.
 * </p>
 * @see CloseBrowsersPolicy
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CloseBrowsers {
	CloseBrowsersPolicy value() default CloseBrowsersPolicy.AT_END;
}
