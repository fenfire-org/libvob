include rules.mk

SUBDIRS = src/trans src/util src/texture src/paper src/lines src/os src/main src/geom src/stats 
# src/jwrapper off for now
# ctest off for now

RAWSRC = `find org/ -name "*.java"` 

RJSRC := $(shell find org/ -name "*.rj")

GENSRC := $(RJSRC:%.rj=%.java)

.PHONY: all subdirs $(SUBDIRS)

all: subdirs generate java jni

CLASSDIR=CLASSES/

%.java: %.rj
	python metacode/rj2java.py $*.rj $*.java

java: $(GENSRC)
	mkdir -p CLASSES
	$(JAVAC) $(DEBUG) -d $(CLASSDIR) $(RAWSRC) 

jar: java
	jar cf libvob.jar -C CLASSES org

ctest: src/trans

generate: src/os src/texture src/util src/trans src/main
	make -C src/jni -f Makefile-Gen 

jni: src/jni java
	make -C src/jni -f Makefile-Gen javahs
	make -C src/jni jnilib

jniq: # Just compile jni quickly, no java compilation or gen.
	make -C src/jni jnilib

subdirs: $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@

tags::
	ctags -R

DEPENDS = ../depends

CLASSPATH := $(CLASSDIR):../navidoc/CLASSES:$(DEPENDS)/jython.jar:$(DEPENDS)/png.jar:$(CLASSPATH):$(DEPENDS)/javolution.jar
export CLASSPATH

ifeq (,$(JYTHONPATH))
 JYTHONPATH=.:$(DEPENDS)/jythonlib.jar:$(DEPENDS)/pythonlib.jar
endif

JAVA ?= java -Xincgc -Xnoclassgc
# JAVA = src/jwrapper/build/jwrapper -Xincgc -Xnoclassgc

API?="gl"

ifeq (,$(JYTHON))
# python.verbose can be: "error", "warning", "message", "comment", "debug"
 JYTHON=$(JAVA) $(JVMFLAGS) -Dvob.api=$(API) -Dpython.cachedir=. -Dpython.path=$(JYTHONPATH) -Dpython.verbose=message $(EDITOR_OPTION) org.python.util.jython
endif

lobsample:
	$(LDLIB) $(JAVA) -cp $(CLASSPATH) $(DBG) org.nongnu.libvob.layout.unit.SampleLob

run_notebook:
	$(LDLIB) $(JAVA) -cp $(CLASSPATH) $(DBG) org.nongnu.libvob.demo.Notebook
run_puzzle:
	$(LDLIB) $(JAVA) -cp $(CLASSPATH) $(DBG) org.nongnu.libvob.demo.Puzzle

run_java:
	$(LDLIB) $(JAVA) -cp $(CLASSPATH) $(DBG) `echo $(CLASS) | sed 's/\//./g;'` $(ARGS)

run_lobdemo:
	$(LDLIB) $(JAVA) -cp $(CLASSPATH) $(DBG) org.nongnu.libvob.demo.LobDemo
run_lobdemo_trans:
	$(LDLIB) $(JAVA) -cp $(CLASSPATH) $(DBG) org.nongnu.libvob.demo.TranslationDemo

run_bossbuzzle:
	$(LDLIB) $(JAVA) -cp $(CLASSPATH) $(DBG) org.nongnu.libvob.demo.BossBuzzle

LOB_DOCS?= $(shell find org/nongnu/libvob/lob/doc/ -name '*.java' | sed 's/.java//')

generate_lob_docs:
	for file in $(LOB_DOCS); do echo $$file; $(LDLIB) $(JYTHON) -Dvob.windowsize="320x256" -Dvob.api="awt" $(DBG) rundemo.py  vob/putil/lob_printter.py  $$file  $(DEMO); done

# Have to use params for find for it not to descend into the {arch} dirs.
FINDSRC=[a-zA-Z0-9]*

clean:
	find $(FINDSRC) -name "*.gen.*"|xargs rm -f
	find $(FINDSRC) -name "*.dep" |xargs rm -f
	find $(FINDSRC) -name "*.bin" |xargs rm -f
	find $(FINDSRC) -name "*.generated.*"|xargs rm -f
	find $(FINDSRC) -name "*.o" |xargs rm -f
	find $(FINDSRC) -name "*.class" |xargs rm -f
	find $(FINDSRC) -name "*.transjniobj" |xargs rm -f
	find $(FINDSRC) -name "*.vobjniobj" |xargs rm -f
# remove files where "gen" or "dep" is in last suffix
	find $(FINDSRC) -regex ".+\.[^.]*gen[^.]*" |xargs rm -f
	find $(FINDSRC) -regex ".+\.[^.]*dep[^.]*" |xargs rm -f
	rm -f src/jni/Generator
	rm -f src/jni/made*
	rm -f src/jni/org_nongnu*
	rm -rf CLASSES
	rm -f $(GENSRC)


#  # Generate a random number suitable for use as a coordinate system
#  # id. Only 31 bits of randomness but should be ok
#  coordsysid::
#  	python -c 'import random; print random.randrange(2**31-1)'

GLLIB=LD_LIBRARY_PATH=/usr/lib:src/jni:${JAVAHOME}/jre/lib/i386:${JAVAHOME}/jre/lib/i386/client

runjython: 
	$(GLLIB) $(JYTHON) $(DBG)

TEST=.

test:: test-awt test-gl

test-awt::  # Use: make test TEST=test/gzz/vob/vobmatcher.test, to run a single test.
	$(GLLIB) $(JYTHON) test.py -Dvob.api=awt -f GL $(DBG) $(TEST)
test-gl::
	$(GLLIB) $(JYTHON) test.py -Dvob.api=gl -f AWT $(DBG) jni $(TEST)

testbugs-awt::
	$(GLLIB) $(JYTHON) test.py -f \* $(DBG) $(TEST)
testbugs-gl::
	$(GLLIB) $(JYTHON) test.py -Dvob.api=gl -f \* $(DBG) jni $(TEST)

bench-gl::
	$(GLLIB) $(JYTHON) runbench.py -Dvob.api=gl $(DBG) $(BENCH)

bench-awt::
	$(GLLIB) $(JYTHON) runbench.py -Dvob.api=awt $(DBG) $(BENCH)


rundemo::
	$(GLLIB) $(JYTHON) rundemo.py $(DBG) $(DEMO)

runusertest::
	$(GLLIB) $(JYTHON) runusertest.py $(DBG) $(TEST)

runsnaps::
	$(GLLIB) $(JYTHON) runsnaps.py $(DBG) $(SNAPS)

copyrighted::
	python ../fenfire/metacode/copyrighter.py Libvob

##########################################################################
# General documentation targets
docs:   docxx java-doc navidoc navilink

docxx:
	doc++  -a --before-group --before-class --no-java-graphs --trivial-graphs  -H -F -d doc/docxx -u `find . -name "*.hxx"`

DOCPKGS= -subpackages org
#DOCPKGS= org.nongnu.libvob\
#	 org.nongnu.libvob.impl\
#	 org.nongnu.libvob.impl.awt\
#	 org.nongnu.libvob.impl.gl\
#	 org.nongnu.libvob.util\
#	 org.nongnu.libvob.vobs\
#	 org.nongnu.libvob.buoy\
#	 org.nongnu.libvob.gl

JAVADOCOPTS=-use -version -author -windowtitle "Libvob Java API"
java-doc:
	find . -name '*.class' | xargs rm -f # Don't let javadoc see these
	rm -Rf doc/javadoc
	mkdir -p doc/javadoc
	javadoc $(JAVADOCOPTS) -d doc/javadoc -sourcepath . $(DOCPKGS)
##########################################################################
# Navidoc documentation targets
navidoc: # Compiles reST into HTML
	make -C "../navidoc/" html DBG="$(DBG)" RST="../libvob/doc/"

navilink: # Bi-directional linking using imagemaps
	make -C "../navidoc/" imagemap HTML="../libvob/doc/"

naviloop: # Compiles, links, loops
	make -C "../navidoc/" html-loop DBG="--imagemap $(DBG)" RST="../libvob/$(RST)"

peg: # Creates a new PEG
	make -C "../navidoc/" new-peg PEGDIR="../libvob/doc/pegboard"

pegs:   # Compiles only pegboard
	make -C "../navidoc/" html DBG="$(DBG)" RST="../libvob/doc/pegboard/"

html: # Compiles reST into HTML, directories are processed recursively
	make -C "../navidoc/" html DBG="$(DBG)" RST="../libvob/$(RST)"

html-loop: # Loop version for quick recompiling
	make -C "../navidoc/" html-loop DBG="$(DBG)" RST="../libvob/$(RST)"

latex: # Compiles reST into LaTeX, directories are processed recursively
	make -C "../navidoc/" latex DBG="$(DBG)" RST="../libvob/$(RST)"

latex-loop: # Loop version for quick recompiling
	make -C "../navidoc/" latex-loop DBG="$(DBG)" RST="../libvob/$(RST)"

