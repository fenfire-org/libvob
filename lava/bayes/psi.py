#(c): Janne V. Kujala

# This is the old 1d psi method implementation

import os
from random import random, seed
from Numeric import *

# erfcc function copied from python-stats

def aerfcc(x):
    """
Returns the complementary error function erfc(x) with fractional error
everywhere less than 1.2e-7.  Adapted from Numerical Recipies.  Can
handle multiple dimensions.

Usage:   aerfcc(x)
"""
    z = abs(x)
    t = 1.0 / (1.0+0.5*z)
    ans = t * exp(-z*z-1.26551223 + t*(1.00002368+t*(0.37409196+t*(0.09678418+t*(-0.18628806+t*(0.27886807+t*(-1.13520398+t*(1.48851587+t*(-0.82215223+t*0.17087277)))))))))
    return where(greater_equal(x,0), ans, 2.0-ans)


def N(x):
    x = where(greater(x,38), 38, x)
    x = where(less(x,-38),-38, x)
    return 1 - .5 * aerfcc(x/sqrt(2))

def d_prime(x,a,b):
    return (x / a)**b

def psi(x,a,b):
    d = .04
    return (1 - d) * (2 * N(d_prime(x,a,b) / sqrt(2)) - 1) + .5 * d

# gnuplot equivalents:
# N(x) =  .5+.5*erf(x/sqrt(2))
# d(x) = (x/a)**b
# psi(x) = (1 - .04) * (2 * N(d(x) / sqrt(2)) - 1) + .5 * .04

def map_x(x): return 10**((x-59)/20.0)
def map_a(a): return 10**((a-59)/20.0)
def map_b(b): return 10**((b+10)/20.0)

psi_tbl = fromfunction(lambda x,a,b:
                       psi(map_x(x),
                           map_a(a),
                           map_b(b)),                     
                       (60, 60, 20))

lambda_shape = shape(psi_tbl)[1:]

prior = ones(lambda_shape) / float(product(lambda_shape))

seed(0)

def sum_lambda(p):
    return sum(sum(p,-1),-1)

def d2(p):
    return reshape(p, list(shape(p)) +[1,1])

def H2(p):
    return -sum_lambda(p * log(p))

def H1(p):
    q = sum(p,-1) 
    return -sum(q * log(q), -1)

def sample(p):
    t = cumsum(reshape(p, (size(p),))) - random()
    i = argmin(where(t < 0, 1, t))
    ind = []
    s = list(shape(p))
    while len(s):
        d = s.pop()
        ind = [i % d] + ind
        i /= d
    return tuple(ind)

    
    

graph = os.popen("graph -TX -x 0 1 -y -1 7 --bitmap-size 600x1200", "w")

pl = 0

prevx = 0

while 1:
    t1 = psi_tbl * prior
    t0 = (1 - psi_tbl) * prior

    p1_x = sum_lambda(t1)
    p0_x = sum_lambda(t0)

    posterior1 = t1 / d2(p1_x)
    posterior0 = t0 / d2(p0_x)

    H = H2(posterior1) * p1_x + H2(posterior0) * p0_x
    H_ = H1(posterior1) * p1_x + H1(posterior0) * p0_x

    #H[prevx] = 1E10
    x = argmin(H)
    prevx = x

    graph.write("#m=1,S=0\n")
    for i in range(0,len(H)):
        graph.write("%s %s\n" % (map_x(i),H[i]))
    graph.write("#m=0,S=16\n")
    graph.write("%s %s\n" % (map_x(x), H[x]))

    if 0:
        graph.write("#m=2,S=0\n")
        x_ = argmin(H_)
        for i in range(0,len(H)):
            graph.write("%s %s\n" % (map_x(i),H_[i]))
        graph.write("#m=0,S=16\n")
        graph.write("%s %s\n" % (map_x(x_), H_[x_]))

    graph.write("\n")
    graph.flush()

    E = p1_x
    E2 = sum_lambda(psi_tbl * psi_tbl * prior)

    t = E2 - E*E
    SD = list(sqrt(where(t < 0, 0, t)))
    E = list(E)

    if pl:
        graph2 = os.popen("graph -TX -x 0 1 -y -1 7 -B", "w")
        for i in range(0,30):
            a,b = sample(prior)
        
            for i in range(0,len(E)):
                graph2.write("%s %s\n" % (map_x(i), psi_tbl[i][a][b]))
            graph2.write("\n")
            
        graph2.close()
            
    if 0:
        graph2 = os.popen("graph -TX -x 0 1 -y -1 7", "w")
        for i in range(0,len(E)):
            graph2.write("%s %s\n" % (map_x(i), E[i] - 5*SD[i]))

        graph2.write("\n")
        for i in range(0,len(E)):
            graph2.write("%s %s\n" % (map_x(i), E[i] + 5*SD[i]))

        graph2.write("\n")
        for i in range(0,len(E)):
            graph2.write("%s %s\n" % (map_x(i), psi(map_x(i), .5, 20)))
        graph2.write("\n")
        graph2.close()
        
    print map_x(x), x, H[x]

    r = input("trial result> ")
    pl = r
    r = random() < psi(map_x(x), .5, 5)
    print r

    if r == 0:
        prior = posterior0[x]
    else:
        prior = posterior1[x]

