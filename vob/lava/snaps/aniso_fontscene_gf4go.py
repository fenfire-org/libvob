# (c) Tuomas J. Lukka

# To be run using __GL_FSAA_MODE=4
from __future__ import nested_scopes
import vob
import java
from vob.putil.snapshotutil import *

java.lang.System.setProperty("__GL_FSAA_MODE", "4")

from vob.lava.snaps.aniso_fontscene import footprint, setState

def run():
    scene = vob.demo.aniso.fontscene.Scene()
    setState(scene)

    def sh(file):
	vs = getvs()
	scene.scene(vs)
	shoot(file, vs, *footprint)

    sh("tmpsnaps/aniso-font-fsaa.png")
