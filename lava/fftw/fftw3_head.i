//(C): Janne V. Kujala
// SWIG wrapper for fftw3

%module fftw3
%{
#include <fftw3.h>
#include <Numeric/arrayobject.h>
%}

%init 
%{ 
import_array(); 
%}

%typemap(in) int* {
	if ($input == Py_None)
		$1 = NULL;
	else {
		PyArrayObject *a = (PyArrayObject *)$input;
		if (!PyArray_Check(a) || !(a->flags & CONTIGUOUS) ||
		    (a->descr->type_num != PyArray_INT &&
		    (a->descr->type_num != PyArray_LONG || sizeof(long) != sizeof(int)))) {
			PyErr_SetString(PyExc_ValueError, "Expecting a Numeric integer array as parameter '$1_name'");
			SWIG_fail;
		}
		$1 = (int*)a->data;
	}
}

%typemap(in) double* {
	if (!PyArray_Check($input)) {
		PyErr_SetString(PyExc_ValueError, "Expecting a Numeric array as parameter '$1_name'");
		SWIG_fail;
	}
	$1 = (double*)((PyArrayObject *)$input)->data;
}

%typemap(in) long double* {
	if (!PyArray_Check($input)) {
		PyErr_SetString(PyExc_ValueError, "Expecting a Numeric array as parameter '$1_name'");
		SWIG_fail;
	}
	$1 = (long double*)((PyArrayObject *)$input)->data;
}

%typemap(in) float* {
	if (!PyArray_Check($input)) {
		PyErr_SetString(PyExc_ValueError, "Expecting a Numeric array as parameter '$1_name'");
		SWIG_fail;
	}
	$1 = (float*)((PyArrayObject *)$input)->data;
}

%typemap(in) fftw_complex* {
	if (!PyArray_Check($input)) {
		PyErr_SetString(PyExc_ValueError, "Expecting a Numeric array as parameter '$1_name'");
		SWIG_fail;
	}
	$1 = (fftw_complex*)((PyArrayObject *)$input)->data;
}

%typemap(in) fftwf_complex* {
	if (!PyArray_Check($input)) {
		PyErr_SetString(PyExc_ValueError, "Expecting a Numeric array as parameter '$1_name'");
		SWIG_fail;
	}
	$1 = (fftwf_complex*)((PyArrayObject *)$input)->data;
}

%typemap(in) fftwl_complex* {
	if (!PyArray_Check($input)) {
		PyErr_SetString(PyExc_ValueError, "Expecting a Numeric array as parameter '$1_name'");
		SWIG_fail;
	}
	$1 = (fftwl_complex*)((PyArrayObject *)$input)->data;
}

%inline
%{
// Low-level interface to fftw_malloc
// This code depends on the assumption that fftw3 uses malloc()/memalign() 
// and that is is compatible with free()
PyObject *_alignedarray(int n, char type) {
	PyArray_Descr *descr;
	PyArrayObject *a;
	void *data;
	if ((descr = PyArray_DescrFromType(type)) == NULL) {
		PyErr_SetString(PyExc_ValueError, "Invalid type");
		return NULL;
	}
	data = fftw_malloc(n * descr->elsize);
	a = (PyArrayObject *)PyArray_FromDimsAndDataAndDescr(1, &n, descr, data);
	if (!a) {
		free(data);
		return NULL;
	}
	a->flags |= OWN_DATA;
	return (PyObject *)a;
}	
%}

%pythoncode
%{
from Numeric import *
def alignedarray(s, type = Float64):
	s = array(s)
	a = _alignedarray(product(s), type)
	if len(s) > 1: a.shape = s
	return a
%}


%inline
%{
// Some useful inplace functions 
void _mad_r(int n, int howmany, double *a, double *b, double *c) {
	int i;
	for (; howmany--; a += n, b += n)
		for (i = 0; i < n; i++) c[i] += a[i] * b[i];
}

void _madf_r(int n, int howmany, float *a, float *b, float *c) {
	int i;
	for (; howmany--; a += n, b += n)
		for (i = 0; i < n; i++) c[i] += a[i] * b[i];
}

void _mad_c(int n, int howmany, fftw_complex *a, fftw_complex *b, fftw_complex *c) {
	int i;
	for (; howmany--; a += n, b += n)
		for (i = 0; i < n; i++)
			i[(_Complex double*)c] += i[(_Complex double*)a] 
			                        * i[(_Complex double*)b];
}

void _madf_c(int n, int howmany, fftwf_complex *a, fftwf_complex *b, fftwf_complex *c) {
	int i;
	for (; howmany--; a += n, b += n)
		for (i = 0; i < n; i++)
			i[(_Complex float*)c] += i[(_Complex float*)a] 
			                       * i[(_Complex float*)b];
}

%}

%typemap(in) PyArrayObject *contArr {
	$1 = (PyArrayObject*)$input;
	if (!PyArray_Check($1) || !($1->flags & CONTIGUOUS) || 
	    $1->descr->type_num >= PyArray_OBJECT) {
		PyErr_SetString(PyExc_ValueError, 
		                "Expecting a contiguous numeric array");
		SWIG_fail;
	}
}

%inline 
%{
// Clear an array efficiently, without any temporaries in the heap
void clear(PyArrayObject *contArr) {
	int i, n = 1;
	for (i = 0; i < contArr->nd; i++) n *= contArr->dimensions[i];
	memset(contArr->data, 0, contArr->descr->elsize * n);
}



%}

%pythoncode
%{
	def mulsum(a, b, c):
		"Assign c[:] = sum(a * b), summing contiguous blocks of the shape of c"
		assert a.typecode() == b.typecode() == c.typecode()
		assert a.iscontiguous() == b.iscontiguous() == c.iscontiguous()
		assert a.shape == b.shape
		n = size(c)
		assert size(a) % n == 0
		howmany = size(a) / n
		
		if a.typecode() == Float32: func = _madf_r
		elif a.typecode() == Float64: func = _mad_r
		elif a.typecode() == Complex32: func = _madf_c
		elif a.typecode() == Complex64: func = _mad_c

		clear(c)
		func(n, howmany, a, b, c)
%}