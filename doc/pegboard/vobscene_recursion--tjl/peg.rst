=============================================================
PEG vobscene_recursion--tjl: 
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/09/14 11:59:39 $
:Revision: $Revision: 1.9 $
:Status:   Current

Performance problems in FenPDF as well as running out 
of coordinate systems show that we should soon make a big
change I've been mulling over for some time: make vobscenes
hierarchical.

Issues
======

- Should we have a "VobSceneVob", i.e. a Vob that contains a different
  VobScene or some other approach?

    RESOLVED: Too many special properties to be a Vob or a coordsys
    directly. 

    We shall use a special ChildVobScene class.

- What should the efficiency demands be when including a child
  and when rendering? Linear w.r.t. child size?

    RESOLVED: Linear is not good enough! Consider a huge canvas that
    has been generated and is placed into a viewport, and most of it is
    culled away. One of the very reasons for this PEG is that we need
    to have more speed in this case.

    Therefore, the time to include a child should be **constant**,
    and the time to render a vobscene should be **linear** w.r.t. 
    the *rendered*
    coordsystems of the child, but constant w.r.t. child size. This makes
    life a bit more difficult internally (can't just copy a list, have
    to point to it) but should be well worth it e.g. for FenPDF.

- How do we map between coordinate systems of the parent and child vobscenes?

    RESOLVED: By a special method in the parent that returns a coordsys
    corresponding to a coordinate system in the child.

- Are modifications to child vobscenes allowed?

    RESOLVED: Using a vobscene after a child that was placed into it
    has been altered in any way **except** coordinate system parameter
    change gives undefined results for now. Possibly even crash.

    This may change later; easiest to start strict.

- [benja] Should we throw an exception if a child vobscene is modified?

    RESOLVED: Too expensive - this is the innermost loops
    of the Java code we are talking about, here.

- Do we need to define coordinate systems other than the root of the
  child from the parent?

    RESOLVED: Yes. Even for the primitive use case outlined above,
    we need two: the canvas and the viewport coordinate systems.

- How generally do we need to define the coordinate systems 
  inside the child (the ones passed to the child as "parameters")?
  Alternatives: *n* from the beginning, or "random access" for
  redefining any coordinate systems of the child we like.

    RESOLVED: Initially, n from the beginning. We can generalize
    later if necessary. This also makes matching, shouldBeRendered
    hierarchies &c inside the child easier to deal with for now.

    This requires creating the child with the knowledge of how 
    the initial coordinate systems will be numbered -- an extra parameter
    to createVobScene (reserveCS).

    It is recommended that a class with constants is created to
    number the initial coordinate systems, for ease of access.

- What about coordinate system 0? Should it be special in the child
  and always correspond to cs 0 of the parent (i.e. unit transformation)?
  This is made problematic by the twim roles of coordinate system 0:
  on the one hand, it's the "root" coordinate system,
  on the other hand, it's the "no transofrmation" coordinate system.

    RESOLVED: It should not be special. 

    However, we should start a new convention: coordinate system **1**
    is the "root" coordinate system, and **0** remains the "identity"
    coordinate system.

    The box size of CS 1 is set to screen size, while the box size
    of CS 0 is (1,1)

    The semantics of 0 and 1 for child VobScenes are chosen 
    by the programmer, and the programmer can even specify 0 or 1 reserved
    coordinate systems. 

    It is **strongly** recommended that coordsyses 0 at the very least,
    and possibly 1 be used as per the convention for child coordinate
    systems as code may rely on those.

- How do we place the child vobscene and choose the vobscenes? 

    RESOLVED: A special call in VobScene, since the child vobscene
    needs a *key* from the parent coordinate system, to allow
    interpolation (not implemented now).

- Matching between different versions of a child vobscene?

    RESOLVED: Later.

- Is a normal VobScene usable as a child vobscene? Or do we need special
  operations?

    RESOLVED: No, it should not be usable. 
    Too much overhead and difficulties. 

    The correct way is to create a child VobScene by the 
    ``GraphicsAPI.RenderingSurface.createChildVobScene(int reserveCS)``
    call.  

- [benja] Should child vobscenes be their own class?

    RESOLVED: Yes. Gives us type safety for placing child vobscenes.

Introduction
============

This PEG proposes an important change to the VobScene model. So far,
the model has been (we'll ignore keys for a while): 

    A VobScene contains coordinate systems, indexed by integers,
    and a list of Vobs, each of which is associated with 0 or more coordsyses.

    The coordinate system 0 is the root (or unit) coordinate system, representing
    screen coordinates.

The new model is:

    A VobScene contains coordinate systems, indexed by integers,
    and a list of Vobs, each of which is associated with 0 or more coordsyses.

    A VobScene can also contain a child VobScene, which is specially treated
    both as a coordinate system *and* a Vob. A child VobScene can use a fixed
    number of parameter coordinate systems from the parent. The VobScene can
    also take coordinate systems from the child to use as normal coordinate
    systems in the parent.

    The coordinate system 0 is the unit coordinate system in the root
    vobscene and (by convention) others.

    The coordinate system 1 is the screen coordinate system, with the box size
    giving the width/height in screen coordinates. This can also be used
    as a convention in child vobscenes but is not as important as coordsys 0.

This represents quite a complication; OTOH, the gains are important: 
child VobScenes can be created directly as pure Functions of the structure,
allowing REALLY efficient caching &c.

Changes
=======

The API changes are minor. 

Child VobScene
--------------

New class ``ChildVobScene``, extending ``VobScene``. No new methods.

Creating Child VobScenes
------------------------

Extend ``GraphicsAPI.RenderingSurface`` by ::

    /** Create a new VobScene that may be placed into a VobScene
     * by the putChildVobScene call.
     * @param numberOfParameterCS The number of initial coordinate systems in the child scene
     * 			that are to be given to it as parameters from the parent.
     */
    ChildVobScene createChildVobScene(Dimension size, int numberOfParameterCS);

Placing Child VobScenes
-----------------------

Extend ``VobScene`` by::

    /** Place the given VobScene as a child.
     * The given VobScene must not be modified after this call.
     * @param child The child VobScene to place. Child vobscenes
     * 		need to be created especially, through
     *		RenderingSurface.createChildVobScene
     * @param key The key to use
     * @param cs The coordinate systems that shall be used as the first
     *           coordinate systems of the child.
     *           This *must* have the same number of elements as 
     *           the createChildVobScene method was given.
     */
     public void putChildVobScene(ChildVobScene child, Object key, int[] cs);

And correspondingly VobCoorder by::

     public int _putChildVobScene(ChildVobScene child, int[] cs)

and VobPlacer by::
    
     public int _putChildVobScene(ChildVobScene child, int coorderResult,
					int[] cs)

(the VobScene method shall call both; the underscore means that 
they should not be called by users. The VobScene method shall
return the result from VobPlacer).

The key shall be passed to VobMatcher in the normal way, using the integer
returned by VobCoorder._putChildVobScene.

Exporting coordinate systems
----------------------------

Finally, a method exporting child coordinate systems into the
parent, in VobScene::

    /** Get a coordinate system from a child vobscene placed
     * using putChildVobScene into this VobScene.
     * @param childVobSceneId The id returned from putChildVobScene
     * @param nth The index of the coordinate system 
     *            inside the child vobscene.
     */
    public int exportChildCoordsys(int childVobSceneId, int nth);

VobScene semantics
------------------

So far, the only special CS has been coordinate system 0, which 
has had the dual role of being both the "identity transformation" 
and the "screen coordinate system".

From now on, coordinate system **1** shall be the "screen" coordinate system,
and **0** remains the "identity" coordinate system (representing
an identity transformation).

The unit box of CS 1 is set to screen size, while the unit box
of CS 0 is (1,1).
