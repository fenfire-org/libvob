=============================================================
PEG ``vobscene_mouseclicks--tjl``: Mouse clicks and VobScene
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Implemented

The VobScene API contains the method ::
    
    public Object getKeyAt(float x, float y, float[] targetcoords) {
	return null;
    }

as a remnant from past times, when vobs had coordinates.

Now, with coordinate system not always implying transformation to screen coordinates
but transformations between arbitrary coordinate systems (e.g. as with PaperQuad),
we need to redo this functionality somehow.

Issues
------

- Who stores the knowledge of which coordinate systems are activated?

    RESOLVED: The logical choices are either VobCoorder, VobMatcher or a 
    new class. Of these, VobCoorder can most likely store the information
    efficiently. Thus, it's VobCoorder for now.

- How is depth order etc. defined? This becomes a problem
  if (and when) we want to allow generic 3D manipulation; then,
  we need to actually find the "hit" in the coordinate system.
  This is not something that needs to be implemented now, but 
  is an important extension point later.

  Somehow, we need to transform back and forth between coordinate 
  systems. For the usual case, it would be enough to

  1) Inverse transform into the coordinate system; in there, clip
     against the unit square and project to the plane z=0

  2) Transform the projected point back into screen coordinates, making
     note of the z coordinate.

  This approach fails miserably when the z axis has been rotated.

    RESOLVED: For now, define a call suffixed with 2D as the above 
    projection and leave room for a later function. The behaviour when the 
    z axis has been rotated is a little awkward but currently that is not
    used.

- Clipping. If we have a large plane (a la PP), only part of which is visible,
  how do we do it so that only the vobs in the visible part get picked?

    RESOLVED: By explicitly allowing the programmer to control the hierarchy
    at the get phase. getCSAt(parent, ...)

    This may not be the cleanest solution: the knowledge of the structure
    of the scene is needed at that point, but this is good enough for now.

Changes
-------

Into VobScene, add ::

    public void activate(int cs)
    public void activate(Object key)

These are automatically delegated (converting the key to a coordinate
system through VobMatcher) to VobCooder's new method ::

    public void activate(int cs);


Also, make VobScene.put(Vob, coords) automatically call one of these.

Then, redo getKeyAt() to return the key of the topmost **activated** 
coordsys at the given location, with topmost defined as above. ::

    /** Get the topmost activated coordsys at (x,y), whose nearest activated
     * direct ancestor (not determining) is parent.
     */
    public int getCSAt(int parent, float x, float y, float[] targetcoords) ;

    public Object getKeyAt(int parent, float x, float y, float[] targetcoords) ;

