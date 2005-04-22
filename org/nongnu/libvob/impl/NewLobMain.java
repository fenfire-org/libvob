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
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.fn.*;
import org.nongnu.libvob.vobs.SolidBackdropVob;
import javolution.realtime.*;
import java.awt.Dimension;
import java.awt.Color;
import java.util.*;

public abstract class NewLobMain extends Main {
    protected VobScene lastScene, scene2, scene3;
    protected Vob backgroundVob;

    private boolean initialized = false;

    protected Color bgColor;

    protected Model focusModel = SimpleModel.newInstance();
    protected int focusIndex = 0; // XXX

    public NewLobMain(Color bgColor) {
	this.bgColor = bgColor;
    }

    protected abstract Lob createLob();

    public void run() {
	run(new LobBinder(), new LobShower());

	backgroundVob = new SolidBackdropVob(bgColor);
	
	PoolContext.enter();
	try {
	    Lob lob = createLob();
	    List focusable = lob.getFocusableLobs();
	    if(focusable.size() > 0) focusModel.set(focusable.get(0));
	} finally {
	    PoolContext.exit();
	}

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
	    LocalContext.enter();
	    try {
		Lobs.setFocusModel(focusModel);

		long m0 = System.currentTimeMillis();
		
		Lob layout = createLob().layout(size.width, size.height);
		
		long m1 = System.currentTimeMillis();
		
		layout.render(scene, 0, 0, 1, true);
		
		long m2 = System.currentTimeMillis();

		//System.out.println("Gen scene: "+(m1-m0)+"+"+(m2-m1));
	    } finally {
		LocalContext.exit();
		PoolContext.exit();
	    }
	    
	    initialized = true;
	    return scene;
	}
    }

    protected boolean dontUseFocusLob = false;

    protected class LobBinder extends AbstractBinder {
	public void keystroke(String key) {
	    if(!initialized) return;

	    if(dontUseFocusLob) {
		// ARGH!!!
		PoolContext.enter();
		try {
		    Lob lob = createLob();
		    List focusable = lob.getFocusableLobs();
		    if(focusable.size() > 0) focusModel.set(focusable.get(0));
		} finally {
		    PoolContext.exit();
		}
	    }

	    PoolContext.enter();
	    LocalContext.enter();
	    try {
		Lobs.setFocusModel(focusModel);
		Lobs.setWindowAnimation(windowAnim);

		if(!key.equals("Tab")) {
		    Lob l = (Lob)focusModel.get();
		    if(l != null && l.key(key)) {
			if(!windowAnim.hasAnimModeSet())
			    windowAnim.animate();
			else
			    AbstractUpdateManager.chg();
		    }
		} else {
		    Lob lob = createLob();
		    List focusable = lob.getFocusableLobs();
		    if(focusable.size() == 0) {
			focusModel.set(null);
		    } else {
			focusIndex++;
			if(focusIndex >= focusable.size()) focusIndex = 0;
			focusModel.set(focusable.get(focusIndex));
		    }
		    
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
		Dimension size = ((Screen)windowAnim).window.getSize();

		PoolContext.enter();
		LocalContext.enter();
		try {
		    Lobs.setFocusModel(focusModel);
		    Lobs.setWindowAnimation(windowAnim);
		    
		    Lob lob = createLob();
		    lob = lob.layout(size.width, size.height);

		    if(lob.mouse(e, sc, 0, e.getX(), e.getY())) {
			if(!windowAnim.hasAnimModeSet())
			    //if(e.getType() == e.MOUSE_CLICKED)
			    windowAnim.animate();
			else
			    AbstractUpdateManager.chg();
		    }
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
