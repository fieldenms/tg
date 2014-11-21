package ua.com.fielden.platform.serialisation.xstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

/**
 * A base class for XML serialisation of class instances, which is based on {@link XStream}.
 * <p>
 * Provides custom toXml conversion and supports a converter for {@link AbstractEntity} descendants out of the box.
 * 
 * @author TG Team
 * 
 */
public abstract class XStreamSerialiser extends XStream implements ISerialiser {
    private final boolean compact;

    protected XStreamSerialiser(final boolean compact) {
        alias("r", Result.class);
        alias("w", Warning.class);
        alias("q", QueryExecutionModel.class);
        this.compact = compact;
        registerConverter(new ResultConverter());
        registerConverter(new DateConverter());
        registerConverter(new MoneyConverter());
    }

    @Override
    public String toXML(final Object obj) {
        if (!compact) {
            return super.toXML(obj);
        } else {
            final Writer writer = new StringWriter();
            try {
                marshal(obj, new CompactWriter(writer));
            } finally {
                try {
                    writer.flush();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            return writer.toString();
        }
    }

    @Override
    public byte[] serialise(final Object obj) {
        try {
            // every entity type is represented as e at both server and client sides
            if (AbstractEntity.class.isAssignableFrom(obj.getClass())) {
                alias("e", obj.getClass()); // ((AbstractEntity)obj).getType()
            }
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final GZipOutputStreamEx zOut = new GZipOutputStreamEx(output, Deflater.BEST_COMPRESSION);
            toXML(obj, new BufferedWriter(new OutputStreamWriter(zOut, "UTF-8")));
            zOut.flush();
            zOut.close();
            output.flush();
            output.close();
            return output.toByteArray();
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) throws Exception {
        return deserialise(new ByteArrayInputStream(content), type);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) throws Exception {
        // every entity type is represented as e at both server and client sides
        if (AbstractEntity.class.isAssignableFrom(type)) {
            alias("e", type);
        }
        final GZIPInputStream zIn = new GZIPInputStream(content);
        return type.cast(fromXML(new BufferedReader(new InputStreamReader(zIn, "UTF-8"))));
    }

}
