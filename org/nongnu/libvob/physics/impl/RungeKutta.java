// (c): Matti J. Katila

package org.nongnu.libvob.physics.impl;
import org.nongnu.libvob.util.math.Vect; 
import org.nongnu.libvob.physics.*; 

/** Runge-Kutta method to compute the next step size.
 */
public class RungeKutta  implements ODESolver {

    public int getRobustnes() { return 5; }


    static private final float f13 = 1.0f/3.0f;
    static private final float f16 = 1.0f/6.0f;

    // x is place or velocity usually.
    public void solve(ParticleDerivative particles, float t) {

	// particleSpace.getDerivates..

	// particleSpace.getState..
	// particleSpace.set...

	Vect x = particles.getParticleStates();

	float t0 = 0;
	float t2 = t0 + t/2.0f;

	Vect k1,k2,k3,k4;
	k1 = new Vect(new float[x.size()]);
	k2 = new Vect(new float[x.size()]);
	k3 = new Vect(new float[x.size()]);
	k4 = new Vect(new float[x.size()]);

	k1 = particles.deriv(x, t0, k1).mul(t);
	k2 = particles.deriv(x.sum( k1.mul(0.5f)), 
			     t2, k2).mul(t); 
	k3 = particles.deriv(x.sum( k2.mul(0.5f)), 
			     t2, k3).mul(t); 
	k4 = particles.deriv(x.sum(k3), t0 + t,
			     k4).mul(t); 

	particles.setParticleStates(x.sum(
					  k1.mul(f16).
					  sum(k2.mul(f13)).
					  sum(k3.mul(f13)).
					  sum(k4.mul(f16))
					  )
				    );
	// x0 + 1/6x1 + 1/3 x2...
    }    // need to add deriv..

}
