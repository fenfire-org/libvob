=======================================================================
PEG ``transform_parents--tjl``: Coordinate systems with several parents
=======================================================================

:Authors:  Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Accepted [ partly implemented ]

Main benefits
-------------

- More ability for coordinate systems to interpolate to each other
  with less care from the programmer

- Allows buoys and nadir to work inside the C++ code for speed;
  while allowing the buoy to interpolate to a non-buoy.
  
.. contents::


Concepts
--------

Coordinate system type
    E.g. "affine", "rotation", "fisheye", "buoytrans".
    

Derived coordinate system type
    A coordinate system type whose possible transformations
    are a subset of another coordinate system type's transformations,
    implemented internally by generating a set of parameters
    for the parent type.

	For example, the buoytrans coordinate system, which is
	a translation to the location where the buoy should be,
	based on a point in an anchor coordinate system.
	This means that the buoy will move naturally when the
	anchor point moves, instead of linearly interpolating.
	However, the buoytrans function will never transform 
	in any way that's not representable as a pure translation.

Parent coordinate system
    A coordinate system through which the coordinates, having
    been transformed by the child, are transformed::

	p_screen = parent(child(p)),
        
    where ``p`` is the original point (in child coordsys),
    ``child()`` is the transformation of the coordsys itself,
    ``parent()`` is the transformation of the coordsys' parent,
    and ``p_screen`` is the final point in screen coordinates.
    
    All derived coordinate system types have exactly the same
    parent coordinate system(s) and interpretations as the 
    parent coordinate system transformations.

    Currently all coordinate systems have only one parent.

    The theoretical point here is that the parent coordinate
    system's transformation at an uncountable set of locations
    will affect the child coordinate system, unlike with
    determining coordinate systems.

	For example, consider a translation whose parent coordinate
	system is a distortion: all points transformed by the translation
	also get transformed by the distortion: the value
	of the distortion transformation at every point
	is significant.

Determining coordinate system
    A coordinate system which participates in defining another
    coordinate system. That is, when determining the transformation
    to be used by a coordinate system, a finite set of samples
    from the determining coordinate systems can be taken, in addition 
    to the interpolated parameters of the coordinate systems.

    Derived coordinate system types can have entirely different
    sets and interpretations of determining coordinate systems.

	For example, consider again the case of the distortion
	coordinate system as a parent. But this time, let's 
	make an anchor coordinate system inside it, and
	make a buoytrans system use it. This time, only the
	value of the distortion coordinate system at
	the origin of the anchor coordinate system matters!
	This value can be used to create a normal translation.
 
Compatible coordinate systems
    Coordinate systems which are of types ultimately derived from 
    the same coordinate system type.

Changes
-------

This change doesn't affect any frozen APIs yet, and is PEGged only
because of its complexity.

Coordinate systems may accept more than one other coordinate systems
as parameters. The first other coordinate system is the parent,
and the others are determining coordinate systems.

When interpolating coordinate systems of the same type, nothing
changes from the previous code. 

However, when interpolating coordinate systems of different type,

    1. If the types are compatible, their common ancestor is used:
       at both end times, the common ancestor is generated and used
       for interpolation

    2. If not compatible, neither is shown.

For example::

    a = vs1.buoyCS(parent_1, "X", anchorCS, ...)

    b = vs2.translate(parent_2, "X", ...)

would then work as expected.

    (Benja:) ``buoyCS`` would be an addition to ``VobScene``? Also,
    ``translate`` should be ``translateCS`` as of PEG 1009.
