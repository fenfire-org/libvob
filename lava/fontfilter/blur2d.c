/*
blur2d.c
 *    
 *    Copyright (c) 2003, Janne V. Kujala
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
 * Written by Janne V. Kujala
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>

typedef float FLOAT;

int main(int argc, char *argv[])
{
    int x0, y0, x, y, i, n;
    int w = atoi(argv[1]);
    int h = atoi(argv[2]);
    int c = atoi(argv[3]);

    FLOAT *data0 = malloc(w * h * sizeof(FLOAT));
    FLOAT *data1 = malloc(w * h * c * sizeof(FLOAT));
    FLOAT *data2 = malloc(w * h * c * sizeof(FLOAT));
    FLOAT *p2 = data2;

    assert(data0 && data1 && data2);

    fprintf(stderr, "Blurring %dx%dx%d\n", w, h, c);

    n = fread(data0, sizeof(FLOAT), w * h, stdin);
    fprintf(stderr, "Read %d elements\n", n);

    n = fread(data1, sizeof(FLOAT), w * h * c, stdin);
    fprintf(stderr, "Read %d elements\n", n);
    
    for (y0 = 0; y0 < h; y0++) {
	if ((y0 & 7) == 0) {
	    fprintf(stderr, ".");
	    fflush(stderr);
	}
	for (x0 = 0; x0 < w; x0++) {
	    FLOAT a = *data0++;

	    if (a > 0.1) {
		int r = ceil(4 * a);

		double m[r+1];
		double s1 = 0;
		double s[c];
		for (i = 0; i < c; i++)
		    s[i] = 0;

		a = -1 / (2 * a * a);
		for (i = r; i >= 0; i--) {
		    s1 += m[i] = exp(i * i * a);
		}
		s1 += s1 - 1;

		if ((w & w-1) || (h & h-1)) {
		    for (y = -r; y <= r; y++) {
			FLOAT *p0 = data1 + (y0 + y + h) % h * w * c;
			for (x = -r; x <= r; x++) {
			    FLOAT *p = p0 + (x0 + x + w) % w * c;
			    double m2 = m[abs(x)] * m[abs(y)];
			    for (i = 0; i < c; i++) s[i] += m2 * p[i];
			}
		    }
		} else {
		    for (y = -r; y <= r; y++) {
			FLOAT *p0 = data1 + (y0 + y & h-1) * w * c;
			for (x = -r; x <= r; x++) {
			    FLOAT *p = p0 + (x0 + x & w-1) * c;
			    double m2 = m[abs(x)] * m[abs(y)];
			    for (i = 0; i < c; i++) s[i] += m2 * p[i];
			}
		    }
		}

		for (i = 0; i < c; i++) {
		    *p2++ = s[i] / (s1 * s1);
		}
	    } else {
		for (i = 0; i < c; i++) {
		    *p2++ = data1[(x0 + y0 * w) * c + i];
		}
	    }

	}
    }
    fprintf(stderr, "\n");
    fprintf(stderr, "Writing %d elements\n", w * h * c);
    fflush(stderr);
    n = fwrite(data2, sizeof(FLOAT), w * h * c, stdout);

    return 0;
}
