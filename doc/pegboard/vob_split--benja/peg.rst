=======================================================
PEG ``vob_split--benja``: Split the ``gzz.vob`` package
=======================================================

:Author:   Benja Fallenstein
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Accepted [mostly implemented]


Tuomas Lukka wrote in "[Gzz] gzz.vob.vobs, gzz.vobs or ???" (2002-08-26):

    I'd like to split off the real Vob-implementing classes from gzz.vob
    and leave only the framework classes there. However, there's a bit of
    a naming problem...
    
I propose to split the vob package in the following parts:

- ``gzz.vob``: the interfaces, including ``GraphicsAPI``
- ``gzz.vob.linebreaking``: linebreaking-related code
- ``gzz.vob.impl``: the default impls of ``VobScene`` etc.
- ``gzz.vob.impl.awt``, ``gzz.vob.impl.gl``: Platform-specific stuff.
- ``gzz.vob.vobs``: the default vob impls

After this, ``gzz.vob`` would be added to the list of frozen packages
in the pegboard_.

.. _pegboard: ../pegboard.html

\- Benja

