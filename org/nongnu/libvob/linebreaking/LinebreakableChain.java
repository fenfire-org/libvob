/*
LinebreakableChain.java
 *
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob.linebreaking;
import org.nongnu.libvob.*;
import java.util.*;

/** Default impl of <code>HChain</code>.
 */
public class LinebreakableChain implements HChain {

    public static final int GLUE_LENGTH = 0;
    public static final int GLUE_STRETCH = 1;
    public static final int GLUE_SHRINK = 2;
    public static final int BREAKS = 3;

    protected static final int PROPERTIES = 4;

    int nboxes;
    protected HBox[] boxes;

    public LinebreakableChain() {
	allocCopy(20);
    }

    /** For each Vob in boxes, three values:
     * natural, plus, minus.
     * This is glue that comes <strong>before</strong> the
     * vob.
     * XXX Should glues also be virtual and have scale?
     * fonts scale non-uniformly, so should glue?
     */
    protected float[] glues;

    public void addBox(HBox box) {
        ensureBoxes(nboxes+1, false);
        int ind = nboxes++;
        boxes[ind] = box;
        if(ind > 0)
            box.setPrev(boxes[ind-1]);
    }

    public void addGlue(float len, float str, float shr) {
        glues[nboxes*PROPERTIES + GLUE_LENGTH] += len;
        glues[nboxes*PROPERTIES + GLUE_STRETCH] += str;
        glues[nboxes*PROPERTIES + GLUE_SHRINK] += shr;
    }

    public void addBreak() {
        glues[nboxes*PROPERTIES + BREAKS]++;
    }

    public int length() {
	return nboxes;
    }

    public HBox getBox(int n) {
	return boxes[n];
    }

    public float getGlue(int n, int property) {
	if(property < 0 || property > 2)
	    throw new IllegalArgumentException("illegal property: "+property);
	return glues[(n * PROPERTIES) + property];
    }
    
    public int getBreaks(int n) {
        return (int)glues[(n * PROPERTIES) + BREAKS];
    }





    /** Expand the arrays to contain space for <code>n</code> boxes.
     *  This is called by <code>ensureBoxes</code> when there is not enough
     *  space in the arrays. It simply creates new, larger arrays and
     *  copies the values from the old arrays into the new ones.
     */
    void allocCopy(int n) {
	HBox[] nboxes = new HBox[n];
	float[] nglues = new float[PROPERTIES*(n+1)];
	if(boxes != null) {
	    System.arraycopy(boxes, 0, nboxes, 0, boxes.length);
	    System.arraycopy(glues, 0, nglues, 0, glues.length);
	}
	boxes = nboxes;
	glues = nglues;
    }

    /** Ensure that at least <code>n</code> boxes fit into the arrays.
     *  Checks whether <code>n</code> is larger than the number of boxes
     *  that currently fit into the arrays, and if so, calls
     *  <code>allocCopy</code> to increase the arrays' size.
     *  @param accurate If the arrays don't suffice, whether to create
     *  		arrays exactly of size <code>n</code>. Setting this
     *  		to true is only reasonable if it is expected that no
     *  		more than <code>n</code> boxes will be added to
     *  		this chain.
     */
    void ensureBoxes(int n, boolean accurate) {
	if(boxes == null) allocCopy(accurate ? n : n > 50 ? n : 50);
	if(boxes.length < n) {
	    int def = 2 * boxes.length;
	    allocCopy(accurate ? n : n > def ? n : def);
	}
    }




    public float getAscent(int start, int end, float scale) {
	float h = 0;
	for(int i=start; i<end; i++) {
	    float curh = boxes[i].getHeight(scale);
	    if(curh > h) h = curh;
	}
	return h;
    }

    public float getDescent(int start, int end, float scale) {
	float d = 0;
	for(int i=start; i<end; i++) {
	    float curd = boxes[i].getDepth(scale);
	    if(curd > d) d = curd;
	}
	return d;
    }
    
    public float getHeight(int start, int end, float scale) {
        return getAscent(start, end, scale) + getDescent(start, end, scale);
    }





    /** A style of glue.
     *  Constructed from a text style, a glue style represents the kind of
     *  glue that matches the text style (so that a space glue is as wide
     *  as a space of that text style, etc.). When glue needs to be added
     *  to the chain, call <code>GlueStyle.addSpace</code>,
     *  <code>GlueStyle.addCommaSpace</code>, or
     *  <code>GlueStyle.addSentenceSpace</code>. (All three do the same
     *  thing currently, but subclasses can override the behavior.)
     */
    static public class GlueStyle {
	public GlueStyle(TextStyle st) {
	    spc_g = (int)st.getWidth(" ", 1);
	    spc_p = spc_g / 2;
	    spc_m = spc_g / 2;
	    if(spc_p == 0) spc_p = 1;
	}
	int spc_g, spc_p, spc_m;
	public void addSpace(LinebreakableChain ch) {
	    ch.addGlue(spc_g, spc_p, spc_m);
	}
	public void addCommaSpace(LinebreakableChain ch) {
	    ch.addGlue(spc_g, spc_p, spc_m);
	}
	public void addSentenceSpace(LinebreakableChain ch) {
	    ch.addGlue(spc_g, spc_p, spc_m);
	}
    }

}



