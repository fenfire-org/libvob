/*
Fillets.hxx
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

#include <vob/Vec23.hxx>
#include <vob/geom/Quadrics.hxx>

namespace Vob {
namespace Geom {
    PREDBGVAR(dbg_fillets);

/** One side of a circularly filleted connection.
 */
struct CircleFillet {
    float r;
    Vec ctr;
    Vec filletcenter;
    Vec endPoint;
    float filletrad;
    float dist;

    /** A unit vector pointing to the direction of the 
     * connection side.
     */
    Vec dirconn; 
    /** A vector pointing to the direction of the tangent
     * side.
     */
    Vec dirtang;  

    CircleFillet() { }
    CircleFillet(
	    Vec ctr,
	    float r,
	    float angle,
	    float sign,
	    float dist,
	    float thick) {
	this->ctr = ctr;
	this->r = r;
	this->dist = dist;
	Vec dir = dirVec(angle);
	Vec p = ctr + dist * dir + .5 * thick * sign * dir.cw90();
	this->endPoint = p;
	this->filletcenter = 
	    circle__point_norm_circle(p, sign * dir.cw90(), ctr, r);
	this->filletrad = 
	    (filletcenter - p).length();

	this->dirconn = dir;
	this->dirtang = (filletcenter - ctr).normalized();
    }

    bool infillet(Vec dir) {
	return dirtang.cross(dirconn) * dirtang.cross(dir) >= 0 &&
		dirconn.cross(dirtang) * dirconn.cross(dir) >= 0;
    }

    bool atEnd(Vec dir) {
	if(!infillet(dir)) return false;
	bool succ;
	project2circle(ctr + dir, ctr, filletcenter,
		    filletrad, -1, &succ);
	if(!succ) {
	    return true;
	}
	return false;
    }
    Vec end() {
	return endPoint;
    }

    /** Get the radius of the filleted curve
     * at the given direction.
     */
    float rad(Vec dir) {
	if(infillet(dir)) {
	    // Same direction - try intersecting circle.
	    bool succ;
	    Vec in = project2circle(ctr + dir, ctr, filletcenter,
			filletrad, -1, &succ);
	    if(succ) {
		return (in-ctr).length();
	    } else {
		return dist;
	    }

	} else {
	    return r;
	}
    }


};



struct FilletSpan {
    CircleFillet a, b;
    float aa, ab;
    float da, db;
    float za, zb;
    ZVec ctr;
    float r;
    /** The three states of the two fillet edges
     * involved here. Separate = no interaction,
     * Blend = overlap somewhat, have to blend,
     * cleave = overlap much, have to use
     * visual effect of separation.
     */
    enum {
	SEPARATE,
	BLEND, 
	CLEAVE
	};
    int type;

    // For cleaved
    float f;
    float fanglea, fangleb, fangle;
    float aang, bang;

    /** 
     * @param ab Angle of fillet b. Always > aa
     */
    FilletSpan(
	    ZVec ctr,
	    float r,
	    float aa,
	    float da,
	    float ta,
	    float za,
	    float ab,
	    float db,
	    float tb,
	    float zb
	    ) { 
	this->aa = aa;
	this->ab = ab;
	this->da = da;
	this->db = db;
	this->za = za;
	this->zb = zb;
	this->ctr = ctr;
	this->r = r;
	this->f = -1;
	a = CircleFillet(ctr, r, aa, 1, da, ta);
	b = CircleFillet(ctr, r, ab, -1, db, tb);

	DBG(dbg_fillets) << "F CF A: "<<a.filletcenter<<" "<<a.filletrad<<" "
		    <<a.dirconn<<" " << a.dirtang<<"\n";

	DBG(dbg_fillets) << "F CF B: "<<b.filletcenter<<" "<<b.filletrad<<" "
		    <<b.dirconn<<" " << b.dirtang<<"\n";

	if(ab-aa < M_PI && 
	    (b.infillet(a.dirconn) ||
	   a.infillet(b.dirconn))) {
	    type = CLEAVE;

	    fanglea = fabs(asin(a.dirconn.cross(a.dirtang)));
	    fangleb = fabs(asin(b.dirconn.cross(b.dirtang)));
	    float mangle = ab - aa;

	    fangle = fanglea >? fangleb;
	    this-> f = - ( mangle - fangle ) / fangle;
	    
	    this->aang = lerp(aa, aa + fangle, .5 + .5*f);
	    this->bang = lerp(ab, ab - fangle, .5 + .5*f);

	    DBG(dbg_fillets) << "F: Cleave: "<<fanglea<<" "<<fangleb<<" "
			<<mangle<<" "<<f<<" "<<aang<<" "<<bang<<"\n";

	} else if(b.infillet(a.dirtang) ||
		  a.infillet(b.dirtang)) {
	    type = BLEND;
	    DBG(dbg_fillets) << "F: Blend: \n";
	} else {
	    type = SEPARATE;
	    DBG(dbg_fillets) << "F: Separate: \n";
	}
    }


    // If calls to point should be split
    bool split() { return type == CLEAVE; }

    template <class Blender> ZVec point(float fract, const Blender &blend, ZVec *intern = 0) {
	if(type == CLEAVE) {
	    if(fract < .5) {
		// Angle of real current sample
		float ang = lerp(aa, aang, 2*fract);
		Vec d = dirVec(ang);
		ZVec p;
		if(a.atEnd(d)) {
		    p = a.end();
		    p.z = za;
		} else {
		    float r0 = this->a.rad(d);

		    // Angles to use for the blended curve when aa-ab==fangle:
		    Vec da = dirVec(aa + fangle/2 * (2*fract));
		    Vec db = dirVec(ab - fangle + fangle/2 * (2*fract));
		    float ra = this->a.rad(da);
		    float rb = this->b.rad(db);
		    float curvfra = blend(ra-r, rb-r) + r;

		    p = ctr + lerp(
			    da * curvfra, 
					d * r0, 
					f);
		    p.z = lerp(ctr.z, za, ((p-ctr).length() - r) / (this->da-r));
		}

		if(intern) {
		    if(fract < .47) {
			*intern = p-ctr;
			intern->z = 0;
			*intern -= intern->dot(a.dirconn.cw90()) * a.dirconn.cw90();
			intern->z = p.z - ctr.z;
			if(intern->length() <= r) *intern = ZVec(0,0,0);
			*intern += ctr; 
		    } else {
			*intern = ctr;
		    }
		}

		//DBG(dbg_fillets) << "f<.5: "<<ang<<" "<<ra<<" "<<rb<<" "<<
		//	    p <<"\n";
		return p;
	    } else {
		fract = 1-fract;

		float ang = lerp(ab, bang, 2*fract);
		Vec d = dirVec(ang);
		ZVec p;

		if(b.atEnd(d)) {
		    p = b.end();
		    p.z = zb;
		} else {
		    float r0 = this->b.rad(d);

		    // Angles to use for the blended curve when aa-ab==fangle:
		    Vec db = dirVec(ab - fangle/2 * (2*fract));
		    Vec da = dirVec(aa + fangle - fangle/2 * (2*fract));
		    float rb = this->b.rad(db);
		    float ra = this->a.rad(da);
		    float curvfra = blend(rb-r, ra-r) + r;

		    p = ctr + lerp(
			    db * curvfra, 
					d * r0, 
					f);
		    p.z = lerp(ctr.z, zb, ((p-ctr).length() - r) / (this->db-r));
		}

		if(intern) {
		    if(fract < .47) {
			*intern = p-ctr;
			intern->z = 0;
			*intern -= intern->dot(b.dirconn.cw90()) * b.dirconn.cw90();
			intern->z = p.z - ctr.z;
			if(intern->length() <= r) *intern = ZVec(0,0,0);
			*intern += ctr; 
		    } else {
			*intern = ctr;
		    }
		}



		return p;

	    }
	} else {
	    // Use angle
	    Vec d = dirVec(lerp(aa, ab, fract));
	    ZVec res;
	    float z, resr;
	    if(a.atEnd(d)) {
		res = a.end();
		z = za;
	    } else if(b.atEnd(d)) {
		res = b.end();
		z = zb;
	    } else {
		float ra = a.rad(d);
		float rb = b.rad(d);
		if(type == SEPARATE) {
		    if(ra > rb) {
			z = lerp(ctr.z, za, (ra - r) / (da-r)) ;
			resr = ra;
		    } else {
			z = lerp(ctr.z, zb, (rb - r) / (db-r)) ;
			resr = rb;
		    }
		} else {
		    resr = blend(ra-r, rb-r) + r;
		    z = blend(ra-r, rb-r, 
			    lerp(ctr.z, za, (ra - r) / (da-r)),
			    lerp(ctr.z, zb, (rb - r) / (db-r))
			    );
		}
		res = ctr + d * resr;
	    }
	    if(intern) {
		if(fract < .47) {
		    *intern = res - ctr;
		    *intern -= intern->dot(a.dirconn.cw90()) * a.dirconn.cw90();
		    intern->z = z - ctr.z;
		    if(intern->length() <= r) *intern = ZVec(0,0,0);
		    *intern += ctr; 
		} else if(fract < .53) {
		    *intern = ctr;
		} else {
		    *intern = res - ctr;
		    *intern -= intern->dot(b.dirconn.cw90()) * b.dirconn.cw90();
		    intern->z = z - ctr.z;
		    if(intern->length() <= r) *intern = ZVec(0,0,0);
		    *intern += ctr;
		}
	    }
	    res.z = z;
	    return res;
	}
    }


};

struct BlendSimply {
    float operator()(float a, float b) const {
	return a + b;
    }
    float operator()(float a, float b, float fa, float fb) const {
	return (a*fa + b*fb) / (a+b + .0001);
    }
};

}
}
