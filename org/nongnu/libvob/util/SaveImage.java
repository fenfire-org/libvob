/*
SaveImage.java
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.util;
import java.io.*;

public class SaveImage {
    static public void writeBytesRGB(OutputStream o, int[] pixels) 
     throws IOException {
	byte[] tmp = new byte[3*pixels.length];
	for(int i=0; i<pixels.length; i++) {
	    int c = pixels[i];
	    tmp[3*i+0] = (byte)((c >> 16) & 0xff);
	    tmp[3*i+1] = (byte)((c >> 8) & 0xff);
	    tmp[3*i+2] = (byte)((c >> 0) & 0xff);
	}
	o.write(tmp);
    }
}
