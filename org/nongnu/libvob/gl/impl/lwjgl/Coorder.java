// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl;

import org.nongnu.libvob.gl.GLVobCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.LwjglRenderer.TransformProxy;

/**
 * A java coorder.
 * 
 * 
 * @author mudyc
 */
public class Coorder {

    private Transform tr; 
    
    public Coorder(GLVobCoorder from, TransformProxy transProxy) {

	for (int i = 1, lastIndSize = 1; i < from.inds.length && i<from.ninds; i+=lastIndSize) {
	    // System.out.print(inds[i]+" ");
	    // if ((i % 10) == 0) System.out.println();

	    int type = from.inds[i];
	    lastIndSize = from.getIndSize(type);	    
	    Transform tr1 = transProxy.instantiate(type);
	    tr1.setYourself(this, i, from.inds, from.floats);

	    Transform tr2 = null;
	    int cs2;

	}
    }

    /**
     * @return a new Coorder which has been linearly
     *         interpolated between given coordinates.
     */
    public static Coorder lerp(Coorder c1, Coorder c2,
	    int[] interps, float fract) {
	return null;
    }

    public Transform getTransform(int cs0) {
	return null;
    }

}
