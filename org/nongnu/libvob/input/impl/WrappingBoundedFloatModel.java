/*
WrappingBoundedFloatModel.java
 *    
 *    Copyright (c) 2003, Asko Soukka
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
 * Written by Asko Soukka
 */

package org.nongnu.libvob.input.impl;
import org.nongnu.libvob.input.*;
import java.util.*;

/**
 * This is StandardBoundedFloatModel, which wraps value.
 */
public class WrappingBoundedFloatModel 
	extends StandardBoundedFloatModel  {
    public WrappingBoundedFloatModel() { this(0, 1); }
    public WrappingBoundedFloatModel(double min, double max) { super(min, max); }
    public void setValue(double val) {
	while (val < min) val += max;
	while (val > max) val -= max;
	this.val = val;
	callActionPerformed(null);
    }
}
