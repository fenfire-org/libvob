=============================================================
PEG glmosaictext_java--tjl: Move glmosaictext to java
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/09/28 13:02:16 $
:Revision: $Revision: 1.2 $
:Status:   Accepted [the APIs have evolved slightly but the basic architecture remains]

GLMosaicText is the only external graphics library we use that
encapsulates its own textures. This makes hunting the texture
bug difficult, as well as testing novel techniques 
for e.g. filtering font mipmaps and using stranger texture
encodings.

I propose abandoning glmosaictext and moving the codebase
to Java/C++, through

1) a lightweight JNI interface to the routines of FreeType 
   we need

2) a multi-texture quad renderable using strings and a lookup 
   table (quite like what's in there
   now, but supporting e.g. multiple textures for the font
   properly)

3) Jython / java code to create the font textures and quad
   lists. Specifically, all the mosaic managing code
   would be here.

This *might* impact the startup time *slightly* but after that,
there should be no noticeable performance difference.

Issues
======

- This is kind of a problem - GLMosaicText is something others
  could have used directly while this makes GLMosaicText
  dependent on Libvob. Is this a problem?

    RESOLVED: No. The design pressures from Libvob side
    did already affect GLMosaicText, moving it away from
    the simplest possible solutions. The code will survive,
    just not be maintained.

    Also, to our knowledge, no-one else was using it.

- Should QuadFont contain setupCode and teardownCode, 
  like Paper?

    RESOLVED: No. The setup and teardown need to be done
    on a higher level so that we can batch the
    vobs together without state changes. This
    has been shown to be one of the most
    important performance drags on FenPDF (then PP) rendering.

Changes
=======

The main gist is to separate the monolithic font implementation
so that both the bitmap generation and texture font definition
functions are available in Java.


Remove GL.Font
--------------

GL.Font class and implementations shall be removed. All dependencies
on glmosaictext shall be removed.

Add GLFont as a replacement for GL.Font
---------------------------------------

A GLFont is essentially a single QuadFont plus some
measurements information that used to be found
in GL.Font. It's not a subclass since we couldn't
create instances easily but rather a wrapper.

The constructors for text renderables should be overloaded
to take a GLFont and the QuadFont from inside it. ::

    /** A wrapper for QuadFont that knows about 
     * measurements.
     * Later on, this may change to an interface for using
     * different font types, but not yet.
     */
    public class GLFont {

	private float height;
	private float yoffs;
	private float[] widths;

	private QuadFont quadFont;

	public GLFont(float height, float yoffs, float[] widths, QuadFont quadFont) {
	    this.height = height;
	    this.yoffs = yoffs;
	    this.widths = widths;
	    this.quadFont = quadFont;
	}

	/** Get the height of a single line.
	 */
	public float getHeight() { return height; }
	/** Get the offset (downwards from the top
	 * of the line) to the baseline.
	 */
	public float getYOffs() { return yoffs; }
	/** Get the widths (advances) of the characters
	 * in the font.
	 * The returned array MUST NOT BE MODIFIED.
	 */
	public float[] getWidths() { return widths; }

	public QuadFont getQuadFont() { return quadFont; }

    }

FTFont
------

FTFont object added to GL, representing a single FTFont (as
in FTFont object in glmosaictext).

Methods ("native" means native as used in GL, i.e. delegated 
to static method with the int id)::

    /** Create a freetype font, with the given
     * pixel sizes.
     */
    public native static FTFont createFTFont(String filename, 
		    int pixsize_x, int pixsize_y);

    /** A freetype font.
     * Not directly renderable - see GLFont for that.
     */
    public class FTFont {
	/** A low-level character measurements routine.
	 * Intended to be used by a higher-level wrapper.
	 * @return an int array, containing 6*characters.length
	 * 		elements, in groups of
	 *              (x,y,w,h, xadvance, yadvance).
	 *              The coordinates are in pixels, and
	 *              the advances are in FT fixed point units,
	 *              scaled by 2**6.
	 */
	public "native" int[] getMeasurements(int[] characters);

	/** Get bitmaps corresponding to a number of characters.
	 */
	public "native" byte[][] getBitmaps(int[] characters);
	
    }

QuadFont
--------

Additionally, the mutable QuadFont object for rendering
texture quads based on characters (with *dense* storage), in C++::

    struct QuadFont {

	// Length invariants:
	//    length(textureUnits) == textureLayers
	//    length(coordTextureUnits) == textureCoords
	//    length(textures) == NPAGES * textureLayers
	//    length(textureIndex) == NGLYPHS
	//    length(coordinates) == 8*NGLYPHS
	//    length(advances) == 1*NGLYPHS
	//    min(x in textureIndex) = 0
	//    min(x in textureIndex) < NPAGES

	/** The number of textures to be placed
	 * in the texture units.
	 */
	int textureLayers;

	/** The number of texture coordinates used.
	 */
	int textureCoords;

	/** The texture unit tokens into which
	 * the textures are to be placed.
	 */
	vector<GLenum> textureUnits;

	/** The texture unit tokens for which
	 * texture coordinates need to be set.
	 * For vertex and fragment programs, the 
	 * this may be different from the above.
	 */
	vector<GLenum> coordTextureUnits;

	/** The texture targets the textures should be loaded.
	 */
	vector<GLenum> textureTargets;

	/** Bind the textures corresponding
	 * to the given index.
	 */
	void bindTextures(int texIndex) ;

	/** Unbind all textures bound by this class.
	 */
	void unbindTextures() {

	/** The actual texture ids.
	 * An interleaved vector, with textureLayers
	 * textures on the first level.
	 */
	vector<GLuint> textures;

	/** The texture indices.
	 * Used to index the textures array, with multiplier textureLayers.
	 */
	vector<int> textureIndex;

	/** The quad coordinates, corners and texture coordinates.
	 * These give the physical coordinates to use for the
	 * corners of the quad (A is larger than a) 
	 * as well as the texture coordinates.
	 * These are stored in a single array so we can, in the future,
	 * bind and download this to the GPU and just index it,
	 * along with a vector of offsets (the cumulative sum of
	 * the advances).
	 * Stored as groups of 8: x0, y0, x1, y1, tx0, ty0, tx1, ty1.
	 */
	vector<float> coordinates;

	/** The amount to advance the x coordinate after rendering
	 * a glyph. 
	 * Only horizontal text supported here so far.
	 */
	vector<float> advances;

	CallGLCode setupCode;
	CallGLCode teardownCode;

    }

and in Java (operations mapping to the operations of the preceding class)::

    public class QuadFont {
	// XXX When needed, add the get() methods.

	/** Store explicitly the GL.Texture objects
	 * to avoid GC.
	 */
	private GL.Texture[] textures;

	/** Set up the textures to use.
	 * @param texUnits The names of the texunits to bind textures to.
	 * @param targets The names of the texture targets to bind textures to.
	 *                targets.length == texUnits.length
	 * @param texCoordUnits The names of the texunits to set coordinates for.
	 * @param textures The textures. 
	 *                 textures.length = texUnits.length * number of font pages.
	 */
	public "native" void setTextures(
	    String[] texUnits,
	    String[] targets,
	    String[] texCoordUnits,
	    GL.Texture []textures);

	/** Set the measurements of a single glyph.
	 * @param glyph The index of the glyph.
	 * @param texInds The indices of the textures to be bound
	 *                for this glyph. Lenght == layers given to
	 *                setTextures.
	 */
	public "native" void setMeasurements(int glyph, 
				int[] texInds,
				float x0, float y0, float x1, float y1,
				float tx0, float ty0, float tx1, float ty1,
				float xadvance, float yadvance);
    }

The Text1Base Vob and its uses is to be replaced by QuadFontTextVob, a vob
using a QuadFont.


