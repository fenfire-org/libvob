/*
Stamp.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** Modify a content lob so that can have more than one parent 
 *  (setSize() and setParent() are ignored). The idea is that it turns
 *  a lob into a "stamp" of which many "copies" can be created,
 *  each with a different parent. (Of course, no copies are actually
 *  created.)
 */
public class Stamp extends AbstractLob {

    protected Lob content;

    public Stamp(Lob content) {
	this.content = content;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content };
    }
    protected Object clone(Object[] params) {
	return new Stamp((Lob)params[0]);
    }


    public float getMinSize(Axis axis) {
	return content.getMinSize(axis);
    }
    public float getNatSize(Axis axis) {
	return content.getNatSize(axis);
    }
    public float getMaxSize(Axis axis) {
	return content.getMaxSize(axis);
    }


    public boolean key(String key) {
	return content.key(key);
    }
    public boolean mouse(VobMouseEvent e, float x, float y) {
	return content.mouse(e, x, y);
    }
    public List getFocusableLobs() {
	return content.getFocusableLobs();
    }
    public boolean isLargerThanItSeems() {
	return content.isLargerThanItSeems();
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	content.render(scene, into, matchingParent, w, h, d, visible);
    }
}
