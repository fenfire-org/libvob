=============================================================
PEG vobmatcher_keymap--tjl: Sanity to vob key remapping
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Accepted

The current unofficial keymap part of DefaultVobMatcher is unofficial
for a good reason: it's not very useful as it stands. Most importantly,
it sometimes changes interpolation in unpredictable ways.

Issues
======

- Do we want to be more specific, by e.g. saying WHICH VobScene the interpolation
  keymapping touches?

    RESOLVED: Not at this stage; possibly this is a desirable addition later,
    but we should get quite far with the current functionality.

Requirements
============

It has to be easy to say "in the new scene, you should interpolate MAINVP
to foo and bar to MAINVP, when coming from the previous view".

Changes
=======

Add to interpList a parameter "towardsOther".  If true, the key mappings
of the other vobscene are used, and otherwise the key mappings of the
current scene are used.

Make all GLRenderingSurface etc. methods that call interpList use this
in a predictable way.


