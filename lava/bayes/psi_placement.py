#(c): Janne V. Kujala

# Code for computing the placement of the next trial using the Psi method.
# Psi class uses psi_placement.c and PyPsi is a python implementation
# optionally using the bayes.c extension module.
# This is generic code that does not know about the underlying model, i.e.,
# the distributions and values of the psychometric function are handled
# as flat arrays.

from Numeric import *
import popen2
import os
from time import time

def write(f, a):
    f.write(a.tostring())

def read(f, shape, datatype = Float64):
    m = zeros(0, datatype).itemsize()
    
    shape0 = shape
    if type(shape) is type(1): shape = shape,
    n = int(product(shape))
    s = f.read(m * n)
    a = fromstring(s, datatype)
    if shape0 is 1: return a[0]
    a.shape = shape
    return a


class Psi:
    def __init__(self, M, N):
        self.M = M
        self.N = N
        self.f0, self.f1 = popen2.popen2("./psi_placement2 %s %s" % (M, N))

    def placement(self, prior):
        write(self.f1, prior)
        self.f1.flush()

        j = int(read(self.f0, 1))
        #print "Read index", j
        
        self.posteriors = read(self.f0, (2, self.N))
        #print "Read posteriors"

        H = transpose(read(self.f0, (3, self.M)))
        #print "Read entropies"

        return j, H

    def posterior(self, prior, i, r):
        return self.posteriors[r]

    
from bayes import *

def H2(p):
    return -(p * log(p) + (1-p) * log(1-p))/log(2)

class PyPsi:
    def __init__(self, psi):
        self.psi_tbl = psi
        self.psi_H2 = H2(self.psi_tbl)
        
        #self.psi_tbl = self.psi_tbl.astype(Float64)
        
    def placement(self, prior):
        min_H = 1E300
        H = ones((len(self.psi_tbl), 3), Float64)

        st = 0
        st2 = 0

        H_prior = entropy(prior)
        
        start_time = time()
        i = 0
        for psi in self.psi_tbl:
            if 1:
                psi_H2 = self.psi_H2[i]
                p1 = sum(psi * prior)
                H[i,0] = H_prior - H2(p1) + sum(psi_H2 * prior)
            elif 0:
                if 1:
                    t1 = psi * prior
                    t0 = prior - t1

                    p1 = sum(t1)
                    p0 = 1 - p1

                    posterior1 = t1 * (1.0 / p1)
                    posterior0 = t0 * (1.0 / p0)
                else:
                    tt = time()
                    p1, posterior1 = posterior(prior, psi)
                    p0, posterior0 = posterior(prior, ones((1,), Float32) - psi)
                    st2 += time() - tt

                tt = time()
                if 1:
                    H[i][1] = -sum(posterior0 * log(posterior0))
                    H[i][2] = -sum(posterior1 * log(posterior1))
                else:
                    H[i][1] = entropy(posterior0)
                    H[i][2] = entropy(posterior1)
                H[i][0] = p0 * H[i][1] + p1 * H[i][2]
                st += time() - tt
            else:
                tt = time()
                (p0, p1, H0, H1), (posterior0, posterior1) = posterior2(prior, psi)
                st += time() - tt
                H[i][1] = H0
                H[i][2] = H1
                H[i][0] = p0 * H[i][1] + p1 * H[i][2]
                
            
            if H[i][0] < min_H:
                min_H = H[i][0]
                min_i = i

            i += 1

        t = time() - start_time
        print "t=%s s" % t
        print "t=%s s" % st, st2

        return min_i, H

    def posterior(self, prior, i, r):
        t1 = self.psi_tbl[i] * prior
        p1 = sum(t1)
        if r == 1:
            return t1 / p1
        else:
            return (prior - t1) / (1 - p1)
