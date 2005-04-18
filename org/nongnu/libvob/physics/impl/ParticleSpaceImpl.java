/*
ParticleSpaceImpl.java
 *    
 *    Copyright (c) 2004, Matti J. Katila
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
 * Written by Matti J. Katila
 */

package org.nongnu.libvob.physics.impl;
import org.nongnu.libvob.physics.fmm.*;
import org.nongnu.libvob.physics.*;
import org.nongnu.libvob.util.math.Vect; 
import java.util.*;

public class ParticleSpaceImpl 
    implements ParticleSpace, 
	       ODESolver.ParticleDerivative {
    static private void p(String s) { System.out.println("ParticleSpace:: "+s); }

    /* this should work...
    static interface DerivativePlugin extends ODESolver.ParticleDerivative {
	int getVectorSize();
    }
    */


    Map allParticles = new HashMap();
    public String dump() {
	String r = "";
	for (Iterator i = allParticles.entrySet().iterator(); i.hasNext();) {
	    r = r + ((Particle) ((Map.Entry)i.next()).getValue()).toString()+"\n";
	}
	return r + "--------\n";
    }
    

    Set stagnantParticles = new HashSet();
    Set liveParticles = new HashSet();

    public void setLive(Particle p) {
	if (liveParticles.contains(p))
	    stagnantParticles.remove(p);
    }
    public boolean isStagnant(Particle particle) {
	return stagnantParticles.contains(particle);
    }


    public Particle getParticle(Object id) { 
	return (Particle) allParticles.get(id);
    }
    /*
	return getParticle(id, true); }
    public Particle getParticle(Object id, boolean createIfNeeded) {
	Object p = stagnantParticles.get(id);
	if (p == null) 
	    p = liveParticles.get(id);
	if (p == null)
	    if (createIfNeeded) 
		return createLiveParticle(id, 0,0,0,1,1);
	    else
		throw new Error("No particle found!");
	
	return (Particle) p;
    }
    */

    List springMassConnections = new ArrayList();
    List springMassConnectionLengths = new ArrayList();
    List springMassConnectionConsts = new ArrayList();

    Set repulsions = new HashSet();

    
    // implements
    public Particle createStagnantParticle(Object id, Particle p) {
	allParticles.put(id, p);
	stagnantParticles.add(p);
	return p;
    }
    // implements
    public Particle createStagnantParticle(Object id, float x,float y,
				float z, float w,float h) {
	Particle p = new Particle(x,y,z,w,h);
	allParticles.put(id, p);
	stagnantParticles.add(p);
	return p;
    }
    
    // implements
    public Particle createLiveParticle(Object id, Particle p) {
	allParticles.put(id, p);
	liveParticles.add(p);
	return p;
    }

    // implements
    public Particle createLiveParticle(Object id, float x,float y,
			    float z, float w,float h) {
	Particle p = new Particle(x,y,z,w,h);
	allParticles.put(id, p);
	liveParticles.add(p);
	return p;
    }
    
    // implements
    public void connectParticlesWithSpringMassModel(Particle idA,
						    Particle idB, 
						    float length,
						    float springConst) {
	springMassConnections.add(idA);
	springMassConnections.add(idB);
	springMassConnectionLengths.add(new Float(length));
	springMassConnectionConsts.add(new Float(springConst));
    }



    public void setRepulsionForce(Particle p, int q) {
	p.setq(0,q);
	repulsions.add(fmmStruct.add(p));
    }




    
    public ODESolver.ParticleDerivative getDerivative() {
	return this;
    }


    private FMM.FMMStructure fmmStruct = 
	new FMMHashTree(-2000.0f, 2000.0f,
			-2000.0f, 2000.0f, 16, 0.01f);
    private FMM fmm = 
	new FMM(fmmStruct);

    public void init() {
	/*
	fmmStruct = 
	    new FMMHashTree(-2000.0f, 2000.0f,
			    -2000.0f, 2000.0f, 16, 0.01f);
	*/
	fmm = new FMM(fmmStruct);

	springMassConnections = new ArrayList();
	springMassConnectionLengths = new ArrayList();
	springMassConnectionConsts = new ArrayList();
	
	repulsions = new HashSet();
    }

    /*
     * ************************************************
     * implement ODESolver.ParticleDerivative
     * ************************************************
     */

    public float k_d = 0.1f;

    public Vect deriv(Vect x, float t, Vect derivOut) {
	clearAccelerations();
	getSpeed(derivOut);

	int j=0;
	for (Iterator i = springMassConnections.iterator(); i.hasNext(); j++) {
	    Particle a = (Particle) i.next();
	    if (!(i.hasNext())) throw new Error("out of particles!");
	    Particle b = (Particle) i.next();


	    float k_s = ((Float)springMassConnectionConsts.get(j)).floatValue();
	    float r = ((Float)springMassConnectionLengths.get(j)).floatValue();
	    
	    float pabs = a.p.neg(b.p).abs();
	    Vect pan_b = a.p.neg(b.p); 
	    Vect f_a = null;
	    try { // -
		f_a = pan_b.div(pabs).
		    mul(
			( k_s * (pabs - r) + 
			  k_d * a.v.neg(b.v).scalar(pan_b)/pabs)
			); //.invert();
	    } catch (Exception e) { 
		f_a = new Vect(new float[a.p.size()+a.v.size()]); 
	    }
	    if (!(isStagnant(a))) {
		a.a.sum(f_a.div(a.mass), true);
	    }
	    if (!(isStagnant(b))) {
		Vect f_b = f_a.invert();
		b.a.sum(f_b.div(b.mass), true);
	    }
	}
	
	fmm.proceed(0);
	for (Iterator i = liveParticles.iterator(); i.hasNext();) {
	    Particle p = (Particle) i.next();
	    if (!(isStagnant(p))) {
		p.a.sum(new Vect(new float[]{-p.F.r()/p.mass, 
					     -p.F.i()/p.mass}), true);
	    }
	}
	setAcceleration(derivOut);
	return derivOut;
    }

    private void getSpeed(Vect empty) {
	int j = 0;
	for (Iterator i = liveParticles.iterator(); i.hasNext();) {
	    Particle p = (Particle) i.next();
	    empty.set(j, p.v);
	    j+=4;
	}
    }
    private void setAcceleration(Vect v) {
	int j = 0;
	for (Iterator i = liveParticles.iterator(); i.hasNext();) {
	    Particle p = (Particle) i.next();
	    v.set(j, p.a);
	    j+=4;
	}
    }
    private void clearAccelerations() {
	for (Iterator i = liveParticles.iterator(); i.hasNext();) {
	    Particle p = (Particle) i.next();
	    p.a.x(0); p.a.y(0);
	}
    }

    public Vect getParticleStates() {
	Vect states =  // XXX 4!
	    new Vect(new float[liveParticles.size() * 4]);
	int j = 0;
	for (Iterator i = liveParticles.iterator(); i.hasNext();) {
	    Particle p = (Particle) i.next();
	    j += p.getState(states, j);
	}

	return states;
    }
    
    // x0 + addedDerivates of ODE
    public void setParticleStates(Vect stateWithAddedDerivate) {
	int j = 0;
	for (Iterator i = liveParticles.iterator(); i.hasNext();) {
	    Particle p = (Particle) i.next();
	    j += p.setState(stateWithAddedDerivate, j);
	}

    }

}
