/*
LinearPrimitives.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_LINEARPRIMITIVES_HXX
#define VOB_LINEARPRIMITIVES_HXX

#ifndef VOB_PRIMITIVETRANS_DEFINED
#define VOB_PRIMITIVETRANS_DEFINED(x, n)
#endif

#include <GL/gl.h>

#include <vob/trans/Primitives.hxx>
#include <vob/geom/Quadrics.hxx>

namespace Vob {
namespace Primitives {

PREDBGVAR(dbg_buoyoncircle);


    /** A simple translation in 3 dimensions.
     * Can't be used in HierarchicalTransform directly
     * because parameters are required.
     */
    class TranslateXYZ : 
	    public PrimitiveTransform, 
	    public GLPerformablePrimitiveTransform
    {
    public:
	ZPt vec;
	explicit TranslateXYZ() { }
	TranslateXYZ(ZPt vec) : vec(vec) { }
	void tr(const ZPt &from, ZPt &to) const {
	    to = from + vec;
	}
        void performGL() const {
	    glTranslatef(vec.x, vec.y, vec.z);
	}
	typedef TranslateXYZ InverseType;
	void inverse(InverseType &into) const {
	    into.vec = -vec;
	}
    };

    /** A simple scale in 3 dimensions.
     * Can't be used in HierarchicalTransform directly
     * because parameters are required.
     */
    class ScaleXYZ : 
	    public PrimitiveTransform, 
	    public GLPerformablePrimitiveTransform
    {
    public:
	ZPt vec;
	explicit ScaleXYZ() { }
	ScaleXYZ(ZPt vec) : vec(vec) { }

	void tr(const ZPt &from, ZPt &to) const {
	    to = from * vec;
	}
        void performGL() const {
	    glScalef(vec.x, vec.y, vec.z);
	}
	typedef ScaleXYZ InverseType;
	void inverse(InverseType &into) const {
	    into.vec = ZPt(1.0/vec.x, 1.0/vec.y, 1.0/vec.z);
	}
    };

    /** Expose the vec in TranslateXYZ and ScaleXYZ and possibly
     * others explicitly.
     */
    template<class T> struct Vec_Explicit : 
	    public T, 
	    public ParametrizedPrimitiveTransform {

	enum { NParams = 3 };

	template<class Ptr> void setParams(Ptr p) {
	    vec.x = p[0];
	    vec.y = p[1];
	    vec.z = p[2];
	}
    };

    /** An explicit translation by a 3D vector.
     * Parameters: x, y, z.
     */
    typedef Vec_Explicit<TranslateXYZ> TranslateXYZ_Explicit;
    /** An explicit scale by a 3D vector.
     * Parameters: sx, sy, sz.
     */
    typedef Vec_Explicit<ScaleXYZ> ScaleXYZ_Explicit;

    VOB_PRIMITIVETRANS_DEFINED(TranslateXYZ_Explicit, "translate");
    VOB_PRIMITIVETRANS_DEFINED(ScaleXYZ_Explicit, "scale");

    /** Rotation clockwise. 
     */
    class RotateXY :
	public PrimitiveTransform,
	public GLPerformablePrimitiveTransform 
    {
    protected:
	float a;
	void angleWasSet() {
	    s = sin(a * M_PI / 180);
	    c = cos(a * M_PI / 180);
	}
    private:
	float s, c;
    public:
	RotateXY() { }
	RotateXY(float angle) {
	    a = angle;
	    angleWasSet();
	}
	/** Perform the internal transformation of this 
	 * coordsys.
	 */
	void tr(const ZPt &from, ZPt &to) const {
	    to.x = c * from.x + -s * from.y; 
	    to.y = s * from.x + c * from.y; 
	    to.z = from.z;
	}
        void performGL() const {
	    glRotatef(a, 0, 0, 1);
	}
	typedef RotateXY InverseType;
	void inverse(RotateXY &into) const {
	    into.a = -a;
	    into.angleWasSet();
	}
    };

    /** An explicit parametrization of clockwise rotation.
     * Parameters: angle (in degrees)
     */
    class RotateXY_Explicit :
	public RotateXY,
	public ParametrizedPrimitiveTransform {
    public:

	enum {NParams = 1 };

	template<class Ptr> void setParams(Ptr p) {
	    a = p[0];
	    angleWasSet();
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(RotateXY_Explicit, "rotate");

    /** Rotation clockwise around given point. 
     */
    class RotateCenteredXY :
	public PrimitiveTransform,
	public GLPerformablePrimitiveTransform 
    {
    protected:
	float a;
	float ox, oy;
	void angleWasSet() {
	    s = sin(a * M_PI / 180);
	    c = cos(a * M_PI / 180);
	}
    private:
	float s, c;
    public:
	RotateCenteredXY() { }
	RotateCenteredXY(float angle) {
	    a = angle;
	    angleWasSet();
	}
	/** Perform the internal transformation of this 
	 * coordsys.
	 */
	void tr(const ZPt &from, ZPt &to) const {
	    float x = from.x - ox;
	    float y = from.y - oy;
	    to.x = c * x + -s * y + ox; 
	    to.y = s * x + c * y + oy; 
	    to.z = from.z;
	}
        void performGL() const {
	    glTranslatef(-ox, -oy, 0);
	    glRotatef(a, 0, 0, 1);
	    glTranslatef(ox, oy, 0);
	}
	typedef RotateCenteredXY InverseType;
	void inverse(InverseType &into) const {
	    into.a = -a;
	    into.ox = ox;
	    into.oy = oy;
	    into.angleWasSet();
	}
    };


    /** Nadir rotation around unit square center.
     */
    class NadirUnitSq :
	public RotateCenteredXY,
	public BoxPrimitiveTransform,
	public DependentPrimitiveTransform {
    public:
	enum { NDepends = 2 }; // parent and nadir
	template<class SPtr> void setParams(SPtr depends) {
	    Pt p = depends[0]->getSqSize();
	    ox = p.x/2;
	    oy = p.y/2;
	    ZPt origin = depends[0]->transform(ZPt(ox, oy, 0));
	    ZPt nadir = depends[1]->transform(ZPt(0,0,0));

	    float x = origin.x - nadir.x;
	    float y = origin.y - nadir.y;
	    float angle = atan2(x, -y);

	    a = angle * 180 / M_PI;

	    angleWasSet();

	}
	Pt getSqSize() const { 
	    return Pt(2*ox, 2*oy); 
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(NadirUnitSq, "nadirUnitSq");

    /** A no-op: same coords in and out.
     */
    class Unit : 
	    public PrimitiveTransform,
	    public GLPerformablePrimitiveTransform
    {
    public:
	void tr(const ZPt &from, ZPt &to) const {
	    to = from;
	}
        void performGL() const {
	}
	typedef Unit InverseType;
	void inverse(InverseType &into) const { }
    };
    VOB_PRIMITIVETRANS_DEFINED(Unit, "unit");

    /** Set the box size, otherwise no action.
     * Can't be used in HierarchicalTransform directly
     * because parameters are required.
     */
    class Box : 
	    public PrimitiveTransform,
	    public GLPerformablePrimitiveTransform,
	    public BoxPrimitiveTransform
    {
    public:
	Pt box;
	explicit Box() { }
	Box(Pt box) : box(box) { }

	void tr(const ZPt &from, ZPt &to) const {
	    to = from;
	}
        void performGL() const {
	}
	Pt getSqSize() const { return box; }

	typedef Unit InverseType;
	void inverse(InverseType &into) const { }
    };

    /** The explicit parametrization of Box, with
     */
    class Box_Explicit : public Box,
	    public ParametrizedPrimitiveTransform 
    {
    public:
	enum { NParams = 2 };

	template<class Ptr> void setParams(Ptr p) {
	    box.x = p[0];
	    box.y = p[1];
	}

    };
    VOB_PRIMITIVETRANS_DEFINED(Box_Explicit, "box");

    /** Rotation in 3-space.
     */
    class RotateXYZ:
	    public PrimitiveTransform,
	    public GLPerformablePrimitiveTransform
    {
    public:
	ZVec vec;
	/** Angle, degs.
	 */
	float a;
	/** Sine and cosine of angle.
	 */
	float s, c;

	void angleWasSet() {
	    s = sin(a * M_PI / 180);
	    c = cos(a * M_PI / 180);
	}

	void tr(const ZPt &from, ZPt &to) const {
	    ZVec v(from);
	    float same = v.dot(vec);
	    ZVec para = v - same*vec;
	    float  paral = para.length();
	    para = (1/paral)*para;
	    ZVec ortho = para.crossp(vec).normalized();
	    to = ZPt( same * vec + paral * (c * para - s * ortho) );
	}
        virtual void performGL() const {
	    glRotatef(a, vec.x, vec.y, vec.z);
	}
	typedef RotateXYZ InverseType;
	void inverse(InverseType &inv) const {
	    inv.vec = vec;
	    inv.a = -a;
	    inv.angleWasSet();
	}
    };

    /** Explicit parametrization of rotation in 3-space.
     * Parameters: x, y, z, angle (in degrees).
     * x,y,z are the vector to be rotated around.
     */
    class RotateXYZ_Explicit :
	    public RotateXYZ,
	    public ParametrizedPrimitiveTransform
    {
    public:
	enum { NParams = 4 };
	template<class Ptr> void setParams(Ptr p) {
	    vec.x = p[0];
	    vec.y = p[1];
	    vec.z = p[2];
	    vec = vec.normalized();
	    a = p[3];

	    angleWasSet();
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(RotateXYZ_Explicit, "rotateXYZ");

    /** Quaternion parametrization of rotation in 3-space.
     * Parameters: x, y, z, w
     */
    class RotateXYZ_Quaternion :
	    public RotateXYZ,
	    public ParametrizedPrimitiveTransform
    {
    public:
	enum { NParams = 4 };
	template<class Ptr> void setParams(Ptr p) {
	    vec.x = p[0];
	    vec.y = p[1];
	    vec.z = p[2];
	    float norm = vec.length();
	    if (norm > 0)
		vec *= 1 / norm;
	    else
		vec.z = 1;
	    a = 2 * acos(p[3]) * (180 / M_PI);

	    angleWasSet();
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(RotateXYZ_Quaternion, "rotateQuaternion");

    /** Affine coordinate system (in xy), offset in z.
     * Can't be used in HierarchicalTransform directly
     * because parameters are required.
     */
    class AffineXY: 
	    public PrimitiveTransform,
	    public GLPerformablePrimitiveTransform
    {
    public:
	ZPt offset;
	Pt xdot;
	Pt ydot;
	/** Perform the internal transformation of this 
	 * coordsys.
	 */
	void tr(const ZPt &from, ZPt &to) const {
	    to = offset;
	    to.x += xdot.dot(from);
	    to.y += ydot.dot(from);
	    to.z += from.z;
	}
        void performGL() const {
            GLfloat matrix[16] = {
                xdot.x, ydot.x, 0, 0,
                xdot.y, ydot.y, 0, 0,
                0,   0,   1, 0,
                offset.x,   offset.y,   offset.z, 1
            };
            glMultMatrixf(matrix);
        }

	typedef AffineXY InverseType;
	void inverse(InverseType &inv) const {
	    double det = xdot.x * ydot.y - xdot.y * ydot.x;
	    // XXX If det small, trouble!!
	    inv.xdot.x = ydot.y / det;
	    inv.xdot.y = -xdot.y / det;
	    inv.ydot.x = -ydot.x / det;
	    inv.ydot.y = xdot.x / det;

	    inv.offset.x = -inv.xdot.dot(offset);
	    inv.offset.y = -inv.ydot.dot(offset);
	    inv.offset.z = -offset.z;
	}
    };

    /** Explicit parametrization of affine.
     * Parameters: x, y, depth, xx, xy, yx, yy
     */
    class AffineXY_Explicit : 
	    public AffineXY,
	    public ParametrizedPrimitiveTransform
    {
    public:
	enum { NParams = 7 };
	template<class Ptr> void setParams(Ptr p) {
	    offset.z = p[0];

	    offset.x = p[1];
	    offset.y = p[2];

	    xdot.x = p[3];
	    xdot.y = p[4];

	    ydot.x = p[5];
	    ydot.y = p[6];
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(AffineXY_Explicit, "affine");


    /** Orthogonal coordinate system (in xy), offset in z.
     * Can't be used in HierarchicalTransform directly
     * because parameters are required.
     */
    class Ortho: 
	    public PrimitiveTransform,
	    public GLPerformablePrimitiveTransform
    {
    public:
	float x, y, z;
	float sx, sy;
	/** Perform the internal transformation of this 
	 * coordsys.
	 */
	void tr(const ZPt &from, ZPt &to) const {
	    to.x = x + from.x * sx;
	    to.y = y + from.y * sy;
	    to.z = z + from.z;
	}
        void performGL() const {
	    glTranslatef(x, y, z);
	    glScalef(sx, sy, 1);
        }

	typedef Ortho InverseType;
	void inverse(InverseType &inv) const {
	    inv.sx = 1.0/sx;
	    inv.sy = 1.0/sy;

	    inv.x = -x*inv.sx;
	    inv.y = -y*inv.sy;
	    inv.z = -z;
	}
    };

    /** Explicit parametrization of ortho.
     * Parameter layout: depth, x, y, xx, yy
     */
    class Ortho_Explicit : 
	    public Ortho,
	    public ParametrizedPrimitiveTransform
    {
    public:
	enum { NParams = 5 };
	template<class Ptr> void setParams(Ptr p) {
	    z = p[0];
	    x = p[1];
	    y = p[2];
	    sx = p[3];
	    sy = p[4];
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(Ortho_Explicit, "ortho");

    /** A buoy coordinate system.
     */
    class BuoyOnCircle1 :
	public Ortho
    {
    public:
	/** Set this coordinate system to be the buoy with given params.
	 * @param anchorPt The location of the anchor.
	 * @param ctrPoint The center of the buoy circle.
	 * @param radius The radius of the circle.
	 * @param projPoint the point to project the buoy from, if it would be 
	 * 		inside the circle just shifted.
	 * @param shiftAmount The amount to shift the buoy.
	 */
	void setBuoy(Pt anchorPt, Pt ctrPoint, float radius, Pt projPoint, float shiftAmount) {

	    // shifted point: default buoy location
	    Pt shifted = anchorPt + (ctrPoint - projPoint) * shiftAmount;

	    float shiftrad = (shifted - ctrPoint).length();
	    float anchorrad = (anchorPt - ctrPoint).length();
	    
	    Pt buoy;
	    if(shiftrad >= radius) {
		// if shifted point is outside circle, our work is done
		buoy = shifted;
	    } else {
		if(anchorrad >= radius)
		    // otherwise, kludge it by placing it on the anchor.
		    // There's a small jump here; we should do it differently.
		    buoy = anchorPt; // XXX ???
		else
		    // If both anchor and shifted point are inside circle,
		    // project.
		    buoy = Geom::project2circle(anchorPt, projPoint, ctrPoint, radius);
	    }

	    float scale = 1-anchorrad / radius ;

	    if(scale <shiftAmount) {
		scale = shiftAmount;
	    }
	    // DBG(dbg_buoy) << "final: "<<buoy << " "<<" "<<scale<<" "<<"\n";

	    this->x = buoy.x;
	    this->y = buoy.y;
	    this->z = - scale;
	    this->sx = scale;
	    this->sy = scale;

	}
    };

    /*
     * <p>
     * The depth is between -1 (center) and 0 (edge)
     * <p>
     * Parameter layout:
     *  x_circle, y_circle, radius, x_projpoint, y_projpoint, shiftamount
     *  <p>
     *  Linear interp between 0..1, 0..-1
     */
    class BuoyOnCircle1_Explicit :
	public BuoyOnCircle1,
	public DependentPrimitiveTransform,
	public ParametrizedPrimitiveTransform
    {
    public:
	enum { NDepends = 2, NParams = 6 }; // usual super and plus the anchor
	template<class SPtr, class P> void setParams(SPtr depends, P params) {
	    ZPt anchor = depends[1]->transform(ZPt(0,0,0));

	    Pt ctr(params[0], params[1]);
	    float radius = params[2];
	    Pt proj(params[3], params[4]);
	    float shiftamount = params[5];

	    this->setBuoy(anchor, ctr, radius, proj, shiftamount);
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(BuoyOnCircle1_Explicit, "buoyOnCircle1");

    class BuoyOnCircle2 :
	public Ortho,
	public DependentPrimitiveTransform
    {
    public:
	enum { NDepends = 2 }; // Usual super plus the anchor

	/** The number of units to shift the coordsyses by default.
	 */
	float shiftamount;

	/** The direction of shift.
	 */
	float direction;

	BuoyOnCircle2() : shiftamount(10), direction(1) {
	}

	template<class SPtr> void setParams(SPtr depends) {
	    // Get the anchor in the root coordinate system
	    Pt anchor = Pt(depends[0]->getInverse().transform(
			    depends[1]->transform(
				.5 * depends[1]->getSqSize())));
	    DBG(dbg_buoyoncircle) <<
		    "anchor: "<<anchor<<"\n";
	    // And transform it to the square coordsys
	    Pt sq = depends[0]->getSqSize();
	    anchor.x /= sq.x;
	    anchor.y /= sq.y;

	    DBG(dbg_buoyoncircle) <<
		    "anchor sqd: "<<anchor<<"\n";

	    float shift = shiftamount / sq.x;

	    // Here, the circle center is always .5, .5
	    // and radius .5

	    int dir = direction > 0 ? 1 : -1;

	    Pt shifted(anchor.x + dir * shift, anchor.y);
	    Pt ctr(.5,.5);
	    Pt buoy;
	    if((shifted-ctr).length() >= .5) {
		buoy = shifted;
	    } else {
		if((anchor-ctr).length() >= .5) {
		    buoy = anchor;
		} else {
		    buoy = Geom::project2circle(
			    anchor, ctr + -dir * Pt(.5,0),
			    ctr, .5);
		}
	    }
	    
	    float scale = 1 - (anchor-ctr).length() / .5;
	    if(scale < shift) scale = shift;
	    this->x = buoy.x * sq.x;
	    this->y = buoy.y * sq.y;
	    this->z = -scale;
	    this->sx = scale;
	    this->sy = scale;

	    DBG(dbg_buoyoncircle) <<
		    "finished : "<<this->x<<" "<<this->y<<" "<<this->z<<" "
			    <<this->sx<<" "<<this->sy<<"\n";
	}
    };

    /** A better parametrization of buoys.
     * The box of the first coordsys is used directly as the 
     * circle (or ellipse). The two required numeric 
     * parameters are the direction of projection (positive x
     * or negative x) and the amount to shift in terms of
     * the parent
     * coordsys.
     */
    class BuoyOnCircle2_Explicit :
	public BuoyOnCircle2,
	public ParametrizedPrimitiveTransform {
    public:
	enum { NParams = 2 };

	template<class SPtr, class P> void setParams(SPtr depends, P params) {
	    direction = params[0];
	    shiftamount = params[1];
	    BuoyOnCircle2::setParams(depends);
	}

    };

    VOB_PRIMITIVETRANS_DEFINED(BuoyOnCircle2_Explicit, "buoyOnCircle2");

    /** Orthonormal transformation along with setting the box 
     * size of the coordinate system.
     */
    class OrthoBox :
	    public Ortho,
	    public BoxPrimitiveTransform
    {
    public:
	Pt box;
	Pt getSqSize() const { return box; }
    };

    /** Explicit parametrization of OrthoBox.
     * Parameters: z, x, y, sx, sy, bx, by.
     */
    class OrthoBox_Explicit :
	    public OrthoBox,
	    public ParametrizedPrimitiveTransform
    {
    public:
	enum { NParams = 7 };
	template<class Ptr> void setParams(Ptr p) {
	    z = p[0];
	    x = p[1];
	    y = p[2];
	    sx = p[3];
	    sy = p[4];
	    box.x = p[5];
	    box.y = p[6];
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(OrthoBox_Explicit, "orthoBox");

    /** A coordinate system which has as its "unit square"
     * the box
     * of its parent.
     * No parameters.
     */
    class UnitSqBox: 
	public ScaleXYZ,
	public DependentPrimitiveTransform
    {
    public:
	enum { NDepends = 1 }; // just parent
	template<class SPtr> void setParams(SPtr depends) {
	    Pt p = depends[0]->getSqSize();
	    vec.x = p.x;
	    vec.y = p.y;
	    vec.z = 1;
	}
    };

    VOB_PRIMITIVETRANS_DEFINED(UnitSqBox, "unitSq");



    /** A coordinate system which is placed in the middle between two other
     *  coordinate systems.
     */
    class Between: 
        public Ortho,
	public DependentPrimitiveTransform
    {
    public:
        enum { NDepends = 2 };

        template<class SPtr> void setParams(SPtr depends) {
	    ZPt a = depends[0]->transform(ZPt(0,0,0));
	    ZPt b = depends[1]->transform(ZPt(0,0,0));
	    
	    x = .5*(a.x+b.x);
	    y = .5*(a.y+b.y);
	    z = (a.z > b.z) ? a.z : b.z;
	  
	    sx = 1;
	    sy = 1;
	}
    };      
    VOB_PRIMITIVETRANS_DEFINED(Between, "between");


    /** A translation using polar coordinates.
     *  (The important thing is that it is *interpolated* in polar coordinates, too.)
     *
     *  The parameters are distance and angle.
     *
     */
    class TranslatePolar:
	    public PrimitiveTransform,
	    public GLPerformablePrimitiveTransform
    {
    public:
        /** Distance.
	 */
        float d;
	/** Angle, degs.
	 */
	float a;
	/** Sine and cosine of angle.
	 */
	float s, c;

	void angleWasSet() {
	    s = sin(a * M_PI / 180);
	    c = cos(a * M_PI / 180);
	}

	void tr(const ZPt &from, ZPt &to) const {
	    to = ZPt( from.x+d*c, from.y+d*s, from.z );
	}
        virtual void performGL() const {
	    glTranslatef(d*c, d*s, 0);
	}
	typedef TranslatePolar InverseType;
	void inverse(InverseType &inv) const {
	    inv.d = d;
	    inv.a = 360-a;
	    inv.angleWasSet();
	}
    };

    /** Explicit parametrization of translation in polar coords.
     * Parameters: distance, angle (in degrees).
     */
    class TranslatePolar_Explicit :
	    public TranslatePolar,
	    public ParametrizedPrimitiveTransform
    {
    public:
	enum { NParams = 2 };
	template<class Ptr> void setParams(Ptr p) {
	    d = p[0];
	    a = p[1];

	    angleWasSet();
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(TranslatePolar_Explicit, 
			       "translatePolar");

}
}

#endif
