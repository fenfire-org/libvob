/*
Group.java
 *    
 *    Copyright (c) 2003, Benja Fallenstein
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
package org.nongnu.libvob.lava;
import java.awt.Graphics;
import java.util.*;

/** A vob that contains a list of other vobs.
 *  The vobs are all rendered with the same
 *  RenderTraversal settings, i.e. transform,
 *  color etc.
 */
public class Group implements Sequence {

    protected final List content = new ArrayList();
    protected int size = 1;

    public Sequence cloneEmpty() {
	return new Group();
    }

    public int add(Vob v) {
	content.add(v);
	int index = size;
	size += v.getCount();
	return index;
    }

    public float getSize(Vob.Axis axis) {
	float size = 0;
	for(Iterator i=content.iterator(); i.hasNext();) {
	    float s = ((Vob)i.next()).getSize(axis);
	    if(s > size) size = s;
	}
	return size;
    }

    public Sequence close() {
	return this;
    }

    public int getCount() {
	return size;
    }

    public void render(Graphics g, RenderTraversal t) {
	for(Iterator i=content.iterator(); i.hasNext();) {
	    Vob v = (Vob)i.next();
	    v.render(g, t);
	}
    }

    public Vob getVob(int index) {
	if(index == 0) return this;

	int pos = 1;
	for(Iterator i=content.iterator(); i.hasNext();) {
	    Vob v = (Vob)i.next();
	    if(pos + v.getCount() > index)
		return v.getVob(index - pos);
	}
	
	throw new IndexOutOfBoundsException(""+index);
    }

    public RenderTraversal transform(RenderTraversal t, int index) {
	if(index == 0) return t;

	int pos = 1;
	for(Iterator i=content.iterator(); i.hasNext();) {
	    Vob v = (Vob)i.next();
	    if(pos + v.getCount() > index) {
		v.transform(t, index - pos);
		return t;
	    }
	    pos += v.getCount();
	}
	
	throw new IndexOutOfBoundsException(""+index);
    }
}
