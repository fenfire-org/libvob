/*
ObjectStorer.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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

#ifndef VOB_UTIL_OBJECTSTORER_HXX
#define VOB_UTIL_OBJECTSTORER_HXX

#include <vector>

namespace Vob {

    using std::vector;
    using std::cout;
    using std::cerr;
    using std::string;

    /** A template for storing (owning pointers to) objects by integer ids.
     */
    template<class T> class ObjectStorer {
	vector<T *> vec;
	string name;
    public:
	ObjectStorer(string name="") : name(name) {
	    // to avoid '0' as id
	    vec.insert(vec.end(), 0);
	}
	/** Add a new object, get back its assigned id.
	 * Side effect: all object marked for removal
	 * since the last add() are really deleted.
	 * The ObjectStorer takes ownership of the pointer
	 * and will eventually delete() it.
	 */
	int add(T *p) {
	    if(p == 0) {
		return 0; // invalid value
	    }

	    for(int i=vec.size()-1; i> 0; i--) {
		if(vec[i] == 0) {
		    vec[i] = p;
		    return i;
		}
	    }

	    int i = vec.size();
	    vec.insert(vec.end(), p);
	    return i;
	}
	/** Mark the object with the given id for removal.
	 */
	void remove(int p) {
	    if(p == 0) {
		cerr << name<<": Trying to delete element 0\n";
		return;
	    }
	    if((unsigned)p >= vec.size()) {
		cerr << name<<": Trying to delete element past end "<<p<<"\n";
		return;
	    }
	    if(vec[p] == NULL) {
		cerr << name<<": Trying to delete null element! "<<p<<"\n";
		return;
	    }
	    delete vec[p];
	    vec[p] = 0;
	}
	
	/** Get the pointer corresponding to the given id.
	 */
	T *get(int p) {
	    if(p <= 0 || (unsigned)p >= vec.size()) {
		cerr << name<<
		    ": Trying to get element past end "<<p<<"\n";
		return 0;
	    }
	    if(vec[p] == NULL) {
		cerr << name<<": Trying to get null element!\n";
		return 0;
	    }
	    return vec[p];
	}
	
	/** Get the pointer corresponding to the given id.
	 */
	T *get_allowNull(int p) {
	    if(p == 0) return 0;
	    if(p < 0 || (unsigned)p >= vec.size()) {
		cerr << name<<
		    ": Trying to get element past end "<<p<<"\n";
		return 0;
	    }
	    return vec[p];
	}


	/** Alias to get().
	 */
	T *operator[](int p) { return get(p); }

    };

}

#endif
