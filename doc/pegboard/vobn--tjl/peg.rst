=============================================================
PEG vobn--tjl: N-coordinate system vobs
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/05/29 08:00:19 $
:Revision: $Revision: 1.2 $
:Status:   Implemented

For the fillet tests (and probably also other things)
we need N-coordinate system Vobs as a generalization
of the current 1, 2, 3 -cs vobs. N-coordsys Vobs will
be used to allow a vob which is a graph node that knows
all its neighbours at render time.

The C++ part is already almost there - changes
are required to ``VobPlacer`` and the main Vob API.

Issues
======

- Will the current 1, 2, 3 -cs methods be removed?

    RESOLVED: No. They will remain for simplicity and
    efficiency.

Changes
=======

Java
----

Into ``Vob`` add ::

    int putGL(VobScene vs, int[] cs);

Into ``VobPlacer``, add ::

    void put(Vob vob, int[] cs);

which shall for now throw an exception in awt.

Implement these for GL, with a new constant
GL.RENDERABLEN, after which there is the number of
coordinate systems used.

C++
---

Naturally, interpret the RENDERABLEN constant mentioned
above.

Allow a Vob definition to give NTrans == -1, meaning N.
These will implement only ``Vob``, which already has a 
n-coordsys call. The nCoordsys() method will return -1.
