/*
Irregu.hxx
 *    
 *    Copyright (c) 2003, Janne V. Kujala
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
 * Written by Janne V. Kujala
 */

#ifndef VOB_VOBS_IRREGU_HXX
#define VOB_VOBS_IRREGU_HXX

#ifndef VOB_DEFINED
#define VOB_DEFINED(x)
#endif

#include <vob/glerr.hxx>
#include <vob/Debug.hxx>
#include <vob/Transform.hxx>
#include <vob/irregu/Irregu.hxx>

namespace Vob {
namespace Vobs {

PREDBGVAR(dbg_irregularquad);

const int IRREGU_SHIFTS = 0x0001;
const int IRREGU_CS2_TO_SCREEN = 0x0002;

/**
 * coords1: paper => window
 * coords2: tearaway => paper (assumed to be affine)
 * The rectangular paper from which the tearaway is torn away
 * is [x0,y0]..[x1,y1] in paper coordinates (as in PaperQuad) and
 * the tearaway part is [-1,1]x[-1,1] in tearaway coordinates.
 * border and ripple_period are relative to the ripple amplitude and period in
 * paper coordinates
 */
class IrregularQuad {
public:
    enum { NTrans = 2 };

    float x0, y0, x1, y1; 
    float border; 
    float freq; 
    int flags; 

    DisplayListID setup;
    float dicefactor;

    template<class F> void params(F &f) {
	float ripple_period;
	f(x0, y0, x1, y1, border, ripple_period, flags, setup, dicefactor);
	freq = 1.0 / ripple_period;
    }

    template <class Coords>
	inline void vert(const Coords& coords1, const Coords& coords2, ZPt q) const {
	ZPt p = coords2.transform(q);
	float xrelp = (p.x - x0)/ (x1-x0);
	float yrelp = (p.y - y0)/ (y1-y0);
	glMultiTexCoord2f(1, 
			  .25 + .5 * xrelp,  // the center texels of 4x4 are colored
			  .25 + .5 * yrelp
			  );
	coords1.vertex(p);
	DBG(dbg_irregularquad) << "Vert: " << q<<p<<coords1.transform(p) <<"\n";
    }


    // Slide v1 into [x0,x1]x[y0,y1]
    void vert(vector<ZPt> &pts, ZPt v1) const {
	if (v1.x < x0) v1.x = x0;
	if (v1.x > x1) v1.x = x1;
	if (v1.y < y0) v1.y = y0;
	if (v1.y > y1) v1.y = y1;
	pts.push_back(v1);
    }

    // Do vert(pts, v) for any crossings and then vert(pts, v1)
    void vert(vector<ZPt> &pts, ZPt v0, ZPt v1) const {
	ZPt v;
	if ((v0.x - x0) * (v1.x - x0) < 0) v = lerp(v0, v1, (x0 - v0.x) / (v1.x - v0.x)), v.x = x0;
	else if ((v0.x - x1) * (v1.x - x1) < 0) v = lerp(v0, v1, (x1 - v0.x) / (v1.x - v0.x)), v.x = x1;
	else if ((v0.y - y0) * (v1.y - y0) < 0) v = lerp(v0, v1, (y0 - v0.y) / (v1.y - v0.y)), v.y = y0;
	else if ((v0.y - y1) * (v1.y - y1) < 0) v = lerp(v0, v1, (y1 - v0.y) / (v1.y - v0.y)), v.y = y1;
	else {
	    return vert(pts, v1);
	}
	vert(pts, v0, v);
	vert(pts, v, v1);
    }
    
    // Draw a polygon as a trinagle fan from the centroid
    template <class Coords>
	void drawStarPoly(const Coords& coords1, vector<ZPt>& p) const {
	float A = 0, cx = 0, cy = 0;
	p.push_back(p[0]);
	for (unsigned i = 0; i < p.size() - 1; i++) {
	    float t = p[i].x * p[i+1].y - p[i+1].x * p[i].y;
	    A += t;
	    cx += (p[i].x + p[i+1].x) * t;
	    cy += (p[i].y + p[i+1].y) * t;
	}
	if (A != 0) {
	    cx /= 3 * A;
	    cy /= 3 * A;
	} else {
	    cx = 0.5 * (x0 + x1);
	    cy = 0.5 * (y0 + y1);
	}
	
	glBegin(GL_TRIANGLE_FAN);
	coords1.vertex(ZPt(cx, cy, p[0].z));
	for (unsigned i = 0; i < p.size(); i++)
	    coords1.vertex(p[i]);
	glEnd();
    }
    

    template<class T> void render(const T &coords1, const T &coords2) const {
        DBG(dbg_irregularquad) << "Irregular quad\n";

        // Normalize tearaway part unit vectors in paper coords to get border widths
        // Note: we assume that coords2 is an affine transform so that the border (ripple) width
        //       is translation invariant
        // const T &cs2inv = coords2.getInverse();
        float bx = border / (coords2.transform(ZPt(1,0,0)) - coords2.transform(ZPt(0,0,0))).length();
        float by = border /  (coords2.transform(ZPt(0,1,0)) - coords2.transform(ZPt(0,0,0))).length();

        DBG(dbg_irregularquad) << "Bordercalc: " << border<<" "<<
	    coords2.transform(ZPt(0,0,0)) << " " <<
	    coords2.transform(ZPt(1,0,0)) << " " <<
	    coords2.transform(ZPt(0,1,0)) << " " <<
	    bx<<" "<<by<<" \n";


        //cout << (coords2.transform(ZPt(1,0,0)) - coords2.transform(ZPt(0,0,0)))
        //     << (coords2.transform(ZPt(0,1,0)) - coords2.transform(ZPt(0,0,0))) << "\n";
        
	/*
        float bx = border * ( (cs2inv.transform(ZPt(0,0,0) + xvec) 
		              - cs2inv.transform(ZPt(0,0,0)))).length();
        float by = border * ( (cs2inv.transform(ZPt(0,0,0) + yvec) 
		              - cs2inv.transform(ZPt(0,0,0)))).length();
	    */

        //cout << bx << "," << by << "\n";

	Pt box = coords2.getSqSize();

        float x0 = box.x;
        float x1 = box.x - bx;
        float x2 = box.x - 2 * bx;
        float mx0 = 0;
        float mx1 = 0 + bx;
        float mx2 = 0 + 2 * bx;
        float y0 = box.y;
        float y1 = box.y - by;
        float y2 = box.y - 2 * by;
        float my0 = 0;
        float my1 = 0 + by;
        float my2 = 0 + 2 * by;
        float w = .5; 

        // scale of last two vertices of sides 
        float wtbl[] = { 1, 1, 1, 1, w, w, w, w };

        ZPt ctr = coords2.transform(ZPt(0,0,0));
        double r1 = (coords2.transform(ZPt(box.x,box.y,0)) - ctr).length();
        double r2 = (coords2.transform(ZPt(box.x,0,0)) - ctr).length();
        double r3 = (coords2.transform(ZPt(0,box.y,0)) - ctr).length();

	double r = r1;
	if(r < r2) r = r2;
	if(r < r3) r = r3;
        double nonl = dicefactor * coords1.nonlinearity(ctr, r);

        int diceb = (int)fabs(2 * border * sqrt(2) * nonl) + 1;
        int dicex = (int)fabs(2 * border * (1 / bx - 2) * nonl) + 1;
        int dicey = (int)fabs(2 * border * (1 / by - 2) * nonl) + 1;

	if(diceb > 20) diceb = 20;
	if(dicex > 60) dicex = 60;
	if(dicey > 60) dicey = 60;

        // Dicing number for each of the primitives 
        int dice[] = { dicey, dicey, dicex, dicex, diceb, diceb, diceb, diceb };

        ZPt sides[][4] =  { // The sides of the rectangle:
                           { ZPt(mx0,my2,0), ZPt(mx0,+y2,0), ZPt(mx1,+y2,0), ZPt(mx1,my2,0) },
                           { ZPt(+x0,my2,0), ZPt(+x0,+y2,0), ZPt(+x1,+y2,0), ZPt(+x1,my2,0) },
                           { ZPt(mx2,my0,0), ZPt(+x2,my0,0), ZPt(+x2,my1,0), ZPt(mx2,my1,0) },
                           { ZPt(mx2,+y0,0), ZPt(+x2,+y0,0), ZPt(+x2,+y1,0), ZPt(mx2,+y1,0) },
                           // Corners:
                           { ZPt(+x2,+y0,0), ZPt(+x0,+y2,0), ZPt(+x1,+y2,0), ZPt(+x2,+y1,0) },
                           { ZPt(+x2,my0,0), ZPt(+x0,my2,0), ZPt(+x1,my2,0), ZPt(+x2,my1,0) },
                           { ZPt(mx2,+y0,0), ZPt(mx0,+y2,0), ZPt(mx1,+y2,0), ZPt(mx2,+y1,0) },
                           { ZPt(mx2,my0,0), ZPt(mx0,my2,0), ZPt(mx1,my2,0), ZPt(mx2,my1,0) }
                           };

        /* The distance (as a fraction of border) from the square where the 1D texture slice is taken.
         * Smaller number lowers the frequency of the corner pieces
         */
        float texf = 0.5;

        DBG(dbg_irregularquad) << "Dice: " << dicex << " " << dicey << " " << diceb << "\n";

	if (diceb <= 0 || dicex <= 0 || dicey <= 0) {
	    DBG(dbg_irregularquad) << "Skipping IrregularQuad because of invalid geometry\n";
	    return;
	}

        glCallList(setup);
        GLERR;

        for (int pass = 0; pass < ((flags & IRREGU_SHIFTS) ? 4 : 1); pass++) {

        if (flags & IRREGU_SHIFTS) {
            glPushMatrix();
            glTranslatef((pass & 1) ? -2 : 2,
                         (pass & 2) ? -2 : 2,
                         0);
        }
        
        glEnable(GL_REGISTER_COMBINERS_NV);
        glEnable(GL_TEXTURE_2D);

        for (unsigned i = 0; i < sizeof(sides)/sizeof(sides[0]); i++) {
            ZVec tex0 = freq * (coords2.transform(lerp(sides[i][3], sides[i][0], texf)) - ZPt(0,0,0));
            ZVec tex1 = freq * (coords2.transform(lerp(sides[i][2], sides[i][1], texf)) - ZPt(0,0,0));

            float w = wtbl[i];

            glBegin(GL_QUAD_STRIP);

            for (int d = 0; d <= dice[i]; d++) {
                float t = (float)d / dice[i];
                ZVec tex = lerp(tex0, tex1, t);
                ZPt pt0 = lerp(sides[i][0], sides[i][1], t);
                ZPt pt1 = lerp(sides[i][3], sides[i][2], t);

                glSecondaryColor3fEXT(0, 0, 0);
                glTexCoord2f(tex.x, tex.y);
                vert(coords1, coords2, pt0);

                glSecondaryColor3fEXT(1, 1, 1);
                glTexCoord4f(tex.x * w, tex.y * w, 0, w);
                vert(coords1, coords2, pt1);
            }
            glEnd();
            GLERR;

        }

        glDisable(GL_REGISTER_COMBINERS_NV);
        glDisable(GL_TEXTURE_2D);

        ZPt poly[] = { ZPt(+x1,+y2,0), ZPt(+x2,+y1,0),
                       ZPt(mx2,+y1,0), ZPt(mx1,+y2,0),
                       ZPt(mx1,my2,0), ZPt(mx2,my1,0),
                       ZPt(+x2,my1,0), ZPt(+x1,my2,0), ZPt(+x1,+y2,0) };
        int dice[] = { diceb, dicex, diceb, dicey, diceb, dicex, diceb, dicey };

        ZPt poly2[9];
        for (int i = 0; i < 9; i++) poly2[i] = coords2.transform(poly[i]);

        // Disable the effect of TEXTURE1 as the clipping is done explicitly
        glMultiTexCoord2f(1, .5, .5);

        static vector<ZPt> pts;
	pts.clear();
	pts.reserve(int(1.5 * (4*diceb + 2*dicex + 2*dicey)));
        
        ZPt prev = poly2[0];
        for (int i = 0; i < 8; i++) {
            for (int d = 0; d < dice[i]; d++) {
                float t = (float)d / dice[i];
                ZPt v = lerp(poly2[i], poly2[i+1], t);
                vert(pts, prev, v);
                prev = v;
            }
        }

        drawStarPoly(coords1, pts);

        if (flags & IRREGU_SHIFTS) glPopMatrix();

        }

        glPopAttrib();

        DBG(dbg_irregularquad) << "IrregularQuad done\n";
    }
};

VOB_DEFINED(IrregularQuad);

/**
 * coords1: paper => window
 * coords2: frame => paper (assumed to be affine)
 *
 * shape: determines the frame shape; all shapes fit in [-1,1]^2
 * setup: CallGL code that is run before calling Irregu::draw
 * texscale, ..., flags: passed directly to Irregu::draw
 */
class IrregularEdge {
public:
    enum { NTrans = 2 };

    int shape;
    float texscale, linewidth, refsize, scale_pow;
    float border0, border1, texslicing;
    vector<float> const0, const1; int angles, multi, flags;
    DisplayListID setup;
    float dicefactor;

    template<class F> void params(F &f) {
	unicodecharvector const0, const1;
	f(shape, texscale, linewidth, refsize, scale_pow,
	  border0, border1, texslicing,
	  const0, const1, angles, multi, flags,
	  setup, dicefactor);
	this->const0 = getconstvec(const0);
	this->const1 = getconstvec(const1);
    }
	  
    vector<float> getconstvec(unicodecharvector x) {
	vector<float> v;
	if(x.begin() == x.end()) return v;
	string s(x.begin(), x.end());
	const char *p = s.c_str();
	char *e;
	float f;
	while (f = strtod(p, &e), e > p) {
	    v.push_back(f);
	    p = e;
	}
	return v;
    }

    template<class T> void render(const T &coords1, const T &coords2) const {
        DBG(dbg_irregularquad) << "IrregularEdge\n";

        // XXX: TODO: use coords1.nonlinearity()
        int n = (int)(36 * dicefactor);

	DBG(dbg_irregularquad) << "Reserve "<<n<<"\n";
        std::vector<ZPt> vert(n+1);

	Pt box = coords2.getSqSize();
        
        switch (shape) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            n = 2 + shape;
        default:
        case 0: 
	    DBG(dbg_irregularquad) << "GetEllipse "<<n<<"\n";

            vert = Irregu::getEllipse(n, 2, 2);
            break;
        case 7: {
	    DBG(dbg_irregularquad) << "Quad\n";
            n = 4;
            vert[0] = ZPt(+1, -1, 0);
            vert[1] = ZPt(+1, +1, 0);
            vert[2] = ZPt(-1, +1, 0);
            vert[3] = ZPt(-1, -1, 0);
            }
            break;
        case 8:
        case 9:
        case 10:
        case 11: {
            float xw = (coords2.transform(ZPt(1,0,0)) - coords2.transform(ZPt(0,0,0))).length();
            float yw = (coords2.transform(ZPt(0,1,0)) - coords2.transform(ZPt(0,0,0))).length();

            float poww = 2 + (shape - 8) * 3;
	    DBG(dbg_irregularquad) << "GetEllipse type2 "<<n<<" "<<poww<<"\n";
            vert = Irregu::getEllipse(n, poww * pow(box.x * xw/120, 0.2), poww * pow(box.y * yw/120, 0.2));
            }
            break;
        }


	DBG(dbg_irregularquad) << "Resize "<<(n+1)<<"\n";
        vert.resize(n + 1);
        vert[n] = vert[0];
        for (unsigned int i=0; i<vert.size(); i++) {
            vert[i].x = 0.5*(vert[i].x+1) * box.x;
            vert[i].y = 0.5*(vert[i].y+1) * box.y;
        }
	DBG(dbg_irregularquad) << "trans\n";
        Irregu::transform(coords2, vert);
	DBG(dbg_irregularquad) << "transed\n";

        glCallList(setup);
        GLERR;

        // XXX: Kluge for 2D offset texture
        if (flags & 65536) {
	    DBG(dbg_irregularquad) << "offs\n";
            float xw = (coords2.transform(ZPt(1,0,0)) - coords2.transform(ZPt(0,0,0))).length();
            float yw = (coords2.transform(ZPt(0,1,0)) - coords2.transform(ZPt(0,0,0))).length();
            
            float s = .5 * (border1 - border0);
            float sx = s / xw;
            float sy = s / yw;
            ZPt vert[] = {
              ZPt(+1 + sx, -1 - sy, 0),
              ZPt(+1 + sx, +1 + sy, 0),
              ZPt(-1 - sx, +1 + sy, 0),
              ZPt(-1 - sx, -1 - sy, 0)
            };
            GLERR;
            glActiveTexture(GL_TEXTURE1);
            float mat[4] = { .25 * sx, 0, 0, .25 * sy };
            glTexEnvfv(GL_TEXTURE_SHADER_NV, GL_OFFSET_TEXTURE_MATRIX_NV, mat);
            GLERR;
            glActiveTexture(GL_TEXTURE0);

            int passes = 1;
            if (flags & Irregu::SHIFTS) passes = 4;
            if (flags & Irregu::SHIFTS8) passes = 8;

            for (int pass = 0; pass < passes; pass++) {
                if (passes > 1) {
                    glPushMatrix();
                    float t = linewidth * 0.707106781186547;
                    switch (pass) {
                        case 0: glTranslatef(-linewidth, 0, 0); break;
                        case 1: glTranslatef(+linewidth, 0, 0); break;
                        case 2: glTranslatef(0, -linewidth, 0); break;
                        case 3: glTranslatef(0, +linewidth, 0); break;
                        case 4: glTranslatef(-t, -t, 0); break;
                        case 5: glTranslatef(-t, +t, 0); break;
                        case 6: glTranslatef(+t, -t, 0); break;
                        case 7: glTranslatef(+t, +t, 0); break;
                    }
                }
                glBegin(GL_QUADS);
                for (unsigned i = 0; i < 4; i++) {
                    glMultiTexCoord2f(GL_TEXTURE1,
                                      .5 + .25 * vert[i].x,
                                      .5 + .25 * vert[i].y);
                    ZPt tex = coords2.transform(vert[i]);
                    glTexCoord4f(tex.x, tex.y, tex.z, texscale);
                    coords1.vertex(tex);
                }
                glEnd();
                GLERR;
                if (passes > 1) glPopMatrix();
            }
            GLERR;
            
        } else {
	    DBG(dbg_irregularquad) << "norms\n";
            std::vector<ZVec> norm = Irregu::computeNorms(vert, n);
	    DBG(dbg_irregularquad) << "normed - calling draw: "<<const0.size()<<" "
				<< const1.size()<<"\n";
            Irregu::draw(coords1, vert, norm, texscale,
                        linewidth, refsize, scale_pow,
                        border0, border1, texslicing,
                        &const0[0], &const1[0],
                        angles, multi, flags);
	    DBG(dbg_irregularquad) << "drew\n";
                        
            GLERR;
        }

        glPopAttrib();
        GLERR;
        DBG(dbg_irregularquad) << "IrregularEdge done\n";
    }
};

VOB_DEFINED(IrregularEdge);

}
}


#endif
