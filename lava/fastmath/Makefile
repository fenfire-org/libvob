ARCHOPTS=-e 's/^model name.*Pentium III.*$$/-march=pentium3/' \
         -e 's/^model name.*(Coppermine).*$$/-march=pentium3/' \
         -e 's/^model name.*Pentium(R) 4.*$$/-march=pentium4/' \
         -e 's/^model name.*AMD Athlon(tm) XP.*$$/-march=athlon-xp/' \
         -e 's/^model name.*AMD Athlon(tm) MP.*$$/-march=athlon-mp/' \
         -e 's/^model name.*AMD Athlon(tm).*$$/-march=athlon/'
ARCH=$(shell [ ! -f /proc/cpuinfo ] || sed $(ARCHOPTS) -e "/^-/!d" /proc/cpuinfo)
CFLAGS = -Wall -O3 -ffast-math $(ARCH)
LDFLAGS = -lm 

fastmath.so: fastmath.c 
	$(CC) -I /usr/include/python2.3 $(CFLAGS) -shared fastmath.c -o fastmath.so

