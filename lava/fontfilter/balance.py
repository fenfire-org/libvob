from opengl import *

init(width = 1600, height = 1200, double = 1)

def scene(a,b):
    s = .8
    
    glClearColor(0,0,0,0)
    glClear(GL_COLOR_BUFFER_BIT)
    glDisable(GL_DEPTH_TEST)
    glBegin(GL_QUAD_STRIP)
    glColor3i(a,a,a)
    glColor3f(0*a/255.,0*a/255.,a/255.)
    glVertex2f(-s,-s)
    glVertex2f(-s,s)
    glColor3i(b,b,b)
    glColor3f(0*b/255.,0*b/255.,b/255.)
    glVertex2f(s,-s)
    glVertex2f(s,s)
    glEnd()


c = 128
d = 0

while 1:

    a = c - (d+1)/2
    b = c + d/2

    scene(a,b)
    swapBuffers()
    print d, a, b

    k,t = waitkey()
    if k == "Left": d += 1
    if k == "Right": d -= 1
    if k == "Up": c += 1
    if k == "Down": c -= 1
    if k == "q": break

