/*   
Paper.java
 *    
 *    Copyright (c) 2002-2003, Tuomas Lukka
 *
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
 *
 */
/*
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob.gl;
import java.util.ArrayList;

/** The interface to the paper library.
 * This interface is relatively low-level, just to make it 
 * map as easily as possible to the JNI code.
 * For all array-type things (X), there are the methods setNX, getNX
 * and also some (possibly different, for using derived 
 * classes) putX methods.
 * <p>
 * For example, the paper contains the Passes array, 
 * so there are the methods getNPasses() and setNPasses(int i).
 * The passes are all of the same type so they are automatically
 * added as empties - getPass gives the Pass object to use 
 * to place stuff into the pass.
 */
public class Paper implements GLDeletable, org.nongnu.libvob.gl.PaperMill.Paper {
    public static boolean dbg = true;
    private static void pa(String s) { System.err.println(s); }

    /** The identififier to be passed to C for this object.
     */
    private int c_id;

    /** Put all objects that manage displaylists and textures
     * used by this paper here to ensure they don't get
     * GCed before this.
     */
    private ArrayList depends;

    /** Add an object on whose C++ representation this paper
     * depends. For instance, if one of the setup codes uses
     * a texture id of a given GL.Texture object, then that
     * object should be added here.
     */
    public void addDepend(Object o) { 
	if(depends == null) depends = new ArrayList();
	depends.add(o); 
    }

    /** A class representing a single rendering pass
     * of a paper.
     * A pass contains the two CallGL codes Setupcode and Teardowncode,
     * and the following arrays:
     * IndirectTextureBinds, TexGens, LightSetups.
     *
     */
    public class Pass {
	private int index;
	private Pass(int index) { this.index = index; }

	public void setNIndirectTextureBinds(int n) { impl_Pass_setNIndirectTextureBinds(c_id, index, n); }

	public int getNIndirectTextureBinds() { return impl_Pass_getNIndirectTextureBinds(c_id, index); }

	public int getNTexGens() { return impl_Pass_getNTexGens(c_id, index); }
	public void setNTexGens(int n) { impl_Pass_setNTexGens(c_id, index, n); }

	public int getNLightSetups() { return impl_Pass_getNLightSetups(c_id, index); }
	public void setNLightSetups(int n) { impl_Pass_setNLightSetups(c_id, index, n); }

	public void setSetupcode(String code) { impl_Pass_setSetupcode(c_id, index, code); }
	public String getSetupcode() { return impl_Pass_getSetupcode(c_id, index); }
	public void setTeardowncode(String code) { impl_Pass_setTeardowncode(c_id, index, code); }
	public String getTeardowncode() { return impl_Pass_getTeardowncode(c_id, index); }

	public void putNormalTexGen(int ind, float[] matrix) {
	    if(ind < 0 || ind >= getNTexGens())
		throw new ArrayIndexOutOfBoundsException();
	    impl_Pass_putNormalTexGen(c_id, index, ind, matrix);
	}
	public void putEmbossTexGen(int ind, float[] matrix, float eps) {
	    if(ind < 0 || ind >= getNTexGens())
		throw new ArrayIndexOutOfBoundsException();
	    impl_Pass_putEmbossTexGen(c_id, index, ind, matrix, eps);
	}

	/** Put an instruction to bind an indirect texture
	 * for this pass.
	 * The GL.IndirectTexture object is automatically
	 * added to the depends array.
	 * @param ind The index of the indirect texture bind instruction to put
	 * @param activeTexture The GL token string (without the GL_ prefix)
	 * 			for which texture to call glActiveTextureARB
	 * 			for. E.g. "TEXTURE1_ARB"
	 * @param textureTarget The GL token string (w.o. GL_) for
	 * 			which texture target to bind.
	 * 			E.g. "TEXTURE_3D"
	 * @param indirectTexture The indirect texture to bind.
	 */
	public void putIndirectTextureBind(int ind, 
			String activeTexture, String textureTarget,
			GL.IndirectTexture indirectTexture) {
	    if(ind < 0 || ind >= getNIndirectTextureBinds())
		throw new ArrayIndexOutOfBoundsException(ind);
	    addDepend(indirectTexture);
	    impl_Pass_putIndirectTextureBind(c_id, index, ind,
			    activeTexture, textureTarget,
			    indirectTexture.getIndirectTextureId());
	}

	/** Add an object on whose C++ representation this paper
	 * depends. For instance, if one of the setup codes uses
	 * a texture id of a given GL.Texture object, then that
	 * object should be added here.
	 */
	public void addDepend(Object o) {
	    Paper.this.addDepend(o);
	}
    }

    public int getNPasses() { return impl_getNPasses(c_id); }
    public void setNPasses(int i) { impl_setNPasses(c_id, i); }

    public Pass getPass(int p) { 
	if(p < 0 || p >= getNPasses())
	    throw new ArrayIndexOutOfBoundsException();
	return new Pass(p); 
    }

    public Object clone() { 
	Paper p = new Paper(); 
	impl_clone(c_id, p.c_id); 
	if(depends != null) { // Clone also the depending objects
	    p.depends = new ArrayList();
	    p.depends.addAll(depends);
	}
	return p; 
    }

    public Paper() {
	c_id = impl_create();
    }

    public void finalize() {
	if(c_id != 0) GL.addDeletable(this);
    }
    public void deleteObject() {
	impl_delete(c_id);
	c_id = 0;
    }

    public int getId() { return c_id; }

    static private native int impl_create();
    static private native void impl_delete(int pid);

    static private native void impl_clone(int from_id, int to_id);

    static private native int impl_getNPasses(int pid);
    static private native void impl_setNPasses(int pid, int i);

    static private native int impl_Pass_getNTexGens(int pid, int pass);
    static private native void impl_Pass_setNTexGens(int pid, int pass, int i);

    static private native int impl_Pass_getNLightSetups(int pid, int pass);
    static private native void impl_Pass_setNLightSetups(int pid, int pass, int i);

    static private native void impl_Pass_setSetupcode(int pid, int pass, String code);
    static private native String impl_Pass_getSetupcode(int pid, int pass);
    static private native void impl_Pass_setTeardowncode(int pid, int pass, String code);
    static private native String impl_Pass_getTeardowncode(int pid, int pass);

    static private native void impl_Pass_putNormalTexGen(int pid, int pass, int ind, float[] matrix) ;
    static private native void impl_Pass_putEmbossTexGen(int pid, int pass, int ind, float[] matrix, float eps) ;

    static private native void impl_Pass_setNIndirectTextureBinds(int pid, int pass, int n);
    static private native int impl_Pass_getNIndirectTextureBinds(int pid, int pass);
    static private native void impl_Pass_putIndirectTextureBind(int pid, int pass, int ind, String activeTexture, String textureTarget, int indirectTextureId);
} 
