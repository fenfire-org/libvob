/*   
Paper.java
 *    
 *    Copyright (c) 2002-2003, Tuomas Lukka
 *                  2006, Matti J. Katila
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
 * Written by Tuomas Lukka, ported to Java by Matti J. Katila
 */
package org.nongnu.libvob.gl.impl.lwjgl.paper;
import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;
import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.PaperMill;

/** The interface to the paper library.
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
public class Paper implements PaperMill.Paper {
    
    public static boolean dbg = true;
    private static void pa(String s) { System.out.println(s); }

    
    ArrayList passes = new ArrayList();
    

    public void setNPasses(int num) { 
	passes.ensureCapacity(num); 
	while (passes.size() < num) 
	    passes.add(new Pass(passes.size())); 
    }
    
    public Pass getPass(int index) { return (Pass) passes.get(index); }
    
    
    
    
    
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
     */
    public class Pass {

	/** Simple, non-lighting-dependent TexGen.
	 * Transforms paper coordinates into texture coordinates using
	 * the given matrix.
	 */
	class TexGen {
	    Matrix4f tex_mat = new Matrix4f();
	    public void putNormalTexGen(float[] matrix) {
//		this->tex_mat[0] = tex_mat[0];
//		this->tex_mat[1] = tex_mat[1];
//		this->tex_mat[2] = tex_mat[2];
//		this->tex_mat[3] = tex_mat[3];
		tex_mat.m00 = matrix[0];
		tex_mat.m01 = matrix[1];
		tex_mat.m02 = matrix[2];
		tex_mat.m03 = matrix[3];

//		this->tex_mat[4] = tex_mat[4];
//		this->tex_mat[5] = tex_mat[5];
//		this->tex_mat[6] = tex_mat[6];
//		this->tex_mat[7] = tex_mat[7];
		tex_mat.m10 = matrix[4];
		tex_mat.m11 = matrix[5];
		tex_mat.m12 = matrix[6];
		tex_mat.m13 = matrix[7];

//		this->tex_mat[8] = tex_mat[8];
//		this->tex_mat[9] = tex_mat[9];
//		this->tex_mat[10] = tex_mat[10];
//		this->tex_mat[11] = tex_mat[11];
		tex_mat.m20 = matrix[8];
		tex_mat.m21 = matrix[9];
		tex_mat.m22 = matrix[10];
		tex_mat.m23 = matrix[11];

//		this->tex_mat[12] = 0;
//		this->tex_mat[13] = 0;
//		this->tex_mat[14] = 0;
//		this->tex_mat[15] = 1;
		tex_mat.m30 = 0;
		tex_mat.m31 = 0;
		tex_mat.m32 = 0;
		tex_mat.m33 = 1f;
	    }
	    
	}
	
	
	
	private int index;
	private Pass(int index) { this.index = index; }

//	public void setNIndirectTextureBinds(int n) { impl_Pass_setNIndirectTextureBinds(c_id, index, n); }
//
//	public int getNIndirectTextureBinds() { return impl_Pass_getNIndirectTextureBinds(c_id, index); }
//
		
	ArrayList texGens = new ArrayList();
	public int getNTexGens() { return texGens.size(); }
	public void setNTexGens(int n) { 
	    texGens.ensureCapacity(n);
	    while (texGens.size() < n)
		texGens.add(new TexGen());
	}
//
//	public int getNLightSetups() { return impl_Pass_getNLightSetups(c_id, index); }
//	public void setNLightSetups(int n) { impl_Pass_setNLightSetups(c_id, index, n); }
//
	String setupCode = null, tearDownCode = null;
	public void setSetupcode(String code) { 
	    //CallGL.call(code);
	    this.setupCode = code;
	}
	public String getSetupcode() { return setupCode; }
	public void setTeardowncode(String code) { 
	    this.tearDownCode = code;
	}
	public String getTeardowncode() { return tearDownCode; }


	public void putNormalTexGen(int ind, float[] matrix) {
	    ((TexGen)texGens.get(ind)).putNormalTexGen(matrix);
	}
//	public void putEmbossTexGen(int ind, float[] matrix, float eps) {
//	    if(ind < 0 || ind >= getNTexGens())
//		throw new ArrayIndexOutOfBoundsException();
//	    impl_Pass_putEmbossTexGen(c_id, index, ind, matrix, eps);
//	}

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
//	    if(ind < 0 || ind >= getNIndirectTextureBinds())
//		throw new ArrayIndexOutOfBoundsException(ind);
	    addDepend(indirectTexture);
//	    impl_Pass_putIndirectTextureBind(c_id, index, ind,
//			    activeTexture, textureTarget,
//			    indirectTexture.getIndirectTextureId());
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

//    public int getNPasses() { return impl_getNPasses(c_id); }
//    public void setNPasses(int i) { impl_setNPasses(c_id, i); }

//    public Pass getPass(int p) { 
//	if(p < 0 || p >= getNPasses())
//	    throw new ArrayIndexOutOfBoundsException();
//	return new Pass(p); 
//    }

    public Object clone() { 
	Paper p = new Paper(); 
//	impl_clone(c_id, p.c_id); 
	if(depends != null) { // Clone also the depending objects
	    p.depends = new ArrayList();
	    p.depends.addAll(depends);
	}
	return p; 
    }

    public Paper() {
//	c_id = impl_create();
    }

    public int getId() { return c_id; }


} 
