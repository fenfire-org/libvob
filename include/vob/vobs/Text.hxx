/*
Text.hxx
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

#ifndef VOB_VOBS_TEXT_HXX
#define VOB_VOBS_TEXT_HXX

#ifndef VOB_DEFINED
#define VOB_DEFINED(x)
#endif

#include <vob/glerr.hxx>
#include <vob/text/QuadFont.hxx>
#include <vob/Types.hxx>
#include <vob/VecGL.hxx>

namespace Vob {
namespace Vobs {
    using namespace VecGL;

    PREDBGVAR(dbg_text);

template<class str> struct Text1Base {
    enum { NTrans = 1 };

    
    Text::QuadFont *font;
    str text;
    float yoffs;
    int flags;

    template<class F> void params(F &f) {
	f(font, text, yoffs, flags);
    }

    template<class T> void render(const T &t) const {
	if (dbg_text) {
	    DBG(dbg_text) << "HorizText:\n";
	    for (typename str::const_iterator it = text.begin(); 
		    it != text.end(); ++it) {
		unsigned glyph = *it;
		DBG(dbg_text) << "'" << (char)*it << "' (" << 
			(int)*it << "): "<<
		    font->coordinates[8*glyph + 0 + 0] <<" "<<
		    font->coordinates[8*glyph + 0 + 1] << " "<<
		    font->coordinates[8*glyph + 0 + 2] <<" "<<
		    font->coordinates[8*glyph + 0 + 3] << " "<<
		    font->coordinates[8*glyph + 0 + 4] <<" "<<
		    font->coordinates[8*glyph + 0 + 5] << " "<<
		    font->coordinates[8*glyph + 0 + 6] <<" "<<
		    font->coordinates[8*glyph + 0 + 7] << " "<<
		    font->advances[glyph]<<
		    t.transform(ZPt(
			font->coordinates[8*glyph + 0 + 0],
			font->coordinates[8*glyph + 0 + 1],
			0)) <<
		    t.transform(ZPt(
			font->coordinates[8*glyph + 0 + 2],
			font->coordinates[8*glyph + 0 + 3],
			0)) <<
		    "\n";
	    }

	    for(unsigned i=0; i<font->textureUnits.size(); i++) {
		DBG(dbg_text) << i<<": "<<
			font->textureUnits[i]<<" "<<
			font->textureTargets[i]<<"\n";
	    }
	}

	int curTexInd = -1;
	int minGlyph = 0; int nGlyphs = font->textureIndex.size();
	GLERR;

	float x = 0;
	float y = yoffs;
	glBegin(GL_QUADS);
	for (typename str::const_iterator it = text.begin(); 
		it != text.end(); ++it) {
	    int glyph = *it;
	    if(glyph < minGlyph || glyph >= nGlyphs)
		continue;
	    int texInd = font->textureIndex[glyph];
	    if(texInd < 0) continue;
	    if(curTexInd != texInd) {
		glEnd();
		GLERR;
		font->bindTextures(texInd);
		GLERR;
		glBegin(GL_QUADS);
		curTexInd = texInd;
	    }

#define do_corner(a, b) \
	    font->texCoords( \
		    font->coordinates[8*glyph + 4 + a], \
		    font->coordinates[8*glyph + 4 + b]); \
	    t.vertex(ZPt(  \
		    x + font->coordinates[8*glyph + 0 + a], \
		    y + font->coordinates[8*glyph + 0 + b], 0)); 

	    do_corner(0,1);
	    do_corner(2,1);
	    do_corner(2,3);
	    do_corner(0,3);

#undef do_corner

	    x += font->advances[glyph];


	}
	glEnd();
	font->unbindTextures();
	GLERR;

	DBG(dbg_text) << "End text render\n";

    }

};

typedef Text1Base<unicodecharvector> Text1;

VOB_DEFINED(Text1);

/** In a triangle with the given vertices, find 
 * the change of t at unit change of v in x or y direction.
 */
std::pair<Pt,Pt> texDerivs(Pt v00, Pt v01, Pt v10, Pt t00, Pt t01, Pt t10) {
    // Now, find out the correct offsets to the texture
    // coordinates. 
    Pt va = v10-v00; Pt ta = t10-t00;
    Pt vb = v01-v00; Pt tb = t01-t00;

    if(fabs(vb.x) > fabs(va.x)) {
	std::swap(va,vb);
	std::swap(ta,tb);
    }
    // va now greater in x dir
    if(fabs(va.x) < 0.000001) va.x = 1; // avoid inf
    float mul = - vb.x / va.x;
    // This should now have zero x coordinate
    Pt vy = vb + mul*va;
    if(fabs(vy.y) < 0.000001) vy.y = 1; // avoid inf
    Pt ty = (tb + mul*ta);
    
    // Zero y coordinate
    Pt vx = va - (va.y / vy.y)*vy;
    Pt tx = ta - (va.y / vy.y)*ty;

    DBG(dbg_text) << "texDerivs calc: "
	<<va<<vb<<ta<<tb<<mul<<"\n";
    DBG(dbg_text) << "texDerivs vxy: "
	<<vx<<vy<<tx<<ty<<"\n";

    
    return std::pair<Pt,Pt>(ty * (1/vy.y),tx * (1/vx.x));
}


/** An implemenentation of text rendering
 * using 4 texunits for supersampling.
 * This vob only sets up the texture coordinates.
 */
template<class str> struct TextSuper4Base {
    enum { NTrans = 1 };

    
    Text::QuadFont *font;
    str text;
    float yoffs;
    int flags;

    template<class F> void params(F &f) {
	f(font, text, yoffs, flags);
    }

    template<class T> void render(const T &t) const {
	int curTexInd = -1;
	int minGlyph = 0; int nGlyphs = font->textureIndex.size();
	GLERR;

	float x = 0;
	float y = yoffs;
	glBegin(GL_QUADS);
	for (typename str::const_iterator it = text.begin(); 
		it != text.end(); ++it) {
	    int glyph = *it;
	    if(glyph < minGlyph || glyph >= nGlyphs)
		continue;
	    int texInd = font->textureIndex[glyph];
	    if(texInd < 0) continue;
	    if(curTexInd != texInd) {
		glEnd();
		GLERR;
		font->bindTextures(texInd);
		GLERR;
		glBegin(GL_QUADS);
		curTexInd = texInd;
	    }
	    // Won't use font->texCoords since
	    // it depends on the transformation

#define do_corner(ver, tex, a, b) \
	    Pt tex = Pt( \
		    font->coordinates[8*glyph + 4 + a], \
		    font->coordinates[8*glyph + 4 + b]); \
	    ZPt ver = t.transform(ZPt(  \
		    x + font->coordinates[8*glyph + 0 + a], \
		    y + font->coordinates[8*glyph + 0 + b], 0)); 

	    do_corner(v00, t00, 0,1);
	    do_corner(v10, t10, 2,1);
	    do_corner(v11, t11, 2,3);
	    do_corner(v01, t01, 0,3);
#undef do_corner

	    std::pair<Pt,Pt> deriv0 = texDerivs(v00,v01,v10, t00,t01,t10);

#define do_vertex(ver, tex) \
 glMultiTexCoord(GL_TEXTURE0, tex - .25 * deriv0.first - .25*deriv0.second); \
 glMultiTexCoord(GL_TEXTURE1, tex + .25 * deriv0.first - .25*deriv0.second); \
 glMultiTexCoord(GL_TEXTURE2, tex - .25 * deriv0.first + .25*deriv0.second); \
 glMultiTexCoord(GL_TEXTURE3, tex + .25 * deriv0.first + .25*deriv0.second); \
 glVertex(ver);

	    do_vertex(v00, t00);
	    do_vertex(v10, t10);
	    do_vertex(v11, t11);
	    do_vertex(v01, t01);

	    DBG(dbg_text)<<"Coords: "<<v00<<v10<<v11<<v01<<"\n";
	    DBG(dbg_text)<<"Tex: "<<t00<<t10<<t11<<t01<<"\n";
	    DBG(dbg_text)<<"derivs: "<<deriv0.first<<deriv0.second<<"\n";

	    // Assume the transformation is the same all through the quad.
	    // pair<Pt,Pt> deriv1 = texDerivs(v11,v01,v10, t11,t01,t10);

	    x += font->advances[glyph];


	}
	glEnd();
	font->unbindTextures();
	GLERR;

	DBG(dbg_text) << "End text render\n";

    }

};

typedef TextSuper4Base<unicodecharvector> TextSuper4;

VOB_DEFINED(TextSuper4);


}
}

#endif
