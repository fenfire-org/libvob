=============================================================
PEG w2sx--tjl: Replace w, h in most places by sx and sy
=============================================================

:Author:   Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:05 $
:Revision: $Revision: 1.1 $
:Status:   Implemented

Many places in the API use w and h like ::

    abstract public int ortho(int into, float depth, float x, float y, float w, float h);

in VobCoorder. This is not right, in the light of the new box coordinate system.
Most instances of w and h should be renamed to sx and sy.

Issues
------

Changes
-------
No semantic changes; just a name clarification.

Pretty trivial.
