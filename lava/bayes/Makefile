ARCHOPTS=-e 's/^model name.*Pentium III.*$$/-march=pentium3/' \
         -e 's/^model name.*(Coppermine).*$$/-march=pentium3/' \
         -e 's/^model name.*Pentium(R) 4.*$$/-march=pentium4/' \
         -e 's/^model name.*AMD Athlon(tm) XP.*$$/-march=athlon-xp/' \
         -e 's/^model name.*AMD Athlon(tm) MP.*$$/-march=athlon-mp/' \
         -e 's/^model name.*AMD Athlon(tm).*$$/-march=athlon/'
ARCH=$(shell [ ! -f /proc/cpuinfo ] || sed $(ARCHOPTS) -e "/^-/!d" /proc/cpuinfo)
CFLAGS = -Wall -O3 -ffast-math $(ARCH) # -fstrict-aliasing
LDFLAGS = -lm 

datafiles = ,,psi.dat # ,,psi_points.dat ,,psi_models.dat

sources = psi_placement2.c bayes_funcs.c

binaries = psi_placement2 bayes.so

all: $(binaries) $(datafiles) 

$(datafiles): psi_model.py
	python -c "import psi_model; psi_model.sample()"

%.dep: %.c
	$(SHELL) -ec '$(CC) -M $(CPPFLAGS) $< \
                      | sed '\''s/\($*\)\.o[ :]*/\1.o $@ : /g'\'' > $@; \
                      [ -s $@ ] || rm -f $@'

include $(sources:.c=.dep)

psi_placement2: psi_placement2.o bayes_funcs.o

clean:
	rm -f *.o *.dep $(binaries) $(datafiles)

bayes.so: bayes.c bayes_funcs.o
	$(CC) -I /usr/include/python2.3 $(CFLAGS) -shared bayes.c bayes_funcs.o -o bayes.so


run:
	xterm -e python tcpserve.py 5984 python colortest.py &
	make -C ../.. runusertest TEST=vob.usertest.color10

run2:
	PYTHONPATH=../../vob xterm -e python tcpserve.py 5984 python colortest2.py &
	make -C ../.. runusertest TEST=vob.usertest.color11
