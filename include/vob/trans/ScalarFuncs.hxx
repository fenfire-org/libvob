/*
ScalarFuncs.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 */
/*
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_SCALARFUNCS_HXX
#define VOB_SCALARFUNCS_HXX

#ifndef VOB_PRIMITIVETRANS_DEFINED
#define VOB_PRIMITIVETRANS_DEFINED(x, n)
#endif

#include <vob/trans/Primitives.hxx>

namespace Vob {
namespace Primitives {
    /** A 1-dimensional rational function, second degree in
     * both numerator and denominator.
     */
    class Rational1D_2_2 :
	    public PrimitiveTransform
    {
    public:
	float coeff[6];
	void tr(const ZPt &from, ZPt &to) const {
	    to.y = 0; to.z = 0;
	    double x = from.x;
	    double x2 = from.x * from.x;
	    to.x = 
		(coeff[0] + coeff[1]*x + coeff[2]*x2) /
		(coeff[3] + coeff[4]*x + coeff[5]*x2) ;
	}

    };

    class Rational1D_2_2_Explicit :
	    public Rational1D_2_2, 
	    public ParametrizedPrimitiveTransform,
	    public NonInvertiblePrimitiveTransform {
    public:
	enum { NParams = 6 };
	template<class Ptr> void setParams(Ptr p) {
	    for(int i=0; i<6; i++)
		coeff[i] = p[i];
	}
    };

    VOB_PRIMITIVETRANS_DEFINED(Rational1D_2_2_Explicit, "rational1D22");


    /** A 1-dimensional power function a x^b
     */
    class Power1D :
	    public PrimitiveTransform
    {
    public:
	float a, b;
	void tr(const ZPt &from, ZPt &to) const {
	    to.y = 0; to.z = 0;
	    to.x = a * pow(from.x, b);
	}

    };

    class Power1D_Explicit :
	    public Power1D, 
	    public ParametrizedPrimitiveTransform,
	    public NonInvertiblePrimitiveTransform {
    public:
	enum { NParams = 2 };
	template<class Ptr> void setParams(Ptr p) {
	    a = p[0];
	    b = p[1];
	}
    };

    VOB_PRIMITIVETRANS_DEFINED(Power1D_Explicit, "power1D");


    /** A sum of 1-dimensional power functions: a x^b + c x^d
     */
    class Power1D_2 :
	    public PrimitiveTransform
    {
    public:
	float a, b, c, d;
	void tr(const ZPt &from, ZPt &to) const {
	    to.y = 0; to.z = 0;
	    to.x = a * pow(from.x, b) + c * pow(from.x, d);
	}

    };

    class Power1D_2_Explicit :
	    public Power1D_2, 
	    public ParametrizedPrimitiveTransform,
	    public NonInvertiblePrimitiveTransform {
    public:
	enum { NParams = 4 };
	template<class Ptr> void setParams(Ptr p) {
	    a = p[0];
	    b = p[1];
	    c = p[2];
	    d = p[3];
	}
    };

    VOB_PRIMITIVETRANS_DEFINED(Power1D_2_Explicit, "power1D2");
}
}

#endif
