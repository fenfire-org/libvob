/*
ColorableVob.java
 *    
 *    Copyright (c) 2003 by Asko Soukka
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *
 */
/*
 * Written by Asko Soukka
 */

package org.nongnu.libvob;
import java.util.List;
import java.awt.Color;

/** Interface for multiple background colors, which are shown,
 * e.g., as parallel vertical stripes inside a vob.
 */
public interface ColorableVob extends Vob {
    /** Create a multi-colored clone of the vob. Replace existing colors.
     * @param colors An array of colors to show inside the vob. 
     *               Reading of the array will stop at the first null.
     * @return A multi-colored vob.
     */
    ColorableVob cloneColorReplace(Color[] colors);
    ColorableVob cloneColorReplace(Color color);
    ColorableVob cloneColorReplace(List colors);

    /** Create a multi-colored clone of the vob by adding new colors
     * in addition to the already existing colors.
     * @param colors An array of colors to show inside the vob. 
     *               Reading of the array will stop at the first null.
     * @return A multi-colored vob.
     */
    ColorableVob cloneColored(Color[] colors);
    ColorableVob cloneColored(Color color);
    ColorableVob cloneColored(List colors);

    /** Return the colors of the vob in an array. */
    Color[] getColors();
}

