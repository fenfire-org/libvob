/*
GLVobCoorder.java
 *
 *    Copyright (c) 2001-2003, Tuomas Lukka
 *
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
 *
*/
/*
 * Written by Tuomas J. Lukka
 */
package org.nongnu.libvob.impl.gl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.gl.*;
import java.util.*;

/** This is an internal base class for GLVobCoorder.
 * The generated class GLVobCoorder_Gen inherits from this
 * and GLVobCoorder proper inherits from that.
 */
public abstract class GLVobCoorderBase extends AffineVobCoorder {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    float[] floats = new float[80000];
    int nfloats = 0;

    int[] inds = new int[20000];
    int ninds = 1; // zero is special (the root)

    protected final void addFloats(int n) { nfloats += n; }
    protected final void addInds(int n) { ninds += n; }

    protected final void updateCoords(int cs) {};


    public void clear() {
	nfloats = 0;
	ninds = 1;
	childs.clear();
    }

    public void activate(int cs) {
	inds[cs] |= GL.CSFLAG_ACTIVE;
    }

    private boolean isActive(int cs) {
	return (inds[cs] & GL.CSFLAG_ACTIVE) != 0;
    }

    Map actChilds = new HashMap();
    public void activateChildByCS(int cs, int childCS) {
	inds[cs] |= GL.CSFLAG_ACTIVE_REGION;
	actChilds.put(cs+"", ""+childCS);
    }
    public boolean hasActiveChildVS(int cs) {
	return (inds[cs] & GL.CSFLAG_ACTIVE_REGION) != 0;
    }
    public ChildVobScene getChildByCS(int cs) {
	return (ChildVobScene) childs.get(actChilds.get(""+cs));
    }
    
    public void activateRegion(int cs) {
	inds[cs] |= GL.CSFLAG_ACTIVE_REGION;
	normalActs.remove(cs+"");
	if (actRegs.get(cs+"") == null)
	    actRegs.put(cs+"", new HashSet());
    }
    private boolean isActiveRegion(int cs) {
	return (inds[cs] & GL.CSFLAG_ACTIVE_REGION) != 0;
    }
    private Map actRegs = new HashMap();
    private Set normalActs = new HashSet();
    private void checkActiveRegion(int cs_) {
	int cs = getParent(cs_);
	while(cs != -1) {
	    if (isActiveRegion(cs)) {
		//pa("found an active region: "+cs);
		((Set)actRegs.get(cs+"")).add(cs_+"");
		normalActs.remove(cs+"");
		return;
	    } else cs = getParent(cs);
	}
	normalActs.add(""+cs_);
    }
    public void printRegs() {
	for (Iterator i=normalActs.iterator(); i.hasNext();)
	    System.out.println("normal: "+i.next());

	for (Iterator i=actRegs.keySet().iterator(); i.hasNext();) {
	    Object key = i.next();
	    System.out.println("key: "+key);
	    for (Iterator j=((Set)actRegs.get(key)).iterator(); 
		 j.hasNext();) {
		System.out.println("   cs: "+j.next());
	    }
	}
    }


    public int[] getAllCSAt(int parent, float x, float y) {
	if(dbg) pa("getAllCSAt "+parent+" "+x+" "+y);
	//long go = System.currentTimeMillis();
	int[] inds = GL.getAllCSAt(ninds, this.inds,
				   floats, parent, x, y);
	//pa("Time: "+ (System.currentTimeMillis()-go));

	int[] retInts = new int[0];
	for(int i=0; i<inds.length; i++) {
	    if(isNearestActiveAncestor(inds[i], parent)) {
		// create a new array
		int tmpInts[] = new int[retInts.length + 1];
		System.arraycopy(retInts, 0, tmpInts, 0,retInts.length);
		tmpInts[retInts.length] = inds[i];
		retInts = tmpInts;
	    }
	}
	return retInts;
    }

    
    public int getCSAt(int parent, float x, float y,
		       float[] targetcoords) {

	if (dbg) pa("getCSAt "+parent+" "+x+" "+y+
		    " target: "+targetcoords);
	//long go = System.currentTimeMillis();
	int[] inds = GL.getAllCSAt(ninds, this.inds,  
				   this.floats, parent, x, y);
	//pa("Time: "+ (System.currentTimeMillis()-go));

	for(int i=0; i<inds.length; i++)
	    if(isNearestActiveAncestor(inds[i], parent)) {
		int cs = inds[i];
		if(targetcoords != null) {
		    float[] coords = new float[] {x, y, 0};
		    inverseTransformPoints3(cs, coords, coords);
		    targetcoords[0] = coords[0];
		    targetcoords[1] = coords[1];
		}
		return cs;
	    }
	return -1;
    }


    public int getChildCSAt(int[] activateCSs, 
			    int parent, 
			    float x, float y, 
			    float[] targetcoords) {
	int [] css = new int[activateCSs.length];
	for (int i=0; i<css.length; i++)
	    css[i] = Integer.parseInt((String)actChilds.get(""+activateCSs[i]));

	if (dbg) pa("getChildCSAt "+parent+" "+x+" "+y+" target: "+targetcoords);
	int[] inds = GL.getAllChildCSAt(ninds, this.inds, 
					activateCSs, css,
					this.floats, x, y);

	if (parent == -1) {
	    if (inds.length > 0)
		return inds[0];
	}
	else {
	    for(int i=0; i<inds.length; i++)
		if(isNearestActiveAncestor(inds[i], parent)) {
		    int cs = inds[i];
		    if(targetcoords != null) {
			float[] coords = new float[] {x, y, 0};
			inverseTransformPoints3(cs, coords, coords);
			targetcoords[0] = coords[0];
			targetcoords[1] = coords[1];
		    }
		    return cs;
		}
	}
	return -1;
    }




    /** Return true if there is not primary ancestor between
     * cs and parent which is active.
     */
    private boolean isNearestActiveAncestor(int cs, int parent) {
	cs = getParent(cs);
	while(cs != -1) {
	    if(cs == parent) return true;
	    if(isActive(cs)) return false;
	    cs = getParent(cs);
	}
	return false;
    }
    private boolean isAncestor(int cs, int parent) {
	while(cs != parent && cs != -1) 
	    cs = getParent(cs);
	return cs == parent;
    }

    public int getParent(int cs) {
	if(cs == 0) return -1;
	return inds[cs+1];
    }
    public void dump() {
	pa("GLVobCoorder: ");
	for(int i=0; i<ninds; i++) {
	    pa(" "+i+", parent: "+getParent(i));
	}
    }

    public void renderInterp(GraphicsAPI.RenderingSurface into, GLVobMap theVobs, int[] csinds,
			    GLVobCoorder other, float fract,
			    boolean standardcoords, boolean showFinal) {
	renderInterp(((GLScreen)into).getWindow(), theVobs, csinds, other, fract,
			    standardcoords, showFinal);
    }
    public void renderInterp(GL.RenderingSurface into, GLVobMap theVobs, int[] csinds, 
			    GLVobCoorder other0, float fract,
			    boolean standardcoords, boolean showFinal) {
	GLVobCoorderBase other = (GLVobCoorderBase)other0;
	GL.render(into, ninds, this.inds, this.floats,
		    csinds,
		    (other != null ? other.inds : null), 
		    (other != null ? other.floats : null),
		    theVobs.list, fract, standardcoords, showFinal);
    }
    public double timeRender(GL.RenderingSurface into, GLVobMap theVobs,
			   boolean standardcoords, boolean swapbuf, int iters) {
	return GL.timeRender(into, iters,
			ninds, inds, floats, 
			theVobs.list,
				standardcoords, swapbuf);
    }

    public boolean needInterp(VobCoorder interpTo, int[] interpList) {
        // XXX
	return true;
    }

    public float[] transformPoints3(int withCS, float[] points, float[]into) {
	if(into == null) into = new float[points.length];
	if( GL.transform(ninds, inds, floats, withCS, false, points, into))
	    return into;
	return null;
    }
    public float[] inverseTransformPoints3(int withCS, float[] points, float[]into) {
	if(into == null) into = new float[points.length];
	if( GL.transform(ninds, inds, floats, withCS, true, points, into))
	    return into;
	return null;
    }

    public boolean transformPoints3_interp(int[] interpList, GLVobCoorder other0,
			float fract, boolean show1, 
			int withCS, float[] points, float[]into) {
	GLVobCoorderBase other = (GLVobCoorderBase)other0;
	return GL.transform2(ninds, inds, floats, 
			interpList, other.inds, other.floats, 
			fract, show1, withCS, false, points, into);
    }
    public boolean inverseTransformPoints3_interp(int[] interpList, GLVobCoorder other0,
			float fract, boolean show1, 
			int withCS, float[] points, float[]into) {
	GLVobCoorderBase other = (GLVobCoorderBase)other0;
	return GL.transform2(ninds, inds, floats, 
			interpList, other.inds, other.floats, 
			fract, show1, withCS, true, points, into);
    }

    public void getSqSize(int cs, float[] into) {
	GL.transformSq(ninds, inds, floats, cs, into);
    }

    abstract public int concatInverse(int f, int g);
    // Then some simple implementations
    public int invert(int f) {
	return concatInverse(0, f);
    }

    Map childs = new HashMap();
    public int _putChildVobScene(ChildVobScene child, int[] cs) {
	int j=ninds;

	childs.put(""+j, child);
	GLChildVobScene glChildVobScene = (GLChildVobScene)child;
	if(glChildVobScene.nParamCoordsys != cs.length)
	    throw new Error("Invalid number of parameter coordsys!");
	if(glChildVobScene.childVS == null) {
	    glChildVobScene.childVS = ChildVS._createChildVS( 
		    (GLVobMap)child.map,
		    (GLVobCoorder)child.coords
		    );
	}

	inds[j+0] = -1; // Code for child vobscene
	inds[j+1] = glChildVobScene.childVS.getChildVSId(); // Child id
	inds[j+2] = cs.length;
	for(int i=0; i<cs.length; i++)
	    inds[j+3+i] = cs[i];

	ninds += 3+cs.length;

	return j;
    }

    public int exportChildCoordsys(int childVobSceneId, int nth) {
	int j=ninds;
	inds[j+0] = -2; // Code for child vobscene cs export
	inds[j+1] = childVobSceneId; // index in list
	inds[j+2] = nth;
	ninds += 3;
	return j;
    }
    
    /** Internal API: to be called right after creation,
     * to leave room for other coordsyses.
     */
    void setNumberOfParameterCS(int numberOfParameterCS) {
	ninds = numberOfParameterCS;
    }
}


