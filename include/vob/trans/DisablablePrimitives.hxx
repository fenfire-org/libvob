/**
 *  DisablablePrimitives.hxx
 *    
 *    Copyright (c) 2003, Asko Soukka
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
 * Written by Asko Soukka
 */

#ifndef VOB_DISABLABLEPRIMITIVES_HXX
#define VOB_DISABLABLEPRIMITIVES_HXX

#ifndef VOB_PRIMITIVETRANS_DEFINED
#define VOB_PRIMITIVETRANS_DEFINED(x, n)
#endif

#include <GL/gl.h>

#include <vob/trans/Primitives.hxx>

#undef F
#define F <vob/trans/LinearPrimitives.hxx> 
#include <vob/trans/leaf.hxx>

#include <vob/intersect.hxx>
#include <vob/Debug.hxx>

namespace Vob {
namespace Primitives {

    using ::Vob::Util::findBoundingBox;
    using ::Vob::Util::findDistortedBoundingBox;
    using ::Vob::Util::parallelRectIntersect;
       
DBGVAR(dbg_cull, "Vob.Primitives.Cull");

    /** A disablable identity transformation.
     * A transformation which produces exactly the same output as 
     * its parent, including the box size, but which may at setParams
     * time disable itself.
     * */
    class DisablableIdentity :
      public Box,
      public DisablablePrimitiveTransform {
    public:
      /** The flag that determines whether this transformation
       * is enabled.
       */
      bool enabled;

      DisablableIdentity() : Box(Pt(1,1)) { }

      bool shouldBeDrawn() const {
	if (dbg_cull) printf("\nCull.shouldBeDrawn() called");
	return enabled;
      }

      typedef DisablableIdentity InverseType;
      void inverse(InverseType &into) const { into = *this; }
    };

    /** Culling transform can decide not to be drawn when its
     * parents' boxes do not intersect.
     */
    class Cull : 
      public DisablableIdentity,
      public DependentPrimitiveTransform
    {
    public:
      enum { NDepends = 3 };
      template<class SPtr> void setParams(SPtr depends) {
	box = depends[0]->getSqSize();
	if (cullShouldBeDrawn(depends[1], depends[2])) enabled = true;
	else enabled = false;

	if (dbg_cull) {
	  if (enabled) printf("\nCull.enabled: true");
	  else printf("\nCull.enabled: false");
	}
      }

      /** Cull transforms' shouldBeDrawn() returns true always when boxes 
       * of its test and clip coordinate systems do intersect. When
       * the boxes don't intersect, it should retun false.  
       */
      static bool cullShouldBeDrawn(const Transform *test, const Transform *clip) {
	Pt box;
	float hyp;
	/** Lower left and upper right points of bounding boxes for
	 * parents' box (after transformation).
	 */
	ZPt p1, p2, p3, p4;
	
	box = test->getSqSize();
	hyp = hypot(box.x/2, box.y/2);    
	if (test->nonlinearity(ZPt(box.x/2, box.y/2, 0), hyp) > 1/hyp)
	  findDistortedBoundingBox(test, p1, p2);
	else findBoundingBox(test, p1, p2);
	
	box = clip->getSqSize();
	hyp = hypot(box.x/2, box.y/2);
	if (clip->nonlinearity(ZPt(box.x/2, box.y/2, 0), hyp) >  1/hyp) 
	  findDistortedBoundingBox(clip, p3, p4);
	else findBoundingBox(clip, p3, p4);
	
	return (parallelRectIntersect(p1, p2, p3, p4));
      }
    };
    VOB_PRIMITIVETRANS_DEFINED(Cull, "cull");
}
}

#endif
