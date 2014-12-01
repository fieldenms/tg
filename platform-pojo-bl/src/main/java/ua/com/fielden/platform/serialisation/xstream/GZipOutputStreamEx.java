package ua.com.fielden.platform.serialisation.xstream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class enhances {@link GZIPOutputStream} to support to ability to pass in a desired compression level.
 * 
 * @author TG Team
 */
public class GZipOutputStreamEx extends GZIPOutputStream {

    /**
     * Creates a new output stream with the specified buffer size and compression level.
     * 
     * @param out
     * @param size
     * @param compressionLevel
     * @throws IOException
     */
    public GZipOutputStreamEx(final OutputStream out, final int size, final int compressionLevel) throws IOException {
        super(out, size);
        def.setLevel(compressionLevel);
    }

    /**
     * Creates a new output stream with a default buffer size and the specified compression level.
     * 
     * @param out
     * @param compressionLevel
     * @throws IOException
     */
    public GZipOutputStreamEx(final OutputStream out, final int compressionLevel) throws IOException {
        super(out);
        def.setLevel(compressionLevel);
    }
}
