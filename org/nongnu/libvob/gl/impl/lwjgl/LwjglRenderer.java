// (c): Matti J. Katila

// src bases on code of Tuomas J. Lukka and others.

package org.nongnu.libvob.gl.impl.lwjgl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.nongnu.libvob.VobMap;
import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.gl.GLVobCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.trans.linear.BoxTransform;
import org.nongnu.libvob.gl.impl.lwjgl.trans.linear.OrthoBoxTransform;
import org.nongnu.libvob.gl.impl.lwjgl.trans.linear.ScaleTransform;
import org.nongnu.libvob.gl.impl.lwjgl.trans.linear.TranslateTransform;
import org.nongnu.libvob.impl.lwjgl.LWJGL_VobMap;

/*-
 *  What? Why? How?
 *  ===============
 *  
 *  So, what's up? I read tjl's native code like ten times before I started to code this up.
 *  And finally I'm not sure how it should be done, still I feel like that.
 *  
 *  Tuomas' code creates transform (coordinate) instances for every frame. It's not a 
 *  bottleneck in C++ side for some reason. It's also good for API. The only thing that 
 *  one render call needs are the inds and floats given. I would like to continue to 
 *  support this simple API but I'm a bit scary that in the end it's too slow after all.
 *  
 *  Tuomas' architecture instantiates one coordinatesystem but it may instantiate two more if there are interpolations which 
 *  need special handling. 
 *  
 *  
 *  Why not just instantiate coordinate system on the way when coordinate systems are added? There's some issues:
 *  - I want setCoordinateSystem methods to work.
 *  
 *  
 *  Well, argh..
 *  
 *  I will code it like this:
 *  
 *  
 * 
 */
public class LwjglRenderer {

    public class TransformFactory {
	Transform instantiate(int type) {
	    switch(type) {
	    case 7: return new TranslateTransform();
	    case 8: return new ScaleTransform();
	    case 12: return new BoxTransform();
	    case 19: return new OrthoBoxTransform();
	    
	    default:
		throw new Error("No transform for type "+type+" implemented (yet).");
	    }
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
	    for (int i = 0; i < inUse.length; i++)
		freeForUse[i] = new Transform[size];
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
    private static LwjglRenderer instance = null;
    public static final int NUM_TYPES = 22;

    private LwjglRenderer() {
	transProxy = new TransformProxy();
    }

    public static synchronized LwjglRenderer getInstance() {
	if (instance == null)
	    instance = new LwjglRenderer();
	return instance;
    }

    public void render(LWJGL_VobMap map, int[] interps,
	    GLVobCoorder from, GLVobCoorder to, float fract) {

	// creates the coordinate systems according to given animation fraction.
	// this coordinate system is the one that is linearly interpolated.
	Coorder c = createCoorder(from, to, interps, fract);

	// render vobs to coordinates
	realRender(c, map);
	
	transProxy.clear();
    }

    
    private void realRender(Coorder c, LWJGL_VobMap map) {
	int lastVobSize = 1;
	for (int i=0; i<map.list.length && i<map.getSize() && map.list[i] != 0; i+= lastVobSize)
	{
	    int vob = map.list[i];
	    int code = map.list[i] & ~GL.RMASK;
	    
	    if ((vob & GL.RMASK) == GL.RENDERABLE0) {	
		GL11.glCallList(code);
		lastVobSize = 1;
	    } else if ((vob & GL.RMASK) == GL.RENDERABLE1) {	
		int cs0 = map.list[i+1];
		LWJGLRen.Vob1 v = (LWJGLRen.Vob1)map.index2vob[i];
		v.render(c.getTransform(cs0), code);
		lastVobSize = 2;
	    } else if ((vob & GL.RMASK) == GL.RENDERABLE2) {	
		int cs0 = map.list[i+1];
		int cs1 = map.list[i+2];
		LWJGLRen.Vob2 v = (LWJGLRen.Vob2)map.index2vob[i];
		v.render(c.getTransform(cs0), c.getTransform(cs1), code);
		lastVobSize = 3;
	    } else if ((vob & GL.RMASK) == GL.RENDERABLE3) {	
		int cs0 = map.list[i+1];
		int cs1 = map.list[i+2];
		int cs2 = map.list[i+3];
		LWJGLRen.Vob3 v = (LWJGLRen.Vob3)map.index2vob[i];
		v.render(c.getTransform(cs0), c.getTransform(cs1), c.getTransform(cs2), code);
		lastVobSize = 4;
	    } else if ((vob & GL.RMASK) == GL.RENDERABLEN) {	
		int ncs = map.list[i+1];
		System.out.println("renderable N ");
		lastVobSize = ncs;
	    } else if ((vob & GL.RMASK) == GL.RENDERABLE_VS) {	
		throw new Error("un impl.");
		
	    } else throw new Error("out of vobs...");
	}
	
	//for (int i=0; i<map.)
	System.out.println("render impl.");
	Display.update();
    }


    private Coorder createCoorder(GLVobCoorder from, GLVobCoorder to, int[] interps, float fract) {

	// check for still image - then we use just the given vob coorder
	if (interps == null || to == null) {
	    return new Coorder(from, transProxy);
	} else {
	    Coorder c1 = new Coorder(from, transProxy);
	    Coorder c2 = new Coorder(to, transProxy);
	    
	    return Coorder.lerp(c1, c2, interps, fract);
	}
    }	    
	    
	

}
