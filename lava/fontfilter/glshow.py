import sys
import Image
from opengl import *
from time import time, sleep

images = sys.argv[1:]

i = 0
print images

size = 1024,768
#size = 1600,1200

def drawImage(im):
    print im.size, im.getbands()

    glClearColor(0,0,0,0)
    glClear(GL_COLOR_BUFFER_BIT)
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
    glPixelZoom(1,-1)

    x,y= (-im.size[0] / float(size[0]),
          im.size[1] / float(size[1]))
    if x < -1: x = -1
    if y > 1: y = 1
    glRasterPos2f(x,y)

    if im.mode == "RGBA": format = GL_RGBA
    elif im.mode == "RGB": format = GL_RGB
    elif im.mode == "L": format = GL_LUMINANCE

    glDrawPixels(im.size[0], im.size[1],
                 format, GL_UNSIGNED_BYTE, im.tostring())


gl = init(width = size[0], height = size[1], double = 1)


im = Image.open(images[0])
im.load()

while 1:
    drawImage(Image.open(images[i]))
    swapBuffers()

    #flushevents()
    k, t = waitkey()
    print k, t
    if k == "Next":
        i = (i + 1) % len(images)
    elif k == "Prior":
        i = (i + len(images) - 1) % len(images)
    elif k == "q":
        break
    



