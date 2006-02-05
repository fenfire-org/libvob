// (c): Matti J. Katila

// src bases on code of Tuomas J. Lukka and others.

package org.nongnu.libvob.gl.impl.lwjgl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.nongnu.libvob.VobMap;
import org.nongnu.libvob.gl.GLVobCoorder;

/*-
 *  What? Why? How?
 *  ===============
 *  
 *  
 *  
 * 
 */
public class LwjglCoorder {

    public class TransformFactory {
	Transform instantiate(int type) {
	    switch(type) {
	    case 0: return null;
	    case 1: return null;
	    }
	    return null;
	}
    }
    
    
    /*-
     * Well, there's a huge work to build transformation tree per *every*
     * frame and I do not want to lost any instances of objects since
     * creation of those is so slowly. I want to be sure that
     * storing/flushing transformation instances is very fast operation.
     * 
     * Arrays are the fastest way to flush data *fast*. 
     */
    public class TransformProxy {
	TransformFactory factory = new TransformFactory();
	int size = 100;
	int[] useIndex = new int[NUM_TYPES];
	Transform[] clear;
	Transform[][] inUse = new Transform[NUM_TYPES][],
		freeForUse = new Transform[NUM_TYPES][];

	TransformProxy() {
	    for (int i = 0; i < inUse.length; i++)
		inUse[i] = new Transform[size];
	    clear = new Transform[size];
	}

	Transform instantiate(int type) {
	    if (useIndex[type] + 1 >= size) {
		resize(size = size * 2);
	    }
	    if (freeForUse[type][size - 1 - useIndex[type]] == null)
		inUse[type][useIndex[type]] = factory.instantiate(type);
	    else
		inUse[type][useIndex[type]] = freeForUse[type][size - 1 - useIndex[type]];

	    Transform ret = inUse[type][useIndex[type]];
	    useIndex[type] = useIndex[type] + 1;
	    return ret;
	}

	private void resize(int size) {
	    for (int i = 0; i < inUse.length; i++) {
		// create
		Transform[] newArr = new Transform[size];
		Transform[] newArr2 = new Transform[size];
		// copy
		System.arraycopy(inUse[i], 0, newArr, 0,
			inUse[i].length);
		System.arraycopy(freeForUse[i], 0, newArr2, 0,
			inUse[i].length);
		// swap
		inUse[i] = newArr;
		freeForUse[i] = newArr2;
	    }
	    clear = new Transform[size];
	}

	public void clear() {
	    // copy to safe but do not deinitialize
	    for (int i = 0; i < inUse.length; i++)
		System.arraycopy(inUse[i], 0, freeForUse[i],
			size - 1 - useIndex[i], useIndex[i]);
	    // clear
	    for (int i = 0; i < inUse.length; i++)
		System.arraycopy(clear, 0, inUse[i], 0,
			clear.length);
	}
    }

    private TransformProxy transProxy;
    private static LwjglCoorder instance = null;
    public static final int NUM_TYPES = 22;

    private LwjglCoorder() {
	transProxy = new TransformProxy();
    }

    public static synchronized LwjglCoorder getInstance() {
	if (instance == null)
	    instance = new LwjglCoorder();
	return instance;
    }

    public void render(VobMap map, int[] interps,
	    GLVobCoorder from, GLVobCoorder to, float fract,
	    boolean towards, boolean showFinal) {
	int[] inds = from.inds;
	for (int i = 0; i < inds.length; i++) {
	    // System.out.print(inds[i]+" ");
	    // if ((i % 10) == 0) System.out.println();

	    int cs1 = inds[i];

	    Transform tr = transProxy.instantiate(cs1);

	}
    }

}
