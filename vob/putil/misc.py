# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
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

dbg = 0

import java

import vob
from org.nongnu.libvob.gl import GL, GLRen, GLCache

def background(rgb):
    return vob.vobs.SolidBackdropVob(java.awt.Color(float(rgb[0]), float(rgb[1]), float(rgb[2])))

def texbindcode(texid):
    if texid:
	return "BindTexture TEXTURE_2D %s"%texid
    return ""

def coloredQuad(rgb, texid=None, texcoord1 = 1):
    texcode = texbindcode(texid)
    r,g,b = rgb
    return getDList("""
	PushAttrib TEXTURE_BIT
	"""+texcode+"""
	Color %(r)s %(g)s %(b)s
	Begin QUADS
	TexCoord 0 0
	Vertex 0 0
	TexCoord 0 %(texcoord1)s
	Vertex 0 1
	TexCoord %(texcoord1)s %(texcoord1)s
	Vertex 1 1
	TexCoord %(texcoord1)s 0
	Vertex 1 0
	End
	PopAttrib
    """%locals())

def quad(texid=None):
    texcode = texbindcode(texid)
    return getDList("""
	PushAttrib TEXTURE_BIT
	"""+texcode+"""
	Begin QUADS
	TexCoord 0 0
	Vertex 0 0
	TexCoord 0 1
	Vertex 0 1
	TexCoord 1 1
	Vertex 1 1
	TexCoord 1 0
	Vertex 1 0
	End
	PopAttrib
    """)

def partialquad(x0, y0, x1, y1, texid = None):
    texcode = texbindcode(texid)
    return getDList("""
	PushAttrib TEXTURE_BIT
	"""+texcode+"""
	Begin QUADS
	TexCoord %(x0)s %(y0)s
	Vertex -1 -1
	TexCoord %(x0)s %(y1)s
	Vertex -1 1
	TexCoord %(x1)s %(y1)s
	Vertex 1 1
	TexCoord %(x1)s %(y0)s
	Vertex 1 -1
	End
	PopAttrib
    """ % locals())

textstyle = None

def getText(text):
    global textstyle
    if not textstyle:
	textstyle = vob.impl.gl.GLTextStyle.create("sans", 0, 1)
    return vob.vobs.TextVob(textstyle, text, 1)

def putText(vs, cs1, text, color = None, x = 0, y = 0, z = 0, h = 1, key = None):
    if color != None:    
        vs.put( getDListNocoords("Color " + js(color)))
    vs.put( getDListNocoords("""
        PushAttrib ENABLE_BIT
	Disable ALPHA_TEST
        Disable DEPTH_TEST
	Enable TEXTURE_2D
	Enable BLEND
	BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA
    """))

    t = getText(text)

    cs = vs.coords.ortho(cs1, z, x, y-h, h, h)
    vs.matcher.add(cs1, cs, key or str(text))

    vs.map.put(t, cs)
    
    vs.put( getDListNocoords("""
        PopAttrib
    """))

def putMultilineText(vs, cs, text, rowheight):
    y = 0
    for line in text.split("\n"):
	c = vs.orthoCS(cs, str(y), 0, 0, y, rowheight, rowheight)
	vs.map.put(getText(line), c)
	y += rowheight

def js(list):
    return " ".join([str(el) for el in list])

def getDList(s):
    return GLCache.getCallListCoorded(s)

def getDListNocoords(s):
    return GLCache.getCallList(s)

imgs = { }
texs = { }
def getTex(s):
    if not imgs.has_key(s):
	print "Load image ",s
	imgs[s] = GL.createImage(s)
	texs[s] = GL.createTexRect(imgs[s])
    return texs[s]

if not locals().has_key("textures"):
    textures = {}
	
def retexture():
    global textures
    textures = {}

# XXX naming
def getTexture(*args):
    key = str(args)
    if not textures.has_key(key):
	textures[key] = GL.createTexture()
        if dbg:
            print "Generating texture: ", args
	res = textures[key].shade(*args)
        if dbg:
            print "SHADER: ", res
    return textures[key]



def floats(start, end, n):
    return [float(x) * (end-start) / n + start 
	for x in range(0,n+1)]
