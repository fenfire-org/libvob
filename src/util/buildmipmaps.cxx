/*
buildmipmaps.cxx
 *    
 *    Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Janne Kujala and Tuomas J. Lukka
 */

/* Partial implementation of the functionality of gluBuild2DMipmaps */

#include <GL/gl.h>
#include <assert.h>

#include <vob/buildmipmaps.hxx>

namespace Util {

    template <class TYPE>
    void filter(TYPE *dst, TYPE *src, GLsizei w, GLsizei h, int comp, int xfact, int yfact) {
      for (int y = 0; y < h; y += yfact)
	for (int x = 0; x < w; x += xfact)
	  for (int c = 0; c < comp; c++)
	    {
	      float value = 0;
	      for (int j = 0; j < xfact; j++)
		for (int i = 0; i < yfact; i++)
		  value += src[c + (x + i + w * (y + j)) * comp];
	      *dst++ = (TYPE)(value / (xfact * yfact) + (.5f - (TYPE).5f));
	    }
    }

    void buildmipmaps(GLenum target, GLint intformat, GLsizei w, GLsizei h, 
		      GLenum format, GLenum type, const void *pixels) {
      assert((w - 1 & w) == 0 && (h - 1 & h) == 0);

      int comp = 0;
      switch (format) {
      default: 
      case GL_COLOR_INDEX: 
	assert(0); 
	break;
      case GL_DEPTH_COMPONENT:
      case GL_RED:
      case GL_GREEN:  
      case GL_BLUE: 
      case GL_ALPHA:
      case GL_LUMINANCE:  
	comp = 1; break;
      case GL_LUMINANCE_ALPHA: 
	comp = 2; break;
      case GL_RGB:   
      case GL_BGR:   
	comp = 3; break;
      case GL_RGBA:   
      case GL_BGRA:
	comp = 4; break;
      }

      int w2 = w, h2 = h;
      int level = 0;
      char *data = new char[w * h * comp * 4];

      while (1) {
	int xf = w / w2;
	int yf = h / h2;
	switch (type) {
	case GL_UNSIGNED_BYTE: 
	  filter((unsigned char *)data, (unsigned char *)pixels, w, h, comp, xf, yf); break;
	case GL_BYTE: 
	  filter((char *)data, (char *)pixels, w, h, comp, xf, yf); break;
	case GL_UNSIGNED_SHORT: 
	  filter((unsigned short *)data, (unsigned short *)pixels, w, h, comp, xf, yf); break;
	case GL_SHORT: 
	  filter((short *)data, (short *)pixels, w, h, comp, xf, yf); break;
	case GL_UNSIGNED_INT: 
	  filter((unsigned int *)data, (unsigned int *)pixels, w, h, comp, xf, yf); break;
	case GL_INT: 
	  filter((int *)data, (int *)pixels, w, h, comp, xf, yf); break;
	case GL_FLOAT: 
	  filter((float *)data, (float *)pixels, w, h, comp, xf, yf); break;
	default:
	  assert(0);
	}

	glTexImage2D(target, level, intformat, w2, h2, 0, format, type, data);
	level++;
	
	if (w2 <= 1 && h2 <= 1) break;

	w2 = w2 + 1 >> 1;
	h2 = h2 + 1 >> 1;
      }
      
      glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
      glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, level - 1);

      delete[] data;
    }
}
