=====================================================================
PEG ``transform_definitions--tjl``: New coordinate system definitions
=====================================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Incomplete

The previous PEGs on coordinate systems were a tad unclear
on the overall goals - hopefully these new methods will
clarify the picture.

For example, a box coordinate system and an aspect ratio
coordinate system (a mapping from (0,w)x(0,1) to (0,1),(0,1))
we can define a padded text coordinate system (i.e. one which
has isotropic coordinates relative to the parent coordinate system)
based only on these two coordinate systems::

    def paddedText(box, aspectratio):
	vs.

Changes
-------


