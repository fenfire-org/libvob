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

# Screenshots for the aniso article of the font scene.
# Currently suitable for gf4go, but aniso level may be increased
# to show the effect better


from __future__ import nested_scopes

from vob.putil.snapshotutil import *


footprint = (19, 32, 67, 99)

def setState(scene):
    # Base shot
    scene.aniso = 0
    scene.shift = 0
    scene.vert = 0
    scene.lodbias = 0

def run():
    scene = vob.demo.aniso.fontscene.Scene()
    setState(scene)


    def sh(file):
	vs = getvs()
	scene.scene(vs)
	shoot(file, vs, *footprint)

    sh("tmpsnaps/aniso-font-trilinear.png")
    scene.aniso = 1
    sh("tmpsnaps/aniso-font-vert2.png")
    scene.vert = 1
    sh("tmpsnaps/aniso-font-horiz2.png")
    scene.super4 = 1
    sh("tmpsnaps/aniso-font-super4.png")
