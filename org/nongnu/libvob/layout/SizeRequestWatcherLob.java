/*   
SizeRequestWatcherLob.java
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

public class SizeRequestWatcherLob extends AbstractMonoLob {

    protected Axis axis;
    protected Model minModel, natModel, maxModel;

    public void chg() {
	updateModels();
	super.chg();
    }

    public SizeRequestWatcherLob(Axis axis, Lob content, Model minModel, 
				 Model natModel, Model maxModel) {
	super(content);
	this.axis = axis;
	this.minModel = minModel;
	this.natModel = natModel;
	this.maxModel = maxModel;
	updateModels();
    }

    protected void updateModels() {
	if (minModel != null) minModel.setFloat(content.getMinSize(axis));
	if (natModel != null) {
	    natModel.setFloat(content.getNatSize(axis));
	    System.out.println("nat update: "+content.getNatSize(axis));
	} else
	    System.out.println("nat null!");
	if (maxModel != null) maxModel.setFloat(content.getMaxSize(axis));
    }

    protected Replaceable[] getParams() { 
	return new Replaceable[] { content, minModel, natModel, maxModel };
    }
    protected Object clone(Object[] params) {
	System.out.println("clone");
	return new SizeRequestWatcherLob(axis, (Lob)params[0], 
			 (Model)params[1], (Model)params[2], (Model)params[3]);
    }
}
