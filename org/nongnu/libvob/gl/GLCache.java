/*
GLCache.java
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.gl;
import org.nongnu.libvob.*;
import java.util.HashMap;

/** Cache some renderables' creation.
 */
public class GLCache {
    static HashMap callListCache = new HashMap();
    public static Vob getCallList(String s) {
	Vob ret = (Vob)callListCache.get(s);
	if(ret == null) {
	    ret = GLRen.createCallList(s);
	    callListCache.put(s, ret);
	}
	return ret;
    }
    static HashMap callListCoordedCache = new HashMap();
    public static Vob getCallListCoorded(String s) {
	Vob ret = (Vob)callListCoordedCache.get(s);
	if(ret == null) {
	    ret = GLRen.createCallListCoorded(s);
	    callListCoordedCache.put(s, ret);
	}
	return ret;
    }
    static HashMap callListBoxCoordedCache = new HashMap();
    public static Vob getCallListBoxCoorded(String s) {
	Vob ret = (Vob)callListBoxCoordedCache.get(s);
	if(ret == null) {
	    ret = GLRen.createCallListBoxCoorded(s);
	    callListBoxCoordedCache.put(s, ret);
	}
	return ret;
    }
}
