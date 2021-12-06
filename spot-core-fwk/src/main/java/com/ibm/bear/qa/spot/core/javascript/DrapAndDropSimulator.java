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
package com.ibm.bear.qa.spot.core.javascript;


/**
 * Class to help to simulate HTML5 Drag and drop functionality which is currently
 * not supported by Selenium (last test was done using version 2.51.1).
 * <p>
 * See following Google items related to that issue:
 * @see "https://code.google.com/p/selenium/issues/detail?id=3604"
 * @see "https://code.google.com/p/selenium/issues/detail?id=5149"
 * @see "https://code.google.com/p/selenium/issues/detail?id=6315"
 * </p><p>
 * Following code has been extracted from <b>HTML5DragAndDropSimulator.txt</b>
 * file provided at comment 5 of item 6315.
 * </p>
 */
public class DrapAndDropSimulator {

	/**
	 * Controls all the event simulation required for drag and drop
	 */
	private static final String JAVASCRIPT_EVENT_SIMULATOR = "" +
			/* Creates a drag event */
			"function createDragEvent(eventName, options)\r\n" +
			"{\r\n" +
			"	var event = document.createEvent(\"DragEvent\");\r\n" +
			"	var screenX = window.screenX + options.clientX;\r\n" +
			"	var screenY = window.screenY + options.clientY;\r\n" +
			"	var clientX = options.clientX;\r\n" +
			"	var clientY = options.clientY;\r\n" +
//			"	console.log('createDragEvent('+eventName+'): screenX='+screenX+', screenY='+screenY+', clientX='+clientX+', clientY='+clientY);\r\n" +
			"	var dataTransfer = {\r\n" +
			"		data: options.dragData == null ? {} : options.dragData,\r\n" +
			"		setData: function(eventName, val){\r\n" +
			"			if (typeof val === 'string') {\r\n" +
			"				this.data[eventName] = val;\r\n" +
			"				console.log('	store val ['+val+'] in data of dataTransfer');\r\n" +
			"			}\r\n" +
			"		},\r\n" +
			"		getData: function(eventName){\r\n" +
//			"			console.log('	return data for '+eventName+' of dataTransfer: '+this.data[eventName]);\r\n" +
			"			return this.data[eventName];\r\n" +
			"		},\r\n" +
			"		clearData: function(){\r\n" +
			"			return this.data = {};\r\n" +
			"		},\r\n" +
			"		setDragImage: function(dragElement, x, y) {}\r\n" +
			"	};\r\n" +
			"	var eventInitialized=false;\r\n"+
			"	if (event != null && event.initDragEvent) {\r\n" +
			"		try {\r\n"+
//			"			console.log('	event was not initialized...');\r\n" +
			"			event.initDragEvent(eventName, true, true, window, 0, screenX, screenY, clientX, clientY, false, false, false, false, 0, null, dataTransfer);\r\n" +
			"			event.initialized=true;\r\n"+
			"			eventInitialized=true;\r\n"+
			"		} catch(err) {\r\n"+
			"			// no-op\r\n"+
			"		}\r\n"+
			"	}\r\n"+
			"	if (!eventInitialized) {\r\n"+
			"		event = document.createEvent(\"CustomEvent\");\r\n" +
			"		event.initCustomEvent(eventName, true, true, null);\r\n" +
			"		event.view = window;\r\n" +
			"		event.detail = 0;\r\n" +
			"		event.screenX = screenX;\r\n" +
			"		event.screenY = screenY;\r\n" +
			"		event.clientX = clientX;\r\n" +
			"		event.clientY = clientY;\r\n" +
			"		event.ctrlKey = false;\r\n" +
			"		event.altKey = false;\r\n" +
			"		event.shiftKey = false;\r\n" +
			"		event.metaKey = false;\r\n" +
			"		event.button = 0;\r\n" +
			"		event.relatedTarget = null;\r\n" +
			"		event.dataTransfer = dataTransfer;\r\n" +
			"	}\r\n" +
//			"	console.log('	event: screenX='+event.screenX+', screenY='+event.screenY+', clientX='+event.clientX+', clientY='+event.clientY);\r\n" +
			"	return event;\r\n" +
			"}\r\n" +

			/* Creates a mouse event */
			"function createMouseEvent(eventName, options)\r\n" +
			"{\r\n" +
			"	var event = document.createEvent(\"MouseEvent\");\r\n" +
			"	var screenX = window.screenX + options.clientX;\r\n" +
			"	var screenY = window.screenY + options.clientY;\r\n" +
			"	var clientX = options.clientX;\r\n" +
			"	var clientY = options.clientY;\r\n" +
			"	if (event != null && event.initMouseEvent) {\r\n" +
			"		event.initMouseEvent(eventName, true, true, window, 0, screenX, screenY, clientX, clientY, false, false, false, false, 0, null);\r\n" +
			"	} else {\r\n" +
			"		event = document.createEvent(\"CustomEvent\");\r\n" +
			"		event.initCustomEvent(eventName, true, true, null);\r\n" +
			"		event.view = window;\r\n" +
			"		event.detail = 0;\r\n" +
			"		event.screenX = screenX;\r\n" +
			"		event.screenY = screenY;\r\n" +
			"		event.clientX = clientX;\r\n" +
			"		event.clientY = clientY;\r\n" +
			"		event.ctrlKey = false;\r\n" +
			"		event.altKey = false;\r\n" +
			"		event.shiftKey = false;\r\n" +
			"		event.metaKey = false;\r\n" +
			"		event.button = 0;\r\n" +
			"		event.relatedTarget = null;\r\n" +
			"	}\r\n" +
			"	return event;\r\n" +
			"}\r\n" +

			/* Runs the events */
			"function dispatchEvent(webElement, eventName, event)\r\n" +
			"{\r\n" +
			"	if (webElement.dispatchEvent) {\r\n" +
			"		webElement.dispatchEvent(event);\r\n" +
//			"		console.log('dispatchEvent: dispatchEvent '+eventName+' (type='+event.type+')');\r\n" +
			"	} else if (webElement.fireEvent) {\r\n" +
			"		webElement.fireEvent(\"on\"+eventName, event);\r\n" +
//			"		console.log('dispatchEvent: fireEvent '+eventName+' (type='+event.type+')');\r\n" +
			"	}\r\n" +
			"}\r\n" +

			/* Simulates an individual event */
			"function simulateEventCall(element, eventName, dragStartEvent, options) {\r\n" +
			"	var event = null;\r\n" +
			"	if (eventName.indexOf(\"mouse\") > -1) {\r\n" +
			"		event = createMouseEvent(eventName, options);\r\n" +
			"	} else {\r\n" +
			"		event = createDragEvent(eventName, options);\r\n" +
			"	}\r\n" +
			"	if (dragStartEvent != null) {\r\n" +
			"		event.dataTransfer = dragStartEvent.dataTransfer;\r\n" +
			"	}\r\n" +
			"	dispatchEvent(element, eventName, event);\r\n" +
			"	return event;\r\n" +
			"}\r\n";

	/**
	 * Simulates drag and drop
	 */
	public static final String JAVASCRIPT_SIMULATE_EVENHTML5_DRAGANDDROP = JAVASCRIPT_EVENT_SIMULATOR +
			"function simulateHTML5DragAndDrop(dragFrom, dragTo, dragFromX, dragFromY, dragToX, dragToY) {\r\n" +
			"	var mouseDownEvent = simulateEventCall(dragFrom, \"mousedown\", null, {clientX: dragFromX, clientY: dragFromY});\r\n" +
			"	var dragStartEvent = simulateEventCall(dragFrom, \"dragstart\", null, {clientX: dragFromX, clientY: dragFromY});\r\n" +
			"	var dragEnterEvent = simulateEventCall(dragTo,   \"dragenter\", dragStartEvent, {clientX: dragToX, clientY: dragToY});\r\n" +
			"	var dragOverEvent  = simulateEventCall(dragTo,   \"dragover\",  dragStartEvent, {clientX: dragToX, clientY: dragToY});\r\n" +
			"	var dropEvent      = simulateEventCall(dragTo,   \"drop\",      dragStartEvent, {clientX: dragToX, clientY: dragToY});\r\n" +
			"	var dragEndEvent   = simulateEventCall(dragTo, \"dragend\",   dragStartEvent, {clientX: dragToX, clientY: dragToY});\r\n" +
			"}\r\n" +
			"simulateHTML5DragAndDrop(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5]);\r\n";

	public enum Position {
		Top_Left, Top, Top_Right, Left, Center, Right, Bottom_Left, Bottom, Bottom_Right;
		public int getOffset(final int value) {
			switch(this) {
				case Top_Left:
				case Left:
				case Bottom_Left:
					return 0;
				case Top:
				case Center:
				case Bottom:
					return value / 2;
				case Top_Right:
				case Right:
				case Bottom_Right:
					return value - 1;
				default:
					return 0;
			}
		}
	}

}
