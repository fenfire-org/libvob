/*
Pixel.hxx
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

#ifndef VOB_VOBS_PIXEL_HXX
#define VOB_VOBS_PIXEL_HXX


#include <GL/gl.h>
#include <vob/Types.hxx>

#include <vob/Vec23.hxx>
#include <vob/glerr.hxx>


#ifndef VOB_DEFINED
#define VOB_DEFINED(t)
#endif


namespace Vob {
namespace Vobs {

struct DrawPixels {
    enum { NTrans = 1 };

    int w, h;
    Token format;
    Token type;
    GLubyte *bytes;

    template<class F> void params(F &f) {
	f(w, h, format, type, bytes);
    }

    template<class T> void render(const T &t) const {
	glPushAttrib(GL_PIXEL_MODE_BIT);
	glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
	glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	ZPt p = t.transform(ZPt(0,0,0));
	glRasterPos3f(p.x, p.y, p.z);
	glDrawPixels(w, h, format, type, bytes);
	GLERR;
	glPopClientAttrib();
	glPopAttrib();
    }
};
VOB_DEFINED(DrawPixels);

struct ReadPixels {
    enum { NTrans = 1 };

    int w, h;
    Token format;
    Token type;
    GLubyte *bytes;

    template<class F> void params(F &f) {
	f(w, h, format, type, bytes);
    }

    template<class T> void render(const T &t) const {
	glPushAttrib(GL_PIXEL_MODE_BIT);
	glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
	glPixelStorei(GL_PACK_ROW_LENGTH, 0);
	glPixelStorei(GL_PACK_ALIGNMENT, 1);
	ZPt p = t.transform(ZPt(0,0,0));
	glRasterPos3f(p.x, p.y, p.z);
	float ras[4];
	glGetFloatv(GL_CURRENT_RASTER_POSITION, ras);

	glReadPixels((int)ras[0], (int)ras[1]-h, w, h, format, type, bytes);
	GLERR;
	glPopClientAttrib();
	glPopAttrib();
    }
};
VOB_DEFINED(ReadPixels);

struct CopyPixels {
    enum { NTrans = 2 };

    int w, h;
    Token type;

    template<class F> void params(F &f) {
	f(w, h, type);
    }

    template<class T> void render(const T &t1, const T &t2) const {
	ZPt p = t1.transform(ZPt(0,0,0));
	glRasterPos3f(p.x, p.y, p.z);
	float ras[4];
	glGetFloatv(GL_CURRENT_RASTER_POSITION, ras);

	p = t2.transform(ZPt(0,0,0));
	glRasterPos3f(p.x, p.y, p.z);

	glCopyPixels((int)ras[0], (int)ras[1]-h, w, h, type);
	GLERR;
    }
};
VOB_DEFINED(CopyPixels);


}
}

#endif
