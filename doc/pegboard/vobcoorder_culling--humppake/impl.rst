:Status:	Incomplete

After changing ``gzz.vob.VobCoorder`` these methods should also be 
implemented in all implementing classes, which inherit 
``gzz.vob.VobCoorder``. Because culling is already implemented in 
``gzz.gfx.gl.GLVobCoorder``, currently classes left seem to be:

* ``gzz.vob.impl.OrthoCoorder``
  
  **NOTE:** ``CullingCoordsys`` is currently implemented only on the
  C++ side, but ``OrthoCoorder`` is not allowed to use JNI. *Dummy*
  ``CullingCoordSys`` can be implented on the Java side as 
  ``translate(parent, 0, 0)``.

