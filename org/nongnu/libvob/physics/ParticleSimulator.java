// (c): Matti J. Katila

package org.nongnu.libvob.physics;
import org.nongnu.libvob.physics.impl.*;

public class ParticleSimulator {

    private ODESolver ode;
    private ParticleSpace ps;

    public ParticleSimulator(ParticleSpace sp) {
	ps = sp;
	ode = new RungeKutta();
    }

    public void simulate(float t) {
	ode.solve(ps.getDerivative(), t);
    }

}
