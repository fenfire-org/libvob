===========================
Design of LibVob
===========================

.. UML:: design_navigation
   :menu: 0

   page (Design) "Design of Libvob"
      	link
		design.gen.html

   page (C) "Design of the C"
	use Design
      	link
		design_C.gen.html

   page (Vobs) "Design of the Vobs"
	use Design
      	link
		design_vobs.gen.html

   page (Transforms) "Design of the Transforms"
	use Design
	link
		design_transforms.gen.html

   ---
   vertically(20, ver_c, Design, Vobs);
   horizontally(20, hor_c, C, Vobs, Transforms);

The goal of the LibVob toolkit is to allow
interpolation between independently programmed views as well as 
drawing 'cross-cutting' connections between objects 
in different places of the view hierarchy. 
(For example, a connection between objects in two
different windows would be 'cross-cutting' as it
connects two different places in the hierarchy.)

This distinguishes the Vob system from normal scene graphs:
instead of one global scene graph, there are *keyframe* scene
graphs. 
After each discrete user action (like a key press or mouse click), 
a new scene graph is generated
by the user-specified *view*.
Then, the previous keyframe is animated into the
new keyframe to allow the user to keep track of all changes.
The animation is governed by *keys* associated with the
visible objects; an object in the first scene graph
is animated to an object in the second scene graph
if both have the same key. The keys are taken from the same
underlying model (for example, they could be cells in a
spreadsheet) and enable two independently developed views
to be interpolated to each other (for example, two different
ways to render spreadsheet data in a graph).

Continuous user actions (e.g. zooming/panning with the mouse)
are treated more conventionally by editing the parameters of
the current keyframe scene graph.

Example using an early version of libvob
----------------------------------------

To clarify, let's have an example using the first version of
the Vob design. This version was considerably more limited
than the current version but may be easier for grasping the essential concepts.
In the early version, the structure of a keyframe was simply

.. UML:: vobs_overall_1

   class VobScene
	assoc multi(1) - multi(*) Vob

   class Vob
   	fields
   		int x, y, w, h, depth
		Object key
	methods
		renderinterp(Vob other, float fract)

   ---
   Vob.c = VobScene.c + (0, -150);

Two keyframes were matched by directly matching Vobs with 
the same key.

For instance, 

XXX Example: two vobscenes, two interpolated vobs and some non-interpolated;
show "match" relations

In this case, the vob A is animated to the vob B ...

The modern design: Separation of concerns
-------------------------------------------

While the above model already demonstrated the basic idea of vobs, 
it was too limited for most uses. Various extensions were tried
until the current one was found.

.. UML:: vobs_overall_2

    clinkpackage Vob

    class VobScene
	assoc multi(*) - multi(*) Vob
	assoc compos - multi(*) Transform
	assoc compos - multi(*) Placement

    class Vob
	clink
	methods
	    draw(Transform*)

    class Placement "pseudoclass"
	assoc multi(*) - multi(1) Vob
	assoc multi(*) - multi(*) Transform

    class Transform
	clink
	methods
	    ZPt transform(ZPt)

    ---

    horizontally(70, foo, Vob, Placement, Transform);
    vertically(100, bar, VobScene, foo);

Notable new points are

- Vobs may be reused between vobscenes. This greatly helps
  performance on systems (e.g. Kaffe) where garbage collection
  has not yet been optimized enough.

- Interpolation is handled by creating temporary Transforms, the Vobs
  no longer need to worry about anything except drawing themselves 
  to a given coordinate system.

- Transforms can be more general since they are not fixed to the Vobs:

.. UML:: vobs_overall_transforms

    clinkpackage Vob

    class Transform "interface"
	clink
	methods
	    ZPt transform(ZPt orig)
	    Transform getInverse()
	    float nonlinearity(ZPt p, float radius)

    transassoc = assoc Transform role(transformparent) multi(*) - multi(*) role(transchild) Transform

    class OrthoTransform
	inherit Transform

    class AffineTransform
	inherit Transform

    class FisheyeTransform
	inherit Transform

    ---
    Transform.c = (0,0);
    pair p,pu,pl;
    p := Transform.e;
    pu := Transform.ne;
    pl := Transform.se;

    transassoc.p = .85[pu,pl]{right} .. p+(40,0){up} .. .15[pu,pl]{left};

    horizontally(10, foo, OrthoTransform, AffineTransform, FisheyeTransform);
    vertically(60, bar, Transform, foo);

- Transforms can depend on other transforms (the transchild - transformparent relationship above).
  This is another generalization of scene graphs which are usually
  just trees.
  For example, XXX nadir

- Key matching is also hierarchical, but the hierarchy is separate from the transform hierarchy.
  XXX example


The Coordinate systems form two intertwined structures:
a DAG of transform parents and a tree of match parents. See the ``VobScene`` javadoc
for details.

Interpolation
-------------

An important reason for the Vob construction is interpolation: it is possible
to smoothly animate between Vobscenes generated by independent views.
The interpolation is based on the keys of the coordinate systems.  
The two steps are:

- Match, starting from the root coordinate system (0), coordinate systems
  whose parents have been matched and which share the same key.

- For all coordinate systems for which a match was found, form an interpolating
  coordinate system and use that for rendering. It is optional whether non-interpolating
  coordinate systems will be rendered or not (experience shows that it's good to start rendering
  them at some small time before the animation ends).

Sometimes it is desirable to change the interpolation behaviour for some coordinate
systems. An example is a "main" coordinate system surrounded by buoys, and interpolation
between a buoy and the main coordinate system. It is not desirable to adjust the keys
but to rather have the main coordinate system have a static key and change the
interpolation behaviour of the buoys.






The Java implementation
-----------------------

In the Java implementation CoordinateSystems are represented by
integer indices to various arrays due to efficiency reasons.
If we were using a stronger language such as C++ where making
the CoordinateSystem its own class in a template wouldn't cost us, we certainly
would do so.

The class diagram of VobScene and related classes is shown below. 
The "pseudoclass" stereotype refers to the above array implementation of the
coordsys "class"es.

The diagram is slightly convoluted (feel free to improve the layout); the central 
class is a VobScene, which is a facade for the whole system of vobs and coordinate
systems. It contains three objects which implement the ``VobMap``, ``VobCoorder`` and
``VobMatcher`` interfaces. These manage various relationships of the Transform.

.. UML:: vobscene_overall

    jlinkpackage org.nongnu.libvob
    clinkpackage Vob

    class VobScene
	jlink
	assoc compos - multi(1) VobMap
	assoc compos - multi(1) VobCoorder
	assoc compos - multi(1) VobMatcher
	methods
	    java.awt.Dimension getSize()
	    void put(Vob v, Object key, float depth, x, y, w, h)

    class VobMap "interface"
	jlink
	assoc aggreg multi(*) - multi(*) Vob
	methods
	    Vob getVobByCS(int coordsys)
	    void put(Vob)
	    void put(Vob, int coordsys)
	    void put(Vob, int coordsys1, int coordsys2)


    class VobCoorder "interface"
	jlink
	assoc compos - multi(*) Transform
	methods
	    int ortho(int parent, float depth, x, y, w, h)
	    int scale(int parent, float sx, sy)
	    ...
	    int getCSAt(int parent, float x, y)
	    boolean needInterp(VobCoorder interpTo, int[] interpList)
	    float[] transformPoints3(int withCS, float[] pt)
	    ...

    class VobMatcher "interface"
	jlink
	methods
	    int add(int into, int cs, Object key)
	    int[] interpList(VobMatcher other)
	    ...


    class Vob "interface"
	jlink
	methods
	    void render(Graphics g, boolean fast, RenderInfo info1, info2) 
	    int addToListGL(...) 
	    boolean intersect(int x, int y, RenderInfo info1, info2) 

    class Transform "pseudoclass"
	clink
	assoc multi(*) - multi(1) role(key) java.lang.Object
	assoc multi(*) - multi(*) Vob

    class java.lang.Object
	methods
	    int hashCode()
	    boolean equals(Object o)

    transassoc = assoc Transform role(transformparent) multi(*) - multi(*) role(transchild) Transform
    matchassoc = assoc Transform aggreg role(matchparent) multi(1) - multi(*) role(matchchild) Transform

	   
    ---
    Transform.c = (0,300);

    vertically(50, side, Vob, java.lang.Object);

    side.e = Transform.w + (-80,0);

    VobMap.s = .5[Vob.e,Transform.w] + (0, 100);
    VobMatcher.c = (xpart(VobMap.c), ypart(2*Transform.c - VobMap.c));

    VobCoorder.sw = Transform.ne + (20,60);

    VobScene.nw = Transform.se + (40,-50);

    pair p,pu,pl;
    p := Transform.e;
    pu := Transform.ne;
    pl := Transform.se;

    transassoc.p = .85[pu,pl]{right} .. p+(40,0){up} .. .15[pu,pl]{left};

    pair m, mu, ml;
    m := Transform.s;
    mu := Transform.sw;
    ml := Transform.se;
    matchassoc.p = .1[mu,ml]{down} .. m+(0,-40){right} .. .9[mu,ml]{up};

    % association minders
    draw VobMap.c -- 0.7[Vob.c, Transform.c] dashed evenly;
    draw VobCoorder.c -- point .5 of transassoc.p dashed evenly;
    draw VobMatcher.c -- 0.6[Transform.c, java.lang.Object.c] dashed evenly;
    draw VobMatcher.c -- point .5 of matchassoc.p dashed evenly;

The construction of a VobScene by a view goes as follows:

.. UML:: vobsceneseq

    jlinkpackage org.nongnu.libvob

    seqobject VobScene
 	jlink
    seqobject VobMap
	jlink
    seqobject VobMatcher
	jlink
    seqobject VobCoorder
	jlink

    jlinkpackage org.fenfire.loom

    seqobject View
	jlink	

    sequence simplecall
	call View 
	    call VobScene "put(vob, key, d, x, y, w, h)" 
		call VobCoorder "cs = coordsys(0, d, x, y, w, h)"
		    return
		call VobMatcher "add(cs, key)"
		    return
		call VobMap "put(vob, cs)"
		    return
		return
	    return
	return
	call View
	    call VobScene "cs1 = orthoCS(cs0, key..)"
		call VobCoorder "cs = ortho(cs0, ...)"
		    return
		call VobMatcher "add(cs0, cs, key)"
		    return
		return "cs"
	    call VobCoorder "cs2 = coordsys(...)"
	    call VobMap "put(vob1, cs1, cs2)"
	    call VobMap "put(vob2, cs1, cs2)"
	    call VobMap "put(vob3, cs1)"
    ---
    View.c = (0,0);
    horizontally(60, aaa, View, VobScene, VobMap, VobMatcher, VobCoorder);

There are two modes of calling: either through the VobScene as a shorthand
for the most
common cases, or directly through to the ``VobCoorder`` and ``VobMap`` members.

Rendering a ``VobScene`` can happen in two ways, depending on whether OpenGL
or AWT is being used. OpenGL is described in `GLRenderables`__.

.. _GLRenderables: Gzz_Frontend_GLRend.gen.html

__ GLRenderables_
