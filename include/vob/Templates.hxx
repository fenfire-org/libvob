/*
Templates.hxx
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

#ifndef VOB_TEMPLATES_HXX
#define VOB_TEMPLATES_HXX

namespace Vob {
/** Generic template utilities.
 */
namespace Templates {
    /** Borrowed from Alexandrescu's Modern C++ Design.
     * A distinct type for each integer.
     */
    template <int v> struct Int2Type {
	enum { value = v } ;
    };

    /** A class which is either empty or contains one
     * instance of the other class, depending on the boolean.
     * Used in code :generation.
     */
    template <bool really, class C> class IfTempl {
    };

    template <class C> struct IfTempl<true, C> {
	C c;
	template<class T> IfTempl(T t) : c(t) { }
	template<class T, class U> IfTempl(T t, U u) : c(t, u) { }
    };
    template <class C> struct IfTempl<false, C> {
	template<class T> IfTempl(T t) { }
	template<class T, class U> IfTempl(T t, U u) { }
    };
}
}

#endif
