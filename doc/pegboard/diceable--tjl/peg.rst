=============================================================
PEG diceable--tjl: General dicing for polygonal things
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Incomplete

Re: recent discussion on gzz-dev, we need a general API
for specifying polygons in the Java level, and then
transforming and *dicing* them for non-linear coordinate
systems on the OpenGL level.

Issues
======

- How much overhead is acceptable / achievable on a single triangle?

    - If there's a single CallGL list, more overhead is acceptable
      than if there are none

- What level of reusability of diced+transformed vertices
  do we want? Possibilities:
    
    1) Dicing for only one use, with one set of parameters:
       corresponds to one glBegin/glEnd pair

    2) Dicing for several passes simultaneously, with CallGL
       inbetween. Corresponds to a single renderable.

    3) Dicing for several renderables in the same coordsys.
       Useful for e.g. rendering pass A, rendering a different
       renderable between and then rendering pass B.
       This is the most complete operation but difficult to
       implement

  This choice will determine the final efficiency.

    RESOLVED: 2) above. 3) is too complicated and seldom used
    for now, but 1) is too limited. 2) is a reasonable compromise
    and still straightforward to implement.

- Should we do "skinning" at the same time, i.e., should more
  than one coordsys be supported?

    RESOLVED: yes. This is the ideal place to allow that
    in a unified way.

- How should this interact with liblines? Drawing lines as an edge
  to a transformed polygon would be nice.

    RESOLVED: Yes, we should do that. However, it gets a little 
    tricky: to avoid cracks, the line should be diced like the
    polygon edge. However, it should not be transformed, unless explicitly
    requested by the user: liblines should do the transformation
    and the rest itself.

- Should structured meshes be allowed?

    RESOLVED: maybe later. The idea is to cache the Diceable objects
    so they can structure themselves as desired, without much performance
    overhead.

- Should we support the OpenGL primitives ``TRIANGLE_STRIP``, etc.?

    RESOLVED: At first, no. Later, at a Java interface level. 
    Even later, in the renderable interface,
    since those primitives don't really help when dicing.
    It could be a little help when rendering undiced but we'll have to
    see.  

Introduction
============

Currently there's a lot of code in Java using the OpenGL transformation
pipeline by using CallGL code such as ::
    
    Begin QUAD_STRIP
    Vertex 0.5 1 1000
    Vertex 0.5 -1 1000
    Vertex -1 1 1000
    Vertex -1 -1 1000
    End

to draw something. This is not good, since the transformation pipeline
may contain non-linear transformations and these commands will
not be correctly transformed since the non-linear transformations
cannot be implemented as OpenGL transformations matrices.

It *would*, of course, be possible to use vertex programs on architectures
where those are available, but this does not really help much, since
the non-linear transformation can take the above rectangle to
a shape with curved edges --- the transformation would be inaccurate
anyway.

Currently, this problem is solved in PaperQuad by dicing the 
rectangle appropriately before transformation. However, the trivial
generalization of this solution --- having a renderable for each 
of the polygonal things we want to render in OpenGL --- is unreasonable.

We need a general way to specify polygons and texture coordinates
(and other attributes) to be diced. At the same time, we can
solve the other problem: it has not been possible to create polygonal
data to be rendered depending on more than one coordinate system, 
e.g. connections.

Changes
=======

A new renderable, DiceableMesh, containing

    - A set of indexed vertices, each containing

	- vertex location

	- possibly other vertex data: texture coordinates, normal,
	  etc.
    
    - A set of lines and polygons as an unstructured mesh between
      the vertices

    - A vector of CallGL codes

    - A vector of primitive sets:
	
	- whether the primitive set is GL line or libline line or GL triangle

	- the vector of vertex indices of the primitives

    - center and radius of nonlinearity query

When rendering and dicing, all vertex attributes are interpolated
linearly.

