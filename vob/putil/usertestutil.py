# 
# Copyright (c) 2003, Tuomas J. Lukka
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


import vob
import java

w = vob.putil.demowindow.w

_currentvs = None

def getvs():
    return w.createVobScene()

def render(vs):
    w.renderStill(vs, 0)
    global _currentvs
    _currentvs = vs

def renderOnly(vs):
    """ Render without swapping """
    t = w.timeRender(vs, 0, 1)
    #print "render", 1000*t, "ms"
    global _currentvs
    _currentvs = vs

dummyvs = w.createVobScene()
def swapBuffers():
    """ Swap buffers; use after renderOnly """
    w.renderStill(dummyvs, 0)

_keyqueue = java.util.Collections.synchronizedList(
		java.util.ArrayList())

def _key(s):
    # print "Put key!"
    _keyqueue.add( 
	(s, java.lang.System.currentTimeMillis()))
    #vob.util.JythonWaiting.putQueue(_keyqueue, 
    #		    (s, java.lang.System.currentTimeMillis()) )

def waitkey(proc = None, t0 = None, timeout = None):
    if t0 == None:
        t0 = java.lang.System.currentTimeMillis()
    # print "Wait key!"
    while _keyqueue.isEmpty():
        if timeout != None:
            t = java.lang.System.currentTimeMillis()
            if t - t0 > timeout:
                return None, t - t0 
            vob.AbstractUpdateManager.tickIdle()
        elif proc:
            proc()
            vob.AbstractUpdateManager.tickIdle()
        else:
            vob.AbstractUpdateManager.waitEvent()
    k = java.util.List.remove(_keyqueue, 0)
    # print "Got key!", k[0], k[1]-t0
    return (k[0], k[1] - t0)

def wait(ms, proc = None):
    t0 = java.lang.System.currentTimeMillis()
    while 1:
        if proc: proc()
        t = java.lang.System.currentTimeMillis()
        if t - t0 >= ms:
            return t - t0
    

def timeScrub():
    java.lang.System.gc()
    while vob.AbstractUpdateManager.tickIdle():
	pass
    java.lang.System.gc()
    
