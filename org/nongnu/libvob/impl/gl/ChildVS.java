// (c) Tuomas J. Lukka

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
