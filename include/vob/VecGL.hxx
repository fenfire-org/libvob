/*
VecGL.hxx
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#ifndef __VECGL_HXX
#define __VECGL_HXX

#include <GL/gl.h>
#include <vob/Vec23.hxx>


namespace Vob {

/** Useful overloads for OpenGL routines using Vec23.
 */
namespace VecGL {

    /** Call glVertex using a Vec23 ZVec.
     */
    inline void glVertex(const ZVec &v) { 
	glVertex3f(v.x, v.y, v.z); 
    }

    /** Call glNormal using a Vec23 ZVec.
     */
    inline void glNormal(const ZVec &v) { 
	glNormal3f(v.x, v.y, v.z); 
    }

    /** Call glTexCoord using a Vec23 ZVec.
     */
    inline void glTexCoord(const ZVec &v) { 
	glTexCoord3f(v.x, v.y, v.z); 
    }

    inline void glMultiTexCoord(GLenum tex, const ZVec &v) {
	glMultiTexCoord3f(tex, v.x, v.y, v.z);
    }
    inline void glMultiTexCoord(GLenum tex, const Vec &v) {
	glMultiTexCoord2f(tex, v.x, v.y);
    }

}

}



#endif
