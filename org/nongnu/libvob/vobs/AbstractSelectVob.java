/*
AbstractSelectVob.java
 *    
 *    Copyright (c) 2003, Matti J. Katila
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
 * Written by Matti J. Katila
 */

package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.util.*;
import java.awt.*;


/** An abstract Vob which is selectable, i.e., when mouse 
 * is pressed within a one the vob is visualized differently.
 * There are three different visualizations a.k.a select modes.
 * Switching between these modes is done by setting parameters of
 * the second coordinate system. 
 * Please notice that constructing of this second
 * coordinate system should be done trough the static methods
 * <code>getControl</code> and 
 * <code>setControl</code> provided by this class.
 */
public abstract class AbstractSelectVob extends AbstractVob {
    public static boolean dbg = false;
    static private void p(String s) { System.out.println("AbstractSelectVob:: "+s); }

    /** The placeable mask which is placed above the select mode Vob.
     * For example, if the mask is text then the text is drawn 
     * over the select mode Vob (see. normal, pre or post attributes).
     */
    protected final org.nongnu.libvob.lava.placeable.Placeable mask;

    /** Vob visualizing the mode of select.
     */
    protected final Vob normal, pre, post;

    /** A renderable vob which is used in OpenGL side to 
     * represent the current select mode inside one vob scene.
     */
    protected Vob select = null;

    /** @param mask A Placeable object which is placed over select mode vob.
     */
    public AbstractSelectVob(org.nongnu.libvob.lava.placeable.Placeable mask) {
	this(mask, Color.white, Color.cyan, Color.gray);
    }

    /** @param mask A Placeable object which is placed over select mode vob.
     * @param normal The color of normal select-mode.
     * @param pre The color of pre select-mode.
     * @param post The color of post select-mode.
     */
    public AbstractSelectVob(org.nongnu.libvob.lava.placeable.Placeable mask,
			     Color normal, Color pre, Color post) {
	this(mask, selectVob(normal), selectVob(pre), selectVob(post));
    }

    static private Vob selectVob(Color color) {
	return GLCache.getCallListCoorded(
	    "PushAttrib ENABLE_BIT CURRENT_BIT TEXTURE_BIT\n"+
	    "Disable TEXTURE_2D \n"+
	    "Color "+ ColorUtil.colorGLString(color) +"\n"+
	    "Begin QUADS \n"+
	        "Vertex 0 0 \n"+
	        "Vertex 0 1 \n"+
	        "Vertex 1 1 \n"+
	        "Vertex 1 0 \n"+
	    "End\n"+
	    "PopAttrib");
    }


    public AbstractSelectVob(
	 org.nongnu.libvob.lava.placeable.Placeable mask,
	 Vob normalVob, Vob preSelectVob, Vob postActivatedVob)
    {
	this.mask = mask;
	this.normal = normalVob;
	this.pre = preSelectVob;
	this.post = postActivatedVob;
    }
    
    static private Rectangle rect = new Rectangle();

    /** Renders by the current select mode. 
     * Select modes are chosen by the 2. cs's box's width:
     *    <=1 normal, 
     *    <= 2 pre selection and 
     *    other is post selection.
     */
    public void render(Graphics g, boolean fast,
		       Vob.RenderInfo info1, Vob.RenderInfo info2) {
	if (info2.width <= 1)
	    normal.render(g, fast, info1, null);
	else if (info2.width <= 2)
	    pre.render(g, fast, info1, null);
	else post.render(g, fast, info1, null);
	
	//mask.place(vs, maskCS);
	throw new Error("Not implemented");
    }

    
    public int putGL(VobScene vs, int selectBoxCS, int controlCS) {
	return putGL(vs, selectBoxCS, selectBoxCS, controlCS);
    }
    public int putGL(VobScene vs, int selectBoxCS, int maskCS, int controlCS) {
	if (select == null)
	    select = GLRen.createSelectVob((GL.Renderable1JavaObject) normal,
					   (GL.Renderable1JavaObject) pre,
					   (GL.Renderable1JavaObject) post);
	vs.map.put(select, selectBoxCS, controlCS);
	mask.place(vs, maskCS);
	return 0;
    }   
 
    private static final Object baseKey = "SelectVobControlLine"; 
    private static int baseControlCS(VobScene vs, Object key) {
	if (vs.matcher.getCS(0, key) < 2)
	    vs.translateCS(0, key,0,0);
	return vs.matcher.getCS(0, key);
    }
    private static int realControlCS(VobScene vs, int control, 
				     Object key) {
	if (vs.matcher.getCS(control, key) < 2)
	    // default normal
	    vs.orthoBoxCS(control, key,0, 0,0, 1,1, 1,1); 
	return vs.matcher.getCS(control, key);
    }
    
    
    /** Help class to support typesafe enumeration of control state.
     */
    static public class ControlState { private ControlState() {; } }
    
    /** Enumeration of control state.
     */
    static public final ControlState 
	normalState = new ControlState(),
	preState = new ControlState(),
	postState = new ControlState();
    

    /** Get the coordinate system of mode selection control.
     * @param vs The current VobScene
     * @param controlKey The key for this control coordinate 
     *                   system. The key must be unique.
     */
    public static int getControl(VobScene vs, 
				 Object controlKey) 
    {
	int control = baseControlCS(vs, baseKey);
	return realControlCS(vs, control, controlKey);
    }


    /** Set the coordinate system for controling the selection mode.
     * @param vs The current VobScene
     * @param controlKey The key for this control coordinate 
     *                   system. The key must be unique.
     * @param state The state of three possible selection 
     *              modes . 
     */
    public static int setControl(VobScene vs, 
	      Object controlKey, ControlState state) { 
	int control = baseControlCS(vs, baseKey);
	int cs = realControlCS(vs, control, controlKey);
	if (dbg) p("cs: "+control+" real: "+cs);
	float width = -1;
	if (state == normalState) {
	    if (dbg) p("normal");
	    width = 1;
	} else if (state == preState) {
	    if (dbg) p("pre");
	    width = 2;
	} else { 
	    if (dbg) p("post");
	    width = 3;
	}
	vs.coords.setOrthoBoxParams(cs, 0,0,0, 1,1, width, width );
	return cs;
    } 
    
}
