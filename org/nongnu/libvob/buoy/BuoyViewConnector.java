/*
BuoyViewConnector.java
 *    
 *    Copyright (c) 2003 by Benja Fallenstein
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
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.buoy;
import org.nongnu.libvob.*;

/** An interface that creates buoys for a main node.
 *  This must take care of the bidirectionality:
 *  If, for one given main node A, it creates a buoy B,
 *  then for a main node created by B, it must
 *  create a buoy corresponding to A.
 */
public interface BuoyViewConnector {

    /** Add buoys to the view created by a given main node.
     *  This is given the coordinate system that the
     *  main node has already rendered itself into.
     *  This method traverses the structure created
     *  by the main node and adds buoys accordingly.
     *  @param vs The vobscene
     *  @param cs The coordinate system that the main node was rendered into
     *  @param mainNode The main node object that was rendered
     *  @param linkListener The object to call for making buoys
     */
    void addBuoys(VobScene vs, int cs,
		  BuoyViewMainNode mainNode,
		  BuoyLinkListener linkListener);
}
