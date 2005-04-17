/*
VobCoorder.java
 *
 *    Copyright (c) 2000-2002, Tuomas Lukka
 *
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob;

/** A set of coordinate systems for vobs.
 * In the new Vob APIs, the keys that were formerly inside the Vob
 * objects are associated with <em>coordinate systems</em>, each of
 * which may contain any number of vobs. This allows us to easily
 * animate connections between two coordinate systems etc.
 * <p>
 * Coordinate systems are recursive; the coordinate system <code>0</code>
 * represents the root coordinate system (i.e., the whole vob scene).
 * <p>
 * Warn about setXParams!
 * <p>
 * The transform functions (transformPoints3, inverseTransformPoints3,
 * transformPoint, transformPoints2) can be used to transform points
 * into coordinate systems.
 * The functions take an array of original points, an (optional) destination
 * array, and return either the destination array, or,
 * if the coordinate system is not rendered in the current configuration
 * (e.g. culled out), null.
 */
public abstract class VobCoorder {
    /** Create a new coordinate system.
     * @param key The key that identifies this coordinate system.
     * 			The key is used for interpolation
     * @param into The coord system to place the new coordinate
     *             system into, <code>0</code> for the root
     *             coordinate system.
     */
    abstract public int ortho(int into, float depth, float x, float y, float sx, float sy);

    /** Remove all coorsys from this coorder.
     *  This method makes coorders re-usable; rather than creating
     *  a new coorder object, an old one can be re-used by clearing it.
     */
    abstract public void clear();

    public int translate(int into, float x, float y) {
	return translate(into, x, y, 0);
    }
    public int translate(int into, float x, float y, float z) {
	throw new Error("unimpl.");
    }
    public int scale(int into, float sx, float sy) {
	return scale(into, sx, sy, 1);
    }
    abstract public int scale(int into, float sx, float sy, float sz);
    

    abstract public void setOrthoParams(int into, float depth, float x, float y, float sx, float sy);

    public void setTranslateParams(int into, float x, float y) {
	setTranslateParams(into, x, y, 0);
    }
    public void setTranslateParams(int into, float x, float y, float z) {
	throw new Error("unimpl.");
    }
    public void setScaleParams(int into, float sx, float sy) {
	throw new Error("unimpl.");
    }

    public int box(int into, float w, float h) {
	return orthoBox(into, 0, 0, 0, 1, 1, w, h);
    }
    public int box(int into, float x, float y, float w, float h) {
	return orthoBox(into, 0, x, y, 1, 1, w, h);
    }
    public int box(int into, float d, float x, float y, float w, float h) {
	return orthoBox(into, d, x, y, 1, 1, w, h);
    }
    public void setBoxParams(int cs, float w, float h) {
	setOrthoBoxParams(cs, 0, 0, 0, 1, 1, w, h);
    }

    public int unitSq(int into) {
	// XXX Really bad default impl...
	float []tmp = new float[2];
	getSqSize(into, tmp);
	return ortho(into, 0, 0, 0, tmp[0], tmp[1]);
    }

    /** Return a coordinate system whose origin is at the center
     *  of the parent.
     */
    public int center(int box) {
	return translate(unitSq(box), .5f, .5f, 0);
    }

    abstract public int orthoBox(int into, float z, float x, float y, float sx, float sy, float w, float h) ;
    abstract public void setOrthoBoxParams(int cs, float z, float x, float y, float sx, float sy, float w, float h) ;

    /** Creates a CullingCoordSys with distinct parent and test 
     * coordinate systems. Exluding the test for drawing, the 
     * CullingCoordSys works like its parent coordinate system.
     * E.g. CullingCoordSys returns its parents box. 
     * <p>
     * This coordsys will not necessarily be drawn if the boxes
     * of the test and clip coordinate systems do not intersect.
     * However, this is not guaranteed; the only thing guaranteed
     * is that if the boxes of the test and clip coordinate systems
     * *do* intersect, the CullingCoordsys will be drawn.
     * <p>
     * The default implementation (although this may change in the
     * future) in VobCoorder is to simply return parent.
     * <p>
     * @param parent ID of the coordinate system which points 
     *               will be transformed, if CullingCoordSys 
     *               is shown
     * @param test ID of the coordinate system whose box is tested 
     *             against the clip coordinate system.
     * @param clip ID of the coordinate system whose box is tested
     *             against the test coordinate system.
     */
    public int cull(int parent, int test, int clip) {
	throw new Error("no cull implemented!");
    }

    /** Creates a CullingCoordSys using the parent also as the test 
     * coordinate system. 
     */
    public int cull(int parent, int clip) {
	return cull(parent, parent, clip);
    }

    /** Create a new transformation that is the concatenation 
     * of two existing transformations.
     * If we look at the transformations as x' = f(x) and x' = g(x) then
     * the result of this operation is a transformation h,
     * for which h(x) = f(g(x)) always.
     */
    public int concat(int f, int g) {
	throw new Error("Not implemented in this coorder\n\n");
    }

    /** Create a new transformation that is the inverse
     * of an existing transformation.
     */
    public int invert(int f) {
	throw new Error("Not implemented in this coorder\n\n");
    }

    abstract public int between(int a, int b);
    abstract public int translatePolar(int parent, float distance, float angle);

    abstract public int buoyOnCircle2(int circle, int anchor, float dir, float XXX);

    /** Get the size of the "unit square" of the given coordinate
     * system. This is the size that the unit square of unitSqCS() 
     * would be in the given coordinate system.
     */
    abstract public void getSqSize(int cs, float[] into);

    abstract public boolean needInterp(VobCoorder interpTo, int[] interpList);

    abstract public void dump();

    public void activateChildByCS(int CS, int childCS) {
	throw new Error("not implemented.");
    }
    public boolean hasActiveChildVS(int cs) {
	throw new Error("not implemented.");
    }

    public ChildVobScene getChildByCS(int cs) {
	throw new Error("not implemented.");
    }

    public int getChildCSAt(int [] activeCSs, int parent, 
			    float x, float y, 
			    float[] targetcoords) {
	throw new Error("not implemented.");
    }


    /** Cause the given coordinate system to be considered when 
     * getCSAt() is called.
     */
    abstract public void activate(int CS);
    /** Get the topmost activated coordinate system which 
     * has parent as a primary 
     * ancestor.
     * This is defined as follows:
     * <pre>
	  1) Inverse transform into the coordinate system; in there, clip
	     against the unit square and project to the plane z=0

	  2) Transform the projected point back into screen coordinates, making
	     note of the z coordinate.
	    </pre>
     * @return Coordinate system number, or -1 if none.
     */
    abstract public int getCSAt(int parent, float x, float y, float[] targetcoords);

    /** Transform a point to screen coordinates from the given cs.
     * It is explicitly allowed for pt and into to be the same array.
     */
    public float[] transformPoints3(int withCS, float[] pt, float[]into) {
	throw new UnsupportedOperationException("transform not supported yet");
    }
    /** 
     * @return True, if a reasonable inverse was found.
     */
    public float[] inverseTransformPoints3(int withCS, float[] pt, float[]into) {
	throw new UnsupportedOperationException("inverse transform not supported yet");
    }

    public java.awt.Point transformPoint(int cs, float x, float y, java.awt.Point into) {
	// Slow default impl
	float[] pt = new float[] {x, y, 0};
	pt = transformPoints3(cs, pt, pt);
	if(pt == null) return null;
	if(into == null) into = new java.awt.Point();
	into.x = (int)pt[0];
	into.y = (int)pt[1];
	return into;
    }

    public java.awt.Point[] transformPoints2(int cs, float[] coords, java.awt.Point[] into) {
	// Slow default impl
	float[] pt = new float[coords.length * 3 / 2];
	for(int i=0; i<coords.length/2; i++) {
	    pt[3*i+0] = coords[2*i+0];
	    pt[3*i+1] = coords[2*i+1];
	    pt[3*i+2] = 0;
	}
	pt = transformPoints3(cs, pt, pt);
	if(pt == null) return null;
	if(into == null) into = new java.awt.Point[pt.length / 3];
	for(int i=0; i<pt.length/3; i++) {
	    into[i].x = (int)pt[3*i+0];
	    into[i].y = (int)pt[3*i+1];
	}
	return into;
    }


    /** (Not public API: for use by VobScene).
     */
    public int _putChildVobScene(ChildVobScene child, int[] cs) {
	throw new Error("Not implemented");
    }


    /** Get a coordinate system from a child vobscene placed
     * using putChildVobScene into this VobScene.
     * @param childVobSceneId The id returned from putChildVobScene
     * @param nth The index of the coordinate system 
     *            inside the child vobscene.
     */
    public int exportChildCoordsys(int childVobSceneId, int nth) {
	throw new Error("Not implemented");
    }
    

}


