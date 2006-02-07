// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl;

import java.io.OutputStream;
import java.io.PrintStream;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.gl.GLVobCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.LwjglRenderer.TransformProxy;

/**
 * A java coorder.
 * 
 * 
 * @author mudyc
 */
public class Coorder {

    static private Transform root = new Transform() {

	public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
	}
	public boolean shouldBeDrawn() {
	    return true;
	}
	public Vector3f transform(Vector3f p) {
	    return p;
	}
	public void vertex(Vector3f p) {
	}
	public boolean isNonlinear() {
	    return false;
	}
	public float nonlinearity(Vector3f p, float radius) {
	    return 0;
	}
	public boolean canPerformGL() {
	    return true;
	}
	public boolean performGL() {
	    return true;
	}

	public Transform getInverse() {
	    return this;
	}
	public void dump(PrintStream out) {
	    System.
	    out.print("Root");
	}

	Vector2f sq = new Vector2f(1,1);
	public Vector2f getSqSize() {
	    return sq;
	}
    };
    
    private Transform[] tr; 
    
    public Coorder(GLVobCoorder from, TransformProxy transProxy) {
	tr = new Transform[from.ninds];

	tr[0] = root;
	
	for (int i = 1, lastIndSize = 1; i < from.inds.length && i<from.ninds; i+=lastIndSize) {
	    // System.out.print(inds[i]+" ");
	    // if ((i % 10) == 0) System.out.println();

	    int type = from.inds[i];
	    lastIndSize = from.getIndSize(type);	    
	    Transform tr1 = transProxy.instantiate(type);
	    tr1.setYourself(this, i, from.inds, from.floats);
	    
	    tr[i] = tr1;
	}
    }

    /**
     * @return a new Coorder which has been linearly
     *         interpolated between given coordinates.
     */
    public static Coorder lerp(Coorder c1, Coorder c2,
	    int[] interps, float fract) {
	System.out.println("to be implemented.");
	return c1;
    }

    public Transform getTransform(int cs0) {
	return tr[cs0];
    }

}
