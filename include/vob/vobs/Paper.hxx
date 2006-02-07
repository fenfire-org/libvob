/*
Paper.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_VOBS_PAPER_HXX
#define VOB_VOBS_PAPER_HXX

#ifndef VOB_DEFINED
#define VOB_DEFINED(x)
#endif

#include <boost/lambda/bind.hpp>

#include <algorithm>

#include <ext/slist>

#include <vob/glerr.hxx>
#include <vob/Debug.hxx>
#include <vob/Transform.hxx>

#include <vob/paper/Paper.hxx>

#include <vob/VecGL.hxx>

#include <vob/poly/Dicer.hxx>

#include <vob/stats/TexAccum.hxx>

namespace Vob {
namespace Vobs {

PREDBGVAR(dbg_paperquad);

namespace PaperPriv {

struct Verts {
    const Transform &t;

    struct T2V3Vert {
	Pt orig;
	ZPt final;
	T2V3Vert() { }
	T2V3Vert(Pt p, ZPt z) : orig(p), final(z) {
	}
    };
    vector<T2V3Vert> points;

    Verts(const Transform &t) : t(t) { 
	points.reserve(1000);
    }

    unsigned int size() {
	return points.size();
    }

    int append(Pt p) {
	DBG(dbg_paperquad) << "DiceTester append "<<p<<"\n";
	int ind = points.size();
	points.push_back(T2V3Vert(p, t.transform(p).finitized()));
	DBG(dbg_paperquad) << "DiceTester append ret "<<ind<<"\n";
	return ind;
    }

    int operator() (int i, int j, float fract = .5) {
	DBG(dbg_paperquad) << "New vertex "<<i<<" "<<j<<" "<<fract<<"\n"
	    << points[i].orig <<" "
	    << points[j].orig <<" "
	    << points[i].final <<" "
	    << points[j].final <<"\n"
	    ;
	return append(
		lerp(points[i].orig, points[j].orig, fract) );
    }



    void startT2V3Operation() {
	glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
	glInterleavedArrays(GL_T2F_V3F, 5*sizeof(float), &(points[0]));
	glLockArraysEXT(0, points.size());
    }
    void endT2V3Operation() {
	glUnlockArraysEXT();
	glPopClientAttrib();
    }

    void startV3Operation() {
	glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
	glInterleavedArrays(GL_V3F, 5*sizeof(float), &(points[0].final));
	glLockArraysEXT(0, points.size());
    }
    void endV3Operation() {
	glUnlockArraysEXT();
	glPopClientAttrib();
    }
};

inline float split(Verts &v, float dicelen, int i, int j) {
    float ret = (v.points[i].final - v.points[j].final).xylength() 
		    / dicelen - 1;
    DBG(dbg_paperquad) << "Split "<<i<<" "<<j<<" "<<
	    v.points[i].final<<" "<<v.points[j].final<<" "<<ret<<"\n";
    return ret;
}

inline void addToAccum(Stats::TexAccum *acc, Verts &v, 
		int v0, int v1, int v2, float texAreaMult) {
    acc->add(
	    v.points[v0].final,
	    v.points[v1].final,
	    v.points[v2].final,
	    v.points[v0].orig,
	    v.points[v1].orig,
	    v.points[v2].orig,
	    texAreaMult);
}

inline int splitTri(Verts &v, float dicelen1, float dicelen2, int i, int j, int k) {
    DBG(dbg_paperquad) << "SplitTri "<<i<<" "<<j<<" "<<k<<" "<<
	    v.points[i].orig<<" "<<
	    v.points[j].orig<<" "<<
	    v.points[k].orig<<" "<<
	    v.points[i].final<<" "<<
	    v.points[j].final<<" "<<
	    v.points[k].final<< "\n";
    Pt ctr = 1/3. * (
		v.points[i].orig + 
		v.points[j].orig + 
		v.points[k].orig );
    ZPt ctrt = 1/3. * (
		v.points[i].final + 
		v.points[j].final + 
		v.points[k].final );
    ZPt tc = v.t.transform(ctr).finitized();
    if( (tc-ctrt).xylength() < dicelen1) {
	DBG(dbg_paperquad) << "NO SPLIT\n";
	return -1;
    }
    float l0 = (v.points[i].orig - v.points[j].orig).length();
    float l1 = (v.points[j].orig - v.points[k].orig).length();
    float l2 = (v.points[k].orig - v.points[i].orig).length();
    DBG(dbg_paperquad) << "SPLIT "<<l0<<" "<<l1<<" "<<l2<<"\n";
    if(l0 > l1 && l0 > l2) return 0;
    if(l1 > l2) return 1;
    return 2;
}

}


class DiceTester {
public:
    enum { NTrans = 1 };

    float dicelen1;
    float dicelen2;
    int flags;
    int maxdepth;

    template<class F> void params(F &f) {
	f(dicelen1, dicelen2, flags, maxdepth);
    }

    template<class T> void render(const T &coords) const {
	using namespace PaperPriv;
	Verts verts(coords);
	::Vob::Dicer::Triangles<Verts> triangler(verts);
	verts.append(Pt(0,0));
	verts.append(Pt(1,0));
	verts.append(Pt(0,1));
	verts.append(Pt(1,1));
	using namespace boost;
	using namespace boost::lambda;
	DBG(dbg_paperquad) << "Set_and_initial\n";
	triangler.add(0,1,3);
	triangler.add(0,3,2);
	triangler.dice(bind(splitTri, ref(verts), dicelen1, dicelen2, _1, _2, _3));

	verts.startT2V3Operation();
	    triangler.draw();
	verts.endT2V3Operation();

    }

};

VOB_DEFINED(DiceTester);

const int PAPERQUAD_CS2_TO_SCREEN = 1;
const int PAPERQUAD_USE_VERTEX_PROGRAM = 2;
const int PAPERQUAD_NONL_MAXLEN = 4;


/** A paperquad with no separation between paper and object coordinates.
 */
class FixedPaperQuad {
public:
    enum { NTrans = 1 };
    ::Vob::Paper::Paper *paper;
    float x0,y0,x1,y1;
    int flags;

    float diceLength, diceLength2;
    int diceDepth;

    Stats::TexAccum *texAccum;
    float texAreaMult;

    template<class F> void params(F &f) {
	f(paper, x0, y0, x1, y1, flags, diceLength, diceLength2, diceDepth,
		texAccum, texAreaMult);
    }


    template<class T> void render(const T &coords) const {
	GLERR;

	using namespace PaperPriv;
	Verts verts(coords);
	::Vob::Dicer::Triangles<Verts> triangler(verts);
	verts.append(Pt(x0,y0));
	verts.append(Pt(x1,y0));
	verts.append(Pt(x0,y1));
	verts.append(Pt(x1,y1));
	using namespace boost;
	using namespace boost::lambda;
	DBG(dbg_paperquad) << "FQ: Set_and_initial "<<texAccum<<"\n";

	triangler.add(0, 1, 3);
	triangler.add(0, 3, 2);
	DBG(dbg_paperquad) << "dice\n";

	if(texAccum) {
	    // First, dice roughly for the texture magnification
	    // accumulator
	    triangler.dice(bind(splitTri, ref(verts), 
			diceLength*2, diceLength2*2, _1, _2, _3));
	    // Then, calculate the texture surface estimates
	    DBG(dbg_paperquad) << "callbacks\n";
	    triangler.iterateTriangles(
		    bind(addToAccum, texAccum, ref(verts), _1, _2, _3, texAreaMult));
	}

	// Dice to finish
	triangler.dice(bind(splitTri, ref(verts), diceLength, diceLength2, _1, _2, _3));
	DBG(dbg_paperquad) << "diced\n";

	Paper::LightParam lightParam;
	lightParam.orig = ZPt(0,0,0);
	lightParam.e0 = ZPt(0,0,0);
	lightParam.e1 = ZPt(0,0,0);
	lightParam.e2 = ZPt(0,0,0);
	lightParam.Light = ZVec(-1,-1,1);
	lightParam.Light_w = 0.0;

	if(flags & PAPERQUAD_USE_VERTEX_PROGRAM) {
	    verts.startT2V3Operation();

	    for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {
		GLERR;
		(*it).setUp_VP(&lightParam);
		triangler.draw();
		(*it).tearDown_VP();
	    }

	    verts.endT2V3Operation();
	} else {
	    // Only vertex position comes from here
	    verts.startV3Operation();
	    for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {
		GLERR;
		(*it).setUp_explicit(&lightParam);

		glBegin(GL_TRIANGLES);

		for(::Vob::Dicer::Triangles<Verts>::Titer i=triangler.tris.begin(); 
			    i != triangler.tris.end(); i++) {
		     for(int v = 0; v < 3; v++) {
			 int ind = (*i).v[v];
			 float tmp[4] = {
			     verts.points[ind].orig.x,
			     verts.points[ind].orig.y,
			     0, 1};
			 (*it).texcoords_explicit( tmp );
			 glArrayElement(ind);
		     }
		}

		glEnd();
		(*it).tearDown_explicit();
	    }
	    verts.endV3Operation();

	}

	GLERR;
	
	
    }

};
VOB_DEFINED(FixedPaperQuad);

/**
# PaperQuad is a bit complicated: there are three coordinate
# systems here: the window cs, the object cs and the paper cs.
# cs1 is object => window,
# and cs2 is paper => object, unless PAPERQUAD_CS2_TO_SCREEN is set, when it is
#				paper => window
# Corners give the corners of the quad to render, in object
# coordinates.
*/

class PaperQuad {
public:
    enum { NTrans = 2 };

    ::Vob::Paper::Paper *paper;
    float x0,y0,x1,y1;
    float scale;
    float dicefactor;
    int flags;

    template<class F> void params(F &f) {
	f(paper, x0, y0, x1, y1, scale, dicefactor, flags);
    }

    template<class T> void render(const T &coords1, const T &coords2) const {
	    const int flags = this->flags;
	    // object -> paper/window
	    const Transform &coords2inv = coords2.getInverse();

	    GLERR;


	    ZPt paperorigin, paperx, papery;
	    if(flags & PAPERQUAD_CS2_TO_SCREEN) {
		const Transform &coords1inv = coords1.getInverse();
		paperorigin = coords1inv.transform(coords2.transform(ZPt(0, 0, 0)));
		paperx = coords1inv.transform(coords2.transform(ZPt(1, 0, 0))) 
				- paperorigin;
		papery = coords1inv.transform(coords2.transform(ZPt(0, 1, 0))) 
				- paperorigin;
	    } else {
		paperorigin = coords2.transform(ZPt(0, 0, 0));
		paperx = coords2.transform(ZPt(1, 0, 0)) - paperorigin;
		papery = coords2.transform(ZPt(0, 1, 0)) - paperorigin;
	    }

	
	    Paper::LightParam lightParam;

	    // These are now irrelevant
	    lightParam.orig = paperorigin-ZPt(0,0,0);
	    lightParam.e0 = paperx * scale;
	    lightParam.e1 = papery * scale;
	    lightParam.e2 = ZVec(0,0,paperx.length()) * scale;

            lightParam.Light = ZVec(-1,-1,1);
            lightParam.Light_w = 0.0;

	    DBG(dbg_paperquad) << "Paperquad: " <<
	            lightParam.orig << " " <<
		    lightParam.e0 << " " <<
		    lightParam.e1 << " " <<
		    lightParam.e2 << " " <<
		    "\\nCorners" <<
		    x0 << " " <<
		    y0 << " " <<
		    x1 << " " <<
		    y1 << " " <<
		    "\\n"
		    ;
	    GLERR;


	    int dice;

	    if(flags & PAPERQUAD_NONL_MAXLEN) {
		Pt p1 = coords1.transform(Pt(x0,y0));
		Pt p2 = coords1.transform(Pt(x0,y1));
		Pt p3 = coords1.transform(Pt(x1,y0));
		Pt p4 = coords1.transform(Pt(x1,y1));
		float dist[4] = {
		    (p2-p1).length(),
		    (p3-p1).length(),
		    (p4-p2).length(),
		    (p4-p3).length()
		};
		float m = *std::max_element(dist, dist+4);

		dice = (int)(m / dicefactor) + 2;
	    } else { // old way

		ZPt ctr = ZPt(lerp(x0, x1, 0.5), lerp(y0, y1, 0.5), 0);
		double len = hypot(x1-x0, y1-y0) / 2;
		double nonl = coords1.nonlinearity(ctr, len);
		
		dice = (int)(len * nonl * dicefactor) + 2;
	    }
	    DBG(dbg_paperquad) << "Dice: " << dice <<"\\n";
	    // Cap it at a ridiculous value
	    if( dice > 100) dice = 100;
	    if(dice < 2 ) dice = 2;

	    float *vertices = new float[dice * dice * 5];

            int *indices = new int[(dice) * (2*dice)];

            #define VERTICES3(x, y, z) vertices[((x)*dice + (y))*5 + (z)]
            #define VERTICES2(x, y)    vertices[((x)*dice + (y))*5]
            #define INDICES2(x, y)     indices[(x)*2*dice + (y)]
            #define INDICES1(x)        indices[(x)*2*dice]

	    int *indps[dice-1];
	    int counts[dice-1];
	    for(int ix = 0; ix<dice; ix++) {
		if(ix < dice-1) {
		    counts[ix] = 2*dice;
		    indps[ix] = &INDICES1(ix);
		}
		for(int iy = 0; iy<dice; iy++) {
		    if(ix < dice-1) {
			INDICES2(ix, 2*iy) = dice * ix + iy;
			INDICES2(ix, 2*iy+1) = dice * (ix+1) + iy;
		    }
		    float x = ix / (dice - 1.0);
		    float y = iy / (dice - 1.0);
		    ZPt p(lerp(x0, x1, x), lerp(y0, y1, y), 0);
		    ZPt v = coords1.transform(p);
		    VERTICES3(ix, iy, 2) = v.x;
		    VERTICES3(ix, iy, 3) = v.y;
		    VERTICES3(ix, iy, 4) = v.z;
		    ZPt t;
		    if(flags & PAPERQUAD_CS2_TO_SCREEN) {
			t = coords2inv.transform(v);
		    } else {
			t = coords2inv.transform(p);
		    }
		    VERTICES3(ix, iy, 0) = t.x;
		    VERTICES3(ix, iy, 1) = t.y;
		    DBG(dbg_paperquad) << "   vert: " << 
			    ix << " " <<
			    iy << " : " <<
			    VERTICES3(ix, iy, 0) << " " <<
			    VERTICES3(ix, iy, 1) << " " <<
			    VERTICES3(ix, iy, 2) << " " <<
			    VERTICES3(ix, iy, 3) << " " <<
			    VERTICES3(ix, iy, 4) << " " <<
			    "\\n";
		}
	    }

	    if(flags & PAPERQUAD_USE_VERTEX_PROGRAM) {
		glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
		glInterleavedArrays(GL_T2F_V3F, 5*sizeof(float), vertices);
		glLockArraysEXT(0, dice*dice);

		for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {

                    DBG(dbg_paperquad) << "Pass\\n";
                    GLERR;
                    (*it).setUp_VP(&lightParam);
                    
                    DBG(dbg_paperquad) << "Going to multidraw\\n";
                    GLERR;
                    glMultiDrawElementsEXT(GL_QUAD_STRIP, counts,
                       GL_UNSIGNED_INT, (const GLvoid **)indps, dice-1);
                    DBG(dbg_paperquad) << "Teardown\\n";
                    GLERR;
                    (*it).tearDown_VP();
                
                    GLERR;
                    DBG(dbg_paperquad) << "Pass over\\n";

		}
		glUnlockArraysEXT();
		glPopClientAttrib();
	    } else {
		for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {

                    DBG(dbg_paperquad) << "Pass\\n";
                    GLERR;
                    (*it).setUp_explicit(&lightParam);
                    
                    DBG(dbg_paperquad) << "Going to set texcoords explicit\\n";
                    GLERR;


                    for(int ix = 0; ix<dice-1; ix++) {
                        glBegin(GL_QUAD_STRIP);
                        for(int iy = 0; iy<dice; iy++) {

                             float tmp[4] = { VERTICES3(ix, iy, 0), VERTICES3(ix, iy, 1), 0 ,1 };
			    DBG(dbg_paperquad) << "to texcoords\\n";
                             (*it).texcoords_explicit( tmp );
			    DBG(dbg_paperquad) << "to vertex\\n";
                             glVertex3fv( (&(VERTICES2(ix, iy))+2) );
                                
                             float tmp2[4] = { VERTICES3(ix+1, iy, 0), VERTICES3(ix+1, iy, 1), 0 ,1 };
			    DBG(dbg_paperquad) << "to texcoords\\n";
                             (*it).texcoords_explicit( tmp2 );
			    DBG(dbg_paperquad) << "to vertex\\n";
                             glVertex3fv( ((&VERTICES2(ix+1, iy))+2) );
                         }
			DBG(dbg_paperquad) << "to end\\n";
                         glEnd();
                    }


                    DBG(dbg_paperquad) << "Teardown\\n";
                    GLERR;
                    (*it).tearDown_explicit();
                
                    GLERR;
                    DBG(dbg_paperquad) << "Pass over\\n";
                }
	    }

	    DBG(dbg_paperquad) << "Passes over\\n";

	    GLERR;

            delete [] vertices;
            delete [] indices;
            
    }


    
};


VOB_DEFINED(PaperQuad);

/**
# there are three coordinate
# systems here as well: the window cs, the object cs and the paper cs.
# cs1 is object => window,
# and cs2 is object => paper.
* This version renders exactly the box of cs2
*/

class EasyPaperQuad {
public:
    enum { NTrans = 2 };

    ::Vob::Paper::Paper *paper;
    float dicefactor;
    int flags;

    template<class F> void params(F &f) {
	f(paper, dicefactor, flags);
    }

    template<class T> void render(const T &coords1, const T &coords2) const {
	    const int flags = this->flags;

	    GLERR;

	    Paper::LightParam lightParam;

	    // These are now irrelevant
	    lightParam.orig = ZPt(0,0,0);
	    lightParam.e0 = ZPt(1,0,0);
	    lightParam.e1 = ZPt(0,1,0);
	    lightParam.e2 = ZPt(0,0,1);

            lightParam.Light = ZVec(-1,-1,1);
            lightParam.Light_w = 0.0;

	    DBG(dbg_paperquad) << "EasyPaperquad: " <<
	            lightParam.orig << " " <<
		    lightParam.e0 << " " <<
		    lightParam.e1 << " " <<
		    lightParam.e2 << " " <<
		    "\\n"
		    ;
	    GLERR;


	    int dice;
	    Pt box = coords2.getSqSize();

	    if(flags & PAPERQUAD_NONL_MAXLEN) {
		Pt p1 = coords1.transform(Pt(0,0));
		Pt p2 = coords1.transform(Pt(0,box.y));
		Pt p3 = coords1.transform(Pt(box.x,0));
		Pt p4 = coords1.transform(Pt(box.x,box.y));
		float dist[4] = {
		    (p2-p1).length(),
		    (p3-p1).length(),
		    (p4-p2).length(),
		    (p4-p3).length()
		};
		float m = *std::max_element(dist, dist+4);

		dice = (int)(m / dicefactor) + 2;
	    } else { // old way

		ZPt ctr = .5 * box; 
		double len = box.length() / 2;
		double nonl = coords1.nonlinearity(ctr, len);
		
		dice = (int)(len * nonl * dicefactor) + 2;
	    }
	    DBG(dbg_paperquad) << "Dice: " << dice <<"\\n";
	    // Cap it at a ridiculous value
	    if( dice > 100) dice = 100;
	    if(dice < 2 ) dice = 2;

	    float *vertices = new float[dice * dice * 5];

            int *indices = new int[(dice) * (2*dice)];

            #define VERTICES3(x, y, z) vertices[((x)*dice + (y))*5 + (z)]
            #define VERTICES2(x, y)    vertices[((x)*dice + (y))*5]
            #define INDICES2(x, y)     indices[(x)*2*dice + (y)]
            #define INDICES1(x)        indices[(x)*2*dice]

	    int *indps[dice-1];
	    int counts[dice-1];
	    for(int ix = 0; ix<dice; ix++) {
		if(ix < dice-1) {
		    counts[ix] = 2*dice;
		    indps[ix] = &INDICES1(ix);
		}
		for(int iy = 0; iy<dice; iy++) {
		    if(ix < dice-1) {
			INDICES2(ix, 2*iy) = dice * ix + iy;
			INDICES2(ix, 2*iy+1) = dice * (ix+1) + iy;
		    }
		    float x = ix / (dice - 1.0);
		    float y = iy / (dice - 1.0);
		    ZPt p(lerp(0., box.x, x), lerp(0., box.y, y), 0);
		    ZPt v = coords1.transform(p);
		    VERTICES3(ix, iy, 2) = v.x;
		    VERTICES3(ix, iy, 3) = v.y;
		    VERTICES3(ix, iy, 4) = v.z;
		    ZPt t;
		    t = coords2.transform(p);
		    VERTICES3(ix, iy, 0) = t.x;
		    VERTICES3(ix, iy, 1) = t.y;
		    DBG(dbg_paperquad) << "   vert: " << 
			    ix << " " <<
			    iy << " : " <<
			    VERTICES3(ix, iy, 0) << " " <<
			    VERTICES3(ix, iy, 1) << " " <<
			    VERTICES3(ix, iy, 2) << " " <<
			    VERTICES3(ix, iy, 3) << " " <<
			    VERTICES3(ix, iy, 4) << " " <<
			    "\\n";
		}
	    }

	    if(flags & PAPERQUAD_USE_VERTEX_PROGRAM) {
		glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
		glInterleavedArrays(GL_T2F_V3F, 5*sizeof(float), vertices);
		glLockArraysEXT(0, dice*dice);

		for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {

                    DBG(dbg_paperquad) << "Pass\\n";
                    GLERR;
                    (*it).setUp_VP(&lightParam);
                    
                    DBG(dbg_paperquad) << "Going to multidraw\\n";
                    GLERR;
                    glMultiDrawElementsEXT(GL_QUAD_STRIP, counts,
                       GL_UNSIGNED_INT, (const GLvoid **)indps, dice-1);
                    DBG(dbg_paperquad) << "Teardown\\n";
                    GLERR;
                    (*it).tearDown_VP();
                
                    GLERR;
                    DBG(dbg_paperquad) << "Pass over\\n";

		}
		glUnlockArraysEXT();
		glPopClientAttrib();
	    } else {
		for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {

                    DBG(dbg_paperquad) << "Pass\\n";
                    GLERR;
                    (*it).setUp_explicit(&lightParam);
                    
                    DBG(dbg_paperquad) << "Going to set texcoords explicit\\n";
                    GLERR;


                    for(int ix = 0; ix<dice-1; ix++) {
                        glBegin(GL_QUAD_STRIP);
                        for(int iy = 0; iy<dice; iy++) {

                             float tmp[4] = { VERTICES3(ix, iy, 0), VERTICES3(ix, iy, 1), 0 ,1 };
			    DBG(dbg_paperquad) << "to texcoords\\n";
                             (*it).texcoords_explicit( tmp );
			    DBG(dbg_paperquad) << "to vertex\\n";
                             glVertex3fv( (&(VERTICES2(ix, iy))+2) );
                                
                             float tmp2[4] = { VERTICES3(ix+1, iy, 0), VERTICES3(ix+1, iy, 1), 0 ,1 };
			    DBG(dbg_paperquad) << "to texcoords\\n";
                             (*it).texcoords_explicit( tmp2 );
			    DBG(dbg_paperquad) << "to vertex\\n";
                             glVertex3fv( ((&VERTICES2(ix+1, iy))+2) );
                         }
			DBG(dbg_paperquad) << "to end\\n";
                         glEnd();
                    }


                    DBG(dbg_paperquad) << "Teardown\\n";
                    GLERR;
                    (*it).tearDown_explicit();
                
                    GLERR;
                    DBG(dbg_paperquad) << "Pass over\\n";
                }
	    }

	    DBG(dbg_paperquad) << "Passes over\\n";

	    GLERR;

            delete [] vertices;
            delete [] indices;
            
    }


    
};


VOB_DEFINED(EasyPaperQuad);



/** A vob that's useful for demoing papers.
 * Not for real use.
 * Doesn't set up paper properly.
 * Should probably be done mostly in .py code.
 */
class BasisPaperQuad {
public:
    enum { NTrans = 2 };
    
    ::Vob::Paper::Paper *paper;
    float x0_0, y0_0, x0_1, y0_1;
    float x1_0, y1_0, x1_1, y1_1;
    DisplayListID tex0, tex1, isect;
    
    template<class F> void params(F &f) {
	f(paper, x0_0, y0_0, x0_1, y0_1, x1_0, y1_0, x1_1, y1_1, tex0, tex1, isect);
    }

    template<class T> void render(const T &coords1, const T &coords2) const {
	    DBG(dbg_paperquad) << "Paper\\n";
	    GLERR;

	    const Transform &cs1inv = coords1.getInverse();
	    const Transform &cs2inv = coords2.getInverse();

	    for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {
		if (dbg_paperquad) cout << "Pass\\n";

		GLERR;

                Pt p0[] = { Pt(x0_0,y0_0), Pt(x0_0,y0_1), Pt(x0_1,y0_1), Pt(x0_1,y0_0) };
                Pt p1[] = { Pt(x1_0,y1_0), Pt(x1_0,y1_1), Pt(x1_1,y1_1), Pt(x1_1,y1_0) };
                ZPt q;

                // Draw the first texture
                (*it).setupcode();
                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_LESS);
                glCallList(tex0);
                GLERR;
		glBegin(GL_QUADS);
                for (int i = 0; i < 4; i++) {
                    ZPt v = coords1.transform(p0[i]);
                    glMultiTexCoord2fARB(0, p0[i].x, p0[i].y);
                    q = cs2inv.transform(v);
                    glMultiTexCoord2fARB(1, q.x, q.y);
                    coords1.vertex(p0[i]);
                }
                glEnd();
		GLERR;
		(*it).teardowncode();
		GLERR;

                // Draw the intersection
                (*it).setupcode();
                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_EQUAL);
                glCallList(isect);
		GLERR;
		glBegin(GL_QUADS);
                for (int i = 0; i < 4; i++) {
                    ZPt v = coords2.transform(p1[i]);
                    glMultiTexCoord2fARB(1, p1[i].x, p1[i].y);
                    q = cs1inv.transform(v);
                    glMultiTexCoord2fARB(0, q.x, q.y);
                    coords2.vertex(p1[i]);
                }
                glEnd();
		GLERR;
		(*it).teardowncode();
		GLERR;

                // Draw the second texture
                (*it).setupcode();
                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_LESS);
                glCallList(tex1);
		GLERR;
		glBegin(GL_QUADS);
                for (int i = 0; i < 4; i++) {
                    ZPt v = coords2.transform(p1[i]);
                    glMultiTexCoord2fARB(1, p1[i].x, p1[i].y);
                    q = cs1inv.transform(v);
                    glMultiTexCoord2fARB(0, q.x, q.y);
                    coords2.vertex(p1[i]);
                }
                glEnd();
		GLERR;
		(*it).teardowncode();
		GLERR;




	    }

	    GLERR;
    }
};

VOB_DEFINED(BasisPaperQuad);

}
}


#endif
