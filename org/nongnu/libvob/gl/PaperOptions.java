/*
PaperOptions.java
 *    
 *    Copyright (c) 2003, Vesa Kaihlavirta and Matti Katila
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
 * Written by Vesa Kaihlavirta and Matti Katila
 */

package org.nongnu.libvob.gl;

public class PaperOptions {
    public static boolean use_opengl_1_1 = false;
    static private PaperOptions _instance = null;

    static public PaperOptions instance() {
	if(null == _instance) {
	    _instance = new PaperOptions();
	}
	return _instance;
    }
    private PaperOptions() {
    }
}
