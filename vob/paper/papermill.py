# 
# Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
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


import vob

import vob.paper.textures
from vob.paper.textures import Textures
from vob.paper.colors import Colors
from vob.paper.texcoords import TexGenXYRepeatUnit
from vob.paper.params import *
import vob.putil.texture

import vob.paper.texcomb_NV1X
import vob.paper.texcomb_NV2X
import vob.paper.texcomb_GL1_1

from org.nongnu.libvob.gl import GL, GLRen, Paper, PaperMill
import java
from java.lang import Math

from org.nongnu.libvob.gl import PaperOptions
paperopt = PaperOptions.instance();

dbg=1
# Discriminate between different renderers.
# These are for debug output only.
if dbg:
    vendor = GL.getGLString("VENDOR")
    renderer = GL.getGLString("RENDERER")
    version = GL.getGLString("VERSION")
    print "GL strings: '%s' '%s' '%s'"%(
        vendor,renderer,version)

#
# Now, go through some questions.
#

# Check which texture operations to use.
if not paperopt.use_opengl_1_1 and GL.hasExtension("GL_NV_texture_shader3"):
    # We can use the general texture shaders.
    # XXX Should check separately for texture_shader2,
    # otherwise it'll be SLOW.
    if dbg: print "Using NV20 texture shaders"
    from vob.paper.texops_NV2X import makeNormalShaderPass, scaleFactor
    numpasses = 2
else:
    if dbg: print "Using unextended OpenGL texture accesses"
    from vob.paper.texops_STD import makeNormalShaderPass, scaleFactor
    numpasses = 3


# Check whether anisotropic filtering is supported
if GL.hasExtension("GL_EXT_texture_filter_anisotropic"):
    if dbg: print "Anisotropic filtering available"
else:
    if dbg: print "Anisotropic filtering not available"


# The size of the texture for an optimized paper
optimizedPaperSize=256
optimizedPaperMaxLevel=7
if not GL.workaroundStupidBuggyAtiDrivers:
    # With ATI drivers, we won't even try to optimize papers.
    # With others, create a stable offscreen surface on which
    # to render the papers
    optimizingWindow = vob.GraphicsAPI.getInstance().createStableOffscreen(optimizedPaperSize, optimizedPaperSize)
    
dbg = 0

def selectRandom(list, gen):
    return list[gen.nextInt(len(list))]

def setSolidPass(pas, color):
    pas.setSetupcode("""
        PushAttrib ENABLE_BIT TEXTURE_BIT
        Disable TEXTURE_2D
        Disable BLEND
        Enable DEPTH_TEST
        DepthFunc LESS
        Color %s %s %s
    """ % color)
    pas.setTeardowncode("""
        PopAttrib
    """)

def setDummyPass(pas):
    pas.setSetupcode("""
    PushAttrib ENABLE_BIT TEXTURE_BIT
    Enable BLEND
    BlendFunc ZERO ONE
    """)
    pas.setTeardowncode("PopAttrib")

# Wrapper of Java Paper class;
# allows arbitrary attributes of paper objects to be set
class PaperHanger(Paper):
    pass

class ThePaperMill(PaperMill):
    def __init__(self):
        self.paperopts = PaperOptions.instance()
        self.newpaperopts = OpenGL11_PaperOptions()
    
    def getPaper(self, seed, passmask=[1,1,1,1,1,1,1],
                 numcolors = 8,
                 minlum = 80,
                 blend = 0,
                 vecs = None):
        pap = PaperHanger()
        pap.reg = Registry()

        rng = java.util.Random(pap.reg.get(regseed, "seed", seed))
        for foo in range(0,20): # eat bad beginning (Java's bad PRNG)
            rng.nextInt()

        colors = Colors(rng.nextInt(), colors = numcolors, minlum = minlum,
                        blend = blend)
        textures = Textures(rng.nextInt())
        rootrep = TexGenXYRepeatUnit(rng, scale = 60 * scaleFactor, vecs = vecs)
        passes = [ { "trans" : 0, "emboss" : 0 },
                   { "trans" : .5, "emboss" : 0 },
                   { "trans" : .9375, "emboss" : 0 },
                   #{ "trans" : 0, "emboss" : 1 },
                   ][0:numpasses]
        seeds = [rng.nextInt(2000000000) for foo in passes]

        # XXX: TODO: these could be passed inside the paper object
        # (and then the textures, too, would be protected from gc)

        pap.setNPasses(len(passes))

        self.newpaperopts.setPassMask(passmask)
        
        for i in range(0, len(passes)):
            #if passmask[i]:
            if self.newpaperopts.getPassMask()[i]:
                passreg = pap.reg.sub("pass"+str(i))
                passreg.get(regseed, "seed", seeds[i])
                self.makePaperPass(passreg,
                                   pap.getPass(i), colors, textures, rootrep,
                                   passes[i]["trans"],
                                   emboss = passes[i]["emboss"])
            else:
                if i == 0:
                    setSolidPass(pap.getPass(i), (1,1,1))
                else:
                    setDummyPass(pap.getPass(i))

        #if dbg: pap.reg.dump()
	pap.repeat = rootrep
        return pap

    def getOptimizedPaper(self, seed, passmask = [1, 1, 1, 1, 1, 1, 1],
	    numcolors = 8, minlum = 80, blend = 0):
	pap = self.getPaper(seed, passmask, numcolors, minlum, blend)

        if not GL.hasExtension("GL_SGIS_generate_mipmap"):
            print "Warning: not returning optimized paper because"
            print "GL_SGIS_generate_mipmap extension is required but not available"
            return pap
        if  GL.workaroundStupidBuggyAtiDrivers:
            print "Warning: not returning optimized paper because"
            print "copyTexImage2D has problems on ATI drivers"
            return pap

	# Now, we render a region.
	v = pap.repeat._getSTVectors()

	vs = optimizingWindow.createVobScene()
	vs.map.put(vob.vobs.SolidBackdropVob(java.awt.Color.red))

	cs1 = vs.coords.ortho(0, 0, 0, 0, 
		optimizedPaperSize+1, optimizedPaperSize+1)
	cs2 = vs.coords.affine(
	    0, 0, 0, 0, 
	     v[0][0], v[0][1], v[1][0], v[1][1]
	)
	vs.map.put(GLRen.createPaperQuad(pap, 0, 0, 1, 1, 1),
	    cs1, cs2)
	optimizingWindow.renderStill(vs, 1)

	tex = GL.createTexture()
	texid = tex.getTexId()
	GL.call("""
	    BindTexture TEXTURE_2D %(texid)s
	    TexParameter TEXTURE_2D TEXTURE_MAX_ANISOTROPY_EXT 2
	    TexParameter TEXTURE_2D  GENERATE_MIPMAP_SGIS TRUE
	    TexParameter TEXTURE_2D  TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
	    TexParameter TEXTURE_2D  TEXTURE_MAG_FILTER LINEAR
	    BindTexture TEXTURE_2D 0
	""" % locals())


	tex.copyTexImage2D(optimizingWindow.getRenderingSurface(), 
			"FRONT", "TEXTURE_2D", 0,
			"RGB5", 0, 0, 
			    optimizedPaperSize, optimizedPaperSize, 0)

	# Apparently, NV drivers 44.96 (maybe others) have some trouble
	# with the 1x1 mipmap getting clobbered.
	# Usually, that wouldn't be a problem, but papers will be viewed
	# 1) at largely different scales
	# 2) blurred for text background
	# so this matters.
	# We shall forbid the use of that mipmap
	tex.setTexParameter("TEXTURE_2D", 
		"TEXTURE_MAX_LEVEL", optimizedPaperMaxLevel)


	if dbg:
	    vob.putil.texture.printTex(tex.getTexId())
	
	npap = PaperHanger()
	npap.setNPasses(1)
	npap.cachedTexture = tex
	npap.addDepend(tex) # Need this for clones to survive

	ppass = npap.getPass(0)


        ppass.setSetupcode("""
            PushAttrib ENABLE_BIT TEXTURE_BIT DEPTH_BUFFER_BIT COLOR_BUFFER_BIT CURRENT_BIT
            Disable BLEND
	    ActiveTexture TEXTURE1
	    Disable TEXTURE_2D
	    ActiveTexture TEXTURE0
	    Enable DEPTH_TEST
	    DepthFunc LESS
	    BindTexture TEXTURE_2D %(texid)s
	    TexEnv TEXTURE_ENV TEXTURE_ENV_MODE REPLACE
	    Color 0 1 0
	    Enable TEXTURE_2D
	    SecondaryColorEXT 0 0 0
	""" % locals())

	ppass.setNTexGens(1)
	# t = pap.repeat.vecs
	t = v
	if dbg:
	    print "T ",t
	ppass.putNormalTexGen(0, 
	    [ t[0][0], t[0][1], 0, 0,
	      -t[1][0], -t[1][1], 0, 0,
	      0,	0,	0, 0,
	      0,	0,	0, 0,])

        ppass.setTeardowncode("""
            PopAttrib
            ActiveTexture TEXTURE0
        """)

	if dbg:
	    print "Ret: ",npap.toString(), npap.getPass(0).getNTexGens()

	return npap


    def selectCombiner(self):
        # Check which combiners to use.
        if not self.paperopts.use_opengl_1_1 and (
            GL.hasExtension("GL_NV_register_combiners") or
            GL.hasExtension("GL_ARB_fragment_program")):
            
            # We have at least a NV10, possibly better.
            # Check the number of general combiners to be sure.
            #maxcomb = GL.getGLFloat("MAX_GENERAL_COMBINERS_NV")[0]
            #if maxcomb < 4:
            if 1: #XXX NV20 version is broken
                # use NV10 version
                if dbg: print "Using NV10 combiners ",maxcomb
                texcomb = vob.paper.texcomb_NV1X
                # from org.nongnu.libvob.paper.texcomb_NV1X import TransparentCombinerPass,DebugCombinerPass
            else:
                # use NV20 version
                if dbg: print "Using NV20 combiners ",maxcomb
                texcomb = vob.paper.texcomb_NV2X
                #from org.nongnu.libvob.paper.texcomb_NV2X import TransparentCombinerPass,DebugCombinerPass
        else:
            # Must use OpenGL 1.1 specified calls.
	    if dbg: print "Using OpenGL 1.1 texenv and blending"
            texcomb = vob.paper.texcomb_GL1_1
            self.paperopts.use_opengl_1_1 = 1


        self.TransparentCombinerPass = texcomb.TransparentCombinerPass
        self.DebugCombinerPass = texcomb.DebugCombinerPass



    def makePaperPass(self, reg, ppass, colors, textures, rootrep, trans = 0, emboss = 0):
        self.selectCombiner()
        seed = reg.get(regseed, "seed")
        rnd = java.util.Random(seed)
        if emboss:
            sh = makeEmbossShaderPass()
        else:
            sh = makeNormalShaderPass()

        types = sh.getTextureTypes()
        for i in range(0, len(types)):
            if types[i] != None:
                t = textures.getPaperTexture(types[i], rnd)

                if emboss:
                    if (i%2) == 0:
                        prev = t
                    else:
                        t = prev
                    
                if dbg: print "Texture"+str(i)+":", t.getName()
                    
                sh.setTexture(i, t)

        if emboss:
            comb = EmbossCombinerPass()
        else:
            #comb = self.DebugCombinerPass()
            comb = self.TransparentCombinerPass()

        code = """
            PushAttrib ENABLE_BIT TEXTURE_BIT DEPTH_BUFFER_BIT COLOR_BUFFER_BIT CURRENT_BIT
            Disable BLEND
        """

        # Performance optimization:
        # Set normal depth testing for the first (non-transparent) pass
        # and "equal" depth testing for the latter (transparent) passes,
        # and don't write to the depth buffer.

        if trans == 0 and emboss == 0:
            code += """
                Enable DEPTH_TEST
                DepthFunc LESS
            """
        else:
            code += """
                Enable DEPTH_TEST
                DepthFunc EQUAL
                DepthMask 0
            """
                
        code += sh.setupCode(rnd)
        code += comb.setupCode(sh.getRGBoutputs(), sh.getRGBoutputscales(),
                               colors, rnd, trans)

        ppass.setSetupcode(code)
        if dbg: print "SETUP: ", code
        ppass.setTeardowncode("""
            PopAttrib
            ActiveTexture TEXTURE0
        """)

        eps = 1E-4
        
        ttyp = sh.getTexgenTypes()
        ppass.setNTexGens(len(ttyp))

        for i in range(0, len(ttyp)):
            if ttyp[i] != None:
                sca2 = 0.8

                if ttyp[i] in ("TexGen2D", "TexGen3D"):
                    data = rootrep.getRelated(rnd).texCoords2D(rnd).getVec()
                elif ttyp[i] == "TexGen2D":
                    data = TexCoords().texCoords2D(rnd).getVec()
                elif ttyp[i] == "TexGen3D":
                    data = TexCoords().texCoords3D(rnd).getVec()
                    
                elif ttyp[i] == "TexGenDotVector":
                    data = [ 0, 0, 0, sca2*rnd.nextGaussian(), 
                             0, 0, 0, sca2*rnd.nextGaussian(),
                             0, 0, 0, sca2*rnd.nextGaussian() ]
                else:
                    assert 0
                if len(data) < 12:
                    for i in (0,0,0,1): data.append(i)
                if emboss :
                    if eps > 0:
                        prev = data
                    else:
                        data = prev
                    if dbg: print "TexGenEmboss"+str(i), data, eps
                    ppass.putEmbossTexGen(i, prev, eps)
                    eps = -eps
                else:
                    if dbg: print "TexGen"+str(i), data
                    ppass.putNormalTexGen(i, data)



class OpenGL11_PaperOptions:
    def __init__(self):
        self.useOpengL11 = None
        self.passMask = None
        self.trueOpenGL11 = None
        self.lastState = None

        # Check which combiners to use.
        if (GL.hasExtension("GL_NV_register_combiners") or
            GL.hasExtension("GL_ARB_fragment_program")):
            self.trueOpenGL11 = 0
            self.useOpenGL11 = 0
        else:
            self.trueOpenGL11 = 1
            self.useOpenGL11 = 1

    def getUseOpenGL11(self):
        return self.useOpenGL11

    def setUseOpenGL11(value):
        if self.trueOpenGL11:
            return
        self.useOpenGL11 = value

    def getPassMask(self):
        return self.passMask

    # XXX this is not very clever
    def setPassMask(self, mask):
        if self.useOpenGL11:
            # XXXX self.passMask = mask[2:] + [0]
            self.passMask = [0,1,1,1]
        else:
            self.passMask = [1,1,1,1]
        #print 'passmask: ', self.passMask


    def restoreState(self):
        if not self.lastState:
            print 'Error: lastState is None'
            return
        
        self.setUseOpenGL11(self.lastState.getUseOpenGL11() )
        self.setPassMask(self.lastState.getPassMask() )
        self.lastState = None

    def saveState(self):
        if self.lastState:
            print 'Error: lastState is not None'
            return

        self.lastState = OpengGL11_PaperOptions()
        self.lastState.setUseOpenGL11(self.getUseOpenGL11() )
        self.lastState.setPassMask(self.getPassMask() )


    
