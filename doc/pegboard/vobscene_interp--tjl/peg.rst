=============================================================
PEG vobscene_interp--tjl: 
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Implemented
:Affects-PEGs: vob_considerations--benja, vobcoorder_parents--tjl, vobscene_hierarchies_clicks--tjl
:Type:		Architecture
:Scope: 	Limited
:Stakeholders:	mudyc, benja

As witnessed by mudyc, vobscene interpolation needs a bit of adjustment:
it is very easy to accidentally get non-interpolating behaviour, especially
when reparenting coordinate systems.

This PEG clarifies the relationship of the different hierarchies
and sets simple principles for interpolation.

Issues
======

- Are there compatibility issues with this change?

    RESOLVED: Not really. This change only affects the frames rendered during 
    interpolation, and mostly by allowing interpolation where there previously
    was none.
    
    If some coordinate systems are defined without being careful, this MIGHT lead
    to floating-point overflows that did not occur previously, but this is unlikely.
    
    For some situations, it may remove a jumping behaviour, which should be a desirable
    change in just about all situations...

- What about the situation where we have ::

	vs1.cs1 = translate(0, null, 500, 500)
	vs1.cs2 = rotate(cs1, "A", 50)

	vs2.cs1 = translate(0, null, 500, 500)
	vs2.cs2 = rotate(cs1, "A", 150)

  The cs1 systems do not have keys but cs2 are to be interpolated
  to each other.

  If the rotations are interpolated point-wise, the coordinate system
  will shrink during the animation; if parameter-wise, the animation
  will be just a rotation. 

  Should the system try to discover that cs1s are the same and interpolate
  parameter-wise?

    RESOLVED: No. This would be too much work at a time-critical stage,
    and also error-prone. It is possible to add this later, but for now,
    point-wise interpolation is ok.

    The result should be easy to detect and fix in most situations.





Principles
==========

In the following, vs1.cs is a coordinate system of the first vobscene.

- If vs1.cs and vs2.cs match in the VobMatcher tree, then their transformations
  will be interpolated to each other. The types of the coordinate systems,
  their parents in the vobcoorder hierarchy and their determining coordinate systems 
  shall not affect this.

- If vs1.cs and vs2.cs are to be interpolated and are of the same type and
  share interpolating parents, they will be interpolated parameter-wise.


Changes
=======

If two completely unrelated coordinate systems are to be interpolated,
linear (or other) interpolation of the **transformed points** shall be
used as a fallback. This means that a point ``x`` will be transformed as::

    interp.cs(x) = (1-fract)*vs1.cs(x) + fract*vs2.cs(x)

where ``interp.cs`` is the interpolated coordinate system at fraction ``fract``
and ``cs(x)`` is ``x`` transformed according to ``cs``.

We shall not define the exact circumstances when this shall happen;
we shall define one circumstance where this shall not happen: if 
the types of the coordinate systems, their parents and their keys are identical
in the two vobscenes, then parameterwise interpolation shall be used.
