/*
Transform.cxx
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

#include <vob/Coorder.hxx>
#include <vob/Renderer.hxx>
#include <vob/Debug.hxx>
#include <vob/glerr.hxx>

#include <vob/vobs/GLState.hxx>

#include <vob/jni/Types.hxx>

#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"

using namespace Vob;
using namespace Vob::JNI;
using std::cout;
STATICDBGVAR(dbg_render, "JNI.render");

namespace Vob {

extern Primitives::HierarchicalTransform *defaultTransformFactory(int id) ;

namespace JNI {
extern ObjectStorer<ChildVS> childVSs;
}
using JNI::childVSs;

/** The initial transforms to give to the coorders.
 * The screen-sized box is taken care of in the Java code,
 * so we just have root coords here.
 */
static Transform *initialTransforms[2] = {
    new RootCoords(),
    0
};

extern "C" {

// Note we can use pointer to childVSs since
// the initialization doesn't actually *call* that objectstorer.
// if it did, we'd crash.
Renderer renderer(defaultTransformFactory, &childVSs);

jf( jstring, getTestStateRetainCorrect)
  (JNIEnv *env, jclass) {
      jstring res = stdstr2jstr(env, ::Vob::Vobs::testStateRetainCorrect);
      ::Vob::Vobs::testStateRetainCorrect = "";
      return res;
}

jf( void, renderImpl)
  (JNIEnv *env, jclass, jint window,
	jint numinds,
	jintArray j_inds1, jfloatArray j_pts1, 
	jintArray j_interpinds,
	jintArray j_inds2, jfloatArray j_pts2, 
	jintArray j_codes,
	jfloat fract, jboolean standardcoords, jboolean showFinal) {
       DBG(dbg_render) << "RENDER "<<window<<" "<<numinds<<" "
		<<j_inds1<<" "<<j_pts1<<" "<<
		j_interpinds<<" "<<j_inds2<<" "<<j_pts2<<" "<<j_codes<<"\n";
       GLERR;
       DBG(dbg_render) << "1\n";
       if(standardcoords) 
	   setWindow(window);
       DBG(dbg_render) << "2\n";

      jint *inds1 = env->GetIntArrayElements(j_inds1, 0);
       DBG(dbg_render) << "4\n";
      jfloat *pts1 = env->GetFloatArrayElements(j_pts1, 0);
       DBG(dbg_render) << "5\n";

      jint *inds2 = 0;
      jfloat *pts2 = 0;
      jint *interpinds = 0;
      if(j_inds2 != 0) {
	   DBG(dbg_render) << "6\n";
	  inds2 = env->GetIntArrayElements(j_inds2, 0);
	  pts2 = env->GetFloatArrayElements(j_pts2, 0);
	  interpinds = env->GetIntArrayElements(j_interpinds, 0);
      }
       DBG(dbg_render) << "7\n";

      jint *codes = env->GetIntArrayElements(j_codes, 0);

      DBG(dbg_render) << "Got data\n";

      renderer.setPoints(1, initialTransforms,
			(int)numinds, 
			(int *)inds1, (float *)pts1,
			(int *)interpinds,
			(int *)inds2, (float *)pts2,
			(float)fract, (bool)showFinal);

      DBG(dbg_render) << "Set datapoints\n";

      if(standardcoords) {
	  int xywh[4];
	  windows.get(window)->getSize(xywh);
	  DBG(dbg_render) << "Did getSize "<<xywh[0]<<" "<<xywh[1]<<" "<<xywh[2]<<" "
		    <<xywh[3]<<"\n";

	  Renderer::setStandardCoordinates(Vec(xywh[2], xywh[3]));
      }
       GLERR;

      DBG(dbg_render) << "Did stdcoords\n";
      renderer.renderScene((int *)codes, vob0s, vob1s, vob2s, vob3s, vobNs);
      DBG(dbg_render) << "Did renderscene\n";
      windows.get(window)->swapBuffers();
      DBG(dbg_render) << "Did swapbuffers\n";

      env->ReleaseIntArrayElements(j_codes, codes, JNI_ABORT);

      env->ReleaseIntArrayElements(j_inds1, inds1, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_pts1, pts1, JNI_ABORT);

      if(inds2 != 0) {
	  DBG(dbg_render) << "Releasearrays: "<<j_inds2<<" "<<j_pts2<<" "<<j_interpinds<<"\n";
	env->ReleaseIntArrayElements(j_inds2, inds2, JNI_ABORT);
	env->ReleaseFloatArrayElements(j_pts2, pts2, JNI_ABORT);
        env->ReleaseIntArrayElements(j_interpinds, interpinds, JNI_ABORT);
      }
      DBG(dbg_render) << "Did releasearrays\n";
       GLERR;

      if(standardcoords) {
	  releaseWindow();
      }

       Renderer::fpsTick();
}

#include <sys/time.h>
static double getTime() {
  struct timeval t;
  gettimeofday(&t, 0);
  return t.tv_usec*1E-6 + t.tv_sec;
}

jf( jdouble, timeRenderImpl)
    (JNIEnv *env, jclass,
	jint window, jint iters,
	jint ninds, jintArray j_inds1, jfloatArray j_pts1, jintArray j_codes,
	jboolean standardcoords, jboolean swapbuf) {

       DBG(dbg_render) << "RENDER "<<window<<" "<<" "
		<<j_inds1<<" "<<j_pts1<<" "<<
		" "<<j_codes<<"\n";
       GLERR;
       DBG(dbg_render) << "1\n";
       if(standardcoords) 
	   setWindow(window);
       DBG(dbg_render) << "2\n";

      jint *inds1 = env->GetIntArrayElements(j_inds1, 0);
       DBG(dbg_render) << "4\n";
      jfloat *pts1 = env->GetFloatArrayElements(j_pts1, 0);
       DBG(dbg_render) << "5\n";

      jint *inds2 = 0;
      jfloat *pts2 = 0;
      jint *interpinds = 0;

      jint *codes = env->GetIntArrayElements(j_codes, 0);

      DBG(dbg_render) << "Got data\n";

      renderer.setPoints(1, initialTransforms,
			(int)ninds, 
			(int *)inds1, (float *)pts1,
			(int *)interpinds,
			(int *)inds2, (float *)pts2,
			0, 1);

      DBG(dbg_render) << "Set datapoints\n";

      if(standardcoords) {
	  int xywh[4];
	  windows.get(window)->getSize(xywh);
	  DBG(dbg_render) << "Did getSize "<<xywh[0]<<" "<<xywh[1]<<" "<<xywh[2]<<" "
		    <<xywh[3]<<"\n";

	  Renderer::setStandardCoordinates(Vec(xywh[2], xywh[3]));
      }
       GLERR;

      double t0 = getTime();
      for(int iter = 0; iter < iters; iter++) {
	  DBG(dbg_render) << "Did stdcoords\n";
	  renderer.renderScene((int *)codes, vob0s, vob1s, vob2s, vob3s, vobNs);
	  DBG(dbg_render) << "Did renderscene\n";
	  if(swapbuf) {
	      windows.get(window)->swapBuffers();
	      DBG(dbg_render) << "Did swapbuffers\n";
	   }
      }
      double t1 = getTime();
      double t = t1 - t0;
      DBG(dbg) << "The time was " << t << " [" << t0 << " - " << t1 << "]\n";

      env->ReleaseIntArrayElements(j_codes, codes, JNI_ABORT);

      env->ReleaseIntArrayElements(j_inds1, inds1, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_pts1, pts1, JNI_ABORT);

      DBG(dbg_render) << "Did releasearrays\n";
       GLERR;

      if(standardcoords) {
	  releaseWindow();
      }

    return t;
}


}
}
