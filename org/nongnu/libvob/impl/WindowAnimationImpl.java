/*
WindowAnimationImpl.java
 *    
 *    Copyright (c) 2004, Matti J. Katila
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
 */
/*
 * Written by Matti J. Katila
 */

package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.awt.AWTAPI;

/** Improved Screen. 
 */
public class WindowAnimationImpl extends Screen
    implements WindowAnimation, 
	       WindowAnimation.BackgroundProcess {

    static private void p(String s) { System.out.println("WinAnimationImpl:: "+s); }
    static public boolean dbg = false;


    public WindowAnimationImpl(GraphicsAPI.Window window, Binder binder, 
			       Shower show) {
	this(window, binder, show, true);
    }
    public WindowAnimationImpl(GraphicsAPI.Window window, Binder binder, 
			       Shower show, boolean createScene) {
	super(window, binder, show);
	createVS = createScene;
    }
    private boolean createVS;

    // The possible values for animMode:
    static protected final Object ANIM = new String("ANIM"); 
    static protected final Object SWITCH = new String("SWITCH"); 
    static protected final Object RERENDER = new String("RERENDER");

    protected Object animMode = null;

    /*
     * ----------------------------------------------
     *  Implement Window Animation . Bacground Process
     * ----------------------------------------------
     */

    public WindowAnimation.BackgroundProcess getInstance() {
	return this;
    }
    public void chg() {
	Thread t = new Thread() {
		public void run() {
		    try {
			sleep(50);
		    } catch (Exception _) {}
		    rerender();
		}
	    };
	t.start();
    }


    /*
     * ----------------------------------------------
     *  Implement Window Animation
     * ----------------------------------------------
     */


    public void animate() {
	animMode = ANIM;
	AbstractUpdateManager.chg();
    }

    public void switchVS() {
	animMode = SWITCH;
	AbstractUpdateManager.chg();
    }

    public void rerender() {

	/*
	// until awt *CAN* rerender we need to switch!
	if (GraphicsAPI.getInstance() instanceof AWTAPI) {
	    switchVS();
	    return;
	}
	*/
	if (animMode == null || animMode == RERENDER) {
	    animMode = RERENDER;
	    AbstractUpdateManager.chg();
            //
            // DO NOT CALL THIS.
            // libovb is SINGLE threaded!
            //window.renderStill(prev, latestLod);
            //p("did anything happen?");
	    //repaint();
	}
    }

    public VobScene getCurrentVS() {
	return super.getVobSceneForEvents();
    }

    public boolean hasSceneReplacementPending() {
	return animMode == ANIM || animMode == SWITCH;
    }

    public boolean hasAnimModeSet() {
	return animMode != null;
    }

    /*
     * ----------------------------------------------
     *  Implement AbstractUpdateManager.Window
     * ----------------------------------------------
     */

    public boolean generateEndState(int millis, float lod) { 
	//p("end state! "+animMode);
	if (animMode == RERENDER) {
	    next = prev;
	} else {
	    if (dbg) p("generate new scene");
	    long start = System.currentTimeMillis();
	    VobScene emptyVS;
	    if (createVS) {
		emptyVS = super.window.createVobScene();
		emptyVS.anim = this;
	    } else 
		emptyVS = null;
	    super.next = super.shower.generate(emptyVS);
	    super.next.anim = this; // someone can replace the scene

	    long stop = System.currentTimeMillis();
	    if (stop-start > 50)
		if (dbg) p("Oh' no! View generation took "+
			   "too much time: "+(stop-start));
	}
	return true;
    }
    public boolean animUseful() { 
	if (dbg) p("useful anim?: "+(animMode == ANIM));
	return animMode == ANIM && super.animUseful(); 
    }
    public void renderAnim(float fract, float lod, boolean showFinal) {
	super.renderAnim(fract, lod, showFinal);
    }
    public void changeStartState(float fract) { 
	// next.setInterpCoords(prev, 1-fract);
	super.changeStartState(fract);
	//animMode = ANIM;
    }
    public void renderStill(float lod) { super.renderStill(lod); }
    public boolean hasEndState() { return super.hasEndState(); }
    public void endAnimation() {
	super.endAnimation();
	animMode = null;
    }
}
