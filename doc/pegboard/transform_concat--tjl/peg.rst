=============================================================================
PEG ``transform_concat--tjl``: Coordinate system concatenation and inversion
=============================================================================

:Authors:  Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Accepted

A vital part of making coordinate system manipulation easier
is the ability to concatenate and invert transformations
when creating them, not only when drawing.

Changes
-------

To VobCoorder, add::

    /** Create a new coordsys whose transformation 
     * is the concatenation of the parent and child 
     * transformations: x' = parent(child(x))
     */
    abstract public int concat(int parentCS, int childCS);

    /** Create a new coordsys whose transformation
     * is the inverse / pseudoinverse (i.e. if the
     * original transformation is singular / near-singular,
     * use closest reasonable value) of the 
     * given coordinate system.
     */
    abstract public int invert(int coordsys);
