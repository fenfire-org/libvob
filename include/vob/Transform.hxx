/*
Transform.hxx
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

#ifndef VOB_TRANSFORM_HXX
#define VOB_TRANSFORM_HXX

#include <vob/Vec23.hxx>

namespace Vob {

#define ATTR(c)  __attribute__(c)

    /** A single "final" (i.e. composited) transform, possibly defined
     * hierarchically through primitive transforms.
     * This is the <b>external</b> coordsys interface
     * for use by vobs.
     */
    class Transform {
    private:
        bool activated;
    public:
        inline void setActivated(bool b) { this->activated = b; }
        inline bool isActive() { return this->activated; }

	virtual ~Transform() { };

	/** Check whether this coordinate system should be drawn with
	 * the current parameters.
	 * This method should not recurse; the parents will already 
	 * have been asked. It should only consider the parameters
	 * of the current coordinate system.
	 */
	virtual bool shouldBeDrawn() const = 0;

	/** Return the given ZPt transformed
	 * into this coordinate system.
	 * Note that some "coordinate systems" may overload
	 * this to return e.g. a color in the ZPt always,
	 * without regard to the parameter.
	 */
	virtual ZPt transform(const ZPt &p) const ATTR((pure)) = 0;

	/** Call glVertex with the given ZPt transformed
	 * into this coordinate system.
	 */
	virtual void vertex(const ZPt &p) const = 0;

	/** Whether the transformation is nonlinear.
	 */
	virtual bool isNonlinear() const ATTR((pure)) = 0;

	/** How nonlinear is the coordinate system at the given point.
	 * The return value is 1/l where l would be a reasonable length 
	 * for dicing.
	 * Returns 0 if dicing is not required.
	 * XXX This needs more thought.
	 */
	virtual float nonlinearity(const ZPt &p, float radius) const ATTR((pure)) = 0; 

	/** Whether this transformation can be performed by OpenGL
	 * alone by using the transformation matrix. 
	 * If true, calling performGL allows the caller
	 * to use plain glVertex calls to place vertices using this
	 * transform.
	 */
	virtual bool canPerformGL() const ATTR((pure)) = 0;

	/** Try to perform the GL operations to set this
	 * coordinate system in the current matrix.
	 * Only the topmost matrix on the matrix stack may be altered by 
	 * this routine, no other GL state. The matrix
	 * used is determined by the GL current matrix state.
	 * <p>
	 * This method will NOT set up vertex programs or change
	 * any other OpenGL state.
	 * @return True if successful, but if false is
	 * 	returned, then the matrix
	 * 	is in an undefined state. If this is
	 * 	not acecptable, try canPerformGL() first.
	 */
	virtual bool performGL() const = 0;

	typedef Transform InverseType;

	/** Get the inverse of this coordinate system.
	 * Always returns non-null but it is not guaranteed
	 * that this will work properly. (XXX canInvert() ?)
	 * The returned inverse is owned by this object and
	 * mustn't be deleted by the caller.
	 */
	virtual const Transform &getInverse() const = 0;

	/** Print this coordinate system into the given
	 * ostream.
	 */
	virtual void dump(std::ostream &out) const = 0;

	/** Get the size of the "unit square" of this coordinate system.
	 * For most coordinate systems, this will be Pt(1,1) but there are
	 * some which alter this, for the purpose of catching mouse clicks
	 * at a larger area. A mouse click is "in" this coordinate system,
	 * if it is in the area Pt(0,0) .. getSqSize()
	 *
	 * NOTE: Must be implemented also at GLVobCoorder.java.
	 */
	virtual Pt getSqSize() const ATTR((pure)) = 0;
    };

    /** A null transformation: directly the OpenGL coordinates.
     */
    class RootCoords : public Transform {
    public:
	RootCoords();
	virtual ZPt transform(const ZPt &p) const {
	    return p;
	}
	virtual void vertex(const ZPt &p) const ;
	virtual bool canPerformGL() const { return true; }
	virtual bool performGL() const {
	    return true;
	}
	virtual bool isNonlinear() const {
	    return false;
	}
	virtual float nonlinearity(const ZPt &p, float radius) const { 
	    return 0;
	}
	virtual bool shouldBeDrawn() const {
	    return true;
	}
	virtual const Transform &getInverse() const {
	    return *this;
	}
	virtual void dump(std::ostream &out) const {
	    out << " ROOT ";
	}
	virtual Pt getSqSize() const { 
	    return Pt(1,1);
	}
    };



}

#endif
