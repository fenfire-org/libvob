===================================
Design of the C++ side of LibVob
===================================

This document discusses only the C++ side; the interface
between C++ and Java is discussed elsewhere.

In order to make the code reusable by
decoupling different aspects as well as possible,
we have settled on the following package structure:

.. UML:: vob_c_packages

    package TransformInterfaces

    package TransformsImpl
	use TransformInterfaces

    package VobInterfaces
	use TransformInterfaces

    package VobSceneImpl
	use VobInterfaces
	use TransformInterfaces

    package VobImpl
	use VobInterfaces

    ---

    TransformInterfaces.c = (0,0);

    TransformsImpl.c = (-100, 100);
    VobInterfaces.c = (100, 40);

    VobSceneImpl.c = (0, 150);
    VobImpl.c = (130, 190);

This way the relationships between the different
aspects of the system go through well-defined
interfaces in the packages TransformInterfaces and VobInterfaces.

Interfaces
==========

The transform interfaces and vob interfaces are relatively simple,
defined in the include files ``vob/Transform.hxx`` and ``vob/Vob.hxx``.

The central interfaces on the C side are ``Vob`` and
``Transform``, corresponding to the Java side Vob design.

.. UML-refer:: vobs_overall_2

In C++, however, transforms are *real* classes since the overhead
is not significant.

Implementations: Code Generation
================================

The TransformsImpl and VobImpl packages in the above diagram are a different
story: in order to support extensibility and multiple language bindings,
we rely on code generation and templates to create the glue code
from a simple definition of the actual functionality.

The details of creating each type of class are documented
separately (design_vobs, design_transforms)

.. UML-refer:: design_navigation

A basic concept is the PrimitiveTransform, which is a simple
parametrized function of points to points.
The public members are as follows::

    class PrimitiveTransform {
	enum { NParams = ???
	};
	template<class Ptr> void setParams(Ptr p) ;
	bool canPerformGL() ;
	void inverseTransform(InverseType &into) {
	}

	void tr(const ZPt &from, ZPt &to) const ;
        bool performGL() ;
	
    }

In order to maximize efficiency, none of the member functions are virtual.
