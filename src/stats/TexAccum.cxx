/*
TexAccum.cxx
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

#include <vob/Debug.hxx>
#include <vob/stats/TexAccum.hxx>

namespace Vob {
namespace Stats {
    DBGVAR(dbg_texaccum, "Stats.TexAccum");

    TexAccum::TexAccum(Stats::Statistics *stats) 
	    : Stats::Collector(stats) 
    {
	clear();
    }

    void TexAccum::add(ZPt &p1, ZPt &p2, ZPt &p3, Pt &t1, Pt &t2, Pt &t3, float texAreaMult) {
	Vec vp1 = Vec(p2) - Vec(p1);
	Vec vp2 = Vec(p3) - Vec(p1);

	Vec vt1 = t2 - t1;
	Vec vt2 = t3 - t2;

	double parea = fabs(vp1.cross(vp2)) / 2;
	double tarea = texAreaMult * fabs(vt1.cross(vt2)) / 2;

	if(parea < 1) return;

	int exponent;
	frexp(parea / tarea, &exponent);

	int mip = exponent / 2;

	DBG(dbg_texaccum) << "Stats got "<<mip<<" "<<parea<<"\n";

	if(mip < 0) return;
	if(mip >= NLEVELS) mip = NLEVELS-1;

	this->pixels[mip] += parea;


	gotStatistics();
    }

    void TexAccum::clear() {
	DBG(dbg_texaccum) << "Stats clear\n";
	for(int i=0; i<NLEVELS; i++) pixels[i] = 0;
    }

}
}

