
=============================================================
PEG vobcoorder_fontscale--tjl: 
=============================================================

:Author:   Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Current

We need to improve the scale mechanism for fonts.

Issues
------

- How should getFontScale be used with TextStyle and TextVob?

    RESOLVED: In a bit awkward way, but workable::

	scale = coords.getFontScale(cs)
	height = textStyle.getHeight(scale) / scale
	textcs = coords.ortho(cs, 0, 0, height, height)
	vs.put(textvob, textcs)

    This example puts text that is in the natural height of its textStyle inside
    the coordinate system.

Changes
-------

Into VobCoorder, add::

    /** Get the scale that vobs put into this coordsys have,
     * for use with AWT font metrics.
     * The semantics depends on the current windowing system:
     * <ul>
     * <li> If fonts in the current system scale in any desirable way,
     *      this method will always return 1.
     * <li> If fonts in the current system change their metrics as a result
     *      of scaling (e.g. AWT), this method returns the ratio between 
     *      the change of coordinates inside the coordinate system and the
     *      change of coordinates in the window coordinates, i.e. the magnification
     *      ratio.
     * 		If the coordinate system is an anisotropic scaling, the results
     * 		are undefined (but should be reasonable, somewhere between the two
     *          scales).
     * </ul>
     */
    float getFontScale(int cs);

