/*   
AssignedSizeWatcherLob.java
 *    
 *    Copyright (c) 2004, Tuukka Hastrup.
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Tuukka Hastrup
 */
package org.nongnu.libvob.layout;

public class AssignedSizeWatcherLob extends AbstractMonoLob {

    protected Axis axis;
    protected Model sizeModel;

    public AssignedSizeWatcherLob(Axis axis, Lob content, Model sizeModel) {
	super(content);
	this.axis = axis;
	this.sizeModel = sizeModel;
	this.sizeModel.setFloat(Float.NaN);
    }

    public void setSize(float width, float height) {
	sizeModel.setFloat(axis.coord(width, height));
	super.setSize(width, height);
    }

    protected Replaceable[] getParams() { 
	return new Replaceable[] { content, sizeModel };
    }
    protected Object clone(Object[] params) {
	return new AssignedSizeWatcherLob(axis, (Lob)params[0], 
					  (Model)params[1]);
    }
}
