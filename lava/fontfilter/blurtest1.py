import Image
import ImageDraw
from opengl import *
from fontfilter import FontFilter
from util import *
import fontmap
import imagegen
from random import choice

w,h = 1280, 960
tilew, tileh = 1024,768
dists = [1,2,4,999]
blurs = [0,1,2,6]

gl = init(width = w, height = h, double = 1)

glClear(GL_COLOR_BUFFER_BIT)
glDisable(GL_DEPTH_TEST)
swapBuffers()

print "gl init done"


font = fontmap.getFont("Helvetica", 12)


c0 = .25
bg = load("bg2.png")[:,:,0].copy()
bg.shape += (1,)
bg = where(bg == 1, 1.0, 0.0)
b = (1 - sum(flat(bg)) / size(bg))
bg = where(bg == 1, 1.0, ((c0 + 1) / 2 - (1 - b) ) / b)

bgcol = ((0.5 * (1+c0))**(1.0/gamma),)*4

bgims = [fft_blur(bg, blur, axes=(0,1)) for blur in blurs]
bgims = [tileImage(array2image(b), tilew, tileh) for b in bgims]

starim = array2image(bg)
draw = ImageDraw.Draw(starim)
txt = "|||||||||||||||"
s = font.getsize(txt)
p = (starim.size[0]/2 - s[0]/2,
     starim.size[1]/2)
print p
draw.text(p, txt, font = font, fill = 0)


def drawImage(im):
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
    glPixelZoom(1,-1)

    x,y= (-im.size[0] / float(w),
          im.size[1] / float(h))
    if x < -1: x = -1
    if y > 1: y = 1
    glRasterPos2f(x,y)

    if im.mode == "RGBA": format = GL_RGBA
    elif im.mode == "RGB": format = GL_RGB
    elif im.mode == "L": format = GL_LUMINANCE

    glDrawPixels(im.size[0], im.size[1],
                 format, GL_UNSIGNED_BYTE, im.tostring())


def drawFrame():
    glColor3f(bgcol)
    glBegin(GL_QUAD_STRIP)
    glVertex2f(-1.0,-1.0)
    glVertex2f(-0.6,-0.6)

    glVertex2f(+1.0,-1.0)
    glVertex2f(+0.6,-0.6)

    glVertex2f(+1.0,+1.0)
    glVertex2f(+0.6,+0.6)

    glVertex2f(-1.0,+1.0)
    glVertex2f(-0.6,+0.6)

    glVertex2f(-1.0,-1.0)
    glVertex2f(-0.6,-0.6)
    glEnd()

d,b=1,1
trial = 0
while 1:
    print "Trial", trial 
    drawImage(bgims[0])
    drawImage(starim)
    #drawFrame()
    swapBuffers()

    txt = imagegen.wordMatrix(font, 0)
    f = FontFilter(bg, txt)

    d = choice(dists)
    b = choice(blurs)

    f.update(d, b, 0, 0)
    im = array2image(f.get())

    glClearColor(bgcol[0],bgcol[1],bgcol[2],0)
    glClear(GL_COLOR_BUFFER_BIT)
    if d < 999:
        drawImage(bgims[0])
    else:
        i = blurs.index(b)
        print i
        drawImage(bgims[i])
    
    drawImage(im)
    #drawFrame()
    swapBuffers()

    k,t = waitkey()

    if k == "q": break
    if k == "Left": d *= .5
    if k == "Right": d *= 2
    if k == "Up": b *= .5
    if k == "Down": b *= 2

    trial += 1

