package org.nongnu.libvob.gl.impl.lwjgl.texture;

import java.nio.FloatBuffer;
import java.security.SecureRandom;

public class GeometricTex extends NamedTexture {
    /*-
    geometric.texture
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

//     -*-C++-*-
//    #include <math.h>



	//    #define FPARAM(name, default) float name = params->getFloat(#name, default);
    	float SQR(float A) { return (float) ((A) * (A)); }

    
    
    float pyramid(float x, float y) {
      x -= Math.floor(x);
      y -= Math.floor(y);

      x = (float) Math.abs(x - .5);
      y = (float) Math.abs(y - .5);

      return 1 - 2 * (x > y ? x : y);
    }

    float checkerboard(float x, float y) {
      x -= Math.floor(x);
      y -= Math.floor(y);

      return (x < .499999) ^ (y < .499999)? 1f:0;
    }

    float cone(float x, float y) {
      x -= Math.floor(x);
      y -= Math.floor(y);

      x -= .5;
      y -= .5;
      
      return (float) (1 - 2 * Math.sqrt(.5 * (x * x + y * y)));
    }

    float checkerboard2(float x, float y) {
      x -= Math.floor(x);
      y -= Math.floor(y);
      return (x < .499999) && (y < .499999)?1f:0;
    }

    float saw(float x) {
      return (float) (x - Math.floor(x));
    }

    float triangle(float x) {
      x -= Math.floor(x);
      return (float) (1 - 2 * Math.abs(x - .5));
    }

    float stripe(float x) {
      x -= Math.floor(x);
      return (x < .4999999)?1f:0;
    }

    float circle(float x, float y) {
      return ((SQR(x - 0.5f) + SQR(y - 0.5f)) <= SQR(0.5f))? 1f: 0;
    }

    public GeometricTex() {}
    public void render(TextureParam params, int width, int height, int depth, int components, FloatBuffer data) {

	float type = params.get("type", 0);
	float scale = params.get("params", 1f);
	float bias = params.get("bias", 0);
	float seed = params.get("seed", 1f);

        if(components > 4) return;
        
        SecureRandom srand = new SecureRandom();
        if (seed != 1f)
            srand.setSeed((long)seed);
//          srandom((unsigned)seed);
 

        int ind=0, i, j;
        float x, y;
        float xstep = 1.0f / width;
        float ystep = 1.0f / height;

        for (j = 0, y = 0; j < height; j++, y += ystep) {	
          for (i = 0, x = 0; i < width; i++, x += xstep) {

    	switch ((int)type) {
    	case 0: 
    	  if (components >= 1) data.put(ind++, pyramid(x, y));
    	  if (components >= 2) data.put(ind++, pyramid(x + .5f, y));
    	  if (components >= 3) data.put(ind++, pyramid(x, y + .5f));
    	  if (components >= 4) data.put(ind++, pyramid(x + .5f, y + .5f));
    	  break;
    	case 1:
    	  if (components >= 1) data.put(ind++, checkerboard(x, y));
    	  if (components >= 2) data.put(ind++, checkerboard(x + .25f, y));
    	  if (components >= 3) data.put(ind++, checkerboard(x, y + .25f));
    	  if (components >= 4) data.put(ind++, checkerboard(x + .25f, y + .25f));
    	  break;
    	case 2:
    	  if (components >= 1) data.put(ind++, cone(x, y));
    	  if (components >= 2) data.put(ind++, cone(x + .5f, y));
    	  if (components >= 3) data.put(ind++, cone(x, y + .5f));
    	  if (components >= 4) data.put(ind++, cone(x + .5f, y + .5f));
    	  break;
    	case 3:
    	  if (components >= 1) data.put(ind++, checkerboard2(x, y));
    	  if (components >= 2) data.put(ind++, checkerboard2(x + .5f, y));
    	  if (components >= 3) data.put(ind++, checkerboard2(x, y + .5f));
    	  if (components >= 4) data.put(ind++, checkerboard2(x + .5f, y + .5f));
    	  break;
    	case 4:
    	  if (components >= 1) data.put(ind++, saw(x));
    	  if (components >= 2) data.put(ind++, saw(y));
    	  if (components >= 3) data.put(ind++, saw(1 - xstep - x));
    	  if (components >= 4) data.put(ind++, saw(1 - ystep - y));
    	  break;
    	case 5:
    	  if (components >= 1) data.put(ind++, triangle(x));
    	  if (components >= 2) data.put(ind++, triangle(y));
    	  if (components >= 3) data.put(ind++, triangle(.5f + x));
    	  if (components >= 4) data.put(ind++, triangle(.5f + y));
    	  break;
    	case 6:
    	  if (components >= 1) data.put(ind++, stripe(x));
    	  if (components >= 2) data.put(ind++, stripe(y));
    	  if (components >= 3) data.put(ind++, stripe(1 - xstep - x));
    	  if (components >= 4) data.put(ind++, stripe(1 - ystep - y));
    	  break;
    	case 7:
    	    throw new Error("argh");
//    	  if (components >= 1) data[ind++] = srand.nextFloat / (RAND_MAX + 1.0);
//    	  if (components >= 2) data[ind++] = srand.nextFloat / (RAND_MAX + 1.0);
//    	  if (components >= 3) data[ind++] = srand.rand() / (RAND_MAX + 1.0);
//    	  if (components >= 4) data[ind++] = rand() / (RAND_MAX + 1.0);
//    	  break;
    	case 8:
    	  if (components >= 1) data.put(ind++, circle(x + 0.5f/width, y + 0.5f/height));
    	  if (components >= 2) data.put(ind++, circle(x + 0.5f/width, y + 0.5f/height));
    	  if (components >= 3) data.put(ind++, circle(x + 0.5f/width, y + 0.5f/height));
    	  if (components >= 4) data.put(ind++, circle(x + 0.5f/width, y + 0.5f/height));
    	  break;
          	}
          }	
        }

        for(i = 0; i < width * height * depth * components; i++) {
          data.put(i, data.get(i) * scale + bias);
        }

//        data.flip();
    }

}
