/*   
Placeable.java
 *    
 *    Copyright (c) 2003, Matti J. Katila, Asko Soukka
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
 *
 */
/*
 * Written by Matti J. Katila, Asko Soukka
 */
package org.nongnu.libvob.lava.placeable;
import org.nongnu.libvob.*;

/** 
 * Placeable interface for making it easier to locate
 * the text cursor and put it into in a VobScene.
 */
public interface TextPlaceable extends Placeable {
    /** 
     * Get the coordinates before the given character position.
     * This returns one x and two y coordinates: the top and
     * bottom of the line.
     * @param position The text cursor position.
     * @param xyyOut The coordinates of the given text cursor position:
     *               the x coordinate, the top y coordinate, and the
     *               bottom y coordinate.
     */
    void getCursorXYY(int position, float[] xyyOut);
    
    /**
     * Get the position of the first character placed the most
     * closest to the given coordinates.
     * @param x The X coordinate where the text cursor should be located.
     * @param y The Y coordinate where the text cursor should be located.
     * @return The text cursor position most closest to the given coordinates.
     */
    int getCursorPos(float x, float y);
}
