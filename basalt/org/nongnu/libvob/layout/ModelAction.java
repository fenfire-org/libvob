/*
ModelAction.java
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

public class ModelAction extends AbstractAction {

    protected Model actionModel;

    public ModelAction(Model actionModel) {
	this.actionModel = actionModel;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { actionModel };
    }
    protected Object clone(Object[] params) {
	return new ModelAction((Model)params[0]);
    }

    public void run() {
	Action action = (Action)actionModel.get();
	action.run();
    }
}
