=============================================================
PEG ``vob_renderinfo--tjl``: Reduce Vob.RenderInfo
=============================================================

:Authors:  Tuomas Lukka
:Stakeholders: Benja Fallenstein
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Accepted

With the transformPoints3 function of PEG 1009, most of Vob.RenderInfo
is not necessary any more.

Changes
-------

Eliminate::

	public abstract void xform(float x, float y, Point into);
	public abstract void xform_horiz(float x, float y, Point into);
	public abstract void xform_vert(float x, float y, Point into);

	public abstract void getExtRect(Rectangle into);

        public abstract void getClipRect(Rectangle into);

	public abstract float getAspectRatio();

	public abstract void invert(float xs, float ys, float[] xyout);

Also, all render() methods should only get one RenderInfo object, as
it will mostly be about colors.

The render() method's new prototype would be

	public void render(Graphics g, VobCoorder coords,
		    int cs1, int cs2, Vob.RenderInfo ri);
