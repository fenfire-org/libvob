#(C): Janne V. Kujala

# The server for vob/usertest/color10.py (see Makefile)

import sys
from psi_server import PsiServer
from random import random, randrange
from math import *

def LtoY(L):
    return pow((L + 16.0) / 116, 3.0)

cols = [ LtoY(x) for x in (65, 70, 75, 80) ]
print >> sys.stderr, cols

n = len(cols)
conds = [ (cols[i], cols[j]) for i in range(0,n) for j in range(i,n) ]

conds = [ (0.35, 0.60), (0.45, 0.60), (0.55, 0.60),
          (0.35, 0.50), (0.45, 0.50), 
          (0.35, 0.40),
          ]
          
                       
                                       

print >> sys.stderr, conds

condmaps = []
for cond in conds:
    a = random() * pi
    def outmap(x, cond = cond, a = a):
        r = .3
        c0, c1 = cond
        d0, d1 = (r * cos(a) * x[0] - r * sin(a) * x[1],
                  r * sin(a) * x[0] + r * cos(a) * x[1])
        if randrange(2):
            d0, d1 = -d0, -d1

        return "(%s,)*3, (%s,)*3, (%s,)*3, (%s,)*3" % (
            c0 - d0, c1 - d1, c0 + d0, c1 + d1)
        
    condmaps.append(outmap)
        


server = PsiServer(sys.stdin, sys.stdout, condmaps,
                   len(conds) * (26+3), len(conds) * 3)

server.mainloop()

print >> sys.stderr, "Mainloop exited"
