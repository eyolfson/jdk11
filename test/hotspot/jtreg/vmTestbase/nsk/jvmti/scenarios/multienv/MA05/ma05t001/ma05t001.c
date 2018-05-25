/*
 * Copyright (c) 2004, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

#include <stdlib.h>
#include <string.h>
#include "jni_tools.h"
#include "agent_common.h"
#include "jvmti_tools.h"

#define PASSED 0
#define STATUS_FAILED 2

#ifdef __cplusplus
extern "C" {
#endif

/* ========================================================================== */

/* scaffold objects */
static jlong timeout = 0;

/* test objects */
static jthread thread = NULL;
static jmethodID method = NULL;
static int BreakpointEventsCount = 0;
static int FramePopEventsCount = 0;

/* ========================================================================== */

/** callback functions **/

static void JNICALL
Breakpoint(jvmtiEnv *jvmti_env, JNIEnv *jni_env,
        jthread thread, jmethodID method, jlocation location) {
    char *name = NULL;
    char *signature = NULL;

    BreakpointEventsCount++;
    if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB5(GetMethodName,
            jvmti_env, method, &name, &signature, NULL))) {
        nsk_jvmti_setFailStatus();
    }

    NSK_DISPLAY2("Breakpoint event: %s%s\n", name, signature);

    if (name != NULL)
        NSK_CPP_STUB2(Deallocate, jvmti_env, (unsigned char*)name);
    if (signature != NULL)
        NSK_CPP_STUB2(Deallocate, jvmti_env, (unsigned char*)signature);

    switch(BreakpointEventsCount) {
    case 1:
        NSK_DISPLAY0("Testcase #1: FramePop in both agents\n");
        break;

    case 2:
        NSK_DISPLAY0("Testcase #2: w/o NotifyFramePop in 2nd agent\n");
        break;

    case 3:
        NSK_DISPLAY0("Testcase #3: FramePop disabled in 2nd agent\n");
        break;

    default:
        NSK_COMPLAIN0("Should no reach here");
        nsk_jvmti_setFailStatus();
        break;
    }

    if (!NSK_JVMTI_VERIFY(
        NSK_CPP_STUB3(NotifyFramePop, jvmti_env, thread, 0))) {
        nsk_jvmti_setFailStatus();
    }
}

static void JNICALL
FramePop(jvmtiEnv *jvmti_env, JNIEnv *jni_env,
        jthread thread, jmethodID method,
        jboolean wasPopedByException) {
    char *name = NULL;
    char *signature = NULL;

    FramePopEventsCount++;
    if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB5(GetMethodName,
            jvmti_env, method, &name, &signature, NULL))) {
        nsk_jvmti_setFailStatus();
        return;
    }

    NSK_DISPLAY2("FramePop event: %s%s\n", name, signature);

    if (name != NULL)
        NSK_CPP_STUB2(Deallocate, jvmti_env, (unsigned char*)name);
    if (signature != NULL)
        NSK_CPP_STUB2(Deallocate, jvmti_env, (unsigned char*)signature);
}

/* ========================================================================== */

static int prepare(jvmtiEnv* jvmti, JNIEnv* jni) {
    const char* THREAD_NAME = "Debuggee Thread";
    jvmtiThreadInfo info;
    jthread *threads = NULL;
    jclass klass = NULL;
    jint threads_count = 0;
    int i;

    NSK_DISPLAY0("Prepare: find tested thread\n");

    /* get all live threads */
    if (!NSK_JVMTI_VERIFY(
           NSK_CPP_STUB3(GetAllThreads, jvmti, &threads_count, &threads)))
        return NSK_FALSE;

    if (!NSK_VERIFY(threads_count > 0 && threads != NULL))
        return NSK_FALSE;

    /* find tested thread */
    for (i = 0; i < threads_count; i++) {
        if (!NSK_VERIFY(threads[i] != NULL))
            return NSK_FALSE;

        /* get thread information */
        if (!NSK_JVMTI_VERIFY(
                NSK_CPP_STUB3(GetThreadInfo, jvmti, threads[i], &info)))
            return NSK_FALSE;

        NSK_DISPLAY3("    thread #%d (%s): %p\n", i, info.name, threads[i]);

        /* find by name */
        if (info.name != NULL && (strcmp(info.name, THREAD_NAME) == 0)) {
            thread = threads[i];
        }

        if (info.name != NULL) {
            if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB2(
                    Deallocate, jvmti, (unsigned char*)info.name)))
                return NSK_FALSE;
        }
    }

    /* deallocate threads list */
    if (!NSK_JVMTI_VERIFY(
            NSK_CPP_STUB2(Deallocate, jvmti, (unsigned char*)threads)))
        return NSK_FALSE;

    if (thread == NULL) {
        NSK_COMPLAIN0("Debuggee thread not found");
        return NSK_FALSE;
    }

    if (!NSK_JNI_VERIFY(jni, (thread =
            NSK_CPP_STUB2(NewGlobalRef, jni, thread)) != NULL))
        return NSK_FALSE;

    /* get tested thread class */
    if (!NSK_JNI_VERIFY(jni, (klass =
            NSK_CPP_STUB2(GetObjectClass, jni, thread)) != NULL))
        return NSK_FALSE;

    /* get tested thread method 'checkPoint' */
    if (!NSK_JNI_VERIFY(jni, (method = NSK_CPP_STUB4(
            GetMethodID, jni, klass, "checkPoint", "()V")) != NULL))
        return NSK_FALSE;

    if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB3(SetBreakpoint, jvmti, method, 0)))
        return NSK_FALSE;

    /* enable events */
    if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB4(SetEventNotificationMode,
            jvmti, JVMTI_ENABLE, JVMTI_EVENT_BREAKPOINT, NULL)))
        return NSK_FALSE;
    if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB4(SetEventNotificationMode,
            jvmti, JVMTI_ENABLE, JVMTI_EVENT_FRAME_POP, NULL)))
        return NSK_FALSE;

    return NSK_TRUE;
}

/* ========================================================================== */

/** Agent algorithm. */
static void JNICALL
agentProc(jvmtiEnv* jvmti, JNIEnv* jni, void* arg) {

    if (!nsk_jvmti_waitForSync(timeout))
        return;

    if (!prepare(jvmti, jni)) {
        nsk_jvmti_setFailStatus();
        return;
    }

    /* resume debugee and wait for sync */
    if (!nsk_jvmti_resumeSync())
        return;
    if (!nsk_jvmti_waitForSync(timeout))
        return;

    if (FramePopEventsCount == 0) {
        NSK_COMPLAIN0("No FramePop events\n");
        nsk_jvmti_setFailStatus();
    } else if (FramePopEventsCount != 3) {
        NSK_COMPLAIN1("Some of FramePop events were missed: %d\n",
            FramePopEventsCount);
        nsk_jvmti_setFailStatus();
    }

    if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB4(SetEventNotificationMode,
            jvmti, JVMTI_DISABLE, JVMTI_EVENT_FRAME_POP, NULL)))
        nsk_jvmti_setFailStatus();

    NSK_TRACE(NSK_CPP_STUB3(ClearBreakpoint, jvmti, method, 0));
    NSK_TRACE(NSK_CPP_STUB2(DeleteGlobalRef, jni, thread));

    if (!nsk_jvmti_resumeSync())
        return;
}

/* ========================================================================== */

/** Agent library initialization. */
#ifdef STATIC_BUILD
JNIEXPORT jint JNICALL Agent_OnLoad_ma05t001(JavaVM *jvm, char *options, void *reserved) {
    return Agent_Initialize(jvm, options, reserved);
}
JNIEXPORT jint JNICALL Agent_OnAttach_ma05t001(JavaVM *jvm, char *options, void *reserved) {
    return Agent_Initialize(jvm, options, reserved);
}
JNIEXPORT jint JNI_OnLoad_ma05t001(JavaVM *jvm, char *options, void *reserved) {
    return JNI_VERSION_1_8;
}
#endif
jint Agent_Initialize(JavaVM *jvm, char *options, void *reserved) {
    jvmtiEnv* jvmti = NULL;
    jvmtiEventCallbacks callbacks;
    jvmtiCapabilities caps;

    NSK_DISPLAY0("Agent_OnLoad\n");

    if (!NSK_VERIFY(nsk_jvmti_parseOptions(options)))
        return JNI_ERR;

    timeout = nsk_jvmti_getWaitTime() * 60 * 1000;

    if (!NSK_VERIFY((jvmti =
            nsk_jvmti_createJVMTIEnv(jvm, reserved)) != NULL))
        return JNI_ERR;

    memset(&caps, 0, sizeof(caps));
    caps.can_generate_breakpoint_events = 1;
    caps.can_generate_frame_pop_events = 1;
    if (!NSK_JVMTI_VERIFY(NSK_CPP_STUB2(AddCapabilities, jvmti, &caps)))
        return JNI_ERR;

    if (!NSK_VERIFY(nsk_jvmti_setAgentProc(agentProc, NULL)))
        return JNI_ERR;

    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.Breakpoint = &Breakpoint;
    callbacks.FramePop = &FramePop;
    if (!NSK_VERIFY(nsk_jvmti_init_MA(&callbacks)))
        return JNI_ERR;

    return JNI_OK;
}

/* ========================================================================== */

#ifdef __cplusplus
}
#endif
