//(c): Janne V. Kujala

#include <Python.h>
#include <Numeric/arrayobject.h>
#include <Numeric/ufuncobject.h>

static PyObject *ErrorObject;


static void DOUBLE_mad(char **args, int *dimensions, int *steps, void *func) {
    int i, is1=steps[0],is2=steps[1],os=steps[2], n=dimensions[0];
    char *i1=args[0], *i2=args[1], *op=args[2];
    for(i=0; i<n; i++, i1+=is1, i2+=is2, op+=os) {
	*((double *)op) += *((double *)i1) * *((double *)i2);
    }
}

static void FLOAT_mad(char **args, int *dimensions, int *steps, void *func) {
    int i, is1=steps[0],is2=steps[1],os=steps[2], n=dimensions[0];
    char *i1=args[0], *i2=args[1], *op=args[2];
    for(i=0; i<n; i++, i1+=is1, i2+=is2, op+=os) {
	*((float *)op) += *((float *)i1) * *((float *)i2);
    }
}

static char mad_signatures[] = { 
    PyArray_FLOAT, PyArray_FLOAT, PyArray_FLOAT,  
    PyArray_DOUBLE, PyArray_DOUBLE, PyArray_DOUBLE,  
    PyArray_CFLOAT, PyArray_CFLOAT, PyArray_CFLOAT,  
    PyArray_CDOUBLE, PyArray_CDOUBLE, PyArray_CDOUBLE
};

static void CDOUBLE_mad(char **args, int *dimensions, int *steps, void *func) {
    int i, is1=steps[0],is2=steps[1],os=steps[2], n=dimensions[0];
    char *i1=args[0], *i2=args[1], *op=args[2];
    for(i=0; i<n; i++, i1+=is1, i2+=is2, op+=os) {
	double real = 0[(double *)i1] * 0[(double *)i2] - 1[(double *)i1] * 1[(double *)i2] + 0[(double *)op];
	double imag = 0[(double *)i1] * 1[(double *)i2] + 1[(double *)i1] * 0[(double *)i2] + 1[(double *)op];
	0[(double *)op] = real;
	1[(double *)op] = imag;
    }
}

static void CFLOAT_mad(char **args, int *dimensions, int *steps, void *func) {
    int i, is1=steps[0],is2=steps[1],os=steps[2], n=dimensions[0];
    char *i1=args[0], *i2=args[1], *op=args[2];
    for(i=0; i<n; i++, i1+=is1, i2+=is2, op+=os) {
	float real = 0[(float *)i1] * 0[(float *)i2] - 1[(float *)i1] * 1[(float *)i2] + 0[(float *)op];
	float imag = 0[(float *)i1] * 1[(float *)i2] + 1[(float *)i1] * 0[(float *)i2] + 1[(float *)op];
	0[(float *)op] = real;
	1[(float *)op] = imag;
    }
}

static PyUFuncGenericFunction mad_functions[] = { FLOAT_mad, DOUBLE_mad, CFLOAT_mad, CDOUBLE_mad };
static void * null_data[] = { (void *)NULL, (void *)NULL, (void *)NULL, (void *)NULL };


static char doc_repeat[] = "repeat(a, n, axis=0). Return a slice of a with the given axis repeated n times";

static PyObject *array_repeat(PyObject *dummy, PyObject *args, PyObject *kwds) {
    PyObject *a0;
    PyArrayObject *self, *ret;
    int n, axis=0;
    static char *kwlist[] = {"array", "n", "axis", NULL};
	
    if (!PyArg_ParseTupleAndKeywords(args, kwds, "Oi|i", kwlist, 
                                     &a0, &n, &axis)) return NULL;
	
    if (n < 0) {
	PyErr_SetString(PyExc_ValueError, "n is invalid");
	return NULL;
    }
	
    self = (PyArrayObject *)PyArray_FromObject(a0, PyArray_NOTYPE, 0, 0);
    if (self == NULL) return NULL;

    if (axis < 0) axis = self->nd + axis;
    if (axis < 0 || axis >= self->nd) {
	PyErr_SetString(PyExc_ValueError, "axis is invalid");
	Py_DECREF(self);
	return NULL;
    }
    
    if (self->dimensions[axis] != 1) {
	PyErr_SetString(PyExc_ValueError, "length of the repeated axis must be one");
	Py_DECREF(self);
	return NULL;
    }

    //------------

    self->dimensions[0] = n;
    ret = (PyArrayObject *)PyArray_FromDimsAndDataAndDescr(self->nd, self->dimensions, self->descr, self->data);
    self->dimensions[0] = 1;
       
    ret->flags &= ~CONTIGUOUS;
    if (self->flags & SAVESPACE) ret->flags |= SAVESPACE;

    memmove(ret->strides, self->strides, sizeof(int)*self->nd);
    ret->base = (PyObject *)self;

    ret->strides[axis] = 0;
   	
    return (PyObject *)ret;
}

static struct PyMethodDef module_methods[] = {
    {"repeat",	(PyCFunction)array_repeat, METH_VARARGS|METH_KEYWORDS, doc_repeat},
    {NULL,              NULL, 0}                /* sentinel */
};

DL_EXPORT(void) initfastmath(void) {
    PyObject *m, *d, *s, *f;

    /* Create the module and add the functions */
    m = Py_InitModule("fastmath", module_methods);

    /* Import the array and ufunc objects */
    import_array();
    import_ufunc();

    /* Add some symbolic constants to the module */
    d = PyModule_GetDict(m);

    f = PyUFunc_FromFuncAndData(mad_functions, null_data, mad_signatures, sizeof(mad_functions) / sizeof(mad_functions[0]),
				2, 1, PyUFunc_One, "multiply_add", 
				"Multiply the arguments elementwise and add the result to the return argument.", 0);
    PyDict_SetItemString(d, "multiply_add", f);
    Py_DECREF(f);

    ErrorObject = PyString_FromString ("fastmath.error");
    PyDict_SetItemString (d, "error", ErrorObject);
        
    s = PyString_FromString("0.01");
    PyDict_SetItemString(d, "__version__", s);
    Py_DECREF(s);
        
    /* Check for errors */
    if (PyErr_Occurred())
        Py_FatalError("can't initialize module fastmath");
}
