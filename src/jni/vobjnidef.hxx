/*
vobjnidef.hxx
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

/** Define a jni function in the vob GL class.
 */
#define jf(t, rootname) JNIEXPORT t JNICALL Java_org_nongnu_libvob_gl_GL_##rootname

#include <vob/os/Os.hxx>
#include <vob/util/ObjectStorer.hxx>

namespace Vob {
namespace JNI {

extern void setWindow(int wid = -1) ;
extern void releaseWindow() ;
extern ObjectStorer<Os::RenderingSurface> windows;

/** If an exception occurred, print a debug message for it and clear it.
*/
void javaExc(JNIEnv *env, const char *where) ;

/** Throw a Java Error with the given string.
 */
void throwJavaError(JNIEnv *env, char *err) ;

/** If an OpenGL exception has occurred, throw it to Java and return true.
 */
bool GLERR_JNI_impl(JNIEnv *env, const char *file, int line) ;

#define GLERR_JNI(env) GLERR_JNI_impl(env, __FILE__, __LINE__)

}
}
