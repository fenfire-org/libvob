# 
# Copyright (c) 2003, Tuomas J. Lukka
# 
# This file is part of Gzz.
# 
# Gzz is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Gzz is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Gzz; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 


from org.nongnu.libvob.util import SaveImage
from java.io import FileOutputStream
import os

# A module for saving an image frame from the current front buffer

def save(filename, pixels, w, h):
    """Save an int[] array of pixel values.
    """
    f = FileOutputStream("img.tmp")
    SaveImage.writeBytesRGB(f, pixels)
    f.close()
    print "To system"
    os.system("rawtoppm %(w)s %(h)s img.tmp | convert - %(filename)s" % locals())
    print "Done system"
    #os.system("rawtoppm %(w)s %(h)s img.tmp | ppmtojpeg > %(filename)s" % locals())
    # ppmtojpeg is a bit faster than convert
