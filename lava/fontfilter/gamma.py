from opengl import *

import color.spaces
from color.spaces import linear_to_monitor

init(width = 1024, height = 768,double = 1)

N=48

if 1:
    stripe = glGenLists(1)
    glNewList(stripe, GL_COMPILE)
    glLineWidth(1.0)
    glBegin(GL_LINES)
    for i in range(0,N/2):
        glVertex2f(0, 2*i)
        glVertex2f(.3333 , 2*i)
    glEnd()
    glEndList()

    stripe2 = glGenLists(1)
    glNewList(stripe2, GL_COMPILE)
    glLineWidth(1.0)
    glBegin(GL_LINES)
    for i in range(0,N/3):
        glVertex2f(.6666, 3*i+1)
        glVertex2f(1, 3*i+1)
    glEnd()
    glEndList()

    stripe3 = glGenLists(1)
    glNewList(stripe3, GL_COMPILE)
    glLineWidth(1.0)
    glBegin(GL_LINES)
    for i in range(0,N):
        if i % 3 != 1:
            glVertex2f(.6666, i)
            glVertex2f(1, i)
    glEnd()
    glEndList()

    block = glGenLists(1)
    glNewList(block, GL_COMPILE)
    glBegin(GL_QUAD_STRIP)
    glVertex2f(.3333, 0)
    glVertex2f(.6666, 0)
    glVertex2f(.3333, 2*N)
    glVertex2f(.6666, 2*N)
    glEnd()
    glEndList()


def scene():
    w,h = getwinsize()
    
    glClearColor(0,0,0,1)
    glClear(GL_COLOR_BUFFER_BIT)
    glDisable(GL_DEPTH_TEST)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glTranslatef(-1,1,0)
    glScalef(2.0/w, -2.0/h, 1)
    
    glMatrixMode(GL_MODELVIEW)

    color = linear_to_monitor

    css = [(0, y-0.5, w, 1) for y in range(0,h,N)]
    i = 0
    for cs in css:
        glPushMatrix()

        glTranslatef(cs[0], cs[1], 0)
        glScalef(cs[2], cs[3], 1)
        
        col = [(1,1,1),(0,1,0),(1,0,0),(0,0,1)][i&3]
        f = .5 * (1 - (i / 4) * .2)**3
        i += 1
        col = [c * f for c in col]
        col1 = [c * 2 for c in col]
        col2 = [c * 3 for c in col]
        col3 = [c * 3 / 2 for c in col]

        if (col1[0] <= 1.0001 and
            col1[1] <= 1.0001 and
            col1[2] <= 1.0001):
            glColor3f(color(col1))
            glCallList(stripe)

        if (col2[0] <= 1.0001 and
            col2[1] <= 1.0001 and
            col2[2] <= 1.0001):
            glColor3f(color(col2))
            glCallList(stripe2)
        elif (col3[0] <= 1.0001 and
              col3[1] <= 1.0001 and
              col3[2] <= 1.0001):
            glColor3f(color(col3))
            glCallList(stripe3)

        glColor3f(color(col))
        glCallList(block)

        glPopMatrix()

    swapBuffers()


color.spaces.offset = 0

while 1:
    scene()

    k,t = waitkey()
    if k == "q": break
    if k == "Up": color.spaces.gamma += .05
    if k == "Down": color.spaces.gamma -= .05
    if k == "Right": color.spaces.offset += .01
    if k == "Left": color.spaces.offset -= .01
    if color.spaces.offset < 0:
        color.spaces.offset = 0
    print color.spaces.gamma, color.spaces.offset
