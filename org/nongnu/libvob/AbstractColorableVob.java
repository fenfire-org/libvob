/*
AbstractColorableVob.java
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
import java.util.Iterator;
import java.util.List;
import java.awt.Color;
import java.lang.CloneNotSupportedException;

/** Colorable Vob is a very basic vob type, which enhances the regular
 * Vob with interface for placing multiple solid colors on its
 * background. The multiple solid colors are used a lot in basic views
 * i.e. to show cursor location and node properties. In the current
 * implementations multiple solid colors are shown as parallel vertical
 * stripes or sectors.
 */
public abstract class AbstractColorableVob extends AbstractVob
    implements ColorableVob, Cloneable {
    public static boolean dbg = false;
    static final void pa(String s) { System.out.println(s); }

    /** An array to store colors. The default value is null, but 
     * otherwise the array contains never null values.
     */
    protected Color[] colors = null;

    /** Create a multi-colored clone of the vob. Replace existing colors.
     * @param colors An array of colors to show inside the vob. 
     *               Reading of the array will stop at the first null.
     * @return A multi-colored vob.
     */
    public ColorableVob cloneColorReplace(Color[] colors) {
	AbstractColorableVob clone;
	try { clone =  (AbstractColorableVob) this.clone(); }
	catch (CloneNotSupportedException e) {
	    /* if (dbg) */ e.printStackTrace();
	    return null;
	}
	if (colors != null) {
	    int n;
	    for (n=0; n<colors.length; n++)
		if (colors[n] == null) break;
	    clone.colors = new Color[n];
	    System.arraycopy(colors, 0, clone.colors, 0, n);
	} else { clone.colors = null; }
	return (ColorableVob) clone;
    }
    public ColorableVob cloneColorReplace(List colors) {
	return cloneColorReplace((Color[])colors.toArray(new Color[0]));
    }
    public ColorableVob cloneColorReplace(Color color) {
	Color[] colors = new Color[1];
	colors[0] = color;
	return cloneColorReplace(colors);
    }

    /** Create a multi-colored clone of the vob by adding new colors
     * in addition to the already existing colors.
     * @param colors An array of colors to show inside the vob. 
     *               Reading of the array will stop at the first null.
     * @return A multi-colored vob.
     */
    public ColorableVob cloneColored(Color[] colors) {
	Color[] oldColors = getColors();
	return cloneColorReplace(concatColorArrays(oldColors, colors));
    }
    public ColorableVob cloneColored(Color color) {
	if (getColors() == null) return cloneColorReplace(color);

	Color[] oldColors = getColors();
	Color[] colors = new Color[oldColors.length + 1];
	for (int i=0; i<oldColors.length; i++)
	    colors[i] = oldColors[i];
	colors[oldColors.length] = color;
	return cloneColorReplace(colors);
    }
    public ColorableVob cloneColored(List colors) {
	Color[] oldColors = getColors();
	Color[] newColors = (Color[])colors.toArray(new Color[0]);
	return cloneColorReplace(concatColorArrays(oldColors, newColors));
    }
    
    /** Concats two Color arrays.
     * @return Return concatenation of two Color arrays. If both
     *         arrays are null, return null.
     */
    private Color[] concatColorArrays(Color[] first, Color[] second) {
	if (first == null && second == null) return null; 

	int firstLength = 0; int secondLength = 0;
	if (first != null) firstLength = first.length;
	if (second != null) secondLength = second.length;
	Color[] merged = new Color[firstLength + secondLength];

	int index = 0;
  	for (int i=0; i<firstLength; i++)
	    merged[index++] = first[i];
  	for (int i=0; i<secondLength; i++)
	    merged[index++] = second[i];
	return merged;
    }

    /** Return the colors of the vob in an array. */
    public Color[] getColors() { 
	if (colors != null) {
	    Color[] c = new Color[colors.length];
	    System.arraycopy(colors, 0, c, 0, colors.length);
	    return c;
	} else return null;
    }
}
