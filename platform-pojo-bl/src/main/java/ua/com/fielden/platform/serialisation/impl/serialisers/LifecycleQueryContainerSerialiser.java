package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.nio.ByteBuffer;
import java.util.List;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleQueryContainer;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

public class LifecycleQueryContainerSerialiser extends TgSimpleSerializer<LifecycleQueryContainer> {

    public LifecycleQueryContainerSerialiser(final TgKryo kryo) {
	super(kryo);
    }

    @Override
    public void write(final ByteBuffer buffer, final LifecycleQueryContainer data) {
	writeValue(buffer, data.getBinaryTypes());
	writeValue(buffer, data.getModel());
	writeValue(buffer, data.getDistributionProperties());
	writeValue(buffer, data.getPropertyName());
	writeValue(buffer, data.getFrom());
	writeValue(buffer, data.getTo());
    }

    @SuppressWarnings("unchecked")
    @Override
    public LifecycleQueryContainer read(final ByteBuffer buffer) {
	// deserialise a list of binary representation of dynamically generated types
	final List<byte[]> binaryTypes = readValue(buffer, List.class);
	// load dynamically generated types from their binary representation before restoring query model
	final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
	for (final byte[] binaryType : binaryTypes) {
	    classLoader.defineClass(binaryType);
	}

	// now we should be able to restore the query model
	final EntityResultQueryModel<? extends AbstractEntity<?>> model = readValue(buffer, EntityResultQueryModel.class);
	final List<String> distributionProperties = readValue(buffer, List.class);
	final String propertyName = readValue(buffer, String.class);
	final DateTime from = readValue(buffer, DateTime.class);
	final DateTime to = readValue(buffer, DateTime.class);

	return new LifecycleQueryContainer(model, binaryTypes, distributionProperties, propertyName, from, to);
    }
}
