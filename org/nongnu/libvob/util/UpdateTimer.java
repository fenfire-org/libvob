/*
UpdateTimer.java
 *    
 *    Copyright (c) 2003, : Tuomas J. Lukka
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
 * Written by : Tuomas J. Lukka
 */

package org.nongnu.libvob.util;

/** A timer that runs a given runnable after a certain number
 * of milliseconds. Used in AbstractUpdateManager to
 * implement chgAfter()
 */
public class UpdateTimer {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    private Runnable r;

    boolean updateSet;
    long updateTime;


    private Thread t = new Thread() {

	public void run() {
	    while(true) {
		if(dbg) pa("UpdateLoop: "+UpdateTimer.this+" start");
		try {
		    synchronized(UpdateTimer.this) {
			long time = 0;
			if(updateSet) {
			    long t = System.currentTimeMillis();
			    if(dbg) pa("UpdateLoop: "+UpdateTimer.this+" times " +
					t + " " + updateTime);
			    if(updateTime <= t) {
				if(dbg) pa("UpdateLoop: "+UpdateTimer.this+" run");
				r.run();
				updateSet = false;
				continue;
			    }
			    time = updateTime - t;
			}
			UpdateTimer.this.wait(time);
			if(dbg) pa("UpdateLoop: "+UpdateTimer.this+" Wait over");
		    }
		} catch(InterruptedException e) {
		}
	    }
	}

    };

    /** Create a new UpdateTimer.
     * @param r The runnable this timer will run when its timer expires.
     */
    public UpdateTimer(Runnable r) {
	this.r = r;
	t.setDaemon(true);
	t.setPriority(Thread.NORM_PRIORITY);
	t.start();
    }

    /** Request that the runnable this timer was constructed with
     * should be run at the latest after the given number of milliseconds.
     * Calling updateAfter a second time before the timer has
     * triggered has no effect unless the time specified on the second time
     * is shorter than the time remaining to the triggering specified
     * on the first time.
     */
    public void updateAfter(int millis) {
	long time = System.currentTimeMillis() + millis;
	synchronized(this) {
	    if(dbg) pa("UpdateAfter: "+this+" "+millis+" "+time+" "+updateSet+" "+updateTime);
	    if(!updateSet || updateTime > time) {
		updateSet = true;
		updateTime = time;
		this.notifyAll();
	    }
	}
    }

    /** Tell this timer that the triggered update already happened.
     * If updateAfter has been called and the routine that would be triggered
     * after the interval has been invoked anyway, the updateTimer's future
     * invocation can be cancelled.
     */
    public void updated() {
	updateSet = false; // access to one primitive type atomic
	if(dbg) pa("UpdateAfter: "+this+" updated");
    }

    
}
