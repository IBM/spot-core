/*
* Licensed Materials - Property of IBM
* 5725-B69 5655-Y17 5655-Y31 5724-X98 5724-Y15 5655-V82
* Copyright IBM Corp. 1987, 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/
package com.ibm.bear.qa.spot.template.steps;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.bear.qa.spot.core.utils.FailureBlocker;
import com.ibm.bear.qa.spot.template.scenario.TemplateScenarioStep;
import com.ibm.bear.qa.spot.template.scenario.TemplateScenarioStepRunner;

/**
 * First step of the scenario.
 * <p>
 * </p><p>
 * This class defines following internal API methods:
 * <ul>
 * <li>{@link #test01_FirstTest()}: First test.</li>
 * <li>{@link #test02_SecondTest()}: Second test.</li>
 * </ul>
 * </p>
 */
@RunWith(TemplateScenarioStepRunner.class)
@FailureBlocker
public class TemplateStep01 extends TemplateScenarioStep {

/**
 * First test.
 */
@Test
public void test01_FirstTest() {
}

/**
 * Second test.
 */
@Test
public void test02_SecondTest() {
}
}
