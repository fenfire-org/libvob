import Numeric
from Numeric import *
import os
from random import random,seed
from psi_placement import *

from psi_model import points, models, prior, getPoint, getModel, get_psi

from show import showPointData

def refl(xy):
    return xy[0], xy[1], -xy[0], -xy[1]

def drawModel(graph, i):
    graph.write("#m=2,S=0\n")
    for p in getModel(i):
        graph.write("%s %s\n" % (p[0] + (random() - .5) * .01,
                                 p[1] + (random() - .5) * .01))
    
    graph.write("\n")
    graph.flush()

def sample(p):
    t = cumsum(p) - random()
    return int(argmin(where(less(t, 0), 1, t)))

def drawEstimate(prior):
    entropy = -sum(prior * log(prior))
    title = "Entropy %.2f bits" % (entropy / log(2))
    cmd = "graph -TX -w .8 -h .8 -u .1 -r .1 -x -1 1 -y -1 1 -L '%s'" % title
    graph2 = os.popen(cmd, "w")
    for i in range(0,100):
        i = sample(prior)
        if 0:
            print models[i], "likelihood = %.3f" % (prior[i] / sum(prior))
                
        drawModel(graph2, i)
            
    graph2.close()

#seed(1)

print shape(points), shape(models)

M = len(points)
N = len(models)

simulate = 1

def simul_psi(x,y):
    a = pi/12/4
    x, y = ( cos(a) * x - sin(a) * y,
             sin(a) * x + cos(a) * y )
    
    t = -(abs(x*8)**1+abs(y*1)**1)**2
    #t = -(abs(x*4)**2+abs(y*4)**2)**2
    #t = -(abs(x*2)**4+abs(y*5)**4)
    #return .5 + 0 * t
    p = .9 - .8 * exp(where(less(t, -745), -745, t))
    return p

graph = os.popen("graph -TX -w .8 -h.8 -u .1 -r .1 -x -1 1 -y -1 1 ", "w")

if simulate:
    for c in (.25, .5, .75):
        graph.write("#m=2,S=0\n");
        for a in arange(0, 2 * pi, .05):
            r = arange(0, 1, .01)
            xx = r * cos(a)
            yy = r * sin(a)
            p = simul_psi(xx, yy)
            i = int(argmin(abs(p - c)))
            graph.write("%s %s\n" % (xx[i], yy[i]))
        graph.write("\n")
    graph.flush()

#psi = Psi(M, N)
psi = PyPsi(get_psi())

trial = 0
while 1:
    print "Computing placement"

    j, H = psi.placement(prior)

    showPointData(H[:,0])
    
    xy = getPoint(j)

    print points[j], j, H[j], xy

    graph.write("#m=0,S=4\n")
    graph.write("%s %s %s %s\n" % refl(xy))
    graph.flush()

    r = 0
    while 1:
        if not simulate:
            print "0: no difference seen, 1: seen, 2: show current estimate, 3: quit"
            r = input("trial %s result> " % trial)
        else:
            #if trial % 10 or r == 2:
            if r == 2:
                r = 3
            elif trial < 20:
                p = apply(simul_psi, xy)
                r = random() < p
            else:
                r = 2
                
        if r != 2: break

        drawEstimate(prior)

    if r == 3: break

    graph.write("#m=0,S=%s\n" % (5, 16)[r])
    graph.write("%s %s %s %s\n" % refl(xy))
    graph.flush()

    prior = psi.posterior(prior, j, r)

    trial += 1
