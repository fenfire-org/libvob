/*   
VobMap.java
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
import java.awt.*;

/** An interface for mapping vobs and coordinate systems.
 * This interface extends VobPlacer with a call to get the vob at index i.
 */

public interface VobMap extends VobPlacer {
    void dump();

    /** Get the topmost vob (the one that was PLACED LAST)
     * in the given coordinate system.
     */
    Vob getVobByCS(int i);

    void setVS(VobScene vs);

    /** Remove all mappings from this map.
     *  This method makes maps re-usable; rather than creating
     *  a new map object, an old one can be re-used by clearing it.
     */
    void clear();

 
    /** (Not public API: for use by VobScene).
     */
    int _putChildVobScene(ChildVobScene child, int coorderResult,
			  int[] cs);

}

