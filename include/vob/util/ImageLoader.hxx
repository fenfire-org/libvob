/*
ImageLoader.hxx
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

#ifndef VOB_UTIL_IMAGELOADER_HXX
#define VOB_UTIL_IMAGELOADER_HXX

#include <GL/gl.h>


#include <vob/Debug.hxx>
#include <vector>

namespace Vob {

/** Interfaces for image loading.
 */
namespace ImageLoader {

    PREDBGVAR(dbg);

// /** An image being loaded.
//  * For maximum flexibility, the class provides two methods,
//  * one of which may be called from any thread, and the another
//  * which is called from the (synchronous) GL thread and will execute
//  * without hanging to wait for e.g. network data.
//  */
// class ImageLoaderJob {
// public:
//     virtual ~ImageLoader() {};
//     /** Process a piece of the non-GL part of the image loading.
//      * @return True, if processNoGL needs to be called again before processGL.
//      */
//     virtual bool processNoGL() = 0; //     /** Process a piece of the non-GL part of the image loading.
//      * @return True, if processNoGL needs to be called again; false, if processing
//      *         is finished.
//      */
//     virtual bool processGL() = 0;
// };

    using std::vector;

    /** A simple class for storing images.
     */
    class RGBARaster {
	int width;
	int height;
	vector<GLuint> data;
    public:
	RGBARaster(int w, int h, vector<GLuint> &data0) :
	    width(w), height(h), data(data0) {
	}
	int getWidth() { return width; }
	int getHeight() { return height; }
	void setGLPixelModes() {
	    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	    glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
	}

	GLint getGLInternalFormat() {
	    return GL_RGBA;
	}
	GLenum getGLFormat() {
	    return GL_RGBA;
	}
	GLenum getGLType() {
	    return GL_UNSIGNED_BYTE;
	}
	const vector<GLuint> &getData() { return data; }
	void *getPointer() {
	    return &(data[0]);
	}


    };


    /** Load a single file synchronously into an RGBA raster.
     * Bad entry point, will be changed.
     */
    extern RGBARaster *loadImageRGBA(const char *filename);


}
}

#endif
