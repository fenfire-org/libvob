/*
BuoyGeometryConfiguration.java
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
import java.util.Set;

/** An interface for telling Buoy Managers which
 * geometers to use when.
 */
public interface BuoyGeometryConfiguration {
    /** Get the Main View Geometer to use for
     * the given main node.
     */
    BuoyMainViewGeometer getMainViewGeometer(
			BuoyViewMainNode node);

    /** Get the Sizer to use for
     * the given main node and connector.
     */
    BuoySizer getSizer(BuoyViewMainNode node,
			BuoyViewConnector connector);

    /** Get the Geometer to use for
     * the given main node and connector.
     */
    BuoyGeometer getGeometer(BuoyViewMainNode node,
			BuoyViewConnector connector);

    /** Get the set of all BuoyGeometers (for preparing).
     * This method returns a set containing all BuoyGeometer
     * objects that can be returned by the getGeometer method
     * of this object for any parameters.
     * The point is that the BuoyGeometer.prepare() method has to be
     * called exactly once for each, and this method gives
     * all of them conveniently set up for iteration.
     */
    Set getGeometers(BuoyViewMainNode node);
}
