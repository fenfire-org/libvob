/*
BuoySizer.java
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

/** An interface for objects that determine the real size to
 * use for a buoy.
 */
public interface BuoySizer {
    /** Get the size and scale
     * a buoy should be shown at (at its maximum,
     * nearest to the focus),
     * @param w The pixel width desired by the buoy
     * @param h The pixel height desired by the buoy
     * @param whout The output width and height
     * @return The scale to use (width and height are *before*
     *   scaling, i.e. (400,400) and .5 means real size (200,200)
     */
    float getBuoySize(float w, float h, float[] whout);
}
