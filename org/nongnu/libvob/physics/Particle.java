// (c): Matti J. Katila

package org.nongnu.libvob.physics;
import org.nongnu.libvob.util.math.*;
import org.nongnu.libvob.physics.fmm.*;

 
public class Particle { 
    public Vect p; // place float x,y,z;
    public Vect v; // velocity
    public Vect a; // acceleration

    //float w,h;
    public float mass = 1.0f;

    public int[] q; // coulombian const?
    public Complex near, far, F; // fmm things..
    float priority = 0;
    public Object node = null;
    private int id;



    public int id() { return id; }
    public Object getNode() { return this.node; }
    public int q(int ind) { return q[ind]; }
    public int[] q() { return q; }
    public void setq(int ind, int qval) { q[ind] = qval; }
    public float x() { return p.x(); }
    public float y() { return p.y(); }
    public void x(float new_x) { p.x(new_x); }
    public void y(float new_y) { p.y(new_y); }
    public float[] coord() { return new float[] {x(), y()}; }
    //public void coord(float[] new_c) { x = new_c[0]; y = new_c[1]; }
    public float priority() { return priority; }
    public void setPriority(float new_p) { priority = new_p; }
    public int size() { return q.length; }

    public Complex z(FMMCell fc) {
	return new Complex(x()-fc.xcenter, y()-fc.ycenter);
    }




    public Particle(float x,float y) {
	p = new Vect(x,y);
	v = new Vect(0,0);
	a = new Vect(0,0);
	q = new int[]{0};
	near = new Complex();
	far = new Complex();
	F = new Complex();
    }
    public Particle(float x,float y,float z,
		    float w, float h) {
	p = new Vect(x,y,z);
	v = new Vect(0,0);
	a = new Vect(0,0);
	q = new int[]{0};
	near = new Complex();
	far = new Complex();
	F = new Complex();
	//this.w = w;
	//this.h = h;
    }

    public String toString() {
	return "[x: "+p.x()+", y: "+p.y()+" - v("+v.x()+", "+v.y()+") a("+a.x()+", "+a.y()+")]";
    }
	

    // to solve all derivates...
    // iterate through all particles and add vector..
    public int getState(Vect V, int index) {
	V.set(index, p.x());
	V.set(index+1, p.y());
	V.set(index+2, v.x());
	V.set(index+3, v.y());
	return 4;
    }

    public int setState(Vect V, int index) {
	p.x(V.get(index));
	p.y(V.get(index+1));
	v.x(V.get(index+2));
	v.y(V.get(index+3));
	return 4;
    }

}

