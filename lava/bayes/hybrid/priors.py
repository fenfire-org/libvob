#(c) Tuomas J. Lukka 

"""Some priors.

These return their values as energies: -log(p).
"""
from RandomArray import *
import math
from Numeric import *

class HyperGaussian:
    def __init__(self, nparams, hyperindex, indices, coeffs):
	"""

	nparams -- Number of elements in the param vector
	hyperindex -- The index of the hyper-sigma parameter
	indices -- The indices of the real parameters 
	coeffs -- The the numbers to multiply the hyperindex parameter
		    by
	"""
	self.nparams = nparams
	self.hyperindex = hyperindex
	self.indices = indices
	self.coeffs = coeffs

    def eval(self, params):
	"""Evaluate the value of this hyperprior.
	
	params -- (ninst, nparams)

	Returns: (ninst), (ninst, nparams)
	"""

	hyperparam = params[...,self.hyperindex:self.hyperindex+1]
	reals = take(params, self.indices, -1)

	sigmas = hyperparam * self.coeffs[NewAxis, :]

	E = reals**2 / (2 * sigmas**2)
	dReals = reals / (sigmas ** 2)
	dSigmas = - reals**2 / (sigmas**3)

	print "DS",dSigmas
	dHyperparam = sum(dSigmas * self.coeffs[NewAxis, :], -1)[...,NewAxis]
	print "DH",dHyperparam
	
	ret = 0*params
	ret[...,self.hyperindex:self.hyperindex+1] = dHyperparam
	# ARGH - why not?!
	# put(ret, self.indices, -1, dReals)
	for i in range(0,shape(self.indices)[0]):
	    ret[:,self.indices[i]] = dReals[:, i]

	return sum(E, -1), ret

def testHyperGaussian():
    """Test that the derivatives are right.
    """
    hg = HyperGaussian(4, 1, [0,2], array([.5,2]))

    params = random((5,4)) + 1
    eps = .0001
    for r in range(1,5):
	params[r] = params[0]
	params[r][r-1] += eps

    vals, diffs = hg.eval(params)
    print vals
    print diffs
    for r in range(1,5):
	print (
	    (vals[r] - vals[0]) / eps,
	    diffs[0][r-1])
	assert allclose(
		(vals[r] - vals[0]) / eps,
		diffs[0][r-1], rtol = .01)
    

if __name__ == '__main__':
    testHyperGaussian()




