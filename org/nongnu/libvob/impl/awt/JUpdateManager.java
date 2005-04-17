/*   
JUpdateManager.java
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
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

public class JUpdateManager extends AbstractUpdateManager {
    private static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    public JUpdateManager(Runnable r) { super(r); }

    /** An interface for avoiding the Java event queue, which we have
     * synchronization problems with.
     * This way we can encapsulate a call to the Java2 EventQueue API
     * if we want to.
     */
    public interface EventProcessor {
	void zzProcessEvent(AWTEvent e);
    }

    private static LinkedList eventList = new LinkedList();
    public static void addEvent(EventProcessor proc, AWTEvent e) {
	JUpdateManager m = (JUpdateManager)instance;
	synchronized(m.ordering) {

	    // if events come faster than we can handle, kill some of them.
	    // othervise the ui will freeze down
	    if (eventList.size() > 0) {
		int type = e.getID();
		if (type == MouseEvent.MOUSE_DRAGGED ||
		    type == MouseEvent.MOUSE_MOVED)
		    if (((AWTEvent) eventList.getLast()).getID() == type) {

			// replace...
			eventList.removeLast();
			eventList.removeLast();
			eventList.addLast(proc);
			eventList.addLast(e);
			m.ordering.notifyAll();
			return;
		    }
	    }
	    
	    eventList.add(proc);
	    eventList.add(e);
	    m.ordering.notifyAll();
	    if(dbg) pa("Queue "+e+"; listlen="+eventList.size());
	}
    }

    private static ImageObserver imageobs = new ImageObserver() {
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	    // if((infoflags & ALLBITS) != 0) chg();
	    return true; // continue loading image. XXX Should this be false? We redraw soon anyway!
	}
    };

    /** Get the imageobserver object to give to image painting commands, if
     * the update manager should redraw things when the image becomes available.
     */
    static public ImageObserver getImageObserver() { return imageobs; }

    private static EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();

    static void startJUpdateManager(Runnable r) {
	setInstance(new JUpdateManager(r));
    }

    private Thread t = new Thread(this);
    {
	if(dbg) pa("STARTORDTHREAD");
	t.start();
    }

    private boolean handleEvents_nohang() {
	boolean got = false;
	while(eventList.size() != 0) {
	    got = true;
	    EventProcessor proc;
	    AWTEvent evt;
	    synchronized(ordering) {
		proc = (EventProcessor)eventList.get(0);
		evt = (AWTEvent)eventList.get(1);
		eventList.remove(1);
		eventList.remove(0);
		if(dbg) pa("Unqueue "+evt+"; listlen="+eventList.size());
	    }
	    proc.zzProcessEvent(evt);
	    // XXX wait...
	} 
	return got;
    }

    protected boolean handleEvents(boolean waitForEvent) {
	if(!waitForEvent)
	    return handleEvents_nohang();
	synchronized(ordering) {
	    if(handleEvents_nohang()) return true;
	    try {
		if(dbg) pa("JUpdateManager: going to wait for next event");
		// We wait on 'ordering' since that is also
		// what is notified by AbstractUpdateManager.chg()
		ordering.wait();
	    } catch(InterruptedException e) {
		if(dbg) pa("JUpdateManager: interrupted");
	    }
	    return handleEvents_nohang();
	}
    }

    protected void interruptEventloop()  { 
	synchronized(ordering) {
	    ordering.notifyAll();
	}
    }

    protected void synchronizeToolkit() {
	Toolkit.getDefaultToolkit().sync();
    }
}
