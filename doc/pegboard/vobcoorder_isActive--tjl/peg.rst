=============================================================
PEG vobcoorder_isActive--tjl: Add isActive into VobCoorder
=============================================================

:Author:   Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Accepted

One more method for VobCoorder, for querying activity of a cs.

Issues
------

Changes
-------
Add into VobCoorder::

    /** Whether activate(cs) has been called.
     */
    public boolean isActive(int cs);
    
