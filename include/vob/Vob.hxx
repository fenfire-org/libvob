/*
Vob.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_VOB_HXX
#define VOB_VOB_HXX

#include <vob/Transform.hxx>

/** The main namespace for libvob.
 * All libvob symbols are placed in this namespace.
 */
namespace Vob {

    /** A vob: an object that knows how to render itself,
     * given a number of coordinate transforms.
     */
    class Vob {
    public:
	virtual ~Vob() { }
	/** Get the number of transforms this Vob's render() call
	 * requires.
	 */
	virtual int nCoordsys() const = 0;
	/** Render this Vob, given a number of transforms.
	 * @param t The transforms
	 * @param n The number of transforms in t.
	 */
	virtual void render(const Transform **t, int n) const = 0;

	virtual const char* getVobName() const = 0;
    };

    /** A vob which uses no coordinate systems. A specialization
     * of Vob for efficiency.
     */
    class Vob0 : public Vob {
    public:
	virtual ~Vob0() {}
	virtual int nCoordsys() const { return 0; };
	virtual void render(const Transform **t, int n) const { render0(); }
	virtual void render0() const = 0;
    };


    /** A vob which uses one coordinate system. A specialization
     * of Vob for efficiency.
     */
    class Vob1 : public Vob {
    public:
	virtual ~Vob1() {}
	virtual int nCoordsys() const { return 1; };
	virtual void render(const Transform **t, int n) const { render1(*(t[0])); }
	virtual void render1(const Transform &t) const = 0;
    };

    /** A vob which uses two coordinate systems. A specialization
     * of Vob for efficiency.
     */
    class Vob2 : public Vob {
    public:
	virtual ~Vob2() {}
	virtual int nCoordsys() const { return 2; };
	virtual void render(const Transform **t, int n) const { 
	    render2(*(t[0]), *(t[1])); 
	}
	virtual void render2(const Transform &t0, const Transform &t1) const = 0;
    };

    /** A vob which uses two coordinate systems. A specialization
     * of Vob for efficiency.
     */
    class Vob3 : public Vob {
    public:
	virtual ~Vob3() {}
	virtual int nCoordsys() const { return 3; };
	virtual void render(const Transform **t, int n) const { 
	    render3(*(t[0]), *(t[1]), *(t[2])); 
	}
	virtual void render3(const Transform &t0, 
			const Transform &t1,
			const Transform &t2) const = 0;
    };


    /** A template class which implements Vob, given a class with a render() method.
     * Used by the code generation mechanism to decouple defining Vobs from the
     * actual Vob interface.
     */
    template<class VobSeed> class Vob0Maker : public Vob0, public VobSeed {
	static char* name;
	virtual void render0() const {
	    VobSeed::render();
	}
	virtual const char* getVobName() const { return name; }
    };

    /** A template class which implements Vob, given a class with a render(t) method.
     * Used by the code generation mechanism to decouple defining Vobs from the
     * actual Vob interface.
     */
    template<class VobSeed> class Vob1Maker : public Vob1, public VobSeed {
	static char* name;
	virtual void render1(const Transform &t) const {
	    VobSeed::render(t);
	}
	virtual const char* getVobName() const { return name; }
    };

    /** A template class which implements Vob, given a class with a render(t0, t1) method.
     * Used by the code generation mechanism to decouple defining Vobs from the
     * actual Vob interface.
     */
    template<class VobSeed> class Vob2Maker : public Vob2, public VobSeed {
	static char* name;
	virtual void render2(const Transform &t0, const Transform &t1) const {
	    VobSeed::render(t0, t1);
	}
	virtual const char* getVobName() const { return name; }
    };

    /** A template class which implements Vob, given a class with a render(t0, t1, t2) method.
     * Used by the code generation mechanism to decouple defining Vobs from the
     * actual Vob interface.
     */
    template<class VobSeed> class Vob3Maker : public Vob3, public VobSeed {
	static char* name;
	virtual void render3(const Transform &t0, 
		const Transform &t1,
		const Transform &t2
		) const {
	    VobSeed::render(t0, t1, t2);
	}
	virtual const char* getVobName() const { return name; }
    };

    /** A template class which implements Vob, given a class with a render(t **, n) method.
     * Used by the code generation mechanism to decouple defining Vobs from the
     * actual Vob interface.
     */
    template<class VobSeed> class VobNMaker : public Vob, public VobSeed {
	static char* name;
	virtual int nCoordsys() const {
	    return -1;
	};
	virtual void render(const Transform **t, int n) const {
	    VobSeed::render(t, n);
	}
	virtual const char* getVobName() const { return name; }
    };


}
//@Include: Transform.hxx
//@Include: Primitives.hxx
//@Include: LinearPrimitives.hxx
//@Include: FunctionalPrimitives.hxx
//@Include: DisablablePrimitives.hxx
//
//@Include: util/Perlin.hxx
//@Include: Vec23.hxx


#endif
