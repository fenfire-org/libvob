// (c): Matti J. Katila, and others worked on AWTScreen


package org.nongnu.libvob.impl.terminal;
import org.nongnu.libvob.impl.awt.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.*;
import java.io.*;
import java.util.*;
import java.awt.*;


/** A class that encapsulates ANSI character buffer.
 */
public class ANSIBuffer {

    static public boolean dbg = false;
    static private void p(String s) { System.out.println("ANSIBuffer:: "+s); }
    
    private Dimension size;
    private Color[] back, front;
    private char[] buff;

    static private HashMap 
        backMap = new HashMap(), 
	frontMap = new HashMap();

    {
	if (true || ! frontMap.isEmpty()) {
	    frontMap.put(Color.BLACK, FG_BLACK);
	    frontMap.put(Color.RED, FG_RED);
	    frontMap.put(Color.GREEN, FG_GREEN);
	    frontMap.put(Color.YELLOW, FG_YELLOW);
	    frontMap.put(Color.BLUE, FG_BLUE);
	    frontMap.put(Color.MAGENTA, FG_MAGENTA);
	    frontMap.put(Color.CYAN, FG_CYAN);
	    frontMap.put(Color.WHITE, FG_WHITE);

	    backMap.put(Color.BLACK, BG_BLACK);
	    backMap.put(Color.RED, BG_RED);
	    backMap.put(Color.GREEN, BG_GREEN);
	    backMap.put(Color.YELLOW, BG_YELLOW);
	    backMap.put(Color.BLUE, BG_BLUE);
	    backMap.put(Color.MAGENTA, BG_MAGENTA);
	    backMap.put(Color.CYAN, BG_CYAN);
	    backMap.put(Color.WHITE, BG_WHITE);
	}
    }
    
    static final String 
        ESC = "\u001b",
	CLEAR = ESC+"[0m",

	FG_BLACK = ESC+"[30m",
	FG_RED = ESC+"[31m",
	FG_GREEN = ESC+"[32m",
	FG_YELLOW = ESC+"[33m",
	FG_BLUE = ESC+"[34m",
	FG_MAGENTA = ESC+"[35m",
	FG_CYAN = ESC+"[36m",
	FG_WHITE = ESC+"[37m",

	BG_BLACK = ESC+"[40m",
	BG_RED = ESC+"[41m",
	BG_GREEN = ESC+"[42m",
	BG_YELLOW = ESC+"[43m",
	BG_BLUE = ESC+"[44m",
	BG_MAGENTA = ESC+"[45m",
	BG_CYAN = ESC+"[46m",
	BG_WHITE = ESC+"[47m";
    
    /* Some ANSI color codes:
     *
     * [30m  set foreground color to black
     * [31m  set foreground color to red
     * [32m  set foreground color to green
     * [33m  set foreground color to yellow
     * [34m  set foreground color to blue
     * [35m  set foreground color to magenta (purple)
     * [36m  set foreground color to cyan
     * [37m  set foreground color to white
     * [39m  set foreground color to default (white)
     *
     * [40m  set background color to black
     * [41m  set background color to red
     * [42m  set background color to green
     * [43m  set background color to yellow
     * [44m  set background color to blue
     * [45m  set background color to magenta (purple)
     * [46m  set background color to cyan
     * [47m  set background color to white
     * [49m  set background color to default (black)
     */



    public ANSIBuffer(Dimension size) {
	this.size = size;
	back = new Color[size.height*size.width];
	front = new Color[size.height*size.width];
	buff = new char[size.width*size.height];
    }

    public void set(int col, int row, Color fron, Color back, char ch) {
	System.out.println(col+":"+row+" "+ch);
	int ind = row*size.width + col;
	this.back[ind] = back;
	this.front[ind] = fron;
	buff[ind] = ch;
    }
    

    public void flush() {
	System.out.print(CLEAR);
	for (int i=0; i<buff.length; i++)
	{
	    System.out.print((String) backMap.get(back[i]));
	    System.out.print((String) frontMap.get(front[i]));
	    System.out.print(buff[i]);
	}
    }
}
