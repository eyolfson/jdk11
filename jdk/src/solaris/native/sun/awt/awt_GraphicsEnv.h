/*
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
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

#ifndef _AWT_GRAPHICSENV_H_
#define _AWT_GRAPHICSENV_H_

#include <jni_util.h>

#ifndef HEADLESS
#define MITSHM
#endif /* !HEADLESS */

#define UNSET_MITSHM (-2)
#define NOEXT_MITSHM (-1)
#define CANT_USE_MITSHM (0)
#define CAN_USE_MITSHM (1)

#ifdef MITSHM

#include <sys/ipc.h>
#include <sys/shm.h>
#include <X11/extensions/XShm.h>
#ifndef X_ShmAttach
#include <X11/Xmd.h>
#include <X11/extensions/shmproto.h>
#endif

extern int XShmQueryExtension();

void TryInitMITShm(JNIEnv *env, jint *shmExt, jint *shmPixmaps);
void resetXShmAttachFailed();
jboolean isXShmAttachFailed();

#endif /* MITSHM */

/* fieldIDs for X11GraphicsConfig fields that may be accessed from C */
struct X11GraphicsConfigIDs {
    jfieldID aData;
    jfieldID bitsPerPixel;
    jfieldID screen;
};

/* fieldIDs for X11GraphicsDevice fields that may be accessed from C */
struct X11GraphicsDeviceIDs {
    jfieldID screen;
};

#endif /* _AWT_GRAPHICSENV_H_ */
