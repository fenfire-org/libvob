/*
Debug.hxx
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

#ifndef VOB_VOBS_DEBUG
#define VOB_VOBS_DEBUG

#include <GL/gl.h>
#include <vob/glerr.hxx>
#include <vob/Debug.hxx>

#ifndef VOB_DEFINED
#define VOB_DEFINED(t)
#endif

namespace Vob {
namespace Vobs {

    /** Turn on/off the given debug variable.
     * Useful to avoid tons of output - only get detailed
     * info from where you want it.
     */
    struct DebugSwitch {
	enum { NTrans = 0 };
	std::string name;
	int tostate;
	template<class F> void params(F &f) {
	    f(name, tostate);
	}
	void render() const {
	    Debug::var(name.c_str()) = tostate;
	}

    };
    VOB_DEFINED(DebugSwitch);

}
}


#endif
