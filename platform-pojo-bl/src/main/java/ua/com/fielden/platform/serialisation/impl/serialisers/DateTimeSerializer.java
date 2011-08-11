
package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.nio.ByteBuffer;

import org.joda.time.DateTime;

import com.esotericsoftware.kryo.serialize.LongSerializer;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;

/**
 * Serialises instances of {@link DateTime}.
 */
public class DateTimeSerializer extends SimpleSerializer<DateTime> {

    @Override
    public DateTime read(final ByteBuffer buffer) {
	return new DateTime(LongSerializer.get(buffer, true));
    }

    @Override
    public void write(final ByteBuffer buffer, final DateTime object) {
	LongSerializer.put(buffer, object.getMillis(), true);
    }
}
