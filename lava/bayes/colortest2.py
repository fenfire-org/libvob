import sys
from psi_server2 import PsiServer
from random import random, randrange
from math import *

from color.spaces import *



conds = [
    #((0,0), (0,0)),
    ((0,0), (.15,0)),
    ((0,0), (-.15,0)),
    ((0,0), (0,.15)),
    ((0,0), (0,-.15)),
    ((0,0), (.4,0)),
    ((0,0), (-.4,0)),
    ((0,0), (0,.4)),
    ((0,0), (0,-.4))
    ]

conds2 = [
    ((0,0), ( .1061, .1061)),
    ((0,0), (-.1061,-.1061)),
    ((0,0), ( .1061,-.1061)),
    ((0,0), (-.1061, .1061)),
    ((0,0), ( .2828, .2828)),
    ((0,0), (-.2828,-.2828)),
    ((0,0), ( .2828,-.2828)),
    ((0,0), (-.2828, .2828)),
    ]

#conds = conds2

print >> sys.stderr, conds

condmaps = []
for cond in conds:
    a = random() * pi
    def outmap(x, cond = cond, a = a):
        r = .2
        c0, c1 = cond
        d = (r * cos(a) * x[0] - r * sin(a) * x[1],
             r * sin(a) * x[0] + r * cos(a) * x[1])

        Y = .5
        col0 = YSTtoRGB((Y, c0[0] + d[0], c0[1] + d[1]))
        col1 = YSTtoRGB((Y, c0[0] - d[0], c0[1] - d[1]))
        col2 = YSTtoRGB((Y, c1[0], c1[1]))
        return col0, col1, col2
        
    condmaps.append(outmap)
        


server = PsiServer(sys.stdin, sys.stdout, condmaps, len(conds) * (22 + 3), len(conds) * 3)

server.mainloop()

print >> sys.stderr, "Mainloop exited"
