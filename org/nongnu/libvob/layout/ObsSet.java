/*
ObsSet.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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
package org.nongnu.libvob.layout;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class ObsSet {
    protected Obs singleObserver = null;
    protected Obs[] observers = null;

    protected boolean updateInProgress = false;

    public void startUpdate() { updateInProgress = true; }
    public void endUpdate() { updateInProgress = false; trigger(); }

    public void addObs(Obs obs) {
	if(obs == null) return;

	if(observers != null) {
	    for(int i=0; i<observers.length; i++) {
		if(observers[i] == obs) return;
	    }
	    for(int i=0; i<observers.length; i++) {
		if(observers[i] == null) {
		    observers[i] = obs;
		    return;
		}
	    }
	    // expand array
	    Obs[] nobs = new Obs[2*observers.length];
	    System.arraycopy(observers, 0, nobs, 0, observers.length);
	    nobs[observers.length] = obs;
	    observers = nobs;
	} else if(singleObserver == null) {
	    singleObserver = obs;
	} else if(singleObserver != obs) {
	    observers = new Obs[] { singleObserver, obs };
	    singleObserver = null;
	}
    }
    
    public void removeObs(Obs obs) {
	if(obs == null) return;

	if(observers == null) {
	    if(singleObserver == obs)
		singleObserver = null;
	} else {
	    int i=0;
	    for(; i<observers.length; i++)
		if(observers[i] == obs)
		    break;

	    for(; i<observers.length-1; i++) {
		observers[i] = observers[i+1];
		if(observers[i] == null)
		    break;
	    }
	    
	    observers[observers.length-1] = null;

	    if(i <= observers.length/4) {
		Obs[] nobs = new Obs[observers.length/4];
		System.arraycopy(observers, 0, nobs, 0, nobs.length);
		observers = nobs;
	    }
	}
    }

    public void removeAll() {
	observers = null;
	singleObserver = null;
    }
    
    public void trigger() {
	if(updateInProgress) return;

	if(observers == null) {
	    if(singleObserver != null)
		singleObserver.chg();
	} else {
	    for(int i=0; i<observers.length; i++)
		if(observers[i] != null)
		    observers[i].chg();
	}
    }


    public static abstract class AbstractObservable implements Observable {
	protected ObsSet obses = new ObsSet();
	public void addObs(Obs o) { obses.addObs(o); }
	public void removeObs(Obs o) { obses.removeObs(o); }
	public void chg() { obses.trigger(); }
    }
}
