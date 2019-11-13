/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


#import "jni_util.h"

#import <JavaNativeFoundation/JavaNativeFoundation.h>
#import <ApplicationServices/ApplicationServices.h>

#import "CRobotKeyCode.h"
#import "LWCToolkit.h"
#import "sun_lwawt_macosx_CRobot.h"
#import "java_awt_event_InputEvent.h"
#import "java_awt_event_KeyEvent.h"
#import "sizecalc.h"

// Starting number for event numbers generated by Robot.
// Apple docs don't mention at all what are the requirements
// for these numbers. It seems that they must be higher
// than event numbers from real events, which start at some
// value close to zero. There is no API for obtaining current
// event number, so we have to start from some random number.
// 32000 as starting value works for me, let's hope that it will
// work for others as well.
#define ROBOT_EVENT_NUMBER_START 32000

#define k_JAVA_ROBOT_WHEEL_COUNT 1

#if !defined(kCGBitmapByteOrder32Host)
#define kCGBitmapByteOrder32Host 0
#endif

// In OS X, left and right mouse button share the same click count.
// That is, if one starts clicking the left button rapidly and then
// switches to the right button, then the click count will continue
// increasing, without dropping to 1 in between. The middle button,
// however, has its own click count.
// For robot, we aren't going to emulate all that complexity. All our
// synhtetic clicks share the same click count.
static int gsClickCount;
static NSTimeInterval gsLastClickTime;

// Apparently, for mouse up/down events we have to set an event number
// that is incremented on each button press. Otherwise, strange things
// happen with z-order.
static int gsEventNumber;
static int* gsButtonEventNumber;

static inline CGKeyCode GetCGKeyCode(jint javaKeyCode);

static void PostMouseEvent(const CGPoint point, CGMouseButton button,
                           CGEventType type, int clickCount, int eventNumber);

static int GetClickCount(BOOL isDown);

static void
CreateJavaException(JNIEnv* env, CGError err)
{
    // Throw a java exception indicating what is wrong.
    NSString* s = [NSString stringWithFormat:@"Robot: CGError: %d", err];
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/awt/AWTException"),
                     [s UTF8String]);
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    initRobot
 * Signature: (V)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_initRobot
(JNIEnv *env, jobject peer)
{
    // Set things up to let our app act like a synthetic keyboard and mouse.
    // Always set all states, in case Apple ever changes default behaviors.
    static int setupDone = 0;
    if (!setupDone) {
        int i;
        jint* tmp;
        jboolean copy = JNI_FALSE;

        setupDone = 1;
        // Don't block local events after posting ours
        CGSetLocalEventsSuppressionInterval(0.0);

        // Let our event's modifier key state blend with local hardware events
        CGEnableEventStateCombining(TRUE);

        // Don't let our events block local hardware events
        CGSetLocalEventsFilterDuringSupressionState(
                                    kCGEventFilterMaskPermitAllEvents,
                                    kCGEventSupressionStateSupressionInterval);
        CGSetLocalEventsFilterDuringSupressionState(
                                    kCGEventFilterMaskPermitAllEvents,
                                    kCGEventSupressionStateRemoteMouseDrag);

        gsClickCount = 0;
        gsLastClickTime = 0;
        gsEventNumber = ROBOT_EVENT_NUMBER_START;

        gsButtonEventNumber = (int*)SAFE_SIZE_ARRAY_ALLOC(malloc, sizeof(int), gNumberOfButtons);
        if (gsButtonEventNumber == NULL) {
            JNU_ThrowOutOfMemoryError(env, NULL);
            return;
        }

        for (i = 0; i < gNumberOfButtons; ++i) {
            gsButtonEventNumber[i] = ROBOT_EVENT_NUMBER_START;
        }
    }
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    mouseEvent
 * Signature: (IIIIZZ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_mouseEvent
(JNIEnv *env, jobject peer, jint mouseLastX, jint mouseLastY, jint buttonsState,
 jboolean isButtonsDownState, jboolean isMouseMove)
{
    JNF_COCOA_ENTER(env);

    // This is the native method called when Robot mouse events occur.
    // The CRobot tracks the mouse position, and which button was
    // pressed. The peer also tracks the mouse button desired state,
    // the appropriate key modifier state, and whether the mouse action
    // is simply a mouse move with no mouse button state changes.

    // volatile, otherwise it warns that it might be clobbered by 'longjmp'
    volatile CGPoint point;

    point.x = mouseLastX;
    point.y = mouseLastY;

    __block CGMouseButton button = kCGMouseButtonLeft;
    __block CGEventType type = kCGEventMouseMoved;

    void (^HandleRobotButton)(CGMouseButton, CGEventType, CGEventType, CGEventType) =
        ^(CGMouseButton cgButton, CGEventType cgButtonUp, CGEventType cgButtonDown,
          CGEventType cgButtonDragged) {

            button = cgButton;
            type = cgButtonUp;

            if (isButtonsDownState) {
                if (isMouseMove) {
                    type = cgButtonDragged;
                } else {
                    type = cgButtonDown;
                }
            }
        };

    // Left
    if (buttonsState & java_awt_event_InputEvent_BUTTON1_MASK ||
        buttonsState & java_awt_event_InputEvent_BUTTON1_DOWN_MASK ) {

        HandleRobotButton(kCGMouseButtonLeft, kCGEventLeftMouseUp,
                          kCGEventLeftMouseDown, kCGEventLeftMouseDragged);
    }

    // Other
    if (buttonsState & java_awt_event_InputEvent_BUTTON2_MASK ||
        buttonsState & java_awt_event_InputEvent_BUTTON2_DOWN_MASK ) {

        HandleRobotButton(kCGMouseButtonCenter, kCGEventOtherMouseUp,
                          kCGEventOtherMouseDown, kCGEventOtherMouseDragged);
    }

    // Right
    if (buttonsState & java_awt_event_InputEvent_BUTTON3_MASK ||
        buttonsState & java_awt_event_InputEvent_BUTTON3_DOWN_MASK ) {

        HandleRobotButton(kCGMouseButtonRight, kCGEventRightMouseUp,
                          kCGEventRightMouseDown, kCGEventRightMouseDragged);
    }

    // Extra
    if (gNumberOfButtons > 3) {
        int extraButton;
        for (extraButton = 3; extraButton < gNumberOfButtons; ++extraButton) {
            if ((buttonsState & gButtonDownMasks[extraButton])) {
                HandleRobotButton(extraButton, kCGEventOtherMouseUp,
                            kCGEventOtherMouseDown, kCGEventOtherMouseDragged);
            }
        }
    }

    int clickCount = 0;
    int eventNumber = gsEventNumber;

    if (isMouseMove) {
        // any mouse movement resets click count
        gsLastClickTime = 0;
    } else {
        clickCount = GetClickCount(isButtonsDownState);

        if (isButtonsDownState) {
            gsButtonEventNumber[button] = gsEventNumber++;
        }
        eventNumber = gsButtonEventNumber[button];
    }

    PostMouseEvent(point, button, type, clickCount, eventNumber);

    JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    mouseWheel
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_mouseWheel
(JNIEnv *env, jobject peer, jint wheelAmt)
{
    CGEventRef event = CGEventCreateScrollWheelEvent(NULL,
                                            kCGScrollEventUnitLine,
                                            k_JAVA_ROBOT_WHEEL_COUNT, wheelAmt);

    if (event != NULL) {
        CGEventPost(kCGSessionEventTap, event);
        CFRelease(event);
    }
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    keyEvent
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_keyEvent
(JNIEnv *env, jobject peer, jint javaKeyCode, jboolean keyPressed)
{
    CGKeyCode keyCode = GetCGKeyCode(javaKeyCode);

    CGEventRef event = CGEventCreateKeyboardEvent(NULL, keyCode, keyPressed);
    if (event != NULL) {
        CGEventPost(kCGSessionEventTap, event);
        CFRelease(event);
    }
}

/*
 * Class:     sun_lwawt_macosx_CRobot
 * Method:    nativeGetScreenPixels
 * Signature: (IIIII[I)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CRobot_nativeGetScreenPixels
(JNIEnv *env, jobject peer,
 jint x, jint y, jint width, jint height, jdouble scale, jintArray pixels)
{
    JNF_COCOA_ENTER(env);

    jint picX = x;
    jint picY = y;
    jint picWidth = width;
    jint picHeight = height;

    CGRect screenRect = CGRectMake(picX / scale, picY / scale,
    				picWidth / scale, picHeight / scale);
    CGImageRef screenPixelsImage = CGWindowListCreateImage(screenRect,
                                        kCGWindowListOptionOnScreenOnly,
                                        kCGNullWindowID, kCGWindowImageBestResolution);

    if (screenPixelsImage == NULL) {
        return;
    }

    // get a pointer to the Java int array
    void *jPixelData = (*env)->GetPrimitiveArrayCritical(env, pixels, 0);
    CHECK_NULL(jPixelData);

    // create a graphics context around the Java int array
    CGColorSpaceRef picColorSpace = CGColorSpaceCreateWithName(
                                            kCGColorSpaceSRGB);
    CGContextRef jPicContextRef = CGBitmapContextCreate(
                                            jPixelData,
                                            picWidth, picHeight,
                                            8, picWidth * sizeof(jint),
                                            picColorSpace,
                                            kCGBitmapByteOrder32Host |
                                            kCGImageAlphaPremultipliedFirst);

    CGColorSpaceRelease(picColorSpace);

    // flip, scale, and color correct the screen image into the Java pixels
    CGRect bounds = { { 0, 0 }, { picWidth, picHeight } };
    CGContextDrawImage(jPicContextRef, bounds, screenPixelsImage);
    CGContextFlush(jPicContextRef);

    // cleanup
    CGContextRelease(jPicContextRef);
    CGImageRelease(screenPixelsImage);

    // release the Java int array back up to the JVM
    (*env)->ReleasePrimitiveArrayCritical(env, pixels, jPixelData, 0);

    JNF_COCOA_EXIT(env);
}

/****************************************************
 * Helper methods
 ****************************************************/

static void PostMouseEvent(const CGPoint point, CGMouseButton button,
                           CGEventType type, int clickCount, int eventNumber)
{
    CGEventRef mouseEvent = CGEventCreateMouseEvent(NULL, type, point, button);
    if (mouseEvent != NULL) {
        CGEventSetIntegerValueField(mouseEvent, kCGMouseEventClickState, clickCount);
        CGEventSetIntegerValueField(mouseEvent, kCGMouseEventNumber, eventNumber);
        CGEventPost(kCGSessionEventTap, mouseEvent);
        CFRelease(mouseEvent);
    }
}

static inline CGKeyCode GetCGKeyCode(jint javaKeyCode)
{
    CRobotKeyCodeMapping *keyCodeMapping = [CRobotKeyCodeMapping sharedInstance];
    return [keyCodeMapping getOSXKeyCodeForJavaKey:javaKeyCode];
}

static int GetClickCount(BOOL isDown) {
    NSTimeInterval now = [[NSDate date] timeIntervalSinceReferenceDate];
    NSTimeInterval clickInterval = now - gsLastClickTime;
    BOOL isWithinTreshold = clickInterval < [NSEvent doubleClickInterval];

    if (isDown) {
        if (isWithinTreshold) {
            gsClickCount++;
        } else {
            gsClickCount = 1;
        }

        gsLastClickTime = now;
    } else {
        // In OS X, a mouse up has the click count of the last mouse down
        // if an interval between up and down is within the double click
        // threshold, and 0 otherwise.
        if (!isWithinTreshold) {
            gsClickCount = 0;
        }
    }

    return gsClickCount;
}
