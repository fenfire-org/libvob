/*
Debug.hxx
 *    
 *    Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Janne Kujala and Tuomas J. Lukka
 */

#ifndef __VOB_DEBUG_HXX__
#define __VOB_DEBUG_HXX__

#include <vector>

namespace Vob {

/** Handling debug variables.
 * This namespace (essentially a Singleton object) provides
 * a global namespace of integer variables, useful for
 * debugging.
 */
namespace Debug {

    extern std::ostream *debugStream();

/** A macro to get a reference to a debug variable.
 * Example: 
 * <pre>
    DBGVAR(dgb, "JNI.general");
 * </pre>
 * @param cname The name of the local reference variable to be created
 * by the invocation.
 */
#define DBGVAR(cname, name) const char *cname##___DBGNAME = name; int &cname = Debug::var(name)
#define STATICDBGVAR(cname, name) static const char *cname##___DBGNAME = name; static int &cname = Debug::var(name)
/** Predeclare an extern debug variable.
 * Useful for header files.
 * Example: 
 * <pre>
    PREDBGVAR(dgb);
 * </pre>
 */
#define PREDBGVAR(cname) extern const char *cname##___DBGNAME; extern int &cname
/** A macro create a physical debug variable and connect it
 * to the namespace.
 * <b>Do not use unless you know what you're doing.</b>
 * @param cname The name of the local variable to be created
 * by the invocation.
 */
#define DBGVAREXT(cname, name) int cname##__DBGDUMMY = Debug::extVar(name, &cname)

/** A macro that gives an output stream into which it has written
 * the name of the debug variable.
 * Example:
 * <pre>
    DBG(dbg) << "Help! The zombie grepped me with a pipe!\n";
 * </pre>
 * Note that the name printed is not the name of the C++ variable
 * but the name used in DBGVAR to define it, through a little magic.
 * <p>
 * Using this macro gives us the option, at a later date, to 
 * redirect different streams to different files.
 * It's also shorter than
 * <pre>
 * 	if(dbg) cout <<
 * </pre>
 */
#define DBG(cname) if(!cname);else ((*(::Vob::Debug::debugStream())) << cname##___DBGNAME << ": ")

  /** Get a variable reference by name.
   * If the name does not exist, a new name is created with an
   * address of a newly allocated variable having the default value 0.
   */
  int& var(const char *name);

  /** Set the address of the named variable.
   * If the name does not exist, a new name is created.
   * Any previous references to the name are invalidated 
   * (they point to the old address).
   * Note: any previous value is preserved 
   * Note: the old location is not freed 
   * @param name The name to set
   * @param var The pointer to set the variable to point to.
   */
  int& extVar(const char *name, int *var);

  /** Get a list of variable names. */
  std::vector<const char *> getVarNames();
}
}


#endif
