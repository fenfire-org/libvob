// (c) Matti J. Katila

package org.nongnu.libvob.gl;

import org.nongnu.libvob.GraphicsAPI;
import org.nongnu.libvob.TextStyle;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.gl.PaperMill.Paper;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen;
import org.nongnu.libvob.impl.gl.GLTextStyle;
import org.nongnu.libvob.impl.lwjgl.LWJGL_API;


/** The Ren class contains abstraction layer to create and use objects 
 * able to render in OpenGL. This class has many tag interfaces.
 * 
 * @author Matti J. Katila
 */
public class Ren {

    public interface SelectVob {
    }
    public interface TransTest {
    }
    public interface Quad {
    }
    public interface CallListBoxCoorded {
    }
    public interface CallListCoorded {
    }
    public interface CallList {
    }
    public interface NonFilledRectangle {
    }
    public interface PinStub {
    }
    public interface LineConnector {
    }
    public interface CopyPixels {
    }
    public interface ReadPixels {
    }
    public interface DrawPixels {
    }
    public interface TextSuper4 {
    }
    public interface Text1 {
    }
    public interface BasisPaperQuad {
    }
    public interface EasyPaperQuad {
    }
    
    final static public int PAPERQUAD_CS2_TO_SCREEN = 1;
    final static public int PAPERQUAD_USE_VERTEX_PROGRAM = 2;
    final static public int PAPERQUAD_NONL_MAXLEN = 4;
    public interface PaperQuad extends Vob {
    }

    public interface FixedPaperQuad {
    }
    public interface DiceTester {
    }
    public interface IrregularEdge {
    }
    public interface IrregularQuad {
    }
    public interface ProgramNamedParameterNV {
    }
    public interface ProgramLocalParameterARB {
    }
    public interface TestStateRetainTest {
    }
    public interface TestStateRetainSetup {
    }
    public interface TransMatrix {
    }
    public interface ContinuousLine {
    }
    public interface Fillet3DBlend {
    }
    public interface Fillet3D {
    }
    public interface FilletSpan2 {
    }
    public interface IterConnections {
    }
    public interface SortedConnections {
    }
    public interface DebugSwitch {
    }
    public interface TexSubImage2D {
    }
    public interface CopyTexSubImage2D {
    }

    static LWJGLRen lwjglRenderables = null;
    static {
	if (GraphicsAPI.getInstance() instanceof LWJGL_API)
	    lwjglRenderables = new LWJGLRen();
    }
    
    public static PaperQuad createPaperQuad(PaperMill.Paper paper, 
	    float x0, float y0, float x1, float y1, float dicefactor) {

	if (lwjglRenderables != null)
	    return lwjglRenderables.createPaperQuad(paper, x0,y0,x1,y1, dicefactor);
	else
	    return (PaperQuad)GLRen.createPaperQuad(paper, x0, y0, x1, y1, dicefactor);
    }

    /* Where it's easier to directly call implementation - do it.
    public static Text1 createText1(TextStyle p0, String p1, float p2, int p3) { 
	if (lwjglRenderables != null)
	    return lwjglRenderables.createText1(p0,p1,p2,p3);
	else {
	    GLTextStyle gls = (GLTextStyle)p0;
	    return (Text1)GLRen.createText1(gls.getQuadFont(), p1, p2, p3);
	}
    }
    */

}
