#(c) Tuomas J. Lukka and Janne V. Kujala

"""Define the parametrized psychometric function for hybrid MCMC.

This differs from the other definition by including more paramters
and allowing computation of derivatives.

The actual model used for each set
of parameters is outp = c * (1-exp(-(x/a)^b)) + .5*(1-c)
where a is the distance, b is the slope, and c is the probability 
of informative reply.


"""

from RandomArray import *
import math
from Numeric import *
from sys import stderr
import os
import time

# Fix underflow
min_exp = -745.13321910194116526
def safe_exp(x):
    x = where(less(x, min_exp), min_exp, x)
    return Numeric.exp(x)
log2 = log(2)

def psi_local(point, a, b, c, derivs = 0):
    """Calculate a local probability value.

    Point is a "scalar", and a,b, and c give
    the local model paramters. The ranges are
    not restricted, to avoid problems.

    Returns: (p, [da, db, dc]) if derivs==1, otherwise
    only p.

    """

    inva = (a>=0)*2-1
    a = abs(a)
    invb = (b>=0)*2-1
    b = abs(b)
    c = c - 2*floor((c+1)/2) # Limit to -1..1
    invc = (c>=0)*2-1
    c = abs(c)



    parg = (log2 * point / a) 
    earg = -parg ** b
    expo = safe_exp(earg)
    smooth = c * (.5 -expo) + .5

    if derivs:
	    d_parg_a = - (log2 * point / (a**2))

	    d_earg_a = - d_parg_a * b * (parg ** (b - 1))
	    # print "PARG:",parg
	    d_earg_b = log(parg) * earg

	    d_expo_a = d_earg_a * expo
	    d_expo_b = d_earg_b * expo

	    d_smooth_a = - c * d_expo_a 
	    d_smooth_b = - c * d_expo_b 
	    d_smooth_c = (.5 - expo)

	    return (smooth, [inva*d_smooth_a, invb*d_smooth_b, invc*d_smooth_c])


    return smooth

def test_psi_local():
    pt = array([[0.1],[.5],[1],[2]])

    model = array([1.5,2,.95])

    ps, diffs =  psi_local(pt, model[0],model[1],model[2], derivs=1)
    print ps
    print diffs

    eps = .001
    for m in (0,1,2):
	    dmm = array(model)
	    dmp = array(model)
	    dmm[m] -= eps
	    dmp[m] += eps
	    pd = (psi_local(pt, *dmp) - psi_local(pt, *dmm) ) / (2*eps)
	    print pd

	    assert max(abs(pd - diffs[m])) < .001


class Model1:
    """A model with a single cyclic parameter (such as angle).
    The angle goes around the full circle here, if symmetry
    is desired, multiply the angles given to this model by two.

    The two first parameters of the psi function are defined as truncated
    fourier series of the parameter, and the third parameter
    is a constant.
    """

    def __init__(self, nfourier):
	self.NFOURIER = nfourier
	coeffs = []
	offsets = []
	for i in range(0,self.NFOURIER):
	    # Cosine
	    coeffs.append(i)
	    offsets.append(.5 * math.pi)
	    if i != 0:
		# Sine only with no 0
		coeffs.append(i)
		offsets.append(0)
	self.coeffs = array(coeffs)
	self.offsets = array(offsets)
	self.ncoeffs = shape(self.coeffs)[0]

	# Number of parameters in a parameter vector = 
	# 2 coeffs for the slope and location, plus
	# 1 for the probability of a real answer.
	self.nparams = self.ncoeffs * 2 + 1
    def getNCoeffs(self):
	return self.ncoeffs
    def getNParams(self):
	"""The length of the parameter vector.
	"""
	return self.nparams

    def setPoints(self, points):
	"""Set the points.

	points -- a 2D array: p[0] = distance, p[1] = angle
	"""

	print self.offsets, self.coeffs, points, (points[1])
	self.pointcoeffs = sin(self.offsets[:,NewAxis] + self.coeffs[:,NewAxis] * (points[1]))
	self.pointdists = points[0]

    def _params(self, coeffs, derivs = 0):
	"""Calculate model parameters for each point from fourier
	coefficients.

	coeffs -- a getNCoeffs() array

	returns: an array, the size of which is the number of points
	given

	"""
	params = dot(coeffs, self.pointcoeffs)
	if derivs:
	    dparams = self.pointcoeffs
	    return (params, dparams)
	return params

    def evaluate(self, params):
	"""Evaluate the model for the given parameters.

	params = array whose first dimension is self.nparams
	"""
	ca = params[..., 0:self.ncoeffs]
	cb = params[..., self.ncoeffs:(2*self.ncoeffs)]

	cc = params[..., 2*self.ncoeffs:]

	pa = self._params(ca)
	pb = self._params(cb)
	# print "EVAL: ",pa,pb,cc

	return psi_local(self.pointdists, pa, pb, cc)

    def evalLog(self, params, pointres):
	"""Evaluate the log probability and its gradient
	on a point.

	params -- parameters for the evaluation
	pointres -- The desired results for the points set by setPoints
	"""
	# Tear the parameter vector
	ca = params[..., 0:self.ncoeffs]
	cb = params[..., self.ncoeffs:(2*self.ncoeffs)]

	cc = params[..., 2*self.ncoeffs:]

	# Evaluate the fourier series
	pa = self._params(ca)
	pb = self._params(cb)
	# print "EVAL: ",pa,pb,cc

	# Evaluate the function
	v, derivs = psi_local(self.pointdists, pa, pb, cc, derivs=1)

	# Now, when pointres = 0, calculate 1-p, otherwise p.
	vpp = pointres[NewAxis, :]
	p = vpp * v + (1-vpp) * (1-v)
	# print "P",p
	# print "DER",derivs
	derivs = [(2*vpp-1) * di for di in derivs]
	# print "D",derivs

	# Look at sum of logarithms

	# print "Ptolog:",p
	logp = log(p)
	dlogp = [1/p * di for di in derivs]
	# print "Logged:",logp,dlogp

	# Now, we need the derivatives of the fourier coefficients

	da = innerproduct(
		dlogp[0],
		self.pointcoeffs,
		)

	db = innerproduct(
		dlogp[1],
		self.pointcoeffs,
		)
	dc = sum(dlogp[2], -1)[:,NewAxis]
	# print self.pointcoeffs
	# print "Dersum",da,db,dc

	#
	logpsum = sum(logp, -1)

	# Re-pack
	derivs = array(params)
	derivs[..., 0:self.ncoeffs] = da
	derivs[..., self.ncoeffs:(2*self.ncoeffs)] = db
	derivs[..., 2*self.ncoeffs:] = dc


	return logpsum, derivs

    def test(self):

	m = Model1(3)
	m.setPoints(array([[1.,1,1,.5,.5,.5,1.5,1.5,1.5],
	    [0,.25,.5,0,.25,.5,0,.25,.5]]))

	print "Model:"
	print m.pointcoeffs
	print m.pointdists

	print "Params:"
	print m._params(array([1,0,0,0,0]),1) 
	print m._params(array([0,1,0,0,0]),1) 
	print m._params(array([0,0,1,0,0]),1) 
	print m._params(array([0,0,0,1,0]),1) 
	print m._params(array([0,0,0,0,1]),1) 
	

	print m.evaluate(
	    array(
	    [
		[0,1,1,0,0, 0,0,0,1,0, .95],
		[0,1,0,0,0, 0,0,0,1,0, .95]
	    ]
		))

	m.setPoints(array([[1.,.5,.5],
	    [0,.25,.3]]))

	x = array(
	    [
		[0.1, 1, 1,0.1,0.1, 0.1,0.1,0.1,1,0.1, .95],
		[0.2, 1,0.1,0.1,0.1, 0.1,0.1,0.1,1,0.1, .95]
	    ] )

	res = array([0,1,0])
	vals, derivs = m.evalLog(
	    x,
	    res)
	eps = .0000001
	failed = 0
	for d in range(0,shape(x)[-1]):
	    xp = array(x)
	    xp[...,d] += eps
	    nvals, nderivs = m.evalLog(
		xp,
		res)
	    print "comp:",vals,nvals,(nvals-vals)/eps,derivs[...,d]
	    if not allclose((nvals - vals)/eps, derivs[...,d]):
		failed = 1
	    assert not failed



if __name__ == '__main__':
	# test_psi_local()
	# Model1(0).test()

	if 1:
	    points = array([
		[ .5, .5, 1.5, 1.5, 2.5,2.5, .5, .5, 1.5, 1.5, 2.5,2.5 ],
		[ 0, 0, 0, 0, 0,0, 1, 1, 1, 1,1,1 ],
		])
	    results = array([0,0,1,1,1,1, 0,0,0,0,1,1])

	    nh = 50
	    def probep(p,x):
		return where(p, floor(x/nh) * .5, (x%nh)*4./nh)
	    probepoints = fromfunction(probep, (2,3*nh))
	    
	    m = Model1(4)

	    x = array([random(m.getNParams())])
	    i = 0
	    while 1:
		
		if i % 100 == 0:
		    m.setPoints(probepoints)
		    vals = m.evaluate(x)
		    graph = os.popen("graph -TX", "w")
		    print "PROBE:",probepoints
		    print "VALS:",vals
		    for p in range(0, shape(probepoints)[1]):
			graph.write("%s %s\n" %
			    ( probepoints[0][p], 
			      probepoints[1][p] * 2 + vals[0][p] ))
		    graph.close()

		    m.setPoints(points)
		    time.sleep(3)



		i+=1
		vals, derivs = m.evalLog(x, results)
		print vals
		derivs /= sqrt(innerproduct(derivs, derivs))
		derivs /= 100
		x += derivs


	if 0:
	    angles = arrayrange(0,1,.1)
	    dists = arrayrange(0,5,.02)

	    an = dists + (0*angles[:, NewAxis])
	    di = 0*dists + (angles[:, NewAxis])
	    l = len(angles) * len(dists)
	    an = reshape(an, (l,))
	    di = reshape(di, (l,))


	    m.setPoints(array([an, di]))
	    print "AN,DI"
	    print an,di

	    print m._params(array([0,1,0,0,0]))
	    print m._params(array([0,0,1,0,0]))

	    graph = os.popen("graph -TX", "w")

	    i = 0
	    vals = m.evaluate(array([0,3,0,0,0, 0,0,5,0,0, 1]))
	    print "VALS:",vals
	    for p in range(0, shape(vals)[0]):
		i += 1
		str = "%s %s\n" % ( i, vals[p] )
		print str
		graph.write(str)
	    graph.close()


