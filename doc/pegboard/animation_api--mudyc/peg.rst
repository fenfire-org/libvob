
==========================================================================
PEG animation_api--mudyc: Animation Layer API 
==========================================================================

:Authors:  Matti J. Katila
:Date-Created: 2003-09-27
:Last-Modified: $Date: 2003/09/27 11:20:51 $
:Revision: $Revision: 1.1 $
:Status:   Incomplete
:Stakeholders: mudyc, tjl, benja, humppake
:Scope:    Minor
:Type:     Policy, Interface


We have very powerful system for drawing in screen and making animations
with it. In larger programs, where it's quite easy to write bugs, it's
more than often when you missuse the current API. To make it easier to
debug the animation and events, I propose  we make a new layer for 
calling the animation routines.

Instead of calling directly the: ::

    AbstractUpdateManager.chg()

we should call a new api which can be switch to debug the caller, like: ::

    AbstractUpdateManager.chg(String caller)


Issues
======

How come we make sure that none other object call the public method 
AbstractUpdateManager.chg()?

   RESOLVED: Use the source, Luke. Yes, we have source and we can 
   use the command `grep` to search trough source tree. After all, 
   this is easy to convert as a simple test.


Changes
=======

We create a new animation layer which encapsulates 
AbstractUpdateManager method calls, i.e, chg() and setNoAnimation().
The new layer is also the only one that takes care of reusing VobScenes.
*No other objects are allowed to call AbstractUpdateManager.chg() in 
the whole source tree!*

Let us define the following interface: ::

    package org.nongnu.libvob.view;
    import org.nongnu.libvob.VobScene;

    /** An interface for providing common tool set for animation 
     * and animation debugging information.
     * This interface encapsulates the low-level animation interface
     * such as AbstractUpdateManager methods. 
     * <p>
     * This interface set strict policy for several routines.
     *
     *   1) There must not be other place to get previous/last 
     *      VobScene. If a VobScene is saved in other place than 
     *      here, it could prevent the GC to clean old VobScenes.
     *      By using only the correct ''previous'' VobScene
     *      program can to get the famous ``invalid coorsys`` bug.
     * 
     *   2) There must not be objects that call low-level animation 
     *      interface to change animation state. This could prevent
     *      proper animation, i.e., by setting no animation even
     *      animation should be done.
     */
    public interface AnimationAPI {

	void reuseVS(String caller);

        void animate(String caller);

        void chg(String caller);


        void chgReusingVS(String caller);

        void chgWithAnimation(String caller);


        VobScene getLastVS();
    } 
