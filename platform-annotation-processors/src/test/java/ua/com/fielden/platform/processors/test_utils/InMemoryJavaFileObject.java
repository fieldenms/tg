package ua.com.fielden.platform.processors.test_utils;

import com.google.common.io.ByteSource;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Implementation of {@link JavaFileObject} that is stored in memory.
 * <p>
 * Based on {@link com.google.testing.compile.InMemoryJavaFileManager.InMemoryJavaFileObject}.
 * 
 * @author TG Team
 */
public class InMemoryJavaFileObject extends SimpleJavaFileObject {
    private long lastModified = 0L;
    private Optional<ByteSource> data = Optional.empty();

    InMemoryJavaFileObject(final URI uri, final Kind kind) {
        super(uri, kind);
    }

    InMemoryJavaFileObject(final URI uri, final Kind kind, final String source) {
        super(uri, kind);
        this.data = Optional.of(ByteSource.wrap(source.getBytes()));
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        if (data.isPresent()) {
            return data.get().openStream();
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                data = Optional.of(ByteSource.wrap(toByteArray()));
                lastModified = System.currentTimeMillis();
            }
        };
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        if (data.isPresent()) {
            return data.get().asCharSource(Charset.defaultCharset()).openStream();
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        if (data.isPresent()) {
            return data.get().asCharSource(Charset.defaultCharset()).read();
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public Writer openWriter() throws IOException {
        return new StringWriter() {
            @Override
            public void close() throws IOException {
                super.close();
                data = Optional.of(ByteSource.wrap(toString().getBytes(Charset.defaultCharset())));
                lastModified = System.currentTimeMillis();
            }
        };
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean delete() {
        this.data = Optional.empty();
        this.lastModified = 0L;
        return true;
    }

    @Override
    public String toString() {
        return format("%s{uri=%s, kind=%s}", this.getClass().getSimpleName(), toUri(), kind);
    }
}
