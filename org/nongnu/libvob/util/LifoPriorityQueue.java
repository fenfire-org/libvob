/*
LifoPriorityQueue.java
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
import java.util.*;

/** A simple implementation of a priority queue, but which uses LIFO
 * for equal-priority rules.
 * This class guarantees that if several jobs have the same
 * priority, the one for which add() was called *latest* will
 * be run.
 * This is a good behaviour for superlazy caches where the older
 * tasks may be already irrelevant.
 * <p>
 * This current implementation is not the most efficient 
 * possible one,
 * by far.
 * <p>
 * An integer is used for counting so there will be some
 * wrap-around effects every 4294967296 jobs.
 */
public class LifoPriorityQueue implements PriorityQueue {
    static public boolean dbg = false;
    private void p(String s) { 
	System.out.println(""+this+": "+s);
    }

    private int counter = 0;

    /** The priority of a job.
     * The priority is the explicit float priority
     * and the value of the counter when the priority
     * was last updated.
     */
    class Priority {
	float priority;
	int count;

	Priority(float p) {
	    this.priority = p;
	    tick(p);
	}
	void tick(float np) {
	    if(np < this.priority) this.priority = np;
	    this.count = counter++;
	    if(dbg) p("Tick: "+this+" "+priority+" "+count);
	}
    }

    /** Compare two objects using the jobPriority value
     * as primary and hash code as secondary code.
     */
    private class Comp implements Comparator {
	public int compare(Object o1, Object o2) {
	    Priority p1 = (Priority)jobPriority.get(o1);
	    Priority p2 = (Priority)jobPriority.get(o2);

	    // First: least priority wins
	    float f1 = p1.priority;
	    float f2 = p2.priority;
	    if(f1 < f2) return -1;
	    if(f1 > f2) return 1;

	    // Second: greatest count wins
	    int i1 = p1.count;
	    int i2 = p2.count;
	    if(i1 > i2) return -1;
	    if(i1 < i2) return 1;

	    // Third: just disambiguate.
	    i1 = o1.hashCode();
	    i2 = o2.hashCode();
	    if(i1 < i2) return -1;
	    if(i1 > i2) return 1;
	    return 0;
	}
    }

    /** Job object to Float (the priority).
     */
    private Map jobPriority = new HashMap();

    /** Set of jobs, to be compared by their priorities.
     */
    private TreeSet jobs = new TreeSet(new Comp());


    public void add(Object job, float priority) {
	Priority prevprio = (Priority)jobPriority.get(job);
	if(dbg) p("Add: "+job+" "+priority+" "+prevprio);
	if(prevprio != null) {
	    // Now, in order not to corrupt the TreeSet, 
	    // we must FIRST remove the element, then change
	    // the priority and then re-add it after the priority
	    // has been changed.
	    jobs.remove(job);

	    prevprio.tick(priority);
	} else {
	    prevprio = new Priority(priority);
	}
	jobPriority.put(job, prevprio);
	jobs.add(job);
    }

    public void remove(Object job) {
	// Can't just remove as it wouldn't have an entry
	// there.
	if(dbg) p("Remove: "+job);
	if(jobPriority.containsKey(job)) {
	    if(dbg) p("Removed really: "+job);
	    jobs.remove(job);
	    jobPriority.remove(job);
	}
    }

    public Object getAndRemoveLowest() {
	if(dbg) p("GetAndRemove"+jobs.isEmpty());
	if(jobs.isEmpty()) return null;
	Object j = jobs.first();
	if(dbg) {
	    Priority prio = (Priority)jobPriority.get(j);
	    p("Returning "+j+" "+prio.priority+" "+prio.count);
	}
	jobs.remove(j);
	jobPriority.remove(j);
	return j;
    }
}
