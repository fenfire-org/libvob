/*
TexAccum.cxx
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
#include <vob/Debug.hxx>
#include <vob/glerr.hxx>

#include <vob/vobs/GLState.hxx>

#include <vob/util/ObjectStorer.hxx>
#include <vob/paper/Paper.hxx>

#include <vob/stats/TexAccum.hxx>

#include <vob/jni/Types.hxx>

#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"


namespace Vob {
namespace JNI {

DBGVAR(dbg_texaccum, "JNI.texaccum");

Stats::Statistics statistics;

ObjectStorer<Stats::TexAccum> texaccums("texaccums");

jclass globalclass_WeakStatsCaller = 0;
jmethodID WSC_call = 0;

TexAccum_JNI::TexAccum_JNI(JNIEnv *env,
	Stats::Statistics *stats, jobject cb) :
    Stats::TexAccum(stats) {
    if(cb)
	cb = env->NewGlobalRef(cb);
    this->callback = cb;
}
TexAccum_JNI::~TexAccum_JNI() {
}
void TexAccum_JNI::prepareToDelete(JNIEnv *env) {
    if(callback) {
	env->DeleteGlobalRef(callback);
	callback = 0;
    }
}
void TexAccum_JNI::call(void *u) {
    if(callback) {
	JNIEnv *env = (JNIEnv *)u;
	env->CallVoidMethod(callback, WSC_call);
	javaExc(env, "StatsCallback");
    }
}

extern "C" {

jf( jint, impl_1TexAccum_1create )
	(JNIEnv *env, jclass) {
    DBG(dbg_texaccum)<<"Create without cb\n";
    TexAccum_JNI *accum = new TexAccum_JNI(env, 0, 0);
    return texaccums.add(accum);
}

jf( jint, impl_1TexAccum_1create_1cb )
	(JNIEnv *env, jclass, jobject cb) {
    DBG(dbg_texaccum)<<"Create with cb: "<<cb<<"\n";
    TexAccum_JNI *accum = new TexAccum_JNI(env, &statistics, cb);
    if(!globalclass_WeakStatsCaller) {
	globalclass_WeakStatsCaller = 
		(jclass)(env->NewGlobalRef(env->GetObjectClass(cb)));
	WSC_call = env->GetMethodID(globalclass_WeakStatsCaller,
				"call", "()V");
    }
    return texaccums.add(accum);
}

jf( void, impl_1TexAccum_1delete )
	(JNIEnv *env, jclass, jint id) {
    // We know only TexAccum_JNIs get into texaccums
    TexAccum_JNI *accum = (TexAccum_JNI *)texaccums.get(id);
    accum->prepareToDelete(env);
    texaccums.remove(id);
}

jf( void, impl_1TexAccum_1clear )
    (JNIEnv *env, jclass, jint id) {
    Stats::TexAccum *accum = texaccums.get(id);
    accum->clear();
}

jf( jdouble, impl_1TexAccum_1get )
    (JNIEnv *env, jclass, jint id, jint mip) {
    Stats::TexAccum *accum = texaccums.get(id);
    if(mip < 0 || mip >= Stats::TexAccum::NLEVELS)
	return -1;
    return accum->pixels[mip];
}

jf( void, callQueuedStatistics )
    (JNIEnv *env, jclass) {
    statistics.call(env);
}
jf( void, clearQueuedStatistics )
    (JNIEnv *env, jclass) {
    statistics.clear();
}

}

}
}


