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


from __future__ import nested_scopes

import java
import vob
import org
from test.tools.gfx import *
from vob.putil.misc import *



def setUp_imzm(imzm):
    global indirectMipzipManager
    global compressedTextures
    indirectMipzipManager = imzm
    compressedTextures = 1


def testMipmapSwitches():
    """Test with separate copies of a single image that
    the image gets loaded to the right levels of detail.

    XXX Make this test not use compressed textures...
    """

    if compressedTextures:
	indirectMipzipManager.init("COMPRESSED_RGB_S3TC_DXT1_EXT", 2048, 2048)
	texdir = ""
    else:
	indirectMipzipManager.init("RGBA", 2048, 2048)
	texdir = "rgba/"

    indirectMipzipManager.glBackground = vob.util.ExplicitBackground();
    # indirectMipzipManager.background = vob.util.ExplicitBackground();
    indirectMipzipManager.background = vob.util.ThreadBackground();

    indirectMipzipManager.setAllocations([
	1, 0, 0,2, 0,     0, 0, 0, 0, 0,   0, 0])

    virtualTextures = [
	org.nongnu.libvob.gl.virtualtexture.VirtualTexture(vob.gl.GL.createIndirectTexture(), 
	    vob.gl.MipzipFile(java.io.File("testdata/%s%s.png.mipzip" % (texdir, i))))
	for i in ("blue", "red", "yellow", "cyan")]

    def vt(*ar):
	def mvt(i):
	    if i == None: return None
	    return virtualTextures[i]
	return [mvt(i) for i in ar]

    def mkvs(i):
	vs = getvs()
	vs.put(background((.5,.6,.6)))

	paper = vob.gl.Paper()
	paper.setNPasses(1)
	pas = paper.getPass(0)
	pas.setSetupcode("""
	    Color 1 1 1
	    Enable TEXTURE_2D
	    TexEnv TEXTURE_ENV TEXTURE_ENV_MODE REPLACE
	""")
	pas.setTeardowncode("""
	    Disable TEXTURE_2D
	    Color 0 0 0
	""")
	pas.setNIndirectTextureBinds(1)
	pas.putIndirectTextureBind(0, 
	    "TEXTURE0_ARB", "TEXTURE_2D", virtualTextures[i].indirectTexture)
	pas.setNTexGens(1)
	pas.putNormalTexGen(0, [
	    1, 0, 0, 0,
	    0, 1, 0, 0,
	    0, 0, 1, 0,
	    0, 0, 0, 1
	])

	# Test patches for all 12 mipmap levels: 10x10 pixels drawn from each.

	for i in range(0, 12):
	    pq = GLRen.createFixedPaperQuad(paper, 0, 0, 40.0 / (2048 >> i), 
			40.0 / (2048 >> i), 0, 1, 1, 1)
	    cs = vs.orthoCS(0, str(i), 0, 40*i, 50, 2048>>i, 2048>>i)
	    vs.put(pq, cs)

	return vs

    def checkvs(i, rgb):
	render(vss[i])
	# java.lang.Thread.sleep(500)
	for i in range(0, 10): # XXX ??
	    checkAvgColor(1 + 40*i, 1 + 50, 5, 5, rgb)


    vss = [mkvs(i) for i in range(0, len(virtualTextures))]

    global scrunnableAssertError
    scrunnableAssertError = 0

    class SCRunnable(java.lang.Runnable):
	def __init__(self, list):
	    self.list = list
	def run(self):
	    global scrunnableAssertError
	    print "SCRUNNABLE!"
	    indirectMipzipManager.setSlotContents(self.list)
	    print "SCRUNNABLE DONE!"
	    for level in range(0, len(self.list)):
		for index in range(0, len(self.list[level])):
		    if self.list[level][index] == None: continue
		    if level != indirectMipzipManager.getSlotLevel(self.list[level][index]):
			scrunnableAssertError = 1
			print "SCRUN ERROR L", level, index, indirectMipzipManager.getSlotLevel(self.list[level][index])

    def checkfilt(i):
	tex = virtualTextures[i].indirectTexture.getTexture()
	str = GL.getGLTokenString(
		int(tex.getParameter("TEXTURE_MIN_FILTER")[0]))
	assert str == "LINEAR_MIPMAP_LINEAR"

    indirectMipzipManager.setDefaultTexParameters([
	"TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR"
    ])

    # Do it in the Bg thread to make sure it doesn't call GL stuff
    indirectMipzipManager.background.addTask(
	SCRunnable(
	    [
	vt(0),
	[],
	[],
	vt(1,2),
	[], [], [], [], [], [], [], []
	]), -500)

    def gb(i):
	"""Get the base level of the given texture.
	100 = not loaded.
	"""
	tex = virtualTextures[i].indirectTexture.getTexture()
	if tex == None: return 100
	return tex.getParameter("TEXTURE_BASE_LEVEL")[0]

    def totlev(i, l):
	"""Whether the virtualtexture i is totally loaded
	at level l.
	"""
	baselevel = gb(i)
	print "totlev: baselevel = ",baselevel
	tex = virtualTextures[i].indirectTexture.getTexture()
	if tex == None: return 0
	wid = tex.getLevelParameter(int(baselevel), "TEXTURE_WIDTH")[0]
	if wid != (2048 >> l): return 0
	return 1

    done = 0
    for i in range(0, 10000):
	indirectMipzipManager.glBackground.performOneTask()
	java.lang.Thread.sleep(500)
	# indirectMipzipManager.background.performOneTask()

	if totlev(0,0) and totlev(1,3) and totlev(2,3) :
	    done = 1
	    break
    assert done, (gb(0), gb(1), gb(2), gb(3))
    checkvs(0, (0, 0, 255))
    checkvs(1, (255, 0, 0))
    checkvs(2, (255, 255, 0))
    # checkvs(3, (0, 0, 0))
    for i in (0,1,2): checkfilt(i)

    indirectMipzipManager.background.addTask(
	SCRunnable([
	vt(1),
	[],
	[],
	vt(0, 3),
	[], [], [], [], [], [], [], []
	]), -500)
	

    done = 0
    for i in range(0, 10000):
	indirectMipzipManager.glBackground.performOneTask()
	# indirectMipzipManager.background.performOneTask()

	if totlev(0,3) and totlev(1,0) and totlev(3, 3) :
	    done = 1
	    break
    assert done, (gb(0), gb(1), gb(2), gb(3))

    checkvs(0, (0, 0, 255))
    checkvs(1, (255, 0, 0))
    # checkvs(2, (0, 0, 0))
    checkvs(3, (0, 255, 255))

    # Check that empty slots work as well...

    indirectMipzipManager.background.addTask(
	SCRunnable([
	vt(3),
	[],
	[],
	vt(2, None),
	[], [], [], [], [], [], [], []
	]), -500)
	

    done = 0
    for i in range(0, 10000):
	indirectMipzipManager.glBackground.performOneTask()
	# indirectMipzipManager.background.performOneTask()

	if totlev(3,0) and totlev(2,3) :
	    done = 1
	    break
    assert done, (gb(0), gb(1), gb(2), gb(3))

    # checkvs(0, (0, 0, 255))
    # checkvs(1, (255, 0, 0))
    checkvs(2, (255, 255, 0))
    checkvs(3, (0, 255, 255))

    # XXX !!!
    if isinstance(indirectMipzipManager, 
	    org.nongnu.libvob.gl.virtualtexture.FixedIndirectMipzipManager):
		return

    indirectMipzipManager.setSlotContents_synchronously([
	vt(0),
	[],
	[],
	vt(1,2),
	[], [] ,[], [], [], [], [], [],
	])

    assert totlev(0,0)
    assert totlev(1,3)
    assert totlev(2,3)
    assert virtualTextures[3].indirectTexture.getTexture() == None

    if scrunnableAssertError:
	assert 0 == 1

def testSingleImageLoads():
    return

    indirectMipzipManager.init("COMPRESSED_RGB_S3TC_DXT1_EXT", 1024, 1024)

    indirectMipzipManager.glBackground = vob.util.ExplicitBackground();
    indirectMipzipManager.background = vob.util.ExplicitBackground();

    virtualTexture = org.nongnu.libvob.gl.virtualtexture.VirtualTexture(
	vob.gl.MipzipFile(java.io.File("testdata/modularspace.mipzip")))


    indirectMipzipManager.setAllocations([
	1, 0, 0,0, 0,     0, 0, 0, 0, 0,   0])

    indirectMipzipManager.setSlotContents([
	[virtualTexture], [], [], [], [], [], [], [], [], [], []])
    done = 0
    for i in range(0, 10000):
	indirectMipzipManager.glBackground.performOneTask()
	indirectMipzipManager.background.performOneTask()

	if virtualTexture.indirectTexture.getTexture().getParameter("TEXTURE_BASE_LEVEL")[0] == 0: 
	    done = 1
	    break

    assert done


    vs = getvs()
    vs.put(background((.5,.6,.6)))
    vs.put(getDListNocoords("""
	Color 1 1 1
	Enable TEXTURE_2D
	"""))
    vs.put(
	quad(virtualTexture.indirectTexture.getTexture().getTexId()),
	vs.orthoCS(0,"A", 0, 100, 100, 200, 200))
    render(vs)

    
    assert 0 == 1

# vim: set syntax=python :
