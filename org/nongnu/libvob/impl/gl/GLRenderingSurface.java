/*
GLRenderingSurface.java
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
package org.nongnu.libvob.impl.gl;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;

import java.util.HashMap;

import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.gl.*;

public class GLRenderingSurface extends GraphicsAPI.AbstractRenderingSurface {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    GLRenderingSurface(GraphicsAPI gfxapi, int x, int y, int w, int h) {
	super(gfxapi);
	surface = createGLObj(x,y,w,h);
    }

    GL.RenderingSurface surface;
    public GL.RenderingSurface getRenderingSurface() { return surface; }

    static private final Object SCREENSIZEKEY = new Object();


    /** Called during initialization to create the corresponding GL object.
     */
    protected GL.RenderingSurface createGLObj(int x, int y, int w, int h) {
	if(dbg) pa("Create rendersurface "+w+" "+h);
	return GL.createStableRenderingSurface(w, h);
    }

    public VobScene createVobScene(Dimension size) {
	

	VobScene vs = new VobScene(
		new GLVobMap(this),
		new GLVobCoorder(),
		new DefaultVobMatcher(),
		this.getGraphicsAPI(),
		this,
		size
		);

	// Put the cs no 1, i.e. the screen size
	vs.boxCS(0, SCREENSIZEKEY, size.width, size.height);
	
	// the API particularly forbids this.
	// vs.put(getBGClear(), "NOCKEY", 10, 0, 0, 1, 1);
	return vs;
    }

    public ChildVobScene createChildVobScene(Dimension size, 
		int numberOfParameterCS) {
	GLVobCoorderBase glVobCoorder = new GLVobCoorder();
	glVobCoorder.setNumberOfParameterCS(numberOfParameterCS);
	GLChildVobScene vs = new GLChildVobScene(
		new GLVobMap(this),
		glVobCoorder,
		new DefaultVobMatcher(),
		this.getGraphicsAPI(),
		this,
		size,
		numberOfParameterCS
		);
	return vs;
    }

    public Dimension getSize() {
	Rectangle bounds = surface.getBounds();
	return new Dimension(bounds.width, bounds.height);
    }

    public void renderStill(VobScene scene, float lod) {
	// XXX Not sure if this is right place for stats -- need to think about
	// it some more -- Tjl
	GL.clearQueuedStatistics();
	((GLVobCoorder)scene.coords).renderInterp(surface,
		    (GLVobMap)scene.map, null,
		    null, 0, true, true);
	GL.callQueuedStatistics();
    }

    VobScene listprev, listnext;
    int[] interplist;

    protected void createInterpList(VobScene sc, VobScene osc, 
				    boolean towardsOther) {
	if(sc != listprev || osc != listnext) {
	    listprev = sc;
	    listnext = osc;
	    interplist = sc.matcher.interpList(osc.matcher, towardsOther);
	    interplist[0] = interplist.length;
	}
    }

    public boolean needInterp(VobScene prev, VobScene next) {
	createInterpList(prev, next, true);
	if(interplist == null) return false;
	return prev.coords.needInterp(next.coords, interplist);
    }

    public void renderAnim(VobScene prev, VobScene next, float fract, float lod,
	    boolean showFinal) {
	if(dbg) pa("glscreen renderanim "+fract+" "+lod);
	VobScene sc = prev;
	VobScene osc = next;
	boolean towardsOther = true;
	if (fract > AbstractUpdateManager.jumpFract) {
	    sc = next;
	    osc = prev;
	    fract = 1-fract;
	    towardsOther = false;
	}
	if(osc == null) osc = sc;
	if(dbg) {
	    pa("Going to render: "+sc+" "+osc+" "+fract);
	    sc.dump();
	}

	createInterpList(sc, osc, towardsOther);

	((GLVobCoorder)sc.coords).renderInterp(surface, 
					(GLVobMap)sc.map, interplist, 
					       (GLVobCoorder)osc.coords, fract,
					       true, showFinal);

    }

    public int[] readPixels(int x, int y, int w, int h) {
	GL.ByteVector v = GL.createByteVector(w*h*4);
	Dimension d = ((GraphicsAPI.AbstractRenderingSurface)this).getSize();
	v.readFromBuffer(surface, "FRONT",
		    x, d.height-y-h, w, h,
		    "BGRA", "UNSIGNED_BYTE");
	int[] res = v.getInts();
	// Exchange to get it right way up
	for(int row = 0; row < h/2; row++) {
	    for(int col = 0; col < w; col++) {
		int tmp = res[row*w + col];
		res[row*w + col] = res[(h-1-row)*w + col];
		res[(h-1-row)*w + col] = tmp;
	    }
	}
	return res;
    }
    public double timeRender(VobScene vs, boolean swapbuf, int iters) {
	return ((GLVobCoorder)vs.coords).timeRender(surface, (GLVobMap)vs.map,
						    true, swapbuf, iters);
    }


}
