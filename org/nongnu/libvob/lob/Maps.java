/*
Maps.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
 *
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
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob;
import javolution.realtime.*;
import javolution.util.*;
import java.util.*;

/** Static methods for creating FastMaps, usually as parameters
 *  to lob-creating functions.
 */
public class Maps {

    public static Map map() {
	return FastMap.newInstance();
    }

    public static Map map(Object k1, Object v1) {
	Map m = FastMap.newInstance();
	m.put(k1, v1);
	return m;
    }

    public static Map map(Object k1, Object v1, Object k2, Object v2) {
	Map m = FastMap.newInstance();
	m.put(k1, v1);
	m.put(k2, v2);
	return m;
    }
}
