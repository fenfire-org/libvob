from Numeric import *
from fftw3 import *
from time import time

# These are for comparisons
from FFT import *
from MLab import rand


if 1:
    a=zeros(16,Float64)
    b=zeros(16/2+1,Complex64)

    assert a.iscontiguous()
    assert b.iscontiguous()

    print size(a),a.itemsize()
    print size(b),b.itemsize()

    plan=fftw_plan_dft_r2c_1d(16, a, b, FFTW_MEASURE)

    a[:] = arange(0,16)
    print "arrays before:"
    print a
    print b
    print "Plan:", plan
    fftw_print_plan(plan)
    print
    fftw_execute(plan)
    print "arrays after executing"
    print a
    print b
    print "result with NumPy's fft:"
    print real_fft(a)



    print
    print "Trying planning many"
    b=zeros(4*3,Complex64)
    plan2 = fftw_plan_many_dft_r2c(1, array(4), 4,
                                   a, None, 1, 4,
                                   b, None, 1, 3,
                                   FFTW_MEASURE + FFTW_PRESERVE_INPUT);
    a[:] = arange(0,16)
    print a

    fftw_execute(plan2)
    
    print b

    print concatenate([ real_fft(a[i:i+4]) for i in range(0,16,4) ])

if 1:
    n = 1024
    m = 5000

    print
    print "Trying a large array of fft's"

    print "Allocating properly aligned memory..."
    a = alignedarray((m,n), Float64)
    b = alignedarray((m,n/2+1), Complex64)
    
    print "Zeroing arrays..."; t = time()
    clear(a)
    clear(b)
    print "t=%.3f" % (time() - t)
    
    print "Planning..."; t = time()
    plan = fftw_plan_many_dft_r2c(1, array(n), m,
                                  a, None, 1, n,
                                  b, None, 1, n/2+1,
                                  FFTW_MEASURE + FFTW_PRESERVE_INPUT)
    print "t=%.3f" % (time() - t)

    print "Creating random data..."; t = time()
    a[...] = rand(m,n)
    print "t=%.3f" % (time() - t)

    print "Transforming..."; t = time()
    fftw_execute(plan)
    print "t=%.3f" % (time() - t)

    print "Trying the same with NumPy's fft:"; t = time()
    c = real_fft(a, axis = -1)
    print "t=%.3f" % (time() - t)

    e = abs(b - c)
    print "|diff| <= ", max(e.flat)

if 1:
    n = 1024
    m = 5000

    print
    print "Trying a large array of fft's in fortran order"

    print "Allocating properly aligned memory..."
    a = alignedarray((n,m), Float64)
    b = alignedarray((n/2+1,m), Complex64)
    
    print "Zeroing arrays..."; t = time()
    clear(a)
    clear(b)
    print "t=%.3f" % (time() - t)

    print "Planning..."; t = time()
    plan = fftw_plan_many_dft_r2c(1, array(n), m,
                                  a, None, m, 1,
                                  b, None, m, 1,
                                  FFTW_MEASURE + FFTW_PRESERVE_INPUT)
    print "t=%.3f" % (time() - t)

    print "Creating random data..."; t = time()
    a[...] = rand(n,m)
    print "t=%.3f" % (time() - t)

    print "Transforming..."; t = time()
    fftw_execute(plan)
    print "t=%.3f" % (time() - t)

    print "Trying the same with NumPy's fft:"; t = time()
    c = real_fft(a, axis = 0)
    print "t=%.3f" % (time() - t)

    e = abs(b - c)
    print "|diff| <= ", max(e.flat)

    
if 1:
    n = 1024
    m = 2000

    a0 = rand(m,n) + 1j * rand(m,n) - (.5+.5j)
    b0 = rand(m,n) + 1j * rand(m,n) - (.5+.5j)

    for type in [ Float32, Float64, Complex32, Complex64 ]:
        print
        print "Testing mulsum with type %s..." % type

        a = a0.astype(type)
        b = b0.astype(type)
        c = zeros(n, type)

        print "Calling mulsum..."; t = time()
        mulsum(a, b, c)
        print "t=%.3f" % (time() - t)

        print "NumPy's multiply and sum..."; t = time()
        d = sum(a * b)
        print "t=%.3f" % (time() - t)

        e = abs(d - c)
        print "|diff| <= %.2G (%.2G %%)" % (max(e.flat), max(e.flat / abs(d.flat)) * 100)
