/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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

#include <jlong.h>

#include "sun_java2d_opengl_GLXSurfaceData.h"

#include "OGLRenderQueue.h"
#include "GLXGraphicsConfig.h"
#include "GLXSurfaceData.h"
#include "awt_Component.h"
#include "awt_GraphicsEnv.h"

/**
 * The methods in this file implement the native windowing system specific
 * layer (GLX) for the OpenGL-based Java 2D pipeline.
 */

#ifndef HEADLESS

extern LockFunc       OGLSD_Lock;
extern GetRasInfoFunc OGLSD_GetRasInfo;
extern UnlockFunc     OGLSD_Unlock;
extern DisposeFunc    OGLSD_Dispose;

extern struct MComponentPeerIDs mComponentPeerIDs;

extern void
    OGLSD_SetNativeDimensions(JNIEnv *env, OGLSDOps *oglsdo, jint w, jint h);

jboolean surfaceCreationFailed = JNI_FALSE;

#endif /* !HEADLESS */

JNIEXPORT void JNICALL
Java_sun_java2d_opengl_GLXSurfaceData_initOps(JNIEnv *env, jobject glxsd,
                                              jobject peer, jlong aData)
{
#ifndef HEADLESS
    OGLSDOps *oglsdo = (OGLSDOps *)SurfaceData_InitOps(env, glxsd,
                                                       sizeof(OGLSDOps));
    GLXSDOps *glxsdo = (GLXSDOps *)malloc(sizeof(GLXSDOps));

    J2dTraceLn(J2D_TRACE_INFO, "GLXSurfaceData_initOps");

    if (glxsdo == NULL) {
        JNU_ThrowOutOfMemoryError(env, "creating native GLX ops");
        return;
    }

    oglsdo->privOps = glxsdo;

    oglsdo->sdOps.Lock       = OGLSD_Lock;
    oglsdo->sdOps.GetRasInfo = OGLSD_GetRasInfo;
    oglsdo->sdOps.Unlock     = OGLSD_Unlock;
    oglsdo->sdOps.Dispose    = OGLSD_Dispose;

    oglsdo->drawableType = OGLSD_UNDEFINED;
    oglsdo->activeBuffer = GL_FRONT;
    oglsdo->needsInit = JNI_TRUE;

#ifdef XAWT
    if (peer != NULL) {
        glxsdo->window = JNU_CallMethodByName(env, NULL, peer,
                                              "getContentWindow", "()J").j;
    } else {
        glxsdo->window = 0;
    }
#else
    if (peer != NULL) {
        struct ComponentData *cdata;
        cdata = (struct ComponentData *)
            JNU_GetLongFieldAsPtr(env, peer, mComponentPeerIDs.pData);
        if (cdata == NULL) {
            free(glxsdo);
            JNU_ThrowNullPointerException(env, "Component data missing");
            return;
        }
        if (cdata->widget == NULL) {
            free(glxsdo);
            JNU_ThrowInternalError(env, "Widget is NULL in initOps");
            return;
        }
        glxsdo->widget = cdata->widget;
    } else {
        glxsdo->widget = NULL;
    }
#endif

    glxsdo->configData = (AwtGraphicsConfigDataPtr)jlong_to_ptr(aData);
    if (glxsdo->configData == NULL) {
        free(glxsdo);
        JNU_ThrowNullPointerException(env,
                                 "Native GraphicsConfig data block missing");
        return;
    }

    if (glxsdo->configData->glxInfo == NULL) {
        free(glxsdo);
        JNU_ThrowNullPointerException(env, "GLXGraphicsConfigInfo missing");
        return;
    }
#endif /* HEADLESS */
}

#ifndef HEADLESS

/**
 * This function disposes of any native windowing system resources associated
 * with this surface.  For instance, if the given OGLSDOps is of type
 * OGLSD_PBUFFER, this method implementation will destroy the actual pbuffer
 * surface.
 */
void
OGLSD_DestroyOGLSurface(JNIEnv *env, OGLSDOps *oglsdo)
{
    GLXSDOps *glxsdo = (GLXSDOps *)oglsdo->privOps;

    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_DestroyOGLSurface");

    if (oglsdo->drawableType == OGLSD_PBUFFER) {
        if (glxsdo->drawable != 0) {
            j2d_glXDestroyPbuffer(awt_display, glxsdo->drawable);
            glxsdo->drawable = 0;
        }
    } else if (oglsdo->drawableType == OGLSD_WINDOW) {
        // X Window is free'd later by AWT code...
    }
}

/**
 * Makes the given context current to its associated "scratch" surface.  If
 * the operation is successful, this method will return JNI_TRUE; otherwise,
 * returns JNI_FALSE.
 */
static jboolean
GLXSD_MakeCurrentToScratch(JNIEnv *env, OGLContext *oglc)
{
    GLXCtxInfo *ctxInfo;

    J2dTraceLn(J2D_TRACE_INFO, "GLXSD_MakeCurrentToScratch");

    if (oglc == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "GLXSD_MakeCurrentToScratch: context is null");
        return JNI_FALSE;
    }

    ctxInfo = (GLXCtxInfo *)oglc->ctxInfo;
    if (!j2d_glXMakeContextCurrent(awt_display,
                                   ctxInfo->scratchSurface,
                                   ctxInfo->scratchSurface,
                                   ctxInfo->context))
    {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "GLXSD_MakeCurrentToScratch: could not make current");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/**
 * Returns a pointer (as a jlong) to the native GLXGraphicsConfigInfo
 * associated with the given OGLSDOps.  This method can be called from
 * shared code to retrieve the native GraphicsConfig data in a platform-
 * independent manner.
 */
jlong
OGLSD_GetNativeConfigInfo(OGLSDOps *oglsdo)
{
    GLXSDOps *glxsdo;

    if (oglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_GetNativeConfigInfo: ops are null");
        return 0L;
    }

    glxsdo = (GLXSDOps *)oglsdo->privOps;
    if (glxsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_GetNativeConfigInfo: glx ops are null");
        return 0L;
    }

    if (glxsdo->configData == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_GetNativeConfigInfo: config data is null");
        return 0L;
    }

    return ptr_to_jlong(glxsdo->configData->glxInfo);
}

/**
 * Makes the given GraphicsConfig's context current to its associated
 * "scratch" surface.  If there is a problem making the context current,
 * this method will return NULL; otherwise, returns a pointer to the
 * OGLContext that is associated with the given GraphicsConfig.
 */
OGLContext *
OGLSD_SetScratchSurface(JNIEnv *env, jlong pConfigInfo)
{
    GLXGraphicsConfigInfo *glxInfo =
        (GLXGraphicsConfigInfo *)jlong_to_ptr(pConfigInfo);
    OGLContext *oglc;

    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_SetScratchContext");

    if (glxInfo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_SetScratchContext: glx config info is null");
        return NULL;
    }

    oglc = glxInfo->context;
    if (!GLXSD_MakeCurrentToScratch(env, oglc)) {
        return NULL;
    }

    if (OGLC_IS_CAP_PRESENT(oglc, CAPS_EXT_FBOBJECT)) {
        // the GL_EXT_framebuffer_object extension is present, so this call
        // will ensure that we are bound to the scratch pbuffer (and not
        // some other framebuffer object)
        j2d_glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    return oglc;
}

/**
 * Makes a context current to the given source and destination
 * surfaces.  If there is a problem making the context current, this method
 * will return NULL; otherwise, returns a pointer to the OGLContext that is
 * associated with the destination surface.
 */
OGLContext *
OGLSD_MakeOGLContextCurrent(JNIEnv *env, OGLSDOps *srcOps, OGLSDOps *dstOps)
{
    GLXSDOps *dstGLXOps = (GLXSDOps *)dstOps->privOps;
    OGLContext *oglc;

    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_MakeOGLContextCurrent");

    oglc = dstGLXOps->configData->glxInfo->context;
    if (oglc == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_MakeOGLContextCurrent: context is null");
        return NULL;
    }

    if (dstOps->drawableType == OGLSD_FBOBJECT) {
        OGLContext *currentContext = OGLRenderQueue_GetCurrentContext();

        // first make sure we have a current context (if the context isn't
        // already current to some drawable, we will make it current to
        // its scratch surface)
        if (oglc != currentContext) {
            if (!GLXSD_MakeCurrentToScratch(env, oglc)) {
                return NULL;
            }
        }

        // now bind to the fbobject associated with the destination surface;
        // this means that all rendering will go into the fbobject destination
        // (note that we unbind the currently bound texture first; this is
        // recommended procedure when binding an fbobject)
        j2d_glBindTexture(dstOps->textureTarget, 0);
        j2d_glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, dstOps->fbobjectID);
    } else {
        GLXSDOps *srcGLXOps = (GLXSDOps *)srcOps->privOps;
        GLXCtxInfo *ctxinfo = (GLXCtxInfo *)oglc->ctxInfo;

        // make the context current
        if (!j2d_glXMakeContextCurrent(awt_display,
                                       dstGLXOps->drawable,
                                       srcGLXOps->drawable,
                                       ctxinfo->context))
        {
            J2dRlsTraceLn(J2D_TRACE_ERROR,
                "OGLSD_MakeOGLContextCurrent: could not make current");
            return NULL;
        }

        if (OGLC_IS_CAP_PRESENT(oglc, CAPS_EXT_FBOBJECT)) {
            // the GL_EXT_framebuffer_object extension is present, so we
            // must bind to the default (windowing system provided)
            // framebuffer
            j2d_glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        }
    }

    return oglc;
}

/**
 * This function initializes a native window surface and caches the window
 * bounds in the given OGLSDOps.  Returns JNI_TRUE if the operation was
 * successful; JNI_FALSE otherwise.
 */
jboolean
OGLSD_InitOGLWindow(JNIEnv *env, OGLSDOps *oglsdo)
{
    GLXSDOps *glxsdo;
    Window window;
#ifdef XAWT
    XWindowAttributes attr;
#else
    Widget widget;
#endif

    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_InitOGLWindow");

    if (oglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_InitOGLWindow: ops are null");
        return JNI_FALSE;
    }

    glxsdo = (GLXSDOps *)oglsdo->privOps;
    if (glxsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_InitOGLWindow: glx ops are null");
        return JNI_FALSE;
    }

#ifdef XAWT
    window = glxsdo->window;
    if (window == 0) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_InitOGLWindow: window is invalid");
        return JNI_FALSE;
    }

    XGetWindowAttributes(awt_display, window, &attr);
    oglsdo->width = attr.width;
    oglsdo->height = attr.height;
#else
    widget = glxsdo->widget;
    if (widget == NULL) {
        J2dTraceLn(J2D_TRACE_WARNING, "OGLSD_InitOGLWindow: widget is null");
    }

    if (!XtIsRealized(widget)) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_InitOGLWindow: widget is unrealized");
        return JNI_FALSE;
    }

    window = XtWindow(widget);
    oglsdo->width = widget->core.width;
    oglsdo->height = widget->core.height;
#endif

    oglsdo->drawableType = OGLSD_WINDOW;
    oglsdo->isOpaque = JNI_TRUE;
    oglsdo->xOffset = 0;
    oglsdo->yOffset = 0;
    glxsdo->drawable = window;
    glxsdo->xdrawable = window;

    J2dTraceLn2(J2D_TRACE_VERBOSE, "  created window: w=%d h=%d",
                oglsdo->width, oglsdo->height);

    return JNI_TRUE;
}

static int
GLXSD_BadAllocXErrHandler(Display *display, XErrorEvent *xerr)
{
    int ret = 0;
    if (xerr->error_code == BadAlloc) {
        surfaceCreationFailed = JNI_TRUE;
    } else {
        ret = (*xerror_saved_handler)(display, xerr);
    }
    return ret;
}

JNIEXPORT jboolean JNICALL
Java_sun_java2d_opengl_GLXSurfaceData_initPbuffer
    (JNIEnv *env, jobject glxsd,
     jlong pData, jlong pConfigInfo,
     jboolean isOpaque,
     jint width, jint height)
{
    OGLSDOps *oglsdo = (OGLSDOps *)jlong_to_ptr(pData);
    GLXGraphicsConfigInfo *glxinfo =
        (GLXGraphicsConfigInfo *)jlong_to_ptr(pConfigInfo);
    GLXSDOps *glxsdo;
    GLXPbuffer pbuffer;
    int attrlist[] = {GLX_PBUFFER_WIDTH, 0,
                      GLX_PBUFFER_HEIGHT, 0,
                      GLX_PRESERVED_CONTENTS, GL_FALSE, 0};

    J2dTraceLn3(J2D_TRACE_INFO,
                "GLXSurfaceData_initPbuffer: w=%d h=%d opq=%d",
                width, height, isOpaque);

    if (oglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "GLXSurfaceData_initPbuffer: ops are null");
        return JNI_FALSE;
    }

    glxsdo = (GLXSDOps *)oglsdo->privOps;
    if (glxsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "GLXSurfaceData_initPbuffer: glx ops are null");
        return JNI_FALSE;
    }

    if (glxinfo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "GLXSurfaceData_initPbuffer: glx config info is null");
        return JNI_FALSE;
    }

    attrlist[1] = width;
    attrlist[3] = height;

    surfaceCreationFailed = JNI_FALSE;
    EXEC_WITH_XERROR_HANDLER(
        GLXSD_BadAllocXErrHandler,
        pbuffer = j2d_glXCreatePbuffer(awt_display,
                                       glxinfo->fbconfig, attrlist));
    if ((pbuffer == 0) || surfaceCreationFailed) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
            "GLXSurfaceData_initPbuffer: could not create glx pbuffer");
        return JNI_FALSE;
    }

    oglsdo->drawableType = OGLSD_PBUFFER;
    oglsdo->isOpaque = isOpaque;
    oglsdo->width = width;
    oglsdo->height = height;
    oglsdo->xOffset = 0;
    oglsdo->yOffset = 0;

    glxsdo->drawable = pbuffer;
    glxsdo->xdrawable = 0;

    OGLSD_SetNativeDimensions(env, oglsdo, width, height);

    return JNI_TRUE;
}

void
OGLSD_SwapBuffers(JNIEnv *env, jlong window)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_SwapBuffers");

    if (window == 0L) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLSD_SwapBuffers: window is null");
        return;
    }

    j2d_glXSwapBuffers(awt_display, (Window)window);
}

#endif /* !HEADLESS */
