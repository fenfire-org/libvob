
==========================================================================
PEG activateregion--mudyc:
==========================================================================

:Authors:  Matti J. Katila
:Date-Created: 2004-08-08
:Revision: $Revision: 1.1 $
:Status:   Incomplete
:Scope:    Minor
:Type:     Architecture

.. Affect-PEGs:


Performance problems in mouse dragging in FenPDF is a real issue which
needs to be solved. The problem is basically that there are n
activated coordinate systems which *all* need to be iterated to check
whether coordinate system should be considered as needed. There may be
activated coordinate systems hidden because of culling as well. 

One way to improve the iteration phase is to skip those not needed
activated coordinate systems. For example if in double view there are
two canvases represented, activated coordinate  system should be
searched only from the view region wherein mouse is hit. We should be
able to say that some coordinate system is activated region, i.e., if
mouse is not hit to activated region all it's child coordinate systems
should be considered as not searched and for that reason, skipped.


Issues
======

.. none yet

Changes
=======

Add the following method to VobCoorder::

    /** Sets a speed hint for a coordinate system, region that
     *  may have activated child coordinate systems. Speed hint is
     *  mainly used only with getCSAt method wherein child coordinate
     *  systems are only checked if parent coordinate system, the
     *  activated region, is hit by mouse.
     * @see activate
     */
    void activateRegion(int cs);

