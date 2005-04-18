/*
ParticleSpace.java
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

public interface ParticleSpace {


    /** Creates stagnant, stopped particle.
     */
    Particle createStagnantParticle(Object particleId, Particle particle);
    Particle createStagnantParticle(Object particleId, float x,float y,
				float z, float w,float h);

    /** Creates live, moving, flying particle.
     */
    Particle createLiveParticle(Object particleId, float x,float y,
			    float z, float w,float h);
    Particle createLiveParticle(Object particleId, Particle particle);

    /** XXX
     */
    void connectParticlesWithSpringMassModel(Particle particleIdA,
					     Particle particleIdB,
					     float length, 
					     float springConst);

    /** Set repulsion force for particle. Force influences only to 
     *  other particles which have repulsion force set.
     *  @param q The XXX qoulombs const.
     */
    void setRepulsionForce(Particle p, int q);


    ODESolver.ParticleDerivative getDerivative();


    Particle getParticle(Object node);

}
