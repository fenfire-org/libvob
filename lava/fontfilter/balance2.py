from Numeric import *
from opengl import *

init(width = 1600, height = 1200, double = 1)


def initDither():
    a = zeros((4, 1024), UnsignedInt8)

    b = (arange(0, 1024) / 4).astype(UnsignedInt8)
    b.shape = (1, 1024)

    a += b

    d = array( [ (0,1,0,0),
                 (0,0,1,1),
                 (0,0,0,1),
                 (0,0,1,1) ], UnsignedInt8 )
    
    for i in range(0,1020,4):
        a[:,i:i+4] += d

    if 0:
        for i in range(0,4):
            print a[i,:16],a[i,-10:]
        for i in range(0,4):
            print a[i,:16],a[i,-10:]


    tex = glGenTextures(1)

    glBindTexture(GL_TEXTURE_2D, tex)

    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 1024, 4, 0, GL_LUMINANCE,
                 GL_UNSIGNED_BYTE, a.tostring())
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)


def scene(a,b):
    s = .8

    w,h = getwinsize()

    y = h * .8 / 4

    glClearColor(0,0,0,0)
    glClear(GL_COLOR_BUFFER_BIT)
    glDisable(GL_DEPTH_TEST)

    if dither:
        glColor3f(1,1,1)
        glEnable(GL_TEXTURE_2D)
        glBegin(GL_QUAD_STRIP)
        glTexCoord2f(a/256., 0)
        glVertex2f(-s,-s)
        glTexCoord2f(a/256., y)
        glVertex2f(-s,s)
        glTexCoord2f(b/256., 0)
        glVertex2f(s,-s)
        glTexCoord2f(b/256., y)
        glVertex2f(s,s)
        glEnd()
    else:
        glDisable(GL_TEXTURE_2D)
        glBegin(GL_QUAD_STRIP)
        glColor3f(a/255., a/255., a/255.)
        glVertex2f(-s,-s)
        glVertex2f(-s,s)
        glColor3f(b/255., b/255., b/255.)
        glVertex2f(s,-s)
        glVertex2f(s,s)
        glEnd()

initDither()

c = 128
d = 0

dither = 1

while 1:

    a = c - d/2
    b = c + d/2

    scene(a,b)
    swapBuffers()
    print d, a, b

    k,t = waitkey()
    if k == "Left": d += .25
    if k == "Right": d -= .25
    if k == "Up": c += 1
    if k == "Down": c -= 1
    if k == "d": dither = not dither
    if k == "q": break

