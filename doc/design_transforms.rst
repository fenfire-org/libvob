===================================
Design of LibVob C++ transforms
===================================

In order to make the library extensible,
enabling hierarchical transforms and optimizations,
the transforms are created from simple PrimitiveTransform
objects via templates.

The main idea is that the source code of each
primitivetransform will need to provide only
those methods relevant to it, as indicated
by deriving the main class from the *tag interfaces*.

The tag interfaces are

``PrimitiveTransform``
    Basic class that just says the class is a primitive 
    transform

``ParametrizedPrimitiveTransform``
    The transform takes float parameters from outside.

``GLPerformablePrimitiveTransform``
    It is possible to perform the transform through
    the OpenGL fixed matrix vertex pipeline.

``NonlinearPrimitiveTransform``
    The output coordinates depend nonlinearly on
    the input.

``BoxPrimitiveTransform``
    The transform sets the size of the output box.

XXX tjl

    - examples
