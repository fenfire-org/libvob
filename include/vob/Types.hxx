/*
Types.hxx
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

#ifndef VOB_TYPES_HXX
#define VOB_TYPES_HXX

#include <vector>

#include <GL/gl.h>

namespace Vob {
    // need separate C++ types...
    
    class GLIndexBase {
    protected:
	int index;
    public:
	explicit GLIndexBase(int index) : index(index) { };
	int get() const { return index; }
	void operator=(int i) { index = i; }
    };

    template<int id> struct GLIndex : public GLIndexBase {
	GLIndex() : GLIndexBase(-1) { }
	explicit GLIndex(int index) : GLIndexBase(index) { }
	void operator=(int i) { index = i; }
	operator int() const {
	    return index;
	}
    };

    typedef GLIndex<GL_LIST_MODE> DisplayListID;
    typedef GLIndex<GL_INVALID_ENUM> Token;

    typedef std::vector<unsigned short> unicodecharvector;
    
}

#endif
