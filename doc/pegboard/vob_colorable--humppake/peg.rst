=======================================================
PEG ``vob_colorable--humppake``: Abstract Colorable Vob
=======================================================

:Authors:   Asko Soukka
:Stakeholders: Benja Fallenstein, Tuomas Lukka 
:Date-created: 2003-03-05
:Last-Modified: $Date: 2003/04/24 07:47:36 $
:Revision: $Revision: 1.3 $
:Status:   Implemented
:Scope:    Trivial
:Type:     Feature

This PEG proposes creating **AbstractColorableVob** class, which
implements a new **ColorableVob interface** also defined by this PEG.

**ColorableVob** is a very basic vob type, which enhances the regular
Vob with interface for placing multiple solid colors on its
background. The multiple solid colors are used a lot in basic views
i.e. to show cursor location and node properties. In the current
implementations multiple solid colors are shown as parallel vertical
stripes or sectors.

This PEG replaces the older `PEG for Abstract Background Vob`__.

This PEG is depends on replacing current abstract class Vob with
interface Vob and abstract class AbstractVob, which implements the
first one.

.. _PEG: ../vob_bgvob--humppake/peg.gen.html
__ PEG_

Issues
------

- Why is Abstract Colorable Vob needed?

  RESOLVED: Currently there is no common interface for multiple solid
  colors in vobs, but they are defined and implemented from the
  scratch in every background vob (starting from ``RectBgVob``). A
  common interface is necessary when a single node view that supports
  solid colors, should select between multiple different background
  vob types (basing the selection i.e. on nodes' content).

- Why was the PEG for Abstract BackgroundVob declared irrelevant?

  RESOLVED: It was about to propose such interfaces for background
  vobs, which would have made them strongly mutable. It was also
  criticized to restrict background vobs too much by forcing them to
  have border and background color properties.

- Should Colorable Vob be immutable?

  RESOLVED: Yes. The current multi-color implementation in
  ``RectBgVob`` and other background vobs have made them mutable.
  Inheriting those multi-color features from Colorable Vob should turn
  them back to immutable. Immutability allows storing created vob
  objects, re-using them and finally enhancing the overall
  performance.

  Multi-coloring stored vob prototype (which implements Colorable Vob)
  could be cloned using method::
  
    ColorableVob cloneColored(Color[] colors);

  There will be also several shorthands for that method, which are
  discussed later.

- How is Colorable Vob related to background vobs?

  RESOLVED: Colorable Vob won't define any background vob properties
  like background color or border. Although, background vobs should be
  immutable and use the common interface for multiple solid colors;
  They should be inherited from ``AbstractColorableVob``.

- How should Colorable Vob be assembled?

  RESOLVED: ColorableVob extends the basic Vob class and implements
  Colorable interface for multiple background colors.

  RE-RESOLVED: AbstractColorableVob extends AbstractVob and implements
  ColorableVob interface. ColorableVob interface is extended from 
  interface Vob.

- How should the new interfaces and classes be named?

  RESOLVED: **public interface Colorable** and **public abstract class
  AbstractColorableVob**

  RE-RESOLVED: **public interface Colorable** and **public abstract
  class ColorableVob**

  The prefix Abstract has been truncated from AbstractColorableVob to
  help casting when using cloneColored() and keep naming consistent 
  with other Vob package's classes.

  interface Colorable + abstract class Vob = 
  abstract class ColorableVob

  RE-RESOLVED: **public interface ColorableVob** and
  **public abstract class AbstractColorableVob**, because creation of
  AbstractVob

- Where they should be located?

  RESOLVED: Currently in the package **``gzz.vob``**
  (``gzz.vob.ColorableVob`` and
  ``gzz.vob.AbstractColorableVob``. Probably in the
  future they will be moved into package ``org.libvob.vob``.

- How should *solids* be renamed?

  RESOLVED: Earlier, *cellColors* has been proposed. Although, we are
  currently moving away from *cells* to *nodes*. Therefore *solids*
  should be called simply **colors**. Note that the method
  adding them in the current implementations has been called
  *addColor*.

- How should we store colors?

  RESOLVED: Since we are using the *java.util Collections API*
  the **ArrayList** implementation of List should be used.

  RE-RESOLVED: For performance reasons we should use pure **Color Array**.
  This is also reasonable, because vobs are immutable and therefore 
  colors can't be added after creating the multi-colored vob (via
  ``Colorable.cloneColore()``). 

  It still possible to use ArrayList for building up the colors and pass
  ``List.toArray()`` to ``Colorable.cloneColored()``.

  Even the inner presentation of colors in ColorableVob is Color Array, 
  It should also be possible to call cloneColored with an ArrayList.

- How should the colors be set?

  RESOLVED: Since Colorable Vob should be immutable, colors
  could be added only by cloning a new vob. This method would prefer
  setting all the colors at once as a single List.

  RE-RESOLVED: All colors should be set at once by passing 
  a single **array** to vob's ``Colorable.cloneColored()``.
  If the array is null or empty and vob's own color array is
  null or empty, cloneColored() will return the vob itself.

  The vob is *allowed* to return itself also, if parameter 
  colors == its own colors, but it's not obligated to compare the
  lists.

  There should be also several shorthands for the default
  cloneColored::

    ColorableVob cloneColored(List); // for List
    ColorableVob cloneColored(Color); // for single color

  RE-RESOLVED: Previously introduced methods replace the old
  colors (the colors of the original vob) with new ones (parameter
  colors). The following methods are used to add the new colors
  next to old ones::

    ColorableVob cloneColorAdded(Color[]);
    ColorableVob cloneColorAdded(List);
    ColorableVob cloneColorAdded(Color);

  They could be implemented as shorthands to the cloneColored(color[])
  method. I.e. ``return cloneColored(getColors().clone().append(color))``.

  RE-RESOLVED: *The default action* of cloneColored() should be adding
  new colors in addition to the already existing colors. Therefore
  cloneColorAdded() methods will be renamed to cloneColor() and methods
  cloning with only colors provided as parameter, will be renamed to
  cloneColorReplace().

- Should SolidBgVob be inherited from Colorable Vob?

  RESOLVED: No. ``SolidBgVob`` is not a such background vob than other
  BgVobs (``RectBgVob`` and ``OvalBgVob``). More prefarably, SolidBgVob
  should be renamed to avoid misunderstandings.

- Should **Colored Sector Vobs** be inherited from Colorable Vob?

  RESOLVED: Yes. Also ``gzz.vob.vobs.ColoredSectorVob`` should be
  inherited from Colored Vob (via ``OvalBgVob``, which holds some
  circle routines for GL). Semantically Colored Sector Vobs are not
  meant to be background vobs, but they do have the same features. The
  difference to background vobs will be done by dropping ``Bg`` off from
  their naming.

- Interface Colorable needs Object.clone(), that needs implenting
  java.lang.Cloneable interface. Who should implement it?

  RESOLVED: Since it's currently needed only by interface ColorableVob,
  its implementation AbstractColorableVob should implement also interface
  Cloneable.

Changes
-------

The Java classes **public interface ColorableVob** and **public abstract
class AbstractColorableVob** should be created after the following
diagram:

.. UML:: abstractcolorablevob

    jlinkpackage org.nongnu.libvob

    class Vob "interface"
	jlink

    class AbstractVob "abstract"
	jlink
	realize Vob

    class ColorableVob "interface"
	jlink
	inherit Vob
        methods
	    +ColorableVob cloneColored(Color[] colors)
	    +ColorableVob cloneColored(List colors)
	    +ColorableVob cloneColored(Color c)
	    +ColorableVob cloneColorReplace(Color[] colors)
	    +ColorableVob cloneColorReplace(List colors)
	    +ColorableVob cloneColorReplace(Color c)
	    +Color[] getColors()
   
    class java.lang.Cloneable "interface"

    class AbstractColorableVob "abstract"
	jlink
	realize java.lang.Cloneable
        realize ColorableVob
        inherit AbstractVob
        methods
	    #Color[] colors
            
    class vobs.RectBgVob
	jlink
        realize AbstractColorableVob

    class vobs.OvalBgVob
	jlink
	realize AbstractColorableVob

    class vobs.ColoredSectorVob
	inherit vobs.OvalBgVob

    class vobs.ColoredSquareSectorVob
	inherit vobs.ColoredSectorVob

   ---
   horizontally(50, vob_h, Vob, AbstractVob, java.lang.Cloneable);
   vertically(50, vob_v, java.lang.Cloneable, AbstractColorableVob, vobs.OvalBgVob);
   horizontally(50, colorable_h, ColorableVob, AbstractColorableVob);
   horizontally(50, vobs_h, vobs.RectBgVob, vobs.OvalBgVob);
   vertically(50, sector_v, vobs.OvalBgVob, vobs.ColoredSectorVob);
   horizontally(50, sector_h, vobs.ColoredSquareSectorVob, vobs.ColoredSectorVob);

The following background vobs should be modified to inherit
``AbstractColorableVob``:

 - ``gzz.vob.vobs.RectBgVob``
 - ``gzz.vob.vobs.OvalBgVob``

**Cell Views** and **Node Views** will be broken (and hast to be
fixed) after this change, since background vobs' addColor interface
will be removed to make vobs immutable. 

The recommended way to fix Views is to store all needed vob prototypes
as class attributes when instantiating the class at the first
time. When a multi-colored version of any prototype (whose class
implements Colorable) is needed, it will be created by passing colors
to prototype's cloning method::

    ColorableVob cloneColored(Color[] colors);
