/*   
ThemeFrame.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import java.util.*;


/** A frame whose appearance is determined by the theme; usually
 *  it will be similar to the frame rendered around components.
 */
public class ThemeFrame extends LobMonoLob {

    public static final String 
	URI = "http://fenfire.org/2004/07/layout/ThemeFrame";

    public static final Object[] PARAMS = { CONTENT };

    public ThemeFrame(Lob content) {
	Map params = new HashMap();
	params.put(CONTENT, content);

	Theme t = Theme.getDefaultTheme();

	setDelegate(t.getLob(URI, params));
    }
}
