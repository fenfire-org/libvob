/*
FisheyeState.java
 *    
 *    Copyright (c) 2003, : Tuomas J. Lukka
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
 * Written by : Tuomas J. Lukka
 */

package org.nongnu.libvob.view;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.gl.GLVobCoorder_Gen;

/** A class which encapsulates a fisheye transformation, zooming 
 * and panning, adjustable
 * by dragging with the mouse.
 * Uses clicks and drags by mouse button 3.
 */
public class FisheyeState {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println("FisheyeState:: "+s); }

    public float aspect;
    public float minmag;
    public float maxmag;
    public float minsize;
    public float maxsize;

    public float cursize;
    public float curx, cury;
    public float curmag, curmin;

    /** The mouse button used for changing
     *  the fisheye state. Default: Button 2.
     */
    public int MOUSE_BUTTON = java.awt.event.MouseEvent.BUTTON2_MASK;
    /** The mouse button used for changing
     *  the fisheye state, the vob constant. Default: Button 2.
     */
    public int VOBMOUSE_BUTTON = 2;

    private int curMouseX, curMouseY;

    /**
     * @param aspect The aspect ratio (x/y) to be used; if &gt;1, flat.
     */
    public FisheyeState(float aspect, float minmag, float maxmag, float minsize, float maxsize) {
	this.aspect = aspect;
	this.minmag = minmag;
	this.maxmag = maxmag;
	this.minsize = minsize;
	this.maxsize = maxsize;

	curx = 0;
	cury = 0;
	cursize = (float)Math.sqrt(minsize * maxsize);

	curmag = (float)Math.sqrt(minmag * maxmag);
	curmin = minmag;
    }

    public FisheyeState(float aspect, float minmag, float maxmag, 
			float minsize, float maxsize,
			float curmag, float cursize) {
	this(aspect, minmag, maxmag, minsize, maxsize);
	this.curmag = curmag;
	this.cursize = cursize;
    }    
    
    public void setCenter(float x, float y) {
	curx = x;
	cury = y;
    }

    VobScene vs;
    int cs, cs2;

    /** Put the current fisheye coordinate system
     * into the given vobscene. This puts more than one coordinate
     * system, and only the first one uses the given key.
     * The returned one is not matched and should be added to
     * the matcher by the caller.
     */
    public int getCoordsys(VobScene vs, int into, Object key) {
	this.vs = vs;
	cs = ((GLVobCoorder_Gen)vs.coords).distort(into,
		    0, 0, 0, 0, 0, 0);
	vs.matcher.add(into, cs, key);
	cs2 = vs.coords.translate(cs, 0, 0);
        vs.matcher.add(cs2, key.toString()+"_Inner");
	setCoordsysParams();
	return cs2;
    }
    public void setCoordsysParams() {
	if(dbg) pa("Fisheyestate: " +
		    cursize + " " +
		    curx + " " +
		    cury + " " +
		    curmag + " " +
		    curmin + " ");

	((GLVobCoorder_Gen)vs.coords).setDistortParams(cs,
		    0, 0,
		    cursize * aspect, cursize,
		    (float)Math.log(curmag), (float)Math.log(curmin)
		    );
	vs.coords.setTranslateParams(cs2, -curx, -cury);
    }

    public boolean event(java.awt.event.MouseEvent e) {
	if(dbg) pa("Ev:"+e);
	if(e.getID() == e.MOUSE_PRESSED) {
	    if(e.getModifiers() != MOUSE_BUTTON) return false;
	    curMouseX = e.getX();
	    curMouseY = e.getY();
	} else if(e.getID() == e.MOUSE_DRAGGED) {
	    if(e.getModifiers() != MOUSE_BUTTON) return false;

	    int dx = e.getX() - curMouseX;
	    int dy = e.getY() - curMouseY;

	    curMouseX = e.getX();
	    curMouseY = e.getY();

	    curmag *= Math.exp(.005 * dy);
	    cursize *= Math.exp(.005 * dx);

	    if(curmag < minmag) curmag = minmag;
	    if(curmag > maxmag) curmag = maxmag;

	    if(cursize < minsize) cursize = minsize;
	    if(cursize > maxsize) cursize = maxsize;

	    if(dbg) pa("Z: "+curmag+" "+cursize);

	    return true;

	}
	return false;
    }

    public boolean event(VobMouseEvent e) {
	if(dbg) pa("Ev:"+e);
	if(e.getType() == e.MOUSE_PRESSED) {
	    if(e.getButton() != VOBMOUSE_BUTTON) return false;
	    curMouseX = e.getX();
	    curMouseY = e.getY();
	} else if(e.getType() == e.MOUSE_DRAGGED) {
	    if(e.getButton() != VOBMOUSE_BUTTON) return false;

	    int dx = e.getX() - curMouseX;
	    int dy = e.getY() - curMouseY;

	    curMouseX = e.getX();
	    curMouseY = e.getY();

	    curmag *= Math.exp(.005 * dy);
	    cursize *= Math.exp(.005 * dx);

	    if(curmag < minmag) curmag = minmag;
	    if(curmag > maxmag) curmag = maxmag;

	    if(cursize < minsize) cursize = minsize;
	    if(cursize > maxsize) cursize = maxsize;

	    if(dbg) pa("Z: "+curmag+" "+cursize);

	    return true;

	}
	return false;
    }


    public void changeSize(float change) {
	cursize *= Math.exp(.005 * change);
	if(cursize < minsize) cursize = minsize;
	if(cursize > maxsize) cursize = maxsize;
    }

    public void changeMagnitude(float change) {
	curmag *= Math.exp(.005 * -change);
	if(curmag < minmag) curmag = minmag;
	if(curmag > maxmag) curmag = maxmag;
    }

}
