================================================================================
PEG ``vob_transforms--tjl``: Reform the coordinate systems passed to ``TextVob``
================================================================================

:Authors:  Tuomas Lukka, Benja Fallenstein
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Implemented


Current situation
-----------------

We currently use two coordinate systems for each ``TextVob``, one giving the
transformation (upper-left corner, scaling etc.) of the vob, and one giving
the size (width and height). This is more than a bit ugly.


Proposed solution
-----------------

Mostly, we *don't* want to pass both the height and width of the text
to the text rendering routine. We just want to prescribe the height,
the width being determined accordingly.

The height needs to be passed in a coordsys to enable interpolation
(very desirable). Also, the origin of the text, and the direction.

The solution is to pass nothing about what should be done to the width
of the string, but simply the unit square of the font.  The font will 
have its normal aspect ratio if the unit vectors are equal, i.e. 
if the distances (0,1)..(0,0) and (0,1)..(1,1) are the same.

TextVob will take a flag ``baselined``, indicating whether the baseline
should be at y=1 or not. That is, ``baselined`` affects the layout
in the following way:

true
    The baseline will be at y=1. This allows different fonts to be
    rendered on the same line by matching their baselines
false
    The baseline will be between y=1 and y=0, so that as much of the
    text as possible will fit. This allows easy placement of a single
    line (or several lines) of text of the same font into a box.
    In Java terms, baseline y coord = 1-descent.

A table-driven lookup needs to be done to find the correct point size
for a given height in the AWT rendering code.
