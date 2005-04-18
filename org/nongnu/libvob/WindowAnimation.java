/*
WindowAnimation.java
 *    
 *    Copyright (c) 2004, Matti J. Katila
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

package org.nongnu.libvob;
import org.nongnu.libvob.VobScene;

/** An interface for providing common tool set for animation 
 *  and animation debugging information.
 *  This interface encapsulates the low-level animation interface
 *  such as AbstractUpdateManagers' chg and setNoAnimation methods. 
 * <p>
 *  This interface sets strict policy for several routines:
 * <ol><li>
 *      The previously shown vobscene should not be stored 
 *      anywhere else. If a VobScene is saved in other place than 
 *      here, it could prevent the GC to clean old VobScenes.
 *      By using only the correct 'previous' VobScene
 *      program can not get the famous 'invalid coorsys' bug.
 * </li>
 * <li>
 *      No other objects should call the low-level animation 
 *      interface in AbstractUpdateManager. This could prevent
 *      proper animation, e.g., by setting no animation even when
 *      animation should be done.
 * </li></ol>
 * <p>
 *  Methods <code>animate</code> , <code>switchVS</code> and 
 *  <code>rerender</code> are pending state methods, i.e., 
 *  the time when the operation of the method is accomplished 
 *  is (undetermined) soon but not immediately. 
 * <p>
 *
 * Examples:
 * <ol><li>
 * Example: When you want to interpolate a red box from left to right
 * you could do it, like:
 * <pre>
 *  class Scene:
 *      """ Example scene to animate red box from left to right 
 *      and backwards.
 *      """
 *      def __init__(self, windowAnimation):
 *          self.windowAnimation = windowAnimation
 *          self.keyHit = 0
 *          self.left = 100
 *          self.right = 500
 *      def scene(self, vs):
 *          if self.keyHit == 0:
 *              cs = vs.orthoBoxCS(0, "RedBox", 0,self.left,
 *                      50, 1,1, 100,100)
 *          else:
 *              cs = vs.orthoBoxCS(0, "RedBox", 0,self.right,
 *                      50, 1,1, 100,100)
 *          vs.put(vob.vobs.RectBgVob(java.awt.Color.red), cs)
 *      def key(self, key):
 *          self.keyHit = 1 - self.keyHit
 *          self.windowAnimation.animate()
 * </pre>
 * </li><li>
 * Example: When you want to pop a red box from left to right.
 *  The point of example is to use creation of new scene without
 *  animation:
 * <pre>
 *  class Scene:
 *      """ Example scene to pop red box from left to right 
 *      and backwards.
 *      """
 *      def __init__(self, windowAnimation):
 *          self.windowAnimation = windowAnimation
 *          self.keyHit = 0
 *          self.left = 100
 *          self.right = 500
 *      def scene(self, vs):
 *          if self.keyHit == 0:
 *              cs = vs.orthoBoxCS(0, "RedBox", 0,self.left,
 *                      50, 1,1, 100,100)
 *          else:
 *              cs = vs.orthoBoxCS(0, "RedBox", 0,self.right,
 *                      50, 1,1, 100,100)
 *          vs.put(vob.vobs.RectBgVob(java.awt.Color.red), cs)
 *      def key(self, key):
 *          self.keyHit = 1 - self.keyHit
 *          self.windowAnimation.switchVS()
 * </pre>
 * </li><li>
 * Example: Another way to pop a red box from left to right.
 *  This way is like ten times faster than previous example 
 *  where we created a new scene. In this example we set
 *  coordinate system parameters instead:
 * <pre>
 *  class Scene:
 *      """ Example scene to pop red box from left to right 
 *      and backwards.
 *      """
 *      def __init__(self, windowAnimation):
 *          self.windowAnimation = windowAnimation
 *          self.keyHit = 0
 *          self.left = 100
 *          self.right = 500
 *      def scene(self, vs):
 *          if self.windowAnimation.getCurrentVS() != None: return
 *          cs = vs.orthoBoxCS(0, "RedBox", 0,self.left,
 *              50, 1,1, 100,100)
 *          vs.put(vob.vobs.RectBgVob(java.awt.Color.red), cs)
 *      def key(self, key):
 *          self.keyHit = 1 - self.keyHit
 *
 *          vs = self.windowAnimation.getCurrentVS()
 *          cs = vs.matcher.getCS(0, "RedBox")
 *          if self.keyHit == 0:
 *              vs.coords.setOrthoBoxParams(cs, 0,self.left,
 *                   50, 1,1, 100,100)
 *          else:
 *              vs.coords.setOrthoBoxParams(cs, 0,self.right,
 *                   50, 1,1, 100,100)
 *          self.windowAnimation.rerender()
 * </pre>
 * </li><li>
 *  Example: Quite a difficult example where we start to drag 
 *  red box around the scene that may end to be yellow one.
 *  Point of the example is to use both (fast) rerendering of one 
 *  scene and switch scene when it's unavoidable.
 * <p>
 *  The main problem in example is that when handling mouse event you
 *  don't know what is drawn in the current window: a yellow or red
 *  box? We try to guess *the position of the moon* from previous 
 *  event if we even have a one.
 *
 * <pre>
 *  class Scene:
 *      """ Example scene to drag the red or yellow box around.
 *      In left the box is red but in right it is yellow.
 *      """
 *      def __init__(self, windowAnimation):
 *          self.windowAnimation = windowAnimation
 *          self.half = 300
 *          self.ev = None
 *      def scene(self, vs):
 *          # this is an event grabbing example, really.
 *          self.evenrGrabberDraw(vs)
 *      def eventGrabberDraw(self, vs):
 *          if self.ev != None:
 *              cs = vs.orthoBoxCS(0, "Box", 0,
 *                  self.ev.getX(),self.ev.getY(), 1,1, 100,100)
 *          else:
 *              cs = vs.orthoBoxCS(0, "Box", 0,10,10, 1,1, 100,100)
 *          if self.ev.getX() < self.half:
 *              vs.put(vob.vobs.RectBgVob(java.awt.Color.red), cs)
 *          else:
 *              vs.put(vob.vobs.RectBgVob(java.awt.Color.yellow), cs)
 *      def mouse(self, ev):
 *          if self.ev == None: self.ev = ev
 *          vs = self.windowAnimation.getCurrentVS()
 *          cs = vs.matcher.getCS(0, "Box")
 *          if self.ev.getX() < self.half and ev.getX() < self.half:
 *              vs.coords.setOrthoBoxParams(cs, 0,ev.getX(),ev.getY(),
 *                  1,1, 100,100)
 *              if not self.windowAnimation.hasSceneReplacementPending():
 *                  self.windowAnimation.rerender()
 *          elif self.ev.getX() > self.half and ev.getX() > self.half:
 *              vs.coords.setOrthoBoxParams(cs, 0,ev.getX(),ev.getY(),
 *                  1,1, 100,100)
 *              if not self.windowAnimation.hasSceneReplacementPending():
 *                  self.windowAnimation.rerender()
 *          else:
 *              self.windowAnimation.switchVS()
 *          self.ev = ev
 * </pre>
 * </li> </ol>
 */
public interface WindowAnimation {

    public interface BackgroundProcess {

	/** Something has changes from the background process' point of view.
	 *  Implementation note: should be much like rerender in main interface.
	 */
	void chg();
	
	BackgroundProcess getInstance();
    }


    /** Animate to next VobScene by creating a new VobScene.
     *  The interpolation time between current screen and VobScene
     *  (screen after animations) in future is set via 
     *  AbstractUpdateManager.
     *  This method is pending, i.e., next screen update is soon 
     *  but not immediately.
     * <p> 
     *  Animation between scenes is done between keys, i.e., key "A" in 
     *  current visible scene will animate to where 
     *  key "A" is in scene in future.
     *
     * @see AbstractUpdateManager
     * @see VobMatcher
     */
    void animate();


    /** Switch to next VobScene by creating a new VobScene.
     * The switch is fast and no animation is seen. 
     * This method is pending, i.e., next screen update is soon 
     * but not immediately.
     */
    void switchVS();


    /** Rerender the current VobScene. 
     * This method is pending, i.e., next screen update is soon 
     * but not immediately.
     * <p>
     * Changes in next frame are seen if coordinate system
     * parameters are set. Rerendering the current VobScene 
     * is much faster than creating a new VobScene, 
     * so e.g., any drag actions should be implemented to use
     * this method. 
     * <p>
     * Implementation note:
     * <ol><li>
     *  Even though new coordinate systems can be created, the current 
     *  coorder implementation uses finite range of coordinate systems. 
     *  Creating too many new coordinate systems leads to
     *  undefined behauviour.
     * </li><li>
     *  If there's a vob which needs to be removed,
     *  it's usually faster to set coordinate system parameters 
     *  to zero than generate a new scene without the accursed vob.
     * </li></ol>
     * @see VobCoorder
     */
    void rerender();


    /** Get the current visible vobscene. 
     * <p>
     * Prgogramming note: When programming, you create vobscenes
     * for future usually, so this returns the current visible
     * vobscene, e.g., to set coordinate system parameters or 
     * use activated coordinate systems to catch mouse events.
     */
    VobScene getCurrentVS();


    /** Returns true if there are pending methods that 
     *  create a new VobScene which has not yet been updated
     *  within window. The methods that create a new VobScene
     *  are <code>animate</code> and <code>switchVS</code>.
     * <p>
     * Programming note:
     *  In some situations when handling the events the programmer 
     *  needs to know whether the VobScene is new or the still the old one,
     *  e.g., when waiting to move some new vob you
     *  you need to pass all events trough before the screen has updated.
     */
    boolean hasSceneReplacementPending();

    /** Returns true if animate(), rerender() or switchVS()
     *  has been called since the scene has been last rendered.
     */
    boolean hasAnimModeSet();
} 
