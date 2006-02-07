/*
Types.hxx
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
#ifndef VOB_JNI_TYPES_HXX
#define VOB_JNI_TYPES_HXX

// Workaround for error in kaffe's c++
// include file, Debian pkg kaffe 1.1.1-5
#define CheckException ExceptionCheck

#include <jni.h>


#include <vob/Vob.hxx>
#include <vob/Types.hxx>
#include <vob/util/ObjectStorer.hxx>
#include <vob/Debug.hxx>

#include <vob/jni/Stats.hxx>
#include <vob/jni/Strings.hxx>
#include <vob/jni/Arrays.hxx>



namespace Vob {
    // Predeclarations, to avoid including
    // too many files here, since this file is
    // included *everywhere*.
    namespace Os {
	class RenderingSurface;
    }
    namespace Paper {
	class Paper;
    }
    namespace Primitives {
    }
    namespace Vobs {
    }
    namespace ImageLoader {
	class RGBARaster;
    }
    namespace Stats {
	struct TexAccum;
    }
    namespace JNI {
	struct TexAccum_JNI;
    }
    namespace Text {
	struct QuadFont;
    }

namespace JNI {
    PREDBGVAR(dbg);
    PREDBGVAR(dbg_convert);

    /** The JNI parameter template class.
     *
     *
     * javaParam, javaImplParamCode, javaImplParam,
     * javaStruct, javaStructCode
     *
     * jniParam, jniStruct, jniStructCode, 
     */
    template<class T> class JParameter ;

    typedef std::vector<GLubyte> ByteVector;

    extern ObjectStorer<Vob0> vob0s;
    extern ObjectStorer<Vob1> vob1s;
    extern ObjectStorer<Vob2> vob2s;
    extern ObjectStorer<Vob3> vob3s;
    extern ObjectStorer<Vob> vobNs;
    extern ObjectStorer<Os::RenderingSurface> windows;
    extern ObjectStorer<ByteVector> bytevectors;

    extern ObjectStorer<ImageLoader::RGBARaster> images;

    extern ObjectStorer<Text::QuadFont> quadFonts;

    // Only TexAccum_JNI objects will be stored here
    extern ObjectStorer<Stats::TexAccum> texaccums;

    typedef ::Vob::Paper::Paper P; // g++3.2 doesn't like ::... inside
				   // template param
    extern ObjectStorer<P> papers;


    typedef std::vector<float> floatvector;

#define START_VOB_JNI_CONVERSION(type, javaName_, jnitype_) 	\
    template<> struct JParameter<type> {					\
	typedef jnitype_ jniType; 				\
	static std::string javaParam(std::string paramPrefix) {  	\
	    return std::string(javaName_)+" "+paramPrefix;  	\
	}							\
	static std::string javaStruct(std::string paramPrefix) { \
	    return "";						\
	}							\
	static std::string javaStructCode(std::string paramPrefix) {	\
	    return "";							\
	}								\
	static std::string javaImplParamCode(std::string paramPrefix) {  	\
	    return paramPrefix;  				\
	}							\
	static std::string javaImplParam(std::string paramPrefix) {  	\
	    return std::string(javaName_)+" "+paramPrefix;  	\
	}							\
	static std::string jniParam(std::string paramPrefix) {  	\
	    return std::string(#jnitype_)+" "+paramPrefix;  	\
	}							\
	static std::string jniStruct(std::string paramPrefix) {  	\
	    return jniParam(paramPrefix)+";\n";			\
	}							\
	static std::string jniStructCode(std::string paramPrefix) {  	\
	    return "_."+paramPrefix+"="+paramPrefix+";\n";			\
	}							\
	static void convert(JNIEnv *env, jnitype_ &in, type &out) {

#define END_VOB_JNI_CONVERSION 			\
	    }};

#define VOB_JNI_CONVERSION_ASSIGN(type, javaName, jnitype) 	\
	    START_VOB_JNI_CONVERSION(type, javaName, jnitype)   \
		out = in;					\
	    END_VOB_JNI_CONVERSION

#define START_VOB_JNI_CONVERSION_IDDED(type, javaName_)		\
    template<> struct JParameter<type> {					\
	typedef jint jniType; 				\
	static std::string javaParam(std::string paramPrefix) {  	\
	    return std::string(javaName_)+" "+paramPrefix;  	\
	}							\
	static std::string javaStruct(std::string paramPrefix) { \
	    return javaParam(paramPrefix)+";\n";			\
	}							\
	static std::string javaStructCode(std::string paramPrefix) {	\
	    return "_."+paramPrefix+" = "+paramPrefix+";\n";	\
	}								\
	static std::string javaImplParamCode(std::string paramPrefix) {  	\
	    return "("+paramPrefix+" == null ? 0 : "+paramPrefix+".getId())";  				\
	}							\
	static std::string javaImplParam(std::string paramPrefix) {  	\
	    return "int "+paramPrefix;  	\
	}							\
	static std::string jniParam(std::string paramPrefix) {  	\
	    return "jint "+paramPrefix;  	\
	}							\
	static std::string jniStruct(std::string paramPrefix) {  	\
	    return jniParam(paramPrefix)+";\n";			\
	}							\
	static std::string jniStructCode(std::string paramPrefix) {  	\
	    return "_."+paramPrefix+"="+paramPrefix+";\n";			\
	}							\
	static void convert(JNIEnv *env, jint &in, type &out) {		

    /* 
     * Below, you see several different ways of converting
     * Java primitives and objects to C++ ones.
     *
     * Some important points: for most objects, we make a copy on the C++
     * side which has to get deleted when the struct it is in is deleted
     * so we shall use types like vector<float> &c.
     *
     * However, there are several objects which we do not want to
     * copy but want references or pointers to, like DefaultTextRendered
     * or Vob::Paper::Paper. These have corresponding Java objects on 
     * the Java side and are handled by integer ids to the objectstorer.
     * Say we have a Foo which uses a Bar.
     * The code will 
     * 1) make the corresponding Foo *java* object contain a reference to the
     *    Bar java object 
     * 2) Pass the integer id of the Bar object for the objectstorer
     * 3) the id will be used (inside START_VOB_JNI_CONVERSION_IDDED below)
     *    to get the object from the objectstorer.
     * Obviously, these must never be deleted.
     *
     * ByteVectors are intended for *large* arrays of data (screen
     * captures &c) so copies would be "not so nice", so we use idded objects
     * for them, too.
     */

    VOB_JNI_CONVERSION_ASSIGN(float, "float", jfloat)
    VOB_JNI_CONVERSION_ASSIGN(int, "int", jint)
    VOB_JNI_CONVERSION_ASSIGN(bool, "boolean", jboolean)

    START_VOB_JNI_CONVERSION(Token, "String", jstring)
	DBG(dbg_convert) << "Converting token "<<in<<"\n";
	out = tokenFromJstring(env, in);
	DBG(dbg_convert) << "Converted token "<<out<<"\n";
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION(unicodecharvector, "String", jstring)
	out = jstr2unistr(env, in);
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION(std::string, "String", jstring)
	out = jstr2stdstr(env, in);
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION(floatvector, "float []", jfloatArray) 
      jfloats2floatvector(env, in, out);
    END_VOB_JNI_CONVERSION


    START_VOB_JNI_CONVERSION_IDDED(GLubyte *, "GL.ByteVector")
	DBG(dbg_convert) << "Converting bytevec "<<in<<"\n";
	ByteVector *bv = bytevectors[in];
	DBG(dbg_convert) << "Got bytevec "<<bv<<"\n";
	out = &((*bv)[0]);
	DBG(dbg_convert) << "Got ptr "<<(int)out<<"\n";
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(DisplayListID, "GL.DisplayList")
	    out = in;
    END_VOB_JNI_CONVERSION


    START_VOB_JNI_CONVERSION_IDDED(::Vob::Text::QuadFont *, 
		"GL.QuadFont")
	out = quadFonts[in];
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(::Vob::Paper::Paper *, 
		"Paper")
	out = papers[in];
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(::Vob::Vob3 *, 
		"GL.Renderable3JavaObject")
	out = vob3s[in];
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(::Vob::Vob2 *, 
		"GL.Renderable2JavaObject")
	out = vob2s[in];
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(::Vob::Vob1 *, 
		"GL.Renderable1JavaObject")
	out = vob1s[in];
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(::Vob::Vob0 *, 
		"GL.Renderable0JavaObject")
	out = vob0s[in];
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(::Vob::Vob *, 
		"GL.RenderableNJavaObject")
	out = vobNs[in];
    END_VOB_JNI_CONVERSION

    START_VOB_JNI_CONVERSION_IDDED(Stats::TexAccum *, 
		"GL.TexAccum")
	out = texaccums.get_allowNull(in);
    END_VOB_JNI_CONVERSION

}
}

#endif
