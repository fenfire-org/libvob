/*
Define.hxx
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

#ifndef VOB_JNI_DEFINE_HXX
#define VOB_JNI_DEFINE_HXX

#include <boost/preprocessor/repetition/enum.hpp>
#include <boost/preprocessor/repetition/enum_binary_params.hpp>
#include <boost/preprocessor/repetition/enum_params.hpp>
#include <boost/preprocessor/repetition/enum_shifted_params.hpp>
#include <boost/preprocessor/iteration/local.hpp>
#include <string>
#include <iostream>

#include <jni.h>

#include <vob/Vob.hxx>
#include <vob/trans/Primitives.hxx>

namespace Vob {
    using Primitives::HierarchicalTransform;
namespace JNI {

    template<class T> class JParameter ;

    template<class thunk> struct Vob_thunk_base : public thunk {
	JNIEnv *env_;

	template<class T> void doParam(
		T &t, typename JParameter<T>::jniType &p) {
	    JParameter<T>::convert(env_, p, t);
	}

	void operator()() {
	}

#define VOB_TEXT(z, i, ignored) doParam(t##i, p##i)

#define BOOST_PP_LOCAL_LIMITS (1, 20)
#define BOOST_PP_LOCAL_MACRO(i)                             	\
	template<BOOST_PP_ENUM_PARAMS(i , class T)> 		\
		void operator()(				\
		BOOST_PP_ENUM_BINARY_PARAMS(i , T, &t)) { 	\
		    BOOST_PP_ENUM(i, VOB_TEXT, ignored); }

#include BOOST_PP_LOCAL_ITERATE()

#undef VOB_TEXT

    };


    typedef HierarchicalTransform *(*HierarchicalTransformFactory)();

    template<class T> HierarchicalTransform *HierFactImpl() {
	return new Primitives::PrimitiveHierarchicalTransform<T>();
    }

    template<class T> class HierFact {
    public:
	HierFact(int id) {
	    transFactories[id] = HierFactImpl<T>;
	}
    };


}
}


#endif
