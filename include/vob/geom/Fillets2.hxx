/*
Fillets2.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka and Janne V. Kujala
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
 * Written by Tuomas J. Lukka and Janne V. Kujala
 */

#include <boost/format.hpp>

#include <vob/Vec23.hxx>
#include <vob/geom/Quadrics.hxx>

namespace Vob {
namespace Geom {
    PREDBGVAR(dbg_fillets);
    using boost::format;

    /** Concept: a span of edge.
     * Usually, the 0-side is the connection and 1 is
     * return to circle (or other point).
     */
    struct FilletSpanConcept {
	/** Get the point on this part of the edge.
	 */
	ZVec point(float fract, ZVec *intern = 0) const {
	    return ZVec(0,0,0);
	}
    };

    /** Concept: a blendable span of edge.
     * Usually, the 0-side is the connection and 1 is
     * return to circle (or other point).
     */
    struct BlendableFilletSpanConcept : public FilletSpanConcept{
	/** Get the point on a given direction vector.
	 * @param dir The unit direction vector from the center.
	 * @param success Boolean into which to return whether there was something.
	 */
	ZVec point(Vec dir, bool *success) {
	    return ZVec(0,0,0);
	}
    };

    /** A circular node.
     */
    struct CircularNode {
	/** The center of the node.
	 */
	ZVec ctr;
	/** The radius of the node.
	 */
	float r;

	CircularNode(ZVec ctr, float r) : ctr(ctr), r(r) { }

	ZVec point(Vec dir) {
	    return ctr + r * dir;
	}
    };

    /** A connection. 
     */
    struct LinearConnectionHalf {
	const CircularNode &node;

	/** The compass angle of the connection.
	 */
	float a;
	/** The distance from the center to the middle of the connection.
	 */
	float d;
	/** The thickness(radius) of the connection at the middle.
	 */
	float t;
	/** Whether we're looking at the clockwise (+1) or counterclockwise (-1) side.
	 */
	int sign;
	/** The z coordinate of the middle.
	 */
	float z;

	/////////////////////////////////
	// The following data members are derivable from the already mentioned ones.
	// They are calculated here to be cached.

	/** The direction (unit) vector of the connection (from the node).
	 */
	Vec dir;

	/** The normal (unit) vector of the connection pointing to the outside.
	 */
	Vec norm;

	/** The endpoint of the edge.
	 */
	ZVec endPoint;

	LinearConnectionHalf(
		const CircularNode &node,
		float a, 
		float d,
		float t, 
		int sign,
		float z) :
		    node(node),
		    a(a),
		    d(d),
		    t(t),
		    sign(sign),
		    z(z) {
	    dir = dirVec(a);
	    norm = dir.cw90() * sign;
	    endPoint = node.ctr + d * dir + t * norm;
	}

	/** Project a given point to the connecting line.
	 * If the point would go to the negative direction (or inside the circular
	 * node) on the connection
	 * line, it will be clamped to the center.
	 * Useful for generating internal points.
	 * XXX Z calculation needs thinking.
	 */
	ZVec projectToConnLine(ZVec v) const {
	    v = v - norm.dot(v-node.ctr) * norm;
#if 0
	    if(dir.dot(v-node.ctr) <= node.r) {
		v = v - dir.dot(v-node.ctr) * dir;
	    }
#endif
	    v.z = depth(v);
	    return v;
	}

	float depth(Vec v) const {
	    float l = dir.dot(v - node.ctr);
	    //float l = (v - node.ctr).length();
	    float f = (l - node.r) / (d - node.r);
	    if (d > node.r)
		if (f >= 1)
		    return z;
		else if (f >= 0)
		    return lerp(node.ctr.z, z, f);
	    return node.ctr.z;
	}

    };

    /** A nonlinearly stretched circle fillet (test). 
     * Not blendable yet.
     */
    struct StretchedCircleFillet {
	const CircularNode &node;
	const LinearConnectionHalf &conn;
	float tangentAngle;

	Vec dirTang;
	float dtSign;

	float r1;

	float C;

	float x0;
	float x1;

	float cutangle;

	StretchedCircleFillet(
		const CircularNode &node,
		const LinearConnectionHalf &conn,
		float ta
		) : node(node), conn(conn), tangentAngle(ta) {
	    r1 = (conn.t - sin(ta) * node.r) / (sin(ta) - 1);

	    x0 = cos(ta) * node.r;

	    float d0 = cos(ta) * r1;
	    
	    C = (conn.d - x0 - d0) / (d0 * d0 * d0);

	    x1 = (node.r+r1) * cos(ta);

	    dirTang = dirVec(conn.a - conn.sign * tangentAngle);
	    dtSign = conn.sign;

	    cutangle = M_PI/2 - tangentAngle;
	}
	ZVec point(float fract, ZVec *intern = 0) const {
	    float angle = lerp(0, cutangle, fract);

	    float y = (1-cos(angle)) * r1 + conn.t;

	    float x = x1 - sin(angle) * r1 - x0;

	    float xn = x + C*x*x*x + x0;

	    if (!(fabs(C) < 1E4)) xn = (fract == 0) ? conn.d : x0;

	    ZVec pt = node.ctr + xn * conn.dir + y * conn.norm;

	    ZVec proj = conn.projectToConnLine(pt);
	    //pt.z = proj.z;
	    pt.z = conn.depth(pt);
	    if(intern) {
		//if(fract > .94)
		//    *intern = node.ctr;
		//else
		    *intern = proj;
		intern->z -= 20;
	    }
	    return pt;

	}

	ZVec point(Vec dir, bool &success, float *ptfract = NULL, float tol = .01) const {
	    float f0 = 0;
	    float f1 = 1;
	    float f;

	    ZVec p0 = point(f0) - node.ctr;
	    ZVec p1 = point(f1) - node.ctr;
	    ZVec p;
		
	    if (dir.cross(p0) * dtSign < 0 ||
		dir.cross(p1) * dtSign > 0) {
		success = false;
		return ZVec();
	    }

	    Vec dir2 = dir.cw90();
	    double err;
	    double maxerr = tol * node.r;

	    int iter = 100;
	    while (iter--) {
		f = 0.5 * (f0 + f1);
		p = point(f) - node.ctr;
		err = dir2.dot(p);
		if (fabs(err) < maxerr) break;

		if (dir.cross(p) * dtSign > 0) {
		    p0 = p;
		    f0 = f;
		} else {
		    p1 = p;
		    f1 = f;
		}
	    }
	    if (iter == 0) {
		cout << "EllipseFillet::point reached max iter" << std::endl;
		success = false;
		return ZVec();
	    }

	    success = true;
	    if (ptfract) *ptfract = f;
	    return p - err * dir2 + node.ctr;
	}
	void cutEnd(Vec dir) {
	    bool success;
	    float ptfract;
	    cutangle = M_PI/2 - tangentAngle;
	    point(dir, success, &ptfract, .0001);
	    if (success)
		cutangle = lerp(0, M_PI/2 - tangentAngle, ptfract);
	}

	/** Whether the given direction vector from the center of the node is inside
	 * the fillet. This does *not* guarantee that point(dir) will be a success:
	 * this function includes vectors inside the connection to the fillet but point()
	 * naturally does not.
	 */
	bool infillet(Vec dir) const {
	    return dirTang.cross(conn.dir) * dirTang.cross(dir) >= 0 &&
		conn.dir.cross(dirTang) * conn.dir.cross(dir) >= 0;
	}
	/** Returns true if either of the directions where the circles
	 * are tangent is inside the other fillet area.
	 * This is not *quite* the same as the trivial definition
	 * of overlapping: it returns false if the tangent points are 
	 * past the connections.
	 */
	bool overlaps(const StretchedCircleFillet &other) const {
	    return infillet(other.dirTang) || other.infillet(dirTang);
	}

    };

    /** An elliptical fillet meeting a circle.
     * All the parameters that specify the shape come from outside.
     */
    struct EllipseCircleFillet {
	const CircularNode &node;
	const LinearConnectionHalf &conn;
	float tangentAngle;

	/**  Direction vector pointing to the point where the fillet touches the circle
	 */
	Vec dirTang;
	float dtSign;

	Vec ept, eno;

	/** Lengths of the half axes 
	 */
	Vec elli;

	Vec ey;
	Vec eydir;
	Vec ex;
	Vec exdir;

	Vec ecenter;

	float eangle;

	/** Make a new fillet.
	 * @param ta The tangent angle, i.e. always positive angle between
	 *      connection line and tangent line.
	 */
	EllipseCircleFillet(
		const CircularNode &node,
		const LinearConnectionHalf &conn,
		float ta
		) : node(node), conn(conn), tangentAngle(ta)
		    {
	    eno = Vec(-cos(tangentAngle), sin(tangentAngle));
	    ept = Vec(conn.d, -conn.t) + node.r * eno;

	    elli = Geom::symmellipse__point_norm(ept, eno);

	    ex = -conn.norm * elli.y;
	    exdir = ex.normalized();
	    ey = -conn.dir * elli.x;
	    eydir = ey.normalized();

	    ecenter = conn.endPoint - ex;

	    dirTang = dirVec(conn.a - conn.sign * tangentAngle);
	    dtSign = conn.sign;

	    /* Now, the ellipse always starts at angle 0. Find 
	     * the other angle.
	     */
	    Vec pt2 = ept;
	    pt2.x *= elli.y / elli.x;
	    pt2.y = elli.y - pt2.y;

	    eangle = atan2(pt2.x, pt2.y);

	    DBG(dbg_fillets) << format("Ellipse: ept: %s, eno: %s, elli: %s, ecenter: %s, "
				"dirtang: %s, dtsign: %s, eangle: %s\n") %
			ept % eno % elli % ecenter % dirTang % dtSign % eangle;
	}

	float iAngle(Vec dir, bool &succ) const {
	    Vec pt = dir * node.r;
	    Vec ptRel = ecenter - pt;
	    Vec ptC;
	    ptC.x = exdir.dot(ptRel) / elli.y;
	    ptC.y = eydir.dot(ptRel) / elli.x;

	    Vec norm;
	    norm.x = dir.x * elli.y;
	    norm.y = dir.y * elli.x;

	    ZVec proj = project2circle(ptC + norm, ptC, Vec(0,0), 1, -1, &succ);

	    return Vec(proj).atan();
	}

	ZVec point(float fract, ZVec *intern = 0) const {
	    float angle = lerp(0, eangle, fract);
	    Vec v(cos(angle), sin(angle));
	    ZVec pt = ecenter + v.x * ex + v.y * ey;
	    ZVec proj = conn.projectToConnLine(pt);
	    pt.z = proj.z;
	    if(intern) {
		if(fract > .94)
		    *intern = node.ctr;
		else
		    *intern = proj;
	    }
	    return pt;

	}
	void cutEnd(Vec dir) {
	    bool s;
	    eangle = iAngle(dir, s);
	}
	ZVec point(Vec dir, bool &success) const {
	    if(dirTang.cross(dir) * dtSign < 0) {
		success = false;
		return ZVec(0,0,0);
	    }

	    float an = iAngle(dir, success);
	    if(!success) return ZVec(0,0,0);

	    return point(an / eangle);
	}
	bool infillet(Vec dir) const {
	    return dirTang.cross(conn.dir) * dirTang.cross(dir) >= 0 &&
		conn.dir.cross(dirTang) * conn.dir.cross(dir) >= 0;
	}
	/** Returns true if either of the directions where the circles
	 * are tangent is inside the other fillet area.
	 * This is not *quite* the same as the trivial definition
	 * of overlapping: it returns false if the tangent points are 
	 * past the connections.
	 */
	bool overlaps(const EllipseCircleFillet &other) const {
	    return infillet(other.dirTang) || other.infillet(dirTang);
	}

    };

    /** A circular fillet edge span, for a circular node.
     * This is simply a circular arc from the connection to the point
     * where it is tangent to the circular node.
     */
    struct CircleCircleFillet {
	const CircularNode &node;
	const LinearConnectionHalf &conn;

	/** Center of the circle of which the arc is taken.
	 */
	Vec fcenter;

	/** Radius of the arc.
	 */
	float frad;

	/** The direction vector from the center of the node
	 * to the point where
	 * the two circles are tangent.
	 */
	Vec dirTang;

	/** The sign: calculate cross product of a vector with dirTang,
	 * if sign is same as here, then we it is on the same side as the 
	 * arc.
	 */
	float dtsign;

	/** The start angle of the arc, looking from fcenter.
	 */
	float astart;
	/** The end angle of the arc, looking from fcenter.
	 */
	float aend;

	CircleCircleFillet(
		const CircularNode &node,
		const LinearConnectionHalf &conn) : node(node), conn(conn) {
	    this->fcenter = 
		circle__point_norm_circle(conn.endPoint, conn.norm, node.ctr, node.r);
	    this->frad = 
		(fcenter - conn.endPoint).length();
	    this->dirTang = (fcenter - node.ctr) . normalized();
	    this->dtsign = dirTang.cross(conn.dir);
	    this->astart = Vec(conn.endPoint - fcenter).atan();
	    this->aend = Vec(node.ctr - fcenter).atan();
	    while(aend - astart >= M_PI) aend -= 2 * M_PI;
	    while(astart - aend >= M_PI) aend += 2 * M_PI;
	}

	/** For blending, we want to stop the fillet halfway to avoid
	 * overdraw and strange shapes while cleaving.
	 */
	void cutEnd(Vec dir) {
	    bool success;
	    ZVec in = project2circle(node.ctr + dir, node.ctr, fcenter,
			frad, -1, &success);
	    if(!success) return;
	    this->aend = Vec(in-fcenter).atan();
	    while(aend - astart >= M_PI) aend -= 2 * M_PI;
	    while(astart - aend >= M_PI) aend += 2 * M_PI;
	}

	ZVec point(float fract, ZVec *intern = 0) const {
	    ZVec pt = fcenter + frad * dirVec(lerp(astart, aend, fract));
	    ZVec proj = conn.projectToConnLine(pt);
	    pt.z = proj.z;
	    if(intern) {
		if(fract > .94)
		    *intern = node.ctr;
		else
		    *intern = proj;
	    }
	    return pt;
	}

	ZVec point(Vec dir, bool &success) const {
	    if(dirTang.cross(dir) * dtsign < 0) {
		success = false;
		return ZVec(0,0,0);
	    }
	    ZVec in = project2circle(node.ctr + dir, node.ctr, fcenter,
			frad, -1, &success);
	    if(!success) return ZVec(0,0,0);
	    ZVec proj = conn.projectToConnLine(in);
	    in.z = proj.z;
	    return in;
	}

	/** Whether the given direction vector from the center of the node is inside
	 * the fillet. This does *not* guarantee that point(dir) will be a success:
	 * this function includes vectors inside the connection to the fillet but point()
	 * naturally does not.
	 */
	bool infillet(Vec dir) const {
	    return dirTang.cross(conn.dir) * dirTang.cross(dir) >= 0 &&
		conn.dir.cross(dirTang) * conn.dir.cross(dir) >= 0;
	}
	/** Returns true if either of the directions where the circles
	 * are tangent is inside the other fillet area.
	 * This is not *quite* the same as the trivial definition
	 * of overlapping: it returns false if the tangent points are 
	 * past the connections.
	 */
	bool overlaps(const CircleCircleFillet &other) const {
	    return infillet(other.dirTang) || other.infillet(dirTang);
	}

    };

    /** A blend of two fillets.
     * Note that this is only one side of the blend!
     */
    template<class S1, class S2> struct FilletBlend {
	S1 main;
	const S2 &other;

	FilletBlend(const S1 &main0, 
		    const S2 &other) :
		main(main0), other(other) {
	    Vec cutdir = (main.dirTang + other.dirTang).normalized();
	    main.cutEnd(cutdir);
	}

	ZVec point(float fract, ZVec *intern = 0) const {
	    ZVec p = main.point(fract, intern);
	    bool success;
	    ZVec p2 = other.point(Vec(p-main.node.ctr).normalized(), success);
	    ZVec res;
	    if(success) {
		Vec edgep = p + p2 - 2 * main.node.ctr;
		edgep *= main.node.r / edgep.length();
		res = p + p2 - main.node.ctr - edgep;
	    } else {
		res = p;
	    } 
	    return res;
	}
    };
    template<class S1, class S2> FilletBlend<S1, S2> makeFilletBlend(
		const S1 &s1, const S2 &s2) {
	return FilletBlend<S1,S2>(s1, s2);
    }

    /** A linearly interpolated fillet span between two given 
     * ones.
     */
    template<class S1, class S2> struct LerpFilletSpan {
	const S1 &s1;
	const S2 &s2;
	float lerpfract;
	LerpFilletSpan(const S1 &s1, const S2 &s2, float lerpfract) : s1(s1), s2(s2), lerpfract(lerpfract) { }

	ZVec point(float fract, ZVec *intern = 0) const {
	    ZVec i1, i2;
	    ZVec res = lerp(s1.point(fract, &i1), s2.point(fract, &i2), lerpfract);
	    if(intern)
		*intern = lerp(i1, i2, lerpfract);
	    return res;
	}
    };

    template<class S1, class S2> LerpFilletSpan<S1, S2> makeLerpFilletSpan(
		const S1 &s1, const S2 &s2, float fract) {
	return LerpFilletSpan<S1,S2>(s1, s2, fract);
    }

    /** A span of the circular node shape. 
     */
    struct CircularNodeSpan {
	const CircularNode &node;
	float astart;
	float aend;
	CircularNodeSpan(const CircularNode &node, 
			float astart, float aend)  :
		node(node), astart(astart), aend(aend) {
	}

	ZVec point(float fract, ZVec *intern = 0) const {
	    if(intern) *intern = node.ctr - ZVec(0,0,20);
	    return node.ctr + node.r * dirVec(lerp(astart, aend, fract));
	}

    };

    /** A span of the circular node shape with a segment removed. 
     */
    struct CircularSliceSpan {
	const CircularNode &node;
	ZVec v0;
	ZVec v1;
	CircularSliceSpan(const CircularNode &node, 
			  float astart, float aend)  :
	    node(node), 
	    v0(node.ctr + node.r * dirVec(astart)), 
	    v1(node.ctr + node.r * dirVec(aend)) {
	}
	CircularSliceSpan(const CircularNode &node, 
			  Vec vstart, Vec vend, int mode = 0) :
	    node(node), 
	    v0(vstart.x, vstart.y, node.ctr.z), 
	    v1(vend.x, vend.y, node.ctr.z) {
	}

	ZVec point(float fract, ZVec *intern = 0) const {
	    if(intern) *intern = node.ctr - ZVec(0,0,20);
	    return lerp(v0, v1, fract);;
	}

    };

    struct int3 {
	int v[3];
	int3(int a, int b, int c) { v[0] = a; v[1] = b; v[2] = c; }
	
	int operator[](int i) const { return v[i]; }
    };

    /** Spherical delaunay triangulation
     * Naive O(n^4) implementation,
     * probably has problems with more than three coplanar vertices
     */
    template <class ZVecArray>
    void Triangulate(ZVecArray v, int n, 
		     std::vector<int3> &tri) {
	int i, j, k, l;
	
	for (i = 0; i < n; i++)
	    for (j = i + 1; j < n; j++)
		for (k = j + 1; k < n; k++) {
		    ZVec d = (v[j] - v[i]).crossp(v[k] - v[j]).normalized();
		    float dot = d.dot(v[k]);
		    d /= dot;
		    for (l = 0; l < n; l++) {
			if (l == i || l == j || l == k) continue;
			if (d.dot(v[l]) >= 1) break;
		    }
		    if (l == n)
			if (dot < 0)
			    tri.push_back(int3(j,i,k));
			else
			    tri.push_back(int3(i,j,k));
		}
	
    }

    /** Build a matrix of the number of triangles using each edge
     */
    template <class IntArray>
    void FindEdges(IntArray edge, int n, 
		   std::vector<int3> &tri) {
	int i, j;
	int m = tri.size();

	for (i = 0; i < n; i++)
	    for (j = 0; j < n; j++)
		edge[n * i + j] = 0;

	for (i = 0; i < m; i++) {
	    edge[n * tri[i][0] + tri[i][1]]++;
	    edge[n * tri[i][1] + tri[i][2]]++;
	    edge[n * tri[i][0] + tri[i][2]]++;
	}
    }

    /** Mark vertices whose edges belong to only one triangle 
     */
    template <class IntArray1, class IntArray2>
    void FindEdgeVertices(IntArray1 vert, IntArray2 edge, int n) {
	int i, j;
	
	for (i = 0; i < n; i++)
	    vert[i] = 0;
	
	for (i = 0; i < n; i++)
	    for (j = i + 1; j < n; j++)
		if (edge[n * i + j] == 1)
		    vert[i] = vert[j] = 1;
    }


    template <class Fillet>
    struct Filletoid {
	const CircularNode &node;
	LinearConnectionHalf c;
	Fillet f;
	ZVec dir;

	float a0;
	vector<float> rtbl;

	Filletoid(const CircularNode &node,
		  float d,
		  float th,
		  float a,
		  ZVec dir,
		  int tblsize = 0) :
	    node(node),
	    c(node, 0, d, th, -1, 0), 
	    f(node, c, a), dir(dir) {
	    if (tblsize) compute_rtbl(tblsize);
	}

	Vec trans(ZVec v) const {
	    float x = dir.dot(v);
	    float y = (v - x * dir).length();
	    return Vec(x, y);
	}

	float rad(ZVec v, bool &success) const {
	    Vec t = trans(v);
	    if (rtbl.size()) {
		success = true;
		return rad_rtbl(t);
	    }
	    ZVec pt = f.point(t, success);
	    if (success) return pt.length();
	    if (f.infillet(t)) {
		success = true;
		// return distance to the middle of the connection
		return c.d / v.normalized().dot(dir);
	    }
	    return node.r;
	}

	void compute_rtbl(int n) {
	    rtbl.resize(n + 1);
	    
	    a0 = atan(c.t / c.d);

	    for (int i = 1; i < n; i++) {
		float t = i * (1.0 / n);
		float a = a0 + (t*t) * (f.tangentAngle - a0);
		bool success;
		float fract;
		ZVec pt = f.point(dirVec(a), success, &fract, .0001);
		if (success)
		    rtbl[i] = pt.length();
		else
		    rtbl[i] = node.r;
		//cout << i << ": " << rtbl[i] << pt << fract << std::endl;
	    }
	    rtbl[0] = c.d / cos(a0);
	    rtbl[n] = node.r;
	}

	float rad_rtbl(Vec v) const {
	    int n = rtbl.size() - 1;
	    float a = v.atan();
	    if (a < a0) return rtbl[0];
	    float t = sqrt((a - a0) / (f.tangentAngle - a0));
	    int i = (int)(t * n);
	    if (i >= n) return rtbl[n];

	    float fract = t * n - i;
	    float r0 = rtbl[i];
	    float r1 = rtbl[i + 1];

	    // lerp
	    //return (1 - fract) * r0 + fract * r1;
	    
	    // polar lerp approximation
	    //return r0 * r1 / (r0 * fract + r1 * (1-fract));

	    // polar lerp
	    float da = (f.tangentAngle - a0) / n;
	    da *= 2 * t + 1E-4;// compensate for the squaring
	    return r0 * r1 * sin(da) / (r0 * sin(fract * da) +
					r1 * sin((1-fract) * da)) >? r1;
	}
    };

    
}
}
