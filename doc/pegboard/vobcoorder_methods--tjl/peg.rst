=============================================================================
PEG ``vobcoorder_methods--tjl``: more VobCoorder and AffineVobCoorder methods
=============================================================================

:Authors:  Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Accepted

The newest VobCoorder changes make it much more usable but leave
some holes in the API:

- It is not possible to create a keyless coordsys from VobScene

- It is not possible to create a coordsys whose parent is not the
  same as the interpolation parent. It makes sense to separate
  these in several cases. 

Issues
------

    - Should we add::
    
	    putOrtho(int into, Object key, Vob vob, ...)
      
      and others at the same time?

	RESOLVED: Not yet. Because we would like to have the same
	versions as below, there'd be 4 methods per coordsys type.

    - Should we have a keyless method ::

	final public int orthoCS(int into, float depth, 
				float x, float y, float w, float h) ;

      as well?

	RESOLVED: No, rather define null key to be no key.

Changes
-------

Clarify the documentation of the two separate hierarchies in VobScene.

AffineVobCoorder
    Rename affineCoordsys to affine, to be consistent with others.

VobScene
    For all ``*CS`` methods, add a second version. For example::

	final public int orthoCS(int into, Object key, float depth, 
				float x, float y, float w, float h) ;
	final public int orthoCS(int into, int keyparent, Object key, float depth, 
				float x, float y, float w, float h) ;

    This is possible because it is easy to separate the different versions
    due to the presence of the Object parameter.

VobMatcher
    Explicitly define that a null key and not adding a key produce the same result.
