=============================================================
PEG ``vobscene_split--tjl``: Split VobScene once more
=============================================================

:Authors:  Tuomas Lukka
:Stakeholders: Benja Fallenstein
:Last-Modified: $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Date-Created: 2002-09-05
:Status:   Implemented

Currently, a VobScene consists of

1) VobMap: the binding between coordinate systems indices and vobs,

2) VobCoorder: the coordinate systems and their keys

I'm suggesting splitting this one more time:

1) VobMap: the binding between coordinate systems indices and vobs,

2) VobCoorder: the coordinate systems

3) VobMatcher: the binding between coordinate system indices and keys.

GraphicsAPI calls creating vobscenes would accept a parameter for VobMatcher for now,
or alternatively it would not yet be a final member.

(Benja:)::

	Sounds ok.

The point is to free the vob matching functionality for extending and experiments;
we want hierarchical things, things depending not only on identity but clicks etc.

(Benja:)::

	Not sure we can make it general enough to encompass all the experiments
	we'd like to do...

(Tjl:)::

	Of course not; OTOH, it's better than what we have now. Going to the right 
	direction.

The VobMatcher API would be::

    public interface VobMatcher {
	void add(int cs, Object key); // called by VobScene.put &c.

	int getCS(Object key);

	/** Return, for each coordinate system of this matcher, an integer
	 * giving the coordinate system of the other matcher that system should move
	 * to.
	 */
	int[] interpList(VobMatcher other);
    }

Now, the important point is that particular vobmatchers can add other add methods, e.g.::

	void add(int cs, Object key, String subkey)
	
(Benja:)::
	
	One thing I would like to do is span interpolation similar to the old text
	interpolation stuff from 0.6. So,

        void add(int cs, Object key, int start, int n)

	But that needs help from the coorder or somewhere to work right,
	because it may need to split the spans while interpolating...
    
	Can that work with this scheme?`

(Tjl:)::

	I don't see why it couldn't. All we basically need is a call to
	quash a renderable (don't show while animating) and replacing it with
	split pieces.


	At the same time, add to VobPlacer a new method

	put(Vob v)

	without a coordinate system, for non-coorded vobs.

(Benja:)::
 
	I'd prefer a longer (more descriptive) method name as this won't be used 
	as often.

(Tjl:)::

	Consistency: we now have
	    void put(Vob vob, int coordsys);
	    void put(Vob vob, int coordsys1, int coordsys2);
	so I think put(Vob v) would be best. Sorry, I first said
	VobMap, I meant of course VobPlacer.  








