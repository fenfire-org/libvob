/*
FunctionalPrimitives.hxx
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

#ifndef VOB_FUNCTIONALPRIMITIVES_HXX
#define VOB_FUNCTIONALPRIMITIVES_HXX

#ifndef VOB_PRIMITIVETRANS_DEFINED
#define VOB_PRIMITIVETRANS_DEFINED(x, n)
#endif

#include <GL/gl.h>

#include <vob/trans/Primitives.hxx>

namespace Vob {
namespace Primitives {

    /** A transform which applies another transform
     * to points before giving them to its parent transform.
     * With the hierarchicaltransform, this becomes the way
     * to concatenate a transformation to another.
     */
    struct Concat :
	    public PrimitiveTransform,
	    public PotentiallyGLPerformablePrimitiveTransform,
	    public DependentPrimitiveTransform,
	    public BoxPrimitiveTransform,
	    public DumpingPrimitiveTransform
    {
	const Transform *it;
	enum { NDepends = 2 };
	template<class SPtr> void setParams(SPtr depends) {
	    it = depends[1];
	}
	bool canPerformGL() const {
	    return it->canPerformGL();
	}
        bool performGL() const {
	    return it->performGL();
        }
	void tr(const ZPt &from, ZPt &to) const {
	    to = it->transform(from);
	}
	Pt getSqSize() const { return it->getSqSize(); }

	typedef Concat InverseType;
	void inverse(InverseType &inv) const {
	    inv.it = &(it->getInverse());
	}

        void dump(std::ostream &out) const { 
	    out << "OTHER:";
	    it->dump(out);
	}
    };

    VOB_PRIMITIVETRANS_DEFINED(Concat, "concat");

    /** A transform which applies the <em>inverse</em> of another transform
     * to points before giving them to its parent transform.
     * This can be used to generate inverse transforms in a way
     * that does not clash with the primitivetransform ideas.
     */
    struct ConcatInverse :
	    public Concat
    {
	template<class SPtr> void setParams(SPtr depends) {
	    it = &(depends[1]->getInverse());
	}
    };
    VOB_PRIMITIVETRANS_DEFINED(ConcatInverse, "concatInverse");
}
}

#endif
