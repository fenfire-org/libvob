/*
Generate.hxx
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

/** A companion header file to Define.hxx.
 * This file contains code for generating the Java and JNI
 * code to interface a bunch of Vobs.
 * Prior to including this file, define the Java package
 * name as VOB_JAVAPREFIX.
 * The strstreams to put output in should be in VOB_JNISTREAM and
 * VOB_JAVASTREAM
 * <p>
 * The struct idea: there may be several JNI parameters for
 * each Vob member, but only one struct member: the type should
 * define a struct if it needs more.
 *
 */

#ifndef VOB_JNI_GENERATE_HXX
#define VOB_JNI_GENERATE_HXX

#include <string>
#include <cctype>
#include <iostream>

#include <boost/preprocessor/repetition/enum_binary_params.hpp>
#include <boost/preprocessor/repetition/enum_params.hpp>
#include <boost/preprocessor/repetition/enum_shifted_params.hpp>
#include <boost/preprocessor/iteration/local.hpp>

#include <vob/trans/Primitives.hxx>
#include <vob/Transform.hxx>

#include <vob/Templates.hxx>

#include <vob/jni/Types.hxx>

extern int getTransId() ;

namespace Vob {
namespace JNI {

    template<class T> class JParameter ;

    

    /** Generate and hold the Java codes.
     * Used by JNIGenerator
     */
    struct VobJavaParamList {
	std::string javaParamS;
	std::string javaImplParamCodeS;
	std::string javaImplParamS;
	std::string javaStructS;
	std::string javaStructCodeS;
	std::string jniParamS;
	std::string jniStructS;
	std::string jniStructCodeS;
	int ind;
	VobJavaParamList() : ind(0) { }
	void operator()() {
	}
	template<class T> void doParam(T &t) {
	    static char buf[100];
	    static char buf2[100];
	    std::sprintf(buf, "p%d", ind);
	    std::sprintf(buf2, "j%d", ind);

	    typedef JParameter<T> J;
#define A(n) n##S += J::n(buf);

	    A(javaParam);
	    A(javaImplParamCode);
	    A(javaImplParam);
	    A(javaStruct);
	    A(javaStructCode);
	    jniParamS += ", ";
	    A(jniParam);
	    A(jniStruct);
	    A(jniStructCode);

	    ind ++;
	}
	template<class T> void operator()(T &t) {
	    doParam(t);
	}
#define BOOST_PP_LOCAL_LIMITS (2, 20)
#define BOOST_PP_LOCAL_MACRO(i)                             	\
	template<BOOST_PP_ENUM_PARAMS(i , class T)> 		\
		void operator()(				\
		BOOST_PP_ENUM_BINARY_PARAMS(i , T, &t)) { 	\
		    doParam(t0);				\
		javaParamS += ", ";			\
		javaImplParamCodeS += ", ";			\
		javaImplParamS += ", ";			\
		(*this)(BOOST_PP_ENUM_SHIFTED_PARAMS(		\
			     i, t));				\
	    }
#include BOOST_PP_LOCAL_ITERATE()
    };

    template<class VobSeed> struct VobJNIGenerator {
	string ntrans;

	std::string tclass(std::string name) {
	    return std::string("") + "Vob" + ntrans + "Maker<"+name+">";
	}

	VobJNIGenerator(std::string name) {
	    if(VobSeed::NTrans < 0) 
		ntrans = "N";
	    else {
		static char buf[100];
		sprintf(buf, "%d", VobSeed::NTrans);
		ntrans = buf;
	    }

	    std::cout << "\t" << name << "\n" ;
	    VobJavaParamList pl;
	    VobSeed().params(pl);

	    std::string structName = name + "_vob_thunk_";
	    std::string makerName = 
		std::string(tclass(name))
		;
	    makerName + "<"+name +">";

	    VOB_JAVASTREAM << "static public class "<<name<<
		    " extends GL.Renderable"<<ntrans<<"JavaObject "<<
		    " { private "<<name<<"(int i) { super(i); }\n"
		    <<pl.javaStructS<<
			    "}\n";
		    
	    VOB_JAVASTREAM << "static public "<<name<<" create"<<name<<"(" 
			    << pl.javaParamS << ") { \n"
			    << name <<" _ = new "<<name<<"(\n"
			    << "implcreate"<<name<<"("<<pl.javaImplParamCodeS<<"));\n"
			    << pl.javaStructCodeS<<
		    "\n return _; }\n\n";

	    VOB_JAVASTREAM << "static private native int implcreate"<<name<<"(" 
			    << pl.javaImplParamS << ") ; \n";

	    VOB_JNISTREAM << "\n struct " << structName << "  { "
		<< pl.jniStructS<< "\n} ;\n\n";
	    VOB_JNICXXSTREAM << "\n char *"<<makerName<<"::name = \""<<name<<"\";\n";

	    VOB_JNISTREAM << "JNIEXPORT jint JNICALL "<<
		 VOB_JAVAPREFIX<<"implcreate"<<name<<  
		"(JNIEnv *env_, jclass  " << pl.jniParamS << "\n)";

	    VOB_JNISTREAM << "{ JNI::Vob_thunk_base<" << structName <<"> _;\n" 
		<< pl.jniStructCodeS <<"\n"
		<< makerName <<" *p_ = new " << makerName <<";\n"
		<< "_.env_ = env_; p_->params(_);\n"
		<< "return vob"<<ntrans<<"s.add(p_);\n"
		<<"\n}\n";
	    VOB_JNISTREAM << "\n" ;
	}

	void operator()() {
	}

    };


    template<class Primitive> struct TransJNIGenerator {
	int ind;
	Primitives::PrimitiveHierarchicalTransform<Primitive> hier;

	TransJNIGenerator(std::string className, std::string methodName) {
	    ind = getTransId();
	    VOB_TRANSJAVASTREAM << "public int "<<methodName
			<<"(int d0 ";
	    for(int i=1; i<hier.getNDepends(); i++) {
		VOB_TRANSJAVASTREAM << ", int d"<<i<<" ";
	    }
	    for(int i=0; i<hier.getNParams(); i++) {
		VOB_TRANSJAVASTREAM << ", float p"<<i<<" ";
	    }
	    std::ostringstream  passignCode;
	    for(int i=0; i<hier.getNParams(); i++) {
		passignCode << "floats[i+"<<i
			<<"] = p"<<i<<";\n";
	    }
	    VOB_TRANSJAVASTREAM << ") {\n"
		    "int i = nfloats; \n" <<
		    "addFloats("<<hier.getNParams()<<");\n";

	    VOB_TRANSJAVASTREAM << passignCode.str();

	    VOB_TRANSJAVASTREAM <<
		    "int j = ninds; addInds("<<
			(hier.getNDepends()+2)
			<<"); inds[j+0] = "<<ind<<";\n";

	    for(int i=0; i<hier.getNDepends(); i++) {
		VOB_TRANSJAVASTREAM << 
		    "inds[j+"<<(i+1)<<"] = d"<<i<<";\n";
	    }
	    VOB_TRANSJAVASTREAM <<
		    "inds[j+"<<(hier.getNDepends()+1)<<
			"] = i;\n return j;";

	    VOB_TRANSJAVASTREAM << "}\n";

	    VOB_JNICXXSTREAM 
		    << "const char *PrimitiveHierarchicalTransform<"
			<<className<<">::name = \""<<className<<"\";\n\n ";
	    VOB_JNISTREAM << "static HierFact<"<<className<<"> "
		<< className<<"__fac("<<ind<<");\n";


	    if(hier.getNParams() > 0) {
		std::string n = methodName;
		n[0] = std::toupper(n[0]);
		VOB_TRANSJAVASTREAM << "public void set"<<n
			<<"Params(int ind";
		for(int i=0; i<hier.getNParams(); i++)
		    VOB_TRANSJAVASTREAM << ", float p"<<i;
		VOB_TRANSJAVASTREAM<<") {\n";
		VOB_TRANSJAVASTREAM<<"int i = inds[ind+"<<
			(hier.getNDepends()+1)<<"];\n";
		VOB_TRANSJAVASTREAM << "updateCoords(i);\n";
		VOB_TRANSJAVASTREAM << passignCode.str() << "}\n";
	    }
	}
    };

}
}


#define VOB_DEFINED(x)  \
static Templates::IfTempl<!VOB_NON_LEAF, JNI::VobJNIGenerator<x> > x##generator(#x);


#define VOB_PRIMITIVETRANS_DEFINED_LEAF(x, n)  \
    static JNI::TransJNIGenerator<x> x##generator(#x, n); \
    const char *Primitives::PrimitiveHierarchicalTransform<x>::name = #x

#define VOB_PRIMITIVETRANS_DEFINED_NONLEAF(x, n)  

#define VOB_PRIMITIVETRANS_DEFINED(x, n)  VOB_PRIMITIVETRANS_DEFINED_LEAF(x, n)

#ifndef VOB_NON_LEAF
#define VOB_NON_LEAF 0
#endif

#endif
