/*
Vec23.hxx
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_VEC23_HXX
#define VOB_VEC23_HXX
#include <iostream>
#include <math.h>

namespace Vob {
/** Simple vectors for 2D / 3D graphics.
 */
namespace Vec23 {
    using std::ostream;

    template <class T>class Vector;
    template <class T>class Vector3;

    inline float finitize(float f) {
	return finitef(f) ? f : 0;
    }

    /** A 2D vector.
     */
    template <class T>class Vector {
    public:
	T x, y;
	/** Null.
	 */
	Vector() : x(0), y(0) { }
	/** Given components.
	 */
	Vector(T x, T y) : x(x), y(y) { }
	/** Create from another Vector.
	 */
	template<class F>
	    Vector(const Vector3<F> &v) : x(v.x), y(v.y) { }

	/** Create from an array.
	 */
	Vector(T *v) : x(v[0]), y(v[1]) { }

	template<class F>
	    Vector &operator=(const Vector<F> &v) { x = v.x; y = v.y; return *this; }

	template<class F>
	    operator Vector<F>() const { return Vector<F>(x, y); }

	/** Making all components finite.
	 */
	Vector finitized() const {
	    return Vector(finitize(x), finitize(y));
	}

	/** Multiplication by scalar.
	 */
	Vector operator*(const double &s) const { return Vector(s * x, s * y); }
	/** In-place multiplication by scalar.
	 */
	template<class U>const Vector &operator*=(const U &s) { x *= s; y *= s; return *this; }

	/** Normalize the normalized version of this vector.
	 */
	Vector normalized() const { return (1/length()) * *this ; }

	/** Vector negation.
	 */
	Vector<T> operator-() const { return Vector<T>(-x, -y); }
	/** Vector addition.
	 */
	Vector operator+(const Vector<T>&v) const { return Vector(x+v.x, y+v.y); }
	/** Vector subtraction.
	 */
	Vector operator-(const Vector<T>&v) const { return Vector(x-v.x, y-v.y); }
	/** Return a vector like this, but rotated 90 degrees clockwise.
	 */
	Vector cw90() const { return Vector(y, -x); }
	/** Return the length of this vector.
	 */
	double length() const { return hypot(x, y); }

	/** Dot this vector with another.
	 */
	double dot(const Vector<T> &v) const { return x * v.x + y * v.y; }

	/** Cross this 2D vector with another - 
	 * gives the sine of the angle between the two,
	 * multiplied by the lengths.
	 * Useful for telling which side of a given vector you are on.
	 */
	double cross(const Vector<T> &v) const {
	    return x * v.y - y * v.x;
	}

	double atan() const {
	    return atan2(y, x);
	}
    };

    /** Multiply vector by scalar.
     */
    template<class T> Vector<T> operator*(const double &s, const Vector<T> &v) {
	return v * s;
    }

    /** Print out a vector.
     */
    template<class T> inline ostream& operator<<(ostream &o, const Vector<T> &p) {
	return o << "[vector "<<p.x<<" "<<p.y<<"]";
    };

    /** A 3D vector.
     */
    template <class T>class Vector3 {
    public:
	T x, y, z;
	/** Null.
	 */
	Vector3() : x(0), y(0), z(0) { }
	/** From components.
	 */
	Vector3(T x, T y, T z) : x(x), y(y), z(z) { }
	/** From a 2D vector and an optional Z-component.
	 */
	Vector3(const Vector<T> &v, float z = 0) : x(v.x), y(v.y), z(z) { }

	/** Making all components finite.
	 */
	Vector3 finitized() const {
	    return Vector3(finitize(x), finitize(y), finitize(z));
	}
	
	/** Multiplication by scalar.
	 */
	Vector3 operator*(const double &s) const { return Vector3(s * x, s * y, s * z); } ;

	/** Multiplication by scalar.
	 */
	template<class U> const Vector3 &operator*=(const U &s) { 
	    x *= s; y *= s; z *= s;
	    return *this; 
	} ;
	/** Multiplication by reciprocal of a scalar.
	 */
	template<class U> const Vector3 &operator/=(const U &s) { 
	    x /= s; y /= s; z /= s;
	    return *this; 
	} ;

	/** Multiplication by reciprocal of a scalar.
	 */
	template<class U> Vector3 operator/(const U &s) { 
	    return Vector3(x/s, y/s, z/s);
	}

	/** Return the normalized version of this vector.
	 */
	Vector3 normalized() const { return (1/length()) * *this ; }

	/** Negation.
	 */
	Vector3<T> operator-() const { return Vector3<T>(-x, -y, -z); }

	/** Addition.
	 */
	Vector3 operator+(const Vector3<T>&v) const { return Vector3(x+v.x, y+v.y, z+v.z); }

	/** Member-wise multiplication of vectors!
	 */
	Vector3 operator*(const Vector3<T>&v) const { 
	    return Vector3(x*v.x, y*v.y, z*v.z); }

	/** Vector addition.
	 */
	const Vector3 &operator+=(const Vector3<T>&v) { x+=v.x; y+=v.y; z+=v.z; return *this; }
	/** Vector subtraction.
	 */
	const Vector3 &operator-=(const Vector3<T>&v) { x-=v.x; y-=v.y; z-=v.z; return *this; }

	/** Vector subtraction.
	 */
	Vector3 operator-(const Vector3<T>&v) const { return Vector3(x-v.x, y-v.y, z-v.z); }

	/** Return a vector like this, but rotated 90 degrees clockwise IN X AND Y.
	 */
	Vector3 cw90() const { return Vector3(y, -x, z); }

	/** Dot product with another 3-vector.
	 */
	double dot(const Vector3<T> &v) const { return x * v.x + y * v.y + z * v.z; }
	/** Dot product of x and y components only.
	 */
	double dot2(const Vector3<T> &v) const { return x * v.x + y * v.y ; }
	/** Length of this vector.
	 */
	double length() const { return hypot(hypot(x, y), z); }
	/** Length of this vector in xy plane.
	 */
	double xylength() const { return hypot(x, y); }
	/** Cross-product with another vector.
	 */
	Vector3 crossp(const Vector3<T> &v) const {
	    return Vector3(
		    y * v.z - z * v.y,
		    z * v.x - x * v.z,
		    x * v.y - y * v.x
		    );
	}
    };

    /** Multiply vector by scalar.
     */
    template<class T> Vector3<T> operator*(const double &s, const Vector3<T> &v) {
	return v * s;
    }

    /** Output a 3-vector.
     */
    template<class T> inline ostream& operator<<(ostream &o, const Vector3<T> &p) {
	return o << "[vector "<<p.x<<" "<<p.y<<" "<<p.z<<"]";
    };


    /** A rectangle.
     */
    template <class T>class Rectangle {
    public:
	T x, y, w, h;
	/** Create a rectangle with the given components.
	 */
	Rectangle(T x, T y, T w, T h) : x(x), y(y), w(w), h(h) { }
	/** Get the first X-coordinate.
	 */
	T x0() { return x; }
	/** Get the last X-coordinate.
	 */
	T x1() { return x + w; }
	/** Get the first Y-coordinate.
	 */
	T y0() { return y; }
	/** Get the last Y-coordinate.
	 */
	T y1() { return y + h; }

	/** Get the upper-left corner.
	 */
	Vector<T> ul() { return Vector<T>(x, y); }
	/** Get the lower-right corner.
	 */
	Vector<T> lr() { return Vector<T>(x +w, y +h); }
    };

    /** Output a rectangle.
     */
    template<class T>inline ostream& operator<<(ostream &o, const Rectangle<T> &r) {
	return o << "[rect "<<r.x<<" "<<r.y<<" "<<r.w<<" "<<r.h<<"]";
    }

    /** Shorthand.
     */
    typedef Vector<float> Pt;
    /** Shorthand.
     */
    typedef Vector3<float> ZPt;
    /** Shorthand.
     */
    typedef Vector<float> Vec;
    /** Shorthand.
     */
    typedef Vector3<float> ZVec;

    /** Shorthand.
     */
    typedef Vector<double> PtD;
    /** Shorthand.
     */
    typedef Vector3<double> ZPtD;
    /** Shorthand.
     */
    typedef Vector<double> VecD;
    /** Shorthand.
     */
    typedef Vector3<double> ZVecD;



    /** Linear interpolation.
     * Returns a + fract*(b-a)
     */
    template<class X> inline X lerp(X a, X b, double fract) {
	return a + fract * (b-a);
    }
    inline float lerp(double a, double b, double fract) {
	return a + fract * (b-a);
    }



    template<class T> void cross3(T x1, T y1, T z1, T x2, T y2, T z2, T &xr, T &yr, T &zr) {
	xr = y1 * z2 - y2 * z1;
	yr = z1 * x2 - z2 * x1;
	zr = x1 * y2 - x2 * y1;
    }

#define VEC23_EPS 0.000001

    template<class T> class HLine2;

    /** A homogeneous 2D point.
     * Useful for computing: no fear of bad divisions!
     */
    template<class T> struct HPoint2 {
	T x, y, w;
	HPoint2() {}
	HPoint2(T x, T y, T w) : x(x), y(y), w(w) { }
	HPoint2(ZPt p) : x(p.x), y(p.y), w(1) { }
	HPoint2(Pt p) : x(p.x), y(p.y), w(1) { }

	HLine2<T> line(HPoint2 p) {
	    T a, b, c;
	    cross3(x, y, w, p.x, p.y, p.w, a, b, c);
	    return HLine2<T>(a, b, c);
	}

	bool finite() {
	    T r = fabs(x) + fabs(y);
	    T i = fabs(w);
	    if(i < r * VEC23_EPS) return false;
	    return true;
	}

	operator Vector<T> () {
	    return Vector<T>(x/w, y/w);
	}

    };
    template<class T> inline ostream& operator<<(ostream &o, const HPoint2<T> &p) {
	return o << "[2hv "<<p.x<<" "<<p.y<<" "<<p.w<<"]";
    };

    /** A homogeneous 2D line.
     */
    template<class T> struct HLine2 {
	T a, b, c;
	HLine2() {}
	HLine2(T a, T b, T c) : a(a), b(b), c(c) { }

	HPoint2<T> intersection(HLine2 l) {
	    T x, y, w;
	    cross3(a, b, c, l.a, l.b, l.c, x, y, w);
	    return HPoint2<T>(x, y, w);
	}

	bool finite() {
	    T r = fabs(a) + fabs(b);
	    T i = fabs(c);
	    if(r < i * VEC23_EPS) return false;
	    return true;
	}

    };
    template<class T> inline ostream& operator<<(ostream &o, const HLine2<T> &p) {
	return o << "[2hl "<<p.a<<" "<<p.b<<" "<<p.c<<"]";
    };

    typedef HPoint2<float> HPt;
    typedef HLine2<float> HL;

    template<class T> T dot(Vector<T> a, Vector<T> b) { return a.dot(b); }


    /** Give the direction vector for a given angle:
     * (sin angle, cos angle).
     */
    inline Vec dirVec(float angle) {
	return Vec(cos(angle), sin(angle));
    }
}

// Export some to Vob

using Vec23::ZPt;
using Vec23::ZVec;
using Vec23::Pt;
using Vec23::Vec;
using Vec23::ZPtD;
using Vec23::ZVecD;
using Vec23::PtD;
using Vec23::VecD;
using Vec23::lerp;
using Vec23::dirVec;
}
#endif
