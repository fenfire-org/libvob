package org.nongnu.libvob.gl.impl.lwjgl.texture;

import java.nio.FloatBuffer;
import java.security.SecureRandom;

import org.lwjgl.BufferUtils;

// may be fourier noise...
public class FNoiseTex extends NamedTexture {
    /*
    fnoise.texture
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
    // what?!
//    #define a b

    public FNoiseTex() {}

    static SecureRandom rand = new SecureRandom();
    
    static double drand() {
	return rand.nextDouble();
    }

    /* Add a fourier noise to the data.
     */
    static void fourier_noise(int width, int height, int depth, int components, FloatBuffer data, 
		float freq, float df) {
	fourier_noise(width, height, depth, components, data, freq, df, 1.0f);
    }
    static void fourier_noise(int width, int height, int depth, int components, FloatBuffer data, 
    		float freq, float df, float aniso) {
	System.out.println("fn: "+components);
	
        int nf = (int)(2 * (freq + df) * aniso);

        float xsin[][][] = new float[2][nf][width];
        xsin[0] = new float[nf][width];
        for (int i=0; i<nf; i++)
            xsin[0][i] = new float[width];
        xsin[1] = new float[nf][width];
        for (int i=0; i<nf; i++)
            xsin[1][i] = new float[width];
        for(int i=0; i<width; i++) 
    	for(int f = 0; f < nf; f++) {
    	    xsin[0][f][i] = (float) Math.sin(i/(float)width * f * Math.PI * 2);
    	    xsin[1][f][i] = (float) Math.cos(i/(float)width * f * Math.PI * 2);
        }

        float ysin[][][] = new float[2][nf][height];
        ysin[0] = new float[nf][height];
        for (int i=0; i<nf; i++)
            ysin[0][i] = new float[height];
        ysin[1] = new float[nf][height];
        for (int i=0; i<nf; i++)
            ysin[1][i] = new float[height];
        for(int j=0; j<width; j++) 
    	for(int f = 0; f < nf; f++) {
    	    ysin[0][f][j] = (float) Math.sin(j/(float)height * f * Math.PI * 2);
    	    ysin[1][f][j] = (float) Math.cos(j/(float)height * f * Math.PI * 2);
        }
        
        float sumsq[] = new float[components];
        for(int i=0; i<components; i++) sumsq[i] = 0;
        
        if(depth < 2) {
    	// 2D
    	for(int xf = 0; xf < nf; xf++) {
    	  for(int yf = 0; yf < nf; yf++) {

    	    double f = Math.sqrt(xf*xf + yf*yf*(aniso*aniso));
    	    if(f < freq-df || f > freq+df) continue;

    	    for(int xsc = 0; xsc < 2; xsc++) {
    	      for(int ysc = 0; ysc < 2; ysc++) {
    		float coeff[] = new float[components];
    		for(int co = 0; co < components; co++) {
    		    coeff[co] = (float) (drand()-0.5);
    		    sumsq[co] += coeff[co] * coeff[co];
    		}
    		int ind = 0;
    		for (int j = 0; j < height; j++) {
    		  for (int i = 0; i < width; i++) {
    		    for(int co = 0; co < components; co++) {
    			data.put(ind, data.get(ind) + (xsin[xsc][xf][i] * ysin[ysc][yf][j] * coeff[co]));
    			ind++;
    		    }
    		  }
    		}
    	      }
    	    }
    	  }
    	}
    	int ind = 0;

    	for(int co = 0; co < components; co++)
    	  sumsq[co] = (float) Math.sqrt(sumsq[co]);
    	
    	for (int j = 0; j < height; j++) {
    	  for (int i = 0; i < width; i++) {
    	    for(int co = 0; co < components; co++) {
    		data.put(ind, data.get(ind) / sumsq[co]);
    		ind++;
    	    }
    	  }
    	}
    	
        } else {
    	// float zsin[width][nf];
    	// 3D
        }
    }

    double identity(double x) { return x; }

    public void render(TextureParam params, int width, int height, int depth, int components, FloatBuffer data) {
	float bias = params.get("bias", 0);
	float scale = params.get("scale", 1f);
	float freq = params.get("freq", 5f);
	float aniso = params.get("aniso", 1.0f);
	float df = params.get("df", 2f);
	long seed = params.getLong("seed", 0);
	float turb = params.get("turb", 0);
	float fbm = params.get("fbm", 0);
	float freq2 = params.get("freq2", 20f);
        
        //double (*func)(double) = identity;
        if (turb >0) {
            fbm = 1;
        }

        if (seed > 0) rand.setSeed(seed); //srandom((long)seed);

        int d = (depth==0 ? 1 : depth);
        int n = width*height*d*components;

        for(int i = 0; i<n; i++)
          data.put(i, 0);


        if (fbm > 0) {
            FloatBuffer tmp = BufferUtils.createFloatBuffer(n);
          
            for (float f = freq; f <= freq2; f += f) {
        	for(int i = 0; i<n; i++)
        	    tmp.put(i, 0);
        	fourier_noise(width,height,d,components,tmp, f, df, aniso);

        	float m = (float) (1.0 / (Math.log(f)/Math.log(2) + 1));
    	
        	for(int i = 0; i<n; i++)
        	    if (turb > 0)
        		data.put(i,  data.get(i) + m * Math.abs(tmp.get(i)));
        	    else
        		data.put(i,  data.get(i) + m * tmp.get(i));
            }
          
            for(int i = 0; i<n; i++)
        	tmp.put(i, 0);

        } else {

            fourier_noise(width,height,d,components,data, freq, df, aniso);
        }
        
        for(int i = 0; i<n; i++) {
            data.put(i, data.get(i) * scale);
            data.put(i, data.get(i) + bias);
        }
    }


}
