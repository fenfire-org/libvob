/*
Trivial.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka and Janne V. Kujala
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
 * Written by Tuomas J. Lukka and Janne V. Kujala
 */

#ifndef VOB_VOBS_FILLET
#define VOB_VOBS_FILLET

#include <algorithm>

#include <math.h>

#include <GL/gl.h>
#include <vob/Types.hxx>

#include <vob/Vec23.hxx>
#include <vob/VecGL.hxx>

#include <vob/glerr.hxx>

#include <vob/geom/Fillets2.hxx>

#include <vob/poly/Dicer.hxx>

#ifndef VOB_DEFINED
#define VOB_DEFINED(t)
#endif

using namespace Vob::Geom;

namespace Vob {
namespace Vobs {
using namespace Vob::VecGL;

    PREDBGVAR(dbg_vfillets);

/** A vob which is used for graph rendering: given a number of coordinate systems,
 * it *sorts* them to angle order and calls its child vob for each pair.
 * @param single The child vob to call
 * @param nconst The number of constant coordinate systems to pass forth to the child.
 */

struct SortedConnections {
    enum { NTrans = -1 };

    Vob *single, *single0;
    int nconst;

    template<class F> void params(F &f) {
	f(single, single0, nconst);
    }

    struct AngleInd {
	float angle;
	int ind;
	bool operator<(const AngleInd &b) const { return angle < b.angle; }
    };

    void render(const Transform **t, int n) const {
	DBG(dbg_vfillets) << "Sortedconns: start "<<n<<"\n";
	Vec v[n];
	for(int i=nconst; i<n; i++)
	    v[i] = t[i]->transform(.5 * t[i]->getSqSize());

	AngleInd a[n];
	a[nconst].angle = -1000;
	a[nconst].ind = 0;
	for(int i=nconst + 1; i<n; i++)  {
	    a[i].angle = (v[i] - v[nconst]).atan();
	    a[i].ind = i;
	}

	// Special case, with no connections
	if (nconst + 1 == n) {
	  if (single0) single0->render(t, nconst + 1);
	  return;
	}

	std::sort(a + nconst, a+n);

	// Render the circle
	const Transform *ct[nconst + 3];
	for(int i=0; i<nconst; i++)
	    ct[i] = t[i];

	ct[nconst] = t[nconst + 0]; 

	for(int i=nconst + 1; i<n-1; i++) {
	    DBG(dbg_vfillets) << "Sortedconns: "<< i <<" "
		    << a[i].ind << " "
		    << a[i+1].ind <<"\n";
		    
	    ct[nconst + 1] = t[a[i].ind],
	    ct[nconst + 2] = t[a[i+1].ind];
	    single->render(ct, nconst + 3);
	}

	DBG(dbg_vfillets) << "Sortedconns final: "
		<< a[n-1].ind << " "
		<< a[nconst+1].ind <<"\n";

	ct[nconst + 1] = t[a[n-1].ind], 
	ct[nconst + 2] = t[a[nconst+1].ind];
	single->render(ct, nconst + 3);
    }

};

VOB_DEFINED(SortedConnections);

/** A vob which is used for graph rendering: given a number of coordinate systems,
 * it calls its child vob for each
 * @param single The child vob to call
 * @param single0 The child vob to call in case of no non-const coordsyses
 * @param nconst The number of constant coordinate systems (minus one) to pass forth to the child.
 */
struct IterConnections {
    enum { NTrans = -1 };

    Vob *single, *single0;
    int nconst;

    template<class F> void params(F &f) {
	f(single, single0, nconst);
    }

    void render(const Transform **t, int n) const {
	const Transform *ct[nconst + 2];
	for(int i=0; i<nconst; i++)
	    ct[i] = t[i];

	ct[nconst] = t[nconst + 0]; 

	if (single0 && nconst + 1 == n)
	    single0->render(ct, nconst + 1);

	for(int i=nconst + 1; i<n; i++) {
	    ct[nconst + 1] = t[i], 
	    single->render(ct, nconst + 2);
	}
    }

};

VOB_DEFINED(IterConnections);

struct FilletSpan2 {
    enum { NTrans = -1 };

    float border;
    int ndice;
    int flags;

    template<class F> void params(F &f) {
	f(border, ndice, flags);
    }

    template<class T> float crad(const T &t) const {
	return 0.5 * t.getSqSize().x;
    }

    struct Conn {
	template<class T> float crad(const T &t) const {
	    return 0.5 * t.getSqSize().x;
	}
	float th;
	float a;

	Conn(const Transform &thick_t, const Transform &angle_t,
		const Transform &t0, const Transform &t1, float d) {
	    float r0 = crad(t0);
	    float r1 = crad(t1);
	    float r = lerp(r0, r1, .5);
	    th = r * thick_t.transform(ZVec(d / r, 0, 0)).x;

	    a = atan(angle_t.transform(ZVec(d / r, 0, 0)).x);

	    // XXX: prevent the connection from being thicker than the node
	    //if (th > r0) th = r0;

	    // XXX: prevent the middle of the connection from being 
	    // thicker than the start by adjusting the "tangent" 
	    // angle if necessary
	    float aw = asin(th / r0);
	    if (a < aw) a = aw;

	    // XXX: prevent negative stretching by adjusting the "tangent"
	    // angle, if necessary
	    float r2 = (d*d + th*th - r0*r0) / (2*r0 - 2*th);
	    float at = asin((th + r2) / (r0 + r2));
	    if (a > at) a = at;

	    // XXX: interpolate the "tangent" angle to zero from the point of
	    // "overlap" (i.e., where the beginning and end of the side
	    // of a fillet meet) to where the nodes coincide.
	    float f = d / (cos(aw) * r0);
	    if (f < 1.0) a = aw * sin(M_PI_2 * f);

	    DBG(dbg_vfillets) << 
		format("FilletSpan CONN: r: %s d: %s  th: %s a: %s") %
		    r% d% th% a;
	}
    };

	/*
	float rmax = r0 >? r1;
	float rmin = r0 <? r1;
	if(d <= rmax - rmin) return .96 * rmin;
	float dr = (d - (rmax-rmin)) / (r0 + r1);
	return .96 * rmin *  1 / (1 + dr);
	*/


    void v(ZVec p) const {
	if(flags & 4) {
	    glColor3f(1, p.z / 800, p.z / 800);
	}
	glVertex(p);
    }

    void vl(ZVec p) const {
	if(flags & 4) {
	    glColor3f(0,p.z / 800, p.z / 800);
	}
	glVertex(p);
    }

    void pointInternNormal(ZVec p, ZVec intern, ZVec normal) const {
	if(flags & 1) {
	    // solid 
	    v(p - border/2 * normal);
	    v(intern);
	} else {
	    // Line
	    vl(p + border/2 * normal);
	    vl(p - border/2 * normal);
	}
    }


    ZVec norm(ZVec v) const {
	if (v.length() > 0) 
	    return v.normalized();
	return ZVec(0,0,0);
    }

    template<class G> void renderSpanPolyedged(const G &g, int sign) const {
	// 32 = show curvature lines
	if(flags & 32) 
	    glBegin(GL_LINES); 
	else 
	    glBegin(GL_QUAD_STRIP); 
	ZVec intern0;
	ZVec p0 = g.point(0, &intern0);

	ZVec intern1;
	ZVec p1 = g.point(1 / (ndice-1.0), &intern1);

	ZVec normal0 = sign * norm(Vec(p1-p0).cw90());

	if(!(flags & 32))
	    pointInternNormal(p0, intern0, normal0);

	ZVec prevpt = p0;
	ZVec curpt = p1; ZVec curintern = intern1;
	ZVec nextpt, nextintern;

	for(int i=1; i<ndice-1; i++) {
	    float fract = (i+1) / (ndice-1.0);
	    nextpt = g.point(fract, &nextintern);

	    ZVec normal = sign * norm(Vec(nextpt - prevpt).cw90());

	    if(flags & 32) {
		glVertex(curpt);
		// Calculate curvature
		Vec d = (nextpt - prevpt) / 2;
		Vec dd = (nextpt - 2* curpt + prevpt);
		float l = d.length();
		float k = (d.x * dd.y - d.y * dd.x) / (l * l * l);

		glVertex(curpt + border * k * normal * sign);
		
		
	    } else
		pointInternNormal(curpt, curintern, normal);

	    prevpt = curpt;
	    curpt = nextpt;
	    curintern = nextintern;
	}

	ZVec normalLast = sign * norm(Vec(curpt - prevpt).cw90());
	if(!(flags & 32))
	    pointInternNormal(curpt, curintern, normalLast);
	
	glEnd();
    }

    template<class G> void renderSpan(const G &g, int sign) const {
	renderSpanPolyedged(g, sign);
    }



    float normAngle(float a) const {
	while(a < 0) a += 2*M_PI;
	while(a >= 2*M_PI) a -= 2*M_PI;
	return a;
    }

    void render(const Transform **t, int n) const {
        // If no connections, render a single circle
        if (n == 3) {
	  const Transform &t0 = *t[2];
	  ZVec ctr = t0.transform(0.5 * t0.getSqSize());
	  float csize = crad(t0);
	  CircularNode node(ctr, csize);
	  renderSpan(CircularNodeSpan(node, 0, 2*M_PI), 1);
	  return;
        }

	const Transform &thick_t = *t[0];
	const Transform &angle_t = *t[1];

	const Transform &t0 = *t[2];
	const Transform &t1 = *t[3];
	const Transform &t2 = *t[4];

	ZVec p0 = t0.transform(0.5 * t0.getSqSize());
	ZVec p1 = t1.transform(0.5 * t1.getSqSize()) ;
	ZVec p2 = t2.transform(0.5 * t2.getSqSize()) ;

	ZVec ctr = p0;
	Vec v1 = p1 - ctr;
	Vec v2 = p2 - ctr;

	DBG(dbg_vfillets) << "FilletSpan "<<ctr<<" "<<v1<<" "<<v2<<"\n";

	float a1 = v1.atan();
	float a2 = v2.atan();
	if(a2 < a1 || &t1 == &t2) a2 += 2*M_PI;

	float d1 = v1.length() / 2;
	float d2 = v2.length() / 2;

	float csize = crad(t0);

	Conn conn1(thick_t, angle_t, t0, t1, d1);
	Conn conn2(thick_t, angle_t, t0, t2, d2);

	float th1 = conn1.th;
	float th2 = conn2.th;

	bool overlap1 = d1 <= csize * cos(conn1.a);
	bool overlap2 = d2 <= csize * cos(conn2.a);

	DBG(dbg_vfillets) << "P: "<<ctr<<" "<<csize<<" "<<
		    a1<<" "<<d1<<" "<<th1<<" "<<
		    a2<<" "<<d2<<" "<<th2<<" "<<
		    "\n";

	CircularNode node(ctr, csize);
	LinearConnectionHalf c1(node, a1, d1, th1, -1, lerp(p0.z, p1.z, .5));
	LinearConnectionHalf c2(node, a2, d2, th2, 1, lerp(p0.z, p2.z, .5));

	if(flags & 64) { // Stretched circle

	    StretchedCircleFillet f1(node, c1, conn1.a);
	    StretchedCircleFillet f2(node, c2, conn2.a);

	    //renderSpan(f1, 1);
	    //float ta1 = f1.dirTang.atan();
	    //float ta2 = f2.dirTang.atan();
	    //if(ta2 < ta1) ta2 += 2*M_PI;
	    //renderSpan(CircularNodeSpan(node, ta1, ta2), 1);
	    //renderSpan(f2, -1);

	    // Find out how close they are.
	    if(a2-a1 < M_PI &&
		    (f1.infillet(f2.conn.dir) ||
		     f2.infillet(f1.conn.dir))) {
		// Cleaved case
		// Need to construct virtual second connection, with only angle changed

		// Width of fillet in radians
		float w1 = normAngle(f1.dirTang.atan() - a1);
		float w2 = normAngle(a2 - f2.dirTang.atan());
		float maxw = w1 >? w2;
		// Angle between connections
		float ab = a2 - a1;

		float fract = (maxw - ab) / maxw;
		float va1 = a2 - maxw;
		float va2 = a1 + maxw;

		LinearConnectionHalf vc2(node, va2, d2, th2, 1, lerp(p0.z, p2.z, .5));
		StretchedCircleFillet vf2(node, vc2, conn2.a);

		LinearConnectionHalf vc1(node, va1, d1, th1, -1, lerp(p0.z, p1.z, .5));
		StretchedCircleFillet vf1(node, vc1, conn1.a);

		DBG(dbg_vfillets) << "P cleave: "<< w1<<" "<<w2<<" "
			<< ab<<" "<<fract<<" "<<va1<<" "<<va2<<
			    "\n";
		renderSpan2(node, a1,
			    makeLerpFilletSpan(
					       makeFilletBlend(f1, vf2),
					       f1,
					       fract), 1, overlap1);

		renderSpan2(node, a2,
			    makeLerpFilletSpan(
					       makeFilletBlend(f2, vf1),
					       f2,
					       fract), -1, overlap2);
	    } else 
		renderNormalOrBlend(node, f1, f2, overlap1, overlap2);
	} else if(flags & 16) { // Ellipses
	    // For now, fix angle of attachment to the given angle
	    EllipseCircleFillet f1(node, c1, conn1.a);
	    EllipseCircleFillet f2(node, c2, conn2.a);

	    // Find out how close they are.
	    if(a2-a1 < M_PI &&
		    (f1.infillet(f2.conn.dir) ||
		     f2.infillet(f1.conn.dir))) {
		// Cleaved case
		// Need to construct virtual second connection, with only angle changed

		// Width of fillet in radians
		float w1 = normAngle(f1.dirTang.atan() - a1);
		float w2 = normAngle(a2 - f2.dirTang.atan());
		float maxw = w1 >? w2;
		// Angle between connections
		float ab = a2 - a1;

		float fract = (maxw - ab) / maxw;
		float va1 = a2 - maxw;
		float va2 = a1 + maxw;

		LinearConnectionHalf vc2(node, va2, d2, th2, 1, lerp(p0.z, p2.z, .5));
		EllipseCircleFillet vf2(node, vc2, conn2.a);

		LinearConnectionHalf vc1(node, va1, d1, th1, -1, lerp(p0.z, p1.z, .5));
		EllipseCircleFillet vf1(node, vc1, conn1.a);

		DBG(dbg_vfillets) << "P cleave: "<< w1<<" "<<w2<<" "
			<< ab<<" "<<fract<<" "<<va1<<" "<<va2<<
			    "\n";
		renderSpan(
			makeLerpFilletSpan(
			    makeFilletBlend(f1, vf2),
			    f1,
			    fract), 1
			);

		renderSpan(
			makeLerpFilletSpan(
			    makeFilletBlend(f2, vf1),
			    f2,
			    fract), -1
			);
	    } else 
		renderNormalOrBlend(node, f1, f2);
	} else {

	    CircleCircleFillet f1(node, c1);
	    CircleCircleFillet f2(node, c2);

	    // Find out how close they are.
	    if(a2-a1 < M_PI &&
		    (f1.infillet(f2.conn.dir) ||
		     f2.infillet(f1.conn.dir))) {
		// Cleaved case
		// Need to construct virtual second connection, with only angle changed

		// Width of fillet in radians
		float w1 = normAngle(f1.dirTang.atan() - a1);
		float w2 = normAngle(a2 - f2.dirTang.atan());
		float maxw = w1 >? w2;
		// Angle between connections
		float ab = a2 - a1;

		float fract = (maxw - ab) / maxw;
		float va1 = a2 - maxw;
		float va2 = a1 + maxw;

		LinearConnectionHalf vc2(node, va2, d2, th2, 1, lerp(p0.z, p2.z, .5));
		CircleCircleFillet vf2(node, vc2);

		LinearConnectionHalf vc1(node, va1, d1, th1, -1, lerp(p0.z, p1.z, .5));
		CircleCircleFillet vf1(node, vc1);

		DBG(dbg_vfillets) << "P cleave: "<< w1<<" "<<w2<<" "
			<< ab<<" "<<fract<<" "<<va1<<" "<<va2<<
			    "\n";
		renderSpan(
			makeLerpFilletSpan(
			    makeFilletBlend(f1, vf2),
			    f1,
			    fract), 1
			);

		renderSpan(
			makeLerpFilletSpan(
			    makeFilletBlend(f2, vf1),
			    f2,
			    fract), -1
			);
	    } else 
		renderNormalOrBlend(node, f1, f2);
	}

    }

    template<class F> void renderNormalOrBlend(const CircularNode &node, const F &f1, const F &f2, bool overlap1 = false, bool overlap2 = false) const {
	if(f1.overlaps(f2)) {
	    renderSpan2(node, f1.conn.a, makeFilletBlend(f1, f2), 1, overlap1);
	    renderSpan2(node, f2.conn.a, makeFilletBlend(f2, f1), -1, overlap2);
	} else {
	    renderSpan2(node, f1.conn.a, f1, 1, overlap1);
	    
	    float ta1 = f1.dirTang.atan();
	    float ta2 = f2.dirTang.atan();
	    if(ta2 < ta1) ta2 += 2*M_PI;
	    renderSpan(CircularNodeSpan(node, ta1, ta2), 1);

	    renderSpan2(node, f2.conn.a, f2, -1, overlap2);
	}
    }

    template<class F> void renderSpan2(const CircularNode &node, float a0, const F &f, int sign, bool overlap) const {
	ZVec p2;
	ZVec p1 = f.point(1, &p2);
	if (flags & 128)
	    renderSpan(CircularSliceSpan(node, p1, p2), 0);
	if (!overlap) renderSpan(f, sign);
    }

};
VOB_DEFINED(FilletSpan2);

struct Fillet3D {
    enum { NTrans = -1 };

    float border;
    int ndice;
    int flags;

    template<class F> void params(F &f) {
	f(border, ndice, flags);
    }

    template<class T> float crad(const T &t) const {
	return 0.5 * t.getSqSize().x;
    }

    ZVec norm(ZVec v) const {
	if (v.length() > 0) 
	    return v.normalized();
	return ZVec(0,0,0);
    }

    vector<Vec> computeNormals(const vector<Vec> &v) const {
	vector<Vec> n(v.size());

	size_t i;
	n[0] = (v[1] - v[0]).cw90().normalized();
	for (i = 1; i < v.size() - 1; i++)
	    n[i] = (v[i+1] - v[i-1]).cw90().normalized();
	n[v.size() - 1] = (v[v.size()-1] - v[v.size()-2]).cw90().normalized();
	
	return n;
    }

    void render(const Transform &t0) const {
	ZVec p0 = t0.transform(0.5 * t0.getSqSize());
	float r = crad(t0);
	CircularNode node(ZVec(0,0,0), r);
	CircularNodeSpan f0(node, 0, M_PI);
	
	int i;
	vector<Vec> v;
	vector<float> tex;

	for (i = 0; i <= ndice; i++) {
	    v.push_back(f0.point((float)i / ndice));
	    tex.push_back(0);
	}

	ZVec e0 = (t0.transform(ZVec(0,0,1)) - t0.transform(ZVec(0,0,0))).normalized();
	ZVec e1 = (t0.transform(ZVec(0,1,0)) - t0.transform(ZVec(0,0,0))).normalized();
	ZVec e2 = (t0.transform(ZVec(1,0,0)) - t0.transform(ZVec(0,0,0))).normalized();

	float mat[16] = {
	    e0.x, e0.y, e0.z, 0,
	    e1.x, e1.y, e1.z, 0,
	    e2.x, e2.y, e2.z, 0,
	    p0.x, p0.y, p0.z, 1,
	};
	glPushMatrix();
	glMultMatrixf(mat);
	render(v, computeNormals(v), tex);
	glPopMatrix();
    }

    void render(const Transform **t, int ntrans) const {
	if (ntrans == 3) {
	    render(*t[2]);
	    return;
	}

	const Transform &thick_t = *t[0];
	const Transform &angle_t = *t[1];

	const Transform &t0 = *t[2];
	const Transform &t1 = *t[3];

	ZVec p0 = t0.transform(0.5 * t0.getSqSize());
	ZVec p1 = t1.transform(0.5 * t1.getSqSize());

	float d = (p1 - p0).length() / 2;
	float r = crad(t0);
	
	FilletSpan2::Conn conn(thick_t, angle_t, t0, t1, d);

	CircularNode node(ZVec(0,0,0), r);
	LinearConnectionHalf c1(node, 0/*angle*/, d, conn.th, -1, 0/*z*/);

	StretchedCircleFillet f1(node, c1, conn.a);
	
	CircularNodeSpan f0(node, conn.a, M_PI);

	int i;
	vector<Vec> v;
	vector<float> tex;

	for (i = 0; i < ndice; i++) {
	    float f = (float)i / ndice;
	    Vec p = f1.point(f);
	    v.push_back(p);
	    tex.push_back(1 - f);
	}

	for (i = 0; i <= ndice; i++) {
	    v.push_back(f0.point((float)i / ndice));
	    tex.push_back(0);
	}

	
	ZVec ref = t0.transform(ZVec(0,0,1)) - t0.transform(ZVec(0,0,0));
	ZVec e0 = (p1 - p0).normalized();
	ZVec e1 = ref.crossp(e0).normalized();
	ZVec e2 = e0.crossp(e1);

	float mat[16] = {
	    e0.x, e0.y, e0.z, 0,
	    e1.x, e1.y, e1.z, 0,
	    e2.x, e2.y, e2.z, 0,
	    p0.x, p0.y, p0.z, 1,
	};
	glPushMatrix();
	glMultMatrixf(mat);
	render(v, computeNormals(v), tex);
	glPopMatrix();
    }

    void render(const vector<Vec> &v, const vector<Vec> &n, 
		const vector<float> &tex) const {
	size_t i;
	int j;

	if (v.size() < 2) return;

	for (i = 0; i < v.size() - 1; i++) {
	    glBegin(GL_QUAD_STRIP);
	    float s0 = 2 * (float)i / (v.size() - 1);
	    float s1 = 2 * (float)(i + 1) / (v.size() - 1);

	    for (j = 0; j <= ndice; j++) {
		float a = 2 * M_PI * j / ndice;

		float t = (float)j / ndice;

		glTexCoord3f(s0, t, tex[i]);
		glNormal3f(n[i].x,
			   cos(a) * n[i].y, 
			   sin(a) * n[i].y);
		glVertex3f(v[i].x, 
			   cos(a) * v[i].y, 
			   sin(a) * v[i].y);

		glTexCoord3f(s1, t, tex[i + 1]);
		glNormal3f(n[i + 1].x,
			   cos(a) * n[i + 1].y, 
			   sin(a) * n[i + 1].y);
		glVertex3f(v[i + 1].x, 
			   cos(a) * v[i + 1].y, 
			   sin(a) * v[i + 1].y);
	    }
	    glEnd();
	}
    }
    
};
VOB_DEFINED(Fillet3D);


/** Draw a surface of blended fillets ...
 */
struct Fillet3DBlend {
    enum { NTrans = -1 };

    int ndice;
    float dicelen;
    int tblsize;
    int mode;

    template<class F> void params(F &f) {
	f(ndice, dicelen, tblsize, mode);
    }

    template<class T> float crad(const T &t) const {
	return 0.5 * t.getSqSize().x;
    }

    typedef Filletoid<StretchedCircleFillet> Conn;

    ZVec blend(Conn *conns[], int N, float r, ZVec pt, 
	       int *maxip = NULL) const {
	int i, num = 0;
	double sum = 0;
	float x[N];
	float maxt = -1;
	int maxi = -1;

	pt = pt.normalized();

	//cout << "Blending " << pt << ": ";
	// Compute distances from the node for each fillet surface
	for (i = 0; i < N; i++) {
	    bool success;
	    float t = conns[i]->rad(pt, success);
	    if (success && t > r) {
		if (t > maxt) {
		    maxt = t;
		    maxi = i;
		}
		x[num++] = (t - r) / r;

		// Approximate the slope of the fillet surface
		float t0 = sqrt(conns[i]->c.d * conns[i]->c.d +
				conns[i]->c.t * conns[i]->c.t);
		t = (t - r) / (t0 - r);
		t = (t >? 0) <? 1;
		t = sqrt(t * (2 - t)) / (1 - t);

		sum += t; 

		//cout << t << " "; 
	    }
	}
	if (maxip) *maxip = maxi;

	// Compute p for an l^p norm to be used as the blending function
	// p == 1: sum of distances, 
	// p == \infty: maximum of distances
	float p = 1.0 + sum;

	//cout << "p=" << p << " ";

	sum = 0;
	for (i = 0; i < num; i++) {
	    sum += pow(x[i], p);
	}

	if (!finite(sum) || !finite(p))
	    return pt * maxt;
	
	//cout << "->" << pow(sum, 1 / p) << std::endl;

	return pt * (r + r * pow(sum, 1 / p));

    }

    struct Vert {
	ZVec norm;
	ZVec vert;
	int id;
	Vert(const ZVec &v, int id = 0) : vert(v), id(id) {}
    };

    struct Verts : std::vector<Vert> {
	const Fillet3DBlend &f;
	Conn **conns;
	int N;
	float r;
	bool noblend;

	int append(const ZVec &v, int id = 0) {
	    int ind = size();
	    if (id || noblend)
		push_back(Vert(v, id));
	    else
		push_back(f.blend(conns, N, r, v));

	    return ind;
	}

	int operator() (int i, int j, float fract = .5) {
	    return append(lerp((*this)[i].vert, (*this)[j].vert, fract));
	}

	Verts(const Fillet3DBlend &f, Conn **conns, int N, float r) : 
	    f(f), conns(conns), N(N), r(r), noblend(false) {}

	void startN3V3Operation() {
	    glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
	    glInterleavedArrays(GL_N3F_V3F, sizeof(Vert), &(operator[](0)));
	    glLockArraysEXT(0, size());
	}
	void endN3V3Operation() {
	    glUnlockArraysEXT();
	    glPopClientAttrib();
	}

    };

    struct DiceCrit {
	const Verts &v;
	float dicelen;

	DiceCrit(const Verts &v, float dicelen) : v(v), dicelen(dicelen) {}

	int operator()(int i, int j, int k) {
	    if (dicelen >= 1000) return -1;

	    if (v[i].id && v[j].id && v[i].id != v[j].id) return 0;
	    if (v[j].id && v[k].id && v[j].id != v[k].id) return 1;
	    if (v[k].id && v[i].id && v[k].id != v[i].id) return 2;

	    float l0 = (v[i].vert - v[j].vert).length() * !(v[i].id && v[i].id == v[j].id);
	    float l1 = (v[j].vert - v[k].vert).length() * !(v[j].id && v[j].id == v[k].id);
	    float l2 = (v[k].vert - v[i].vert).length() * !(v[k].id && v[k].id == v[i].id);

	    if (!(l0 < 1000 && l1 < 1000 && l2 < 1000))
		return -1;

	    if (l0 < dicelen && l1 < dicelen && l2 < dicelen)
		return -1;

	    if(l0 > l1 && l0 > l2) return 0;
	    if(l1 > l2) return 1;
	    return 2;
	}
    };


    void addSpan(std::vector<int> &poly, Verts &verts, int i0, int i1, ZVec d0, ZVec d1) const {
	int i, k0 = i0, k1 = i0;
	float d0min = 1E20;
	float d1min = 1E20;
	for (i = i0; i < i1; i++) {
	    float dist0 = (verts[i].vert - d0).length();
	    float dist1 = (verts[i].vert - d1).length();
	    if (dist0 < d0min) d0min = dist0, k0 = i;
	    if (dist1 < d1min) d1min = dist1, k1 = i;
	}

	i = k0;
	while (1) {
	    poly.push_back(i);
	    if (i == k1) break;
	    if (++i == i1) i = i0;
	} 
	
    }

    void renderCylinder(const std::vector<ZVec> &pt, int n, int m,
			bool pole0 = false, bool pole1 = false) const {
	std::vector<ZVec> norm(n * m);
	int i, j;

	ZVec sum(0,0,0);
	for (j = 0; j < m; j++) {
	    ZVec px0 = pt[0 * m + j];
	    ZVec px1 = pt[1 * m + j];
	    ZVec py0 = pt[pole0 * m + (j==  0 ? m-1 : j-1)];
	    ZVec py1 = pt[pole0 * m + (j==m-1 ? 0 : j+1)];
	    
	    sum += norm[0 * m + j] = (px1 - px0).crossp(py1 - py0).normalized();
	}
	if (pole0) {
	    sum = sum.normalized();
	    for (j = 0; j < m; j++) norm[0 * m + j] = sum;
	}

	sum = ZVec(0,0,0);
	for (i = 1; i < n-1; i++) {
	    for (j = 0; j < m; j++) {
		ZVec px0 = pt[(i-1) * m + j];
		ZVec px1 = pt[(i+1) * m + j];
		ZVec py0 = pt[i * m + (j==  0 ? m-1 : j-1)];
		ZVec py1 = pt[i * m + (j==m-1 ?   0 : j+1)];

		norm[i * m + j] = (px1 - px0).crossp(py1 - py0).normalized();
	    }
	}

	for (j = 0; j < m; j++) {
	    ZVec px0 = pt[(n-2) * m + j];
	    ZVec px1 = pt[(n-1) * m + j];
	    ZVec py0 = pt[(n-1-pole1) * m + (j==  0 ? m-1 : j-1)];
	    ZVec py1 = pt[(n-1-pole1) * m + (j==m-1 ?   0 : j+1)];

	    sum += norm[(n - 1) * m + j] = (px1 - px0).crossp(py1 - py0).normalized();
	}
	if (pole1) {
	    sum = sum.normalized();
	    for (j = 0; j < m; j++) norm[(n-1) * m + j] = sum;
	}

	for (i = 0; i < n - 1; i++) {
	    glBegin(GL_QUAD_STRIP);
	    for (j = 0; j < m; j++) {
		glNormal(norm[i * m + j]);
		glVertex(pt[i * m + j]);

		glNormal(norm[(i+1) * m + j]);
		glVertex(pt[(i+1) * m + j]);
	    }
	    glNormal(norm[i * m + 0]);
	    glVertex(pt[i * m + 0]);

	    glNormal(norm[(i+1) * m + 0]);
	    glVertex(pt[(i+1) * m + 0]);
	    glEnd();
	}
    }

    void renderCylinder2(const std::vector<ZVec> &pt, 
			 const std::vector<bool> &clip, 
			 const std::vector<float> &tex, 
			 int n, int m) const {
	std::vector<ZVec> norm(n * m);
	int i, j;

	for (i = 0; i < n; i++) {
	    for (j = 0; j < m; j++) {
		ZVec px0 = pt[(i==  0 ?   0 : i-1) * m + j];
		ZVec px1 = pt[(i==n-1 ? n-1 : i+1) * m + j];
		ZVec py0 = pt[i * m + (j==  0 ? m-1 : j-1)];
		ZVec py1 = pt[i * m + (j==m-1 ?   0 : j+1)];

		norm[i * m + j] = (px1 - px0).crossp(py1 - py0).normalized();
	    }
	}

	for (j = 0; j < m; j++) {

	    glBegin(GL_QUAD_STRIP);
	    for (i = 0; i < n; i++) {
		glTexCoord3f(0, 0, tex[i * m + j]);
		glNormal(norm[i * m + j]);
		glVertex(pt[i * m + j]);
		
		glTexCoord3f(0, 0, tex[i * m + (j + 1) % m]);
		glNormal(norm[i * m + (j + 1) % m]);
		glVertex(pt[i * m + (j + 1) % m]);

		int k;
		for (k = i; k < n; k++) 
		    if (!clip[k * m + j] ||
			!clip[i * m + (j + 1)])
			break;
		if (k > i) {
		    if (k == n) break;
		    if (k >= i + 2) {
			glEnd();
			i = k - 2;
			glBegin(GL_QUAD_STRIP);
		    }
		}
	    }
	    glEnd();
	}
    }

    void render(const Transform **t, int n) const {
	const Transform &thick_t = *t[0];
	const Transform &angle_t = *t[1];

	const Transform &t0 = *t[2];

	int N = n - 3;
	ZVec p0 = t0.transform(0.5 * t0.getSqSize());
	float r = crad(t0);
	CircularNode node(ZVec(0,0,0), r);

	Conn* conns[N];

	std::vector<ZVec> dirs;
	int i, j;

	for (i = 0; i < N; i++) {
	    const Transform &t1 = *t[3 + i];
	    ZVec p1 = t1.transform(0.5 * t1.getSqSize());
	    float d = (p1 - p0).length() / 2;
	
	    FilletSpan2::Conn conn(thick_t, angle_t, t0, t1, d);
	    
	    conns[i] = new Conn(node, d, conn.th, conn.a, 
				(p1 - p0).normalized(), tblsize);

	    dirs.push_back((p1 - p0).normalized());
	}

	if (mode == 0 || mode == 1) {

	for (int k = 0; k < N; k++) {
	    std::vector<ZVec> pt((ndice + 1) * ndice);
	    std::vector<float> tex(pt.size());

	    ZVec ref = t0.transform(ZVec(0,0,1)) - t0.transform(ZVec(0,0,0));
	    ZVec e0 = conns[k]->dir;
	    ZVec e1 = ref.crossp(e0).normalized();
	    ZVec e2 = e0.crossp(e1);

	    for (i = 0; i <= ndice; i++) {
		float f = (float)i / ndice;
		
		Vec v = conns[k]->f.point(f);

		for (j = 0; j < ndice; j++) {
		    float a = j * 2 * M_PI / ndice;

		    pt[i * ndice + j] = 
			v.x * e0
			+ v.y * cos(a) * e1 
			+ v.y * sin(a) * e2;
		    tex[i * ndice + j] = 1 - f;
		}
	    }

	    std::vector<bool> clip(pt.size());
	    // note: not blending the connection mid-point
	    for (j = 0; j < ndice; j++) {
		pt[j] += p0;
		clip[j] = false;
	    }
	    for (i = 1; i <= ndice; i++) {
		for (j = 0; j < ndice; j++) {
		    int ind = i * ndice + j;
		    int maxi;
		    pt[ind] = blend(conns, N, r, pt[ind], &maxi) + p0;
		    clip[ind] = (maxi != k);
		}
	    }
	    
	    renderCylinder2(pt, clip, tex, ndice + 1, ndice);

	    if (k == 0 && mode == 0) {
		for (i = 0; i <= ndice; i++) {
		    float f = (float)i / ndice;
		
		    float a = lerp(conns[k]->f.tangentAngle, M_PI, f);
		    Vec v(r * cos(a), r * sin(a));

		    for (j = 0; j < ndice; j++) {
			float a = j * 2 * M_PI / ndice;
			
			pt[i * ndice + j] = p0 + 
			    v.x * e0
			    + v.y * cos(a) * e1 
			    + v.y * sin(a) * e2;
		    }
		}

		glTexCoord3f(0,0,0);
		renderCylinder(pt, ndice + 1, ndice, false, true);
	    }
	}

	} else if (mode == 2) {

	    std::vector<ZVec> pt((ndice + 1) * (ndice * 2));

	    for (i = 0; i <= ndice; i++) {
		float a = i * M_PI / ndice;
		float x = cos(a);
		float R = sin(a);

		for (j = 0; j < ndice*2; j++) {
		    float b = j * M_PI * 2 / (ndice*2);

		    float y = cos(b) * R;
		    float z = sin(b) * R;

		    pt[i * (ndice * 2) + j] = r * ZVec(x, y, z);
		}
	    }



	    for (i = 0; i < (ndice + 1) * (ndice * 2); i++)
		pt[i] = blend(conns, N, r, pt[i]) + p0;

	    renderCylinder(pt, ndice + 1, ndice * 2, true, true);
	} else {
		

	// use Dicer

	Verts verts(*this, conns, N, r);
	::Vob::Dicer::Triangles<Verts> triangler(verts);

	if (mode == 3) {

	// use Delaunay triangulation based topology

	if (dirs.size() == 2) {
	    ZVec sum = dirs[0] + dirs[1];
	    ZVec dif = dirs[1] - dirs[0];

	    ZVec v0 = -sum.normalized();
	    ZVec v1 = sum.crossp(dif).normalized();
	    dirs.push_back((v0 - v1).normalized());
	    dirs.push_back((v0 + v1).normalized());
	}

	std::vector<int3> tri;
	Triangulate(dirs, dirs.size(), tri);

	{
	    int edge[dirs.size() * dirs.size()];
	    int vert[dirs.size()];
	    FindEdges(edge, dirs.size(), tri);
	    FindEdgeVertices(vert, edge, dirs.size());
	    ZVec sum(0,0,0);
	    bool incomplete = false;
	    for (i = 0; i < (int)dirs.size(); i++) {
		if (vert[i]) {
		    sum += dirs[i];
		    incomplete = true;
		}
	    }
	    if (incomplete) {
		dirs.push_back(-sum.normalized());
		tri.clear();
		Triangulate(dirs, dirs.size(), tri);
	    }
	}
	    
	/*
	for (i = 0; i < (int)tri.size(); i++) {
	    glBegin(GL_LINE_LOOP);
	    glVertex(p0 + 3 * r * dirs[tri[i][0]]);
	    glVertex(p0 + 3 * r * dirs[tri[i][1]]);
	    glVertex(p0 + 3 * r * dirs[tri[i][2]]);
	    glEnd();
	}
	*/

	// Add the vertices of the diced midsections of the connectors
	for (i = 0; i < N; i++) {
	    float t = conns[i]->c.t;
	    float d = conns[i]->c.d;

	    ZVec ref = t0.transform(ZVec(0,0,1)) - t0.transform(ZVec(0,0,0));
	    ZVec e0 = conns[i]->dir;
	    ZVec e1 = ref.crossp(e0).normalized();
	    ZVec e2 = e0.crossp(e1);

	    ZVec p0 = d * e0;

	    for (j = 0; j < ndice; j++) {
		float a = -j * M_PI * 2 / ndice;
		verts.append(p0 + t * (e1 * cos(a) + e2 * sin(a)), i + 1);
	    }
	}

	// Add the dummy connector vertices
	int dummy_i0 = verts.size();
	for (i = N; i < (int)dirs.size(); i++) {
	    verts.append(dirs[i]);
	}

	// Triangulate the surface
	for (i = 0; i < (int)tri.size(); i++) {
	    std::vector<int> poly;
	    for (j = 0; j < 3; j++) {
		int k = tri[i][j];
		if (k >= N) {
		    poly.push_back(dummy_i0 + k - N);
		} else {
		    int k0 = tri[i][(j+1)%3];
		    int k1 = tri[i][(j+2)%3];
			
		    addSpan(poly, verts, ndice * k, ndice * (k + 1), dirs[k1], dirs[k0]);
		}
	    }

	    // Triangulate as a star polygon
	    ZVec sum(0,0,0);
	    for (j = 0; j < (int)poly.size(); j++)
		sum += verts[poly[j]].vert.normalized();

	    int nvert = verts.append(dirs[tri[i][0]] +
				     dirs[tri[i][1]] +
				     dirs[tri[i][2]]);

	    for (j = 0; j < (int)poly.size(); j++)
		triangler.add(nvert, poly[j], poly[(j+1) % poly.size()]);
	}

	} else if (mode == 4 || mode == 5) {

	// icosahedron code adapted from sphere.c found in
	// http://www.sgi.com/Technology/openGL/advanced/programs.html

/* for icosahedron */
#define CZ_ (0.89442719099991)   /*  2/sqrt(5) */
#define SZ_ (0.44721359549995)   /*  1/sqrt(5) */
#define C1_ (0.951056516)        /* cos(18),  */
#define S1_ (0.309016994)        /* sin(18) */
#define C2_ (0.587785252)        /* cos(54),  */
#define S2_ (0.809016994)        /* sin(54) */
#define X1_ (C1_*CZ_)
#define Y1_ (S1_*CZ_)
#define X2_ (C2_*CZ_)
#define Y2_ (S2_*CZ_)

	if (mode == 5) verts.noblend = true;
	int Ip0 = verts.append(ZVec(  0.,    0.,    1.));
	int Ip1 = verts.append(ZVec(-X2_,  -Y2_,   SZ_));
	int Ip2 = verts.append(ZVec( X2_,  -Y2_,   SZ_));
	int Ip3 = verts.append(ZVec( X1_,   Y1_,   SZ_));
	int Ip4 = verts.append(ZVec(  0,    CZ_,   SZ_));
	int Ip5 = verts.append(ZVec(-X1_,   Y1_,   SZ_));
	
	int Im0 = verts.append(ZVec(-X1_,  -Y1_,  -SZ_));
	int Im1 = verts.append(ZVec(  0,   -CZ_,  -SZ_));
	int Im2 = verts.append(ZVec( X1_,  -Y1_,  -SZ_));
	int Im3 = verts.append(ZVec( X2_,   Y2_,  -SZ_));
	int Im4 = verts.append(ZVec(-X2_,   Y2_,  -SZ_));
	int Im5 = verts.append(ZVec(  0.,    0.,   -1.));

        /* front pole */
        triangler.add(Ip0, Ip1, Ip2);
        triangler.add(Ip0, Ip5, Ip1);
        triangler.add(Ip0, Ip4, Ip5);
        triangler.add(Ip0, Ip3, Ip4);
        triangler.add(Ip0, Ip2, Ip3);

        /* mid */
        triangler.add(Ip1, Im0, Im1);
        triangler.add(Im0, Ip1, Ip5);
        triangler.add(Ip5, Im4, Im0);
        triangler.add(Im4, Ip5, Ip4);
        triangler.add(Ip4, Im3, Im4);
        triangler.add(Im3, Ip4, Ip3);
        triangler.add(Ip3, Im2, Im3);
        triangler.add(Im2, Ip3, Ip2);
        triangler.add(Ip2, Im1, Im2);
        triangler.add(Im1, Ip2, Ip1);

        /* back pole */
        triangler.add(Im3, Im2, Im5);
        triangler.add(Im4, Im3, Im5);
        triangler.add(Im0, Im4, Im5);
        triangler.add(Im1, Im0, Im5);
        triangler.add(Im2, Im1, Im5);

	}
	

	triangler.dice(DiceCrit(verts, verts.noblend ? .01 * dicelen : dicelen));

	if (verts.noblend) {
	    ZVec e0 = (t0.transform(ZVec(1,0,0)) - t0.transform(ZVec(0,0,0)));
	    ZVec e1 = (t0.transform(ZVec(0,1,0)) - t0.transform(ZVec(0,0,0)));
	    ZVec e2 = (t0.transform(ZVec(0,0,1)) - t0.transform(ZVec(0,0,0)));
	    for (i = 0; i < (int)verts.size(); i++)
		verts[i] = blend(conns, N, r, 
				 e0 * verts[i].vert.x + 
				 e1 * verts[i].vert.y + 
				 e2 * verts[i].vert.z);
	}

	// Compute normals
	for(::Vob::Dicer::Triangles<Verts>::Titer x = triangler.tris.begin(); 
	    x != triangler.tris.end(); x++) {
	    ZVec v0 = verts[x->v[1]].vert - verts[x->v[0]].vert;
	    ZVec v1 = verts[x->v[2]].vert - verts[x->v[1]].vert;
	    ZVec norm = v0.crossp(v1).normalized();
	    verts[x->v[0]].norm += norm;
	    verts[x->v[1]].norm += norm;
	    verts[x->v[2]].norm += norm;
	}

	for (i = 0; i < (int)verts.size(); i++)
	    verts[i].vert += p0;
	
	verts.startN3V3Operation();
	triangler.draw();
	verts.endN3V3Operation();

	}

	for (i = 0; i < N; i++) {
	    delete conns[i];
	}
	
    }
	
};

VOB_DEFINED(Fillet3DBlend);



}
}

#endif
