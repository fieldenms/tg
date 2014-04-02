package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;

/**
 * A serialiser for {@link Class} instances, which relies on the fact that all classes are registered with Kryo. So, it simply writes and reads a generated class ID.
 * 
 * @author TG Team
 * 
 */
public class ClassSerialiser extends SimpleSerializer<Class<?>> {

    private final Kryo kryo;

    public ClassSerialiser(final Kryo kryo) {
        this.kryo = kryo;
    }

    @Override
    public void write(final ByteBuffer buffer, final Class<?> clazz) {
        kryo.writeClass(buffer, clazz);
    }

    @Override
    public Class<?> read(final ByteBuffer buffer) {
        return kryo.readClass(buffer).getType();
    }

}
