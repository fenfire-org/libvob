# 
# Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
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
# 


# Some code to make it easier to deal with NVidia
# OpenGL extensions

dbg = 0

def combinercode(c):
    return (c.replace("CI0", "CombinerInputNV COMBINER0_NV").
		replace("CI1", "CombinerInputNV COMBINER1_NV").
		replace("CI2", "CombinerInputNV COMBINER2_NV").
		replace("CI3", "CombinerInputNV COMBINER3_NV").
		replace("CI4", "CombinerInputNV COMBINER4_NV").
		replace("CI5", "CombinerInputNV COMBINER5_NV").
		replace("CI6", "CombinerInputNV COMBINER6_NV").
		replace("CI7", "CombinerInputNV COMBINER7_NV").
		replace("CO0", "CombinerOutputNV COMBINER0_NV").
		replace("CO1", "CombinerOutputNV COMBINER1_NV").
		replace("CO2", "CombinerOutputNV COMBINER2_NV").
		replace("CO3", "CombinerOutputNV COMBINER3_NV").
		replace("CO4", "CombinerOutputNV COMBINER4_NV").
		replace("CO5", "CombinerOutputNV COMBINER5_NV").
		replace("CO6", "CombinerOutputNV COMBINER6_NV").
		replace("CO7", "CombinerOutputNV COMBINER7_NV").
		replace("FCI", "FinalCombinerInputNV").
		replace(" A ", " VARIABLE_A_NV ").
		replace(" B ", " VARIABLE_B_NV ").
		replace(" C ", " VARIABLE_C_NV ").
		replace(" D ", " VARIABLE_D_NV ").
		replace(" E ", " VARIABLE_E_NV ").
		replace(" F ", " VARIABLE_F_NV ").
		replace(" G ", " VARIABLE_G_NV ")
		)

# return the NV_register_combiner scale that matches x best
def combinerscale(x):
    if x < .75: return "SCALE_BY_ONE_HALF_NV"
    elif x < 1.5: return "NONE"
    elif x < 3: return "SCALE_BY_TWO_NV"
    return "SCALE_BY_FOUR_NV"


def matchop(str,i):
    def opchar(str, i):
        return i < len(str) and str[i] in [ "*", "/", "+", "-", ".", "|", "%" ]
    if opchar(str, i): return i + 1
    return 0

def matchid(str, i):
    def idchar(str, i):
        return i < len(str) and ("0" <= str[i] <= "9" or
                                 "A" <= str[i] <= "Z" or
                                 "a" <= str[i] <= "z" or
                                 str[i] == "_")
    if not idchar(str, i): return 0
    while idchar(str, i): i += 1
    return i

def matchnum(str, i):
    if (i < len(str) and "0" <= str[i] <= "9") or (
        i + 1 < len(str) and str[i] == "." and  "0" <= str[i + 1] <= "9"):
        while i < len(str) and (str[i] == "." or "0" <= str[i] <= "9"): i += 1
        return i
    return 0

def exptree(exp, i):
    if i == len(exp): return ([], i)

    if exp[i] in [" ", "\t"]:
        return exptree(exp, i + 1)
    elif exp[i] == "(":
        (subtree, pos) = exptree(exp, i + 1)
        car = ("sub", subtree)
        if pos == len(exp):
            print "WARNING: Missing closing parenthesis"
        else:
            pos += 1
    elif exp[i] == ")":
        return ([], i)
    else:
        pos = matchnum(exp, i)
        if pos:
            car = (float(exp[i:pos]),)
        else:
            pos = matchid(exp, i)
            if pos:
                car = ("id", exp[i:pos])
            else:
                pos = matchop(exp, i)

                if pos:
                    car = (exp[i:pos],)
                else:
                    return ([], i)
        
    (cdr,i) = exptree(exp, pos)

    return ([car] + cdr,i)


def printtree(tree, pre = ""):
    for node in tree:
        if node[0] == "sub":
            printtree(node[1], pre + "> ")
        else:
            if len(node) == 1: node = node[0]
            print pre + str(node)

def parsecomp(name):
    if name in ["a", "alpha", "ALPHA"]: return "ALPHA"
    if name in ["b", "blue", "BLUE"]: return "BLUE"
    if name in ["rgb", "color", "col", "RGB", "COLOR", "COL"]: return "RGB"
    return None

def parseregs(tree):
    regmap = {
        "COL0": "PRIMARY_COLOR_NV",
        "COL1": "SECONDARY_COLOR_NV",
        "TEX0": "TEXTURE0",
        "TEX1": "TEXTURE1",
        "TEX2": "TEXTURE2",
        "TEX3": "TEXTURE3",
        "SPARE0": "SPARE0_NV",
        "SPARE1": "SPARE1_NV",
        "FOG": "FOG",
        "CONST0": "CONSTANT_COLOR0_NV",
        "CONST1": "CONSTANT_COLOR1_NV",
        "ZERO": "ZERO",
        "DISCARD": "DISCARD_NV",
        "EF": "E_TIMES_F_NV",
        "SPARE0_PLUS_COL1" : "SPARE0_PLUS_SECONDARY_COLOR_NV",
        }

    i = 0
    while i < len(tree):
        node = tree[i]
        if node[0] == "sub":
            parseregs(node[1])
        else:
            if node[0] == "id" and regmap.has_key(node[1]):
                if (i + 2 < len(tree) and
                    tree[i+1] == (".",) and
                    tree[i+2][0] == "id" and
                    parsecomp(tree[i+2][1])):
                    tree[i:i+3] = [("reg", regmap[node[1]],
                                    parsecomp(tree[i+2][1]))]
                else:
                    tree[i:i+1] = [("reg", regmap[node[1]], "RGB")]
        i += 1

def parsekey(tree):
    # Return list of node types
    return tuple(map(lambda x: x[0], tree))
    
def findregs(tree, type = "reg"):
    def func(x, type=type): return x[0] == type
    return filter(func, tree)

def parseinput(tree, final = 0):
    if final:
        inputmap = {
            ("reg",): "UNSIGNED_IDENTITY_NV",
            (1, "-", "reg"): "UNSIGNED_INVERT_NV",
            (1,): "UNSIGNED_INVERT_NV",
            (0,): "UNSIGNED_IDENTITY_NV",
            }
    else:
        inputmap = {
            ("reg",): "UNSIGNED_IDENTITY_NV",
            (1, "-", "reg"): "UNSIGNED_INVERT_NV",
            ("+", "reg"): "SIGNED_IDENTITY_NV",
            ("-", "reg"): "SIGNED_NEGATE_NV",
            (2, "*", "reg", "-", 1): "EXPAND_NORMAL_NV",
            (1, "-", 2, "*", "reg"): "EXPAND_NEGATE_NV",
            ("reg", "-", .5): "HALF_BIAS_NORMAL_NV",
            (.5, "-", "reg"): "HALF_BIAS_NEGATE_NV",

            (0,): "UNSIGNED_IDENTITY_NV",
            (1,): "UNSIGNED_INVERT_NV",
            (.5,): "HALF_BIAS_NEGATE_NV",
            ("+", 0): "SIGNED_IDENTITY_NV",
            ("-", 0): "SIGNED_NEGATE_NV",
            ("-", 1): "EXPAND_NORMAL_NV",
            ("+", 1): "EXPAND_NEGATE_NV",
            ("+", .5): "HALF_BIAS_NEGATE_NV",
            ("-", .5): "HALF_BIAS_NORMAL_NV",
            }

    for i in range(0,len(tree)):
        if tree[i][0] == "sub":
            key = parsekey(tree[i][1])
            if inputmap.has_key(key):
                reg = (findregs(tree[i][1]) + [("reg", "ZERO", "ALPHA")])[0]
                tree[i] = ("in", reg[1], inputmap[key], reg[2])
            else:
                parseinput(tree[i][1], final)
        elif tree[i][0] == "reg":
            # Convert identity-mapped regs
            tree[i] = ("in", tree[i][1], inputmap["reg",], tree[i][2])

    return tree

def parseGeneralCombiner(outreg, tree):
    out = {
        "scale": "NONE",
        "bias": "NONE"
        }

    outputscale = {
        ("sub", "*", .5): "SCALE_BY_ONE_HALF_NV",
        ("sub", "/", 2): "SCALE_BY_ONE_HALF_NV",
        ("sub", "*", 1): "NONE",
        ("sub", "*", 2): "SCALE_BY_TWO_NV",
        ("sub", "*", 4): "SCALE_BY_FOUR_NV"
        }
    outputbias = {
        ("-", .5): "BIAS_BY_NEGATIVE_ONE_HALF_NV",
        ("-", 0): "NONE",
        }
    
    key = parsekey(tree)
    if outputscale.has_key(key):
        out["scale"] = outputscale[key]
        tree = tree[0][1]

    key = parsekey(tree[-2:])
    if outputbias.has_key(key):
        out["bias"] = outputbias[key]
        tree = tree[:-2]

    parseinput(tree)

    funcmap = {
        ("in", ".", "in"): ("AB", "TRUE"),
        ("in", "*", "in"): ("AB", "FALSE"),
        ("in",):           ("A",  "FALSE"),
        ("-", "in"):       ("A",  "FALSE", 1),

        ("in", "+", "in"):                       ("AC",   "FALSE"),
        ("in", "+", "in", "*", "in"):            ("ACD",  "FALSE"),
        ("in", "*", "in", "+", "in"):            ("ABC",  "FALSE"),
        ("in", "*", "in", "+", "in", "*", "in"): ("ABCD", "FALSE"),
        ("-", "in", "+", "in"):                  ("AC",   "FALSE", 1),
        ("-", "in", "+", "in", "*", "in"):       ("ACD",  "FALSE", 1),
        ("in", "-", "in"):                       ("AC",   "FALSE", 2),
        ("in", "*", "in", "-", "in"):            ("ABC",  "FALSE", 1),
        ("-", "in", "-", "in"):                  ("AC",   "FALSE", 3),
        ("in", "|", "in"):                       ("AC",   "TRUE"),
        ("in", "|", "in", "*", "in"):            ("ACD",  "TRUE"),
        ("in", "*", "in", "|", "in"):            ("ABC",  "TRUE"),
        ("in", "*", "in", "|", "in", "*", "in"): ("ABCD", "TRUE"),
        }

    key = parsekey(tree)
    if funcmap.has_key(key):
        func = funcmap[key]
        regs = findregs(tree, "in")
        neg = len(func) > 2 and func[2]

        if "C" in func[0]:
            regset = "ABCD"
            out["muxSum"] = func[1]
            out["sumOut"] = outreg
        else:
            regset = "AB"
            out["abDot"] = func[1]
            out["abOut"] = outreg

        for i in range(0,len(regs)):
            out[func[0][i]] = " ".join(regs[i][1:])

        for reg in regset:
            if reg not in func[0]:
                if neg & 1: out[reg] = "ZERO EXPAND_NORMAL_NV ALPHA"
                else: out[reg] = "ZERO UNSIGNED_INVERT_NV ALPHA"
                neg >>= 1
    else:
        print "ERROR: General combiner function", key, "not recognized", tree
        return None

    return out

def addStageComp(out, new):
    def remapAB(old):
        new = old.copy()
        new["C"] = new["A"]; del new["A"]; 
        new["D"] = new["B"]; del new["B"]; 
        new["cdDot"] = new["abDot"]; del new["abDot"]; 
        new["cdOut"] = new["abOut"]; del new["abOut"]; 
        return new

    for key in new.keys():
        if out.has_key(key) and (out[key] != new[key] or key.endswith("Out")):
            return new.has_key("abOut") and addStageComp(out, remapAB(new))
        
    out.update(new)
    return 1

# Output general combiner stage, and initialize for the next stage
def outputGeneralCombiner(stage):
    code = ""
    if not stage["RGB"] and not stage["ALPHA"]:
        for c in "CONSTANT_COLOR0_NV", "CONSTANT_COLOR1_NV":
            if stage.has_key(c):
                code += "CombinerParameterNV %s %s\n" % (c, stage[c])
                stage["globalConst"] = stage["num"]
                del stage[c]
        return code

    for c in "CONSTANT_COLOR0_NV", "CONSTANT_COLOR1_NV":
        if stage.has_key(c):
            code += ("CombinerStageParameterNV COMBINER%s_NV %s %s\n"
                     % (stage["num"], c, stage[c]))
            stage["perStageConst"] = 1
            del stage[c]
            
    for comp in "RGB", "ALPHA":
        #if stage[comp]:
            for var in "A", "B", "C", "D":
                if stage[comp].has_key(var):
                    code += (
                        "CombinerInputNV COMBINER%s_NV %s VARIABLE_%s_NV %s\n"
                        % (stage["num"], comp, var, stage[comp][var]))

            code += (
                "CombinerOutputNV COMBINER%s_NV %s %s %s %s %s %s %s %s %s\n"
                % (stage["num"], comp,
                   stage[comp].get("abOut", "DISCARD_NV"),
                   stage[comp].get("cdOut", "DISCARD_NV"),
                   stage[comp].get("sumOut", "DISCARD_NV"),
                   stage[comp].get("scale", "NONE"),
                   stage[comp].get("bias", "NONE"),
                   stage[comp].get("abDot", "FALSE"),
                   stage[comp].get("cdDot", "FALSE"),
                   stage[comp].get("muxSum", "FALSE"),
                   )
                )
            stage[comp] = {}

    stage["num"] += 1

    return code


def parseFinalCombiner(outreg, tree):
    tree = parseinput([("sub",tree)], final = 1)
    if tree[0][0] == "sub": tree = tree[0][1]

    key = parsekey(tree)

    if outreg == "EF":
        if key != ("in", "*", "in"):
            print "ERROR: EF must be a product, got", key
            return ""
        return ("FinalCombinerInputNV VARIABLE_E_NV " + 
                " ".join(tree[0][1:]) + "\n" +
                "FinalCombinerInputNV VARIABLE_F_NV " + 
                " ".join(tree[2][1:]) + "\n")
    elif outreg == "ALPHA":
        if key != ("in",):
            print "ERROR: alpha must be an input mapped register, got", key
            return ""
        return ("FinalCombinerInputNV VARIABLE_G_NV " + 
                " ".join(tree[0][1:]) + "\n")
    else:
        funcmap = {
            ("in", "*", "in", "+", "in", "*", "in", "+", "in"): "ABACD",
            ("in", "*", "in", "+", "in", "*", "in"): "ABAC",
            ("in", "*", "in", "+", "in"): "ABD",
            ("in", "+", "in"): "CD",
            ("in", "*", "in"): "AB",
            ("in",): "D",
            }
        if funcmap.has_key(key):
            func = funcmap[key]
            regs = findregs(tree, "in")

            code = ""
            for i in range(0,len(regs)):
                if i > 0 and func[i] == "A":
                    if (regs[i][1] != regs[0][1] or
                        regs[i][2] == regs[0][2] or
                        regs[i][3] != regs[0][3]):
                        print ("ERROR: inconsistent A variables:",
                               regs[0], regs[i])
                        return ""
                else:
                    code += ("FinalCombinerInputNV VARIABLE_%s_NV %s\n"
                             % (func[i], " ".join(regs[i][1:])))

            for reg in "ABCD":
                if reg not in func:
                    code += ("FinalCombinerInputNV VARIABLE_%s_NV "
                             "ZERO UNSIGNED_IDENTITY_NV ALPHA\n" % reg)

            return code
        else:
            print "ERROR: Final combiner function", key, "not recognized:"
            return ""

"""
REGNAME = COL0 | COL1
        | TEX0 | TEX1 | TEX2 | TEX3
        | SPARE0 | SPARE1
        | FOG
        | CONST0 | CONST1
        | ZERO | DISCARD 
	| EF | SPARE0_PLUS_COL1

ALPHA_COMP = a | alpha | ALPHA
BLUE_COMP = b | blue | BLUE
RGB_COMP = rgb | col | color | RGB | COL | COLOR

REG = REGNAME
    | REGNAME . ALPHA_COMP
    | REGNAME . BLUE_COMP
    | REGNAME . RGB_COMP

IN = REG
   | (REG)
   | (1 - REG)
   | (+REG)
   | (-REG)
   | (2 * REG - 1)
   | (1 - 2 * REG)
   | (REG - .5)
   | (.5 - REG)
   | (0) | (.5) | (1) | (-1) | (-.5) | (-0) | (+0) | (+.5) | (+1)

EXP = IN . IN 
    | IN
    | IN * IN
    | IN + IN
    | IN + IN * IN
    | IN * IN + IN
    | IN * IN + IN * IN
    | IN - IN
    | -IN + IN
    | -IN - IN
    | IN * IN - IN 
    | -IN + IN * IN
    | IN '|' IN
    | IN '|' IN * IN
    | IN * IN '|' IN
    | IN * IN '|' IN * IN

BIASED_EXP = EXP
	   | EXP - .5
	   | EXP - 0

OUT = BIASED_EXP
    | (BIASED_EXP) / 2
    | (BIASED_EXP) * .5
    | (BIASED_EXP) * 1
    | (BIASED_EXP) * 2
    | (BIASED_EXP) * 4
	   
FINAL_IN = REG
          | (1 - REG)
          | (0) | (1)

FINAL_IN1 = FINAL_IN 
          | 1 - REG
          | 0 | 1

FINAL_OUT = FINAL_IN * FINAL_IN + FINAL_IN * FINAL_IN + FINAL_IN
          | FINAL_IN * FINAL_IN + FINAL_IN * FINAL_IN
          | FINAL_IN * FINAL_IN + FINAL_IN
          | FINAL_IN + FINAL_IN
          | FINAL_IN * FINAL_IN
          | FINAL_IN1

FUNC = REG '=' OUT
     | RGB_COMP '=' FINAL_OUT
     | ALPHA_COMP '=' FINAL_IN1
     | EF '=' FINAL_IN * FINAL_IN
     | CONST0 '=' vector
     | CONST1 '=' vector
"""
# Parse register combiner code from lines containing a '=' character.
# The syntax is given by FUNC above.
# Code from lines not containing a '=' is passed through.
#
#   str     The code lines separated by '\n' or ';'. '#'-comments are ignored
#   returns The generated code
#
# NUM_GENERAL_COMBINERS_NV and PER_STAGE_CONSTANTS_NV are automatically set.
#
# Semantics:
#   - A new combiner stage is started (unless empty) for all lines except for 
#     those adjacent "REG = OUT" assignments that can fit in the current stage.
#     To force a new stage, just insert a blank line.
#   - Per stage constants are enabled if any general combiner
#     block (separated by empty lines) contains constant assignements.
#     Put global constants into a separate block or into the final
#     combiner block.
#   - Only syntax is verified; Semantic errors may not be detected until
#     the code is executed
#
# Example:
#   CONST0 = .2 .3 .4 .5
#   SPARE0 = (COL0 . COL1 - .5) * 2
#   SPARE1 = (CONST0 * (1 - COL0) - .5) * 2
#   SPARE0.alpha = (1 - 2 * COL0.alpha) + COL1.blue
#   alpha = 1-SPARE0
#   EF = COL0 * (1 - COL1.alpha)
#   color = EF * FOG + (1-EF) * SPARE1
#
def parseCombiner(str):
    def splitLine(line):
        pos = line.find("#")
        if pos >= 0:
            if line[:pos].strip():
                return line[:pos].split(";") + [ line[pos:] ]
            return [ line ]
        return line.split(";")
       
    lines = reduce(lambda x,y: x+y, map(splitLine, str.splitlines()))
    
    code = ""
    
    stage = {
        "RGB": {},
        "ALPHA": {},
        "num": 0,
        }

    for line in lines:
        if line.lstrip().startswith("#"):
            code += line + "\n"
            continue
            
        pos = line.find("=")
        if pos == -1:
            code += outputGeneralCombiner(stage)
            code += line + "\n"
            continue

        (left, lend) = exptree(line, 0)
        (right, rend) = exptree(line, pos + 1)
        if lend < pos:
            print "ERROR: extra input:", line[lend:pos]
            return ""
        if rend < len(line):
            print "ERROR: extra input:", line[rend:]
            return ""


        parseregs(right)

        outreg = None
        if len(left) == 1 and left[0][0] == "id":
            outreg = parsecomp(left[0][1]) or left[0][1]

        if outreg in ("EF", "RGB", "ALPHA"):
            code += outputGeneralCombiner(stage)
            str = parseFinalCombiner(outreg, right)
            if not str: return ""
            code += "# " + line + "\n"
            code += str
        else:
            parseregs(left)
            if len(left) == 1 and left[0][0] == "reg":
                outreg = left[0][1]
                outcomp = left[0][2]

                if outreg.startswith("CONST"):
                    if stage.has_key(outreg):
                        print "WARING: a new stage implied by repeated", outreg
                        code += outputGeneralCombiner(stage)
                    stage[outreg] = line[pos+1:]
                else:
                    out = parseGeneralCombiner(outreg, right)
                    if not out: return ""

                    if outcomp == "BLUE":
                        print "ERROR: cannot write only BLUE in", line
                        return ""

                    if not addStageComp(stage[outcomp], out):
                        print "WARNING: block split to multiple stages at", line
                        code += outputGeneralCombiner(stage)
                        stage[outcomp] = out

                code += "# " + line + "\n"
            else:
                print "ERROR: left side must be register:", line
                return ""

    if stage["num"] == 0 and not (stage["RGB"] or stage["ALPHA"]):
        # Number of general combiners cannot be zero;
        # add dummy state to force output
        stage["RGB"]["foobar"] = 1
        code += "# Dummy combiner stage:\n"
        
    code += outputGeneralCombiner(stage)

    init = ""
    if stage.has_key("perStageConst"):
        init += "Enable PER_STAGE_CONSTANTS_NV\n"

    init += "CombinerParameterNV NUM_GENERAL_COMBINERS_NV %s\n" % stage["num"]
        
    return init + code




# Fragment program emulation of register combiners
# Works on raw CallGL code (i.e., on the ouput of parseCombiner).
#
# Usage:
#
#    code = <CallGL code using register combiners>
#    foo = fpCombiner()
#    code = foo.parseCode(code)
#    --> combiner stuff is removed from the code except for
#        CombinerParameter's, which are converted into ProgramEnvParameter's
#        (program.env indicies 0, 1 and 2..17 for the per-stage constant)
#    fp = GL.Program(foo.getFragmentProgram())
#    --> bind and enable the returned program to emulate the combiners
#
# See convCombiner below for a simpler interface.
#
# ISSUES:
#
# - How should the fragment program be attached to the corresponding
#   CallGL code? (convCombiner below creates a hash table of all encountered
#   programs)
#
# - Creating a new fragment program is much more expensive than
#   modulating some register combiner options. In libpaper,
#   a large amount of different programs is needed, because
#   the discrete scaling options are modulated, too.
#
# - Should we try to create a single fragment program that could
#   emulate all the "combiner programs"? (Seems impossible
#   withouth indirect register accesses)
#
# Implementaion details:
#
# - only 2D textures can be accessed (no way to know which type is enabled)
# - fog fraction is not implemented (requires unknown state)
# - mux operation not yet implemented
#
class fpCombiner:
    def __init__(self):
        
        self.finalInput = {}
        self.generalInput = [{ "ALPHA" : {}, "RGB" : {} } for i in range(0,8)]
        self.generalOutput = [{} for i in range(0,8)]
        
        self.initmap = {
            "TEXTURE0": "TEMP TEXTURE0; TXP TEXTURE0, fragment.texcoord[0], texture[0], 2D;",
            "TEXTURE1": "TEMP TEXTURE1; TXP TEXTURE1, fragment.texcoord[1], texture[1], 2D;",
            "TEXTURE2": "TEMP TEXTURE2; TXP TEXTURE2, fragment.texcoord[2], texture[2], 2D;",
            "TEXTURE3": "TEMP TEXTURE3; TXP TEXTURE3, fragment.texcoord[3], texture[3], 2D;",
            "PRIMARY_COLOR_NV": "TEMP PRIMARY_COLOR_NV; MOV PRIMARY_COLOR_NV, fragment.color.primary;",
            "SECONDARY_COLOR_NV": "TEMP SECONDARY_COLOR_NV; MOV SECONDARY_COLOR_NV, fragment.color.secondary;",
            "FOG": "PARAM FOG = state.fog.color;",
            "SPARE0_NV": "TEMP SPARE0_NV; MOV SPARE0_NV.w, TEXTURE0;",
            "SPARE1_NV": "TEMP SPARE1_NV;",
            "ZERO": "PARAM ZERO = 0;",
            "E_TIMES_F_NV": "TEMP E_TIMES_F_NV; MUL E_TIMES_F_NV, VARIABLE_E_NV, VARIABLE_F_NV;",
            "SPARE0_PLUS_SECONDARY_COLOR_NV": "TEMP SPARE0_PLUS_SECONDARY_COLOR_NV; ADD SPARE0_PLUS_SECONDARY_COLOR_NV, SPARE0, SECONDARY_COLOR_NV;",
            "result.color.w": "",
            }

        self.perStageConstants = 0


    def initParams(self):
        self.initializedRegs = {}

    def initreg(self, reg, output = 0):
        # Map constant colors to program.env[0..1,2..17]
        if reg in ("CONSTANT_COLOR0_NV", "CONSTANT_COLOR1_NV"):
            num = int(reg[14])
            if self.perStageConstants:
                num += 2 + 2 * self.stage

            return "program.env[%s]" % num
            
        if not self.initializedRegs.has_key(reg):
            if output and 0:
                # XXX: should track the need for initialization
                # for rgb and alpha components separately
                self.code += "TEMP %s;\n";
            else:
                if reg == "SPARE0_NV": self.initreg("TEXTURE0")
                self.code += self.initmap[reg] + "\n";

            self.initializedRegs[reg] = 1

        return reg

    def genInputMapping(self, var, reg, mapping, comp):
      
        inmap = {
            "UNSIGNED_IDENTITY_NV": "MOV_SAT TMP, %s;\n",
            "UNSIGNED_INVERT_NV":   "SUB_SAT TMP, 1, %s;\n",
            "SIGNED_IDENTITY_NV":   "MIN TMP, %s, 1; MAX TMP, TMP, -1;\n",
            "SIGNED_NEGATE_NV":     "MIN TMP, -%s, 1; MAX TMP, TMP, -1;\n",
            "EXPAND_NORMAL_NV":     "MOV_SAT TMP, %s; MAD TMP, 2, TMP, -1;\n",
            "EXPAND_NEGATE_NV":     "MOV_SAT TMP, %s; MAD TMP, 2, -TMP, 1;\n",
            "HALF_BIAS_NORMAL_NV":  "MOV_SAT TMP, %s; SUB TMP, TMP, .5;\n",
            "HALF_BIAS_NEGATE_NV":  "MOV_SAT TMP, %s; SUB TMP, .5, TMP;\n",
            }

        if (reg == "SPARE0_PLUS_SECONDARY_COLOR_NV" and
            self.finalInput.get("COLOR_SUM_CLAMP_NV", "0") in ("FALSE","0")):
            inmap["UNSIGNED_IDENTITY_NV"] = "MOV TMP, %s;\n"
        
        reg = self.initreg(reg)

        if comp == "BLUE": reg += ".z"
        if comp == "ALPHA": reg += ".w"

        self.code += inmap[mapping].replace("TMP", var) % reg;

    def genOutputMapping(self, reg, comp, tmp, scale, bias):
        self.initreg(reg, output = 1)

        if comp == "RGB": reg += ".xyz"
        if comp == "ALPHA": reg += ".w"

        outmap = {
            ("SCALE_BY_ONE_HALF_NV","NONE"): "MUL %s, .5, %s;\n",
            ("NONE","NONE"): "MOV %s, %s;\n",
            ("NONE","BIAS_BY_NEGATIVE_ONE_HALF_NV"): "SUB %s, %s, .5;\n",
            ("SCALE_BY_TWO_NV","NONE"): "MUL %s, %s, 2;\n",
            ("SCALE_BY_TWO_NV","BIAS_BY_NEGATIVE_ONE_HALF_NV"): "MAD %s, %s, 2, -1",
            ("SCALE_BY_FOUR_NV","NONE"): "MUL %s, %s, 4;\n",
            }
        self.code += outmap[(scale,bias)] % (reg, tmp);


    def genGeneralCombiner(self, comp, abOut, cdOut, sumOut, scale, bias, abDot, cdDot, muxSum):
        if abOut != "DISCARD_NV" or sumOut != "DISCARD_NV":
            if abDot not in ("FALSE", "0"):
                self.code += "DP3 AB, VARIABLE_A_NV, VARIABLE_B_NV;\n"
            else:
                self.code += "MUL AB, VARIABLE_A_NV, VARIABLE_B_NV;\n"
                
            if abOut != "DISCARD_NV":
                self.genOutputMapping(abOut, comp, "AB", scale, bias);
                    
        if cdOut != "DISCARD_NV" or sumOut != "DISCARD_NV":
            if cdDot not in ("FALSE", "0"):
                self.code += "DP3 CD, VARIABLE_C_NV, VARIABLE_D_NV;\n"
            else:
                self.code += "MUL CD, VARIABLE_C_NV, VARIABLE_D_NV;\n"

            if cdOut != "DISCARD_NV":
                self.genOutputMapping(cdOut, comp, "CD", scale, bias);

        if sumOut != "DISCARD_NV":
            if muxSum not in ("FALSE", "0"):
                # FIXME:
                print "ERROR: mux not supported yet!\n"
            else:
                self.code += "ADD SUM, AB, CD;\n"
                self.genOutputMapping(sumOut, comp, "SUM", scale, bias);
        self.code += "\n"

    def parseCode(self, code):
        out = []
        lines = code.split("\n")
        for line in lines:
            fields = line.split()
            if len(fields) == 0:
                out.append(line)
            # Convert constant parameters to program environment parameters:
            elif (fields[0] == "CombinerParameterNV" and
                fields[1] in ("CONSTANT_COLOR0_NV", "CONSTANT_COLOR1_NV")):
                var = fields[1][14]
                vec = fields[2:]
                out.append("ProgramEnvParameter FRAGMENT_PROGRAM_ARB " +
                           var + " " + " ".join(vec))
            elif fields[0] == "CombinerStageParameterNV":
                num = int(fields[1][8])
                var = str(2 + 2 * num + int(fields[2][14]))
                vec = fields[3:]
                out.append("ProgramEnvParameter FRAGMENT_PROGRAM_ARB " +
                           var + " " + " ".join(vec))
            elif (fields[0] in ("Enable","Disable") and
                     fields[1] == "REGISTER_COMBINERS_NV"):
                out.append(fields[0] + " FRAGMENT_PROGRAM_ARB")
            # Interpret and remove combiner specification:
            elif fields[0] in ("FinalCombinerInputNV",
                               "CombinerParameterNV"):
                var = fields[1]
                if len(fields[2:]) > 1:
                    self.finalInput[var] = fields[2:]
                else:
                    self.finalInput[var] = fields[2]
            elif fields[0] == "CombinerInputNV":
                num = int(fields[1][8])
                comp = fields[2]
                var = fields[3]
                self.generalInput[num][comp][var] = fields[4:] 
            elif fields[0] == "CombinerOutputNV":
                num = int(fields[1][8])
                comp = fields[2]
                self.generalOutput[num][comp] = fields[3:]
            elif (fields[0] in ("Enable","Disable") and
                     fields[1] == "PER_STAGE_CONSTANTS_NV"):
                self.perStageConstants = fields[0] == "Enable"
            # Pass through everything else:
            else:
                out.append(line)

        return "\n".join(out)

    def getFragmentProgram(self):
        self.code = """!!ARBfp1.0
        
        TEMP VARIABLE_A_NV, VARIABLE_B_NV, VARIABLE_C_NV, VARIABLE_D_NV;
        TEMP VARIABLE_E_NV, VARIABLE_F_NV;
        ALIAS AB = VARIABLE_A_NV;
        ALIAS CD = VARIABLE_C_NV;
        ALIAS SUM = AB;\n\n"""

        self.initParams()

        combiners = int(self.finalInput["NUM_GENERAL_COMBINERS_NV"])
        for num in range(0, combiners):
            self.stage = num
            for comp in "RGB", "ALPHA":
                if self.generalOutput[num].has_key(comp):
                    vars = self.generalInput[num][comp]
                    for var in ("VARIABLE_A_NV",
                                "VARIABLE_B_NV",
                                "VARIABLE_C_NV",
                                "VARIABLE_D_NV",):
                        if vars.has_key(var):
                            self.genInputMapping(var, *vars[var])

                    self.genGeneralCombiner(comp, *self.generalOutput[num][comp])

        self.stage = -1

        # E and F need to be first for the proper initialization order
        vars = self.finalInput
        for var in ("VARIABLE_E_NV",
                    "VARIABLE_F_NV",
                    "VARIABLE_A_NV",
                    "VARIABLE_B_NV",
                    "VARIABLE_C_NV",
                    "VARIABLE_D_NV",):
            if vars.has_key(var):
                self.genInputMapping(var, *vars[var])

        self.code += """
        LRP VARIABLE_A_NV, VARIABLE_A_NV, VARIABLE_B_NV, VARIABLE_C_NV;
        ADD_SAT result.color.xyz, VARIABLE_A_NV, VARIABLE_D_NV;
        """
        self.genInputMapping("result.color.w", *vars["VARIABLE_G_NV"])

        self.code += "END"
        return self.code
        

fpcodes = {}
def convCombiner(code, GL):
    """Convert register combiners into fragment programs in CallGL code.
    The generated fragment programs are cached in a table.
    """

    #print "Code: ", code

    foo = fpCombiner()
    code = foo.parseCode(code)

    #print "Converted code: ", code

    fpcode = foo.getFragmentProgram()

    #print "Fragment program: ", fpcode
    

    if fpcodes.has_key(fpcode):
        fp = fpcodes[fpcode]
    else:
        fp = GL.createProgram(fpcode)
        if dbg:
            print "Creating fragment program %s" % fp.getProgId()
        fpcodes[fpcode] = fp

    return """
    BindProgramARB FRAGMENT_PROGRAM_ARB %s
    """ % fp.getProgId() + code
