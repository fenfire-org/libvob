// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl;

import java.awt.Graphics;

import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.TextStyle;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.gl.PaperMill.Paper;
import org.nongnu.libvob.gl.Ren.PaperQuad;
import org.nongnu.libvob.gl.Ren.Text1;
import org.nongnu.libvob.gl.impl.lwjgl.mosaictext.LWJGL_Text;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.CallListBoxCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.CallListCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.NonFilledRectangleVob;

/** A class that implements objects that can render OpenGL with lwjgl. 
 * 
 * @author Matti J. Katila
 */
public class LWJGLRen {

    static public interface Vob0 {
	public void render(int callList);
    }
    static public interface Vob1 {
	public void render(Transform cs0, int callList);
    }
    static public interface Vob2 {
	public void render(Transform cs0, Transform cs1, int callList);
    }
    static public interface Vob3 {
	public void render(Transform cs0, Transform cs1, Transform cs2, int callList);
    }
    static public interface VobN  {
	public void render(Transform[] ncs, int callList);
    }
    
    
    
    
    static AbstractVob stubVob1 = new AbstractVob(){
	public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
	}
	public int putGL(VobScene vs, int cs1) {
	    return 0;
	}
    };
    
    static class GLVob extends AbstractVob {
	public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
	}
    }
    

    public static Vob createNonFilledRectangle(float lineWidth, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
	return new NonFilledRectangleVob(lineWidth, r1,g1,b1,a1,r2,g2,b2,a2);
    }

    // putGL(vs);
    public static Vob createCallList(String s) {
	return new CallGL(s);
    }

    public static Vob createCallListCoorder(String s) {
	return new CallListCoorder(s);
    }

    public static Vob createCallListBoxCoorder(String s) {
	return new CallListBoxCoorder(s);
    }

    public static Vob createText(TextStyle style, String text) {
	return ((LWJGL_Text.TextStyleImpl)style).createVob(text);
    }

    
    // ---- NON-STATIC METHODS --------------------------
    
    
    public PaperQuad createPaperQuad(Paper paper, float x0, float y0, float x1, float y1, float dicefactor) {
	return new PaperQuadImpl(paper, x0,y0,x1,y1, dicefactor);
    }

    public Text1 createText1(TextStyle p0, String p1, float p2, int p3) {
	return null;
    }


    
    class PaperQuadImpl extends GLVob implements PaperQuad, Vob3 {

	public PaperQuadImpl(Paper paper, float x0, float y0, float x1, float y1, float dicefactor) {
	}

	public void render(Transform cs0, Transform cs1, Transform cs2, int callList) {
	    // TODO Auto-generated method stub
	    
	}

	
	/**
	# PaperQuad is a bit complicated: there are three coordinate
	# systems here: the window cs, the object cs and the paper cs.
	# cs1 is object => window,
	# and cs2 is paper => object, unless PAPERQUAD_CS2_TO_SCREEN is set, when it is
	#				paper => window
	# Corners give the corners of the quad to render, in object
	# coordinates.
	*/
/*
	class PaperQuad {
	public:
	    enum { NTrans = 2 };

	    ::Vob::Paper::Paper *paper;
	    float x0,y0,x1,y1;
	    float scale;
	    float dicefactor;
	    int flags;

	    template<class F> void params(F &f) {
		f(paper, x0, y0, x1, y1, scale, dicefactor, flags);
	    }

	    template<class T> void render(const T &coords1, const T &coords2) const {
		    const int flags = this->flags;
		    // object -> paper/window
		    const Transform &coords2inv = coords2.getInverse();

		    GLERR;


		    ZPt paperorigin, paperx, papery;
		    if(flags & PAPERQUAD_CS2_TO_SCREEN) {
			const Transform &coords1inv = coords1.getInverse();
			paperorigin = coords1inv.transform(coords2.transform(ZPt(0, 0, 0)));
			paperx = coords1inv.transform(coords2.transform(ZPt(1, 0, 0))) 
					- paperorigin;
			papery = coords1inv.transform(coords2.transform(ZPt(0, 1, 0))) 
					- paperorigin;
		    } else {
			paperorigin = coords2.transform(ZPt(0, 0, 0));
			paperx = coords2.transform(ZPt(1, 0, 0)) - paperorigin;
			papery = coords2.transform(ZPt(0, 1, 0)) - paperorigin;
		    }

		
		    Paper::LightParam lightParam;

		    // These are now irrelevant
		    lightParam.orig = paperorigin-ZPt(0,0,0);
		    lightParam.e0 = paperx * scale;
		    lightParam.e1 = papery * scale;
		    lightParam.e2 = ZVec(0,0,paperx.length()) * scale;

	            lightParam.Light = ZVec(-1,-1,1);
	            lightParam.Light_w = 0.0;

		    DBG(dbg_paperquad) << "Paperquad: " <<
		            lightParam.orig << " " <<
			    lightParam.e0 << " " <<
			    lightParam.e1 << " " <<
			    lightParam.e2 << " " <<
			    "\\nCorners" <<
			    x0 << " " <<
			    y0 << " " <<
			    x1 << " " <<
			    y1 << " " <<
			    "\\n"
			    ;
		    GLERR;


		    int dice;

		    if(flags & PAPERQUAD_NONL_MAXLEN) {
			Pt p1 = coords1.transform(Pt(x0,y0));
			Pt p2 = coords1.transform(Pt(x0,y1));
			Pt p3 = coords1.transform(Pt(x1,y0));
			Pt p4 = coords1.transform(Pt(x1,y1));
			float dist[4] = {
			    (p2-p1).length(),
			    (p3-p1).length(),
			    (p4-p2).length(),
			    (p4-p3).length()
			};
			float m = *std::max_element(dist, dist+4);

			dice = (int)(m / dicefactor) + 2;
		    } else { // old way

			ZPt ctr = ZPt(lerp(x0, x1, 0.5), lerp(y0, y1, 0.5), 0);
			double len = hypot(x1-x0, y1-y0) / 2;
			double nonl = coords1.nonlinearity(ctr, len);
			
			dice = (int)(len * nonl * dicefactor) + 2;
		    }
		    DBG(dbg_paperquad) << "Dice: " << dice <<"\\n";
		    // Cap it at a ridiculous value
		    if( dice > 100) dice = 100;
		    if(dice < 2 ) dice = 2;

		    float *vertices = new float[dice * dice * 5];

	            int *indices = new int[(dice) * (2*dice)];

	            #define VERTICES3(x, y, z) vertices[((x)*dice + (y))*5 + (z)]
	            #define VERTICES2(x, y)    vertices[((x)*dice + (y))*5]
	            #define INDICES2(x, y)     indices[(x)*2*dice + (y)]
	            #define INDICES1(x)        indices[(x)*2*dice]

		    int *indps[dice-1];
		    int counts[dice-1];
		    for(int ix = 0; ix<dice; ix++) {
			if(ix < dice-1) {
			    counts[ix] = 2*dice;
			    indps[ix] = &INDICES1(ix);
			}
			for(int iy = 0; iy<dice; iy++) {
			    if(ix < dice-1) {
				INDICES2(ix, 2*iy) = dice * ix + iy;
				INDICES2(ix, 2*iy+1) = dice * (ix+1) + iy;
			    }
			    float x = ix / (dice - 1.0);
			    float y = iy / (dice - 1.0);
			    ZPt p(lerp(x0, x1, x), lerp(y0, y1, y), 0);
			    ZPt v = coords1.transform(p);
			    VERTICES3(ix, iy, 2) = v.x;
			    VERTICES3(ix, iy, 3) = v.y;
			    VERTICES3(ix, iy, 4) = v.z;
			    ZPt t;
			    if(flags & PAPERQUAD_CS2_TO_SCREEN) {
				t = coords2inv.transform(v);
			    } else {
				t = coords2inv.transform(p);
			    }
			    VERTICES3(ix, iy, 0) = t.x;
			    VERTICES3(ix, iy, 1) = t.y;
			    DBG(dbg_paperquad) << "   vert: " << 
				    ix << " " <<
				    iy << " : " <<
				    VERTICES3(ix, iy, 0) << " " <<
				    VERTICES3(ix, iy, 1) << " " <<
				    VERTICES3(ix, iy, 2) << " " <<
				    VERTICES3(ix, iy, 3) << " " <<
				    VERTICES3(ix, iy, 4) << " " <<
				    "\\n";
			}
		    }

		    if(flags & PAPERQUAD_USE_VERTEX_PROGRAM) {
			glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
			glInterleavedArrays(GL_T2F_V3F, 5*sizeof(float), vertices);
			glLockArraysEXT(0, dice*dice);

			for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {

	                    DBG(dbg_paperquad) << "Pass\\n";
	                    GLERR;
	                    (*it).setUp_VP(&lightParam);
	                    
	                    DBG(dbg_paperquad) << "Going to multidraw\\n";
	                    GLERR;
	                    glMultiDrawElementsEXT(GL_QUAD_STRIP, counts,
	                       GL_UNSIGNED_INT, (const GLvoid **)indps, dice-1);
	                    DBG(dbg_paperquad) << "Teardown\\n";
	                    GLERR;
	                    (*it).tearDown_VP();
	                
	                    GLERR;
	                    DBG(dbg_paperquad) << "Pass over\\n";

			}
			glUnlockArraysEXT();
			glPopClientAttrib();
		    } else {
			for(Paper::Paper::iterator it = paper->begin(); it != paper->end(); ++it) {

	                    DBG(dbg_paperquad) << "Pass\\n";
	                    GLERR;
	                    (*it).setUp_explicit(&lightParam);
	                    
	                    DBG(dbg_paperquad) << "Going to set texcoords explicit\\n";
	                    GLERR;


	                    for(int ix = 0; ix<dice-1; ix++) {
	                        glBegin(GL_QUAD_STRIP);
	                        for(int iy = 0; iy<dice; iy++) {

	                             float tmp[4] = { VERTICES3(ix, iy, 0), VERTICES3(ix, iy, 1), 0 ,1 };
				    DBG(dbg_paperquad) << "to texcoords\\n";
	                             (*it).texcoords_explicit( tmp );
				    DBG(dbg_paperquad) << "to vertex\\n";
	                             glVertex3fv( (&(VERTICES2(ix, iy))+2) );
	                                
	                             float tmp2[4] = { VERTICES3(ix+1, iy, 0), VERTICES3(ix+1, iy, 1), 0 ,1 };
				    DBG(dbg_paperquad) << "to texcoords\\n";
	                             (*it).texcoords_explicit( tmp2 );
				    DBG(dbg_paperquad) << "to vertex\\n";
	                             glVertex3fv( ((&VERTICES2(ix+1, iy))+2) );
	                         }
				DBG(dbg_paperquad) << "to end\\n";
	                         glEnd();
	                    }


	                    DBG(dbg_paperquad) << "Teardown\\n";
	                    GLERR;
	                    (*it).tearDown_explicit();
	                
	                    GLERR;
	                    DBG(dbg_paperquad) << "Pass over\\n";
	                }
		    }

		    DBG(dbg_paperquad) << "Passes over\\n";

		    GLERR;

	            delete [] vertices;
	            delete [] indices;
	            
	    }
*/
    }

    
    
}
