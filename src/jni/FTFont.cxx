/*
FTFont.cxx
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

#include <ft2build.h>
#include FT_FREETYPE_H

#include <vob/Debug.hxx>
#include <vob/util/ObjectStorer.hxx>
#include <vob/jni/Types.hxx>


#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"
#include <vob/jni/Strings.hxx>

static FT_Library library;
static bool inited = false;
static int error;

namespace Vob {
namespace JNI {
ObjectStorer<FT_Face> ftfonts("ftfonts");

DBGVAR(dbg_ftfont, "JNI.ftfont");

static void gotError(JNIEnv *env, int error) {
    cerr << "Font error: "<<error<<"\n";
    jclass errclass = env->FindClass("java/lang/Error");
    env->ThrowNew(errclass, "Font error!");
}

extern "C" {

jf( jint, impl_1FTFont_1create )
    (JNIEnv *env, jclass, jstring filename, jint pixsize_x, jint pixsize_y,
	jint xx, jint xy, jint yx, jint yy)
{
    std::string filename_str = jstr2stdstr(env, filename);
    if(!inited) {
	DBG(dbg_ftfont) << "Initializing Freetype\n";
	if((error = FT_Init_FreeType(&library))) {
	    gotError(env, error);
	    return -1;
	}
	inited = 1;
    }

    FT_Face *face = new FT_Face();
    if((error = FT_New_Face(library, 
			filename_str.c_str(), 0, face))) {
	gotError(env, error);
	return -1;
    }
    if((error = FT_Set_Pixel_Sizes(*face, pixsize_x, pixsize_y))) {
	gotError(env, error);
	FT_Done_Face(*face);
	
	return -1;
    }

    FT_Matrix transform;
    transform.xx = xx;
    transform.xy = xy;
    transform.yx = yx;
    transform.yy = yy;
    
    FT_Set_Transform(*face, &transform, 0);

    return ftfonts.add(face);
}

jf( void, impl_1FTFont_1delete )
    (JNIEnv *env, jclass, jint id)
{
    FT_Face *face = ftfonts.get(id);
    FT_Done_Face(*face);
    ftfonts.remove(id);

}

jf( jint, impl_1FTFont_1getHeight )
    (JNIEnv *env, jclass, jint id)
{
    FT_Face *face = ftfonts.get(id);
    return (*face)->size->metrics.height;
}

jf( jint, impl_1FTFont_1getYOffs )
    (JNIEnv *env, jclass, jint id)
{
    FT_Face *face = ftfonts.get(id);
    return (*face)->size->metrics.ascender;
}

jf( jintArray, impl_1FTFont_1getMeasurements )
  (JNIEnv *env, jclass, jint id, jintArray charsArray) {
    FT_Face *face = ftfonts.get(id);
    vector<int> chars;
    jints2intvector(env, charsArray, chars);

    vector<int> meas(6 * chars.size());

    for(unsigned i=0; i<chars.size(); i++) {
	int mapped = FT_Get_Char_Index(*face, chars[i]);
	if(mapped == 0) {
	    meas[6*i + 2] =  0;
	    meas[6*i + 3] =  0;
	    continue;
	}
	FT_Load_Glyph(*face, mapped, FT_LOAD_RENDER);
	FT_GlyphSlot slot = (*face)->glyph;
	meas[6*i + 0] =  slot->bitmap_left;
	meas[6*i + 1] = -slot->bitmap_top;
	meas[6*i + 2] =  slot->bitmap.width;
	meas[6*i + 3] =  slot->bitmap.rows;
	meas[6*i + 4] =  slot->advance.x;
	meas[6*i + 5] =  slot->advance.y;
    }

    return intvector2jintArray(env, meas);
}

jf( jobjectArray, impl_1FTFont_1getBitmaps )
  (JNIEnv *env, jclass, jint id, jintArray charsArray) {
    FT_Face *face = ftfonts.get(id);
    vector<int> chars;
    jints2intvector(env, charsArray, chars);

    jclass elementclass = env->FindClass("[B");

    jobjectArray bitmaps = env->NewObjectArray(
		    chars.size(),
		    elementclass,
		    NULL);
    for(unsigned i=0; i<chars.size(); i++) {
	int mapped = FT_Get_Char_Index(*face, chars[i]);
	if(mapped == 0) {
	    continue;
	}
	FT_Load_Glyph(*face, mapped, FT_LOAD_RENDER);
	FT_GlyphSlot slot = (*face)->glyph;
	int size = slot->bitmap.width * slot->bitmap.rows;
	jbyteArray bytes = env->NewByteArray(size);
	env->SetByteArrayRegion(bytes, 0, size, (jbyte *)slot->bitmap.buffer);
	env->SetObjectArrayElement(bitmaps, i, bytes);
    }
    return bitmaps;
}


}


}
}
