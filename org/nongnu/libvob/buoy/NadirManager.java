/*
NadirManager.java
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

package org.nongnu.libvob.buoy;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;

/** A class that manages the nadir for a view or part of it.
 * This class caches the coordinate system internally, so it should
 * only be used for one vobscene at a time.
 */
public class NadirManager {

    /** The VobScene for which the nadir is known.
     */
    private VobScene curVobScene;

    /** The nadir cs.
     */
    private int nadirCS;

    /** The coordinates of the nadir point.
     */
    private float nadirX, nadirY;

    /** The key to use for the nadir-marking CS.
     */
    private Object key;

    /** Create a new nadir manager.
     * @param nadirX The X coordinate of the nadir, 0 = left, 1 = right edge of screen
     * @param nadirY The Y coordinate of the nadir, 0 = top, 1 = bottom edge of screen
     */
    public NadirManager(float nadirX, float nadirY, Object key) {
	this.nadirX = nadirX;
	this.nadirY = nadirY;
	this.key = key;
    }

    /** Return a nadired version of the given coordinate system.
     * The box size and the center of the box remain the same but the
     * coordinate system is rotated to face the nadir.
     * No key is used for the returned coordinate system, the system 
     * can choose it.
     */
    public int makeNadired(VobScene vs, int cs) {
	if(vs != curVobScene) {
	    nadirCS = vs.matcher.getCS(0, key);
	    if(nadirCS < 0)
		nadirCS = vs.translateCS(0, key, 
			    nadirX * vs.size.width, 
			    nadirY * vs.size.height);
	    curVobScene = vs;
	} 
	return ((GLVobCoorder)vs.coords).nadirUnitSq(cs, nadirCS);
    }
}
