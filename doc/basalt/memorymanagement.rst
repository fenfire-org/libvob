=========================
The Libvob memory manager
=========================

This document explains the memory manager used in libvob
to manage **OpenGL texture memory**. Other memory management is
done using Java and C++ normally.

Mipzips - loading textures at reduced resolutions
-------------------------------------------------

The basic premises of texture memory are:

- Textures can be loaded at different resolutions, making
  a tradeoff between quality and space. 

- Space is rather limited, as it's best to have the whole
  working set of textures on the video card.

When using Libvob in a system where there are several pictures,
it is useful to be able to dynamically load and unload 
different **levels of detail** (this is a common term in Computer
Graphics). 

For performance and simplicity, Libvob currently uses mipmap
level granularity for loading and unloading of textures:
for being able to draw an image correctly at different
scales, the graphics cards require a set of *mipmaps* to be stored
in the graphics memory; the mipmaps start from the largest
texture (both sides must be powers-of-two) and each successive mipmap
level divides the size of the sides by two, requiring one fourth 
of the space of the preceding level.

We have chosen to store all mipmap levels of a texture in a .zip file
and load on demand the requested levels.

The MipzipLoader class implements a basic interface to this
functionality:

..  UML:: mipziploader

    jlinkpackage org.nongnu.libvob.gl

    class MipzipLoader
	jlink
	assoc compos - multi(1) java.util.zip.ZipFile
	assoc compos - multi(1) GL.Texture
	methods
	    loadToBaseLevelSynch
	    setGoalBaseLevel
	    getMemory()
	    getMemory(int level)
	    getLevelForBytes(int memory)
	    getLevelForQuality(float quality) 


    class java.util.zip.ZipFile

    class GL.Texture
	jlink

    ---

    horizontally(40, xxxx, GL.Texture, java.util.zip.ZipFile);

    vertically(60, yyyy, MipzipLoader, xxxx);

It is possible to use MipzipLoader synchronously, by calling
loadToBaseLevelSynch, or asynchronously, by setting the *goal*
mipmap level to load to, and loading will take place
in the background.
The class uses the Background and the OpenGL backgrounding
functionality to load the textures, but the complications
will not be described here.

The class also has methods to query the memory usage of the different
levels &c, but these are best described in the Javadoc.


MemoryPartitioner - dividing memory between mipzips
---------------------------------------------------

The MipzipLoader class will not solve our texture memory
problem by itself: it provides the mechanism to load textures
at desired resolutions, removing and adding detail as necessary,
but someone needs to make the decision which textures are important.

For some application architectures, this can be done by the
application itself, centrally; however, e.g., in the Fenfire system,
there may be different views and no single point that knows
everything (due to polymorphism and encapsulation).

For this, we shall use an abstraction of a memory pool
(MemoryPartitioner) and objects that want some memory but
are able to degrade their quality gracefully when decreasing
the amount of memory.

..  UML:: memorypartitioner1

    jlinkpackage org.nongnu.libvob.memory

    class MemoryPartitioner
	jlink
	methods
	    request(MemoryConsumer consumer, float importance, q)
	assoc aggreg - multi(*) MemoryConsumer

    class MemoryConsumer "interface"
	jlink
	methods
	    getMaxBytes(float quality)
	    setReservation(float priority, int bytes, float quality)

    ---

    vertically(60, xxxx, MemoryPartitioner, MemoryConsumer);

The way this is connected to mipzips is through MipzipLoader:

..  UML:: memorypartitionermipzip

    jlinkpackage org.nongnu.libvob.memory

    class MemoryPartitioner
	jlink


    class MemoryConsumer "interface"
	jlink

    jlinkpackage org.nongnu.libvob.gl

    class MipzipLoader
	jlink

    class MipzipMemoryConsumer
	jlink
	assoc aggreg - multi(1) MipzipLoader
	realize MemoryConsumer

    class ClientClass

    dep "requestMemory" ClientClass MemoryPartitioner
    dep "create" ClientClass MipzipMemoryConsumer
    dep "getTexture" ClientClass MipzipLoader

    dep "use" MemoryPartitioner MemoryConsumer

    ---

    horizontally(100, xxxx, MemoryPartitioner, MemoryConsumer);
    horizontally(60, zzzz, MipzipMemoryConsumer, MipzipLoader);
    vertically(60, yyyy, MemoryConsumer, MipzipMemoryConsumer);
    vertically(100, qqqq, MipzipMemoryConsumer, ClientClass);




To do
-----

To obtain better estimates of the relative importances of different
textures, we should not only rely on calls when building the view
but provide in, e.g., PaperQuad some feedback -generating routine
which would allow us to read the number of pixels that were actually
rendered (this is possible using some OpenGL extensions).

