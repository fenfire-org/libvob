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

	coordinates.dirty = true;
    }


    /** A set of coordinates for each cs in this coorder -- there's one set
     *  for the coorder by itself, and one set for every interpolation frame.
     *  (XXX currently only one for the coorder and one per interpolation)
     */
    protected class Coordinates {
	float[] coords;
	boolean dirty;
	boolean[] interpolated;

	void init() {
	    p("init!");

	    if(coords == null || ninds*5 > coords.length)
		coords = new float[ninds*5];

	    Arrays.fill(coords, 0);
	    dirty = false;

	    for(int i=0; i<ncs; i++) {
		Trans t = getTrans(cses[i]);
		t.put(this);
		//p("init "+i+"th ("+cses[i]+", type "+inds[cses[i]]+") to "+coords[5*cses[i]+0]+" "+coords[5*cses[i]+1]+" "+coords[5*cses[i]+2]+" "+coords[5*cses[i]+3]+" "+coords[5*cses[i]+4]);
		t.pop();
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

	    for(int i=0; i<ncs; i++) {
		int cs = cses[i];
		int ocs;

		if(cs < interpList.length)
		    ocs = interpList[cs];
		else
		    ocs = VobMatcher.SHOW_IN_INTERP;

		if(ocs >= 0) {
		    Trans t = getTrans(cs);
		    interpolated[cs] = !t.isDontInterpSet();
		    t.pop();

		    if(interpolated[cs]) {
			for(int j=0; j<5; j++) {
			    coords[5*cs+j] = 
				i(coordinates.coords[5*cs+j],
				  otherCoorder.coordinates.coords[5*ocs+j],
				  fract);
			}
		    }
		} else if(ocs == VobMatcher.DONT_INTERP) {
		    interpolated[cs] = false;
		} else if(ocs == VobMatcher.SHOW_IN_INTERP) {
		    Trans t = getTrans(cses[i]);
		    t.put(this);
		    t.pop();
		    interpolated[cs] = true;
		} else {
		    throw new UnsupportedOperationException("Interpolation type: "+ocs);
		}
	    }
	}

	void check() {
	    if(dirty || coords == null || ninds*5 > coords.length)
		init();
	}

	void checkInterpolation() {
	    if(interpList != _interpList || fract != _fract) 
		initInterpolation();
	}

	float x(int cs) { return coords[5*cs]; }
	float y(int cs) { return coords[5*cs+1]; }
	float sx(int cs) { return coords[5*cs+2]; }
	float sy(int cs) { return coords[5*cs+3]; }
	float d(int cs) { return coords[5*cs+4]; }

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
	if(ncs+1 > cses.length) {
	    int[] ncses = new int[cses.length*2];
	    System.arraycopy(cses, 0, ncses, 0, cses.length);
	    cses = ncses;
	}
	cses[ncs] = ninds;
	ncs++;

	ninds += n; 
	if(ninds > inds.length) {
	    int[] ni = new int[inds.length*2];
	    ChildVobScene[] nc = new ChildVobScene[inds.length*2];
	    System.arraycopy(inds, 0, ni, 0, inds.length);
	    System.arraycopy(children, 0, nc, 0, inds.length);
	    inds = ni; children = nc;
	}
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
	    void doTransformRect(float[] rect, boolean useInterp) { 
		transformRect(getParent(), rect, useInterp);
	    }
	};

    Trans root = new Trans(){
	    public String toString() { return "root"; }
	    void doTransformRect(float[] rect, boolean useInterp) { 
	    }
	    float sx() { return 1; }
	    float sy() { return 1; }
	    float w() { return width; }
	    float h() { return height; }
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
	    void doTransformRect(float[] rect, boolean useInterp) { 
		transformRect(inds[cs()+2], rect, useInterp);
		transformRect(inds[cs()+1], rect, useInterp);
	    }
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
	    void doTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(inds[cs()+2], rect, useInterp);
		transformRect(inds[cs()+1], rect, useInterp);
	    }
	    void put(Coordinates into) {
		int p1 = inds[cs()+1], p2 = inds[cs()+2];
		into.setX(cs(), into.x(p2)*into.sx(p1)/into.sx(p2)+into.x(p1));
		into.setY(cs(), into.y(p2)*into.sy(p1)/into.sy(p2)+into.y(p1));
		into.setSX(cs(), into.sx(p1)/into.sx(p2));
		into.setSY(cs(), into.sy(p1)/into.sy(p2));
		into.setD(cs(), into.d(p1) - into.d(p2));
	    }
	    int nparents() { return 2; }
	},
	new Trans() {   // 7 translate
	    public String toString() { return "translate"; }
	    void doTransformRect(float[] rect, boolean useInterp) { 
		int f = inds[cs()+2];
		for (int i=0; i<2; i++)
		    rect[i] += floats[f+i];
		rect[4] += floats[f+2]; // depth

		transformRect(getParent(), rect, useInterp);
	    }
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
	    void doTransformRect(float[] rect, boolean useInterp) { 
		int f = inds[cs()+2];
		for (int i=0; i<2; i++) {
		    rect[i] *= floats[f+i];
		    rect[i+2] *= floats[f+i];
		}
		rect[4] *= floats[f+2]; // depth

		transformRect(getParent(), rect, useInterp);
	    }
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
	    void doTransformRect(float[] rect, boolean useInterp) { 
		transformRect(getParent(), rect, useInterp);
	    }
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
	},
	noOp, // 13 rotateXYZ
	noOp, // 14 rotateQuaternion	    
	noOp, // 15 affine
	new Trans() {   // 16 ortho
	    public String toString() { return "ortho"; }
	    void doTransformRect(float[] rect, boolean useInterp) { 
		int f = inds[cs()+2];
		rect[4] += floats[f+0]; // depth

		rect[0] *= floats[f+3];
		rect[1] *= floats[f+4];
		rect[2] *= floats[f+3];
		rect[3] *= floats[f+4];
		for (int i=0; i<2; i++)
		    rect[i] += floats[f+1+i];

		transformRect(getParent(), rect, useInterp);
	    }
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

	    /** copied from include/vob/trans/LinearPrimitives.hxx 
	     *  coded by Tuomas J. Lukka
	     */
	    void doTransformRect(float[] rect, boolean useInterp) {
		int vectorCount = Vect.currentVectorCount();

		try {
		    int areaCS = inds[cs()+1];
		    int anchorCS = inds[cs()+2];

		    // get absolute area
		    float[] areaF = getarr();
		    float[] areaScale = getarr();
		    getAbsoluteRect(areaCS, areaF, areaScale, useInterp);

		    /*
		    // compute sq of the area

		    sqF[0] = areaF[2] - areaF[0];
		    sqF[1] = areaF[3] - areaF[1];

		    // get anchor
		    float[] anchorF = new float[5];
		    getAbsoluteRect(anchorCS, anchorF, 
		    new float[]{1,1},useInterp);
		    */


		    float[] anchorCoords = getarr();
		    getSqSize(anchorCS, anchorCoords);

		    anchorCoords[0] *= .5f; anchorCoords[1] *= .5f;

		    Trans t = getTrans(anchorCS);
		    t.transformRect(anchorCoords, useInterp);
		    t.pop();

		    t = getTrans(areaCS);
		    t.inverseTransformRect(anchorCoords, useInterp);
		    t.pop();

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
		    rect[4] -= scale; // depth
		    rect[0] = (buoy.x() * sqF[0] + scale*rect[0])*areaScale[0] + areaF[0];
		    rect[1] = (buoy.y() * sqF[1] + scale*rect[1])*areaScale[1] + areaF[1];
		    rect[2] *= scale * areaScale[0];
		    rect[3] *= scale * areaScale[1];

		    releaseArrays(4);
		} finally {
		    Vect.releaseVectors(vectorCount);
		}
	    }

	    void put(Coordinates into) {
		float[] f = getarr();
		f[2] = f[3] = 1;
		doTransformRect(f, false); // XXX useInterp shouldn't always be 'false', I suppose
		into.setX(cs(), f[0]);
		into.setY(cs(), f[1]);
		into.setSX(cs(), f[2]);
		into.setSY(cs(), f[3]);
		into.setD(cs(), f[4]);
		releaseArrays(1);
	    }

	    float w() { return 1; }
	    float h() { return 1; }
	    int nparents() { return 2; }
	},
	new Trans() {   // 19 orthoBox
	    public String toString() { return "orthoBox"; }
	    void doTransformRect(float[] rect, boolean useInterp) { 
		int f = inds[cs()+2];

		rect[4] += floats[f+0]; // depth

		rect[0] *= floats[f+3];
		rect[1] *= floats[f+4];
		rect[2] *= floats[f+3];
		rect[3] *= floats[f+4];
		for (int i=0; i<2; i++)
		    rect[i] += floats[f+1+i];

		transformRect(getParent(), rect, useInterp);
	    }
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
	}, 
	new Trans() {   // 20 unitSq
	    void doTransformRect(float[] rect, boolean useInterp) { 
		//System.out.println(rect[0]+" "+rect[1]+" "+rect[2]+" "+rect[3]);
		rect[0] *= sx();
		rect[1] *= sy();
		rect[2] *= sx();
		rect[3] *= sy();

		//System.out.println(rect[0]+" "+rect[1]+" "+rect[2]+" "+rect[3]);
		transformRect(getParent(), rect, useInterp);

		//System.out.println(rect[0]+" "+rect[1]+" "+rect[2]+" "+rect[3]);
	    }
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
	},
	new Trans() {   // 21 between
	    void doTransformRect(float[] rect, boolean useInterp) { 
		int a = inds[cs()+1];
		int b = inds[cs()+2];
		float[] af = new float[5];
		float[] bf = new float[5];

		float[] scale = new float[5];

		getAbsoluteRect(a, af, scale, useInterp);
		getAbsoluteRect(b, bf, scale, useInterp);
		
		for (int i=0; i<2; i++)
		    rect[i] +=  .5*(af[i]+bf[i]);

		rect[4] += (af[4] > bf[4]) ? af[4] : bf[4];
	    }
	    void put(Coordinates into) {
		int p1 = inds[cs()+1], p2 = inds[cs()+2];
		into.setX(cs(), (into.x(p1)+into.x(p2)) / 2);
		into.setY(cs(), (into.y(p1)+into.y(p2)) / 2);
		into.setSX(cs(), 1); // XXX
		into.setSY(cs(), 1); // XXX

		float d1 = into.d(p1), d2 = into.d(p2);
		into.setD(cs(), d1>d2 ? d1 : d2);
	    }
	    int nparents() { return 2; }
	},
	new Trans() {   // 22 translatePolar
	    void doTransformRect(float[] rect, boolean useInterp) { 
		int f = inds[cs()+2];

		float dist = floats[f+0];
		float angle = floats[f+1];

		float sin = (float)Math.sin(angle * Math.PI / 180);
		float cos = (float)Math.cos(angle * Math.PI / 180);

		rect[0] += dist*cos;
		rect[1] += dist*sin;
	    }
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
	if (dbg) p("cs "+cs+ ", "+inds[cs]+(parentCoordsys ==null));
	if (cs < numberOfParameterCS || (inds[cs] < 0 && inds[cs] > -3)) {
	    
	    // it may also be export!
	    // no it may not because cs < numberOfParameterCS
	    if (parentCoordsys == null) {
		root.push(cs);
		return root;
	    }

	    // there is parent coorder...

	    if((cs >= 0) && (cs < numberOfParameterCS)) {
		// go to parent vob coorder

		int n = cs;
		int len = parentCoordsys.inds[parentCS+2];
		if (n >= len) throw new Error("cs too big");
		int c = parentCoordsys.inds[parentCS+3+n];
		return parentCoordsys.getTrans(c);
	    }
	    // check child coorder, i.e., parent: -1 = put 
	    // and -2 = export
	    if (inds[cs] == -1) return noOp;

	    if (inds[cs] == -2) {
		ChildVobScene cvs = children[inds[cs+1]];
		AWTVobCoorderBase coords = (AWTVobCoorderBase)cvs.coords;
		coords.parentCoordsys = this;
		coords.parentCS = inds[cs+1];
		return coords.getTrans(inds[cs+2]);
	    }
	    throw new Error("Help! Wrong coordsys: "+cs);
	} else {
	    if (dbg) p(", ind: "+inds[cs]+", "+isActive(cs));
	    Trans t = trans[inds[cs] & (~GL.CSFLAGS)];
	    t.push(cs);
	    return t;
	}
    }
    

    public void check() {
	for (int i=0; i<trans.length; i++) 
	    if (trans[i].csInd != 0)
		throw new Error(trans[i] +" is guilty!");
	if(parentCoordsys != null)
	    parentCoordsys.check();
    }


    float[] tmprect = new float[5];
    
    public abstract class Trans {
	int cs[] = new int[20];
	int csInd = 0;
	void push(int cs) { 
	    if (dbg) {
		System.out.print(csInd+": ");
		for (int i=0; i<csInd; i++)
		    System.out.print(" ");
		System.out.println("push "+cs+", "+this+ ", "+(parentCoordsys ==null));
	    }
	    this.cs[++csInd] = cs; 
	}
	int cs() { return cs[csInd]; }
	void pop() { 
	    if (dbg)
		System.out.println("pop "+ cs() +", "+this+ ", "+(parentCoordsys ==null));
	    csInd--; 
	}

	int getParent() { return inds[cs()+1]; }
	Trans getParentTrans() { return getTrans(inds[cs()+1]); }

	/** how many parents (param coordinate systems) this cs type has */
	int nparents() { return 1; }

	abstract void put(Coordinates into);

	protected void transformRect(int cs, float[] rect, boolean useInterp) {
	    Trans t = getTrans(cs);
	    try {
		t.transformRect(rect, useInterp);
	    } finally {
		t.pop();
	    }
	}
	protected void inverseTransformRect(int cs, float[] rect, boolean useInterp) {
	    Trans t = getTrans(cs);
	    try {
		t.inverseTransformRect(rect, useInterp);
	    } finally {
		t.pop();
	    }
	}

	void transformRect(float[] rect, boolean useInterp) {
	    transform(rect, useInterp, false);
	}
	void inverseTransformRect(float[] rect, boolean useInterp) {
	    transform(rect, useInterp, true);
	}

	void transform(float[] rect, boolean useInterp, boolean inverse) {
	    if(!useInterp) {
		if(!inverse)
		    doTransformRect(rect, useInterp);
		else
		    doInverseTransformRect(rect, useInterp);
	    } else {
		int ocs;

		if(cs() < interpList.length)
		    ocs = interpList[cs()];
		else
		    ocs = VobMatcher.SHOW_IN_INTERP;

		if(ocs >= 0) {
		    for(int i=0; i<tmprect.length; i++) tmprect[i] = rect[i];

		    if(!inverse)
			doTransformRect(rect, false);
		    else
			doInverseTransformRect(rect, false);

		    Trans o = otherCoorder.getTrans(ocs);
		    try {
			if(!inverse)
			    o.transformRect(tmprect);
			else
			    o.inverseTransformRect(tmprect);
		    } finally {
			o.pop();
		    }
		    
		    for(int i=0; i<rect.length; i++)
			rect[i] = i(rect[i], tmprect[i], fract);
		} else if(ocs == VobMatcher.DONT_INTERP) {
		    throw new Error("transform called with useInterp=true on a cs that has DONT_INTERP set");
		} else if(ocs == VobMatcher.SHOW_IN_INTERP) {
		    if(!inverse)
			doTransformRect(rect, true);
		    else
			doInverseTransformRect(rect, true);
		} else {
		    throw new UnsupportedOperationException("Interpolation type: "+ocs);
		}
	    }
	}

	void transformRect(float[] rect) {
	    transformRect(rect, false);
	}
	void inverseTransformRect(float[] rect) {
	    inverseTransformRect(rect, false);
	}
    
	abstract void doTransformRect(float[] rect, boolean useInterp);

	void doInverseTransformRect(float[] rect, boolean useInterp) { 
	    float rx = rect[0], ry = rect[1], rw = rect[2], rh = rect[3];
	    float rd = rect[4];

	    rect[0] = rect[1] = rect[4] = 0;
	    rect[2] = rect[3] = 1;
	    doTransformRect(rect, useInterp);
	    
	    float x = rect[0], y = rect[1];
	    float scaleX = rect[2], scaleY = rect[3];
	    float depth = rect[4];
	    
	    rect[0] = rx-x; rect[1] = ry-y;
	    rect[2] = rw;   rect[3] = rh;
	    rect[4] = rd-depth;
	    
	    rect[0] /= scaleX; rect[1] /= scaleY;
	    rect[2] /= scaleX; rect[3] /= scaleY;
	}

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
	    Trans p = getParentTrans();
	    float w = p.w();
	    p.pop();
	    return w; 
	}
	float h() { 
	    Trans p = getParentTrans();
	    float h = p.h();
	    p.pop();
	    return h; 
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
		wh[0] = w(); wh[1] = h();
	    }		
	}

	boolean isDontInterpSet() {
	    if(cs() < interpList.length && 
	       interpList[cs()] == VobMatcher.DONT_INTERP) return true;
	    
	    for(int i=0; i<nparents(); i++) {
		Trans p = getTrans(inds[cs()+i+1]);
		if(p.isDontInterpSet()) { p.pop(); return true; }
		p.pop();
	    }

	    return false;
	}
    }


    public void getSqSize(int cs, float[] into) {
	Trans t = getTrans(cs);
	into[0] = t.w();
	into[1] = t.h();
	t.pop();
    }


    public float[] transformPoints3(int withCS, float[] pt, float[]into) {
	if(into == null)
	    into = new float[pt.length];
	float[] rect = new float[] { 0, 0, 1, 1, 0};
	Trans t = getTrans(withCS);
	t.transformRect(rect);
	t.pop();

	float ox = rect[0];
	float oy = rect[1];
	float sx = rect[2];
	float sy = rect[3];
	for(int i=0; i<pt.length; i+=3) {
	    into[i + 0] = ox + sx * pt[i + 0];
	    into[i + 1] = oy + sy * pt[i + 1];
	    into[i + 2] = rect[4] + pt[i + 2];
	}
	return into;
    }

    public float[] inverseTransformPoints3(int withCS, float[] pt, 
					   float[]into) {
	if(into == null)
	    into = new float[pt.length];
	System.arraycopy(pt, 0, into, 0, pt.length);

	Trans t = getTrans(withCS);
	t.inverseTransformRect(into);
	t.pop();

	return into;
    }





    int parentCS = 0;
    AWTVobCoorderBase parentCoordsys = null;


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

	ninds += 3+cs.length;

	return j;
    }


    // Stupid implementation...
    public int getCSAt(int parent, float x, float y, float[] internalcoords) {

	sorter.sort();
	int[] sorted = sorter.sorted;
	int nsorted = sorter.nsorted;

	float d[] = new float[5];

	for(int i=nsorted-1; i>=0; i--) {

	    if(isActive(sorted[i]) && 
	       (getParent(sorted[i]) == parent || parent == -1)) {
		Trans t = getTrans(sorted[i]);
		d[0] = d[1] = 0;
		d[2] = t.w();
		d[3] = t.h();
		t.transformRect(d);
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
	ninds += 3;
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


    public int getChildCSAt(int[] activateCSs, 
			    int parent, 
			    float x, float y, 
			    float[] targetcoords) {
	int [] css = new int[activateCSs.length];
	for (int i=0; i<css.length; i++)
	    css[i] = Integer.parseInt((String)actChildren.get(""+activateCSs[i]));

	ChildVobScene cvs = children[css[0]];
	((AWTVobCoorderBase)cvs.coords).parentCoordsys = this;
	((AWTVobCoorderBase)cvs.coords).parentCS = css[0];
	for (int i=1; i<css.length; i++) {
	    ChildVobScene oldCvs = cvs;
	    cvs = ((AWTVobCoorderBase)cvs.coords).children[css[i]];
	    ((AWTVobCoorderBase)cvs.coords).parentCoordsys = 
		((AWTVobCoorderBase)oldCvs.coords);
	    ((AWTVobCoorderBase)cvs.coords).parentCS = css[i];
	}

	return cvs.getCSAt(parent, x,y, targetcoords);
    }


}


