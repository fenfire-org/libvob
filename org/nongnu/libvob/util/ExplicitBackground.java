/*
ExplicitBackground.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.util;

/** An implementation of Background where there is an explicit "performOneTask" method.
 * Useful for, e.g., tests.
 */
public class ExplicitBackground implements Background {
    public static boolean dbg = false;
    final static void pa(String s) { System.out.println(s); }

    // Predictible lifo semantics
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

    /** Take one of the tasks in the queue and run it.
     * This method is only synchronized while it takes the task
     * from the queue so several different threads *can* run tasks
     * simultaneously.
     */
    public void performOneTask() {
	Runnable r;
	synchronized(queue) {
	    r = (Runnable)queue.getAndRemoveLowest();
	}
	if(r == null) 
	    return;
	if(dbg) pa("BG: Going to run "+r);
	r.run();
	if(dbg) pa("BG: Did run "+r);
    }

}
