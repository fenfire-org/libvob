/* 
GLRen.template.java
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


package org.nongnu.libvob.gl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen;
import org.nongnu.libvob.impl.lwjgl.LWJGL_API;

import java.awt.Graphics;


public class GLRen {

    public static Vob createCallList(String s) {
	if (GraphicsAPI.getInstance() instanceof LWJGL_API) {
	    return LWJGLRen.createCallList(s);
	}
	return createCallList(GL.createDisplayList(s));
    }
    public static Vob createCallListCoorded(String s) {
	if (GraphicsAPI.getInstance() instanceof LWJGL_API) {
	    return LWJGLRen.createCallListCoorder(s);
	}
	return createCallListCoorded(GL.createDisplayList(s));
    }
    public static Vob createCallListBoxCoorded(String s) {
	if (GraphicsAPI.getInstance() instanceof LWJGL_API) {
	    return LWJGLRen.createCallListBoxCoorder(s);
	}
	return createCallListBoxCoorded(GL.createDisplayList(s));
    }
    /*
    public static Vob createMotionCallList(String still, String motion) {
	return createMotionCallList(GL.createDisplayList(still),
				    GL.createDisplayList(motion));
    }
    */

    private static boolean need_init = true;
    private static boolean have_VP_1_1;

    public static final int PAPERQUAD_CS2_TO_SCREEN = 1;
    public static final int PAPERQUAD_USE_VERTEX_PROGRAM = 2;

    public static final int IRREGU_SHIFTS = 0x0001;
    public static final int IRREGU_CS2_TO_SCREEN = 0x0002;

    public static void init() {
        if (GL.hasExtension("GL_NV_vertex_program1_1")) {
            have_VP_1_1 = true;
        } else {
            have_VP_1_1 = false;
        }
        need_init = false;
    }

    // ----  Shorthands.
    public static PaperQuad createPaperQuad(Paper paper, 
	    float x0, float y0, float x1, float y1, float dicefactor) {

            if (need_init) init();
            int flags = 0;

            if (have_VP_1_1) {
               flags |= PAPERQUAD_USE_VERTEX_PROGRAM;
            } else {
               flags &= ~PAPERQUAD_USE_VERTEX_PROGRAM;
            }
            
	return createPaperQuad(paper, x0, y0, x1, y1, 1, dicefactor, flags);
    }

    public static PaperQuad createPaperQuad(Paper paper, 
	    float x0, float y0, float x1, float y1, float dicefactor, int flags) {
	return createPaperQuad(paper, x0, y0, x1, y1, 1, dicefactor, flags);
    }

    public static IrregularQuad createIrregularQuad(float x0, float y0, float x1, float y1, float border, float freq, int flags,
						String setup, float dicefactor) {
	return createIrregularQuad(x0, y0, x1, y1, border, freq, flags, GL.createDisplayList(setup), dicefactor);
    }

    public static IrregularEdge createIrregularEdge(
	    int shape, float texscale, float linewidth, float refsize, float scale_pow,
            float border0, float border1, float texslicing,
            String const0, String const1, int angles, int multi, int flags,
            String setup, float dicefactor) {
	return createIrregularEdge(shape, texscale, linewidth, refsize, scale_pow,
            border0, border1, texslicing, const0, const1, angles, multi, flags,
            GL.createDisplayList(setup), dicefactor);
    }

    public static FixedPaperQuad createFixedPaperQuad(
	    org.nongnu.libvob.gl.Paper paper, 
	    float x0, float y0, float x1, float y1, 
	    int flags, 
	    float diceLength, float diceLength2, int diceDepth) {
	return createFixedPaperQuad(paper, x0, y0, x1, y1, flags, 
		    diceLength, diceLength2, diceDepth, null, 1);
    }


    
