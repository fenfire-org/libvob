/*
Stats.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_STATS_STATS_HXX
#define VOB_STATS_STATS_HXX

namespace Vob {
namespace Stats {

    class Collector;

    class Statistics {
	Collector *first;
    public:
	Statistics();
	virtual ~Statistics();
	void triggered(Collector *c);
	void clear();
	void call(void *u);

	void toBeDeleted(Collector *c);
    };

    class Collector {
    private:
	friend class Statistics;

	Statistics *stats;
	bool onList;
	Collector *next;

    public:
	Collector(Statistics *stats);
	virtual ~Collector();

	virtual void clear() = 0;
	virtual void call(void *u) = 0;

	void gotStatistics();
    };

}
}










#endif
