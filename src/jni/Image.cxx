/*
Image.cxx
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
#include <vob/glerr.hxx>

#include <vob/Texture.hxx>
#include <vob/buildmipmaps.hxx>
#include <vob/Debug.hxx>

#include <vob/util/ImageLoader.hxx>

#include "org_nongnu_libvob_gl_GL.h"
#include "vobjnidef.hxx"

namespace Vob {
namespace JNI {
    using namespace Vob::ImageLoader;

    ObjectStorer<ImageLoader::RGBARaster> images("Images");

static void gotError(JNIEnv *env) {
    cerr << "Image error\n";
    jclass errclass = env->FindClass("java/lang/Error");
    env->ThrowNew(errclass, "Image loading error!");
}

extern "C" {
// Image

jf( jint , createImageImpl )
  (JNIEnv *env, jclass, jstring filename) {
      std::string utf = jstr2stdstr(env, filename);
      RGBARaster *img = loadImageRGBA(utf.c_str());
      if(img == 0) {
	  gotError(env);
	  return 0;
      }
      return images.add(img);
  }

jf( void , deleteImage )
  (JNIEnv *, jclass, jint img) {
      images.remove(img);
  }

jf( jint , getImageSize )
  (JNIEnv *, jclass, jint img, jint dimNo) {
      RGBARaster *i = images[img];
      if(dimNo == 0)
	  return i->getWidth();
      else
	  return i->getHeight();
}

jf( jint, getImagePixel )
(JNIEnv *, jclass, jint img, jint offset) {
    RGBARaster *i = images[img];
    if(offset < 0 || (unsigned)offset >= i->getData().size()) 
	return -1;
    return i->getData()[offset];
}

jf( void , impl_1Texture_1loadSubImage )
  (JNIEnv *env, jclass, jint id, jint level, jint imageId, jint x, jint y,
    jint xoffs, jint yoffs, jint w, jint h) {

    glBindTexture(GL_TEXTURE_2D, id);
    GLERR;
    glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP_SGIS, GL_TRUE);
    GLERR;

    RGBARaster *img = images.get(imageId);

    glPushClientAttrib(GL_CLIENT_PIXEL_STORE_BIT);
    img->setGLPixelModes();
    glPixelStorei(GL_UNPACK_SKIP_PIXELS, x);
    glPixelStorei(GL_UNPACK_SKIP_ROWS, y);
    GLERR;
    glTexSubImage2D(GL_TEXTURE_2D, level, xoffs, yoffs,
	    w, h, img->getGLFormat(), img->getGLType(), img->getPointer()
	    );
    GLERR;
    glPopClientAttrib();

    GLERR;
    glBindTexture(GL_TEXTURE_2D, 0);
    GLERR;
}



}
}
}

