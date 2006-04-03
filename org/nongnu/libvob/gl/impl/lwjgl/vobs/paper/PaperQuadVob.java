package org.nongnu.libvob.gl.impl.lwjgl.vobs.paper;

import java.awt.Graphics;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTCompiledVertexArray;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.gl.PaperMill;
import org.nongnu.libvob.gl.Ren;
import org.nongnu.libvob.gl.Ren.PaperQuad;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen.Vob2;
import org.nongnu.libvob.gl.impl.lwjgl.paper.Paper;
import org.nongnu.libvob.gl.impl.lwjgl.paper.Paper.Pass;

    /**
     * PaperQuad is a bit complicated: there are three coordinate
     * systems here: the window cs, the object cs and the paper cs.
     * cs1 is object => window,
     * and cs2 is paper => object, unless PAPERQUAD_CS2_TO_SCREEN is set, when it is
     *  paper => window
     * Corners give the corners of the quad to render, in object
     * coordinates.
     */
    public class PaperQuadVob extends AbstractVob implements PaperQuad, Vob2 {

	public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
	}
	public int putGL(VobScene vs, int cs1, int cs2) {
	    return 1;
	}
	static public boolean dbg_paperquad = true;
	static private void p(String s) { System.out.println("PaperQuad:: "+s); }
	
	Paper paper;
	float x0,y0,x1,y1;
	float scale, dicefactor;
	int flags;
	
	float []vertices;
	int []indices;
	int dice;
	
	public PaperQuadVob(PaperMill.Paper paper, float x0, float y0, float x1, float y1, float scale, float dicefactor) {
	    this(paper, x0,y0,x1,y1,scale, dicefactor, GL.hasExtension("GL_NV_vertex_program1_1")? Ren.PAPERQUAD_USE_VERTEX_PROGRAM: 0);
	}
	public PaperQuadVob(PaperMill.Paper paper, float x0, float y0, float x1, float y1, float scale, float dicefactor, int flags) {
	    this.paper = (Paper)paper;
	    this.x0 = x0;
	    this.y0 = y0;
	    this.x1 = x1;
	    this.y1 = y1;
	    this.scale = scale;
	    this.dicefactor = dicefactor;
	    this.flags = flags;
	}

	public void render(Transform coords1, Transform coords2, int callList) {

	    final Transform coords2inv = coords2.getInverse();

	    CallGL.checkGlError("start PaperQuad render");

	    Vector3f paperorigin, paperx, papery;
	    if((flags & Ren.PAPERQUAD_CS2_TO_SCREEN) != 0) {
		Transform coords1inv = coords1.getInverse();
		paperorigin = coords1inv.transform(coords2.transform(new Vector3f(0, 0, 0)));
		paperx = coords1inv.transform(coords2.transform(new Vector3f(1f, 0, 0))).negate(paperorigin);
		papery = coords1inv.transform(coords2.transform(new Vector3f(0, 1f, 0))).negate(paperorigin); 
	    } else {
		paperorigin = coords2.transform(new Vector3f(0, 0, 0));
		paperx = coords2.transform(new Vector3f(1f, 0, 0)).negate(paperorigin);
		papery = coords2.transform(new Vector3f(0, 1f, 0)).negate(paperorigin);
	    }

	
	    Paper.LightParam lightParam = new Paper.LightParam();

	    // These are now irrelevant
	    lightParam.orig = paperorigin; // - new Vector3f(0,0,0);
	    lightParam.e0 = (Vector3f) paperx.scale(scale);
	    lightParam.e1 = (Vector3f) papery.scale(scale);
	    lightParam.e2 = (Vector3f) new Vector3f(0,0,paperx.length()).scale(scale);

            lightParam.Light = new Vector3f(-1f,-1f,1f);
            lightParam.Light_w = 0.0f;

            if (dbg_paperquad) p(lightParam.orig+", "+lightParam.e0+", "+lightParam.e1+", "+lightParam.e2+
        	    ", x0: "+x0+", y0: "+y0+", x1: "+x1+", y1: +y1");
	    CallGL.checkGlError("midle of PaperQuad render.");


	    if((flags & Ren.PAPERQUAD_NONL_MAXLEN) != 0) {
		Vector2f p1 = coords1.transform(new Vector2f(x0,y0));
		Vector2f p2 = coords1.transform(new Vector2f(x0,y1));
		Vector2f p3 = coords1.transform(new Vector2f(x1,y0));
		Vector2f p4 = coords1.transform(new Vector2f(x1,y1));
		float dist[] = new float[]{
		    (p2.negate(p1)).length(),
		    (p3.negate(p1)).length(),
		    (p4.negate(p2)).length(),
		    (p4.negate(p3)).length()
		};
		float m = Float.MIN_VALUE;
		for (int i = 0; i < dist.length; i++)
		    if (dist[i] > m) m = dist[i];

		dice = (int)(m / dicefactor) + 2;
	    } else { // old way

		Vector3f ctr = new Vector3f(lerp(x0, x1, 0.5f), lerp(y0, y1, 0.5f), 0);
		float len = (float) (hypot(x1-x0, y1-y0) / 2);
		float nonl = coords1.nonlinearity(ctr, len);
		
		dice = (int)(len * nonl * dicefactor) + 2;
	    }
	    if (dbg_paperquad) p("Dice: "+dice); 

	    // Cap it at a ridiculous value
	    if( dice > 100) dice = 100;
	    if(dice < 2 ) dice = 2;

	    
	    
	    vertices = new float[dice * dice * 5];
            indices = new int[(dice) * (2*dice)];

//            #define VERTICES3(x, y, z) vertices[((x)*dice + (y))*5 + (z)]
//            #define VERTICES2(x, y)    vertices[((x)*dice + (y))*5]
//            #define INDICES2(x, y)     indices[(x)*2*dice + (y)]
//            #define INDICES1(x)        indices[(x)*2*dice]

	    int indps[] = new int[dice-1];
	    int counts[] = new int[dice-1];
	    for(int ix = 0; ix<dice; ix++) {
		if(ix < dice-1) {
		    counts[ix] = 2*dice;
		    indps[ix] = indexOfINDICES1(ix);
		}
		for(int iy = 0; iy<dice; iy++) {
		    if(ix < dice-1) {
			setINDICES2(ix, 2*iy,    dice * ix + iy);
			setINDICES2(ix, 2*iy+1,  dice * (ix+1) + iy);
		    }
		    float x = ix / (dice - 1.0f);
		    float y = iy / (dice - 1.0f);
		    Vector3f p = new Vector3f(lerp(x0, x1, x), lerp(y0, y1, y), 0);
		    Vector3f v = coords1.transform(new Vector3f(p));
		    setVERTICES3(ix, iy, 2,  v.x);
		    setVERTICES3(ix, iy, 3,  v.y);
		    setVERTICES3(ix, iy, 4,  v.z);
		    Vector3f t;
		    if((flags & Ren.PAPERQUAD_CS2_TO_SCREEN) != 0) {
			t = coords2inv.transform(v);
		    } else {
			t = coords2inv.transform(p);
		    }
		    setVERTICES3(ix, iy, 0,  t.x);
		    setVERTICES3(ix, iy, 1,  t.y);
		    if(dbg_paperquad) p("PaperQuad:: vert: " + 
			    ix + " " +
			    iy +" : " +
			    VERTICES3(ix, iy, 0) + " " +
			    VERTICES3(ix, iy, 1) + " " +
			    VERTICES3(ix, iy, 2) + " " +
			    VERTICES3(ix, iy, 3) + " " +
			    VERTICES3(ix, iy, 4) + " " +
			    "\n");
		}
	    }

	    if((flags & Ren.PAPERQUAD_USE_VERTEX_PROGRAM) != 0) {
		GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
		FloatBuffer v = BufferUtils.createFloatBuffer(vertices.length);
		v.put(vertices);
		v.flip();
		GL11.glInterleavedArrays(GL11.GL_T2F_V3F, 5*4 /* *4 size of byte*/, v);
		EXTCompiledVertexArray.glLockArraysEXT(0, dice*dice);

		for(int it = 0; it < paper.getPassCount(); ++it) {

		    Pass pass = paper.getPass(it);
		    
                    if(dbg_paperquad) p("Pass");
                    CallGL.checkGlError("start pass.");
                    pass.setUp_VP(lightParam);
                    
                    if(dbg_paperquad) p("Going to multidraw");
                    CallGL.checkGlError("start quad strip.");
                    
                    
//                    glMultiDrawElementsEXT(GL_QUAD_STRIP, counts,
//                	    GL11.GL_UNSIGNED_INT, (const GLvoid **)indps, dice-1);
                    // http://oss.sgi.com/projects/ogl-sample/registry/EXT/multi_draw_arrays.txt
                    IntBuffer ints = BufferUtils.createIntBuffer(2*dice);
                    
                    for (int ix=0; ix<dice-1; ix++) {
                	ints.position(0);
                	
                	for(int iy = 0; iy<dice; iy++) {
                	    ints.put(dice * ix + iy);
                	    ints.put(dice * (ix+1) + iy);
                	}
                	ints.flip();
                	GL11.glDrawElements(GL11.GL_QUAD_STRIP, ints);
                    }
//                    throw new Error("argh lwjgl is missing impl.");
                    
                    if(dbg_paperquad) p("Teardown");
                    CallGL.checkGlError("tear down.");
                    pass.tearDown_VP();
                
                    CallGL.checkGlError("end pass.");
                    if(dbg_paperquad) p("Pass over");
		}
		EXTCompiledVertexArray.glUnlockArraysEXT();
		GL11.glPopClientAttrib();
	    } else {
		for(int it = 0; it < paper.getPassCount(); ++it) {

		    Pass pass = paper.getPass(it);

                    if(dbg_paperquad) p("Pass");
                    CallGL.checkGlError("start pass.");
                    pass.setUp_explicit(lightParam);
                    
                    if(dbg_paperquad) p("Going to set texcoords explicit");
                    CallGL.checkGlError("set texcoords explicit.");

                    for(int ix = 0; ix<dice-1; ix++) {
                	GL11.glBegin(GL11.GL_QUAD_STRIP);
                        for(int iy = 0; iy<dice; iy++) {

                             float tmp[] = new float[]{ VERTICES3(ix, iy, 0), VERTICES3(ix, iy, 1), 0 ,1f };
			    if(dbg_paperquad) p("to texcoords");
			    pass.texcoords_explicit( tmp );
			    if(dbg_paperquad) p("to vertex");
			    int ind = indexOfVERTICES2(ix, iy)+2;
                             GL11.glVertex3f(vertices[ind], vertices[ind+1], vertices[ind+2] );
                                
                             float tmp2[] = new float[]{ VERTICES3(ix+1, iy, 0), VERTICES3(ix+1, iy, 1), 0 ,1f };
			    if(dbg_paperquad) p("to texcoords");
                             pass.texcoords_explicit( tmp2 );
			    if(dbg_paperquad) p("to vertex");
			    ind = indexOfVERTICES2(ix, iy)+2;
                            GL11.glVertex3f(vertices[ind], vertices[ind+1], vertices[ind+2] );
                         }
			if(dbg_paperquad) p("to end");
			GL11.glEnd();
                    }


                    if(dbg_paperquad) p("Teardown");
                    CallGL.checkGlError("tear down.");
                    pass.tearDown_explicit();
                
                    CallGL.checkGlError("end pass.");
                    if(dbg_paperquad) p("Pass over");
                }
	    }

	    if(dbg_paperquad) p("Passes over");
	    CallGL.checkGlError("at the end of PaperQuad render.");
	}

//        #define VERTICES3(x, y, z) vertices[((x)*dice + (y))*5 + (z)]
//        #define VERTICES2(x, y)    vertices[((x)*dice + (y))*5]
//        #define INDICES2(x, y)     indices[(x)*2*dice + (y)]
//        #define INDICES1(x)        indices[(x)*2*dice]

	private float VERTICES3(int x, int y, int z) { 
	    return vertices[((x)*dice + (y))*5 + (z)]; 
	}
	private void setVERTICES3(int x, int y, int z, float value) {
	    vertices[((x)*dice + (y))*5 + z] = value;
	}
	private void setINDICES2(int x, int y, int val) {
	    vertices[((x)*dice + (y))*5] = val;
	}
	private int indexOfVERTICES2(int x, int y) {
	    return (x)*2*dice + (y);
	}
	private int indexOfINDICES1(int x) {
	    return x*2*dice;
	}
	private double hypot(float a, float b) {
	    return Math.sqrt(a*b+b*a);
	}
	
	//	a ..... b
	//   0   b ..... a
	private float lerp(float a, float b, float d) {
	    return a+(b-a)*d;
	}

    }