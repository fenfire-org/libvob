/*
HBox.java
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
import org.nongnu.libvob.impl.*;
import java.util.*;

/** A box that knows its size at different scales.
 * Used for linebreaking with billowing, where we need to layout
 * pieces at different sizes depending on which line they fall on.
 * <p>
 * The "normal" size is scale=1.
 */

public interface HBox {

    float getWidth(float scale);

    /** Get the ascent of the hbox from the baseline upwards.
     */
    float getHeight(float scale);

    /** Get the descent of the hbox from the baseline upwards.
     */
    float getDepth(float scale);

    /** Place the contents of this box into a given coordsys.
     * This function <strong>may</strong> change
     * this HBox and place it into the coordsys:
     * the following is a legal implementation:
     * <pre>
     *	public class FooBox extends Vob implements HBox {
     *		float scale;
     *   	...
     *   	...
     *		public void place(VobScene vs, int cs, float scale) {
     *			this.scale = scale;
     *			vs.map.put(this, cs);
     *		}
     *  }
     * </pre>
     */
    void place(VobScene vs, int coordsys, float scale);

    /** Get this box's length in characters or other units.
     *  <code>getX(i)</code> is valid for
     *  <code>i &lt; getLength()</code>.
     */
    int getLength();

    /** Get the position after a unit (usually character)
     *  inside this HBox.
     */
    float getX(int i, float scale) throws IndexOutOfBoundsException;

    /** The key for this HBox. XXX */
    Object getKey();

    /** Set the preceding HBox.
     * This is useful for beaming where
     * shared sequences should be truly shared.
     */
    void setPrev(HBox b);

    class Null implements HBox {
	protected int length;
	public Null() { this(0); }
	public Null(int length) { this.length = length; }

	public float getWidth(float scale) { return 0; }
	public float getHeight(float scale) { return 0; }
	public float getDepth(float scale) { return 0; }
	public void place(VobScene vs, int coordsys, float scale) {}
	public int getLength() { return length; }
	public float getX(int i, float scale) {
	    if(i<0 || i>length) throw new IndexOutOfBoundsException(""+i);
	    return 0;
	}
	public Object getKey() { return null; }
	public void setPrev(HBox b) { }
    }

    Object WH_KEY = new Object();

    /** A useful base class for hboxes that are vobs.
     *  The second coordsys will contain the width and height for this vob.
     */
    abstract class VobHBox extends AbstractVob implements HBox {
	// public VobHBox(Object key) { super(key); }
	protected float scale = 0;
	public float getScale() { return scale; }

	public void place(VobScene vs, int box, float scale) {
	    this.scale = scale;
	    /*
	      float [] boxwh = new float[2];
	      vs.coords.getSqSize(box, boxwh);
	      float h = boxwh[1];
	    */
	    float h = getHeight(scale)+getDepth(scale);
	    int cs_scale = vs.scaleCS(box, "textbox", h, h);
	    vs.map.put(this, cs_scale);
	}

	public int getLength() { return 1; }
	public float getX(int i, float scale) {
	    if(i < 0 || i > 1) throw new IndexOutOfBoundsException(""+i);
	    if(i == 0) return 0;
	    else return getWidth(scale);
	}

	public Object getKey() { return null; /* XXX */ }

	public void setPrev(HBox b) { }
	public void setPosition(int depth, int x, int y, int w, int h) { }

    }

}
