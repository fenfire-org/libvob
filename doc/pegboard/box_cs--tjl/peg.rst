=============================================================
PEG box_cs--tjl: Boxes in coorders.
=============================================================

:Author:   Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Implemented

There is an enormous problem with the current Box class: the activated
coordsys is not the parent of the children since the activated cs is
the UnitCoordsys of the box, and the children are usually placed in the 
WHCoordSys.

We can solve this by introducing (as suggested by Benja) width and height
to coordinate systems; here I outline an approach which doesn't cost if 
it's not used.

Issues
======

- Should VobCoorder.unitSq(parent) be allowed to return parent directly,
  if the parent's type is such that its w and h are always 1?
  Because there are no parameters, we needn't worry about setUnitSqParams screwing
  this up (that call doesn't need to be there).

    RESOLVED: yes, explicitly allow this; probably will not be implemented yet, though.

New coordinate systems
======================

The concept of "unit square" is added to all coordinate systems.
It is always a rectangle with one corner at origin and the sides
parallel to the coordinate axes. Thus, it can be described by one 
point: (w, h).

For most coordinate system types, w and h are *defined* to be 1; thus,
they need not be stored and can be simply returned as 1 when requested.

For using them, we define three new coordinate systems:

Box(w,h)
    A unit transformation, but with given w, h.

UnitSq()
    A parameterless transformation, which gives a transformation from
    (0,1)x(0,1) to (0,w)x(0,h) of the parent.

OrthoBox(x, y, sx, sy, w, h)
    An orthogonal transformation; equivalent to Box(w, h) inside Ortho(x, y, sx, sy)

Additionally, the Cull coordsys needs to be altered to pass through the unit
square of its parent, and to use the unit squares of the coordinate systems
to be culled.

Changes
=======

Into VobCoorder, and VobScene, add the usual methods for dealing with the new coordinate systems
above. Change the definition of cull.

Change the definition of getCSAt to use the "unit square" instead of the unit square.

Into VobCoorder, add::

    /** Get the size of the "unit square" of the given coordinate
     * system. This is the size that the unit square of unitSqCS() 
     * would be in the given coordinate system.
     */
    void getSqSize(int cs, float[] into);
