====================================================
PEG ``vob_bgvob--humppake``: Abstract Background Vob
====================================================

:Authors:   Asko Soukka
:Stakeholders: Benja Fallenstein, Tuomas Lukka 
:Date-created: 2003-03-05
:Last-Modified: $Date: 2003/05/21 10:43:56 $
:Revision: $Revision: 1.3 $
:Status:   Irrelevant
:Scope:    Trivial
:Type:     Feature

This PEG proposes creating **Abstract Background Vob** class, which
implies creation of **Background interface** and **Colored
Interface**.

Background Vob is a very basic vob type, which enhances the Vob
currently with the the following features:

 - single color background
 - possibility for single color border
 - possibility for multiple background colors, which are shown 
   e.g. as parallel vertical stripes

Background Vob implements interface for single color backround and
border from Background Interface and handling of multiple
background colors from Colored Interface.

Issues
------

- Why is AbstractBgVob needed?

  RESOLVED: Currently there is no common interface for multiple solid
  colors in node vobs, but they are defined and implemented from the scratch in 
  every background vob (starting from ``RectBgVob``). A common interface is
  necessary when that a single node view could select between multiple different
  background vob types depending on nodes' content; the same node view could place
  different vob depending on the node's content.

  RE-RESOLVED: AbstractBgVob will be replaced by AbstractColorableVob
  defined by a new PEG_. This PEG will be declared irrelevant.

.. _PEG: ../vob_colorable--humppake/peg.gen.html

- How should AbstractBgVob features be assembled?

  RESOLVED: Background Vob implements interface for single color
  backround and border from Background Interface and handling of
  multiple background colors from Colored Interface.

- How should the new interfaces and classes be named?

  RESOLVED: **public interface Background**, **public interface
  Colored** and **public abstract class AbstractBgVob**. In
  AbstractBgVob the word Background is truncated after the current
  *background vob* naming practice (``SolidBgVob``, ``RectBgVob``,
  ``OvalBgVob``).

- Where they should be located?

  RESOLVED: Currently in the package **``gzz.vob``**
  (``gzz.vob.Colored``, ``gzz.vob.Background`` and
  ``gzz.vob.AbstractBgVob``). Probably in the future they will be 
  moved into package ``org.libvob.vob``.

- How should the common features of current background vobs be
  splitted between Background Interface and Colored Interface?

  RESOLVED: Methods for single color backround and border will be
  defined in Background Interface and methods of multiple
  background colors in Colored Interface. The AbstractBgVob will
  contain the default implementations for them.

- How should *solids* be renamed?

  RESOLVED: Earlier, *cellColors* has been proposed. Although, we are
  currently moving away from *cells* to *nodes*. Therefore *solids*
  should be called simply **colors**. Note that already the method
  adding them has been called *addColor*.

- How should we store colors?

  RESOLVED: Since we are using the *java.util Collections API*
  the **ArrayList** implementation of List should be used.

- What should be the default values for **bgColor**, **borderColor**,
  **drawBorder** and **colors**?

  RESOLVED: Default bgColor will be **``java.awt.Color.white``**,
  borderColor will be **``java.awt.Color.black``**, drawborder will be
  **true** and colors will be **null**. These are the currently used
  default settings for background vobs.
 
Changes
-------

The Java classes **public interface Background**, **public interface
Colored** and **public abstract class AbstractBgVob** should  be
created after the following diagram:

.. UML:: abstractbgvob

    jlinkpackage org.nongnu.libvob

    class Vob "abstract"

    class Background "interface"
        methods
	    +void setBgColor(Color c)
	    +Color getBgColor()
	    +void setBorderColor(Color c)
	    +Color getBorderColor()
	    +void setDrawBorder(boolean b)
            +boolean getDrawBorder()
   
    class Colored "interface"
        methods
	    +void addColor(Color c)
	    +List getColors()

    class AbstractBgVob "abstract"
        realize Colored
        realize Background
        inherit Vob
        methods
	    #boolean drawBorder
	    #Color bgColor
	    #Color borderColor
	    #ArrayList colors
            
    class vobs.RectBgVob
        realize AbstractBgVob

    class vobs.SolidBgVob
        realize AbstractBgVob

    class vobs.OvalBgVob
	realize AbstractBgVob

    class vobs.ColoredSectorVob
	inherit vobs.OvalBgVob

    class vobs.ColoredSquareSectorVob
	inherit vobs.ColoredSectorVob

   ---
   horizontally(50, vob_h, Colored, Vob, Background);
   vertically(50, vob_v, Vob, AbstractBgVob, vobs.RectBgVob);
   horizontally(50, vobs_h, vobs.SolidBgVob, vobs.RectBgVob, vobs.OvalBgVob);
   vertically(50, sector_v, vobs.OvalBgVob, vobs.ColoredSectorVob);
   horizontally(50, sector_h, vobs.ColoredSquareSectorVob, vobs.ColoredSectorVob);

The following background vobs should be modified to inherit
``AbstractBgVob``:

 - ``gzz.vob.vobs.RectBgVob``
 - ``gzz.vob.vobs.SolidBgVob``
 - ``gzz.vob.vobs.OvalBgVob``

Also ``gzz.vob.vobs.ColoredSectorVob`` should be inherited from
AbstractBgVob (via ``OvalBgVob``, which holds some circle routines for
GL). Semantically *colored sector vobs* are not meant to be background
vobs, but they do have the same features. The difference to background
vobs will be done by dropping ``Bg`` off from their naming.

No **Cell Views** or **Node Views** should be broken after this
change, since background vobs' addColor interface remains
same. Although, at least Loom's Node Views should be build using
Abstract Background Vob instead of ``RectBgVob`` or any other
particular Vob.
