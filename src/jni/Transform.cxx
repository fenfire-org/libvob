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

#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"
#include "vob/Debug.hxx"

namespace Vob {

DBGVAR(dbg_trans, "JNI.transform");

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



jf( jboolean , transform)
 (JNIEnv *env, jclass, jint ninds, jintArray j_inds, jfloatArray j_pts,
    jint coordsys, jboolean inverse, jfloatArray j_points, jfloatArray j_into) {
      jint *inds = env->GetIntArrayElements(j_inds, 0);
      jfloat *pts = env->GetFloatArrayElements(j_pts, 0);

      Coorder coordset(defaultTransformFactory, &childVSs);
      coordset.clean();
      coordset.setPoints(1, initialTransforms,
			 (int)ninds, (int*)inds, 
			 (float*)pts, (int*)0,
			 (int*)0, (float*)0, 
			 (float)0, (bool)true);

      DBG(dbg_trans) << "SetPoints -- now: "<<coordsys<<"\n";
      if(coordsys < 0 || coordsys > coordset.size()) {
        jclass Exception = env->FindClass("java/lang/Exception");
        env->ThrowNew(Exception,"Invalid coordsys ind!");
	return false;
      }

     int arrayLength = env->GetArrayLength(j_points);
     jfloat *points = env->GetFloatArrayElements(j_points, 0);
     int nInto = env->GetArrayLength(j_into);
     if(arrayLength % 3 || arrayLength != nInto) {
        jclass Exception = env->FindClass("java/lang/Exception");
        env->ThrowNew(Exception,"Invalid into array length");
	return false;
     }
     jfloat *into = env->GetFloatArrayElements(j_into, 0);

     const Transform *cs = coordset.get(coordsys);
     if(cs != 0) {
	 if(inverse) {
		DBG(dbg_trans) << "Inverting..."<<cs<<"\n";
		cs = &cs->getInverse();
	        DBG(dbg_trans) << "Done ("<<cs<<", releasing...\n";
	 }
	 if(cs != 0)
	  for(int i=0; i<arrayLength; i+=3) {
	     DBG(dbg_trans) << "Pt: "<<i<<" "<<arrayLength<<"\n";
	     ZPt pt(points[i], points[i+1], points[i+2]);
	     DBG(dbg_trans) << "Transform "<<pt<<"...\n";
	     ZPt pt2 = cs->transform(pt);
	     into[i] = pt2.x;
	     into[i+1] = pt2.y;
	     into[i+2] = pt2.z;
	  }
      }
      DBG(dbg_trans) << "Done, releasing...\n";

      env->ReleaseFloatArrayElements(j_points, points, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_into, into, 0); // These were changed, commit.

      env->ReleaseIntArrayElements(j_inds, inds, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_pts, pts, JNI_ABORT);

      return (cs != 0);
 }

jf( jboolean , transformSq)
 (JNIEnv *env, jclass, jint ninds, jintArray j_inds, 
  jfloatArray j_pts,
    jint coordsys, 
    jfloatArray j_into) {
      jint *inds = env->GetIntArrayElements(j_inds, 0);
      jfloat *pts = env->GetFloatArrayElements(j_pts, 0);

      Coorder coordset(defaultTransformFactory, &childVSs);
      coordset.clean();
      coordset.setPoints(1, initialTransforms,
			 (int)ninds, (int*)inds, 
			 (float*)pts, (int*)0,
			 (int*)0, (float*)0, 
			 (float)0, (bool)true);

     jfloat *into = env->GetFloatArrayElements(j_into, 0);


     const Transform *cs = coordset.get(coordsys);


     if(cs != 0) {
	 Pt sq = cs->getSqSize();
	 into[0] = sq.x;
	 into[1] = sq.y;
     }

      env->ReleaseFloatArrayElements(j_into, into, 0); // These were changed, commit.

      env->ReleaseIntArrayElements(j_inds, inds, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_pts, pts, JNI_ABORT);

      return (cs != 0);
 }


jf( jboolean , transform2)
 (JNIEnv *env, jclass, jint ninds, jintArray j_inds, jfloatArray j_pts,
	jintArray j_interpinds, jintArray j_inds2, jfloatArray j_pts2,
	jfloat fract, jboolean show1,
    jint coordsys, jboolean inverse, jfloatArray j_points, jfloatArray j_into) {

      jint *inds = env->GetIntArrayElements(j_inds, 0);
      jfloat *pts = env->GetFloatArrayElements(j_pts, 0);

      jint *interpinds = env->GetIntArrayElements(j_interpinds, 0);

      jint *inds2 = env->GetIntArrayElements(j_inds2, 0);
      jfloat *pts2 = env->GetFloatArrayElements(j_pts2, 0);

      Coorder coordset(defaultTransformFactory, &childVSs);
      coordset.clean();
      coordset.setPoints(1, initialTransforms,
			 (int)ninds, (int*)inds, 
			 (float*)pts, (int*)interpinds,
			 (int*)inds2, (float*)pts2, 
			 (float)fract, (bool)show1);

     int arrayLength = env->GetArrayLength(j_points);
     jfloat *points = env->GetFloatArrayElements(j_points, 0);
     jfloat *into = env->GetFloatArrayElements(j_into, 0);

     const Transform *cs = coordset.get(coordsys);
     if(cs != 0) {
	 if(inverse) cs = &cs->getInverse();
	  for(int i=0; i<arrayLength; i+=3) {
	     ZPt pt(points[i], points[i+1], points[i+2]);
	     ZPt pt2 = cs->transform(pt);
	     into[i] = pt2.x;
	     into[i+1] = pt2.y;
	     into[i+2] = pt2.z;
	  }
      }

      env->ReleaseFloatArrayElements(j_points, points, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_into, into, 0); // These were changed, commit.

      env->ReleaseIntArrayElements(j_inds, inds, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_pts, pts, JNI_ABORT);

      env->ReleaseIntArrayElements(j_interpinds, interpinds, JNI_ABORT);

      env->ReleaseIntArrayElements(j_inds2, inds2, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_pts2, pts2, JNI_ABORT);

      return (cs != 0);
 }


struct Match {
    int ind;
    float depth;
    bool operator<(const Match &m) const {
	return depth < m.depth;
    }
};


jf( jintArray , getAllChildCSAt)
 (JNIEnv *env, jclass, jint ninds, jintArray j_inds, 
  jintArray j_actives, jintArray j_childs,
  jfloatArray j_pts, jfloat x, jfloat y) {


      jint *inds = env->GetIntArrayElements(j_inds, 0);

      jfloat *pts = env->GetFloatArrayElements(j_pts, 0);


      jint * actives = env->GetIntArrayElements(j_actives, 0);
      jint * childs = env->GetIntArrayElements(j_childs, 0);

      std::vector<Match> matches;

      Coorder coordset(defaultTransformFactory, &childVSs);
      coordset.clean();

      coordset.setPoints(1, initialTransforms,
			 (int)ninds, (int*)inds, 
			 (float*)pts, (int*)0,
			 (int*)0, (float*)0, 
			 (float)0, (bool)true);

     ZPt screenpt(x, y, 0);

     if (actives == 0) {
       cout << "AAARRGGHH...!\n";
       abort();
     }

     Coorder * coords = &coordset;

     Transform *c = coords->get(((int *)actives)[0]);
     // transform box to screen
     Pt xy0 = c->transform(Pt(0,0));
     Pt xy1 = c->transform(c->getSqSize());

     //cout << xy0 << ", 1->  " << xy1 << "\n";

     coords = coords->getChildCoorder(((int *)childs)[0]);

     int n = (int)env->GetArrayLength(j_actives);
     //cout << "n: " <<n<<"\n";
     for (int i=1; i<n; i++) {
	 int actCS = ((int *) actives)[i];

	 Transform *cs = coords->get(actCS);
	 Pt sq = cs->getSqSize();

	 cout << "not implemented fully...\n";
	 abort();
	 
	 //coords = coords->getChildCoorder(actCS);

	 // plaah...
	 // See whether inside unit square
	 //if(pt.x < 0 || pt.x > sq.x ||
	 //   pt.y < 0 || pt.y > sq.y) continue;
     }


     for(Coorder::iterator iter = coords->begin(); 
	 iter != coords->end(); iter++) {

	 Transform *cs = coords->get(*iter);
	 if(!cs || !cs->isActive()) continue;

	 //cout << "CS found: "<< ((int)*iter)<<"\n";

	 // Transform screen point to inside coordsys
	 ZPt pt = cs->getInverse().transform(screenpt);
	 Pt sq = cs->getSqSize();
	 // See whether inside unit square
	 if(pt.x < 0 || pt.x > sq.x ||
	    pt.y < 0 || pt.y > sq.y) continue;
	 // Project to zero plane
	 pt.z = 0;
	 // Transform back to screen coordinates
	 pt = cs->transform(pt);
	 // Add match object

	 //cout << "CS found!!!: "<< ((int)*iter)<<"\n";

	 Match m = { *iter, pt.z };
	 matches.push_back(m);
     }

     // Sort the matches according to depth
     std::sort(matches.begin(), matches.end());
  
     // Put the matching indices into the output array.
     jintArray arr = env->NewIntArray(matches.size());
     jint *els = env->GetIntArrayElements(arr, 0);

     for(unsigned i = 0; i<matches.size(); i++)
	els[i] = matches[i].ind;

     env->ReleaseIntArrayElements(arr, els, 0); // Commit
     
     env->ReleaseIntArrayElements(j_childs, childs, JNI_ABORT);
     env->ReleaseIntArrayElements(j_actives, actives, JNI_ABORT);
     env->ReleaseIntArrayElements(j_inds, inds, JNI_ABORT);
     env->ReleaseFloatArrayElements(j_pts, pts, JNI_ABORT);
     
     return arr;
}



jf( jintArray , getAllCSAt)
 (JNIEnv *env, jclass, jint ninds, jintArray j_inds, 
  jfloatArray j_pts, jint parent, jfloat x, jfloat y) {
      jint *inds = env->GetIntArrayElements(j_inds, 0);
      jfloat *pts = env->GetFloatArrayElements(j_pts, 0);

      std::vector<Match> matches;

      Coorder coordset(defaultTransformFactory, &childVSs);
      coordset.clean();
      coordset.setPoints(1, initialTransforms,
			 (int)ninds, (int*)inds, 
			 (float*)pts, (int*)0,
			 (int*)0, (float*)0, 
			 (float)0, (bool)true);

     ZPt screenpt(x, y, 0);
     
     for(Coorder::iterator iter = coordset.begin(); 
	   iter != coordset.end(); iter++) {
	 // XXX Break encapsulation badly
	 Transform *cs = coordset.get(*iter);
	 if(!cs || !cs->isActive()) continue;

	 // Transform screen point to inside coordsys
	 ZPt pt = cs->getInverse().transform(screenpt);
	 Pt sq = cs->getSqSize();
	 // See whether inside unit square
	 if(pt.x < 0 || pt.x > sq.x ||
	    pt.y < 0 || pt.y > sq.y) continue;
	 // Project to zero plane
	 pt.z = 0;
	 // Transform back to screen coordinates
	 pt = cs->transform(pt);
	 // Add match object
	 Match m = { *iter, pt.z };
	 matches.push_back(m);
     }

     // Sort the matches according to depth
     std::sort(matches.begin(), matches.end());

     // Put the matching indices into the output array.
     jintArray arr = env->NewIntArray(matches.size());
     jint *els = env->GetIntArrayElements(arr, 0);

     for(unsigned i = 0; i<matches.size(); i++)
	els[i] = matches[i].ind;

     env->ReleaseIntArrayElements(arr, els, 0); // Commit

      env->ReleaseIntArrayElements(j_inds, inds, JNI_ABORT);
      env->ReleaseFloatArrayElements(j_pts, pts, JNI_ABORT);

      return arr;
 }


}

}
