package ua.com.fielden.platform.serialisation.kryo.serialisers;

import java.util.IdentityHashMap;

import com.esotericsoftware.kryo.util.IntHashMap;

/**
 * Class representing references to instances already serialised or deserialised.
 * 
 * It is used mainly as a cache to resolve circular references during serialisation and provide better performance during deserialisation.
 * 
 * @author TG Team
 * 
 */
public class References {
    public final IdentityHashMap<Object, Integer> objectToReference = new IdentityHashMap();
    public final IntHashMap referenceToObject = new IntHashMap();
    public int referenceCount = 1;

    public void reset() {
        objectToReference.clear();
        referenceToObject.clear();
        referenceCount = 1;
    }
}
