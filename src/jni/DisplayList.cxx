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


// C++ implementation of GL.DisplayList

namespace Vob {
namespace JNI {

extern "C" {

jf(jint, createDisplayListImpl)
  (JNIEnv *, jclass) {
      setWindow();
      int l = glGenLists(1);
      releaseWindow();
      if(!l) {
	  cerr << "Couldn't allocate display list\n";
	  exit(25);
      }
      return l;
  }
jf(void, startCompile)
  (JNIEnv *, jclass, jint l, jint wid) {
      DBG(dbg) << "Start list compilation "<<l<<"\n";

      setWindow(wid);

      GLERR;
      glNewList(l, GL_COMPILE);
      GLERR;
  }
jf(void, endCompile)
  (JNIEnv *, jclass, jint, jint wid) {
      DBG(dbg) << "End list compilation\n";
      GLERR;
      glEndList();
      GLERR;

      releaseWindow();
  }

jf(void, compileCallGL)
  (JNIEnv *env, jclass, jint l, jstring s) {
      std::string utf = jstr2stdstr(env, s);
      DBG(dbg) << "got str\n";
      CallGL::compileGL(utf.c_str(), l);
      DBG(dbg) << "called\n";
  }

jf(void, deleteDisplayList)
  (JNIEnv *, jclass, jint l) {
      DBG(dbg) << "Delete display list (XXX Might crash if contexts wrong)\n";
      glDeleteLists(l, 1);
      DBG(dbg) << "Deleted display list\n";
  }

jf(void, impl_1DisplayList_1call)
  (JNIEnv *, jclass, jint l, jint w) {
      setWindow(w);
      glCallList(l);
      releaseWindow();
}

jf(void, impl_1DisplayList_1call0)
  (JNIEnv *, jclass, jint l) {
      glCallList(l);
}


}
}
}
