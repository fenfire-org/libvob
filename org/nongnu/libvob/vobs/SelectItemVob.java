/*
SelectItemVob.java
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


package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import java.awt.Color;


/** Select item. Item which eats placeable object as one item. 
 * Adapter for plain text is built in.
 * Items can be listed i.e. with SelectListVob
 * @see SelectListVob
 * @see Placeable
 */
public class SelectItemVob {
    private final org.nongnu.libvob.lava.placeable.Placeable item;
    private final Object key;
    private final RectBgVob preBg;
    private final RectBgVob postBg;


    public SelectItemVob(String text, Object key) {
	this(text, GraphicsAPI.getInstance().getTextStyle("sans", 0, 24), key);
    }
    /** Built in adapter for short text. Makes Placeable for the text.
     * @see Placeable
     */
    public SelectItemVob(final String text, final TextStyle style, Object key) {
	this(
	     new org.nongnu.libvob.lava.placeable.Placeable() {
		 final TextVob vob = new TextVob(style, text, false);
		 public void place(VobScene vs, int cs) {
		     float [] size = new float[3];
		     vs.coords.getSqSize(cs, size);
		     int textCS = vs.scaleCS(cs, "CS", size[1], size[1]);
		     vs.put(vob, textCS);
		 }
		 public float getHeight() { return vob.getHeight(1); }
		 public float getWidth() { return vob.getWidth(1); }
	     } , key);
    }
    

    public SelectItemVob(org.nongnu.libvob.lava.placeable.Placeable item, Object key) {
	this(item, key, Color.yellow, Color.blue);
    }

    /** 
     * @param item Placeable item which is to be placed.
     * @param key The object which is found from vs. 
     *             if null, item.toString() is used instead.
     * @param preSelect Color which is used to colorize the item if asked.
     * @param postSelect Color which is used to colorize the item if selected.
     */
    public SelectItemVob(org.nongnu.libvob.lava.placeable.Placeable item, Object key,
			 Color preSelect, Color postSelect
			 ) 
    {
	this.item = item;
	this.key = key;
	this.preBg = new RectBgVob(preSelect);
	this.postBg = new RectBgVob(postSelect);
    }
    
    public Object getKey() { return (key != null ? key :
				     item.toString() ); }

    /** colorizing booleans.
     */
    boolean preSelect = false;
    boolean postSelect = false;

    /** @param frameCS Frame box for the item, 
     *                 usually wider than itemCS. 
     * @param itemCS Box coordinate system for the item.
     */
    public void place(VobScene vs, int frameCS, int itemCS) {
	if (postSelect)
	    vs.put(postBg, frameCS);
	else if (preSelect) 
	    vs.put(preBg, frameCS);
	item.place(vs, itemCS); 
    }
    public float getHeight() { return item.getHeight(); }
    public float getWidth() { return item.getWidth(); }

}
