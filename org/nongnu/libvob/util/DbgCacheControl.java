/*
DbgCacheControl.java
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

/** A CacheControl that just prints out stuff.
 */
public class DbgCacheControl extends CacheControl {
    private void p(String s) { System.out.println(s); }

    public Listener registerCache_impl(final Object cache, final String name) {
	p("Cache: start listen "+cache+" "+name);
	return new Listener() {
	    public void hit(Object key) {
		p("CacheHit: "+cache+" "+name+" "+key);
	    }
	    public void startMiss(Object key) {
		p("CacheStartMiss: "+cache+" "+name+" "+key);
	    }
	    public void endMiss(Object key) {
		p("CacheEndMiss: "+cache+" "+name+" "+key);
	    }

	};
    }

}

