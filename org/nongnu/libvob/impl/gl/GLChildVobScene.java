// (c) Tuomas J. Lukka


package org.nongnu.libvob.impl.gl;
import org.nongnu.libvob.*;
import java.awt.Dimension;

/** A Vobscene that may be placed into another vobscene.
 */
public class GLChildVobScene extends ChildVobScene {

    ChildVS childVS = null;

    int nParamCoordsys;

    public GLChildVobScene(VobMap m, VobCoorder c, VobMatcher mat,
			    GraphicsAPI gfxapi, 
			    GraphicsAPI.RenderingSurface window,
				Dimension size,
				int nParamCoordsys) { 
	super(m, c, mat, gfxapi, window, size);
	this.nParamCoordsys = nParamCoordsys;
    }
}
