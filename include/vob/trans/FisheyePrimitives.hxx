/*
FisheyePrimitives.hxx
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

#ifndef VOB_FISHEYEPRIMITIVES_HXX
#define VOB_FISHEYEPRIMITIVES_HXX


#ifndef VOB_PRIMITIVETRANS_DEFINED
#define VOB_PRIMITIVETRANS_DEFINED(x, n)
#endif


#include <GL/gl.h>

#include <vob/trans/Primitives.hxx>

namespace Vob {
namespace Primitives {

    namespace Fisheye {

	//For given mag, the magnification range is [ 1 - mag/8, 1 + mag ]
	struct scalar_mag_r2 {
	    float mag;
	    float operator()(float r2) const {
		return 1 + mag / (1 + r2);
	    }
	};

	//For given mag, the magnification range is [ 1, 1 + mag ]
	struct scalar_mag_r2_shift {
	    float mag;
	    float operator()(float r2) const {
		float m = 1 + 0.5 * mag / (1 + r2);
		if (r2 > 0) {
		    float r = sqrt(r2);
		    m += 0.5 * r * atan(r) / r;
		}
		return m;
	    }
	};

	//For given z, the magnification range is [ 1, 1 + z ]
	struct scalar_mag_atan {
	    scalar_mag_atan() : z(1) { }
	    scalar_mag_atan(float mag) : z(mag-1) { }
	    float z;
	    float operator()(float r2) const {
		if (r2 > 0) {
		    float r = sqrt(r2);
		    return 1 + z * atan(r) / r;
		}
		return 1;
	    }
	    float func(float r) const {
		return 1 + z * atan(r) / r;
	    }
	    float inverse(float r2) const {
		if(z <= 0) return 1; // Fail gracefully
		if (r2 > 0) {
		    float r_orig = sqrt(r2);
		    float ylow = r_orig - z * M_PI / 2;
		    float yhigh = r_orig;
		    // Then, loop a little bit // XXX Improve
		    for(int i=0; i<18; i++) {
			float y = 0.5*(ylow+yhigh);
			float c = y * func(y);
			if(c < r_orig) {
			    ylow = y;
			} else {
			    yhigh = y;
			}
		    }
		    return 0.5*(ylow+yhigh) / r_orig;
		}
		return 1;
	    }
	};

	template<class F> struct inverse_vector_mag_isotropic ;

	template<class F> struct vector_mag_isotropic {
	    vector_mag_isotropic() { }
	    vector_mag_isotropic(F &f) : f(f) { }
	    void setMag(float mag) { f = F(mag); }
	    F f;
	    typedef inverse_vector_mag_isotropic<F> InverseType;
	    ZPt operator() (const ZPt &p) const {
		float r2 = p.x * p.x + p.y * p.y;
		float m = f(r2);
		return ZPt(m * p.x, m * p.y, p.z);
	    }
	};

	template<class F> struct inverse_vector_mag_isotropic {
	    inverse_vector_mag_isotropic() { }
	    inverse_vector_mag_isotropic(F &f) : f(f) { }
	    void setMag(float mag) { f = F(mag); }
	    F f;
	    typedef vector_mag_isotropic<F> InverseType ;
	    ZPt operator() (const ZPt &p) const {
		float r2 = p.x * p.x + p.y * p.y;
		float m = f.inverse(r2);
		return ZPt(m * p.x, m * p.y, p.z);
	    }
	};

    }

    /** Isotropically distorted coordinate system.
     * Parameter layout: x, y (of center), log(mag), log(min), w, h.
     * W and h give the width and height in the inside coordinate system
     * of the zoomed area.
     */
    template<class F> class DistortPrimitiveTransform  :
	    public PrimitiveTransform, 
	    public ParametrizedPrimitiveTransform ,
	    public NonlinearPrimitiveTransform
    {
	friend class DistortPrimitiveTransform<typename F::InverseType>;
    public:
	float x, y;
	float w, h;
	float mmin;
	float mmax;
	bool iaminverse; // kludge
	F distort;
	enum { NParams = 6 };
	template<class Ptr> void setParams(Ptr p) {
	    x = p[0];
	    y = p[1];
	    w = p[2];
	    h = p[3];
	    mmax = exp(p[4]);
	    mmin = exp(p[5]);
	    distort.setMag(mmax / mmin);
	    iaminverse = false;
	}
	void tr(const ZPt &from, ZPt &to) const {
	    ZPt p = ZPt((from.x-x) / w, (from.y-y)/ h, from.z);
	    if(iaminverse) {
		p.x *= 1/mmin;
		p.y *= 1/mmin;
	    }
	    to = distort(p);
	    to.x *= w; to.y *= h;
	    if(!iaminverse) {
		to.x *= mmin;
		to.y *= mmin;
	    }
	    to.x += x; to.y += y;
	}
	typedef DistortPrimitiveTransform<typename F::InverseType>  InverseType; // XXX !!!
	void inverse(InverseType &inv) const {
	    inv.x = x;
	    inv.y = y;
	    inv.w = w;
	    inv.h = h;
	    inv.mmin = mmin;
	    inv.mmax = mmax;
	    inv.distort.setMag(mmax/mmin);
	    inv.iaminverse = !iaminverse;
	}

	float nonlinearity(const ZPt &p, float radius) const { 
	    float magfact = 10 * (mmax/mmin-1);
	    float clampmagfact = magfact > 1 ? 1 : magfact;

	    float wh = 0.5*(w+h);
	    float dist = hypot((p.x-x)/w, (p.y-y)/h) - radius/wh;
	    if(!finite(dist)) return 1;
	    if(dist < 0) dist = 0;

	    float nonl_at_zero = clampmagfact * 12;

	    float half_dist = 1.5;
	    return nonl_at_zero * half_dist / (dist + half_dist);
	}
    };

    typedef DistortPrimitiveTransform<Fisheye::vector_mag_isotropic<Fisheye::scalar_mag_atan> > 
	UsualDistortPrimitiveTransform;

    VOB_PRIMITIVETRANS_DEFINED(UsualDistortPrimitiveTransform, "distort");
}
}

#endif
