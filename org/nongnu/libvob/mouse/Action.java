// (c): Matti J. Katila

package org.nongnu.libvob.mouse;
import org.nongnu.libvob.*;

/** Action that can be found from scene with mouse and performed.
 */
public class Action extends MouseMultiplexer {

    public static interface RequestHandler {
	Object handleRequest(Object request);
    }


    protected RequestHandler handl;
    public Action(RequestHandler handler) {
	handl = handler;
    }

    public Object request(Object request) {
	if (handl != null)
	    return handl.handleRequest(request);
 	return null;
    }

    public Action parent;
}
