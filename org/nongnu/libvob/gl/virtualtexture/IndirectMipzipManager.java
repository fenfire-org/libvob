/*
IndirectMipzipManager.java
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

package org.nongnu.libvob.gl.virtualtexture;
import org.nongnu.libvob.util.Background;

/** An indirect virtual texture manager: load and unload mipzip files
 * to given levels.
 * Objects implementing 
 * this interface manages a set of texture images that have the same 
 * format, and the same level-0 width and height.
 * <p>
 * The basic model is that there is a fixed number of *slots*
 * of each mipmap-level size, and a single
 * VirtualTexture object may be
 * assigned to a single slot at a time. 
 * The slots may be implemented by simply loading and removing mipmap
 * levels from the underlying texture, but this interface exists
 * to allow a more static implementation that stresses
 * the underlying OpenGL implementation less (exposing less bugs),
 * see NonDeletingIndirectMipzipManager.
 * <p>
 * This interface is a little clumsy, containing initialization
 * as well as use; the alternative would be to have a factory and
 * that would not be fun.
 * <p>
 * The reason for this interface is that we've had *lots* of trouble
 * with the OpenGL drivers about this sort of stuff -- they've
 * obviously not been written with this in mind. The interface
 * is here so that different implementations that may work better
 * or worse on specific drivers may be tried.
 */
public interface IndirectMipzipManager {

    /** Initialize this object.
     * @param format The GL texture format string, 
     * @param width The width of mipmap level 0.
     * @param height The height of mipmap level 0.
     * e.g. COMPRESSED_RGB_S3TC_DXT1_EXT.
     */
    void init(String format, int width, int height) ;

    /** Set the default texture parameters.
     * For now, should be called exactly once between 
     * init and setAllocations.
     * <p>
     * Example use: 
     * <pre>
     * 		setTexParameters(new String[] {
     * 			"TEXTURE_MAG_FILTER", "LINEAR",
     * 			"TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
     * 			"TEXTURE_MAX_ANISOTROPY_EXT", "10",
     * 		});
     * </pre>
     * <p>
     * If you set any of the texture parameters for any virtual textures
     * given to this class (which is actually not yet possible but will
     * be in the future), remember to give the default value here,
     * as otherwise the value cannot be reset when the texture is moved.
     * <p>
     * NOTE: Don't set TEXTURE_BASE_LEVEL or any of the LOD (except
     * LOD bias) parameters here, as the mipmap levels may be shifted
     * inside the class. Setting GENERATE_MIPMAP_SGIS would be just plain 
     * daft.
     * @param params The texture parameters, as pair of parameter-value.
     */
    void setDefaultTexParameters(String[] params);

    /** Set the number of slots of each mipmap level 
     * to allocate.
     * XXX May currently interact nastily with
     * reservations - run this only just after
     * construction. This restriction will be lifted
     * later.
     * @param ntextures Indexed by mipmap level,
     * 			the number of textures of that
     * 			size to allocate.
     */
    void setAllocations(int[] ntextures) ;

    /** Set the background objects that this object should use.
     * @param background The background object to use for
     * 			non-OpenGL tasks.
     * @param bgPriority The priority to give to those tasks
     * @param glBackground The background object to use for OpenGL tasks
     * @param glPriority The priority to give to those tasks
     */
    void setBackgrounds(Background background, float bgPriority,
			    Background glBackground, float glPriority) ;

    /** Set what the slots should contain.
     * The change is not immediate but happens in the background,
     * starting from the lowest levels (highest resolutions)
     * which are considered a priority.
     */
    void setSlotContents(VirtualTexture[][] newContents) ;

    /** Get the currently assigned level of a slot.
     * This is easily obtainable from the most recent parameter
     * of the setSlotContents() method, but to avoid duplication
     * of data structures, access is provided here.
     * @return The slot level, or -1 if none.
     */
    int getSlotLevel(VirtualTexture virtualTexture);

    /** Set what the slots should contain and load the contents
     * right now.
     * Executing this method may take some time, but is vital
     * in, e.g., benchmarks.
     */
    void setSlotContents_synchronously(VirtualTexture[][] newContents) 
	    throws java.io.IOException;

    /** Free all the textures, stop all background threads.
     * This is because with the threads, we can't
     * easily let the GC do its stuff.
     * No methods should be called after this method.
     */
    void decommission();
}
