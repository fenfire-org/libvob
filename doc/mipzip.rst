================================
Mipzip file format specification
================================

The Mipzip file format is a simple format for storing
all mipmap levels of a texture on a hard drive compressed, in a single Zip file, 
but able to be loaded efficiently.

In Libvob, reading mipzip files is implemented by the class
``org.nongnu.libvob.gl.MipzipFile``, around which more sophisticated
classes (such as ``org.nongnu.libvob.gl.MipzipLoader`` are built.

..  UML:: mipzipfile

    jlinkpackage org.nongnu.libvob.gl

    class MipzipFile
	jlink
	assoc compos - multi(1) java.util.zip.ZipFile
	methods

    class java.util.zip.ZipFile

    ---
    horizontally(40, xxxx, MipzipFile, java.util.zip.ZipFile);

The current format, V1.0, is defined as follows. 

The Zip file shall contain two constant entries and a variable
number of mipmap level entries.
The constant entries are

``texformat``
    The texture format used in the file. The **comment** of this entry 
    (not the entry contents!) stores the OpenGL token string, 
    without the ``GL_`` prefix,
    for example ``COMPRESSED_RGB_S3TC_DXT1_EXT`` or ``RGB``.

``internaltexformat``
    (optional - not used for compressed textures)
    The recommended internalformat token to give OpenGL for this
    texture. E.g. ``RGB4``

``datatype``
    (optional - not used for compressed textures)
    Specifies the data type to give to the OpenGL texImage2D command.
    E.g. ``UNSIGNED_SHORT_5_6_5``

``origsize``
    The size of the original image inside this mipzip, as (texcoord1)x(texcoord2),
    e.g., ``0.99609375x0.9990234375``

It is possible that later versions shall add more constant fields, and
a version number identifier.

The variable fields have as their name the integer level index (zero-based),
and as their content the direct binary data. As their **comment** they
have a string (width)x(height), 

Altogether, this gives the mipzip file reader enough information to call
glTexImage2D or glCompressedTexImage2D.

Example: the output of ``unzip -l`` on an archive::

    Archive:  /hdd/6/tmpimg/urn_-_x-storm_-_1.0_-_application__pdf,abslyr6tzv4z4fxbfcdpxcvavyc7cioa.43ffr2a4w44d3yew3fabmxj3srcu2ehdclgye5q-240x186-1
      Length     Date   Time    Name
     --------    ----   ----    ----
	    0  08-26-03 16:03   texformat
    COMPRESSED_RGB_S3TC_DXT1_EXT
	    0  08-26-03 16:03   origsize
    0.99609375x0.9990234375
      2097152  08-26-03 16:03   0
    2048x2048
       524288  08-26-03 16:03   1
    1024x1024
       131072  08-26-03 16:03   2
    512x512
	32768  08-26-03 16:03   3
    256x256
	 8192  08-26-03 16:03   4
    128x128
	 2048  08-26-03 16:03   5
    64x64
	  512  08-26-03 16:03   6
    32x32
	  128  08-26-03 16:03   7
    16x16
	   32  08-26-03 16:03   8
    8x8
	    8  08-26-03 16:03   9
    4x4
	    8  08-26-03 16:03   10
    2x2
	    8  08-26-03 16:03   11
    1x1
     --------                   -------
      2796216                   14 files

