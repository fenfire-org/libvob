/*   
ClickController.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;

public class ClickController extends AbstractMonoLob {

    protected int button, eventType;
    protected Action action;

    protected float w, h;

    public ClickController(Lob content, int button, Action action) {
	this(content, button, VobMouseEvent.MOUSE_CLICKED, action);
    }

    public ClickController(Lob content, int button, int eventType,
			   Action action) {
	super(content);
	this.button = button;
	this.eventType = eventType;
	this.action = action;
    }

    public int getButton() { return button; }
    public Action getAction() { return action; }
    
    public void setButton(int button) { this.button = button; }
    public void setAction(Action action) { this.action = action; }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, action };
    }
    protected Object clone(Object[] params) { 
	return new ClickController((Lob)params[0], button, eventType,
				   (Action)params[1]);
    }

    public void setSize(float w, float h) {
	super.setSize(w, h);
	this.w = w;
	this.h = h;
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	if(x >= 0 && y >= 0 && x < w && y < h &&
	   e.getType() == eventType && e.getButton() == button) {

	    action.run();
	    AbstractUpdateManager.chg();
	    return true;
	} else {
	    return super.mouse(e, x, y);
	}
    }
}
