/*
Paper.cxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#include <iostream>
#include <vector>
#include <GL/glu.h>

#include <vob/util/ObjectStorer.hxx>
#include <vob/paper/Paper.hxx>


#include "org_nongnu_libvob_gl_Paper.h"
#include <vob/jni/Strings.hxx>
#include "vobjnidef.hxx"


using namespace Vob;
using namespace CallGL;

namespace Vob {
namespace JNI {

    extern ObjectStorer<IndirectTexture> indirectTextures;

using std::cout;

#define GLERR { int er = glGetError(); if(er != GL_NO_ERROR) \
		    cout << "===== OPENGL ERROR "<<__FILE__<<" "<<__LINE__ \
			<<"  "<<gluErrorString(er)<<"\n"; \
	    }

ObjectStorer<Vob::Paper::Paper> papers("papers");

#define jpf(t, f) JNIEXPORT t JNICALL Java_org_nongnu_libvob_gl_Paper_##f

extern "C" {

jpf( jint , impl_1create)
  (JNIEnv *, jclass) {

      Vob::Paper::Paper *p = new Vob::Paper::Paper();
      return papers.add(p);
}

jpf( void , impl_1delete)
  (JNIEnv *, jclass, jint id) {

      papers.remove(id);
}

jpf( void , impl_1clone)
  (JNIEnv *, jclass, jint from_id, jint to_id) {
  *papers[to_id] = *papers[from_id];
}


jpf( jint , impl_1getNPasses)
  (JNIEnv *, jclass, jint id) {

      return papers[id]->size();
}

jpf( void , impl_1setNPasses)
  (JNIEnv *, jclass, jint id, jint size) {
      papers[id]->resize(size);
}


jpf( jint , impl_1Pass_1getNIndirectTextureBinds)
  (JNIEnv *, jclass, jint id, jint pass) {
      return (*papers[id])[pass].indirectTextureBinds.size();
}

jpf( void , impl_1Pass_1setNIndirectTextureBinds)
  (JNIEnv *, jclass, jint id, jint pass, jint size) {
      (*papers[id])[pass].indirectTextureBinds.resize(size);
}


jpf( jint , impl_1Pass_1getNTexGens)
  (JNIEnv *, jclass, jint id, jint pass) {
      return (*papers[id])[pass].texgen.size();
}

jpf( void , impl_1Pass_1setNTexGens)
  (JNIEnv *, jclass, jint id, jint pass, jint size) {
      (*papers[id])[pass].texgen.resize(size);
}

jpf( jint , impl_1Pass_1getNLightSetups)
  (JNIEnv *, jclass, jint id, jint pass) {
      return (*papers[id])[pass].setup.size();
}

jpf( void , impl_1Pass_1setNLightSetups)
  (JNIEnv *, jclass, jint id, jint pass, jint size) {
      (*papers[id])[pass].setup.resize(size);
}


jpf( void , impl_1Pass_1setSetupcode)
  (JNIEnv *env, jclass, jint id, jint pass, jstring code) {
      GLERR
      const char *utf = env->GetStringUTFChars(code, 0);

      (*papers[id])[pass].setupcode = CallGLCode(utf);

      GLERR

      env->ReleaseStringUTFChars(code, utf);
}

jpf( jstring , impl_1Pass_1getSetupcode)
  (JNIEnv *env, jclass, jint id, jint pass) {

      string s = (*papers[id])[pass].setupcode.getSource();
      return env->NewStringUTF(s.c_str());
}


jpf( void , impl_1Pass_1setTeardowncode)
  (JNIEnv *env, jclass, jint id, jint pass, jstring code) {
      GLERR
      const char *utf = env->GetStringUTFChars(code, 0);

      (*papers[id])[pass].teardowncode = CallGLCode(utf);

      GLERR

      env->ReleaseStringUTFChars(code, utf);
}

jpf( jstring , impl_1Pass_1getTeardowncode)
  (JNIEnv *env, jclass, jint id, jint pass) {

      string s = (*papers[id])[pass].teardowncode.getSource();
      return env->NewStringUTF(s.c_str());
}


jpf( void , impl_1Pass_1putNormalTexGen)
  (JNIEnv *env, jclass, jint id, jint pass, jint ind, jfloatArray arr) {

      GLERR

      jfloat *floats = env->GetFloatArrayElements(arr, 0);

      (*papers[id])[pass].texgen[ind] = shared_ptr<Vob::Paper::TexGen>(new Paper::TexGen(floats));

      GLERR

      env->ReleaseFloatArrayElements(arr, floats, JNI_ABORT);
      
}

jpf( void , impl_1Pass_1putEmbossTexGen)
  (JNIEnv *env, jclass, jint id, jint pass, jint ind, jfloatArray arr, jfloat eps) {

      GLERR

      jfloat *floats = env->GetFloatArrayElements(arr, 0);

      (*papers[id])[pass].texgen[ind] = shared_ptr<Paper::TexGen>(new Paper::TexGenEmboss(floats, eps));

      GLERR

      env->ReleaseFloatArrayElements(arr, floats, JNI_ABORT);
      
}

jpf( void , impl_1Pass_1putIndirectTextureBind)
  (JNIEnv *env, jclass, jint id, jint pass, jint ind, 
	jstring activeTexture, jstring textureTarget,
	int indirectTextureId) 
{
    GLERR

    (*papers[id])[pass].indirectTextureBinds[ind] =
	shared_ptr<IndirectTextureBind>(
		new IndirectTextureBind(
		    tokenFromJstring(env, activeTexture),
		    tokenFromJstring(env, textureTarget),
		    indirectTextures.get(indirectTextureId)));

    GLERR
}





}

}
}
