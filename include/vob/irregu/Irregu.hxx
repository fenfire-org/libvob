/*
Irregu.hxx
 *    
 *    Copyright (c) 2003, Janne Kujala
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
 * Written by Janne Kujala
 */

#include <vob/Vec23.hxx>
#include <GL/gl.h>
#include <iostream>

namespace Vob {

// XXX: name
namespace Irregu {

PREDBGVAR(dbg_irregu);

    inline void setDotVec(float angle, int angles, float dotvec[]) {
	float a = (angle + M_PI) * angles * (1 / M_PI);
	int ind = (int)a;
	float f = a - (int)a;
	
	dotvec[ind % angles] = 1 - f;
	dotvec[(ind + 1) % angles] = f;
    }

    template <class T>
    void nop(T t) {};

    inline void texCoord(ZPt p) {
	glTexCoord3f(p.x, p.y, p.z);
    }

    inline void texCoord(ZPt p, float q) {
	glTexCoord4f(p.x, p.y, p.z, q);
    }

    inline void multiTexCoord(GLenum unit, ZPt p, float q) {
	//std::cout << "multitexcoord" << (unit & 61)
	//	  << " " << p << q << "\n";
	glMultiTexCoord4f(unit, p.x, p.y, p.z, q);
    }

    inline void vertex(ZPt p) {
	glVertex3f(p.x, p.y, p.z);
    }

    inline void vertex(ZPt p, float w) {
	glVertex4f(p.x, p.y, p.z, w);
    }

    inline void vertex_mul(ZPt p, float w) {
	glVertex4f(p.x * w, p.y * w, p.z * w, w);
    }

    const unsigned Y_COLOR       = 1;
    const unsigned Y_SECCOLOR    = 2;
    const unsigned DOTVEC_COLOR  = 4;
    const unsigned INTERP_DOTVEC = 8;
    const unsigned SLICE_1D      = 16;
    const unsigned SLICE_2D      = 32;
    const unsigned SHIFTS        = 64;
    const unsigned INSIDE        = 128;
    const unsigned SHIFTS8       = 256;

    template <class Coords>
    void fill(const Coords &coords, ZPt center,
	      const vector<ZPt> &pt, const vector<ZVec> &norm, 
	      float border0) {
	glBegin(GL_TRIANGLE_FAN);
	coords.vertex(center);
	for (unsigned i = 0; i < pt.size(); i++)
	    coords.vertex(pt[i] + border0 * norm[i]);
	glEnd();
    }

    /** Compute w value for the point d in quadrilateral a,b,c,d
     * Use w(b,c,d), w(a,c,d), w(a,b,d), w(a,b,c) to get compatible 
     * w values for all vertices
     */
    inline float compute_w(Pt a, Pt b, Pt c) {
	return fabs(c.x * (b.y - a.y) + b.x * (a.y - c.y) + a.x * (c.y - b.y));
    }


    inline double power(double x, double y) {
	return x < 0 ? -pow(-x,y) : pow(x,y);
    }

    inline vector<ZPt> getEllipse(int n, float pow0 = 2.0, float pow1 = 2.0) {
	vector<ZPt> vert(n);
	for (int i = 0; i < n; i++) { 
	    float a = i * 2*M_PI / n;
	    vert[i] = ZPt(power(cos(a), 2 / pow0), 
			  power(sin(a), 2 / pow1), 0);
	}
	return vert;
    }

    template <class Coords>
    void transform(const Coords &coords2, vector<ZPt> &pt) {
	for (unsigned i = 0; i < pt.size(); i++)
	    pt[i] = coords2.transform(pt[i]);
    }

    inline vector<ZVec> computeNorms(vector<ZPt> &pt, unsigned n) {
	vector<ZVec> norm(n + 1);
        for (unsigned i = 0; i <= n; i++) {
            ZVec v1 = (pt[(i+1) % n] - pt[i % n]).cw90().normalized();
            ZVec v2 = (pt[i % n] - pt[(i+n-1) % n]).cw90().normalized();
	    
            norm[i] = (v1 + v2).normalized();
            norm[i] *= 1.0 / v1.dot(norm[i]);
        }
	return norm;
    }

    /** Draws an irregular edge. 
     * The edge specified in paper coordinates. 
     *
     * @param coords     vertex->screen transformation
     * @param pt         vertices of the edge
     * @param norm       normals at each vertex 
     * @param texscale   vertex to texcoords scaling (divides texcoords)
     * @param linewidth  width of border line in pixels at refsize zoom 
     *                   (actually in units of the coords mapping destination)
     * @param refsize    the coords zoom factor where linewidth is defined
     * @param scale_pow  linewidth scaling exponent: e.g., 
     *                   0 constant, 1 linear, 1/2 sqrt
     * @param border0    quad inner edge displacement in units of norm
     * @param border1    quad outer edge displacement in units of norm
     * @param texslicing border0 and border1 factor for texcoords
     *                   0: 1D texture slice, 1: direct sprinkled
     * @param c0         constant vector; currently specifies 
     *                   inner and outer color 4+4 floats
     * @param c1         second constant vecotr; used for secondary
     *                   color if c0 is already taken for primary color
     * @param angles     number of precomputed slicing angles in the texture
     * @param multi      the "radius" of texture units (i.e, (1 + 2 * multi)
     *                   units); multi > 0 also enables some hardcoded options
     * @param flags 
     *   Y_COLOR         glColor4fv(c0) / glColor4fv(c0+4) at inner/outer edge
     *   Y_SECCOLOR      as Y_COLOR but with glSecondaryColor3fvEXT(...)
     *   DOTVEC_COLOR    specify the angle-interpolation dotvector as glColor
     *                   currently assumes a span of 180 degrees, i.e.,
     *                   only works for a pure 1D slice 
     *                   (2D texture slice requires full 360 degree span)
     *   INTERP_DOTVEC   interpolate the dotvector using normals at each vertex
     *                   (as opposed to using tangents at each quad)
     *   SLICE_1D        map 1D tex slice (or 2D if texslicing != 0)
     *   SLICE_2D        map 2D tex slice (use w/ SLICE_1D to get both coords)
     *   SHIFTS          draw each quad 3 or 4 (texslicing != 0) times 
     *                   with linewidth-perturbed coordinates
     *   INSIDE          draw the inside as a polygon 
     *                   (also maps texcoords as in the inner edge)
     */
    template <class Coords>
    void draw(const Coords &coords, 
	      const vector<ZPt> &pt, const vector<ZVec> &norm, 
	      float texscale, float linewidth, float refsize, float scale_pow,
	      float border0, float border1, float texslicing,
	      const float c0[], const float c1[],
	      int angles, int multi,
	      unsigned flags) {

	DBG(dbg_irregu) << "Irregu draw "<<pt.size() <<" "<<norm.size()<<"\n";
    
	if (pt.size() < 2) return;

	void (*colorfv)(const GLfloat *v) = nop;
	void (*colorfv2)(const GLfloat *v) = nop;

	
	if (flags & Y_COLOR) {
	    DBG(dbg_irregu) << "COLOR1 SET\n";
	    colorfv = glColor4fv;
	}
	if (flags & Y_SECCOLOR) {
	    if(flags & Y_COLOR) {
		colorfv2 = glSecondaryColor3fvEXT;
		DBG(dbg_irregu) << "COLOR2 SET\n";
	    } else {
		colorfv =  glSecondaryColor3fvEXT;
		DBG(dbg_irregu) << "COLOR1 SET2\n";
	    }
	}
	

	ZPt vert[pt.size()][2];
	ZPt vert1[pt.size()][2];
	ZPt vert2[pt.size()][2];

	for (unsigned i = 0; i < pt.size(); i++) {
	    vert[i][0] = pt[i] + border0 * norm[i];
	    vert[i][1] = pt[i] + border1 * norm[i];
	    vert1[i][0] = pt[i] + texslicing * border0 * norm[i];
	    vert1[i][1] = pt[i] + texslicing * border1 * norm[i];
	    vert2[i][0] = coords.transform(vert[i][0]);
	    vert2[i][1] = coords.transform(vert[i][1]);
	}

	if (flags & INSIDE) {
	    DBG(dbg_irregu) << "Inside\n";
	    colorfv(c0);
	    glBegin(GL_POLYGON);
	    for (unsigned i = 0; i < pt.size(); i++) {
		texCoord(vert[i][0], texscale);
		if ((flags & SLICE_1D + SLICE_2D) == SLICE_1D + SLICE_2D)
		    multiTexCoord(GL_TEXTURE1, vert[i][0], texscale);
		coords.vertex(vert[i][0]);
	    }
	    glEnd();
	}

	if ((flags & SLICE_2D) && !(flags & SLICE_1D)) {
	    DBG(dbg_irregu) << "Slice2D\n";
	    glBegin(GL_QUAD_STRIP);
	    for (unsigned i = 0; i < pt.size(); i++) {

		if (flags & DOTVEC_COLOR) {
		    // angle(norm.cw90())
		    float angle = atan2(-norm[i].x, norm[i].y); 
		    float dotvec[4] = {0,0,0,0};
		    setDotVec(angle, angles, dotvec);
		    glColor4fv(dotvec);
		}
		
		colorfv(c0);
		colorfv2(c1);
		texCoord(vert[i][0], texscale);
		vertex(vert2[i][0]);

		colorfv(c0 + 4);
		colorfv2(c1 + 4);
		texCoord(vert[i][1], texscale);
		vertex(vert2[i][1]);
	    }
	    glEnd();
	}
	
	if (flags & SLICE_1D) {
	    DBG(dbg_irregu) << "Slice1D\n";
	    glBegin(GL_QUADS);
	    for (unsigned i = 0, j = 1; j < pt.size(); i++, j++) {
		DBG(dbg_irregu) << "Slice1D loop "<<i<<" "<<j<<"\n";

		ZVec dv0 = vert[i][0] - vert[j][0];
		ZVec dv1 = vert[i][1] - vert[j][1];
		ZVec dv0t;
		ZVec dv1t;

		ZVec d0, d1;
		float dy0=0, dy1=0;
		float scale0=0, scale1=0;

		if (multi > 0 || (flags & SHIFTS+SHIFTS8)) {
		    DBG(dbg_irregu) << "Slice1D loop opt1\n";
		    dv0t = vert2[j][0] - vert2[i][0];
		    dv1t = vert2[j][1] - vert2[i][1];

		    scale0 = dv0t.length() / (refsize * dv0.length());
		    scale1 = dv1t.length() / (refsize * dv1.length());
		    scale0 = linewidth * pow(scale0, scale_pow);
		    scale1 = linewidth * pow(scale1, scale_pow);

		    if (multi > 0) {
		    // XXX: .75 hardcoded
		    ZVec dt = pt[j] - pt[i];
		    d0 = scale0 * dt * (.75 / dv0t.length());
		    d1 = scale1 * dt * (.75 / dv1t.length());

		    float dy = 1.0 / 
			dv0t.cw90().normalized().dot(vert2[i][1] - vert2[i][0]);
		    dy0 = scale0 * dy;
		    dy1 = scale1 * dy;
		    //std::cout << dy << " " << dy0 << " " << dy1 << "\n";
		    }
		}

		float q1 = dv0.dot(dv1) / dv0.dot(dv0);
		q1 /= lerp(1.0, q1, texslicing);
		ZPt a1q = vert1[i][1] * q1;
		ZPt b1q = vert1[j][1] * q1;

		ZVec shift0(0,0,0);
		ZVec shift1(0,0,0);

		int numshifts = flags & SHIFTS 
		    ? 3 + (texslicing != 0 || flags & SLICE_2D) 
		    : 1;
		if (flags & SHIFTS8) numshifts = 8;
		for (int s = 0; s < numshifts; s++) {
		if (flags & SHIFTS+SHIFTS8) {
		    DBG(dbg_irregu) << "Slice1D loop opt2 "<<
			s<<"\n";
		    switch (s) {
		    case 0: shift0 = scale1 * dv0t.normalized().cw90(); break;
		    case 1: shift0 = scale1 * dv1t.normalized(); break;
		    case 2: shift0 = -scale1 * dv1t.normalized(); break;
		    case 3: shift0 = -scale1 * dv0t.normalized().cw90(); break;
		    case 4: shift0 = scale1 * 0.707106781186547 * (dv0t.normalized().cw90() + dv1t.normalized()); break;
		    case 5: shift0 = scale1 * 0.707106781186547 * (dv0t.normalized().cw90() - dv1t.normalized()); break;
		    case 6: shift0 = scale1 * 0.707106781186547 * (-dv0t.normalized().cw90() + dv1t.normalized()); break;
		    case 7: shift0 = scale1 * 0.707106781186547 * (-dv0t.normalized().cw90() - dv1t.normalized()); break;
		    }
		    shift1 = shift0;
		}

		DBG(dbg_irregu) << "Slice1D loop pos 3 \n";

		if (flags & DOTVEC_COLOR) {
		    float angle = flags & INTERP_DOTVEC ?
			atan2(-norm[i].x, norm[i].y) :
			atan2(pt[j].y - pt[i].y,
			      pt[j].x - pt[i].x); 
		    DBG(dbg_irregu) << "Slice1D loop opt3 "<<angle<<" "<<angles<<" \n";
		    float dotvec[4] = {0,0,0,0};
		    setDotVec(angle, angles, dotvec);
		    glColor4fv(dotvec);
		}
		DBG(dbg_irregu) << "Slice1D loop pos3.5 \n";

		GLenum u;
		DBG(dbg_irregu) << "Slice1D loop pos3.59 \n";

		if (multi > 0) glColor4f(0,0,dy1,.5*dy1);
		DBG(dbg_irregu) << "Slice1D loop pos3.5a "<<c0<<"\n";
		DBG(dbg_irregu) << "C0 0 "<<c0[0]<<"\n";
		DBG(dbg_irregu) << "C0 1 "<<c0[1]<<"\n";
		DBG(dbg_irregu) << "C0 2 "<<c0[2]<<"\n";
		DBG(dbg_irregu) << "C0 3 "<<c0[3]<<"\n";
		DBG(dbg_irregu) << "C0 4 "<<c0[4]<<"\n";
		DBG(dbg_irregu) << "C0 5 "<<c0[5]<<"\n";
		DBG(dbg_irregu) << "C0 6 "<<c0[6]<<"\n";
		DBG(dbg_irregu) << "C0 7 "<<c0[7]<<"\n";
		colorfv(c0 + 4);
		DBG(dbg_irregu) << "Slice1D loop pos3.5b \n";
		colorfv2(c1 + 4);
		DBG(dbg_irregu) << "Slice1D loop pos3.5c \n";
		texCoord(a1q, texscale * q1);
		DBG(dbg_irregu) << "Slice1D loop pos3.5d \n";
		u = GL_TEXTURE1;
		DBG(dbg_irregu) << "Slice1D loop pos3.5e \n";
		if (flags & SLICE_2D) multiTexCoord(u, vert[i][1], texscale);
		DBG(dbg_irregu) << "Slice1D loop pos3.5f \n";
		for (int d = 1; d <= multi; d++) {
		    multiTexCoord(u++, a1q + d * q1 * d1, texscale * q1);
		    DBG(dbg_irregu) << "Slice1D loop pos3.5g \n";
		    multiTexCoord(u++, a1q - d * q1 * d1, texscale * q1);
		    DBG(dbg_irregu) << "Slice1D loop pos3.5h \n";
		}
		DBG(dbg_irregu) << "Slice1D loop pos3.5i \n";
		vertex(vert2[i][1] + shift1);

		DBG(dbg_irregu) << "Slice1D loop pos4 \n";

		if (multi > 0) glColor4f(0,0,dy0,.5*dy0);
		colorfv(c0);
		colorfv2(c1);
		texCoord(vert1[i][0], texscale);
		u = GL_TEXTURE1;
		if (flags & SLICE_2D) multiTexCoord(u, vert[i][0], texscale);
		for (int d = 1; d <= multi; d++) {
		    multiTexCoord(u++, pt[i] + d * d0, texscale);
		    multiTexCoord(u++, pt[i] - d * d0, texscale);
		}
		vertex(vert2[i][0] + shift0);

		DBG(dbg_irregu) << "Slice1D loop pos5 \n";

		if ((flags & DOTVEC_COLOR) && (flags & INTERP_DOTVEC)) {
		    DBG(dbg_irregu) << "Slice1D loop opt4 \n";
		    float angle = atan2(-norm[j].x, norm[j].y); 
		    float dotvec[4] = {0,0,0,0};
		    setDotVec(angle, angles, dotvec);
		    glColor4fv(dotvec);
		}
		DBG(dbg_irregu) << "Slice1D loop pos6 \n";

		//colorfv(c0);
		//colorfv2(c1);
		texCoord(vert1[j][0], texscale);
		u = GL_TEXTURE1;
		if (flags & SLICE_2D) multiTexCoord(u, vert[j][0], texscale);
		for (int d = 1; d <= multi; d++) {
		    multiTexCoord(u++, pt[j] + d * d0, texscale);
		    multiTexCoord(u++, pt[j] - d * d0, texscale);
		}
		vertex(vert2[j][0] + shift0);
		DBG(dbg_irregu) << "Slice1D loop pos7 \n";
		
		if (multi > 0) glColor4f(0,0,dy1,.5*dy1);
		colorfv(c0 + 4);
		colorfv2(c1 + 4);
		texCoord(b1q, texscale * q1);
		u = GL_TEXTURE1;
		if (flags & SLICE_2D) multiTexCoord(u, vert[j][1], texscale);
		for (int d = 1; d <= multi; d++) {
		    multiTexCoord(u++, b1q + d * q1 * d1, texscale * q1);
		    multiTexCoord(u++, b1q - d * q1 * d1, texscale * q1);
		}
		vertex(vert2[j][1] + shift1);
		}
		DBG(dbg_irregu) << "Slice1D loop pos8 \n";
	    }

	    glEnd();
	}

    }
}
}
