package ua.com.fielden.platform.serialisation.jackson;

import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;

import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;

/**
 * Serves as thread local storage for serializers. A serializer instance can be used by multiple threads simultaneously, so should be thread safe. This class provides scratch
 * buffers and object storage that serializers can use to remain thread safe.
 *
 * @see Kryo#getContext()
 * @author Nathan Sweet <misc@n4te.com>
 */
public class JacksonContext {
    private HashMap<Object, Object> map;
    private HashMap<Object, Object> tempMap;

    /**
     * Stores an object in thread local storage. This allows serializers to easily make repeated use of objects that are not thread safe.
     */
    public void put(final String key, final Object value) {
        if (map == null) {
            map = new HashMap();
        }
        map.put(key, value);
    }

    /**
     * Returns an object from thread local storage, or null.
     *
     * @see #put(Serializer, String, Object)
     */
    public Object get(final String key) {
        if (map == null) {
            map = new HashMap();
        }
        return map.get(key);
    }

    /**
     * Stores a temporary object in thread local storage. This allows serializers to easily make repeated use of objects that are not thread safe. The object will be removed after
     * when the entire object graph has been serialized or deserialized.
     */
    public void putTemp(final String key, final Object value) {
        if (tempMap == null) {
            tempMap = new HashMap();
        }
        tempMap.put(key, value);
    }

    /**
     * Returns a temporary object from thread local storage, or null.
     *
     * @see #put(Serializer, String, Object)
     */
    public Object getTemp(final String key) {
        if (tempMap == null) {
            tempMap = new HashMap();
        }
        return tempMap.get(key);
    }

    /**
     * Clears temporary values that are only needed for serialization or deserialization per object graph. When using the {@link Kryo} read and write methods, the context is
     * automatically reset after an entire object graph is serialized or deserialized.
     */
    public void reset() {
        if (tempMap != null) {
            tempMap.clear();
        }
        if (TRACE) {
            trace("kryo", "Context reset.");
        }
    }
}
