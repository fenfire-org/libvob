
==========================================================================
PEG recursive_interpolation_list--mudyc:
==========================================================================

:Authors:  Matti J. Katila
:Date-Created: 2004-08-21
:Status:   Incomplete

:Stakeholders: benja
:Scope:    Major
:Type:     Architecture

:Affect-PEGs: vobscene_recursion--tjl


We shall proceed with the recursive vob scene architechture and
implement recursive interpolation list next.

Issues
======

.. none yet

Changes
=======

To interpolate between two scenes we call VobMatcher.interpList
currently. The method returns an integer array which defines how
coordinate system interpolates. Now, when we have recursive vob
scenes, it is not sufficient to return an integer array anymore.
Same child vob scene may occur in multiple places in screen and all
these childs can interpolate in different manner.

We shall make interpolation lists also for child scenes. We enable it
by changing the return type of the method VobMatcher.interpList to be
instance of InterpList as follows::

    public class InterpList {
        int [] interpList;
	InterpList [] childInterpLists;
	int[] mapCStoChildInterpList;
    }


Example
=======

