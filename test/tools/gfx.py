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


from jarray import array, zeros

from java.awt import Color

import vob

dbg = 0

_didRender = 0

_realwin = vob.GraphicsAPI.getInstance().createWindow()
_realwin.setLocation(0, 0, 600, 600)

if vob.GraphicsAPI.getInstance().getTypeString() == "gl":
    GL = vob.gl.GL
    GLRen = vob.gl.GLRen
    GLCache = vob.gl.GLCache
    if GL.workaroundStupidBuggyAtiDrivers:
	# Sorry, ATI doesn't let us use pbuffers on R300 except in FireGL.
	# Because of that, don't put another window in front when using
	# this.
	win = _realwin
    else:
	win = vob.GraphicsAPI.getInstance().createStableOffscreen(500, 500)
	_buf = GL.createByteVector(500*500*3)
	_drawbufvs = _realwin.createVobScene()
	_drawbufvs.map.put(vob.vobs.SolidBackdropVob(Color(0, 0, 0.2)))
	_drawbufvs.map.put(GLCache.getCallList("""
	    Disable TEXTURE_2D
	    Color 1 1 1 1
	"""))
	cs = _drawbufvs.translateCS(0, "tr", 0, 501)
	_drawbufvs.map.put(
	    GLRen.createDrawPixels(
		500, 500,
		"RGB", "UNSIGNED_BYTE", _buf),
		cs)
else:
    win = _realwin

print "GW: ",win

vob.putil.demowindow.w = win

def failUnless(b, msg=None):
    if not b:
	raise str(("FU ",msg))

def getvs():
    return win.createVobScene()

def render(vs):
    global _didRender
    _didRender = 1
    win.renderStill(vs, 0)
    if win != _realwin:
	_buf.readFromBuffer(win.getRenderingSurface(), 
		    "FRONT", 0, 0, 500, 500,
			"RGB", "UNSIGNED_BYTE")
	_realwin.renderStill(_drawbufvs, 0)
	

def getAvgColor(x, y, w, h):
    
    if dbg: print "getavgcolor"
    colors = win.readPixels(x, y, w, h)
    if dbg: print "read done"
    color = vob.util.ColorUtil.avgColor(colors)
    if dbg: print "avgcolored"
    return [c*255 for c in color.getComponents(None)]

def checkAvgColor(x, y, w, h, color, delta=10):
    real = getAvgColor(x, y, w, h)
    msg = str((color, real, ":", x, y, w, h))

    for i in range(0,3):
	if abs(color[i]-real[i]) > delta:
	    raise msg


def checkNotAvgColor(x, y, w, h, color, delta=10):
    if dbg: print "Checknotavgcolor"
    real = getAvgColor(x, y, w, h)
    if dbg: print "got avg"
    msg = str((color, real, ":", x, y, w, h))

    for i in range(0,3):
	if abs(color[i]-real[i]) > delta:
	    return

    if dbg: print "Checknotavgcolor fail - raising"
    raise msg


def checkNoTrans(vs, cs):
    """Check that a transformation is singular with the 
    current coords.
    """
    src = array([0,0,0], 'f')
    dst = vs.coords.transformPoints3(cs, src, None)
    failUnless(dst == None)

def checkTrans(vs, cs, srclist, dstlist, delta=0, alsoRender = 1):
    """Check that a transformation works a certain way.
    """
    src = array(srclist, 'f')
    dst = vs.coords.transformPoints3(cs, src, None)
    failUnless(dst != None)
    for i in range(0, len(src)):
	if dstlist[i] == None:
	    continue
	if abs(dst[i]-dstlist[i]) > delta:
	    raise str([srclist, dstlist, dst, i, dst[i], dstlist[i]])
    if alsoRender:
        vs.fader = None
	for i in range(0, len(src), 3):
	    vs.map.clear()
	    vs.map.put(vob.vobs.SolidBackdropVob(Color.red))
	    d = vob.vobs.TestSpotVob(src[i], src[i+1], src[i+2])
	    vs.map.put(d, cs)
	    render(vs)
	    if (not 3 < dstlist[i] < 500-3 or 
	        not 3 < dstlist[i+1] < 500-3):
		continue
	    checkNotAvgColor(
		int(dstlist[i])-1, int(dstlist[i+1])-1,
		3, 3, (255, 0, 0), delta=50)

def checkTransEq(vs, cs, srclist):
    """Check that opengl and c++ transformations yield equal results.
    """
    src = array(srclist, 'f')
    dst = vs.coords.transformPoints3(cs, src, None)
    failUnless(dst != None)
    dstlist = [i for i in dst]
    checkTrans(vs, cs, srclist, dstlist)


def checkInterp(vs1, vs2, i, fract, cs, srclist, dstlist, delta=0):
    src = array(srclist, 'f')
    dst = zeros(len(src), 'f')
    if not vs1.coords.transformPoints3_interp(i, vs2.coords, fract, 0, cs, src, dst):
	raise str(("transformpoints for checkinterp not done!", vs1, vs2, cs))
    for i in range(0, len(src)):
	if abs(dst[i]-dstlist[i]) > delta:
	    raise str([srclist, dstlist, dst, i, dst[i], dstlist[i]])



def checkState1(vs):
    # Zero the test string
    print "CS1"
    print "CS1_X", GL
    print "CS1_X", GL.getTestStateRetainCorrect
    GL.getTestStateRetainCorrect()
    print "CS1_1"
    vs.put(GLRen.createTestStateRetainSetup())
    print "CS1_2"

def checkState2(vs):
    vs.put(GLRen.createTestStateRetainTest())

def checkState3(vs):
    s = GL.getTestStateRetainCorrect()
    if s != "":
	raise s

