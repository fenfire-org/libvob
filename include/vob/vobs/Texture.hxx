/*
Texture.hxx
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

#ifndef VOB_VOBS_TEXTURE
#define VOB_VOBS_TEXTURE

#include <math.h>

#include <GL/gl.h>
#include <vob/Types.hxx>

#include <vob/Vec23.hxx>
#include <vob/VecGL.hxx>

#include <sstream>
#include <vob/glerr.hxx>


#ifndef VOB_DEFINED
#define VOB_DEFINED(t)
#endif

namespace Vob {
namespace Vobs {

using namespace Vob::VecGL;

/** Copy into the currently bound texture
 * a rectangle from the screen.
 * The x, y, w, h define the rectangle in screen 
 * space and the origin of the coordinate system
 * given gives its *UPPER LEFT* corner.
 */
struct CopyTexSubImage2D {
    enum { NTrans = 1 };
    Token target;
    int level;
    int x, y, w, h;
    template<class F> void params(F &f) {
	f(target, level, x, y, w, h);
    }
    template<class T> void render(const T &t) const {
	ZPt pfrom = t.transform(ZPt(0,0,0));
	glRasterPos3f(pfrom.x, pfrom.y, pfrom.z);
	float ras[4];
	glGetFloatv(GL_CURRENT_RASTER_POSITION, ras);

	glCopyTexSubImage2D(target, level, x, y, 
		    (int)ras[0], (int)ras[1] - h,
		w, h);
	GLERR;
    }

};
VOB_DEFINED(CopyTexSubImage2D);

/** Call CopyTexSubImage from a ByteVector.
 */
struct TexSubImage2D {
    enum { NTrans = 0 };
    Token target;
    int level;
    int x, y, w, h;
    Token format;
    Token type;
    GLubyte *pixels;

    template<class F> void params(F &f) {
	f(target, level, x, y, w, h, format, type, pixels);
    }
    void render() const {
	GLERR;
	glPushAttrib(GL_PIXEL_MODE_BIT);
	glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
	glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

	glTexSubImage2D(target, level, x, y, w, h, format, type, pixels);

	glPopClientAttrib();
	glPopAttrib();
	GLERR;
    }

};
VOB_DEFINED(TexSubImage2D);

}
}

#endif
