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

    private static LocalContext.Variable 
	CLICK_SCENE = new LocalContext.Variable(),
	CLICK_CS = new LocalContext.Variable();

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

    public static VobScene getClickScene() {
	VobScene sc = (VobScene)CLICK_SCENE.getValue();
	if(sc == null)
	    throw new IllegalStateException("not in a ClickController callback");
	return sc;
    }

    public static int getClickCS() {
	Integer i = (Integer)CLICK_CS.getValue();
	if(i == null)
	    throw new IllegalStateException("not in a ClickController callback");
	return i.intValue();
    }

    public Lob wrap(Lob l) {
	return newInstance(l, button, eventType, action);
    }

    public boolean mouse(VobMouseEvent e, VobScene sc, int cs, 
			 float x, float y) {
	if(e.getType() == eventType && e.getButton() == button) {

	    LocalContext.enter();
	    try {
		CLICK_SCENE.setValue(sc);
		CLICK_CS.setValue(new Integer(cs));
		action.run();
	    } finally {
		LocalContext.exit();
	    }

	    delegate.mouse(e, sc, cs, x, y);
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
