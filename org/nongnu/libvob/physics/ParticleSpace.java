// (c): Matti J. Katila

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
