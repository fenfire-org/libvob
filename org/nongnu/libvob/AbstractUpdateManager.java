/*
UpdateManager.java
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

package org.nongnu.libvob;
import org.nongnu.libvob.util.UpdateTimer;
import org.nongnu.libvob.util.PriorityQueue;
import org.nongnu.libvob.util.DumbPriorityQueue;
import org.nongnu.libvob.util.LifoPriorityQueue;
import org.nongnu.navidoc.util.Obs;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.image.*;

/** A single global instance to manage the updating of windows.
 * The windows are set in a priority order so that even if the user is
 * moving fast, he will get an immediate response through the less important
 * windows not being updated at each step.
 * <p>
 * Currently, Fenfire is single-threaded very strongly, so you can't
 * allow anything to happen while the UpdateManager is working.
 * <p>
 * This class implements Background to allow background tasks to be run
 * in the OpenGL-using thread with the common API.
 */

public abstract class AbstractUpdateManager implements Runnable, org.nongnu.libvob.util.Background {
    public static boolean dbg = false;
    private static void pa(String s) { 
	System.out.println("AbstractUpdateManager: "+s); }

    protected static AbstractUpdateManager instance = null;
    protected static void setInstance(AbstractUpdateManager in) {
	if(instance != null)
	    throw new Error("Trying to start two updateManagers");
	if(dbg) pa("Updatemanager "+in);
	instance = in;
    }
    public static AbstractUpdateManager getInstance() {
	return instance;
    }

    public static Obs obs = new Obs() {
	    public void chg() { AbstractUpdateManager.chg(); }
	};

    private Runnable initRunnable;

    protected AbstractUpdateManager(Runnable r) {
	if(dbg) pa("Starting updateManager: "+this);
	initRunnable = r;
    }

    /** The order of windows, from the most important to the least.
     */
    protected ArrayList ordering = new ArrayList();

    /** Whether an update is currently in progress, through 
     * a window being painted. 
     */
    private boolean updating;
    /** XXX What a what??
     */
    private int disabled;

    /** If true, all windows have incorrect end state, and generateEndState()
     * should be called for all of them at appropriate times.
     */
    private boolean restartUpd = false;
    public static final float jumpFract = 0.00000000001f;
 
    /** A window animation is performed in. It is a mutable pair of states:
     * (start, end). The end state doesn't initially exist.
     * @diagram screen MP %t.c = (500,500);
     */
    public interface Window {
	/** Creates an end state, and returns true iff the time given
	 * for generation was sufficient for the given level of detail.
	 * If false is returned, lower level of detail would have produced
	 * a better result.
	 * @diagram screen
	 */
	boolean generateEndState(int millis, float lod);
	/* Returns true iff animation is wanted. The end state is
	 * sometimes so different from the start state that animation isn't
	 * useful, and this function returns false.
	 * @diagram screen
	 */
	boolean animUseful();
	/** Render a state between start and end (interpolation), with the
	 * specified level of detail. The value
	 * of fract is usually in [0, 1], but extrapolation
	 * is also allowed. 
	 * 0.5 should be assumed to be the default lod. If the end state
	 * doesn't exist, may crash.
	 * @param showFinal Whether to show (unanimated) the parts
	 *  	of the final view that are not animated.
	 *  	This can be used to make the effect of going slightly
	 *  	too far - coming back faster, not making the user wait
	 *  	for the very end of the animation to see everything.
	 * @diagram screen
	 */
	void renderAnim(float fract, float lod, boolean showFinal);
	/** After this renderAnim(0, ...) would do the same as
	 * renderAnim(fract, ...) did before the call, if the end state wasn't
	 * destroyed (which it is). Used when the end state changes during
	 * animation, so that the animation continues from the state it
	 * reached, instead of jumping. If the end state doesn't exist, may
	 * crash.
	 * @diagram screen
	 */
	void changeStartState(float fract);
	/** Same as renderAnim(0, lod), except that it's not allowed to crash
	 * even if the end state doesn't exist. The parameter
	 * lod may be interpreted
	 * differently to show things that are not important when animating
	 * (such as connections).
	 * @diagram screen
	 */
	void renderStill(float lod);
	/** Whether the endstate has been generated.
	 * @diagram screen
	 */
	boolean hasEndState();
	/** Same as changeStartState(1), except that the end state doesn't
	 * exist afterwards. If there is no end state, must be a no-op.
	 * @diagram screen
	 */
	void endAnimation();
    }

    /** Call the method of Window. OpenGL needs to wrap this
     * to put it in context.
     */
    protected void callGenerateEnd(Window w, int millis, float lod) {
	w.generateEndState(millis, lod);
    }

    public static void addWindow(Window w) {
	if(instance == null)
	    throw new Error("Trying to add window; no updatemanager instance");
	if(dbg) pa("UpdManager: Adding window "+w+" into \n"+instance.ordering);
	if (instance.ordering.contains(w))
	    throw new IllegalArgumentException("Window already added!");
	instance.ordering.add(w);
    }
    public static void rmWindow(Window w) { instance.ordering.remove(w); }

    /** Set the window that has the privilege of animating the next
     * update. May not be set to null; use setNoAnimation for that.
     */
    public static void prioritize(Window w) { instance.prioritizeImpl(w); }
    protected void prioritizeImpl(Window w) {
	synchronized(ordering) {
	    if (ordering.size() <= 0) return;
	    if(dbg) pa("Setslow: "+w + " cur: "+ordering.get(0));
	    if(w == null) 
		throw new NullPointerException("Null window for AbstractUpdateManager.prioritizeImpl");
	    if(ordering.get(0) != w) {
		if(!ordering.contains(w)) {
		    if(dbg) pa("Tried to set a fast window that isn't registered "+w);
		    return;
		}
		ordering.remove(w);
		ordering.add(0, w);
	    }
	}
    }

    static public int defaultAnimationTime = 1000;
    private int animationtime = defaultAnimationTime;

    public static void setNoAnimation() { 
	instance.setAnimationTimeImpl(0); 
    }
    public static  void setAnimationTime(int millis) {
	instance.setAnimationTimeImpl(millis); 
    }
    protected void setAnimationTimeImpl(int millis) {
	animationtime = millis;
    }
    public static int getAnimationTime() {
	return instance.getAnimationTimeImpl(); 
    }
    protected int getAnimationTimeImpl() {
	return animationtime;
    }

    private static UpdateTimer chgAfterTimer = new UpdateTimer(new Runnable() {
	public void run() {
	    AbstractUpdateManager.chg();
	}
    });

    /** We want to update the screen, but not start immediately in order to do
     * some more background processing.
     * For example, when MipzipLoader is working, we want to update the screen when new information
     * has been loaded, but not right away.
     */
    public static void chgAfter(int millis) {
	chgAfterTimer.updateAfter(millis);
    }

    private static void resetChgAfter() {
	chgAfterTimer.updated();
    }

    /** Called by a space to inform that some cells have been 
     * changed and all windows should be updated.
     */
    public static void chg() { instance.chgImpl(); }
    protected void chgImpl() {
	if(restartUpd) return;
	if(dbg) pa("UPDMANAGER CHG - NO RESTARTUPD");
	synchronized(ordering) {
	    if(dbg) pa("UPDMANAGER CHG - IN SYNCHRONIZED");
	    restartUpd = true;
	    ordering.notifyAll();
	    if(dbg) pa("UPDMANAGER CHG - NOTIFIED");
	    interruptEventloop();
	}
	
	if(dbg) pa("UPDMANAGER CHGOUT");
    }

    /** Disable all updating of windows.
     */
    public static void freeze() {
	if(dbg) pa("Updatemanager freeze");
	instance.interruptEventloop();
	synchronized(instance.ordering) { instance.disabled++; }
	if(dbg) pa("Updatemanager frozen "+instance.disabled);
    }
    /** Enable again the updating of windows. 
     * Unlike freeze(), the effect should be instantaneous.
     */
    public static void thaw() {
	if(dbg) pa("Updatemanager thaw");
	synchronized(instance.ordering) {
	    if(instance.disabled<=0)
		throw new Error("thaw() without matching freeze()!");
	    if(--instance.disabled==0) instance.ordering.notifyAll();
	}
	if(dbg) pa("Updatemanager thawed "+instance.disabled);
    }

    /** An animation main curve calculator.
     */
    public interface FractCalculator {
	float OVER = -100f;
	void eventAt(long time);
	boolean isOver(long time);
	void callRender(long time, Window w);
    }

    static public class LinearCalculator implements FractCalculator {
	float switchFract;
	public LinearCalculator(float switchFract) {
	    this.switchFract = switchFract;
	}
	public LinearCalculator() {
	    switchFract = .5f;
	}
	int millis = 0; // see defaultAnimationTime
	protected long startTime = 0;
	public void eventAt(long time) {
	    startTime = time;
	    millis = getAnimationTime();
	}
	private float fract(long time) {
	    float x = (time-startTime)/((float)millis);
	    if(x >= 1) return 1;
	    return x;
	}
	public boolean isOver(long time) {
	    float x = (time-startTime)/((float)millis);
	    return x >= 1;
	}

	public void callRender(long time, Window w) {
	    float f = fract(time);
	    w.renderAnim(f, 1, false /*f >= switchFract*/);
	}
    }
    static public class SimpleCalculator implements FractCalculator {
	/**
	 * Multiplicates x in Math.cos(); determines the amount of
	 * 'waves' when interpolating between two view states.
	 */
	protected static float n = 0.4f;
	/**
	 * Multiplicates x in Math.exp(); determines how fast animation
	 * slows down, how fast it reduces 'waving'.
	 */
	protected static float r = 2f;

	int millis = 0; // see defaultAnimationTime
	protected long startTime = 0;
	public void eventAt(long time) {
	    startTime = time;
	    millis = getAnimationTime();
	}
	private float fract(long time) {
	    float x = (time-startTime)/((float)millis);
	    x = x + x*x;
	    //x = (float)(1-Math.cos(2*Math.PI*n*x)*Math.pow(1-x, r));
	    float y = (float)(1-Math.cos(2*Math.PI*n*x)*Math.exp(-x*r));
	    return y;
	}
	public boolean isOver(long time) {
	    float x = (time-startTime)/((float)millis);
	    if (-(x + x*x)*r < Math.log(0.02)) {
		if (Math.abs(time-startTime-millis) > 100) {
		    /** XXX: If animation time was either 'too fast' or 'too slow', 
		     * we recalibrate r and n. This is probably irrelevant for now on. 
		     * Although, current values for n and r are found with this. But
		     * if we want to change values of r or n, this will help us to keep
		     * the defaultAnimationTime.
		     */
		    if (dbg) pa("x: " + x + " r: " + r + " n: " + n +
				" millis: " + (time - startTime) + " recalibrating...");
		    r *= x; n *= x;
		}
		return true;
	    }
	    return false;
	}

	public void callRender(long time, Window w) {
	    float f = fract(time);
	    w.renderAnim(f, 1, false /*f >= 0.85*/);
	}
    }
    static public FractCalculator fractCalc = new SimpleCalculator();


    /** For subclasses to override: process incoming events.
     * In order to provide a responsive implementation,
     * this method should only wait for 1000ms at most.
     * @param waitForEvent Hang until an event comes.
     * @return true, if any events were received.
     */
    protected abstract boolean handleEvents(boolean waitForEvent);

    static private PriorityQueue queue = new LifoPriorityQueue();

    /** Run a given task in the main thread at some point
     * in the future.
     * This static method implements the Background
     * method addTask.
     * @see gzz.util.Background#addTask
     */
    public static void doWhenIdle(Runnable r, float priority) {
	synchronized(queue) {
	    queue.add(r, priority);
	    queue.notifyAll();
	}
	instance.interruptEventloop();
    }
    // Implement vob.util.Background
    public void addTask(Runnable r, float priority) {
	doWhenIdle(r, priority);
    }
    public void removeTask(Runnable r) {
	synchronized(queue) {
	    queue.remove(r);
	}
    }

    abstract protected void interruptEventloop() ;
    
    /** In AWT, call Toolkit.sync().
     *  This is necessary so that the JVM won't stuff
     *  X's queue full of stuff and render far more scenes
     *  far more quickly than it should and leave X lagging.
     */
    abstract protected void synchronizeToolkit();

    /** Do the background tasks that need to be done in 
     * the main thread.
     * Implementations overriding this method should also delegate!
     * @return true if something was done.
     */
    protected boolean doIdle() {
	Runnable r;
	synchronized(queue) {
	    r = (Runnable)queue.getAndRemoveLowest();
	}
	if(r != null) {
	    if(dbg) pa("Updmanager idle: run "+r);
	    try {
		r.run();
	    } catch(Exception e) {
		e.printStackTrace();
		pa("EXCEPTION IN BG!!! "+e);
	    }
	    if(dbg) pa("Updmanager idle: finished  run "+r);
	    return true;
	} else {
	    if(dbg) pa("Updmanager No idle tasks");
	}
	return false;
    }

    /** Play event handling loop
     * and the idle tasks one step; return true if something
     * was done.
     * Used mainly from tests.
     */
    static public boolean tickIdle() {
	if(dbg) pa("TickIdle");
	if(instance.handleEvents(false)) {
	    if(dbg) pa("TickIdle: HandleEvents true");
	    return true;
	}
	return instance.doIdle();
    }

    static public boolean waitEvent() {
	return instance.handleEvents(true);
    }


    /** The main loop for all windows updates and event handling.
     */
    public void run() {
	if(initRunnable != null) {
	    initRunnable.run();
	    initRunnable = null;
	}
	// Handle events.

	// We want this thread to have (almost) the lowest priority, since
	// we want all incoming key events to be handled before
	// this thread gets its moment to run.
	// This is not very nice and it would be nicer to use
	// the java.awt.EventQueue wakeup routines of 1.3 / 1.4 here,
	// but kaffe doesn't have them.
	// Sigh.
	// Maybe we should check at runtime...
	Thread.currentThread().setPriority(Thread.MIN_PRIORITY+1);

	int ind = -1;
	boolean initNeeded = false; // Init outside synch(ord) needed.
	boolean firstWinAnim = false; // The first window is still animating.
	Window[] wins = new Window[0], newwins = null;
	boolean[] regenNeeded = new boolean[0];
	long lastTime = 0; // Last start of first win animation.
MAINEVENTLOOP: while(true) try {
	    // This is necessary so that the JVM won't stuff
	    // X's queue full of stuff and render far more scenes
	    // far more quickly than it should and leave X lagging.
            synchronizeToolkit();

	    if(dbg) pa("STARTORD");
	    if (!firstWinAnim) ind++;
	    updating = false;

	    handleEvents(false);

	    while(disabled>0 || (ind >= wins.length && !restartUpd)) {
		// While no screen updates need to be done
		if(dbg) pa("STARTORD "+disabled+" "+ind+" "+restartUpd);

		if(doIdle()) continue MAINEVENTLOOP;

		System.gc();
		// Wait for next event
		if(dbg) pa("Updmanager: WAIT FOR EVENT!!!");
		if(restartUpd) continue;
		handleEvents(true);
		if(dbg) pa("Updmanager: FINISHED WAITING FOR EVENT!!!");
	    }
	    resetChgAfter();
	    // Thread.yield(); // Again, try to let the evena thread have control.
	    synchronized(ordering) {
		if(dbg) pa("STARTORD WAITED");
		updating = true;
		if (restartUpd) {
		    restartUpd = false;
		    newwins = (Window[])ordering.toArray(new Window[0]);
		    ind = 0;
		    initNeeded = true;
		}
		try {
		    if (initNeeded) {
			initNeeded = false;
			long time = System.currentTimeMillis();

			if (firstWinAnim) {
			    if(fractCalc.isOver(time) ||
				!wins[0].hasEndState())
					    wins[0].endAnimation();
			    else
				    wins[0].changeStartState(0.8f);
			}
			firstWinAnim = false;
			// scene generation takes a lot of time, do later!
			//fractCalc.eventAt(time);
			wins = newwins; newwins = null;
			regenNeeded = new boolean[wins.length];
			for (int i = 0; i < regenNeeded.length; i++)
			    regenNeeded[i] = true;
			if (wins.length > 0) {
			    if(dbg) pa("Generating end state for window 0");
			    callGenerateEnd(wins[0], 100, 1);
			    if(dbg) pa("Generating end state: Phase 1 done");
			    if (!wins[0].hasEndState()) {
				if(dbg) pa("Couldn't generate end state for window 0!");
			    } else {
				regenNeeded[0] = false;
				if (animationtime != 0
					&& wins[0].animUseful())
				    firstWinAnim = true;
				animationtime = defaultAnimationTime;
			    }
			    if(dbg) pa("Finished end state for window 0");
			}
			if (firstWinAnim && !wins[0].hasEndState())
			    if(dbg) pa("GRAA!");
			fractCalc.eventAt(System.currentTimeMillis());
		    }
		    if (ind >= wins.length) continue;
		    if(dbg) if(dbg) pa("Try firstwinanim: "+firstWinAnim);
		    if (firstWinAnim) {
			long time = System.currentTimeMillis();

			if (fractCalc.isOver(time)) firstWinAnim = false;
			else {
			    if (!wins[0].hasEndState())
				if(dbg) pa("AIEE! Animating without endstate!");
			    fractCalc.callRender(time, wins[0]);
			    continue;
			}
		    }
		    if (regenNeeded[ind]) {
			if(dbg) pa("Generating end state for "+ind);
			callGenerateEnd(wins[ind], 100, 1);
			if (!wins[ind].hasEndState()) {
			    if(dbg) pa("Couldn't generate end state for win "+ind);
			} else regenNeeded[ind] = false;
		    }
		    if(dbg) pa("End animation "+ind);
		    wins[ind].endAnimation();
		    if(dbg) pa("Render still "+ind);
		    wins[ind].renderStill(1);
		} catch(Error e) {
		    System.err.println("EXCEPTION WHILE UPDATING!");
		    e.printStackTrace();
		}
	    }
	} catch(Throwable t) {
	    t.printStackTrace();
	    if(dbg) pa("Stopping update loop for a second.");
	    try {
		Thread.sleep(1000);
	    } catch(InterruptedException _) {};
	}
    }

    /*
    Object eventloopSynch = new Object();
    Object freezingWait = new Object();
    Object frozenWait = new Object();

    protected int should_disable = 0;

    protected void is_disabling() {
	return should_disable > 0;
    }
    protected void freeze_if_disabled() {
	if(should_disable) {
	    synchronized(
	}
    }

    protected void eventLoop() {
	OUTER: while(true) try {
	    wasFrozen = freeze_if_disabled();
	    handleEvents(!wasFrozen);
	    while(true) {
		if(is_disabling())
		    continue OUTER;
		if(need_redraw()) {
		    redraw_one_step();
		    if(is_disabling())
			continue OUTER;
		} else {
		    continue OUTER;
		}
		handleEvents(false);
	    } 
	}
    }
    */

}


