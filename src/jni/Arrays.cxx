/*
Floats.cxx
 *    
 *    Copyright (c) 2003, Matti J. Katila
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
 */
/*
 * Written by Matti J. Katila
 */

#include <vob/jni/Types.hxx>


namespace Vob {


void jfloats2floatvector(JNIEnv *env, jfloatArray in, vector<float>& out) {
    jsize len = env->GetArrayLength(in);
    out.resize(len);
    jfloat *f = env->GetFloatArrayElements(in, 0);
    std::copy(f, f+len, out.begin());
    env->ReleaseFloatArrayElements(in, f, JNI_ABORT); // no changes, save time
}

void jints2intvector(JNIEnv *env, jintArray in, vector<int>& out) {
    jsize len = env->GetArrayLength(in);
    out.resize(len);
    jint *f = env->GetIntArrayElements(in, 0);
    std::copy(f, f+len, out.begin());
    env->ReleaseIntArrayElements(in, f, JNI_ABORT); // no changes, save time
}

jintArray intvector2jintArray(JNIEnv *env, const vector<int> &in) {
    jintArray res = env->NewIntArray(in.size());
    jint *els = env->GetIntArrayElements(res, 0);
    std::copy(in.begin(), in.end(), els);
    for(unsigned i=0; i<in.size(); i++)
	els[i] = in[i];
    env->ReleaseIntArrayElements(res, els, 0);
    return res;

}



}
