/*
MultiBuoyManager.java
 *    
 *    Copyright (c) 2003, Matti J. Katila
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

package org.nongnu.libvob.buoy;
import org.nongnu.libvob.VobScene;

/** Manager for multiple buoy manager views of buoy oriented interface.
 */
public interface MultiBuoyManager {

    /** Draw all buoy managers, focis and buoys related to them, 
     * into scene.
     * @see VobScene 
     */
    void draw(VobScene vs);

    /** Find the topmost buoy manager 
     * from overlapping managers.
     */
    BuoyManager findTopmostBuoyManager(VobScene oldVS, int x, int y);

    /** Find if a buoy was hit by the coordinates.
     * The buoy is only searched from coordinate system 0's 
     * child coordinate systems.
     */
    BuoyManager.Buoy findIfBuoyHit(VobScene oldVS, int x, int y);

    /** Get the last buoy which has been found 
     * with <code>findIfBuoyHit</code> method.
     */
    BuoyManager.Buoy getLastFoundBuoy();

    /** Get the manager which has the last found buoy.
     */
    BuoyManager getManagerByLastBuoyHit();

    /** Set the active buoy manager.
     * Buo manager is also set if <code>findIfBuoyHit</code>
     * method founds a buoy.
     */
    void setActiveBuoyManager(BuoyManager manager);
	
    /** Get the buoy manager which has been activated last.
     */ 
    BuoyManager getActiveBuoyManager();

}
