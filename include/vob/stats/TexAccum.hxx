/*
TexAccum.hxx
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

#ifndef VOB_LOD_TEXPOLY_HXX
#define VOB_LOD_TEXPOLY_HXX

#include <vob/Vec23.hxx>
#include <vob/stats/Stats.hxx>

namespace Vob {
namespace Stats {
    /** A class that allows accumulation of
     * estimated LODs of textured polys.
     * Used to estimate what mipmap level of texture would
     * be required.
     */
    struct TexAccum : public Stats::Collector {
	TexAccum(Stats::Statistics *stats);

	enum { NLEVELS = 20 };

	/** The number of pixels estimated to have been rendered
	 * at each level of detail.
	 * Level 0 : texture coordinates (0,1)x(0,1) == 1 pixel,
	 * Level 1 : texture coordinates (0,.5)x(0,.5) == 1 pixel, ...
	 */
	double pixels[NLEVELS];

	/** Add a triangle.
	 * Currently, this will calculate a rough estimate
	 * of the LOD, assuming isotropic transformations, 
	 * by calculating the ratio of the areas; a more accurate
	 * algorithm may be substituted later.
	 * @param p1,p2,p3 The corners in screen space, in pixel coords
	 * @param t1,t2,t3 The corners in texture coordinate space.
	 * @param texAreaMult The area in texture space will be multiplied by this.
	 */
	void add(ZPt &p1, ZPt &p2, ZPt &p3, Pt &t1, Pt &t2, Pt &t3, float texAreaMult);
	virtual void clear();
    };
}
}



#endif
