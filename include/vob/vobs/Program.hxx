/*
Program.hxx
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

#ifndef VOB_VOBS_PROGRAM_HXX
#define VOB_VOBS_PROGRAM_HXX

#ifndef VOB_DEFINED
#define VOB_DEFINED(x)
#endif

#include <vob/Types.hxx>

namespace Vob {
namespace Vobs {

    class ProgramLocalParameterARB {
    public:
	enum { NTrans = 1 };

	Token target;
	int index;
	template<class F> void params(F &f) {
	    f(target, index);
	}
	template<class T> void render(const T &t) const {
	    ZPt p1 = t.transform(ZPt(0,0,0));
	    ZPt p2 = t.transform(ZPt(1,1,0));
	    glProgramLocalParameter4fARB(
		    target, index,
		    p1.x, p1.y, p2.x-p1.x, p2.y-p2.x);
	}
    };

    VOB_DEFINED(ProgramLocalParameterARB);

    class ProgramNamedParameterNV {
    public:
	enum { NTrans = 1 };

	int id;
	std::string name;
	template<class F> void params(F &f) {
	    f(id, name);
	}
	template<class T> void render(const T &t) const {
	    ZPt p1 = t.transform(ZPt(0,0,0));
	    ZPt p2 = t.transform(ZPt(1,1,0));
	    glProgramNamedParameter4fNV(
		    id, 
		    name.length(),
		    (const GLubyte *)name.data(),
		    p1.x, p1.y, p2.x-p1.x, p2.y-p2.x);
	}
    };

    VOB_DEFINED(ProgramNamedParameterNV);


}
}


#endif
