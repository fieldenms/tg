
package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.awt.Color;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.serialize.ByteSerializer;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;

/**
 * Serialises instances of {@link Color}.
 */
public class ColorSerializer extends SimpleSerializer<Color> {


    @Override
    public void write(final ByteBuffer buffer, final Color interval) {
	ByteSerializer.putUnsigned(buffer, interval.getRed());
	ByteSerializer.putUnsigned(buffer, interval.getGreen());
	ByteSerializer.putUnsigned(buffer, interval.getBlue());
	ByteSerializer.putUnsigned(buffer, interval.getAlpha());
    }

    @Override
    public Color read(final ByteBuffer buffer) {
	final int r = ByteSerializer.getUnsigned(buffer);
	final int g = ByteSerializer.getUnsigned(buffer);
	final int b = ByteSerializer.getUnsigned(buffer);
	final int a = ByteSerializer.getUnsigned(buffer);
	return new Color(r, g, b, a);
    }

}
