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


# Saving demo sequences

from vob.putil import saveimage
from org.nongnu.libvob.gl import GL
import os

def saveframe(filename, win, x=0, y=0, w=None, h=None):
    s = win.getSize()
    if w==None: w = s.width
    if h==None: h = s.height
    pix = win.readPixels(x, y, w, h)
    saveimage.save(filename, pix, w, h)

def savesequence(win, filebase, vs1, vs2, n, **args):
    for frame in range(0, n+1):
	fract = frame / (n+0.0)
	win.renderAnim(vs1, vs2, fract, 0, 1)
        print "saving frame", frame+1, "of", n+2
	saveframe(filebase + "%03d"%frame + ".jpg", win, **args)

def encodefilm(globpat, outfilm):
    #mencoder = "/BIG/MPlayer-0.90pre8/mencoder"
    #Please install mplayer, there *is* debian/rules script in mplayer CVS
    mencoder = "mencoder"
    cmd = "%(mencoder)s %(globpat)s -lavcopts vcodec=mpeg4 -mf on:fps=25 -ovc lavc -o %(outfilm)s" % locals()
    print cmd
    os.system(cmd)

