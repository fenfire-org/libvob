/*   
LobDemo.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein.
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
 * Written by Benja Fallenstein
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

public class LobDemo extends NewLobMain {

    static Color[][] colors;
    static {
	colors = new Color[10][10];
	for(int row=0; row<10; row++)
	    for(int col=0; col<10; col++)
		colors[row][col] = new Color(50 + row*10, 0, 50 + col*10);
    }

    private class Table extends RealtimeObject implements TableLob.Table {
	public int getRowCount() { return 10; }
	public int getColumnCount() { return 10; }
	
	public Lob getLob(int row, int col) {
	    return Lobs.filledRect(colors[row][col]);
	}
    }

    private Table table = new Table();

    private Model text = SimpleModel.newInstance("Copyright (c) 2005, Benja Fallenstein\n\nThis file is part of Libvob.\n\nLibvob is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.\n\nLibvob is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License along with Libvob; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.");

    public LobDemo(Color bg) {
	super(bg);
    }

    public Lob createLob() {
        Tray tray = Tray.newInstance(Lists.list(), false);

        Lob lob = TableLob.newInstance(table);
        lob = Lobs.request(lob, 400, 400, 400, 300, 300, 300);
        lob = Lobs.translate(lob, 100, 100);
        lob = Lobs.key(lob, "table");
        tray.add(lob);

        lob = Lobs.hbox(Lobs.text(Components.font(Color.blue), "Hello world!"));
        lob = Lobs.frame3d(lob, null, Color.red, 1, 5, false, true);
        lob = Lobs.align(lob, .5f, .5f);
        lob = Lobs.request(lob, 400, 400, 400, 100, 100, 100);
        lob = Lobs.translate(lob, 100, 0);
        lob = Lobs.key(lob, "hello world");
        tray.add(lob);

	List elements = Lists.fromArray(new Object[] {
	    "Alpha", "Beta", "Gamma", "Eene", "Meene",
	    "Miste", "Es", "Rappelt", "In", "Der", 
	    "Kiste", "...Eene", "...Meene", "Muh..."});
	lob = Components.listBox(elements, Maps.map());
        lob = Lobs.request(lob, 400, 400, 400, 100, 100, 100);
	lob = Lobs.translate(lob, 100, 450);
	lob = Lobs.key(lob, "listbox");
	tray.add(lob);

        lob = Components.textArea(text, Maps.map());
        lob = Lobs.request(lob, 300, 300, 300, 200, 200, 200);
        lob = Lobs.translate(lob, 600, 100);
        lob = Lobs.key(lob, "textbox");
        tray.add(lob);

        return tray;
    }

    public static void main(String[] argv) {
	LobDemo demo = new LobDemo(new Color(1, 1, .8f));
	demo.start();
    }
}
