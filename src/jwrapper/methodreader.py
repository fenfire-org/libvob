# 
# Copyright (c) 2003, Benja Fallenstein and Tuomas J. Lukka
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
A script to parse our native (JNI) method declarations
and generate some C code that creates a data structure
that is needed to register our methods, in a Java wrapper
that avoids a current problem with libc.

Uses simple heuristics in many places to get the
method names filtered out, avoiding occurrences of the
word 'native' that are not method declarations.
"""

import sys, string

# The modifiers a Java method can have
javaMethodModifiers = ['static', 'public', 'private', 'protected']

# Java VM type codes for built-in types
typeCode = {
    'boolean': 'Z',
    'byte': 'B',
    'char': 'C',
    'short': 'S',
    'int': 'I',
    'long': 'J',
    'float': 'F',
    'double': 'D',
    'void': 'V',
    }

# Fully qualified names for classes used in our JNI code
# If other classes are used in any native methods, add here
fullyQualifiedName = {
    'String': 'java/lang/String',
    'Object': 'java/lang/Object',
    'Rectangle': 'java/awt/Rectangle',
    'EventHandler': 'org/nongnu/libvob/gl/GL$EventHandler',
    }

def getTypeSignature(javaType):
    javaType = javaType.strip()
    typeSignature = ''

    while javaType.endswith('[]'):
        typeSignature += '['
        javaType = javaType[:-2].strip()

    try:
        typeSignature += typeCode[javaType]
    except KeyError:
        try:
            typeSignature += 'L' + fullyQualifiedName[javaType] + ';'
        except KeyError:
            print
            print "The fully qualified name of class <%s> has not" % javaType
            print "been entered into methodreader.py. To generate the"
            print "Java VM wrapper for working around a libc bug,"
            print "we need the fully qualified name for this class."
            print "Please enter it into the dictionary fullyQualifiedName"
            print "in file libvob/src/jwrapper.methodreader.py."
            print
            sys.exit(1)

    return typeSignature

def isMethodHeuristic(tokens, pos):
    # Only treat 'native' as a method declaration
    # if preceded or followed by a method modifier (static, etc)
    
    if tokens[pos-3] in javaMethodModifiers: return 1
    if tokens[pos-2] in javaMethodModifiers: return 1
    if tokens[pos] in javaMethodModifiers: return 1
    if tokens[pos+1] in javaMethodModifiers: return 1

    return 0

# Mapping from klass to mapping from methodName to lists of type signatures
# (I.e., mapping of mappings!)
# klass a fully qualified name like org/nongnu/libvob/gl/GL
methodDict = {}

for file in sys.argv[1:]:
    if not file.endswith('.java'):
        print 'Not a Java file??? -- %s' % file
        print 'Exiting methodreader.py.'
        sys.exit(1)

    klass = file[:-5]
    
    #print
    #print file, klass
    
    file = open(file)
    tokens = file.read().split()
    file.close()

    pos = 0
    while 1:
        try:
            pos = tokens.index('native', pos) + 1
        except ValueError:
            break

        if not isMethodHeuristic(tokens, pos): continue

        while tokens[pos] in javaMethodModifiers: pos = pos + 1

        methodTokens = []
        while 1:
            methodTokens.append(tokens[pos])
            if tokens[pos].endswith(';'): break
            pos = pos + 1

        signature = string.join(methodTokens)

        #print '    %s' % signature

        space = signature.index(' ')
        bracket1 = signature.index('(')
        bracket2 = signature.index(')')

        returnType = signature[:space]
        methodName = signature[space:bracket1].strip()
        paramList = signature[bracket1+1:bracket2].strip()

        typeSignature = '('

        if paramList:
            for param in paramList.split(','):
                param = param.strip()
                try:
                    i = param.rindex(' ')
                except ValueError:
                    i = param.rindex('[]') + 2

                typeSignature += getTypeSignature(param[:i])

        typeSignature += ')'
        typeSignature += getTypeSignature(returnType)


        if not methodDict.has_key(klass): methodDict[klass] = {}
        d = methodDict[klass]
        if not d.has_key(methodName): d[methodName] = []
        d[methodName].append(typeSignature)


def munge(s):
    """Munge names using underscores like JNI does"""

    res = ''
    for c in s:
        if   c == '/': res += '_'
        elif c == '_': res += '_1'
        elif c == ';': res += '_2'
        else:          res += c

    return res

print """
// GENERATED CODE -- DO NOT EDIT! EDIT methodreader.py INSTEAD.

#include <jni.h>
#include <stdlib.h>
#include "../../jni/org_nongnu_libvob_gl_GL.h"
#include "../../jni/org_nongnu_libvob_gl_GLRen.h"
#include "../../jni/org_nongnu_libvob_gl_Paper.h"
"""

for klass, subDict in methodDict.items():
    #print klass

    n = sum([len(v) for v in subDict.values()])
    i = 0

    print "JNINativeMethod methods_%s[%s] = {" % (munge(klass), n)

    for methodName, types in subDict.items():
        
        munged = 'Java_' + munge(klass) + '_' + munge(methodName)

        def comma(i, n):
            if i < n-1: return ','
            else:     return ''


        if len(types) == 1:
            print '    { "%s", "%s", (void *)%s }%s' % \
                  (methodName, types[0], munged, comma(i,n))
            i += 1
        else:
            for t in types:
                # only the part between '(' and ')' --
                # the *parameters* to the native method
                t = t[:t.index(')')][1:]
            
                print '    { "%s", "%s", (void *)%s }%s' % \
                      (methodName, t, munged + '__' + munge(t), comma(i,n))
                i += 1

    print "};"
    print


print """

void registerNativeMethods(JNIEnv *env) {
    jclass clazz;
"""

for klass, subDict in methodDict.items():
    n = sum([len(v) for v in subDict.values()])

    print '	clazz = env->FindClass("%s");' % klass
    print """
	if(clazz == 0) {
	    fprintf(stderr, "Couldn't find class %s to add methods to\\n");
	    exit(2);
	}
    """ % klass
    print '	env->RegisterNatives(clazz, methods_%s, %s);' % \
          (munge(klass), n)

print """

}
"""
