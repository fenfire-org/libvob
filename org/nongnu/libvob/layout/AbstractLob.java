/*
AbstractLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public abstract class AbstractLob 
    extends AbstractReplaceable.AbstractObservable implements Lob {


    public float getMinSize(Axis axis) throws UnknownSizeError {
	return getNatSize(axis);
    }

    public float getMaxSize(Axis axis) throws UnknownSizeError {
	return getNatSize(axis);
    }

    public boolean key(String key) {
	return false;
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	return false;
    }

    public void setSize(float requestedWidth, float requestedHeight) {
    }


    public List getFocusableLobs() {
	return Collections.EMPTY_LIST;
    }

    public void setFocusModel(Model m) {
    }

    public boolean isLargerThanItSeems() {
	return false;
    }
}
