/*
 * Copyright (c) 2003, 2018, Oracle and/or its affiliates. All rights reserved.
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

#include <string.h>
#include "jvmti.h"
#include "agent_common.h"
#include "jni_tools.h"
#include "jvmti_tools.h"

#ifdef __cplusplus
extern "C" {
#endif

/* ============================================================================= */

/* scaffold objects */
static JNIEnv* jni = NULL;
static jvmtiEnv *jvmti = NULL;
static jlong timeout = 0;

/* constants */
#define THREADS_COUNT   6
#define MAX_NAME_LENGTH 100
#define MAX_STACK_SIZE  100

/* thread description structure */
typedef struct {
    char threadName[MAX_NAME_LENGTH];
    char methodName[MAX_NAME_LENGTH];
    char methodSig[MAX_NAME_LENGTH];
    jthread thread;
    jclass cls;
    jmethodID method;
    jlocation location;
} ThreadDesc;

/* descriptions of tested threads */
static ThreadDesc threadsDesc[THREADS_COUNT] = {
    {"threadRunning", "testedMethod", "()V", NULL, NULL, NULL, NSK_JVMTI_INVALID_JLOCATION},
    {"threadEntering", "testedMethod", "()V", NULL, NULL, NULL, NSK_JVMTI_INVALID_JLOCATION},
    {"threadWaiting", "testedMethod", "()V", NULL, NULL, NULL, NSK_JVMTI_INVALID_JLOCATION},
    {"threadSleeping", "testedMethod", "()V", NULL, NULL, NULL, NSK_JVMTI_INVALID_JLOCATION},
    {"threadRunningInterrupted", "testedMethod", "()V", NULL, NULL, NULL, NSK_JVMTI_INVALID_JLOCATION},
    {"threadRunningNative", "testedMethod", "()V", NULL, NULL, NULL, NSK_JVMTI_INVALID_JLOCATION}
};

/* indexes of known threads */
static const int interruptedThreadIndex = THREADS_COUNT - 2;
static const int nativeThreadIndex = THREADS_COUNT - 1;

/* ============================================================================= */

/* testcase(s) */
static int prepare();
static int checkThreads(int suspended, const char* kind);
static int suspendThreadsIndividually(int suspend);
static int clean();

/* ============================================================================= */

/** Agent algorithm. */
static void JNICALL
agentProc(jvmtiEnv* jvmti, JNIEnv* agentJNI, void* arg) {
    jni = agentJNI;

    /* wait for initial sync */
    if (!nsk_jvmti_waitForSync(timeout))
        return;

    /* perform testcase(s) */
    {
        /* prepare data: find threads */
        NSK_DISPLAY0("Prepare data\n");
        if (!prepare()) {
            nsk_jvmti_setFailStatus();
            return;
        }

        /* testcase #1: check not suspended threads */
        NSK_DISPLAY0("Testcase #1: check stack frames of not suspended threads\n");
        if (!checkThreads(NSK_FALSE, "not suspended"))
            return;

        /* suspend threads */
        NSK_DISPLAY0("Suspend each thread\n");
        if (!suspendThreadsIndividually(NSK_TRUE))
            return;

        /* testcase #2: check suspended threads */
        NSK_DISPLAY0("Testcase #2: check stack frames of suspended threads\n");
        if (!checkThreads(NSK_TRUE, "suspended"))
            return;

        /* resume threads */
        NSK_DISPLAY0("Resume each thread\n");
        if (!suspendThreadsIndividually(NSK_FALSE))
            return;

        /* testcase #3: check resumed threads */
        NSK_DISPLAY0("Testcase #3: check stack frames of resumed threads\n");
        if (!checkThreads(NSK_FALSE, "resumed"))
            return;

        /* clean date: delete threads references */
        NSK_DISPLAY0("Clean data\n");
        if (!clean()) {
            nsk_jvmti_setFailStatus();
            return;
        }
    }

    /* resume debugee after last sync */
    if (!nsk_jvmti_resumeSync())
        return;
}

/* ============================================================================= */

/**
 * Prepare data:
 *    - clean threads list
 *    - get all live threads
 *    - get threads name
 *    - find tested threads
 *    - find tested methos
 *    - make global refs
 */
static int prepare() {
    jthread *allThreadsList = NULL;
    jint allThreadsCount = 0;
    int found = 0;
    int i;

    NSK_DISPLAY1("Find tested threads: %d\n", THREADS_COUNT);

    /* clean threads list */
    for (i = 0; i < THREADS_COUNT; i++) {
        threadsDesc[i].thread = (jthread)NULL;
        threadsDesc[i].method = (jmethodID)NULL;
        threadsDesc[i].location = NSK_JVMTI_INVALID_JLOCATION;
    }

    /* get all live threads */
    if (!NSK_JVMTI_VERIFY(
            NSK_CPP_STUB3(GetAllThreads, jvmti, &allThreadsCount, &allThreadsList)))
        return NSK_FALSE;

    if (!NSK_VERIFY(allThreadsCount > 0 && allThreadsList != NULL))
        return NSK_FALSE;

    /* find tested threads */
    for (i = 0; i < allThreadsCount; i++) {
        jvmtiThreadInfo threadInfo;

        if (!NSK_VERIFY(allThreadsList[i] != NULL))
            return NSK_FALSE;

        /* get thread name (info) */
        if (!NSK_JVMTI_VERIFY(
                NSK_CPP_STUB3(GetThreadInfo, jvmti, allThreadsList[i], &threadInfo)))
            return NSK_FALSE;

        /* find by name */
        if (threadInfo.name != NULL) {
            int j;

            for (j = 0; j < THREADS_COUNT; j++) {
                if (strcmp(threadInfo.name, threadsDesc[j].threadName) == 0) {
                    threadsDesc[j].thread = allThreadsList[i];
                    NSK_DISPLAY3("    thread #%d (%s): %p\n",
                                            j, threadInfo.name, (void*)threadsDesc[j].thread);
                }
            }
        }
    }

    /* deallocate all threads list */
    if (!NSK_JVMTI_VERIFY(
            NSK_CPP_STUB2(Deallocate, jvmti, (unsigned char*)allThreadsList)))
        return NSK_FALSE;

    /* check if all tested threads found */
    found = 0;
    for (i = 0; i < THREADS_COUNT; i++) {
        if (threadsDesc[i].thread == NULL) {
            NSK_COMPLAIN2("Not found tested thread #%d (%s)\n", i, threadsDesc[i].threadName);
        } else {
            found++;
        }
    }

    if (found < THREADS_COUNT)
        return NSK_FALSE;

    /* get threads class and frame method */
    NSK_DISPLAY0("Find tested methods:\n");
    for (i = 0; i < THREADS_COUNT; i++) {
        /* get thread class */
        if (!NSK_JNI_VERIFY(jni, (threadsDesc[i].cls =
                NSK_CPP_STUB2(GetObjectClass, jni, threadsDesc[i].thread)) != NULL))
            return NSK_FALSE;
        /* get frame method */
        if (!NSK_JNI_VERIFY(jni, (threadsDesc[i].method =
                NSK_CPP_STUB4(GetMethodID, jni, threadsDesc[i].cls,
                            threadsDesc[i].methodName, threadsDesc[i].methodSig)) != NULL))
            return NSK_FALSE;

        NSK_DISPLAY4("    thread #%d (%s): %p (%s)\n",
                                i, threadsDesc[i].threadName,
                                (void*)threadsDesc[i].method,
                                threadsDesc[i].methodName);
    }

    /* make global refs */
    for (i = 0; i < THREADS_COUNT; i++) {
        if (!NSK_JNI_VERIFY(jni, (threadsDesc[i].thread = (jthread)
                NSK_CPP_STUB2(NewGlobalRef, jni, threadsDesc[i].thread)) != NULL))
            return NSK_FALSE;
        if (!NSK_JNI_VERIFY(jni, (threadsDesc[i].cls = (jclass)
                NSK_CPP_STUB2(NewGlobalRef, jni, threadsDesc[i].cls)) != NULL))
            return NSK_FALSE;
    }

    return NSK_TRUE;
}

/**
 * Suspend or resume tested threads.
 */
static int suspendThreadsIndividually(int suspend) {
    int i;

    for (i = 0; i < THREADS_COUNT; i++) {
        if (suspend) {
            NSK_DISPLAY2("    suspend thread #%d (%s)\n", i, threadsDesc[i].threadName);
            if (!NSK_JVMTI_VERIFY(
                    NSK_CPP_STUB2(SuspendThread, jvmti, threadsDesc[i].thread)))
                nsk_jvmti_setFailStatus();
        } else {
            NSK_DISPLAY2("    resume thread #%d (%s)\n", i, threadsDesc[i].threadName);
            if (!NSK_JVMTI_VERIFY(
                    NSK_CPP_STUB2(ResumeThread, jvmti, threadsDesc[i].thread)))
                nsk_jvmti_setFailStatus();
        }
    }
    return NSK_TRUE;
}

/**
 * Testcase: check tested threads
 *    - call GetFrameCount() and then GetStackTrace()
 *    - for each stack frame of common depth GetFrameLocation()
 *    - compare frame ifno returned by GetFrameLocation() and GetStackTrace()
 *    - find expected frame for tested method
 *
 * Returns NSK_TRUE if test may continue; or NSK_FALSE for test break.
 */
static int checkThreads(int suspended, const char* kind) {
    int i;

    /* check each thread */
    for (i = 0; i < THREADS_COUNT; i++) {
        jint frameCount = 0;
        jint frameStackSize = 0;
        jvmtiFrameInfo frameStack[MAX_STACK_SIZE];
        int commonDepth = 0;
        int found = 0;
        int j;

        NSK_DISPLAY2("  thread #%d (%s):\n", i, threadsDesc[i].threadName);

        /* get frame count */
        if (!NSK_JVMTI_VERIFY(
                NSK_CPP_STUB3(GetFrameCount, jvmti,
                                    threadsDesc[i].thread, &frameCount))) {
            nsk_jvmti_setFailStatus();
            return NSK_TRUE;
        }
        NSK_DISPLAY1("    frameCount:  %d\n", (int)frameCount);

        /* get stack trace */
        if (!NSK_JVMTI_VERIFY(
                NSK_CPP_STUB6(GetStackTrace, jvmti, threadsDesc[i].thread,
                                    0, MAX_STACK_SIZE, frameStack, &frameStackSize))) {
            nsk_jvmti_setFailStatus();
            return NSK_TRUE;
        }
        NSK_DISPLAY1("    stack depth: %d\n", (int)frameStackSize);

        commonDepth = (frameCount < frameStackSize ? frameCount : frameStackSize);
        NSK_DISPLAY1("         common: %d\n", (int)commonDepth);

        /* check first commonDepth frames and find expected method there */
        found = 0;
        for (j = 0; j < commonDepth; j++) {
            jmethodID qMethod = (jmethodID)NULL;
            jlocation qLocation = NSK_JVMTI_INVALID_JLOCATION;

            NSK_DISPLAY3("      %d frame: method: %p, location: %ld\n",
                                        j, (void*)frameStack[j].method,
                                        (long)frameStack[j].location);
            /* query frame location */
            if (!NSK_JVMTI_VERIFY(
                    NSK_CPP_STUB5(GetFrameLocation, jvmti, threadsDesc[i].thread,
                                                        j, &qMethod, &qLocation))
                && (suspended == NSK_TRUE)) {
                nsk_jvmti_setFailStatus();
                continue;
            }

            NSK_DISPLAY2("      queried: method: %p, location: %ld\n",
                                        (void*)qMethod, (long)qLocation);

            /* check frame equality */
            if ((suspended == NSK_TRUE) && (frameStack[j].method != qMethod)) {
                NSK_COMPLAIN6("Different method in stack frame #%d for %s thread #%d (%s):\n"
                            "#   GetStackTrace():    %p\n"
                            "#   GetFrameLocation(): %p\n",
                            j, kind, i, threadsDesc[i].threadName,
                            (void*)frameStack[j].method, (void*)qMethod);
                nsk_jvmti_setFailStatus();
            }
            if ( (suspended == NSK_TRUE) && (frameStack[j].location != qLocation) ) {
                NSK_COMPLAIN6("Different location in stack frame #%d for %s thread #%d (%s):\n"
                            "#   GetStackTrace():    %ld\n"
                            "#   GetFrameLocation(): %ld\n",
                            j, kind, i, threadsDesc[i].threadName,
                            (long)frameStack[j].location, (long)qLocation);
                nsk_jvmti_setFailStatus();
            }

            /* find expected method */
            if (frameStack[j].method == threadsDesc[i].method) {
                found++;
                NSK_DISPLAY1("        found expected method: %s\n",
                                                threadsDesc[i].methodName);
            }
        }

        /* check if expected method frame found */
        if (found <= 0) {
            NSK_COMPLAIN3("No expected method frame for %s thread #%d (%s)\n",
                                kind, i, threadsDesc[i].threadName);
            nsk_jvmti_setFailStatus();
        }
    }

    /* test may continue */
    return NSK_TRUE;
}

/**
 * Clean data:
 *   - dispose global references to tested threads
 */
static int clean() {
    int i;

    /* dispose global references to threads */
    for (i = 0; i < THREADS_COUNT; i++) {
        NSK_TRACE(NSK_CPP_STUB2(DeleteGlobalRef, jni, threadsDesc[i].thread));
        NSK_TRACE(NSK_CPP_STUB2(DeleteGlobalRef, jni, threadsDesc[i].cls));
    }

    return NSK_TRUE;
}

/* ============================================================================= */

static volatile int testedThreadRunning = NSK_FALSE;
static volatile int testedThreadShouldFinish = NSK_FALSE;

/** Native running method in tested thread. */
JNIEXPORT void JNICALL
Java_nsk_jvmti_scenarios_sampling_SP02_sp02t003ThreadRunningNative_testedMethod(JNIEnv* jni,
                                                                                jobject obj) {
    volatile int i = 0, n = 1000;

    /* run in a loop */
    testedThreadRunning = NSK_TRUE;
    while (!testedThreadShouldFinish) {
        if (n <= 0)
            n = 1000;
        if (i >= n)
            i = 0;
        i++;
    }
    testedThreadRunning = NSK_FALSE;
}

/** Wait for native method is running. */
JNIEXPORT jboolean JNICALL
Java_nsk_jvmti_scenarios_sampling_SP02_sp02t003ThreadRunningNative_checkReady(JNIEnv* jni,
                                                                            jobject obj) {
    while (!testedThreadRunning) {
        nsk_jvmti_sleep(1000);
    }
    return testedThreadRunning ? JNI_TRUE : JNI_FALSE;
}

/** Let native method to finish. */
JNIEXPORT void JNICALL
Java_nsk_jvmti_scenarios_sampling_SP02_sp02t003ThreadRunningNative_letFinish(JNIEnv* jni,
                                                                            jobject obj) {
    testedThreadShouldFinish = NSK_TRUE;
}

/* ============================================================================= */

/** Agent library initialization. */
#ifdef STATIC_BUILD
JNIEXPORT jint JNICALL Agent_OnLoad_sp02t003(JavaVM *jvm, char *options, void *reserved) {
    return Agent_Initialize(jvm, options, reserved);
}
JNIEXPORT jint JNICALL Agent_OnAttach_sp02t003(JavaVM *jvm, char *options, void *reserved) {
    return Agent_Initialize(jvm, options, reserved);
}
JNIEXPORT jint JNI_OnLoad_sp02t003(JavaVM *jvm, char *options, void *reserved) {
    return JNI_VERSION_1_8;
}
#endif
jint Agent_Initialize(JavaVM *jvm, char *options, void *reserved) {

    /** Init framework and parse options. */
    if (!NSK_VERIFY(nsk_jvmti_parseOptions(options)))
        return JNI_ERR;

    timeout = nsk_jvmti_getWaitTime() * 60 * 1000;

    /* create JVMTI environment */
    if (!NSK_VERIFY((jvmti =
            nsk_jvmti_createJVMTIEnv(jvm, reserved)) != NULL))
        return JNI_ERR;

    /* add specific capabilities for suspending thread */    {
        jvmtiCapabilities suspendCaps;
        memset(&suspendCaps, 0, sizeof(suspendCaps));
        suspendCaps.can_suspend = 1;
        if (!NSK_JVMTI_VERIFY(
                NSK_CPP_STUB2(AddCapabilities, jvmti, &suspendCaps)))
            return JNI_ERR;
    }

    /* register agent proc and arg */
    if (!NSK_VERIFY(nsk_jvmti_setAgentProc(agentProc, NULL)))
        return JNI_ERR;

    return JNI_OK;
}

/* ============================================================================= */

#ifdef __cplusplus
}
#endif
