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

# 
# A gl screenshot script framework - utilities
# This is the module that snapshot scripts should
# import

import vob

def getvs():
    return vob.putil.demowindow.w.createVobScene()


def shoot(file, vs, x, y, w, h):
    vob.putil.demowindow.w.renderStill(vs, 0)
    vob.putil.saveimage.save(file, 
		vob.putil.demowindow.w.readPixels(x,y,w,h),
		w, h)
    if vob.putil.snapshots.loop:
	if file == vob.putil.snapshots.loop:
	    vob.putil.snapshots.keyhit = 0
	    while not vob.putil.snapshots.keyhit:
		vob.AbstractUpdateManager.waitEvent()
	    raise vob.putil.snapshots.SnapshotLoop()
		    
