import java.net
from java.lang import System,Thread
import jarray
import vob.color.spaces
from vob.color.spaces import linear_to_monitor
from vob.putil.usertestutil import *
from vob.putil.misc import *
from vob.putil import demowindow
import random
import math

vob.color.spaces.gamma = 2.15
vob.color.spaces.offset = .04

s = java.net.Socket("localhost", 5984)
instream = s.getInputStream()
outstream = s.getOutputStream()

w,h,s = 1280,960,80
#w,h,s = 1600,1200,100
bgcol = (.444,.444,.444)
bgcol = (0,0,0)

trials2 = [
    0, ((.4,)*3, (.55,)*3, (.4,)*3, (.45,)*3, " "),
    0, ((.4,)*3, (.5,)*3, (.4,)*3, (.5,)*3, " "),
    87, ("Tauko...\n\n\n\n       - jatkuu v\xe4lily\xf6nnill\xe4",),
    ]



trial = 0
def getNextTrial():
    global trial,trials2
    if trials2 and trials2[0] == trial:
        t = trials2[1]
        trials2 = trials2[2:]
        return t

    trial += 1
    
    s = ""
    while 1:
        ch = instream.read()
        if ch == -1: return None
        ch = chr(ch)
        if ch == "\n": break
        s += ch

    return eval(s)

def sendResult(r):
    outstream.write(str(r) + "\n")

def lerp(c0, c1, t):
    return [(1-t) * c0[i] + t * c1[i] for i in range(0,3)] 

def stencil(vs):
    cs = vs.coords.ortho(0, 0, 0, 0, s, s)
    vs.put(getDListNocoords("""
    PushAttrib ENABLE_BIT COLOR_BUFFER_BIT STENCIL_BUFFER_BIT
    StencilMask 255
    Clear STENCIL_BUFFER_BIT
    StencilFunc ALWAYS 255 255
    StencilOp REPLACE REPLACE REPLACE
    Enable STENCIL_TEST
    """))
    for x in range(0, w/s):
        for y in range(0, h/s):
            if x + y & 1:
                cs2 = vs.coords.ortho(cs, 0, x, y, 1, 1)
                vs.put(quad(), cs2)
    vs.put(getDListNocoords("""
    PopAttrib
    """))

def scene(vs, cols):
    col0,col1,col2,col3 = map(js, map(linear_to_monitor, cols) )
                                                             
    x1 = .375
    x2 = .625

    x1,x2 = 6/14.0, 8/14.0

    dx = s 
    dy = s
    
    cs = vs.coords.ortho(0, 0, dx, dy, w - 2 * dx, h - 2 * dy)
    col = js(linear_to_monitor(bgcol))
    if 0:
        vs.put(getDList("""
        ClearColor %(col)s 0
        Clear COLOR_BUFFER_BIT
    
        StencilOp KEEP KEEP KEEP
        Enable STENCIL_TEST
        Disable DEPTH_TEST
        
        StencilFunc LESS 1 255
        Begin QUAD_STRIP
        Color %(col0)s
        Vertex 0 0
        Vertex 0 1
        Vertex %(x1)s 0
        Vertex %(x1)s 1
        Color %(col2)s
        Vertex %(x2)s 0
        Vertex %(x2)s 1
        Vertex 1 0
        Vertex 1 1
        End
        
        StencilFunc GEQUAL 1 255
        Begin QUAD_STRIP
        Color %(col1)s
        Vertex 0 0
        Vertex 0 1
        Vertex %(x1)s 0
        Vertex %(x1)s 1
        Color %(col3)s
        Vertex %(x2)s 0
        Vertex %(x2)s 1
        Vertex 1 0
        Vertex 1 1
        End
        
        Disable STENCIL_TEST
        """ % locals()), cs)
    else:
        x1,x2 = 5/14., 9/14.
        vs.put(getDList("""
        ClearColor %(col)s 0
        Clear COLOR_BUFFER_BIT
    
        StencilOp KEEP KEEP KEEP
        Enable STENCIL_TEST
        Disable DEPTH_TEST
        
        StencilFunc LESS 1 255
        Begin QUAD_STRIP
        Color %(col0)s
        Vertex 0 0
        Vertex 0 1
        Color %(col2)s
        Vertex %(x1)s 0
        Vertex %(x1)s 1
        Color %(col0)s
        Vertex %(x2)s 0
        Vertex %(x2)s 1
        Color %(col2)s
        Vertex 1 0
        Vertex 1 1
        End
        
        StencilFunc GEQUAL 1 255
        Begin QUAD_STRIP
        Color %(col1)s
        Vertex 0 0
        Vertex 0 1
        Color %(col3)s
        Vertex %(x1)s 0
        Vertex %(x1)s 1
        Color %(col1)s
        Vertex %(x2)s 0
        Vertex %(x2)s 1
        Color %(col3)s
        Vertex 1 0
        Vertex 1 1
        End
        
        Disable STENCIL_TEST
        """ % locals()), cs)

def scene2(vs, cols):
    cs = vs.coords.ortho(0, 0, 0, 0, s, s)
    vs.put(background((0,0,0)))
    nx = w/s
    ny = h/s

    def f(x,nx=nx,ny=ny):
        if x < nx/2: return 0
        if x == nx/2: return 0.5
        return 1
        

    for x in range(1, nx-1):
        for y in range(1, ny-1):
            cs2 = vs.coords.ortho(cs, 0, x, y, 1, 1)

            t0 = f(x)
            t1 = f(x+1)
            
            i = (x+y)&1
            col0 = js(linear_to_monitor(lerp(cols[0+i], cols[2+i], t0)))
            col1 = js(linear_to_monitor(lerp(cols[0+i], cols[2+i], t1)))

            #if x == 1: col0 = "0 0 0"
            #if x+1 == nx-1: col1 = "0 0 0"

            if col0 == col1:
                vs.put(getDList("""
                Begin QUAD_STRIP
                Color %(col0)s
                Vertex 0 0
                Vertex 0 1
                Color %(col1)s
                Vertex 1 0
                Vertex 1 1
                End
                """ % locals()), cs2)
            else:
                y = s / 4.0
                col0 = linear_to_monitor(lerp(cols[0+i], cols[2+i], t0))
                col1 = linear_to_monitor(lerp(cols[0+i], cols[2+i], t1))
                c0 = int(col0[0] * 255.0 + 0.5) / 256.0 + 0.5 / 1024
                c1 = int(col1[0] * 255.0 + 0.5) / 256.0 + 0.5 / 1024
                texid = tex.getTexId()
                vs.put(getDList("""
                PushAttrib ENABLE_BIT
                Color 1 1 1
                BindTexture TEXTURE_2D %(texid)s
                Enable TEXTURE_2D
                Begin QUAD_STRIP
                TexCoord %(c0)s 0
                Vertex 0 0
                TexCoord %(c0)s %(y)s
                Vertex 0 1
                TexCoord %(c1)s 0
                Vertex 1 0
                TexCoord %(c1)s %(y)s
                Vertex 1 1
                End
                PopAttrib
                """ % locals()), cs2)

def instruct(vs, txt, x = .2, y = .2):
    vs.put(getDListNocoords("""
    PushAttrib ENABLE_BIT
    Color 0 0 0
    Enable BLEND
    BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA
    """))
    cs = vs.coords.ortho(0, 0,  w *x, h * y, 1, 1)
    putMultilineText(vs, cs, txt, h * .1)
    vs.put(getDListNocoords("""PopAttrib"""))


if 0:
    rnd = random.Random(834)
    N = 64
    tex = GL.createTexture()
    print "Building random texture... ", 
    arr = jarray.zeros(N * N, 'b')
    for i in range(0, N * N):
        arr[i] = int(127*linear_to_monitor((.25+.1*rnd.random(),)*3)[0] + .5)
            
    tex.texImage2D(0, "LUMINANCE", N, N, 0, "LUMINANCE", "BYTE", arr)
    tex.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_FILTER", "LINEAR")
    tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAG_FILTER", "LINEAR")
    del arr

    texgen = getDList("""
	TexGen S TEXTURE_GEN_MODE EYE_LINEAR
	TexGen T TEXTURE_GEN_MODE EYE_LINEAR
	TexGen S EYE_PLANE 1 0 0 0
	TexGen T EYE_PLANE 0 0.75 0 0
	Enable TEXTURE_GEN_S
	Enable TEXTURE_GEN_T
        """)

if 1:
    tex = GL.createTexture()
    arr = jarray.zeros(1024 * 4, 'b')

    d = [ (0,1,0,0),
          (0,0,1,1),
          (0,0,0,1),
          (0,0,1,1) ]

    i = 0
    for y in range(0,4):
        for x in range(0, 1020):
            arr[i] = x / 4 + d[y][x & 3]
            i += 1
        for x in range(1020, 1024):
            arr[i] = 255
            i += 1
        
            
    tex.texImage2D(0, "LUMINANCE", 1024, 4, 0, "LUMINANCE", "UNSIGNED_BYTE", arr)
    tex.setTexParameter("TEXTURE_2D", "TEXTURE_WRAP_S", "CLAMP")
    tex.setTexParameter("TEXTURE_2D", "TEXTURE_WRAP_T", "REPEAT")
    tex.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_FILTER", "NEAREST")
    tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAG_FILTER", "NEAREST")
    del arr


def run():
    outfile = open(",,RES", "a")
    outfile.write("---\n")

    demowindow.w.setLocation(0,0,w,h)


    vs0 = getvs()
    vs0.put(getDListNocoords("""
    ClearColor %s 0
    Clear COLOR_BUFFER_BIT
    AlphaFunc GREATER 0
    Enable ALPHA_TEST
    Disable DEPTH_TEST
    """ % js(linear_to_monitor((.25,.25,.25)))))
    if 0:
        cs = vs0.coords.ortho(0, 0, s, s, w-2*s, h-2*s)
        vs0.put(getDList("""
        Color 0 0 0
        LineWidth 2.0
        Begin LINE_LOOP
        Vertex 0 0
        Vertex 0 1
        Vertex 1 1
        Vertex 1 0
        End
        """), cs)
    render(vs0)

    if 0:
        noisevs = getvs()
        cs = noisevs.coords.ortho(0, 0, 0, 0, w, h)
        noisecs = noisevs.coords.ortho(cs, 0, 0, 0, 1, 1)
        noisevs.put(texgen, noisecs)
        noisevs.put(getDList("""
        PushAttrib TEXTURE_BIT
        BindTexture TEXTURE_2D %s
        Enable TEXTURE_2D
        Color 1 1 1 1
        Begin QUADS
        Vertex 0 0
        Vertex 0 1
        Vertex 1 1
        Vertex 1 0
        End
        PopAttrib
        """ % tex.getTexId()), cs)

    answer = [ "Control_L", "Control_R" ]
    while 1:
        renderOnly(vs0)
        vs = getvs()
        instruct(vs, "Aloita v\xe4lily\xf6nnill\xe4", x = .28)
        instruct(vs, "<-- vastausn\xe4pp\xe4imet -->", y = .7)
        x0 = (.1,.8)[answer[0][-1] == "R"]
        x1 = (.1,.8)[answer[1][-1] == "R"]
        instruct(vs, "Ei", x = x0, y = .85)
        instruct(vs, "Kyll\xe4", x = x1, y = .85)
        render(vs)
        k,t = waitkey()
        if k == answer[0]:
            answer = answer[::-1]
        if k == " ": break

    render(vs0)
    t0 = java.lang.System.currentTimeMillis()
    while 1:
        cols = getNextTrial()
        if cols == None: break
        t = t0
        print cols

        txt = None
        if len(cols) > 4:
            cols, txt = cols[:4], cols[4]
        if len(cols) == 1:
            cols, txt = None,cols[0]

        vs = getvs()
        #stencil(vs)
        #scene(vs, cols)
        if cols:
            scene2(vs, cols)
        else:
            renderOnly(vs0)
        if txt:
            instruct(vs, txt)
        renderOnly(vs)

        timeScrub()

        t = 2000 - (java.lang.System.currentTimeMillis() - t0)
        if t > 0: Thread.sleep(t)

        swapBuffers()
        t = java.lang.System.currentTimeMillis()
        print t - t0
        
        if not cols:
            print "waiting for space"

            t0 = t
            while 1:
                k,t = waitkey(t0 = t0)
                if t < 1000: continue
                if k == " ": break
                
            t0 = java.lang.System.currentTimeMillis()

            render(vs0)
            continue
        
        t0 = t
        #timeout = 6000
        timeout = None
        while 1:
            k,t = waitkey(t0 = t0, timeout = timeout)
            #print "(%s)", k
            if t < 100: continue
            
            #if k == None:
            #    res = 0
            #    break
            #elif k == " ":
            #    res = 1
            #    break
            if k == answer[1]:
                res = 1
                break
            elif k == answer[0]:
                res = 0
                break
            if k == "Escape":
                renderOnly(vs)
                tmp = getvs()
                instruct(tmp, "Paused at trial %s" % trial)
                renderOnly(tmp)
                swapBuffers()
                while 1:
                    k,t = waitkey()
                    t0 += t
                    if k == "Escape": break
                render(vs)
                continue

        if 1:
            render(vs0)
        else:
            noisevs.coords.setOrthoParams(noisecs, 0, rnd.random(), rnd.random(), 2, 2)
            render(noisevs)
        
        outfile.write("%s  %s  %s %s\n" % (
            t0,
            js(reduce(lambda x,y:x+y, cols)),
            res, t) )
        outfile.flush()

        if not txt:
            sendResult(res)

        t0 += t
        
    System.exit(0)
