# (c) Tuomas J. Lukka

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


