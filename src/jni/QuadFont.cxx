/*
QuadFont.cxx
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
#include <vob/jni/Strings.hxx>

#include <vob/text/QuadFont.hxx>

#include "vobjnidef.hxx"

namespace Vob {
namespace JNI {
    DBGVAR(dbg_quadfont, "QuadFont");
    typedef ::Vob::Text::QuadFont QF;
ObjectStorer<QF> quadFonts("quadfonts");

void throwJavaError(JNIEnv *env, char *err) {
    jclass errclass = env->FindClass("java/lang/Error");
    env->ThrowNew(errclass, err);
}

extern "C" {
    
jf(jint, impl_1QuadFont_1create)
    (JNIEnv *, jclass) 
{
    return quadFonts.add(new Text::QuadFont());
}

jf(void, impl_1QuadFont_1delete)
    (JNIEnv *, jclass, jint id) 
{
    quadFonts.remove(id);
}


jf(void, impl_1QuadFont_1setTextures)
    (JNIEnv *env, jclass, jint id,
	    jobjectArray texUnits, 
	    jobjectArray targets,
	    jobjectArray texCoordUnits,
	    jintArray textures) 
{
    Text::QuadFont *quadFont = quadFonts.get(id);

    int nTexUnits = env->GetArrayLength(texUnits);
    int nTargets = env->GetArrayLength(targets);
    int nTextures = env->GetArrayLength(textures);

    if(nTargets != nTexUnits) {
	throwJavaError(env, "Targets != texunits");
	return;
    }
    if(nTargets != 0 && nTextures % nTargets != 0) {
	throwJavaError(env, "Targets ! divides textures");
	return;
    }

    int nTexCoords = env->GetArrayLength(texCoordUnits);

    // Set array lengths
    quadFont->setNTextureLayers(nTargets, nTargets == 0 ? 0 : nTextures / nTargets);
    quadFont->setNTextureCoords(nTexCoords);

    // Set the values
    for(int i=0; i<nTargets; i++) {
	quadFont->textureUnits[i] =
	    tokenFromJstring(env,
		    (jstring)env->GetObjectArrayElement(
					texUnits, i));
	quadFont->textureTargets[i] =
	    tokenFromJstring(env,
		    (jstring)env->GetObjectArrayElement(
					targets, i));

	DBG(dbg_quadfont) << "textarg: "<<
	    quadFont->textureUnits[i] <<" "<<
	    quadFont->textureTargets[i]<<"\n";
    }
    for(int i=0; i<nTexCoords; i++) {
	quadFont->coordTextureUnits[i] =
	    tokenFromJstring(env,
		    (jstring)env->GetObjectArrayElement(
					texCoordUnits, i));
    }
    vector<int> tex;
    jints2intvector(env, textures, tex);
    std::copy(tex.begin(), tex.end(), quadFont->textures.begin());

}

jf(void, impl_1QuadFont_1setNGlyphs)
    (JNIEnv *env, jclass, jint id, jint n)
{
    Text::QuadFont *quadFont = quadFonts.get(id);
    quadFont->setNGlyphs(n);
}

jf(void, impl_1QuadFont_1setMeasurements)
    (JNIEnv *env, jclass,
	jint id, jint glyph, 
			    jint texInd,
			    jfloat x0, jfloat y0, jfloat x1, jfloat y1,
			    jfloat tx0, jfloat ty0, jfloat tx1, jfloat ty1,
			    jfloat xadvance, jfloat yadvance) 
{
    Text::QuadFont *quadFont = quadFonts.get(id);

    quadFont->textureIndex[glyph] = texInd;
    
    quadFont->coordinates[8*glyph + 0] = x0;
    quadFont->coordinates[8*glyph + 1] = y0;
    quadFont->coordinates[8*glyph + 2] = x1;
    quadFont->coordinates[8*glyph + 3] = y1;
    quadFont->coordinates[8*glyph + 4] = tx0;
    quadFont->coordinates[8*glyph + 5] = ty0;
    quadFont->coordinates[8*glyph + 6] = tx1;
    quadFont->coordinates[8*glyph + 7] = ty1;

    quadFont->advances[glyph] = xadvance;

}

jf(jfloatArray, impl_1QuadFont_1getMeasurements)
    (JNIEnv *env, jclass, jint id, jint glyph) 
{
    Text::QuadFont *quadFont = quadFonts.get(id);

    jfloatArray ret = env->NewFloatArray(11);
    jfloat *p = env->GetFloatArrayElements(ret, 0);

    p[0] = quadFont->textureIndex[glyph];

    p[1] = quadFont->coordinates[8*glyph + 0];
    p[2] = quadFont->coordinates[8*glyph + 1];
    p[3] = quadFont->coordinates[8*glyph + 2];
    p[4] = quadFont->coordinates[8*glyph + 3];
    p[5] = quadFont->coordinates[8*glyph + 4];
    p[6] = quadFont->coordinates[8*glyph + 5];
    p[7] = quadFont->coordinates[8*glyph + 6];
    p[8] = quadFont->coordinates[8*glyph + 7];

    p[9] = quadFont->advances[glyph];
    p[10] = 0;

    env->ReleaseFloatArrayElements(ret, p, 0);
    return ret;
}

jobjectArray enumVector2tokenStringArray(JNIEnv *env, vector<GLenum> &vec) {
    jobjectArray ret = env->NewObjectArray(
		vec.size(),
		env->FindClass("java/lang/String"), 
		NULL);
    for(unsigned i=0; i< vec.size(); i++)
	env->SetObjectArrayElement(
	    ret,
	    i,
	    env->NewStringUTF(CallGL::getTokenString(vec[i]))
		);
    return ret;
}

jf(jobjectArray, impl_1QuadFont_1getTexUnits)
    (JNIEnv *env, jclass, jint id) 
{
    Text::QuadFont *quadFont = quadFonts.get(id);
    return enumVector2tokenStringArray(env, quadFont->textureUnits);
}

jf(jobjectArray, impl_1QuadFont_1getTexTargets)
    (JNIEnv *env, jclass, jint id) 
{
    Text::QuadFont *quadFont = quadFonts.get(id);
    return enumVector2tokenStringArray(env, quadFont->textureTargets);
}





}

}
}
