/*   
LobMain.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein,
 *                  2004, Matti J. Katila.      
 *
 *    This file is part of Fenfire.
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
 * Written by Benja Fallenstein and Matti J. Katila
 */
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.vobs.SolidBackdropVob;
import org.nongnu.navidoc.util.Obs;
import java.awt.Dimension;
import java.awt.Color;

public abstract class LobMain extends Main implements Obs {
    protected VobScene lastScene, scene2, scene3;
    protected Vob backgroundVob;

    private boolean initialized = false;

    /** The lob rendered by this Shower.
     */
    protected Lob lob;

    protected Color bgColor;

    protected boolean needLayout = true;
    protected float layoutedW, layoutedH;

    public LobMain(Color bgColor) {
	this.bgColor = bgColor;
    }

    protected abstract Lob createLob();

    public void run() {
	run(new LobBinder(), new LobShower());

	lob = createLob();
	lob.addObs(this);

	backgroundVob = new SolidBackdropVob(bgColor);


	lastScene = makeScene();
	scene2 = makeScene();
	scene3 = makeScene();
    }

    protected class LobShower extends AbstractShower {
	public VobScene generate(VobScene emptyScene) {
	    Dimension size = ((Screen)windowAnim).window.getSize();
	    VobScene scene = scene3;

	    scene.clear(size);
	    
	    scene3 = scene2;
	    scene2 = lastScene;
	    lastScene = scene;

	    scene.map.put(backgroundVob);

	    long m0 = System.currentTimeMillis();
	    
	    if(needLayout || layoutedW != size.width || 
	       layoutedH != size.height) {
		
		lob.setSize(size.width, size.height);
		layoutedW = size.width; layoutedH = size.height;
		needLayout = false;
	    }
	    
	    long m1 = System.currentTimeMillis();

	    lob.render(scene, 0, 0, size.width, size.height, 1, true);

	    long m2 = System.currentTimeMillis();

	    //System.out.println("Gen scene: "+(m1-m0)+"+"+(m2-m1));
	    
	    initialized = true;
	    return scene;
	}
    }

    protected class LobBinder extends AbstractBinder {
	public void keystroke(String key) {
	    if (initialized) {
		if(lob.key(key))
		    windowAnim.animate();
	    }
	}

	public void mouse(VobMouseEvent e) {
	    if (initialized) {
		if(lob.mouse(e, e.getX(), e.getY()))
		    //if(e.getType() == e.MOUSE_CLICKED)
		    windowAnim.animate();
	    }
	}
    }

    public void chg() {
	needLayout = true;
    }

    // override this to use different kind of matcher
    protected VobMatcher makeMatcher() {
	return new IndexedVobMatcher();
    }
    
    protected VobScene makeScene() {
	VobScene sc0 = ((Screen)windowAnim).window.createVobScene();
	return new VobScene(sc0.map, sc0.coords, makeMatcher(),
			    sc0.gfxapi, sc0.window,
			    sc0.size);
    }
}
