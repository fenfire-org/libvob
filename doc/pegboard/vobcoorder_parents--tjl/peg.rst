=============================================================
PEG vobcoorder_parents--tjl: 
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Accepted

For the mouse click stuff to work properly with the Box API, we need
to be able to query parents etc. That's a nice piece of functionality 
anyway.

The connection to the Box API is as follows: the mouse clicks are determined inside
the unit square (see PEG 1021). For boxes, the unit square is a separate coordinate
system from the box itself and will always have the same key (Box.UNITCOORDSYSKEY).
Because of this, getting the parent is necessary when interpreting the mouse click.


Issues
------

- What should getParent(0) return?

    RESOLVED: It should return -1.

    The reasonable alternatives would be 0 and -1, and 0
    would easily lead to infinite loops. With -1, it's easy to go backwards
    until ``cs<0``.



Changes
-------

Add into VobCoorder::

    /** Get the primary parent of the given coordinate system.
     * getParent(0) will return -1.
     * Always equal to getParents(cs)[0]
     */
    abstract public int getParent(int cs);

    /** Return an array with all the parent and determining coordinate
     * systems of the given coordinate system. For instance, for a coordsys
     * created with GLVobCoorder.nadirOrigin, this should be an array of length two.
     */
    abstract public int[] getParents(int cs);

