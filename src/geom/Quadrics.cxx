/*
Quadrics.cxx
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

#include <boost/format.hpp>

#include <vob/Debug.hxx>
#include <vob/opt/Zero1D.hxx>
#include <vob/geom/Quadrics.hxx>

namespace Vob {

namespace Geom {

    DBGVAR(dbg_quadrics, "Quadrics");

    using boost::format;

    struct SymEllProblem {
	VecD point;
	VecD normal;

	VecD centerPoint(double s) const {
	    // Point and normal in this scaled problem
	    VecD p(s * point.x, point.y);
	    VecD n(normal.x / s, normal.y);
	    n = n.normalized();

	    // Find point on x==0
	    double m = - p.x / n.x;
	    VecD p0 = p + m * n;
	    // Invariant: p0.x == 0
	    DBG(dbg_quadrics) << 
		format("sep: p: %s, n:%s, m:%s, p0:%s\n") 
		    % p % n % m % p0;
	    return p0;

	}

	/** Calculate the discrepancy: a number that is zero when 
	 * the s given to us is right and positive and negative otherwise.
	 */
	double operator()(double s) const {
	    // Point in the scaled problem
	    VecD p(s * point.x, point.y);

	    VecD p0 = centerPoint(s);
	    double ret = (p0.length() - (p0-p).length());
	    DBG(dbg_quadrics) << 
		format("s: %s, p:%s, p0:%s ret::%s\n") 
		    % s % p % p0 % ret;
	    return ret;
	}

    };

    Vec symmellipse__point_norm(Vec point, Vec normal) {
	SymEllProblem prob;
	prob.point = point;
	prob.normal = normal;

	bool succ;
	double scale = Opt::findZero1D(prob, .001, 1000, 35, succ);

	VecD res = prob.centerPoint(scale);
	res.x = res.y / scale;
	return res;
    }

}
}
