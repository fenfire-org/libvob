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


# To be run using __GL_FSAA_MODE=4
from __future__ import nested_scopes
import vob
import java
from vob.putil.snapshotutil import *

java.lang.System.setProperty("__GL_FSAA_MODE", "4")

from vob.lava.snaps.aniso_gffx import footprint, setISO, setANISO

def run():
    scene = vob.demo.aniso.probe2.Scene()
    setISO(scene)

    def sh(file):
	vs = getvs()
	scene.scene(vs)
	shoot(file, vs, *footprint)
    scene.minfilter = scene.minfilters.index("LINEAR_MIPMAP_LINEAR")
    scene.maxlevel=2

    sh("tmpsnaps/aniso-gf4go-tbl-iso-fsaa.png")

    setANISO(scene)
    scene.maxlevel=1
    scene.faniso=2
    sh("tmpsnaps/aniso-gf4go-tbl-aniso-fsaa.png")


