/*
StandardBoundedFloatModel.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.input.impl;
import org.nongnu.libvob.input.*;
import java.util.*;

/** This class implements a BoundedFloatModel
 * in the easiest way: just a float inside the class.
 */
public class StandardBoundedFloatModel 
	extends AbstractModel 
	implements BoundedFloatModel {
    protected double min, max, val;

    public StandardBoundedFloatModel() {
	this(0, 1);
    }
    public StandardBoundedFloatModel(double min, double max) {
	this.min = min;
	this.max = max;
	this.val = .5 * (min + max);
    }

    public void setMinimum(double min) {
	this.min = min;
	if(val < min) val = min;
	if(max < min) max = min;
    }
    public double getMinimum() {
	return min;
    }
    public void setMaximum(double max) {
	this.max = max;
	if(val > max) val = max;
	if(min > max) min = max;
    }
    public double getMaximum() {
	return max;
    }
    public void setValue(double val) {
	if(val < min) val = min;
	if(val > max) val = max;
	this.val = val;
	callActionPerformed(null);
    }
    public double getValue() {
	return val;
    }

}
