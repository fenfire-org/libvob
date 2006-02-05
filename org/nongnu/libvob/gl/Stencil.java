/*
Stencil.java
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.gl;
import org.nongnu.libvob.impl.gl.*;
import org.nongnu.libvob.*;

/** Simple encapsulation of stenciling methods for OpenGL.
 * XXX Make recursive by using stencils greater than 1,
 * and making this an object, attached to a GLVobMap (?).
 */

public class Stencil {

    public static Vob initStencil;
    public static Vob initOutside;
    public static Vob initBackplane;
    public static Vob exitBackplane;
    public static Vob initContents_depth;
    public static Vob initContents_nodepth;
    public static Vob initZero;
    public static Vob exit;

    private static void init() {
	if(initStencil != null) return;

	initStencil = GLRen.createCallList(
" PushAttrib DEPTH_BUFFER_BIT ENABLE_BIT STENCIL_BUFFER_BIT COLOR_BUFFER_BIT DEPTH_BUFFER_BIT\n"+
"    Enable STENCIL_TEST\n"+
"    Enable DEPTH_TEST\n"+
"    StencilFunc ALWAYS 1 255\n"+
"    StencilOp ZERO ZERO REPLACE\n"+ // XXX
"    StencilMask 255\n"+
"    ColorMask 0 0 0 0\n"+
"    DepthMask 0 \n"
	    );

	initOutside = GLRen.createCallList(
"    StencilFunc EQUAL 0 1\n"+
"    Enable DEPTH_TEST\n"+
"    ColorMask 1 1 1 1\n"+
"    StencilMask 0\n"+
"    DepthMask 1\n"
	    );

	initBackplane = GLRen.createCallList(
" PushAttrib ENABLE_BIT STENCIL_BUFFER_BIT COLOR_BUFFER_BIT DEPTH_BUFFER_BIT\n"+
" PushMatrix \n"+
" Translate 0 0 5000 \n"+
"    StencilFunc EQUAL 1 1\n"+
"    ColorMask 0 0 0 0\n"+
"    Enable DEPTH_TEST\n"+
"    DepthFunc ALWAYS\n"+
"    DepthMask 1\n"
		);

	exitBackplane = GLRen.createCallList(
"    PopMatrix\n" +
"    PopAttrib\n"
	    );

	initContents_depth = GLRen.createCallList(
"    ColorMask 1 1 1 1\n"+
"    StencilFunc EQUAL 1 1\n"+
"    StencilMask 0\n"+
"    DepthMask 1\n"+
"    DepthFunc LEQUAL\n"+
"    Enable DEPTH_TEST\n"
	    );
	initContents_nodepth = GLRen.createCallList(
"    ColorMask 1 1 1 1\n"+
"    StencilFunc EQUAL 1 1\n"+
"    StencilMask 0\n"+
"    DepthMask 0\n"+
"    Disable DEPTH_TEST\n"
	    );

	initZero = GLRen.createCallList(
"    StencilFunc EQUAL 1 1\n"+
"    StencilOp ZERO ZERO ZERO\n"+ // XXX
"    StencilMask 255\n"+
"    Enable DEPTH_TEST\n"+ 
"    DepthFunc ALWAYS\n"+ 
"    DepthMask 1\n"+ // Do write now.
"    ColorMask 0 0 0 0\n"
	    );
	exit = GLRen.createCallList(
"    PopAttrib\n"
	    );

    }

    /** Draw something stenciled 
     * Assumes that stencil buffer is all zeroes when called; leaves
     * it zeroed if contract of drawOverStencil is met.
     * </b>
     * @param vs The VobScene to draw into
     * @param drawStencil The routine which causes exactly those
     * 			fragments to be drawn that need to be in the
     * 			stencil.
     * 			The colormask is set to zero so the fragments
     * 			won't be drawn into the framebuffer; XXX do we
     * 			need more API for this? Is it possible to want
     * 			them to be drawn?
     * @param drawOverStencil (may be null): the routine to draw 
     *                  at least those fragments in the stencil and
     *                  possibly more: e.g. if drawStencil uses
     *                  textures, this can draw the same polygons without
     *                  to save time.
     * @param drawOutside (may be null): if desired, something that
     *                  will be rendered only in the area OUTSIDE the
     *                  stencil
     * @param drawContents (may be null): the contents of the stencil.
     * @param needDepth Whether depth tests will be used inside the stencil.
     */
    static public void drawStenciled(VobScene vs, 
		Runnable drawStencil,
		Runnable drawOverStencil,
		Runnable drawOutside,
		Runnable drawContents,
		boolean needDepth) {
	init();
	if(drawOverStencil == null) drawOverStencil = drawStencil;
	VobMap vm = (VobMap)vs.map;

	// First, draw the stencil
	vm.put(initStencil);
	drawStencil.run();
	
	// Draw the outside
	if(drawOutside != null) {
	    vm.put(initOutside);
	    drawOutside.run();
	}
	
	// If depth is needed, draw the "backplane"
	if(needDepth) {
	    vm.put(initBackplane);
	    drawOverStencil.run();
	    vm.put(exitBackplane);
	}

	
	// Draw the contents
	if(drawContents != null) {
	    if(needDepth) 
		vm.put(initContents_depth);
	    else
		vm.put(initContents_nodepth);
	    drawContents.run();
	}

	vm.put(initZero);
	drawOverStencil.run();

	vm.put(exit);
	
	// Zero stencil buffer
    }
}
