/*
IndirectTexture.cxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    
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
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */


#include <iostream>


#include <vob/Debug.hxx>
#include <vob/glerr.hxx>
#include <vob/util/ObjectStorer.hxx>
#include <vob/jni/Types.hxx>


#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"
#include <vob/jni/Strings.hxx>

#include <vob/IndirectTexture.hxx>


namespace Vob {
namespace JNI {
    DBGVAR(dbg_indirect, "IndirectTexture");
    ObjectStorer<IndirectTexture> indirectTextures("indirectTexs");

extern "C" {
    
jf(jint, impl_1IndirectTexture_1create)
    (JNIEnv *, jclass) 
{
    return indirectTextures.add(new IndirectTexture());
}

jf(void, impl_1IndirectTexture_1delete)
    (JNIEnv *, jclass, jint id) 
{
    indirectTextures.remove(id);
}

jf(void, impl_1IndirectTexture_1setTexture)
    (JNIEnv *, jclass, jint id, jint texid) 
{
    IndirectTexture *indirectTexture = indirectTextures.get(id);
    indirectTexture->texId = texid;
}

}

}
}
