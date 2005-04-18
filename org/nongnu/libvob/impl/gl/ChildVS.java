/*
ChildVS.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *                  2004, Matti J. Katila
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
 * Written by Tuomas J. Lukka and Matti J. Katila
 */

package org.nongnu.libvob.impl.gl;
import org.nongnu.libvob.gl.*;

/** The C++-side representation of a child vobscene.
 * Must be outside GL because must be in the same package as GLVobMap etc.
 */
class ChildVS extends GL.NonRenderableJavaObject {
    Object dep;
    private ChildVS(int id, Object dep) {
	super(id);
	this.dep = dep;
    }
    protected void deleteObj() {
	impl_delete(getId());
    }
    public int getChildVSId() { return getId(); }

    static private native int impl_create(
		    int nMapCodes,
		    int[] mapCodes,
		    int nCoorderInds,
		    int[] coorderInds,
		    int nCoorderFloats,
		    float[] coorderFloats);
		    
    static private native void impl_delete(int id);


    /** (NON-PUBLIC API) Create a C++ child vobscene object.
     */
    static ChildVS _createChildVS(GLVobMap glVobMap, GLVobCoorder glVobCoorder) {
	// Some Java compilers get confused by the inheritance,
	// not allowing us to access the GLVobCoorderBase
	// package-visible members through the GLVobCoorder instance.
	// Oh well...
	GLVobCoorderBase glVobCoorderBase = glVobCoorder;
	// Avoid weird cases
	if(glVobMap.curs >= glVobMap.list.length-2)
	    throw new Error("Vobmap too full!");
	ChildVS res = new ChildVS(
	    impl_create(
		glVobMap.curs + 1, // leave space for zero
		glVobMap.list,
		glVobCoorderBase.ninds,
		glVobCoorderBase.inds,
		glVobCoorderBase.nfloats,
		glVobCoorderBase.floats),
	    new Object[] { // Keep the vobmap and coorder for GC avoidance
		glVobMap, glVobCoorder
	    });
	/*
	// Free the extraneous lists
	glVobMap.list = null;
	// XXX free vobs, realloc to right length
	glVobCoorderBase.inds = null;
	glVobCoorderBase.floats = null;
	*/
	return res;
    }
}
