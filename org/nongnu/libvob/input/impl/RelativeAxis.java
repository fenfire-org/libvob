/*
RelativeAxis.java
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

/** A single input axis which gets relative motion events
 * from the outside. 
 */
public class RelativeAxis implements Axis {

// --- The actual mechanics
	
    private int state = InputDeviceManager.STATE_NORMAL;

    private int rawValue = 0;

    private int min = 0;
    private int max = 1000;

    private int calMin;
    private int calMax;

    private int choiceDistance = 0;

    private void callAbs() {
	if(state != InputDeviceManager.STATE_NORMAL)
	    return;
	if(absListener != null)
	    absListener.changedAbsolute(
		(rawValue - min) / (float)(max - min));
    }


// --- Interfacing with the outer world

    private String name;

    private RelativeAxisListener relListener;
    private AbsoluteAxisListener absListener;

    public RelativeAxis(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    synchronized public void setMainListener(AxisListener listener) {
	this.relListener = null;
	this.absListener = null;
	if(listener instanceof RelativeAxisListener)
	    this.relListener = (RelativeAxisListener)listener;
	else {
	    this.absListener = (AbsoluteAxisListener)listener;
	    callAbs();
	}
    }

    synchronized public void setState(int state) {
	this.state = state;
	if(state == InputDeviceManager.STATE_CHOOSING)
	    choiceDistance = 0;
	if(state == InputDeviceManager.STATE_CALIBRATING)
	    calMin = calMax = rawValue;
	if(state == InputDeviceManager.STATE_NORMAL) {
	    min = calMin; max = calMax;
	    callAbs();
	}
    }

    public float getChoiceProbability() {
	return 1.0f / (50 + choiceDistance);
    }

    /** The value of this axis was changed.
     * This is different from the RelativeAxisListener method,
     * since we take an integer argument and can thus keep an exact
     * count.
     */
    synchronized public void changedRelative(int delta) {
	rawValue += delta;
	if(state == InputDeviceManager.STATE_NORMAL) {
	    if(relListener != null)
		relListener.changedRelative(delta);
	    else if(absListener != null)
		callAbs();
	} else if(state == InputDeviceManager.STATE_CHOOSING) {
	    choiceDistance += Math.abs(delta);
	} else if(state == InputDeviceManager.STATE_CALIBRATING) {
	    if(calMin > rawValue)
		calMin = rawValue;
	    if(calMax < rawValue)
		calMax = rawValue;
	}
    }

}
