==================================================================================
PEG ``vobcoorder_improvements--tjl``: VobCoorder and AffineVobCoorder improvements
==================================================================================

:Authors:  Tuomas Lukka
:Stakeholders: Benja Fallenstein
:Last-Modified: $Date: 2003/08/12 06:45:00 $
:Revision: $Revision: 1.2 $
:Status:   implemented

Transformations
---------------

Add into VobCoorder the following convenience functions::

    public int translate(int into, float x, float y) {
	return ortho(into, 0, x, y, 1, 1);
    }

    public int translate(int into, float x, float y, float z) {
	return ortho(into, z, x, y, 1, 1);
    }

    public int scale(int into, float x, float y) {
	return ortho(into, 0, 0, 0, x, y);
    }

and rename coordsys into ortho.

And into AffineVobCoorder the following functions::

    int rotate(int into, float degrees);
    int scale(int into, float x, float y, float z);

Also, for all these functions, we should have a shorthand
in VobScene::

    public int translateCS(int into, Object key, float x, float y);

just to make it a little easier to say them.

For the affine ones, the VobCoorder is cast into AffineVobCoorder, and
the error propagated if it's not. A direct cast is not permitted;
the functions must check ``instanceof`` and then throw
NotAffineVobCoorderException, which is a subclass of
UnsupportedOperationException.

For all of the above calls which create coordinate systems, have also 
setParameter calls. This means, for x in translate, scale, rotate,
orthoCoordsys, affineCoordsys, the coorder should also have a method

    abstract public void setXParams(int id, ...);

for example,

    abstract public int ortho(int into, float x, float y, float w, float h);
    abstract public void setOrthoParams(int id, float x, float y, float w, float h);


Transforming points into coordinate systems
-------------------------------------------

It would be nice to be able to easily transform points to and from
coordsystems, since we now have hierarchies. Since the coordsys are
known incrementally, it should not be a problem to provide in VobCoorder::

    /** Transform one or more points to screen coordinates from the given cs.
     *  Each point is represented as three values (x,y,z) in pt[].
     * @param into The array to store the results in.
     */
    public void transformPoints3(int withCS, float[] pt, float[]into) ;

    /** Transform one or more points from screen to given CS.
     *  Each point is represented as three values (x,y,z) in pt[].
     * @return True, if a reasonably accurate inverse was found.
     * If an inverse cannot be found, a reasonable attempt shall
     * be made to give a point close (e.g. if an affine transform
     * is singular / near-singular, pick a point on the line
     * that's close to the original.
     */
    public boolean inverseTransformPoints3(int withCS, float[] pt, float[]into) ;

At the same time, remove VobCoorder.getRenderInfo as unnecessary.

The reason for allowing the array ``pt[]`` to contain several points is
simply performance.

