/*
CacheControl.java
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

/** A class that is able to follow all caches in the running
 * program and provides a place to plug in better algorithms
 * and profile things.
 * <p>
 * A basic way of working in Fenfire and libvob 
 * is to have a pure (side-effect free)
 * function y = f(x), for example for getting
 * a libpaper background for a given document id, and
 * then to wrap a cache around the function.
 * <p>
 * This class is for observing caches and later, possibly,
 * tuning them at run-time.
 */
public abstract class CacheControl {

    /** The callback provided to caches. 
     * The caches should call the methods to inform the cache controller
     * about their actions.
     */
    public interface Listener {
	/** There was a cache hit for the given object.
	 */
	void hit(Object key);
	/** There was a cache miss for the given object.
	 * For timing and information about the recursive use
	 * of other caches, the endMiss key will be called
	 * after the object is regenerated.
	 */
	void startMiss(Object key);
	/** The regeneration of the object was completed.
	 */
	void endMiss(Object key);
    }

    /** A listener that does nothing.
     */
    public static class DummyListener implements Listener {
	    public void hit(Object key) { }
	    public void startMiss(Object key) { }
	    public void endMiss(Object key) { }
    }
    private static class DummyInstance extends CacheControl {
	public Listener registerCache_impl(final Object cache, 
		    final String name) {
	    return new DummyListener();
	}
    }

    public abstract Listener registerCache_impl(Object cache,
			    String name) ;

    static private CacheControl instance; 

    private static void createInstance() {
	try {
	    String name = System.getProperty("vob.cachecontrol");
	    if(name != null)
		instance = (CacheControl)(Class.forName(name).newInstance());
	    else 
		instance = new DummyInstance();
	    System.out.println("Cache control initialized: "+name+" "
			    +instance);
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new Error("Cache control instance" + e);
	}
    }

    /** Register a cache.
     * @param cache The cache object. Used to uniquely identify caches
     * 			of the same name
     * @param name A user-readable name: what is being cached.
     * @return The listener that the cache should use. Never null,
     * 		so that code doesn't need to be littered with
     * 		if(xxx != null) statements.
     */
    static public Listener registerCache(
	    Object cache,
	    String name) {
	if(instance == null) createInstance();
	return instance.registerCache_impl(cache, name);
    }
		
}
