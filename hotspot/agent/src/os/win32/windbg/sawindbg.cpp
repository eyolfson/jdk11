/*
 * Copyright 2002-2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

// this is source code windbg based SA debugger agent to debug
// Dr. Watson dump files and process snapshots.

#include "sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal.h"

#ifdef _M_IA64
  #include "sun_jvm_hotspot_debugger_ia64_IA64ThreadContext.h"
  #define NPRGREG sun_jvm_hotspot_debugger_ia64_IA64ThreadContext_NPRGREG
#elif _M_IX86
  #include "sun_jvm_hotspot_debugger_x86_X86ThreadContext.h"
  #define NPRGREG sun_jvm_hotspot_debugger_x86_X86ThreadContext_NPRGREG
#elif _M_AMD64
  #include "sun_jvm_hotspot_debugger_amd64_AMD64ThreadContext.h"
  #define NPRGREG sun_jvm_hotspot_debugger_amd64_AMD64ThreadContext_NPRGREG
#else
  #error "SA windbg back-end is not supported for your cpu!"
#endif

#include <limits.h>
#include <windows.h>

#ifndef STDMETHODV
#define STDMETHODV(method) virtual HRESULT STDMETHODVCALLTYPE method
#endif

#define DEBUG_NO_IMPLEMENTATION
#include <dbgeng.h>
#include <dbghelp.h>

// simple template to manage array delete across early (error) returns

template <class T>
class AutoArrayPtr {
      T* m_ptr;
   public:
      AutoArrayPtr(T* ptr) : m_ptr(ptr) {
      }

      ~AutoArrayPtr() {
         delete [] m_ptr;
      }

      T* asPtr() {
         return m_ptr;
      }
};

class AutoJavaString {
      JNIEnv* m_env;
      jstring m_str;
      const char* m_buf;

   public:
      AutoJavaString(JNIEnv* env, jstring str, const char* buf)
        : m_env(env), m_str(str), m_buf(buf) {
      }

      ~AutoJavaString() {
         m_env->ReleaseStringUTFChars(m_str, m_buf);
      }

      operator const char* () {
         return m_buf;
      }
};

// field and method IDs we want here

static jfieldID imagePath_ID                    = 0;
static jfieldID symbolPath_ID                   = 0;
static jfieldID ptrIDebugClient_ID              = 0;
static jfieldID ptrIDebugControl_ID             = 0;
static jfieldID ptrIDebugDataSpaces_ID          = 0;
static jfieldID ptrIDebugOutputCallbacks_ID     = 0;
static jfieldID ptrIDebugAdvanced_ID            = 0;
static jfieldID ptrIDebugSymbols_ID             = 0;
static jfieldID ptrIDebugSystemObjects_ID       = 0;

static jmethodID addLoadObject_ID               = 0;
static jmethodID addThread_ID                   = 0;
static jmethodID createClosestSymbol_ID         = 0;
static jmethodID setThreadIntegerRegisterSet_ID = 0;

#define CHECK_EXCEPTION_(value) if(env->ExceptionOccurred()) { return value; }
#define CHECK_EXCEPTION if(env->ExceptionOccurred()) { return;}

#define THROW_NEW_DEBUGGER_EXCEPTION_(str, value) { \
                          throwNewDebuggerException(env, str); return value; }

#define THROW_NEW_DEBUGGER_EXCEPTION(str) { throwNewDebuggerException(env, str); \
 return;}

static void throwNewDebuggerException(JNIEnv* env, const char* errMsg) {
  env->ThrowNew(env->FindClass("sun/jvm/hotspot/debugger/DebuggerException"), errMsg);
}

/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_initIDs
  (JNIEnv *env, jclass clazz) {
  imagePath_ID = env->GetStaticFieldID(clazz, "imagePath", "Ljava/lang/String;");
  CHECK_EXCEPTION;

  symbolPath_ID = env->GetStaticFieldID(clazz, "symbolPath", "Ljava/lang/String;");
  CHECK_EXCEPTION;

  ptrIDebugClient_ID = env->GetFieldID(clazz, "ptrIDebugClient", "J");
  CHECK_EXCEPTION;

  ptrIDebugControl_ID = env->GetFieldID(clazz, "ptrIDebugControl", "J");
  CHECK_EXCEPTION;

  ptrIDebugDataSpaces_ID = env->GetFieldID(clazz, "ptrIDebugDataSpaces", "J");
  CHECK_EXCEPTION;

  ptrIDebugOutputCallbacks_ID = env->GetFieldID(clazz,
                                            "ptrIDebugOutputCallbacks", "J");
  CHECK_EXCEPTION;

  ptrIDebugAdvanced_ID = env->GetFieldID(clazz, "ptrIDebugAdvanced", "J");
  CHECK_EXCEPTION;

  ptrIDebugSymbols_ID = env->GetFieldID(clazz,
                                         "ptrIDebugSymbols", "J");
  CHECK_EXCEPTION;

  ptrIDebugSystemObjects_ID = env->GetFieldID(clazz,
                                         "ptrIDebugSystemObjects", "J");
  CHECK_EXCEPTION;

  addLoadObject_ID = env->GetMethodID(clazz, "addLoadObject",
                                         "(Ljava/lang/String;JJ)V");
  CHECK_EXCEPTION;

  addThread_ID = env->GetMethodID(clazz, "addThread", "(J)V");
  CHECK_EXCEPTION;

  createClosestSymbol_ID = env->GetMethodID(clazz, "createClosestSymbol",
    "(Ljava/lang/String;J)Lsun/jvm/hotspot/debugger/cdbg/ClosestSymbol;");
  CHECK_EXCEPTION;

  setThreadIntegerRegisterSet_ID = env->GetMethodID(clazz,
                                         "setThreadIntegerRegisterSet", "(J[J)V");
  CHECK_EXCEPTION;

}

// class for IDebugOutputCallbacks

class SAOutputCallbacks : public IDebugOutputCallbacks {
   LONG  m_refCount;
   char* m_msgBuffer;

   public:
      SAOutputCallbacks() : m_refCount(0), m_msgBuffer(0) {
      }

      ~SAOutputCallbacks() {
         clearBuffer();
      }

      const char* getBuffer() const {
         return m_msgBuffer;
      }

      void clearBuffer() {
         if (m_msgBuffer) {
            free(m_msgBuffer);
            m_msgBuffer = 0;
         }
      }

      STDMETHOD_(ULONG, AddRef)(THIS);
      STDMETHOD_(ULONG, Release)(THIS);
      STDMETHOD(QueryInterface)(THIS_
                                IN REFIID interfaceId,
                                OUT PVOID* ppInterface);
      STDMETHOD(Output)(THIS_
                        IN ULONG mask,
                        IN PCSTR msg);
};

STDMETHODIMP_(ULONG) SAOutputCallbacks::AddRef(THIS) {
   InterlockedIncrement(&m_refCount);
   return m_refCount;
}

STDMETHODIMP_(ULONG) SAOutputCallbacks::Release(THIS) {
   LONG retVal;
   InterlockedDecrement(&m_refCount);
   retVal = m_refCount;
   if (retVal == 0) {
      delete this;
   }
   return retVal;
}

STDMETHODIMP SAOutputCallbacks::QueryInterface(THIS_
                                          IN REFIID interfaceId,
                                          OUT PVOID* ppInterface) {
   *ppInterface = 0;
   HRESULT res = E_NOINTERFACE;
   if (TRUE == IsEqualIID(interfaceId, __uuidof(IUnknown)) ||
       TRUE == IsEqualIID(interfaceId, __uuidof(IDebugOutputCallbacks))) {
      *ppInterface = (IDebugOutputCallbacks*) this;
      AddRef();
      res = S_OK;
   }
   return res;
}

STDMETHODIMP SAOutputCallbacks::Output(THIS_
                                       IN ULONG mask,
                                       IN PCSTR msg) {
   int len = (int) (strlen(msg) + 1);
   if (m_msgBuffer == 0) {
      m_msgBuffer = (char*) malloc(len);
      if (m_msgBuffer == 0) {
         fprintf(stderr, "out of memory debugger output!\n");
         return S_FALSE;
      }
      strcpy(m_msgBuffer, msg);
   } else {
      m_msgBuffer = (char*) realloc(m_msgBuffer, len + strlen(m_msgBuffer));
      if (m_msgBuffer == 0) {
         fprintf(stderr, "out of memory debugger output!\n");
         return S_FALSE;
      }
      strcat(m_msgBuffer, msg);
   }
   return S_OK;
}

static bool getWindbgInterfaces(JNIEnv* env, jobject obj) {
  // get windbg interfaces ..

  IDebugClient* ptrIDebugClient = 0;
  if (DebugCreate(__uuidof(IDebugClient), (PVOID*) &ptrIDebugClient) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: not able to create IDebugClient object!", false);
  }
  env->SetLongField(obj, ptrIDebugClient_ID, (jlong) ptrIDebugClient);

  IDebugControl* ptrIDebugControl = 0;
  if (ptrIDebugClient->QueryInterface(__uuidof(IDebugControl), (PVOID*) &ptrIDebugControl)
     != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: not able to get IDebugControl", false);
  }
  env->SetLongField(obj, ptrIDebugControl_ID, (jlong) ptrIDebugControl);

  IDebugDataSpaces* ptrIDebugDataSpaces = 0;
  if (ptrIDebugClient->QueryInterface(__uuidof(IDebugDataSpaces), (PVOID*) &ptrIDebugDataSpaces)
     != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: not able to get IDebugDataSpaces object!", false);
  }
  env->SetLongField(obj, ptrIDebugDataSpaces_ID, (jlong) ptrIDebugDataSpaces);

  SAOutputCallbacks* ptrIDebugOutputCallbacks = new SAOutputCallbacks();
  ptrIDebugOutputCallbacks->AddRef();
  env->SetLongField(obj, ptrIDebugOutputCallbacks_ID, (jlong) ptrIDebugOutputCallbacks);
  CHECK_EXCEPTION_(false);

  IDebugAdvanced* ptrIDebugAdvanced = 0;
  if (ptrIDebugClient->QueryInterface(__uuidof(IDebugAdvanced), (PVOID*) &ptrIDebugAdvanced)
     != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: not able to get IDebugAdvanced object!", false);
  }
  env->SetLongField(obj, ptrIDebugAdvanced_ID, (jlong) ptrIDebugAdvanced);

  IDebugSymbols* ptrIDebugSymbols = 0;
  if (ptrIDebugClient->QueryInterface(__uuidof(IDebugSymbols), (PVOID*) &ptrIDebugSymbols)
     != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: not able to get IDebugSymbols object!", false);
  }
  env->SetLongField(obj, ptrIDebugSymbols_ID, (jlong) ptrIDebugSymbols);

  IDebugSystemObjects* ptrIDebugSystemObjects = 0;
  if (ptrIDebugClient->QueryInterface(__uuidof(IDebugSystemObjects), (PVOID*) &ptrIDebugSystemObjects)
     != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: not able to get IDebugSystemObjects object!", false);
  }
  env->SetLongField(obj, ptrIDebugSystemObjects_ID, (jlong) ptrIDebugSystemObjects);

  return true;
}

static bool setImageAndSymbolPath(JNIEnv* env, jobject obj) {
  jboolean isCopy;
  jclass clazz = env->GetObjectClass(obj);
  jstring path;
  const char* buf;

  path = (jstring) env->GetStaticObjectField(clazz, imagePath_ID);
  buf = env->GetStringUTFChars(path, &isCopy);
  CHECK_EXCEPTION_(false);
  AutoJavaString imagePath(env, path, buf);

  path = (jstring) env->GetStaticObjectField(clazz, symbolPath_ID);
  buf = env->GetStringUTFChars(path, &isCopy);
  CHECK_EXCEPTION_(false);
  AutoJavaString symbolPath(env, path, buf);

  IDebugSymbols* ptrIDebugSymbols = (IDebugSymbols*) env->GetLongField(obj,
                                                      ptrIDebugSymbols_ID);
  CHECK_EXCEPTION_(false);

  ptrIDebugSymbols->SetImagePath(imagePath);
  ptrIDebugSymbols->SetSymbolPath(symbolPath);
  return true;
}

static bool openDumpFile(JNIEnv* env, jobject obj, jstring coreFileName) {
  // open the dump file
  jboolean isCopy;
  const char* buf = env->GetStringUTFChars(coreFileName, &isCopy);
  CHECK_EXCEPTION_(false);
  AutoJavaString coreFile(env, coreFileName, buf);
  if (setImageAndSymbolPath(env, obj) == false) {
     return false;
  }

  IDebugClient* ptrIDebugClient = (IDebugClient*) env->GetLongField(obj,
                                                      ptrIDebugClient_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugClient->OpenDumpFile(coreFile) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: OpenDumpFile failed!", false);
  }

  IDebugControl* ptrIDebugControl = (IDebugControl*) env->GetLongField(obj,
                                                     ptrIDebugControl_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugControl->WaitForEvent(DEBUG_WAIT_DEFAULT, INFINITE) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: WaitForEvent failed!", false);
  }

  return true;
}


static bool attachToProcess(JNIEnv* env, jobject obj, jint pid) {
  if (setImageAndSymbolPath(env, obj) == false) {
     return false;
  }
  IDebugClient* ptrIDebugClient = (IDebugClient*) env->GetLongField(obj,
                                                      ptrIDebugClient_ID);
  CHECK_EXCEPTION_(false);

  /***********************************************************************************

     We are attaching to a process in 'read-only' mode. i.e., we do not want to
     put breakpoints, suspend/resume threads etc. For read-only JDI and HSDB kind of
     usage this should suffice. We are not intending to use this for full-fledged
     ProcessControl implementation to be used with BugSpotAgent.

     Please refer to DEBUG_ATTACH_NONINVASIVE mode source comments from dbgeng.h.
     In this mode, debug engine does not call DebugActiveProrcess. i.e., we are not
     actually debugging at all. We can safely 'detach' from the process anytime
     we want and debuggee process is left as is on all Windows variants.

     This also makes JDI-on-SA installation/usage simpler because with this we would
     not need a tool like ServiceInstaller from http://www.kcmultimedia.com/smaster.

  ***********************************************************************************/


  if (ptrIDebugClient->AttachProcess(0, pid, DEBUG_ATTACH_NONINVASIVE) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: AttachProcess failed!", false);
  }

  IDebugControl* ptrIDebugControl = (IDebugControl*) env->GetLongField(obj,
                                                     ptrIDebugControl_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugControl->WaitForEvent(DEBUG_WAIT_DEFAULT, INFINITE) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: WaitForEvent failed!", false);
  }

  return true;
}


static bool addLoadObjects(JNIEnv* env, jobject obj) {
  IDebugSymbols* ptrIDebugSymbols = (IDebugSymbols*) env->GetLongField(obj,
                                                      ptrIDebugSymbols_ID);
  CHECK_EXCEPTION_(false);
  ULONG loaded = 0, unloaded = 0;
  if (ptrIDebugSymbols->GetNumberModules(&loaded, &unloaded) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: GetNumberModules failed!", false);
  }

  AutoArrayPtr<DEBUG_MODULE_PARAMETERS> params(new DEBUG_MODULE_PARAMETERS[loaded]);

  if (params.asPtr() == 0) {
      THROW_NEW_DEBUGGER_EXCEPTION_("out of memory to allocate debug module params!", false);
  }

  if (ptrIDebugSymbols->GetModuleParameters(loaded, 0, NULL, params.asPtr()) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: GetModuleParameters failed!", false);
  }

  for (int u = 0; u < (int)loaded; u++) {
     TCHAR imageName[MAX_PATH];
     if (ptrIDebugSymbols->GetModuleNames(DEBUG_ANY_ID, params.asPtr()[u].Base,
                                      imageName, MAX_PATH, NULL, NULL,
                                      0, NULL, NULL, 0, NULL) != S_OK) {
        THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: GetModuleNames failed!", false);
     }

     jstring strName = env->NewStringUTF(imageName);
     CHECK_EXCEPTION_(false);
     env->CallVoidMethod(obj, addLoadObject_ID, strName, (jlong) params.asPtr()[u].Size,
                               (jlong) params.asPtr()[u].Base);
     CHECK_EXCEPTION_(false);
  }

  return true;
}

static bool addThreads(JNIEnv* env, jobject obj) {
  IDebugSystemObjects* ptrIDebugSystemObjects = (IDebugSystemObjects*) env->GetLongField(obj,
                                                      ptrIDebugSystemObjects_ID);
  CHECK_EXCEPTION_(false);

  ULONG numThreads = 0;
  if (ptrIDebugSystemObjects->GetNumberThreads(&numThreads) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: GetNumberThreads failed!", false);
  }

  AutoArrayPtr<ULONG> ptrSysThreadIds = new ULONG[numThreads];

  if (ptrSysThreadIds.asPtr() == 0) {
     THROW_NEW_DEBUGGER_EXCEPTION_("out of memory to allocate thread ids!", false);
  }

  AutoArrayPtr<ULONG> ptrThreadIds = new ULONG[numThreads];

  if (ptrThreadIds.asPtr() == 0) {
     THROW_NEW_DEBUGGER_EXCEPTION_("out of memory to allocate thread ids!", false);
  }

  if (ptrIDebugSystemObjects->GetThreadIdsByIndex(0, numThreads,
                                      ptrThreadIds.asPtr(), ptrSysThreadIds.asPtr()) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: GetThreadIdsByIndex failed!", false);
  }


  IDebugAdvanced* ptrIDebugAdvanced = (IDebugAdvanced*) env->GetLongField(obj,
                                                      ptrIDebugAdvanced_ID);
  CHECK_EXCEPTION_(false);

  // for each thread, get register context and save it.
  for (ULONG t = 0; t < numThreads; t++) {
     if (ptrIDebugSystemObjects->SetCurrentThreadId(ptrThreadIds.asPtr()[t]) != S_OK) {
        THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: SetCurrentThread failed!", false);
     }

     jlongArray regs = env->NewLongArray(NPRGREG);
     CHECK_EXCEPTION_(false);

     jboolean isCopy = JNI_FALSE;
     jlong* ptrRegs = env->GetLongArrayElements(regs, &isCopy);
     CHECK_EXCEPTION_(false);

     // copy register values from the CONTEXT struct
     CONTEXT context;
     memset(&context, 0, sizeof(CONTEXT));

#undef REG_INDEX
#ifdef _M_IA64
     #define REG_INDEX(x) sun_jvm_hotspot_debugger_ia64_IA64ThreadContext_##x

     context.ContextFlags = CONTEXT_FULL | CONTEXT_DEBUG;
     ptrIDebugAdvanced->GetThreadContext(&context, sizeof(CONTEXT));

     ptrRegs[REG_INDEX(GR0)]  = 0; // always 0
     ptrRegs[REG_INDEX(GR1)]  = context.IntGp;  // r1
     ptrRegs[REG_INDEX(GR2)]  = context.IntT0;  // r2-r3
     ptrRegs[REG_INDEX(GR3)]  = context.IntT1;
     ptrRegs[REG_INDEX(GR4)]  = context.IntS0;  // r4-r7
     ptrRegs[REG_INDEX(GR5)]  = context.IntS1;
     ptrRegs[REG_INDEX(GR6)]  = context.IntS2;
     ptrRegs[REG_INDEX(GR7)]  = context.IntS3;
     ptrRegs[REG_INDEX(GR8)]  = context.IntV0;  // r8
     ptrRegs[REG_INDEX(GR9)]  = context.IntT2;  // r9-r11
     ptrRegs[REG_INDEX(GR10)] = context.IntT3;
     ptrRegs[REG_INDEX(GR11)] = context.IntT4;
     ptrRegs[REG_INDEX(GR12)] = context.IntSp;  // r12 stack pointer
     ptrRegs[REG_INDEX(GR13)] = context.IntTeb; // r13 teb
     ptrRegs[REG_INDEX(GR14)] = context.IntT5;  // r14-r31
     ptrRegs[REG_INDEX(GR15)] = context.IntT6;
     ptrRegs[REG_INDEX(GR16)] = context.IntT7;
     ptrRegs[REG_INDEX(GR17)] = context.IntT8;
     ptrRegs[REG_INDEX(GR18)] = context.IntT9;
     ptrRegs[REG_INDEX(GR19)] = context.IntT10;
     ptrRegs[REG_INDEX(GR20)] = context.IntT11;
     ptrRegs[REG_INDEX(GR21)] = context.IntT12;
     ptrRegs[REG_INDEX(GR22)] = context.IntT13;
     ptrRegs[REG_INDEX(GR23)] = context.IntT14;
     ptrRegs[REG_INDEX(GR24)] = context.IntT15;
     ptrRegs[REG_INDEX(GR25)] = context.IntT16;
     ptrRegs[REG_INDEX(GR26)] = context.IntT17;
     ptrRegs[REG_INDEX(GR27)] = context.IntT18;
     ptrRegs[REG_INDEX(GR28)] = context.IntT19;
     ptrRegs[REG_INDEX(GR29)] = context.IntT20;
     ptrRegs[REG_INDEX(GR30)] = context.IntT21;
     ptrRegs[REG_INDEX(GR31)] = context.IntT22;

     ptrRegs[REG_INDEX(INT_NATS)] = context.IntNats;
     ptrRegs[REG_INDEX(PREDS)]    = context.Preds;

     ptrRegs[REG_INDEX(BR_RP)] = context.BrRp;
     ptrRegs[REG_INDEX(BR1)]   = context.BrS0;  // b1-b5
     ptrRegs[REG_INDEX(BR2)]   = context.BrS1;
     ptrRegs[REG_INDEX(BR3)]   = context.BrS2;
     ptrRegs[REG_INDEX(BR4)]   = context.BrS3;
     ptrRegs[REG_INDEX(BR5)]   = context.BrS4;
     ptrRegs[REG_INDEX(BR6)]   = context.BrT0;  // b6-b7
     ptrRegs[REG_INDEX(BR7)]   = context.BrT1;

     ptrRegs[REG_INDEX(AP_UNAT)] = context.ApUNAT;
     ptrRegs[REG_INDEX(AP_LC)]   = context.ApLC;
     ptrRegs[REG_INDEX(AP_EC)]   = context.ApEC;
     ptrRegs[REG_INDEX(AP_CCV)]  = context.ApCCV;
     ptrRegs[REG_INDEX(AP_DCR)]  = context.ApDCR;

     ptrRegs[REG_INDEX(RS_PFS)]      = context.RsPFS;
     ptrRegs[REG_INDEX(RS_BSP)]      = context.RsBSP;
     ptrRegs[REG_INDEX(RS_BSPSTORE)] = context.RsBSPSTORE;
     ptrRegs[REG_INDEX(RS_RSC)]      = context.RsRSC;
     ptrRegs[REG_INDEX(RS_RNAT)]     = context.RsRNAT;

     ptrRegs[REG_INDEX(ST_IPSR)] = context.StIPSR;
     ptrRegs[REG_INDEX(ST_IIP)]  = context.StIIP;
     ptrRegs[REG_INDEX(ST_IFS)]  = context.StIFS;

     ptrRegs[REG_INDEX(DB_I0)] = context.DbI0;
     ptrRegs[REG_INDEX(DB_I1)] = context.DbI1;
     ptrRegs[REG_INDEX(DB_I2)] = context.DbI2;
     ptrRegs[REG_INDEX(DB_I3)] = context.DbI3;
     ptrRegs[REG_INDEX(DB_I4)] = context.DbI4;
     ptrRegs[REG_INDEX(DB_I5)] = context.DbI5;
     ptrRegs[REG_INDEX(DB_I6)] = context.DbI6;
     ptrRegs[REG_INDEX(DB_I7)] = context.DbI7;

     ptrRegs[REG_INDEX(DB_D0)] = context.DbD0;
     ptrRegs[REG_INDEX(DB_D1)] = context.DbD1;
     ptrRegs[REG_INDEX(DB_D2)] = context.DbD2;
     ptrRegs[REG_INDEX(DB_D3)] = context.DbD3;
     ptrRegs[REG_INDEX(DB_D4)] = context.DbD4;
     ptrRegs[REG_INDEX(DB_D5)] = context.DbD5;
     ptrRegs[REG_INDEX(DB_D6)] = context.DbD6;
     ptrRegs[REG_INDEX(DB_D7)] = context.DbD7;

#elif _M_IX86
     #define REG_INDEX(x) sun_jvm_hotspot_debugger_x86_X86ThreadContext_##x

     context.ContextFlags = CONTEXT_FULL | CONTEXT_DEBUG_REGISTERS;
     ptrIDebugAdvanced->GetThreadContext(&context, sizeof(CONTEXT));

     ptrRegs[REG_INDEX(GS)]  = context.SegGs;
     ptrRegs[REG_INDEX(FS)]  = context.SegFs;
     ptrRegs[REG_INDEX(ES)]  = context.SegEs;
     ptrRegs[REG_INDEX(DS)]  = context.SegDs;

     ptrRegs[REG_INDEX(EDI)] = context.Edi;
     ptrRegs[REG_INDEX(ESI)] = context.Esi;
     ptrRegs[REG_INDEX(EBX)] = context.Ebx;
     ptrRegs[REG_INDEX(EDX)] = context.Edx;
     ptrRegs[REG_INDEX(ECX)] = context.Ecx;
     ptrRegs[REG_INDEX(EAX)] = context.Eax;

     ptrRegs[REG_INDEX(FP)] = context.Ebp;
     ptrRegs[REG_INDEX(PC)] = context.Eip;
     ptrRegs[REG_INDEX(CS)]  = context.SegCs;
     ptrRegs[REG_INDEX(EFL)] = context.EFlags;
     ptrRegs[REG_INDEX(SP)] = context.Esp;
     ptrRegs[REG_INDEX(SS)]  = context.SegSs;

     ptrRegs[REG_INDEX(DR0)] = context.Dr0;
     ptrRegs[REG_INDEX(DR1)] = context.Dr1;
     ptrRegs[REG_INDEX(DR2)] = context.Dr2;
     ptrRegs[REG_INDEX(DR3)] = context.Dr3;
     ptrRegs[REG_INDEX(DR6)] = context.Dr6;
     ptrRegs[REG_INDEX(DR7)] = context.Dr7;

#elif _M_AMD64
     #define REG_INDEX(x) sun_jvm_hotspot_debugger_amd64_AMD64ThreadContext_##x

     context.ContextFlags = CONTEXT_FULL | CONTEXT_DEBUG_REGISTERS;
     ptrIDebugAdvanced->GetThreadContext(&context, sizeof(CONTEXT));

     // Segment Registers and processor flags
     ptrRegs[REG_INDEX(CS)]  = context.SegCs;
     ptrRegs[REG_INDEX(DS)]  = context.SegDs;
     ptrRegs[REG_INDEX(ES)]  = context.SegEs;
     ptrRegs[REG_INDEX(FS)]  = context.SegFs;
     ptrRegs[REG_INDEX(GS)]  = context.SegGs;
     ptrRegs[REG_INDEX(SS)]  = context.SegSs;
     ptrRegs[REG_INDEX(RFL)] = context.EFlags;

     // Integer registers
     ptrRegs[REG_INDEX(RDI)] = context.Rdi;
     ptrRegs[REG_INDEX(RSI)] = context.Rsi;
     ptrRegs[REG_INDEX(RAX)] = context.Rax;
     ptrRegs[REG_INDEX(RCX)] = context.Rcx;
     ptrRegs[REG_INDEX(RDX)] = context.Rdx;
     ptrRegs[REG_INDEX(RBX)] = context.Rbx;
     ptrRegs[REG_INDEX(RBP)] = context.Rbp;
     ptrRegs[REG_INDEX(RSP)] = context.Rsp;

     ptrRegs[REG_INDEX(R8)]  = context.R8;
     ptrRegs[REG_INDEX(R9)]  = context.R9;
     ptrRegs[REG_INDEX(R10)] = context.R10;
     ptrRegs[REG_INDEX(R11)] = context.R11;
     ptrRegs[REG_INDEX(R12)] = context.R12;
     ptrRegs[REG_INDEX(R13)] = context.R13;
     ptrRegs[REG_INDEX(R14)] = context.R14;
     ptrRegs[REG_INDEX(R15)] = context.R15;

     // Program counter
     ptrRegs[REG_INDEX(RIP)] = context.Rip;
#endif

     env->ReleaseLongArrayElements(regs, ptrRegs, JNI_COMMIT);
     CHECK_EXCEPTION_(false);

     env->CallVoidMethod(obj, setThreadIntegerRegisterSet_ID,
                        (jlong) ptrThreadIds.asPtr()[t], regs);
     CHECK_EXCEPTION_(false);

     ULONG sysId;
     if (ptrIDebugSystemObjects->GetCurrentThreadSystemId(&sysId) != S_OK) {
        THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: GetCurrentThreadSystemId failed!", false);
     }

     env->CallVoidMethod(obj, addThread_ID, (jlong) sysId);
     CHECK_EXCEPTION_(false);
  }

  return true;
}

/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    attach0
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_attach0__Ljava_lang_String_2Ljava_lang_String_2
  (JNIEnv *env, jobject obj, jstring execName, jstring coreFileName) {

  if (getWindbgInterfaces(env, obj) == false) {
     return;
  }

  if (openDumpFile(env, obj, coreFileName) == false) {
     return;
  }

  if (addLoadObjects(env, obj) == false) {
     return;
  }

  if (addThreads(env, obj) == false) {
     return;
  }
}

/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    attach0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_attach0__I
  (JNIEnv *env, jobject obj, jint pid) {

  if (getWindbgInterfaces(env, obj) == false) {
     return;
  }

  if (attachToProcess(env, obj, pid) == false) {
     return;
  }

  if (addLoadObjects(env, obj) == false) {
     return;
  }

  if (addThreads(env, obj) == false) {
     return;
  }
}


static bool releaseWindbgInterfaces(JNIEnv* env, jobject obj) {
  IDebugDataSpaces* ptrIDebugDataSpaces = (IDebugDataSpaces*) env->GetLongField(obj,
                                                      ptrIDebugDataSpaces_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugDataSpaces != 0) {
     ptrIDebugDataSpaces->Release();
  }

  IDebugOutputCallbacks* ptrIDebugOutputCallbacks = (IDebugOutputCallbacks*)
                          env->GetLongField(obj, ptrIDebugOutputCallbacks_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugOutputCallbacks != 0) {
     ptrIDebugOutputCallbacks->Release();
  }

  IDebugAdvanced* ptrIDebugAdvanced = (IDebugAdvanced*) env->GetLongField(obj,
                                                      ptrIDebugAdvanced_ID);
  CHECK_EXCEPTION_(false);

  if (ptrIDebugAdvanced != 0) {
     ptrIDebugAdvanced->Release();
  }

  IDebugSymbols* ptrIDebugSymbols = (IDebugSymbols*) env->GetLongField(obj,
                                                      ptrIDebugSymbols_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugSymbols != 0) {
     ptrIDebugSymbols->Release();
  }

  IDebugSystemObjects* ptrIDebugSystemObjects = (IDebugSystemObjects*) env->GetLongField(obj,
                                                      ptrIDebugSystemObjects_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugSystemObjects != 0) {
     ptrIDebugSystemObjects->Release();
  }

  IDebugControl* ptrIDebugControl = (IDebugControl*) env->GetLongField(obj,
                                                     ptrIDebugControl_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugControl != 0) {
     ptrIDebugControl->Release();
  }

  IDebugClient* ptrIDebugClient = (IDebugClient*) env->GetLongField(obj,
                                                      ptrIDebugClient_ID);
  CHECK_EXCEPTION_(false);
  if (ptrIDebugClient != 0) {
     ptrIDebugClient->Release();
  }

  return true;
}

/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    detach0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_detach0
  (JNIEnv *env, jobject obj) {
  IDebugClient* ptrIDebugClient = (IDebugClient*) env->GetLongField(obj,
                                                      ptrIDebugClient_ID);
  CHECK_EXCEPTION;
  ptrIDebugClient->DetachProcesses();
  releaseWindbgInterfaces(env, obj);
}


/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    readBytesFromProcess0
 * Signature: (JJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_readBytesFromProcess0
  (JNIEnv *env, jobject obj, jlong address, jlong numBytes) {
  jbyteArray byteArray = env->NewByteArray((long) numBytes);
  CHECK_EXCEPTION_(0);

  jboolean isCopy = JNI_FALSE;
  jbyte* bytePtr = env->GetByteArrayElements(byteArray, &isCopy);
  CHECK_EXCEPTION_(0);

  IDebugDataSpaces* ptrIDebugDataSpaces = (IDebugDataSpaces*) env->GetLongField(obj,
                                                       ptrIDebugDataSpaces_ID);
  CHECK_EXCEPTION_(0);

  ULONG bytesRead;
  if (ptrIDebugDataSpaces->ReadVirtual((ULONG64) address, (PVOID) bytePtr,
                                  (ULONG)numBytes, &bytesRead) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: ReadVirtual failed!", 0);
  }

  if (bytesRead != numBytes) {
     return 0;
  }

  env->ReleaseByteArrayElements(byteArray, bytePtr, 0);
  CHECK_EXCEPTION_(0);

  return byteArray;
}

/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    getThreadIdFromSysId0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_getThreadIdFromSysId0
  (JNIEnv *env, jobject obj, jlong sysId) {
  IDebugSystemObjects* ptrIDebugSystemObjects = (IDebugSystemObjects*) env->GetLongField(obj,
                                                    ptrIDebugSystemObjects_ID);
  CHECK_EXCEPTION_(0);

  ULONG id = 0;
  if (ptrIDebugSystemObjects->GetThreadIdBySystemId((ULONG)sysId, &id) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: GetThreadIdBySystemId failed!", 0);
  }

  return (jlong) id;
}

// manage COM 'auto' pointers (to avoid multiple Release
// calls at every early (exception) returns). Similar to AutoArrayPtr.

template <class T>
class AutoCOMPtr {
      T* m_ptr;

   public:
      AutoCOMPtr(T* ptr) : m_ptr(ptr) {
      }

      ~AutoCOMPtr() {
         if (m_ptr) {
            m_ptr->Release();
         }
      }

      T* operator->() {
         return m_ptr;
      }
};

/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    consoleExecuteCommand0
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_consoleExecuteCommand0
  (JNIEnv *env, jobject obj, jstring cmd) {
  jboolean isCopy = JNI_FALSE;
  const char* buf = env->GetStringUTFChars(cmd, &isCopy);
  CHECK_EXCEPTION_(0);
  AutoJavaString command(env, cmd, buf);

  IDebugClient* ptrIDebugClient = (IDebugClient*) env->GetLongField(obj, ptrIDebugClient_ID);
  CHECK_EXCEPTION_(0);

  IDebugClient*  tmpClientPtr = 0;
  if (ptrIDebugClient->CreateClient(&tmpClientPtr) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: CreateClient failed!", 0);
  }
  AutoCOMPtr<IDebugClient> tmpClient(tmpClientPtr);

  IDebugControl* tmpControlPtr = 0;
  if (tmpClient->QueryInterface(__uuidof(IDebugControl), (PVOID*) &tmpControlPtr) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: QueryInterface (IDebugControl) failed", 0);
  }
  AutoCOMPtr<IDebugControl> tmpControl(tmpControlPtr);

  SAOutputCallbacks* saOutputCallbacks = (SAOutputCallbacks*) env->GetLongField(obj,
                                                                   ptrIDebugOutputCallbacks_ID);
  CHECK_EXCEPTION_(0);

  saOutputCallbacks->clearBuffer();

  if (tmpClient->SetOutputCallbacks(saOutputCallbacks) != S_OK) {
     THROW_NEW_DEBUGGER_EXCEPTION_("Windbg Error: SetOutputCallbacks failed!", 0);
  }

  tmpControl->Execute(DEBUG_OUTPUT_VERBOSE, command, DEBUG_EXECUTE_DEFAULT);

  const char* output = saOutputCallbacks->getBuffer();
  if (output == 0) {
     output = "";
  }

  jstring res = env->NewStringUTF(output);
  saOutputCallbacks->clearBuffer();
  return res;
}

/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    lookupByName0
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */

JNIEXPORT jlong JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_lookupByName0
(JNIEnv *env, jobject obj, jstring objName, jstring sym) {
  IDebugSymbols* ptrIDebugSymbols = (IDebugSymbols*) env->GetLongField(obj,
                                                      ptrIDebugSymbols_ID);
  CHECK_EXCEPTION_(0);

  jboolean isCopy;
  const char* buf = env->GetStringUTFChars(sym, &isCopy);
  CHECK_EXCEPTION_(0);
  AutoJavaString name(env, sym, buf);

  ULONG64 offset = 0L;
  if (strstr(name, "::") != 0) {
    ptrIDebugSymbols->AddSymbolOptions(SYMOPT_UNDNAME);
  } else {
    ptrIDebugSymbols->RemoveSymbolOptions(SYMOPT_UNDNAME);
  }
  if (ptrIDebugSymbols->GetOffsetByName(name, &offset) != S_OK) {
    return (jlong) 0;
  }
  return (jlong) offset;
}

#define SYMBOL_BUFSIZE 512
/*
 * Class:     sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal
 * Method:    lookupByAddress0
 * Signature: (J)Lsun/jvm/hotspot/debugger/cdbg/ClosestSymbol;
 */
JNIEXPORT jobject JNICALL Java_sun_jvm_hotspot_debugger_windbg_WindbgDebuggerLocal_lookupByAddress0
(JNIEnv *env, jobject obj, jlong address) {
  IDebugSymbols* ptrIDebugSymbols = (IDebugSymbols*) env->GetLongField(obj,
                                                      ptrIDebugSymbols_ID);
  CHECK_EXCEPTION_(0);

  ULONG64 disp = 0L;
  char buf[SYMBOL_BUFSIZE];
  memset(buf, 0, sizeof(buf));

  if (ptrIDebugSymbols->GetNameByOffset(address, buf, sizeof(buf),0,&disp)
      != S_OK) {
    return 0;
  }

  jstring sym = env->NewStringUTF(buf);
  CHECK_EXCEPTION_(0);
  jobject res = env->CallObjectMethod(obj, createClosestSymbol_ID, sym, disp);
  CHECK_EXCEPTION_(0);
  return res;
}
