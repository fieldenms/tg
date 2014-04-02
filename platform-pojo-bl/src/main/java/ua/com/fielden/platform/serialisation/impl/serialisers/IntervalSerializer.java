package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.nio.ByteBuffer;

import org.joda.time.Interval;

import com.esotericsoftware.kryo.serialize.LongSerializer;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;

/**
 * Serialises instances of {@link Interval}.
 */
public class IntervalSerializer extends SimpleSerializer<Interval> {

    @Override
    public void write(final ByteBuffer buffer, final Interval interval) {
        LongSerializer.put(buffer, interval.getStartMillis(), true);
        LongSerializer.put(buffer, interval.getEndMillis(), true);
    }

    @Override
    public Interval read(final ByteBuffer buffer) {
        final Long start = LongSerializer.get(buffer, true);
        final Long end = LongSerializer.get(buffer, true);
        return new Interval(start, end);
    }

}
