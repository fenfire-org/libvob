// (c) Tuomas J. Lukka


package org.nongnu.libvob;
import java.awt.Dimension;

/** A Vobscene that may be placed into another vobscene.
 */
public class ChildVobScene extends VobScene {
    public ChildVobScene(VobMap m, VobCoorder c, VobMatcher mat,
			    GraphicsAPI gfxapi, 
			    GraphicsAPI.RenderingSurface window,
				Dimension size) { 
	super(m, c, mat, gfxapi, window, size);
    }
}
