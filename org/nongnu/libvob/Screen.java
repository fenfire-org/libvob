/*
Screen.java
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
 * Written by Rauli Ruohonen, Antti-Juhani Kaijanaho and Tuomas Lukka
 */
package org.nongnu.libvob;
import java.util.*;
import java.io.*;

/** An aggregate which defines a user-visible window and its contents.
 * The View+Controller part of MVC.
 */

public class Screen implements AbstractUpdateManager.Window {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    public interface RenderListener {
	void render(Screen scr, VobScene prev, VobScene next);
    }

    /** The window system -level window.
     */
    public final GraphicsAPI.Window window;

    /** The input handler (controller).
     */
    public final Binder binder;

    /** The view.
     */
    public final Shower shower;

    public RenderListener renderListener = null;


    protected VobScene prev, next;

    protected float latestFract = -2;
    protected float latestLod = 0;
    protected boolean latestShowFinal = false;

    public Screen(GraphicsAPI.Window window, Binder binder, Shower shower) {
	this.window = window;
	this.binder = binder;
	this.shower = shower;

	binder.setScreen(this);
	shower.setScreen(this);

	prev = window.createVobScene();


	window.registerBinder(binder);
    }

    public void printScreen() {
	pa("Sorry, not implemented by the current screen type.");
    }


    public boolean hasEndState() { return next != null; }
    public boolean animUseful() {
        if(prev != null && next != null)
	    return window.needInterp(prev, next);
	else
	    return false;
    }

    public void changeStartState(float fract) {
	// next.setInterpCoords(prev, 1-fract);
	prev = next;
	next = null;
    }

    public void endAnimation() {
	if (next != null) {
	    prev = next; next = null;
	}
    }
    
    // EggTimer generateView = new EggTimer("Generate view", 30);

    public boolean generateEndState(int millis, float lod) {
	// generateView.start();
	next = shower.generate(null); // XXX vob.putil.demo.py
	// generateView.stop();
	
	if(renderListener != null) renderListener.render(this, prev, next);

	return true;
    }


    /** Renders and display current view state on screen
     */
    public void renderStill(float lod) {
	latestFract = -1;
	latestLod = lod;
	window.renderStill(prev, lod);
    }
    public void renderAnim(float fract, float lod, boolean showFinal) {
	latestFract = fract;
	latestLod = lod;
	latestShowFinal = showFinal;
	window.renderAnim(prev, next, fract, lod, showFinal);
    }

    public VobScene getVobSceneForEvents() { return next != null ? next : prev; }

    public void repaint() {
	if(latestFract == -2) return;
	if(latestFract == -1)
	    window.renderStill(prev, latestLod);
	else
	    window.renderAnim(prev, next, latestFract, latestLod, latestShowFinal);
    }
    
}
