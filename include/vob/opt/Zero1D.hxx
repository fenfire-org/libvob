/*
Zero1D.hxx
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

namespace Vob {
namespace Opt {

    /** A simple 1D zero finder that starts with two points where the function must
     * have a different sign.
     */
    template<class F> double findZero1D(const F &f, double low, double high, int nrounds,
		bool &success) {
	double fl = f(low);
	double fh = f(high);
	if(fl * fh >= 0) {
	    success = false;
	    return low;
	}
	for(int i=0; i<nrounds; i++) {
	    double mid = lerp(low, high, .5);
	    double fm = f(mid);
	    if(fm * fl >= 0) {
		low = mid;
		fl = fm;
	    } else {
		high = mid;
		fh = fm;
	    }
	}
	success = true;
	return lerp(low, high, .5);
    }

}
}
