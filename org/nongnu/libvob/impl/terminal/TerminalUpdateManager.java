// (c): Matti J. Katila

package org.nongnu.libvob.impl.terminal;

import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** A singleton class update manager for terminal.
 */
public class TerminalUpdateManager extends AbstractUpdateManager {
    private static boolean dbg = true;
    private static void p(String s) { System.out.println("TermUpdateMgr:: "+s); }

    static private TerminalUpdateManager mgr = null;
    private UnixTerminal terminal = null;
    
    private TerminalUpdateManager(Runnable r) { super(r); }

    //static public TerminalUpdateManager getInstance() { return mgr; }

    static void startUpdateManager(Runnable r) {
	if (mgr != null) 
	    throw new Error("Only one instance of terminal update manager allowed!");
	mgr = new TerminalUpdateManager(r);
	if(dbg) p("STARTORDTHREAD");
	new Thread(mgr).start();
    }

    public void set(UnixTerminal term) { this.terminal = term; }

    // return true if there were events..
    protected boolean handleEvents(boolean waitForEvent) {
	if (dbg) p("handleEvents: "+waitForEvent);
	if (terminal == null || !waitForEvent)
	    return false;

	boolean ret = terminal.hasEvents();
	while (terminal.hasEvents())
	{
	    p("key: "+((int)terminal.popEvent()));
	}
	return ret;
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
