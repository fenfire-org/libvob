/*
MemoryPartitioner.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.memory;
import org.nongnu.libvob.AbstractUpdateManager;

import java.util.*;


/** A static API which divides the available memory between
 * MemoryConsumers.
 * Note that this is only one possible implementation.
 */
public class MemoryPartitioner {
    public static boolean dbg = false;
    static final void pa(String s) { System.out.println(s); }

    /** The amount of memory reserved to MemoryConsumers.
     * Let's start with 64MB
     */
    public int memory = 64 * 1024 * 1024;


    Stack currentScene = new Stack();
    {
	currentScene.push(null);
    }

    public void start(Object id) {
	currentScene.push(id);
    }
    public void end(Object id) {
	currentScene.pop();
    }

    static class ConsumerRecord {
	/** The current maximum importance.
	 * May be set to -1 at any time by the background thread.
	 */
	float maxImportance = -1;
	float maxQuality = -1;

	/** The filtered importance.
	 */
	float curImportance = -1;
	float curQuality = -1;

	/** The number of bytes requested from the consumer.
	 */
	int setBytes = 0;
	float setQuality = -1;

	/** The number of bytes the consumer reports using currently.
	 */
	int gotBytes = 0;
	float gotQuality = -1;


	/** Update curImportance and curQuality towards the observed
	 * values.
	 */
	void bgUpdate() {
	    float i = maxImportance;
	    maxImportance = -1;
	    float q = maxQuality;
	    maxQuality = -1;

	    if(i < 0) {
		curImportance *= .98;
	    } else {
		if(i >= curImportance)
		    curImportance = i;
		else {
		    curImportance -= .02 * (curImportance - i);
		}
	    }
	    // We won't decay: will always remain an interpolate
	    if(q >= 0) {
		if(q >= curQuality)
		    curQuality = q;
		else
		    curQuality -= .02 * (curQuality - q);
	    }
	    if(dbg) pa("Bgupdate: "+i+" "+q+" "+curImportance+" "+curQuality);
	}

	void update(float importance, float quality) {
	    if(importance > maxImportance)
		maxImportance = importance;
	    if(quality > maxQuality)
		maxQuality = quality;
	}

	void fit(MemoryConsumer cons, float priority, int bytes) {
	    if(dbg) pa("Reserving "+bytes+" for "+cons);
	    cons.setReservation(priority, bytes, curQuality);
	    if(dbg) pa("Reserved");
	    setBytes = bytes;
	    setQuality = curQuality;
	}

	int maximum(MemoryConsumer cons, float priority, int left) {
	    int m = cons.getMaxBytes(curQuality);
	    cons.setReservation(priority, m, curQuality);
	    setBytes = m;
	    setQuality = curQuality;
	    return left-m;
	}

    }

    Map consumer2record = Collections.synchronizedMap(
					new WeakHashMap());

    /** Indicate that a request for the data of the given consumer was made.
     * @param consumer The consumer whose data was requested.
     * @param importance How central the consumer was [in the view]. 1 = the focus, 
     * 			0 = invisible, peripheral
     * @param quality An abstract value to be passed to 
     * 				{@link MemoryConsumer#setReservation}. For example,
     * 				this could be the DPI resolution of an image.
     * 				0 = no quality at all, negative values
     * 				not allowed.
     * @see MemoryConsumer
     */
    public void request(MemoryConsumer consumer, float importance, float quality) {
	ConsumerRecord rec = (ConsumerRecord)consumer2record.get(consumer);
	if(rec == null) {
	    rec = new ConsumerRecord();
	    consumer2record.put(consumer, rec);
	}
	rec.update(importance, quality);

    }

    boolean keepRepartitioner = true;
    Thread repartitioner = new Thread() {
	public void run() {
	    while(true) {
		if(!keepRepartitioner) return;
		try {
		    if(dbg) pa("Going to rerate");
		    rerate();
		    // AbstractUpdateManager.chg();
		    if(dbg) pa("rerated, sleeping");
		    Thread.sleep(250);
		} catch(ConcurrentModificationException e) { 
		} catch(Exception e) { 
		    pa("gzz.mem.MemoryPartitioner thread!"+e);
		    e.printStackTrace();
		}
	    }
	}
    };

    /** Reallocate all memory.
     */
    private void rerate() {
	// Maximum importance found
	float maximportance = 0;
	// Sum of maxbytes with priority 1
	int sum1 = 0;
	for(Iterator i = consumer2record.keySet().iterator(); i.hasNext(); ) {
	    MemoryConsumer cons = (MemoryConsumer)i.next();
	    ConsumerRecord rec = (ConsumerRecord)consumer2record.get(cons);
	    rec.bgUpdate();
	    if(rec.curImportance > maximportance) maximportance = rec.curImportance;
	    if(rec.curImportance == 1)
		sum1 += cons.getMaxBytes(rec.curQuality);
	}
	if(sum1 > memory) {
	    // Problem! Not enough memory for all 1-importances
	    // - fill from start as many as we can
	    int left = memory;
	    for(Iterator i = consumer2record.keySet().iterator(); i.hasNext(); ) {
		MemoryConsumer cons = (MemoryConsumer)i.next();
		ConsumerRecord rec = (ConsumerRecord)consumer2record.get(cons);
		if(rec.curImportance == 1) 
		    left = rec.maximum(cons, 0, left);
		else 
		    rec.fit(cons, 0, 0);
	    }
	    return;
	}
	if(maximportance <= 0) return;
	// Else, find a good exponent
	for(float round = 0; ; round+= .1) {
	    float mulf = (round > 5 ? .999f : 1);
	    // Sum of maxbytes multiplied with priority^round
	    int sumReduced = 0;
	    if(dbg) pa("Rerate round "+round);
	    for(Iterator i = consumer2record.keySet().iterator(); i.hasNext(); ) {
		MemoryConsumer cons = (MemoryConsumer)i.next();
		ConsumerRecord rec = (ConsumerRecord)consumer2record.get(cons);
		float imp = 0;
		if(rec.curImportance > 0)
		    imp = (float)Math.pow(mulf * rec.curImportance/maximportance, round);
		else if(round == 0)
		    imp = 1;
		int byt = cons.getMaxBytes(rec.curQuality);
		sumReduced += (int)(imp * byt);
	    }
	    if(dbg) pa("Counted: "+sum1+" "+sumReduced);
	    if(sumReduced <= memory) {
		// Hey, now it fits!
		//
		for(Iterator i = consumer2record.keySet().iterator(); i.hasNext(); ) {
		    MemoryConsumer cons = (MemoryConsumer)i.next();
		    ConsumerRecord rec = (ConsumerRecord)consumer2record.get(cons);
		    float imp = 0;
		    if(rec.curImportance > 0)
			imp = (float)Math.pow(mulf * rec.curImportance/maximportance, round);
		    else if(round == 0)
			imp = 1;
		    int byt = cons.getMaxBytes(rec.curQuality);

		    float priority = 1-rec.curImportance;
		    byt = (int) (imp * byt);

		    if(byt < cons.getReservation())
			priority -= 1; // First free

		    rec.fit(cons, priority, (int)(imp * byt));
		}
		return;
	    }
	    // didn't fit - try with a larger exponent
	}
    }

    public MemoryPartitioner(int size) {
	memory = size;
	repartitioner.start();
    }

    public void stop() {
	keepRepartitioner = false;
    }
}

