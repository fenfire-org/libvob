/*
Texture.cxx
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


#include <assert.h>

#include <vob/jni/Types.hxx>
#include <vob/jni/Strings.hxx>
#include <vob/glerr.hxx>

#include <vob/Texture.hxx>
#include <vob/buildmipmaps.hxx>
#include <vob/Debug.hxx>


#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"

namespace Vob {
namespace JNI {

/** Convert the object array into setting the texture ("shader")
 * string parameters.
 */
Texture::TextureParam textureParams(JNIEnv *env, jobjectArray params) {

      Texture::TextureParam p;

      for(int i=0; i<env->GetArrayLength(params)-1; i+=2) {
	  std::string k = jstr2stdstr(env, (jstring)(env->GetObjectArrayElement(params, i)));
	  std::string v = jstr2stdstr(env, (jstring)(env->GetObjectArrayElement(params, i+1)));
	  p.setParam(k.c_str(), v.c_str());
      }

      return p;
}


extern "C" {

// Texture
jf(jint, impl_1createTexture)
  (JNIEnv *env, jclass) {
      GLuint ret;
      glGenTextures(1, &ret);
      DBG(dbg) << "Created texture id "<<ret<<"\n";
      GLERR_JNI(env);
      if(ret == 0) {
	  throwJavaError(env, "Texture couldn't be created");
      }
      return ret;
}

jf(void, impl_1deleteTexture)
  (JNIEnv *, jclass, jint id) {
      GLuint rel = id;
      glDeleteTextures(1, &rel);
      GLERR;
}

bool hasGenMipmaps() {
    static int initialized;
    static bool hasExtension;
    // XXX: the test should probably be done elsewhere
    if (!initialized) {
	hasExtension = strstr((const char *)glGetString(GL_EXTENSIONS), 
			      "GL_SGIS_generate_mipmap") != 0;
	initialized = true;
    }
    return hasExtension;
}

jf(void, impl_1Texture_1loadNull2D)
  (JNIEnv *env, jclass, jint id, 
     jstring target_s, jint level, jstring internalFormat_s,
    jint w, jint h, jint border, jstring format_s, jstring type_s) {
    int target = tokenFromJstring(env, target_s);
    int internalFormat = tokenFromJstring(env, internalFormat_s);
    int format = tokenFromJstring(env, format_s);
    int type = tokenFromJstring(env, type_s);

    glBindTexture(target, id);
    // Null = just set size and texture format. 
    glTexImage2D(target,
		level, internalFormat, w, h, border, format, type, NULL);
    glBindTexture(target, 0);
    GLERR;
  }

jf(void, impl_1Texture_1texImage2D)
  (JNIEnv *env, jclass, jint id, jint level, jstring internalFormat_s,
    jint w, jint h, jint border, jstring format_s, jstring type_s,
    jbyteArray jdata) {
    int internalFormat = tokenFromJstring(env, internalFormat_s);
    int format = tokenFromJstring(env, format_s);
    int type = tokenFromJstring(env, type_s);
    jbyte *data = env->GetByteArrayElements(jdata, 0);

    glBindTexture(GL_TEXTURE_2D, id);
    glTexImage2D(GL_TEXTURE_2D,
		level, internalFormat, w, h, border, format, type, data);
    env->ReleaseByteArrayElements(jdata, data, JNI_ABORT);
    glBindTexture(GL_TEXTURE_2D, 0);
    GLERR;
  }


jf(void, impl_1Texture_1texSubImage2D)
  (JNIEnv *env, jclass, jint id, jint level, jint x, jint y,
    jint w, jint h, jint border, jstring format_s, jstring type_s,
    jbyteArray jdata) {
    int format = tokenFromJstring(env, format_s);
    int type = tokenFromJstring(env, type_s);
    jbyte *data = env->GetByteArrayElements(jdata, 0);

    glBindTexture(GL_TEXTURE_2D, id);

    glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glTexSubImage2D(GL_TEXTURE_2D,
		level, x, y, w, h, format, type, data);

    glPopClientAttrib();

    env->ReleaseByteArrayElements(jdata, data, JNI_ABORT);
    glBindTexture(GL_TEXTURE_2D, 0);
    GLERR;
  }


jf(jbyteArray, impl_1Texture_1getCompressedTexImage)
  (JNIEnv *env, jclass, jint id, jint lod, jbyteArray preArray) {
      int size;
      glBindTexture(GL_TEXTURE_2D, id);
      glGetTexLevelParameteriv(GL_TEXTURE_2D, lod, GL_TEXTURE_COMPRESSED_IMAGE_SIZE_ARB, &size);
      if(GLERR_JNI(env)) return 0;
      jbyteArray arr;
      if(preArray == 0)
	  arr = env->NewByteArray(size);
      else
	  arr = preArray;
      jbyte *b = env->GetByteArrayElements(arr, 0);
      glGetCompressedTexImageARB(GL_TEXTURE_2D, lod, b);
      env->ReleaseByteArrayElements(arr, b, 0);
      glBindTexture(GL_TEXTURE_2D, 0);
      if(GLERR_JNI(env)) return 0;
     return arr;
}

jf(void, impl_1Texture_1getTexImage)
 (JNIEnv *env, jclass, jint id, jint level, jstring jformat, jstring jtype,
    jbyteArray jdata) {
    glBindTexture(GL_TEXTURE_2D, id);
    GLenum format = tokenFromJstring(env, jformat);
    GLenum type = tokenFromJstring(env, jtype);
    jbyte *b = env->GetByteArrayElements(jdata, 0);

    glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
    glPixelStorei(GL_PACK_ROW_LENGTH, 0);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);

    glGetTexImage(GL_TEXTURE_2D, level, format, type, b);

    glPopClientAttrib();

    env->ReleaseByteArrayElements(jdata, b, 0);
    if(GLERR_JNI(env)) return;
 }



jf(void, impl_1Texture_1compressedTexImage)
 (JNIEnv *env, jclass, jint id, jint level, jstring jinternalFormat, jint width, jint height,
	jint border, jint size, jbyteArray jdata) {
     jbyte *data = env->GetByteArrayElements(jdata, 0);
     glBindTexture(GL_TEXTURE_2D, id);
     GLenum internalFormat = tokenFromJstring(env, jinternalFormat);

     glCompressedTexImage2DARB(GL_TEXTURE_2D, level, internalFormat,
		    width, height, border, size, data);
     glBindTexture(GL_TEXTURE_2D, 0);
//     glFinish(); // Appears that NV 4191 drivers need this.
     env->ReleaseByteArrayElements(jdata, data, 0);
     GLERR;

}

jf(void, impl_1Texture_1compressedTexSubImage2D)
 (JNIEnv *env, jclass, jint id, jint level, jint xoffs, jint yoffs, jint width, jint height,
	jstring jFormat, jint size, jbyteArray jdata) {
     jbyte *data = env->GetByteArrayElements(jdata, 0);
     glBindTexture(GL_TEXTURE_2D, id);
     GLenum format = tokenFromJstring(env, jFormat);

     glCompressedTexSubImage2DARB(GL_TEXTURE_2D, level, xoffs, yoffs,
		    width, height, format, size, data);
     glBindTexture(GL_TEXTURE_2D, 0);
//     glFinish(); // Appears that NV 4191 drivers need this.
     env->ReleaseByteArrayElements(jdata, data, 0);
     GLERR;

}

jf(void, impl_1Texture_1copyTexImage2D)
    (JNIEnv *env, jclass, jint id, jint wid, jstring bufferstr,
    jstring targetstr, jint level,
    jstring iforstring, jint x, jint y, jint w, jint h,
    jint border) {
    setWindow(wid);
    GLenum buffer = tokenFromJstring(env, bufferstr);
    GLenum target = tokenFromJstring(env, targetstr);
    GLenum ifor = tokenFromJstring(env, iforstring);
    glBindTexture(target, id);
    glReadBuffer(buffer);
    DBG(dbg) << "Copyteximage "<<target<<" "<<level<<" "<<ifor<<" "
	    <<x<<" "<<y<<" "<<w<<" "<<h<<" "<<border<<"\n";
    glCopyTexImage2D(target, level, ifor, x, y, w, h, border);
    glBindTexture(target, 0);
    GLERR;
    releaseWindow();
}

jf(jint, impl_1Texture_1shade)
  (JNIEnv *env, jclass, jint id, jint w, jint h, jint d, jint comp, 
	jstring internalFormat,
	jstring format,
	    jstring name, jobjectArray params, jboolean shade_all_levels) {
      DBG(dbg)<<"Shade into "<<id<<"\n";

      std::string name_utf = jstr2stdstr(env, name);

      Texture::Texture *s = Texture::Texture::getTexture(name_utf.c_str());

      if(!s) {
	  return 0;
      }

      Texture::TextureParam p = textureParams(env, params);

      float *value = new float[w * h * (d==0?1:d) * comp];

      GLenum target = (d == 0) ? GL_TEXTURE_2D : GL_TEXTURE_3D;

      glBindTexture(target, id);

      int buildmipmaps = 0;

      if (!shade_all_levels) {
	  if (hasGenMipmaps()) {
	      glTexParameteri(target, GL_GENERATE_MIPMAP_SGIS, GL_TRUE);
	      GLERR;
	  } else {
	      buildmipmaps = 1;
	  }
      } 
	
      int level;
      for (level = 0; ; level++) {
	  s->render(&p, w, h, (d==0?1:d), comp, value);

	  if (buildmipmaps) {
	      assert(d==0); // 3D buildmipmaps not implemented in libutil
	      Util::buildmipmaps(GL_TEXTURE_2D, 
				 tokenFromJstring(env, internalFormat),
				 w, h, 
				 tokenFromJstring(env, format),
				 GL_FLOAT,
				 value);
	  } else 
	  if (d == 0)
	      glTexImage2D(GL_TEXTURE_2D, level,
			   tokenFromJstring(env, internalFormat),
			   w, h, 0, 
			   tokenFromJstring(env, format),
			   GL_FLOAT,
			   value);
	  else
	      glTexImage3D(GL_TEXTURE_3D, level,
			   tokenFromJstring(env, internalFormat),
			   w, h, d, 0, 
			   tokenFromJstring(env, format),
			   GL_FLOAT,
			   value);

	  GLERR;
	      
	  if (! (shade_all_levels && (w > 1 || h > 1 || d > 1))) break;
	  
	  w = (w + 1) >> 1;
	  h = (h + 1) >> 1;
	  d = (d + 1) >> 1;
      } 
      
      if (shade_all_levels) {
	  glTexParameterf(target, GL_TEXTURE_BASE_LEVEL, 0);
	  glTexParameterf(target, GL_TEXTURE_MAX_LEVEL, level);
      }

      glBindTexture(target, 0);
      GLERR;

      delete[] value;
      delete s;


      return 1;
}


}
}
}
