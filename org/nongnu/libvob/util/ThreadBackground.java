/*   
ThreadBackground.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Tuomas Lukka
 */


package org.nongnu.libvob.util;

/** An implementation of Background using another thread.
 * Implementation Note: 
 * XXX should Reimplement using ExplicitBackground as a base
 */

public class ThreadBackground implements Background {
    public static boolean dbg = false;
    final static void pa(String s) { System.out.println(s); }

    private PriorityQueue queue = new LifoPriorityQueue();

    public void addTask(Runnable r, float priority) {
	synchronized(queue) {
	    queue.add(r, priority);
	    queue.notifyAll();
	}
    }

    public void removeTask(Runnable r) {
	synchronized(queue) {
	    queue.remove(r);
	}
    }

    private Thread bgThread = new Thread() {
	public void run() {
	    try {
		while(true) {
		    Runnable r;
		    synchronized(queue) {
			r = (Runnable)queue.getAndRemoveLowest();
			if(r == null) {
			    queue.wait();
			    continue;
			}
		    }
		    if(dbg) pa("BG: Going to run "+r);
		    r.run();
		    if(dbg) pa("BG: Did run "+r);
		}
	    } catch(InterruptedException e) {
		pa("BG INTERRUPTED");
		throw new Error("Interrupted");
	    }
	}
    };

    {
	bgThread.setDaemon(true);
	bgThread.setPriority(Thread.MIN_PRIORITY);
	bgThread.start();
    }

    private static ThreadBackground defaultInstance = null;

    /** Get the default instance of ThreadBackground - using this
     * will result in lower load and more sequential processing.
     */
    public static ThreadBackground getDefaultInstance() { 
	if(defaultInstance == null)
	    defaultInstance = new ThreadBackground();
	return defaultInstance;
    }

}

