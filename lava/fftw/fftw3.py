# This file was created automatically by SWIG.
# Don't modify this file, modify the SWIG interface instead.
# This file is compatible with both classic and new-style classes.
import _fftw3
def _swig_setattr(self,class_type,name,value):
    if (name == "this"):
        if isinstance(value, class_type):
            self.__dict__[name] = value.this
            if hasattr(value,"thisown"): self.__dict__["thisown"] = value.thisown
            del value.thisown
            return
    method = class_type.__swig_setmethods__.get(name,None)
    if method: return method(self,value)
    self.__dict__[name] = value

def _swig_getattr(self,class_type,name):
    method = class_type.__swig_getmethods__.get(name,None)
    if method: return method(self)
    raise AttributeError,name

import types
try:
    _object = types.ObjectType
    _newclass = 1
except AttributeError:
    class _object : pass
    _newclass = 0


_alignedarray = _fftw3._alignedarray

from Numeric import *
def alignedarray(s, type = Float64):
	s = array(s)
	a = _alignedarray(product(s), type)
	if len(s) > 1: a.shape = s
	return a


_mad_r = _fftw3._mad_r

_madf_r = _fftw3._madf_r

_mad_c = _fftw3._mad_c

_madf_c = _fftw3._madf_c

clear = _fftw3.clear

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


fftw_execute = _fftw3.fftw_execute

fftw_plan_dft = _fftw3.fftw_plan_dft

fftw_plan_dft_1d = _fftw3.fftw_plan_dft_1d

fftw_plan_dft_2d = _fftw3.fftw_plan_dft_2d

fftw_plan_dft_3d = _fftw3.fftw_plan_dft_3d

fftw_plan_many_dft = _fftw3.fftw_plan_many_dft

fftw_execute_dft = _fftw3.fftw_execute_dft

fftw_execute_split_dft = _fftw3.fftw_execute_split_dft

fftw_plan_many_dft_r2c = _fftw3.fftw_plan_many_dft_r2c

fftw_plan_dft_r2c = _fftw3.fftw_plan_dft_r2c

fftw_plan_dft_r2c_1d = _fftw3.fftw_plan_dft_r2c_1d

fftw_plan_dft_r2c_2d = _fftw3.fftw_plan_dft_r2c_2d

fftw_plan_dft_r2c_3d = _fftw3.fftw_plan_dft_r2c_3d

fftw_plan_many_dft_c2r = _fftw3.fftw_plan_many_dft_c2r

fftw_plan_dft_c2r = _fftw3.fftw_plan_dft_c2r

fftw_plan_dft_c2r_1d = _fftw3.fftw_plan_dft_c2r_1d

fftw_plan_dft_c2r_2d = _fftw3.fftw_plan_dft_c2r_2d

fftw_plan_dft_c2r_3d = _fftw3.fftw_plan_dft_c2r_3d

fftw_execute_dft_r2c = _fftw3.fftw_execute_dft_r2c

fftw_execute_dft_c2r = _fftw3.fftw_execute_dft_c2r

fftw_execute_split_dft_r2c = _fftw3.fftw_execute_split_dft_r2c

fftw_execute_split_dft_c2r = _fftw3.fftw_execute_split_dft_c2r

fftw_plan_many_r2r = _fftw3.fftw_plan_many_r2r

fftw_plan_r2r = _fftw3.fftw_plan_r2r

fftw_plan_r2r_1d = _fftw3.fftw_plan_r2r_1d

fftw_plan_r2r_2d = _fftw3.fftw_plan_r2r_2d

fftw_plan_r2r_3d = _fftw3.fftw_plan_r2r_3d

fftw_execute_r2r = _fftw3.fftw_execute_r2r

fftw_destroy_plan = _fftw3.fftw_destroy_plan

fftw_forget_wisdom = _fftw3.fftw_forget_wisdom

fftw_cleanup = _fftw3.fftw_cleanup

fftw_export_wisdom_to_file = _fftw3.fftw_export_wisdom_to_file

fftw_export_wisdom_to_string = _fftw3.fftw_export_wisdom_to_string

fftw_export_wisdom = _fftw3.fftw_export_wisdom

fftw_import_system_wisdom = _fftw3.fftw_import_system_wisdom

fftw_import_wisdom_from_file = _fftw3.fftw_import_wisdom_from_file

fftw_import_wisdom_from_string = _fftw3.fftw_import_wisdom_from_string

fftw_import_wisdom = _fftw3.fftw_import_wisdom

fftw_fprint_plan = _fftw3.fftw_fprint_plan

fftw_print_plan = _fftw3.fftw_print_plan

fftw_malloc = _fftw3.fftw_malloc

fftw_free = _fftw3.fftw_free

fftw_flops = _fftw3.fftw_flops

fftwf_execute = _fftw3.fftwf_execute

fftwf_plan_dft = _fftw3.fftwf_plan_dft

fftwf_plan_dft_1d = _fftw3.fftwf_plan_dft_1d

fftwf_plan_dft_2d = _fftw3.fftwf_plan_dft_2d

fftwf_plan_dft_3d = _fftw3.fftwf_plan_dft_3d

fftwf_plan_many_dft = _fftw3.fftwf_plan_many_dft

fftwf_execute_dft = _fftw3.fftwf_execute_dft

fftwf_execute_split_dft = _fftw3.fftwf_execute_split_dft

fftwf_plan_many_dft_r2c = _fftw3.fftwf_plan_many_dft_r2c

fftwf_plan_dft_r2c = _fftw3.fftwf_plan_dft_r2c

fftwf_plan_dft_r2c_1d = _fftw3.fftwf_plan_dft_r2c_1d

fftwf_plan_dft_r2c_2d = _fftw3.fftwf_plan_dft_r2c_2d

fftwf_plan_dft_r2c_3d = _fftw3.fftwf_plan_dft_r2c_3d

fftwf_plan_many_dft_c2r = _fftw3.fftwf_plan_many_dft_c2r

fftwf_plan_dft_c2r = _fftw3.fftwf_plan_dft_c2r

fftwf_plan_dft_c2r_1d = _fftw3.fftwf_plan_dft_c2r_1d

fftwf_plan_dft_c2r_2d = _fftw3.fftwf_plan_dft_c2r_2d

fftwf_plan_dft_c2r_3d = _fftw3.fftwf_plan_dft_c2r_3d

fftwf_execute_dft_r2c = _fftw3.fftwf_execute_dft_r2c

fftwf_execute_dft_c2r = _fftw3.fftwf_execute_dft_c2r

fftwf_execute_split_dft_r2c = _fftw3.fftwf_execute_split_dft_r2c

fftwf_execute_split_dft_c2r = _fftw3.fftwf_execute_split_dft_c2r

fftwf_plan_many_r2r = _fftw3.fftwf_plan_many_r2r

fftwf_plan_r2r = _fftw3.fftwf_plan_r2r

fftwf_plan_r2r_1d = _fftw3.fftwf_plan_r2r_1d

fftwf_plan_r2r_2d = _fftw3.fftwf_plan_r2r_2d

fftwf_plan_r2r_3d = _fftw3.fftwf_plan_r2r_3d

fftwf_execute_r2r = _fftw3.fftwf_execute_r2r

fftwf_destroy_plan = _fftw3.fftwf_destroy_plan

fftwf_forget_wisdom = _fftw3.fftwf_forget_wisdom

fftwf_cleanup = _fftw3.fftwf_cleanup

fftwf_export_wisdom_to_file = _fftw3.fftwf_export_wisdom_to_file

fftwf_export_wisdom_to_string = _fftw3.fftwf_export_wisdom_to_string

fftwf_export_wisdom = _fftw3.fftwf_export_wisdom

fftwf_import_system_wisdom = _fftw3.fftwf_import_system_wisdom

fftwf_import_wisdom_from_file = _fftw3.fftwf_import_wisdom_from_file

fftwf_import_wisdom_from_string = _fftw3.fftwf_import_wisdom_from_string

fftwf_import_wisdom = _fftw3.fftwf_import_wisdom

fftwf_fprint_plan = _fftw3.fftwf_fprint_plan

fftwf_print_plan = _fftw3.fftwf_print_plan

fftwf_malloc = _fftw3.fftwf_malloc

fftwf_free = _fftw3.fftwf_free

fftwf_flops = _fftw3.fftwf_flops

fftwl_execute = _fftw3.fftwl_execute

fftwl_plan_dft = _fftw3.fftwl_plan_dft

fftwl_plan_dft_1d = _fftw3.fftwl_plan_dft_1d

fftwl_plan_dft_2d = _fftw3.fftwl_plan_dft_2d

fftwl_plan_dft_3d = _fftw3.fftwl_plan_dft_3d

fftwl_plan_many_dft = _fftw3.fftwl_plan_many_dft

fftwl_execute_dft = _fftw3.fftwl_execute_dft

fftwl_execute_split_dft = _fftw3.fftwl_execute_split_dft

fftwl_plan_many_dft_r2c = _fftw3.fftwl_plan_many_dft_r2c

fftwl_plan_dft_r2c = _fftw3.fftwl_plan_dft_r2c

fftwl_plan_dft_r2c_1d = _fftw3.fftwl_plan_dft_r2c_1d

fftwl_plan_dft_r2c_2d = _fftw3.fftwl_plan_dft_r2c_2d

fftwl_plan_dft_r2c_3d = _fftw3.fftwl_plan_dft_r2c_3d

fftwl_plan_many_dft_c2r = _fftw3.fftwl_plan_many_dft_c2r

fftwl_plan_dft_c2r = _fftw3.fftwl_plan_dft_c2r

fftwl_plan_dft_c2r_1d = _fftw3.fftwl_plan_dft_c2r_1d

fftwl_plan_dft_c2r_2d = _fftw3.fftwl_plan_dft_c2r_2d

fftwl_plan_dft_c2r_3d = _fftw3.fftwl_plan_dft_c2r_3d

fftwl_execute_dft_r2c = _fftw3.fftwl_execute_dft_r2c

fftwl_execute_dft_c2r = _fftw3.fftwl_execute_dft_c2r

fftwl_execute_split_dft_r2c = _fftw3.fftwl_execute_split_dft_r2c

fftwl_execute_split_dft_c2r = _fftw3.fftwl_execute_split_dft_c2r

fftwl_plan_many_r2r = _fftw3.fftwl_plan_many_r2r

fftwl_plan_r2r = _fftw3.fftwl_plan_r2r

fftwl_plan_r2r_1d = _fftw3.fftwl_plan_r2r_1d

fftwl_plan_r2r_2d = _fftw3.fftwl_plan_r2r_2d

fftwl_plan_r2r_3d = _fftw3.fftwl_plan_r2r_3d

fftwl_execute_r2r = _fftw3.fftwl_execute_r2r

fftwl_destroy_plan = _fftw3.fftwl_destroy_plan

fftwl_forget_wisdom = _fftw3.fftwl_forget_wisdom

fftwl_cleanup = _fftw3.fftwl_cleanup

fftwl_export_wisdom_to_file = _fftw3.fftwl_export_wisdom_to_file

fftwl_export_wisdom_to_string = _fftw3.fftwl_export_wisdom_to_string

fftwl_export_wisdom = _fftw3.fftwl_export_wisdom

fftwl_import_system_wisdom = _fftw3.fftwl_import_system_wisdom

fftwl_import_wisdom_from_file = _fftw3.fftwl_import_wisdom_from_file

fftwl_import_wisdom_from_string = _fftw3.fftwl_import_wisdom_from_string

fftwl_import_wisdom = _fftw3.fftwl_import_wisdom

fftwl_fprint_plan = _fftw3.fftwl_fprint_plan

fftwl_print_plan = _fftw3.fftwl_print_plan

fftwl_malloc = _fftw3.fftwl_malloc

fftwl_free = _fftw3.fftwl_free

fftwl_flops = _fftw3.fftwl_flops

FFTW_BACKWARD = _fftw3.FFTW_BACKWARD
FFTW_MEASURE = _fftw3.FFTW_MEASURE
FFTW_DESTROY_INPUT = _fftw3.FFTW_DESTROY_INPUT
FFTW_UNALIGNED = _fftw3.FFTW_UNALIGNED
FFTW_CONSERVE_MEMORY = _fftw3.FFTW_CONSERVE_MEMORY
FFTW_EXHAUSTIVE = _fftw3.FFTW_EXHAUSTIVE
FFTW_PRESERVE_INPUT = _fftw3.FFTW_PRESERVE_INPUT
FFTW_PATIENT = _fftw3.FFTW_PATIENT
FFTW_ESTIMATE = _fftw3.FFTW_ESTIMATE
FFTW_ESTIMATE_PATIENT = _fftw3.FFTW_ESTIMATE_PATIENT
FFTW_BELIEVE_PCOST = _fftw3.FFTW_BELIEVE_PCOST
FFTW_DFT_R2HC_ICKY = _fftw3.FFTW_DFT_R2HC_ICKY
FFTW_NONTHREADED_ICKY = _fftw3.FFTW_NONTHREADED_ICKY
FFTW_NO_BUFFERING = _fftw3.FFTW_NO_BUFFERING
FFTW_NO_INDIRECT_OP = _fftw3.FFTW_NO_INDIRECT_OP
FFTW_ALLOW_LARGE_GENERIC = _fftw3.FFTW_ALLOW_LARGE_GENERIC
FFTW_NO_RANK_SPLITS = _fftw3.FFTW_NO_RANK_SPLITS
FFTW_NO_VRANK_SPLITS = _fftw3.FFTW_NO_VRANK_SPLITS
FFTW_NO_VRECURSE = _fftw3.FFTW_NO_VRECURSE
FFTW_NO_SIMD = _fftw3.FFTW_NO_SIMD
cvar = _fftw3.cvar

