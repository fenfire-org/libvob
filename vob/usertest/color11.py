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

practice = [
    #((.4,)*3, (.5,)*3, (.4,)*3, (.5,)*3, "Molemmat v\xe4rit tasaisia"),
    #((.4,)*3, (.55,)*3, (.4,)*3, (.45,)*3, "Nyt on eroa"),
    #((.4,)*3, (.4,)*3, (.4,)*3, (.4,)*3, "Molemmat v\xe4rit tasaisia"),
    #((.38,)*3, (.42,)*3, (.42,)*3, (.38,)*3, "Nyt on eroa"),
    ]

def getNextTrial():
    global practice
    if practice:
        t = practice[0]
        practice = practice[1:]
        return t
    
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

parity = 0
def scene(vs, cols):
    cs = vs.coords.ortho(0, 0, 0, 0, s, s)
    nx = w/s
    ny = h/s

    global parity
    pariry = 1 - parity

    for x in range(0, nx):
        for y in range(0, ny):
            cs2 = vs.coords.ortho(cs, 0, x, y, 1, 1)

            if (x+y&1) == parity:
                col = cols[2]
            else:
                if random.random() < .5:
                    col = cols[0]
                else:
                    col = cols[1]

            vs.put(coloredQuad(linear_to_monitor(col)), cs2)

def instruct(vs, txt):
    putText(vs, 0, txt, x = w*.2, y = h*.2, h = h * .1, color = (0,0,0))


def run():
    outfile = open(",,RES", "a")
    outfile.write("---\n")

    demowindow.w.setLocation(0,0,w,h)


    vs0 = getvs()
    vs0.put(getDListNocoords("""
    ClearColor %s 0
    Clear COLOR_BUFFER_BIT
    """ % js(linear_to_monitor((.25,.25,.25)))))
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

        vs = getvs()
        scene(vs, cols)
        if txt:
            instruct(vs, txt)
        timeScrub()
        renderOnly(vs)

        
        t = 500 - (java.lang.System.currentTimeMillis() - t0)
        if t > 0: Thread.sleep(t)

        swapBuffers()

        t = java.lang.System.currentTimeMillis()
        print t - t0
        t0 = t
        while 1:
            k,t = waitkey(t0 = t0)
            if t < 100: continue
        
            if k == "Control_R":
                res = 1
                break
            elif k == "Control_L":
                res = 0
                break

        render(vs0)
        
        outfile.write("%s  %s  %s %s\n" % (
            t0,
            js(reduce(lambda x,y:x+y, cols)),
            res, t) )
        outfile.flush()

        if not txt:
            sendResult(res)

        t0 += t
        
    System.exit(0)
