/*
AbstractLayout.java
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
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** A Lob that has been fully layouted.
 */
public abstract class AbstractLayout extends AbstractLob {

    protected float width, height;

    protected void setSize(float width, float height) {
	this.width = width; this.height = height;
    }

    public SizeRequest getSizeRequest() {
	return SizeRequest.newInstance(width, height);
    }

    public Lob layout(float width, float height) {
	if(this.width != width || this.height != height)
	    throw new IllegalArgumentException(this+" "+width+" "+height+" "+this.width+" "+this.height);

	return this;
    }
}
