/*
Dicer.hxx
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

#ifndef VOB_POLY_DICER_HXX
#define VOB_POLY_DICER_HXX

#include <GL/gl.h>
#include <algorithm>
#include <set>
#include <map>
#include <vector>
#include <list>
#include <ext/slist>

namespace Vob {
namespace Dicer {

    PREDBGVAR(dbg);

    template<class Verts> struct Triangles {
	Verts &verts;
	struct Tri { 
	    int v[3];
	    typename std::list<Tri>::iterator n[3];
	};
	std::list<Tri> tris;

	typedef typename std::list<Tri>::iterator Titer;
	vector<Titer> vertFirst;

	void remove(Titer x) {
	    Tri &t = *x;
	    DBG(dbg) << "Triangler: remove "<<t.v[0]<<" "<<t.v[1]<<" "<<t.v[2]<<"\n";
	    // Remove from the edge list
	    for(int i=0; i<3; i++) {
		Titer v = vertFirst[t.v[i]];
		DBG(dbg) << "Titer: "<<(*v).v[0]<<" "<<(*v).v[1]<<" "<<(*v).v[2]<<"\n";
		if(v == x) {
		    DBG(dbg) << "Was first\n";
		    vertFirst[t.v[i]] = t.n[i];
		} else {
		    Titer prev = v;
		    int vind = 0;
		    if((*v).v[0] == t.v[i]) vind = 0;
		    if((*v).v[1] == t.v[i]) vind = 1;
		    if((*v).v[2] == t.v[i]) vind = 2;
		    v = (*v).n[vind];
		    while(v != tris.end()) {
			DBG(dbg) << "Titer: "<<(*v).v[0]<<" "<<(*v).v[1]<<" "<<(*v).v[2]<<"\n";
			if(v == x) {
			    (*prev).n[vind] = t.n[i];
			    break;
			}
			if((*v).v[0] == t.v[i]) vind = 0;
			if((*v).v[1] == t.v[i]) vind = 1;
			if((*v).v[2] == t.v[i]) vind = 2;
			prev = v;
			v = (*v).n[vind];
		    }
		}
	    }
	    DBG(dbg) << "Remove: realerase\n";
	    tris.erase(x);
	}

	void splitEdge(int v1, int v2) {
	    DBG(dbg) << "Triangler: split "<<v1<<" "<<v2<<"\n";
	    vector<Titer> toremove;
	    for(Titer x = vertFirst[v1]; x != tris.end(); ) {
		int vind = 0;
		if((*x).v[0] == v1) vind = 0;
		if((*x).v[1] == v1) vind = 1;
		if((*x).v[2] == v1) vind = 2;
		if((*x).v[0] == v2 ||
		   (*x).v[1] == v2 ||
		   (*x).v[2] == v2) toremove.push_back(x);
		x = (*x).n[vind];
	    }
	    if(toremove.size() == 0) return;
	    int nvert = verts(v1, v2, .5);
	    DBG(dbg) << "Have new vert "<<nvert<<"\n";
	    for(unsigned i=0; i<toremove.size(); i++) {
		// Save to local
		Tri t = *toremove[i];
		remove(toremove[i]);
		int i = (t.v[0] == v1 ? nvert : t.v[0]);
		int j = (t.v[1] == v1 ? nvert : t.v[1]);
		int k = (t.v[2] == v1 ? nvert : t.v[2]);
		add(i, j, k);
		i = (t.v[0] == v2 ? nvert : t.v[0]);
		j = (t.v[1] == v2 ? nvert : t.v[1]);
		k = (t.v[2] == v2 ? nvert : t.v[2]);
		add(i, j, k);
	    }
	    DBG(dbg) << "Split finished\n";
	}

    public:
	void add(int i, int j, int k) {
	    DBG(dbg) << "Triangler: add "<<i<<" "<<j<<" "<<k<<"\n";
	    Tri t;
	    t.v[0] = i;
	    t.v[1] = j;
	    t.v[2] = k;
	    if(vertFirst.size() != verts.size())
		vertFirst.resize(verts.size(), tris.end());
	    t.n[0] = vertFirst[i];
	    t.n[1] = vertFirst[j];
	    t.n[2] = vertFirst[k];
	    vertFirst[i] = vertFirst[j] = vertFirst[k] = 
		tris.insert(tris.begin(), t);
	}

	template <class F> bool diceRound(F criterion) {
	    typedef std::pair<int, int> Edge;
	    vector<Edge> tosplit;
	    DBG(dbg) << "Triangler: dice round\n";
	    for(Titer t = tris.begin(); t != tris.end(); t++) {
		Tri &tri = *t;
		int spl = criterion(tri.v[0], tri.v[1], tri.v[2]);
		if(spl >= 0) {
		    tosplit.push_back( Edge( tri.v[spl], tri.v[(spl+1) % 3]));
		}
	    }
	    if(!tosplit.size()) return false;
	    for(unsigned i=0; i<tosplit.size(); i++)
		splitEdge(tosplit[i].first, tosplit[i].second);
	    return true;
	}

	template <class F> void dice(F criterion) {
	    DBG(dbg) << "Triangler: dice\n";
	    int round = 0;
	    while(1) {
		if(!diceRound(criterion)) return;
		round ++;
		if(round > 20) {
		    DBG(dbg) << "OVER ROUND LIMIT! ABORTING!\n";
		    return;
		}
	    }
	}
	template <class F> void iterateTriangles(F func) {
	    DBG(dbg) << "IterateTriangles: start\n";
	    for(Titer x = tris.begin(); x != tris.end(); x++) {
		DBG(dbg) << "IterateTriangles: tri " << 
			(*x).v[0] <<" "<<
			(*x).v[1] <<" "<<
			(*x).v[2] << "\n";
		func( (*x).v[0], (*x).v[1], (*x).v[2]);
	    }
	}
	void draw() {
	    glBegin(GL_TRIANGLES);
	    for(Titer x = tris.begin(); x != tris.end(); x++) {
		glArrayElement((*x).v[0]);
		glArrayElement((*x).v[1]);
		glArrayElement((*x).v[2]);
	    }
	    glEnd();
	}
	Triangles(Verts &v) : verts(v) {
	}

    };


}
}


#endif
