/*
AWTVobCoorder.java
 *
 *    Copyright (c) 2004-2005, Matti J. Katila and Benja Fallenstein
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
 * Written by Matti J. Katila and Benja Fallenstein
 */
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.util.math.Vect;
import java.util.*;

/** This is an internal base class for AWTVobCoorder.
 */
public abstract class AWTVobCoorderBase extends VobCoorder {
    public static boolean dbg = false;
    private static void p(String s) { System.out.println("AWTCoorder:: "+s); }


    float width, height;
    final DepthSorter sorter;
    public AWTVobCoorderBase(float w, float h) {
	width = w; height = h;
	sorter = new DepthSorter(this);
    }


    float[] floats = new float[512];
    int nfloats = 0;

    int[] inds = new int[128];
    int ninds = 1; // zero is special (the root)
    ChildVobScene[] children = new ChildVobScene[128];

    int[] cses = new int[128];
    int ncs = 1;

    public void clear() {
	nfloats = 0;
	ninds = 1;
	ncs = 1;

	otherCoorder = null;
	interpList = null;
	fract = 0;

	coordinates.maxcs = 0;

	sorter.invalidate();
    }


    /** A set of coordinates for each cs in this coorder -- there's one set
     *  for the coorder by itself, and one set for every interpolation frame.
     *  (XXX currently only one for the coorder and one per interpolation)
     */
    protected class Coordinates {
	float[] coords;
	int maxcs; // number of cs that have been put into this Coordinates
	boolean[] interpolated;

	void init() {
	    //p("init!");

	    if(coords == null || ninds*5 > coords.length)
		coords = new float[ninds*5];

	    Arrays.fill(coords, 0);
	    maxcs = ncs;

	    if(parentCoorder != null) {
		Coordinates parent = parentCoorder.coordinates;
		parent.check();
		for(int cs=0; cs<numberOfParameterCS; cs++) {
		    for(int i=0; i<5; i++) {
			int pcs = parentCoorder.inds[parentCS+3+cs];
			coords[5*cs+i] = parent.coords[5*pcs+i];
		    }
		}
	    }

	    for(int i=0; i<ncs; i++) {
		int cs = cses[i];
		//p("cs: "+cs);
		//p("cs: "+ (inds[cs] & (~GL.CSFLAGS)));

		if((inds[cs] & (~GL.CSFLAGS)) >= 0) {
		    Trans t = getTrans(cses[i]);
		    t.put(this);
		    //p("init "+i+"th ("+cses[i]+", type "+inds[cses[i]]+") to "+coords[5*cses[i]+0]+" "+coords[5*cses[i]+1]+" "+coords[5*cses[i]+2]+" "+coords[5*cses[i]+3]+" "+coords[5*cses[i]+4]);
		    t.pop();
		} else if(inds[cs] == -1) {
		    // child vobscene -- don't need to do anything
		} else if(inds[cs] == -2) {
		    // cs exported from child vs
		    ChildVobScene cvs = children[inds[cs+1]];
		    int ccs = inds[cs+2];

		    AWTVobCoorderBase child = (AWTVobCoorderBase)cvs.coords;
		    child.setParentCoorder(AWTVobCoorderBase.this, inds[cs+1]);
		    child.coordinates.check();
		    for(int k=0; k<5; k++) {
			coords[5*cs+k] = child.coordinates.coords[5*ccs+k];
		    }
		} else {
		    throw new Error("cs type "+inds[cs]);
		}
	    }

	    _interpList = null;
	}

	int[] _interpList;
	float _fract;

	void initInterpolation() {
	    if(coords == null || ninds*5 > coords.length)
		coords = new float[ninds*5];

	    if(interpolated == null || ninds > interpolated.length)
		interpolated = new boolean[ninds];

	    coordinates.check();
	    otherCoorder.coordinates.check();

	    Arrays.fill(coords, 0);
	    Arrays.fill(interpolated, false);

	    _interpList = interpList;
	    _fract = fract;

	    for(int i=numberOfParameterCS; i<ncs; i++) {
		int cs = cses[i];
		int ocs;

		if(cs < interpList.length)
		    ocs = interpList[cs];
		else
		    ocs = VobMatcher.SHOW_IN_INTERP;

		if(ocs >= 0) {
		    interpolated[cs] = true;

		    for(int j=0; j<5; j++) {
			coords[5*cs+j] = 
			    i(coordinates.coords[5*cs+j],
			      otherCoorder.coordinates.coords[5*ocs+j],
			      fract);
		    }
		} else if(ocs == VobMatcher.DONT_INTERP) {
		    interpolated[cs] = false;
		} else if(ocs == VobMatcher.SHOW_IN_INTERP) {
		    Trans t = getTrans(cs);

		    interpolated[cs] = true;
		    for(int k=0; k<t.nparents(); k++)
			if(!interpolated[inds[cs+k+1]])
			    interpolated[cs] = false;

		    if(interpolated[cs]) t.put(this);

		    t.pop();
		} else {
		    throw new UnsupportedOperationException("Interpolation type: "+ocs);
		}
	    }
	}

	void check() {
	    if(maxcs < ncs || coords == null /*|| ninds*5 > coords.length*/)
		init();
	}

	void checkInterpolation() {
	    if(interpList != _interpList || fract != _fract) 
		initInterpolation();
	}

	float get(int cs, int offs) {
	    return coords[5*cs+offs];
	}

	float x(int cs) { return get(cs, 0); }
	float y(int cs) { return get(cs, 1); }
	float sx(int cs) { return get(cs, 2); }
	float sy(int cs) { return get(cs, 3); }
	float d(int cs) { return get(cs, 4); }

	void setX(int cs, float value) { coords[5*cs] = value; }
	void setY(int cs, float value) { coords[5*cs+1] = value; }
	void setSX(int cs, float value) { coords[5*cs+2] = value; }
	void setSY(int cs, float value) { coords[5*cs+3] = value; }
	void setD(int cs, float value) { coords[5*cs+4] = value; }

	void copy(int from, int to) {
	    for(int i=0; i<5; i++) coords[5*to+i] = coords[5*from+i];
	}

	public void transform(int cs, float[] rect) {
	    check();

	    int i = 5*cs;

	    //if(!interpolated[cs]) return false;

	    rect[0] *= coords[i+2]; rect[1] *= coords[i+3];
	    rect[2] *= coords[i+2]; rect[3] *= coords[i+3];

	    rect[0] += coords[i]; rect[1] += coords[i+1];
	    rect[4] += coords[i+4];

	    //return true;
	}

	public boolean inverseTransform(int cs, float[] rect) {
	    check();

	    int i = 5*cs;
	    if(coords[i] < 0) return false;

	    rect[0] -= coords[i]; rect[1] -= coords[i+1];
	    rect[4] -= coords[i+4];

	    rect[0] /= coords[i+2]; rect[1] /= coords[i+3];
	    rect[2] /= coords[i+2]; rect[3] /= coords[i+3];

	    return true;
	}
    }


    protected final void addFloats(int n) { 
	nfloats += n; 
	if(nfloats > floats.length) {
	    float[] nf = new float[floats.length*2];
	    System.arraycopy(floats, 0, nf, 0, floats.length);
	    floats = nf;
	}
    }
    protected final void addInds(int n) { 
	// coordinate system added:

	if(ncs+1 > cses.length) {
	    int[] ncses = new int[cses.length*2];
	    System.arraycopy(cses, 0, ncses, 0, cses.length);
	    cses = ncses;
	}
	cses[ncs] = ninds;
	ncs++;

	sorter.invalidate();

	ninds += n; 
	if(ninds > inds.length) {
	    int[] ni = new int[inds.length*2];
	    ChildVobScene[] nc = new ChildVobScene[inds.length*2];
	    System.arraycopy(inds, 0, ni, 0, inds.length);
	    System.arraycopy(children, 0, nc, 0, inds.length);
	    inds = ni; children = nc;
	}
    }
    
    protected final void updateCoords(int cs) {
	coordinates.maxcs = 0; //cs-1;
	sorter.invalidate();
    }


    static float i(float a, float b, float fract)
	{ return (a + fract * (b-a)); }

    static void interpolate(float[] a, float[] b, float fract) {
	for(int i=0; i<a.length; i++) a[i] = i(a[i], b[i], fract);
    }


    float[] cs1rect = new float[5];
    float[] cs2rect = new float[5];
    float[] wh = new float[4];
    float[] scale = new float[2], scale2 = new float[2];

    protected void getAbsoluteRect(int cs, float[] into, float[] scale,
				   boolean useInterp) {
	into[0] = 0; into[1] = 0;
	into[2] = 1; into[3] = 1;
	into[4] = 0;

	if (dbg) {
	    for (int i=0; i<4; i++)
		p("info: "+into[i]);
	}

	if(!useInterp)
	    coordinates.transform(cs, into);
	else
	    interpCoordinates.transform(cs, into);
	
	Trans t = getTrans(cs);
	try {
	    //t.transformRect(into, useInterp);
	    t.getWH(wh, useInterp);
	    //scale[0] = t.sx(); scale[1] = t.sy();
	} finally {
	    t.pop();
	}

	scale[0] = into[2];
	scale[1] = into[3];

	into[2] = wh[0]*scale[0];
	into[3] = wh[1]*scale[1];
    }


    int[] interpList;
    AWTVobCoorderBase otherCoorder;
    float fract;

    Coordinates coordinates = new Coordinates();
    Coordinates interpCoordinates = new Coordinates();

    /**
     *  Return true if the coordinate system should be interpolated,
     *  false if one of its ancestors is DONT_INTERP in the interp list.
     */
    public boolean setInterpInfo(int cs, AWTVobCoorderBase other, 
				 int[] interpList, float fract,
				 OrthoRenderInfo info) {

	this.otherCoorder = other;
	this.interpList = interpList;
	this.fract = fract;

	interpCoordinates.checkInterpolation();
	if(!interpCoordinates.interpolated[cs]) return false;

	getAbsoluteRect(cs, cs1rect, scale, true);
	    
        info.setCoords(cs1rect[4],// depth
                       cs1rect[0], cs1rect[1], cs1rect[2], cs1rect[3],
                       scale[0], scale[1]);

	return true;
    }

    public void setInfo(int cs, OrthoRenderInfo info) {
	coordinates.check();

	getAbsoluteRect(cs, cs1rect, scale, false);
	    
        info.setCoords(cs1rect[4],// depth
                       cs1rect[0], cs1rect[1], cs1rect[2], cs1rect[3],
                       scale[0], scale[1]);
    }


    Trans noOp = new Trans(){
	    public String toString() { return "no op"; }
	    void put(Coordinates into) {
		into.copy(getParent(), cs());
	    }
	};

    Trans root = new Trans(){
	    public String toString() { return "root"; }
	    float sx() { return width; }
	    float sy() { return height; }
	    float w() { return 1; }
	    float h() { return 1; }
	    boolean has_own_wh() {
		return true;
	    }
	    void getWH(float[] wh, boolean useInterp) { wh[0] = wh[1] = 1; }

	    void put(Coordinates into) {
		into.setX(cs(), 0); 
		into.setY(cs(), 0);
		into.setSX(cs(), 1);
		into.setSY(cs(), 1);
		into.setD(cs(), 0);
	    }

	    int nparents() { return 0; }
	};

    
    Trans [] trans =  
    new Trans[] {
	noOp, // 0 rational1D22
	noOp, // 1 power1D
	noOp, // 2 power1D2
	noOp, // 3 distort
	noOp, // 4 cull
	new Trans() {   // 5 concat
	    public String toString() { return "concat"; }
	    void put(Coordinates into) {
		int p1 = inds[cs()+1], p2 = inds[cs()+2];
		into.setX(cs(), into.x(p2)*into.sx(p1) + into.x(p1));
		into.setY(cs(), into.y(p2)*into.sy(p1) + into.y(p1));
		into.setSX(cs(), into.sx(p2)*into.sx(p1));
		into.setSY(cs(), into.sy(p2)*into.sy(p1));
		into.setD(cs(), into.d(p2) + into.d(p1));
	    }
	    int nparents() { return 2; }
	},
	new Trans() {   // 6 concatInverse
	    public String toString() { return "concat inverse"; }
	    void put(Coordinates into) {
		int p1 = inds[cs()+1], p2 = inds[cs()+2];
		into.setX(cs(), -into.x(p2)*into.sx(p1)/into.sx(p2)+into.x(p1));
		into.setY(cs(), -into.y(p2)*into.sy(p1)/into.sy(p2)+into.y(p1));
		into.setSX(cs(), into.sx(p1)/into.sx(p2));
		into.setSY(cs(), into.sy(p1)/into.sy(p2));
		into.setD(cs(), into.d(p1) - into.d(p2));
	    }
	    int nparents() { return 2; }
	},
	new Trans() {   // 7 translate
	    public String toString() { return "translate"; }
	    void put(Coordinates into) {
		int f = inds[cs()+2];
		into.copy(getParent(), cs());
		into.setX(cs(), into.x(cs()) + floats[f+0]*into.sx(cs()));
		into.setY(cs(), into.y(cs()) + floats[f+1]*into.sy(cs()));
		into.setD(cs(), into.d(cs()) + floats[f+2]);
		//p("put transl "+getParent()+" "+floats[f+0]+" "+floats[f+1]+" "+floats[f+2]);
	    }
	},
	new Trans() {   // 8 scale
	    public String toString() { return "scale"; }
	    void put(Coordinates into) {
		int f = inds[cs()+2];
		into.copy(getParent(), cs());
		into.setSX(cs(), into.sx(cs()) * floats[f+0]);
		into.setSY(cs(), into.sy(cs()) * floats[f+1]);

		if(floats[f+2] != 1) throw new Error("not implemented");
		//into.setD(cs(), into.d(cs()) * floats[f+2]);
	    }
	    float sx() {
		int f = inds[cs()+2];
		return floats[f+0];
	    }
	    float sy() {
		int f = inds[cs()+2];
		return floats[f+1];
	    }
	}, 
	noOp, // 9 rotate
	noOp, // 10 nadirUnitSq
	noOp, // 11 unit
	new Trans() {   // 12 box
	    public String toString() { return "box"; }
	    void put(Coordinates into) {
		into.copy(getParent(), cs());
	    }
	    float w() {
		int f = inds[cs()+2];
		return floats[f+0];
	    }
	    float h() {
		int f = inds[cs()+2];
		return floats[f+1];
	    }
	    boolean has_own_wh() {
		return true;
	    }
	},
	noOp, // 13 rotateXYZ
	noOp, // 14 rotateQuaternion	    
	noOp, // 15 affine
	new Trans() {   // 16 ortho
	    public String toString() { return "ortho"; }
	    void put(Coordinates into) {
		int f = inds[cs()+2];
		into.setX(cs(), floats[f+1]*into.sx(getParent()) + 
			        into.x(getParent()));
		into.setY(cs(), floats[f+2]*into.sy(getParent()) + 
			        into.y(getParent()));
		into.setSX(cs(), floats[f+3]*into.sx(getParent()));
		into.setSY(cs(), floats[f+4]*into.sy(getParent()));
		into.setD(cs(), floats[f+0] + into.d(getParent()));
	    }
	    float sx() {
		int f = inds[cs()+2];
		return floats[f+3];
	    }
	    float sy() {
		int f = inds[cs()+2];
		return floats[f+4];
	    }
	}, 
	noOp, // 17 buoyOnCircle1
	new Trans() { // 18 buoyOnCircle2
	    public String toString() { return "buoyOnCircle2"; }

	    /** copied from include/vob/geom/Quadrics.hxx
	     *  coded by Tuomas J. Lukka
	     */
	    Vect project2circle(Vect pt, Vect p, Vect ctr, 
				float rad
				/*boolean *success = 0*/) {
		int ansdir = 1;

		//p("pt: "+pt+", p: "+p+", ctr: "+ctr);

		Vect ao = pt.neg(ctr);
		Vect ap = pt.neg(p);

		//p("ao: "+ao);
		//p("ap: "+ap);

		// Coefficients of the 2nd degree equation
		float a = ap.scalar(ap);
		float b = 2*ap.scalar(ao);
		float c = ao.scalar(ao) - rad * rad;

		// determinant of the equation
		float det = b*b - 4*a*c;

		float ans = (float) (det > 0 ? (-b + ansdir * Math.sqrt(det)) / (2*a) : 0);
		//if(success) *success = (det > 0);

		//p("a: "+a+", b: "+b+", c: "+c+", det: "+det+", ans: "+ans);

		return pt.sum(ap.mul(ans));
	    }

	    float[][] arrays = new float[512][];
	    int usedArrays = 0;

	    float[] getarr() {
		float[] arr = arrays[usedArrays];
		if(arr == null) arr = arrays[usedArrays] = new float[5];
		for(int i=0; i<5; i++) arr[i] = 0;
		usedArrays++;
		return arr;
	    }
	    void releaseArrays(int n) { usedArrays -= n; }

	    /** copied/modified from include/vob/trans/LinearPrimitives.hxx 
	     *  coded by Tuomas J. Lukka
	     */
	    void put(Coordinates into) {
		int vectorCount = Vect.currentVectorCount();

		try {
		    int areaCS = inds[cs()+1];
		    int anchorCS = inds[cs()+2];

		    // get absolute area
		    /*
		    float[] areaF = getarr();
		    float[] areaScale = getarr();
		    getAbsoluteRect(areaCS, areaF, areaScale, useInterp);
		    */


		    // get anchor, compute sq of the area

		    float[] anchorCoords = getarr();
		    getSqSize(anchorCS, anchorCoords);

		    anchorCoords[0] *= .5f; anchorCoords[1] *= .5f;

		    into.transform(anchorCS, anchorCoords);
		    into.inverseTransform(areaCS, anchorCoords);

		    if(Float.isNaN(anchorCoords[0])) {
			for(int i=0; i<anchorCoords.length; i++)
			    anchorCoords[i] = 0;
		    }

		    float[] sqF = getarr();
		    getSqSize(areaCS, sqF);

		    anchorCoords[0] /= sqF[0];
		    anchorCoords[1] /= sqF[1];

		    Vect anchor = Vect.get(anchorCoords[0], anchorCoords[1]);

		
		    float shift = floats[inds[cs()+3]+1] / sqF[0];
		    float direction = floats[inds[cs()+3]];
		    int dir = direction > 0 ? 1 : -1;

		    Vect shifted = Vect.get((float) anchor.x() + dir * shift, anchor.y());
		    Vect ctr = Vect.get(.5f, .5f);
		    Vect buoy;

		    if(shifted.neg(ctr).abs() >= .5) {
			buoy = shifted;
		    } else {
			if(anchor.neg(ctr).abs() >= .5) {
			    buoy = anchor;
			} else {
			    Vect v = ctr.sum(Vect.get(.5f,0).mul(-dir));
			    buoy = project2circle(anchor, v, ctr, .5f);
			}
		    }

		    //p("final: "+buoy);
		    
		    float scale = 1 - (anchor.neg(ctr)).abs() / .5f;
		    if(scale < shift) scale = shift;

		    into.setX(cs(), buoy.x() * sqF[0] * into.sx(areaCS) + into.x(areaCS));
		    into.setY(cs(), buoy.y() * sqF[1] * into.sy(areaCS) + into.y(areaCS));
		    into.setSX(cs(), scale * into.sx(areaCS));
		    into.setSY(cs(), scale * into.sy(areaCS));
		    into.setD(cs(), into.d(areaCS) - scale);

		    //releaseArrays(4);
		    releaseArrays(2);
		} finally {
		    Vect.releaseVectors(vectorCount);
		}
	    }

	    float w() { return 1; }
	    float h() { return 1; }
	    boolean has_own_wh() {
		return true;
	    }
	    int nparents() { return 2; }
	},
	new Trans() {   // 19 orthoBox
	    public String toString() { return "orthoBox"; }
	    void put(Coordinates into) {
		int f = inds[cs()+2];
		into.setX(cs(), floats[f+1]*into.sx(getParent()) + 
			        into.x(getParent()));
		into.setY(cs(), floats[f+2]*into.sy(getParent()) + 
			        into.y(getParent()));
		into.setSX(cs(), floats[f+3]*into.sx(getParent()));
		into.setSY(cs(), floats[f+4]*into.sy(getParent()));
		into.setD(cs(), floats[f+0] + into.d(getParent()));
	    }
	    float sx() {
		int f = inds[cs()+2];
		return floats[f+3];
	    }
	    float sy() {
		int f = inds[cs()+2];
		return floats[f+4];
	    }
	    float w() {
		int f = inds[cs()+2];
		return floats[f+5];
	    }
	    float h() {
		int f = inds[cs()+2];
		return floats[f+6];
	    }
	    boolean has_own_wh() {
		return true;
	    }
	}, 
	new Trans() {   // 20 unitSq
	    void put(Coordinates into) {
		into.copy(getParent(), cs());
		into.setSX(cs(), sx()*into.sx(cs()));
		into.setSY(cs(), sy()*into.sy(cs()));
	    }
	    float sx() {
		Trans t = getParentTrans();
		try {
		    return t.w();
		} finally {
		    t.pop();
		}
	    }
	    float sy() {
		Trans t = getParentTrans();
		try {
		    return t.h();
		} finally {
		    t.pop();
		}
	    }
	    float w() {
		return 1;
	    }
	    float h() {
		return 1;
	    }
	    boolean has_own_wh() {
		return true;
	    }
	},
	new Trans() {   // 21 between
	    void put(Coordinates into) {
		int p1 = inds[cs()+1], p2 = inds[cs()+2];
		into.setX(cs(), (into.x(p1)+into.x(p2)) / 2);
		into.setY(cs(), (into.y(p1)+into.y(p2)) / 2);

		float sx1 = into.sx(p1), sx2 = into.sx(p2);
		into.setSX(cs(), sx1<sx2 ? sx1 : sx2);

		float sy1 = into.sy(p1), sy2 = into.sy(p2);
		into.setSY(cs(), sy1<sy2 ? sy1 : sy2);

		float d1 = into.d(p1), d2 = into.d(p2);
		into.setD(cs(), d1>d2 ? d1 : d2);
	    }
	    int nparents() { return 2; }
	},
	new Trans() {   // 22 translatePolar
	    void put(Coordinates into) {
		into.copy(getParent(), cs());

		int f = inds[cs()+2];

		float dist = floats[f+0];
		float angle = floats[f+1];

		float sin = (float)Math.sin(angle * Math.PI / 180);
		float cos = (float)Math.cos(angle * Math.PI / 180);

		into.setX(cs(), into.x(cs()) + dist*cos*into.sx(cs()));
		into.setY(cs(), into.y(cs()) + dist*sin*into.sy(cs()));
	    }
	},
    };
    
    Trans getTrans(int cs) {
	if (dbg) p("cs "+cs+ ", "+inds[cs]+(parentCoorder ==null));
	    
	if(cs < numberOfParameterCS) {
	    // it may also be export!
	    // no it may not because cs < numberOfParameterCS
	    if (parentCoorder == null) {
		root.push(cs);
		return root;
	    }

	    // there is parent coorder...

	    // go to parent vob coorder

	    int n = cs;
	    int len = parentCoorder.inds[parentCS+2];
	    if (n >= len) throw new Error("cs too big");
	    int c = parentCoorder.inds[parentCS+3+n];
	    return parentCoorder.getTrans(c);
	}

	// check child coorder, i.e., parent: -1 = put 
	// and -2 = export
	if (inds[cs] == -1) return noOp;

	if (inds[cs] == -2) {
	    ChildVobScene cvs = children[inds[cs+1]];
	    AWTVobCoorderBase coords = (AWTVobCoorderBase)cvs.coords;
	    coords.setParentCoorder(this, inds[cs+1]);
	    return coords.getTrans(inds[cs+2]);
	}

	// the code below this should be in somewhere else.
	// CSFLAGS already make everything negative...
	//if((inds[cs] & (~GL.CSFLAGS)) < -2)
	//    throw new Error("Help! Wrong coordsys: "+cs);

	if (dbg) p(", ind: "+inds[cs]+", "+isActive(cs));
	Trans t = trans[inds[cs] & (~GL.CSFLAGS)];
	t.push(cs);
	return t;
    }
    

    public void check() {
	for (int i=0; i<trans.length; i++) 
	    if (trans[i].csInd != 0)
		throw new Error(trans[i] +" is guilty!");
	if(parentCoorder != null)
	    parentCoorder.check();
    }


    float[] tmprect = new float[5];
    
    public abstract class Trans {
	int cs[] = new int[64];
	int csInd = 0;
	void push(int cs) { 
	    if (dbg) {
		System.out.print(csInd+": ");
		for (int i=0; i<csInd; i++)
		    System.out.print(" ");
		System.out.println("push "+cs+", "+this+ ", "+(parentCoorder ==null));
	    }
	    this.cs[++csInd] = cs; 
	}
	int cs() { return cs[csInd]; }
	void pop() { 
	    if (dbg)
		System.out.println("pop "+ cs() +", "+this+ ", "+(parentCoorder ==null));
	    csInd--; 
	}

	int getParent() { return inds[cs()+1]; }
	Trans getParentTrans() { return getTrans(inds[cs()+1]); }

	/** how many parents (param coordinate systems) this cs type has */
	int nparents() { return 1; }

	abstract void put(Coordinates into);

	float sx() { 
	    Trans p = getParentTrans();
	    float x = p.sx();
	    p.pop();
	    return x; 
	}
	float sy() { 
	    Trans p = getParentTrans();
	    float y = p.sy();
	    p.pop();
	    return y; 
	}
	float w() {
	    throw new UnsupportedOperationException("w()");
	}
	float h() { 
	    throw new UnsupportedOperationException("h()");
	}

	boolean has_own_wh() {
	    return false;
	}

	void getWH(float[] wh, boolean useInterp) {
	    int ocs;

	    if(useInterp && cs() < interpList.length)
		ocs = interpList[cs()];
	    else
		ocs = VobMatcher.SHOW_IN_INTERP;

	    if(ocs >= 0) {
		Trans o = otherCoorder.getTrans(ocs);
		try {
		    getWH(wh, false);
		    float w1 = wh[0], h1 = wh[1];
		    o.getWH(wh, false);
		    float w2 = wh[0], h2 = wh[1];

		    wh[0] = i(w1, w2, fract);
		    wh[1] = i(h1, h2, fract);

		    // don't let w/h grow too small: it doesn't look good
		    // when a box goes to almost zero size during the "bounce"
		    // when fract > 1 at the end of the animation
		    if(wh[0] < Math.min(w1, w2)) wh[0] = Math.min(w1, w2);
		    if(wh[1] < Math.min(h1, h2)) wh[1] = Math.min(h1, h2);
		} finally {
		    o.pop();
		}
	    } else if(ocs == VobMatcher.DONT_INTERP) {
		wh[0] = 1; wh[1] = 1;
	    } else if(ocs == VobMatcher.SHOW_IN_INTERP) {
		if(has_own_wh()) {
		    wh[0] = w(); wh[1] = h();
		} else {
		    Trans t = getParentTrans();
		    t.getWH(wh, useInterp);
		    t.pop();
		}
	    }		
	}
    }


    public void getSqSize(int cs, float[] into) {
	Trans t = getTrans(cs);
	t.getWH(into, false);
	t.pop();
    }


    public float[] transformPoints3(int withCS, float[] pt, float[]into) {
	if(into == null)
	    into = new float[pt.length];

	coordinates.check();
	float x  = coordinates.x(withCS);
	float y  = coordinates.y(withCS);
	float sx = coordinates.sx(withCS);
	float sy = coordinates.sy(withCS);
	float d  = coordinates.d(withCS);

	for(int i=0; i<pt.length; i+=3) {
	    into[i + 0] = x + pt[i + 0];
	    into[i + 0] *= sx;
	    into[i + 1] = y + pt[i + 1];
	    into[i + 1] *= sy;
	    into[i + 2] = d + pt[i + 2];
	}
	return into;
    }

    public float[] inverseTransformPoints3(int withCS, float[] pt, 
					   float[]into) {
	if(into == null)
	    into = new float[pt.length];

	coordinates.check();
	float x  = coordinates.x(withCS);
	float y  = coordinates.y(withCS);
	float sx = coordinates.sx(withCS);
	float sy = coordinates.sy(withCS);
	float d  = coordinates.d(withCS);

	for(int i=0; i<pt.length; i+=3) {
	    into[i + 0] = pt[i + 0] - x;
	    into[i + 0] /= sx;
	    into[i + 1] = pt[i + 1] - y;
	    into[i + 1] /= sy;
	    into[i + 2] = pt[i + 2] - d;
	}
	return into;
    }





    int parentCS = 0;
    AWTVobCoorderBase parentCoorder = null;


    public void activate(int cs) {
	inds[cs] |= GL.CSFLAG_ACTIVE;
	//checkActiveRegion(cs);
    }

    private boolean isActive(int cs) {
	return (inds[cs] & GL.CSFLAG_ACTIVE) != 0;
    }
    public int getParent(int cs) {
	if(cs == 0) return -1;
	return inds[cs+1];
    }

    public void dump() {
	// XXX check DepthSorter...

	p("Coorder dump!");
	for(int i=0; i<ninds; i++) {
	    p("   "+i+", parent: "+getParent(i));
	}
    }

    public boolean needInterp(VobCoorder interpTo0, int[] interpList) {
        AWTVobCoorderBase interpTo = (AWTVobCoorderBase)interpTo0;
	for(int my=0; my<interpList.length; my++) {

	    int other = interpList[my];

	    if(other > 0)
	        if(needInterp(my, interpTo, other)) return true;
	}
	return false;
    }

    public boolean needInterp(int cs1, AWTVobCoorderBase coords2, int cs2) {
	AWTVobCoorderBase coords1 = this;

	coords1.getAbsoluteRect(cs1, cs1rect, scale, false);
	coords2.getAbsoluteRect(cs2, cs2rect, scale, false);

	float 
	    x1 = cs1rect[0], y1 = cs1rect[1], w1 = cs1rect[2], h1 = cs1rect[3],
	    x2 = cs2rect[0], y2 = cs2rect[1], w2 = cs2rect[2], h2 = cs2rect[3];

	if(Math.abs(x1 - x2) + Math.abs(y1 - y2) + 
	   Math.abs(w1 - w2) + Math.abs(h1 - h2) > 5) // heuristic
	    
	    return true;
	else
	    return false;
    }








    abstract public int concatInverse(int f, int g);
    // Then some simple implementations
    public int invert(int f) {
	return concatInverse(0, f);
    }

    public boolean isChildVS(int cs) { 
	return children[cs] != null;
    }

    public int _putChildVobScene(ChildVobScene child, int[] cs) {
	int j=ninds;

	children[j] = child;

	inds[j+0] = -1; // Code for child vobscene
	inds[j+1] = j;
	inds[j+2] = cs.length;
	for(int i=0; i<cs.length; i++)
	    inds[j+3+i] = cs[i];

	addInds(3+cs.length);

	return j;
    }


    // Stupid implementation...
    public int getCSAt(int parent, float x, float y, float[] internalcoords) {

	sorter.sort();
	int[] sorted = sorter.sorted;
	int nsorted = sorter.nsorted;

	float d[] = new float[5];

	for(int i=nsorted-1; i>=0; i--) {

	    /*
	      p("is active? "+isActive(sorted[i]));
	      p("parent? "+getParent(sorted[i]));
	    */
	    if(isActive(sorted[i]) && 
	       (getParent(sorted[i]) == parent || parent == -1)) {

		Trans t = getTrans(sorted[i]);
		d[0] = d[1] = 0;
		d[2] = d[3] = 1;

		if (t.has_own_wh()) {
		    d[2] = t.w();
		    d[3] = t.h();
		}
		coordinates.transform(sorted[i], d);
		t.pop();


		if (x >= d[0] && y >= d[1] &&
		    x < d[0]+d[2] && y < d[1]+d[3]) {
		    if(internalcoords != null) {
			internalcoords[0] = (x-d[0])/(d[2]);
			internalcoords[1] = (y-d[1])/(d[3]);
		    }
		    return sorted[i];
		}
	    }
	}
	return -1;
    }



    public int exportChildCoordsys(int childVobSceneId, int nth) {
	int j=ninds;
	inds[j+0] = -2; // Code for child vobscene cs export
	inds[j+1] = childVobSceneId; // index in list
	inds[j+2] = nth;
	addInds(3);
	return j;
    }
    
    /** Internal API: to be called right after creation,
     * to leave room for other coordsyses.
     */
    void setNumberOfParameterCS(int numberOfParameterCS) {
	ninds = this.numberOfParameterCS = numberOfParameterCS;
    }
    int numberOfParameterCS = 1;










    Map actChildren = new HashMap();
    public void activateChildByCS(int cs, int childCS) {
	inds[cs] |= GL.CSFLAG_ACTIVE_REGION;
	actChildren.put(cs+"", ""+childCS);
    }
    public boolean hasActiveChildVS(int cs) {
	return (inds[cs] & GL.CSFLAG_ACTIVE_REGION) != 0;
    }
    public ChildVobScene getChildByCS(int cs) {
	return children[Integer.parseInt((String) actChildren.get(""+cs))];
    }


    void setParentCoorder(AWTVobCoorderBase coorder, int cs) {
	if(parentCoorder == coorder && parentCS == cs) return;

	parentCoorder = coorder;
	parentCS = cs;
	coordinates.maxcs = interpCoordinates.maxcs = 0;
    }


    public int getChildCSAt(int[] activateCSs, 
			    int parent, 
			    float x, float y, 
			    float[] targetcoords) {
	int [] css = new int[activateCSs.length];
	for (int i=0; i<css.length; i++)
	    css[i] = Integer.parseInt((String)actChildren.get(""+activateCSs[i]));

	ChildVobScene cvs = children[css[0]];
	((AWTVobCoorderBase)cvs.coords).setParentCoorder(this, css[0]);
	for (int i=1; i<css.length; i++) {
	    ChildVobScene oldCvs = cvs;
	    cvs = ((AWTVobCoorderBase)cvs.coords).children[css[i]];
	    ((AWTVobCoorderBase)cvs.coords).setParentCoorder(
                (AWTVobCoorderBase)oldCvs.coords, css[i]);
	}

	return cvs.getCSAt(parent, x,y, targetcoords);
    }


}


