/*
SelectListVob.java
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

package org.nongnu.libvob.vobs.lava;
import org.nongnu.libvob.vobs.*;
import org.nongnu.libvob.*;
import java.awt.*;

/** Immutable class to contain items in list. 
 * This Vob is usefull for listing items. Can be used i.e., 
 * to show a list after mouse click.
 * @see SelectItemVob
 */
public class SelectListVob extends AbstractVob {
    public static boolean dbg = false;
    private static void p(String s) { System.out.println("SelectListVob:: "+s); }

    private final SelectItemVob[] items;
    private final RectBgVob bg;

    public SelectListVob(SelectItemVob[] items) {
	this(items, new Color(.9f, .9f, 1));
    }
    /** @param items the items in list.
     * @param bgColor background color of list 
     */
    public SelectListVob(SelectItemVob[] items, Color bgColor) {
	this.items = items;
	this.bg = new RectBgVob(bgColor); 
    }


    public void render(Graphics g, boolean fast,
		       Vob.RenderInfo info1, Vob.RenderInfo info2) {
	throw new Error("Not implemented");
    }

    
    private float xRatio = 0, yRatio = 0;
    public int putGL(VobScene vs, int into) {
	vs.put(bg, into);

	float [] size = new float[3];
	vs.coords.getSqSize(into, size);
	if (dbg) p("size:"+size[0]+","+size[1]);
	
	float x = 0, y = 0;
	xRatio = size[0]/getWidth();
	yRatio = size[1]/getHeight();

	for (int i=0; i<items.length; i++) {
	    SelectItemVob item = items[i];
	    int itemFrame =
		vs.orthoCS(into, item.getKey(), 0, x,y,
			   xRatio * getWidth(), yRatio * item.getHeight());
	    vs.activate(itemFrame);

	    int itemCS =
		vs.orthoBoxCS(into, item.getKey().toString()+"_item", 0, x,y,  1,1,
			      xRatio * item.getWidth(), yRatio * item.getHeight());
	    item.place(vs, AbstractSelectVob.getControl(vs, item), 
		itemFrame, itemCS);
	    
	    y += yRatio * item.getHeight();
	}
	
	return 0;
    }


    public float getWidth() {
	float max = 0;
	for (int i=0; i<items.length; i++) {
	    max = Math.max(max, items[i].getWidth());
	}
	return max;
    }

    public float getHeight() {
	float height = 0;
	for (int i=0; i<items.length; i++) {
	    height += items[i].getHeight();
	}
	return height;
    }

    /** Before slecting the item - colorize the thing like 
     * saing: "You are being to hit me!"
     */
    public void preSelect(VobScene vs, float x, float y) {
	cleanSelects(vs);
	if (miss(x,y)) return;
	AbstractSelectVob.setControl(vs, getItem(x,y), 
			 AbstractSelectVob.preState);
    }

    /** After slecting the item - colorize the thing like 
     * saing: "Ouh, you really hit me and that hurt."
     */
    public void postSelect(VobScene vs, float x, float y) {
	cleanSelects(vs);
	if (miss(x,y)) return;
	p("item: "+getItem(x,y));
	AbstractSelectVob.setControl(vs, getItem(x,y), 
			 AbstractSelectVob.postState);
    }


    private SelectItemVob getItem(float x, float y) {
	float y_tmp = 0;
	for (int i=0; i<items.length; i++) {
	    if (y >= y_tmp   && 
		y < y_tmp + yRatio * items[i].getHeight()) 
		    return items[i];
	    y_tmp += items[i].getHeight() * yRatio;
	}
	return null;
    }

    private boolean miss(float x, float y) {
	if (x < 0 || x > xRatio * getWidth() ||
	    y < 0 || y > yRatio * getHeight()) {
	    if (dbg) p("miss");
	    return true;
	} return false;
    }

    private void cleanSelects(VobScene vs) {
	for (int i=0; i<items.length; i++) {
	    AbstractSelectVob.setControl(vs, items[i], 
				      AbstractSelectVob.normalState);
	}
    }

}
