/*   
VobPlacer.java
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
package org.nongnu.libvob;

/** An interface for placing vobs into coordinate systems in 
 * VobCoorder s.
 * Used in VobScene through the subclass VobMap.
 * <p>
 * Note that the vobs know their place inside the 
 * VobCoorder coordinate system -- this class only maps 
 * the vobs to the coordinate systems.
 * @see VobCoorder
 * @see VobScene
 * @see VobMap
 */

public interface VobPlacer {
    void put(Vob vob);
    /** Place a <code>Vob</code> onto this scene. 
     *  @param vob   the vob to be placed
     *  @param coordsys The index of the coordinate system, 
     *  	obtained from the VobCoorder
     */
    void put(Vob vob, int coordsys);
    /** Place a <code>Vob</code> onto this scene, using two
     * coordinate systems.
     * This call is used e.g. for connectors which move along with two different
     * coordinate systems.. 
     *  @param vob   the vob to be placed
     *  @param coordsys The index of the coordinate system, 
     *  	obtained from the VobCoorder
     */
    void put(Vob vob, int coordsys1, int coordsys2);

    void put(Vob vob, int[] cs);
}

