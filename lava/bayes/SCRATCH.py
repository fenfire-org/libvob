use_fast = 1

if use_fast:
    from fastmath import *

    def mulsum(a,b,c):
        a = reshape(a, (-1, size(c)))
        b = reshape(b, (-1, size(c)))
        c = reshape(c, (1, size(c)))
        clear(c)
        c = repeat(c, size(a) / size(c))
        multiply_add(a, b, c)







if 0:
    models = cartesian_product( params )
    models.shape = ( nmodels, len(params[-2]), len(params[-1]), len(params) )

    print shape(models)


    print "Computing psi table..."
    t = time()

    rest = [ models[...,i] for i in range(0, len(params) - 2) ]

    psi_tbl = concatenate((
        psi( models[...,-2] * F**(nr-1), models[...,-1], 1, 0, *rest),
        psi( models[...,-2] * F**(  -1), models[...,-1], 1, 0, *rest),
        ), axis = -2)

    print time() - t




print "Computing models..."

def zeropad(a, left = 0, right = 0):
    "Pad the last dimension with the given numbers of zeros from left and right"
    return concatenate((zeros(shape(a)[:-1] + (left,)),
                        a,
                        zeros(shape(a)[:-1] + (right,))),
                       axis = -1)


def cartesian_product(arrays):
    n = len(arrays)
    lens = map(len, arrays)

    a = []
    for i in range(0, n):
        s = [1,] * (n+1)
        s[i] = lens[i]
        a.append( zeropad(reshape(arrays[i], s), i, n-i-1) )

    return reduce(lambda x,y:x+y, a)


