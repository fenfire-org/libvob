/*
Quadrics.hxx
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

#ifndef VOB_GEOM_QUADRICS
#define VOB_GEOM_QUADRICS

#include <vob/Vec23.hxx>

namespace Vob {

namespace Geom {

    /** Project a point through another point to a circle.
     * @param pt the point to project
     * @param p the projection center
     * @param ctr The center of the circle
     * @param rad The radius of the circle
     * @param ansdir -1 or 1 : 1 = take further-away answer
     * @param success Optional, the pointer to a boolean set if the projection was possible.
     * @return The projected point, or if not possible, the original pt.
     */
    inline Vec project2circle(Vec pt, Vec p, Vec ctr, float rad, int ansdir = 1,
	    bool *success = 0) {
	Vec ao = pt - ctr;
	Vec ap = pt - p;

	// Coefficients of the 2nd degree equation
	float a = dot(ap, ap);
	float b = 2*dot(ap, ao);
	float c = dot(ao, ao) - rad * rad;

	// determinant of the equation
	float det = b*b - 4*a*c;

	float ans = (det > 0 ? (-b + ansdir * sqrt(det)) / (2*a) : 0);
	if(success) *success = (det > 0);

	return pt + ans * ap;

    }

    /** Given a point and a normal there and a circle,
     * find the circle that passes through the point
     * with the given normal (normal pointing to *center* 
     * of circle) and is tangential to the circle.
     * Norm must be normalized.
     */
    inline Vec circle__point_norm_circle(
	    Vec pt,
	    Vec norm,
	    Vec ctr,
	    float r,
	    bool *success = 0) {
	Vec q = pt - r * norm;
	Vec c = lerp(ctr, q, .5);
	using namespace Vec23;
	HL lc =  HPt(c).line(HPt(c + (q - ctr).cw90()));
	HL ln =  HPt(q).line(pt);
	HPt it = lc.intersection(ln);
	if(it.finite()) {
	    if(success)
		*success = 1;
	    return (Vec)it;
	} else {
	    if(success)
		*success = 0;
	    return pt;
	}
    }


    /** A rather special geometric problem arising from fillets.
     * Given a 2D point and a normal, find the ellipse that passes through
     * the point with the normal that also passes through origin tangent to the 
     * X axis and is symmetric w.r.t. the Y axis.
     * @return A 2D point, y component is vertical radius of ellipse, x component is
     * 			horizontal radius.
     */
    Vec symmellipse__point_norm(Vec point, Vec normal);

}
}


#endif
