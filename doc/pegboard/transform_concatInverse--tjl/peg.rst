=============================================================
PEG transform_concatInverse--tjl: 
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/08/05 08:26:38 $
:Revision: $Revision: 1.1 $
:Status:   Current

The ``transform_concat--tjl`` PEG left one type
of concatenation out, which will make things slightly 
more efficient. Concatenating the inverse of one coordinate
system to another can be done in a single operation instead of 
two.

This is a useful operation in, e.g., the View2D stuff in FenPDF.

Issues
======

Changes
=======

The OpenGL libvob already implements invert as ::

    public int invert(int f) {
	return concatInverse(0, f);
    }

This peg proposes to add concatInverse into the official
VobScene definition.

Add into VobScene::

    public int concatInverseCS(int into, Object key, int other) ;
    

and VobCoorder::

    /** Create a new transformation that is the concatenation 
     * of an existing transformation and the inverse of another
     * transformation.
     * If we look at the transformations as x' = f(x) and x' = g(x) then
     * the result of this operation is a transformation h,
     * for which h(x) = f(g^-1(x)) always.
     */
    public int concatInverse(int f, int g) ;
