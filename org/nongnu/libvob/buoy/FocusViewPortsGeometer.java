/*
FocusViewPortsGeometer.java
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


/** A view ports geometer interface for multiple focuses.
 */
public interface FocusViewPortsGeometer {

    /** Creates a coordinate systems for focus view ports 
     *  or set new parameters for existing ones.
     * @param viewPortsOut if viewPortsOut item is < 0, a new 
     *                     coordinate system should be created, 
     *                     else set coordinate system parameters.
     */
    void place(VobScene vs, int[] viewPortsOut);

}
