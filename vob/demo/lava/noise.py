# (c) Janne V. Kujala

import org
import vob
from vob.putil.misc import *
from vob.putil.demokeys import *
from vob.putil.nvcode import parseCombiner
import jarray
import random
import math
from vob.paper.texcache import getCachedTexture

GL = vob.gl.GL
GLRen = vob.gl.GLRen


N = 512
N = 64

if 1:
    global tex
    tex = GL.createTexture()

    print "Building random texture... ", 
    arr = jarray.zeros(N * N, 'b')
    for i in range(0, N * N):
        arr[i] = random.randint(0, 255)
            
    tex.texImage2D(0, "LUMINANCE", N, N, 0, "LUMINANCE", "UNSIGNED_BYTE", arr)
    arr = None
    print "done"
else:
    tex = getCachedTexture([512,512,0,1,"LUMINANCE", "LUMINANCE", "noise",
	[
	"freq", "20",
	"type", "fBm",
	"bias", ".5",
	"scale", ".5",
	"fbmgain", ".9",
	"fbmoct", "8",
	"fbmlacu", "1.892",
	]])


tex.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_FILTER", "NEAREST")
tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAG_FILTER", "NEAREST")


texgen = getDList("""
	TexGen S TEXTURE_GEN_MODE EYE_LINEAR
	TexGen T TEXTURE_GEN_MODE EYE_LINEAR
	TexGen S EYE_PLANE 1 0 0 0
	TexGen T EYE_PLANE 0 1 0 0
	Enable TEXTURE_GEN_S
	Enable TEXTURE_GEN_T
        Enable TEXTURE_2D
        BindTexture TEXTURE_2D %s
        """ % (tex.getTexId()))


def randCS():
    m = 1
    n = 100
    cx, cy = m * random.randint(0, n * N-1), m * random.randint(0, n * N-1)
    x = (1 - 2 * (random.random() < .5)) * N * m * n
    y = (1 - 2 * (random.random() < .5)) * N * m * n

    if 0:
        a = random.random() * math.pi * 2
        x *= .71
        y *= .71
        c, s = math.cos(a), math.sin(a)
        return (cx, cy,
                c * x - s * y,
                c * y + s * x,
                s * x + c * y,
                s * y - c * x,
                )
    #return cx, cy, 0, 2 * N, 2 * N, 0
    if random.random() < .5:
        return cx, cy, x, 0, 0, y
    else:
        return cx, cy, 0, x, y, 0

class Scene:
    """Noise"""
    def __init__(self):
        self.vs = None
    	self.key = KeyPresses(
            self, 
            SlideLin("x", 0, 10, "x coord", "Left", "Right"),
            SlideLin("y", 0, 10, "y coord", "Up", "Down"),
	)
    def scene(self, vs):
        org.nongnu.libvob.AbstractUpdateManager.chg()
        if self.vs != None:
            cx, cy, x_x, x_y, y_x, y_y = randCS()
            self.vs.coords.setAffineParams(self.cs1, 0, cx, cy, x_x, x_y, y_x, y_y)

            cx, cy, x_x, x_y, y_x, y_y = randCS()
            self.vs.coords.setAffineParams(self.cs2, 0, cx, cy, x_x, x_y, y_x, y_y)
            
            return self.vs
        self.vs = vs
        print "Scene"
        
        cx, cy, x_x, x_y, y_x, y_y = randCS()
        self.cs1 = vs.affineCS(0, "1", 0, cx, cy, x_x, x_y, y_x, y_y)

        cx, cy, x_x, x_y, y_x, y_y = randCS()
        self.cs2 = vs.affineCS(0, "2", 0, cx, cy, x_x, x_y, y_x, y_y)
        
	vs.put( background((0, 0, 0)))

        vs.put( getDListNocoords("""
        PushAttrib ENABLE_BIT TEXTURE_BIT CURRENT_BIT
        ActiveTexture TEXTURE1
        """))
        vs.put(texgen, self.cs2)
        vs.put(getDListNocoords("""
        ActiveTexture TEXTURE0
        """))
        vs.put(texgen, self.cs1)


        vs.put(getDListNocoords(parseCombiner("""
        Enable REGISTER_COMBINERS_NV

        SPARE0 = ((1 - 2 * TEX0) * (1 - 2 * TEX1) + (1.0)) * 0.5

        color = TEX0
        alpha = 1
        
        
        """)))

        vs.map.put( getDList("""
        Color 1 1 1
        Begin QUAD_STRIP
        Vertex 0 0
        Vertex 1600 0
        Vertex 0 1200
        Vertex 1600 1200
        End
        """), 0 )

        vs.put( getDListNocoords("""
        PopAttrib
        """))
        
        return vs

