=============================================================
PEG vobscene_twids--tjl: Small changes mostly to VobMatcher
=============================================================

:Author:   Tuomas Lukka
:Date:     $Date: 2003/03/31 10:00:04 $
:Revision: $Revision: 1.1 $
:Status:   Implemented

There are some small details in VobScene and related classes
that need to be addressed.

Issues
------

    - should getParent() return 0 or -1 when no parent
      was specified? 0 would be consistent with VobCoorder,
      but -1 would be more logical since then
      getCS(key) could be different from 
      getCS(0, key).

        RESOLVED: 0. getCS(key) **should** be equivalent to
	getCS(0, key) to avoid horrible confusion.


Changes
-------

Currently, VobMatcher has the methods ::

    int add(int cs, Object key); 

    int addSub(int into, int cs, Object key);

    int getCS(Object key);

    Object getKey(int cs);

The proposed changes are as follows:

    - Name change: addSub --> add::

	int add(int into, int cs, Object key);

    - add getCS with parent::
	
	/** Return the index of cs that was added into parent with key.
	 */
	int getCS(int parent, Object key);

    - add getParent and isAncestor::

	int getParent(int cs);

	/** Whether calling getParent(cs) recursively
	 * (at least one time!) would eventually
	 * return parent.
	 */
	boolean isAncestor(int cs, int parent);

    - Document that getCS returns -1 when no such cs exists.

Taken together, these changes make VobMatcher into a fully queriable
implementation of a tree data structure with hash keys at each level of the tree.
