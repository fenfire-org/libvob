=============================================================
PEG buoydesign--tjl: Buoy view (xupdf) redesign
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/08/11 07:11:03 $
:Revision: $Revision: 1.7 $
:Status:   Incomplete

This PEG attempts to define a flexible framework for FenPDF
and related designs.


Issues
======

- Who creates the nodes? What about when a node contains a link 
  to itself? Are all nodes really the same?

    RESOLVED: A-ha! There was confusion because the first
    design treated buoy nodes and main nodes the same,
    when they are not at all the same.

    The buoy nodes know how to render themselves at a given
    anchor.

    The main node has a cursor and handles events etc.

    The buoy node objects can be static objects; the main node
    objects must be created for each view.

Architecture
============

The abstract buoy data model
----------------------------

First of all, define the abstract buoyview model as follows:

..  UML:: BuoyviewModel

    class Node
	methods
	    putAsBuoy(Anchor a)
	    putAsMain()
	    handleKeystroke(String s)


    class Anchor
	methods
	    setCursor()
	assoc multi(*) - multi(1) Node

    class Link
	assoc multi(*) - multi(2,ordered) Anchor

    ---

    horizontally(60, foo, Node, Anchor, Link);

In PP, a node is a 2D canvas on which there are cells,
and Anchors are zz cells and links are connections
between clones of anchor cells on a particular dimension.

In xupdf, a node is a cell containing a PDF document,
and the anchors and links are defined implicitly by
the xanalogical transclusions and xulinks.

A PDF scrollblock, on the other hand,
is very different: no cell, no cellview!
The architecture needs to support all these.

.. UML:: BuoyviewModels
    
    class Node

    class ZZStructureNode
	inherit Node

    class PPCanvasNode
	inherit Node

    class PDFDocNode
	inherit Node


    class Anchor

    class CellTranscludesAnchor
	inherit Anchor
	assoc multi(*) - multi(0..1) ZZStructureNode
	assoc multi(*) - multi(0..1) PPCanvasNode

    class DocFragmentAnchor
	inherit Anchor
	assoc multi(*) - multi(1) PDFDocNode

    class PPAnchor
	inherit Anchor
	assoc multi(*) - multi(1) PPCanvasNode



    class Link

    class TransclusionLink
	inherit Link
	assoc multi(*) - multi(1) CellTranscludesAnchor
	assoc multi(*) - multi(1) DocFragmentAnchor

    class XuLink
	inherit Link
	assoc multi(*) - multi(0..2) CellTranscludesAnchor
	assoc multi(*) - multi(0..2) DocFragmentAnchor

    class PPLink
	inherit Link
	assoc multi(*) - multi(2,ordered) PPAnchor

    ---

    vertically(60, foo, PDFDocNode, ZZStructureNode, PPCanvasNode);

    vertically(60, bar, DocFragmentAnchor, CellTranscludesAnchor, PPAnchor);

    vertically(60, baz, TransclusionLink, XuLink, PPLink);

    horizontally(70, xxx, foo, bar, baz);

    pair o; o = (-100, 60);
    Node.c = PDFDocNode.c + o;
    Anchor.c = DocFragmentAnchor.c + o;
    Link.c = TransclusionLink.c + o;


The Main Interfaces
-------------------

Now, the picture gets clearer: the whole structure of the current buoy view
should be *defined* by a single facade:


Important Invariant (a la ZZstructure dimension): 
if node A, anchor A.1 shows a link X to node B anchor B.4, 
hen after B.setFocus(B.4), node B will show the link X as well.

.. UML:: BuoyManagerBasic

    jlinkpackage org.nongnu.libvob.buoy

    class RealBuoyViewManager 
	fields
	    BuoyViewMainNode current
	inherit BuoyLinkListener

    class BuoyLinkListener "interface"
	jlink
	methods
	    void link(int dir, int anchorCS, BuoyViewNodeType otherNode, Object linkId, Object otherAnchor)

    class BuoyViewConnector "interface"
	jlink
	methods
	    void addBuoys(VobScene, BuoyViewMainNode, BuoyLinkListener)

    class BuoyViewMainNode "interface"
	jlink
	fields
	    cursor, zoom, coordsystems...
	methods
	    void renderMain(VobScene vs, int into)
	    keystroke(...)
	    mouse(...)
    
    class BuoyViewNodeType "interface"
	jlink
	methods
	    void renderBuoy(VobScene vs, int into, Object linkId, Object anchor)
	    BuoyViewNodeType createMainNode(Object linkId, Object anchor, BuoyLinkListener l)


    dep "calls" BuoyViewConnector BuoyLinkListener
    dep "use" RealBuoyViewManager BuoyViewNodeType
    dep "use" RealBuoyViewManager BuoyViewMainNode
    dep "use" RealBuoyViewManager BuoyViewConnector
    dep "create" BuoyViewNodeType BuoyViewMainNode

    ---

    vertically(60, zap, BuoyViewConnector, BuoyViewMainNode, BuoyViewNodeType);
    horizontally(100, foo, RealBuoyViewManager, zap);
    vertically(160, bar, BuoyLinkListener, RealBuoyViewManager);

The linkId passed to BuoyLinkListener must be unique for each node 
and the identifies the link -- it must be the same in both directions.
The previous invariant in code is an invariant which these interfaces
must obey::

    // mainnode1's BuoyLinkListener set to l.

    mainnode1.renderMain(...);
    connector.addBuoys(...);
    // l.link(d, *, nodetype2, L, A) got called as callback

    mainnode2 = nodetype2.createMainNode(L, A, l)
    mainnode2.renderMain(...);
    connector.addBuoys(...);
    // l.link(-d, *, nodetype1, L, B) MUST GET CALLED

Of course, if the underlying
data structure is modified between the calls to renderMain, 
the invariant need no longer hold.

The coordsys the main view is being rendered into defines the extent
by its (modified) unit square; this extent is not binding but more like
a hint.

Matcher structure can separate left&right links and know which way

Now, to make things clear, the call sequence for RealBuoyViewManager
is

.. UML:: BuoyManagerCall

    sequence CallBuoyManager
	call RealBuoyViewManager "render"
	    call BuoyViewMainNodeA "renderMain"
	    call BuoyViewConnector "addBuoys"
		call RealBuoyViewManager "link(NodeTypeB)"
	    call BuoyViewNodeTypeB "renderBuoy"
	call RealBuoyViewManager "mouse"
	    call BuoyViewNodeTypeB "createMainNode"
		create BuoyViewMainNodeB
	call RealBuoyViewManager "render"
	    call BuoyViewMainNodeB "renderMain"
	    call BuoyViewConnector "addBuoys"
		call RealBuoyViewManager "link(NodeTypeA)"
	    call BuoyViewNodeTypeA "renderBuoy"

    seqobject RealBuoyViewManager
    seqobject BuoyViewConnector
    seqobject BuoyViewNodeTypeA
    seqobject BuoyViewNodeTypeB
    seqobject BuoyViewMainNodeA
    seqobject BuoyViewMainNodeB


    ---
    RealBuoyViewManager.c = (0,0);
    horizontally(20, foo, RealBuoyViewManager, BuoyViewConnector, BuoyViewNodeTypeA, 
		BuoyViewMainNodeA, BuoyViewNodeTypeB,
		    BuoyViewMainNodeB.ghost);

In the diagram, we first see RealBuoyViewManager render the view, calling
BuoyViewMainNodeA to render the main node and getting a callback
through the BuoyLinkListener interface about a link. This causes it to call
BuoyViewNodeTypeB to render the buoy.

Then, the mouse click comes which takes the focus to the buoy of type B.
Its node type object is called to create the new focus object. Then, the 
render pattern is repeated, but now with BuoyViewMainNodeB in the focus.

Varying geometries
------------------

It is possible (e.g., FenPDF has this) that different main nodees require 
different geometries for the buoys.

Let's list the design pressures:

- The size of the area reserved for the buoy view has to be changeable easily
  (e.g., in FenPDF, scrolling the separator)

- The sizes and geometries should be set centrally, pluggably, not hardcoded
  to the different view

- The geometries depend on the main node type, but not on the content (at least
  yet).

- Different connectors should be able to have different buoy geometries (think, e.g.,
  about normal structlink buoys in fenpdf vs. the treetime buoys)

So, the solution is to provide the buoymanager different geometry managers, 
multiplexed between main node types and buoy types.
There are three different geometry managers, one for the main view and two
for the buoys. The buoys' geometry management is split into Sizer, which 
decides how large the buoy should be, and Geometer, which places it.

..  UML:: buoygeometry_flexible

    jlinkpackage org.nongnu.libvob.buoy

    class RealBuoyViewManager
	dep "use" BuoyMainViewGeometer
	dep "use" BuoyGeometer
	dep "use" BuoySizer

    class BuoyMainViewGeometer "interface"
	jlink


    class BuoyGeometer "interface"
	jlink
	

    class BuoySizer "interface"
	jlink

    jlinkpackage org.nongnu.libvob.buoy.impl

    class RatioMainGeometer
	jlink
	realize BuoyMainViewGeometer

    class BuoyOnCircleGeometer "abstract"
	jlink
	realize BuoyGeometer

    class RatioBuoyOnCircleGeometer 
	jlink
	inherit BuoyOnCircleGeometer

    class AspectBuoySizer
	jlink
	realize BuoySizer

    ---

    horizontally(100, xx, BuoyMainViewGeometer, BuoySizer, BuoyGeometer);

    vertically(60, kk, RealBuoyViewManager, xx);

    vertically(50, yy, BuoyGeometer, BuoyOnCircleGeometer, RatioBuoyOnCircleGeometer);

    vertically(130, zz, BuoyMainViewGeometer, RatioMainGeometer);
    vertically(130, ww, BuoySizer, AspectBuoySizer);

Both may be called two ways: to create the coordinate systems, or just to set
the changed coordinates.

Here's the geometer for the main views::

    public interface BuoyMainViewGeometer {
	/** Create or set the main coordinate system size.
	 * @param into The matching parent and parent, giving the rectangle into
	 *             which to place the cs.
	 * @param key The key to use for the returned coordinate system
	 *             in into. There may be others in between.
	 * @param create Whether to create the coordinate systems or just set parameters
	 *		in existing ones.
	 * @return The coordinate system into which to place the main view.
	 */
	int mainCS(VobScene vs, int into, Object key, boolean create);
    }

And here's the geometer for the buoys::

    public interface BuoyGeometer {

	/** Create any parameter coordinate systems depending on the surrounding
	 * rectangle. 
	 * This method **may** set state inside the BuoyGeometer.
	 * @param into The matching parent and parent, giving the rectangle in
	 *             which the whole buoyview and mainview are placed.
	 * @param create Whether to create the coordinate systems or just set parameters
	 *		in existing ones.
	 */
	void prepare(int into, boolean create);

	/** Create or set the main coordinate system size.
	 * @param into The matching parent and parent, giving the rectangle in
	 *             which the whole buoyview and mainview are placed.
	 * @param anchor The coordinate system of the anchor, or -1 if not applicable.
	 * @param direction 1 for right, -1 for left.
	 * @param key The key to use for the returned coordinate system
	 *             in into. There may be others in between.
	 * @param index The index of the buoy (counted from the anchor)
	 * @param total The total number of buoys from the anchor
	 *		(might be inaccurate)
	 * @param w,h The size the buoy box should be closest
	 * to the focus.
	 * @param scale The scale that should be applied to the w, h at
	 * focus
	 * 
	 */
	int buoyCS(VobScene vs, int into, int anchor, 
		    int direction,
		    Object key, 
		    int index, int total,
		    float w, float h, float scale);

    }

Before the previous interface is called,
the buoy size is found out from the buoy sizer interface::

    public interface BuoySizer {
	/** Get the size and scale
	 * a buoy should be shown at (at its maximum,
	 * nearest to the focus),
	 * @param w The pixel width desired by the buoy
	 * @param h The pixel height desired by the buoy
	 * @param whout The output width and height
	 * @return The scale to use (width and height are *before*
	 *   scaling, i.e. (400,400) and .5 means real size (200,200)
	 */
	float getBuoySize(float w, float h, float[] whout);
    }



.. :vim set syntax=text:
