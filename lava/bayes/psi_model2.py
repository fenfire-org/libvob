#(C): Janne V. Kujala

# This module defines the psychometric function and the Bayesian model
# for computing the placement of the next trial
#
# This is a test of the new algorithm without proper abstractions yet...

import Numeric
from Numeric import *
from FFT import *
from sys import stderr
from time import time

use_fftw = 1

if use_fftw:
    import sys
    sys.path.append('../fftw')
    from fftw3 import *
else:
    alignedarray = zeros
    def clear(a): a[...] = 0
    

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

def getPointIndex(x, y):
    """Return the index of a point nearest to (x,y)
    """
    r = hypot(x, y)
    r = where(r == 0, 1E-100, r)
    r = log(r) / log(F) + nr - 1
    r = (r + .5).astype(Int32)

    a = arctan2(y, x)
    a = where(less(a, 0), a + pi, a) / pi * na
    
    a = (a + .5).astype(Int32)
    a = where(a == na, 0, a)

    return where((r >= nr) | (r < 0), -1, r * na + a)


def getModel(i):
    aspect, slope, d,  r, a0 = decodeModel(i)

    mat = array([[cos(a0), -sin(a0)],
                 [sin(a0),  cos(a0)]])

    n = 32
    a = arange(0,n+1) * 2 * pi / n

    x = r * sqrt(aspect) * cos(a)
    y = r / sqrt(aspect) * sin(a)

    return transpose(dot(mat, array((x,y))))




# Sampling constants

F = 10**(1/20.)   # Intensity value resolution
nr = 40           # Number of intensity values
na = 28           # Number of intensity angles

params = (
    # aspect ratio
    [ (10**(1/20.))**(i+1-20) for i in range(0,20) ],

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

print "Params:", map(len, params)

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

print "Computing prior..."

# Prior

prior = ones( (nm, nr, na), Float64)
prior = prior / sum(prior.flat)

print usage(prior)

print "Computing psi table..."; t = time()

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

print usage(psi_tbl), "%.3f s" % (time() - t)


def log2(x): return log(x) / log(2)

def H2(p):
    "Compute the entropy of a binary distribution with probabilities p and 1-p"
    return -p * log2(p) - (1-p) * log2(1-p)

print "Computing auxiliary psi table..."

psi_H2 = H2(psi_tbl)
print usage(psi_H2)

print "Fourier transforming psi tables..."

F_psi = real_fft2d(psi_tbl, axes = (-2,-1)).copy()
F_psi_H2 = real_fft2d(psi_H2, axes = (-2,-1)).copy()

print usage(F_psi, 1), usage(F_psi_H2, 1)

del psi_H2

print "Building reversed psi table..."

psi_tbl2 = alignedarray((nm, nr * 2, na * 2), Float64)
psi_tbl2[:,:nr,:na] = psi_tbl[:,:nr,:][:,::-1,::-1]
psi_tbl2[:,:nr,na:] = psi_tbl[:,:nr,:][:,::-1,::-1]
psi_tbl2[:,nr:,:na] = psi_tbl[:,nr:,:][:,::-1,::-1]
psi_tbl2[:,nr:,na:] = psi_tbl[:,nr:,:][:,::-1,::-1]

print usage(psi_tbl2, 1)

if __name__ != "__main__":
    # Not needed for other than tests
    del psi_tbl


print "Allocating temporary storage..."
_ft_tmp = alignedarray((nm, 2*nr, na), Float64)
if use_fftw:
    _ft_tmp2 = alignedarray((nm, 2*nr, na/2+1), Complex64)
    _ft_tmp3 = alignedarray((2*nr,na/2+1), Complex64)
    print usage(_ft_tmp, 1), usage(_ft_tmp2, 1), usage(_ft_tmp3, 1)
else:
    print usage(_ft_tmp, 1)

if use_fftw:
    print "Planning fft..."; t0 = time()
    _plan = fftw_plan_many_dft_r2c(2, array((2*nr, na)), nm,
                                   _ft_tmp, None, 1, 2*nr*na,
                                   _ft_tmp2, None, 1, 2*nr*(na/2+1),
                                   FFTW_MEASURE | FFTW_PRESERVE_INPUT)
    print "t=%.3f" % (time() - t0)

    def doFT():
        fftw_execute(_plan)
        return _ft_tmp2
else:
    def doFT():
        return real_fft2d(_ft_tmp, axes = (-2,-1))

clear(_ft_tmp)
_ft_tmp[...,:nr,:] = prior
prior = _ft_tmp[...,:nr,:]

def FT(p):
    """Return the Fourier transform of p (with proper padding added
    for dimensions not to be wrapped)
    """
    if not p is prior:
        print "WARNING: FT(): not prior!"
        prior[...] = p
        
    return doFT()

def invFT(p):
    """Return the relevant part of the inverse Fourier transform of an array
    obtained by FT
    """
    return inverse_real_fft2d(p, axes = (-2,-1))[:nr,...]


def get_psi(i, j):
    if 0:
        psi1 = psi_tbl
        psi1 = concatenate((psi1[...,nr+i+1:,:], psi1[...,:i+1,:]), -2)[...,::-1,:]
        psi1 = concatenate((psi1[...,j+1:], psi1[...,:j+1]), -1)[...,::-1]
        return psi1
    else:
        return psi_tbl2[...,nr-i-1:-i-1,na-j-1:-j-1]

def get_psi_slow(i, j):
    psi1 = zeros( (nm, nr, na), Float64 )
    for m in range(0, nm):
        rest = decodeModel(m, params = params[:-2])
        for k in range(0, nr):
            for l in range(0, na):
                psi1[m,k,l] = psi(params[-2][i],
                                  params[-1][j],
                                  params[-2][k],
                                  params[-1][l],
                                  *rest)
                #t = psi(F**(i-k), (j-l)*pi/na, 1, 0, F**(m+1-nm))
                #t = psi_tbl[m, i-k, j-l]
                
    return psi1

def get_psi2(i, j):
    return (
        (psi_tbl[...,i::-1,j::-1], lambda a:a[...,:i+1,:j+1]),
        (psi_tbl[...,i::-1,:j:-1], lambda a:a[...,:i+1,j+1:]),
        (psi_tbl[...,:nr+i:-1,j::-1], lambda a:a[...,i+1:,:j+1]),
        (psi_tbl[...,:nr+i:-1,:j:-1], lambda a:a[...,i+1:,j+1:]),
        )

print "Allocating more temporary storage..."
_psi0 = alignedarray(shape(prior), Float64)
print usage(_psi0, 1)

def posterior(oldprior, i, r):
    print "Updating prior:",i,r
    t0 = time()
    psi1 = get_psi(i / na, i % na)
        
    if r:
        multiply(oldprior, psi1, prior)
        m = 1 / p1.flat[i]
    else:
        subtract(1, psi1, _psi0)
        multiply(oldprior, _psi0, prior)
        m = 1 / (1 - p1.flat[i])

    multiply(prior, m, prior)

    print "t=%.3f" % (time() - t0)
    return prior

def posterior2(oldprior, i, r):
    global prior
    print "Updating prior:",i,r
    t0 = time()
    
    psi1 = get_psi2(i / na, i % na)
        
    if r:
        for psi, slice in psi1:
            multiply(slice(oldprior), psi, slice(prior))
            
        m = 1 / p1.flat[i]
    else:
        for psi, slice in psi1:
            subtract(1, psi, slice(_psi0))
            
        multiply(oldprior, _psi0, prior)
        m = 1 / (1 - p1.flat[i])

    multiply(prior, m, prior)

    print "t=%.3f" % (time() - t0)
    return prior

print "Total usage: *%.2f MB*" % (total_usage / 1024**2.0)

def placement(prior):
    # Computational complexity:
    #
    # Fourier transform of prior: O( nr * na * log(nr * na) * nm)
    # Inverse Fourier transforms: O( nr * na * log(nr * na) )
    #          Sums and products: O( nr * na * nm )
    #                  Other ops: O( nr * na )

    global p1

    t0 = time()

    F_p = FT(prior)
    print "t=%.3f" % (time() - t0)

    if use_fftw:
        mulsum(F_p, F_psi, _ft_tmp3)
        p1 = invFT(_ft_tmp3)
        mulsum(F_p, F_psi_H2, _ft_tmp3)
        dH = H2(p1) - invFT(_ft_tmp3)
    else:
        p1 = invFT(sum(F_p * F_psi))
        print "t=%.3f" % (time() - t0)
        dH = H2(p1) - invFT(sum(F_p * F_psi_H2))

    print "t=%.3f" % (time() - t0)

    return argmax(dH.flat), dH, p1


def placement_slow(prior):
    t0 = time()

    p1 = zeros((nr,na), Float64)
    dH = zeros((nr,na), Float64)
    H_prior = -sum((prior * log2(prior)).flat)
    
    for i in range(0,nr):
        for j in range(0,na):

            for m in range(0,nm):
                for k in range(0,nr):
                    for l in range(0,na):
                        p1[i,j] += prior[m,k,l] * psi_tbl[m, i-k, j-l]

            H0, H1 = 0, 0
            for m in range(0,nm):
                for k in range(0,nr):
                    for l in range(0,na):
                        p = prior[m,k,l] * psi_tbl[m, i-k, j-l] / p1[i,j]
                        H1 -= p * log2(p)
                        p = prior[m,k,l] * (1 - psi_tbl[m, i-k, j-l]) / (1 - p1[i,j])
                        H0 -= p * log2(p)

            dH[i,j] = H_prior - ((1 - p1[i,j]) * H0 + p1[i,j] * H1)

            print p1[i,j], tt[0][i,j], dH[i,j], tt[1][i,j]

    print "t=%.3f" % (time() - t0)

    return argmax(dH.flat), dH, p1


def marginal(p, k):
    p = reshape(p, map(len, params))
    for i in range(0, len(params))[::-1]:
        if i != k:
            p = sum(p, i)
    return p
    

if __name__ == "__main__":

    from MLab import rand
    prior = apply(rand, shape(prior))
    prior /= sum(prior.flat)

    if 1:
        for (i,j) in [(3,7), (17,5), (19,11)]:
            print "testing get_psi(%s,%s)" % (i,j),
            a = get_psi(i,j)
            b = get_psi_slow(i,j)
            d = a - b
            print "E <=", max(abs(d).flat)

    i, dH, p1 = placement(prior)
    tt = p1, dH

    placement_slow(prior)
    
    print i, shape(dH)

    
