#!/usr/bin/python
#
# Copyright (c) 2005, Benja Fallenstein
#
# This file is part of Libvob.
# 
# Libvob is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Libvob is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Libvob; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
#

'''
A code preprocessor to make Javolution programming a bit easier.

With Javolution, a simple inner class like

    new LobList() {
        public int getLobCount() { list.getLobCount(); }
        public Lob getLob(int index) {
            return KeyLob.newInstance(list.getLob(index));
        }
    }

becomes a really long and complex thing, see KeyLobList for how the above
then looks.

I found this too hard in practice, so with this new preprocessor, this becomes

    new @realtime LobList(LobList list) {
        public int getLobCount() { list.getLobCount(); }
        public Lob getLob(int index) {
            return KeyLob.newInstance(list.getLob(index));
        }
    }

The preprocessor will create an inner class and create a Javolution FACTORY
for that class etc.

The thing after @realtime must be an interface, not a class (for now).

We can not access final members of the enclosing method from the inner class,
though, that was too hard to program, that's why we explicitly have to say

    (LobList list)

in the 'new @realtime' clause.
'''

import sys, re

try:
    (_, infile, outfile) = sys.argv
except:
    print "usage: rj2java.py infile outfile"
    sys.exit(1)

f = open(infile)
src = f.read()
f.close()


def parse_balanced_parens(str, start):
    o = str.find('{', start)
    c = str.find('}', start)
    lc = str.find('//', start)
    mc = str.find('/*', start)

    if o < 0:  o = len(str)
    if c < 0:  c = len(str)
    if lc < 0: lc = len(str)
    if mc < 0: mc = len(str)

    if o < c and o < lc and o < mc:
        in_parens = parse_balanced_parens(str, o+1)
        return parse_balanced_parens(str, in_parens)
    elif c < lc and c < mc:
        return c+1
    elif lc < mc:
        e = str.find('\n', lc)
        return parse_balanced_parens(str, e)
    else:
        e = str.find('*/', mc+2)
        return parse_balanced_parens(str, e)


new_realtime = re.compile(r'new\s*@realtime\s*([^ \r\n\t\f\v\(\)]*)\s*\(([^\(\)]*)\)\s*\{')
variable = re.compile(r'\s*(\S*)\s*(\S*)\s*')

replacements = []

n = 1
for match in new_realtime.finditer(src):
    #print match.group(1), match.group(2), match.group(3)

    #print 'match <', src[match.start():match.end()], '>'

    start = match.start()
    end   = parse_balanced_parens(src, match.end())

    interface = match.group(1)
    params_spec = match.group(2)
    parameters = params_spec.split(',')
    body = src[match.end() : end-1]

    fields = ';'.join(parameters) + ';'
    params = [variable.match(p).group(2) for p in parameters]

    params_list = ','.join(params)
    params_code = '\n'.join(['o.%s = %s;' % (p,p) for p in params])

    impl = '_%s_%s' % ('_'.join(interface.split('.')), n)
    n += 1

    code = '''
        private static class %(impl)s extends RealtimeObject
            implements %(interface)s {

                private %(impl)s() {}

                %(fields)s

                %(body)s
        }

        private static final Factory %(impl)s_FACTORY = new Factory() {
            protected Object object() { return new %(impl)s(); }
        };

        private static %(impl)s new%(impl)s(%(params_spec)s) {
            %(impl)s o = (%(impl)s)%(impl)s_FACTORY.object();
            %(params_code)s
            return o;
        }
    ''' % locals()

    repl = '''new%(impl)s(%(params_list)s)''' % locals();

    replacements.append((start, end, repl, code))

java = src
epilog = ''

replacements.reverse()

for (start, end, repl, code) in replacements:
    #print start, end
    #print 'repl <', java[start:end], '> with ', repl
    java = java[:start] + repl + java[end:]
    epilog += code

#print java

lastbrace = java.rfind('}')
java = java[:lastbrace] + epilog + java[lastbrace:]

f = open(outfile, 'w')
f.write('''/* DO NOT EDIT THIS FILE. THIS FILE WAS GENERATED FROM %s,
 * EDIT THAT FILE INSTEAD!
 * All changes to this file will be lost.
 */''' % infile)
f.write(java)
f.close()
    
