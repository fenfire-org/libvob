/*
Coorder.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka and Asko Soukka
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
 * Written by Tuomas J. Lukka and Asko Soukka
 */

#ifndef VOB_COORDER_HXX
#define VOB_COORDER_HXX

#include <vector>
#include <map>
#include <vob/Transform.hxx>
#include <vob/ChildVS.hxx>
#include <vob/util/ObjectStorer.hxx>
#include <vob/trans/Primitives.hxx>

namespace Vob {

enum {
    CSFLAG_ACTIVE = 0x80000000,
    CSFLAGS = 0xf0000000
};



typedef Primitives::HierarchicalTransform *(*TransformFactory)(int i);

/** A class that manages a set of coordinate systems.
 */
class Coorder {
    /** The number of initial coordinate systems
     * that were given to us from outside, that we MUSTN'T delete.
     */
    int ninitCS;
    std::vector<Transform *> cs;
    std::vector<float> params;

    int maxcs;

    TransformFactory transformFactory;
    ObjectStorer<ChildVS> *childVsStorer;

    Coorder *cs1_tmp ;
    Coorder *cs2_tmp ;

    std::map<int, Coorder *> childCoorders;

    // Not to be ever trusted except inside setPoints()
    // calls: used to make it possible to use submethods.
    int ninds;
    int *inds1; float *points1; 
    int *interpinds; 
    int *inds2; float *points2; 
    bool shouldInterpolate(int cs1, int cs2, int nprev);


public:
    Coorder(TransformFactory fac, ObjectStorer<ChildVS> *chlidVsStorer);
    ~Coorder();
    void clean();
    Coorder * getChildCoorder(int cs) { return childCoorders[cs]; }
    class iterator {
	int ind;
	Coorder *parent;

	void incr() {
	    ind++;
	    while(parent->get(ind) == 0 && ind < parent->maxcs)
		ind++;
	}
    public:
	iterator(int ind, Coorder *parent) : ind(ind), parent(parent) { }
	int operator*() { 
	    return ind;
	}
	iterator& operator++() { 
	    incr();
	    return *this;
	}
	iterator operator++(int) { 
	    iterator tmp = *this;
	    incr();
	    return tmp;
	}
	bool operator==(const iterator &it) const { return ind == it.ind; }
	bool operator!=(const iterator &it) const { return ind != it.ind; }

    };

    /** Return an iterator pointing to the first coordinate system 
     * (not root, i.e. not 0).
     */
    iterator begin();
    /** Return an iterator pointing one past the last coordinate system.
     */
    iterator end();
    /** Set the coordinate systems.
     * @param ninitCS Number of initial coordsyses given by initCS array.
     * @param points1 floats: the parameters of the coordinate
     * 		systems.
     * @param inds1 Array of length ninds, 
     * 	containing descriptions of coordinate systems.
     * 	1) type code, see Coords.cxx for current codes
     * 	2) parent and determining coordinate systems,
     * 	   as many as the type code requires (usually 1 or 2)
     * 	3) an index to the points1 array, where the parameters
     * 	   of the coordinate system should be read from.
     * @param inds2 A second array, similar to inds1, but for points2.
     */
    void Coorder::setPoints( int ninitCS, Transform **initCS,
			     int ninds, 
			     int *inds1, float *points1, 
			     int *interpinds, 
			     int *inds2, float *points2, 
			     float fract, bool show1) ;

    int size() { return cs.size(); }
    Transform *get(int i) {
	if(i < 0) return 0;
	if((unsigned)i >= cs.size()) return 0;
	return cs[i];
    }

};

}

#endif
