import Numeric
from Numeric import *
import os
from random import random,seed

import psi_model2
from psi_model2 import *

from show2 import showPointData

import time

def refl(xy):
    return xy[0], xy[1], -xy[0], -xy[1]

def drawModel(graph, i):
    graph.write("#m=2,S=0\n")
    for p in getModel(i):
        graph.write("%s %s\n" % (p[0] + (random() - .5) * .01,
                                 p[1] + (random() - .5) * .01))
    
    graph.write("\n")
    graph.flush()

def sample(p, n = 1):
    return searchsorted(cumsum(ravel(p)), [ random() for i in range(0, n) ])

def drawEstimate(prior):
    entropy = -sum((prior * log(prior)).flat)
    title = "Entropy %.2f bits" % (entropy / log(2))
    cmd = "graph -TX -w .8 -h .8 -u .09 -r .1 -x -1 1 -y -1 1 -L '%s'" % title
    graph2 = os.popen(cmd, "w")
    for i in sample(prior, 100):
        drawModel(graph2, i)
            
    graph2.close()

seed(1)

print npoints, "points"
print nmodels, "models"

simulate = 1

def simul_psi(x,y):
    a = pi/12/4
    x, y = ( cos(a) * x - sin(a) * y,
             sin(a) * x + cos(a) * y )
    
    t = -(abs(x*8)**1+abs(y*1)**1)**2
    #t = -(abs(x*4)**2+abs(y*4)**2)**2
    #t = -(abs(x*2)**4+abs(y*5)**4)
    #return .5 + 0 * t
    d = .08
    p = 1-.5*d - (1-d) * exp(where(less(t, -745), -745, t))
    return p

graph = os.popen("graph -TX -w .8 -h.8 -u .09 -r .1 -x -1 1 -y -1 1 -L Trials", "w")

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


def modelConfidence(prior, data):
    p = 1
    q = 1
    pt = []
    for i,res in data:
        #r,a = decodeModel(i, params = params[-2:])
        #t = psi(r, a, *args)
        #t = sum( (get_psi(i/na,i%na) * prior).flat )
        t = psi_model2.p1.flat[i]
        pt.append(t)
        q *= (t**2 + (1 - t)**2)
        if res:
            p *= t
        else:
            p *= 1 - t

    n = 1000
    m = 0
    for iter in range(0,n):
        pp = 1
        for p1 in pt:
            if random() < p1:
                pp *= p1
            else:
                pp *= 1-p1
        if p > pp:
            m += 1

    print "Model confidence: *%.0f dB* = %.3G/%.3G" % (log10(p/q) * 10, p, q)
    print "likelihood p-value:" , (float(m) / n)
        

trial = 0
trials = []
while 1:
    print "Computing placement"

    j, H, p = placement(prior)

    #modelConfidence(prior, trials)
    #showPointData(H)
    #showPointData(psi_model2.p1)

    xy = getPoint(j)

    print trial, j, H.flat[j], xy

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

    prior = posterior(prior, j, r)
    trials.append((j,r))
    trial += 1

    if trial % 10 == -1:
        title = "Marginals, T=" + str(trial)
        graph2 = os.popen("graph -TX -w .8 -h.8 -u .09 -r .1 -l x -L '%s'" % title, "w")
        for i in range(0,5):
            p = marginal(prior, i)
            for k in range(0, len(p)):
                graph2.write("%s %s\n" % (params[i][k], p[k]+i))
            graph2.write("\n")
            
        graph2.close()

