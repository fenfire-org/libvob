#(c) Tuomas J. Lukka

"""A test case for the hybrid MCMC driver:
fit a set of points using a fourier series.

Uses hyperparameters for both accuracy of fit
and hierarchical hyperparameters for the fourier
series.

Since we use Gaussians for the errors, it's 
"""

class FouFit:
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

	# Two hyperparameters plus the fourier
	# series
	self.nparams = 2 + self.ncoeffs 
    def setPoints(self, points):
	"""

	points --  x coordinates
	"""
	self.pointcoeffs = sin(self.offsets[:,NewAxis] + self.coeffs[:,NewAxis] * (points))

    def evalValues(self, params):
	foucoeffs = params[..., 2:]
	return dot(foucoeffs, self.pointcoeffs)

    
    def evalEnergy(self, params):
	foucoeffs = params[..., 2:]
	
