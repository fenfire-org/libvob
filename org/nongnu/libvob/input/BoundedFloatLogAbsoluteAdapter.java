/*
BoundedFloatLogAbsoluteAdapter.java
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

package org.nongnu.libvob.input;
import java.util.*;

/** A class which receives absolute events from an axis
 * and interprets them logarithmically onto a bounded float.
 * Note that the minimum and maximum of the bounded float
 * must both be greater than zero for this class to work.
 */
public class BoundedFloatLogAbsoluteAdapter 
	implements AbsoluteAxisListener {
    private BoundedFloatModel model;

    public BoundedFloatLogAbsoluteAdapter(BoundedFloatModel model) {
	this.model = model;
    }

    public void changedAbsolute(float newValue) {
	double min = Math.log(model.getMinimum());
	double max = Math.log(model.getMaximum());
	model.setValue(Math.exp(min + (max-min) * newValue));
    }
}




