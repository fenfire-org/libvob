# 
# Copyright (c) 2003, Tuomas J. Lukka
# 
# This file is part of Libvob.
# 
# Libvob is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Libvob is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Libvob; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 


# Script to pack textures into zip files of mipmap levels ("mipzip" files)
# The files contain all mipmap levels of the the texture in 
# the COMPRESSED_RGB_S3TC_DXT1_EXT format, with the mipmap level as the file name
# and a "%dx%d" string of the width and height as the file comment.

# Usage: something like:
#
#   make runjython DBG="vob/putil/mipzipmaker.py  ../tmpimg/*-170-*[0-9]"

# Can also be used as a python module - fenfire does this automatically

from __future__ import nested_scopes

import sys
import getopt
from java.lang import Runnable, System
from java.util import zip
import java
import vob
import vob.putil.dbg
import jarray

prop = System.getProperties()
prop.setProperty("vob.api", "gl")


from org.nongnu.libvob.gl import GL

def roundup2(n):
    i = 1
    while i < n:
	i *= 2
    return i
def chomp4(n):
    n /= 4
    return 4 * n

defaultTexFormat = 0


def _init():
    global defaultTexFormat, suffix
    if defaultTexFormat:
	return

    if GL.workaroundStupidBuggyAtiDrivers:
	defaultTexFormat = "RGB"
	suffix = ".mipzipBLAH"
    else:
	defaultTexFormat = "COMPRESSED_RGB_S3TC_DXT1_EXT"
	suffix = ".mipzip"

def _clipmax(x, max):
    if max < 0: return x
    if max > x: return x
    return max

def bytesPerTexel(format, dataType):
    return vob.gl.GLUtil.findBpt(format, dataType)

def makeMipzip(image, mipzip, maxwidth=-1, maxheight=-1, 
	texformat = None, 
	internalTexFormat = None,
	uncompressedTexType = None
	):
    """Convert the given image file into a mipzip file.

    image -- the image file name
    mipzip -- the mipzip file name
    maxwidth -- if image is wider than maxwidth, cut off edge
    maxheight -- if image is taller than maxheight, cut off edge
    texformat -- The texture format to use
    internalTexFormat -- The internal texture format to represent the data in
                         for compressed textures, same as texformat
    uncompressedTexType -- The datatype (relevant only for uncompressed
			   textures)
    """

    if texformat == None:
	if not defaultTexFormat:
	    _init()
	texformat = defaultTexFormat

    print "TEXFORMAT: ",texformat, defaultTexFormat
    isCompressed = (java.lang.String(texformat).indexOf("COMPRESS") >= 0)

    if internalTexFormat == None:
	internalTexFormat = texformat

    if not isCompressed:
	if uncompressedTexType == None:
	    uncompressedTexType = "UNSIGNED_BYTE"

    _init()
    GL.freeQueue()

    im = GL.createImage(image)
    d0 = im.getSize();
    print d0
    d = java.awt.Dimension(
	_clipmax(d0.width, maxwidth),
	_clipmax(d0.height, maxheight),
	)


    w = roundup2(d.width)
    h = roundup2(d.height)
    tex = GL.createTexture()
    tex.loadNull2D('TEXTURE_2D',0, internalTexFormat, w, h, 0, "RGB", "BYTE")
    print "WH: ",w, h
    tex.loadSubImage(0, im, 0, 0, 0, 0, chomp4(d.width), chomp4(d.height))

    print "Write ",mipzip

    out = zip.ZipOutputStream(java.io.FileOutputStream(mipzip)) 

    def metaEntry(name, comment):
	entry = zip.ZipEntry(name)
	entry.setComment(comment)
	entry.setSize(0)
	out.putNextEntry(entry)
	out.closeEntry()

    metaEntry("texformat", texformat)

    if not isCompressed:
	metaEntry("internaltexformat", internalTexFormat)
	metaEntry("datatype", uncompressedTexType)
	
    metaEntry("origsize", "%sx%s" % (d.width / float(w),
			        d.height/ float(h)))

    l = 0
    while 1:
	w = int(tex.getLevelParameter(l, "TEXTURE_WIDTH")[0])
	h = int(tex.getLevelParameter(l, "TEXTURE_HEIGHT")[0])
	print "WH: ", w, h

	if isCompressed:
	    bytes = tex.getCompressedTexImage(l)
	else:
	    bpt = bytesPerTexel(texformat, uncompressedTexType)

	    bytes = jarray.zeros(bpt * w * h, "b")
	    tex.getTexImage(l, texformat, uncompressedTexType, bytes)

	print "Bytes: ",l, len(bytes)
	entry = zip.ZipEntry(str(l))
	entry.setComment("%sx%s" % (int(w),int(h)))
	entry.setSize(len(bytes))
	out.putNextEntry(entry)
	out.write(bytes)
	out.closeEntry()
	if w == 1 and h == 1 : break
	l += 1
    out.close()
    del tex
    java.lang.System.gc()
    GL.freeQueue()
    

class Main(Runnable):
    def __init__(self, texfiles, **mzparms):
	self.texfiles = texfiles
	self.mzparms = mzparms
	_init()
    def run(self):
	w = gfxapi.createWindow()
	for texfile in self.texfiles:
	    self.handle(texfile)
	System.exit(0)

    def handle(self, texfile):
	of = texfile + suffix
	makeMipzip(texfile, of, **self.mzparms)

if __name__ == '__main__':

    opts, args = getopt.getopt(sys.argv[1:], 
	    vob.putil.dbg.short, 
	    ["texFormat=", "internalTexFormat=", "datatype="]
		+ vob.putil.dbg.long)
    texFormat = None
    internalTexFormat = None
    datatype = None
    for o,a in opts:
	print "Opt: ",o,a
	if o in vob.putil.dbg.all:
	    vob.putil.dbg.option(o,a)
	if o == "--texFormat":
	    texFormat = a
	if o == "--internalTexFormat":
	    internalTexFormat = a
	if o == "--datatype":
	    datatype = a


    gfxapi = vob.GraphicsAPI.getInstance()
    gfxapi.startUpdateManager(Main(args,
	texformat=texFormat,
	internalTexFormat=internalTexFormat,
	uncompressedTexType=datatype
	))

