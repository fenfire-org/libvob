
====================================================
PEG vob_event_action--mudyc: A Vob, Event and Action 
====================================================

:Authors:  Matti J. Katila
:Date-Created: 2003-09-08
:Last-Modified: $Date: 2003/09/26 14:44:20 $
:Revision: $Revision: 1.5 $
:Status:   Current
:Stakeholders: mudyc, tjl
:Scope:    Minor
:Type:     Architecture


Our framework still lacks of good way to create button and
menu like objects. These objects are different than
most other objects, since usually Vob doesn't know anything
about events and events don't know anything about Vobs. 
Now it's time to take care that vobs and events
meet eachother in a way that selectable objects
can be created.


Issues
======

..

Changes
=======

Create a new abstract Vob which can react to following events:

    1) select down(pre selection, event pressed but no released), 
    2) select up(no events, mostly this is the normal mode before events) and 
    3) selection selected(post selection, event pressed and released).

These three selections, `selection modes`, are represent with 
three Vobs. Because of efficiency (reusing vobscene), 
the select Vob is immutable and all three Vobs must be given 
in constructor. The current `selection mode` is shown by one of the Vobs
at the bottom and then placeable object is placed over it, i.e. text.

Switching between `selection modes` is done with second 
coordinate system since it's the only way to do it with immutable object.

I propose the following abstraction for select Vob: ::


    /** An abstract Vob which is selectable, i.e., when mouse 
     * is pressed within a one the vob is visualized differently.
     * There are three different visualizations a.k.a select modes.
     * Switching between these modes is done by setting parameters of 
     * the second coordinate system. 
     * Please notice that constructing and modifing of this second
     * coordinate system should be done trough the static methods
     * <code>getControl</code> and 
     * <code>setControl</code> provided by this class.
     */
    public abstract class AbstractSelectVob extends AbstractVob {

        /** The placeable mask which is placed on top of the select mode Vob.
         * For example, if the mask is text then the text is drawn 
         * over the select mode Vob (see. normal, pre or post attributes).
         */
        protected final org.nongnu.libvob.lava.placeable.Placeable mask;

	/** The vob visualizing of three possible select modes.
         * In OpenGL the vobs must be GL.Renderable1JavaObject.
         */
        protected final Vob normal, pre, post;

        /** A renderable vob which is used in OpenGL side to 
         * represent the current select mode inside one vob scene.
         */
        protected Vob select = null;

    	public AbstractSelectVob(
	    org.nongnu.libvob.lava.placeable.Placeable mask,
	    Vob normalVob, Vob preSelectVob, Vob postActivatedVob)
        {
	    this.mask = mask;
    	    this.normal = normalVob;
            this.pre = preSelectVob;
            this.post = postActivatedVob;
        }


        private static final Object baseKey = "SelectVobControlLine"; 

	/** Creates a base cs tree for select vobs' coordinate system.
	 */
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
    

        /** Get the coordinate system of mode selection control.
         * @param vs The current VobScene
         * @param controlKey A key for this control coordinate 
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

     
        /** Help class to support typesafe enumeration of control state.
         */
        static public class ControlState { private ControlState() {; } }
    
        /** Enumeration of control state.
         */
        static public final ControlState 
	    normalState = new ControlState(),
	    preState = new ControlState(),
	    postState = new ControlState();

    }


We also need OpenGL renderable which can multiplex between three vobs.
The change is trivial and belongs to libvob/include/vob/vobs/Trivial.hxx

