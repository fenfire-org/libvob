/*
Strings.cxx
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

#include <vob/jni/Types.hxx>
#include <vob/jni/Strings.hxx>
#include <callgl.hxx>

namespace Vob {

jstring stdstr2jstr(JNIEnv *env, std::string stdstr) {
  return env->NewStringUTF(stdstr.c_str());
}

std::string jstr2stdstr(JNIEnv *env, jstring jstr) {
  const char *strptr = env->GetStringUTFChars(jstr, 0);
  std::string stdstr(strptr, env->GetStringUTFLength(jstr));
  env->ReleaseStringUTFChars(jstr, strptr);
  return stdstr;
}


GLenum tokenFromJstring(JNIEnv *env, jstring token) {
    DBG(JNI::dbg_convert) << "Converting jstring "<<env<<" "<<token<<"\n";
    std::string str = jstr2stdstr(env, token);
    DBG(JNI::dbg_convert) << "Got std str "<<str<<"\n";
    GLenum ret = CallGL::getTokenValue(str.c_str());
    DBG(JNI::dbg_convert) << "Got val "<<ret<<"\n";
    return ret;
}

/** Converts jstring to unistring
 *
 * Unistring type currently defined at 
 * gzz/gfx/librenderables/Renderables.hxx
 * 
 * @param env the JNI interface pointer
 */
unicodecharvector jstr2unistr(JNIEnv *env, jstring jstr) {
  const jchar *strptr = env->GetStringChars(jstr, 0);
  unicodecharvector unistr(strptr, strptr + env->GetStringLength(jstr));
  env->ReleaseStringChars(jstr, strptr);
  return unistr;
}

}
