#(C): Janne V. Kujala

# Classes implementing a trial placement computation server
# using the Psi method

# This is a cut & paste kluge...

import Numeric
from Numeric import *
from select import *
import sys
from random import shuffle, random, seed, choice

from FFT import *
from time import time

sys.path.append('../fftw')
from fftw3 import *

# Fix underflow
min_exp = -745.13321910194116526
def safe_exp(x):
    x = where(less(x, min_exp), min_exp, x)
    return Numeric.exp(x)

def psi(r1, a1, r2, a2, aspect, slope, d):
    a = a1 - a2
    
    x = r1 / (r2 * sqrt(aspect)) * cos(a)
    y = r1 / (r2 / sqrt(aspect)) * sin(a)

    r2 = (x*x + y*y)**(0.5 * slope)
    p = ((1 - 0.5 * d) - (1 - d) * safe_exp( -r2  ));

    return p

def getPoint(i):
    r,a = decodeModel(i, params = params[-2:])
    x = r * cos(a)
    y = r * sin(a)
    return x,y

# Sampling constants

F = 10**(1/20.)   # Intensity value resolution
nr = 40           # Number of intensity values
na = 32           # Number of intensity angles

params = (
    # aspect ratio
    [ (10**(1/20.))**(i+1-32) for i in range(0,32) ],

    # slope
    [ (10**(1/20.))**i for i in range(0,20) ],    
    #( 4, ),

    # missed trials ratio
    #[ 10**(-i*.1) for i in range(0,20) ],
    #(.01, .02, .04, .08, .16, .32, .64),
    #(.02,.04,.08),
    (.04, ),
    
    # intensity/model radius
    [ F**(i+1-nr) for i in range(0,nr) ],

    # intensity/model angle
    [ j*pi/na for j in range(0, na) ],
    )

nm = product(map(len, params[:-2]))

npoints = nr * na
nmodels = nm * nr * na

print >> sys.stderr, "Params:", map(len, params)

def decodeModel(i, params = params):
    res = []
    for param in params[::-1]:
        res.append( param[i % len(param)] )
        i /= len(param)
    return res[::-1]

total_usage = 0
def usage(a, count = 0):
    global total_usage
    if count:
        total_usage += size(a) * a.itemsize()
        return "%s *%.2f MB*" % (shape(a), size(a) * a.itemsize() / 1024.0**2)
    else:
        return "%s [%.2f MB]" % (shape(a), size(a) * a.itemsize() / 1024.0**2)

print >> sys.stderr, "Computing prior..."

# Prior

prior = ones( (nm, nr, na), Float64)
prior = prior / sum(prior.flat)

print >> sys.stderr, usage(prior)

print >> sys.stderr, "Computing psi table..."; t = time()

def redim(a, i, n): s = [1] * n; s[i] = len(a); return reshape(a, s)

n = len(params)
r = redim(params[-2], n-2, n)
a = redim(params[-1], n-1, n)
rest = [ redim(params[i], i, n) for i in range(0, n - 2) ]

psi_tbl = concatenate((
    psi( r * F**(nr-1), a, 1, 0, *rest ),
    psi( r * F**(  -1), a, 1, 0, *rest ),
    ), axis = -2)

del n, r, a, rest

psi_tbl = reshape(psi_tbl, (nm, nr * 2, na))

print >> sys.stderr, usage(psi_tbl), "%.3f s" % (time() - t)


def log2(x): return log(x) / log(2)

def H2(p):
    "Compute the entropy of a binary distribution with probabilities p and 1-p"
    return -p * log2(p) - (1-p) * log2(1-p)

print >> sys.stderr, "Computing auxiliary psi table..."

psi_H2 = H2(psi_tbl)
print >> sys.stderr, usage(psi_H2)

print >> sys.stderr, "Fourier transforming psi tables..."

F_psi = real_fft2d(psi_tbl, axes = (-2,-1)).copy()
F_psi_H2 = real_fft2d(psi_H2, axes = (-2,-1)).copy()

print >> sys.stderr, usage(F_psi, 1), usage(F_psi_H2, 1)

del psi_H2

print >> sys.stderr, "Building reversed psi table..."

psi_tbl2 = alignedarray((nm, nr * 2, na * 2), Float64)
psi_tbl2[:,:nr,:na] = psi_tbl[:,:nr,:][:,::-1,::-1]
psi_tbl2[:,:nr,na:] = psi_tbl[:,:nr,:][:,::-1,::-1]
psi_tbl2[:,nr:,:na] = psi_tbl[:,nr:,:][:,::-1,::-1]
psi_tbl2[:,nr:,na:] = psi_tbl[:,nr:,:][:,::-1,::-1]

print >> sys.stderr, usage(psi_tbl2, 1)

if __name__ != "__main__":
    # Not needed for other than tests
    del psi_tbl


print >> sys.stderr, "Allocating temporary storage..."
_ft_tmp = alignedarray((nm, 2*nr, na), Float64)
_ft_tmp2 = alignedarray((nm, 2*nr, na/2+1), Complex64)
_ft_tmp3 = alignedarray((2*nr,na/2+1), Complex64)
print >> sys.stderr, usage(_ft_tmp, 1), usage(_ft_tmp2, 1), usage(_ft_tmp3, 1)

print >> sys.stderr, "Planning fft..."; t0 = time()
_plan = fftw_plan_many_dft_r2c(2, array((2*nr, na)), nm,
                               _ft_tmp, None, 1, 2*nr*na,
                               _ft_tmp2, None, 1, 2*nr*(na/2+1),
                               FFTW_MEASURE | FFTW_PRESERVE_INPUT)
print >> sys.stderr, "t=%.3f" % (time() - t0)
clear(_ft_tmp)

def FT(p):
    """Return the Fourier transform of p (with proper padding added
    for dimensions not to be wrapped)
    """
    _ft_tmp[...,:nr,:] = p
    fftw_execute(_plan)
    return _ft_tmp2


def invFT(p):
    """Return the relevant part of the inverse Fourier transform of an array
    obtained by FT
    """
    return inverse_real_fft2d(p, axes = (-2,-1))[:nr,...]


def get_psi(i, j):
    return psi_tbl2[...,nr-i-1:-i-1,na-j-1:-j-1]


print >> sys.stderr, "Allocating more temporary storage..."
_psi0 = alignedarray(shape(prior), Float64)
print >> sys.stderr, usage(_psi0, 1)

print >> sys.stderr, "Total usage: *%.2f MB*" % (total_usage / 1024**2.0)

def placement(prior):
    # Computational complexity:
    #
    # Fourier transform of prior: O( nr * na * log(nr * na) * nm)
    # Inverse Fourier transforms: O( nr * na * log(nr * na) )
    #          Sums and products: O( nr * na * nm )
    #                  Other ops: O( nr * na )

    global p1






class Psi:
    def __init__(self, prior):
        self.prior = prior.copy()
        self.posterior = None
        self.i = None

    def compute_placement(self):
        assert self.has_prior()

        F_p = FT(self.prior)
        mulsum(F_p, F_psi, _ft_tmp3)
        p1 = invFT(_ft_tmp3)
        mulsum(F_p, F_psi_H2, _ft_tmp3)
        dH = H2(p1) - invFT(_ft_tmp3)

        self.i = argmax(dH.flat)

        # XXX
        self.posterior = self.prior
        self.prior = None
        self.p1 = p1.flat[self.i]
        self.psi1 = get_psi(self.i / na, self.i % na)

    def get_placement(self):
        assert self.has_placement()
        p = getPoint(self.i)
        self.i = None
        return p

    def has_prior(self): return not self.prior is None
    def has_placement(self): return not self.i is None

    def update_prior(self,res):
        assert self.i is None

        self.prior = self.posterior
        self.posterior = None
        if res:
            multiply(self.prior, self.psi1, self.prior)
            m = 1 / self.p1
        else:
            subtract(1, self.psi1, _psi0)
            multiply(self.prior, _psi0, self.prior)
            m = 1 / (1 - self.p1)

        multiply(self.prior, m, self.prior)


class DummyPsi:
    def get_placement(self): return (0,0)
    def has_prior(self): return 0
    def has_placement(self): return 1
    def update_prior(self,res):
        self.prior = 1
        self.posterior = None
    

def blockRandomize(values, trials, sep):
    l = [ v for v in values ]
    shuffle(l)
    while len(l) < trials:
        t = [ v for v in values ]
        shuffle(t)
        while t:
            while t[-1] in l[len(l)-sep:]:
                shuffle(t)
            l.append(t.pop())

    return l[:trials]

def indices(a, v):
    l = []
    for i in range(0, len(a)):
        if a[i] == v: l.append(i)
    return l
                
class PsiServer:
    def __init__(self, instream, outstream, out_mappings, trials, dummytrials, sep = 3):
        self.f_in = instream
        self.f_out = outstream
        self.po = poll()
        self.po.register(instream, POLLIN)
        #self.po.register(outstream, POLLOUT)

        self.psi = [ Psi(prior) for x in out_mappings ]
        self.psi += [ DummyPsi() for x in out_mappings ]
        self.trials = blockRandomize( range(0, len(out_mappings)), trials, sep)

        # Add dummy trials (with 0 intensity)
        for i in range(0, dummytrials):
            c = i % len(out_mappings)
            j = choice(indices(self.trials, c))
            self.trials[j] += len(out_mappings)

        self.out_mappings = out_mappings + out_mappings

        
        print >> sys.stderr, "Trials:", self.trials
        #print >> sys.stderr, "Outmaps:", self.out_mappings

        self.ind0 = 0
        self.ind1 = 0
        
    def canread(self):
        if (self.f_in.fileno(), POLLIN) in self.po.poll(0):
            return 1
        return 0

    def canwrite(self):
        if (self.f_out.fileno(), POLLOUT) in self.po.poll(0):
            return 1
        return 0
        
    def read(self):
        line = self.f_in.readline()
        if not line:
            print >> sys.stderr, "EOF"
            return 0
        res = int(line[0])
        print >> sys.stderr, "Trial", self.ind0, "result:", line
        
        i = self.trials[ self.ind0 ]
        self.psi[i].update_prior(res)
        self.ind0 += 1

        return self.ind0 < len(self.trials)

    def write(self):
        if self.ind1 >= len(self.trials):
            return 0
        i = self.trials[self.ind1]
        psi = self.psi[i]
        if psi.has_placement():
            p = psi.get_placement()
            self.f_out.write(str(self.out_mappings[i](p)) + "\n")
            self.f_out.flush()
            self.ind1 += 1
            return 1
        else:
            return 0

    def mainloop(self):

        idle = 0
        while 1:
            if idle:
                print >> sys.stderr, "Trials in/out/total:", self.ind0, self.ind1, len(self.trials)
            while self.canread() or idle:
                idle = 0
                if self.read() == 0:
                    return

            if self.write():
                continue

            idle = 1
            for j in range(self.ind1, len(self.trials)):
                i = self.trials[ j ]
                if self.psi[i].has_prior():
                    print >> sys.stderr, "Computing placement for condition", i, "...",
                    sys.stderr.flush()
                    self.psi[i].compute_placement()
                    print >> sys.stderr, self.psi[i].i

                    idle = 0
                    break


if __name__ == "__main__":
    PsiServer(sys.stdin, sys.stdout, [ lambda x,i=i: (i,x) for i in range(0,10) ], 220, 20).mainloop()

        
                
            
