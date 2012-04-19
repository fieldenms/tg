package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.nio.ByteBuffer;
import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

/**
 * Serialises {@link DynamicallyTypedQueryContainer} instances with dynamic loading of associated types upon deserialisation
 * using a local instance of {@link DynamicEntityClassLoader}.
 *
 * @author TG Team
 *
 */
public class DynamicallyTypedQueryContainerSerialiser extends TgSimpleSerializer<DynamicallyTypedQueryContainer> {

    public DynamicallyTypedQueryContainerSerialiser(final TgKryo kryo) {
	super(kryo);
    }

    @Override
    public void write(final ByteBuffer buffer, final DynamicallyTypedQueryContainer data) {
	writeValue(buffer, data.getDynamicallyGeneratedTypes());
	writeValue(buffer, data.getQem());
    }

    @Override
    public DynamicallyTypedQueryContainer read(final ByteBuffer buffer) {
	// deserialise a list of binary representation of dynamically generated types
	final List<byte[]> binaryTypes = readValue(buffer, List.class);
	// load dynamically generated types from their binary representation before restoring query model
	final DynamicEntityClassLoader classLoader = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
	for (final byte[] binaryType : binaryTypes) {
	    classLoader.defineClass(binaryType);
	}

	// now we should be able to restore the query model
	final QueryExecutionModel<?, ?> qem = readValue(buffer, QueryExecutionModel.class);
	return new DynamicallyTypedQueryContainer(binaryTypes, qem);
    }

}
