/*   
ClickController.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein.
 *
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
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.lob.*;
import javolution.realtime.*;

public class ClickController extends AbstractDelegateLob {

    protected int button, eventType;
    protected Action action;

    private ClickController() {}

    public static ClickController newInstance(Lob content, int button, 
					      Action action) {
	return newInstance(content, button, VobMouseEvent.MOUSE_CLICKED, action);
    }

    public static ClickController newInstance(Lob content, int button, 
					      int eventType, Action action) {
	ClickController c = (ClickController)FACTORY.object();
	c.delegate = content;
	c.button = button;
	c.eventType = eventType;
	c.action = action;
	return c;
    }

    public Lob layout(float w, float h) {
	return newInstance(delegate.layout(w, h), button, eventType, action);
    }

    public boolean mouse(VobMouseEvent e, VobScene sc, int cs, 
			 float x, float y) {
	if(e.getType() == eventType && e.getButton() == button) {

	    action.run();
	    AbstractUpdateManager.chg();
	    return true;
	} else {
	    return delegate.mouse(e, sc, cs, x, y);
	}
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new ClickController();
	    }
	};
}
