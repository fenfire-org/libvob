// (c): Matti J. Katila

package org.nongnu.libvob.impl.terminal;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.awt.*;
import org.nongnu.libvob.util.*;

import java.io.*;
import java.util.*;
import java.awt.Dimension;

public class UnixTerminal implements ConsoleOperations {


    public static final short ARROW_START           = 27;
    public static final short ARROW_PREFIX          = 91;
    public static final short ARROW_LEFT            = 68;
    public static final short ARROW_RIGHT           = 67;
    public static final short ARROW_UP              = 65;
    public static final short ARROW_DOWN            = 66;

    private List events = new ArrayList();

    public boolean hasEvents() {
	synchronized(events) {
	    return !events.isEmpty(); 
	}
    }

    public int popEvent() {
	if (!hasEvents()) throw new Error("event queue is empty.");
	synchronized(events) {
	    return ((Integer)events.remove(0)).intValue();
	}
    }

    public void initializeTerminal ()
	throws IOException, InterruptedException {
	
        // save the initial tty configuration
	final String ttyConfig = stty ("-g");

	// sanity check
	if (ttyConfig.length () == 0
	    || (ttyConfig.indexOf ("=") == -1
		&& ttyConfig.indexOf (":") == -1))
	{
	    throw new IOException ("Unrecognized stty code: " + ttyConfig);
	}


	// set the console to be character-buffered instead of line-buffered
	stty ("-icanon min 1");

	// disable character echoing
	stty ("-echo");

	// at exit, restore the original tty configuration (for JDK 1.3+)
	try
	{
	    Runtime.getRuntime ().addShutdownHook (new Thread ()
		{
		    public void start ()
			{
			    try
			    {
				stty (ttyConfig);
			    }
			    catch (Exception e)
			    {
				consumeException (e);
			    }
			}
		});
	}
	catch (AbstractMethodError ame)
	{
	    // JDK 1.3+ only method. Bummer.
	    consumeException (ame);
	}

	new Thread(){
	    public void run() {
		while (true) {
		    try {
			Integer i = new Integer(readVirtualKey(System.in));
			synchronized(events) {
			    events.add(i);
			}
		    } catch (Exception e) { 
			e.printStackTrace(); 
		    }
		}
	    }
	}.start();
    }

    /**
     *  Read a single character from the input stream. This might
     *  enable a terminal implementation to better handle nuances of
     *  the console.
     */
    public int readCharacter (final InputStream in)
	throws IOException {
	return in.read ();
    }

    public int readVirtualKey (InputStream in)
	throws IOException {
	
	int c = readCharacter (in);

	// in Unix terminals, arrow keys are represented by
	// a sequence of 3 characters. E.g., the up arrow
	// key yields 27, 91, 68
	if (c == ARROW_START)
	{
	    System.out.println("arrow start");
	    c = readCharacter (in);
	    if (c == ARROW_PREFIX)
	    {
		System.out.println("arrow prefix");
		c = readCharacter (in);
		if (c == ARROW_UP)
		    return CTRL_P;
		else if (c == ARROW_DOWN)
		    return CTRL_N;
		else if (c == ARROW_LEFT)
		    return CTRL_B;
		else if (c == ARROW_RIGHT)
		    return CTRL_F;
	    }
	}
	
	
	return c;
    }


    /** 
     *  No-op for exceptions we want to silently consume.
     */
    private void consumeException (Throwable e) { }


    public boolean isSupported () {
	return true;
    }


    public boolean getEcho () {
	return false;
    }


    /**
     *	Returns the value of "stty size" width param.
     *
     *	<strong>Note</strong>: this method caches the value from the
     *	first time it is called in order to increase speed, which means
     *	that changing to size of the terminal will not be reflected
     *	in the console.
     */
    public Dimension getTerminalSize() {
	try
	{
	    String size = stty ("size");
	    if (size.length () != 0 && size.indexOf (" ") != -1)
	    {
		int width = Integer.parseInt (
		    size.substring (size.indexOf (" ") + 1));
		int height = Integer.parseInt (
		    size.substring (0, size.indexOf (" ")));
		return new Dimension (width, height);
	    }
	}
	catch (Exception e)
	{
	    consumeException (e);
	}
	return new Dimension(80,24);
    }




    /**
     *  Execute the stty command with the specified arguments
     *  against the current active terminal.
     */
    private static String stty (final String args)
	throws IOException, InterruptedException  {
	return exec ("stty " + args + " < /dev/tty").trim ();
    }


    /**
     *  Execute the specified command and return the output
     *  (both stdout and stderr).
     */
    private static String exec (final String cmd)
	throws IOException, InterruptedException {
	return exec (new String [] { "sh", "-c", cmd });
    }


    /**
     *  Execute the specified command and return the output
     *  (both stdout and stderr).
     */
    private static String exec (final String [] cmd)
	throws IOException, InterruptedException {
	ByteArrayOutputStream bout = new ByteArrayOutputStream ();
	
	Process p = Runtime.getRuntime ().exec (cmd);
	int c;
	InputStream in;
	
	in = p.getInputStream ();
	while ((c = in.read ()) != -1)
	    bout.write (c);
	
	in = p.getErrorStream ();
	while ((c = in.read ()) != -1)
	    bout.write (c);
	
	p.waitFor ();
	
	String result = new String (bout.toByteArray ());
	return result;
    }

}
