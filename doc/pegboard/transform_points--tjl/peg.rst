=============================================================
PEG ``transform_points--tjl``: Point transformations
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Implemented

To make PEG 1011 easier to implement, VobCoorder
needs easy and efficient transformation functions
for AWT.  Here are some.

Changes
-------

Into VobCoorder, add::

    java.awt.Point transformPoint(int cs, 
	    float x, float y, java.awt.Point into);
    Point[] transformPoint2(int cs, float[] coords, Point[] into);

The functions take an "into" parameter, which (if non-null) they
return, in order to avoid object creation overhead.
