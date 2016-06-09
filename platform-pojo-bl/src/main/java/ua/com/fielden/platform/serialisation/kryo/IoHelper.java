package ua.com.fielden.platform.serialisation.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * A helper class to simplify streaming and byte array/buffer operations.
 *
 * @author TG Team
 *
 */
public class IoHelper {
    public static final String ENTITY_REFERENCES = "entity-references";

    /* Block size that is read at once.*/
    private static final int READ_BLOCK = 8192;

    /**
     * Reads everything from the input stream using NIO and returns an array of bytes.
     *
     * @param source
     *            input stream
     * @return resultant byte array
     * @throws IOException
     *             by {@code Channel.read()}
     */
    public static byte[] readAsByteArray(final InputStream source) throws IOException {
        final ByteBuffer bb = readAsByteBuffer(source);
        final byte[] result = new byte[bb.limit()];
        bb.get(result);
        bb.clear();
        return result;
    }

    /**
     * Reads everything from the input stream using NIO and returns a byte buffer.
     *
     * @param source
     *            input stream
     * @return resultant byte buffer
     * @throws IOException
     */
    public static ByteBuffer readAsByteBuffer(final InputStream source) throws IOException {
        // create channel for input stream
        final ReadableByteChannel bc = Channels.newChannel(source);
        ByteBuffer bb = ByteBuffer.allocate(READ_BLOCK);
        while (bc.read(bb) != -1) { // read the data while it lasts in chunks defined by READ_BLOCK
            bb = resizeBuffer(bb); //resize the buffer if required to fit the data on the next read
        }
        bb.flip();
        return bb;
    }

    /**
     * A helper method to resize byte buffer upon read.
     *
     * @param in
     * @return
     */
    private static ByteBuffer resizeBuffer(final ByteBuffer in) {
        if (in.remaining() < READ_BLOCK) {
            // create new buffer with double capacity
            final ByteBuffer result = ByteBuffer.allocate(in.capacity() * 2);
            // flip the in buffer in preparation for it to be copied into the newly created larger buffer
            in.flip();
            // copy the in buffer into new buffer
            result.put(in);
            return result;
        }
        return in;
    }
}
