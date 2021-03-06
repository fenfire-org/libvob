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
    s = str.find('"', start)
    lc = str.find('//', start)
    mc = str.find('/*', start)

    if o < 0:  o = len(str)
    if c < 0:  c = len(str)
    if s < 0:  s = len(str)
    if lc < 0: lc = len(str)
    if mc < 0: mc = len(str)

    if o < c and o < s and o < lc and o < mc:
        in_parens = parse_balanced_parens(str, o+1)
        return parse_balanced_parens(str, in_parens)
    elif c < s and c < lc and c < mc:
        return c+1
    elif s < lc and s < mc:
        e = s+1
        while True:
            e = str.find('"', e)
            assert e > 0

            n = 0
            while str[e-n-1] == '\\':
                assert e-n-1 > 0
                n = n + 1

            if (n % 2) == 0: break
            print 'did not break'

        return parse_balanced_parens(str, e+1)
    elif lc < mc:
        e = str.find('\n', lc)
        return parse_balanced_parens(str, e)
    else:
        e = str.find('*/', mc+2)
        try:
            return parse_balanced_parens(str, e)
        except:
            print str[e:]
            assert 0



lastbrace = src.rfind('}')
code = src[:lastbrace]
epilog = src[lastbrace:]



# first, check whether the whole class is declared @realtime

rmodifiers = r'((?:public|protected|private|static)\s+)*'
rparents = r'((?:\s+(?:extends\s+\S*|implements\s+(?:\S*,\s*)*\S*))*)'
rclass_decl = (
    r'%(rmodifiers)s@realtime\s+%(rmodifiers)sclass\s+(\S*)%(rparents)s\s*{'
    % locals())

#print rclass_decl

match = re.compile(rclass_decl).search(src)
if match:
    modifiersA, modifiersB, classname, parents = match.groups()

    modifiers = (modifiersA or '') + (modifiersB or '')

    #print 'mod', modifiers
    #print 'class', classname
    #print 'parents', parents

    # do we have an extends? (else, need to extend RealtimeObject)

    has_extends = re.compile('extends\s+(\S*)').search(parents) != None
    #print 'has extends', has_extends

    if not has_extends:
        parents = 'extends RealtimeObject ' + parents

    # now, replace the class declaration

    code = code[:match.start()] + modifiers + ' class ' + classname + ' ' + \
           parents + ' { \n private ' + classname + '() {} ' + \
           code[match.end():]

    # the factory

    epilog = ('''
        private static Factory FACTORY = new Factory() {
        protected Object create() {
                return new %(classname)s();
            }
        };
    ''' % locals()) + epilog


    # now, need to find constructors...

    rconstructors = re.compile('@constructor\s*(\S*)\s*\(([^)]*)\)\s*{')

    while True:
        match = rconstructors.search(code)
        if match == None: break

        name, params = match.groups()

        if name.strip() == '': name = 'newInstance'

        start = match.start()
        end   = parse_balanced_parens(code, match.end())

        body = code[match.end() : end-1]

        body = re.sub('this', 'the_new_object', body)

        repl = '''
            static %(classname)s %(name)s(%(params)s) {
                %(classname)s the_new_object = (%(classname)s)FACTORY.object();
                %(body)s
                return the_new_object;
            }
        ''' % locals()

        code = code[:start] + repl + code[end:]

    


            

# now, check for @realtime anonymous inner classes

new_realtime = re.compile(r'new\s*@realtime(-i)?\s*([^ \r\n\t\f\v\(\)]*)\s*\(([^\(\)]*)\)\s*\{')
variable = re.compile(r'\s*(@set\s+)?(\S*)\s*(\S*)\s*')

replacements = []


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
    parameters = match.group(3).split(',')
    body = code[match.end() : end-1]

    impl = '_%s_%s' % ('_'.join(interface.split('.')), n)
    n += 1

    var = 'the_new' + impl

    if parameters == ['']: parameters = []

    fields = ';'.join([p for p in parameters if not p.strip().startswith('@set')]) + ';'

    params_spec = ', '.join([match.group(2) + ' ' + match.group(3)
                             for match in
                             [variable.match(p) for p in parameters]])

    params = [variable.match(p).group(3) for p in parameters]
    params_list = ','.join(params)
    params_code = '\n'.join(['%s.%s = %s;' % (var,p,p) for p in params])

    repl = """new%(impl)s(%(params_list)s)""" % locals();
    code = code[:start] + repl + code[end:]

    if is_interface:
        extends = 'extends RealtimeObject implements %(interface)s' % locals()
    else:
        extends = 'extends %(interface)s' % locals()

    objectSpaceMoves = ''
    
    for pstr in parameters:
        xm = variable.match(pstr)
        xclazz = xm.group(2)
        xvar = xm.group(3)
        if xclazz in ('int', 'float', 'double', 'byte', 'boolean',
                      'short', 'char', 'long'): continue

        # we first cast to Object because otherwise this generates
        # a compiler error for final classes not implementing Realtime
        # ("an instance of xyz cannot possibly be an instance of Realtime")
        objectSpaceMoves += \
            'if(((Object)%s) instanceof Realtime) ((Realtime)((Object)%s)).move(os); ' % (xvar,xvar)

    code += """
        private static class %(impl)s %(extends)s {

            private %(impl)s() {}

            %(fields)s

            %(body)s

            public boolean move(ObjectSpace os) {
                if(super.move(os)) {
                    %(objectSpaceMoves)s
                    return true;
                }
                return false;
            }
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
    
