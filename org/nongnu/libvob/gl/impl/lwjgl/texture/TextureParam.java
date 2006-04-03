package org.nongnu.libvob.gl.impl.lwjgl.texture;

import java.util.HashMap;

public class TextureParam extends HashMap{

    public TextureParam(String[] params) {
	super();
	for (int i = 0; i < params.length; i+=2) {
	    String key = params[i];
	    Float val = new Float(params[i+1]);
	    put(key, val);
	}
    }

    public float get(String key, float def) {
	if (!containsKey(key))
	    return def;
	else return ((Float)get(key)).floatValue();
    }

    public long getLong(String key, int i) {
	if (!containsKey(key))
	    return i;
	else return ((Float)get(key)).longValue();
    }

}
