/*
Main.cxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#include <assert.h>
#include <callgl.hxx>

#include <vob/jni/Types.hxx>
#include <vob/jni/Strings.hxx>
#include <vob/glerr.hxx>

#include <vob/Texture.hxx>
#include <vob/Debug.hxx>


#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"


namespace Vob {
namespace JNI {
DBGVAR(dbg, "JNI.general");
DBGVAR(dbg_event, "JNI.event");
DBGVAR(dbg_convert, "JNI.convert");

ObjectStorer<Vob0> vob0s("Vob0");
ObjectStorer<Vob1> vob1s("Vob1");
ObjectStorer<Vob2> vob2s("Vob2");
ObjectStorer<Vob3> vob3s("Vob3(none)");
ObjectStorer<Vob> vobNs("VobN");

Os::WindowSystem *ws;

ObjectStorer<ByteVector> bytevectors("bytevectors");

extern Texture::TextureParam textureParams(JNIEnv *env, jobjectArray params) ;

ObjectStorer<Os::RenderingSurface> windows("windows");
Os::Window *defaultWindow; // A kludge

Os::RenderingSurface *getWindowByWID(int wid) {
    return  (wid<0 ? defaultWindow : windows.get(wid));
}

JNIEnv *jnienv_eventloop;

vector<int> contextStack;

void setWindow(int wid) {
    Os::RenderingSurface *win = getWindowByWID(wid);
    if(!win->setCurrent()) {
	  cerr << "Couldn't set window to current -> returning\n";
	  return;
    }
    contextStack.push_back(wid);
    DBG(dbg) << "Set current "<<wid<<"\n";

}
void releaseWindow() {
    if(contextStack.size() == 0) {
	  cerr << "TRYING TO RELEASE CONTEXT WHEN STACK EMPTY!!! PLEASE REPORT BUG!";
	  return;
    }
    int wid = contextStack[contextStack.size()-1];
    contextStack.pop_back();
    Os::RenderingSurface *win = getWindowByWID(wid);
    if(!win->releaseCurrent()) {
	  cerr << "Couldn't release window current!!!";
	  exit(17);
    }
    if(contextStack.size() != 0) {
	getWindowByWID(contextStack[contextStack.size()-1])->setCurrent();
    }
    DBG(dbg) << "Release current "<<wid<<"\n";
}


void javaExc(JNIEnv *env, const char *where) {
    if(env->ExceptionOccurred()) {
	cerr << "Exception in "<<where<<"\n";
         env->ExceptionDescribe();
         env->ExceptionClear();
    }
}

bool GLERR_JNI_impl(JNIEnv *env, const char *file, int line) {
    int errorVariable = glGetError();
    if(errorVariable != GL_NO_ERROR) {
	cout << "===== JNI OPENGL ERROR "<<file<<" "<<line 
	    <<"  "<<gluErrorString(errorVariable)<<"\n";
	jclass errclass = env->FindClass("java/lang/Error");
	env->ThrowNew(errclass, "OpenGL error!");
	
	return true;
    }
    return false;
}

struct GZZJNIEventHandler : public Os::Eventhandler {

    jobject globalRef;
    jclass globalclass; // must keep for mid to remain valid
    jmethodID mid_repaint;
    jmethodID mid_keystroke;
    jmethodID mid_mousepress;
    jmethodID mid_timeout;
    jmethodID mid_windowClosed;

    GZZJNIEventHandler(JNIEnv *env, jobject globalRef) : globalRef(globalRef) {
	if(globalRef) {
	    jclass cls = env->GetObjectClass(globalRef);
	    globalclass = (jclass)env->NewGlobalRef(cls);
	    mid_repaint = env->GetMethodID(globalclass,
			 "repaint", "()V");
	    mid_keystroke =
		 env->GetMethodID(cls,
			 "keystroke", "(Ljava/lang/String;)V");
	    mid_mousepress =
	      env->GetMethodID(cls, "mouse", "(IIIII)V");
	    mid_timeout =
	      env->GetMethodID(cls, "timeout", "(I)V");
	    mid_windowClosed =
	      env->GetMethodID(cls, "windowClosed", "()V");
	}
    }
    ~GZZJNIEventHandler() {
    }
    void repaint() {
	DBG(dbg_event) << "CALLING REPAINT!!!\n";
	jnienv_eventloop->CallVoidMethod(globalRef, mid_repaint);
	javaExc(jnienv_eventloop, "repaint");

	// jnienv_eventloop->DeleteLocalRef(cls);

	DBG(dbg_event) << "CALLED REPAINT\n";
    }
    virtual void keystroke(const char *str) {
	DBG(dbg_event) << "Keystroke being sent to java: '"<<str<<"' \n";
	jstring jstr = jnienv_eventloop->NewStringUTF(str);
	DBG(dbg_event) << "Keystroke has been sent\n";

	jnienv_eventloop->CallVoidMethod(globalRef, mid_keystroke, jstr);
	javaExc(jnienv_eventloop, "keystroke");

	DBG(dbg_event) << "Call finished\n";
    }

    virtual void mouse(int x, int y, int button, int type, int modifiers) {

        jnienv_eventloop->CallVoidMethod(globalRef, mid_mousepress, x, y, button, type, modifiers);
	javaExc(jnienv_eventloop, "mouse");
    }
    virtual void timeout(int id) {
        jnienv_eventloop->CallVoidMethod(globalRef, mid_timeout, id);
	javaExc(jnienv_eventloop, "timeout");
    }
    virtual void windowClosed() {
        jnienv_eventloop->CallVoidMethod(globalRef, mid_windowClosed);
	javaExc(jnienv_eventloop, "windowClosed");
    }
};


struct VobJNIEventHandler : public Os::Eventhandler {

    jobject globalRef;
    jclass globalclass; // must keep for mid to remain valid
    jmethodID mid_repaint;
    jmethodID mid_keystroke;
    jmethodID mid_mousepress;
    jmethodID mid_timeout;
    jmethodID mid_windowClosed;

    VobJNIEventHandler(JNIEnv *env, jobject globalRef) : globalRef(globalRef) {
	if(globalRef) {
	    jclass cls = env->GetObjectClass(globalRef);
	    globalclass = (jclass)env->NewGlobalRef(cls);
	    mid_repaint = env->GetMethodID(globalclass,
			 "repaint", "()V");
	    mid_keystroke =
		 env->GetMethodID(cls,
			 "keystroke", "(Ljava/lang/String;)V");
	    mid_mousepress =
	      env->GetMethodID(cls, "mouse", "(IIII)V");
	    mid_timeout =
	      env->GetMethodID(cls, "timeout", "(I)V");
	    mid_windowClosed =
	      env->GetMethodID(cls, "windowClosed", "()V");
	}
    }
    ~VobJNIEventHandler() {
    }
    void repaint() {
	DBG(dbg_event) << "CALLING REPAINT!!!\n";
	jnienv_eventloop->CallVoidMethod(globalRef, mid_repaint);
	javaExc(jnienv_eventloop, "repaint");

	// jnienv_eventloop->DeleteLocalRef(cls);

	DBG(dbg_event) << "CALLED REPAINT\n";
    }
    virtual void keystroke(const char *str) {
	DBG(dbg_event) << "Keystroke being sent to java: '"<<str<<"' \n";
	jstring jstr = jnienv_eventloop->NewStringUTF(str);
	DBG(dbg_event) << "Keystroke has been sent\n";

	jnienv_eventloop->CallVoidMethod(globalRef, mid_keystroke, jstr);
	javaExc(jnienv_eventloop, "keystroke");

	DBG(dbg_event) << "Call finished\n";
    }

    virtual void mouse(int x, int y, int button, int type) {
        jnienv_eventloop->CallVoidMethod(globalRef, mid_mousepress, x, y, button, type);
	javaExc(jnienv_eventloop, "mouse");
    }
    virtual void timeout(int id) {
        jnienv_eventloop->CallVoidMethod(globalRef, mid_timeout, id);
	javaExc(jnienv_eventloop, "timeout");
    }
    virtual void windowClosed() {
        jnienv_eventloop->CallVoidMethod(globalRef, mid_windowClosed);
	javaExc(jnienv_eventloop, "windowClosed");
    }
};


extern "C" {


jf(jint, init)
  (JNIEnv *env, jclass, jint) {
    static int inited = 0;
    if(inited++) {
	cerr << "Already inited: "<<(inited-1)<<"\n";
	return 0;
    }
    DBG(dbg) << "Initializing GZZGL\n";
    ws = Os::WindowSystem::getInstance();
    DBG(dbg) << "Creating one new window for initialization purposes\n";
    defaultWindow = ws->openWindow(0, 0, 2, 2);
    DBG(dbg) << "Setting it current to get a gl context\n";

    setWindow();

    VobJNIEventHandler *eh = new VobJNIEventHandler(env, 0);
    eh = eh; // eat warning

      // We don't release the window here, to leave the context
      // current.
      // releaseWindow();
    return 0;
}


// Renderable

jf(void, deleteRenderable0)
  (JNIEnv *, jclass, jint id) {
      DBG(dbg) << "Delete renderable0 "<<id<<"\n";
      vob0s.remove(id);
  }

jf(void, deleteRenderable1)
  (JNIEnv *, jclass, jint id) {
      DBG(dbg) << "Delete renderable1 "<<id<<"\n";
      vob1s.remove(id);
  }

jf(void, deleteRenderable2)
  (JNIEnv *, jclass, jint id) {
      DBG(dbg) << "Delete renderable2 "<<id<<"\n";
      vob2s.remove(id);
  }

jf(void, deleteRenderable3)
  (JNIEnv *, jclass, jint id) {
      DBG(dbg) << "Delete renderable3 "<<id<<"\n";
      vob3s.remove(id);
  }

jf(void, deleteRenderableN)
  (JNIEnv *, jclass, jint id) {
      DBG(dbg) << "Delete renderableN "<<id<<"\n";
      vobNs.remove(id);
  }



// RenderingSurface

jf(jint, createStableRenderingSurfaceImpl)
  (JNIEnv *env, jclass, jint w, jint h) {
	Os::RenderingSurface *win = ws->openStableOffScreen(w, h);
	return windows.add(win);
}

// Window

jf(jint, createWindowImpl)
  (JNIEnv *env, jclass, jboolean first,
	    jint x, jint y, jint w, jint h, jobject eh) {
      DBG(dbg) << "Creating new window for Java "<<x<<" "<<y<<" "<<w<<" "<<h<<"\n";
      jobject ehglobal = env->NewGlobalRef(eh);
      GZZJNIEventHandler *evh = new GZZJNIEventHandler(env, ehglobal);

      Os::Window *win;
      if(first) {
	  /* Reuse the window that was created at init time.
	   */
	  win = defaultWindow;
	  win->resize(w,h);
	  win->move(x, y);
      } else {
	  win = ws->openWindow(x, y, w, h);
      }
      DBG(dbg) << "Setting its eventhandler to "<<((int)evh)<<"\n";
      win->setEventHandler(evh);
      DBG(dbg) << "Return to J\n";
      return windows.add(win);
  }

jf(void, deleteWindow)
  (JNIEnv *env, jclass, jint id) {

      // Window *w = windows.get(id);
      // GZZJNIEventHandler *h = (GZZJNIEventHandler *)w->getEventHandler();
      // env->DeleteGlobalRef(h->globalRef);
      windows.remove(id);
  }

jf(void, getWindowSize)
  (JNIEnv *env, jclass, jint id, jobject rect) {
      Os::RenderingSurface *win = windows.get(id);
      int xywh[4];
      win->getSize(xywh);

      jclass clazz = env->GetObjectClass(rect);

      jfieldID f;
      f = env->GetFieldID(clazz, "x", "I");
      if(f!=0) env->SetIntField(rect, f, xywh[0]);
      f = env->GetFieldID(clazz, "y", "I");
      if(f!=0) env->SetIntField(rect, f, xywh[1]);
      f = env->GetFieldID(clazz, "width", "I");
      if(f!=0) env->SetIntField(rect, f, xywh[2]);
      f = env->GetFieldID(clazz, "height", "I");
      if(f!=0) env->SetIntField(rect, f, xywh[3]);

  }

jf(void, addTimeoutWindow)
  (JNIEnv *env, jclass, jint id, jint ms, jint tid) {
      Os::Window *w = (Os::Window *)windows.get(id);
      w->addTimeout(ms, tid);
  }

jf(void, impl_1Window_1setCurrent)
  (JNIEnv *env, jclass, jint id) {
      setWindow(id);
}

jf(void, impl_1Window_1release)
  (JNIEnv *env, jclass, jint id) {
      releaseWindow();
}

jf(void, impl_1Window_1move)
  (JNIEnv *env, jclass, jint id, jint x, jint y) {
      Os::Window *w = (Os::Window *)windows.get(id);
      DBG(dbg) << "Move window "<<id<<" "<<x<<" "<<y<<" at "<<(int)w<<"\n";
      w->move(x,y);
}

jf(void, impl_1Window_1setCursor)
(JNIEnv *env, jclass, jint id, jstring name) {
      Os::Window *w = (Os::Window *)windows.get(id);
      DBG(dbg) << "Set window "<<id<<" Cursor name "<<name<<" at "<<(int)w<<"\n";
      std::string name_str = jstr2stdstr(env, name);
      w->setCursor(name_str);
}

jf(void, impl_1Window_1resize)
  (JNIEnv *env, jclass, jint id, jint wid, jint h) {
      Os::Window *w = (Os::Window *)windows.get(id);
      w->resize(wid,h);
}


// OpenGL Program
jf(jint, impl_1createProgram)
  (JNIEnv *, jclass) {
      setWindow();
      GLuint ret;
#ifdef GL_VERTEX_PROGRAM_ARB
      glGenProgramsARB(1, &ret);
      GLERR;
#endif
      releaseWindow();
      return ret;
}

jf(void, impl_1deleteProgram)
  (JNIEnv *, jclass, jint id) {
#ifdef GL_VERTEX_PROGRAM_ARB
      setWindow();
      GLuint rel = id;
      glDeleteProgramsARB(1, &rel);
      releaseWindow();
#endif
}

jf(jint, impl_1Program_1load)
  (JNIEnv *env, jclass, jint id, jstring prog) {
#ifdef GL_VERTEX_PROGRAM_ARB
      setWindow();
      std::string prog_utf = jstr2stdstr(env, prog);
      CallGL::loadProgram(id, prog_utf);
      releaseWindow();
#endif
      return 1;
  }

// Real program immediate execution

JNIEXPORT void JNICALL Java_org_nongnu_libvob_gl_GL_call__Ljava_lang_String_2I
  (JNIEnv *env, jclass, jstring code, jint window) {
      DBG(dbg) << "call(s,w)\n";
      std::string utf = jstr2stdstr(env, code);
      DBG(dbg) << "got str\n";
      setWindow(window);
      CallGL::callGL(utf.c_str());
      releaseWindow();
      DBG(dbg) << "called\n";
  }

JNIEXPORT void JNICALL Java_org_nongnu_libvob_gl_GL_call__Ljava_lang_String_2
  (JNIEnv *env, jclass, jstring code) {
      DBG(dbg) << "call(s)\n";
      std::string utf = jstr2stdstr(env, code);
      DBG(dbg) << "got str\n";
      CallGL::callGL(utf.c_str());
      DBG(dbg) << "called\n";
  }



// ByteVector
//
jf(jint, createByteVectorImpl)
  (JNIEnv *, jclass, jint size) {
      DBG(dbg) << "Try to Alloc byte vector "<<size<<"\n";
      ByteVector *vec = new ByteVector(size);
      DBG(dbg) << "Alloc byte vector "<<(int)vec<<"\n";
      return bytevectors.add(vec);
}

jf(void, deleteByteVector)
  (JNIEnv *, jclass, jint id) {
      DBG(dbg) << "Delete byte vector "<<(int)bytevectors[id]<<"\n";
      bytevectors.remove(id);
}

jf(void, impl_1ByteVector_1readFromBuffer)
  (JNIEnv *env, jclass, jint id, jint winid, jstring buffer, 
	jint x, jint y, jint w, jint h, 
	jstring format, jstring type) 
{
    DBG(dbg) << "ReadFromBuffer\n";
    setWindow(winid);
    ByteVector *v = bytevectors[id];
    GLenum buf = tokenFromJstring(env, buffer);
    GLenum form = tokenFromJstring(env, format);
    GLenum typ = tokenFromJstring(env, type);

    DBG(dbg) << "prepped\n";

    glPushAttrib(GL_PIXEL_MODE_BIT);
    glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
    glReadBuffer(buf);
    glPixelStorei(GL_PACK_ROW_LENGTH, 0);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);
    DBG(dbg) << "set\n";
    glReadPixels(x, y, w, h, form, typ, &((*v)[0]));
    DBG(dbg) << "fin1\n";
    GLERR;
    glPopClientAttrib();
    glPopAttrib();
    releaseWindow();
    DBG(dbg) << "done\n";
}

jf(void, impl_1ByteVector_1drawPixels)
  (JNIEnv *env, jclass, jint id, jint winid, 
	jint x, jint y, jfloat z, jint w, jint h, 
	jstring format, jstring type) 
{
    setWindow(winid);
    ByteVector *v = bytevectors[id];
    GLenum form = tokenFromJstring(env, format);
    GLenum typ = tokenFromJstring(env, type);

    glPushAttrib(GL_PIXEL_MODE_BIT);
    glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glRasterPos3f(x, y, z);
    glDrawPixels(w, h, form, typ, &((*v)[0]));
    GLERR;
    glPopClientAttrib();
    glPopAttrib();
    releaseWindow();
}


jf(jint, impl_1ByteVector_1get__II)
 (JNIEnv *env, jclass, jint id, jint ind) {
    return (*bytevectors[id])[ind];
}

jf(jbyteArray, impl_1ByteVector_1get__I)
  (JNIEnv *env, jclass, jint id) {
    int len = bytevectors[id]->size();
    jbyteArray arr = env->NewByteArray(len);
    env->SetByteArrayRegion(arr, 0, len, (jbyte*) &(*(bytevectors[id]))[0]);
    return arr;
}

jf(void, impl_1ByteVector_1set)
  (JNIEnv *env, jclass, jint id, jbyteArray array) {
      jbyte *bytes = env->GetByteArrayElements(array, 0);
      ByteVector *v = bytevectors[id];

      unsigned len = env->GetArrayLength(array);
      if(len > v->size()) len = v->size();

      for(unsigned i=0; i<len; i++)
	  (*v)[i] = bytes[i];
      
      env->ReleaseByteArrayElements(array, bytes, 0);
}

jf(jintArray, impl_1ByteVector_1getInts)
  (JNIEnv *env, jclass, jint id) {
    int len = bytevectors[id]->size() / 4;
    jintArray arr = env->NewIntArray(len);
    env->SetIntArrayRegion(arr, 0, len, (jint*) (&(*(bytevectors[id]))[0]));
    return arr;
}


jf(jint, impl_1ByteVector_1shade)
  (JNIEnv *env, jclass, jint id, jint w, jint h, jint d, jint comp, 
	    jstring name, jobjectArray params) {
      setWindow();
      DBG(dbg)<<"Shade into "<<id<<"\n";

      std::string name_utf = jstr2stdstr(env, name);

      Texture::Texture *s = Texture::Texture::getTexture(name_utf.c_str());

      if(!s) {
	  return 0;
      }

      Texture::TextureParam p = textureParams(env, params);

      bytevectors[id]->resize(w * h * (d==0 ? 1 : d) * comp * 4);
      s->render( &p, w, h, (d==0?1:d), comp, (float*)&((*bytevectors[id])[0]));
      return 1;
}


jf(void, setDebugVar)
  (JNIEnv *env, jclass, jstring name, jint value) {
      std::string utf = jstr2stdstr(env, name);
      Debug::var(utf.c_str()) = value;
}

jf(jint, getDebugVar)
  (JNIEnv *env, jclass, jstring name) {
      std::string utf = jstr2stdstr(env, name);
      int value = Debug::var(utf.c_str()) ;
      return value;
}

jf(jobjectArray, getDebugVarNames)
  (JNIEnv *env, jclass) {
      vector<const char *> vec;
      vec = Debug::getVarNames();
      jclass strclass = env->FindClass("java/lang/String");
      jobjectArray result = env->NewObjectArray(vec.size(), 
				  strclass, 0);
      for(unsigned i=0; i<vec.size(); i++)
	  env->SetObjectArrayElement(result, i, 
			  env->NewStringUTF(vec[i]));
      return result;
}

jf(jstring, getGLString)
  (JNIEnv *env, jclass, jstring name) {
      std::string utf = jstr2stdstr(env, name);
      const char *value = CallGL::getString(utf.c_str()) ;
      return env->NewStringUTF(value);
}

jf(jfloatArray, implgetGLFloat)
  (JNIEnv *env, jclass, jint rsid, jstring name) {
      std::string utf = jstr2stdstr(env, name);

      if(rsid >= 0) 
	  setWindow(rsid);

      vector<float> vec = CallGL::getFloat(utf.c_str()) ;
      jfloatArray result = env->NewFloatArray(vec.size());
      env->SetFloatArrayRegion(result, 0, vec.size(), &vec[0]);

      if(rsid >= 0) 
	  releaseWindow();

      return result;
}

jf(jfloatArray, getGLProgram)
  (JNIEnv *env, jclass, jstring j_target, jstring j_name) {
      std::string target = jstr2stdstr(env, j_target);
      std::string name = jstr2stdstr(env, j_name);
      vector<float> vec = CallGL::getProgram(target.c_str(), name.c_str()) ;
      jfloatArray result = env->NewFloatArray(vec.size());
      env->SetFloatArrayRegion(result, 0, vec.size(), &vec[0]);
      return result;
}

jf(jint, getGLProgrami)
  (JNIEnv *env, jclass, jstring j_target, jint id, jstring j_name) {
      GLenum target = tokenFromJstring(env, j_target);
      GLenum name = tokenFromJstring(env, j_name);
      glBindProgramARB(target, id);
      GLint res;
      glGetProgramivARB(target, name, &res);
      glBindProgramARB(target, 0);
      return res;
}

jf(jfloatArray, getGLTexParameterFloat)
  (JNIEnv *env, jclass, jstring target, jint tex, jstring name) {
      std::string utf_target = jstr2stdstr(env, target);
      std::string utf = jstr2stdstr(env, name);
      vector<float> vec = CallGL::getTexParameterFloat(utf_target.c_str(), tex, utf.c_str());
      jfloatArray result = env->NewFloatArray(vec.size());
      env->SetFloatArrayRegion(result, 0, vec.size(), &vec[0]);
      return result;
}

jf(jfloatArray, getGLTexLevelParameterFloat)
  (JNIEnv *env, jclass, jstring target, jint tex, jint level, jstring name) {
      std::string utf_target = jstr2stdstr(env, target);
      std::string utf = jstr2stdstr(env, name);
      vector<float> vec = CallGL::getTexLevelParameterFloat(utf_target.c_str(), tex, level, utf.c_str()) ;
      jfloatArray result = env->NewFloatArray(vec.size());
      env->SetFloatArrayRegion(result, 0, vec.size(), &vec[0]);
      return result;
}

jf(jstring, getGLTokenString)
  (JNIEnv *env, jclass, jint value) {
      const char *str = CallGL::getTokenString(value) ;
      return env->NewStringUTF(str);
}


jf( jboolean , eventLoop)
  (JNIEnv *env, jclass, jboolean wait) {
	DBG(dbg) << "Going into eventloop in C++\n";
	jnienv_eventloop = env;
	return ws->eventLoop(wait);
  }

jf( void , interruptEventloop )
  (JNIEnv *env, jclass) {
      DBG(dbg) << "Interrupting C++ eventloop\n";
      ws->interrupt();
      DBG(dbg) << "Done interrupting - should wake soon\n";
  }






}


}
}
