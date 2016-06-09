package ua.com.fielden.platform.serialisation.kryo.serialisers;

import java.nio.ByteBuffer;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import com.esotericsoftware.kryo.serialize.StringSerializer;

/**
 * Serialises {@link PropertyDescriptor} instances.
 * 
 * @author TG Team
 * 
 */
public class ProperyDescriptorSerialiser extends SimpleSerializer<PropertyDescriptor> {

    private final EntityFactory factory;

    public ProperyDescriptorSerialiser(final EntityFactory factory) {
        this.factory = factory;
    }

    @Override
    public void write(final ByteBuffer buffer, final PropertyDescriptor descriptor) {
        StringSerializer.put(buffer, descriptor.toString());
    }

    @Override
    public PropertyDescriptor read(final ByteBuffer buffer) {
        final String str = StringSerializer.get(buffer);
        try {
            return PropertyDescriptor.fromString(str, factory);
        } catch (final Exception e) {
            throw new SerializationException("Could not create from string representation \"" + str + "\"");
        }
    }

}
