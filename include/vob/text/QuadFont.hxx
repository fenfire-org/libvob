/*
QuadFont.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#include "callgl_objects.hxx"

namespace Vob {
    namespace Text {
	PREDBGVAR(dbg);

	using CallGL::CallGLCode;

	/** A font described as quads from textures.
	 */
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

	    /** The actual texture ids.
	     * An interleaved vector, with textureLayers
	     * textures on the first level.
	     */
	    vector<GLuint> textures;

	    QuadFont() :
		textureLayers(0),
		textureCoords(0) { }

	    /** Set the number of texture layers.
	     * Discards all glyphs since changing this
	     * is kind of big.
	     */
	    void setNTextureLayers(int n, int npages) {
		textureLayers = n;
		textureUnits.resize(n, 0);
		textureTargets.resize(n, 0);
		textures.resize(n * npages, 0);

		textureIndex.resize(0,-1);
		coordinates.resize(0,-1);
		advances.resize(0,-1);
	    }

	    void setNTextureCoords(int n) {
		textureCoords = n;
		coordTextureUnits.resize(n, 0);
	    }

	    /** Bind the textures corresponding
	     * to the given index.
	     */
	    void bindTextures(int texIndex) {
		for(int i=0; i<textureLayers; i++) {
		    glActiveTextureARB(textureUnits[i]);
		    GLERR;
		    glBindTexture(textureTargets[i], 
				  textures[i + texIndex * textureLayers]);
		    GLERR;

		}
		glActiveTextureARB(GL_TEXTURE0_ARB);
	    }
	    void unbindTextures() {
		for(int i=0; i<textureLayers; i++) {
		    glActiveTextureARB(textureUnits[i]);
		    glBindTexture(textureTargets[i], 0);
		}
		glActiveTextureARB(GL_TEXTURE0_ARB);
	    }

	    void texCoords(float s, float t) {
		for(int i=0; i<textureCoords; i++) {
		    glMultiTexCoord2f(coordTextureUnits[i], s, t);
		}
	    }

	    /** The number of glyphs.
	     */
	    int nGlyphs;

	    /** The texture indices.
	     * Used to index the textures array, with multiplier textureLayers.
	     * -1 for "no such glyph".
	     */
	    vector<int> textureIndex;


	    /** The quad coordinates.
	     * These are stored in a single array so we can, in the future,
	     * bind and download this to the GPU and just index it,
	     * along with a vector of offsets (the cumulative sum of
	     * the advances).
	     * Stored as groups of 8: x0, y0, x1, y1, tx0, ty0, tx1, ty1.
	     */
	    vector<float> coordinates;

	    /** The advances. Only horizontal text supported here so far.
	     */
	    vector<float> advances;

	    
	    void setNGlyphs(int n) {
		nGlyphs = n;
		textureIndex.resize(n, -1);
		coordinates.resize(8*n);
		advances.resize(n);
	    }


	};
    }

}
