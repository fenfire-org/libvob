/*
Strings.hxx
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

#include <jni.h>
#include <string>
#include <GL/gl.h>
#include <vob/Types.hxx>

namespace Vob {

/** Converts std::string to jstring through UTF-8 
 * transformation 
 */
jstring stdstr2jstr(JNIEnv *env, std::string stdstr) ;

/** Converts jstring to std::string through UTF-8 
 * transformation 
 */
std::string jstr2stdstr(JNIEnv *env, jstring jstr) ;

/** Converts a jstring to a GL token.
 */
GLenum tokenFromJstring(JNIEnv *env, jstring token) ;

unicodecharvector jstr2unistr(JNIEnv *env, jstring jstr) ;

}
