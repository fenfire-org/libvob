//(c): Janne V. Kujala

#include "Python.h"
#include <string.h>
#include <math.h>

#include <Numeric/arrayobject.h>

#include "bayes_funcs.h"

static PyObject *ErrorObject;

extern PyObject *PyArray_Entropy(PyObject *op) {
    PyArrayObject *ap, *rp;
    char *ip;
    int i, n, m, elsize;
        
    rp = NULL;
    ap = (PyArrayObject *)PyArray_ContiguousFromObject(op, PyArray_DOUBLE, 1, 0);
    if (ap == NULL) return NULL;
        
    rp = (PyArrayObject *)PyArray_FromDims(ap->nd-1, ap->dimensions, PyArray_DOUBLE);
    if (rp == NULL) goto fail;

    elsize = ap->descr->elsize;
    m = ap->dimensions[ap->nd-1];
    if (m == 0) {
        PyErr_SetString(ErrorObject, "Attempt to get entropy of an empty sequence??");
        goto fail;
    }
    n = PyArray_SIZE(ap)/m;
    for (ip = ap->data, i=0; i<n; i++, ip += elsize*m) {
	((double *)rp->data)[i] = get_entropy((double *)ip, m);
    }
    Py_DECREF(ap);
    return PyArray_Return(rp);
        
 fail:
    Py_DECREF(ap);
    Py_XDECREF(rp);
    return NULL;
}  

#define MAX_DIMS 30
extern PyObject *PyArray_Posterior(PyObject *op1, PyObject *op2) {
    PyArrayObject *ap1 = NULL, *ap2 = NULL, *ret1 = NULL, *ret2 = NULL;
    int i;
    int dimensions[MAX_DIMS], nd;
    int ind[MAX_DIMS];

    ap1 = (PyArrayObject *)PyArray_ContiguousFromObject(op1, PyArray_DOUBLE, 1, 0);
    if (ap1 == NULL) return NULL;

    ap2 = (PyArrayObject *)PyArray_ContiguousFromObject(op2, PyArray_FLOAT, 1, 0);
    if (ap2 == NULL) goto fail;
      
    if (ap1->nd == 0 || ap2->nd == 0) {
	PyErr_SetString(PyExc_TypeError, "scalar arguments not allowed");
	goto fail;
    }

    nd = ap1->nd > ap2->nd ? ap1->nd : ap2->nd;
    for (i = 0; i < nd; i++) {
	int n1 = 1, n2 = 1;
	if (i < ap1->nd) n1 = ap1->dimensions[ap1->nd - 1 - i];
	if (i < ap2->nd) n2 = ap2->dimensions[ap2->nd - 1 - i];

	if (n1 != n2 && ((n1 != 1 && n2 != 1) || i == 0)) {
	    PyErr_SetString(PyExc_ValueError, "frames are not aligned");
	    goto fail;
	}
	dimensions[nd - 1 - i] = n1 > n2 ? n1 : n2;
    }

    ret1 = (PyArrayObject *)PyArray_FromDims(nd, dimensions, PyArray_DOUBLE);
    if (ret1 == NULL) goto fail;

    ret2 = (PyArrayObject *)PyArray_FromDims(nd-1, dimensions, PyArray_DOUBLE);
    if (ret2 == NULL) goto fail;

    for (i = 0; i < nd; i++)
	ind[i] = 0;

    {
	char *a1 = ap1->data, *a2 = ap2->data;
	double *r = (double*)ret1->data;
	double *r2 = (double*)ret2->data;
	int l = dimensions[nd - 1];

	while (1) {
	    *r2++ = get_posterior((double *)a1, (float *)a2, 1, r, l);
	    for (i = 1; i < nd; i++) {
		if (i < ap1->nd && ap1->dimensions[ap1->nd - 1 - i] != 1) 
		    a1 += ap1->strides[ap1->nd - 1 - i];
		if (i < ap2->nd && ap2->dimensions[ap2->nd - 1 - i] != 1)
		    a2 += ap2->strides[ap2->nd - 1 - i];

		if (++ind[nd - 1 - i] < dimensions[nd - 1 - i]) break;

		if (i < ap1->nd && ap1->dimensions[ap1->nd - 1 - i] != 1) 
		    a1 -= ap1->strides[ap1->nd - 1 - i] * ind[nd - 1 - i];
		if (i < ap2->nd && ap2->dimensions[ap2->nd - 1 - i] != 1)
		    a2 -= ap2->strides[ap2->nd - 1 - i] * ind[nd - 1 - i];
		    
		ind[nd - 1 - i] = 0;

	    }
	    if (i == nd) break;
	    r += l;
	}
    }
	
    Py_DECREF(ap1);
    Py_DECREF(ap2);

    {
	PyObject *ret = PyTuple_New(2);
	if (ret == NULL) goto fail;
	PyTuple_SET_ITEM(ret, 0, PyArray_Return(ret2));
	PyTuple_SET_ITEM(ret, 1, PyArray_Return(ret1));
	return ret;
    }
        
 fail:
    Py_DECREF(ap1);
    Py_XDECREF(ap2);
    Py_XDECREF(ret1);
    Py_XDECREF(ret2);
    return NULL;
}  


extern PyObject *PyArray_Posterior2(PyObject *op1, PyObject *op2) {
    PyArrayObject *ap1 = NULL, *ap2 = NULL, *ret1 = NULL, *ret2 = NULL;
    int i, l;
    int dimensions[MAX_DIMS+2], nd;
    int ind[MAX_DIMS+2];

    ap1 = (PyArrayObject *)PyArray_ContiguousFromObject(op1, PyArray_DOUBLE, 1, 0);
    if (ap1 == NULL) return NULL;

    ap2 = (PyArrayObject *)PyArray_ContiguousFromObject(op2, PyArray_FLOAT, 1, 0);
    if (ap2 == NULL) goto fail;
      
    if (ap1->nd == 0 || ap2->nd == 0) {
	PyErr_SetString(PyExc_TypeError, "scalar arguments not allowed");
	goto fail;
    }

    nd = ap1->nd > ap2->nd ? ap1->nd : ap2->nd;
    for (i = 0; i < nd; i++) {
	int n1 = 1, n2 = 1;
	if (i < ap1->nd) n1 = ap1->dimensions[ap1->nd - 1 - i];
	if (i < ap2->nd) n2 = ap2->dimensions[ap2->nd - 1 - i];

	if (n1 != n2 && ((n1 != 1 && n2 != 1) || i == 0)) {
	    PyErr_SetString(PyExc_ValueError, "frames are not aligned");
	    goto fail;
	}
	dimensions[nd - 1 - i] = n1 > n2 ? n1 : n2;
    }
    
    l = dimensions[nd - 1];

    dimensions[nd - 1] = 2;
    dimensions[nd] = l;
    ret1 = (PyArrayObject *)PyArray_FromDims(nd+1, dimensions, PyArray_DOUBLE);
    if (ret1 == NULL) goto fail;
    
    dimensions[nd - 1] = 4;
    ret2 = (PyArrayObject *)PyArray_FromDims(nd, dimensions, PyArray_DOUBLE);
    if (ret2 == NULL) goto fail;

    for (i = 0; i <= nd; i++)
	ind[i] = 0;

    {
	char *a1 = ap1->data, *a2 = ap2->data;
	double *r = (double*)ret1->data;
	double *p = (double*)ret2->data;

	while (1) {
#if 0
	    *p++ = get_posterior((double *)a1, (float *)a2, 0, r, l);
	    *p++ = get_posterior((double *)a1, (float *)a2, 1, r + l, l);
	    *p++ = get_entropy(r, l);
	    *p++ = get_entropy(r + l, l);
#else
	    get_posterior2((double*)a1, (float*)a2, r, p, l);
	    p += 4;
#endif
	    r += 2 * l;
	    for (i = 1; i < nd; i++) {
		if (i < ap1->nd && ap1->dimensions[ap1->nd - 1 - i] != 1) 
		    a1 += ap1->strides[ap1->nd - 1 - i];
		if (i < ap2->nd && ap2->dimensions[ap2->nd - 1 - i] != 1)
		    a2 += ap2->strides[ap2->nd - 1 - i];

		if (++ind[nd - 1 - i] < dimensions[nd - 1 - i]) break;

		if (i < ap1->nd && ap1->dimensions[ap1->nd - 1 - i] != 1) 
		    a1 -= ap1->strides[ap1->nd - 1 - i] * ind[nd - 1 - i];
		if (i < ap2->nd && ap2->dimensions[ap2->nd - 1 - i] != 1)
		    a2 -= ap2->strides[ap2->nd - 1 - i] * ind[nd - 1 - i];
		    
		ind[nd - 1 - i] = 0;

	    }
	    if (i == nd) break;
	}
    }
	
    Py_DECREF(ap1);
    Py_DECREF(ap2);

    {
	PyObject *ret = PyTuple_New(2);
	if (ret == NULL) goto fail;
	PyTuple_SET_ITEM(ret, 0, PyArray_Return(ret2));
	PyTuple_SET_ITEM(ret, 1, PyArray_Return(ret1));
	return ret;
    }
        
 fail:
    Py_DECREF(ap1);
    Py_XDECREF(ap2);
    Py_XDECREF(ret1);
    Py_XDECREF(ret2);
    return NULL;
}  



static char doc_entropy[] = "entropy(a). Return log2 entropy of the last dimension of array a.";

static PyObject *array_entropy(PyObject *dummy, PyObject *args) {
    PyObject *a0;
        
    if (!PyArg_ParseTuple(args, "O", &a0)) return NULL;
        
    return PyArray_Entropy(a0);
}


static char doc_posterior[] = "posterior(prior, likelihood). Compute normalized posterior distribution. Returns the pair (p, posterior) where p is the normalizing constant.";

static PyObject *array_posterior(PyObject *dummy, PyObject *args) {
    PyObject *a0, *a1;
        
    if (!PyArg_ParseTuple(args, "OO", &a0, &a1)) return NULL;
        
    return PyArray_Posterior(a0, a1);
}

static char doc_posterior2[] = "posterior2(prior, likelihood)";

static PyObject *array_posterior2(PyObject *dummy, PyObject *args) {
    PyObject *a0, *a1;
        
    if (!PyArg_ParseTuple(args, "OO", &a0, &a1)) return NULL;
        
    return PyArray_Posterior2(a0, a1);
}



static struct PyMethodDef module_methods[] = {
    {"entropy", (PyCFunction)array_entropy, METH_VARARGS, doc_entropy},
    {"posterior", (PyCFunction)array_posterior, METH_VARARGS, doc_posterior},
    {"posterior2", (PyCFunction)array_posterior2, METH_VARARGS, doc_posterior2},
    {NULL,              NULL, 0}                /* sentinel */
};


DL_EXPORT(void) initbayes(void) {
    PyObject *m, *d, *s, *one, *zero;
    int i;
    char *data;
    PyArray_Descr *descr;
        
    /* Create the module and add the functions */
    m = Py_InitModule("bayes", module_methods);

    /* Import the array object */
    import_array();

    /* Add some symbolic constants to the module */
    d = PyModule_GetDict(m);

    ErrorObject = PyString_FromString ("bayes.error");
    PyDict_SetItemString (d, "error", ErrorObject);
        
    s = PyString_FromString("0.01");
    PyDict_SetItemString(d, "__version__", s);
    Py_DECREF(s);
        
    /*Load up the zero and one values for the types.*/
    one = PyInt_FromLong(1);
    zero = PyInt_FromLong(0);
        
    for(i=PyArray_CHAR; i<PyArray_NTYPES; i++) {
        descr = PyArray_DescrFromType(i);
        data = (char *)malloc(descr->elsize);
        memset(data, 0, descr->elsize);
        descr->setitem(one, data);
        descr->one = data;
        data = (char *)malloc(descr->elsize);
        memset(data, 0, descr->elsize);
        descr->setitem(zero, data);
        descr->zero = data;
    }
    Py_DECREF(zero);
    Py_DECREF(one);
        
    /* Check for errors */
    if (PyErr_Occurred())
        Py_FatalError("can't initialize module bayes");
}
