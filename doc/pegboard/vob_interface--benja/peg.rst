===================================================
``vob_interface--benja``: Make ``Vob`` an interface
===================================================

:Author:   Benja Fallenstein
:Date:	   2003-02-23
:Type:     Interface
:Scope:    Minor
:Status:   Accepted


``Vob`` is currently an abstract class; for Java, 
central things like this are usually interfaces,
allowing for multiple inheritance, e.g. 
subinterfaces ``ColorableVob`` and ``ShapableVob``
or something like that, both of which could be
implemented by the same class.

To conform better to the usual conventions,
make ``Vob`` an interface and provide
an abstract implementation (``AbstractVob``).

\- Benja
