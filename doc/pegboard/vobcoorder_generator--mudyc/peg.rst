============================================================
``vob_cs_generator--mudyc``: Vob Coordinate System Generator
============================================================

:Authors:  Matti Katila
:Last-Modified: $Date: 2003/09/17 13:09:25 $
:Revision: $Revision: 1.2 $
:Status:   Irrelevant


Introduction
------------

The Vob Coordinate System Generator generates coordinate systems 
automatically for Vobs. Currently I'm planning a special case, but in 
the future there may be many different generators. Generators make Vob 
placing easier.

The planned usage is for a view's render().

Usage
-----

1) Make an instance of the generator like this::

    /** Constructor:
     *  @params:
     *     int CS = Coordinate system where you place all vobs
     *     int vob_count = The amount of vobs in given CS.
     *     int style = Horizontal, vertical, etc..
     */
    public MiscVobCSGenerator(int CS, int vob_count, int style);


2) Use the generator::


        MiscVocCSGenerator vob_cs_gen = ...;
        Cell c = ...;

        while (c != null) {
            int vob_cs = vob_cs_generator.getNext();
            vs.activate(vob_cs);  
            vs.map.put(new SomeVob(c), vob_cs);
            c = c.s(d.d1);
        }

- the generator makes matching and gives good CS.
- a suitable CS can be asked with the getNext() method.


Issues
------

- autoactivation for Vob matching could be needed.
- the Vob's matching name must be given somehow.
- where to put this in the gzz/ directory hierarchy?

Examples
--------

::

	public static final int HORIZONTAL = 1;
	public static final int VERTICAL   = 2;

Need for more?::

	public static final int .... = n+1;


Vertical example::

	vob_count = 2       vob_count = 3        vob_count = 6

	+-----------+       +-----+-----+        +-----+-----+ 
	|           |       | Vob1|     |        | Vob1| Vob5| 
	|           |       |     |     |        |     |     | 
	|   Vob 1   |       +-----+     |        +-----+-----+ 
	|           |       | Vob2|     |        | Vob2| Vob6| 
	|           |       |     |     |        |     |     | 
	+-----------+	    +-----+     |        +-----+-----+ 	
	|           |	    | Vob3|     | 	 | Vob3|     | 	
	|           |	    |     |     | 	 |     |     | 	
 	|   Vob 2   |	    +-----+     | 	 +-----+     | 	
 	|           |	    |           | 	 | Vob4|     | 	
 	|           |	    |           | 	 |     |     | 	
 	+-----------+       |           |        +-----+     |           
 	|  Empty    |	    |  Empty    | 	 |   Empty   | 	
 	+-----------+       +-----------+        +-----+-----+ 


	vob_count = 15 
	
 	+---+---+---+
 	|V1 |V8 |V15|
 	+---+---+---+
 	|V2 |V9 |   |
	+---+---+   |
	|V3 |V10| E |
	+---+---+ m |
	|V4 |V11| p |
 	+---+---+ t |
	|V5 |V12| y |
	+---+---+   |
	|V6 |V13|   |
	+---+---+   |
	|V7 |V14|   |
	+---+---+---+