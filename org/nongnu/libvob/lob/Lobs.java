/*
Lobs.java
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
import org.nongnu.libvob.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.libvob.util.*;
import javolution.realtime.*;
import java.awt.Color;
import java.util.*;

/** Static methods for creating common kinds of lobs.
 */
public class Lobs {

    public static Lob rect(Color color, float lineWidth) {
	return VobLob.newInstance(RectVob.newInstance(color, lineWidth));
    }

    public static Lob rect3d(Color color, float lineWidth, boolean raised) {
	return VobLob.newInstance(RectVob.newInstance(color, lineWidth, 
						      raised));
    }

    public static Lob filledRect(Color color) {
	return VobLob.newInstance(FilledRectVob.newInstance(color));
    }
}
