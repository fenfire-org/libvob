/*   
NewLobMain.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein,
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
import org.nongnu.libvob.layout.IndexedVobMatcher;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.vobs.SolidBackdropVob;
import javolution.realtime.*;
import java.awt.Dimension;
import java.awt.Color;

public abstract class NewLobMain extends Main {
    protected VobScene lastScene, scene2, scene3;
    protected Vob backgroundVob;

    private boolean initialized = false;

    protected Color bgColor;

    public NewLobMain(Color bgColor) {
	this.bgColor = bgColor;
    }

    protected abstract Lob createLob();

    public void run() {
	run(new LobBinder(), new LobShower());

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

	    PoolContext.enter();
	    try {
		long m0 = System.currentTimeMillis();
		
		Lob layout = createLob().layout(size.width, size.height);
		
		long m1 = System.currentTimeMillis();
		
		layout.render(scene, 0, 0, 1, true);
		
		long m2 = System.currentTimeMillis();

		//System.out.println("Gen scene: "+(m1-m0)+"+"+(m2-m1));
	    } finally {
		PoolContext.exit();
	    }
	    
	    initialized = true;
	    return scene;
	}
    }

    protected class LobBinder extends AbstractBinder {
	public void keystroke(String key) {
	    PoolContext.enter();
	    LocalContext.enter();
	    try {
		Lobs.setWindowAnimation(windowAnim);

		if (initialized && createLob().key(key)) {
		    if(!windowAnim.hasSceneReplacementPending())
			windowAnim.animate();
		}
	    } finally {
		LocalContext.exit();
		PoolContext.exit();
	    }
	}

	float origX, origY;

	public void mouse(VobMouseEvent e) {
	    if (initialized) {
		/*
		if(e.getType() != e.MOUSE_DRAGGED) {
		    origX = e.getX(); origY = e.getY();
		}
		*/

		VobScene sc = windowAnim.getCurrentVS();

		PoolContext.enter();
		LocalContext.enter();
		try {
		    Lobs.setWindowAnimation(windowAnim);
		    
		    Dimension size = ((Screen)windowAnim).window.getSize();

		    Lob lob = createLob();
		    lob = lob.layout(size.width, size.height);

		    if(lob.mouse(e, sc, 0, e.getX(), e.getY()) &&
		       !windowAnim.hasAnimModeSet())
			//if(e.getType() == e.MOUSE_CLICKED)
			windowAnim.animate();
		} finally {
		    LocalContext.exit();
		    PoolContext.exit();
		}
	    }
	}

	public void windowClosed() {
	    System.exit(0);
	}
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
