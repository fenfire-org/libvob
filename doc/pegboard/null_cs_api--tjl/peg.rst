=============================================================
PEG null_cs_api--tjl: Interface changes 
=============================================================

:Author:   Tuomas Lukka
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Implemented
:Scope:	   Minor
:Type:     Interface

There are some coordinate systems that will not get created sometimes:
currently, buoy and cull coordsystems are sometimes returned as null.
This needs to be addressed in the VobCoorder transforming API.

Issues
======

    - is it a problem that transformPoint and transformPoints2 will sometimes
      not return their ``into`` parameters?

	RESOLVED: Not really. The most common use is in Vobs and there the 
	coordinate systems are guaranteed to be valid; otherwise, they would
	not be drawn.


Changes
=======

Change ::

    public void transformPoints3(int withCS, float[] pt, float[]into) {
    public boolean inverseTransformPoints3(int withCS, float[] pt, float[]into) {

into ::

    public float[] transformPoints3(int withCS, float[] pt, float[]into) {
    public float[] inverseTransformPoints3(int withCS, float[] pt, float[]into) {

Which will return into, if non-null and the cs is valid.. 

Then, change the spec of the functions ::

    public float[] transformPoints3(int withCS, float[] pt, float[]into) {
    public float[] inverseTransformPoints3(int withCS, float[] pt, float[]into) {
    public java.awt.Point transformPoint(int cs, float x, float y, java.awt.Point into) {
    public java.awt.Point[] transformPoints2(int cs, float[] coords, java.awt.Point[] into) {

so that if the coordinate system is not valid (e.g. it's a buoy or culled coordinate
system that is currently not shown), these will return null and not
transform anything.
