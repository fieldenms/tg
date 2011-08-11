package ua.com.fielden.platform.serialisation.impl.serialisers.not_used;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import ua.com.fielden.platform.equery.tokens.properties.AbstractQueryProperty;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

import com.esotericsoftware.kryo.SerializationException;

/**
 * Serialises {@link SearchProperty} instances.
 * 
 * @author TG Team
 * 
 */
public class SearchPropertySerialiser extends TgSimpleSerializer<SearchProperty> {

    private final Field expressionField;

    public SearchPropertySerialiser(final TgKryo kryo) {
	super(kryo);
	try {
	    expressionField = AbstractQueryProperty.class.getDeclaredField("expression");
	    expressionField.setAccessible(true);
	} catch (final Exception e) {
	    throw new SerializationException("Could not obtain fields for SearchProperty type.");
	}
    }

    @Override
    public void write(final ByteBuffer buffer, final SearchProperty value) {
	try {
	    writeValue(buffer, expressionField.get(value));
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not write SearchProperty: " + e.getMessage());
	}
    }

    @Override
    public SearchProperty read(final ByteBuffer buffer) {
	try {
	    final SearchProperty sp = new SearchProperty();
	    expressionField.set(sp, readValue(buffer, ArrayList.class));
	    return sp;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not read SearchProperty: " + e.getMessage());
	}
    }

}
