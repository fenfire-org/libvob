/*
Layout.java
 *    
 *    Copyright (c) 2003-2005, Benja Fallenstein
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
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** A <em>layouted</em> object -- something with a fixed size that is ready
 *  to be placed into a vob scene.
 *  This also implements Layoutable; the size request is the fixed size
 *  of this object, and layouting it just returns the object itself.
 */
public interface Layout extends Lob {

    /**
     *  @param visible Whether to put lobs into the coordinate systems.
     *         If false, the tree of coordinate systems is created,
     *         but no lobs are put into them.
     *  @param d depth -- is to the z-axis like width is to the x-axis 
     *           and like height is to the y-axis
     */
    void render(VobScene scene, int into, int matchingParent, 
		float d, boolean visible);

    /** Get the size of this object.
     */
    Size getSize();
}
