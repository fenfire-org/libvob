// (c): Matti J. Katila

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
