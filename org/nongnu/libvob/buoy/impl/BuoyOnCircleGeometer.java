/*
BuoyOnCircleGeometer.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.buoy.impl;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.gl.GLVobCoorder;
import org.nongnu.libvob.buoy.*;

/** An abstract base class for buoy geometers
 * using BuoyOnCircle.
 * This class assumes that the derived class will set the 
 * buoyinto to the surrounding coordinate system for the 
 * BuoyOnCircle2 coordinate system.
 */
public abstract class BuoyOnCircleGeometer implements BuoyGeometer {
    /** The BuoyOnCircle2 ellipse coordinate system.
     * This coordinate system defines the ellipse used by
     * the buoy geometry algorithm, and should be set by
     * the derived class.
     * <p>
     * PUBLIC ONLY FOR DEBUGGING! DO NOT TOUCH OR USE
     */
    public int buoyinto;

    /** The factor by which to shift the anchor when 
     * multiple buoys float from the same anchor.
     */
    public float shiftFactor = 18;

    /** If non-null, this class will be used to generate
     * nadir coordinate systems.
     */
    public NadirManager nadirManager;

    public int buoyCS(VobScene vs, int anchor, int direction,
		Object key, 
		int index, int total,
		float w, float h, float scale) {
	if(anchor < 0) return -1; // Ignore anchorless

	int shiftedAnchor = anchor;

	// Shift the anchor, if required
	if(shiftFactor != 0) {
	    float shift = index;
	    if(index > 0)
		if(index % 2 == 1) {
		    shift += 1;
		    shift *= -1;
		}
	    shift *= .5;
	    if(shift != 0) {
		// XXX Strictly speaking, we should also get the
		// unit square here - should do so later.
		// However, for now the important thing is that
		// when there is one link, that at least works all right.
		shiftedAnchor = vs.translateCS(anchor, "Shift", 
			    0, shift * shiftFactor);
	    }
	}
	int buoy = ((GLVobCoorder)vs.coords).
			buoyOnCircle2(
				    this.buoyinto,
				    shiftedAnchor,
				    direction, 10);

	int inBuoy = vs.coords.orthoBox(buoy, 
			    -100, -w/2 * scale, -h/2 * scale, 
				    scale, scale, w, h);

	if(nadirManager != null)
	    inBuoy = nadirManager.makeNadired(vs, inBuoy);

	vs.matcher.add(this.buoyinto, inBuoy, key);

	return inBuoy;

    }

}
