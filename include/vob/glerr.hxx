/*
glerr.hxx
 *    
 *    Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
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
 * Written by Janne Kujala and Tuomas J. Lukka
 */

// Two error macros

#include <GL/glu.h>

namespace Vob {
    extern int vobglErrorVariable;
}

/** Show a GL error, if one has occurred. Requires semicolon,
 * allows more stream output to error stream.
 * Example use:
 * <pre>
 *    glFoo(...);
 *    GLERR << localVariable << " " << localVariable2;
 *    glBar(...);
 *    GLERR;
 * </pre>
 */
#define GLERR if((vobglErrorVariable = glGetError()) != GL_NO_ERROR) \
		    cout << "===== OPENGL ERROR "<<__FILE__<<" "<<__LINE__ \
			<<"  "<<gluErrorString(vobglErrorVariable)<<"\n"


#define STKS { if(dbg) {int sa, sm; glGetIntegerv(GL_ATTRIB_STACK_DEPTH, &sa); \
		glGetIntegerv(GL_MODELVIEW_STACK_DEPTH, &sm); \
		cout << __FILE__<<" "<<__LINE__<<" STACKS: "<<sa<<" "<<sm<<"\n";} }


