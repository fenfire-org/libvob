/*   
GLUpdateManager.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
 *
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Tuomas Lukka and Rauli Ruohonen
 */
package org.nongnu.libvob.impl.gl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import java.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.AWTEventMulticaster;


public class GLUpdateManager extends AbstractUpdateManager {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println("GLUpdateManager: "+s); }

    // can be switched from jython..
    public static boolean demoMode = false;

    public GLUpdateManager(Runnable r) { super(r); }

    static void startGLUpdateManager(Runnable r) {
	setInstance(new GLUpdateManager(r));
    }

    /** The OpenGL thread for OpenGL update manager.
     *  Starts AbstractUpdateManager's, which implements Runnable,
     *  event and drawing loop.
     */
    private Thread t = new Thread(this);
    {
	if(dbg) pa("STARTGLTHREAD");
	t.start();
    }

    protected boolean handleEvents(boolean waitForEvent) {
	if(dbg) pa("HandleEvents "+waitForEvent);
	return GL.eventLoop(waitForEvent);
    }

    /* for demo-events at the end of doIdle() */
    private ActionListener actionListener = null;
    public synchronized void addActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.add(actionListener, l);
    }
    private void processEvent(ActionEvent e) {
        if (actionListener != null) {
            actionListener.actionPerformed(e);
        }         
    }
    private int freeMemoryCountdown = 50;

    protected boolean doIdle() {
	if(dbg) pa("GLUpdatemanager doIdle");
	if(demoMode) {
            processEvent(
                new ActionEvent(this, ActionEvent.ACTION_LAST, "DEMO_EVENT"));
            freeMemoryCountdown--;
           
            if (freeMemoryCountdown < 0) {
                GL.freeQueue();
                freeMemoryCountdown = 50;
            }
        }

	if(super.doIdle()) {
	    if(dbg) pa("super.doIdle true");
	    return true;
	}
	GL.freeQueue();
	if(dbg) pa("doIdle false");
	return false;
    }

    protected void interruptEventloop() {
	if(dbg) pa("GLUpdatemanager interrupt eventloop");
	GL.interruptEventloop();
	if(dbg) pa("GLUpdatemanager interrupt done");
    }

    protected void synchronizeToolkit() { }
}
