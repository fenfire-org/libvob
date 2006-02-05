//COMPUTER GENERATED DO NOT EDIT
/* 
GLRen.template.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */


package org.nongnu.libvob.gl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen;
import org.nongnu.libvob.impl.lwjgl.LWJGL_API;

import java.awt.Graphics;


public class GLRen {

    public static Vob createCallList(String s) {
	if (GraphicsAPI.getInstance() instanceof LWJGL_API) {
	    return LWJGLRen.createCallList(s, LWJGLRen.NONE);
	}
	return createCallList(GL.createDisplayList(s));
    }
    public static Vob createCallListCoorded(String s) {
	if (GraphicsAPI.getInstance() instanceof LWJGL_API) {
	    return LWJGLRen.createCallList(s, LWJGLRen.COORDER);
	}
	return createCallListCoorded(GL.createDisplayList(s));
    }
    public static Vob createCallListBoxCoorded(String s) {
	if (GraphicsAPI.getInstance() instanceof LWJGL_API) {
	    return LWJGLRen.createCallList(s, LWJGLRen.BOX_COORDER);
	}
	return createCallListBoxCoorded(GL.createDisplayList(s));
    }
    /*
    public static Vob createMotionCallList(String still, String motion) {
	return createMotionCallList(GL.createDisplayList(still),
				    GL.createDisplayList(motion));
    }
    */

    private static boolean need_init = true;
    private static boolean have_VP_1_1;

    public static final int PAPERQUAD_CS2_TO_SCREEN = 1;
    public static final int PAPERQUAD_USE_VERTEX_PROGRAM = 2;

    public static final int IRREGU_SHIFTS = 0x0001;
    public static final int IRREGU_CS2_TO_SCREEN = 0x0002;

    public static void init() {
        if (GL.hasExtension("GL_NV_vertex_program1_1")) {
            have_VP_1_1 = true;
        } else {
            have_VP_1_1 = false;
        }
        need_init = false;
    }

    // ----  Shorthands.
    public static PaperQuad createPaperQuad(Paper paper, 
	    float x0, float y0, float x1, float y1, float dicefactor) {

            if (need_init) init();
            int flags = 0;

            if (have_VP_1_1) {
               flags |= PAPERQUAD_USE_VERTEX_PROGRAM;
            } else {
               flags &= ~PAPERQUAD_USE_VERTEX_PROGRAM;
            }
            
	return createPaperQuad(paper, x0, y0, x1, y1, 1, dicefactor, flags);
    }

    public static PaperQuad createPaperQuad(Paper paper, 
	    float x0, float y0, float x1, float y1, float dicefactor, int flags) {
	return createPaperQuad(paper, x0, y0, x1, y1, 1, dicefactor, flags);
    }

    public static IrregularQuad createIrregularQuad(float x0, float y0, float x1, float y1, float border, float freq, int flags,
						String setup, float dicefactor) {
	return createIrregularQuad(x0, y0, x1, y1, border, freq, flags, GL.createDisplayList(setup), dicefactor);
    }

    public static IrregularEdge createIrregularEdge(
	    int shape, float texscale, float linewidth, float refsize, float scale_pow,
            float border0, float border1, float texslicing,
            String const0, String const1, int angles, int multi, int flags,
            String setup, float dicefactor) {
	return createIrregularEdge(shape, texscale, linewidth, refsize, scale_pow,
            border0, border1, texslicing, const0, const1, angles, multi, flags,
            GL.createDisplayList(setup), dicefactor);
    }

    public static FixedPaperQuad createFixedPaperQuad(
	    org.nongnu.libvob.gl.Paper paper, 
	    float x0, float y0, float x1, float y1, 
	    int flags, 
	    float diceLength, float diceLength2, int diceDepth) {
	return createFixedPaperQuad(paper, x0, y0, x1, y1, flags, 
		    diceLength, diceLength2, diceDepth, null, 1);
    }


    
// <vob/trans/ScalarFuncs.hxx>
 
// <vob/trans/FisheyePrimitives.hxx>
 
// <vob/trans/DisablablePrimitives.hxx>
 
// <vob/trans/FunctionalPrimitives.hxx>
 
// <vob/trans/LinearPrimitives.hxx>
 
// <vob/vobs/Texture.hxx>
 static public class CopyTexSubImage2D extends GL.Renderable1JavaObject  { private CopyTexSubImage2D(int i) { super(i); }
}
static public CopyTexSubImage2D createCopyTexSubImage2D(String p0, int p1, int p2, int p3, int p4, int p5) { 
CopyTexSubImage2D _ = new CopyTexSubImage2D(
implcreateCopyTexSubImage2D(p0, p1, p2, p3, p4, p5));

 return _; }

static private native int implcreateCopyTexSubImage2D(String p0, int p1, int p2, int p3, int p4, int p5) ; 
static public class TexSubImage2D extends GL.Renderable0JavaObject  { private TexSubImage2D(int i) { super(i); }
GL.ByteVector p8;
}
static public TexSubImage2D createTexSubImage2D(String p0, int p1, int p2, int p3, int p4, int p5, String p6, String p7, GL.ByteVector p8) { 
TexSubImage2D _ = new TexSubImage2D(
implcreateTexSubImage2D(p0, p1, p2, p3, p4, p5, p6, p7, (p8 == null ? 0 : p8.getId())));
_.p8 = p8;

 return _; }

static private native int implcreateTexSubImage2D(String p0, int p1, int p2, int p3, int p4, int p5, String p6, String p7, int p8) ; 

// <vob/vobs/Debug.hxx>
 static public class DebugSwitch extends GL.Renderable0JavaObject  { private DebugSwitch(int i) { super(i); }
}
static public DebugSwitch createDebugSwitch(String p0, int p1) { 
DebugSwitch _ = new DebugSwitch(
implcreateDebugSwitch(p0, p1));

 return _; }

static private native int implcreateDebugSwitch(String p0, int p1) ; 

// <vob/vobs/Fillet.hxx>
 static public class SortedConnections extends GL.RenderableNJavaObject  { private SortedConnections(int i) { super(i); }
GL.RenderableNJavaObject p0;
GL.RenderableNJavaObject p1;
}
static public SortedConnections createSortedConnections(GL.RenderableNJavaObject p0, GL.RenderableNJavaObject p1, int p2) { 
SortedConnections _ = new SortedConnections(
implcreateSortedConnections((p0 == null ? 0 : p0.getId()), (p1 == null ? 0 : p1.getId()), p2));
_.p0 = p0;
_.p1 = p1;

 return _; }

static private native int implcreateSortedConnections(int p0, int p1, int p2) ; 
static public class IterConnections extends GL.RenderableNJavaObject  { private IterConnections(int i) { super(i); }
GL.RenderableNJavaObject p0;
GL.RenderableNJavaObject p1;
}
static public IterConnections createIterConnections(GL.RenderableNJavaObject p0, GL.RenderableNJavaObject p1, int p2) { 
IterConnections _ = new IterConnections(
implcreateIterConnections((p0 == null ? 0 : p0.getId()), (p1 == null ? 0 : p1.getId()), p2));
_.p0 = p0;
_.p1 = p1;

 return _; }

static private native int implcreateIterConnections(int p0, int p1, int p2) ; 
static public class FilletSpan2 extends GL.RenderableNJavaObject  { private FilletSpan2(int i) { super(i); }
}
static public FilletSpan2 createFilletSpan2(float p0, int p1, int p2) { 
FilletSpan2 _ = new FilletSpan2(
implcreateFilletSpan2(p0, p1, p2));

 return _; }

static private native int implcreateFilletSpan2(float p0, int p1, int p2) ; 
static public class Fillet3D extends GL.RenderableNJavaObject  { private Fillet3D(int i) { super(i); }
}
static public Fillet3D createFillet3D(float p0, int p1, int p2) { 
Fillet3D _ = new Fillet3D(
implcreateFillet3D(p0, p1, p2));

 return _; }

static private native int implcreateFillet3D(float p0, int p1, int p2) ; 
static public class Fillet3DBlend extends GL.RenderableNJavaObject  { private Fillet3DBlend(int i) { super(i); }
}
static public Fillet3DBlend createFillet3DBlend(int p0, float p1, int p2, int p3) { 
Fillet3DBlend _ = new Fillet3DBlend(
implcreateFillet3DBlend(p0, p1, p2, p3));

 return _; }

static private native int implcreateFillet3DBlend(int p0, float p1, int p2, int p3) ; 

// <vob/vobs/Lines.hxx>
 static public class ContinuousLine extends GL.Renderable1JavaObject  { private ContinuousLine(int i) { super(i); }
}
static public ContinuousLine createContinuousLine(int p0, float p1, int p2, boolean p3, float [] p4) { 
ContinuousLine _ = new ContinuousLine(
implcreateContinuousLine(p0, p1, p2, p3, p4));

 return _; }

static private native int implcreateContinuousLine(int p0, float p1, int p2, boolean p3, float [] p4) ; 

// <vob/vobs/GLState.hxx>
 static public class TransMatrix extends GL.Renderable1JavaObject  { private TransMatrix(int i) { super(i); }
}
static public TransMatrix createTransMatrix(String p0) { 
TransMatrix _ = new TransMatrix(
implcreateTransMatrix(p0));

 return _; }

static private native int implcreateTransMatrix(String p0) ; 
static public class TestStateRetainSetup extends GL.Renderable0JavaObject  { private TestStateRetainSetup(int i) { super(i); }
}
static public TestStateRetainSetup createTestStateRetainSetup() { 
TestStateRetainSetup _ = new TestStateRetainSetup(
implcreateTestStateRetainSetup());

 return _; }

static private native int implcreateTestStateRetainSetup() ; 
static public class TestStateRetainTest extends GL.Renderable0JavaObject  { private TestStateRetainTest(int i) { super(i); }
}
static public TestStateRetainTest createTestStateRetainTest() { 
TestStateRetainTest _ = new TestStateRetainTest(
implcreateTestStateRetainTest());

 return _; }

static private native int implcreateTestStateRetainTest() ; 

// <vob/vobs/Program.hxx>
 static public class ProgramLocalParameterARB extends GL.Renderable1JavaObject  { private ProgramLocalParameterARB(int i) { super(i); }
}
static public ProgramLocalParameterARB createProgramLocalParameterARB(String p0, int p1) { 
ProgramLocalParameterARB _ = new ProgramLocalParameterARB(
implcreateProgramLocalParameterARB(p0, p1));

 return _; }

static private native int implcreateProgramLocalParameterARB(String p0, int p1) ; 
static public class ProgramNamedParameterNV extends GL.Renderable1JavaObject  { private ProgramNamedParameterNV(int i) { super(i); }
}
static public ProgramNamedParameterNV createProgramNamedParameterNV(int p0, String p1) { 
ProgramNamedParameterNV _ = new ProgramNamedParameterNV(
implcreateProgramNamedParameterNV(p0, p1));

 return _; }

static private native int implcreateProgramNamedParameterNV(int p0, String p1) ; 

// <vob/vobs/Irregu.hxx>
 static public class IrregularQuad extends GL.Renderable2JavaObject  { private IrregularQuad(int i) { super(i); }
GL.DisplayList p7;
}
static public IrregularQuad createIrregularQuad(float p0, float p1, float p2, float p3, float p4, float p5, int p6, GL.DisplayList p7, float p8) { 
IrregularQuad _ = new IrregularQuad(
implcreateIrregularQuad(p0, p1, p2, p3, p4, p5, p6, (p7 == null ? 0 : p7.getId()), p8));
_.p7 = p7;

 return _; }

static private native int implcreateIrregularQuad(float p0, float p1, float p2, float p3, float p4, float p5, int p6, int p7, float p8) ; 
static public class IrregularEdge extends GL.Renderable2JavaObject  { private IrregularEdge(int i) { super(i); }
GL.DisplayList p13;
}
static public IrregularEdge createIrregularEdge(int p0, float p1, float p2, float p3, float p4, float p5, float p6, float p7, String p8, String p9, int p10, int p11, int p12, GL.DisplayList p13, float p14) { 
IrregularEdge _ = new IrregularEdge(
implcreateIrregularEdge(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, (p13 == null ? 0 : p13.getId()), p14));
_.p13 = p13;

 return _; }

static private native int implcreateIrregularEdge(int p0, float p1, float p2, float p3, float p4, float p5, float p6, float p7, String p8, String p9, int p10, int p11, int p12, int p13, float p14) ; 

// <vob/vobs/Paper.hxx>
 static public class DiceTester extends GL.Renderable1JavaObject  { private DiceTester(int i) { super(i); }
}
static public DiceTester createDiceTester(float p0, float p1, int p2, int p3) { 
DiceTester _ = new DiceTester(
implcreateDiceTester(p0, p1, p2, p3));

 return _; }

static private native int implcreateDiceTester(float p0, float p1, int p2, int p3) ; 
static public class FixedPaperQuad extends GL.Renderable1JavaObject  { private FixedPaperQuad(int i) { super(i); }
Paper p0;
GL.TexAccum p9;
}
static public FixedPaperQuad createFixedPaperQuad(Paper p0, float p1, float p2, float p3, float p4, int p5, float p6, float p7, int p8, GL.TexAccum p9, float p10) { 
FixedPaperQuad _ = new FixedPaperQuad(
implcreateFixedPaperQuad((p0 == null ? 0 : p0.getId()), p1, p2, p3, p4, p5, p6, p7, p8, (p9 == null ? 0 : p9.getId()), p10));
_.p0 = p0;
_.p9 = p9;

 return _; }

static private native int implcreateFixedPaperQuad(int p0, float p1, float p2, float p3, float p4, int p5, float p6, float p7, int p8, int p9, float p10) ; 
static public class PaperQuad extends GL.Renderable2JavaObject  { private PaperQuad(int i) { super(i); }
Paper p0;
}
static public PaperQuad createPaperQuad(Paper p0, float p1, float p2, float p3, float p4, float p5, float p6, int p7) { 
PaperQuad _ = new PaperQuad(
implcreatePaperQuad((p0 == null ? 0 : p0.getId()), p1, p2, p3, p4, p5, p6, p7));
_.p0 = p0;

 return _; }

static private native int implcreatePaperQuad(int p0, float p1, float p2, float p3, float p4, float p5, float p6, int p7) ; 
static public class EasyPaperQuad extends GL.Renderable2JavaObject  { private EasyPaperQuad(int i) { super(i); }
Paper p0;
}
static public EasyPaperQuad createEasyPaperQuad(Paper p0, float p1, int p2) { 
EasyPaperQuad _ = new EasyPaperQuad(
implcreateEasyPaperQuad((p0 == null ? 0 : p0.getId()), p1, p2));
_.p0 = p0;

 return _; }

static private native int implcreateEasyPaperQuad(int p0, float p1, int p2) ; 
static public class BasisPaperQuad extends GL.Renderable2JavaObject  { private BasisPaperQuad(int i) { super(i); }
Paper p0;
GL.DisplayList p9;
GL.DisplayList p10;
GL.DisplayList p11;
}
static public BasisPaperQuad createBasisPaperQuad(Paper p0, float p1, float p2, float p3, float p4, float p5, float p6, float p7, float p8, GL.DisplayList p9, GL.DisplayList p10, GL.DisplayList p11) { 
BasisPaperQuad _ = new BasisPaperQuad(
implcreateBasisPaperQuad((p0 == null ? 0 : p0.getId()), p1, p2, p3, p4, p5, p6, p7, p8, (p9 == null ? 0 : p9.getId()), (p10 == null ? 0 : p10.getId()), (p11 == null ? 0 : p11.getId())));
_.p0 = p0;
_.p9 = p9;
_.p10 = p10;
_.p11 = p11;

 return _; }

static private native int implcreateBasisPaperQuad(int p0, float p1, float p2, float p3, float p4, float p5, float p6, float p7, float p8, int p9, int p10, int p11) ; 

// <vob/vobs/Text.hxx>
 static public class Text1 extends GL.Renderable1JavaObject  { private Text1(int i) { super(i); }
GL.QuadFont p0;
}
static public Text1 createText1(GL.QuadFont p0, String p1, float p2, int p3) { 
Text1 _ = new Text1(
implcreateText1((p0 == null ? 0 : p0.getId()), p1, p2, p3));
_.p0 = p0;

 return _; }

static private native int implcreateText1(int p0, String p1, float p2, int p3) ; 
static public class TextSuper4 extends GL.Renderable1JavaObject  { private TextSuper4(int i) { super(i); }
GL.QuadFont p0;
}
static public TextSuper4 createTextSuper4(GL.QuadFont p0, String p1, float p2, int p3) { 
TextSuper4 _ = new TextSuper4(
implcreateTextSuper4((p0 == null ? 0 : p0.getId()), p1, p2, p3));
_.p0 = p0;

 return _; }

static private native int implcreateTextSuper4(int p0, String p1, float p2, int p3) ; 

// <vob/vobs/Pixel.hxx>
 static public class DrawPixels extends GL.Renderable1JavaObject  { private DrawPixels(int i) { super(i); }
GL.ByteVector p4;
}
static public DrawPixels createDrawPixels(int p0, int p1, String p2, String p3, GL.ByteVector p4) { 
DrawPixels _ = new DrawPixels(
implcreateDrawPixels(p0, p1, p2, p3, (p4 == null ? 0 : p4.getId())));
_.p4 = p4;

 return _; }

static private native int implcreateDrawPixels(int p0, int p1, String p2, String p3, int p4) ; 
static public class ReadPixels extends GL.Renderable1JavaObject  { private ReadPixels(int i) { super(i); }
GL.ByteVector p4;
}
static public ReadPixels createReadPixels(int p0, int p1, String p2, String p3, GL.ByteVector p4) { 
ReadPixels _ = new ReadPixels(
implcreateReadPixels(p0, p1, p2, p3, (p4 == null ? 0 : p4.getId())));
_.p4 = p4;

 return _; }

static private native int implcreateReadPixels(int p0, int p1, String p2, String p3, int p4) ; 
static public class CopyPixels extends GL.Renderable2JavaObject  { private CopyPixels(int i) { super(i); }
}
static public CopyPixels createCopyPixels(int p0, int p1, String p2) { 
CopyPixels _ = new CopyPixels(
implcreateCopyPixels(p0, p1, p2));

 return _; }

static private native int implcreateCopyPixels(int p0, int p1, String p2) ; 

// <vob/vobs/Trivial.hxx>
 static public class LineConnector extends GL.Renderable2JavaObject  { private LineConnector(int i) { super(i); }
}
static public LineConnector createLineConnector(float p0, float p1, float p2, float p3) { 
LineConnector _ = new LineConnector(
implcreateLineConnector(p0, p1, p2, p3));

 return _; }

static private native int implcreateLineConnector(float p0, float p1, float p2, float p3) ; 
static public class PinStub extends GL.Renderable2JavaObject  { private PinStub(int i) { super(i); }
}
static public PinStub createPinStub(float p0, float p1, float p2, float p3, float p4, float p5) { 
PinStub _ = new PinStub(
implcreatePinStub(p0, p1, p2, p3, p4, p5));

 return _; }

static private native int implcreatePinStub(float p0, float p1, float p2, float p3, float p4, float p5) ; 
static public class NonFilledRectangle extends GL.Renderable1JavaObject  { private NonFilledRectangle(int i) { super(i); }
}
static public NonFilledRectangle createNonFilledRectangle(float p0, float p1, float p2, float p3, float p4, float p5, float p6, float p7, float p8) { 
NonFilledRectangle _ = new NonFilledRectangle(
implcreateNonFilledRectangle(p0, p1, p2, p3, p4, p5, p6, p7, p8));

 return _; }

static private native int implcreateNonFilledRectangle(float p0, float p1, float p2, float p3, float p4, float p5, float p6, float p7, float p8) ; 
static public class CallList extends GL.Renderable0JavaObject  { private CallList(int i) { super(i); }
GL.DisplayList p0;
}
static public CallList createCallList(GL.DisplayList p0) { 
CallList _ = new CallList(
implcreateCallList((p0 == null ? 0 : p0.getId())));
_.p0 = p0;

 return _; }

static private native int implcreateCallList(int p0) ; 
static public class CallListCoorded extends GL.Renderable1JavaObject  { private CallListCoorded(int i) { super(i); }
GL.DisplayList p0;
}
static public CallListCoorded createCallListCoorded(GL.DisplayList p0) { 
CallListCoorded _ = new CallListCoorded(
implcreateCallListCoorded((p0 == null ? 0 : p0.getId())));
_.p0 = p0;

 return _; }

static private native int implcreateCallListCoorded(int p0) ; 
static public class CallListBoxCoorded extends GL.Renderable1JavaObject  { private CallListBoxCoorded(int i) { super(i); }
GL.DisplayList p0;
}
static public CallListBoxCoorded createCallListBoxCoorded(GL.DisplayList p0) { 
CallListBoxCoorded _ = new CallListBoxCoorded(
implcreateCallListBoxCoorded((p0 == null ? 0 : p0.getId())));
_.p0 = p0;

 return _; }

static private native int implcreateCallListBoxCoorded(int p0) ; 
static public class Quad extends GL.Renderable1JavaObject  { private Quad(int i) { super(i); }
}
static public Quad createQuad(int p0, int p1, int p2) { 
Quad _ = new Quad(
implcreateQuad(p0, p1, p2));

 return _; }

static private native int implcreateQuad(int p0, int p1, int p2) ; 
static public class TransTest extends GL.Renderable1JavaObject  { private TransTest(int i) { super(i); }
}
static public TransTest createTransTest(int p0, int p1) { 
TransTest _ = new TransTest(
implcreateTransTest(p0, p1));

 return _; }

static private native int implcreateTransTest(int p0, int p1) ; 
static public class SelectVob extends GL.Renderable2JavaObject  { private SelectVob(int i) { super(i); }
GL.Renderable1JavaObject p0;
GL.Renderable1JavaObject p1;
GL.Renderable1JavaObject p2;
}
static public SelectVob createSelectVob(GL.Renderable1JavaObject p0, GL.Renderable1JavaObject p1, GL.Renderable1JavaObject p2) { 
SelectVob _ = new SelectVob(
implcreateSelectVob((p0 == null ? 0 : p0.getId()), (p1 == null ? 0 : p1.getId()), (p2 == null ? 0 : p2.getId())));
_.p0 = p0;
_.p1 = p1;
_.p2 = p2;

 return _; }

static private native int implcreateSelectVob(int p0, int p1, int p2) ; 



}
