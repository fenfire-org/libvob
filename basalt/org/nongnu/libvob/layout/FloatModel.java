/*   
FloatModel.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of Libvob.
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
 */
package org.nongnu.libvob.layout;
import org.nongnu.navidoc.util.*;
import java.util.*;

public class FloatModel extends AbstractModel.AbstractFloatModel {

    protected float value;

    public FloatModel() {
    }

    public FloatModel(float value) { 
	this.value = value; 
    }
	
    public float getFloat() { 
	return value;
    }

    public void setFloat(float value) {
	if(this.value == value) return;
	this.value = value;
	obses.trigger();
    }

    public String toString() {
	return "FloatModel("+value+")";
    }
}
