/*
SimpleBreaker.java
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

public class SimpleBreaker extends AbstractMonoVob
    implements Sequence {

    /** The sequence lines are put into. */
    protected Sequence lineHolder;

    /** The width of each line. */
    protected float lineWidth;

    /** The axis along which to do breaking:
     *  X is for left-to-right line, Y for 
     *  top-to-bottom lines. 
     */
    protected Vob.Axis axis;


    /** The current line. */
    protected Box line;

    /** The width of the vobs currently put into the current line. */
    protected float currentWidth;


    public SimpleBreaker(Sequence lineHolder,
			 float lineWidth,
			 Vob.Axis axis) {
	super(lineHolder);

	this.lineHolder = lineHolder;
	this.lineWidth = lineWidth;
	this.axis = axis;

	newline();
    }

    protected void newline() {
	if(line != null) lineHolder.add(line);
	line = new Box(axis);
	currentWidth = 0;
    }

    public Sequence cloneEmpty() {
	return new SimpleBreaker(lineHolder.cloneEmpty(),
				 lineWidth, axis);
    }

    public Sequence close() {
	lineHolder.add(line);
	lineHolder.close();
	return this;
    }

    public int add(Vob v) {
	float size = v.getSize(axis);
	if(currentWidth + size > lineWidth)
	    newline();

	currentWidth += size;
	line.add(v);
	return -1; // XXX!!!
    }

    public void render(Graphics g, RenderTraversal t) {
	content.render(g, t);
    }
}
