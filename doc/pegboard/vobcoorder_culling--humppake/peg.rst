================================================================================
PEG vobcoorder_culling--humppake: Interface for creating ``CullingCoordSys``
================================================================================

:Authors:   Asko Soukka
:Stakeholders: Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Implemented
:Date-Created:	2002-11-05
:Scope:    Minor
:Type:     Feature

``CullingCoordSys`` can be currently created through its interface
in ``GLVobCoorder``. This peg is about making culling also as a part
of general ``VobCoorder`` interface.

Changes
-------

Into ``gzz.vob.VobCoorder`` add::

	/** Creates a CullingCoordSys with distinct parent and test 
	 * coordinate systems. Exluding the test for drawing, the 
	 * CullingCoordSys works like its parent coordinate system.
	 * E.g. CullingCoordSys returns its parents box. 
	 *
      	 * This coordsys will not necessarily be drawn if the boxes
	 * of the test and clip coordinate systems do not intersect.
	 * However, this is not guaranteed; the only thing guaranteed
	 * is that if the boxes of the test and clip coordinate systems
	 * *do* intersect, the CullingCoordsys will be drawn.
	 *
	 * @param parent ID of the coordinate system which points 
	 *               will be transformed, if CullingCoordSys 
	 *               is shown
	 * @param test ID of the coordinate system whose box is tested 
	 *             against the clip coordinate system.
	 * @param clip ID of the coordinate system whose box is tested
	 *             against the test coordinate system.
	 */
	public int cull(int parent, int test, int clip) {
	}

	/** Creates a CullingCoordSys using the parent also as the test 
	 * coordinate system. 
	 */
	public int cull(int parent, int clip) {
	  cull(parent, parent, clip);
    	}
