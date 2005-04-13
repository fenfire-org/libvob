/*   
BossBuzzle.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein.
 *                  2005, Matti J. Katila
 *
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
 *
 */
/*
 * Written by Benja Fallenstein and Matti J. Katila
 */
package org.nongnu.libvob.demo;
import org.nongnu.libvob.*;
import org.nongnu.libvob.fn.*;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.lob.lobs.*;
import org.nongnu.libvob.impl.NewLobMain;
import javolution.realtime.*;
import javolution.util.*;
import java.awt.Color;
import java.util.*;

public class BossBuzzle extends NewLobMain {

    private class Block {
	String id;
	int row, col;
	Block(int col, int row, String id) {
	    this.col = col; this.row = row; this.id = id;
	}
	boolean is(int col, int row) { return col == this.col && 
					   row == this.row; }
    }

    private class BossTable extends RealtimeObject implements TableLob.Table {
	
	Block empty;
	List blocks = new ArrayList();
	int rows, cols;
	public BossTable(int cols, int rows) {
	    this.cols = cols;
	    this.rows = rows;
	    
	    int id = 1;
	    for (int i=0; i<cols; i++)
		for (int j=0; j<rows; j++) {
		    if (id == rows*cols) break;
		    blocks.add(new Block(j,i, ""+id++));
		}
	    empty = new Block(cols-1, rows-1, "empty");
	}

	public int getRowCount() { return rows; }
	public int getColumnCount() { return cols; }
	
	public Lob getLob(int row, int col) {
	    for (int i=0; i<blocks.size(); i++) {
		final Block block = (Block) blocks.get(i);
		if (!(block.is(col, row))) continue;
		
		LobFont font = SimpleLobFont.newInstance("serif", 
							 0, 36, Color.blue);
		Lob lob = Lobs.hbox(font.text(block.id));
		
		lob = Lobs.align(lob, .5f, .5f);
		lob = Lobs.clickController(lob, 1, new Action() {
			public void run() {
			    int x = block.row, y = block.col;
			    block.row = empty.row; block.col = empty.col;
			    empty.row = x; empty.col = y;
			}
			public javolution.lang.Text toText() { return null; }
			public boolean move(javolution.realtime.Realtime.ObjectSpace o) { return false; }
		    });
		lob = Lobs.frame3d(lob, null, Color.red, 3, 5, false, true);
		lob = Lobs.request(lob, 100, 100, 100, 100, 100, 100);
		lob = Lobs.key(lob, block.id);
		lob = Lobs.align(lob, .5f, .5f);
		return lob;
	    }
	 
	    Lob lob = Lobs.filledRect(Color.black);
	    lob = Lobs.key(lob, empty.id);
	    return lob;
	}
    }

    private BossTable table = new BossTable(4,4);

    public BossBuzzle(Color bg) {
	super(bg);
    }

    public Lob createLob() {

        Lob lob = TableLob.newInstance(table);
        //lob = Lobs.request(lob, 500, 500, 500, 400, 500, 500);
        lob = Lobs.align(lob, .5f, .5f);
        lob = Lobs.key(lob, "table");
	return lob;

    }

    public static void main(String[] argv) {
	BossBuzzle demo = new BossBuzzle(new Color(1, 1, .8f));
	demo.start();
    }
}
