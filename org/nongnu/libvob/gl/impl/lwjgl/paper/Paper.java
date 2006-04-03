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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentProgram;
import org.lwjgl.opengl.ARBProgram;
import org.lwjgl.opengl.ARBVertexProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.NVFloatBuffer;
import org.lwjgl.opengl.NVVertexProgram;
import org.lwjgl.opengl.NVVertexProgram2Option;
import org.lwjgl.opengl.NVVertexProgram3;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.PaperMill;

/** A class for rendering parts
 * of infinite planes with affine mappings of texture coordinates.
 */
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

    
    
    /** Specifies texture coordinate system and 
     * the parameters of the diffuse light for
     * lighting the backgrounds using bump mapping.
     */
    static public class LightParam {
      /** Paper coordinate system basis vectors and origin in object 
       * coordinates.
       * Paper coordinates x, y, (and z) are mapped to 
       * object position (orig + x e0 + y e1 + z e2).
       * For the eye-linear TexGens, the basis and origin vectors 
       * are transformed
       * to eye coordinates using the modelview matrix in effect 
       * at the time the 
       * TexGen::setUp method is called. 
       * Thus, after setup, the user can switch to a different coordinate
       * system for drawing the paper vertices.
       * e0, e1, and e2 should be orthogonal and the same length. */
	public Vector3f e0, e1, e2, orig;

      /** Light position in object coordinates.
       * Light position should be given in the same coordinate system as 
       * the basis vectors above. */
	public Vector3f Light;

      /** Light position w component. 
       * Basically, Light_w should be 1.0 for finite light and
       * 0.0 for infinite light */
	public float Light_w;

      /** Light color */
	public float color[];
    };

   

    
    
    
    
    
    
    /** A class representing a single rendering pass
     * of a paper.
     * A pass contains the two CallGL codes Setupcode and Teardowncode,
     * and the following arrays:
     * IndirectTextureBinds, TexGens, LightSetups.
     */
    /** A single rendering pass.
     * The data members are public to allow modification,
     * but when using a ready-made PaperPass, _choose one_
     * (and only one) of the following ways to use it:
     *
     *   1) using texgen
     *      - call setUp_texgen() with LightParam to use
     *      - call glVertex*() directly from the context
     *      - teardown_texgen()
     *
     *   2) using a vertex program
     *      - call setUp_VP with LightParam to use
     *      - call glTexCoord*() and glVertex*() directly from 
     *        the context _or_ call vertex_VP() with position
     *        within the paper in array of 4 floats
     *      - call teardown_VP()
     *
     *   3) using explicit coordinates
     *     - call setUp_explicit with LightParam to use
     *     - call texcoords_explicit() with paper coordinates in
     *       arrays of 4 floats and call glVertex*() directly
     *       from the context.
     *     - call teardown_explicit()
     */
    public class Pass {

	
	/** (internal): an abstract base class
	 * for objects used to set up 
	 * the texture blending using the light parameters.
	 */
	abstract class LightSetup {
	    public abstract void setUp(LightParam param);
	};


	
	/** Simple, non-lighting-dependent TexGen.
	 * Transforms paper coordinates into texture coordinates using
	 * the given matrix.
	 */
	class TexGen {

	    /** The matrix that maps v[TEX0] into o[TEXi] is stored 
	     * starting at c[tex_addr_base + 4*i].
	     */
	    static protected final int textAddressBase = 60;

	    protected Matrix4f tex_mat = new Matrix4f();

	    /** Pointer to current translation matrix used in 
	     * texcoords_explicit(). 
	     */
	    Matrix4f explicit_mat = new Matrix4f();
	    
	    
	    /** Creates a new TexGen.
	     * @param tex_mat A float vector of 12 elements, 
	     * components of s, t and r vectors, respectively.
	     */
	    TexGen(float [] matrix)
	    {
		tex_mat.m00 = matrix[0];
		tex_mat.m10 = matrix[1];
		tex_mat.m20 = matrix[2];
		tex_mat.m30 = matrix[3];

		tex_mat.m01 = matrix[4];
		tex_mat.m11 = matrix[5];
		tex_mat.m21 = matrix[6];
		tex_mat.m31 = matrix[7];

		tex_mat.m02 = matrix[8];
		tex_mat.m12 = matrix[9];
		tex_mat.m22 = matrix[10];
		tex_mat.m32 = matrix[11];

		tex_mat.m03 = 0;
		tex_mat.m13 = 0;
		tex_mat.m23 = 0;
		tex_mat.m33 = 1f;
	    }
	    
	    
	    /** Matrix used to transform paper position into texture coordinates. */
	    // XXX: Currently the last four values are always initialized to 0,0,0,1
	    public String getVPCode(int unit) {
		StringBuffer b = new StringBuffer();
		int base = unit * 4 + textAddressBase;

		b.append("DP4 o[TEX");
		b.append(unit);
		b.append("].x, c[");
		b.append(base + 0);
		b.append("], v[TEX0];\n"); 

		b.append("DP4 o[TEX");
		b.append(unit);
		b.append("].y, c[");
		b.append(base + 1);
		b.append("], v[TEX0];\n"); 
		
		b.append("DP4 o[TEX");
		b.append(unit);
		b.append("].z, c[");
		b.append(base + 2);
		b.append("], v[TEX0];\n"); 
		
		b.append("DP4 o[TEX");
		b.append(unit);
		b.append("].w, c[");
		b.append(base + 3);
		b.append("], v[TEX0];\n"); 
		return b.toString();
	    }
	    
	    public void setUp_VP(int unit, LightParam param) {
		// XXX: This could also be implemented as CallGL code
		int base = unit * 4 + textAddressBase;
		NVVertexProgram.glProgramParameter4fNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 0, tex_mat.m00, tex_mat.m10, tex_mat.m20, tex_mat.m30);
		NVVertexProgram.glProgramParameter4fNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 1, tex_mat.m01, tex_mat.m11, tex_mat.m21, tex_mat.m31);
		NVVertexProgram.glProgramParameter4fNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 2, tex_mat.m02, tex_mat.m12, tex_mat.m22, tex_mat.m32);
		NVVertexProgram.glProgramParameter4fNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 3, tex_mat.m03, tex_mat.m13, tex_mat.m23, tex_mat.m33);
	    }
	    
	    public void setUp_explicit(LightParam param) {
		explicit_mat.load(tex_mat);
	    }
	    
	    public void setUp_texgen(LightParam param) {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		
		float[] _mat = { param.e0.x, param.e0.y, param.e0.z, 0,
				  param.e1.x, param.e1.y, param.e1.z, 0,
				  param.e2.x, param.e2.y, param.e2.z, 0,
				  param.orig.x, param.orig.y, param.orig.z, 1 };
		FloatBuffer mat = BufferUtils.createFloatBuffer(16);
		mat.put(_mat);
		mat.flip();
		GL11.glMultMatrix(mat);

		FloatBuffer m0 = BufferUtils.createFloatBuffer(4);
		m0.put(tex_mat.m00);
		m0.put(tex_mat.m10);
		m0.put(tex_mat.m20);
		m0.put(tex_mat.m30);
		m0.flip();
		
		FloatBuffer m1 = BufferUtils.createFloatBuffer(4);
		m1.put(tex_mat.m01);
		m1.put(tex_mat.m11);
		m1.put(tex_mat.m21);
		m1.put(tex_mat.m31);
		m1.flip();
		
		FloatBuffer m2 = BufferUtils.createFloatBuffer(4);
		m2.put(tex_mat.m02);
		m2.put(tex_mat.m12);
		m2.put(tex_mat.m22);
		m2.put(tex_mat.m32);
		m2.flip();
		

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTexGenf(GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
		GL11.glTexGen(GL11.GL_S, GL11.GL_EYE_PLANE, m0);
		GL11.glTexGenf(GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
		GL11.glTexGen(GL11.GL_T, GL11.GL_EYE_PLANE, m1);
		GL11.glTexGenf(GL11.GL_R, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
		GL11.glTexGen(GL11.GL_R, GL11.GL_EYE_PLANE, m2);
		GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
		GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
		GL11.glEnable(GL11.GL_TEXTURE_GEN_R);
		GL11.glPopMatrix();
	    }
	}

	/** TexGen for embossing.
	 * Shifts the texture towards the light by an amount proportional to eps.
	 * Take the difference of the filtered values generated by two emboss 
	 * texgens using the same texture with +eps and -eps parameters
	 * to obtain an approximation of the diffuse dot product.
	 */
	class TexGenEmboss extends TexGen {
	    
	    /** Amount to shift the texture towards the light. */
	    protected float eps;
	    
	    /**  When embossing (depends of LightParam), texcoords_explicit() needs 
	     * a different matrix to transform paper position into embossed 
	     * texture coordinates.
	     */
	    //float explicit_tmp_mat[16];

	    
	    /** Creates a new TexGen for embossing.
	     * @param tex_mat A float vector of 12 elements, 
	     * components of s, t and r vectors, respectively.
	     * @param eps mount to shift the texture towrds the light
	     */
	    TexGenEmboss(float[] matrix, float eps) {
		super(matrix);
		this.eps = eps;
	    }
	    
	    public void setUp_VP(int unit, LightParam param) {
		/** Suppose 
		 *   x = vertex position
		 *   p = paper coordinates
		 *   s = texture coordinates
		 *   A = paper-to-vertex mapping
		 *   E = embossing mapping (translates x towards light)
		 *   M = modelview matrix
		 *   T = texture matrix
		 *
		 * The usual eye-linear embossing texgen computes
		 *   s' = Tp' = T((MEA)^-1 M x) = T(A^-1 E^-1 x)
		 * The usual simple texgen computes
		 *   s = Tp = T((MA)^-1 M x) = T(A^-1 x)  ==>  x = Ap
		 * Thus, the mapping from p to s' is
		 *   s' = Tp' = T(A^-1 E^-1 x) = T A^-1 E^-1 A p
		 * 
		 * The following code computes T A^-1 E^-1 A and stores it
		 * in place of T in the constant registers.
		 */

		int base = unit * 4 + textAddressBase;

		GL11.glMatrixMode(NVVertexProgram.GL_MATRIX0_NV);
		GL11.glLoadIdentity();

		Vector3f tmp = new Vector3f(param.orig.x, param.orig.y, param.orig.z);
		tmp.scale(param.Light_w);
		
		float eps = this.eps * Vector3f.dot(param.e2, param.e2) / Vector3f.dot(Vector3f.sub(param.Light, tmp, tmp), param.e2);
			
		GL11.glTranslatef(eps * param.Light.x, eps * param.Light.y, eps * param.Light.z);
		float s = 1 - param.Light_w * eps;
		GL11.glScalef(s, s, s);
			
		float[] _mat = { param.e0.x, param.e0.y, param.e0.z, 0,
			  param.e1.x, param.e1.y, param.e1.z, 0,
			  param.e2.x, param.e2.y, param.e2.z, 0,
			  param.orig.x, param.orig.y, param.orig.z, 1 };
		FloatBuffer mat = BufferUtils.createFloatBuffer(16);
		mat.put(_mat);
		mat.flip();
		GL11.glMultMatrix(mat);

		// Hack for easily inverting the matrix
		NVVertexProgram.glTrackMatrixNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base, NVVertexProgram.GL_MATRIX0_NV, NVVertexProgram.GL_INVERSE_NV);
		NVVertexProgram.glTrackMatrixNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base, GL11.GL_NONE, NVVertexProgram.GL_INVERSE_NV);
		FloatBuffer foo = BufferUtils.createFloatBuffer(16);
		NVVertexProgram.glGetProgramParameterNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 0, NVVertexProgram.GL_PROGRAM_PARAMETER_NV, foo);
		NVVertexProgram.glGetProgramParameterNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 1, 
			NVVertexProgram.GL_PROGRAM_PARAMETER_NV, foo);
		NVVertexProgram.glGetProgramParameterNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 2, 
			NVVertexProgram.GL_PROGRAM_PARAMETER_NV, foo);
		NVVertexProgram.glGetProgramParameterNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base + 3, 
			NVVertexProgram.GL_PROGRAM_PARAMETER_NV, foo);
		
		FloatBuffer tex_mat_ = BufferUtils.createFloatBuffer(16);
		tex_mat.store(tex_mat_);
		tex_mat_.flip();
		GL11.glLoadMatrix(tex_mat_);

		GL11.glMultMatrix(foo);

		GL11.glMultMatrix(mat);


		// Load the current matrix in c[base:base+4]
		NVVertexProgram.glTrackMatrixNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base, NVVertexProgram.GL_MATRIX0_NV, NVVertexProgram.GL_IDENTITY_NV);
		NVVertexProgram.glTrackMatrixNV(NVVertexProgram.GL_VERTEX_PROGRAM_NV, base, GL11.GL_NONE, NVVertexProgram.GL_IDENTITY_NV);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    }

	    public void setUp_texgen(LightParam param) {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();

		Vector3f tmp = new Vector3f(param.orig.x, param.orig.y, param.orig.z);
		tmp.scale(param.Light_w);
		
		float eps = this.eps * Vector3f.dot(param.e2, param.e2) / Vector3f.dot(Vector3f.sub(param.Light, tmp, tmp), param.e2);
			
		GL11.glTranslatef(eps * param.Light.x, eps * param.Light.y, eps * param.Light.z);
		float s = 1 - param.Light_w * eps;
		GL11.glScalef(s, s, s);
			
		float[] _mat = { param.e0.x, param.e0.y, param.e0.z, 0,
			  param.e1.x, param.e1.y, param.e1.z, 0,
			  param.e2.x, param.e2.y, param.e2.z, 0,
			  param.orig.x, param.orig.y, param.orig.z, 1 };
		FloatBuffer mat = BufferUtils.createFloatBuffer(16);
		mat.put(_mat);
		mat.flip();
		GL11.glMultMatrix(mat);

		FloatBuffer m0 = BufferUtils.createFloatBuffer(4);
		m0.put(tex_mat.m00);
		m0.put(tex_mat.m10);
		m0.put(tex_mat.m20);
		m0.put(tex_mat.m30);
		m0.flip();
		
		FloatBuffer m1 = BufferUtils.createFloatBuffer(4);
		m1.put(tex_mat.m01);
		m1.put(tex_mat.m11);
		m1.put(tex_mat.m21);
		m1.put(tex_mat.m31);
		m1.flip();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTexGenf(GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
		GL11.glTexGen(GL11.GL_S, GL11.GL_EYE_PLANE, m0);
		GL11.glTexGenf(GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
		GL11.glTexGen(GL11.GL_T, GL11.GL_EYE_PLANE, m1);
		GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
		GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
		GL11.glPopMatrix();
	    }
	    public void setUp_explicit(LightParam param) {
		System.out.println("WARNING: no setup explicit implemented.");
	    }
	}
	
	
	protected ARB_VertexProgram texgenvp;
	
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
		texGens.add(null);
	}

	ArrayList setupCodes = new ArrayList();
	public int getNLightSetup() { return setupCodes.size(); }
	public void setNLightSetup(int n) { 
	    setupCodes.ensureCapacity(n);
	    while (setupCodes.size() < n)
		setupCodes.add(new LightSetup(){
		    public void setUp(LightParam param) {
			// TODO Auto-generated method stub
		    }
		});
	}
	
	String setupCode = null, tearDownCode = null;
	CallGL setupCodeGl = null, tearDownCodeGl = null;
	public void setSetupcode(String code) { 
	    //p("setup: "+code);
	    this.setupCode = code;
	    this.setupCodeGl = new CallGL(code);
	}
	public String getSetupcode() { return setupCode; }
	public void setTeardowncode(String code) { 
	    //p("teardown: "+code);
	    this.tearDownCode = code;
	    this.tearDownCodeGl = new CallGL(code);
	}
	public String getTeardowncode() { return tearDownCode; }

	/** Explicit version of the PaperPass texcoords.
	 */
	public void texcoords_explicit(float []ppos) { }

	public void tearDown_explicit() { 
	    throw new Error("un impl.");
	}

	public void putNormalTexGen(int ind, float[] matrix) {
	    texGens.set(ind, new TexGen(matrix));
	}
	public void putEmbossTexGen(int ind, float[] matrix, float eps) {
	    texGens.set(ind, new TexGenEmboss(matrix, eps));
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
	    System.out.println("put indirect text: "+ind+", "+activeTexture+", "+textureTarget+", "+indirectTexture);
	    
//	    if(ind < 0 || ind >= getNIndirectTextureBinds())
//		throw new ArrayIndexOutOfBoundsException(ind);
	    addDepend(indirectTexture);
//	    impl_Pass_putIndirectTextureBind(c_id, index, ind,
//			    activeTexture, textureTarget,
//			    indirectTexture.getIndirectTextureId());
	}


	
	
	/** Generates and loads the texgen vertex program.
	 * This is automatically called on first setUp_VP 
	 * unless it's  already loaded.
	 */
	private void loadVP() {
	    String code = 
		      "!!VP1.1 OPTION NV_position_invariant;\n" +
		      "MOV o[COL0], v[COL0];\n" +
		      "MOV o[COL1], v[COL1];\n";
		    
	    int unit = 0;
	    for (int i =0; i < texGens.size(); i++) {
		if (texGens.get(i) != null) 
		    code += ((TexGen)texGens.get(i)).getVPCode(unit);
		else 
		    p("Warning: ignoring null TexGen");
		unit++;
	    }
		    
	    code += "END\n";
		
	    //p("Creating VPCode with the source: "+code);
		    //std::cerr << "Creating VPCode with the source " << code << "\n";


	    texgenvp = new ARB_VertexProgram(code);
	}

	/** Call setupcode and indirect texture binds.
	 */
	protected void independentSetup() {
	    setupCodeGl.call();
	}
	
	/** Call teardown and indirect texture binds.
	 */
	protected void independentTeardown() {
	    tearDownCodeGl.call();
	}

	
	
	
	private void p(String s) {
	    System.out.println("PaperPass:: "+s);
	}
	public void setUp_VP(LightParam param) {

	    if (texgenvp == null) loadVP();

	    independentSetup();
	    
	    /* Set up VP TexGen parameters for each texture unit */
	    int unit = 0;
	    for (int it = 0; it < texGens.size(); ++it) {
		if (texGens.get(it) != null) ((TexGen)texGens.get(it)).setUp_VP(unit, param);
		else p("Warning: ignoring null TexGen");
		unit++;
	    }
	    
	    /* Do general parametric setup */
	    for (int it = 0; it < setupCodes.size(); ++it) {
	      if (setupCodes.get(it) != null) ((LightSetup)setupCodes.get(0)).setUp(param);
	      else p("Warning: ignoring null LightSetup");
	    }
	    
	    texgenvp.bind(); // Bind vertex program

	    GL11.glEnable(NVVertexProgram.GL_VERTEX_PROGRAM_NV);

	}
	public void tearDown_VP() {
	    independentTeardown();
	    GL11.glDisable(NVVertexProgram.GL_VERTEX_PROGRAM_NV);
	}
	
	
	public void setUp_explicit(LightParam lightParam) {
	    throw new Error("un impl.");
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

    public int getId() { return 0; }

    public int getPassCount() {
	return passes.size();
    }

    
    /** A simple automatic NVIDIA program id object.
     * Allocates a new program name when created
     * and deletes the name when destroyed.
     * No assignment or copying is allowed; 
     * use shared_ptr<NVProgID> for proper value semantics.
     */
    static public class ProgID {
	public final IntBuffer id = BufferUtils.createIntBuffer(1);
	public ProgID() {
	    ARBProgram.glGenProgramsARB(id);
	}
	protected void finalize() throws Throwable {
	    super.finalize();
	    ARBProgram.glDeleteProgramsARB(id);
	}
    };

    
    
    /** An instance of a GL program (e.g. vertex or fragment program)
     * loaded into the driver.
     * The Program objects are immutable with value semantics.
     * Example:
     * 		NVProg<GL_VERTEX_PROGRAM_NV> code("!!VP1.0 MOV o[HPOS],v[OPOS];END");
     */
    //template <GLuint TARGET>
    static public class Program {
	protected ProgID progId = new ProgID();
	protected String source;
	protected ByteBuffer codeBuffer;
	protected int TYPE;
	protected Program(int TYPE, String source) {
	    this.TYPE = TYPE;
	    byte[] codeBytes = source.getBytes();
	    codeBuffer = BufferUtils.createByteBuffer(codeBytes.length);
	    codeBuffer.put(codeBytes);
	    codeBuffer.flip();
	    compile();
	}
	public String getSource() { return source; }

	public IntBuffer getProgId() { return progId.id; }
	
	public void bind() {
	    NVVertexProgram.glBindProgramNV(TYPE, getProgId().get(0));
	}

	public void operator(FloatBuffer params) {
	    NVVertexProgram.glExecuteProgramNV(TYPE, getProgId().get(0), params);
	}
	
	protected void compile() {
	    NVVertexProgram.glLoadProgramNV(TYPE, getProgId().get(0), codeBuffer);
	}
    };

    static public class ARB_VertexProgram extends Program {
	public ARB_VertexProgram(String src)
	{
	    super(ARBVertexProgram.GL_VERTEX_PROGRAM_ARB, src);
	}
    }
    static public class NV_VertexProgram extends Program {
	public NV_VertexProgram(String src)
	{
	    super(NVVertexProgram.GL_VERTEX_PROGRAM_NV,src);
	}
    }
    static public class NV_VertexStateProgram extends Program {
	public NV_VertexStateProgram(String src)
	{
	    super(NVVertexProgram.GL_VERTEX_STATE_PROGRAM_NV,src);
	}
    }
    static public class FragmentProgram extends Program {
	public FragmentProgram(String src)
	{
	    super(ARBFragmentProgram.GL_FRAGMENT_PROGRAM_ARB,src);
	}
    }
    
} 
