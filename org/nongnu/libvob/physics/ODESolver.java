/*
ODESolver.java
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

package org.nongnu.libvob.physics;
import org.nongnu.libvob.util.math.Vect; 


/** An interface for ODE (Ordinady Differential Equation Solver) 
 *  for particle system.
 */
public interface ODESolver {

    static interface ParticleDerivative {
	Vect deriv(Vect x, float t, Vect derivOut);
	Vect getParticleStates();
	void setParticleStates(Vect addedDerivate);
    }


    /** Returns the robustness factor XXX of solver method.
     *  For example, Euler O(h^2) => 2, 
     *  Midpoint O(h^3) => 3 and
     *  Runge-Kutta O(h^5) => 5.
     */
    int getRobustnes();


    //void setForceAccumulator(Evaluator evaluator);

    /** Solve the different equation with timestep t.
     */
    void solve(ParticleDerivative particleSystem, float t);

}
