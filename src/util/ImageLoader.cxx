/*
ImageLoader.cxx
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


#include <vector>

#include </usr/include/gdk-pixbuf-1.0/gdk-pixbuf/gdk-pixbuf.h>

#include <vob/util/ImageLoader.hxx>
#include <iostream>


namespace Vob {
namespace ImageLoader {

    DBGVAR(dbg, "ImageLoader");

    /** Load an image into a raster using gdk-pixbuf.
     */
    RGBARaster *loadImageRGBA(const char *filename) {
	GdkPixbuf* pb = gdk_pixbuf_new_from_file(filename);
	if(!pb) return 0;

	DBG(dbg) << "NC "<<gdk_pixbuf_get_n_channels(pb)
	    <<" ALP "<<gdk_pixbuf_get_has_alpha(pb)
	    <<" BPS "<<gdk_pixbuf_get_bits_per_sample(pb) 
	    <<" PIX "<<int(gdk_pixbuf_get_pixels(pb))
	    <<" W "<<gdk_pixbuf_get_width(pb)
	    <<" H "<<gdk_pixbuf_get_height(pb)
	    <<" RS "<<gdk_pixbuf_get_rowstride(pb)
	    <<"\n";

	int nc = gdk_pixbuf_get_n_channels(pb);
	std::vector<GLuint> data;
	int w = gdk_pixbuf_get_width(pb);
	int h = gdk_pixbuf_get_height(pb);
	if(nc == 4) {
	    DBG(dbg) << "4: data resize\n";
	    data.resize(w*h);
	    DBG(dbg) << "get pixels\n";
	    GLuint *c = (GLuint *)gdk_pixbuf_get_pixels(pb);
	    DBG(dbg) << "pixels "<<(int)c<<"\n";
	    DBG(dbg) << "copy\n";
	    copy(c, c + w * h, data.begin());
	    /*
	    for(int i=0; i<w*h; i++) 
		cout << data[i] << " ";
	    */
	    DBG(dbg) << "\n";
	} else if(nc == 3) {
	    DBG(dbg) << "3: data resize\n";
	    data.resize(w*h);
	    DBG(dbg) << "get pixels\n";
	    GLubyte *c = (GLubyte *)gdk_pixbuf_get_pixels(pb);
	    DBG(dbg) << "pixels "<<(int)c<<"\n";
	    for(int i=0; i<w*h; i++) {
		data[i] = c[i*3] + (c[i*3+1] << 8) + (c[i*3+2] << 16) + (255 << 24);
	    }
	    DBG(dbg) << "copied \n";
	} else {
	    DBG(dbg) << "invalid \n";
	    gdk_pixbuf_unref(pb);
	    return 0;
	}
	gdk_pixbuf_unref(pb);
	DBG(dbg) << "unrefed \n";
	return new RGBARaster(w, h, data);
    }
}
}

