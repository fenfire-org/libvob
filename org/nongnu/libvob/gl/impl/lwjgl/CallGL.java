// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl;

import java.awt.Graphics;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.lwjgl.opengl.ARBImaging;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ARBProgram;
import org.lwjgl.opengl.ARBVertexProgram;
import org.lwjgl.opengl.EXTSecondaryColor;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.NVProgram;
import org.lwjgl.opengl.NVRegisterCombiners;
import org.lwjgl.opengl.NVRegisterCombiners2;
import org.lwjgl.opengl.NVVertexProgram;
import org.lwjgl.opengl.glu.GLU;
import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.Vob.RenderInfo;

public class CallGL extends AbstractVob implements LWJGLRen.Vob0 {

    private static boolean Begin;

    protected int callList;

    public CallGL(String s) {
	checkGlError("before callGL compilation.");

	callList = GL11.glGenLists(1);
	GL11.glNewList(callList, GL11.GL_COMPILE);
	call(s);
	GL11.glEndList();

	checkGlError("after callGL compilation.");
    }

    public static void call(String s) {
	checkGlError("before callGL operation.");

	// tokenize to lines
	StringTokenizer st = new StringTokenizer(s, "\n");
	while (st.hasMoreTokens()) {
	    String line = st.nextToken();
	    int hashPos = line.indexOf('#');
	    if (hashPos >= 0)
		line = line.substring(0, hashPos);

	    ArrayList call = new ArrayList();
	    StringTokenizer words = new StringTokenizer(line, " \t");
	    while (words.hasMoreTokens()) {
		String word = words.nextToken().trim();
		call.add(word);
	    }
	    String[] callArr = new String[call.size()];
	    call.toArray(callArr);
	    call(callArr);
	}
    }

    private static boolean call(String[] v) {

	if (v.length == 0)
	    return true;

	if (checkfunc(v, "Enable", 1)) {
	    GL11.glEnable(getToken(v[1]));
	} else if (checkfunc(v, "Disable", 1)) {
	    GL11.glDisable(getToken(v[1]));
	} else if (checkfunc(v, "EnableVertexAttribArray", 1)) {
	    ARBVertexProgram.glEnableVertexAttribArrayARB(getToken(v[1]));
	} else if (checkfunc(v, "DisableVertexAttribArray", 1)) {
	    ARBVertexProgram.glDisableVertexAttribArrayARB(getToken(v[1]));
	} else if (checkfunc(v, "Hint", 2)) {
	    GL11.glHint(getToken(v[1]), getToken(v[2]));
	} else if (checkfunc(v, "ReadBuffer", 1)) {
	    GL11.glReadBuffer(getToken(v[1]));
	} else if (checkfunc(v, "DrawBuffer", 1)) {
	    GL11.glDrawBuffer(getToken(v[1]));
	} else if (checkfunc(v, "CallList", 1)) {
	    GL11.glCallList(getToken(v[1]));
	} else if (checkfunc(v, "MatrixMode", 1)) {
	    GL11.glMatrixMode(getToken(v[1]));
	} else if (checkfunc(v, "PushMatrix", 0)) {
	    GL11.glPushMatrix();
	} else if (checkfunc(v, "PopMatrix", 0)) {
	    GL11.glPopMatrix();
	} else if (checkfunc(v, "LoadIdentity", 0)) {
	    GL11.glLoadIdentity();
	} else if (checkfunc(v, "LoadMatrix", 16)) {
	    DoubleBuffer params = getdv(v, 1, 16);
	    GL11.glLoadMatrix(params);
	} else if (checkfunc(v, "MultMatrix", 16)) {
	    DoubleBuffer params = getdv(v, 1, 16);
	    GL11.glMultMatrix(params);
	} else if (checkfunc(v, "Rotate", 4)) {
	    GL11.glRotatef(getTokenf(v[1]), getTokenf(v[2]), getTokenf(v[3]),
		    getTokenf(v[4]));
	} else if (checkfunc(v, "Scale", 3)) {
	    GL11.glScalef(getTokenf(v[1]), getTokenf(v[2]), getTokenf(v[3]));
	} else if (checkfunc(v, "Translate", 3)) {
	    GL11
		    .glTranslated(getTokend(v[1]), getTokend(v[2]),
			    getTokend(v[3]));
	} else if (checkfunc(v, "Ortho", 6)) {
	    GL11.glOrtho(getTokend(v[1]), getTokend(v[2]), getTokend(v[3]),
		    getTokend(v[4]), getTokend(v[5]), getTokend(v[6]));
	} else if (checkfunc(v, "Frustum", 6)) {
	    GL11.glFrustum(getTokenf(v[1]), getTokenf(v[2]), getTokenf(v[3]),
		    getTokenf(v[4]), getTokenf(v[5]), getTokenf(v[6]));
	} else if (checkfunc(v, "uLookAt", 9)) { // [gl]uLookAt
	    GLU.gluLookAt(getTokenf(v[1]), getTokenf(v[2]), getTokenf(v[3]),
		    getTokenf(v[4]), getTokenf(v[5]), getTokenf(v[6]),
		    getTokenf(v[7]), getTokenf(v[8]), getTokenf(v[9]));
	} else if (checkfunc(v, "BindTexture", 2)) {
	    GL11.glBindTexture(getToken(v[1]), getToken(v[2]));
	} else if (checkfunc(v, "DeleteTextures", 0, true)) {
	    IntBuffer params = getiv(v, 1);
	    GL11.glDeleteTextures(params);
	} else if (checkfunc(v, "DepthMask", 1)) {
	    GL11.glDepthMask(getTokenb(v[1]));
	} else if (checkfunc(v, "ColorMask", 4)) {
	    GL11.glColorMask(getTokenb(v[1]), getTokenb(v[2]), getTokenb(v[3]),
		    getTokenb(v[4]));
	} else if (checkfunc(v, "AlphaFunc", 2)) {
	    GL11.glAlphaFunc(getToken(v[1]), getTokenf(v[2]));
	} else if (checkfunc(v, "DepthFunc", 1)) {
	    GL11.glDepthFunc(getToken(v[1]));
	} else if (checkfunc(v, "BlendFunc", 2)) {
	    GL11.glBlendFunc(getToken(v[1]), getToken(v[2]));
	} else if (checkfunc(v, "StencilFunc", 3)) {
	    GL11.glStencilFunc(getToken(v[1]), getToken(v[2]), getToken(v[3]));
	} else if (checkfunc(v, "StencilOp", 3)) {
	    GL11.glStencilOp(getToken(v[1]), getToken(v[2]), getToken(v[3]));
	} else if (checkfunc(v, "StencilMask", 1)) {
	    GL11.glStencilMask(getToken(v[1]));
	} else if (checkfunc(v, "PolygonOffset", 2)) {
	    GL11.glPolygonOffset(getTokenf(v[1]), getTokenf(v[2]));
	} else if (checkfunc(v, "PolygonMode", 2)) {
	    GL11.glPolygonMode(getToken(v[1]), getToken(v[2]));
	} else if (checkfunc(v, "ShadeModel", 1)) {
	    GL11.glShadeModel(getToken(v[1]));
	} else if (checkfunc(v, "BlendEquation", 1)) {
	    ARBImaging.glBlendEquation(getToken(v[1]));
	} else if (checkfunc(v, "BlendColor", 4)) {
	    ARBImaging.glBlendColor(getTokenf(v[1]), getTokenf(v[2]),
		    getTokenf(v[3]), getTokenf(v[4]));
	} else if (checkfunc(v, "SecondaryColorEXT", 3)) {
	    EXTSecondaryColor.glSecondaryColor3dEXT(getTokenf(v[1]),
		    getTokenf(v[2]), getTokenf(v[3]));

	} else if (checkfunc(v, "Fog", 2, true)) {
	    if (v.length > 3) {
		FloatBuffer params = getfv(v, 2, 4);
		GL11.glFog(getToken(v[1]), params);
	    } else {
		GL11.glFogf(getToken(v[1]), getTokenf(v[2]));
	    }
	} else if (checkfunc(v, "TexSubImage2D", 8, true)) {
	    int w = getToken(v[5]);
	    int h = getToken(v[6]);
	    FloatBuffer pixels = getfv(v, 8, w * h);
	    if ((pixels.capacity() % (w * h)) != 0) {
		System.out.println("TexSubImage2D dimensions " + w + "x" + h
			+ " do not match the size " + pixels.capacity()
			+ " of data\n");
		return false;
	    }
	    GL11.glTexSubImage2D(getToken(v[1]), getToken(v[2]),
		    getToken(v[3]), getToken(v[4]), w, h, getToken(v[7]),
		    GL11.GL_FLOAT, pixels);
	} else if (checkfunc(v, "TexImage2D", 8, true)) {
	    int w = getToken(v[4]);
	    int h = getToken(v[5]);
	    FloatBuffer pixels = getfv(v, 8, w * h);
	    if ((pixels.capacity() % (w * h)) != 0) {
		System.out.println("TexImage2D dimensions " + w + "x" + h
			+ " do not match the size " + pixels.capacity()
			+ " of data\n");
		return false;
	    }
	    GL11
		    .glTexImage2D(getToken(v[1]), getToken(v[2]),
			    getToken(v[3]), w, h, getToken(v[6]),
			    getToken(v[7]), GL11.GL_FLOAT, pixels);
	} else if (checkfunc(v, "TexImage2D_ushort", 8, true)) {
	    int w = getToken(v[4]);
	    int h = getToken(v[5]);
	    ShortBuffer pixels = getiv(v, 8, w * h);
	    if ((pixels.capacity() % (w * h)) != 0) {
		System.out.println("TexImage2D dimensions " + w + "x" + h
			+ " do not match the size " + pixels.capacity()
			+ " of data\n");
		return false;
	    }
	    GL11.glTexImage2D(getToken(v[1]), getToken(v[2]), getToken(v[3]),
		    w, h, getToken(v[6]), getToken(v[7]),
		    GL11.GL_UNSIGNED_SHORT, pixels);
	} else if (checkfunc(v, "CopyTexImage2D", 8)) {
	    GL11.glCopyTexImage2D(getToken(v[1]), getToken(v[2]),
		    getToken(v[3]), getToken(v[4]), getToken(v[5]),
		    getToken(v[6]), getToken(v[7]), getToken(v[8]));
	} else if (checkfunc(v, "CopyTexSubImage2D", 8)) {
	    GL11.glCopyTexSubImage2D(getToken(v[1]), getToken(v[2]),
		    getToken(v[3]), getToken(v[4]), getToken(v[5]),
		    getToken(v[6]), getToken(v[7]), getToken(v[8]));
	} else if (checkfunc(v, "ColorTable", 5, true)
		|| checkfunc(v, "ColorTableEXT", 5, true)) {
	    FloatBuffer pixels = getfv(v, 5);

	    ARBImaging.glColorTable(getToken(v[1]), getToken(v[2]),
		    getToken(v[3]), getToken(v[4]), GL11.GL_FLOAT, pixels);
	} else if (checkfunc(v, "SeparableFilter2D", 6, true)) {
	    if (true)
		throw new Error("unimpl.");
	    /*
                 * FloatBuffer filters = getfv(v, 6); int w = getToken(v[3]);
                 * int h = getToken(v[4]);
                 * 
                 * ARBImaging.glSeparableFilter2D( getToken(v[1]),
                 * getToken(v[2]), w, h, getToken(v[5]), GL11.GL_FLOAT,
                 * filters[0]), filters[w]));
                 */
	} else if (checkfunc(v, "ConvolutionFilter2D", 6, true)) {
	    IntBuffer filters = getiv(v, 6);
	    int w = getToken(v[3]);
	    int h = getToken(v[4]);

	    ARBImaging.glConvolutionFilter2D(getToken(v[1]), getToken(v[2]), w,
		    h, getToken(v[5]), GL11.GL_INT, filters);

	} else if (checkfunc(v, "ConvolutionParameter", 3, true)) {
	    if (v.length > 4) {
		FloatBuffer params = getfv(v, 3, 4);
		ARBImaging.glConvolutionParameter(getToken(v[1]),
			getToken(v[2]), params);
	    } else {
		ARBImaging.glConvolutionParameterf(getToken(v[1]),
			getToken(v[2]), getTokenf(v[3]));
	    }

	} else if (checkfunc(v, "PixelTransfer", 2)) {
	    GL11.glPixelTransferf(getToken(v[1]), getTokenf(v[2]));

	} else if (checkfunc(v, "TexEnv", 3, true)) {
	    if (v.length > 4) {
		FloatBuffer params = getfv(v, 3, 4);
		GL11.glTexEnv(getToken(v[1]), getToken(v[2]), params);
	    } else {
		GL11.glTexEnvf(getToken(v[1]), getToken(v[2]), getTokenf(v[3]));
	    }
	} else if (checkfunc(v, "TexParameter", 3, true)) {
	    if (v.length > 4) {
		FloatBuffer params = getfv(v, 3, 4);
		GL11.glTexParameter(getToken(v[1]), getToken(v[2]), params);
	    } else {
		GL11.glTexParameterf(getToken(v[1]), getToken(v[2]),
			getTokenf(v[3]));
	    }
	} else if (checkfunc(v, "TexGen", 3, true)) {
	    if (v.length > 4) {
		DoubleBuffer params = getdv(v, 3, 4);
		GL11.glTexGen(getToken(v[1]), getToken(v[2]), params);
	    } else {
		GL11.glTexGend(getToken(v[1]), getToken(v[2]), getTokenf(v[3]));
	    }
	} else if (checkfunc(v, "PushAttrib", 0, true)) {
	    int mask = 0;
	    for (int i = 1; i < v.length; i++)
		mask |= getToken(v[i]);
	    GL11.glPushAttrib(mask);
	} else if (checkfunc(v, "PopAttrib", 0)) {
	    GL11.glPopAttrib();
	} else if (checkfunc(v, "PushClientAttrib", 0, true)) {
	    int mask = 0;
	    for (int i = 1; i < v.length; i++)
		mask |= getToken(v[i]);
	    GL11.glPushClientAttrib(mask);
	} else if (checkfunc(v, "PopClientAttrib", 0)) {
	    GL11.glPopClientAttrib();
	} else if (checkfunc(v, "Clear", 0, true)) {
	    int mask = 0;
	    for (int i = 1; i < v.length; i++)
		mask |= getToken(v[i]);
	    GL11.glClear(mask);
	} else if (checkfunc(v, "ClearDepth", 1)) {
	    GL11.glClearDepth(getTokenf(v[1]));
	} else if (checkfunc(v, "ClearColor", 4)) {
	    GL11.glClearColor(getTokenf(v[1]), getTokenf(v[2]),
		    getTokenf(v[3]), getTokenf(v[4]));
	} else if (checkfunc(v, "ActiveTextureARB", 1)
		|| checkfunc(v, "ActiveTexture", 1)) {
	    ARBMultitexture.glActiveTextureARB(getToken(v[1]));
	} else if (checkfunc(v, "Begin", 1)) {
	    GL11.glBegin(getToken(v[1]));
	    Begin = true;
	} else if (checkfunc(v, "End", 0)) {
	    GL11.glEnd();
	    Begin = false;
	} else if (checkfunc(v, "Vertex", 2, true)) {
	    switch (v.length) {
	    case 3:
		GL11.glVertex2d(getTokenf(v[1]), getTokenf(v[2]));
		break;
	    case 4:
		GL11.glVertex3d(getTokenf(v[1]), getTokenf(v[2]),
			getTokenf(v[3]));
		break;
	    default:
		System.out.println("Ignoring extra arguments to Vertex\n");
	    case 5:
		GL11.glVertex4d(getTokenf(v[1]), getTokenf(v[2]),
			getTokenf(v[3]), getTokenf(v[4]));
		break;
	    }
	} else if (checkfunc(v, "TexCoord", 1, true)) {
	    switch (v.length) {
	    case 2:
		GL11.glTexCoord1d(getTokenf(v[1]));
		break;
	    case 3:
		GL11.glTexCoord2d(getTokenf(v[1]), getTokenf(v[2]));
		break;
	    case 4:
		GL11.glTexCoord3d(getTokenf(v[1]), getTokenf(v[2]),
			getTokenf(v[3]));
		break;
	    default:
		System.out.println("Ignoring extra arguments to TexCoord\n");
	    case 5:
		GL11.glTexCoord4d(getTokenf(v[1]), getTokenf(v[2]),
			getTokenf(v[3]), getTokenf(v[4]));
		break;
	    }
	} else if (checkfunc(v, "MultiTexCoord", 2, true)) {
	    int tex = getToken(v[1]);
	    switch (v.length) {
	    case 3:
		ARBMultitexture.glMultiTexCoord1dARB(tex, getTokenf(v[2]));
		break;
	    case 4:
		ARBMultitexture.glMultiTexCoord2dARB(tex, getTokenf(v[2]),
			getTokenf(v[3]));
		break;
	    case 5:
		ARBMultitexture.glMultiTexCoord3dARB(tex, getTokenf(v[2]),
			getTokenf(v[3]), getTokenf(v[4]));
		break;
	    default:
		System.out
			.println("Ignoring extra arguments to MultiTexCoord\n");
	    case 6:
		ARBMultitexture.glMultiTexCoord4dARB(tex, getTokenf(v[2]),
			getTokenf(v[3]), getTokenf(v[4]), getTokenf(v[5]));
		break;
	    }
	} else if (checkfunc(v, "Color", 3, true)) {
	    switch (v.length) {
	    case 4:
		GL11.glColor3d(getTokenf(v[1]), getTokenf(v[2]),
			getTokenf(v[3]));
		break;
	    default:
		System.out.println("Ignoring extra arguments to Color\n");
	    case 5:
		GL11.glColor4d(getTokenf(v[1]), getTokenf(v[2]),
			getTokenf(v[3]), getTokenf(v[4]));
		break;
	    }
	} else if (checkfunc(v, "Normal", 3)) {
	    GL11.glNormal3d(getTokenf(v[1]), getTokenf(v[2]), getTokenf(v[3]));
	} else if (checkfunc(v, "LineWidth", 1)) {
	    GL11.glLineWidth(getTokenf(v[1]));
	} else if (checkfunc(v, "PointSize", 1)) {
	    GL11.glPointSize(getTokenf(v[1]));
	} else if (checkfunc(v, "CombinerParameterNV", 2, true)) {
	    int pname = getToken(v[1]);
	    if (pname == NVRegisterCombiners.GL_CONSTANT_COLOR0_NV
		    || pname == NVRegisterCombiners.GL_CONSTANT_COLOR1_NV) {
		// Need 4 params
		if (v.length != 6) {
		    System.out.println("Inv num params CombinerParameterNV\n");
		    return false;
		}
		FloatBuffer params = getfv(v, 2, 4);
		NVRegisterCombiners.glCombinerParameterNV(pname, params);

	    } else if (pname == NVRegisterCombiners.GL_NUM_GENERAL_COMBINERS_NV
		    || pname == NVRegisterCombiners.GL_COLOR_SUM_CLAMP_NV) {
		if (v.length != 3) {
		    System.out.println("Inv num params CombinerParameterNV\n");
		    return false;
		}
		NVRegisterCombiners.glCombinerParameterfNV(pname,
			getTokenf(v[2]));
	    } else {
		System.out.println("Invalid CombinerParameterNV first param: "
			+ pname + "\n");
		return false;
	    }
	} else if (checkfunc(v, "CombinerStageParameterNV", 6, true)) {
	    FloatBuffer params = getfv(v, 3, 4);
	    NVRegisterCombiners2.glCombinerStageParameterNV(getToken(v[1]),
		    getToken(v[2]), params);
	} else if (checkfunc(v, "CombinerInputNV", 6)) {
	    NVRegisterCombiners.glCombinerInputNV(getToken(v[1]),
		    getToken(v[2]), getToken(v[3]), getToken(v[4]),
		    getToken(v[5]), getToken(v[6]));
	} else if (checkfunc(v, "CombinerOutputNV", 10)) {

	    NVRegisterCombiners.glCombinerOutputNV(getToken(v[1]),
		    getToken(v[2]), getToken(v[3]), getToken(v[4]),
		    getToken(v[5]), getToken(v[6]), getToken(v[7]),
		    getTokenb(v[8]), getTokenb(v[9]), getTokenb(v[10]));

	} else if (checkfunc(v, "FinalCombinerInputNV", 4)) {
	    NVRegisterCombiners.glFinalCombinerInputNV(getToken(v[1]),
		    getToken(v[2]), getToken(v[3]), getToken(v[4]));
	} else if (checkfunc(v, "BindProgram", 2)
		|| checkfunc(v, "BindProgramARB", 2)) {
	    NVProgram.glBindProgramNV(getToken(v[1]), getToken(v[2]));
	} else if (checkfunc(v, "ProgramLocalParameter", 6)
		|| checkfunc(v, "ProgramLocalParameterARB", 6)) {

	    ARBProgram.glProgramLocalParameter4fARB(getToken(v[1]),
		    getToken(v[2]), getTokenf(v[3]), getTokenf(v[4]),
		    getTokenf(v[5]), getTokenf(v[6]));
	} else if (checkfunc(v, "ProgramEnvParameter", 6)
		|| checkfunc(v, "ProgramEnvParameterARB", 6)) {
	    ARBProgram.glProgramEnvParameter4fARB(getToken(v[1]),
		    getToken(v[2]), getTokenf(v[3]), getTokenf(v[4]),
		    getTokenf(v[5]), getTokenf(v[6]));
	} else if (checkfunc(v, "BindProgramNV", 2)) {
	    NVProgram.glBindProgramNV(getToken(v[1]), getToken(v[2]));
	} else if (checkfunc(v, "TrackMatrixNV", 4)) {
	    NVVertexProgram.glTrackMatrixNV(getToken(v[1]), getToken(v[2]),
		    getToken(v[3]), getToken(v[4]));
	} else if (checkfunc(v, "ProgramParameterNV", 6)) {
	    NVVertexProgram.glProgramParameter4fNV(getToken(v[1]),
		    getToken(v[2]), getTokenf(v[3]), getTokenf(v[4]),
		    getTokenf(v[5]), getTokenf(v[6]));
	} else {
	    System.out.println("Unknown function \"" + v[0] + "\" with "
		    + (v.length - 1) + " arguments\n");
	    return false;
	}
	return true;
    }

    private static DoubleBuffer getdv(String[] v, int i, int capa) {
	ByteBuffer ret = ByteBuffer.allocateDirect(capa*8);
	for (; i < v.length; i++) {
	    ret.putDouble(getTokend(v[i]));
	}
	ret.position(0);
	return ret.asDoubleBuffer();
    }

    private static FloatBuffer getfv(String[] v, int i) {
	return getfv(v, i, 0);
    }

    private static FloatBuffer getfv(String[] v, int i, int capa) {
	ByteBuffer ret = ByteBuffer.allocateDirect((/*2* */capa)*4);
	for (; i < v.length; i++) {
	    ret.putFloat(getTokenf(v[i]));
	}
	ret.position(0);
	return ret.asFloatBuffer();
    }

    private static IntBuffer getiv(String[] v, int i) {
	ByteBuffer ret = ByteBuffer.allocateDirect((v.length - i)*4);
	for (; i < v.length; i++) {
	    ret.putInt(getToken(v[i]));
	}
	ret.position(0);
	return ret.asIntBuffer();
    }

    private static ShortBuffer getiv(String[] v, int i, int capa) {
	ByteBuffer ret = ByteBuffer.allocateDirect(capa);
	for (; i < v.length; i++) {
	    ret.putShort( (short) getToken(v[i]));
	}
	ret.position(0);
	return ret.asShortBuffer();
    }

    private static boolean getTokenb(String string) {
	if (string.equals("FALSE"))
	    return false;
	return true;
    }

    private static boolean checkfunc(String[] v, String name, int numargs) {
	return checkfunc(v, name, numargs, false);
    }

    private static boolean checkfunc(String[] v, String name, int numargs, boolean extra_args) {
	if (!v[0].equals(name)) return false;
	
	if ((v.length - 1) < numargs ||
		(!extra_args && (v.length - 1) > numargs))
	{
	    System.out.println("illegal number of arguments to "+v[0]+": expected "+numargs+", got "+(v.length-1));
	    for (int i = 0; i < v.length; i++) {
		System.out.print(v[i]);
		System.out.print(" ");
	    }
	    System.out.println();
	    return false;
	}
	return true;
    }

    private static double getTokend(String d) {
	return Double.parseDouble(d);
    }

    private static float getTokenf(String f) {
	try {
	    return Float.parseFloat(f);
	} catch (NumberFormatException e) {
	    return getToken(f);
	}
    }

    public static int getToken(String token) {
	//System.out.println("token: "+token);
	Field f = null;
	Class[] clzz = {
	    ARBImaging.class,
		ARBMultitexture.class,
		ARBProgram.class,
		ARBVertexProgram.class,
		EXTSecondaryColor.class,
		GL11.class,
		GL12.class,
		GL13.class,
		GL14.class,
		GL15.class,
		GL20.class,
		NVProgram.class,
		NVRegisterCombiners.class,
		NVRegisterCombiners2.class,
		NVVertexProgram.class,
		EXTTextureFilterAnisotropic.class,
	};
	for (int i = 0; i < clzz.length; i++) {
	    try {
		f = clzz[i].getDeclaredField("GL_"+token);
	    } catch (Exception e) {
		// e.printStackTrace();
	    }
	    if (f != null) break;
	}
	if (f == null)
	    try {
		f  = GLU.class.getDeclaredField("GLU_"+token);
	    } catch (Exception e) {
		//e.printStackTrace();
	    }

	if (f == null)
	{
	    //System.out.println("Couldn't find token for "+token);
	    return Integer.parseInt(token);
	}

	/*
	if (f.getType() != Integer.class)
	{
	    System.out.println("type unknown! "+f.getType().getName());
	}
	*/
	try {
	    int t = f.getInt(f.getClass());
	    //System.out.println("val: "+t);
	    return t;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	throw new Error("assert error.");
    }

    public static void checkGlError(String context) {
	int errno = GL11.glGetError();
	if (errno != GL11.GL_NO_ERROR)
	    throw new Error(GLU.gluErrorString(errno) + " " + context);
    }

    
    
    /*-
     * Implement Vob here
     * ==================
     */
    
    
    public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
	throw new Error();
    }

    public int putGL(VobScene vs)
    {
	return callList;
    }
    public int putGL(VobScene vs, int cs)
    {
	return callList;
    }

    public void render(int callList) {
	// push attrib?
	checkGlError("before call list in callgl render");
	GL11.glCallList(callList);
	checkGlError("after call list in callgl render");
	// pop attrib?
    }
    
    public void call() {
	checkGlError("before call list in callgl call");
	GL11.glCallList(callList);
	checkGlError("after call list in callgl call");
    }
}
