/*
ModelModel.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
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
import org.nongnu.navidoc.util.Obs;

public class ModelModel extends AbstractModel {

    protected Model modelModel;
    protected Model currentModel;

    public ModelModel(Model modelModel) {
	this.modelModel = modelModel;
	currentModel = (Model)modelModel.get();
	if(currentModel != null) currentModel.addObs(this);

	modelModel.addObs(new Obs() { public void chg() {
	    //currentModel.removeObs(this);
	    currentModel = (Model)ModelModel.this.modelModel.get();
	    if(currentModel != null) currentModel.addObs(this);
	    obses.trigger();
	}});
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { modelModel };
    }
    protected Object clone(Object[] params) {
	return new ModelModel((Model)params[0]);
    }

    public Object get() { return currentModel.get(); }
    public int getInt() { return currentModel.getInt(); }
    public float getFloat() { return currentModel.getFloat(); }
    public boolean getBool() { return currentModel.getBool(); }

    public void set(Object value) { currentModel.set(value); }
    public void setInt(int value) { currentModel.setInt(value); }
    public void setFloat(float value) { currentModel.setFloat(value); }
    public void setBool(boolean value) { currentModel.setBool(value); }
}
