// (c): Matti J. Katila

package org.nongnu.libvob.impl;
import java.lang.*;
import java.util.*;

/** A scheduler for loading things at background with priority order.
 *  The scheduler is intended to use with image loading.
 *  It's singleton object.
 */
public class Scheduler extends Thread {
    static public boolean dbg = false;
    static private void p(String s) { System.out.println("Scheduler:: "+s); }


    /* INTERFACE
     */
    
    /** Sets a priority for key binding which may be binded later. 
     *  Zero (0) is highest priority, one (1) second highest 
     *  priority and so forth.
     */
    public void setPriority(Object key, int priority) {
	realSetPrior(key, priority);
    }

    /** Start scheduling for a particular thread with key.
     *  Key shall be set or Error is thrown.
     */
    public void schedule(Thread t, Object key) {
	realSchedule(t, key);
    }


    int timeTick = 50; //milliseconds



    /* How does it work?
     * =================
     *
     *     0   1   2   3   4   5   priority
     * t   ->
     * i   ---->
     * m   -------->
     * e   ------------>
     *\|/  ---------------->
     *  
     *  - The lift goes to priority and then jumps back to zero.
     *    First task in priority list is executed for time tick.
     * 
     *  - The task list in current priority is rotated.
     *
     *  - After all priorities have been run start from 
     *    zero with lift path 1.
     * 
     */


    private Scheduler() {
	this.start();
    }
    public void run() {

	while (true) {
	    boolean nothing = true;
	    try {

		for (int currentPathLength=1; 
		     currentPathLength < priorities.size(); 
		     currentPathLength++) {

		    for (int i=0; i<=currentPathLength; i++) {
			if (dbg) p("currentPrior: "+i+",  len: "+currentPathLength);
			int currentPriority = i;

			synchronized(priorities) {
			    LinkedList tasks = (LinkedList) 
				priorities.get(currentPriority);
			    
			    if (tasks.size() > 0) {
				nothing = false;
				Task task = (Task) tasks.removeFirst();
				Thread t = (Thread) task.t;
				if (dbg) p("thread: "+t);

				t.resume();
				//System.out.println(t.toString());
				sleep(timeTick);
				if (dbg) p("woke up");
				t.suspend();


				if (t.isAlive()) {
				    if (dbg) p("t is alive "+t.isAlive());
				    // this can produce a dead-lock 
				    // state if the thread is using 
				    // system resources!
				    
				    tasks.addLast(task);
				}
			    }
			}
		    }		    
		}
		    
		if (nothing)
		    sleep(8 * 1000);
	    } catch (InterruptedException e) {
		p("Scheduler interrupted...");
	    } catch (Error e) {
		p("Error or exception got! "+e.getMessage());
		e.printStackTrace();
	    }
	}
    }
    private static Scheduler instance = null;
    public static Scheduler getInstance() {
	if (instance == null)
	    instance = new Scheduler();
	return instance;
    }


    class Task {
	Object k;
	Thread t;
	Task(Thread t, Object k) { this.t=t; this.k=k; }
    }


    /** Assumes that there exist LinkedLists in the array.
     */
    List priorities = new ArrayList();
    Map key2prior = new HashMap();
    protected void realSchedule(Thread t, Object k) {
	if (!key2prior.containsKey(k))
	    throw new Error("No priority set for key: "+k);

	t.suspend();

	synchronized(priorities) {
	    if (dbg) p("start: "+t);
	    Integer ii = (Integer) key2prior.get(k);
	    ((LinkedList)priorities.get(ii.intValue())).addLast(
		new Task(t,k));
	}
    }

    protected void realSetPrior(Object key, int priority) {
	synchronized(priorities) {

	// fullfil the assume of linked lists.
	for (int i=0; i<=priority; i++) {
	    if (priorities.size() <= i) 
		priorities.add(i, new LinkedList());
	}

	// if we already have the key we want to change the
	// priority of those task with that key.
	if (key2prior.containsKey(key)) {
	    Integer ii = (Integer) key2prior.get(key);
	    if (ii.intValue() == priority) return;

	    LinkedList src = (LinkedList) priorities.get(ii.intValue());
	    LinkedList dest = (LinkedList) priorities.get(priority);

	    for (ListIterator i = src.listIterator(0); i.hasNext();) {
		Task task = (Task) i.next();
		if (task.k.equals(key)) {
		    i.remove();
		    dest.addLast(task);
		}
	    }
	    
	}
	else // or we want to just make priority reserve.
	    key2prior.put(key, new Integer(priority));
	}
    }
    
    

}
