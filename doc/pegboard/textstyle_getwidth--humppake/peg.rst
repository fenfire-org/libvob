==========================================================================
``textstyle_getwidth--humppake``: How to get valid text widths in AWT
==========================================================================

:Authors:	Benja Fallenstein, Asko Soukka
:Date:		2002-11-26
:Last-Modified:	$Date: 2003/03/31 10:00:03 $
:Revision:	$Revision: 1.1 $
:Status:	Irrelevant
:Scope:		Minor
:Type:		Interface

::

	<benja> in AWT, we need to do all the font-related calculations 
		in screen coordinates (pixels)
	<benja> so, we first need to transform local coordinates 
		into screen coordinates
	<benja> then we can calculate the scale correctly

	<benja> not only do we have to encapsulate getting the scale
	<benja> but also getting the width
	<benja> that can go into TextStyle

Problem
-------

We ran into problem with TextCellContentView. We got proper scrolling 
and correctly placed cursor vob in AWT, only after a few ugly
explicit transformations::

	<benja> actually, we first need to calculate the text width 
		in screen coordinates
	<benja> and then convert *that* to local 
	
	<benja> the problem is the difference between screen 
		coordinates and coordinates inside the box coordinate 
		system
	<benja> (or should I say: The problem is the difference 
		between the screen and the box coordinate system)
	<humppake> benja: And why it is not a problem with GL?
	<benja> humppake, because of the difference in how 
		getScaleByHeight works, I think
	<benja> humppake, the main difference between AWT and GL here
		is that GL has arbitrarily scalable fonts

Issues
------

Is there any way to make ``AWTTextStyle`` scale virtually arbitrarily
like GLTextStyle?

Would it be enough to fix our problem (with ``TextCellContentView``
for example)?

If not, which would be the best of the following alternatives as  the
new interface?
	
Alternatives
------------

One special method
~~~~~~~~~~~~~~~~~~

::

	<benja> why not simply float getWidthForHeight(VobCoorder 
		coorder, int cs, String s, float height)?

	<benja> so that we can provide an AWT-specific 
		impl in AWTTextStyle
	<benja> what we need to get is: the width of the text inside a 
		given coordsys, if the height of the text in that 
		coordsys is X
	<benja> however, you can have a rather simple implementation
		for GL
	<benja> which doesn't actually look at the cs, probably


Stack of CSs
~~~~~~~~~~~~~

::

	<humppake> benja: I mean, if we give TextStyle a CS before
		calling getFunctions, it will use that CS to transform 
		the results.
	<humppake> benja: It lasts only for one call O:-)
	<benja> I don't know. It could be a possibility, if we find 
		good definitions for all the methods