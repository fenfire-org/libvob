/*
AWTVobCoorder.java
 *
 *    Copyright (c) 2004, Matti J. Katila and Benja Fallenstein
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


    public static class DoNotInterpolateException extends RuntimeException {
	public DoNotInterpolateException(String s) { super(s); }
	public DoNotInterpolateException() { super(); }
    }


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

    public void clear() {
	nfloats = 0;
	ninds = 1;
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


    float[] cs1rect = new float[5];
    float[] cs2rect = new float[5];
    float[] wh = new float[4];
    float[] scale = new float[2];

    protected void getAbsoluteRect(int cs, float[] into, float[] scale,
				   boolean useInterp) {
	into[0] = 0; into[1] = 0;
	into[2] = 1; into[3] = 1;
	into[4] = 0;

	if (dbg) {
	    for (int i=0; i<4; i++)
		p("info: "+into[i]);
	}
	
	Trans t = getTrans(cs);
	try {
	    t.transformRect(into, useInterp);
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

    public void setInterpInfo(int cs1, AWTVobCoorderBase other, 
			      int[] interpList, float fract,
			      OrthoRenderInfo info
			) throws DoNotInterpolateException {

	if(interpList != null) {
	    this.interpList = interpList;
	    this.otherCoorder = other;
	    this.fract = fract;
	    
	    getAbsoluteRect(cs1, cs1rect, scale, true);
	} else {
	    getAbsoluteRect(cs1, cs1rect, scale, false);
	}

        info.setCoords(cs1rect[4],// depth
                       cs1rect[0], cs1rect[1], cs1rect[2], cs1rect[3],
                       scale[0], scale[1]);

	/*
	int cs2;

	if(interpList != null) {
	    try {
		cs2 = interpList[cs1];
	    } catch(ArrayIndexOutOfBoundsException e) {
		cs2 = VobMatcher.SHOW_IN_INTERP;
	    }
	} else {
	    other = this; cs2 = cs1; fract = 0;
	}

	if(cs2 == VobMatcher.DONT_INTERP)
	    throw new DoNotInterpolateException();
	else if(cs2 == VobMatcher.SHOW_IN_INTERP) {
	    // XXXXXY
	    other = this; cs2 = cs1; fract = 0;
	}

	this.getAbsoluteRect(cs1, cs1rect, scale);
	float sx1 = scale[0], sy1 = scale[1];

	other.getAbsoluteRect(cs2, cs2rect, scale);
	float sx2 = scale[0], sy2 = scale[1];
	
	if (dbg) check();
	
	if (dbg) {
	    for (int i=0; i<4; i++)
		p("info 1: "+cs1rect[i]);
	    p("sx: "+sx1+", sy: "+sy1);
	}
	info.setCoords(i(cs1rect[4], cs2rect[4], fract),// depth
		       i(cs1rect[0], cs2rect[0], fract),
		       i(cs1rect[1], cs2rect[1], fract),
		       i(cs1rect[2], cs2rect[2], fract),
		       i(cs1rect[3], cs2rect[3], fract),
		       i(sx1, sx2, fract),
		       i(sy1, sy2, fract));
	*/
    }

    public void setInfo(int cs, OrthoRenderInfo info) {
	try {
	    setInterpInfo(cs, this, null, 0, info);
	} catch(DoNotInterpolateException e) {
	    throw new Error(e);
	}
    }


    Trans noOp = new Trans(){
	    public String toString() { return "no op"; }
	    void doTransformRect(float[] rect, boolean useInterp) { 
		transformRect(getParent(), rect, useInterp);
	    }
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(getParent(), rect, useInterp);
	    }
	};

    Trans root = new Trans(){
	    public String toString() { return "root"; }
	    void doTransformRect(float[] rect, boolean useInterp) { 
	    }
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
	    }
	    float sx() { return width; }
	    float sy() { return height; }
	    float w() { return 1; }
	    float h() { return 1; }
	    void getWH(float[] wh, boolean useInterp) { wh[0] = wh[1] = 1; }
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
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(inds[cs()+1], rect, useInterp);
		inverseTransformRect(inds[cs()+2], rect, useInterp);
	    }
	},
	new Trans() {   // 6 concatInverse
	    public String toString() { return "concat inverse"; }
	    void doTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(inds[cs()+2], rect, useInterp);
		transformRect(inds[cs()+1], rect, useInterp);
	    }
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		transformRect(inds[cs()+1], rect, useInterp);
		inverseTransformRect(inds[cs()+2], rect, useInterp);
	    }
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
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(getParent(), rect, useInterp);

 		int f = inds[cs()+2];
		for (int i=0; i<2; i++)
		    rect[i] -= floats[f+i];
		rect[4] -= floats[f+2]; // depth
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
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(getParent(), rect, useInterp);

		int f = inds[cs()+2];
		for (int i=0; i<2; i++) {
		    rect[i] /= floats[f+i];
		    rect[i+2] /= floats[f+i];
		}
		rect[4] /= floats[f+2]; // depth
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
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(getParent(), rect, useInterp);
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
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(getParent(), rect, useInterp);

		int f = inds[cs()+2];
		rect[4] -= floats[f+0]; // depth

		for (int i=0; i<2; i++)
		    rect[i] -= floats[f+1+i];

		rect[0] /= floats[f+3];
		rect[1] /= floats[f+4];
		rect[2] /= floats[f+3];
		rect[3] /= floats[f+4];
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

	    /** copied from include/vob/trans/LinearPrimitives.hxx 
	     *  coded by Tuomas J. Lukka
	     */
	    void doTransformRect(float[] rect_, boolean useInterp) {
		float [] rect = new float[rect_.length];

		int areaCS = inds[cs()+1];
		int anchorCS = inds[cs()+2];

		// get absolutely area
		getAbsoluteRect(areaCS, rect, 
				new float[]{1,1},useInterp);
		float[] areaF = new float[rect.length];
		System.arraycopy(rect,0,areaF,0,rect.length);

		// compute sq of the area
		getSqSize(areaCS, rect);
		float[] sqF = new float[2];
		System.arraycopy(rect,0,sqF,0,2);

		// get anchor
		getAbsoluteRect(anchorCS, rect, 
				new float[]{1,1},useInterp);
		float[] anchorF = new float[rect.length];
		System.arraycopy(rect,0,anchorF,0,rect.length);

		Vect anchor = new Vect((anchorF[0]-areaF[0]) / sqF[0], 
				       (anchorF[1]-areaF[1]) / sqF[1]); 
		//p("anchor sqd: "+anchor);
		
		float shift = floats[inds[cs()+3]+1] / sqF[0];
		float direction = floats[inds[cs()+3]];
		int dir = direction > 0 ? 1 : -1;

		Vect shifted = new Vect((float) anchor.x() + dir * shift, anchor.y());
		Vect ctr = new Vect(.5f, .5f);
		Vect buoy;

		if(shifted.neg(ctr).abs() >= .5) {
		    buoy = shifted;
		} else {
		    if(anchor.neg(ctr).abs() >= .5) {
			buoy = anchor;
		    } else {
			buoy = project2circle(
			    anchor, 
			    ctr.sum((new Vect(.5f,0)).mul(-dir)),
			    ctr, .5f);
		    }
		}

		//p("final: "+buoy);

		float scale = 1 - (anchor.neg(ctr)).abs() / .5f;
		if(scale < shift) scale = shift;
		rect_[4] += -scale; // depth
		rect_[0] = buoy.x() * sqF[0] + areaF[0] + 2f*scale*rect_[0]/2f;
		rect_[1] = buoy.y() * sqF[1] + areaF[1] + 2f*scale*rect_[1]/2f;
		rect_[2] *= scale;
		rect_[3] *= scale;

	    }
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		int parent = inds[cs()+1];
		int anchor = inds[cs()+2];
		inverseTransformRect(parent, rect, useInterp);
		p("inverse parent: "+rect);
		throw new Error("not impl.");
	    }
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
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		inverseTransformRect(getParent(), rect, useInterp);

		int f = inds[cs()+2];

		rect[4] -= floats[f+0]; // depth

		for (int i=0; i<2; i++)
		    rect[i] -= floats[f+1+i];

		rect[0] /= floats[f+3];
		rect[1] /= floats[f+4];
		rect[2] /= floats[f+3];
		rect[3] /= floats[f+4];
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
		throw new Error("unitSq unimplemented");
	    }
	    void doInverseTransformRect(float[] rect, boolean useInterp) { 
		throw new Error("unitSq unimplemented");
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

	protected void transformRect(int cs, float[] rect, boolean useInterp) {
	    Trans t = getTrans(cs);
	    try {
		t.transformRect(rect, useInterp);
	    } finally {
		t.pop();
	    }
	}
	protected void inverseTransformRect(int cs, float[] rect, 
					    boolean useInterp) {
	    Trans t = getTrans(cs);
	    try {
		t.inverseTransformRect(rect, useInterp);
	    } finally {
		t.pop();
	    }
	}

	void transformRect(float[] rect, boolean useInterp) {
	    if(!useInterp) 
		doTransformRect(rect, useInterp);
	    else {
		int ocs;

		if(cs() < interpList.length)
		    ocs = interpList[cs()];
		else
		    ocs = VobMatcher.SHOW_IN_INTERP;

		if(ocs >= 0) {
		    for(int i=0; i<tmprect.length; i++) tmprect[i] = rect[i];

		    doTransformRect(rect, false);

		    Trans o = otherCoorder.getTrans(ocs);
		    try {
			o.transformRect(tmprect);
		    } finally {
			o.pop();
		    }
		    
		    for(int i=0; i<rect.length; i++)
			rect[i] = i(rect[i], tmprect[i], fract);
		} else if(ocs == VobMatcher.DONT_INTERP) {
		    throw new DoNotInterpolateException();
		} else if(ocs == VobMatcher.SHOW_IN_INTERP) {
		    doTransformRect(rect, true);
		} else {
		    throw new UnsupportedOperationException("Interpolation type: "+ocs);
		}
	    }
	}
	void inverseTransformRect(float[] rect, boolean useInterp) {
	    if(!useInterp)
		doInverseTransformRect(rect, useInterp);
	    else
		throw new UnsupportedOperationException("not implemented");
	}

	void transformRect(float[] rect) {
	    transformRect(rect, false);
	}
	void inverseTransformRect(float[] rect) {
	    inverseTransformRect(rect, false);
	}
    
	abstract void doTransformRect(float[] rect, boolean useInterp);
	abstract void doInverseTransformRect(float[] rect, boolean useInterp);

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
	float w() { return 1; }
	float h() { return 1; }

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
		if(w() == 1 && h() == 1) {
		    Trans p = getParentTrans();
		    p.getWH(wh, useInterp);
		    p.pop();
		} else {
		    wh[0] = w(); wh[1] = h();
		}
	    }		
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


