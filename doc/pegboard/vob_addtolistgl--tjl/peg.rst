=============================================================
PEG vob_addtolistgl--tjl: 
=============================================================

:Author:   	Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: 	$Revision: 1.1 $
:Status:   	Accepted
:Stakeholders: 	mudyc, benja, humppake
:Scope:		Minor
:Type:		Interface

When making AbstractUpdateManager call the idle task to delete 
unreferenced GL objects, it caused PP to fail. The reason was
quickly found out to be the Vob.addToListGL call: it was
creating new GL objects but not retaining references to them,
which caused the garbage collector to locate and destroy them.

This behaviour is obviously wrong and needs to be changed. Changing
just the addToListGL calls that do it would IMO be the wrong solution
because then it would be easy to create new crashes.

Issues
======

- How bad will the performance degradation be?

    RESOLVED: With Sun's hotspot JVM, shouldn't be much or at all.
    With other JVMs, it may be an issue. However, there are so many
    other headaches with placing Vobs and this can also be precompiled
    away if desired.

Changes
=======

There are two kinds of Vobs: ones that get added directly and ones that
add others. To accommodate both, we need some trickery.

Replace ::
    
    addToListGL(GraphicsAPI.RenderingSurface, int[], int, int, int)

in Vob with

    int putGL(VobScene vs)
    int putGL(VobScene vs, int cs1)
    int putGL(VobScene vs, int cs1, int cs2)

which returns the integer to be added to the vobmap. The methods' default
implementations will throw an error for adding with the wrong number of coordsys.

The methods will return either the index, or 0, in which case nothing is added.
Returning 0 is usually accompanied by several calls to VobScene.put() for putting
the component parts of the vob; i.e., it is explicitly permitted that calls to
VobScene are made during these callbacks.

Renderable0 &c will implement these.

As for GLVobMap, the behaviour is specified so that calling put() will
cause putGL() to be called immediately, so that any vobs placed therein
will be placed immediately in the list.

Analoguous changes in Vob3.
