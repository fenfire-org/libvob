=================================================================
PEG vobscene_put--tjl: Delegate VobPlacer's methods from VobScene
=================================================================

:Author:   Tuomas Lukka
:Date:     $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Implemented

Too often need to write::

    vs.map.put(...)

when it should be just ``vs.put``.

Changes
-------

For all methods in ``VobPlacer``, there should be corresponding
methods in ``VobScene`` delegating to the ``VobMap``.
