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

"""
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

    new @realtime-i LobList(LobList list) {
        public int getLobCount() { list.getLobCount(); }
        public Lob getLob(int index) {
            return KeyLob.newInstance(list.getLob(index));
        }
    }

The preprocessor will create an inner class and create a Javolution FACTORY
for that class etc.

@realtime extends a class, @realtime-i implements an interface
(and inherits from RealtimeObject).

We can not access final members of the enclosing method from the inner class,
though, that was too hard to program, that's why we explicitly have to say

    (LobList list)

in the 'new @realtime' clause.
"""

import sys, re

try:
    (_, infile, outfile) = sys.argv
except:
    print "usage: rj2java.py infile outfile"
    sys.exit(1)

f = open(infile)
src = f.read()
f.close()


# <currently unused>: regexes to parse @realtime in normal class definitions,
#                     like 'public static @realtime class Foo', instead of
#                     only in anonymous classes.
#
# modifiers = r'((public|protected|private|static)\s+)*'
# parents = r'(\s+(extends\s+\S*|implements\s+\S*)*'
# class_decl = r'%(modifiers)s@realtime\s+class\s+(\S*)%(parents)s\s*{'
# </currently unused>


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


new_realtime = re.compile(r'new\s*@realtime(-i)?\s*([^ \r\n\t\f\v\(\)]*)\s*\(([^\(\)]*)\)\s*\{')
variable = re.compile(r'\s*(\S*)\s*(\S*)\s*')

replacements = []

lastbrace = src.rfind('}')
code = src[:lastbrace]
epilog = src[lastbrace:]


n = 1
while True:
    match = new_realtime.search(code)

    if not match: break

    #print match.group(1), match.group(2), match.group(3)

    #print 'match <', code[match.start():match.end()], '>'

    start = match.start()
    end   = parse_balanced_parens(code, match.end())

    is_interface = match.group(1) != None
    interface = match.group(2)
    params_spec = match.group(3)
    parameters = params_spec.split(',')
    body = code[match.end() : end-1]

    impl = '_%s_%s' % ('_'.join(interface.split('.')), n)
    n += 1

    var = 'the_new' + impl

    if parameters == ['']: parameters = []

    fields = ';'.join(parameters) + ';'
    params = [variable.match(p).group(2) for p in parameters]

    params_list = ','.join(params)
    params_code = '\n'.join(['%s.%s = %s;' % (var,p,p) for p in params])

    repl = """new%(impl)s(%(params_list)s)""" % locals();
    code = code[:start] + repl + code[end:]

    if is_interface:
        extends = 'extends RealtimeObject implements %(interface)s' % locals()
    else:
        extends = 'extends %(interface)s' % locals()

    code += """
        private static class %(impl)s %(extends)s {

                private %(impl)s() {}

                %(fields)s

                %(body)s
        }

        private static final RealtimeObject.Factory %(impl)s_FACTORY =
            new RealtimeObject.Factory() {
                protected Object create() { return new %(impl)s(); }
            };

        private static %(impl)s new%(impl)s(%(params_spec)s) {
            %(impl)s %(var)s = (%(impl)s)%(impl)s_FACTORY.object();
            %(params_code)s
            return %(var)s;
        }
    """ % locals()

code = code + epilog

f = open(outfile, 'w')
f.write("""/* DO NOT EDIT THIS FILE. THIS FILE WAS GENERATED FROM %s,
 * EDIT THAT FILE INSTEAD!
 * All changes to this file will be lost.
 */""" % infile)
f.write(code)
f.close()
    
