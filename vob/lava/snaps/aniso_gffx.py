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


# Screenshots for the aniso article to be taken on gf4go:
# Example footprints of bilinear, trilinear, anisotropic
# and too anisotropic footprints

from __future__ import nested_scopes

from vob.putil.snapshotutil import *

k = 200
# footprint = (0,0,1024,768)
footprint = (700-k, 450-k, 100+2*k, 100+2*k)

def setISO(scene):
    scene.rot = 30
    scene.scale = .09
    scene.ctrx=.463
    scene.ctry=.523
    scene.faniso = 1

def setANISO(scene):
    scene.ctry=.499
    scene.ctrx=.483
    scene.scale = .041
    scene.rot = 5
    scene.aniso = 2.2
    scene.faniso = 1

def run():
    scene = vob.demo.aniso.probe2.Scene()
    setISO(scene)



    def sh(file):
	vs = getvs()
	scene.scene(vs)
	shoot(file, vs, *footprint)

    # Nearest
    scene.maxlevel = 0
    scene.minfilter = scene.minfilters.index("NEAREST")

    sh("tmpsnaps/aniso-gffx-tbl-iso-nearest.png")

    # Trilinear
    scene.minfilter = scene.minfilters.index("LINEAR_MIPMAP_LINEAR")
    scene.maxlevel=3

    sh("tmpsnaps/aniso-gffx-tbl-iso-trilinear.png")

    # Aniso
    scene.faniso=10
    sh("tmpsnaps/aniso-gffx-tbl-iso-aniso.png")

    # Custom
    scene.faniso=10
    scene.super4=1
    scene.maxlevel=2
    sh("tmpsnaps/aniso-gffx-tbl-iso-super4.png")
    scene.super4=0

    # Anisotropic cs

    setANISO(scene)

    scene.faniso=1

    # Anisotropic nearest
    scene.minfilter = scene.minfilters.index("NEAREST")
    scene.maxlevel = 0

    sh("tmpsnaps/aniso-gffx-tbl-aniso-nearest.png")

    # Anisotropic trilinear
    scene.minfilter = scene.minfilters.index("LINEAR_MIPMAP_LINEAR")
    scene.faniso = 1
    scene.maxlevel = 3

    sh("tmpsnaps/aniso-gffx-tbl-aniso-trilinear.png")

    # Anisotropic, with aniso filter
    scene.faniso = 10
    scene.maxlevel = 2

    sh("tmpsnaps/aniso-gffx-tbl-aniso-aniso.png")

    scene.super4=1
    scene.maxlevel = 1
    sh("tmpsnaps/aniso-gffx-tbl-aniso-super4.png")
    scene.super4=0
    

    # ortho text
    scene = vob.demo.aniso.probe2.Scene()

    shifts = [
	(.563, .56),
	(.58, .523),
	(.61, .509),
    ]


    scene.faniso = 1
    scene.rot = 0
    scene.scale = .09
    scene.minfilter = scene.minfilters.index("LINEAR_MIPMAP_LINEAR")

    for i in range(0, len(shifts)):
	scene.ctrx, scene.ctry = shifts[i]
	sh("tmpsnaps/aniso-gffx-ortho-trilinear-%s.png" % i)

    # Anisotext
    scene.faniso = 4
    scene.aniso = 4


    for i in range(0, len(shifts)):
	scene.ctrx, scene.ctry = shifts[i]
	sh("tmpsnaps/aniso-gffx-ortho-stretchsquish-%s.png" % i)





