package ua.com.fielden.platform.serialisation.impl.serialisers.not_used;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import ua.com.fielden.platform.equery.tokens.main.Select;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

import com.esotericsoftware.kryo.SerializationException;

/**
 * Serialises {@link Select} instances.
 * 
 * @author TG Team
 * 
 */
public class SelectSerialiser extends TgSimpleSerializer<Select> {

    private final Field selectCalculatedPropsField;

    public SelectSerialiser(final TgKryo kryo) {
	super(kryo);
	try {
	    selectCalculatedPropsField = Select.class.getDeclaredField("selectCalculatedProps");
	    selectCalculatedPropsField.setAccessible(true);
	} catch (final Exception e) {
	    throw new SerializationException("Could not obtain fields for Select type.");
	}
    }

    @Override
    public void write(final ByteBuffer buffer, final Select value) {
	try {
	    writeValue(buffer, selectCalculatedPropsField.get(value));
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not write Select: " + e.getMessage());
	}
    }

    @Override
    public Select read(final ByteBuffer buffer) {
	try {
	    final Select select = new Select();
	    selectCalculatedPropsField.set(select, readValue(buffer, ArrayList.class));
	    return select;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not read Select: " + e.getMessage());
	}
    }

}
