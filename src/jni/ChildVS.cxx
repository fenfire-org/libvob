// (c) Tuomas J. Lukka

#include <iostream>


#include <vob/Debug.hxx>
#include <vob/glerr.hxx>
#include <vob/util/ObjectStorer.hxx>
#include <vob/jni/Types.hxx>


#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"
#include <vob/jni/Strings.hxx>

#include <vob/ChildVS.hxx>

#define jfChild(t, rootname) JNIEXPORT t JNICALL Java_org_nongnu_libvob_impl_gl_ChildVS_##rootname

namespace Vob {
namespace JNI {
    DBGVAR(dbg_childvs, "ChildVS");

    ObjectStorer<ChildVS> childVSs("childvs");

template<class Iter> void copyJints(JNIEnv *env, jintArray array, 
			Iter begin, int n) {
    jint *f = env->GetIntArrayElements(array, 0);
    std::copy(f, f+n, begin);
    env->ReleaseIntArrayElements(array, f, JNI_ABORT); // no changes, save time
}

template<class Iter> void copyJfloats(JNIEnv *env, jfloatArray array, 
			Iter begin, int n) {
    jfloat *f = env->GetFloatArrayElements(array, 0);
    std::copy(f, f+n, begin);
    env->ReleaseFloatArrayElements(array, f, JNI_ABORT); // no changes, save time
}


extern "C" {
jfChild(jint, impl_1create)
    (JNIEnv *env, jclass,
	jint nMapCodes,
	jintArray mapCodes, jint nCoorderInds, jintArray coorderInds,
	jint nCoorderFloats, jfloatArray coorderFloats) 
{
    ChildVS *childVS = new ChildVS();


    childVS->mapCodes.resize(nMapCodes);
    copyJints(env, mapCodes, childVS->mapCodes.begin(), nMapCodes);

    childVS->coorderInds.resize(nCoorderInds);
    copyJints(env, coorderInds, childVS->coorderInds.begin(), nCoorderInds);

    childVS->coorderFloats.resize(nCoorderFloats);
    copyJfloats(env, coorderFloats, childVS->coorderFloats.begin(), nCoorderFloats);


    return childVSs.add(childVS);
}

jfChild(void, impl_1delete)
    (JNIEnv *, jclass, jint id) 
{
    childVSs.remove(id);
}

}
}
}
