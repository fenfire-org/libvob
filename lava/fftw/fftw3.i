// GENERATED FILE - DO NOT EDIT
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

%typemap(in) PyArrayObject* {
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
void clear(PyArrayObject *a) {
	int i, n = 1;
	for (i = 0; i < a->nd; i++) n *= a->dimensions[i];
	memset(a->data, 0, a->descr->elsize * n);
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
%}typedef double fftw_complex[2];
typedef struct fftw_plan_s *fftw_plan;
void fftw_execute(const fftw_plan p);
fftw_plan fftw_plan_dft(int rank, const int *n, fftw_complex *in, fftw_complex *out, int sign, unsigned flags);
fftw_plan fftw_plan_dft_1d(int n, fftw_complex *in, fftw_complex *out, int sign, unsigned flags);
fftw_plan fftw_plan_dft_2d(int nx, int ny, fftw_complex *in, fftw_complex *out, int sign, unsigned flags);
fftw_plan fftw_plan_dft_3d(int nx, int ny, int nz, fftw_complex *in, fftw_complex *out, int sign, unsigned flags);
fftw_plan fftw_plan_many_dft(int rank, const int *n, int howmany, fftw_complex *in, const int *inembed, int istride, int idist, fftw_complex *out, const int *onembed, int ostride, int odist, int sign, unsigned flags);
void fftw_execute_dft(const fftw_plan p, fftw_complex *in, fftw_complex *out);
void fftw_execute_split_dft(const fftw_plan p, double *ri, double *ii, double *ro, double *io);
fftw_plan fftw_plan_many_dft_r2c(int rank, const int *n, int howmany, double *in, const int *inembed, int istride, int idist, fftw_complex *out, const int *onembed, int ostride, int odist, unsigned flags);
fftw_plan fftw_plan_dft_r2c(int rank, const int *n, double *in, fftw_complex *out, unsigned flags);
fftw_plan fftw_plan_dft_r2c_1d(int n,double *in,fftw_complex *out,unsigned flags);
fftw_plan fftw_plan_dft_r2c_2d(int nx, int ny, double *in, fftw_complex *out, unsigned flags);
fftw_plan fftw_plan_dft_r2c_3d(int nx, int ny, int nz, double *in, fftw_complex *out, unsigned flags);
fftw_plan fftw_plan_many_dft_c2r(int rank, const int *n, int howmany, fftw_complex *in, const int *inembed, int istride, int idist, double *out, const int *onembed, int ostride, int odist, unsigned flags);
fftw_plan fftw_plan_dft_c2r(int rank, const int *n, fftw_complex *in, double *out, unsigned flags);
fftw_plan fftw_plan_dft_c2r_1d(int n,fftw_complex *in,double *out,unsigned flags);
fftw_plan fftw_plan_dft_c2r_2d(int nx, int ny, fftw_complex *in, double *out, unsigned flags);
fftw_plan fftw_plan_dft_c2r_3d(int nx, int ny, int nz, fftw_complex *in, double *out, unsigned flags);
void fftw_execute_dft_r2c(const fftw_plan p, double *in, fftw_complex *out);
void fftw_execute_dft_c2r(const fftw_plan p, fftw_complex *in, double *out);
void fftw_execute_split_dft_r2c(const fftw_plan p, double *in, double *ro, double *io);
void fftw_execute_split_dft_c2r(const fftw_plan p, double *ri, double *ii, double *out);
fftw_plan fftw_plan_many_r2r(int rank, const int *n, int howmany, double *in, const int *inembed, int istride, int idist, double *out, const int *onembed, int ostride, int odist, const fftw_r2r_kind *kind, unsigned flags);
fftw_plan fftw_plan_r2r(int rank, const int *n, double *in, double *out, const fftw_r2r_kind *kind, unsigned flags);
fftw_plan fftw_plan_r2r_1d(int n, double *in, double *out, fftw_r2r_kind kind, unsigned flags);
fftw_plan fftw_plan_r2r_2d(int nx, int ny, double *in, double *out, fftw_r2r_kind kindx, fftw_r2r_kind kindy, unsigned flags);
fftw_plan fftw_plan_r2r_3d(int nx, int ny, int nz, double *in, double *out, fftw_r2r_kind kindx, fftw_r2r_kind kindy, fftw_r2r_kind kindz, unsigned flags);
void fftw_execute_r2r(const fftw_plan p, double *in, double *out);
void fftw_destroy_plan(fftw_plan p);
void fftw_forget_wisdom(void);
void fftw_cleanup(void);
void fftw_export_wisdom_to_file(FILE *output_file);
char *fftw_export_wisdom_to_string(void);
void fftw_export_wisdom(void (*write_char)(char c, void *), void *data);
int fftw_import_system_wisdom(void);
int fftw_import_wisdom_from_file(FILE *input_file);
int fftw_import_wisdom_from_string(const char *input_string);
int fftw_import_wisdom(int (*read_char)(void *), void *data);
void fftw_fprint_plan(const fftw_plan p, FILE *output_file);
void fftw_print_plan(const fftw_plan p);
void *fftw_malloc(size_t n);
void fftw_free(void *p);
void fftw_flops(const fftw_plan p, double *add, double *mul, double *fma);
extern const char fftw_version[];
extern const char fftw_cc[];
extern const char fftw_codelet_optim[];
typedef float fftwf_complex[2];
typedef struct fftwf_plan_s *fftwf_plan;
void fftwf_execute(const fftwf_plan p);
fftwf_plan fftwf_plan_dft(int rank, const int *n, fftwf_complex *in, fftwf_complex *out, int sign, unsigned flags);
fftwf_plan fftwf_plan_dft_1d(int n, fftwf_complex *in, fftwf_complex *out, int sign, unsigned flags);
fftwf_plan fftwf_plan_dft_2d(int nx, int ny, fftwf_complex *in, fftwf_complex *out, int sign, unsigned flags);
fftwf_plan fftwf_plan_dft_3d(int nx, int ny, int nz, fftwf_complex *in, fftwf_complex *out, int sign, unsigned flags);
fftwf_plan fftwf_plan_many_dft(int rank, const int *n, int howmany, fftwf_complex *in, const int *inembed, int istride, int idist, fftwf_complex *out, const int *onembed, int ostride, int odist, int sign, unsigned flags);
void fftwf_execute_dft(const fftwf_plan p, fftwf_complex *in, fftwf_complex *out);
void fftwf_execute_split_dft(const fftwf_plan p, float *ri, float *ii, float *ro, float *io);
fftwf_plan fftwf_plan_many_dft_r2c(int rank, const int *n, int howmany, float *in, const int *inembed, int istride, int idist, fftwf_complex *out, const int *onembed, int ostride, int odist, unsigned flags);
fftwf_plan fftwf_plan_dft_r2c(int rank, const int *n, float *in, fftwf_complex *out, unsigned flags);
fftwf_plan fftwf_plan_dft_r2c_1d(int n,float *in,fftwf_complex *out,unsigned flags);
fftwf_plan fftwf_plan_dft_r2c_2d(int nx, int ny, float *in, fftwf_complex *out, unsigned flags);
fftwf_plan fftwf_plan_dft_r2c_3d(int nx, int ny, int nz, float *in, fftwf_complex *out, unsigned flags);
fftwf_plan fftwf_plan_many_dft_c2r(int rank, const int *n, int howmany, fftwf_complex *in, const int *inembed, int istride, int idist, float *out, const int *onembed, int ostride, int odist, unsigned flags);
fftwf_plan fftwf_plan_dft_c2r(int rank, const int *n, fftwf_complex *in, float *out, unsigned flags);
fftwf_plan fftwf_plan_dft_c2r_1d(int n,fftwf_complex *in,float *out,unsigned flags);
fftwf_plan fftwf_plan_dft_c2r_2d(int nx, int ny, fftwf_complex *in, float *out, unsigned flags);
fftwf_plan fftwf_plan_dft_c2r_3d(int nx, int ny, int nz, fftwf_complex *in, float *out, unsigned flags);
void fftwf_execute_dft_r2c(const fftwf_plan p, float *in, fftwf_complex *out);
void fftwf_execute_dft_c2r(const fftwf_plan p, fftwf_complex *in, float *out);
void fftwf_execute_split_dft_r2c(const fftwf_plan p, float *in, float *ro, float *io);
void fftwf_execute_split_dft_c2r(const fftwf_plan p, float *ri, float *ii, float *out);
fftwf_plan fftwf_plan_many_r2r(int rank, const int *n, int howmany, float *in, const int *inembed, int istride, int idist, float *out, const int *onembed, int ostride, int odist, const fftwf_r2r_kind *kind, unsigned flags);
fftwf_plan fftwf_plan_r2r(int rank, const int *n, float *in, float *out, const fftwf_r2r_kind *kind, unsigned flags);
fftwf_plan fftwf_plan_r2r_1d(int n, float *in, float *out, fftwf_r2r_kind kind, unsigned flags);
fftwf_plan fftwf_plan_r2r_2d(int nx, int ny, float *in, float *out, fftwf_r2r_kind kindx, fftwf_r2r_kind kindy, unsigned flags);
fftwf_plan fftwf_plan_r2r_3d(int nx, int ny, int nz, float *in, float *out, fftwf_r2r_kind kindx, fftwf_r2r_kind kindy, fftwf_r2r_kind kindz, unsigned flags);
void fftwf_execute_r2r(const fftwf_plan p, float *in, float *out);
void fftwf_destroy_plan(fftwf_plan p);
void fftwf_forget_wisdom(void);
void fftwf_cleanup(void);
void fftwf_export_wisdom_to_file(FILE *output_file);
char *fftwf_export_wisdom_to_string(void);
void fftwf_export_wisdom(void (*write_char)(char c, void *), void *data);
int fftwf_import_system_wisdom(void);
int fftwf_import_wisdom_from_file(FILE *input_file);
int fftwf_import_wisdom_from_string(const char *input_string);
int fftwf_import_wisdom(int (*read_char)(void *), void *data);
void fftwf_fprint_plan(const fftwf_plan p, FILE *output_file);
void fftwf_print_plan(const fftwf_plan p);
void *fftwf_malloc(size_t n);
void fftwf_free(void *p);
void fftwf_flops(const fftwf_plan p, double *add, double *mul, double *fma);
extern const char fftwf_version[];
extern const char fftwf_cc[];
extern const char fftwf_codelet_optim[];
typedef long double fftwl_complex[2];
typedef struct fftwl_plan_s *fftwl_plan;
void fftwl_execute(const fftwl_plan p);
fftwl_plan fftwl_plan_dft(int rank, const int *n, fftwl_complex *in, fftwl_complex *out, int sign, unsigned flags);
fftwl_plan fftwl_plan_dft_1d(int n, fftwl_complex *in, fftwl_complex *out, int sign, unsigned flags);
fftwl_plan fftwl_plan_dft_2d(int nx, int ny, fftwl_complex *in, fftwl_complex *out, int sign, unsigned flags);
fftwl_plan fftwl_plan_dft_3d(int nx, int ny, int nz, fftwl_complex *in, fftwl_complex *out, int sign, unsigned flags);
fftwl_plan fftwl_plan_many_dft(int rank, const int *n, int howmany, fftwl_complex *in, const int *inembed, int istride, int idist, fftwl_complex *out, const int *onembed, int ostride, int odist, int sign, unsigned flags);
void fftwl_execute_dft(const fftwl_plan p, fftwl_complex *in, fftwl_complex *out);
void fftwl_execute_split_dft(const fftwl_plan p, long double *ri, long double *ii, long double *ro, long double *io);
fftwl_plan fftwl_plan_many_dft_r2c(int rank, const int *n, int howmany, long double *in, const int *inembed, int istride, int idist, fftwl_complex *out, const int *onembed, int ostride, int odist, unsigned flags);
fftwl_plan fftwl_plan_dft_r2c(int rank, const int *n, long double *in, fftwl_complex *out, unsigned flags);
fftwl_plan fftwl_plan_dft_r2c_1d(int n,long double *in,fftwl_complex *out,unsigned flags);
fftwl_plan fftwl_plan_dft_r2c_2d(int nx, int ny, long double *in, fftwl_complex *out, unsigned flags);
fftwl_plan fftwl_plan_dft_r2c_3d(int nx, int ny, int nz, long double *in, fftwl_complex *out, unsigned flags);
fftwl_plan fftwl_plan_many_dft_c2r(int rank, const int *n, int howmany, fftwl_complex *in, const int *inembed, int istride, int idist, long double *out, const int *onembed, int ostride, int odist, unsigned flags);
fftwl_plan fftwl_plan_dft_c2r(int rank, const int *n, fftwl_complex *in, long double *out, unsigned flags);
fftwl_plan fftwl_plan_dft_c2r_1d(int n,fftwl_complex *in,long double *out,unsigned flags);
fftwl_plan fftwl_plan_dft_c2r_2d(int nx, int ny, fftwl_complex *in, long double *out, unsigned flags);
fftwl_plan fftwl_plan_dft_c2r_3d(int nx, int ny, int nz, fftwl_complex *in, long double *out, unsigned flags);
void fftwl_execute_dft_r2c(const fftwl_plan p, long double *in, fftwl_complex *out);
void fftwl_execute_dft_c2r(const fftwl_plan p, fftwl_complex *in, long double *out);
void fftwl_execute_split_dft_r2c(const fftwl_plan p, long double *in, long double *ro, long double *io);
void fftwl_execute_split_dft_c2r(const fftwl_plan p, long double *ri, long double *ii, long double *out);
fftwl_plan fftwl_plan_many_r2r(int rank, const int *n, int howmany, long double *in, const int *inembed, int istride, int idist, long double *out, const int *onembed, int ostride, int odist, const fftwl_r2r_kind *kind, unsigned flags);
fftwl_plan fftwl_plan_r2r(int rank, const int *n, long double *in, long double *out, const fftwl_r2r_kind *kind, unsigned flags);
fftwl_plan fftwl_plan_r2r_1d(int n, long double *in, long double *out, fftwl_r2r_kind kind, unsigned flags);
fftwl_plan fftwl_plan_r2r_2d(int nx, int ny, long double *in, long double *out, fftwl_r2r_kind kindx, fftwl_r2r_kind kindy, unsigned flags);
fftwl_plan fftwl_plan_r2r_3d(int nx, int ny, int nz, long double *in, long double *out, fftwl_r2r_kind kindx, fftwl_r2r_kind kindy, fftwl_r2r_kind kindz, unsigned flags);
void fftwl_execute_r2r(const fftwl_plan p, long double *in, long double *out);
void fftwl_destroy_plan(fftwl_plan p);
void fftwl_forget_wisdom(void);
void fftwl_cleanup(void);
void fftwl_export_wisdom_to_file(FILE *output_file);
char *fftwl_export_wisdom_to_string(void);
void fftwl_export_wisdom(void (*write_char)(char c, void *), void *data);
int fftwl_import_system_wisdom(void);
int fftwl_import_wisdom_from_file(FILE *input_file);
int fftwl_import_wisdom_from_string(const char *input_string);
int fftwl_import_wisdom(int (*read_char)(void *), void *data);
void fftwl_fprint_plan(const fftwl_plan p, FILE *output_file);
void fftwl_print_plan(const fftwl_plan p);
void *fftwl_malloc(size_t n);
void fftwl_free(void *p);
void fftwl_flops(const fftwl_plan p, double *add, double *mul, double *fma);
extern const char fftwl_version[];
extern const char fftwl_cc[];
extern const char fftwl_codelet_optim[];
#define FFTW3_H
#define FFTW_CONCAT(prefix, name) prefix ## name
#define FFTW_MANGLE_DOUBLE(name) FFTW_CONCAT(fftw_, name)
#define FFTW_MANGLE_FLOAT(name) FFTW_CONCAT(fftwf_, name)
#define FFTW_MANGLE_LONG_DOUBLE(name) FFTW_CONCAT(fftwl_, name)
#define FFTW_DEFINE_API(X, R, C)					\
#define FFTW_FORWARD (-1)
#define FFTW_BACKWARD (+1)
#define FFTW_MEASURE (0U)
#define FFTW_DESTROY_INPUT (1U << 0)
#define FFTW_UNALIGNED (1U << 1)
#define FFTW_CONSERVE_MEMORY (1U << 2)
#define FFTW_EXHAUSTIVE (1U << 3) /* NO_EXHAUSTIVE is default */
#define FFTW_PRESERVE_INPUT (1U << 4) /* cancels FFTW_DESTROY_INPUT */
#define FFTW_PATIENT (1U << 5) /* IMPATIENT is default */
#define FFTW_ESTIMATE (1U << 6)
#define FFTW_ESTIMATE_PATIENT (1U << 7)
#define FFTW_BELIEVE_PCOST (1U << 8)
#define FFTW_DFT_R2HC_ICKY (1U << 9)
#define FFTW_NONTHREADED_ICKY (1U << 10)
#define FFTW_NO_BUFFERING (1U << 11)
#define FFTW_NO_INDIRECT_OP (1U << 12)
#define FFTW_ALLOW_LARGE_GENERIC (1U << 13) /* NO_LARGE_GENERIC is default */
#define FFTW_NO_RANK_SPLITS (1U << 14)
#define FFTW_NO_VRANK_SPLITS (1U << 15)
#define FFTW_NO_VRECURSE (1U << 16)
#define FFTW_NO_SIMD (1U << 17)
